-- Populate routingmsdb with sample data
USE routingmsdb;

-- Clear existing data
DELETE FROM carrier_movement;
DELETE FROM VOYAGE;

-- Insert voyages
INSERT INTO VOYAGE (id, voyage_number) VALUES 
(1, 'V0100'),
(2, 'V0200'),
(3, 'V0300');

-- Insert carrier movements
INSERT INTO carrier_movement (id, arrival_date, departure_date, arrival_location_id, departure_location_id, voyage_id) VALUES 
(1, '2024-01-14 12:00:00', '2024-01-10 12:00:00', 'SGSIN', 'JPTYO', 1),
(2, '2024-01-16 12:00:00', '2024-01-12 12:00:00', 'SGSIN', 'JPTYO', 2),
(3, '2024-01-24 12:00:00', '2024-01-20 12:00:00', 'JPTYO', 'SGSIN', 3);