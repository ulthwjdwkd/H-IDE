FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Python 설치
RUN apt-get update && \
    apt-get install -y python3 python3-pip

# Node.js 설치
RUN apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get install -y nodejs

# Copy the JAR file to the working directory
COPY build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
