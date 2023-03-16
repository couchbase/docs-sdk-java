#!/bin/bash

# exit immediately if a command fails or if there are unset vars
set -euo pipefail

CREATE_ANALYTICS_DATASETS=$1

CB_USER="${CB_USER:-Administrator}"
CB_PSWD="${CB_PSWD:-password}"
CB_HOST=localhost

CB_BUCKET_RAMSIZE="${CB_BUCKET_RAMSIZE:-128}"

NO_COLOR="$(tput sgr0)"
BOLD_BLUE="$(tput bold)$(tput setaf 4)"
BOLD_GREEN="$(tput bold)$(tput setaf 2)"

printf '%s%s%s\n' $BOLD_BLUE 'couchbase-cli bucket-create travel-sample...' $NO_COLOR
/opt/couchbase/bin/couchbase-cli bucket-create \
    -c ${CB_HOST} -u ${CB_USER} -p ${CB_PSWD} \
    --bucket travel-sample \
    --bucket-type couchbase \
    --bucket-ramsize ${CB_BUCKET_RAMSIZE} \
    --bucket-replica 0 \
    --bucket-priority low \
    --bucket-eviction-policy fullEviction \
    --enable-flush 1 \
    --enable-index-replica 0 \
    --wait

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'cbimport travel-sample...' $NO_COLOR
/opt/couchbase/bin/cbimport json --format sample --verbose \
    -c ${CB_HOST} -u ${CB_USER} -p ${CB_PSWD} \
    -b travel-sample \
    -d file:///opt/couchbase/samples/travel-sample.zip

printf '%s%s%s\n' $BOLD_BLUE 'create airports dataset' $NO_COLOR
curl --fail -v -u ${CB_USER}:${CB_PSWD} -H "Content-Type: application/json" -d '{
    "statement": "CREATE DATASET airports ON `travel-sample` WHERE `type`=\"airport\";",
    "pretty":true,
    "client_context_id":"test"
}' http://${CB_HOST}:8095/analytics/service

printf '%s%s%s\n' $BOLD_BLUE 'Check if analytics datasets need to be built...' $NO_COLOR
# These are already setup in the official Couchbase Enterprise docker image.
# However, this is not the case for our internal dev image.
if [ $CREATE_ANALYTICS_DATASETS = true ]; then
    printf '%s%s%s\n' $BOLD_BLUE 'create scoped airport dataset' $NO_COLOR
    curl --fail -v -u ${CB_USER}:${CB_PSWD} -H "Content-Type: application/json" -d '{
        "statement": "ALTER COLLECTION `travel-sample`.`inventory`.`airport` ENABLE ANALYTICS;",
        "pretty":true,
        "client_context_id":"test"
    }' http://${CB_HOST}:8095/analytics/service

    curl --fail -v -u ${CB_USER}:${CB_PSWD} -H "Content-Type: application/json" -d '{
        "statement": "CONNECT LINK Local;",
        "pretty":true,
        "client_context_id":"test"
    }' http://${CB_HOST}:8095/analytics/service

else
    printf '%s%s%s\n' $BOLD_BLUE '...analytics datasets are not required.' $NO_COLOR
fi

printf '%s%s%s\n' $BOLD_BLUE 'create huge-dataset dataset' $NO_COLOR
curl --fail -v -u ${CB_USER}:${CB_PSWD} -H "Content-Type: application/json" -d '{
        "statement": "CREATE DATASET `huge-dataset` ON `travel-sample`;",
        "pretty":true,
        "client_context_id":"test"
}' http://${CB_HOST}:8095/analytics/service

printf '%s%s%s\n' $BOLD_BLUE 'sleep 10 to allow stabilization...' $NO_COLOR
sleep 10

echo
printf '%s%s%s\n' $BOLD_BLUE 'create travel-sample-index' $NO_COLOR
curl --fail -s -u ${CB_USER}:${CB_PSWD} -X PUT \
    http://${CB_HOST}:8094/api/index/travel-sample-index \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -d @/init-couchbase/travel-sample-index.json

echo
printf '%s%s%s\n' $BOLD_BLUE 'Waiting for travel-sample-index to be ready...' $NO_COLOR
until curl --fail -s -u ${CB_USER}:${CB_PSWD} http://${CB_HOST}:8094/api/index/travel-sample-index/count |
    jq -e '.count' | grep 917 >/dev/null; do # there are 917 docs to be processed in this index...
    echo "Waiting for travel-sample-index to be ready. Trying again in 10 seconds."
    sleep 10
done

printf '%s%s%s\n' $BOLD_BLUE 'couchbase-cli bucket-create student-bucket ...' $NO_COLOR
/opt/couchbase/bin/couchbase-cli bucket-create \
    -c ${CB_HOST} -u ${CB_USER} -p ${CB_PSWD} \
    --bucket student-bucket \
    --bucket-type couchbase \
    --bucket-ramsize ${CB_BUCKET_RAMSIZE} \
    --bucket-replica 0 \
    --bucket-priority low \
    --bucket-eviction-policy fullEviction \
    --enable-flush 1 \
    --enable-index-replica 0 \
    --wait

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'creating art-school-scope ...' $NO_COLOR
curl -X POST --fail -s -u ${CB_USER}:${CB_PSWD} \
    http://${CB_HOST}:8091/pools/default/buckets/student-bucket/scopes \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d name=art-school-scope

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'creating student record collection ...' $NO_COLOR
curl -X POST --fail -s -u ${CB_USER}:${CB_PSWD} \
    http://${CB_HOST}:8091/pools/default/buckets/student-bucket/scopes/art-school-scope/collections \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d name=student-record-collection

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'creating course record collection ...' $NO_COLOR
curl -X POST --fail -s -u ${CB_USER}:${CB_PSWD} \
    http://${CB_HOST}:8091/pools/default/buckets/student-bucket/scopes/art-school-scope/collections \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d name=course-record-collection

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'create student index ...' $NO_COLOR
curl --fail -v -u ${CB_USER}:${CB_PSWD} -H "Content-Type: application/json" -d '{
    "statement": "create primary index student_idx on `student-bucket`.`art-school-scope`.`student-record-collection`",
    "pretty":true,
    "client_context_id":"test"
}' http://${CB_HOST}:8093/query/service

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'create course index ...' $NO_COLOR
curl --fail -v -u ${CB_USER}:${CB_PSWD} -H "Content-Type: application/json" -d '{
    "statement": "create primary index course_idx on `student-bucket`.`art-school-scope`.`course-record-collection`",
    "pretty":true,
    "client_context_id":"test"
}' http://${CB_HOST}:8093/query/service

sleep 5

printf '%s%s%s\n' $BOLD_GREEN 'Done.' $NO_COLOR
