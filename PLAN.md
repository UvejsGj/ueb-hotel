# PLAN.md: faza 1 e ueb-hotel

Afati: **e diel 22.11.2026, 23:59** (kodi në `main`). Demo live: **e hënë 23.11 (M4)**.
Repository: `https://github.com/UvejsGj/ueb-hotel`

Secili punon me Claude-in e vet. Claude i çdo anëtari e lexon **CLAUDE.md** dhe **SPEC.md** para çdo detyre; kështu katër Claude shkruajnë kod me të njëjtat emra dhe të njëjtin stil.

> **Kujdes:** 8 nga 10 pikët e mbrojtjes janë "shpjego kodin tënd dhe kodin e shokut, pa AI". Claude e shkruan, por ju duhet ta dini çdo rresht. Pas çdo detyre kërkoni nga Claude shpjegimin dhe lexojeni.

## Renditja dhe varësitë

```
1. Uvejs: repo + skeleti + entitetet ──► merge në main (e diel 15.11)
                    │
2. Muzlind: layout + fragmente ─────────► merge në main (e hënë 16.11)
                    │
3. Xhemaledin: CRUD rezervimet ─┐
   Dion: CRUD dhomat ───────────┼──────► merge (e mërkurë 18.11)
                                │
4. Dion: login + logout ────────────────► merge (e premte 20.11)
5. Të gjithë: testimi + SPEC + AI-LOG ──► e shtunë 21.11
6. Dorëzimi ─────────────────────────────► e diel 22.11, para 20:00
```

Muzlind mund ta fillojë layout-in menjëherë pasi Uvejs e shtyn skeletin (pa pritur entitetet). Xhemaledin dhe Dion fillojnë pasi të jetë në `main` `feature/entities`; kur hyn layout-i, bëjnë `git pull origin main`.

## Rishikimi i PR-ve (kush e rishikon kë)

| PR-i i | E rishikon |
|---|---|
| Uvejs | Muzlind |
| Muzlind | Dion |
| Xhemaledin | Uvejs |
| Dion | Xhemaledin |

Uvejs (lideri) e bën merge pasi rishikuesi e aprovon.

---

## 1. Uvejs Gjelaj (lideri): skeleti, Docker, entitetet

**Branch:** `feature/entities` · **Afati:** e diel 15.11

**Para Claude-it (me dorë):**
1. Repo `UvejsGj/ueb-hotel` private; Settings → Collaborators: `Muzlind`, `Xhemaledin`, `Dion`, `jonkursani`.
2. start.spring.io: Maven, Java 25, Boot 4.1, Group `com.cacttus`, Artifact `hotel`, package `com.cacttus.hotel`. Dependencies: Spring Web, Thymeleaf, Spring Data JPA, PostgreSQL Driver, Validation, Lombok. **Pa Security.**
3. Hidheni projektin në repo; shtoni `SPEC.md`, `CLAUDE.md`, `PLAN.md`, `AI-LOG.md` (bosh me titull). Commit në `main`: `Initial project, spec and plan`.
4. Docker: komanda te CLAUDE.md.

**Prompti për Claude:**
```text
Lexo CLAUDE.md dhe SPEC.md. Jam Uvejs, punoj në branch-in feature/entities.
Detyra:
1. Konfiguro application.properties dhe application-dev.properties ashtu si në CLAUDE.md.
2. Krijo entitetet Room, RoomType (enum SINGLE, DOUBLE, SUITE) dhe Reservation në paketën domain,
   me fushat dhe validimin saktësisht si te SPEC.md pjesa 3, dhe relacionin si te pjesa 4.
   Room.number unik (@Column(unique = true)). createdBy/createdAt me @Column(updatable = false), createdAt me @PrePersist.
3. Krijo RoomRepository dhe ReservationRepository (JpaRepository). Shto:
   - RoomRepository: findAllByOrderByNumberAsc(), existsByNumber(String), existsByNumberAndIdNot(String, Long),
     dhe një @Query me "left join fetch r.reservations" për ta marrë dhomën me rezervimet sipas id.
   - ReservationRepository: findAllByOrderByCheckInAsc(), existsByRoomId(Long).
4. Krijo SeedData (CommandLineRunner) që, vetëm kur tabela room është bosh, shton 6 dhoma (2 SINGLE, 3 DOUBLE, 1 SUITE,
   në kate 1–3) dhe 4 rezervime në dhoma të ndryshme, në dhjetor 2026.
5. HomeController: GET "/" bën redirect te /app/reservations.
Shpjegomë në shqip çdo annotation të entiteteve dhe pse mappedBy është te Room. Në fund jepmë draftin e AI-LOG.md.
```

