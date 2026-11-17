# AI-LOG: ueb-hotel

### 2026-11-14, Skeleti, entitetet Room dhe Reservation, Uvejs

- **Pyetja:** sipas PLAN.md pika 1: konfigurimi i profilit dev, entitetet Room/RoomType/Reservation me validim dhe relacion, repository-t me metodat e planit, SeedData me 6 dhoma dhe 4 rezervime, HomeController për "/".
- **Përgjigjja:** Claude krijoi skeletin (pom.xml me Boot 4.1.1, Java 25, Lombok, mvnw), application*.properties, entitetet me mesazhe në shqip, @ManyToOne/@OneToMany(mappedBy = "room"), RoomRepository me "left join fetch", ReservationRepository, SeedData dhe HomeController. Shtoi edhe @Enumerated(STRING), @Column(precision = 7, scale = 2) dhe @OrderBy("checkIn ASC").
- **Çka bëmë:** i pranuam të gjitha; service/ dhe DTO-të i lamë për fazën 2. Skeletin e bëri Claude me të njëjtat zgjedhje si start.spring.io, sepse start.spring.io nuk hapej nga sesioni i tij. Në laptopin tim porti 8080 është i zënë nga shërbimi Cafe Manager, prandaj lokalisht përdor 8081 me config/application.properties, që nuk shkon në Git.
- **Si e verifikuam:** ./mvnw spring-boot:run niset pa gabime (Started HotelApplication); psql "select * from room" tregon 6 dhoma; tabela reservation ka 4 rezervime dhe kolonën room_id me foreign key te room(id); "/" bën redirect te /app/reservations.

### 2026-11-17, CRUD i rezervimeve, Xhemaledin

