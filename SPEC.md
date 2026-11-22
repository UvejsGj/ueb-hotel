# SPEC: Hotel Management

> Versioni 1, para rishikimit të AI-së.
> Grupi: Uvejs Gjelaj (lideri), Muzlind Matoshi, Xhemaledin Beluli, Dion Kastrati · Repository: `ueb-hotel`

## 1. Përshkrimi

Një aplikacion për recepsionin e një hoteli të vogël: stafi i sheh dhomat dhe i regjistron rezervimet e mysafirëve.
Recepsionisti krijon dhe ndryshon rezervime; menaxheri (administratori) i menaxhon dhomat dhe fshin rezervime.
Aplikacioni është vetëm për stafin e hotelit, jo për mysafirët.

## 2. Rolet

| Roli | Çka bën |
|---|---|
| USER (recepsionisti) | sheh listën e dhomave dhe faqen e secilës dhomë; sheh, shton dhe ndryshon rezervime |
| ADMIN (menaxheri) | gjithçka që bën USER, plus: fshin çdo rezervim, shton/ndryshon/fshin dhoma |

Pa llogari (vetëm lexim): lista e rezervimeve, lista e dhomave dhe faqja e dhomës, pa e-mail-in e mysafirit. Krijimi, ndryshimi dhe fshirja kërkojnë login: "Rezervim i ri" ose "Ndrysho" të çojnë te `/login`, dhe pas login-it të kthejnë te forma. Header-i tregon "Hyr" pa login, dhe "Përshëndetje, emri" me "Dil" pas login-it.

USER-i nuk i sheh butonat e ADMIN-it ("Fshij" te rezervimet; "Dhomë e re", "Ndrysho" dhe "Fshij" te dhomat); "Ndrysho" te rezervimet i mbetet. Nëse e hap adresën `/admin/...` me dorë, sheh faqen 403.

Faza 1: përdoruesit janë in-memory: `user` (ROLE_USER), `admin` (ROLE_ADMIN), dhe një për secilin anëtar të grupit, me ROLE_ADMIN: `uvejs`, `muzlind`, `xhemaledin`, `dion`. Fjalëkalimet lexohen nga `application-dev.properties` (vlera për zhvillim, p.sh. `uvejs123`).

Faza 2: përdoruesit ruhen në databazë (entiteti `AppUser`), fjalëkalimet me BCrypt. `/admin/**` vetëm ADMIN, `/app/**` USER dhe ADMIN; kush s'ka qasje e sheh faqen `/access-denied`.

## 3. Entitetet

**Room** (dhoma)

| Fusha | Tipi | Validimi |
|---|---|---|
| id | Long | gjenerohet |
| number | String | `@NotBlank`, `@Size(max = 10)`, unik në databazë (p.sh. "101", "203A") |
| type | RoomType (enum: `SINGLE`, `DOUBLE`, `SUITE`) | `@NotNull` |
| floor | Integer | `@NotNull`, `@Min(0)`, `@Max(20)` |
| capacity | Integer | `@NotNull`, `@Min(1)`, `@Max(6)` |
| pricePerNight | BigDecimal | `@NotNull`, `@Positive`, `@Digits(integer = 5, fraction = 2)` |

**Reservation** (rezervimi)

| Fusha | Tipi | Validimi |
|---|---|---|
| id | Long | gjenerohet |
| guestName | String | `@NotBlank`, `@Size(max = 100)` |
| guestEmail | String | `@NotBlank`, `@Email` |
| checkIn | LocalDate | `@NotNull`, `@DateTimeFormat(iso = ISO.DATE)` |
| checkOut | LocalDate | `@NotNull`, `@DateTimeFormat(iso = ISO.DATE)`, pas `checkIn` (`@AssertTrue` te metoda `isCheckOutAfterCheckIn()`) |
| guests | Integer | `@NotNull`, `@Min(1)`, `@Max(6)` |
| room | Room | `@NotNull` |
| createdBy | String | username-i i atij që e shtoi, vendoset nga serveri |
| createdAt | LocalDateTime | vendoset nga serveri |

**AppUser** (faza 2)

| Fusha | Tipi | Validimi |
|---|---|---|
| id | Long | gjenerohet |
| username | String | `@NotBlank`, `@Size(min = 3, max = 30)`, unik në databazë |
| password | String | hash me BCrypt, kurrë tekst i qartë |
| role | Role (enum: `USER`, `ADMIN`) | `@NotNull` |

Mesazhet e gabimeve janë në shqip, p.sh. "Shkruani emrin e mysafirit", "E-mail-i nuk është i saktë", "Data e daljes duhet të jetë pas datës së hyrjes".

Faza 1: format lidhen direkt me entitetet `Room` dhe `Reservation`. Faza 2: format përdorin DTO (`RoomForm`, `ReservationForm`) dhe shtresat Controller → Service → Repository; controller-i nuk e thërret repository-n direkt.

## 4. Relacioni

`Room` 1 → shumë `Reservation` (`@OneToMany(mappedBy = "room")` te Room, `@ManyToOne` te Reservation).
Një rezervim është për saktësisht një dhomë; një dhomë ka shumë rezervime me kohë.
Foreign key-n `room_id` e mban tabela `reservation`.

Në faqe: forma e rezervimit e zgjedh dhomën me `<select>`; faqja e dhomës tregon rezervimet e saj (me `left join fetch`).

