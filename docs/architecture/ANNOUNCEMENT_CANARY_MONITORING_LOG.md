# ANNOUNCEMENT Canary Monitoring Log

> **Status:** Canary active in local Docker; hourly monitoring authorized and active
> **Canary start:** `2026-08-26T12:56:09Z`
> **Earliest exit review:** `2026-08-27T12:56:09Z`, and only after at least 100 ANNOUNCEMENT unified compatibility requests
> **Baseline:** `ANNOUNCEMENT_CANARY_BASELINE.md`, frozen in commit `5e896be3691c22f8380e83f5e5d02d47145fb741`
> **Scheduled task:** `announcement-canary-monitoring`, hourly heartbeat, active
> **Rule:** Monitor ANNOUNCEMENT only. Do not change flags, rollback automatically, declare graduation, enable another content type, start write cutover, or deploy to production.

## Activation observation

### 2026-08-26T12:56:09Z — Activation gates passed; monitoring approval hold

- Scope: local Docker backend only; no production approval.
- Baseline integrity: commit `5e896be3691c22f8380e83f5e5d02d47145fb741`; frozen file SHA-256 `4443C02AE297DB9D9AF13705CE2310BC2E7A64B419440F891901A6E7EF37E054`.
- Deployment: backend only was recreated; database and frontend were not recreated.
- Health: `UP` (HTTP 200).
- Routing: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 5; legacy count: 0.
- Automatic fallbacks: 0 for every content type; fallback reasons empty; `comparisonError=null`.
- List probes: 200/200, baseline SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched twice; latencies 34.771ms and 24.234ms.
- Detail probes for numeric id `1`: 200/200/200, baseline SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched three times; latencies 37.619ms, 21.137ms, and 19.729ms.
- Missing id `999`: 404; latency 25.933ms.
- Portal-home isolation: 200, full-response SHA-256 `43CCE4E9882DB254AB6AE219B038A187F074D8BE8E73A837BA7CE0E2B3263BAC` matched the baseline; latency 46.861ms.
- Shadow: 12/12 mapped, zero differences, globally ready; ANNOUNCEMENT 3/3 with count/order/field parity true, zero additional items, and ready; existing portal-home comparison 3/3 ready.
- Compatibility 5xx: 0 of 5 counted ANNOUNCEMENT smoke probes.
- Database pressure: 0 waiting locks, deadlocks, rollbacks, and temp files/bytes; database size unchanged; no matching backend/database error log.
- Activation result: all gates `PASS`; canary clock started at this timestamp.
- Monitoring state: no scheduled automation exists and no periodic monitoring run is authorized yet.
- Graduation state: ANNOUNCEMENT is not graduated; DECISION and DOCUMENT remain inactive; no write cutover or production deployment occurred.

### 2026-08-26T13:04:10Z — Hourly monitoring authorized

- Owner authorized ANNOUNCEMENT canary monitoring only.
- Scheduled heartbeat `announcement-canary-monitoring` was created with an hourly cadence and is active.
- Each run is restricted to exactly five counted ANNOUNCEMENT compatibility requests: two list and three detail for numeric id `1`; missing id `999` is checked separately and is not counted.
- Healthy cycles append one observation to this log. Any fallback, mismatch, 5xx, readiness/shadow failure, routing change, or health/database/backend anomaly must report `FAIL` without automatic rollback.
- Exit Review remains forbidden until both `2026-08-27T12:56:09Z` has passed and ANNOUNCEMENT unified requests are at least 100.
- No feature flag, routing state, database, write path, production environment, DECISION, or DOCUMENT was changed while scheduling monitoring.

### 2026-08-26T14:05:28Z — Monitoring cycle 1 — PASS

- Elapsed since verified activation: 1h 9m 19s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 414.877ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 5 before and 10 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 46.330ms and 43.534ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 44.488ms, 41.856ms, and 35.670ms.
- Missing id `999`: HTTP 404; latency 50.383ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 2,035 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, and 0 waiting locks. Resource snapshot: backend 0.60% CPU / 405.8MiB, PostgreSQL 0.01% CPU / 40.07MiB. No health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-26T15:17:06Z — Monitoring cycle 2 — PASS

