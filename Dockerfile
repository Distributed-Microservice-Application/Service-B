FROM openjdk:21-slim

WORKDIR /app

# Create the files directory and ensure proper permissions
RUN mkdir -p /app/files && chmod 777 /app/files

COPY target/Service-B-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
