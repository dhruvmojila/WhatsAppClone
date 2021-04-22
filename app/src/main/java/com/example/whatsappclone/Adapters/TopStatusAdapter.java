package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Activites.MainActivity;
import com.example.whatsappclone.Modals.Status;
import com.example.whatsappclone.Modals.UserStatus;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ItemStatusBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusAdapterViewHolder>{

    Context context;
    ArrayList<UserStatus> userStatuses;

    public TopStatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public TopStatusAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status,parent,false);
        return new TopStatusAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusAdapterViewHolder holder, int position) {

        UserStatus userStatus = userStatuses.get(position);
        Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size()-1);
        Glide.with(context).load(lastStatus.getImageUrl())
                .into(holder.binding.imageStatus);

        if (userStatus.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
        {
            holder.binding.uStatusName.setText("Your Status");
        }else {
            holder.binding.uStatusName.setText(userStatus.getName());
        }

        holder.binding.status.setPortionsCount(userStatus.getStatuses().size());

        holder.binding.status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<MyStory> myStories = new ArrayList<>();
                for (Status status:userStatus.getStatuses()){
                    myStories.add(new MyStory(status.getImageUrl()));
                }
                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories)
                        .setStoryDuration(5000)
                        .setTitleText(userStatus.getName())
                        .setSubtitleText("")
                        .setTitleLogoUrl(userStatus.getProfileImage())
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {

                            }

                            @Override
                            public void onTitleIconClickListener(int position) {

                            }
                        })
                        .build()
                        .show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class TopStatusAdapterViewHolder extends RecyclerView.ViewHolder{

        ItemStatusBinding binding;
        public TopStatusAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }
}
