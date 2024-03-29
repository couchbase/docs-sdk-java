#!./test/libs/bats/bin/bats

load 'test_helper'

@test "[concept-docs] - BucketsAndClustersExample.java" {
    runExample BucketsAndClustersExample
    assert_success
}

@test "[concept-docs] - CollectionsExample.java" {
    runExample CollectionsExample
    assert_success
}

@test "[concept-docs] - CompressionExample.java" {
    runExample CompressionExample
    assert_success
}

@test "[concept-docs] - DataModelExample.java" {
    runExample DataModelExample
    assert_success
}

@test "[concept-docs] - HealthCheckConcepts.java" {
    runExample HealthCheckConcepts
    assert_success
}

@test "[concept-docs] - N1qlQueryExample.java" {
    runExample N1qlQueryExample
    assert_success
}

@test "[concept-docs] - XattrExample.java" {
    runExample XattrExample
    assert_success
}

@test "[concept-docs] - DocumentsExample.java" {
    runExample DocumentsExample
    assert_success
}
