-- Initialize all required databases
CREATE DATABASE IF NOT EXISTS bookingmsdb;
CREATE DATABASE IF NOT EXISTS handlingmsdb;
CREATE DATABASE IF NOT EXISTS routingmsdb;
CREATE DATABASE IF NOT EXISTS trackingmsdb;

-- Create users for each microservice (if they don't exist)
CREATE USER IF NOT EXISTS 'bookingmsdb'@'%' IDENTIFIED BY 'bookingmsdb';
CREATE USER IF NOT EXISTS 'handlingmsdb'@'%' IDENTIFIED BY 'handlingmsdb';
CREATE USER IF NOT EXISTS 'routingmsdb'@'%' IDENTIFIED BY 'routingmsdb';
CREATE USER IF NOT EXISTS 'trackingmsdb'@'%' IDENTIFIED BY 'trackingmsdb';

-- Grant privileges
GRANT ALL PRIVILEGES ON bookingmsdb.* TO 'bookingmsdb'@'%';
GRANT ALL PRIVILEGES ON handlingmsdb.* TO 'handlingmsdb'@'%';
GRANT ALL PRIVILEGES ON routingmsdb.* TO 'routingmsdb'@'%';
GRANT ALL PRIVILEGES ON trackingmsdb.* TO 'trackingmsdb'@'%';

-- Create tables for message logging (opcional)
USE bookingmsdb;
CREATE TABLE IF NOT EXISTS message_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(255),
    exchange VARCHAR(255),
    routing_key VARCHAR(255),
    message_type VARCHAR(255),
    message_content TEXT,
    service_name VARCHAR(100),
    direction ENUM('SENT', 'RECEIVED'),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_service (service_name),
    INDEX idx_timestamp (timestamp),
    INDEX idx_message_type (message_type)
);

FLUSH PRIVILEGES;