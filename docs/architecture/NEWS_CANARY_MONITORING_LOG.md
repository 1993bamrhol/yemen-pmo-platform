# NEWS Canary Monitoring Log

> **Status:** Stopped by owner request on `2026-08-26`; scheduled automation deleted
> **Canary start:** `2026-08-24T14:02:24Z`
> **Earliest exit review:** `2026-08-25T14:02:24Z`, and only after at least 100 NEWS unified compatibility requests
> **Rule:** Monitor NEWS only. Do not change flags, perform rollback, declare success, or enable another content type without explicit approval.

Baseline: `NEWS_CANARY_BASELINE.md`

## Observations

### 2026-08-24T14:02:24Z — Activation smoke

- Health: `UP`.
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`.
- Isolation: ANNOUNCEMENT, DECISION, and DOCUMENT configured `false`, effective source `LEGACY`.
- NEWS unified request count: 3.
- Automatic fallbacks: 0; fallback reasons: none.
- Contracts: list 200, detail 200, missing id 404, portal home 200; all three content hashes matched the baseline.
- Shadow: 12/12 mapped, zero differences; NEWS count/order/field parity true; portal-home projection ready.
- Security: direct unified public read 404; anonymous metrics 401.
- Latency sample: not captured during activation smoke; scheduled observations use client-observed route latency.
- Anomalies: none.

### 2026-08-24T14:28:44Z — Monitoring cycle 1

- Health: `UP`.
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; every other content type remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 8.
- Automatic fallbacks: 0; fallback reasons: none.
- List probes: 200/200, hashes matched; latencies 164.002ms and 28.432ms.
- Detail probes: 200/200, hashes matched; latencies 32.852ms and 31.254ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 38.998ms.
- Missing id: 404.
- Shadow: 12/12 mapped, zero differences, globally ready; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T15:26:19Z — Monitoring cycle 2

- Health: `UP`.
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; every other content type remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 13.
- Automatic fallbacks: 0; fallback reasons: none.
- List probes: 200/200, hashes matched; latencies 423.843ms and 48.668ms.
- Detail probes: 200/200, hashes matched; latencies 40.304ms and 40.486ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 44.244ms.
- Missing id: 404.
- Shadow: 12/12 mapped, zero differences, globally ready; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Latency note: one list sample exceeded 300ms; this single sample does not establish a p95 breach or the 15-minute consecutive rollback trigger and remains under observation.
- Anomalies: none requiring action.

### 2026-08-24T16:28:54Z — Monitoring cycle 3

- Health: `UP`.
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; every other content type remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 18.
- Automatic fallbacks: 0; fallback reasons: none.
- List probes: 200/200, hashes matched; latencies 396.070ms and 50.055ms.
- Detail probes: 200/200, hashes matched; latencies 48.048ms and 37.539ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 39.843ms.
- Missing id: 404.
- Shadow: 12/12 mapped, zero differences, globally ready; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Latency note: the first list probe again exceeded 300ms. Hourly single samples do not establish a 15-minute consecutive breach; final p95 must use a baseline-comparable reused-client sample after the observation window.
- Anomalies: none requiring action.

### 2026-08-24T17:29:29Z — Monitoring cycle 4

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 23.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 92.134ms and 40.426ms.
- Detail probes: 200/200, hashes matched; latencies 51.155ms and 41.134ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 50.993ms.
- Missing id: 404; latency 40.886ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T18:29:18Z — Monitoring cycle 5

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 28.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 45.293ms and 32.957ms.
- Detail probes: 200/200, hashes matched; latencies 47.596ms and 66.810ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 54.333ms.
- Missing id: 404; latency 29.214ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T19:30:10Z — Monitoring cycle 6

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 33.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 68.475ms and 94.050ms.
- Detail probes: 200/200, hashes matched; latencies 154.679ms and 68.967ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 164.263ms.
- Missing id: 404; latency 161.163ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T20:30:14Z — Monitoring cycle 7

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 38.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 29.854ms and 21.036ms.
- Detail probes: 200/200, hashes matched; latencies 28.745ms and 23.771ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 26.799ms.
- Missing id: 404; latency 16.533ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T21:30:47Z — Monitoring cycle 8

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 43.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 48.992ms and 19.351ms.
- Detail probes: 200/200, hashes matched; latencies 22.889ms and 18.510ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 23.969ms.
- Missing id: 404; latency 20.038ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T22:32:47Z — Monitoring cycle 9

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 48.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 22.999ms and 18.651ms.
- Detail probes: 200/200, hashes matched; latencies 20.518ms and 18.538ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 26.190ms.
- Missing id: 404; latency 16.058ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-24T23:34:18Z — Monitoring cycle 10

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 53.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 26.138ms and 36.598ms.
- Detail probes: 200/200, hashes matched; latencies 36.707ms and 26.704ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 23.175ms.
- Missing id: 404; latency 21.888ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T00:34:19Z — Monitoring cycle 11

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 58.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 25.301ms and 19.566ms.
- Detail probes: 200/200, hashes matched; latencies 22.014ms and 18.833ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 39.826ms.
- Missing id: 404; latency 16.260ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T01:33:55Z — Monitoring cycle 12

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 63.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 18.868ms and 15.839ms.
- Detail probes: 200/200, hashes matched; latencies 14.460ms and 17.612ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 24.097ms.
- Missing id: 404; latency 12.084ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T02:34:21Z — Monitoring cycle 13

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 68.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 23.792ms and 20.759ms.
- Detail probes: 200/200, hashes matched; latencies 25.237ms and 26.910ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 22.239ms.
- Missing id: 404; latency 15.337ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T03:34:26Z — Monitoring cycle 14

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 73.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 20.865ms and 20.144ms.
- Detail probes: 200/200, hashes matched; latencies 20.104ms and 17.035ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 21.946ms.
- Missing id: 404; latency 14.060ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T04:35:27Z — Monitoring cycle 15

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 78.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 24.453ms and 18.023ms.
- Detail probes: 200/200, hashes matched; latencies 19.597ms and 21.919ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 18.377ms.
- Missing id: 404; latency 15.835ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T05:35:39Z — Monitoring cycle 16

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 83.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 21.081ms and 18.802ms.
- Detail probes: 200/200, hashes matched; latencies 19.059ms and 15.742ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 19.296ms.
- Missing id: 404; latency 14.259ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T06:35:03Z — Monitoring cycle 17

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 88.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 19.030ms and 15.127ms.
- Detail probes: 200/200, hashes matched; latencies 17.365ms and 16.802ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 21.057ms.
- Missing id: 404; latency 6.431ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T07:35:04Z — Monitoring cycle 18

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 93.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 14.937ms and 14.075ms.
- Detail probes: 200/200, hashes matched; latencies 13.965ms and 12.227ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 15.176ms.
- Missing id: 404; latency 6.356ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T08:35:03Z — Monitoring cycle 19

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 98.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 22.176ms and 17.319ms.
- Detail probes: 200/200, hashes matched; latencies 19.193ms and 15.851ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 18.906ms.
- Missing id: 404; latency 16.034ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Anomalies: none.

### 2026-08-25T09:35:05Z — Monitoring cycle 20

- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false` and effective on `LEGACY`.
- NEWS unified request count after probes: 103.
- Automatic fallbacks: 0; fallback reasons: none for every content type.
- List probes: 200/200, hashes matched; latencies 23.781ms and 181.961ms.
- Detail probes: 200/200, hashes matched; latencies 35.696ms and 21.964ms.
- Portal-home probe: 200, `latestNews` hash matched; latency 21.697ms.
- Missing id: 404; latency 21.528ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true; portal-home projection ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit readiness: the 100-request minimum is satisfied; the 24-hour time gate remains pending until `2026-08-25T14:02:24Z`, so no exit review was created.
- Anomalies: none.

