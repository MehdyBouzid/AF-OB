# Règles spécifiques au projet AF-OB

Ce fichier contient uniquement les faits et décisions propres à AF-OB. Les règles personnelles de collaboration et d'apprentissage vivent dans `~/.claude/CLAUDE.md` et s'appliquent déjà à ce projet ; ne pas les redéclarer ici.

Pour tout choix important concernant la sécurité, les données, les transactions, la concurrence ou une dépendance supplémentaire, appliquer la règle définie dans `~/.claude/CLAUDE.md` : présenter les possibilités, comparer, recommander, puis attendre la décision de Mehdy.

## Objectif métier

AF-OB présente les prestations de deux entreprises collaboratrices et permet aux clients de demander un rendez-vous ou un devis sans créer de compte.

Les prestations se divisent en deux catégories :

- **Tarif fixe** : le client choisit une prestation, un intervenant et un créneau directement.
- **Sur devis** : le tarif dépend de l'état du véhicule ou de la nature du travail. Le client soumet une demande de devis ; le gérant établit le devis avant toute prise de rendez-vous.

Prestations sur devis confirmées :
- **Nettoyage intérieur véhicule** (voiture et camionnette regroupées dans la catégorie « véhicule ») — prix indicatif [À VÉRIFIER] : deux supports affichent respectivement « dès 60 € » et « à partir de 70 € ». Ne pas fixer le montant avant confirmation.
- **Vitres** — sur devis.

### Parcours prestation à tarif fixe

```text
consulter les prestations et tarifs
→ choisir une prestation
→ choisir une formule tarifaire (libellé + prix fixe)
→ choisir un intervenant
→ consulter ses disponibilités
→ choisir une date et un créneau
→ saisir les coordonnées du client
→ envoyer une demande de rendez-vous
→ l'intervenant accepte ou refuse
→ un rendez-vous accepté bloque le créneau
→ le planning et le client sont suivis
```

### Parcours prestation sur devis

```text
consulter les prestations
→ choisir une prestation sur devis
→ choisir le patron destinataire
→ saisir les informations du client
→ joindre une photo de l'intérieur du véhicule si nécessaire
→ envoyer la demande de devis (enregistrée en base)
→ le gérant consulte la demande dans son espace de gestion
→ le gérant détermine le montant et rédige la réponse
→ le backend envoie la réponse par email au client
→ le rendez-vous est organisé après traitement du devis
```

Les deux intervenants travaillent indépendamment : chacun possède ses disponibilités, ses demandes et son planning.

Les deux patrons partagent les mêmes droits et fonctionnalités — un seul niveau d'accès est nécessaire.
Le catalogue `Prestation` est commun aux deux patrons, sans ownership individuel.
L'ownership concerne uniquement les données individuelles : disponibilités, demandes, rendez-vous et planning.
L'identité du patron connecté provient exclusivement de la session serveur — jamais d'un paramètre fourni par le frontend.

Le paiement est réalisé après la prestation, par carte ou espèces. Aucun paiement en ligne n'est prévu.

## Stack et structure du dépôt

```text
AF-OB/
├── frontend/   Angular
└── backend/    Java 21, Spring Boot, Maven
```

- Base de données : PostgreSQL.
- Persistance : Spring Data JPA / Hibernate.
- Migrations de schéma : Flyway (confirmé).
- Package backend de base : `com.afob.backend`.

Ne pas inventer une commande, un port ou un nom de service Docker. Les lire dans les fichiers du projet lorsqu'ils existent : `pom.xml`, `package.json`, `compose.yaml` ou `docker-compose.yml`.

Commandes backend attendues après génération du Maven Wrapper :

```bash
cd backend
./mvnw spring-boot:run
./mvnw test
```

Pour Angular, utiliser les scripts réellement définis dans `frontend/package.json`.

### Décisions à valider

Les points suivants ne sont pas encore des faits établis. Ne pas les traiter comme acquis et vérifier avec Mehdy avant de les implémenter :

