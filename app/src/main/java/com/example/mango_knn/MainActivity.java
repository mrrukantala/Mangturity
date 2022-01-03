package com.example.mango_knn;


import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import ir.androidexception.filepicker.dialog.SingleFilePickerDialog;

public class MainActivity extends AppCompatActivity {

    private final int FEAT_CNT = 4;
    private double[] scaler_data_min = {300.0, 5.0, 0.0, 0.3};
    private double[] scaler_data_max = {1600.0, 20.0, 0.6, 0.8};

    private TextView edtPath;
    private Button btnBrowse;
    private Button btnPredict;
    private TextView edtResult;
    private ImageView imgResult;

    String[] class_name = {"matang", "mentah", "sangat_matang"};

    private final String DATA_FILE_NAME = "data.csv";
    private final String RESULT_FILE_NAME = "result.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize controls
        edtPath = this.findViewById(R.id.edtPath);
        btnBrowse = this.findViewById(R.id.btnBrowse);
        btnPredict = this.findViewById(R.id.btnPredict);
        edtResult = this.findViewById(R.id.edtResult);
        imgResult = this.findViewById(R.id.imgResult);

        // add button listener
        btnBrowse.setOnClickListener(v -> {
            onClickBrowse();
        });
        btnPredict.setOnClickListener(v -> {
            onClickPredict();
        });

        // test code for extract all features
        Toast.makeText(this, "Run", Toast.LENGTH_SHORT).show();
        testWithAllData();
        extractGLCMforAll();
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
        edtResult.setText(String.format("File: %s\nResult = %s\n\n\n%s: %.2f\n%s: %.2f\n%s: %.2f",
                edtPath.getText().toString(), class_name[prediction],
                class_name[0], prob_result[0],
                class_name[1], prob_result[1],
                class_name[2], prob_result[2]));

//        // feature:0
//        double[] features0 = {0.588804, 0.539979, 0.25955, 0.582839, 0.447251, 0.421144, 0.263315, 0.444884, 0.471023, 0.465584, 0.492461, 0.46241, 0.382667, 0.376604, 0.415602, 0.376871};
//
//        // feature:1
//        double[] features1 = {0.756979, 0.739611, 0.336453, 0.625054, 0.408937, 0.406517, 0.214313, 0.349591, 0.54861, 0.540095, 0.568018, 0.544127, 0.467331, 0.457242, 0.497231, 0.467834};
//
//        // feature:2
//        double[] features2 = {0.381966, 0.432081, 0.250848, 0.388679, 0.232221, 0.251189, 0.143969, 0.240299, 0.468945, 0.455229, 0.481412, 0.45876, 0.443517, 0.460632, 0.489147, 0.421502};
//
//        prediction = clf.predict(features0);
//        System.out.println(prediction);
//        prediction = clf.predict(features1);
//        System.out.println(prediction);
//        prediction = clf.predict(features2);
//        System.out.println(prediction);
    }

    public void writefile(String text, String filename) {
        File myFile = new File("/storage/emulated/0/" + filename);
        try {
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            FileOutputStream fostream = new FileOutputStream(myFile, true);
            OutputStreamWriter oswriter = new OutputStreamWriter(fostream);
            oswriter.append(text);
            oswriter.append("\n");
            oswriter.close();
            fostream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void testWithAllData() {
        // Features:
        double[] features = new double[FEAT_CNT];

        // Estimators:
        KNeighborsClassifier clf = new KNeighborsClassifier(model_data.model_data_json);

        // write column headers
        writefile("Class,FileName,Predicted,Result,Prob_matang,Prob_mentah,Prob_sangat_matang", RESULT_FILE_NAME);

        for (int idx = 0; idx < 3; idx++) {
            for (int num = 1; num <= 40; num++) {
                try {
                    // make path name
                    String szPath = "/storage/emulated/0/mangga/" + class_name[idx] + "/" + String.format("%03d.JPG", num);
                    System.out.println(szPath);

                    // extract image & get feature
                    GLCMFeatureExtraction glcmfe = new GLCMFeatureExtraction(szPath, 256);
                    glcmfe.extract(5);

                    // get features
                    features[0] = glcmfe.getContrast();
                    features[1] = glcmfe.getDissimilarity();
                    features[2] = glcmfe.getEnergy();
                    features[3] = glcmfe.getHomogenity();

                    // scale features
                    for (int i = 0; i < FEAT_CNT; i++) {
                        features[i] = (features[i] - scaler_data_min[i]) / (scaler_data_max[i] - scaler_data_min[i]);
                    }

                    // Prediction:
                    int prediction = clf.predict(features);

                    // write result to file
                    String resultData = class_name[idx] + ",";
                    resultData += String.format("%03d.JPG", num) + ",";
                    resultData += class_name[prediction] + ",";
                    if (idx == prediction)
                        resultData += "OK,";
                    else
                        resultData += "NG,";
                    double[] prob_res = clf.getProb();
                    resultData += String.format("%.2f,%.2f,%.2f", prob_res[0], prob_res[1], prob_res[2]);
                    writefile(resultData, RESULT_FILE_NAME);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Toast.makeText(this, "Test With ALl Data Succes", Toast.LENGTH_SHORT).show();
    }

    void extractGLCMforAll() {
        // Features:
        double[] features = new double[FEAT_CNT];
        String[] class_name = {"matang", "mentah", "sangat_matang"};

        // write column headers
        writefile("contrast,dissimilarity,energy,homogeneity,Maturity_level", DATA_FILE_NAME);

        int id = 0;
        for (int idx = 0; idx < 3; idx++) {
            for (int num = 1; num <= 40; num++) {
                try {
                    // make path name
                    String szPath = "/storage/emulated/0/mangga/" + class_name[idx] + "/" + String.format("%03d.JPG", num);
                    System.out.println(szPath);

                    // extract image & get feature
                    GLCMFeatureExtraction glcmfe = new GLCMFeatureExtraction(szPath, 256);
                    glcmfe.extract(5);

                    // get features
                    features[0] = glcmfe.getContrast();
                    features[1] = glcmfe.getDissimilarity();
                    features[2] = glcmfe.getEnergy();
                    features[3] = glcmfe.getHomogenity();

                    String writeData = "";

                    // scale features
                    for (int i = 0; i < FEAT_CNT; i++) {
                        features[i] = (features[i] - scaler_data_min[i]) / (scaler_data_max[i] - scaler_data_min[i]);
                        writeData += String.format("%f,", features[i]);
                    }
                    writeData += String.format("%s", class_name[idx]);
                    writefile(writeData, DATA_FILE_NAME);

                    id++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Toast.makeText(this, "Extract GLCM Succes", Toast.LENGTH_SHORT).show();
    }
}
