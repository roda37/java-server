# java-server

# setup mariadb on archlinux (example)
```
CREATE USER 'user'@'localhost' IDENTIFIED BY 'passwd';
GRANT ALL PRIVILEGES ON *.* TO 'user'@'localhost' IDENTIFIED BY 'passwd' WITH GRANT OPTION;

CREATE DATABASE TESTING;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);
```

# edit port and database ip address in Webserver.java
# run

./compile