- **Déploiement** : Render envisagé, à confirmer.
- **Déplacement d'un rendez-vous** : fonctionnalité non définie. Ne pas développer de comportement définitif avant d'avoir validé qui peut déplacer, dans quelles conditions et avec quelle vérification de disponibilité.
- **Mécanisme d'acceptation du devis par le client** : le client reçoit la réponse par email, mais la suite du parcours (acceptation, prise de RDV) n'est pas encore définie. Ne pas implémenter avant confirmation.
- **Tarif indicatif véhicule** : deux supports affichent des montants différents (60 € / 70 €). À vérifier avant tout affichage ou enregistrement de valeur.
- **Application mobile** : une application Ionic + Angular est envisagée pour une version ultérieure. Hors périmètre V1.
- **Statut des formules lors de la désactivation d'une prestation FIXE** : lorsqu'un patron désactive une prestation, les formules doivent-elles conserver leur statut individuel actif/inactif, ou être toutes désactivées automatiquement ? Option recommandée (non validée) : les formules conservent leur statut interne — elles ne sont simplement plus disponibles tant que la prestation est désactivée, et les formules encore actives redeviennent disponibles à la réactivation. À trancher avant d'implémenter le comportement de désactivation/réactivation des prestations.

## Architecture

- Architecture système : frontend Angular séparé et backend Spring Boot monolithique.
- Architecture applicative du backend : layered modulaire organisée par feature.
- Aucun microservice n'est prévu.

Structure cible :

```text
backend/src/main/java/com/afob/backend/
├── prestation/
├── intervenant/
├── disponibilite/
├── rendezvous/
├── demandedevis/
├── notification/
└── auth/
```

Chaque feature contient uniquement les sous-dossiers utiles parmi : `controller/`, `service/`, `repository/`, `entity/`, `dto/`.

Flux principal d'un CRUD : `Controller → Service → Repository → PostgreSQL`.

Un Service peut utiliser plusieurs repositories ou collaborer avec un autre Service lorsqu'un besoin métier concret le justifie. Éviter les dépendances circulaires entre features.

| Couche | Responsabilité |
|---|---|
| Controller | Adaptation HTTP et validation des entrées |
| Service | Orchestration des règles métier et transactions |
| Entity | État et invariants internes |
| Repository | Accès aux données |
| DTO | Contrats d'entrée et de sortie de l'API |

Utiliser l'injection par constructeur. Ne pas appeler un Repository directement depuis un Controller. Ne pas exposer une Entity JPA comme contrat HTTP.

Ne pas créer `shared/` par anticipation. Le créer seulement lorsqu'un élément technique sans logique métier est réellement utilisé par au moins deux features.

## Limites architecturales

Ne pas introduire sans besoin concret et décision explicite : architecture hexagonale, Clean Architecture, CQRS, microservices, bus d'événements, interfaces ou repositories génériques ajoutés uniquement « pour être propre ».

Le Repository Pattern est réalisé simplement avec Spring Data JPA tant qu'aucun besoin ne justifie une abstraction supplémentaire.

## Convention de nommage

- Utiliser le français pour le vocabulaire métier : `rendezvous`, `disponibilite`, `intervenant`, `prestation`, `notification`.
- Conserver l'anglais pour le vocabulaire technique : `repository`, `service`, `controller`, `dto` et les noms de méthodes techniques.
- `auth` reste en anglais, comme abréviation technique courante d'authentification.

## Conventions techniques

Ces conventions indiquent comment utiliser les frameworks du projet. Elles ne tranchent pas les décisions encore ouvertes listées plus haut.

### Java 21 et Spring Boot

