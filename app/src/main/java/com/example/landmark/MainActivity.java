package com.example.landmark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.image);
        textView = (TextView) findViewById(R.id.textView) ;

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

        // *********** ML Kit code *****************************
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

                    FirebaseVisionCloudDetectorOptions options =
                            new FirebaseVisionCloudDetectorOptions.Builder()
                                    .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                                    .setMaxResults(15)
                                    .build();


                    FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                            .getVisionCloudLandmarkDetector(options);
                    // Or, to change the default settings:
                    // FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    //         .getVisionCloudLandmarkDetector(options);

                    Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                                    // Task completed successfully
                                    // ...

                                    for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                                        Rect bounds = landmark.getBoundingBox();
                                        String landmarkName = landmark.getLandmark();

                                        textView.setText(landmarkName);

                                        String entityId = landmark.getEntityId();
                                        float confidence = landmark.getConfidence();

                                        // Multiple locations are possible, e.g., the location of the depicted
                                        // landmark and the location the picture was taken.
                                        for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                            double latitude = loc.getLatitude();
                                            double longitude = loc.getLongitude();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                }
                            });




                    // *************************************************************


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
