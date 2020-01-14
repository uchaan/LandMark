package com.example.landmark;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


import noman.googleplaces.PlaceType;

import static android.speech.tts.TextToSpeech.ERROR;

public class SecondActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Marker myMarker;

    private String name;
    private String OpenChat;
    double lat, lon;

    private TextView InfoT, WebT, TicketT, NameT;

    private Button button_show, button_chat;
    private Button SpeakButton, OpenInfoButton;

    private TextToSpeech tts;

    List<Marker> previous_marker = null;
    MapView map;
    GoogleMap restaurant_map;

    String request_type = "restaurant";
    int search_bound = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_second);

        init();

        if (map != null)
        {
            map.onCreate(null);
            map.onResume();
            map.getMapAsync(this);
        }

        previous_marker = new ArrayList<Marker>();

        // 서버에 request 보내서 지도에 레스토랑 찍는 버튼
        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showPlaceInformation(request_type);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                View view = LayoutInflater.from(v.getContext()).inflate(R.layout.place_type, null, false);
                builder.setView(view);

                final AlertDialog dialog = builder.create();
                final Button button_close = view.findViewById(R.id.place_type_close);
                final Button button_search = view.findViewById(R.id.place_type_search);
                final Spinner spinner_placetype = view.findViewById(R.id.select_place_type);
                final EditText text_search_bound = view.findViewById(R.id.place_search_bound);


                button_close.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                spinner_placetype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        request_type = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                dialog.show();

                button_search.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        // 사용자가 범위 입력 안함
                        if (text_search_bound.getText().toString().equals(""))
                            Toast.makeText(getApplicationContext(), "범위를 입력해 주세요!", Toast.LENGTH_SHORT).show();

                        // 사용자가 범위 입력 완료
                        else {
                            search_bound = Integer.parseInt(text_search_bound.getText().toString());
                            showPlaceInformation(request_type, search_bound);
                            dialog.dismiss();
                        }
                    }
                });

            }
        });

 
        // 이전 액티비티에서 넘겨준 값들 다 받기
        Intent intent = getIntent();
        name = intent.getExtras().getString("name");
        lat = intent.getExtras().getDouble("latitude");
        lon = intent.getExtras().getDouble("longitude");

        // 타이틀 띄우기
        NameT.setText(name);

        // 가져온 랜드마크 이름으로 랜드마크 데이 가져오기 .
        landmark_info landmark = new landmark_info().get(name.toLowerCase());
        InfoT.setText(landmark.info);
        WebT.setText("공식 웹사이트 : " + landmark.website);
        TicketT.setText("티켓 구매처 : "+landmark.ticket);
        OpenChat = landmark.OpenChatting;

        // 오픈채팅 버튼 링크 설정.
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
                    OpenInfoButton.setText("설명 닫기");

                } else {
                    findViewById(R.id.information).setVisibility(View.GONE);
                    OpenInfoButton.setText("설명 열기");
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
    public void showPlaceInformation(String type, int radius)
    {
        restaurant_map.clear();//지도 클리어

        restaurant_map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                Typeface face = ResourcesCompat.getFont(getApplicationContext(), R.font.ridibatang);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                title.setTypeface(face);
                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());
                snippet.setTypeface(face);
                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });

        //marker를 선택했을 때 Detail 불러옴
        restaurant_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                        sendDetailsRequest task = new sendDetailsRequest();
                        RequestItem item = new RequestItem().requestDetailsItem(marker.getSnippet());
                        try {
                            RequestItem result = task.execute(item).get();

                            if (result.address.equals("")) {
                                marker.setSnippet("주소: 미등록 " + "\n"+"평점: 미등록 ");
                            } else {

                                marker.setSnippet("주소: " + result.address+"\n"+"평점: " + String.valueOf(result.rating));

                            }



                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                return false;
            }
        });

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        //사용자가 지정한 type 에 따라 서버에 Request 보내서 정보 받아옴
        RequestItem item = new RequestItem(type, lat, lon, radius);
        sendRequest task = new sendRequest();
        try {
            ArrayList<RequestItem> result = task.execute(item).get();

            for (int i = 0; i < result.size(); i++){

                if (result.get(i).error.equals("error")) {
                    Toast.makeText(getApplicationContext(), "검색 결과가 없어요 ㅜㅜ", Toast.LENGTH_SHORT).show();
                    break;
                }

                myMarker = restaurant_map.addMarker(new MarkerOptions()
                            .position(new LatLng(result.get(i).lat, result.get(i).lng))
                            .title(result.get(i).name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin))
                            .snippet(result.get(i).id));
              
                myMarker.setTag(new LatLng(result.get(i).lat, result.get(i).lng));
                Toast.makeText(getApplicationContext(), "위치 정보를 받아왔어요!! ^^", Toast.LENGTH_SHORT).show();

            }

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
                show_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.london_eye_pin));
                break;
            case "tower bridge" :
                show_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.tower_bridge_pin));
                break;
            case "va museum" :
                show_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.museum_pin));
                break;
            default:
                show_marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin));
                break;
        }
        restaurant_map.addMarker(show_marker);
    }


    public void init() {

        NameT = (TextView) findViewById(R.id.name);
        InfoT = (TextView) findViewById(R.id.info);
        WebT = (TextView) findViewById(R.id.website);
        TicketT = (TextView) findViewById(R.id.ticket);

        SpeakButton = (Button) findViewById(R.id.speak);
        OpenInfoButton = (Button) findViewById(R.id.openInfo);

        map = (MapView) findViewById(R.id.map_restaurant);

        button_show = (Button)findViewById(R.id.show_restaurant);
        button_chat = (Button)findViewById(R.id.chat);
    }

}
