package com.example.mango_knn;

import android.annotation.SuppressLint;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Utilites {

    public static final int FEAT_CNT = 4;

    public static final String[] class_name = {"matang", "mentah", "sangat_matang"};

    private final String DATA_FILE_NAME = "data.csv";
    private final String RESULT_FILE_NAME = "result.csv";

    public static final double[] scaler_data_min = {300.0, 5.0, 0.0, 0.3};
    public static final double[] scaler_data_max = {1600.0, 20.0, 0.6, 0.8};

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

    }

    @SuppressLint("DefaultLocale")
    void extractGLCMforAll() {
        // Features:
        float[] features = new float[FEAT_CNT];
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
                    features[0] = (float) glcmfe.getContrast();
                    features[1] = (float) glcmfe.getDissimilarity();
                    features[2] = (float) glcmfe.getEnergy();
                    features[3] = (float) glcmfe.getHomogenity();

                    StringBuilder writeData = new StringBuilder();

                    // scale features
                    for (int i = 0; i < FEAT_CNT; i++) {
                        features[i] = (float) ((features[i] - scaler_data_min[i]) / (scaler_data_max[i] - scaler_data_min[i]));
                        writeData.append(String.format("%s,", features[i]));
                    }
                    writeData.append(String.format("%s", class_name[idx]));
                    writefile(writeData.toString(), DATA_FILE_NAME);

                    id++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
