package com.cacttus.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shkruani numrin e dhomës")
    @Size(max = 10, message = "Numri i dhomës ka më së shumti 10 shenja")
    @Column(unique = true)
    private String number;

    @NotNull(message = "Zgjidhni tipin e dhomës")
    @Enumerated(EnumType.STRING)
    private RoomType type;

    @NotNull(message = "Shkruani katin")
    @Min(value = 0, message = "Kati duhet të jetë së paku 0")
    @Max(value = 20, message = "Kati duhet të jetë më së shumti 20")
    private Integer floor;

    @NotNull(message = "Shkruani kapacitetin")
    @Min(value = 1, message = "Kapaciteti duhet të jetë së paku 1")
    @Max(value = 6, message = "Kapaciteti duhet të jetë më së shumti 6")
    private Integer capacity;

    @NotNull(message = "Shkruani çmimin për natë")
    @Positive(message = "Çmimi duhet të jetë më i madh se 0")
    @Digits(integer = 5, fraction = 2, message = "Çmimi ka më së shumti 5 shifra para presjes dhe 2 pas saj")
    @Column(precision = 7, scale = 2)
    private BigDecimal pricePerNight;

    @OneToMany(mappedBy = "room")
    @OrderBy("checkIn ASC")
    private List<Reservation> reservations = new ArrayList<>();
}