- Elapsed since verified activation: 2h 20m 57s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 485.467ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 10 before and 15 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 119.839ms and 424.219ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 103.506ms, 96.216ms, and 200.177ms.
- Missing id `999`: HTTP 404; latency 82.051ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Performance note: one isolated list sample was 424.219ms; this does not establish the plan's 15-consecutive-minute p95 rollback trigger, and all contracts, routing, health, shadow, and resource evidence remained clean.
- Database/backend evidence: 2 database connections, 2,660 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, and 0 waiting locks. Resource snapshot: backend 0.31% CPU / 406.9MiB, PostgreSQL 0.00% CPU / 40.2MiB. No health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-26T16:05:48Z — Monitoring cycle 3 — PASS

- Elapsed since verified activation: 3h 9m 39s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 164.148ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 15 before and 20 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 38.062ms and 35.520ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 32.946ms, 31.269ms, and 33.472ms.
- Missing id `999`: HTTP 404; latency 26.211ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 3,096 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, and 0 waiting locks. Resource snapshot: backend 0.51% CPU / 409.2MiB, PostgreSQL 0.01% CPU / 40.04MiB. No health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-26T17:18:16Z — Monitoring cycle 4 — PASS

- Elapsed since verified activation: 4h 22m 7s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 41.994ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 20 before and 25 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 25.386ms and 21.621ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 20.838ms, 19.113ms, and 21.412ms.
- Missing id `999`: HTTP 404; latency 15.245ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 3,798 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size unchanged at 9,346,071 bytes. Resource snapshot: backend 0.24% CPU / 406.7MiB, PostgreSQL 0.00% CPU / 37.62MiB. A bounded scan of the latest 75 minutes found no matching backend/database error log. No health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-26T18:17:16Z — Monitoring cycle 5 — PASS

- Elapsed since verified activation: 5h 21m 7s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 35.900ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 25 before and 30 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 22.458ms and 19.718ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 23.497ms, 16.511ms, and 17.139ms.
- Missing id `999`: HTTP 404; latency 13.079ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 4,315 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size unchanged at 9,346,071 bytes. Resource snapshot: backend 0.21% CPU / 408MiB, PostgreSQL 0.00% CPU / 37.74MiB. A bounded scan of the latest 65 minutes found no matching backend/database error log. No health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-26T21:20:35Z — Monitoring cycle 6 — PASS

- Elapsed since verified activation: 8h 24m 26s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 108.339ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 30 before and 35 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 43.911ms and 34.818ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 47.441ms, 35.238ms, and 32.911ms.
- Missing id `999`: HTTP 404; latency 35.633ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 5,855 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size unchanged at 9,346,071 bytes. Resource snapshot: backend 0.29% CPU / 409MiB, PostgreSQL 0.22% CPU / 37.81MiB. A bounded scan of the latest 75 minutes found no matching backend/database error log. No health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-26T22:19:07Z — Monitoring cycle 7 — PASS

- Elapsed since verified activation: 9h 22m 58s; exit-review time and traffic gates are not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 22.832ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT unified compatibility count: 35 before and 40 after the five counted probes; legacy count 0.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 25.276ms and 19.639ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 21.698ms, 19.522ms, and 19.713ms.
- Missing id `999`: HTTP 404; latency 33.382ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; existing portal-home comparison remained 3/3 ready without issuing a portal-home probe.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 6,409 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size unchanged at 9,346,071 bytes. Resource snapshot: backend 0.47% CPU / 419.1MiB, PostgreSQL 0.00% CPU / 37.48MiB. A bounded scan of the latest 75 minutes found no `ERROR`, `FATAL`, deadlock, timeout, connection failure, or pool-error match. One WARN at 22:18:32Z recorded a malformed admin-login JSON generated by the monitoring harness while Docker credential inspection was sandbox-blocked; the retry used the local environment file and all authenticated status/shadow checks passed. This harness warning did not touch compatibility routing, canary traffic, the database, or application state and is not a canary/backend failure.
- Monitoring result: `PASS`; ANNOUNCEMENT remains active but not graduated. No flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T09:46:48Z — Monitoring cycle 8 — FAIL (backend unavailable)

