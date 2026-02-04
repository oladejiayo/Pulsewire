# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY pulsewire-core/pom.xml pulsewire-core/
COPY pulsewire-control-plane/pom.xml pulsewire-control-plane/
COPY pulsewire-data-plane/pom.xml pulsewire-data-plane/
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B || true
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -pl pulsewire-control-plane -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/pulsewire-control-plane/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
