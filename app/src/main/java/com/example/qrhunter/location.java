package com.example.qrhunter;


public class location {

    private double longitude;
    private double latitude;
    private String score;

    public location(double a, double l, String s){
        longitude = a;
        latitude = l;
        score = s;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public String getScore(){
        return score;
    }
}