- Elapsed since verified activation: 20h 50m 39s; the 24-hour exit-review time gate is not yet satisfied.
- Preflight failed before compatibility traffic: the admin login request to `http://localhost:8081` was actively refused, so backend health, authenticated routing isolation, current counters, fallbacks, `comparisonError`, and live shadow readiness could not be verified in this cycle.
- Counted ANNOUNCEMENT compatibility requests sent in this cycle: 0. The harness stopped before the five-probe block; no replacement or retry probes were sent. Missing id `999` was not requested.
- Last successful counter evidence: ANNOUNCEMENT reached 45 unified requests at `2026-08-26T23:19:22Z` after five contract-matching probes (2 list and 3 detail), with HTTP 200, the frozen hashes, 0 fallbacks, 0/5 5xx, and live shadow 12/12 with zero differences. That interrupted heartbeat completed its probes but did not append a separate observation; this note preserves the counter provenance without replaying traffic.
- Container evidence: `docker compose ps` could not connect because the Docker Desktop Linux engine named pipe did not exist. Database-pressure and recent container-log checks were therefore unavailable; no container or database state was changed.
- Routing safety: no feature flag, rollback, write path, production environment, DECISION, or DOCUMENT change was made. The last verified routing state remains NEWS and ANNOUNCEMENT `UNIFIED`, DECISION and DOCUMENT `LEGACY`, but it is not asserted as current while the backend is offline.
- Monitoring result: `FAIL` due to backend/Docker unavailability. No automatic rollback or restart was attempted. Owner decision is required before any recovery action; ANNOUNCEMENT remains not graduated and no Exit Review was created.

### 2026-08-27T10:02:34Z — Monitoring cycle 9 — FAIL (service recovered; continuity reset)