**Gati kur:** aplikacioni niset me `./mvnw spring-boot:run`; `docker exec -it hotel-db psql -U hotel -c "select * from room"` tregon 6 dhoma; tabela `reservation` ka kolonën `room_id`.

---

## 2. Muzlind Matoshi: layout, fragmentet, CSS, 404 + spec-i

**Branch:** `feature/layout` · **Afati:** e hënë 16.11 (spec v2: e diel 15.11)

**Para kodit, spec-i (e shtunë/e diel):**
1. Hapni `review-prompt.txt` dhe ngjiteni të plotë në një chat **të ri** (ChatGPT, Gemini ose Claude i ri). Jo në chat-in ku u shkrua spec-i.
2. Për çdo koment: ✔ pranojmë (ndryshojmë spec-in), ✂ jo tani (te pjesa 9 ose faza 2), ✘ e gabuar (me arsye).
3. Ndryshimet te `SPEC.md`, commit nga llogaria juaj: `Spec v2 pas rishikimit`. Hyrja e parë te `AI-LOG.md`.

**Prompti për Claude:**
```text
Lexo CLAUDE.md dhe SPEC.md. Jam Muzlind, punoj në branch-in feature/layout.
Detyra:
1. templates/layout.html: fragmenti layout(title, content) me <head> (charset, viewport, app.css),
   header-in, fragmentin flash, <main> nga content, footer-in.
2. templates/fragments.html me fragmentet:
   - header(active): emri "Hotel" dhe menyja: Rezervimet (/app/reservations), Dhomat (/app/rooms),
     me th:classappend 'active' sipas parametrit; djathtas një formë POST te /logout me butonin "Dil".
   - flash: shfaq ${message} (class flash-success) dhe ${error} (class flash-error) vetëm kur ekzistojnë.
   - footer: "© 2026 ueb-hotel · Cacttus Education".
3. static/css/app.css: dizajn i pastër dhe i thjeshtë, pa framework. Klasat: .error, .flash-success, .flash-error,
   .btn, .btn-danger, .table, .form-row, nav a.active. Tabela dhe format të lexueshme në laptop.
4. templates/error/404.html që përdor layout-in: "Faqja nuk u gjet" dhe link te /app/reservations.
5. Një faqe provë te /app/rooms nuk është detyra ime: mos krijo controller. Provoje 404-ën me një adresë që s'ekziston.
Shpjegomë dallimin mes th:replace dhe th:insert dhe si e merr layout-i title dhe main nga faqja.
Në fund jepmë draftin e AI-LOG.md.
```

**Gati kur:** `/diçka-që-s'ekziston` tregon 404-ën tuaj me menynë dhe footer-in; nuk ka Whitelabel.

---

## 3. Xhemaledin Beluli: CRUD i rezervimeve

**Branch:** `feature/reservations` · **Fillon:** pas merge të `feature/entities` · **Afati:** e mërkurë 18.11

**Prompti për Claude:**
```text
Lexo CLAUDE.md dhe SPEC.md. Jam Xhemaledin, punoj në branch-in feature/reservations.
Entitetet dhe repository-t ekzistojnë tashmë (mos i ndrysho, përveç nëse më duhet një metodë e re në repository).
Detyra, sipas SPEC.md pjesa 5:
1. ReservationController:
   - GET /app/reservations → reservations/list (të gjitha, sipas checkIn).
   - GET /app/reservations/new dhe POST /app/reservations/new → reservations/form (krijimi).
   - GET /app/reservations/{id}/edit dhe POST /app/reservations/{id}/edit → reservations/form (ndryshimi).
   - POST /admin/reservations/{id}/delete → fshin, flash "Rezervimi u fshi", redirect te lista.
   - @ModelAttribute("rooms") për <select>-in. createdBy: emri nga Principal, ose "system" kur Principal është null
     (login-i vjen më vonë). id që s'ekziston → 404.
   - @Valid + BindingResult; me gabime kthe formën, pa gabime ruaj + flash + redirect.
2. reservations/list.html: tabelë me mysafirin, e-mailin, dhomën (numri, link te /app/rooms/{id}), hyrjen, daljen,
   mysafirët; butonat "Ndrysho" dhe "Fshij" (formë POST). Mesazh kur lista është bosh. Butoni "Rezervim i ri".
3. reservations/form.html: një formë për krijim dhe ndryshim (th:action ndryshon sipas id), <input type="date">
   për datat, <select> për dhomën me "-- zgjidhni --", gabimet pranë çdo fushe, edhe gabimi checkOutAfterCheckIn
   pranë datës së daljes.
Të gjitha faqet me layout-in dhe header('reservations'). Shpjegomë rrjedhën GET → POST → redirect dhe pse
BindingResult vjen menjëherë pas @Valid. Në fund jepmë draftin e AI-LOG.md.
```

