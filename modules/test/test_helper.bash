setup() {
    load 'node_modules/bats-support/load'
    load 'node_modules/bats-assert/load'
}

function runExample() {
    # Current dir (in docker container): /docs/modules (echo $PWD >&3).
    # We need to cd into the root directory to access the `pom.xml`.
    cd ..

    # Now we can run the example.
    run mvn compile exec:java -Dexec.mainClass=$1 -Dexec.cleanupDaemonThreads=false
}
