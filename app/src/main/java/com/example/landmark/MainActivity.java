package com.example.landmark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;

import java.net.URI;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    RecyclerView recyclerView;
    List<FirebaseVisionCloudLandmark> init;
    private String currentPhotoPath = "";
    FirebaseVisionImage image;
    private final int GET_GALLERY_IMAGE = 200;
    TextView text_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 앱 시작 전 권한 요청
        permission permission_check = new permission();
        permission_check.checkPermissions(MainActivity.this);

        imageView = (ImageView) findViewById(R.id.image);
        text_count = (TextView) findViewById(R.id.landmark_count);

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);

            }
        });

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.landmark_candidate_recyclerview) ;
        recyclerView.setLayoutManager(new LinearLayoutManager
                (getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        init = new ArrayList<>();
        landmark_candidate_adapter adapter =
                new landmark_candidate_adapter(init);
        recyclerView.setAdapter(adapter) ;
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorAccent));
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(300, 300)
                .start(this);
    }

    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    private void imageFromPath(Context context, Uri uri) {
        // [START image_from_path]

        try {
            image = FirebaseVisionImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // [END image_from_path]
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // Check which request we're responding to
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {

                Uri selectedImageUri = data.getData();
                File file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);

//            imageView.setImageURI(selectedImageUri);
                openCropActivity(selectedImageUri, destinationUri);

            } catch (Exception e) {
                Toast.makeText(this,"틀림!", Toast.LENGTH_LONG);
            }

        } else if (resultCode==RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            imageView.setImageURI(resultUri);
            button.setText("Another Image");

            imageFromPath(this, resultUri);

            FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    .getVisionCloudLandmarkDetector();

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
                                    text_count.setVisibility(View.INVISIBLE);
                                }
                                else{
                                    // 런던 시내 안인지 체크하고 아니면 알려주기
                                    ArrayList<FirebaseVisionCloudLandmark> updated_result =
                                            inLondonChecker(firebaseVisionCloudLandmarks);
                                    if(updated_result.isEmpty()){
                                        Toast.makeText(getApplicationContext(),
                                                "런던의 랜드마크가 아니네요! ^^", Toast.LENGTH_LONG).show();
                                        text_count.setVisibility(View.INVISIBLE);

                                        // RecyclerView 초기화
                                        landmark_candidate_adapter adapter =
                                                new landmark_candidate_adapter(init);
                                        recyclerView.setAdapter(adapter) ;
                                    }

                                    // 여기 진입하면 런던시내안이고, 결과 NULL 아님
                                    // top3 (또는 그 이하) RecyclerView 보여주기
                                    else{
                                        int min_item = Math.min(3, updated_result.size());
                                        text_count.setText("총 "+min_item+"개를 찾았어요!");
                                        text_count.setVisibility(View.VISIBLE);

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
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
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
