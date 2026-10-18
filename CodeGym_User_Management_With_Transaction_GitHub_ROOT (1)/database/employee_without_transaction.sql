USE demo;
-- Classroom-only table; the demo endpoint drops and recreates Employee each time.
CREATE TABLE IF NOT EXISTS Employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    salary DECIMAL(15,2) NOT NULL,
    created_Date DATETIME
);
-- After triggering /users?action=test-without-tran run:
-- SELECT * FROM Employee;
