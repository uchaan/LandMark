package com.example.landmark;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


//How to use =======================================================================
//        sendRequest task = new sendRequest();
////        try {
////                ArrayList<RequestItem> result = task.execute(item).get();
////
////        for (int i = 0; i < result.size(); i++){
////        MarkerOptions temp = new MarkerOptions();
////        temp.position(new LatLng(result.get(i).lat, result.get(i).lng));
////        temp.title(result.get(i).name);
////        temp.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin));
////        restaurant_map.addMarker(temp);
////        }
////        } catch (InterruptedException e) {
////        e.printStackTrace();
////        } catch (ExecutionException e) {
////        e.printStackTrace();
////        }
//=====================================================================================

public class sendRequest extends AsyncTask<RequestItem, Void, ArrayList<RequestItem>>{
    static String type;
    static double lat;
    static double lng;
    static int radius;

    public sendRequest(){}

    @Override
    protected ArrayList<RequestItem> doInBackground(RequestItem... requestItems) {

        RequestItem item = requestItems[0];
        this.type = item.type;
        this.lat = item.lat;
        this.lng = item.lng;
        this.radius = item.radius;

        String LOG_TAG = "ExampleApp";
        String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        String TYPE_SEARCH = "/search";
        String OUT_JSON = "/json";
        String API_KEY = "AIzaSyCbczuPt2sl8N5DOQqCKPvynvN9n55rGak";

        //Boolean resultList = false;
        ArrayList<RequestItem> resultList = new ArrayList<>();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            // requestItem 정보 이용해서 요청할 url 생성 .
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_SEARCH);
            sb.append(OUT_JSON);
            sb.append("?sensor=false");
            sb.append("&key=" + API_KEY);
//            sb.append("&keyword=" + URLEncoder.encode(keyword, "utf8"));
            sb.append("&location=" + String.valueOf(lat) + "," + String.valueOf(lng));
            sb.append("&radius=" + String.valueOf(radius));
            sb.append("&type=" + String.valueOf(type));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            //return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            //return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            for (int i = 0; i < predsJsonArray.length(); i++) {

                String name = predsJsonArray.getJSONObject(i).getString("name");
                String id = predsJsonArray.getJSONObject(i).getString("place_id");

                String json_geometry = predsJsonArray.getJSONObject(i).getString("geometry");
                JSONObject geometry = new JSONObject(json_geometry);
                String json_location = geometry.getString("location");
                JSONObject location = new JSONObject(json_location);
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                RequestItem temp = new RequestItem().setItem(name, id, lat, lng);
                resultList.add(temp);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }

        return resultList;
    }
}