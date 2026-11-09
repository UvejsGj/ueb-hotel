package com.ueb.hotel.config;

import com.ueb.hotel.model.Room;
import com.ueb.hotel.model.RoomType;
import com.ueb.hotel.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Shton disa dhoma fillestare kur tabela e dhomave është bosh. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final RoomRepository rooms;

    public DataSeeder(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @Override
    public void run(String... args) {
        if (rooms.count() > 0) {
            return;
        }
        rooms.save(room("101", RoomType.SINGLE, 1, "35.00"));
        rooms.save(room("102", RoomType.DOUBLE, 2, "50.00"));
        rooms.save(room("201", RoomType.DOUBLE, 3, "60.00"));
        rooms.save(room("301", RoomType.SUITE, 4, "120.00"));
    }

    private static Room room(String number, RoomType type, int capacity, String price) {
        Room r = new Room();
        r.setNumber(number);
        r.setType(type);
        r.setCapacity(capacity);
        r.setPricePerNight(new BigDecimal(price));
        return r;
    }
}
