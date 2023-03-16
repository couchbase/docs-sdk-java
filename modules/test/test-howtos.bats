#!./test/libs/bats/bin/bats

load 'test_helper'

@test "[howtos] - Analytics.java" {
    runExample Analytics
    assert_success
}

@test "[howtos] - AsyncOperations.java" {
    runExample AsyncOperations
    assert_success
}

@test "[howtos] - Auth.java" {
    runExample Auth
    assert_success
}

@test "[howtos] - Cas.java" {
    runExample Cas
    assert_success
}

@test "[howtos] - ClusterExample.java" {
    runExample ClusterExample
    assert_success
}

@test "[howtos] - CollectingInformationAndLogging.java" {
    runExample CollectingInformationAndLogging
    assert_success
}

@test "[howtos] - CollectionManagerExample.java" {
    runExample CollectionManagerExample
    assert_success
}

@test "[howtos] - EncryptingUsingSDK.java" {
    skip "Example requires a keystore, needs further investigation."

    runExample EncryptingUsingSDK
    assert_success
}

@test "[howtos] - ErrorHandling.java" {
    skip "Need further investigation on how to check expected exceptions."

    runExample ErrorHandling
    assert_success
}

@test "[howtos] - HealthCheck.java" {
    runExample HealthCheck
    assert_success
}

@test "[howtos] - Import.java" {
    runExample Import
    assert_success
}

@test "[howtos] - Json.java" {
    runExample Json
    assert_success
}

@test "[howtos] - KvOperations.java" {
    runExample KvOperations
    assert_success
}

@test "[howtos] - managing_connections.java" {
    runExample managing_connections
    assert_success
}

@test "[howtos] - ManagingConnections.java" {
    skip "Example requires certificates and multiple nodes, unable to test."

    runExample ManagingConnections
    assert_success
}

@test "[howtos] - Metrics.java" {
    skip "Example seems to hang when executed, needs further investigation."

    runExample Metrics
    assert_success
}

@test "[howtos] - Queries.java" {
    runExample Queries
    assert_success
}

@test "[howtos] - Search.java" {
    runExample Search
    assert_success

    #  example tag: simple
    assert_output --partial "Found row"
    assert_output --partial "id='hotel_26223'"

    #  example tag: squery
    assert_output --partial "Document Id: hotel_20420"
    assert_output --partial "\"description\":\"Swimming Pool, Restaurant.\""

    # example tag: simplereactive
    assert_output --partial "Found reactive row"
    assert_output --partial "id='hotel_26223'"
}

@test "[howtos] - SimpleQuery.java" {
    runExample SimpleQuery
    assert_success
}

@test "[howtos] - SimpleQueryCloud.java" {
    skip "Example requires a cloud endpoint. Unable to run."

    runExample SimpleQueryCloud
    assert_success
}

@test "[howtos] - SubDocument.java" {
    runExample SubDocument
    assert_success
}

@test "[howtos] - Tracing.java" {
    skip "Example fails after upgrading to SDK 3.4, needs investigation."

    runExample Tracing
    assert_success
}

@test "[howtos] - TransactionsExample.java" {
    skip "Example doesn't compile, unable to test."

    runExample TransactionsExample
    assert_success
}

@test "[howtos] - Transcoding.java" {
    skip "Not a runnable class, we can be satisfied that it compiles for now."

    runExample Transcoding
    assert_success
}

@test "[howtos] - UserManagementExample.java" {
    runExample UserManagementExample
    assert_success
}

@test "[howtos] - Views.java" {
    runExample Views
    assert_success

    # example tag: views-meta
    assert_output --partial "Got total rows: 4"
    assert_output --partial "Got debug info as well"
}

@test "[howtos] - QueryIndexManagerExample.java" {
    runExample QueryIndexManagerExample
    assert_success
}

@test "[howtos] - ProvisioningResourcesBuckets.java" {
    runExample ProvisioningResourcesBuckets
    assert_success
}

@test "[howtos] - ProvisioningResourcesViews.java" {
    runExample ProvisioningResourcesViews
    assert_success
}
