# Washafo (AF-OB) : contexte du projet

Seul le backend existe. Le frontend Angular n'est pas commencé (pas de dossier `frontend/`). `[prévu]` marque ce qui n'est pas implémenté. Règles métier de référence : `.claude/rules/project.md`.

## Présentation

Washafo présente les prestations de deux entreprises partenaires. Un client demande un rendez-vous ou un devis sans créer de compte. Le paiement se fait après la prestation, sans paiement en ligne.

Utilisateurs :
- Client : aucun compte. Ses parcours sont `[prévu]`.
- Intervenant (patron) : seul acteur authentifié, un seul niveau d'accès. Les deux patrons partagent le catalogue ; leurs disponibilités, demandes et planning seront individuels `[prévu]`.
- Administrateur : crée les comptes intervenants directement en base, sans inscription publique.

## Fonctionnalités

Existantes :
- Connexion par email et mot de passe, session serveur.
- Catalogue : lecture, création et modification d'une prestation.

Prévues :
- Liste, désactivation et réactivation des prestations.
- Formules tarifaires : table SQL créée, pas de code Java.
- Demandes de devis avec photo, disponibilités, rendez-vous (acceptation, refus, annulation), notifications par email.
- Isolation des données par intervenant.

Décisions ouvertes : hébergement, déplacement d'un rendez-vous, acceptation d'un devis par le client, prix indicatif du nettoyage véhicule (60 ou 70 euros), sort des formules quand une prestation est désactivée.

Deux types de prestation (`TypeTarification`) :
- `FIXE` : prix porté par des formules (libellé et prix), le client choisit ensuite intervenant et créneau.
- `DEVIS` : le patron fixe le montant ; `prixMinimum` est un prix indicatif facultatif.

## Stack

- Java 21, Spring Boot 4.1.1, Maven (`backend/mvnw`).
- Spring Web MVC, Spring Data JPA (Hibernate), Spring Security, Bean Validation.
- PostgreSQL 17 dans Docker, migrations Flyway.
- Angular `[prévu]`.

## Architecture

```text
Angular (4200, prévu)  ->  Spring Boot (4000)  ->  PostgreSQL (5434 sur l'hôte)

Requête : Spring Security -> Controller -> Service -> Repository -> PostgreSQL
```

Monolithe en couches, organisé par feature. Responsabilités :
- Controller : HTTP, JSON, DTO. Ne touche jamais un repository.
- Service : règles métier.
- Repository : accès aux données (Spring Data).
- Entity : mapping des tables, jamais exposée en HTTP.

Pourquoi : les règles métier se testent sans HTTP ni base.

```text
backend/src/main/java/com/afob/backend/
  auth/         connexion, entité Intervenant
  prestation/   catalogue
  config/       SecurityConfig, WebConfig (CORS)
backend/src/main/resources/
  application.properties
  application-dev.properties   ignoré par git, à créer localement
  db/migration/                V1 à V5
```

Chaque feature contient `controller`, `service`, `repository`, `entity`, `dto`, `exception`. Les features `disponibilite`, `rendezvous`, `demandedevis` et `notification` n'existent pas.

## Modèle de données

Tables créées par les migrations :
- `intervenant` : `id`, `prenom`, `email` (unique), `password_hash`.
- `prestation` : `id`, `nom`, `description`, `active` (vrai par défaut), `type_tarification` (enum PostgreSQL `tarif`), `prix_minimum` (nullable).
- `formule_tarifaire` : `id_prix`, `prestation_id` (clé étrangère vers `prestation`), `prix`, `libelle`.

Relation : une prestation possède plusieurs formules. Elle n'existe qu'en SQL, aucune entité Java ne représente `formule_tarifaire`. `intervenant` et `prestation` ne sont pas liées.

Règles dans `PrestationService` : une prestation est créée active ; pour `FIXE`, `prixMinimum` est toujours remis à `null`.

Migrations :
- V1 et V4 sont des fichiers vides déjà appliqués : ne pas les remplir.
- V5 (`formule_tarifaire`) n'est pas committée mais déjà appliquée sur la base de dev. Elle reste à définir : `prix` est un entier, sans colonne d'activation.
- Une migration appliquée ne se modifie jamais (checksum) : on ajoute `V<n>__description.sql`.

L'enum PostgreSQL est mappé par `@Enumerated(EnumType.STRING)` et `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` dans `prestation/entity/Prestation.java`. `ddl-auto=validate` : l'entité doit rester alignée sur les migrations.

## Authentification et sécurité

Configuration : `backend/src/main/java/com/afob/backend/config/SecurityConfig.java`.

- Session serveur (cookie `JSESSIONID`), pas de JWT en V1.
- Mots de passe hashés en BCrypt ; l'email est l'identifiant.
- Publiques : `/auth/login` et `/auth/csrf`. Tout le reste exige une session, y compris les chemins inexistants.
- CSRF actif : cookie `XSRF-TOKEN` renvoyé dans l'en-tête `X-XSRF-TOKEN` pour les `POST` et `PUT`.
- CORS limité à `FRONTEND_URL` (défaut `http://localhost:4200`), cookies autorisés.
- Aucun détail interne dans les erreurs.
- Pas de contrôle de rôle ni d'isolation par intervenant.
- Absents : déconnexion, `GET /auth/me`.

Les routes et leur appel sont détaillés dans `02-api-routes-et-choix.md`.

## État actuel et prochaines étapes

État :
- Branche de travail `feature/prestations` ; `dev` contient `auth`.
- Aucune route publique : un client ne peut encore rien consulter.

Prochaines étapes :
1. Terminer le catalogue : liste, désactivation, validation des entrées.
2. Fixer puis implémenter les formules tarifaires.
3. `demandedevis`, `disponibilite`, `rendezvous`, `notification`.
4. Autorisations par intervenant.
5. Frontend Angular.
