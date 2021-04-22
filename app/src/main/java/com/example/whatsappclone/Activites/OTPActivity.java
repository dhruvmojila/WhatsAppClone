package com.example.whatsappclone.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.whatsappclone.databinding.ActivityOTPBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    ActivityOTPBinding binding;
    String phoneNumber;
    FirebaseAuth auth;
    ProgressDialog dialog;
    String verficationCodeBySystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending otp");
        dialog.setCancelable(false);
        dialog.dismiss();

        phoneNumber = getIntent().getStringExtra("phoneNumber");

        binding.phonenumshowtextview.setText("Verify " + phoneNumber);

        sendVerificationCodeToUser(phoneNumber);

        binding.verifyotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = binding.otpview.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6 || code.length() > 6) {
                    binding.otpview.setError("Wrong OTP");
                    binding.otpview.requestFocus();
                    return;
                }
                dialog.show();
                verifyCode(code);
            }
        });
    }

    public void sendVerificationCodeToUser(String phoneNumber) {
        auth = FirebaseAuth.getInstance();
        auth.useAppLanguage();
        auth.getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91" + phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                verficationCodeBySystem = s;
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInTheUser(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    public void verifyCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verficationCodeBySystem, code);
        signInTheUser(credential);

    }

    public void signInTheUser(PhoneAuthCredential credential) {

        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Intent intent = new Intent(OTPActivity.this, SetUpProfile.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            Toast.makeText(OTPActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}