- Privilégier les `record` pour les DTO immuables. Utiliser une classe lorsqu'un besoin concret le justifie.
- Séparer les DTO d'entrée et de sortie lorsque leurs contrats ou responsabilités diffèrent. Un DTO commun reste possible pour un cas simple et réellement identique.
- Valider la forme des entrées sur les DTO avec Bean Validation (`@NotNull`, `@Email`, `@Size`, etc.) et déclencher cette validation avec `@Valid` dans le Controller.
- Conserver les règles métier, par exemple « créneau déjà réservé », dans le Service ou l'Entity plutôt que dans les annotations de validation.
- Effectuer le mapping DTO ↔ Entity manuellement tant que son volume reste limité. Présenter les possibilités et attendre la validation avant d'ajouter une bibliothèque de mapping.
- Placer principalement les limites transactionnelles sur les opérations métier du Service. Ne pas placer `@Transactional` sur les Controllers et tenir compte des transactions déjà fournies par Spring Data.
- Garder les transactions courtes. Ne pas appeler un fournisseur externe, par exemple un service d'e-mail ou de SMS, pendant une transaction de base de données ouverte.
- Utiliser des exceptions métier explicites et centraliser leur traduction en réponses JSON avec `@RestControllerAdvice`. Ne jamais retourner de stack trace ou de détail interne au client.

### JPA et PostgreSQL

- Privilégier les requêtes dérivées de Spring Data lorsqu'elles restent lisibles. Utiliser `@Query` lorsqu'une requête dérivée devient illisible ou insuffisante.
- Ne pas utiliser de SQL natif sans raison précise et documentée.
- Privilégier le chargement `LAZY` pour les relations. Un chargement `EAGER` doit être justifié par un besoin identifié.
- Vérifier les risques de requêtes N+1 et charger explicitement les données nécessaires au cas d'usage.
- Ne jamais accepter ou retourner directement une Entity JPA dans un Controller.

### Angular

- Organiser les services Angular par feature. Une feature peut posséder plusieurs services lorsque leurs responsabilités sont différentes.
- Privilégier les Reactive Forms pour les formulaires métier afin de garder leur état et leur validation explicites et testables.
- Typer les contrats échangés avec le backend.
- Traiter explicitement les états de chargement, de succès, d'absence de données et d'erreur.
- La validation Angular améliore l'expérience utilisateur ; le backend reste la source de vérité pour les règles métier et la sécurité.
- Utiliser les Guards pour la navigation uniquement. Ils ne remplacent jamais l'autorisation côté backend.
- Ne pas introduire systématiquement Signals, RxJS complexe ou une bibliothèque de gestion d'état lorsqu'une solution locale plus simple suffit.

### HTTP

- Utiliser des codes cohérents : `200` pour un succès avec réponse, `201` pour une création, `204` lorsqu'une opération réussit sans corps, `400` pour une entrée invalide, `401` si l'utilisateur n'est pas authentifié, `403` si l'accès est refusé, `404` si la ressource n'existe pas et `409` pour un conflit avec l'état actuel, par exemple un créneau déjà réservé.
- Utiliser un format JSON cohérent pour les erreurs de tous les endpoints.

### Authentification

- Spring Security est utilisé pour l'authentification des intervenants.
- L'email est l'identifiant de connexion.
- Les mots de passe sont hashés avec BCrypt via le `PasswordEncoder` de Spring Security. Ils ne sont jamais stockés en clair.
- L'authentification utilise une session serveur avec cookie HttpOnly. JWT n'est pas utilisé pour cette V1.
- Il n'y a aucune inscription publique. Les comptes intervenants sont créés manuellement par l'administrateur/développeur.
- Le Repository est responsable uniquement de l'accès aux données, notamment la recherche d'un intervenant par email (`findByEmail`).
- Le Service porte la logique métier de récupération de l'intervenant et gère le cas où aucun intervenant ne correspond à l'email fourni.
- `IntervenantNotFoundException` est levée lorsque l'intervenant recherché n'existe pas.

### Tests

