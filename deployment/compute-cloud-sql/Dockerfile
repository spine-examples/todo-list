# https://github.com/GoogleCloudPlatform/openjdk-runtime
FROM gcr.io/google_appengine/openjdk:8

RUN apt-get update \
    && apt-get -y -q upgrade \
    && rm -rf /var/lib/apt/lists/*

COPY ./build/libs/compute-cloud-sql.jar /todolist/compute-cloud-sql.jar

ENTRYPOINT ["java", "-jar", "/todolist/compute-cloud-sql.jar"]
