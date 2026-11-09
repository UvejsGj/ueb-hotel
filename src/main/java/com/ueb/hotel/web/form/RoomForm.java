package com.ueb.hotel.web.form;

import com.ueb.hotel.model.Room;
import com.ueb.hotel.model.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class RoomForm {

    @NotBlank(message = "Numri i dhomës është i detyrueshëm.")
    @Size(max = 10, message = "Numri mund të ketë më së shumti 10 shenja.")
    private String number;

    @NotNull(message = "Zgjidhni tipin e dhomës.")
    private RoomType type;

    @NotNull(message = "Shkruani kapacitetin.")
    @Min(value = 1, message = "Kapaciteti duhet të jetë së paku 1.")
    @Max(value = 6, message = "Kapaciteti mund të jetë më së shumti 6.")
    private Integer capacity;

    @NotNull(message = "Shkruani çmimin për natë.")
    @DecimalMin(value = "1.00", message = "Çmimi duhet të jetë së paku 1.00 €.")
    @Digits(integer = 5, fraction = 2, message = "Çmimi duhet të ketë më së shumti 2 decimale.")
    private BigDecimal pricePerNight;

    public static RoomForm from(Room r) {
        RoomForm f = new RoomForm();
        f.number = r.getNumber();
        f.type = r.getType();
        f.capacity = r.getCapacity();
        f.pricePerNight = r.getPricePerNight();
        return f;
    }

    public void applyTo(Room r) {
        r.setNumber(number.trim());
        r.setType(type);
        r.setCapacity(capacity);
        r.setPricePerNight(pricePerNight);
    }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
}