- Utiliser `@WebMvcTest` pour tester un Controller isolé.
- Utiliser `@DataJpaTest` pour tester les requêtes et comportements du Repository.
- Utiliser des tests unitaires pour les règles du Service ou de l'Entity.
- Utiliser `@SpringBootTest` uniquement pour les parcours nécessitant réellement plusieurs couches ou l'application complète.
- Choisir le type de test selon le comportement et le risque à protéger.

## Règles métier principales

- Un client ne crée pas de compte.
- Une demande de rendez-vous commence avec un statut d'attente.
- Une demande appartient à l'intervenant choisi par le client.
- Chaque intervenant consulte et gère uniquement ses propres demandes et son planning.
- Un rendez-vous ne peut être accepté que s'il est encore en attente.
- L'acceptation vérifie que le créneau est toujours disponible, puis le bloque pour l'intervenant concerné.
- Deux rendez-vous acceptés ne doivent jamais occuper le même créneau pour le même intervenant.
- L'acceptation et le blocage du créneau doivent rester cohérents dans une même transaction.
- Une annulation doit libérer le créneau correspondant de manière cohérente.
- Une notification externe ne doit être déclenchée qu'après la réussite de la décision métier et de la transaction associée.

### Règles de tarification — [VALIDÉ]

**Prestations FIXE :**
- Une prestation FIXE possède une ou plusieurs formules tarifaires (`VarianteTarifaire`).
- Chaque formule possède un libellé et un prix fixe.
- Le client choisit sa formule avant de demander un rendez-vous.
- Exemples validés :
  - Canapé : 2–4 places 70 €, 5–6 places 90 €, 7–8 places 130 €
  - Matelas : 1 place 60 €, 2 places 90 €
- `prixMinimum` sur l'entité `Prestation` est `null` pour les prestations FIXE (le prix est porté par les formules).

**Prestations DEVIS :**
- Le prix final est déterminé par le patron après étude de la demande.
- `prixMinimum` est facultatif. S'il est renseigné, afficher « À partir de X € ». Sinon, afficher « Sur devis ».
- Le tarif indicatif pour le nettoyage véhicule reste [À VÉRIFIER] : voir « Décisions à valider ».

**`VarianteTarifaire` — [VALIDÉ, non implémenté]**
Le concept est validé et nécessaire pour les prestations FIXE. La classe Java et la migration V4 existent mais sont vides. L'implémentation est à réaliser.

**Création des formules — [VALIDÉ]**
- Une prestation FIXE doit posséder au moins une formule tarifaire valide dès sa création.
- Une prestation FIXE créée est active par défaut et possède donc au moins une formule active.

**Gestion des formules — [VALIDÉ]**
- Les deux patrons peuvent ajouter des formules au catalogue commun.
- Une formule peut être activée, désactivée et réactivée — jamais supprimée physiquement.
- Pour modifier le prix ou le libellé d'une formule active, le patron doit obligatoirement la désactiver au préalable.
- Une formule désactivée reste modifiable.
- Une formule désactivée n'est plus sélectionnable pour les nouvelles demandes.
- Une formule réactivée redevient sélectionnable lorsque sa prestation est active.

**Cohérence prestation / formules — [VALIDÉ]**
- Une prestation FIXE active doit toujours conserver au moins une formule active.
- Pour désactiver la dernière formule active d'une prestation, le patron doit d'abord désactiver la prestation entière.
- Une prestation désactivée peut conserver ses formules sans les supprimer.

**Historique des tarifs — [VALIDÉ]**
- Lorsqu'un client envoie une demande de rendez-vous FIXE, le prix de la formule choisie est conservé dans cette demande.
- Les modifications ultérieures du catalogue ne changent pas le prix des demandes déjà enregistrées.
- Les nouveaux tarifs s'appliquent uniquement aux nouvelles demandes.

### Règles de gestion du catalogue — [VALIDÉ]

- `nom`, `description` et `typeTarification` sont obligatoires à la création.
- Une prestation est active par défaut à la création.
- Les prestations sont désactivées ou réactivées — jamais supprimées physiquement.

