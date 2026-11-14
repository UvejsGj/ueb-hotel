# CLAUDE.md: rregullat për Claude në projektin ueb-hotel

Ky file e lexon Claude i çdo anëtari para se të shkruajë kod. Katër veta punojnë me katër Claude të ndryshëm në të njëjtin repo: këto rregulla e mbajnë kodin të njëjtë.

## Projekti

- Aplikacion web për recepsionin e një hoteli: dhoma (`Room`) dhe rezervime (`Reservation`). Lexo **SPEC.md**: aty janë emrat e fushave, URL-të dhe rolet. SPEC.md është marrëveshja e grupit; mos shpik emra të tjerë.
- Lënda: Zhvillimi i Ueb-it, Cacttus Education 2026–27. Faza 1 dorëzohet e diel 22.11.2026, 23:59; demo live të hënën 23.11.
- Repository: `https://github.com/UvejsGj/ueb-hotel`. Grupi: Uvejs Gjelaj (lideri), Muzlind Matoshi, Xhemaledin Beluli, Dion Kastrati.

## Si punon Claude këtu

1. **Përgjigju në shqip.** Kodi, emrat e klasave dhe commit-et në anglisht.
2. **Kodi duhet ta shpjegojë studenti, pa AI.** Në mbrojtje secili pyetet rresht për rresht për kodin e vet dhe të shokut. Prandaj:
   - Shkruaj kod të thjeshtë, në stilin e ligjëratave (shembulli "Thëniet" / projekti `hello`). Pa pattern-a të avancuar, pa librari shtesë, pa "zgjuarsi".
   - Pas çdo hapi, shpjego shkurt çka bën çdo file dhe çdo annotation, në shqip.
   - Në fund të punës, shkruaj draftin e hyrjes në `AI-LOG.md` (formati më poshtë).
3. **Rri brenda detyrës së anëtarit** (shih PLAN.md). Mos ndrysho file-t që i takojnë tjetërkujt (tabela "Kush e mban file-n"). Nëse duhet ndryshim aty, thuaje anëtarit që ta kërkojë te pronari.
4. **Mos bëj commit, push apo merge pa leje të anëtarit.** Commit-et bëhen me llogarinë e anëtarit, në branch-in e tij, kurrë direkt në `main`.
5. Kur ndryshon diçka që e prek spec-in (fushë, URL, rregull), përditëso edhe **SPEC.md** në të njëjtin PR.

## Stack-u (i fiksuar, mos e ndrysho)

- Java 25 (Temurin), Spring Boot 4.1, Maven me `mvnw`.
- Starter-at: `spring-boot-starter-webmvc`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `org.postgresql:postgresql` (runtime), Lombok. `spring-boot-starter-security` e shton Dion në `feature/login`.
- PostgreSQL 18 në Docker, porti **5433** në laptop.
- **NUK** pranohen: React/Angular/Vue, aplikacion vetëm me REST dhe JWT, `javax.*` (përdor `jakarta.*`), Spring Boot 3. Faqet vijnë nga Thymeleaf.

## Paketat dhe file-t

```
src/main/java/com/cacttus/hotel/
├── HotelApplication.java
├── config/       SecurityConfig.java
├── domain/       Room.java, RoomType.java, Reservation.java
├── repository/   RoomRepository.java, ReservationRepository.java
├── web/          HomeController.java, RoomController.java, ReservationController.java, AuthController.java
└── SeedData.java

src/main/resources/
├── application.properties
├── application-dev.properties
├── static/css/app.css
└── templates/
    ├── layout.html
    ├── fragments.html
    ├── login.html
    ├── error/404.html
    ├── rooms/        list.html, form.html, detail.html
    └── reservations/ list.html, form.html
```

Faza 2 shton `service/`, `web/dto/`, `validation/`, `access-denied.html`. **Mos i krijo në fazën 1.**

## Konfigurimi

`application.properties`:
```properties
spring.application.name=hotel
spring.profiles.active=dev
```

`application-dev.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/hotel
spring.datasource.username=hotel
spring.datasource.password=${DB_PASSWORD:hotel}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Docker (një herë, në çdo laptop):
```
docker run --name hotel-db -e POSTGRES_USER=hotel -e POSTGRES_PASSWORD=hotel -e POSTGRES_DB=hotel -p 5433:5432 -v hotel-data:/var/lib/postgresql -d postgres:18-alpine
```
Pastaj `docker start hotel-db` / `docker stop hotel-db`. Asnjë fjalëkalim i vërtetë ose çelës API në GitHub.

## Rregullat e kodit (si në ligjërata)

**Entitetet**
- `@Entity @Getter @Setter @NoArgsConstructor`. **Kurrë `@Data`** mbi entitet (equals/hashCode me `id` prish `HashSet`-in).
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- Validimi direkt te fushat e entitetit në fazën 1 (DTO vjen në fazën 2). Mesazhet në shqip: `@NotBlank(message = "Shkruani emrin e mysafirit")`.
- Relacioni: `Reservation.room` me `@ManyToOne` + `@JoinColumn(name = "room_id")` + `@NotNull(message = "Zgjidhni dhomën")`. `Room.reservations` me `@OneToMany(mappedBy = "room")`, `List<Reservation> reservations = new ArrayList<>()`. Pa cascade REMOVE.
- `Reservation.createdBy` dhe `createdAt`: `@Column(updatable = false)`, që ndryshimi i rezervimit të mos i fshijë. `createdAt` vendoset me `@PrePersist`.
- Datat `checkIn`/`checkOut`: `LocalDate` me `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`, ndryshe `<input type="date">` nuk lidhet.
- "Dalja pas hyrjes": metodë `@AssertTrue(message = "Data e daljes duhet të jetë pas datës së hyrjes") public boolean isCheckOutAfterCheckIn()`. Kthen `true` kur njëra datë është `null` (atë e kap `@NotNull`). Gabimi lexohet me `#fields.hasErrors('checkOutAfterCheckIn')`.

