FROM eclipse-temurin:17-jdk-alpine

CMD ["/opt/java/openjdk/bin/java", "-jar", "/opt/ssdc-rm-support-tool.jar"]

RUN addgroup --gid 1000 supporttool && \
    adduser --system --uid 1000 supporttool supporttool
USER supporttool

ARG JAR_FILE=ssdc-rm-support-tool*.jar

COPY target/$JAR_FILE /opt/ssdc-rm-support-tool.jar