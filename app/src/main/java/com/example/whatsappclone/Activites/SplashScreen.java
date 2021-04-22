package com.example.whatsappclone.Activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.whatsappclone.databinding.ActivitySplashScreenBinding;

public class SplashScreen extends AppCompatActivity {

    ActivitySplashScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView.setTranslationX(500);
        binding.animationView.setTranslationX(-500);

        binding.textView.animate().translationX(0).setDuration(1500).setStartDelay(0);
        binding.animationView.animate().translationX(0).setDuration(1500).setStartDelay(0);

//        binding.textView.animate().translationX(-1000).setDuration(1500).setStartDelay(3000);
//        binding.animationView.animate().translationX(1000).setDuration(1500).setStartDelay(3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this,PhoneNumberActivity.class);
                startActivity(intent);
                finish();
            }
        },5000);

    }
}