# moza-actualiteiten-service

Service die actualiteiten-gerelateerde informatie (artikelen, subsidies, wetswijzigingen, bekendmakingen) verzamelt, cached en aanbiedt aan het MOZa portaal. Host ook de actualiteiten-specifieke voorkeuren (postcode, onderwerp, favoriete artikelen) per ADR 0016.

## Ontwikkelen

Postgres vereist (zie `docker-compose.yml`):

```bash
docker compose up -d postgres
./mvnw quarkus:dev
```

Dev server draait op `http://localhost:8080`. Health check: `GET /api/actualiteitenservice/v1/health`.

## Tests

```bash
./mvnw test
```
