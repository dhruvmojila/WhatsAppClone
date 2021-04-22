package com.example.whatsappclone.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Modals.Message;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.ItemreceiveBinding;
import com.example.whatsappclone.databinding.ItemsendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.datatransport.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import kotlin.reflect.KVisibility;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;
    boolean emotionSelected = false;

    public MessagesAdapter(Context context,ArrayList<Message> messages,String senderRoom, String receiverRoom){
        this.context = context;
        this.messages = messages;
        this.receiverRoom = receiverRoom;
        this.senderRoom = senderRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.itemsend,parent,false);
            return new SentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.itemreceive,parent,false);
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }else {
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);
        int reaction [] = new int[]{
                R.drawable.wink,
                R.drawable.happy,
                R.drawable.shocked,
                R.drawable.sad2,
                R.drawable.sad,
                R.drawable.laughing,
                R.drawable.inlove,
                R.drawable.delete
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reaction).build();

        ReactionPopup popup = new ReactionPopup(context,config, (pos) -> {
            if (pos>=0 && pos<=6){
                if (holder.getClass() == SentViewHolder.class){
                    SentViewHolder viewHolder = (SentViewHolder)holder;
                    viewHolder.binding.emotionsend.setImageResource(reaction[pos]);
                    viewHolder.binding.emotionsend.setVisibility(View.VISIBLE);
                }else {
                    ReceiveViewHolder viewHolder = (ReceiveViewHolder)holder;
                    viewHolder.binding.emotionreceive.setImageResource(reaction[pos]);
                    viewHolder.binding.emotionreceive.setVisibility(View.VISIBLE);
                }
                message.setEmotion(pos);
                FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom)
                        .child("message").child(message.getMessageId()).setValue(message);
                FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom)
                        .child("message").child(message.getMessageId()).setValue(message);
            }
            else if (pos==7){
                new AlertDialog.Builder(context)
                        .setIcon(R.drawable.delete)
                        .setTitle("Delete !!")
                        .setMessage("Delete Message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                message.setMessage("These Message was deleted.");
                                message.setEmotion(-1);
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom)
                                        .child("message").child(message.getMessageId()).setValue(message);
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom)
                                        .child("message").child(message.getMessageId()).setValue(message);
                                Toast.makeText(context, "message deleted successfully.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
           return true;
        });

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if (message.getMessage().equals("photo")){
                viewHolder.binding.imagesend.setVisibility(View.VISIBLE);
                viewHolder.binding.sendertext.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUri())
                        .placeholder(R.drawable.ic_baseline_image_24).into(viewHolder.binding.imagesend);
            }

            viewHolder.binding.sendertext.setText(message.getMessage());

            if (message.getEmotion()>=0){
//                message.setEmotion(reaction[message.getEmotion()]);
                viewHolder.binding.emotionsend.setImageResource(reaction[message.getEmotion()]);
                viewHolder.binding.emotionsend.setVisibility(View.VISIBLE);
            }else {
                viewHolder.binding.emotionsend.setVisibility(View.GONE);
            }

            viewHolder.binding.sendertext.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (popup.isShowing()){
                        popup.dismiss();
                    }
                    else {
                        popup.onTouch(view,motionEvent);
                    }
                    return false;
                }
            });
            viewHolder.binding.imagesend.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (popup.isShowing()){
                        popup.dismiss();
                    }
                    else {
                        popup.onTouch(view,motionEvent);
                    }
                    return false;
                }
            });
        }else {
            ReceiveViewHolder viewHolder = (ReceiveViewHolder)holder;

            if (message.getMessage().equals("photo")){
                viewHolder.binding.imagereceive.setVisibility(View.VISIBLE);
                viewHolder.binding.receviertext.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUri())
                        .placeholder(R.drawable.ic_baseline_image_24).into(viewHolder.binding.imagereceive);
            }

            viewHolder.binding.receviertext.setText(message.getMessage());

            if (message.getEmotion()>=0){
//                message.setEmotion(reaction[message.getEmotion()]);
                viewHolder.binding.emotionreceive.setImageResource(reaction[message.getEmotion()]);
                viewHolder.binding.emotionreceive.setVisibility(View.VISIBLE);
            }else {
                viewHolder.binding.emotionreceive.setVisibility(View.GONE);
            }

            viewHolder.binding.receviertext.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (popup.isShowing()){
                        popup.dismiss();
                    }
                    else {
                        popup.onTouch(view,motionEvent);
                    }
                    return false;
                }
            });
            viewHolder.binding.imagereceive.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (popup.isShowing()){
                        popup.dismiss();
                    }
                    else {
                        popup.onTouch(view,motionEvent);
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        ItemsendBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemsendBinding.bind(itemView);
        }
    }
    public class ReceiveViewHolder extends RecyclerView.ViewHolder{

        ItemreceiveBinding binding;
        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemreceiveBinding.bind(itemView);
        }
    }
}
