CREATE TABLE  formule_tarifaire(
    id_prix BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    prestation_id BIGINT NOT NULL,
    prix INT NOT NULL,
    libelle VARCHAR(255)  NOT NULL,
    FOREIGN KEY (prestation_id)
    REFERENCES prestation(id)
);