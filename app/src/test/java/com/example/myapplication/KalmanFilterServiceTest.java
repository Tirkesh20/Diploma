package com.example.myapplication;

import com.example.myapplication.service.KalmanFilterService;

import junit.framework.TestCase;

public class KalmanFilterServiceTest extends TestCase {
    private final static int SIZE = 5;
    private static final float FULL_SUM = 12.5f;

    private KalmanFilterService kalman;

    public void setUp() {
        kalman = new KalmanFilterService(SIZE);
    }

    public void testInitial() {
        assertEquals(0f, kalman.getAverage());
    }

    public void testOne() {
        kalman.add(3.5f);
        assertEquals(3.5f / SIZE, kalman.getAverage());
    }

    public void testFillBuffer() {
        fillBufferAndTest();
    }

    public void testForceOverWrite() {
        fillBufferAndTest();

        double newVal = SIZE + .5f;
        kalman.add(newVal);
        assertEquals((FULL_SUM + newVal - .5f) / SIZE, kalman.getAverage());
    }

    public void testManyValues() {
        for (int i = 0; i < 1003; i++) kalman.add((float) i);
        fillBufferAndTest();
    }


    private void fillBufferAndTest() {
        for (int i = 0; i < SIZE; i++) kalman.add(i + .5d);
        assertEquals(FULL_SUM / SIZE, kalman.getAverage());
    }
}
