


mvn clean package -DskipTests
docker build -f Dockerfile -t notification-service .
docker-compose down
docker-compose up -d

On 
CREATE DATABASE IF NOT EXISTS keycloak;
CREATE DATABASE IF NOT EXISTS inventoryDB;
CREATE DATABASE IF NOT EXISTS orderDB;

GRANT ALL PRIVILEGES ON keycloak.* TO 'mauro'@'%';
GRANT ALL PRIVILEGES ON inventoryDB.* TO 'mauro'@'%';
GRANT ALL PRIVILEGES ON orderDB.* TO 'mauro'@'%';

FLUSH PRIVILEGES;


docker network connect microservices-network api-gateway
docker network inspect microservices-network

./mvnw compile jib:build


update host file on local machine 127.0.0.1 keycloak on mac
sudo nano /etc/hosts
127.0.0.1 keycloak
