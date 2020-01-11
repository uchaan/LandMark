package com.example.landmark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

import static android.speech.tts.TextToSpeech.ERROR;

public class SecondActivity extends AppCompatActivity implements PlacesListener, OnMapReadyCallback {

    String name, confidence;
    double lat, lon;
    TextView NameT, TestT;
    Button button_speak, button_show;
    private TextToSpeech tts;

    List<Marker> previous_marker = null;
    MapView map;
    GoogleMap restaurant_map;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        NameT = (TextView) findViewById(R.id.textView1);
        TestT = (TextView) findViewById(R.id.textView2);
        button_speak = (Button) findViewById(R.id.speak);
        map = (MapView) findViewById(R.id.map_restaurant);

        if (map != null)
        {
            map.onCreate(null);
            map.onResume();
            map.getMapAsync(this);
        }

        previous_marker = new ArrayList<Marker>();

        button_show = (Button)findViewById(R.id.show_restaurant);
        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(new LatLng(lat, lon));
            }
        });

        // 이전 액티비티에서 넘겨준 값들 다 받기
        Intent intent = getIntent();
        name = intent.getExtras().getString("name");
        confidence = intent.getExtras().getString("confidence");
        lat = intent.getExtras().getDouble("latitude");
        lon = intent.getExtras().getDouble("longitude");

        NameT.setText(name);

        landmark_info landmark = new landmark_info().get(name.toLowerCase());
        String test = landmark.info;
        TestT.setText(test);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status!= ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        button_speak.setOnClickListener(new View.OnClickListener() {
            int Count = 0;
            @Override
            public void onClick(View v) {
                Count++;
                if (Count%2==1){
                    tts.setSpeechRate(1f);
                    tts.speak(TestT.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    tts.stop();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }


    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {

                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

//                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
//                    markerOptions.snippet(markerSnippet);
                    Marker item = restaurant_map.addMarker(markerOptions);
                    previous_marker.add(item);

                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getApplicationContext());
        restaurant_map = googleMap;

        MarkerOptions big_ben_marker = new MarkerOptions();
        big_ben_marker.position(new LatLng(lat, lon));
        big_ben_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.big_ben_pin));
        restaurant_map.addMarker(big_ben_marker);

        LatLng curPoint = new LatLng(lat, lon);
        restaurant_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 14));
    }

    public void showPlaceInformation(LatLng location)
    {
        restaurant_map.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(SecondActivity.this)
                .key("AIzaSyCbczuPt2sl8N5DOQqCKPvynvN9n55rGak")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .build()
                .execute();
    }
}
