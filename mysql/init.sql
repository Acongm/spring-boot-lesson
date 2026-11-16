-- MySQL 容器首次初始化空数据卷时执行这份脚本。
CREATE TABLE student (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    age INT NOT NULL
) CHARACTER SET utf8mb4;

INSERT INTO student (name, phone, age) VALUES ('Tom', '1234567890', 30);
