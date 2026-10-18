USE demo;
-- The servlet demo resets this table when run. Use ONLY in the demo schema.
-- Storage engine InnoDB supports JDBC commit/rollback.
CREATE TABLE IF NOT EXISTS Employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    salary DECIMAL(15,2) NOT NULL,
    created_Date DATETIME
) ENGINE=InnoDB;

-- After /users?action=test-use-tran (failed update):
-- SELECT * FROM Employee; -- zero records
-- After /users?action=test-use-tran&mode=success (correct update):
-- SELECT name, salary FROM Employee ORDER BY id;
-- Expected Quynh: 999.99, Ngan: 20.00
