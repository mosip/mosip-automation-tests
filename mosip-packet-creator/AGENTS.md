# AGENTS.md — mosip-packet-creator

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

A Spring Boot service ("Packet Utility" / "Centralized Packet Creator")
that generates and uploads MOSIP registration packets, which the
`mosip-acceptance-tests` DSL orchestrator then consumes for E2E tests.

## Layout

- `pom.xml` — Spring Boot 3.2.3 parent, artifact `dslrig-packetcreator`.
  Depends on `io.mosip.testrig.dslrig.dataprovider:dslrig-dataprovider`
  (the `mosipTestDataProvider` module) — **build that module first**.
- `src/main/resources/dockersupport/centralized/mosip-packet-creator/` —
  the local run/deploy bundle: biometric device sample data
  (`Biometric Devices/`), profile resources (`profile_resource/`),
  registration-client config (`config/application.properties`,
  `config/error-codes.properties`), and Windows batch launchers
  (`run_build_packetutility.bat`, `run_centralized_packet_creator.bat`).
- `Dockerfile` / `entrypoint.sh` / `build.bat` at the module root — Docker
  and local build/copy helpers.
- `lib/` — vendored jars referenced directly by the build.

## How to run

### Local build

Build `mosipTestDataProvider` first (this module depends on it), then this
module:

```bash
cd mosipTestDataProvider && mvn clean install -Dgpg.skip
cd ../mosip-packet-creator && mvn clean install -Dgpg.skip
```

This mirrors `src/main/resources/dockersupport/centralized/mosip-packet-creator/run_build_packetutility.bat`,
which builds `mosipTestDataProvider` then this module, then copies the
resulting jar into a `centralized/mosip-packet-creator` deployment folder.

### Local run (packet utility)

1. Copy the `centralized` folder from
   `src/main/resources/dockersupport` to your deployment location.
2. Fill in the blank fields in
   `centralized/mosip-packet-creator/config/application.properties`
   (`mosip.test.regclient.machineid`, `mosip.test.baseurl`,
   `mosip.test.regclient.userid`, `mosip.test.regclient.password`, etc.)
   for your target environment. **Do not commit these filled-in values.**
3. Run the built jar, pointing Spring at that properties file. `-D`
   system properties must precede `-jar`:

   ```bash
   java -Xss8m -Dfile.encoding=UTF-8 \
     -jar dslrig-packetcreator-1.5.0.jar \
     --spring.config.location=file:///path/to/centralized/mosip-packet-creator/config/application.properties
   ```

   (`run_centralized_packet_creator.bat` in this repo runs a debug variant
   of this command with `-Xdebug -Xrunjdwp:...` and redirects output to
   `PacketUtilityRunlog.txt` — that log file is a local run artifact, not
   something to commit.)
4. Verify it's up at `http://localhost:8080/v1/packetcreator/swagger-ui.html#/`.
5. On failure, check the log at
   `centralized/mosip-packet-creator/PacketUtilityRunlog.txt`.

### Docker

`Dockerfile` builds on `mosipid/openjdk-21-jre:21.0.4`, copies
`build_files/` and `profile_resource/` into the image, and runs via
`entrypoint.sh`. Docker build/publish is driven by
`.github/workflows/packetcreator.yaml`, which only triggers its build job
for pushes/PRs touching `mosip-packet-creator/**`.

## Configuration

- `src/main/resources/dockersupport/centralized/mosip-packet-creator/config/application.properties`
  ships with credential and machine-id fields intentionally blank (e.g.
  `mosip.test.regclient.password =`, `mosip.test.regclient.secretkey=`).
  Fill these locally for your environment; never commit real values back.
- `resource/privatekeys/` under the same `dockersupport` tree already
  contains a registration-client key file checked into this repo's
  history. Do not add further real private keys here without maintainer
  confirmation.

## Agent rules

### Do

1. Build `mosipTestDataProvider` before this module — the Maven reactor
   will fail otherwise since `dslrig-packetcreator` depends on
   `dslrig-dataprovider`.
2. Keep `-D` system properties before `-jar` in any Java command you write
   for this module.
3. Treat `PacketUtilityRunlog.txt` and any `dslrig-packetcreator-*.jar` in
   the `dockersupport/centralized` tree as local run output — don't add
   them to a commit.

### Do not

1. Do not commit filled-in values for the blank credential fields in
   `config/application.properties`.
2. Do not assume the `centralized` folder's `.bat` scripts are
   cross-platform — they are Windows batch scripts with hardcoded local
   paths (e.g. `D:\centralized\mosip-packet-creator`); treat the paths as
   examples to adapt, not literal instructions.
