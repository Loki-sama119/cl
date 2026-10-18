-- CodeGym [Thuc hanh] Goi MySQL Stored Procedures tu JDBC
-- Run database/demo.sql FIRST to create schema and users table.
-- Use MySQL Workbench or the mysql client (DELIMITER is a client command).
USE demo;

DROP PROCEDURE IF EXISTS get_user_by_id;
DROP PROCEDURE IF EXISTS insert_user;

DELIMITER $$
CREATE PROCEDURE get_user_by_id(IN user_id INT)
BEGIN
    SELECT users.name, users.email, users.country
    FROM users
    WHERE users.id = user_id;
END$$

CREATE PROCEDURE insert_user(
    IN user_name VARCHAR(50),
    IN user_email VARCHAR(50),
    IN user_country VARCHAR(50)
)
BEGIN
    INSERT INTO users (name, email, country)
    VALUES (user_name, user_email, user_country);
END$$
DELIMITER ;

-- Smoke verification (read-only):
-- CALL get_user_by_id(1);
-- SHOW PROCEDURE STATUS WHERE Db = 'demo';
-- Test insert (modifies data):
-- CALL insert_user('Lan', 'lan@example.com', 'Viet Nam');
-- SELECT * FROM users ORDER BY id DESC;
