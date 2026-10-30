param(
    [string]$BaseUrl = "http://localhost:8082"
)

$ErrorActionPreference = "Stop"
$utf8 = [System.Text.UTF8Encoding]::new($false)
$BaseUrl = $BaseUrl.TrimEnd("/")
$startedAt = [DateTimeOffset]::Now.ToString("o")

function Invoke-RouteApi {
    param(
        [string]$Path,
        [object]$Body = $null
    )

    $responseFile = [System.IO.Path]::GetTempFileName()
    $requestFile = $null

    try {
        $arguments = @(
            "--fail-with-body"
            "--silent"
            "--show-error"
            "--max-time"
            "120"
            "--header"
            "Accept: application/json"
            "--output"
            $responseFile
        )

        if ($null -ne $Body) {
            $requestFile = [System.IO.Path]::GetTempFileName()

            [System.IO.File]::WriteAllText(
                $requestFile,
                ($Body | ConvertTo-Json -Depth 20),
                [System.Text.UTF8Encoding]::new($false)
            )

            $arguments += @(
                "--header"
                "Content-Type: application/json"
                "--data-binary"
                "@$requestFile"
            )
        }

        $arguments += "$BaseUrl$Path"

        & curl.exe @arguments
        $exitCode = $LASTEXITCODE

        $text = [System.IO.File]::ReadAllText(
            $responseFile,
            [System.Text.Encoding]::UTF8
        )

        if ($exitCode -ne 0) {
            throw "API request failed: $Path`n$text"
        }

        $response = $text | ConvertFrom-Json

        if ($response.success -ne $true -or $null -eq $response.data) {
            throw "API returned an unsuccessful or empty response: $Path"
        }

        return $response
    }
    finally {
        Remove-Item -LiteralPath $responseFile -ErrorAction SilentlyContinue

        if ($requestFile) {
            Remove-Item -LiteralPath $requestFile -ErrorAction SilentlyContinue
        }
    }
}

function Get-TmapLeg {
    param(
        [string]$Mode,
        [object]$Start,
        [object]$End
    )

    $culture = [System.Globalization.CultureInfo]::InvariantCulture
    $startLat = ([double]$Start.latitude).ToString("R", $culture)
    $startLng = ([double]$Start.longitude).ToString("R", $culture)
    $endLat = ([double]$End.latitude).ToString("R", $culture)
    $endLng = ([double]$End.longitude).ToString("R", $culture)

    $path = "/api/routes/tmap/${Mode}?startLat=$startLat&startLng=$startLng&endLat=$endLat&endLng=$endLng"
    $response = Invoke-RouteApi -Path $path

    if (
        $null -eq $response.data.distanceMeters -or
        $null -eq $response.data.durationSeconds -or
        $response.data.distanceMeters -lt 0 -or
        $response.data.durationSeconds -lt 0 -or
        @($response.data.coordinates).Count -lt 2
    ) {
        throw "Invalid TMAP route response: $Mode"
    }

    return $response
}

$nearby = Invoke-RouteApi -Path (
    "/api/parking-lots/nearby" +
    "?latitude=37.5667867&longitude=126.8177876&radius=100"
)

# Locate the seeded parking lot by its known coordinates.
$parking = $nearby.data |
    Where-Object {
        [Math]::Abs([double]$_.latitude - 37.5667867) -lt 0.000001 -and
        [Math]::Abs([double]$_.longitude - 126.8177876) -lt 0.000001
    } |
    Select-Object -First 1

if ($null -eq $parking -or $null -eq $parking.id) {
    throw "The expected parking lot was not found."
}

$request = [ordered]@{
    origin = @{
        latitude = 37.5586
        longitude = 126.8377
    }
    departurePoi = @{
        latitude = 37.5585
        longitude = 126.8376
    }
    parkingId = [long]$parking.id
    destination = @{
        latitude = 37.5673
        longitude = 126.8270
    }
}

$parkingCoordinate = @{
    latitude = [double]$parking.latitude
    longitude = [double]$parking.longitude
}

$integrated = Invoke-RouteApi `
    -Path "/api/routes/all" `
    -Body $request

$segments = @($integrated.data.segments)

if (
    $segments.Count -eq 0 -or
    $null -eq $integrated.data.totalDistanceMeters -or
    $null -eq $integrated.data.totalDurationSeconds
) {
    throw "The integrated route is missing required fields."
}

$distanceSum = ($segments | Measure-Object distanceMeters -Sum).Sum
$durationSum = ($segments | Measure-Object durationSeconds -Sum).Sum

if (
    $distanceSum -ne $integrated.data.totalDistanceMeters -or
    $durationSum -ne $integrated.data.totalDurationSeconds
) {
    throw "Integrated route totals do not match segment sums."
}

$firstWalking = Get-TmapLeg `
    -Mode "walking" `
    -Start $request.origin `
    -End $request.departurePoi

$driving = Get-TmapLeg `
    -Mode "driving" `
    -Start $request.departurePoi `
    -End $parkingCoordinate

$lastWalking = Get-TmapLeg `
    -Mode "walking" `
    -Start $parkingCoordinate `
    -End $request.destination

$tmapDistance = [long]$firstWalking.data.distanceMeters +
    [long]$driving.data.distanceMeters +
    [long]$lastWalking.data.distanceMeters

$tmapDuration = [long]$firstWalking.data.durationSeconds +
    [long]$driving.data.durationSeconds +
    [long]$lastWalking.data.durationSeconds

$summary = [ordered]@{
    integratedDistanceMeters = [long]$distanceSum
    tmapDistanceMeters = $tmapDistance
    distanceDifferenceMeters = [long]$distanceSum - $tmapDistance
    integratedDurationSeconds = [long]$durationSum
    tmapDurationSeconds = $tmapDuration
    durationDifferenceSeconds = [long]$durationSum - $tmapDuration
    integratedSegmentCount = $segments.Count
}

$commit = git rev-parse HEAD

if ($LASTEXITCODE -ne 0) {
    throw "Unable to identify the Git commit."
}

$changes = @(git status --porcelain)

if ($LASTEXITCODE -ne 0) {
    throw "Unable to inspect the working tree."
}

$report = [ordered]@{
    caseId = "balsan-sinbanghwa-magonaru"
    startedAt = $startedAt
    finishedAt = [DateTimeOffset]::Now.ToString("o")
    gitHead = "$commit".Trim()
    workingTreeDirty = ($changes.Count -gt 0)
    request = $request
    parkingCoordinate = $parkingCoordinate
    comparisonType = "same-waypoints-service-output-comparison"
    limitations = @(
        "Integrated driving includes walking access segments."
        "TMAP driving may use a different road access model."
        "Integrated driving excludes live traffic and signal waiting."
        "Numeric differences are observations, not pass/fail thresholds."
        "This report covers one fixed scenario only."
    )
    summary = $summary
    integrated = $integrated
    tmap = [ordered]@{
        firstWalking = $firstWalking
        driving = $driving
        lastWalking = $lastWalking
    }
}

$outputDirectory = Join-Path $PSScriptRoot "../build/route-comparison"
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

$outputPath = Join-Path $outputDirectory (
    "comparison-" + (Get-Date -Format "yyyyMMdd-HHmmss-fff") + ".json"
)

[System.IO.File]::WriteAllText(
    $outputPath,
    ($report | ConvertTo-Json -Depth 100),
    $utf8
)

[pscustomobject]$summary | Format-List

Write-Host "Comparison report:"
Write-Host ([System.IO.Path]::GetFullPath($outputPath))