package com.example.landmark;


//How to use ======================================================
// RequestItem item = new RequestItem("restaurant", lat, lon, 500);
// RequestItem temp = new RequestItem().setItem(name, id, lat, lng);
//=================================================================

public class RequestItem {
    public String name;
    public String type;
    public double lat;
    public double lng;
    public int radius;
    public String id;
    public double rating;
    public String address;


    public RequestItem(){}

    // 어떤 지점(lat, lng) 근처(radius)의 특정 장소(type) 요청할때 아이템
    // sendRequest()에 정보전달할때 쓰임
    public RequestItem(String type, double lat, double lng, int radius) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public RequestItem requestDetailsItem(String id){
        RequestItem result = new RequestItem();
        result.id = id;

        return result;
    }

    public RequestItem setItem(String name, String id, double lat, double lng){
        RequestItem result = new RequestItem();
        result.name = name;
        result.id = id;
        result.lat = lat;
        result.lng = lng;

        return result;
    }

    public RequestItem setDetailsItem(double rating, String address){
        RequestItem result = new RequestItem();
        result.rating = rating;
        result.address = address;

        return result;
    }


}
