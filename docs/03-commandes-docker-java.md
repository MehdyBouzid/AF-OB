# Commandes : antisèche

Repères :

```text
Service Compose   db (PostgreSQL 17), conteneur observé : af-ob-db-1
Port base         5434 (hôte) -> 5432 (conteneur)
API               http://localhost:4000
Variables .env    POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD   (modèle : .env.example)
Profils Spring    dev (à activer à la main), test (activé par les tests)
```

Ne jamais afficher ni committer `.env`, `application-dev.properties` ni `cookies.txt`.

Charger les variables du `.env` dans le terminal courant :

```bash
# racine du dépôt : rend $POSTGRES_USER et $POSTGRES_DB disponibles, sans les afficher
set -a; source .env; set +a
```

# Consultation (sans risque)

## Docker

```bash
# racine du dépôt
docker compose ps                     # état du service db
docker compose logs --tail 100 db     # 100 dernières lignes de logs
docker compose logs -f db             # logs en direct (Ctrl+C ne stoppe pas la base)

# n'importe quel dossier
docker ps                             # conteneurs actifs
docker ps -a                          # y compris arrêtés
docker exec af-ob-db-1 pg_isready -U "$POSTGRES_USER"   # la base accepte-t-elle les connexions ?
```

## PostgreSQL

```bash
# n'importe quel dossier, .env chargé
docker exec -it af-ob-db-1 psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"    # session interactive
docker compose exec db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"        # idem depuis la racine, sans dépendre du nom du conteneur
docker exec af-ob-db-1 psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c '\dt'   # liste des tables, sans session
docker exec af-ob-db-1 psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c 'SELECT id, nom, type_tarification, active FROM prestation ORDER BY id;'   # contenu du catalogue
```

Dans `psql` :

```text
\l                 bases (dev et afob_test)
\dt                tables de la base courante
\d prestation      colonnes et contraintes
\dT+ tarif         valeurs de l'enum
\c afob_test       changer de base
\q                 quitter
```

Ne sélectionne jamais `intervenant.password_hash` dans un terminal partagé.

## Flyway

```bash
# n'importe quel dossier, .env chargé : migrations appliquées
docker exec af-ob-db-1 psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c 'SELECT installed_rank, version, description, success, checksum FROM flyway_schema_history ORDER BY installed_rank;'

# racine du dépôt : migrations présentes dans le code
ls backend/src/main/resources/db/migration/
```

