ARG BASE_IMAGE=ubuntu:20.04

ARG CB_EDITION=couchbase/server:enterprise
ARG CB_BUILD=7.1.2
ARG CB_IMAGE=$CB_EDITION-$CB_BUILD

ARG CB_CLIENT_OS=ubuntu2004
ARG CB_CLIENT_OS_TYPE=focal

# SDK related images...

FROM adoptopenjdk AS adoptopenjdk

FROM maven AS maven

FROM $CB_IMAGE

ARG CB_EDITION
ARG CB_VERSION
ARG CB_IMAGE

ARG CB_CLIENT_OS
ARG CB_CLIENT_OS_TYPE

ENV TZ=America/Los_Angeles
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get update && \
    apt-get install -y \
    git curl wget jq unzip zip \
    build-essential cmake libssl-dev \
    atop htop psmisc strace time \
    vim npm

# ------------------------------------------------------

COPY --from=adoptopenjdk /opt/java /opt/java

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$PATH:/opt/java/openjdk/bin"

RUN javac --version && \
    java --version

# Copy init-couchbase files into image.

RUN mkdir -p /init-couchbase
COPY modules/test/scripts/init-couchbase /init-couchbase
RUN chmod +x /init-couchbase/*.sh

# Append to /opt/couchbase/etc/couchbase/static_config...

RUN if [ ! -d /opt/couchbase/etc/couchbase ]; then mkdir -p /opt/couchbase/etc/couchbase; fi \
    && cat /init-couchbase/init-static-config.txt >> \
    /opt/couchbase/etc/couchbase/static_config

# ------------------------------------------------
# SDK java.

# Copy maven shared files.

COPY --from=maven /usr/share/maven /usr/share/maven

RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
