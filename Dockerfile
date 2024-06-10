FROM eclipse-temurin:21

MAINTAINER artemistechnica.com

COPY build/libs/template.internal-api-java-0.0.1-SNAPSHOT.jar template.internal-api-java.jar

ENTRYPOINT ["java","-jar","/template.internal-api-java.jar"]