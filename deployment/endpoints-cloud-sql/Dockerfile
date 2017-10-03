# https://github.com/GoogleCloudPlatform/openjdk-runtime
FROM gcr.io/google_appengine/openjdk8

RUN apt-get update \
    && apt-get -y -q upgrade \
    && rm -rf /var/lib/apt/lists/*

ADD ./build/libs/endpoints-cloud-sql-0.9.64-SNAPSHOT.jar /todolist/endpoints-cloud-sql-0.9.64-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/todolist/endpoints-cloud-sql-0.9.64-SNAPSHOT.jar"]
