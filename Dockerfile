FROM eclipse-temurin

WORKDIR /app
# The font libraries are for the label we write to the get-image image
RUN apt-get update && apt-get install -y fontconfig libfreetype6

COPY data data
COPY .env.production .env
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]