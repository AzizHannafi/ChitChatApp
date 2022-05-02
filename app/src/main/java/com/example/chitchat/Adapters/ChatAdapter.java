package com.example.chitchat.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Models.ChatMessage;
import com.example.chitchat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final List<ChatMessage> chatMessages;
    public static Context context;
    public final Bitmap recieveProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECIEIVE=2;

    public static Context getContext() {
        return context;
    }
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap recieveProfileImage, String senderId,Context context) {
        this.chatMessages = chatMessages;
        this.recieveProfileImage = recieveProfileImage;
        this.senderId = senderId;
        this.context= context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new ChatAdapter.SentMessageViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_container_sent_message, parent, false));
        }else {
            return new ChatAdapter.ReciveMessageViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_conatiner_recieve_message, parent, false));

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).txtv_message.setText(chatMessages.get(position).message);
            ((SentMessageViewHolder) holder).txtv_date_time.setText(chatMessages.get(position).dateTime);
        }else {
            ((ReciveMessageViewHolder) holder).setData(chatMessages.get(position),recieveProfileImage);


        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECIEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ReciveMessageViewHolder   extends RecyclerView.ViewHolder{
        TextView txtv_message,txtv_date_time;
        ImageView imageViewProfile;

        public ReciveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtv_message= itemView.findViewById(R.id.txtv_message);
            txtv_date_time= itemView.findViewById(R.id.txtv_date_time);
            imageViewProfile = itemView.findViewById(R.id.imgv_profile);
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            txtv_message.setText(chatMessage.message);
            txtv_date_time.setText(chatMessage.dateTime);
            imageViewProfile.setImageBitmap(receiverProfileImage);

        }
    }

    class SentMessageViewHolder   extends RecyclerView.ViewHolder{
        TextView txtv_message,txtv_date_time;


        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtv_message= itemView.findViewById(R.id.txtv_message);
            txtv_date_time= itemView.findViewById(R.id.txtv_date_time);
        }
    }


}
