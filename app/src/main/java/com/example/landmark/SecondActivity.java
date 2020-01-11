package com.example.landmark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.jar.Attributes;

import static android.speech.tts.TextToSpeech.ERROR;

public class SecondActivity extends AppCompatActivity {

    String name, confidence;
    double lat, lon;
    TextView NameT, InfoT, WebT, TicketT;
    Button SpeakButton, OpenInfoButton;
    private TextToSpeech tts;



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


}