**Gati kur:** krijon, ndryshon dhe fshin rezervime nga faqja; forma bosh tregon gabimet në shqip pranë fushave; data e daljes para hyrjes tregon gabim; pas ndryshimit `createdAt` mbetet i njëjti; F5 pas ruajtjes nuk e ruan dy herë.

---

## 4. Dion Kastrati: CRUD i dhomave, pastaj login-i

### 4a. Dhomat

**Branch:** `feature/rooms` · **Fillon:** pas merge të `feature/entities` · **Afati:** e mërkurë 18.11

**Prompti për Claude:**
```text
Lexo CLAUDE.md dhe SPEC.md. Jam Dion, punoj në branch-in feature/rooms.
Entitetet dhe repository-t ekzistojnë tashmë.
Detyra, sipas SPEC.md pjesa 5:
1. RoomController:
   - GET /app/rooms → rooms/list (sipas numrit).
   - GET /app/rooms/{id} → rooms/detail: dhoma dhe rezervimet e saj (me query-n "left join fetch").
   - GET/POST /admin/rooms/new dhe /admin/rooms/{id}/edit → rooms/form.
   - POST /admin/rooms/{id}/delete: nëse existsByRoomId → flash error "Dhoma ka rezervime dhe nuk mund të fshihet.",
     përndryshe fshin + flash sukses. Redirect te lista.
   - Numri i dhomës unik: para ruajtjes kontrollo existsByNumber (krijimi) / existsByNumberAndIdNot (ndryshimi);
     nëse ekziston, result.rejectValue("number", ...) me "Ky numër dhome ekziston." dhe kthe formën.
   - id që s'ekziston → 404.
2. rooms/list.html: tabelë me numrin, tipin, katin, kapacitetin, çmimin për natë (2 decimale, €); link te faqja e dhomës;
   "Ndrysho" dhe "Fshij" (formë POST); "Dhomë e re".
3. rooms/form.html: krijim dhe ndryshim; <select> për tipin nga RoomType.values(); gabimet pranë fushave.
4. rooms/detail.html: të dhënat e dhomës dhe tabela e rezervimeve të saj (ose "Nuk ka rezervime").
Të gjitha faqet me layout-in dhe header('rooms'). Shpjegomë pse përdorim "left join fetch" te faqja e dhomës.
Në fund jepmë draftin e AI-LOG.md.
```

**Gati kur:** CRUD i dhomave punon; numri i dyfishtë tregon gabim pranë fushës (jo 500); fshirja e dhomës me rezervime tregon mesazhin; faqja e dhomës tregon rezervimet.

### 4b. Login dhe logout

**Branch:** `feature/login` (nga `main` i përditësuar) · **Afati:** e premte 20.11 (ligjërata e Security: e enjte 19.11)

**Para se të fillojë Dioni (vendosni në grup deri të mërkurën 18.11):**

1. **Butonat e ADMIN-it fshihen për USER-in.** Pas login-it, USER-i i sheh ende "Fshij" (rezervimet dhe dhomat), "Dhomë e re" dhe "Ndrysho" te dhomat, dhe merr 403 kur i klikon. Propozimi: Dioni shton te `pom.xml` `org.thymeleaf.extras:thymeleaf-extras-springsecurity6` (pa `<version>`, e menaxhon Spring Boot 4.1) dhe i mbështjell butonat me `sec:authorize="hasRole('ADMIN')"`. Kjo është librari jashtë listës te CLAUDE.md, prandaj duhet OK-ja e grupit; pas OK-së përditësoni edhe listën e starter-ave te CLAUDE.md. Dioni e ndryshon `reservations/list.html` në PR-in e vet: Xhemaledini (pronari) është edhe rishikuesi i tij, pra e aprovon aty.
2. **Faqja 403 në fazën 1.** `access-denied.html` vjen në fazën 2; deri atëherë USER-i që hap `/admin/...` sheh faqen Whitelabel të Spring-ut. Muzlindi shton `templates/error/403.html` (si `error/404.html`: "Nuk keni qasje në këtë faqe" + link te `/app/reservations`), mundësisht te PR-i i layout-it.
3. **Fjalëkalimet në properties.** Te `application-dev.properties` (file i Uvejs-it: Dioni i shton në PR-in e vet dhe e shënon në përshkrim):
   ```properties
   app.user.password=${APP_USER_PASSWORD:user}
   app.admin.password=${APP_ADMIN_PASSWORD:admin}
   ```
