FROM europe-west2-docker.pkg.dev/ssdc-rm-liamtoozer-349808/docker/jdk17-mvn-node16-npm:latest
CMD ["/usr/local/openjdk-17/bin/java", "-jar", "/opt/ssdc-rm-support-tool.jar"]

RUN groupadd --gid 999 supporttool && \
    useradd --create-home --system --uid 999 --gid supporttool supporttool

RUN apt-get update && \
apt-get -yq install curl && \
apt-get -yq clean && \
rm -rf /var/lib/apt/lists/*

USER supporttool

ARG JAR_FILE=ssdc-rm-support-tool*.jar
COPY target/$JAR_FILE /opt/ssdc-rm-support-tool.jar