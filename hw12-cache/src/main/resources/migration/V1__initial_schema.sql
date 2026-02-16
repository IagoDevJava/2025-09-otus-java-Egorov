-- Sequences
CREATE SEQUENCE client_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE address_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE phone_seq START WITH 1 INCREMENT BY 1;

-- Tables
CREATE TABLE client (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(50),
    address_id BIGINT
);

CREATE TABLE users (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(50),
    login VARCHAR(50),
    password VARCHAR(50)
);

CREATE TABLE address (
    id BIGINT NOT NULL PRIMARY KEY,
    street VARCHAR(255)
);

CREATE TABLE phone (
    id BIGINT NOT NULL PRIMARY KEY,
    number VARCHAR(20),
    client_id BIGINT NOT NULL
);

-- Foreign keys
ALTER TABLE client ADD CONSTRAINT fk_client_address FOREIGN KEY (address_id) REFERENCES address(id);
ALTER TABLE phone ADD CONSTRAINT fk_phone_client FOREIGN KEY (client_id) REFERENCES client(id);