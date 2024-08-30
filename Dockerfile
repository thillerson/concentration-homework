FROM openjdk:17-jdk-slim

WORKDIR /app

COPY data data
COPY .env.production .env
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]