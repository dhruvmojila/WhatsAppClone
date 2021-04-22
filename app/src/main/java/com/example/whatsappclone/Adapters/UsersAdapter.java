package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Activites.ChatsActivity;
import com.example.whatsappclone.R;
import com.example.whatsappclone.Modals.User;
import com.example.whatsappclone.databinding.MainActivityUserShowCellBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHoder>{

    Context context;
    ArrayList<User> users;

    public UsersAdapter(Context context,ArrayList<User> users){
        this.context=context;
        this.users=users;
    }


    @NonNull
    @Override
    public UsersViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_activity_user_show_cell,parent,false);
        return new UsersViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHoder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            String time = snapshot.child("lastMsgTIme").getValue(String.class);
                            holder.binding.ulastmsg.setText(lastMsg);
                            holder.binding.ulastmsgtime.setText(time);
                        }else {
                            holder.binding.ulastmsg.setText("Tap To Chat");
                            holder.binding.ulastmsgtime.setText("00:00 AM");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.uprofilename.setText(user.getName());
        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.uprofileimage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatsActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("profilePic",user.getProfileImage());
                intent.putExtra("uid",user.getUid());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHoder extends RecyclerView.ViewHolder{

        MainActivityUserShowCellBinding binding;
        public UsersViewHoder(@NonNull View itemView) {
            super(itemView);
            binding = MainActivityUserShowCellBinding.bind(itemView);


        }
    }
}