### Règles spécifiques aux prestations sur devis

- Une prestation sur devis ne permet pas au client de choisir un créneau directement.
- Le client choisit le patron destinataire de sa demande de devis. La demande lui est rattachée.
- Une demande de devis véhicule doit inclure les informations du client et une photo de l'intérieur du véhicule.
- Voiture et camionnette sont regroupées dans la catégorie « véhicule » pour le nettoyage intérieur.
- Le formulaire soumis par le client constitue une demande de devis, pas un devis.
- La demande est enregistrée et consultable par le patron destinataire depuis son espace de gestion.
- Le patron analyse la demande et détermine lui-même le montant.
- Le patron rédige lui-même la réponse depuis son espace de gestion ; le backend envoie ensuite cette réponse par email au client.
- Le rendez-vous est organisé seulement après le traitement du devis.
- Le mécanisme d'acceptation du devis par le client et la prise de RDV qui suit restent [À DÉCIDER] : voir « Décisions à valider ».

Ne pas placer ces décisions dans le Controller ou le Repository.

Le déplacement d'un rendez-vous n'est pas couvert : voir « Décisions à valider ».

## Progression technique par feature

Développer progressivement, sans introduire tous les concepts simultanément.

1. **`prestation`** — premier CRUD servant de gabarit : Entity JPA, Repository, Service, Controller, DTO et validation selon les besoins réels.
2. **`demandedevis`** — demande de devis sans compte client, avec upload de photo pour les prestations véhicule. À développer après `prestation` et avant `rendezvous`.
3. **`intervenant`** — modèle des deux intervenants et relations nécessaires.
3. **Décision et authentification minimale** — avant de créer des routes privées, présenter les mécanismes pertinents, leurs compromis et une recommandation. Attendre la validation explicite de Mehdy, puis implémenter une authentification réelle avec le mécanisme retenu, en utilisant Spring Security s'il est confirmé. Ne pas utiliser une identité fixe comme mécanisme de sécurité.
4. **`disponibilite`** — dates, horaires, créneaux propres à chaque intervenant et requêtes utiles au planning.
5. **`rendezvous`** — demande sans compte client, relations entre prestation, intervenant et créneau, statuts et transitions.
6. **Acceptation, refus et annulation** — règles métier, transactions, protection contre les doubles réservations et tests des cas limites. Le déplacement reste hors périmètre tant qu'il n'est pas validé.
7. **`notification`** — confirmation par e-mail ou SMS selon le besoin validé, sans mélanger la décision métier et le fournisseur technique.
8. **Autorisations complètes (`auth`)** — isolation des données par intervenant, protection des rendez-vous et plannings, tests `401` et `403`, et tentatives d'accès aux ressources d'un autre intervenant.

## Priorités de test

Tester en priorité :

- la création et la validation d'une prestation ;
- la soumission d'une demande de devis avec photo ;
- le rejet d'une demande de devis incomplète (sans photo pour un véhicule) ;
- la visibilité des créneaux par intervenant ;
- la création d'une demande de rendez-vous ;
- le refus d'une demande déjà traitée ;
- l'acceptation atomique d'un rendez-vous ;
- la prévention d'une double réservation ;
- l'isolation du planning de chaque intervenant ;
- les autorisations d'accès ;
- l'annulation d'un rendez-vous.

## État d'avancement (à mettre à jour après chaque session significative)

### Branches

- `main` : squelette initial uniquement.
- `dev` : contient `auth` complète (merge de `feature/auth-intervenant`).
- `feature/prestations` : branche active — travail en cours sur `prestation`.

### Feature `auth` — [EXISTE] mergée dans `dev`

Committé sur `dev` :

