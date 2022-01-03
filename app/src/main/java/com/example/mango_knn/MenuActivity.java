package com.example.mango_knn;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mango_knn.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();


        Intent intentIdentifikasi = new Intent(this, IdentifikasActivity.class);
        binding.button2.setOnClickListener(view -> startActivity(intentIdentifikasi));

        Intent intentInformasi = new Intent(this, InformationActivity.class);
        binding.button.setOnClickListener(view -> startActivity(intentInformasi));

        Intent intentTentang = new Intent(this, AboutActivity.class);
        binding.button4.setOnClickListener(view -> startActivity(intentTentang));

        Intent intentBantuan = new Intent(this, BantuanActivity.class);
        binding.button3.setOnClickListener(view -> startActivity(intentBantuan));


    }
}