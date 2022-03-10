package com.example.qrhunter;


public class location {

    private double altitude;
    private double latitude;

    public location(double a, double l){
        altitude = a;
        latitude = l;
    }

    public double getAltitude(){
        return altitude;
    }

    public double getLatitude(){
        return latitude;
    }
}

