package com.example.myapplication.service;

public class KalmanFilterService {

    private final int size;
    private float total = 0f;
    private int index = 0;
    private final double[] samples;

    public KalmanFilterService(int size) {
        this.size = size;
        samples = new double[size];
        for (int i = 0; i < size; i++) samples[i] = 0d;
    }

    public void add(double x) {
        total -= samples[index];
        samples[index] = x;
        total += x;
        if (++index == size) index = 0; // cheaper than modulus
    }

    public void fillWithZero() {
        for (int i = 0; i < size; i++) samples[i] = 0d;
        index = 0;
    }

    public float getAverage() {
        return total / size;
    }
}
