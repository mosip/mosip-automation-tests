# AGENTS.md

## Repository Overview

This repo holds the end-to-end (E2E) test framework for MOSIP. It covers
registration, pre-registration + registration, and authentication flows.
It is a Maven multi-module project with three top-level modules, each with
its own build/run story:

- [`mosip-acceptance-tests`](mosip-acceptance-tests/AGENTS.md) — the DSL
  orchestrator that drives the E2E test scenarios (registration,
  authentication, etc.).
- [`mosip-packet-creator`](mosip-packet-creator/AGENTS.md) — a Spring Boot
  service that generates and uploads registration packets consumed by the
  E2E tests.
- [`mosipTestDataProvider`](mosipTestDataProvider/AGENTS.md) — a plain-Java
  library of test-data/persona generators used by the packet creator.

See each module's own `AGENTS.md` (linked above) for module-specific build,
run, and configuration details. This root file only covers what applies to
the whole repo.

## Technology Stack

- Java 21 (per `.github/workflows/automationtests.yaml`, which sets up JDK 21)
- Maven (multi-module reactor build; root `pom.xml` lists all three modules)
- Spring Boot 3.2.3 (used by `mosip-packet-creator`)
- TestNG (used across the acceptance-test modules)
- Docker (each of `mosip-acceptance-tests/ivv-orchestrator` and
  `mosip-packet-creator` ships a `Dockerfile`)
- Helm charts for Kubernetes deployment, under `helm/dslorchestrator` and
  `helm/packetcreator`

## Build & Test Commands

Build the full reactor from the repo root:

```bash
mvn clean install -Dgpg.skip
```

This matches what CI does (`.github/workflows/push-trigger.yml`, which calls
the shared `mosip/kattu` Maven build workflow with `SERVICE_LOCATION: ./`).

To build a single module, either `cd` into it and run the same command, or
use Maven's reactor module flag from the root:

```bash
mvn -pl mosip-packet-creator -am clean install -Dgpg.skip
```

The `-am` (also-make) flag builds required sibling modules (e.g.
`mosipTestDataProvider`) as part of the same reactor run. If you instead
`cd` into `mosip-packet-creator` and build it standalone, you must install
`mosipTestDataProvider` first — see that module's `AGENTS.md`.

There is no repo-wide single-command test runner outside of the Maven
`test` phase (`mvn test` per module) — the actual E2E test execution is a
separate, manual step described in each module's `AGENTS.md`.

## Configuration

- Module-specific runtime configuration (DB connections, environment URLs,
  registration-client credentials, biometric device files, etc.) lives
  inside each module and is documented in that module's `AGENTS.md`.
