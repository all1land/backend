import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';

const base = process.env.ROUTE_TEST_BASE_URL ?? 'http://localhost:8082';
const request = {
  origin: { latitude: 37.5586, longitude: 126.8377 },
  departurePoi: { latitude: 37.5585, longitude: 126.8376 },
  destination: { latitude: 37.561217, longitude: 126.844636 },
  radius: 500,
  limit: 1,
};

const report = {
  startedAt: new Date().toISOString(),
  base,
  gitHead: execFileSync('git', ['rev-parse', 'HEAD'], {
    encoding: 'utf8',
  }).trim(),
  workingTreeDirty: !!execFileSync('git', ['status', '--porcelain'], {
    encoding: 'utf8',
  }).trim(),
  request,
  passed: false,
  candidates: [],
  expected: [],
};

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
  assert.equal(response.status, 200, `${endpoint}: ${text}`);
  const json = JSON.parse(text);
  assert.equal(json.success, true, text);
  assert.ok(json.data !== null && json.data !== undefined);
  return json.data;
}

try {
  const query = new URLSearchParams({
    latitude: String(request.destination.latitude),
    longitude: String(request.destination.longitude),
    radius: String(request.radius),
  });

  report.candidates = await api(`/api/parking-lots/nearby?${query}`);
  assert.ok(Array.isArray(report.candidates));
  assert.ok(report.candidates.length >= 2, 'At least two candidates are required');
  assert.ok(report.candidates.length <= 50, 'Too many candidates');
  assert.equal(
    new Set(report.candidates.map(p => p.id)).size,
    report.candidates.length,
    'Duplicate parking IDs',
  );

  console.log(`Candidates: ${report.candidates.length}`);

  for (const parking of report.candidates) {
    const route = await api('/api/routes/integrated/coordinates', {
      origin: request.origin,
      departurePoi: request.departurePoi,
      parkingId: parking.id,
      destination: request.destination,
    });

    assert.ok(Array.isArray(route.segments));
    for (const key of ['totalDistanceMeters', 'totalDurationSeconds']) {
      assert.ok(Number.isFinite(route[key]) && route[key] >= 0);
    }

    assert.equal(
      route.totalDistanceMeters,
      route.segments.reduce((sum, s) => sum + s.distanceMeters, 0),
    );
    assert.equal(
      route.totalDurationSeconds,
      route.segments.reduce((sum, s) => sum + s.durationSeconds, 0),
    );

    report.expected.push({
      parkingId: parking.id,
      name: parking.name,
      totalDistanceMeters: route.totalDistanceMeters,
      totalDurationSeconds: route.totalDurationSeconds,
    });

    console.log(`Evaluated parking ID: ${parking.id}`);
  }

  report.expected.sort((a, b) =>
    a.totalDurationSeconds - b.totalDurationSeconds ||
    a.totalDistanceMeters - b.totalDistanceMeters ||
    a.parkingId - b.parkingId
  );

  const started = performance.now();
  report.response = await api('/api/routes/integrated/recommendations', request);
  report.elapsedMilliseconds = Math.round(performance.now() - started);

  assert.equal(report.response.candidateCount, report.candidates.length);
  assert.equal(report.response.evaluatedCount, report.candidates.length);
  assert.equal(report.response.recommendations.length, 1);

  const actual = report.response.recommendations[0];
  const expected = report.expected[0];

  assert.equal(actual.rank, 1);
  for (const key of [
    'parkingId', 'name', 'totalDistanceMeters', 'totalDurationSeconds',
  ]) {
    assert.equal(actual[key], expected[key], `Mismatch: ${key}`);
  }

  report.passed = true;
  console.table(report.expected);
  console.table(report.response.recommendations);
  console.log(`Recommendation elapsed: ${report.elapsedMilliseconds} ms`);
  console.log('PASS multi-candidate recommendation');
} catch (error) {
  report.error = error.stack ?? String(error);
  console.error(`FAIL: ${error.message}`);
  process.exitCode = 1;
} finally {
  report.finishedAt = new Date().toISOString();
  const filename = `multi-parking-${report.finishedAt.replace(/[:.]/g, '-')}.json`;

  const directories = [
    path.resolve('build/route-completion'),
    path.resolve('..', 'route-comparison-results'),
  ];

  for (const directory of directories) {
    fs.mkdirSync(directory, { recursive: true });
    fs.writeFileSync(
      path.join(directory, filename),
      JSON.stringify(report, null, 2),
      'utf8',
    );
  }

  console.log(`REPORT ${path.join(directories[1], filename)}`);
}