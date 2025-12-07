-- init-routing-data.sql
USE routingmsdb;

-- Verificar se as tabelas existem antes de limpar
SET FOREIGN_KEY_CHECKS = 0;

-- Limpar dados se as tabelas existirem
DROP TABLE IF EXISTS carrier_movement;
DROP TABLE IF EXISTS voyage;

-- Recriar tabelas (ou confiar no JPA para criá-las)
CREATE TABLE IF NOT EXISTS voyage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voyage_number VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS carrier_movement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voyage_number VARCHAR(50) NOT NULL,
    load_location VARCHAR(10) NOT NULL,
    unload_location VARCHAR(10) NOT NULL,
    load_time TIMESTAMP NOT NULL,
    unload_time TIMESTAMP NOT NULL,
    voyage_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (voyage_id) REFERENCES voyage(id) ON DELETE CASCADE
);

-- Inserir dados de exemplo
INSERT INTO voyage (id, voyage_number) VALUES 
(1, 'V0100'),
(2, 'V0200'),
(3, 'V0300'),
(4, 'V0400'),
(5, 'V0500');

INSERT INTO carrier_movement (
    voyage_number, load_location, unload_location, 
    load_time, unload_time, voyage_id
) VALUES 
('V0100', 'JPTYO', 'SGSIN', '2024-01-10 12:00:00', '2024-01-14 12:00:00', 1),
('V0200', 'JPTYO', 'SGSIN', '2024-01-12 12:00:00', '2024-01-16 12:00:00', 2),
('V0300', 'SGSIN', 'JPTYO', '2024-01-20 12:00:00', '2024-01-24 12:00:00', 3),
('V0400', 'USNYC', 'GBLON', '2024-01-05 12:00:00', '2024-01-06 12:00:00', 4),
('V0500', 'CNHKG', 'JPTYO', '2024-01-08 12:00:00', '2024-01-10 12:00:00', 5);

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Data initialized successfully' AS status;