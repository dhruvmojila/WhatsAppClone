package com.example.whatsappclone.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsappclone.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser()!=null){
            Intent intent = new Intent(PhoneNumberActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.phoneActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhoneNumberActivity.this,OTPActivity.class);
                intent.putExtra("phoneNumber",binding.editTextPhoneNumber.getText().toString().trim());
                startActivity(intent);
            }
        });

    }
}