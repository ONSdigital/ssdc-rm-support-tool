FROM eclipse-temurin:17-jre-alpine

CMD ["java", "-jar", "/opt/ssdc-rm-support-tool.jar"]

RUN addgroup --gid 1000 supporttool && adduser --system --uid 1000 supporttool supporttool
USER supporttool

COPY target/ssdc-rm-support-tool*.jar /opt/ssdc-rm-support-tool.jar
