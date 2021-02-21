FROM maven:3.6.3-jdk-11 as builder
WORKDIR /app
COPY . .

RUN mvn install:install-file \
-Dfile=./opa-java-spring-client-0.3.2.jar \
-DgroupId=build.security \
-DartifactId=opa-java-spring-client \
-Dversion=0.3.1 \
-Dpackaging=jar \
-DgeneratePom=true
RUN mvn package

FROM openjdk:11-jdk-slim as runner
WORKDIR /app

COPY --from=builder /app/target/spring-boot-opa-demo-0.0.1-SNAPSHOT.jar /app/target/spring-boot-opa-demo-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "/app/target/spring-boot-opa-demo-0.0.1-SNAPSHOT.jar"]
