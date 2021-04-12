setup() {
    load 'node_modules/bats-support/load'
    load 'node_modules/bats-assert/load'
}

function runExample() {
    run mvn compile exec:java -Dexec.mainClass=$1 -Dexec.cleanupDaemonThreads=false
}
