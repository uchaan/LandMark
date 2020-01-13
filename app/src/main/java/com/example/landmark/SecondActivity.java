package com.example.landmark;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.ExecutionException;


import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

import static android.speech.tts.TextToSpeech.ERROR;

public class SecondActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Marker myMarker;



    String name, confidence;
    String OpenChat;
    double lat, lon;

    Button button_show, button_chat;

    TextView InfoT, WebT, TicketT, NameT;
    Button SpeakButton, OpenInfoButton;

    private TextToSpeech tts;

    List<Marker> previous_marker = null;
    MapView map;
    GoogleMap restaurant_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        NameT = (TextView) findViewById(R.id.name);
        InfoT = (TextView) findViewById(R.id.info);
        WebT = (TextView) findViewById(R.id.website);
        TicketT = (TextView) findViewById(R.id.ticket);

        SpeakButton = (Button) findViewById(R.id.speak);
        OpenInfoButton = (Button) findViewById(R.id.openInfo);

        map = (MapView) findViewById(R.id.map_restaurant);

        if (map != null)
        {
            map.onCreate(null);
            map.onResume();
            map.getMapAsync(this);
        }

        previous_marker = new ArrayList<Marker>();

        // 서버에 request 보내서 지도에 레스토랑 찍는 버튼
        button_show = (Button)findViewById(R.id.show_restaurant);
        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(new LatLng(lat, lon), PlaceType.RESTAURANT);
            }
        });


        // 이전 액티비티에서 넘겨준 값들 다 받기
        Intent intent = getIntent();
        name = intent.getExtras().getString("name");
        confidence = intent.getExtras().getString("confidence");
        lat = intent.getExtras().getDouble("latitude");
        lon = intent.getExtras().getDouble("longitude");

        NameT.setText(name);

        // 가져온 랜드마크 이름으로 랜드마크 설명 정보 가져오기 .
        landmark_info landmark = new landmark_info().get(name.toLowerCase());
        InfoT.setText(landmark.info);
        WebT.setText(landmark.website);
        TicketT.setText(landmark.ticket);
        OpenChat = landmark.OpenChatting;

        button_chat = (Button)findViewById(R.id.chat);
        button_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(OpenChat));
                startActivity(intent);
            }
        });

        // 설명 보기 버튼 설정.
        OpenInfoButton.setOnClickListener(new View.OnClickListener() {
            int count = 0;
            @Override
            public void onClick(View v) {
                count++;
                if (count%2==1) {
                    findViewById(R.id.information).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.information).setVisibility(View.GONE);
                }

            }
        });

        // 읽어주기  객체 생성.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status!= ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // 읽어주기 시작 버튼 설정.
        SpeakButton.setOnClickListener(new View.OnClickListener() {
            int Count = 0;
            @Override
            public void onClick(View v) {
                Count++;
                if (Count%2==1){
                    tts.setSpeechRate(1f);
                    tts.speak(InfoT.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
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
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getApplicationContext());
        restaurant_map = googleMap;

        show_landmark(name);

        LatLng curPoint = new LatLng(lat, lon);
        restaurant_map.moveCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 14));
    }

    // Show Restaurant 버튼 눌렀을 때 호출됨
    // 서버에서 이름, 아이디, 위도, 경도 받아옴
    // 위도 경도에 맞게 지도에 마커 찍어줌
    public void showPlaceInformation(LatLng location, String type)
    {
        restaurant_map.clear();//지도 클리어

        restaurant_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                        sendDetailsRequest task = new sendDetailsRequest();
                        RequestItem item = new RequestItem().requestDetailsItem(marker.getSnippet());
                        try {
                            RequestItem result = task.execute(item).get();

                            Toast.makeText(getApplicationContext(), marker.getTag().toString() , Toast.LENGTH_LONG);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                return false;
            }
        });

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


        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        RequestItem item = new RequestItem("restaurant", lat, lon, 500);
        sendRequest task = new sendRequest();
        try {
            ArrayList<RequestItem> result = task.execute(item).get();

            for (int i = 0; i < result.size(); i++){
                myMarker = restaurant_map.addMarker(new MarkerOptions()
                            .position(new LatLng(result.get(i).lat, result.get(i).lng))
                            .title(result.get(i).name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin))
                            .snippet(result.get(i).id));

                myMarker.setTag(result.get(i).id);

//                MarkerOptions temp = new MarkerOptions();
//                temp.position(new LatLng(result.get(i).lat, result.get(i).lng));
//                temp.title(result.get(i).name);
//                temp.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin));
//                restaurant_map.addMarker(temp);
            }
            Toast.makeText(getApplicationContext(), "Show place information!! : "+result.get(0).name, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        show_landmark(name);
    }



    // String Parameter 에 따라 다른 아이콘 표시
    public void show_landmark(String landmark){
        MarkerOptions show_marker = new MarkerOptions();
        show_marker.position(new LatLng(lat, lon));

        switch (landmark.toLowerCase()){
            case "big ben":
                show_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.big_ben_pin));
                break;
            case "london eye":
                break;
            case "tower bridge" :
                break;
            case "victoria and albert museum" :
                break;
        }
        restaurant_map.addMarker(show_marker);
    }

}
