CREATE TYPE tarif AS ENUM ('FIXE', 'DEVIS');
CREATE TABLE  prestation (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    type_tarification tarif NOT NULL,
    prix_minimum NUMERIC(10, 2)

);
