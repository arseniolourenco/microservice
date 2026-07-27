# ==============================================================================
# INVENTORY SERVICE
# Arquitetura Multi-stage com Spring Boot Layered JARs e base JRE
# ==============================================================================

# 1. Fase de Extração (Builder)
FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /builder
COPY inventory-service/target/*.jar app.jar
# Extrai as camadas nativamente do Spring Boot
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# 2. Fase de Execução (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Executar como utilizador não-root (Boas práticas de Segurança)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia as camadas ordenadas para tirar partido do Cache do Docker
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

EXPOSE 8383

# Inicia a aplicação injetando suporte a limites do container e JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -XX:+UseContainerSupport -jar app.jar"]