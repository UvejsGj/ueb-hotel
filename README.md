# ueb-hotel

Aplikacion për recepsionin e një hoteli të vogël: dhomat dhe rezervimet e mysafirëve.
Specifikimi i plotë është te [SPEC.md](SPEC.md). Ky është **faza 1**.

## Teknologjitë

Java 25 · Spring Boot 4.1 · Spring MVC + Thymeleaf · Spring Security · Spring Data JPA · PostgreSQL 17 (Docker)

## Si niset

Kërkohet Docker Desktop i ndezur. PostgreSQL niset vetë nga `compose.yaml` kur starton aplikacioni.

```bash
./mvnw spring-boot:run
```

Pastaj hapni <http://localhost:8080>.

## Përdoruesit (vetëm për zhvillim)

| Përdoruesi | Fjalëkalimi | Roli |
|---|---|---|
| `recepsion` | `recepsion123` | USER |
| `admin` | `admin123` | ADMIN |

Kur databaza është bosh, aplikacioni shton 4 dhoma fillestare (101, 102, 201, 301).

## Struktura

```
src/main/java/com/ueb/hotel/
  config/      SecurityConfig (login, rolet), DataSeeder (dhomat fillestare)
  model/       Room, RoomType, Reservation
  repository/  RoomRepository, ReservationRepository
  web/         ReservationController, RoomController, HomeController
  web/form/    ReservationForm, RoomForm (validimi)
src/main/resources/templates/
  layout.html, fragments.html, login.html, access-denied.html, error/404.html
  reservations/  list.html, form.html
  rooms/         list.html, form.html, detail.html
```

## Faza 2 (ende jo)

- `@ValidStay` te `ReservationForm` (datat, mbivendosja, kapaciteti)
- `GET /api/rooms/available` + `fetch` në formën e rezervimit
