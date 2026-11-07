# API : routes et choix techniques

## Toutes les routes

```text
BASE URL : http://localhost:4000

AUTHENTIFICATION
GET    http://localhost:4000/auth/csrf
POST   http://localhost:4000/auth/login

PRESTATIONS
GET    http://localhost:4000/prestations/{id}
POST   http://localhost:4000/prestations
PUT    http://localhost:4000/prestations/{id}
```

Ces 5 routes sont les seules implémentées (`AuthController`, `PrestationController`). Le port vient de `server.port=${PORT:4000}` et aucun préfixe (`/api`, `context-path`) n'est défini.

Accès :
- Publiques : `GET /auth/csrf` et `POST /auth/login`.
- Session requise : les trois routes `/prestations`.
- Jeton CSRF requis : tous les `POST` et `PUT`, y compris le login.

## Appeler l'API

Session : `POST /auth/login` renvoie le cookie `JSESSIONID`. Postman et Bruno le renvoient ensuite automatiquement.

CSRF : l'en-tête `X-XSRF-TOKEN` doit contenir la valeur du cookie `XSRF-TOKEN`. Séquence :

1. `GET /auth/csrf` dépose le cookie `XSRF-TOKEN`.
2. Copier sa valeur dans l'en-tête `X-XSRF-TOKEN`.
3. `POST /auth/login`, puis les autres appels. Si un `POST` échoue en `403` après le login, relire le cookie : il peut avoir changé.

Réponses communes :
- `401` sans session : `{"message":"Non authentifié"}`.
- `403` sur un `POST` ou `PUT` sans jeton CSRF valide.

Aucun rôle n'est contrôlé : tout intervenant connecté peut appeler toutes les routes.

## Authentification

### GET /auth/csrf

Dépose le cookie `XSRF-TOKEN`.

- Pas de paramètre ni de corps.
- Réponse : `200`, corps vide.

### POST /auth/login

