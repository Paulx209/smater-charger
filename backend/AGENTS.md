# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/smartcharger/` is the main codebase, organized by layer: `controller/`, `service/`, `service/impl/`, `repository/`, `entity/`, `dto/`, and `common/` (config, security, exceptions, filters).
- `src/main/resources/` holds runtime configuration and data assets: `application.properties`, `schema.sql`, and Flyway migrations in `db/migration/`.
- `src/main/java/com/smartcharger/task/` contains scheduled jobs.
- Build output goes to `target/`. Do not edit generated files.

## Build, Test, and Development Commands
- `mvn clean package` builds the JAR (runs tests by default).
- `mvn test` runs the test suite only.
- `mvn spring-boot:run` starts the API locally (default base path `/api`).

## Coding Style & Naming Conventions
- Java 17, Spring Boot 3.2.x. Follow existing style: 4-space indentation, braces on the same line.
- Class names are `PascalCase` (e.g., `ChargingPileController`), methods/fields `camelCase`.
- Keep package conventions aligned with existing layout (controller/service/repository/entity/dto/common).
- Lombok is used for boilerplate; prefer consistency with nearby classes.

## Testing Guidelines
- Testing dependencies are provided via `spring-boot-starter-test` (JUnit 5).
- Place tests under `src/test/java/...` mirroring production packages.
- Name tests `*Test` (e.g., `AuthServiceTest`) and keep them focused on one unit or flow.
- Current repo has no tests yet; add or update tests with new features when feasible.

## Commit & Pull Request Guidelines
- Commit messages follow a conventional pattern: `feat(scope): ...` or `fix(scope): ...` (e.g., `feat(backend/user): add admin APIs`).
- Keep commits scoped and descriptive; avoid mixing frontend/backend changes in one commit.
- PRs should include: concise description, linked issue (if any), and key API changes or migrations.

## Configuration & Local Dependencies
- Update `src/main/resources/application.properties` for local DB/Redis credentials.
- Requires MySQL and Redis running locally; schema/migrations live in `src/main/resources/schema.sql` and `src/main/resources/db/migration/`.
