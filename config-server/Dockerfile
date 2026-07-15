# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-22 AS builder

WORKDIR /build

# Copiar POM raiz e POM de todos os microservicos
COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/
COPY api-gateway/pom.xml api-gateway/
COPY inventory-service/pom.xml inventory-service/
COPY order-service/pom.xml order-service/
COPY product-service/pom.xml product-service/
COPY notification-service/pom.xml notification-service/
COPY config-server/pom.xml config-server/
COPY user-service/pom.xml user-service/

# Fazer download das dependencias (Docker layer caching)
RUN mvn dependency:go-offline -pl config-server -am

# Copiar codigo fonte apenas
COPY config-server/src config-server/src

# Compilar a aplicacao
RUN mvn clean package -pl config-server -am -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:22-jdk-alpine

WORKDIR /app

# Criar um utilizador nao-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar o JAR gerado
COPY --from=builder /build/config-server/target/*.jar app.jar

# Expor porta
EXPOSE 8888

ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","app.jar"]