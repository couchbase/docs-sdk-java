.PHONY: build

CB_EDITION ?=couchbase/server:enterprise-7.0.0
CB_BUILD ?=beta
LOCAL_IMAGE_NAME=cb7-sdk3-java-ub20

TEST_NAME=""

# --------------------------
# BUILD
# --------------------------
build:
	$(info ************ BUILDING DOC EXAMPLES ************)
	@mvn compile

# -------------------------------------------
# DOCKER
# -------------------------------------------
cb-build:
	@docker build --build-arg CB_EDITION=${CB_EDITION} --build-arg CB_BUILD=${CB_BUILD} -t ${LOCAL_IMAGE_NAME} -f test/Dockerfile .

# Run couchbase server+sdk container. Note that this runs with the `-rm` option, 
# which will ensure the container is deleted when stopped.
cb-start:
	@docker run -t --rm -v ${PWD}:/modules -d --name cb-test -p 8091-8096:8091-8096 ${LOCAL_IMAGE_NAME}
	@docker exec -t cb-test bin/bash -c "/init-couchbase/init.sh"
	@docker exec -t cb-test bin/bash -c "/init-couchbase/init-buckets.sh"

# Run all tests
tests:
	@cd test && npm install
	@docker exec -t cb-test bin/bash -c "cd modules && bats -T ./test"

# Run a single test
# i.e make TEST_NAME="Analytics.java" single-test
single-test:
	@cd test && npm install
	@docker exec -t cb-test bin/bash -c "cd modules && bats -T ./test -f ${TEST_NAME}"
	
# Stop the server container
cb-stop:
	@docker stop cb-test
