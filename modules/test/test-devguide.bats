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
    assert_output --partial "Connected to the cluster, opened bucket travel-sample"
}

@test "[devguide] - Counter.java" {
    runExample Counter
    assert_success
    assert_output --partial "increment Delta=20, Initial=100. Current value is: 101"
    assert_output --partial "increment Delta=1. Current value is: 102"
    assert_output --partial "decrement Delta=50. Current value is: 52"
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
    assert_output --partial "Doc Id: null, Name: [\"Brass\",\"Doorknob\"], Email: brass.doorknob@juno.com"
}

@test "[devguide] - QueryCriteria.java" {
    runExample QueryCriteria
    assert_success
    assert_output --partial "{\"airportname\":\"Reno Tahoe Intl\",\"country\":\"United States\",\"city\":\"Reno\"}"
    assert_output --partial "{\"airportname\":\"Reno International Airport\",\"country\":\"United States\",\"city\":\"Reno\"}"
    # the error we are asserting is expected 
    assert_output --partial "com.couchbase.client.core.error.ParsingFailureException: Parsing of the input failed"
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
    assert_output --partial "GetResult{content={\"foo\":\"bar\"}"
}

@test "[devguide] - Updating.java" {
    runExample Updating
    assert_success
    assert_output --partial "GetResult{content={\"mutation\":true,\"topic\":\"storing\",\"update\":\"something\"}"
}