- `Intervenant` entity (id, prenom, email, passwordHash)
- `IntervenantRepository` (`findByEmail`)
- `IntervenantService` (`findByEmail`, lève `IntervenantNotFoundException`)
- `IntervenantUserDetailsService` (adapte `Intervenant` vers `UserDetails`)
- `AuthenticationService` (délègue à `AuthenticationManager`)
- `AuthController` (`POST /auth/login`, `GET /auth/csrf`)
- `SecurityConfig` (BCrypt, `DaoAuthenticationProvider`, session HttpOnly, `CookieCsrfTokenRepository`, `CsrfTokenRequestAttributeHandler`, `AuthenticationEntryPoint` JSON UTF-8, routes publiques : `/auth/login` et `/auth/csrf`)
- `WebConfig` (CORS configuré via `FRONTEND_URL`)
- `LoginRequestDto` (record, `@NotBlank`, `@Email`, `@Size`)
- `IntervenantNotFoundException` (extends `UsernameNotFoundException`)
- Migration Flyway V2 : table `intervenant`

`GET /auth/me` : absent des deux branches vérifiées (`dev` et `feature/prestations`).

Tests manuels effectués le 2026-09-07 (curl + PostgreSQL réel) :

| Test | Résultat | Attendu | OK ? |
|---|---|---|---|
| Route protégée sans cookie | 401, JSON | 401 | Oui |
| `POST /auth/login` mauvais mdp | 401, JSON | 401 | Oui |
| `POST /auth/login` bon mdp | 200 + JSESSIONID | 200 | Oui |
| Route protégée inconnue avec cookie | 404 sans trace Java | 404 | Oui |

Tests automatisés :
- `AuthControllerTest` (`@WebMvcTest`) : 6/6 — PASS
  - `routeProtegee_sansAuthentification_renvoie401`
  - `login_identifiantsValides_renvoie200`
  - `login_mauvaisMotDePasse_renvoie401`
  - `login_motDePasseTropCourt_renvoie400`
  - `login_sansTokenCsrf_renvoie403`
  - `login_identifiantsValides_sauvegardeContexteSecurite`
- `BackendApplicationTests#contextLoads` (`@SpringBootTest`) : FAIL — l'exécution Maven complète échoue sur ce test. Le test tente de démarrer l'application complète ; cause probable : absence de datasource dans l'environnement de test. Diagnostic à confirmer.

### Feature `prestation` — [EXISTE] implémentation partielle sur `feature/prestations`

Committé :

- `Prestation` entity JPA (id, nom, description, active, typeTarification, prixMinimum)
- `TypeTarification` enum (FIXE, DEVIS)
- `PrestationRepository` (étend `JpaRepository`)
- `PrestationService` : `findById` et `save` présents — `save` contient une validation métier partielle (nom, description, typeTarification vérifiés manuellement), sans Bean Validation
- `PrestationController` (`GET /prestations/{id}`)
- `PrestationDto` (record, 6 champs)
- `PrestationNotFoundException`, `PrestationValidationException`, `PrestationExceptionHandler`
- Migrations V3 (type `tarif` + table `prestation`), V4 (vide — variantes non implémentées)

Non committé dans le working tree : corrections de conventions (`isActive`, nommage DTO, import inutilisé). `VarianteTarifaire.java` supprimée du working tree — la migration V4 reste en place. L'implémentation complète de `VarianteTarifaire` est à réaliser (concept validé, voir « Règles de tarification »).

Reste à faire : `POST /prestations`, `GET /prestations` (liste), Bean Validation sur le DTO d'entrée, tests `@WebMvcTest`.

### Features non commencées

`demandedevis`, `disponibilite`, `rendezvous`, `notification`, `intervenant` (feature dédiée).

### Frontend

Dossier `frontend/` inexistant. Angular non commencé.

---

## Évolution de cette règle

Ne pas conserver d'état temporaire tel que « projet non initialisé ».

Mettre à jour ce fichier uniquement lorsqu'une décision stable du projet change : stack, architecture, commande, workflow métier ou contrainte confirmée.

Une entrée de « Décisions à valider » ne devient une règle ferme qu'après validation explicite de Mehdy.
