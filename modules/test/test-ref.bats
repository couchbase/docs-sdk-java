#!./test/libs/bats/bin/bats

load 'test_helper'

@test "[ref] - ClientSettingsExample.java" {
    runExample ClientSettingsExample
    assert_success
    assert_output --partial "Done."
}

@test "[ref] - DataStructuresExample.java" {
    runExample DataStructuresExample
    assert_success
    assert_output --partial "Done."
}
