


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