- **Never commit real credentials, machine keys, or environment secrets**
  into any `.properties` file under `src/main/resources`. Several
  properties files in this repo (for example
  `mosip-packet-creator/src/main/resources/dockersupport/centralized/mosip-packet-creator/config/application.properties`)
  ship with blank credential fields (`mosip.test.regclient.password =`,
  `mosip.test.regclient.secretkey=`, etc.). Keep these tracked files blank:
  copy the containing folder to an untracked deployment location first,
  then fill in real values only in that copy (see
  `mosip-packet-creator/AGENTS.md`'s "Local run" steps) and point the
  runtime at it (e.g. Spring's `--spring.config.location`). Never push
  filled-in values back to git.
- CI secrets (`OSSRH_USER`, `OSSRH_SECRET`, `OSSRH_TOKEN`, `GPG_SECRET`,
  `SLACK_WEBHOOK`) are injected by GitHub Actions from repository secrets —
  see `.github/workflows/automationtests.yaml` and
  `.github/workflows/push-trigger.yml`. Agents should never hardcode
  equivalents of these values in source or workflow files.

## Project Structure Notes

```text
.
├── mosip-acceptance-tests/   # DSL orchestrator (see its AGENTS.md)
│   ├── ivv-core/
│   ├── ivv-dg/
│   ├── ivv-orchestrator/     # produces the runnable orchestrator jar
│   └── ivv-parser/
├── mosip-packet-creator/     # Spring Boot packet-creation service (see its AGENTS.md)
├── mosipTestDataProvider/    # test-data/persona generator library (see its AGENTS.md)
├── deploy/                   # deployment assets (dslrig, packetcreator)
├── helm/                     # Helm charts (dslorchestrator, packetcreator)
├── docs/                     # DSL Scenario Authoring Guide, images
├── packet-utility-setup-linux.md
├── packet-utility-vscode.md
└── pom.xml                   # root reactor pom listing the 3 modules
```

`mosip-acceptance-tests` is itself a Maven multi-module project
(`ivv-core`, `ivv-parser`, `ivv-dg`, `ivv-orchestrator`); only
`ivv-orchestrator` produces a directly runnable artifact
(`dslrig-ivv-orchestrator-<version>-jar-with-dependencies.jar`, main class
`io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner`).

## Development Workflow

1. Fork the repo and branch from `master` (the branch CI publishes from and
   the branch GitHub reports as the default — see `Pull Request Guidelines`
   below for how this was verified).
2. Make your change in the relevant module.
3. Build the affected module(s) with `mvn clean install -Dgpg.skip` before
   opening a PR — this mirrors what `automationtests.yaml` runs in CI.
4. Do not commit generated artifacts: build output (`target/`), IDE files
   (`.idea`, `.classpath`, `.project`), or local run logs
   (e.g. `PacketUtilityRunlog.txt`) — these are already covered by the
   root and module `.gitignore` files, so let git tell you if something
   generated is about to be staged.

## Pull Request Guidelines

- Target branch: `master`. Confirm with `gh repo view
  mosip/mosip-automation-tests --json defaultBranchRef`, which reports the
  current default branch. Verify the current branch policy before opening
  a pull request, since it can change over time.
- CI (`.github/workflows/packetcreator.yaml`,
  `.github/workflows/dslorchestrator.yaml`) triggers on `pull_request` for
  every PR, and also on `push` to `master`, `develop`, `release*`, and
  `1.*`/`MOSIP*` branches. The packet-creator and DSL-orchestrator Docker
  build jobs are additionally scoped by `paths:` to only run when files
  under `mosip-packet-creator/**` or `mosip-acceptance-tests/**` change,
  respectively.
- Follow the existing commit convention seen in `git log` — a short,
  imperative summary line, optionally prefixed with the tracking issue or
  ticket ID (e.g. `[DSD-10610] ...`, `MOSIP-45365-docs: ...`).
- Sign off commits (`git commit -s`) where the project convention expects
  a DCO sign-off.

## Repository-Specific Considerations

- This repo mixes generated/local-run artifacts with source in a few
  module resource trees (e.g. `mosip-packet-creator`'s `dockersupport`
  folder holds both checked-in sample config and files a developer
  generates locally when running the utility, such as
  `PacketUtilityRunlog.txt`). Check `git status` before committing broad
  directory adds.
- The `mosip-packet-creator/src/main/resources/dockersupport/centralized/mosip-packet-creator/resource/privatekeys/`
  folder contains a registration-client key file that is already tracked
  in git history for this repo; do not add further private keys or
  environment-specific credential files here without confirming with a
  maintainer that they are meant to be public.
- `mosip-acceptance-tests/ivv-orchestrator` contains several `.temp-*`
  classpath-arg files checked into `src/` from prior local runs — these
  are historical artifacts already in the repo, not something to emulate;
  don't add new ones.

## Agent rules

### Do

1. Verify build/run commands against the actual `pom.xml`/workflow files in
   the module you're touching before writing documentation or commands.
2. Keep `-D` JVM system properties before `-jar` in any Java command you
   write or run, e.g. `java -Dfile.encoding=UTF-8 -jar app.jar`.
3. Run `mvn clean install -Dgpg.skip` for the affected module(s) before
   proposing a change is complete.
4. Check `git status` before committing to avoid staging build output,
   local run logs, or local credential edits.
5. Update the relevant module `AGENTS.md` (not just this root file) when
   you change that module's build, run, or config story.

### Do not

1. Do not commit real credentials, environment URLs, or machine-specific
   keys into any `application.properties`/`config.properties` file.
2. Do not target `develop` for PRs — it is stale relative to `master`; use
   `master`.
3. Do not add or edit workflow files under `.github/workflows/` without
   confirming the actual `paths:`/`branches:` triggers, since several
   workflows are scoped narrowly (e.g. `packetcreator.yaml` only fires
   Docker build steps for changes under `mosip-packet-creator/**`).
4. Do not assume a subfolder is "live" just because it exists in a local
   checkout — confirm with `git ls-tree <branch> --name-only` that it's
   actually tracked before documenting or relying on it.