### 2026-08-25T10:54:02Z — Monitoring cycle 21

- Health: `UP`.
- Routing after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 108.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- Probe execution: exactly five NEWS compatibility requests were issued (two list, two detail for id `1`, and one portal-home request), and one missing-id request for `999` was issued.
- Probe evidence limitation: the PowerShell observation harness failed while serializing its final result, after the HTTP requests completed but before it emitted the per-request HTTP statuses, SHA-256 hashes, and latencies. Those values are therefore unavailable for this cycle and no payload-match claim is made for cycle 21. No replacement compatibility probes were sent, preserving the five-probe limit.
- Missing id evidence limitation: the same harness failure prevented capture of the missing-id status for this cycle; prior cycles consistently returned 404.
- Shadow after probes: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx evidence: not captured for the five probes because of the monitoring-harness output failure; backend health and protected status/shadow endpoints remained healthy immediately afterward.
- Exit readiness: the 100-request minimum remains satisfied; the 24-hour time gate remains pending until `2026-08-25T14:02:24Z`, so no exit review was created.
- Anomaly: monitoring-harness evidence capture failure only; no routing, fallback, comparison, or shadow anomaly was observed.

### 2026-08-25T12:18:42Z — Monitoring cycle 22

- Health: `UP` (HTTP 200).
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 113.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 43.658ms and 28.556ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 43.661ms and 21.303ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 27.849ms.
- Missing id `999`: 404; latency 102.926ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit readiness: the 100-request minimum is satisfied; the 24-hour time gate remains pending until `2026-08-25T14:02:24Z`, so no exit review was created.
- Anomalies: none.

