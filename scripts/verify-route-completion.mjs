import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';

const base = process.env.ROUTE_TEST_BASE_URL ?? 'http://localhost:8082';
const results = [];
const startedAt = new Date().toISOString();

async function api(endpoint, body) {
  const response = await fetch(base + endpoint, {
    method: body === undefined ? 'GET' : 'POST',
    headers: {
      Accept: 'application/json',
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(120000),
  });
  const text = await response.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch {
    throw new Error(`Non-JSON response: HTTP ${response.status}`);
  }
  return { status: response.status, json };
}

async function success(endpoint, body) {
  const response = await api(endpoint, body);
  assert.equal(response.status, 200, JSON.stringify(response.json));
  assert.equal(response.json.success, true);
  assert.notEqual(response.json.data, null);
  return response.json.data;
}

function coordinate(value) {
  assert.equal(typeof value.latitude, 'number');
  assert.equal(typeof value.longitude, 'number');
  return { latitude: value.latitude, longitude: value.longitude };
}

function xy(value) {
  return [value.longitude, value.latitude];
}

function same(a, b) {
  return a.length === 2 && b.length === 2 &&
    Math.abs(a[0] - b[0]) <= 1e-9 &&
    Math.abs(a[1] - b[1]) <= 1e-9;
}

function checkRoute(route, request) {
  assert.ok(Array.isArray(route.segments));
  let previous = xy(request.origin);
  let distance = 0;
  let duration = 0;
  const boundaries = [previous];

  route.segments.forEach((segment, index) => {
    assert.equal(segment.sequence, index + 1);
    assert.ok(['WALKING', 'DRIVING'].includes(segment.mode));
    assert.ok(Number.isInteger(segment.distanceMeters) && segment.distanceMeters >= 0);
    assert.ok(Number.isInteger(segment.durationSeconds) && segment.durationSeconds >= 0);
    assert.ok(Array.isArray(segment.coordinates) && segment.coordinates.length >= 2);

    for (const point of segment.coordinates) {
      assert.equal(point.length, 2);
      assert.ok(point.every(Number.isFinite));
      assert.ok(Math.abs(point[0]) <= 180 && Math.abs(point[1]) <= 90);
    }

    assert.ok(same(previous, segment.coordinates[0]), 'Disconnected segments');
    previous = segment.coordinates.at(-1);
    boundaries.push(previous);
    distance += segment.distanceMeters;
    duration += segment.durationSeconds;
  });

  assert.ok(same(previous, xy(request.destination)), 'Destination mismatch');
  assert.equal(distance, route.totalDistanceMeters);
  assert.equal(duration, route.totalDurationSeconds);
  return boundaries;
}

async function parkingNear(latitude, longitude) {
  const list = await success(
    `/api/parking-lots/nearby?latitude=${latitude}&longitude=${longitude}&radius=100`
  );
  const matches = list.filter(p =>
    Math.abs(p.latitude - latitude) < 1e-6 &&
    Math.abs(p.longitude - longitude) < 1e-6
  );
  assert.equal(matches.length, 1, 'Expected exactly one parking lot at fixture coordinates');
  return matches[0];
}

async function tmapLeg(mode, start, end) {
  if (same(xy(start), xy(end))) {
    return {
      skippedSamePoint: true,
      distanceMeters: 0,
      durationSeconds: 0,
      coordinates: [],
    };
  }
  const params = new URLSearchParams({
    startLat: String(start.latitude),
    startLng: String(start.longitude),
    endLat: String(end.latitude),
    endLng: String(end.longitude),
  });
  const result = await success(`/api/routes/tmap/${mode}?${params}`);
  assert.ok(Number.isFinite(result.distanceMeters) && result.distanceMeters >= 0);
  assert.ok(Number.isFinite(result.durationSeconds) && result.durationSeconds >= 0);
  assert.ok(Array.isArray(result.coordinates) && result.coordinates.length >= 2);
  return result;
}

async function record(name, fn) {
  try {
    const details = await fn();
    results.push({ name, passed: true, details });
    console.log(`PASS ${name}`);
  } catch (error) {
    results.push({ name, passed: false, error: String(error.message) });
    console.error(`FAIL ${name}: ${error.message}`);
  }
}

function save() {
  const root = process.cwd();
  const outputDirectory = path.join(root, 'build', 'route-completion');
  const backupDirectory = path.resolve(root, '..', 'route-comparison-results');
  const filename = `completion-${new Date().toISOString().replace(/[:.]/g, '-')}.json`;

  const report = {
    startedAt,
    finishedAt: new Date().toISOString(),
    gitHead: execFileSync('git', ['rev-parse', 'HEAD'], { encoding: 'utf8' }).trim(),
    workingTreeDirty: !!execFileSync('git', ['status', '--porcelain'], {
      encoding: 'utf8',
    }).trim(),
    limitations: [
      'TMAP differences are observations, not accuracy pass/fail thresholds.',
      'Coordinates for departure POIs are fixed verification inputs.',
      'TMAP calls are sequential and may reflect different traffic conditions.',
      'Recommendation fails entirely if any candidate route fails.',
    ],
    results,
  };

  for (const directory of [outputDirectory, backupDirectory]) {
    fs.mkdirSync(directory, { recursive: true });
    fs.writeFileSync(path.join(directory, filename), JSON.stringify(report, null, 2));
  }

  console.table(results.map(({ name, passed }) => ({ name, passed })));
  console.log(`REPORT ${path.join(backupDirectory, filename)}`);

  if (results.some(result => !result.passed)) process.exitCode = 1;
}

try {
  const sinbanghwa = await parkingNear(37.5667867, 126.8177876);
  const guam = await parkingNear(37.561217, 126.844636);

  const origin = { latitude: 37.5586, longitude: 126.8377 };
  const poi = { latitude: 37.5585, longitude: 126.8376 };
  const destination = { latitude: 37.5673, longitude: 126.8270 };

  const scenarios = [
    {
      name: 'normal-without-first-walk',
      parking: sinbanghwa,
      request: {
        origin: poi,
        departurePoi: poi,
        parkingId: sinbanghwa.id,
        destination,
      },
    },
    {
      name: 'normal-different-parking',
      parking: guam,
      request: {
        origin,
        departurePoi: poi,
        parkingId: guam.id,
        destination: origin,
      },
    },
  ];

  for (const scenario of scenarios) {
    await record(scenario.name, async () => {
      const request = scenario.request;
      const route = await success('/api/routes/all', request);
      const boundaries = checkRoute(route, request);

      assert.ok(route.segments.some(s => s.mode === 'DRIVING'));
      assert.ok(boundaries.some(p => same(p, xy(request.departurePoi))));
      assert.ok(boundaries.some(p => same(p, xy(scenario.parking))));

      const legs = [
        await tmapLeg('walking', request.origin, request.departurePoi),
        await tmapLeg('driving', request.departurePoi, coordinate(scenario.parking)),
        await tmapLeg('walking', coordinate(scenario.parking), request.destination),
      ];

      const tmapDistance = legs.reduce((sum, leg) => sum + leg.distanceMeters, 0);
      const tmapDuration = legs.reduce((sum, leg) => sum + leg.durationSeconds, 0);

      return {
        request,
        parking: scenario.parking,
        integrated: route,
        tmap: legs,
        summary: {
          integratedDistanceMeters: route.totalDistanceMeters,
          tmapDistanceMeters: tmapDistance,
          integratedDurationSeconds: route.totalDurationSeconds,
          tmapDurationSeconds: tmapDuration,
          distanceDifferenceMeters: route.totalDistanceMeters - tmapDistance,
          durationDifferenceSeconds: route.totalDurationSeconds - tmapDuration,
        },
      };
    });
  }

  const baseRequest = {
    origin,
    departurePoi: poi,
    parkingId: sinbanghwa.id,
    destination,
  };

  for (const side of ['origin', 'destination']) {
    await record(`network-outside-${side}`, async () => {
      const request = {
        ...baseRequest,
        [side]: { latitude: 37.4979, longitude: 127.0276 },
      };
      const response = await api('/api/routes/all', request);
      assert.equal(response.status, 400);
      assert.equal(response.json.success, false);
      assert.equal(response.json.code, 'WALKING_ROUTE_400_001');
      assert.equal(response.json.data, null);
      return { request, response };
    });
  }

  await record('recommendation-real-database', async () => {
    const request = {
      origin,
      departurePoi: poi,
      destination: coordinate(sinbanghwa),
      radius: 500,
      limit: 3,
    };

    const candidates = await success(
      `/api/parking-lots/nearby?latitude=${request.destination.latitude}` +
      `&longitude=${request.destination.longitude}&radius=${request.radius}`
    );
    assert.ok(candidates.length > 0);
    assert.ok(candidates.length <= 50);

    const expected = [];
    for (const candidate of candidates) {
      const routeRequest = {
        origin: request.origin,
        departurePoi: request.departurePoi,
        parkingId: candidate.id,
        destination: request.destination,
      };
      const route = await success('/api/routes/all', routeRequest);
      checkRoute(route, routeRequest);
      expected.push({
        parkingId: candidate.id,
        totalDistanceMeters: route.totalDistanceMeters,
        totalDurationSeconds: route.totalDurationSeconds,
      });
    }

    expected.sort((a, b) =>
      a.totalDurationSeconds - b.totalDurationSeconds ||
      a.totalDistanceMeters - b.totalDistanceMeters ||
      a.parkingId - b.parkingId
    );

    const started = performance.now();
    const response = await success('/api/routes/recommendations', request);
    const elapsedMilliseconds = Math.round(performance.now() - started);

    assert.equal(response.candidateCount, candidates.length);
    assert.equal(response.evaluatedCount, candidates.length);
    assert.equal(response.recommendations.length, Math.min(3, candidates.length));

    response.recommendations.forEach((item, index) => {
      assert.equal(item.rank, index + 1);
      assert.equal(item.parkingId, expected[index].parkingId);
      assert.equal(item.totalDistanceMeters, expected[index].totalDistanceMeters);
      assert.equal(item.totalDurationSeconds, expected[index].totalDurationSeconds);
    });

    return { request, candidates, expected, response, elapsedMilliseconds };
  });
} catch (error) {
  results.push({ name: 'setup', passed: false, error: String(error.message) });
} finally {
  save();
}