**Controller-at**
- `@Controller` (jo `@RestController`) që kthen emrin e template-it, p.sh. `"reservations/list"`.
- Dependency injection me konstruktor (`@RequiredArgsConstructor` + `private final`).
- Forma: `GET .../new` dhe `GET .../{id}/edit` e tregojnë formën; `POST` e ruan.
- `@Valid @ModelAttribute("reservation") Reservation reservation, BindingResult result`: **`BindingResult` menjëherë pas `@Valid`**. Me gabime: kthe formën (jo redirect). Pa gabime: ruaj, pastaj `return "redirect:/app/reservations";` (PRG, që F5 të mos ruajë dy herë).
- Listat për `<select>`: `@ModelAttribute("rooms") public List<Room> rooms()` në controller. Opsioni ka `th:value="${r.id}"`; Spring e kthen id-në në `Room`.
- Mesazhet pas veprimit: `RedirectAttributes.addFlashAttribute("message", "...")` për sukses, `"error"` për gabim. I shfaq fragmenti `flash` te layout-i.
- Fshirja vetëm me `POST`, me `<form method="post" th:action="@{...}">` dhe buton. Kurrë fshirje me link GET.
- `id` që nuk ekziston: `throw new ResponseStatusException(HttpStatus.NOT_FOUND)`, që del faqja 404.

**Template-t (Thymeleaf)**
- Çdo faqe: `<html th:replace="~{layout :: layout(~{::title}, ~{::main})}">` me `<title>` dhe `<main>` të vetat. Mos kopjo `<head>`, menynë apo footer-in në faqe.
- Fragmentet me `th:replace="~{fragments :: header('rooms')}"` (sintaksa `~{...}`, jo e vjetra). Çelësat e menysë: `'reservations'`, `'rooms'`.
- `th:text` gjithmonë, **kurrë `th:utext`** për të dhëna të përdoruesit (XSS).
- Format me `th:action="@{...}"` (jo `action=""` i thjeshtë): kështu Spring Security e shton vetë token-in CSRF.
- Gabimi pranë fushës: `<p class="error" th:if="${#fields.hasErrors('guestName')}" th:errors="*{guestName}"></p>`.
- Klasat CSS të përbashkëta (i mban Muzlind te `app.css`): `.error`, `.flash-success`, `.flash-error`, `.btn`, `.btn-danger`, `.table`, `.form-row`. Mos shto CSS inline nëpër faqe.

## Kush e mban file-n (pronari)

| File | Pronari | Të tjerët |
|---|---|---|
| `pom.xml`, `application*.properties`, entitetet, repository-t, `SeedData.java` | Uvejs | kërkojnë ndryshim te Uvejs (metodë e re në repository: shtojeni vetë në PR-in tuaj dhe shënojeni) |
| `layout.html`, `fragments.html`, `app.css`, `error/404.html` | Muzlind | linku i ri në meny: kërkojeni te Muzlind |
| `ReservationController`, `reservations/*` | Xhemaledin | |
| `RoomController`, `rooms/*`, `SecurityConfig`, `AuthController`, `login.html` | Dion | Dion shton `spring-boot-starter-security` te `pom.xml` |
| `SPEC.md`, `AI-LOG.md` | të gjithë; Muzlind koordinon | |

## Git

- Branch-et: `feature/entities` (Uvejs), `feature/layout` (Muzlind), `feature/reservations` (Xhemaledin), `feature/rooms` dhe `feature/login` (Dion).
- Para punës: `git checkout main && git pull`, pastaj `git checkout -b feature/...`. Kur `main` ndryshon: `git pull origin main` në branch-in tuaj.
- Commit-e të vogla me mesazh të qartë: `Add Room entity`, `Add reservation form validation`.
- Çdo PR e rishikon një anëtar tjetër para merge. Kurrë `git push --force` në `main`.
- Para PR-it: `./mvnw spring-boot:run` niset pa gabime dhe faqet tuaja punojnë në shfletues.

## AI-LOG.md

Çdo përdorim i Claude që dha kod ose zgjidhje merr një hyrje:

```markdown
### 2026-11-15, Entitetet Room dhe Reservation, Uvejs

- **Pyetja:** përmbledhja e promptit
- **Përgjigjja:** çka bëri Claude, shkurt
- **Çka bëmë:** çka pranuam, çka ndryshuam ose refuzuam, dhe pse
- **Si e verifikuam:** si e provuam (nisja, faqja, tabela në psql…)
```
