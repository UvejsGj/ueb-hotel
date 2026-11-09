# Hoteli

> Versioni 1, para rishikimit të AI-së.
> Grupi: Uvejs Gjelaj (lider), Xhemaledin Beluli, Muzlind Matoshi, Dion Kastrati · Repository: `ueb-hotel`

## 1. Përshkrimi

Një aplikacion i brendshëm për recepsionin e një hoteli të vogël: ruan dhomat dhe rezervimet e mysafirëve.
Recepsionistët shohin dhomat dhe regjistrojnë rezervime pasi hyjnë në llogari; administratori i menaxhon dhomat dhe fshin rezervimet.

## 2. Rolet

| Roli | Çka bën |
|---|---|
| USER (recepsionist) | sheh listën e rezervimeve dhe dhomat, shton dhe ndryshon rezervime |
| ADMIN | gjithçka që bën USER, plus: fshin çdo rezervim, shton/ndryshon/fshin dhoma |

Pa llogari: vetëm faqja e login-it.

## 3. Entitetet

**Room**

| Fusha | Tipi | Validimi |
|---|---|---|
| id | Long | gjenerohet |
| number | String | `@NotBlank`, `@Size(max = 10)`, unik në DB (p.sh. “101”, “2A”) |
| type | RoomType (enum: `SINGLE`, `DOUBLE`, `SUITE`) | `@NotNull` |
| capacity | Integer | `@NotNull`, `@Min(1)`, `@Max(6)` |
| pricePerNight | BigDecimal | `@NotNull`, `@DecimalMin("1.00")`, `@Digits(integer = 5, fraction = 2)` |

**Reservation**

| Fusha | Tipi | Validimi |
|---|---|---|
| id | Long | gjenerohet |
| guestName | String | `@NotBlank`, `@Size(max = 80)` |
| guestPhone | String | opsionale, `@Size(max = 20)` |
| checkIn | LocalDate | `@NotNull`, `@FutureOrPresent` |
| checkOut | LocalDate | `@NotNull`, pas `checkIn` (kontrollohet nga `@ValidStay`, faza 2) |
| guests | Integer | `@NotNull`, `@Min(1)`, jo më shumë se `room.capacity` (faza 2) |
| room | Room | `@NotNull` |
| createdBy | String | username-i i atij që e shtoi, vendoset nga serveri |
| createdAt | LocalDateTime | vendoset nga serveri |

## 4. Relacioni

`Room` 1 → shumë `Reservation` (`@OneToMany` te Room, `@ManyToOne` te Reservation).
Një rezervim është për saktësisht një dhomë; një dhomë ka shumë rezervime (në data të ndryshme).
Në faqe: forma e rezervimit e zgjedh dhomën me `<select>` (numri, tipi, çmimi); faqja e dhomës tregon rezervimet e saj.

Një dhomë që ka rezervime nuk fshihet: ADMIN-i merr mesazhin “Dhoma ka rezervime dhe nuk mund të fshihet.”

## 5. Faqet dhe URL-të

| URL | Metoda | Faqja / veprimi | Qasja |
|---|---|---|---|
| `/login` | GET, POST | login.html | të gjithë |
| `/app/reservations` | GET | reservations/list.html | USER, ADMIN |
| `/app/reservations/new` | GET, POST | reservations/form.html | USER, ADMIN |
| `/app/reservations/{id}/edit` | GET, POST | reservations/form.html | USER, ADMIN |
| `/admin/reservations/{id}/delete` | POST | fshin, kthen te lista | ADMIN |
| `/admin/rooms` | GET | rooms/list.html | ADMIN |
| `/admin/rooms/new`, `/{id}/edit`, `/{id}/delete` | GET, POST | rooms/form.html | ADMIN |
| `/app/rooms/{id}` | GET | rooms/detail.html (dhoma + rezervimet) | USER, ADMIN |
| `/access-denied` | GET | access-denied.html | të gjithë |

## 6. Custom validator (faza 2)

`@ValidStay` në nivel klase te `ReservationForm`:

1. `checkOut` duhet të jetë pas `checkIn` (së paku 1 natë).
2. E njëjta dhomë nuk rezervohet dy herë në data që mbivendosen
   (rezervimi `[checkIn, checkOut)`; kur ndryshohet një rezervim, ai vetë nuk llogaritet).
3. `guests` nuk e kalon `capacity` të dhomës.

Mesazhet:
- “Data e largimit duhet të jetë pas datës së ardhjes.”
- “Dhoma është e zënë në këto data.”
- “Dhoma pranon më së shumti {capacity} mysafirë.”

## 7. REST endpoint (faza 2)

`GET /api/rooms/available?from=2026-12-01&to=2026-12-05` → JSON me dhomat e lira në ato data:

```json
[{"id": 4, "number": "104", "type": "DOUBLE", "capacity": 2, "pricePerNight": 45.00}]
```

E thërret `reservations/form.html` me `fetch` sapo përdoruesi i zgjedh datat:
`<select>`-i i dhomave mbushet vetëm me dhomat e lira, dhe forma tregon çmimin total (net × çmimi).

## 8. Ndarja e punës, faza 1

| Kush | Çka |
|---|---|
| Uvejs Gjelaj (lider) | projekti, `compose`/Docker për PostgreSQL, `Room` + `Reservation` + relacioni |
| Xhemaledin Beluli | `layout.html`, `fragments.html`, faqja 404, CSS |
| Muzlind Matoshi | CRUD i rezervimeve: controller, lista, forma, validimi |
| Dion Kastrati | CRUD i dhomave; login-i me përdorues in-memory, logout |

Të gjithë: secili e rishikon PR-in e një tjetri para merge.

## 9. Jashtë fushës

- Nuk ka rezervim online nga mysafirët: vetëm stafi i hotelit e përdor.
- Nuk ka regjistrim të përdoruesve nga faqja (ADMIN i krijon).
- Nuk ka pagesa, fatura, as check-in/check-out të regjistruar.
- Nuk ka foto të dhomave, vlerësime apo komente.
- Nuk ka e-mail, as “kam harruar fjalëkalimin”.
- Nuk ka aplikacion mobil, as frontend të veçantë: vetëm Thymeleaf.
