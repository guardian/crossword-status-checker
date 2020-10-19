#!/usr/bin/env bash

# update-lambda.sh STAGE PROFILE

set -e

test -z $1 && echo 'Stage missing' && exit 1
test -z $2 && echo 'Profile missing' && exit 1

STAGE=$1
PROFILE=$2

my_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

sbt assembly

jar_file=$(echo $my_dir/target/scala-2.11/crossword-status-checker-assembly*.jar)

aws lambda update-function-code \
  --function-name crosswords-status-checker-scheduling-$STAGE \
  --zip-file fileb://$jar_file \
  --profile $PROFILE \
  --region eu-west-1
