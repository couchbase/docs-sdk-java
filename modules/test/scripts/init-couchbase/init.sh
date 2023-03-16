#!/bin/bash

CB_USER="${CB_USER:-Administrator}"
CB_PSWD="${CB_PSWD:-password}"
CB_HOST="${CB_HOST:-127.0.0.1}"
CB_PORT="${CB_PORT:-8091}"
CB_NAME="${CB_NAME:-docs-test}"

CB_SERVICES="${CB_SERVICES:-data,query,index,fts,analytics}"

CB_KV_RAMSIZE="${CB_KV_RAMSIZE:-1024}"
CB_INDEX_RAMSIZE="${CB_INDEX_RAMSIZE:-256}"
CB_FTS_RAMSIZE="${CB_FTS_RAMSIZE:-256}"
CB_EVENTING_RAMSIZE="${CB_EVENTING_RAMSIZE:-256}"
CB_ANALYTICS_RAMSIZE="${CB_ANALYTICS_RAMSIZE:-1024}"

CB_INDEXER_PORT="${CB_INDEXER_PORT:-9102}"

NO_COLOR="$(tput sgr0)"
BOLD_BLUE="$(tput bold)$(tput setaf 4)"

# exit immediately if a command fails or if there are unset vars
set -euo pipefail

# turn on bash's job control, used to bring couchbase-server
# back to the foreground after the node is configured
set -m

printf '%s%s%s\n' $BOLD_BLUE 'Starting couchbase-server...' $NO_COLOR
/entrypoint.sh couchbase-server &

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'Restarting couchbase-server...' $NO_COLOR
/opt/couchbase/bin/couchbase-server -k

sleep 5

printf '%s%s%s\n' $BOLD_BLUE 'Waiting for couchbase-server...' $NO_COLOR
until curl -s http://${CB_HOST}:${CB_PORT}/pools >/dev/null; do
    sleep 5
    printf '%s%s%s\n' $BOLD_BLUE 'Waiting for couchbase-server...' $NO_COLOR
done

printf '%s%s%s\n' $BOLD_BLUE 'Waiting for couchbase-server... ready' $NO_COLOR

if ! couchbase-cli server-list -c ${CB_HOST}:${CB_PORT} -u ${CB_USER} -p ${CB_PSWD} >/dev/null; then
    printf '%s%s%s\n' $BOLD_BLUE 'couchbase cluster-init...' $NO_COLOR
    couchbase-cli cluster-init \
        --services ${CB_SERVICES} \
        --cluster-name ${CB_NAME} \
        --cluster-username ${CB_USER} \
        --cluster-password ${CB_PSWD} \
        --cluster-ramsize ${CB_KV_RAMSIZE} \
        --cluster-index-ramsize ${CB_INDEX_RAMSIZE} \
        --cluster-fts-ramsize ${CB_FTS_RAMSIZE} \
        --cluster-eventing-ramsize ${CB_EVENTING_RAMSIZE} \
        --cluster-analytics-ramsize ${CB_ANALYTICS_RAMSIZE}
fi

sleep 3

curl -v -X POST -d @/init-couchbase/init-indexer.json \
    http://${CB_USER}:${CB_PSWD}@${CB_HOST}:${CB_INDEXER_PORT}/internal/settings?internal=ok

sleep 3

killall indexer

sleep 3

curl http://${CB_USER}:${CB_PSWD}@${CB_HOST}:${CB_INDEXER_PORT}/internal/settings?internal=ok | jq .

printf '%s%s%s\n' $BOLD_BLUE 'Installing bats test framework...' $NO_COLOR
npm install -g bats
export TERM=xterm-256color