package com.example.landmark;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class sendDetailsRequest extends AsyncTask<RequestItem, Void, RequestItem>{
    static String id;


    public sendDetailsRequest(){}

    @Override
    protected RequestItem doInBackground(RequestItem... requestItems) {

        RequestItem item = requestItems[0];
        this.id = item.id;
        System.out.println(id+"\n");

        String LOG_TAG = "ExampleApp";
        String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        String TYPE_DETAILS = "/details";
        String OUT_JSON = "/json";
        String API_KEY = "AIzaSyAnKXGh8d-fz8P3u3hv-OIr5Rcg5CNeBgQ";

        //Boolean resultList = false;
        RequestItem result_item = new RequestItem();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_DETAILS);
            sb.append(OUT_JSON);
            sb.append("?sensor=false");
            sb.append("&key=" + API_KEY);
            sb.append("&place_id=" + id);
            sb.append("&fields=name,rating,formatted_phone_number,formatted_address");

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
//                        System.out.println(jsonResults);

        try {
            // Create a JSON object hierarchy from the results
                System.out.println(jsonResults);

            JSONObject jsonObj = new JSONObject(jsonResults.toString()).getJSONObject("result");

//            place = new Place();
//            place.icon = jsonObj.getString("icon");
//            place.name = jsonObj.getString("name");
//            place.formatted_address = jsonObj.getString("formatted_address");
//            if (jsonObj.has("formatted_phone_number")) {
//                place.formatted_phone_number = jsonObj.getString("formatted_phone_number");

            // Extract the Place descriptions from the results
                Double rating = jsonObj.getDouble("rating");
                System.out.println(rating);
                String address = jsonObj.getString("formatted_address");
                System.out.println(address);

//                result_item.setDetailsItem(rating, address);
                result_item = new RequestItem().setDetailsItem(rating, address);

//                System.out.println(result_item.phone+"\n");

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }

        return result_item;
    }
}