- **Pyetja:** sipas PLAN.md pika 3: ReservationController (lista, krijimi, ndryshimi, fshirja me POST te /admin), reservations/list.html dhe reservations/form.html me <select> të dhomave, validimin dhe gabimet pranë fushave.
- **Përgjigjja:** Claude shkroi ReservationController me @ModelAttribute("rooms"), @Valid + BindingResult, PRG me flash "Rezervimi u ruajt / u ndryshua / u fshi", createdBy nga Principal ose "system", 404 me ResponseStatusException. Lista tregon datat si dd.MM.yyyy dhe numrin e dhomës si link te /app/rooms/{id}; forma është e njëjta për krijim dhe ndryshim (th:action sipas id).
- **Çka bëmë:** i pranuam. Te <select>-i i dhomës përdorëm th:selected (krahasim i id-së) në vend të th:field, që dhoma e zgjedhur të shfaqet sigurt te ndryshimi. Te ndryshimi id merret nga URL-ja (reservation.setId(id)), jo nga forma. Nuk shtuam metoda të reja në repository.
- **Si e verifikuam:** ./mvnw spring-boot:run niset pa gabime; me layout të përkohshëm lokal (layout-i i Muzlindit s'ishte ende në main): lista tregon 4 rezervimet e SeedData; forma bosh tregon 6 gabimet në shqip; dalja para hyrjes tregon "Data e daljes duhet të jetë pas datës së hyrjes"; krijimi, ndryshimi dhe fshirja punojnë me mesazh flash; në psql created_at mbetet i njëjti pas ndryshimit; /app/reservations/999/edit kthen 404; GET te fshirja kthen 405.

### 2026-11-17, CRUD i dhomave dhe faqja e dhomës, Dion

- **Pyetja:** sipas PLAN.md pika 4a: RoomController (lista, faqja e dhomës me rezervimet, krijimi, ndryshimi, fshirja), kontrolli i numrit unik dhe i fshirjes së dhomës me rezervime, template-t rooms/list, form dhe detail.
- **Përgjigjja:** Claude krijoi RoomController me @RequiredArgsConstructor, @ModelAttribute("types") me RoomType.values() për <select>-in, existsByNumber / existsByNumberAndIdNot me result.rejectValue("number", ...), existsByRoomId para fshirjes, findByIdWithReservations ("left join fetch") për faqen e dhomës dhe 404 me ResponseStatusException. Template-t ndjekin stilin e reservations/* (layout, th:object, th:errors, fshirja me formë POST). Çmimi shfaqet me 2 decimale dhe €, p.sh. "45,00 €".
- **Çka bëmë:** i pranuam. Llojet e dhomës i dërgon controller-i (@ModelAttribute("types")) e jo T(...) në template, sepse Thymeleaf 3.1 nuk lejon thirrje statike në shprehje. Te krijimi room.setId(null), që një "id" i futur në formë të mos e mbishkruajë dhomën ekzistuese. Fshehja e butonave për USER pret login-in (feature/login).
- **Si e verifikuam:** ./mvnw compile pa gabime. Docker nuk punonte në laptop, prandaj aplikacionin e nisëm njëherë me H2 në memorie dhe me një layout provë (asnjëra s'është në Git) dhe i provuam me curl: lista me 6 dhoma; /app/rooms/1 tregon rezervimin e Arben Hoxhës, /app/rooms/3 "Nuk ka rezervime"; id 999 → 404; forma bosh dhe vlerat jashtë kufijve tregojnë gabimet në shqip; numri 101 i dyfishtë (krijim dhe ndryshim) → "Ky numër dhome ekziston."; krijimi, ndryshimi dhe fshirja punojnë me flash; fshirja e dhomës 101 → "Dhoma ka rezervime dhe nuk mund të fshihet."; GET te /delete → 405. Mbetet: provë me PostgreSQL në Docker dhe me layout-in e Muzlindit para PR-it.

### 2026-11-17, Layout, fragmentet, CSS dhe faqja 404, Muzlind

- **Pyetja:** sipas PLAN.md pika 2: layout.html me fragmentin layout(title, content), fragments.html me header(active), flash dhe footer, app.css pa framework me klasat e përbashkëta, dhe error/404.html.
- **Përgjigjja:** Claude krijoi të katër file-t. Layout-i e merr `<title>` dhe `<main>` nga faqja me th:replace, dhe e gjen vetë linkun aktiv të menysë nga emri i template-it (`#execInfo.processedTemplateName`, p.sh. "rooms/list"), sepse faqet e Xhemaledinit dhe të Dionit e thërrasin layout-in vetëm me dy parametra. Për faqen "login" menyja dhe butoni "Dil" fshihen. Fragmenti flash nuk shfaqet në faqet e gabimit, sepse aty Spring vendos vetë atributin "error" ("Not Found").
- **Çka bëmë:** provuam së pari ta lexonim adresën e faqes me `springRequestContext.requestUri`, por Thymeleaf 3.1 e ndalon ("Accessing member 'requestUri' is forbidden"), prandaj kaluam te emri i template-it. Komentin në layout e bëmë koment Thymeleaf (`<!--/* */-->`) që të mos i dërgohet shfletuesit. Nuk krijuam controller.
- **Si e verifikuam:** në një kopje lokale me feature/rooms dhe feature/reservations të bashkuara, ./mvnw spring-boot:run (porti 8081): /app/rooms, /app/rooms/1, /app/reservations, format new/edit kthejnë 200 me menynë, linkun aktiv të duhur dhe footer-in; /nuk-ekziston dhe /app/rooms/999 tregojnë 404-ën tonë (jo Whitelabel); fshirja e dhomës me rezervime tregon flash-in e kuq; forma bosh e rezervimit tregon gabimet në shqip me stilin .error.

### 2026-11-17, Faqja 403 (nuk keni qasje), Muzlind

- **Pyetja:** Dioni na tha që i del gabimi 403; hulumtoje dhe rregulloje.
- **Përgjigjja:** Claude gjeti te PR #8 (PLAN.md) që 403 është i saktë kur `user` hap `/admin/...`, por pa faqe tonën del Whitelabel; plani e cakton `templates/error/403.html` te Muzlindi. E krijoi si 404-ën: me layout-in, "Nuk keni qasje në këtë faqe" dhe link te `/app/reservations`.
- **Çka bëmë:** e pranuam. Nuk prekëm SecurityConfig-un dhe as butonat e ADMIN-it, sepse ato janë të Dionit në feature/login.
- **Si e verifikuam:** në një kopje lokale (jashtë Git-it) shtuam përkohësisht spring-boot-starter-security dhe një SecurityConfig si te plani i Dionit. Si `user`: `/app/rooms` 200, `/admin/rooms/new` dhe POST te `/admin/reservations/1/delete` kthejnë 403 me faqen tonë (menyja, footer, pa Whitelabel); "Dil" me token-in CSRF të kthen te `/login?logout`. Si `admin`: `/admin/rooms/new` 200, `/nuk-ekziston` 404.

### 2026-11-17, Dizajni i ri i faqeve, Muzlind

- **Pyetja:** ndryshoje dizajnin: atraktiv dhe profesional, por jo si dizajnet e zakonshme me AI; pak ngjyra, font tjetër, layout tjetër.
- **Përgjigjja:** Claude e ndërtoi dizajnin si "libri i regjistrimit në recepsion": menyja në një shirit majtas (indeks "01 Rezervimet / 02 Dhomat", data e sotme me `#temporals`, "Dil" poshtë), sfond letër krem, bojë e zezë dhe vetëm një ngjyrë (e kuqe e errët) për fshirjen, gabimet dhe linkun aktiv. Tre fonte nga Google Fonts: Libre Caslon Display (titujt), Libre Franklin (teksti), IBM Plex Mono (etiketat dhe numrat). Tabelat si faqe regjistri me vija të holla, fushat e formës vetëm me vijë poshtë, 404/403 me numrin e madh. Në celular shiriti bëhet shirit lart. Ndryshoi vetëm file-t e mia (layout.html, fragments.html, app.css, error/404.html, error/403.html); klasat që përdorin faqet e të tjerëve mbetën të njëjta.
- **Çka bëmë:** e pranuam. Ngjyrat i vendosëm si variabla CSS (`--ink`, `--paper`, `--accent`) që të ndryshohen në një vend. Fontet vijnë nga interneti; pa internet faqja përdor Georgia/Arial/Consolas.
- **Si e verifikuam:** ./mvnw spring-boot:run me main + feature/layout: /app/rooms, /app/rooms/1, /app/reservations, /app/reservations/new (edhe me gabimet e formës bosh) dhe /nuk-ekziston në 1366 px dhe 375 px; fontet ngarkohen (`document.fonts.check`); në celular faqja nuk rrëshqet anash (tabela rrëshqet brenda vetes).

### 2026-11-17, Rishikimi i PR #4 (rezervimet) dhe testi i PR #5 + layout-it, Uvejs

- **Pyetja:** rishikoje PR #4 të Xhemaledinit në vendin tim; pas rregullimit kontrolloje përsëri. Pastaj shpjegoje konfliktin te PR #5 i Dionit dhe kontrollo PR #5 bashkë me layout-in e Muzlindit.
- **Përgjigjja:** Claude e nisi branch-in lokalisht (port 8082, PostgreSQL në Docker, layout i përkohshëm jashtë Git-it) dhe gjeti një gabim: `POST /app/reservations/new` me `id=3` e mbishkruante rezervimin 3, sepse `@ModelAttribute` lidh edhe `id`-në. Propozoi `reservation.setId(null)` te `create()` dhe shkroi review-n "Request changes"; pas rregullimit e provoi përsëri dhe shkroi aprovimin. Konfliktin te PR #5 e shpjegoi: Xhemaledini dhe Dioni e shtuan hyrjen në fund të AI-LOG.md në të njëjtin vend. Pastaj i testoi `main` + layout + dhomat bashkë.
- **Çka bëmë:** i pranuam. Review-të i postuam nga llogaria ime pasi i lexova. Xhemaledini e shtoi `setId(null)`; Dioni e zgjodhi konfliktin duke i mbajtur të dyja hyrjet. Vërejtjet jo bllokuese (renditja e merge-it, butoni "Fshij" për USER-in) i lamë për layout-in dhe login-in.
- **Si e verifikuam:** me curl dhe psql para dhe pas rregullimit: me `id=3` rreshti i "Driton Berisha" u mbishkrua para, por jo pas; forma bosh 6 gabime; `created_at` i pandryshuar pas ndryshimit; 404 dhe 405. Me layout-in: të gjitha faqet 200, menyja aktive saktë, 404 pa Whitelabel; dhomat: numri i dyfishtë, fshirja e dhomës me rezervime, `id=2` i futur s'e mbishkruan dhomën. Të dhënat e testit i kthyem si ishin.

### 2026-11-17, Vendimet për login-in në PLAN.md, Uvejs

- **Pyetja:** a janë te file-t .md të gjitha informatat që i duhen Dionit për login-in; pastaj: shtoji ato që mungojnë te PLAN.md.
- **Përgjigjja:** Claude i krahasoi PLAN.md, SPEC.md dhe CLAUDE.md me kodin dhe gjeti 4 mungesa: fshehja e butonave të ADMIN-it për USER-in, faqja 403 në fazën 1, emrat e fjalëkalimeve në properties dhe kontrolli i `createdBy`. I shtoi te pika 4b, ia përditësoi Dionit promptin dhe "Gati kur".
- **Çka bëmë:** i pranuam, por përdorimin e `thymeleaf-extras-springsecurity6` (`sec:authorize`) e lamë si propozim që e vendos grupi, sepse s'është në listën e starter-ave te CLAUDE.md. Hapëm PR #8.
- **Si e verifikuam:** në pom-in e Spring Boot 4.1.1 kontrolluam që versioni i `thymeleaf-extras-springsecurity6` (3.1.5) menaxhohet vetë; me grep te template-t gjetëm saktë cilët butona shkojnë te `/admin/**`.

### 2026-11-17, Rregullimi pas rishikimit të PR #4, Xhemaledin

- **Pyetja:** Uvejs e rishikoi PR #4 dhe kërkoi ndryshim: krijimi lejonte mbishkrimin e një rezervimi ekzistues kur kërkesa dërgon `id` (p.sh. `id=3` te `POST /app/reservations/new`). I kërkova Claude-it ta rregullojë; pastaj edhe t'i heqë komentet nga ReservationController.
- **Përgjigjja:** Claude shtoi `reservation.setId(null);` te `create()`, para kontrollit `hasErrors()` (jo pas tij), që edhe forma me gabime të mbetet te `/app/reservations/new` e jo te `/{id}/edit`. Pastaj hoqi 5 komentet nga ReservationController.
- **Çka bëmë:** i pranuam të dyja. Vendin e `setId(null)` e ndryshuam nga sugjerimi i Uvejsit (para `hasErrors()`), për arsyen më sipër. Komentet i hoqëm që kodi të jetë më i pastër; shpjegimin e dimë përmendësh për mbrojtjen.
- **Si e verifikuam:** me PostgreSQL dhe layout të përkohshëm: `POST /app/reservations/new` me `id=3` krijoi rreshtin e ri 7 dhe "Driton Berisha" (id 3) mbeti i paprekur në psql; me `id=3` dhe emër bosh forma tregoi gabimin dhe `action="/app/reservations/new"`. Pas heqjes së komenteve `./mvnw compile` kaloi pa gabime. Uvejs e aprovoi PR-in.

### 2026-11-17, Rishikimi i PR #5 (dhomat) të Dionit, Xhemaledin

- **Pyetja:** sipas PLAN.md e rishikoj PR-in e Dionit; i kërkova Claude-it ta rishikojë dhe ta provojë PR #5, pastaj prapë pasi Dion e bëri merge `main`-in në `feature/rooms`.
- **Përgjigjja:** Claude e lexoi diff-in dhe e nisi branch-in në një worktree të veçantë me PostgreSQL në Docker (Dion e kishte provuar vetëm me H2). Nuk gjeti gjë bllokuese; dha 5 sugjerime: `trim()` i numrit të dhomës (" 101 " ruhej si dhomë e re), gabim në anglisht për `type=KING`, renditja e merge-it pas layout-it, konflikti te `AI-LOG.md`, butonat e adminit për USER-in. Në rishikimin e dytë kontrolloi që konflikti te `AI-LOG.md` u zgjidh duke i mbajtur të gjitha hyrjet.
- **Çka bëmë:** e aprovuam PR-in me komente nga llogaria ime dhe pas merge-it të `main`-it shtuam një koment të shkurtër. Sugjerimet i lamë jo bllokuese; `trim()` mbetet për Dionin.
- **Si e verifikuam:** me curl dhe psql: lista, faqja e dhomës me rezervimet sipas `checkIn`, 404, validimi në shqip, numri i dyfishtë te krijimi dhe ndryshimi, fshirja e bllokuar e dhomës me rezervime, 405 për GET te `/delete`, `id=2` te krijimi nuk e mbishkruan dhomën 2. Te kodi i bashkuar: dhoma 501 → rezervim → fshirja bllokohet → pas fshirjes së rezervimit dhoma fshihet. Të dhënat e provës i fshimë; databaza mbeti me 6 dhoma dhe 4 rezervime.

### 2026-11-17, Hapësirat te numri i dhomës, Dion

- **Pyetja:** Xhemaledini te rishikimi i PR #5 vuri re që " 101 " (me hapësira) ruhej si dhomë e re, sepse existsByNumber e krahason numrin me hapësirat; rregulloje te RoomController.
- **Përgjigjja:** Claude shtoi te create() dhe update(), para kontrollit të numrit unik, `if (room.getNumber() != null) { room.setNumber(room.getNumber().trim()); }`, ashtu si e sugjeroi Xhemaledini.
- **Çka bëmë:** e pranuam. Kontrolli për null duhet, sepse pa të `trim()` hedh NullPointerException kur fusha mungon në kërkesë (atë rast e kap @NotBlank). Sugjerimin për `type=KING` (gabim në anglisht) e lamë për fazën 2, sepse nga faqja normale s'mund të ndodhë.
- **Si e verifikuam:** ./mvnw clean compile pa gabime; me H2 dhe curl: " 101 " te krijimi → "Ky numër dhome ekziston."; "  601  " ruhet si "601" dhe pastaj "601 " → "Ky numër dhome ekziston."; ndryshimi i dhomës 101 në " 102 " → gabim, në " 101 " (numri i vet) → ruhet; vetëm hapësira → "Shkruani numrin e dhomës"; lista tregon numrat pa hapësira; forma me gabim e rishfaq numrin e pastruar.
