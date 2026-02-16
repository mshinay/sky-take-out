# AGENTS Guide: sky-take-out

This file defines working rules for coding agents in this repository.
Prefer small, focused changes that match existing conventions.

## Repo Snapshot

- Project type: Maven multi-module Java backend
- Root module: `sky-take-out` (`pom` packaging)
- Submodules:
  - `sky-common` (shared constants, exceptions, utils, properties)
  - `sky-pojo` (DTO / Entity / VO models)
  - `sky-server` (Spring Boot app: controller/service/mapper/config)
- Main class: `sky-server/src/main/java/com/sky/SkyApplication.java`
- Main config: `sky-server/src/main/resources/application.yml`
- Mapper XML path: `sky-server/src/main/resources/mapper/*.xml`

## Environment Expectations

- Maven wrapper is not present (`mvnw` not found), use `mvn`
- Java 17 is recommended for this repo
- Java 21 may fail due Lombok/Javac compatibility in current setup
- Ensure `JAVA_HOME` points to a valid JDK directory

## Build Commands

Run from repo root `D:\work\project\myproject\sky-take-out`.

- Compile all modules:
  - `mvn -DskipTests compile`
- Package all modules:
  - `mvn clean package -DskipTests`
- Build only server with required modules:
  - `mvn -pl sky-server -am -DskipTests compile`
- Build only common module:
  - `mvn -pl sky-common -am -DskipTests compile`

## Run Commands

- Start backend service:
  - `mvn -pl sky-server spring-boot:run`
- Alternative full build + run pattern:
  - `mvn -pl sky-server -am -DskipTests package`
  - `java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar`

## Test Commands

Current repo has no committed `src/test` files, but use these commands when tests exist.

- Run all tests:
  - `mvn test`
- Run tests for server module only:
  - `mvn -pl sky-server test`
- Run one test class:
  - `mvn -pl sky-server -Dtest=OrderServiceImplTest test`
- Run one test method:
  - `mvn -pl sky-server -Dtest=OrderServiceImplTest#shouldRejectOutOfRangeAddress test`
- Run one test class and build dependencies:
  - `mvn -pl sky-server -am -Dtest=OrderServiceImplTest test`

Important note:

- `.gitignore` currently contains `*Test.java` and `**/test/`.
- If you add tests, update ignore rules first or tests may not be tracked.

## Lint / Format / Static Analysis

- No Checkstyle/Spotless/PMD plugin is configured in current POMs.
- No dedicated lint command exists today.
- Baseline quality gate is compile success:
  - `mvn -pl sky-server -am -DskipTests compile`
- Avoid formatting-only churn unless requested.

## Architecture and Layering Rules

- Keep clear flow: `Controller -> Service -> Mapper -> SQL/XML`.
- Controllers should stay thin and delegate business logic to services.
- Service interfaces belong in `service`, implementations in `service.impl`.
- Mappers live in `mapper`; SQL lives in XML for dynamic queries.
- DTO/Entity/VO separation should be preserved:
  - request payloads -> DTO
  - persistence models -> Entity
  - response view models -> VO

## API and Response Conventions

- Use `Result<T>` as the unified API response wrapper.
- Success should return `Result.success(...)`.
- Business failures should surface clear message text.
- Keep endpoint grouping style consistent:
  - admin endpoints under `/admin/**`
  - user endpoints under `/user/**`

## Java Style Guidelines

- Use 4-space indentation.
- Keep braces and spacing consistent with existing files.
- Prefer small methods; extract private helpers for complex blocks.
- Minimize nested conditionals where possible.
- Keep method names descriptive and business-oriented.

## Imports Guidelines

- Prefer explicit imports for new code.
- Keep consistency with file-local style when touching legacy files.
- Remove unused imports in files you modify.
- Do not reorder imports across untouched files.

## Naming Guidelines

- Class names: `PascalCase`
- Methods/fields: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Mapper XML IDs should align with mapper method names.
- Keep package naming under `com.sky...`.

## Types and Data Handling

- Monetary values should use `BigDecimal`.
- Time fields should use `LocalDateTime` (existing project pattern).
- Respect nullability semantics in DTO/entity mapping.
- Reuse existing constants for status/state codes where available.
- Avoid introducing magic numbers; extract constants.

## Error Handling Rules

- Prefer domain exceptions extending `BaseException` for business errors.
- Reuse existing exception types when semantically appropriate.
- Reuse `MessageConstant` for stable error messages.
- Let `GlobalExceptionHandler` shape API error output.
- Do not swallow exceptions silently.
- For external API failures, log clearly and follow explicit fallback strategy.

## Logging Rules

- Use `@Slf4j` and structured placeholder logging (`{}`), not string concat.
- Log key IDs and branch decisions at info/warn levels.
- Avoid logging secrets, tokens, AK/SK, passwords.
- Keep noisy debug logs out of production paths unless necessary.

## SQL and Mapper Rules

- Prefer explicit column lists in INSERT and SELECT.
- Use dynamic XML (`<if>`, `<where>`, `<set>`) for optional conditions.
- Keep mapper params aligned with entity/DTO field names.
- Be careful with null and empty-string checks in XML conditions.

## Config and Secrets

- Never commit real credentials to git.
- `application-dev.yml` is local-only and gitignored.
- When adding config keys, update:
  - `application.yml` defaults
  - `application-dev.yml.template` placeholders
- Keep third-party keys under `sky.*` namespaced properties.

## Agent Change Discipline

- Make minimal, task-scoped edits.
- Do not refactor unrelated modules opportunistically.
- Preserve backward compatibility unless task requires a break.
- Validate compile after changes before finishing.
- Mention any environment-dependent validation gaps explicitly.

## Cursor and Copilot Rules Check

The following rule files were checked:

- `.cursor/rules/` -> not found
- `.cursorrules` -> not found
- `.github/copilot-instructions.md` -> not found

No additional Cursor/Copilot repository rule files are currently present.
This `AGENTS.md` is the canonical in-repo agent guidance.
