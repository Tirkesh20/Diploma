package com.example.myapplication.dto;


import java.io.Serializable;
import java.time.LocalDateTime;

public class BaseDto implements Serializable {

    private AnemometrData anemometrData;
    private BoatData boatData;
    private MagnitData magnitData;
    private LocalDateTime timeUpdate;

    public BaseDto(LocalDateTime timeUpdate) {
        this.anemometrData = new AnemometrData();
        this.boatData = new BoatData();
        this.magnitData = new MagnitData();
        this.timeUpdate = timeUpdate;
    }

    public BaseDto() {
        this.timeUpdate = LocalDateTime.now();
    }

    public AnemometrData getAnemometrData() {
        return anemometrData;
    }

    public void setAnemometrData(AnemometrData anemometrData) {
        this.anemometrData = anemometrData;
    }

    public BoatData getBoatData() {
        return boatData;
    }

    public void setBoatData(BoatData boatData) {
        this.boatData = boatData;
    }

    public MagnitData getMagnitData() {
        return magnitData;
    }

    public void setMagnitData(MagnitData magnitData) {
        this.magnitData = magnitData;
    }

    public LocalDateTime getTimeUpdate() {
        return timeUpdate;
    }

    public void setTimeUpdate(LocalDateTime timeUpdate) {
        this.timeUpdate = timeUpdate;
    }
}
