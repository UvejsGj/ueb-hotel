# AI-LOG: ueb-hotel

### 2026-11-14, Skeleti, entitetet Room dhe Reservation, Uvejs

- **Pyetja:** sipas PLAN.md pika 1: konfigurimi i profilit dev, entitetet Room/RoomType/Reservation me validim dhe relacion, repository-t me metodat e planit, SeedData me 6 dhoma dhe 4 rezervime, HomeController për "/".
- **Përgjigjja:** Claude krijoi skeletin (pom.xml me Boot 4.1.1, Java 25, Lombok, mvnw), application*.properties, entitetet me mesazhe në shqip, @ManyToOne/@OneToMany(mappedBy = "room"), RoomRepository me "left join fetch", ReservationRepository, SeedData dhe HomeController. Shtoi edhe @Enumerated(STRING), @Column(precision = 7, scale = 2) dhe @OrderBy("checkIn ASC").
- **Çka bëmë:** i pranuam të gjitha; service/ dhe DTO-të i lamë për fazën 2. Skeletin e bëri Claude me të njëjtat zgjedhje si start.spring.io, sepse start.spring.io nuk hapej nga sesioni i tij. Në laptopin tim porti 8080 është i zënë nga shërbimi Cafe Manager, prandaj lokalisht përdor 8081 me config/application.properties, që nuk shkon në Git.
- **Si e verifikuam:** ./mvnw spring-boot:run niset pa gabime (Started HotelApplication); psql "select * from room" tregon 6 dhoma; tabela reservation ka 4 rezervime dhe kolonën room_id me foreign key te room(id); "/" bën redirect te /app/reservations.
