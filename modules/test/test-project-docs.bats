#!./test/libs/bats/bin/bats

load 'test_helper'

@test "[project-docs] - Migrating.java" {
    runExample Migrating
    assert_success
}

@test "[project-docs] - MigratingSDKCodeTo3n.java" {
    runExample MigratingSDKCodeTo3n
    assert_failure
    assert_output --partial "com.couchbase.client.core.error.ParsingFailureException: Parsing of the input failed"
}
