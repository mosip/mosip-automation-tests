# AGENTS.md — mosipTestDataProvider

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

A plain-Java library (Maven artifact `dslrig-dataprovider`) of test-data
and persona generators (demographics, biometrics, country/master data,
CSV helpers, MDS client) used by `mosip-packet-creator` to build
registration packets.

## Layout

- `pom.xml` — artifact `io.mosip.testrig.dslrig.dataprovider:dslrig-dataprovider`,
  Java 21 source/target. Non-standard Maven layout:
  `<sourceDirectory>src</sourceDirectory>` (not `src/main/java`), with
  `<testSourceDirectory>src/test/java</testSourceDirectory>`.
- `src/io/mosip/testrig/dslrig/dataprovider/` — the actual source tree
  (note: directly under `src/`, not `src/main/java/`), including
  `persona/PersonaDemographicsBuilder.java`,
  `preparation/MosipMasterData.java`, `BiometricDataProvider.java`,
  `CountryProvider.java`, `db/DBDataSource.java`, `mds/MDSClient.java`.
- `jar_files/` — third-party jars (`TSS.Java-0.3.0.jar`,
  `bcprov-jdk15on-1.57.jar`, `jna-4.4.0.jar`) installed into the local
  Maven repo via `addtorepo.bat` before they can be resolved as
  dependencies.
- `resources/` — data/resource files used by the providers at runtime.

## How to run / build

```bash
mvn clean install -Dgpg.skip
```

The build uses `maven-antrun-plugin` to `chmod` the produced jar
executable (`ugo+rx`) as a package-phase step (see `pom.xml`).

If a dependency resolution error mentions one of the `jar_files/*.jar`
artifacts, install it into the local repo first, e.g. (adjust
group/artifact/version to what the failing dependency actually needs —
check `pom.xml`'s `<dependencies>` for the exact coordinates):

```bash
mvn install:install-file -Dfile=jar_files/TSS.Java-0.3.0.jar \
  -DgroupId=example.group -DartifactId=example-artifact \
  -Dversion=0.3.0 -Dpackaging=jar
```

`addtorepo.bat` in this directory shows the same pattern for a
`dataprovider-1.2.1-SNAPSHOT.jar` — that specific command is stale (the
current `pom.xml` version is `1.5.0`), so treat it as an example of the
install pattern, not a command to run as-is.

This module is a dependency of `mosip-packet-creator`
(`dslrig-dataprovider` in that module's `pom.xml`), so build it first when
doing a full local build — see
[`../mosip-packet-creator/AGENTS.md`](../mosip-packet-creator/AGENTS.md).

## Configuration

This module has no runtime `application.properties` of its own — it's a
library consumed by `mosip-packet-creator`, which supplies configuration
(persona/master-data source paths, DB connection info for
`DBDataSource.java`) at the caller's runtime, documented in
`../mosip-packet-creator/AGENTS.md`.

## Agent rules

### Do

1. Remember the source root is `src/`, not the Maven-standard
   `src/main/java/` — point IDE/tooling module roots accordingly.
2. Build with `mvn clean install -Dgpg.skip` and confirm it succeeds
   before treating a change here as complete, especially since
   `mosip-packet-creator` depends on this module's local install.
3. When editing generator classes like `PersonaDemographicsBuilder.java`
   or `MosipMasterData.java`, check for callers in
   `mosip-packet-creator` before changing method signatures.

### Do not

1. Do not copy `addtorepo.bat`'s exact `install:install-file` coordinates
   verbatim — its version (`1.2.1-SNAPSHOT`) is stale versus the current
   `pom.xml` version (`1.5.0`).
2. Do not move source files into a new `src/main/java/` tree without
   updating `pom.xml`'s `<sourceDirectory>` — the build is intentionally
   configured for `src/` directly.
