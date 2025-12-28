# 1. Start with a lightweight Linux that has Java 21 installed
FROM eclipse-temurin:21-jdk-alpine

# 2. Create a folder inside the container
WORKDIR /app

# 3. Copy the JAR file from your laptop into the container
COPY target/*.jar app.jar

# 4. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]