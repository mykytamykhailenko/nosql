FROM sbtscala/scala-sbt:eclipse-temurin-11.0.15_1.7.1_2.13.8 AS build

WORKDIR /home/build

COPY common     ./common
COPY phantom    ./phantom
COPY project    ./project
ADD  build.sbt  .

RUN sbt phantom/universal:packageZipTarball

WORKDIR phantom/target/universal

RUN mv phantom-1.0.tgz phantom.tgz

FROM eclipse-temurin:11-jre-alpine

RUN apk update && apk add bash

RUN addgroup -S play
RUN adduser -S play -G play

USER play

WORKDIR /home/play

COPY --from=build --chown=play:play /home/build/phantom/target/universal/phantom.tgz ./phantom.tgz

ENV TOP_LEVEL_COMPONENT=1

RUN tar -xzf phantom.tgz --strip-components ${TOP_LEVEL_COMPONENT} && rm phantom.tgz

EXPOSE 9000

ENTRYPOINT bin/phantom -Dplay.http.secret.key=$(head -c 32 /dev/urandom | base64)