### 2026-08-25T13:22:40Z — Monitoring cycle 23

- Health: `UP` (HTTP 200).
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 118.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 33.393ms and 22.820ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 18.374ms and 18.360ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 22.835ms.
- Missing id `999`: 404; latency 25.180ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit readiness: the 100-request minimum is satisfied; the 24-hour time gate remains pending until `2026-08-25T14:02:24Z`, so no exit review was created.
- Anomalies: none.

### 2026-08-25T14:32:30Z — Monitoring cycle 24 and exit-review trigger

- Health: `UP` (HTTP 200).
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 123.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 25.391ms and 15.973ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 22.679ms and 19.755ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 25.372ms.
- Missing id `999`: 404; latency 18.545ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit readiness: both prerequisites are satisfied at this observation: the canary has run for 24h 30m 6s and NEWS has recorded 123 unified compatibility requests. `NEWS_CANARY_EXIT_REVIEW.md` was created for owner review; this does not declare NEWS successful or authorize another content type.
- Operational note: an initial authentication attempt targeted unused local port 8080 and timed out; Docker inspection confirmed the canary backend was published on port 8081. No compatibility probe was issued before correcting the local endpoint, and exactly five were then issued against the canary backend.
- Anomalies: none affecting canary routing, contracts, shadow state, fallbacks, or backend health.

### 2026-08-25T15:26:52Z — Monitoring cycle 25, approval hold

- Health: `UP` (HTTP 200); latency 14.502ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 128.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 25.618ms and 22.150ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 24.080ms and 13.896ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 34.956ms.
- Missing id `999`: 404; latency 16.568ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit state: `NEWS_CANARY_EXIT_REVIEW.md` remains available with all gates assessed `PASS`, but NEWS is still on explicit-approval hold. No success declaration, flag change, new Slice, or later content-type activation occurred.
- Anomalies: none.

### 2026-08-25T16:27:45Z — Monitoring cycle 26, approval hold

- Health: `UP` (HTTP 200); latency 13.516ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 133.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 24.258ms and 19.654ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 20.339ms and 25.839ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 25.177ms.
- Missing id `999`: 404; latency 19.099ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit state: the prepared exit review remains on explicit-approval hold. NEWS was not declared successful, and no flag, Slice, or later content type changed.
- Anomalies: none.

### 2026-08-25T17:41:50Z — Monitoring cycle 27, approval hold

- Health: `UP` (HTTP 200).
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 138.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 189.426ms and 21.776ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 37.312ms and 18.755ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 22.799ms.
- Missing id `999`: 404; latency 67.627ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit state: the prepared exit review remains on explicit-approval hold. NEWS was not declared successful, and no flag, Slice, or later content type changed.
- Anomalies: none.

### 2026-08-25T18:40:58Z — Monitoring cycle 28, approval hold

- Health: `UP` (HTTP 200); latency 33.026ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 143.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 30.226ms and 28.945ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 27.454ms and 18.636ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 29.286ms.
- Missing id `999`: 404; latency 18.753ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit state: the prepared exit review remains on explicit-approval hold. NEWS was not declared successful, and no flag, Slice, or later content type changed.
- Anomalies: none.

### 2026-08-25T19:42:07Z — Monitoring cycle 29, approval hold

- Health: `UP` (HTTP 200); latency 31.132ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 148.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 26.080ms and 19.241ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 22.856ms and 14.238ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 18.106ms.
- Missing id `999`: 404; latency 15.474ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit state: the prepared exit review remains on explicit-approval hold. NEWS was not declared successful, and no flag, Slice, or later content type changed.
- Anomalies: none.

