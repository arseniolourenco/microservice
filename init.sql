-- Create the Keycloak database
CREATE DATABASE IF NOT EXISTS keycloakDB;
CREATE DATABASE IF NOT EXISTS inventoryDB;

-- Create user 'mauro' and grant privileges for all databases
CREATE USER IF NOT EXISTS 'mauro'@'localhost' IDENTIFIED BY 'mauro007';
GRANT ALL PRIVILEGES ON *.* TO 'mauro'@'localhost' WITH GRANT OPTION;

-- Create localhost-specific user (optional, if accessing from the same machine)
CREATE USER IF NOT EXISTS 'mauro'@'%' IDENTIFIED BY 'mauro007';
GRANT ALL PRIVILEGES ON *.* TO 'mauro'@'%' WITH GRANT OPTION;

-- Ensure changes take effect
FLUSH PRIVILEGES;