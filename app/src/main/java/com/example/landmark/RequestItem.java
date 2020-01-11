package com.example.landmark;

public class RequestItem {
    public String type;
    public double lat;
    public double lng;
    public int radius;

    public RequestItem(){}

    public RequestItem(String type, double lat, double lng, int radius) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

}
