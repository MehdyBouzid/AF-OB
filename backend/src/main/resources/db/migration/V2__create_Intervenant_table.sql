CREATE TABLE intervenant(
    id BIGSERIAL PRIMARY KEY ,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)  NOT NULL
);
