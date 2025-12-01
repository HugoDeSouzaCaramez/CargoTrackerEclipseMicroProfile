-- Populate routingmsdb with sample data
USE routingmsdb;

-- Clear existing data (na ordem correta devido a constraints de chave estrangeira)
DELETE FROM carrier_movement;
DELETE FROM voyage;

-- Insert voyages
INSERT INTO voyage (id, voyage_number) VALUES 
(1, 'V0100'),
(2, 'V0200'),
(3, 'V0300'),
(4, 'V0400'),
(5, 'V0500');

-- Insert carrier movements
INSERT INTO carrier_movement (
    id, voyage_number, load_location, unload_location, load_time, unload_time, voyage_id
) VALUES 
(1, 'V0100', 'JPTYO', 'SGSIN', '2024-01-10 12:00:00', '2024-01-14 12:00:00', 1),
(2, 'V0200', 'JPTYO', 'SGSIN', '2024-01-12 12:00:00', '2024-01-16 12:00:00', 2),
(3, 'V0300', 'SGSIN', 'JPTYO', '2024-01-20 12:00:00', '2024-01-24 12:00:00', 3),
(4, 'V0400', 'USNYC', 'GBLON', '2024-01-05 12:00:00', '2024-01-06 12:00:00', 4),
(5, 'V0500', 'CNHKG', 'JPTYO', '2024-01-08 12:00:00', '2024-01-10 12:00:00', 5);