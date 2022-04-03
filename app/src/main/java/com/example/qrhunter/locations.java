package com.example.qrhunter;

/**
 * Class used to store location information of QR
 */
public class locations {

    private double longitude;
    private double latitude;
    private String score;

    /**
     * Constructor for the location class
     * @param a longitude of Item
     * @param l latitude of Item
     * @param s score of Item
     */
    public locations(double a, double l, String s){
        longitude = a;
        latitude = l;
        score = s;
    }

    /**
     * Function to return the objects Longitude
     * @return double representing Longitude coordinate
     */
    public double getLongitude(){
        return longitude;
    }

    /**
     * Function to return the objects Latitude
     * @return double representing Latitude coordinate
     */
    public double getLatitude(){
        return latitude;
    }

    /**
     * Function to return the score of the object
     * @return String representing score
     */
    public String getScore(){
        return score;
    }
}