Ouvre une session pour un intervenant (identifiant : l'email).

En-têtes : `Content-Type: application/json`, `X-XSRF-TOKEN: <jeton>`.

Requête (`LoginRequestDto`) :

```json
{
  "email": "patron@exemple.fr",
  "password": "motdepasse123"
}
```

`email` obligatoire au format email. `password` obligatoire, 8 caractères minimum.

Réponse `200` : corps vide, cookie `JSESSIONID`.

Autres codes :
- `400` : corps invalide, au format d'erreur par défaut de Spring.
- `401` : mauvais mot de passe ou email inconnu, même réponse dans les deux cas.
- `403` : jeton CSRF absent.

Chaîne : `AuthController`, `AuthenticationService`, `AuthenticationManager`, `IntervenantUserDetailsService`, `IntervenantRepository`.

## Prestations

DTO communs :
- `PrestationRequestDto` (entrée) : `nom` et `description` (texte non vide), `typeTarification` (`"FIXE"` ou `"DEVIS"`), `prixMinimum` (décimal, facultatif).
- `PrestationDto` (sortie) : `id`, `nom`, `description`, `active`, `typeTarification`, `prixMinimum` (nombre ou `null`).

Pour `POST` et `PUT` : en-têtes `Content-Type: application/json` et `X-XSRF-TOKEN: <jeton>`.

### GET /prestations/{id}

Lit une prestation, active ou non. Paramètre de chemin : `id`.

Réponse `200` :

```json
{
  "id": 1,
  "nom": "Intérieur véhicule",
  "description": "Nettoyage intérieur voiture ou camionnette",
  "active": true,
  "typeTarification": "DEVIS",
  "prixMinimum": 60.00
}
```

Autres codes : `401`, `404` avec `{"message":"Prestation introuvable"}`.

### POST /prestations

Crée une prestation dans le catalogue commun.

Requête sur devis :

```json
{
  "nom": "Intérieur véhicule",
  "description": "Nettoyage intérieur voiture ou camionnette",
  "typeTarification": "DEVIS",
  "prixMinimum": 60.00
}
```

Requête à tarif fixe :

```json
{
  "nom": "Canapé",
  "description": "Nettoyage de canapé",
  "typeTarification": "FIXE"
}
```

Réponse `201` pour la requête à tarif fixe :

```json
{
  "id": 2,
  "nom": "Canapé",
  "description": "Nettoyage de canapé",
  "active": true,
  "typeTarification": "FIXE",
  "prixMinimum": null
}
```

Règles :
- Toujours créée active.
- `FIXE` : `prixMinimum` est ignoré et enregistré à `null` (le prix viendra des formules, prévues).
- `DEVIS` : `prixMinimum` est conservé.

Autres codes :
- `400` : nom ou description vide, type absent, `{"message":"..."}`. Un type inconnu (`"GRATUIT"`) donne aussi `400`, au format par défaut de Spring.
- `401`, `403`.
- `500` : texte de plus de 255 caractères ou prix hors de `NUMERIC(10,2)`, cas non géré.

### PUT /prestations/{id}

Remplace nom, description, type et prix minimum. Paramètre de chemin : `id`.

Requête (les quatre champs sont réécrits) :

```json
{
  "nom": "Intérieur véhicule",
  "description": "Nettoyage intérieur voiture ou camionnette",
  "typeTarification": "DEVIS",
  "prixMinimum": 70.00
}
```

Réponse `200` :

```json
{
  "id": 1,
  "nom": "Intérieur véhicule",
  "description": "Nettoyage intérieur voiture ou camionnette",
  "active": true,
  "typeTarification": "DEVIS",
  "prixMinimum": 70.00
}
```

Règles :
- Validation avant toute modification.
- Nouveau type `FIXE` : `prixMinimum` remis à `null`, même s'il est envoyé.
- `id` et `active` ne changent pas.
- Pas de mise à jour partielle : un corps incomplet est refusé.

Autres codes : `400` (comme le `POST`), `401`, `403`, `404` avec `{"message":"Prestation introuvable"}`, `500` (mêmes dépassements que le `POST`).

## Choix techniques

Couches :
- Le contrôleur adapte HTTP : JSON, code de statut, DTO de réponse (mapping manuel).
- Le service porte les règles métier (`PrestationService.save` et `updatePrestation`).
- Le repository est un `JpaRepository` sans requête personnalisée côté prestation.
- Justification proposée : tester les règles sans HTTP ni base, et faire évoluer la table sans changer le contrat de l'API.

DTO :
- Aucune entité JPA dans une signature de contrôleur.
- `PrestationRequestDto` n'a ni `id` ni `active` : un client ne peut pas les imposer.
- `record` : immuable.

Validation :
- Login : Bean Validation avec `@Valid` sur `LoginRequestDto`.
- Prestations : contrôles écrits à la main dans le service ; le DTO n'a aucune annotation et le contrôleur n'utilise pas `@Valid`. C'est un écart avec la convention du projet (forme sur le DTO, métier dans le service).

Erreurs :
- `PrestationExceptionHandler` (`@RestControllerAdvice`) traduit `PrestationValidationException` en `400` et `PrestationNotFoundException` en `404`, en `{"message":"..."}`.
- Aucune stack trace ni message interne exposé (`application.properties`).

REST :
- Ressource au pluriel, id dans le chemin, `POST` en `201`, `PUT` en `200`.
- `PUT` plutôt que `PATCH` : justification proposée, le formulaire enverra les quatre champs.

Sécurité (session, CSRF, CORS) : voir `01-contexte-projet.md`.

## Points ouverts

- `500` sur un texte de plus de 255 caractères ou un prix hors de `NUMERIC(10,2)` : aucun handler.
- Format des `400` non homogène : `{"message"}` pour le service, format par défaut de Spring pour Bean Validation, un enum inconnu ou un JSON illisible.
- Prix minimum négatif accepté.
- Aucun test sur `GET /prestations/{id}` ni sur `GET /auth/csrf`.
- Test `creer_donneesValides_renvoie201` en `@Disabled` avec un motif périmé : le contrôleur renvoie bien `201`.
- Méthode `CreatePrestationDto` du contrôleur : nom hérité d'un ancien DTO.

## Routes prévues, non implémentées

Aucun chemin n'est figé.

- Liste des prestations (publique ou non, à décider).
- Désactivation et réactivation d'une prestation.
- Formules tarifaires.
- Demandes de devis avec photo.
- Disponibilités et rendez-vous : demande, acceptation, refus, annulation.
- Utilisateur connecté et déconnexion.

Une URL inexistante renvoie `401` sans session, `404` avec session.

## Tests

Dans `backend/src/test/java/com/afob/backend/` :
- `auth/controller/AuthControllerTest` : login, `400`, `401`, `403`.
- `prestation/controller/PrestationControllerTest` : `POST` et `PUT`.
- `prestation/service/PrestationServiceTest` : règles de `save` et `updatePrestation`.
- `BackendApplicationTests` : `contextLoads`, application complète sur la base `afob_test`.

Dernière exécution connue : 46 tests, 0 échec, 1 ignoré. Non relancée pour cette documentation.
