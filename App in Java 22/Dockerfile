# Use a official base image of Java Runtime
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file into the image
COPY target/JVMParamsCompare-0.0.1-SNAPSHOT.jar /app

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/JVMParamsCompare-0.0.1-SNAPSHOT.jar"]
