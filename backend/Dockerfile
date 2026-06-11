# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml .
# Resilient dependency resolution: retry on dropped connections (aliyun mirror
# occasionally returns truncated responses -> "Premature end of Content-Length").
ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dmaven.wagon.http.pool=false -Dhttp.keepAlive=false"
RUN mvn -s /usr/share/maven/ref/settings.xml -B dependency:go-offline || \
    mvn -s /usr/share/maven/ref/settings.xml -B dependency:go-offline
COPY src ./src
RUN mvn -s /usr/share/maven/ref/settings.xml -B package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV LANG C.UTF-8
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
