package com.example.qrhunter;


public class location {

    private double altitude;
    private double latitude;
    private double score;

    public location(double a, double l, double s){
        altitude = a;
        latitude = l;
        score = s;
    }

    public double getAltitude(){
        return altitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getScore(){
        return score;
    }
}

