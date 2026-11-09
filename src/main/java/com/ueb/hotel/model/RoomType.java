package com.ueb.hotel.model;

public enum RoomType {
    SINGLE("Teke"),
    DOUBLE("Dyshe"),
    SUITE("Suitë");

    private final String label;

    RoomType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
