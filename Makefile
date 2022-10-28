.PHONY: build

CB_EDITION ?=couchbase/server:enterprise
CB_BUILD ?=7.1.2
LOCAL_IMAGE_NAME=cb7-sdk3-java-ub20

TEST_NAME=""

CREATE_ANALYTICS_DATASETS ?=false

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
	@docker build \
		--build-arg CB_EDITION=${CB_EDITION} \
		--build-arg CB_BUILD=${CB_BUILD} \
		-t ${LOCAL_IMAGE_NAME} -f modules/test/Dockerfile .

# Run couchbase server+sdk container. Note that this runs with the `-rm` option, 
# which will ensure the container is deleted when stopped.
cb-start:
	@docker run -t --rm -v ${PWD}:/docs -d --name cb-test -p 8091-8096:8091-8096 ${LOCAL_IMAGE_NAME}
	@docker exec -t cb-test bin/bash -c "/init-couchbase/init.sh"
	@docker exec -t cb-test bin/bash -c "/init-couchbase/init-buckets.sh ${CREATE_ANALYTICS_DATASETS}"
	@docker exec -t cb-test bin/bash -c "cd docs/modules/test && npm install"

# Run all tests
tests:
	@docker exec -t cb-test bin/bash -c "cd docs/modules && bats -T ./test"

# Run a single test
# i.e make TEST_NAME="Analytics.java" single-test
single-test:
	@docker exec -t cb-test bin/bash -c "cd docs/modules && bats -T ./test -f ${TEST_NAME}"
	
# Stop the server container
cb-stop:
	@docker stop cb-test
