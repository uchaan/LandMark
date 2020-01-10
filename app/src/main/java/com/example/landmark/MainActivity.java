package com.example.landmark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    TextView textView;
    RecyclerView recyclerView;
    List<FirebaseVisionCloudLandmark> init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.image);

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
//                사진여러장 선택할수 있게 해줌.
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, 1);

            }
        });

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.landmark_candidate_recyclerview) ;
        recyclerView.setLayoutManager(new LinearLayoutManager
                (getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

        init = new ArrayList<>();
        landmark_candidate_adapter adapter =
                new landmark_candidate_adapter(init);
        recyclerView.setAdapter(adapter) ;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    // 이미지 표시
                    // *************************************************************
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(img);
                    imageView.setImageBitmap(img);
                    button.setText("another image");

//                    FirebaseVisionCloudDetectorOptions options =
//                            new FirebaseVisionCloudDetectorOptions.Builder()
//                                    .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
//                                    .setMaxResults(15)
//                                    .build();


                    FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                            .getVisionCloudLandmarkDetector();
                    // Or, to change the default settings:
                    // FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    //         .getVisionCloudLandmarkDetector(options);

                    Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                                    // Task completed successfully
                                    // ...

                                    // 받아온 결과가 처음부터 NULL(랜드마크가 아님)
                                    if(firebaseVisionCloudLandmarks.isEmpty()){
                                        Toast.makeText(getApplicationContext(),
                                                "다른 사진으로 재도전!", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        // 런던 시내 안인지 체크하고 아니면 알려주기
                                        ArrayList<FirebaseVisionCloudLandmark> updated_result =
                                                inLondonChecker(firebaseVisionCloudLandmarks);
                                        if(updated_result.isEmpty()){
                                            Toast.makeText(getApplicationContext(),
                                                    "런던의 랜드마크가 아니네요! ^^", Toast.LENGTH_LONG).show();

                                            // RecyclerView 초기화
                                            landmark_candidate_adapter adapter =
                                                    new landmark_candidate_adapter(init);
                                            recyclerView.setAdapter(adapter) ;
                                        }

                                        // 여기 진입하면 런던시내안이고, 결과 NULL 아님
                                        // top3 (또는 그 이하) RecyclerView 보여주기
                                        else{
                                            int min_item = Math.min(3, updated_result.size());
//                                        init = updated_result.subList(0,min_item)
                                            landmark_candidate_adapter adapter =
                                                    new landmark_candidate_adapter(updated_result.subList(0,min_item));
                                            recyclerView.setAdapter(adapter) ;
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                    Toast.makeText(getApplicationContext(),
                                            "랜드마크 인식에 실패했어요 ㅜㅜ", Toast.LENGTH_LONG).show();
                                }
                            });




                    // *************************************************************


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 랜드마크의 위치가 런던 시내 이내인지 확인하고 sorted arraylist 반환
    public ArrayList<FirebaseVisionCloudLandmark> inLondonChecker(List <FirebaseVisionCloudLandmark> arr){

        ArrayList<FirebaseVisionCloudLandmark> updated_arr = new ArrayList<>();

        // 받은 결과값이 null 이 아닌 경우
        if (!arr.isEmpty()) {
            //위도 경도 확인
            for (FirebaseVisionCloudLandmark landmark : arr) {
                for (FirebaseVisionLatLng loc : landmark.getLocations()) {
                    double latitude = loc.getLatitude();
                    double longitude = loc.getLongitude();

                    if (longitude <= 0 && longitude >= -0.204021) {
                        if (latitude <= 51.55 && latitude >= 51.48)
                            updated_arr.add(landmark);
                    }
                }
            }
        }

        return updated_arr;
    }

}
