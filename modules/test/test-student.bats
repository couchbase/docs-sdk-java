#!./test/libs/bats/bin/bats

load 'test_helper'

@test "[student] - ConnectStudent.java" {
    runExample ConnectStudent
    assert_success
}

@test "[student] - InsertStudent.java" {
    runExample InsertStudent
    assert_success
}

@test "[student] - InsertCourses.java" {
    runExample InsertCourses
    assert_success
}

@test "[student] - ArtSchoolRetriever.java" {
    runExample ArtSchoolRetriever
    assert_success
}

@test "[student] - ArtSchoolRetrieverParameters.java" {
    runExample ArtSchoolRetrieverParameters
    assert_success
}

@test "[student] - AddEnrollments.java" {
    runExample AddEnrollments
    assert_success
}
