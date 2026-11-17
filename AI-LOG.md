# AI-LOG: ueb-hotel

### 2026-11-14, Skeleti, entitetet Room dhe Reservation, Uvejs

- **Pyetja:** sipas PLAN.md pika 1: konfigurimi i profilit dev, entitetet Room/RoomType/Reservation me validim dhe relacion, repository-t me metodat e planit, SeedData me 6 dhoma dhe 4 rezervime, HomeController për "/".
- **Përgjigjja:** Claude krijoi skeletin (pom.xml me Boot 4.1.1, Java 25, Lombok, mvnw), application*.properties, entitetet me mesazhe në shqip, @ManyToOne/@OneToMany(mappedBy = "room"), RoomRepository me "left join fetch", ReservationRepository, SeedData dhe HomeController. Shtoi edhe @Enumerated(STRING), @Column(precision = 7, scale = 2) dhe @OrderBy("checkIn ASC").
- **Çka bëmë:** i pranuam të gjitha; service/ dhe DTO-të i lamë për fazën 2. Skeletin e bëri Claude me të njëjtat zgjedhje si start.spring.io, sepse start.spring.io nuk hapej nga sesioni i tij. Në laptopin tim porti 8080 është i zënë nga shërbimi Cafe Manager, prandaj lokalisht përdor 8081 me config/application.properties, që nuk shkon në Git.
- **Si e verifikuam:** ./mvnw spring-boot:run niset pa gabime (Started HotelApplication); psql "select * from room" tregon 6 dhoma; tabela reservation ka 4 rezervime dhe kolonën room_id me foreign key te room(id); "/" bën redirect te /app/reservations.

### 2026-11-17, Layout, fragmentet, CSS dhe faqja 404, Muzlind

- **Pyetja:** sipas PLAN.md pika 2: layout.html me fragmentin layout(title, content), fragments.html me header(active), flash dhe footer, app.css pa framework me klasat e përbashkëta, dhe error/404.html.
- **Përgjigjja:** Claude krijoi të katër file-t. Layout-i e merr `<title>` dhe `<main>` nga faqja me th:replace, dhe e gjen vetë linkun aktiv të menysë nga emri i template-it (`#execInfo.processedTemplateName`, p.sh. "rooms/list"), sepse faqet e Xhemaledinit dhe të Dionit e thërrasin layout-in vetëm me dy parametra. Për faqen "login" menyja dhe butoni "Dil" fshihen. Fragmenti flash nuk shfaqet në faqet e gabimit, sepse aty Spring vendos vetë atributin "error" ("Not Found").
- **Çka bëmë:** provuam së pari ta lexonim adresën e faqes me `springRequestContext.requestUri`, por Thymeleaf 3.1 e ndalon ("Accessing member 'requestUri' is forbidden"), prandaj kaluam te emri i template-it. Komentin në layout e bëmë koment Thymeleaf (`<!--/* */-->`) që të mos i dërgohet shfletuesit. Nuk krijuam controller.
- **Si e verifikuam:** në një kopje lokale me feature/rooms dhe feature/reservations të bashkuara, ./mvnw spring-boot:run (porti 8081): /app/rooms, /app/rooms/1, /app/reservations, format new/edit kthejnë 200 me menynë, linkun aktiv të duhur dhe footer-in; /nuk-ekziston dhe /app/rooms/999 tregojnë 404-ën tonë (jo Whitelabel); fshirja e dhomës me rezervime tregon flash-in e kuq; forma bosh e rezervimit tregon gabimet në shqip me stilin .error.
