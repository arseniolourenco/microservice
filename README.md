# Microservices E-Commerce Platform

Este projeto é uma plataforma de e-commerce baseada numa arquitetura de microserviços, desenvolvida utilizando **Java 21** e **Spring Boot 3.3.4** / **Spring Cloud 2023.0.3**. A arquitetura foi desenhada para ser escalável, resiliente e altamente observável.

## 🏗 Arquitetura

O sistema está dividido em três grandes blocos: **Infraestrutura**, **Core** e **Aplicações (Microserviços)**.

### Microserviços (Aplicações)
- **Order Service:** Responsável pela criação e gestão de encomendas. Utiliza o padrão *Outbox* para emitir eventos de forma fiável.
- **Inventory Service:** Gere o stock de produtos. Ouve eventos (Kafka) provenientes do Order Service.
- **Product Service:** Catálogo de produtos. Utiliza MongoDB para armazenamento primário e Redis para cache.
- **Notification Service:** Responsável por enviar notificações de forma assíncrona baseadas em mensagens Kafka.
- **User Service:** Gere o perfil e os detalhes dos utilizadores da plataforma.
- **Auth Service:** Wrapper sobre os serviços de autenticação do Keycloak.

### Componentes Core (Spring Cloud)
- **API Gateway:** Ponto de entrada único (Routing) na porta `8181`. Protegido via integração com Keycloak.
- **Discovery Server (Eureka):** Registo dinâmico e descoberta de microserviços na porta `8761`.
- **Config Server:** Fornece configurações centralizadas a todos os microserviços a partir de um repositório Git local (`./config-repo`) na porta `8888`.

### Infraestrutura & Bases de Dados
- **Message Broker:** Apache Kafka e Zookeeper.
- **CDC (Change Data Capture):** Debezium Connect (utilizado para o padrão Outbox).
- **Identity Provider (IAM):** Keycloak (OIDC/OAuth2).
- **Bases de Dados:**
  - **MySQL:** Utilizado pelos serviços de inventário (`inventoryDB`) e encomendas (`orderDB`).
  - **PostgreSQL:** Utilizado pelo Keycloak e para a implementação do padrão Outbox lido pelo Debezium.
  - **MongoDB:** Catálogo de produtos.
  - **Redis:** Cache distribuída.
- **Observabilidade:** Zipkin (Distributed Tracing), Prometheus (Métricas) e Grafana (Dashboards).

---

## 🚀 Como Executar o Projeto

O ambiente é orquestrado através do Docker Compose, estando dividido em três ficheiros para garantir a correta ordem de arranque.

### 1. Pré-requisitos
- Docker e Docker Compose instalados.
- Java 21 instalado (caso pretenda compilar localmente).
- Maven instalado (ou utilizar o wrapper `./mvnw`).

### 2. Configuração da Máquina Local (Host)
Para que o Keycloak funcione corretamente em desenvolvimento na sua máquina, atualize o ficheiro `hosts`:

**No Mac/Linux:**
```bash
sudo nano /etc/hosts
```
Adicione a seguinte linha:
```
127.0.0.1 keycloak
```

### 3. Arranque do Ambiente Docker

Deverá arrancar os ficheiros Docker Compose na ordem definida abaixo:

**Passo 1: Infraestrutura (Bases de Dados, Kafka, Observabilidade)**
```bash
docker-compose -f docker-compose.infra.yml up -d
```
*Aguarde até que os contentores estejam saudáveis (healthy), em especial as bases de dados e o Kafka.*

**Passo 2: Serviços Core (Config Server, Eureka, Gateway)**
```bash
docker-compose -f docker-compose.core.yml up -d
```
*O API Gateway e o Discovery dependem do Config Server.*

**Passo 3: Aplicações / Microserviços de Negócio**
Antes de iniciar as aplicações, caso tenha feito alterações de código, pode construir as imagens Docker utilizando o Jib:
```bash
./mvnw compile jib:build
```
Ou construir e arrancar os contentores:
```bash
docker-compose -f docker-compose.apps.yml up -d --build
```

---

## 🛠 Comandos Úteis

**Compilar e criar o pacote sem correr testes:**
```bash
./mvnw clean package -DskipTests
```

**Criar imagem de um serviço específico via Dockerfile:**
```bash
docker build -f Dockerfile -t notification-service .
```

**Parar todo o ambiente:**
```bash
docker-compose -f docker-compose.apps.yml down
docker-compose -f docker-compose.core.yml down
docker-compose -f docker-compose.infra.yml down
```

**Criar Bases de Dados e Privilégios no MySQL (se executado manualmente):**
```sql
CREATE DATABASE IF NOT EXISTS keycloak;
CREATE DATABASE IF NOT EXISTS inventoryDB;
CREATE DATABASE IF NOT EXISTS orderDB;

GRANT ALL PRIVILEGES ON keycloak.* TO 'mauro'@'%';
GRANT ALL PRIVILEGES ON inventoryDB.* TO 'mauro'@'%';
GRANT ALL PRIVILEGES ON orderDB.* TO 'mauro'@'%';
FLUSH PRIVILEGES;
```

---

## 📊 Portas Importantes

| Serviço | Porta Local |
| --- | --- |
| API Gateway | `8181` |
| Keycloak | `8080` |
| Eureka (Discovery) | `8761` |
| Config Server | `8888` |
| Grafana | `3000` |
| Prometheus | `9090` |
| Zipkin | `9411` |
| Kafka | `9092` |
