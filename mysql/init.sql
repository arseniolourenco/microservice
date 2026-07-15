-- Criação das bases de dados (sem aspas duplas)
CREATE DATABASE IF NOT EXISTS keycloakDB;
CREATE DATABASE IF NOT EXISTS inventoryDB;

-- Criação do utilizador 'mauro' com senha 'mauro007'
CREATE USER IF NOT EXISTS 'mauro'@'localhost' IDENTIFIED BY 'mauro007';
CREATE USER IF NOT EXISTS 'mauro'@'%' IDENTIFIED BY 'mauro007';

-- Concessão de privilégios totais (útil para ambiente de desenvolvimento)
GRANT ALL PRIVILEGES ON *.* TO 'mauro'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'mauro'@'%' WITH GRANT OPTION;

-- Aplicar as alterações de privilégios
FLUSH PRIVILEGES;

-- Criação da base de dados e tabela para autenticação Node.js
CREATE DATABASE IF NOT EXISTS node_auth;
USE node_auth;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);