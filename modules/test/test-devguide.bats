#!./test/libs/bats/bin/bats

load 'test/test_helper.bash'

@test "[devguide] - BulkGet.java" {
    runExample BulkGet 
    assert_success
}

@test "[devguide] - BulkInsert.java" {
    runExample BulkInsert
    assert_success
}

@test "[devguide] - CasCheckAndSet.java" {
    runExample CasCheckAndSet
    assert_success
}

@test "[devguide] - Cloud.java" {
    skip "Example requires a cloud endpoint. Unable to run."

    runExample Cloud
    assert_success
}

@test "[devguide] - CloudConnect.java" {
    skip "Example requires a cloud endpoint. Unable to run."

    runExample CloudConnect
    assert_success
}

@test "[devguide] - ConnectingCertAuth.java" {
    runExample ConnectingCertAuth
    assert_success
}

@test "[devguide] - ConnectingSsl.java" {
    skip "Example requires a keystore to be configured."

    runExample ConnectingSsl
    assert_success
}

@test "[devguide] - ConnectionBase.java" {
    runExample ConnectionBase
    assert_success
}

@test "[devguide] - Counter.java" {
    runExample Counter
    assert_success
}

@test "[devguide] - Durability.java" {
    runExample Durability
    assert_success
}

@test "[devguide] - Expiration.java" {
    runExample Expiration
    assert_success
}

@test "[devguide] - FieldEncryptionAES.java" {
    skip "Example requires a keystore to be configured."

    runExample FieldEncryptionAES
    assert_success
}

@test "[devguide] - HealthCheckExample.java" {
    runExample HealthCheckExample
    assert_success
}

@test "[devguide] - QueryConsistency.java" {
    runExample QueryConsistency
    assert_success
}

@test "[devguide] - QueryCriteria.java" {
    runExample QueryCriteria
    assert_success
}

@test "[devguide] - QueryPlaceholders.java" {
    runExample QueryPlaceholders
    assert_success
}

@test "[devguide] - QueryPrepared.java" {
    runExample QueryPrepared
    assert_success
}

@test "[devguide] - Retrieving.java" {
    runExample Retrieving
    assert_success
}

@test "[devguide] - Updating.java" {
    runExample Updating
    assert_success
}
