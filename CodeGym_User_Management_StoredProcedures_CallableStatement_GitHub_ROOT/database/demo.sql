-- The exercise's database and users table; rerunning the script is safe.
CREATE DATABASE IF NOT EXISTS demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE demo;
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(220) NOT NULL,
    country VARCHAR(120),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (name, email, country)
SELECT 'Minh', 'minh@codegym.vn', 'Viet Nam'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'minh@codegym.vn');
INSERT INTO users (name, email, country)
SELECT 'Kante', 'kante@che.uk', 'Kenia'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'kante@che.uk');
