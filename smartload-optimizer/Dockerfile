FROM gradle:8.14.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew build -x test --no-daemon