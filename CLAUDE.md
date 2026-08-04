# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Tesla Backend: a Spring Boot 4 / Java 21 API for an educational platform (courses, weeks, lessons, quizzes, student progress/rankings, and group chat), deployed to AWS ECS Fargate. Infrastructure is modular Terraform, and CI/CD runs through GitHub Actions.

## Commands

```bash
# Run locally (needs COGNITO_ISSUER_URI, see "Local setup" below)
./mvnw spring-boot:run

# Full verify: tests + JaCoCo coverage report (what CI runs)
./mvnw verify

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=CourseServiceTest

# Run a single test method
./mvnw test -Dtest=CourseServiceTest#someMethodName

# Build the jar (skip tests, matches Dockerfile build)
./mvnw clean package -DskipTests

# Start local backing services (Postgres only)
docker compose up -d
```

There is no separate lint step in CI; `mvn verify` (tests + JaCoCo) plus a SonarCloud scan is the quality gate.

### Local setup

1. `docker compose up -d` — Postgres on port 5433 (the only backing service left).
2. Deploy the `dev` Terraform environment (`iac/environments/dev`) to provision Cognito, then grab the printed `dev_cognito_issuer_uri`.
3. Export `COGNITO_ISSUER_URI` and `SPRING_PROFILES_ACTIVE=dev`, then run the app. See README.md for the full step-by-step (bootstrap layer, `terraform.tfvars`, backend config, etc.).

## Architecture

### Package-by-feature layout

Code under `src/main/java/com/tesla/teslabackend/` is organized by domain feature, not by technical layer:

- `common/` — cross-cutting: global exception handling (`GlobalExceptionHandler` + custom exceptions → `ErrorResponse`), request-id logging filter, `MetricsConfig` (CloudWatch, prod-only), `SchedulerLockConfig` (ShedLock).
- `user/` — `Usuario` entity/roles, Cognito integration (`user.cognito`), admin user management, `IdentityExtractor` (JWT → `Usuario`).
- `security/` — `SecurityConfig` (JWT resource server, CORS, route matchers).
- `course/`, `lesson/`, `progress/`, `group/` — domain features, each typically with `controller/`, `service/`, `entity/`, `repository/`, `dto/`.

Each feature package is generally self-contained (`controller` → `service` → `repository`/`entity`), with `common` and `security` providing shared infrastructure. Domain terms (entities, DTOs, exceptions) are in Spanish, matching the business domain; keep new code consistent with that.

### Auth: Cognito + JWT, no local sessions

