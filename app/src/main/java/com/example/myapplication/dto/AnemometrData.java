package com.example.myapplication.dto;

import java.io.Serializable;

public class AnemometrData implements Serializable {

    private float speed;

    public AnemometrData() {

    }

    public AnemometrData(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
