FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","app.jar"]