- Owner authorized resuming monitoring after cycle 8. Docker services were already running when recovery verification began; no container start, rollback, flag change, or database mutation was performed by the monitor.
- Elapsed since verified activation: 21h 6m 25s; the original 24-hour exit-review time gate is not yet satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 67.251ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- Runtime counters restarted with the backend: ANNOUNCEMENT unified compatibility count was 0 before and 5 after the five counted probes; legacy count 0. The durable monitoring evidence contains 50 successful counted probes in total (40 in cycles 1–7, 5 recovered from the interrupted 23:19Z run, and 5 in this cycle), but the live in-memory counter is 5 and remains the conservative traffic gate for automation.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 63.711ms and 44.334ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 42.332ms, 34.038ms, and 25.911ms.
- Missing id `999`: HTTP 404; latency 58.776ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`; counted compatibility 5xx: 0 of 5.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Current database pressure is clean: 3 connections, 244 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.48% CPU / 480.7MiB, PostgreSQL 0.00% CPU / 42.07MiB.
- Operational anomaly: container logs show PostgreSQL was not properly shut down, performed automatic WAL recovery, and then became ready; the backend restarted successfully with schema version 6 and no migration. This restart explains the counter reset and breaks continuous-runtime evidence even though current contracts, routing, health, shadow parity, and database-pressure checks pass.
- Monitoring result: `FAIL` for operational continuity, not for ANNOUNCEMENT contract behavior. Hourly monitoring remains active, ANNOUNCEMENT remains not graduated, and no Exit Review was created. No rollback, feature flag, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T10:48:22Z — Monitoring cycle 10 — PASS

- Elapsed since verified activation: 21h 52m 13s; the original 24-hour exit-review time gate is not yet satisfied, and the live post-restart traffic counter remains below 100.
- Health: backend `UP`, HTTP 200; client-observed health latency 19.355ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 5 before and 10 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 55 successful counted probes, while the conservative live traffic gate is 10 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 32.035ms and 29.151ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 27.110ms, 25.198ms, and 22.493ms.
- Missing id `999`: HTTP 404; latency 32.108ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 620 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.17% CPU / 432.1MiB, PostgreSQL 3.70% CPU / 36.94MiB. The bounded recent log read returned no backend/database entries; no new health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T11:47:51Z — Monitoring cycle 11 — PASS

- Elapsed since verified activation: 22h 51m 42s; the original 24-hour exit-review time gate is not yet satisfied, and the live post-restart traffic counter remains below 100.
- Health: backend `UP`, HTTP 200; client-observed health latency 24.119ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 10 before and 15 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 60 successful counted probes, while the conservative live traffic gate is 15 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 34.031ms and 24.301ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 23.966ms, 23.798ms, and 40.026ms.
- Missing id `999`: HTTP 404; latency 38.373ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 1,184 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.17% CPU / 434.4MiB, PostgreSQL 5.29% CPU / 36.98MiB. The bounded recent log read returned no backend/database entries; no new health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T12:48:23Z — Monitoring cycle 12 — PASS

- Elapsed since verified activation: 23h 52m 14s; the original 24-hour exit-review time gate is not yet satisfied by 7m 46s, and the live post-restart traffic counter remains below 100.
- Health: backend `UP`, HTTP 200; client-observed health latency 16.488ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 15 before and 20 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 65 successful counted probes, while the conservative live traffic gate is 20 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 32.844ms and 23.038ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 22.333ms, 22.643ms, and 21.747ms.
- Missing id `999`: HTTP 404; latency 30.840ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 1,714 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.20% CPU / 437.2MiB, PostgreSQL 3.99% CPU / 37.06MiB. The bounded recent log read returned no backend/database entries; no new health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; both Exit Review conditions are still unmet, so no report was created. The previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T13:48:53Z — Monitoring cycle 13 — PASS

- Elapsed since verified activation: 24h 52m 44s; the original 24-hour time gate is now satisfied, but the live post-restart unified traffic counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 26.603ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 20 before and 25 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 70 successful counted probes, while the conservative live traffic gate is 25 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 35.752ms and 23.039ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 24.058ms, 19.671ms, and 21.878ms.
- Missing id `999`: HTTP 404; latency 37.086ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 2,201 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.42% CPU / 438.9MiB, PostgreSQL 4.30% CPU / 37.64MiB. No current database pressure or backend resource anomaly was observed.
- Monitoring result: `PASS`; the time gate alone does not authorize Exit Review, and the previously documented continuity reset remains part of eventual evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T14:50:55Z — Monitoring cycle 14 — PASS

- Elapsed since verified activation: 25h 54m 46s; the 24-hour time gate is satisfied, but the live post-restart unified traffic counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 33.356ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 25 before and 30 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 75 successful counted probes, while the conservative live traffic gate is 30 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 34.823ms and 23.368ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 32.626ms, 21.266ms, and 21.590ms.
- Missing id `999`: HTTP 404; latency 38.288ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 2,743 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.60% CPU / 436.3MiB, PostgreSQL 0.04% CPU / 37.26MiB. No current database pressure or backend resource anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending and the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T15:51:26Z — Monitoring cycle 15 — PASS

- Elapsed since verified activation: 26h 55m 17s; the 24-hour time gate is satisfied, but the live post-restart unified traffic counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 27.799ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 30 before and 35 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 80 successful counted probes, while the conservative live traffic gate is 35 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 35.481ms and 21.262ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 22.004ms, 18.641ms, and 18.113ms.
- Missing id `999`: HTTP 404; latency 33.732ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 3,273 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.54% CPU / 441.8MiB, PostgreSQL 0.05% CPU / 37.19MiB. No current database pressure or backend resource anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending and the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T16:51:29Z — Monitoring cycle 16 — PASS

- Elapsed since verified activation: 27h 55m 20s; the 24-hour time gate is satisfied, but the live post-restart unified traffic counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 29.302ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 35 before and 40 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 85 successful counted probes, while the conservative live traffic gate is 40 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 30.927ms and 27.246ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 22.111ms, 19.152ms, and 19.228ms.
- Missing id `999`: HTTP 404; latency 35.078ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 3,799 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.44% CPU / 443.1MiB, PostgreSQL 4.27% CPU / 37.98MiB. No current database pressure or backend resource anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending and the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T17:52:32Z — Monitoring cycle 17 — PASS

- Elapsed since verified activation: 28h 56m 23s; the 24-hour time gate is satisfied, but the live post-restart unified traffic counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 26.659ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 40 before and 45 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 90 successful counted probes, while the conservative live traffic gate is 45 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 32.017ms and 19.667ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 20.969ms, 18.132ms, and 21.026ms.
- Missing id `999`: HTTP 404; latency 37.547ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 4,333 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.45% CPU / 446.7MiB, PostgreSQL 4.15% CPU / 37.06MiB. No current database pressure or backend resource anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending and the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T18:53:28Z — Monitoring cycle 18 — PASS

- Elapsed since verified activation: 29h 57m 19s; the 24-hour time gate is satisfied, but the live post-restart unified traffic counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 25.250ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 45 before and 50 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 95 successful counted probes, while the conservative live traffic gate is 50 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 31.490ms and 25.357ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 20.669ms, 17.361ms, and 16.856ms.
- Missing id `999`: HTTP 404; latency 34.064ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 4,866 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.56% CPU / 450.4MiB, PostgreSQL 0.01% CPU / 36.98MiB. No current database pressure or backend resource anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending and the previously documented continuity reset remains part of eventual Exit Review evidence. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T19:53:39Z — Monitoring cycle 19 — PASS

- Elapsed since verified activation: 30h 57m 30s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 31.990ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 50 before and 55 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 100 successful counted probes, while the conservative live traffic gate is 55 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 29.145ms and 28.050ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 25.123ms, 30.833ms, and 24.155ms.
- Missing id `999`: HTTP 404; latency 17.566ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 5,486 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.17% CPU / 455.9MiB, PostgreSQL 0.00% CPU / 36.95MiB. The bounded recent log read returned no backend/database entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 100 durable probes because the required live unified counter is 55 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T20:54:09Z — Monitoring cycle 20 — PASS

- Elapsed since verified activation: 31h 58m 00s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 334.171ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 55 before and 60 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 105 successful counted probes, while the conservative live traffic gate is 60 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 35.666ms and 30.407ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 32.469ms, 31.575ms, and 28.251ms.
- Missing id `999`: HTTP 404; latency 28.047ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 6,049 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.48% CPU / 458.2MiB, PostgreSQL 0.00% CPU / 36.93MiB. The bounded recent log read returned no backend/database entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 105 durable probes because the required live unified counter is 60 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T22:19:28Z — Monitoring cycle 21 — PASS

- Elapsed since verified activation: 33h 23m 19s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 61.848ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 60 before and 65 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 110 successful counted probes, while the conservative live traffic gate is 65 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 34.647ms and 20.690ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 24.159ms, 19.360ms, and 18.068ms.
- Missing id `999`: HTTP 404; latency 25.914ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 6,770 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.22% CPU / 460.3MiB, PostgreSQL 0.00% CPU / 36.72MiB. The bounded recent log scan found zero matching anomaly entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 110 durable probes because the required live unified counter is 65 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-27T23:17:15Z — Monitoring cycle 22 — PASS

- Elapsed since verified activation: 34h 21m 06s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 55.247ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 65 before and 70 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 115 successful counted probes, while the conservative live traffic gate is 70 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 40.556ms and 18.089ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 21.519ms, 18.387ms, and 20.237ms.
- Missing id `999`: HTTP 404; latency 18.076ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 7,297 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.46% CPU / 465.6MiB, PostgreSQL 0.00% CPU / 36.54MiB. The bounded recent log scan found zero matching anomaly entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 115 durable probes because the required live unified counter is 70 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-28T00:18:43Z — Monitoring cycle 23 — PASS

- Elapsed since verified activation: 35h 22m 34s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 48.317ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 70 before and 75 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 120 successful counted probes, while the conservative live traffic gate is 75 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 44.456ms and 14.967ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 16.393ms, 19.994ms, and 12.882ms.
- Missing id `999`: HTTP 404; latency 14.136ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 7,794 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.45% CPU / 468.6MiB, PostgreSQL 0.00% CPU / 37.18MiB. The bounded recent log scan found zero matching anomaly entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 120 durable probes because the required live unified counter is 75 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-28T01:21:58Z — Monitoring cycle 24 — PASS

- Elapsed since verified activation: 36h 25m 49s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 53.581ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 75 before and 80 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 125 successful counted probes, while the conservative live traffic gate is 80 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 33.118ms and 15.756ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 16.754ms, 14.925ms, and 14.284ms.
- Missing id `999`: HTTP 404; latency 13.989ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 8,354 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 13.96% CPU / 488.9MiB, PostgreSQL 0.00% CPU / 36.93MiB. The backend CPU sample was transient and well below local capacity; health, request latencies, memory, database counters, and the bounded recent log scan showed no associated pressure or anomaly.
- Monitoring result: `PASS`; the traffic gate remains pending despite 125 durable probes because the required live unified counter is 80 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-28T02:21:48Z — Monitoring cycle 25 — PASS

- Elapsed since verified activation: 37h 25m 39s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 30.072ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 80 before and 85 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 130 successful counted probes, while the conservative live traffic gate is 85 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 26.408ms and 13.896ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 14.795ms, 16.584ms, and 14.590ms.
- Missing id `999`: HTTP 404; latency 5.904ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 8,887 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.30% CPU / 482.4MiB, PostgreSQL 0.00% CPU / 37.35MiB. The bounded recent log scan found zero matching anomaly entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 130 durable probes because the required live unified counter is 85 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-28T03:29:33Z — Monitoring cycle 26 — PASS

- Elapsed since verified activation: 38h 33m 24s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 26.589ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 85 before and 90 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 135 successful counted probes, while the conservative live traffic gate is 90 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 27.029ms and 25.978ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 25.472ms, 23.456ms, and 21.732ms.
- Missing id `999`: HTTP 404; latency 15.185ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 9,566 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.22% CPU / 483MiB, PostgreSQL 0.00% CPU / 37.52MiB. The bounded recent backend/database log read returned no entries. Initial host-side Docker inspection was sandbox-blocked and was repeated read-only without issuing additional compatibility probes; all database/resource/log checks passed and no canary/backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 135 durable probes because the required live unified counter is 90 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-28T09:21:12Z — Monitoring cycle 27 — PASS

- Elapsed since verified activation: 44h 25m 03s; the 24-hour time gate is satisfied, but the live post-restart ANNOUNCEMENT unified request counter remains below 100, so Exit Review is still forbidden.
- Health: backend `UP`, HTTP 200; client-observed health latency 43.121ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 90 before and 95 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 140 successful counted probes, while the conservative live traffic gate is 95 after the documented restart.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 35.737ms and 25.810ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 26.994ms, 30.015ms, and 23.818ms.
- Missing id `999`: HTTP 404; latency 22.341ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Live shadow: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; no portal-home request was issued.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 12,437 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.52% CPU / 483.6MiB, PostgreSQL 0.00% CPU / 37.32MiB. The bounded recent backend/database log read returned no entries; no health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; the traffic gate remains pending despite 140 durable probes because the required live unified counter is 95 after the documented restart. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.

### 2026-08-28T10:35:11Z — Monitoring cycle 28 — PASS; Exit Review gates reached

- Elapsed since verified activation: 45h 39m 02s; the 24-hour time gate is satisfied and the conservative live post-restart ANNOUNCEMENT unified request counter reached 100, so both prerequisites for Exit Review are now satisfied.
- Health: backend `UP`, HTTP 200; client-observed health latency 14.141ms.
- Routing before and after probes: NEWS configured `true`, shadow-ready `true`, effective source `UNIFIED`; ANNOUNCEMENT configured `true`, shadow-ready `true`, effective source `UNIFIED`; DECISION and DOCUMENT configured `false`, shadow-ready `true`, effective source `LEGACY`.
- ANNOUNCEMENT live unified compatibility count: 95 before and 100 after the five counted probes; legacy count 0. Durable monitoring evidence now covers 145 successful counted probes, while the conservative post-restart live traffic gate is 100.
- List probes: HTTP 200/200; SHA-256 `D675CD1B545C07AEA2A077F0F79563B3A9BB203D28730912B4112E4A8537484D` matched the frozen baseline twice; latencies 36.957ms and 17.698ms.
- Detail probes for numeric id `1`: HTTP 200/200/200; SHA-256 `561E9EF1C359D68EC507BB94049CC1F02C640ABF5AD59A40DB0BA5A75DE248B5` matched the frozen baseline three times; latencies 18.863ms, 18.361ms, and 19.023ms.
- Missing id `999`: HTTP 404; latency 30.632ms; not counted as compatibility traffic.
- Automatic fallbacks: 0 for every content type; fallback-reason maps empty; `comparisonError=null`.
- Final portal-home isolation guard: HTTP 200, SHA-256 `43CCE4E9882DB254AB6AE219B038A187F074D8BE8E73A837BA7CE0E2B3263BAC`, matching the frozen baseline; latency 15.743ms. This request is not ANNOUNCEMENT compatibility traffic.
- Final live shadow after the portal-home guard: globally ready, 12/12 mapped, zero differences; ANNOUNCEMENT 3 legacy / 3 mapped / 3 unified published, count/order/field parity true, zero additional unified items, and `readyForCanary=true`; portal-home comparison remained 3/3 ready.
- Counted compatibility 5xx: 0 of 5; contract/status/hash anomalies: none.
- Database/backend evidence: 2 database connections, 13,075 committed transactions, 0 rollbacks, 0 temp files/bytes, 0 deadlocks, 0 waiting locks, and database size 9,346,071 bytes. Resource snapshot: backend 0.20% CPU / 484.3MiB, PostgreSQL 0.00% CPU / 37.37MiB. The bounded recent backend/database log read returned no entries; no new health, routing, fallback, shadow, contract, database, or backend anomaly was observed.
- Monitoring result: `PASS`; both minimums are satisfied and the final evidence package is clean. The documented backend/database interruption and counter reset remain disclosed in the Exit Review. ANNOUNCEMENT remains active but not graduated, and no flag, rollback, write path, production environment, DECISION, or DOCUMENT was changed.
