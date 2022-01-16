package com.example.mango_knn;


import static com.example.mango_knn.Utilites.class_name;
import static com.example.mango_knn.Utilites.scaler_data_max;
import static com.example.mango_knn.Utilites.scaler_data_min;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import com.example.mango_knn.Utilites.*;

import ir.androidexception.filepicker.dialog.SingleFilePickerDialog;

public class MainActivity extends AppCompatActivity {

    private TextView edtPath;
    private Button btnBrowse;
    private Button btnPredict;
    private TextView edtResult;
    private ImageView imgResult;
    private TextView edtResultBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Utilites utilites = new Utilites();

        utilites.testWithAllData();
        utilites.extractGLCMforAll();

        // initialize controls
        edtPath = this.findViewById(R.id.edtPath);
        btnBrowse = this.findViewById(R.id.btnBrowse);
        btnPredict = this.findViewById(R.id.btnPredict);
        edtResult = this.findViewById(R.id.edtResult);
        imgResult = this.findViewById(R.id.imgResult);
        edtResultBold = this.findViewById(R.id.edtResultBold);

        // add button listener
        btnBrowse.setOnClickListener(v -> {
            onClickBrowse();
        });

        btnPredict.setOnClickListener(v -> {
            onClickPredict();
        });
    }

    private boolean permissionGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    private void onClickBrowse() {
        if(permissionGranted()) {
            SingleFilePickerDialog singleFilePickerDialog = new SingleFilePickerDialog(this,
                    () -> Toast.makeText(MainActivity.this, "Canceled!!", Toast.LENGTH_SHORT).show(),
                    files -> {
                        edtPath.setText(files[0].getPath());
                    });
            singleFilePickerDialog.show();
        }
        else{
            requestPermission();
        }
    }

    private void onClickPredict() {
        // Features:
        double[] features = new double[Utilites.FEAT_CNT];
        for (int i = 1, l = Utilites.FEAT_CNT; i < l; i++) {
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
        for (int i = 0; i < Utilites.FEAT_CNT; i++) {
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