Fshirja e një dhome që ka rezervime nuk lejohet: lista tregon mesazhin "Dhoma ka rezervime dhe nuk mund të fshihet." Pa këtë rregull databaza kthen 500.

## 5. Faqet dhe URL-të

| URL | Metoda | Faqja / veprimi | Qasja |
|---|---|---|---|
| `/login` | GET, POST | login.html | të gjithë |
| `/logout` | POST | kthen te `/login?logout` | USER, ADMIN |
| `/` | GET | redirect te `/app/reservations` | të gjithë |
| `/app/reservations` | GET | reservations/list.html | të gjithë (e-mail-i vetëm i kyçur) |
| `/app/reservations/new` | GET, POST | reservations/form.html | USER, ADMIN |
| `/app/reservations/{id}/edit` | GET, POST | reservations/form.html | USER, ADMIN |
| `/admin/reservations/{id}/delete` | POST | fshin, kthen te lista | ADMIN |
| `/app/rooms` | GET | rooms/list.html | të gjithë |
| `/app/rooms/{id}` | GET | rooms/detail.html (dhoma + rezervimet) | të gjithë (e-mail-i vetëm i kyçur) |
| `/admin/rooms/new` | GET, POST | rooms/form.html | ADMIN |
| `/admin/rooms/{id}/edit` | GET, POST | rooms/form.html | ADMIN |
| `/admin/rooms/{id}/delete` | POST | fshin nëse s'ka rezervime, kthen te lista | ADMIN |
| `/access-denied` | GET | access-denied.html (faza 2) | të gjithë |
| `/api/rooms/available` | GET | JSON (faza 2) | USER, ADMIN (i kyçur) |

Çdo adresë që s'ekziston tregon `error/404.html`. Kush s'ka qasje (p.sh. USER te `/admin/**`) sheh `error/403.html` në fazën 1 (në fazën 2 `/access-denied`). Të gjitha faqet përdorin `layout.html` dhe `fragments.html` (header me menynë, footer).

## 6. Custom validator (faza 2)

`@RoomAvailable` mbi klasën `ReservationForm`: e njëjta dhomë nuk rezervohet dy herë për data që mbivendosen.
Dy rezervime mbivendosen kur `ekzistuesi.checkIn < iRi.checkOut` dhe `ekzistuesi.checkOut > iRi.checkIn`.
Kur ndryshohet një rezervim, kontrolli e përjashton vetë atë rezervim (sipas `id`).
Dita e daljes së njërit mund të jetë dita e hyrjes së tjetrit.
Përdoret te krijimi dhe ndryshimi i rezervimit.
Mesazhi: “Dhoma është e zënë në këto data.” Shfaqet në formë pranë fushës së dhomës.

## 7. Kërkimi dhe REST (faza 2)

**Kërkimi** te `/app/reservations`, në server, me dy kritere, faqe nga 10 rezervime dhe renditje:

- emri i mysafirit (pjesë e emrit, pa dallim shkronjash të mëdha/të vogla)
- intervali i datave `from`–`to` (rezervimet që bien brenda këtij intervali)
- renditja: sipas `checkIn` (default, nga më e afërta), `guestName` ose numrit të dhomës; kolonat e tabelës janë linqe për renditje

`GET /app/reservations?guest=hoxha&from=2026-12-01&to=2026-12-31&page=0&sort=checkIn,asc`

**REST endpoint:** `GET /api/rooms/available?from=2026-12-01&to=2026-12-05&guests=2` → JSON, dhomat e lira për ato data me kapacitet të mjaftueshëm:

```json
[{"id": 3, "number": "203", "type": "DOUBLE", "capacity": 2, "pricePerNight": 45.00}]
```

E thërret `reservations/form.html` me `fetch` sa herë që ndryshojnë datat ose numri i mysafirëve, dhe e mbush `<select>`-in e dhomave vetëm me dhomat e lira.

## 8. Ndarja e punës, faza 1

| Kush | Çka |
|---|---|
| Uvejs Gjelaj (lideri) | projekti (start.spring.io, Maven, Java 25, Boot 4.1), PostgreSQL `hotel-db` në Docker në portin 5433, `application-dev.properties`, `Room` + `Reservation` + relacioni, repository-t, `SeedData` |
| Muzlind Matoshi | `layout.html`, `fragments.html`, `app.css`, faqet 404 dhe 403; koordinon `SPEC.md`, rishikimin e AI-së dhe `AI-LOG.md` |
| Xhemaledin Beluli | CRUD i rezervimeve: controller, lista, forma me `<select>` të dhomave, validimi, gabimet pranë fushës |
| Dion Kastrati | CRUD i dhomave dhe faqja e dhomës me rezervimet; login-i me përdorues in-memory, faqja juaj e login-it, logout |

Të gjithë: secili punon në branch-in e vet (`feature/...`) dhe e rishikon PR-in e një tjetri para merge në `main`.

## 9. Jashtë fushës

- Nuk ka faqe publike ku mysafirët rezervojnë vetë; rezervimet i bën vetëm stafi.
- Nuk ka regjistrim të përdoruesve nga faqja (ADMIN i krijon).
- Nuk ka pagesa online, fatura apo llogaritje të taksave.
- Nuk ka e-mail për mysafirët, as “kam harruar fjalëkalimin”.
- Nuk ka foto të dhomave, as vlerësime/komente.
- Nuk ka shumë hotele: aplikacioni është për një hotel.
- Nuk ka aplikacion mobil, as frontend të veçantë: vetëm Thymeleaf.
