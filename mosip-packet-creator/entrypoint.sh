#!/bin/bash

files=$( find /tmp/profile_resource -type f )
for file in $files; do
  dir_name=$(dirname $file | sed 's/\/tmp\///g');
  file_name=$(basename $file)
  echo "DIRECTORY : $dir_name and MOUNT PATH : $mountPath/$dir_name";
  mkdir -p "$mountPath/$dir_name";
  cp "$file" "$mountPath/$dir_name/$file_name";
done

java --version
java -Dfile.encoding=UTF-8  -jar mosip-packet-creator-*-SNAPSHOT.jar --spring.config.location=./config/application.properties
