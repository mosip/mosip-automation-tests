#!/bin/bash

# Legacy: copy bundled JSON scenario sheets into the mount when present (useExternalScenarioSheet=yes).
# Default Gherkin flow uses build_files/config/scenarios.feature; /tmp/scenarios is optional.
if [[ "${DEFAULT_SCENARIOS}" == "true" && -d /tmp/scenarios ]]; then
  while IFS= read -r -d '' file; do
    domain=$(printenv installation-domain)
    dir_name="${mountPathForScenario}/scenarios/"
    file_name=$(basename "$file" | sed "s/env/$domain/g")
    echo "DIRECTORY and MOUNT PATH : ${dir_name}${file_name}"
    mkdir -p "$dir_name"
    cp "$file" "${dir_name}${file_name}"
  done < <(find /tmp/scenarios -type f -print0)
fi

## Run DSL Orchestrator
java --version
java -jar -Denv.user="$USER" -Denv.endpoint="$ENDPOINT" -Denv.testLevel="$TESTLEVEL" dslrig-ivv-orchestrator-*-jar-with-dependencies.jar
