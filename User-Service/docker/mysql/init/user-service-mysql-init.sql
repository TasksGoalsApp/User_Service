CREATE DATABASE IF NOT EXISTS UserDataSet
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE UserDataSet;

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    password VARCHAR(150) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_role (
    id_user_role BIGINT NOT NULL AUTO_INCREMENT,
    user_role VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id_user_role),
    CONSTRAINT uq_user_role_user UNIQUE (user_id),
    CONSTRAINT chk_user_role_role CHECK (user_role IN ('Customer', 'Admin')),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB;
