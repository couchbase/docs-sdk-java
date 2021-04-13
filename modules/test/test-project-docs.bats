#!./test/libs/bats/bin/bats

load 'test/test_helper.bash'

@test "[project-docs] - Migrating.java" {
    runExample Migrating
    assert_success
}

@test "[project-docs] - MigratingSDKCodeTo3n.java" {
    runExample MigratingSDKCodeTo3n
    assert_failure
    assert_output --partial "com.couchbase.client.core.error.ParsingFailureException: Parsing of the input failed"
}

@test "[project-docs] - Transactions.java" {
    skip "Example is not runnable, we can be satisfied that it compiles. Skipping for now."

    runExample TransactionsDemo
    assert_success
}
