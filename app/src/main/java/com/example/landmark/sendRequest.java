package com.example.landmark;

import android.app.DownloadManager;
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

public class sendRequest extends AsyncTask<RequestItem, Void, Boolean>{
    static String type;
    static double lat;
    static double lng;
    static int radius;

    public sendRequest(){}

    @Override
    protected Boolean doInBackground(RequestItem... requestItems) {

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

        Boolean resultList = false;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
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
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
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
            resultList = true;
            for (int i = 0; i < predsJsonArray.length(); i++) {
//                com.google.android.gms.location.places.Place place = new com.google.android.gms.location.places.Place();
//                place.reference = predsJsonArray.getJSONObject(i).getString("reference");
//                place.name = predsJsonArray.getJSONObject(i).getString("name");
//                resultList.add(place);

                System.out.println(predsJsonArray.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error processing JSON results", e);
        }

        return resultList;
    }
}