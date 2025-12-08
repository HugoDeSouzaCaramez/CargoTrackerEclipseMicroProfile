USE routingmsdb;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS carrier_movement;
DROP TABLE IF EXISTS voyage;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS voyage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voyage_number VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS carrier_movement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voyage_number VARCHAR(255),
    load_location VARCHAR(255),
    unload_location VARCHAR(255),
    load_time DATETIME,
    unload_time DATETIME,
    voyage_id BIGINT,
    FOREIGN KEY (voyage_id) REFERENCES voyage(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Voyages
INSERT INTO voyage (id, voyage_number) VALUES 
(1, '0100S'),
(2, '0200S'),
(3, '0300S'),
(4, '0400A'),
(5, '0500A'),
(6, '0600S'),  -- USNYC→NLRTM
(7, '0700S');  -- NLRTM→USNYC

INSERT INTO carrier_movement (
    voyage_number, load_location, unload_location, 
    load_time, unload_time, voyage_id
) VALUES 
-- Voyage 0100S: Tokyo -> Singapore
('0100S', 'JPTYO', 'SGSIN', '2026-01-01 08:00:00', '2026-01-30 16:00:00', 1),

-- Voyage 0200S: Singapore -> Tokyo
('0200S', 'SGSIN', 'JPTYO', '2026-02-01 10:00:00', '2026-02-15 18:00:00', 2),

-- Voyage 0300S: New York -> London (chega ANTES do deadline)
('0300S', 'USNYC', 'GBLON', '2026-01-01 12:00:00', '2026-01-04 14:00:00', 3),

-- Voyage 0400A: Hong Kong -> Tokyo
('0400A', 'CNHKG', 'JPTYO', '2026-01-01 14:00:00', '2026-01-10 22:00:00', 4),

-- Voyage 0500A: Hamburg -> Rotterdam
('0500A', 'DEHAM', 'NLRTM', '2026-01-01 09:00:00', '2026-01-25 17:00:00', 5),

-- Voyage 0600S: New York -> Rotterdam (CHEGA ANTES DO DEADLINE: 04/01 vs 05/01)
('0600S', 'USNYC', 'NLRTM', '2026-01-01 08:00:00', '2026-01-04 16:00:00', 6),

-- Voyage 0700S: Rotterdam -> New York
('0700S', 'NLRTM', 'USNYC', '2026-01-15 10:00:00', '2026-01-25 18:00:00', 7);

-- Verificar rotas que atendem ao deadline 2026-01-05
SELECT 'Rotas USNYC→NLRTM (chegam antes de 2026-01-05):' AS status;
SELECT * FROM carrier_movement 
WHERE load_location = 'USNYC' 
  AND unload_location = 'NLRTM'
  AND unload_time <= '2026-01-05 23:59:59';

SELECT 'Rota USNYC→NLRTM disponível antes do deadline' AS status;