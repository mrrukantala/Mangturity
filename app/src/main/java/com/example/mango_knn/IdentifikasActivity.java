package com.example.mango_knn;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mango_knn.databinding.ActivityIdentifikasBinding;

public class IdentifikasActivity extends AppCompatActivity {

    private ActivityIdentifikasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdentifikasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intentGalery = new Intent(this, MainActivity.class);
        binding.button5.setOnClickListener(view -> {
            startActivity(intentGalery);
        });

        Intent intentCamera = new Intent(this, MainActivity2.class);
        binding.button6.setOnClickListener(view -> {
            startActivity(intentCamera);
        });


    }
}