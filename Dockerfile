# Stage 1: Build the project
FROM maven:3.8.6-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and other necessary files to cache dependencies
COPY pom.xml ./

# Download Maven dependencies (this will speed up the next build if the dependencies don’t change)
RUN mvn clean install -DskipTests

# Copy the rest of the application code to the container
COPY . .

# Build the Spring Boot app (creates a bootable jar)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

# Expose the port your Spring Boot application will run on
EXPOSE 8080

# Copy the built .jar file from the build stage
COPY --from=build /app/target/*.jar /app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
