package com.example.landmark;


//How to use ======================================================
// RequestItem item = new RequestItem("restaurant", lat, lon, 500);
//=================================================================

public class RequestItem {
    public String name;
    public String type;
    public double lat;
    public double lng;
    public int radius;
    public String id;


    public RequestItem(){}

    public RequestItem(String type, double lat, double lng, int radius) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public RequestItem setItem(String name, String id, double lat, double lng){
        RequestItem result = new RequestItem();
        this.name = name;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }

}
