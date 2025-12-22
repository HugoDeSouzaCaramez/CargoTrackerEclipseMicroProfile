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

CREATE TABLE cargo_views (
    booking_id VARCHAR(50) PRIMARY KEY,
    booking_amount INTEGER NOT NULL,
    origin_location VARCHAR(10) NOT NULL,
    destination_location VARCHAR(10) NOT NULL,
    arrival_deadline TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    routing_status VARCHAR(50),
    transport_status VARCHAR(50),
    last_known_location VARCHAR(10),
    current_voyage VARCHAR(50),
    leg_count INTEGER,
    estimated_transit_hours BIGINT,
    is_on_track BOOLEAN,
    is_misdirected BOOLEAN,
    is_ready_for_claim BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    version BIGINT,
    aggregate_version BIGINT
);

-- Índices para otimização de consultas
CREATE INDEX idx_cargo_view_status ON cargo_views(status);
CREATE INDEX idx_cargo_view_origin ON cargo_views(origin_location);
CREATE INDEX idx_cargo_view_dest ON cargo_views(destination_location);
CREATE INDEX idx_cargo_view_deadline ON cargo_views(arrival_deadline);
CREATE INDEX idx_cargo_view_last_updated ON cargo_views(last_updated);
CREATE INDEX idx_cargo_view_origin_dest ON cargo_views(origin_location, destination_location);
CREATE INDEX idx_cargo_view_routing_status ON cargo_views(routing_status);

FLUSH PRIVILEGES;