### 2026-08-25T20:42:21Z — Monitoring cycle 30, approval hold

- Health: `UP` (HTTP 200); latency 40.694ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT remained configured `false`, shadow-ready `true`, and effective on `LEGACY`.
- NEWS unified request count after probes: 153.
- Automatic fallbacks: 0; fallback reasons: none for every content type; compatibility comparison error: none.
- List probes: 200/200, SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline twice; latencies 22.557ms and 15.699ms.
- Detail probes for id `1`: 200/200, SHA-256 `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` matched the baseline twice; latencies 16.938ms and 16.555ms.
- Portal-home probe: 200, `latestNews` SHA-256 `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` matched the baseline; latency 19.055ms.
- Missing id `999`: 404; latency 36.036ms.
- Shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional unified items; portal-home projection 3/3 ready.
- 5xx responses: 0 of 5 compatibility probes.
- Exit state: the prepared exit review remains on explicit-approval hold. NEWS was not declared successful, and no flag, Slice, or later content type changed.
- Anomalies: none.

### 2026-08-26T10:58:39Z — Monitoring cycle 31, FAIL: backend unavailable

- Health: unavailable; the authentication preflight to `http://localhost:8081/api/auth/login` was refused before any compatibility probe was issued.
- Runtime evidence: `docker compose ps` could not connect to `dockerDesktopLinuxEngine`; the Docker Desktop Linux-engine named pipe was absent.
- Routing, feature-flag isolation, NEWS unified request count, automatic fallbacks, compatibility comparison state, and live shadow state could not be assessed while the backend was unavailable.
- Compatibility probes issued: 0 of 5. No list, detail, portal-home, or missing-id request was sent, so no HTTP, hash, or latency evidence exists for this cycle.
- Exit state: the prepared exit review remains on explicit-approval hold. NEWS was not declared successful, and no rollback, flag change, Slice, or later content-type activation was attempted.
- Anomaly: **FAIL — local canary runtime unavailable; monitoring evidence is interrupted until Docker/backend service is restored.**

### 2026-08-26T11:09:24Z — Final owner-requested exit review; scheduled monitoring stopped

- Scheduled automation `news-canary-monitoring` was deleted before the review; no further automatic run is scheduled.
- Preconditions: PASS. The 24-hour requirement had been met before the interruption, with 30h 39m 57s of healthy evidence; the final review occurred 45h 7m after activation. The pre-restart counter was 153, above the 100-request minimum.
- Runtime recovery: Docker/PostgreSQL and backend were available for the final review. The restart reset process-local compatibility counters; no flag was changed.
- Final probes: two list, two detail for id `1`, and one portal-home request all returned 200 and exact baseline hashes. Latencies were 186.198ms/26.778ms, 40.595ms/20.477ms, and 48.766ms respectively. Missing id `999` returned 404 in 36.304ms.
- Known cumulative NEWS requests: 158 (153 before restart plus 5 final live probes). Current-process NEWS counter: 5.
- Automatic fallbacks: 0; fallback reasons: none; compatibility comparison error: none.
- Final routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT, DECISION, and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- Final shadow: 12/12 mapped, zero differences, globally ready; NEWS count/order/field parity true with zero additional items; portal-home projection 3/3 ready.
- Database-pressure evidence: database CPU 0.00%, memory 41.77MiB, waiting locks 0, deadlocks 0, rollbacks 0, temp files/bytes 0; no post-start connection-pool or database error was logged.
- Exit review: all requested technical gates assessed `PASS` in `NEWS_CANARY_EXIT_REVIEW.md`. Decision remains **HOLD — explicit owner approval required**; NEWS was not declared graduated and no later content type was enabled.

### 2026-08-26 — Owner decision: NEWS graduated in local Docker only

- The owner explicitly approved NEWS canary success and graduation for the local Docker environment only.
- NEWS remains configured for unified compatibility and effectively `UNIFIED`; recording this decision did not change any feature flag.
- ANNOUNCEMENT, DECISION, and DOCUMENT remain configured `false` and effectively `LEGACY`.
- This decision is not production approval and does not authorize a production rollout.
- No write cutover, database change, rollback, new Slice activation, or later content-type activation was performed.
- Scheduled NEWS monitoring remains deleted. ANNOUNCEMENT has a review-only canary plan and remains on activation `HOLD`.
