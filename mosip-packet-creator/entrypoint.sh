#!/bin/bash

sleep 5
export DOCKER_HASH_ID=$( kubectl get pod "$HOSTNAME" -n "$NS" -o jsonpath='{.status.containerStatuses[*].imageID}' | sed 's/ /\n/g' | grep -v 'istio' | sed 's/docker\-pullable\:\/\///g' )
if [[ -z $DOCKER_HASH_ID ]]; then
  echo "DOCKER_HASH_ID IS EMPTY;EXITING";
  exit 1;
fi
echo "DOCKER_HASH_ID ; $DOCKER_HASH_ID"

files=$( find /tmp/profile_resource -type f )
for file in $files; do
  dir_name=$(dirname $file | sed 's/\/tmp\///g');
  file_name=$(basename $file)
  echo "DIRECTORY : $dir_name and MOUNT PATH : $mountPath/$dir_name";
  mkdir -p "$mountPath/$dir_name";
  cp "$file" "$mountPath/$dir_name/$file_name";
done

java --version
java -Dfile.encoding=UTF-8  -jar dslrig-packetcreator-*.jar --spring.config.location=./config/application.properties