- `success = t` : appliquée. Checksum `0` : fichier vide (V1 et V4).
- Une migration appliquée ne se modifie jamais : ajouter `V<n>__description.sql` (deux underscores, extension `.sql`, sinon Flyway l'ignore).

## Git

```bash
# racine du dépôt
git status --short                 # fichiers modifiés ou non suivis
git branch --show-current          # branche courante
git diff                           # modifications non indexées
git diff --staged                  # modifications prêtes à être commitées
git log --oneline -10              # 10 derniers commits
```

Avant un commit : `git status`, `git diff`, `git add <fichiers précis>`, `git diff --staged`. Pas de `git add .`. Aucun commit, push, merge ni rebase sans demande explicite de Mehdy.

# Exécution (modifie l'état, sans perte de données)

## Démarrer et arrêter Docker

```bash
# racine du dépôt
docker compose up -d        # démarre la base en arrière-plan
docker compose stop         # arrête sans rien supprimer
docker compose start        # relance un conteneur arrêté
docker compose restart db   # redémarre la base
```

Le volume `postgres_data` conserve les données après chacune de ces commandes.

## Démarrer Spring Boot

```bash
# dossier backend/
set -a; source ../.env; set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev     # API sur le port 4000
PORT=4001 ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # autre port si 4000 est pris
```

Prérequis : base démarrée et fichier `backend/src/main/resources/application-dev.properties` (ignoré par git) avec `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`, valeurs du `.env`. Sans profil `dev`, l'application n'a pas de datasource et ne démarre pas.

Au démarrage, Flyway applique les migrations en attente à la base ciblée, puis Hibernate valide le schéma.

## Compiler et tester

```bash
# dossier backend/
./mvnw -v                                   # versions de Maven et Java (21 attendu)
./mvnw compile                              # compile sans tester
./mvnw test                                 # toute la suite
./mvnw -Dtest=PrestationServiceTest test    # une classe
./mvnw -Dtest=PrestationServiceTest#save_fixeAvecPrixMinimum_leMetANull test   # une méthode
./mvnw package -DskipTests                  # jar dans backend/target/
./mvnw clean                                # supprime backend/target/ (sans risque pour les données)
```

`contextLoads` démarre toute l'application sur la base `afob_test` (profil `test`) : il faut la base démarrée, `afob_test` créée et `POSTGRES_USER` et `POSTGRES_PASSWORD` disponibles (environnement ou `../.env`). Les autres tests n'utilisent aucune base. Rapports : `backend/target/surefire-reports/`.

## Créer la base de test (une seule fois)

```bash
# n'importe quel dossier, .env chargé : ajoute afob_test sans toucher à la base de dev
docker exec af-ob-db-1 psql -U "$POSTGRES_USER" -d postgres -c 'CREATE DATABASE afob_test;'
```

## Appeler l'API

```bash
# n'importe quel dossier. Les POST et PUT écrivent dans la base de dev : utiliser des données fictives.
curl -s -c /tmp/af-ob-cookies.txt http://localhost:4000/auth/csrf -o /dev/null     # dépose XSRF-TOKEN
TOKEN=$(awk '$6=="XSRF-TOKEN"{print $7}' /tmp/af-ob-cookies.txt)                   # lit sa valeur
curl -s -b /tmp/af-ob-cookies.txt -c /tmp/af-ob-cookies.txt -H "X-XSRF-TOKEN: $TOKEN" \
  -H 'Content-Type: application/json' -d '{"email":"<EMAIL>","password":"<MOT_DE_PASSE>"}' \
  -o /dev/null -w '%{http_code}\n' http://localhost:4000/auth/login                # login, attend 200
curl -s -b /tmp/af-ob-cookies.txt http://localhost:4000/prestations/1              # lecture d'une prestation
```

Le fichier de cookies contient une session valide : ne pas le versionner. Les détails des routes sont dans `02-api-routes-et-choix.md`.

## Sauvegarde avant une opération risquée

```bash
# n'importe quel dossier, .env chargé : écrit un fichier SQL sur ta machine, ne le versionne pas
docker exec af-ob-db-1 pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" > /tmp/afob_backup_$(date +%Y%m%d_%H%M).sql
```

# Diagnostic

Sans suppression de données. Cause, puis correction.

```text
Failed to determine a suitable driver class
  Aucun profil actif. Ajouter -Dspring-boot.run.profiles=dev ; pour un test, vérifier @ActiveProfiles("test").

Connection refused sur localhost:5434
  Base arrêtée. docker compose ps, puis docker compose up -d.

password authentication failed
  Identifiants de application-dev.properties différents de ceux de la base.
  Changer POSTGRES_PASSWORD dans .env ne modifie pas un volume déjà initialisé.

database "afob_test" does not exist
  Créer la base de test (section ci-dessus).

Could not resolve placeholder 'POSTGRES_USER'
  Variables absentes. Charger .env ou lancer Maven depuis backend/.

Migrations have checksum mismatch
  Une migration appliquée a été modifiée. La restaurer (git diff) et créer une nouvelle migration.

Detected failed migration
  success = f dans flyway_schema_history. Corriger le SQL puis décider explicitement du sort de la base concernée.

Schema-validation: missing table / wrong column type
  Entité et schéma divergent. Aligner l'entité ou ajouter une migration ; ne pas passer ddl-auto à update.

Port 4000 was already in use
  lsof -i :4000, ou utiliser PORT=4001.

403 sur un POST
  Jeton CSRF absent. Refaire GET /auth/csrf.

401
  Pas de session. Refaire le login.
```

# Opérations destructives (hors routine)

À ne faire que sur décision explicite, après une sauvegarde.

- `DELETE`, `TRUNCATE`, `DROP`, `UPDATE` sans `WHERE` : faire d'abord un `SELECT` avec la même condition.
- `docker compose down` : supprime le conteneur, garde le volume.
- Supprimer le volume `postgres_data` : efface toutes les bases (dev et test). Jamais une étape de dépannage.
- `git reset --hard`, `git clean -fd`, `git push --force` : perte de travail ou historique réécrit.
