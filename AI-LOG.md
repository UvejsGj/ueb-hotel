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
