#!./test/libs/bats/bin/bats

load 'test/test_helper.bash'

@test "[hello-world] - DocParser.java" {
    runExample DocParser
    assert_success
}

@test "[hello-world] - StartUsing.java" {
    runExample StartUsing
    assert_success
    assert_output --partial "mike"
    assert_output --partial "[{\"greeting\":\"Hello World\"}]"
}

@test "[hello-world] - Overview.java" {
    runExample Overview
    assert_success
    assert_output --partial "RESULT: bar"
}

@test "[hello-world] - KvHelloWorldScoped.java" {
    runExample KvHelloWorldScoped
    assert_success
}

@test "[hello-world] - KvBulkHelloWorld.java" {
    runExample KvBulkHelloWorld
    assert_success
}

@test "[hello-world] - IndexHelloWorld.java" {
    runExample IndexHelloWorld
    assert_success
}
