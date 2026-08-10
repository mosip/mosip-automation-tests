# AGENTS.md — mosip-acceptance-tests

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

This module is the DSL orchestrator: it drives MOSIP's end-to-end (E2E)
test scenarios (registration, pre-registration + registration,
authentication) using Gherkin-style scenario definitions and TestNG.

## Layout

Maven multi-module reactor with 4 submodules (see `pom.xml` `<modules>`):

- `ivv-core` — shared core utilities for the orchestrator.
- `ivv-parser` — scenario/data parsing.
- `ivv-dg` — data-generation helpers.
- `ivv-orchestrator` — the runnable module. Packaged with the Maven
  Assembly plugin into
  `dslrig-ivv-orchestrator-<version>-jar-with-dependencies.jar`, with main
  class `io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner` (see
  `ivv-orchestrator/pom.xml`).

`ivv-orchestrator/src/main/resources/` holds the large bulk of test
configuration: `config/` (per-service `.properties`/`.json` files,
`dsl.properties`, `scenarios.feature`, `testng.xml` under `testngFile/`),
plus `centralized/`, `dbFiles/`, `idRepo/`, `idaData/`, `ivv_masterdata/`,
`kernel/`, `local/`, `preReg/`, `regproc/`, `syncdata/`.

## How to run

Build the orchestrator jar:

```bash
mvn clean install -Dgpg.skip
```

(Run from this directory, or from the repo root with
`mvn -pl mosip-acceptance-tests/ivv-orchestrator -am clean install -Dgpg.skip`.)

Run the built jar with the environment-specific system properties, as
documented in the repo root `README.md`. `-D` system properties must come
**before** `-jar`:

```bash
ENV_USER="qa"
ENV_ENDPOINT="https://qa.example.mosip.net"
java -Denv.user="$ENV_USER" -Denv.endpoint="$ENV_ENDPOINT" \
  -jar ivv-orchestrator/target/dslrig-ivv-orchestrator-1.5.0-jar-with-dependencies.jar
```

- `env.user` — environment name, e.g. `qa`, `qa2`, `dev`.
- `env.endpoint` — base URL of that environment.
- To run one scenario instead of the full suite, set
  `scenariosToExecute=<scenario-number>` in `dsl.properties`; leave it
  empty to run the full suite.
- Scenarios are authored in Gherkin at
  `ivv-orchestrator/src/main/resources/config/scenarios.feature` when
  `useGherkinScenarios=yes` is set in `dsl.properties`.
- After a run, TestNG's emailable report is written to
  `../testng-report/emailable-report.html` (relative to where the jar was
  run), and execution logs go to `src/logs/mosip-api-test.log` under this
  module.

### Docker / container run

`ivv-orchestrator/Dockerfile` builds on `mosipid/openjdk-21-jre:21.0.4` and
runs `entrypoint.sh`, which reads environment variables
(`USER`, `ENDPOINT`, `TESTLEVEL`, and others declared with `ENV` in the
Dockerfile) and maps them to Java `-D` system properties. Docker builds/publishes are
driven by `.github/workflows/dslorchestrator.yaml`, which only triggers
its Docker build/publish job for pushes/PRs touching
`mosip-acceptance-tests/**`.

## Configuration

- `ivv-orchestrator/src/main/resources/config/*.properties` and
  `dsl.properties` hold per-service endpoint, credential-placeholder, and
  scenario-selection settings. Several of these (e.g. `Kernel.properties`,
  which actually lives in the sibling `mosip-functional-tests` repo per
  the root `README.md`) must have their secret keys updated for your
  target environment before a run.
- Do not commit environment-specific secrets you fill into these files
  back to git — see the root `AGENTS.md`'s Configuration section.

## Agent rules

### Do

1. Keep `-D` system properties before `-jar` in any Java run command you
   write (see the corrected example above — the repo root `README.md` uses
   this order too).
2. Build with `mvn clean install -Dgpg.skip` before considering a change
   in this module complete.
3. Confirm which `.properties` file you're editing actually affects the
   scenario you're working on — this module ships many overlapping/backup
   config files (e.g. `application_backup_TBD.properties`,
   `applicationold.properties`, `scenarios_backup.json`); check
   `dsl.properties`/`config.properties` to see which are actually
   referenced before assuming a backup file is live.

### Do not

1. Do not commit filled-in credentials or environment secrets in any
   `config/*.properties` file.
2. Do not assume `entrypoint.sh`'s exact flag ordering is a template to
   copy for local runs — use the `-D...-jar` ordering shown above for any
   new documentation or scripts you write.
