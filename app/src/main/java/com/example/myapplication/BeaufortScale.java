package com.example.myapplication;

public enum BeaufortScale {
    CALM("Calm"), LIGHT_AIR("Light air"),
    LIGHT_BREEZE("Light breeze"), GENTLE_BREEZE("Gentle breeze"),
    MODERATE_BREEZE("Moderate breeze"), FRESH_BREEZE("Fresh breeze"),
    STRONG_BREEZE("Strong breeze"),HIGH_WIND("High wind"),
    GALE("Gale"),STRONG_GALE("Strong gale");

    private final String val;

    BeaufortScale(String s) {
        val = s;
    }

    public String getValue() {
        return val;
    }
}