4. **`createdBy`.** Pas login-it, rezervimet e reja ruajnë username-in e të kyçurit në vend të "system". `ReservationController` s'ka nevojë për ndryshim (e lexon tashmë nga `Principal`), por Dioni e provon në psql.

**Prompti për Claude:**
```text
Lexo CLAUDE.md dhe SPEC.md. Jam Dion, punoj në branch-in feature/login.
Detyra (faza 1: përdorues in-memory):
1. Shto spring-boot-starter-security te pom.xml.
2. config/SecurityConfig:
   - PasswordEncoder BCrypt; dy përdorues in-memory: user (ROLE_USER) dhe admin (ROLE_ADMIN).
     Fjalëkalimet nga application-dev.properties: app.user.password=${APP_USER_PASSWORD:user}
     dhe app.admin.password=${APP_ADMIN_PASSWORD:admin}, të lexuara me @Value, jo të shkruara në Java.
   - /login, /css/**, /error/** të lejuara për të gjithë; /admin/** vetëm ADMIN; /app/** USER dhe ADMIN;
     çdo gjë tjetër kërkon login.
   - formLogin me faqen tonë /login, pas login-it te /app/reservations; logout me POST te /logout,
     pastaj /login?logout.
3. AuthController: GET /login → login.html.
4. login.html me layout-in (pa menynë e aplikacionit, ose me header pa linqe kur s'je i kyçur): username, password,
   mesazhi "Username ose fjalëkalimi i gabuar" kur ka ?error, "U çkyçe me sukses" kur ka ?logout.
   Forma me th:action="@{/login}" që të ketë CSRF.
5. Kontrollo që format POST të të tjerëve (fshirja, ruajtja, logout) kanë th:action, ndryshe dalin 403.
6. Nëse grupi e ka pranuar thymeleaf-extras-springsecurity6 (shih "Para se të fillojë Dioni", pika 1):
   shtoje te pom.xml pa <version> dhe fshihi për USER-in me sec:authorize="hasRole('ADMIN')":
   "Fshij" te reservations/list.html; "Dhomë e re", "Ndrysho" dhe "Fshij" te rooms/list.html;
   "Ndrysho" te rooms/detail.html. Mos ndrysho asgjë tjetër në ato file.
Shpjegomë rrjedhën e login-it, pse duhet CSRF te format dhe si e di sec:authorize rolin e përdoruesit.
Në fund jepmë draftin e AI-LOG.md.
```

**Gati kur:** pa login çdo faqe të çon te `/login` jote (jo faqja e Spring-ut); `user` e sheh `/app/**` por jo `/admin/**`, dhe nuk i sheh butonat e ADMIN-it; `user` që hap `/admin/rooms/new` me dorë sheh faqen 403 të Muzlindit (jo Whitelabel); `admin` i sheh të gjitha; një rezervim i ri nga `user` ka `created_by = 'user'` në psql; "Dil" të kthen te login-i me mesazh.

---

## Gjendja më 18.11 dhe çka mbetet

