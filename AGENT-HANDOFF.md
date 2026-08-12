# Brief agent — Khedma Taktak

Document de passation après le **commit auth** (`parti auth`). À lire avant toute nouvelle implémentation.

---

## Projet

- **Repo** : `C:\Users\PC_DALI\Documents\khedma-taktak`
- **Stack** : Spring Boot 3.3.5 + Angular 22 + MySQL + Flyway
- **Java** : **17** (pas 21 — choix utilisateur pour compatibilité STS)
- **Origine** : évolution de `portfolio-builder` ; repo recréé vide puis commits par parties

---

## Commits faits

| Commit | Contenu |
|--------|---------|
| `first commit` | Scaffold Angular + Spring Boot (`GET /api/health`) |
| `parti auth` | Auth JWT complète (backend + frontend) — **validé et committé par l'utilisateur** |

---

## Partie auth — ce qui est livré

### Backend

- **Flyway** `V1__auth.sql` : tables `users` (UUID `CHAR(36)`) + `refresh_tokens`
- **UserType** : `CANDIDATE`, `STUDENT`, `TRADES_WORKER`
- **Endpoints** : `POST /api/auth/register`, `/login`, `/refresh` ; `GET /api/health` public
- **JWT** + refresh token hashé SHA-256 en DB
- **Noms** sur `users` (`first_name`, `last_name`) — **pas encore de module profil**
- **Tests** : `AuthControllerIntegrationTest` (profil H2 `test`)
- **DotenvEnvironmentPostProcessor** : charge `.env` depuis classpath ou `src/main/resources/.env`
- **JwtService** : clé dérivée sans Base64 forcé (fix erreur `Illegal base64 character: '_'`)

### Frontend

- Pages **login**, **register**, **dashboard**
- `auth.service.ts` (localStorage `kt_*`), guards, interceptor
- **Redirect 401 + 403** → `/login` (hors `/api/auth/*`)

### Config locale (corrections utilisateur)

- `application.yml` : secret JWT par défaut dev `${JWT_SECRET:dev-local-jwt-secret-key-minimum-32-chars!!}`
- `.env.example` dans `backend/src/main/resources/`
- **MySQL** : utilisateur a configuré mot de passe local (voir `.env` / `application.yml` — **ne pas committer de secrets en prod**)

---

## Démarrage local (validé par l'utilisateur)

### Prérequis

- JDK **17** (ex. Eclipse Adoptium)
- **MySQL** sur port 3306, base `khedma_taktak`
- **Maven** (ou Maven portable dans `%TEMP%` si absent du PATH)
- Node **≥ 24.15.0** pour Angular CLI (`npm start`)

### Backend

```powershell
cd backend
# Option : copier .env.example → .env et ajuster
mvn clean spring-boot:run
```

Variables utiles : `JWT_SECRET`, `DB_PASSWORD`, `DB_URL`, `DB_USERNAME`

### Frontend

```powershell
cd frontend
npm install
npm start
```

Proxy : `/api` → `http://localhost:8080`

### STS / Eclipse

- Importer **uniquement** le dossier `backend` (Maven)
- JRE **JavaSE-17**
- Lancer : **Run As → Spring Boot App** ou **Maven build** `spring-boot:run`
- **Ne pas** lancer en Java Application si `target/classes` n'est pas compilé

---

## Décisions produit (plan global)

1. **UUID** pour `users.id` (pas auto-increment)
2. **Métiers manuels** : `TRADES_WORKER` + taxonomy à venir
3. **Notes recruteur** + stats visibilité candidat = feature future (gratuite)
4. **Paiement** : provider non décidé
5. Commits **part par part** sur repo propre

---

## Prochaines parties (ordre plan Phase 1)

| # | Partie | Contenu principal |
|---|--------|-------------------|
| 3 | Profil / wizard | tables `profiles`, wizard candidat, slug public |
| 4 | Import CV | PDF + Word (Apache POI), Ollama optionnel |
| 5 | ATS | scoring, optimisation CV, `/api/ats/*` |
| 6 | Métiers | référentiel trades, champs sectoriels |
| 7+ | Entreprise / matching / monétisation | phases 2–4 |

Référence implémentation : `portfolio-builder` (non committé là-bas).

---

## Points d'attention pour le prochain agent

1. **Secrets** : retirer mot de passe MySQL en dur de `application.yml` si encore présent → utiliser `${DB_PASSWORD:}` + `.env` (gitignoré)
2. **Ne pas éditer** les fichiers de plan `.cursor/plans/` sauf demande
3. **Proportionnalité** : skill `coding-standards-solid` — pas de sur-architecture sur petites tâches
4. **Validation** : `mvn test` (H2), `npm run build` / `tsc` frontend
5. **Commit** : seulement si l'utilisateur le demande explicitement
6. L'utilisateur développe souvent sous **STS** ; prévoir Java 17 et config Eclipse cohérente

---

## Fichiers clés auth

```
backend/
  src/main/resources/db/migration/V1__auth.sql
  src/main/java/com/khedmataktak/
    controller/AuthController.java
    service/AuthService.java
    config/SecurityConfig.java, JwtProperties.java, DotenvEnvironmentPostProcessor.java
    security/JwtService.java, JwtAuthenticationFilter.java
  src/test/java/.../AuthControllerIntegrationTest.java

frontend/src/app/
  services/auth.service.ts
  interceptors/auth.interceptor.ts
  guards/auth.guard.ts
  pages/login/, register/, dashboard/
  models/auth.models.ts
```

---

## Message utilisateur pour nouvelle session

> « Repo khedma-taktak : scaffold + **auth committé** (`parti auth`). Java 17, UUID users, JWT, login/register/dashboard Angular. Lire `AGENT-HANDOFF.md`. Continuer avec **[partie suivante du plan]** en respectant coding-standards-solid et validate-work. Ne pas recommitter l'auth. »

Remplacer `[partie suivante du plan]` par ex. : *profil/wizard*, *import CV*, *ATS*, etc.
