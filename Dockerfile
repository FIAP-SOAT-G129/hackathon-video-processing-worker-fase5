FROM maven:3.9.9-eclipse-temurin-21-jammy AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN useradd -u 10001 -r -g root app \
    && chown -R app:root /app

USER app

ENTRYPOINT ["java", "-jar", "app.jar"]