Në `main` janë: entitetet (#3), rezervimet (#4), dhomat (#5, #12), layout-i me 404 dhe 403 (#7, #10), login-i (#11) dhe 17 hyrje te AI-LOG.md. Nuk ka PR të hapur.

**Hapur:** `sec:authorize` (pika 4b, "Para se të fillojë Dioni", pika 1) ende s'është vendosur. Vendoseni në grup **sot, e mërkurë 18.11**.

### Uvejs (lideri)
- [ ] Sot: mbledh vendimin e grupit për `sec:authorize` dhe shkruaje në grup.
- [ ] `git checkout main && git pull`; mos punoni më mbi `feature/entities`.
- [ ] Pasi ta aprovojë Xhemaledini, bëje merge PR-in e Dionit për hapin 6 (nëse pranohet).
- [ ] E diel 22.11, para 20:00: dorëzimi.
- [ ] Mbrojtja: entitetet (`@ManyToOne`/`@OneToMany`, pse `mappedBy` është te Room, `@AssertTrue`, `@PrePersist`, `updatable = false`), SeedData, dhe `ReservationController` i Xhemaledinit (gabimi me `id=3` dhe pse e rregullon `setId(null)`).

### Muzlind
- [ ] Nëse pranohet `sec:authorize`: shtoje `thymeleaf-extras-springsecurity6` te lista e starter-ave në CLAUDE.md.
- [ ] Deri të shtunën: SPEC.md përputhet me kodin në `main` (fushat, URL-të, qasja, faqja 403, trim-i i numrit të dhomës, përdoruesit `user`/`admin`).
- [ ] AI-LOG.md: secili ka hyrjet e veta, pa shenja konflikti.
- [ ] Para demos: hape faqen një herë me internet, që Google Fonts të ruhen në cache.
- [ ] Mbrojtja: `layout.html` (si i merr `title` dhe `main` nga faqja, `th:replace` vs `th:insert`, `#execInfo.processedTemplateName` për linkun aktiv) dhe PR-të e Uvejsit.

### Xhemaledin
- [ ] Nëse pranohet `sec:authorize`: rishiko PR-in e Dionit për hapin 6; te `reservations/list.html` (file yt) duhet të ndryshojë vetëm butoni "Fshij".
- [ ] Opsionale: te hyrja jote e AI-LOG, "5 komente" → "4 komente".
- [ ] Mbrojtja: `ReservationController` (GET → POST → redirect, pse `BindingResult` vjen menjëherë pas `@Valid`, pse `setId(null)` te `create()`, pse `createdAt` s'ndryshon te ndryshimi) dhe `RoomController` i Dionit.

### Dion
- [ ] Nëse pranohet `sec:authorize`, deri të premten 20.11: PR i vogël nga `main` me hapin 6 të pikës 4b. Para PR-it `git pull origin main`, që të mos ketë konflikt te AI-LOG.md.
- [ ] Shiko dizajnin e ri të Muzlindit (#7, #10): hyri në `main` pa review-in tënd.
- [ ] E shtunë: provoje `main`-in me PostgreSQL në Docker (deri tani e ke provuar me H2).
- [ ] Mbrojtja: `SecurityConfig` (rrjedha e login-it, BCrypt, pse duhet CSRF, `/admin/**` dhe `/app/**`), `RoomController` (`left join fetch`, `existsByNumber`, `trim()`) dhe layout-i i Muzlindit.

Në mbrojtje secili e shpjegon kodin e vet dhe kodin e atij që e ka rishikuar (tabela "Rishikimi i PR-ve" më sipër).

## 5. Të gjithë: e shtunë 21.11

- [ ] Klon i pastër i `main` në një laptop: `docker start hotel-db`, `./mvnw spring-boot:run`, gjithë rrjedha punon (login → dhoma → rezervim → ndryshim → fshirje → logout).
- [ ] SPEC.md i njëjtë me kodin: emrat e fushave, URL-të, qasja. Ndryshimet e javës janë futur.
- [ ] AI-LOG.md: secili ka hyrjet e veta (data, për çka, kush + katër rreshtat).
- [ ] Commit-et: secili ka commit-e me llogarinë e vet në historinë e `main`.
- [ ] Asnjë fjalëkalim i vërtetë ose çelës API në repo.
- [ ] Secili e provon që e shpjegon pjesën e vet pa ndihmë: hapni file-t dhe tregojani njëri-tjetrit.

## 6. Demo, e hënë 23.11

Secili flet për pjesën e vet (rreth 1 minutë): Uvejs spec-in, entitetet dhe tabelat në PostgreSQL; Muzlind layout-in, 404-ën dhe 403-ën; Xhemaledin rezervimet me validimin; Dion dhomat dhe login-in.

Për Uvejsin, në psql (`docker exec -it hotel-db psql -U hotel`): `select * from room;`, `\d reservation` (kolona `room_id` me foreign key) dhe `select guest_name, created_by from reservation;`.

## Nëse Claude-t ndeshen

- Konflikt në `fragments.html` ose `app.css`: e zgjidh Muzlind. Te entitetet ose `pom.xml`: Uvejs.
- Dy Claude shpikin emra të ndryshëm: fiton SPEC.md. Nëse spec-i është i paqartë, vendosni në grup dhe përditësoni spec-in.
- Claude propozon React, JWT, `javax`, `@Data` mbi entitet, ose Spring Boot 3: refuzojeni, është kundër kërkesave.
