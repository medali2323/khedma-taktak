# Khedma Taktak

Plateforme de recrutement — Angular + Spring Boot.

## Structure

```
khedma-taktak/
├── backend/    Spring Boot 3.3 (Java 17)
└── frontend/   Angular 22
```

## Variables d'environnement (backend)

Créez le fichier **`backend/src/main/resources/.env`** (copie de `.env.example`) :

```powershell
cd backend\src\main\resources
copy .env.example .env
```

Éditez `.env` avec vos identifiants :

```env
DB_USERNAME=root
DB_PASSWORD=votre_mot_de_passe_mysql
JWT_SECRET=dev-local-jwt-secret-key-minimum-32-chars!!
```

Spring Boot charge ce fichier au démarrage (STS, Maven, JAR). Les variables système (`$env:DB_PASSWORD`) restent prioritaires.

| Variable                        | Description                                                           |
| ------------------------------- | --------------------------------------------------------------------- |
| `JWT_SECRET`                    | Clé secrète JWT (≥ 32 caractères) — défaut en dev local si absent     |
| `DB_URL`                        | JDBC MySQL (défaut : `jdbc:mysql://localhost:3306/khedma_taktak?...`) |
| `DB_USERNAME`                   | Utilisateur MySQL (défaut : `root`)                                   |
| `DB_PASSWORD`                   | Mot de passe MySQL                                                    |
| `JWT_ACCESS_EXPIRATION_MINUTES` | Durée access token (défaut : 60)                                      |

## API Auth


| Méthode | Endpoint             | Auth   |
| ------- | -------------------- | ------ |
| POST    | `/api/auth/register` | Public |
| POST    | `/api/auth/login`    | Public |
| POST    | `/api/auth/refresh`  | Public |
| GET     | `/api/health`        | Public |


Register body :

```json
{
  "firstName": "Ali",
  "lastName": "Ben",
  "email": "ali@example.com",
  "password": "password123",
  "userType": "CANDIDATE"
}
```

`userType` : `CANDIDATE`, `STUDENT`, `TRADES_WORKER`

Les identifiants utilisateur sont des **UUID** (CHAR(36)).

## Démarrage

### Backend

```bash
cd backend
mvn spring-boot:run
```

API : [http://localhost:8080/api/health](http://localhost:8080/api/health)

### Frontend

```bash
cd frontend
npm install
npm start
```

App : [http://localhost:4200](http://localhost:4200)

## Tests

Backend (H2, profil `test`) :

```bash
cd backend
mvn test
```

Frontend :

```bash
cd frontend
npm run build
```

