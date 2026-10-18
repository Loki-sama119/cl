-- Execute AFTER demo.sql. Requires MySQL/InnoDB for transactional FK integrity.
USE demo;
CREATE TABLE IF NOT EXISTS permission (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS user_permission (
    user_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user_permission_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_permission_permission FOREIGN KEY (permission_id) REFERENCES permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO permission (id, name) VALUES
(1, 'add'), (2, 'edit'), (3, 'delete'), (4, 'view')
ON DUPLICATE KEY UPDATE name = VALUES(name);
