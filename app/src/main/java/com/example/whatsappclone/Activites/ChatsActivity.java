package com.example.whatsappclone.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Adapters.MessagesAdapter;
import com.example.whatsappclone.Modals.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ActivityChatsBinding;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatsActivity extends AppCompatActivity {

    ActivityChatsBinding binding;
    String name,receiverUid,userImage;
    FirebaseDatabase database;
    FirebaseStorage storage;

    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom,receiverRoom;
    String senderUid;
    Uri selectedImage;

    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");
        userImage = getIntent().getStringExtra("profilePic");
        senderUid = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);

        binding.uprofilename.setText(name);
        Glide.with(this).load(userImage).placeholder(R.drawable.avatar).into(binding.uprofileimage);

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerChats.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChats.setAdapter(adapter);

        final Handler handler = new Handler();
        binding.editTextChats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child("onlineStatus").child(senderUid).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopTyping,1000);
            }
            Runnable userStopTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("onlineStatus").child(senderUid).setValue("Online");
                }
            };
        });

        database.getReference().child("onlineStatus").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String onlineStatus =snapshot.getValue(String.class);
                    binding.ulastSeen.setVisibility(View.VISIBLE);
                    if (!onlineStatus.isEmpty()){
                        if (onlineStatus.equals("Offline")){
                            binding.ulastSeen.setVisibility(View.GONE);
                        }else {
                            binding.ulastSeen.setVisibility(View.VISIBLE);
                            binding.ulastSeen.setText(onlineStatus);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ChatsActivity.this,MainActivity.class);
//                startActivity(intent);
                finish();
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });

        database.getReference().child("Chats").child(senderRoom).child("message").addValueEventListener(new ValueEventListener() {
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

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = binding.editTextChats.getText().toString();

                DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                Date now = new Date();
                Message message = new Message(messageText,senderUid,dtf.format(now));
                binding.editTextChats.setText("");


                HashMap<String,Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTIme",dtf.format(now));

                database.getReference().child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMsgObj);

                String randomKey = database.getReference().push().getKey();

                database.getReference().child("Chats").child(senderRoom).child("message").child(randomKey).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("Chats").child(receiverRoom).child("message").child(randomKey).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                    }
                });

            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("onlineStatus").child(uid).setValue("Online");
    }
    @Override
    protected void onPause() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("onlineStatus").child(uid).setValue("Offline");
        super.onPause();
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


                                       HashMap<String,Object> lastMsgObj = new HashMap<>();
                                       lastMsgObj.put("lastMsg",message.getMessage());
                                       lastMsgObj.put("lastMsgTIme",dtf.format(now));
                                       lastMsgObj.put("imageUrl",message.getImageUri());

                                       database.getReference().child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                                       database.getReference().child("Chats").child(receiverRoom).updateChildren(lastMsgObj);

                                       String randomKey = database.getReference().push().getKey();

                                       database.getReference().child("Chats").child(senderRoom).child("message").child(randomKey).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               database.getReference().child("Chats").child(receiverRoom).child("message").child(randomKey).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {

                                                   }
                                               });

                                           }
                                       });

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