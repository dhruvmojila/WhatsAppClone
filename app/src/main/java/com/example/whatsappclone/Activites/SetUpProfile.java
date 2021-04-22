package com.example.whatsappclone.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.whatsappclone.Modals.User;
import com.example.whatsappclone.databinding.ActivitySetUpProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetUpProfile extends AppCompatActivity {

    ActivitySetUpProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);


        binding.ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,200);
            }
        });

        binding.SetProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                String userName = binding.editTextName.getText().toString();

                if (userName.isEmpty()){
                    binding.editTextName.setError("Please enter your name");
                    return;
                }
                if (selectedImage != null){
                    StorageReference storageReference = storage.getReference().child("Profiles").child(auth.getUid());
                    storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUir = uri.toString();
                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.editTextName.getText().toString();

                                        User user1 = new User(uid,name,phone,imageUir);
                                        database.getReference().child("users").child(uid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                Intent intent = new Intent(SetUpProfile.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }else {

                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();
                    String name = binding.editTextName.getText().toString();

                    User user1 = new User(uid,name,phone,"no image");
                    database.getReference().child("users").child(uid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            Intent intent = new Intent(SetUpProfile.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data!=null){
            if (data.getData()!=null){
                binding.ProfileImage.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }

    }
}