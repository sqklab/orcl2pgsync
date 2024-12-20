FROM openjdk:11-jdk-slim as builder
WORKDIR application
COPY build/libs/application.jar ./
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:11-jre-slim
EXPOSE 9000
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
