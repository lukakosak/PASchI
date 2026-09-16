Originales Repository: https://github.com/osl2/PASchI

## Lokale Docker-Umgebung

Die lokale Umgebung besteht aus einem Vue-Frontend hinter Nginx, einer Spring-Boot-API und MySQL.

```text
Browser → Nginx/Vue-Frontend → Spring-Boot-API → MySQL
```

### Voraussetzungen

- Docker Desktop mit Docker Compose v2
- OpenSSL

### Lokale Konfiguration

Lege neben `docker-compose.yml` eine Datei `.env` an:

```dotenv
MYSQL_DATABASE=paschi
MYSQL_USER=paschi
MYSQL_PASSWORD=lokales-datenbank-passwort
MYSQL_ROOT_PASSWORD=anderes-root-passwort
SEED_ADMIN_PASSWORD=lokales-admin-passwort
```

### Lokale JWT-Schlüssel erzeugen

```bash
mkdir -p docker/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out docker/keys/jwt-private.pem
openssl rsa -pubout -in docker/keys/jwt-private.pem -out docker/keys/jwt-public.pem
```

Diese Schlüssel sind ausschließlich für die lokale Entwicklung vorgesehen.

### Anwendung starten

```bash
docker compose up --build
```

Die Anwendung ist anschließend unter [http://localhost:8081](http://localhost:8081) erreichbar.

### Lokaler Administrator

Wenn der Admin-Seeder aktiviert ist, wird beim ersten Start dieses Konto angelegt:

```text
E-Mail:    admin@local.test
Passwort: Wert von SEED_ADMIN_PASSWORD aus .env
```

Eine spätere Änderung von `SEED_ADMIN_PASSWORD` ändert das Passwort eines bereits bestehenden Kontos nicht. Der Seeder darf nur lokal aktiviert werden.

## Nützliche Docker-Befehle

```bash
# API-Logs verfolgen
docker compose logs -f api

# Nur die API neu bauen und starten
docker compose up --build --force-recreate api

# Container anhalten, Datenbank behalten
docker compose down

# Container und sämtliche lokalen Datenbankdaten löschen
docker compose down -v
```

`docker compose down -v` ist destruktiv: Alle lokalen Benutzer:innen, Kurse, Räume und Sitzungen werden gelöscht.

## Technologien

- Frontend: Vue 3, TypeScript, Vuetify, Vite PWA
- Backend: Java 17, Spring Boot, Spring Security, JPA
- Datenbank: MySQL 8
- Lokale Laufzeitumgebung: Docker Compose

## Projektstruktur

```text
Implementierung/
├── Backend/                 Spring-Boot-API
├── frontend/paschi/         Vue-Frontend
└── backendkeys/             älteres Deployment-Material

docker/
├── keys/                    lokale JWT-Schlüssel
└── nginx/                   Reverse-Proxy-Konfiguration
```

## Entwicklung und Tests

Frontend:

```bash
cd Implementierung/frontend/paschi
yarn install --frozen-lockfile
yarn dev
yarn test
```

Backend:

```bash
cd Implementierung/Backend
mvn test
```

## Lizenz

Siehe [LICENSE](LICENSE).
