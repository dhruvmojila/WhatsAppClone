package com.example.whatsappclone.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Modals.User;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    String userName;
    String userName1;
    String image;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    ProgressDialog dialog;
    Uri selectedImage;
    Boolean editNameVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        editNameVisibility = false;

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);

        userName = getIntent().getStringExtra("uname");
        image = getIntent().getStringExtra("image");

        binding.uprofilename.setText(userName);

        Glide.with(this).load(image)
                .placeholder(R.drawable.avatar)
                .into(binding.uprofileimage);

        binding.linearLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Toast.makeText(EditProfileActivity.this, "Successfully Signed Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditProfileActivity.this,PhoneNumberActivity.class);
                startActivity(intent);
            }
        });

        binding.linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editNameVisibility){
                    binding.editTextName.setVisibility(View.GONE);
                    editNameVisibility=false;
                }else {
                    binding.editTextName.setVisibility(View.VISIBLE);
                    editNameVisibility=true;
                }
            }
        });

        binding.linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 200);
            }
        });

        binding.EditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                userName1 = binding.editTextName.getText().toString();

                if (userName1.isEmpty() || !editNameVisibility){
//                    binding.editTextName.setVisibility(View.VISIBLE);
//                    binding.editTextName.setError("Please enter your name");
//                    return;
                    userName1 = userName;
                }
                if (selectedImage != null) {
                    StorageReference storageReference = storage.getReference().child("Profiles").child(auth.getUid());
                    storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUir = uri.toString();
                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
//                                        String name = binding.editTextName.getText().toString();

                                        User user1 = new User(uid, userName1, phone, imageUir);
                                        database.getReference().child("users").child(uid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                } else {

                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();
//                    String name = binding.editTextName.getText().toString();
                    User user1 = new User(uid, userName1, phone, image);
                    database.getReference().child("users").child(uid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
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

        if (data != null) {
            if (data.getData() != null) {
                binding.uprofileimage.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }

    }
}