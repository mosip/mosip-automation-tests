#!/bin/bash

sleep 5
export DOCKER_HASH_ID=$( kubectl get pod "$HOSTNAME" -n "$NS" -o jsonpath='{.status.containerStatuses[*].imageID}' | sed 's/ /\n/g' | grep -v 'istio' | sed 's/docker\-pullable\:\/\///g' )
if [[ -z $DOCKER_HASH_ID ]]; then
  echo "DOCKER_HASH_ID IS EMPTY;EXITING";
  exit 1;
fi
echo "DOCKER_HASH_ID ; $DOCKER_HASH_ID"

# Find files and enclose their paths in quotes
files=$(find /tmp/profile_resource -type f -exec bash -c 'printf "%q\n" "$1"' _ {} \;)

# Set IFS to handle newline as the delimiter
IFS=$'\n'

# Iterate over the files
for file in $files; do
  dir_name=$(dirname $file | sed 's/\/tmp\///g');
  file="$( echo $file | sed 's/\\ / /g' )"
  file_name=$(basename $file)
  echo "file $file  file_name: $file_name"
  echo "DIRECTORY : $dir_name and MOUNT PATH : $mountPath/$dir_name";
  mkdir -p "$mountPath/$dir_name";
  cp "$file" "$mountPath/$dir_name/$file_name";
done

# Reset IFS to its default value
IFS=$' \t\n'

java --version
java -XX:+ExplicitGCInvokesConcurrent  -XX:+UseZGC -XX:+ZGenerational  -XX:MaxGCPauseMillis=200  -XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -XX:+UseCompressedOops -jar dslrig-packetcreator-*.jar --spring.config.location=./config/application.properties