- Spring Security is a stateless OAuth2 resource server validating Cognito-issued JWTs (`SecurityConfig`). The issuer URI comes from `spring.security.oauth2.resourceserver.jwt.issuer-uri` (env `COGNITO_ISSUER_URI`).
- Authorities are derived from the `cognito:groups` JWT claim, prefixed `ROLE_` — method security (`@EnableMethodSecurity`) uses these.
- The JWT decoder additionally requires `token_use=access` (rejects ID tokens).
- `Usuario` rows are linked to Cognito identities via `cognito_sub`. `IdentityExtractor` resolves the authenticated `Jwt` to the local `Usuario`/id — controllers needing the current user go through it rather than reading claims directly.
- User creation (`UsuarioAdminService` + `CognitoService`) creates the Cognito user first, then the local row; Cognito failures are translated into `CognitoNoDisponibleException` so the local record can be left "pending" and retried, instead of the whole flow failing. `AdminSeedRunner` seeds the first admin account idempotently at startup and must never block app startup on failure.
- WebSocket (STOMP) connections are authenticated separately: `StompAuthChannelInterceptor` decodes the `Authorization` bearer token on the STOMP `CONNECT` frame (HTTP-layer security doesn't cover the socket handshake since `/ws-chat/**` is `permitAll`).

### Single-task assumption (post-FinOps)

ElastiCache (Redis) and Amazon MQ were removed to cut fixed hourly cost. Two design consequences follow, and **both only hold while the ECS service runs exactly one task** (`desired_count = 1`, no autoscaling):

- `GroupWebSocketConfig` uses `enableSimpleBroker` — an in-process STOMP broker. Messages are not shared between JVMs, so a second task would silently split the chat.
- `@Scheduled` jobs (`GroupChatCleanupService`, `RankingCronTask`) have no distributed lock. ShedLock was dropped along with its Redis lock store, so a second task would run every job twice.

If horizontal scaling is ever restored, both need addressing before scaling out — an external broker for chat, and a lock provider for the schedulers.

The group chat is also not publicly reachable: API Gateway HTTP API cannot proxy WebSocket upgrades, so the `/ws-chat/*` CloudFront behavior was removed. The endpoint still works locally and directly against the task.

### Service window: the backend is only up 18:00–24:00 (America/Lima)

The two remaining hourly-billed resources — the Fargate task and the `db.t4g.micro` RDS instance — are shut down outside the evening window. `iac/modules/scheduler/` drives it with four EventBridge Scheduler schedules (universal targets, so no Lambda), all expressed in Lima time via `schedule_expression_timezone`:

| Lima time | Action |
|---|---|
| 17:40 | `rds:startDBInstance` — 20 min of headroom; the app won't boot without the DB |
| 18:00 | `ecs:updateService` → `DesiredCount = 1` |
| 00:00 | `ecs:updateService` → `DesiredCount = 0` |
| 00:10 | `rds:stopDBInstance` — after ECS, so no live connections are cut |

Things that depend on this window and will break silently if it moves:

- **RDS backup/maintenance windows must stay inside it.** A stopped instance runs no automated backups. `iac/modules/database/bd.tf` therefore uses `23:10-23:40` UTC (18:10–18:40 Lima) and `Tue:03:00-Tue:04:00` UTC (Mon 22:00–23:00 Lima), not the small hours.
- **`RankingCronTask` must fire inside it.** It moved from Monday 00:00 to Monday 18:05 (`app.ranking.snapshot.cron`). The window it aggregates is derived from `previousOrSame(MONDAY).atStartOfDay()`, so any time on a Monday yields the identical interval — only the day of week matters.
- **The CD pipeline turns everything on before deploying** (`activar` job). With 0 tasks the `servicesStable` waiter returns immediately and the circuit-breaker rollback check would pass without validating anything; and `terraform apply` fails against a stopped RDS instance.
- **CloudFront answers 503 on `/api/*` outside the window** via the `service-hours` function (`cloudfront-js-2.0`, needed for `Date`), rendered from `functions/service-hours.js.tftpl` by `templatefile()` so the hours can't drift from the scheduler. Otherwise callers would get API Gateway's generic error, indistinguishable from an outage.

Terraform never fights the scheduler: `aws_ecs_service.api` has `ignore_changes = [task_definition, desired_count]`. Set `ventana_habilitada = false` in an environment to park the schedules in `DISABLED` and return to 24/7 without destroying anything.

To bring the service up manually outside the window:

```bash
aws rds start-db-instance --db-instance-identifier tesla-backend-<env>-db
aws rds wait db-instance-available --db-instance-identifier tesla-backend-<env>-db
aws ecs update-service --cluster tesla-backend-<env>-cluster \
  --service tesla-backend-<env>-service --desired-count 1
```

### The origin-token filter is the real perimeter

`OriginTokenFilter` (`security/filter/`) rejects with 403 any request lacking the `X-Tesla-Origin-Token` header that CloudFront injects. This is **not** defense in depth — it is the only enforcement point. The HTTP API is deployed with `disable_execute_api_endpoint = false` (it cannot be disabled without a custom domain), so its `execute-api` URL is public and bypasses CloudFront entirely.

Details that matter when touching it:
- It is ordered `HIGHEST_PRECEDENCE + 10` — after `RequestIdFilter` so rejections carry a `requestId`, and well before the Spring Security chain so illegitimate traffic is dropped before JWT processing.
- `/actuator/**` is exempt: the container `HEALTHCHECK` calls localhost without the header, and enforcing it there would put the task in a restart loop.
- The token comparison uses `MessageDigest.isEqual` (constant time), and the received value is never logged.
- `app.security.origin-token` is `${ORIGIN_TOKEN:}` in the shared config (blank ⇒ filter disabled, so local dev works) but `${ORIGIN_TOKEN}` **without a default** in `application-prod.properties`. A missing env var therefore fails startup rather than silently disabling the only perimeter check. Keep that asymmetry if you touch these files.
- The value reaches the task as an SSM `SecureString` via the `secrets` block, never as a plaintext env var.

### Rankings are computed from PostgreSQL

`RankingService` aggregates the weekly leaderboard straight from `IntentoRepository.findExpAgregadaPorVentana` and caps the result at 100. There was previously a Redis ZSET cache in front of it; the SQL path already existed as the fallback and is now the only path. `RankingCronTask` materializes the weekly snapshot into `HistorialRanking` and never touched Redis.

### Metrics: CloudWatch push, prod-only, allow-listed

`MetricsConfig` (`@Profile("prod")`) registers a `CloudWatchAsyncClient` and a Micrometer `MeterFilter` that denies any metric not matching `ALLOWED_METRIC_PREFIXES`. This is a cost control — each custom CloudWatch metric series is billed. When adding metrics that should reach CloudWatch in prod, they must be added to that allow-list or they'll be silently dropped. Locally/dev, `management.cloudwatch.metrics.export.enabled=false` short-circuits export entirely.

### Health checks: liveness vs readiness

Actuator exposes split probes (`management.endpoint.health.probes.enabled=true`, readiness group includes `db` — Redis was dropped with ElastiCache). Liveness (`/actuator/health/liveness`) is process-only and drives the Docker `HEALTHCHECK`/container restarts; readiness includes dependencies. Don't conflate the two when adding new health indicators — a flaky external dependency should affect readiness, not liveness.

### Configuration profiles

- `application.properties` — profile-agnostic defaults (multipart limits, CORS origins list, health probe config, and the `aws.s3.*` block). Note that `S3StorageService` reads four `aws.s3.*` properties via `@Value` with **no defaults**, so they must live in the shared file — putting them in a single profile makes every other profile fail to start.
- `application-dev.properties` — local/dev: `ddl-auto=update`, verbose SQL logging, CloudWatch export disabled.
- `application-prod.properties` — `ddl-auto=validate` (no auto schema changes in prod), HikariCP tuning, Actuator restricted to `health,info`, structured JSON (ECS format) logging, CloudWatch export enabled with a 1-minute step.

Active profile is controlled by `SPRING_PROFILES_ACTIVE` (defaults to `dev`).

## Infrastructure (`iac/`)

Terraform, structured as:

- `iac/bootstrap/` — foundational layer (S3 state bucket, DynamoDB lock table, GitHub OIDC roles). Deployed once, manually, before any environment.
- `iac/modules/` — reusable modules: `networking`, `security`, `database`, `compute`, `edge` (+ `edge/functions`), `cognito`, `apigateway`.
- `iac/environments/{dev,prod}` — per-environment root modules composing the above modules. (QA reuses the same environment pattern via CI branch mapping, see below.)

Checkov scans `iac/` in CI (`.checkov.yaml`, soft-fail mode — findings don't block the PR).

## CI/CD

- **`ci.yml`** (PRs to `develop`/`qa`/`main`): runs `mvn verify` + SonarCloud analysis, and a Checkov IaC scan. Quality gate only — no deploys.
- **`cd.yml`** (push to `develop`/`qa`/`main`, or manual dispatch): maps branch → environment (`develop`→dev, `qa`→qa, `main`→prod), runs `activar` (starts RDS and scales ECS to 1 — see "Service window" above), conditionally runs Terraform only if `iac/**` changed (or on manual dispatch), then builds/pushes the Docker image to ECR and updates the ECS service. `deploy` depends on `terraform` so infra changes land before the new image ships. The ECS service has `ignore_changes=[task_definition]`, so a later `terraform apply` won't roll back the deployed image.
- **Deploy failures roll back automatically.** The service enables `deployment_circuit_breaker` with `rollback = true`: an image that can't start (3 failed task launches, or containers the `HEALTHCHECK` marks unhealthy) aborts the deployment and restores the previous revision. Because the CD action only waits on the `servicesStable` waiter — and a rolled-back service *is* stable — the workflow has an explicit "verificar que no hubo rollback" step comparing the `PRIMARY` deployment's task definition against the one just registered. Without it a failed deploy would report success. Keep that step if you change the deploy action.
- DB/MQ passwords are never passed through GitHub Actions as plain secrets into the app — Terraform writes them to SSM Parameter Store (`SecureString`) from `terraform.tfvars`, and the ECS task definition reads them via `secrets`/`valueFrom`.

## Dockerfile

Multi-stage build: Maven/Temurin 21 build stage → Temurin 21 JRE (Alpine) runtime stage, running as a non-root `spring` user. `JAVA_OPTS` defaults set `-XX:MaxRAMPercentage=75.0` and `America/Lima` timezone. The `HEALTHCHECK` hits liveness only (see "Health checks" above) — it intentionally does not fail the container over a dependency outage.
