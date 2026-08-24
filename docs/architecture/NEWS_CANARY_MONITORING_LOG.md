# NEWS Canary Monitoring Log

> **Status:** Active
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
