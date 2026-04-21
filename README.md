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

## Authenticatie

Deze service verwacht dat er een API gateway voor staat die het JWT-token valideert (handtekening, `exp`, `iss`, enz.). Zelf doet deze service géén validatie — de `SubjectIdFilter` decodeert alleen de payload van de `Authorization: Bearer …` header, leest de `sub`-claim en zet die in een request-scoped `SubjectIdContext`. Controllers gebruiken dat subject-ID als sleutel voor voorkeuren (postcode/onderwerp/favoriet) en berichten.

**Belangrijk:** stel deze service nooit direct bloot aan het publieke internet. Zonder gateway ervoor accepteert de service een token met `"alg":"none"` en kan iedereen elke `sub` verzinnen. In elke deployment hoort dus een gateway die signatures en claims verifieert.

De claim-naam is configureerbaar via `auth.subject-claim-name` (default `sub`).
