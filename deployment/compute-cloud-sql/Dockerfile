# https://github.com/GoogleCloudPlatform/openjdk-runtime
FROM gcr.io/google_appengine/openjdk8

RUN apt-get update \
    && apt-get -y -q upgrade \
    && rm -rf /var/lib/apt/lists/*

ADD ./build/libs/compute-cloud-sql.jar /todolist/compute-cloud-sql.jar

ENTRYPOINT ["java", "-jar", "/todolist/compute-cloud-sql.jar"]
