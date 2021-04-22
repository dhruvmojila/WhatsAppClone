 package com.example.whatsappclone.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.whatsappclone.Adapters.GroupMessagesAdapter;
import com.example.whatsappclone.Adapters.MessagesAdapter;
import com.example.whatsappclone.Modals.Message;
import com.example.whatsappclone.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

 public class GroupChat extends AppCompatActivity {

    ActivityGroupChatBinding binding;

    String senderUid;
    Uri selectedImage;

     FirebaseDatabase database;
     FirebaseStorage storage;

     GroupMessagesAdapter adapter;
     ArrayList<Message> messages;

     ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Group Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        senderUid = FirebaseAuth.getInstance().getUid();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);

        messages = new ArrayList<>();
        adapter = new GroupMessagesAdapter(this,messages);
        binding.recyclerChats.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChats.setAdapter(adapter);

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = binding.editTextChats.getText().toString();

                DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                Date now = new Date();
                Message message = new Message(messageText,senderUid,dtf.format(now));
                binding.editTextChats.setText("");

                database.getReference().child("public").push().setValue(message);
            }
        });
        database.getReference().child("public").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Message message = snapshot1.getValue(Message.class);
                    message.setMessageId(snapshot1.getKey());
                    messages.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

     @Override
     public boolean onSupportNavigateUp() {
        finish();
         return super.onSupportNavigateUp();
     }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == 45){
             if (data!=null){
                 if (data.getData()!=null){
                     selectedImage = data.getData();
                     Calendar ca = Calendar.getInstance();
                     StorageReference storageReference = storage.getReference().child("chats").child(ca.getTimeInMillis()+"");
                     dialog.show();
                     storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                             if (task.isSuccessful()){
                                 storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                     @Override
                                     public void onSuccess(Uri uri) {
                                         dialog.dismiss();
                                         String filePath = uri.toString();

                                         String messageText = binding.editTextChats.getText().toString();

                                         DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                                         Date now = new Date();
                                         Message message = new Message(messageText,senderUid,dtf.format(now));
                                         message.setImageUri(filePath);
                                         message.setMessage("photo");
                                         binding.editTextChats.setText("");


                                         database.getReference().child("public").push().setValue(message);
                                     }
                                 });
                             }
                         }
                     });
                 }
             }
         }
     }
 }