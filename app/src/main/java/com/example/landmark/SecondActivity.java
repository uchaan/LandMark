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
    TextView NameT, TestT;
    Button button;
    private TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        NameT = (TextView) findViewById(R.id.textView1);
        TestT = (TextView) findViewById(R.id.textView2);
        button = (Button) findViewById(R.id.speak);

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
        button.setOnClickListener(new View.OnClickListener() {
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


}
