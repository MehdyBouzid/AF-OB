# Règles spécifiques au projet AF-OB

Ce fichier contient uniquement les faits et décisions propres à AF-OB. Les règles personnelles de collaboration et d'apprentissage vivent dans `~/.claude/CLAUDE.md` et s'appliquent déjà à ce projet ; ne pas les redéclarer ici.

Pour tout choix important concernant la sécurité, les données, les transactions, la concurrence ou une dépendance supplémentaire, appliquer la règle définie dans `~/.claude/CLAUDE.md` : présenter les possibilités, comparer, recommander, puis attendre la décision de Mehdy.

## Objectif métier

AF-OB présente les prestations de deux entreprises collaboratrices et permet aux clients de demander un rendez-vous sans créer de compte.

Parcours principal :

```text
consulter les prestations et tarifs
→ choisir une prestation
→ choisir un intervenant
→ consulter ses disponibilités
→ choisir une date et un créneau
→ saisir les coordonnées du client
→ envoyer une demande de rendez-vous
→ l'intervenant accepte ou refuse
→ un rendez-vous accepté bloque le créneau
→ le planning et le client sont suivis
```

Les deux intervenants travaillent indépendamment : chacun possède ses disponibilités, ses demandes et son planning.

Le paiement est réalisé après la prestation, par carte ou espèces. Aucun paiement en ligne n'est prévu.

## Stack et structure du dépôt

```text
AF-OB/
├── frontend/   Angular
└── backend/    Java 21, Spring Boot, Maven
```

- Base de données : PostgreSQL.
- Persistance : Spring Data JPA / Hibernate.
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

- **Migrations** : Flyway envisagé, à confirmer avant la première migration réelle.
- **Authentification** : Spring Security envisagé pour les intervenants ; le mécanisme exact, par exemple session avec cookie HttpOnly ou token, reste à valider avant la feature `auth`.
- **Déploiement** : Render envisagé, à confirmer.
- **Déplacement d'un rendez-vous** : fonctionnalité non définie. Ne pas développer de comportement définitif avant d'avoir validé qui peut déplacer, dans quelles conditions et avec quelle vérification de disponibilité.

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

Ne pas placer ces décisions dans le Controller ou le Repository.

Le déplacement d'un rendez-vous n'est pas couvert : voir « Décisions à valider ».

## Progression technique par feature

Développer progressivement, sans introduire tous les concepts simultanément.

1. **`prestation`** — premier CRUD servant de gabarit : Entity JPA, Repository, Service, Controller, DTO et validation selon les besoins réels.
2. **`intervenant`** — modèle des deux intervenants et relations nécessaires.
3. **Décision et authentification minimale** — avant de créer des routes privées, présenter les mécanismes pertinents, leurs compromis et une recommandation. Attendre la validation explicite de Mehdy, puis implémenter une authentification réelle avec le mécanisme retenu, en utilisant Spring Security s'il est confirmé. Ne pas utiliser une identité fixe comme mécanisme de sécurité.
4. **`disponibilite`** — dates, horaires, créneaux propres à chaque intervenant et requêtes utiles au planning.
5. **`rendezvous`** — demande sans compte client, relations entre prestation, intervenant et créneau, statuts et transitions.
6. **Acceptation, refus et annulation** — règles métier, transactions, protection contre les doubles réservations et tests des cas limites. Le déplacement reste hors périmètre tant qu'il n'est pas validé.
7. **`notification`** — confirmation par e-mail ou SMS selon le besoin validé, sans mélanger la décision métier et le fournisseur technique.
8. **Autorisations complètes (`auth`)** — isolation des données par intervenant, protection des rendez-vous et plannings, tests `401` et `403`, et tentatives d'accès aux ressources d'un autre intervenant.

## Priorités de test

Tester en priorité :

- la création et la validation d'une prestation ;
- la visibilité des créneaux par intervenant ;
- la création d'une demande de rendez-vous ;
- le refus d'une demande déjà traitée ;
- l'acceptation atomique d'un rendez-vous ;
- la prévention d'une double réservation ;
- l'isolation du planning de chaque intervenant ;
- les autorisations d'accès ;
- l'annulation d'un rendez-vous.

## Évolution de cette règle

Ne pas conserver d'état temporaire tel que « projet non initialisé ».

Mettre à jour ce fichier uniquement lorsqu'une décision stable du projet change : stack, architecture, commande, workflow métier ou contrainte confirmée.

Une entrée de « Décisions à valider » ne devient une règle ferme qu'après validation explicite de Mehdy.
