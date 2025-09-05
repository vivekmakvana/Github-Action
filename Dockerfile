# Use Ubuntu as base image
FROM ubuntu:22.04

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive

# Update package list and install OpenJDK 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Create app directory
WORKDIR /app

# Copy Java source file
COPY HelloWorldServer.java .

# Compile Java application
RUN javac HelloWorldServer.java

# Expose port 8080
EXPOSE 8080

# Run the web server
CMD ["java", "HelloWorldServer"]