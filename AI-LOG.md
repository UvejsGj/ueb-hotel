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
