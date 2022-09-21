FROM sbtscala/scala-sbt:eclipse-temurin-11.0.15_1.7.1_2.13.8 AS build

WORKDIR /home/build

COPY common     ./common
COPY hbase      ./hbase
COPY project    ./project
ADD  build.sbt  .

RUN sbt hbase/universal:packageZipTarball

WORKDIR hbase/target/universal

RUN mv hbase-1.0.tgz hbase.tgz

FROM eclipse-temurin:11-jre-alpine

RUN apk update && apk add bash

RUN addgroup -S play
RUN adduser -S play -G play

USER play

WORKDIR /home/play

COPY --from=build --chown=play:play /home/build/hbase/target/universal/hbase.tgz ./hbase.tgz

RUN tar -xzf hbase.tgz --strip-components 1 && rm hbase.tgz

EXPOSE 9000

ENTRYPOINT bin/hbase -Dplay.http.secret.key=$(head -c 32 /dev/urandom | base64)





