package com.example.whatsappclone.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.Presentation;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.TopStatusAdapter;
import com.example.whatsappclone.Modals.Status;
import com.example.whatsappclone.Modals.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Modals.User;
import com.example.whatsappclone.Adapters.UsersAdapter;
import com.example.whatsappclone.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> arrayList;
    UsersAdapter adapter;
    FirebaseAuth auth;
    TopStatusAdapter topStatusAdapter;
    ArrayList<UserStatus> arrayListStatus;
    ProgressDialog dialog;
    User user;
    User editUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        arrayList = new ArrayList<>();
        arrayListStatus = new ArrayList<>();


        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter = new UsersAdapter(this,arrayList);
        topStatusAdapter = new TopStatusAdapter(this,arrayListStatus);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.showShimmerAdapter();



        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerViewStatus.setLayoutManager(manager);
        binding.recyclerViewStatus.setAdapter(topStatusAdapter);
        binding.recyclerViewStatus.showShimmerAdapter();

        binding.bottomNavigationView2.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.status:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent,75);
                        break;
                }
                return false;
            }
        });

        database.getReference().child("story").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    arrayListStatus.clear();
                    for (DataSnapshot storySnapshot: snapshot.getChildren()){
                        UserStatus userStatus = new UserStatus();
                        userStatus.setName(storySnapshot.child("name").getValue(String.class));
                        userStatus.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        userStatus.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        userStatus.setUid(storySnapshot.child("uid").getValue(String.class));

                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnopshot : storySnapshot.child("status").getChildren()){
                            Status sampleStatus = statusSnopshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        userStatus.setStatuses(statuses);
                        arrayListStatus.add(userStatus);
                    }
                }
                binding.recyclerViewStatus.hideShimmerAdapter();
                topStatusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if (user.getUid().equals(auth.getCurrentUser().getUid())){
                        editUser = user;
                        continue;
                    }
                    else {
                        arrayList.add(user);
                    }
                }
                binding.recyclerView.hideShimmerAdapter();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();

                break;
            case R.id.groups:
                Intent intent1 = new Intent(MainActivity.this, GroupChat.class);
                startActivity(intent1);
                break;
            case R.id.invite:
                Intent sendData = new Intent();
                sendData.setAction(Intent.ACTION_SEND);
                sendData.setType("text/plain");
                sendData.putExtra(Intent.EXTRA_TEXT,"Invite link will be generated soon !!");
                startActivity(sendData);
                break;
            case R.id.settings:
                Intent intent4 = new Intent(MainActivity.this, EditProfileActivity.class);
                intent4.putExtra("uname",editUser.getName());
                intent4.putExtra("image",editUser.getProfileImage());
                intent4.putExtra("uid",editUser.getUid());
                intent4.putExtra("phone",editUser.getPhoneNumber());
                startActivity(intent4);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            if (data.getData() != null){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setUid(user.getUid());
                                    userStatus.setLastUpdated(date.getTime());

                                    Status status = new Status(uri.toString(),userStatus.getLastUpdated(),userStatus.getUid());

                                    HashMap<String,Object> obj = new HashMap<>();
                                    obj.put("name",userStatus.getName());
                                    obj.put("profileImage",userStatus.getProfileImage());
                                    obj.put("lastUpdated",userStatus.getLastUpdated());
                                    obj.put("uid",userStatus.getUid());
                                    database.getReference().child("story").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
                                    database.getReference().child("story").child(FirebaseAuth.getInstance().getUid()).child("status").push().setValue(status);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("onlineStatus").child(uid).setValue("Online");
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        database.getReference().child("onlineStatus").child(uid).setValue("Offline");
//    }


    @Override
    protected void onPause() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.getReference().child("onlineStatus").child(uid).setValue("Offline");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}