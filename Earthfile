VERSION 0.8

build:
    # JDK version has to match everywhere - also change in pom.xml and in build-jre-distributions.sh!
    FROM maven:3.9.9-eclipse-temurin-23-alpine
    WORKDIR /project
    COPY .git .git
    COPY pom.xml ./
    COPY src src
    RUN TZ=Europe/Berlin xvfb-run mvn clean verify -U --no-transfer-progress
    RUN ./build-jre-distributions.sh
    SAVE ARTIFACT target AS LOCAL target

build-and-release-on-github:
    ARG --required GITHUB_TOKEN
    ARG PATTERN_TO_RELEASE
    BUILD +build
    FROM ubuntu:latest
    WORKDIR /project
    COPY .git .git
    COPY +build/target target
    RUN apt-get update >/dev/null 2>&1 && apt-get -y install curl gpg >/dev/null 2>&1
    RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | gpg --dearmor -o /usr/share/keyrings/githubcli-archive-keyring.gpg
    RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null
    RUN apt-get update >/dev/null 2>&1 && apt-get -y install gh >/dev/null 2>&1
    RUN --push export release_timestamp=$(date '+%Y-%m-%d @ %H:%M'); \
               export release_timestamp_terse=$(date '+%Y-%m-%d-%H-%M'); \
               export release_hash_short=$(git rev-parse --short HEAD); \
               export release_hash=$(git rev-parse HEAD); \
               export tag=release-$release_timestamp_terse-$release_hash_short; \
               echo TIMESTAMP: $release_timestamp; \
               echo HASH: $release_hash; \
               echo TAG: $tag; \
               if [ -n "$PATTERN_TO_RELEASE" ]; then \
                   export files=$(ls $PATTERN_TO_RELEASE); \
                   echo FILES: $files; \
               else \
                   unset files; \
                   echo NO FILES GIVEN; \
               fi; \
               gh release create $tag --target $release_hash --title "Release $release_timestamp" --notes "built from commit $release_hash_short" $files

