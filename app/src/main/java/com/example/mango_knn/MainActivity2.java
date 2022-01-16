package com.example.mango_knn;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.IOException;
import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {

    private final int FEAT_CNT = 4;
    private final double[] scaler_data_min = {300.0, 5.0, 0.0, 0.3};
    private final double[] scaler_data_max = {1600.0, 20.0, 0.6, 0.8};

    private TextView edtPath;
    private Button btnBrowse;
    private Button btnPredict;
    private TextView edtResult;
    private ImageView imgResult;
    private TextView edtResultBold;

    String[] class_name = {"matang", "mentah", "sangat_matang"};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            edtPath.setText(data.getData().getPath());
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // initialize controls
        edtPath = this.findViewById(R.id.edtPath);
        btnBrowse = this.findViewById(R.id.btnBrowse);
        btnPredict = this.findViewById(R.id.btnPredict);
        edtResult = this.findViewById(R.id.edtResult);
        imgResult = this.findViewById(R.id.imgResult);
        edtResultBold = this.findViewById(R.id.edtResultBold);

        // add button listener
        btnBrowse.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .crop()
                    .cameraOnly()
                    .start();
        });

        btnPredict.setOnClickListener(v -> {
            onClickPredict();
        });
    }
    @SuppressLint("DefaultLocale")
    private void onClickPredict() {
        // Features:
        double[] features = new double[FEAT_CNT];
        for (int i = 1, l = FEAT_CNT; i < l; i++) {
            features[i - 1] = 0.0;
        }

        // load image
        try {
            GLCMFeatureExtraction glcmfe = new GLCMFeatureExtraction(edtPath.getText().toString(), 256);
            glcmfe.extract(5);
            imgResult.setImageBitmap(glcmfe.getBitmap());

            // get features
            features[0] = glcmfe.getContrast();
            features[1] = glcmfe.getDissimilarity();
            features[2] = glcmfe.getEnergy();
            features[3] = glcmfe.getHomogenity();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // scale features
        for (int i = 0; i < FEAT_CNT; i++) {
            features[i] = (features[i] - scaler_data_min[i]) / (scaler_data_max[i] - scaler_data_min[i]);
        }

        // Estimators:
        KNeighborsClassifier clf = new KNeighborsClassifier(model_data.model_data_json);

        // Prediction:
        int prediction = clf.predict(features);
        System.out.println(prediction);
        double[] prob_result = clf.getProb();
        edtResult.setText(String.format("File: %s\n\n%s: %2.0f%%\n%s: %2.0f%%\n%s: %2.0f%%",
                edtPath.getText().toString(),
                class_name[0], prob_result[0] * 100,
                class_name[1], prob_result[1] * 100,
                class_name[2], prob_result[2] * 100));

        edtResultBold.setText(String.format("Result = %s",
                class_name[prediction]));
    }
}
