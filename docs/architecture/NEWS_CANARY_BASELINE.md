# NEWS Canary Baseline

> **Status:** Frozen for the current NEWS canary exit review
> **Canary start:** `2026-08-24T14:02:24Z`
> **Baseline capture:** `2026-08-24T14:26:46Z`
> **Scope:** Local Docker only; NEWS read compatibility and affected portal-home projection

## 1. Contract baseline

The payload baseline was captured before NEWS activation and verified again through an isolated legacy-reference backend using the same image and PostgreSQL data.

| Contract | Expected status | SHA-256 |
|---|---:|---|
| `GET /api/news` | 200 | `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` |
| `GET /api/news/1` | 200 | `D8FA49FFC088D93E3CA0F7B7BF3E22EC0F255EA8C818F614FC25744CFFC78C25` |
| `GET /api/portal/home` → `latestNews` | 200 | `25B25A49D82B29C553414A040AF66910EAD1648AF4521107060028B6F96891E3` |
| `GET /api/news/999` | 404 | Status contract only |

The expected list and portal-home projection each contain three NEWS records. Numeric identifiers and ordering are part of the exact hashes.

## 2. Legacy performance and error baseline

Method:

- isolated local Docker backend on a separate port
- NEWS and every other compatibility flag set to false
- same backend image, Docker network, and PostgreSQL database as the canary
- one reused HTTP client, ten warm-up requests per route, then 100 sequential measured requests per route
- client-observed end-to-end latency

| Route | Samples | 5xx | 5xx rate | Average | p50 | p95 | Max |
|---|---:|---:|---:|---:|---:|---:|---:|
| `/api/news` | 100 | 0 | 0.000% | 26.860ms | 5.823ms | 10.895ms | 2044.131ms |
| `/api/news/1` | 100 | 0 | 0.000% | 25.471ms | 4.396ms | 9.450ms | 2033.347ms |
| `/api/portal/home` | 100 | 0 | 0.000% | 24.644ms | 3.651ms | 7.484ms | 2045.321ms |

The exit gate permits at most a 0.5 percentage-point increase in 5xx rate. The p95 gate permits at most 25% above the corresponding legacy p95, unless both values are below 300ms.

## 3. Data and routing baseline

- Shadow comparison: 12 legacy public sources, 12 mapped unified items, zero differences.
- NEWS: count parity, order parity, and field parity were true.
- Portal home: three of three compared content sections matched; `latestNews` was ready.
- Before activation: NEWS was `configuredForUnified=false`, `shadowReady=true`, and `effectiveSource=LEGACY`.
- Automatic fallback count before activation: zero.
- Direct unified public read: 404.
- Anonymous actuator metrics: 401.

## 4. Evidence limitations

The process-local Micrometer counters reset when the backend was recreated for activation. The latency baseline is therefore an isolated legacy-reference measurement captured shortly after activation rather than a retained pre-activation production time series. It is valid for this local Docker canary comparison but must not be represented as production evidence.
