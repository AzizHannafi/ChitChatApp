package com.example.chitchat.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Models.ChatMessage;
import com.example.chitchat.Models.User;
import com.example.chitchat.R;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.fragments.ChatFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class RecentConversationAdapter  extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>{
    public static Context context;
    private final List<ChatMessage> chatMessageList;
    public FragmentManager fragmentManager;
    Bundle bundle = new Bundle();

    Fragment chatfragment = new ChatFragment();
    public RecentConversationAdapter(List<ChatMessage> chatMessageList,Context context,FragmentManager fragmentManager) {
        this.chatMessageList = chatMessageList;
        this.context= context ;
        this.fragmentManager= fragmentManager;
    }

    public static Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentConversationAdapter.ConversationViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_container_recent_conversion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessageList.get(position);


        String txt_name = chatMessage.getConversionName();
        String txt_recent_message = chatMessage.getMessage();
        String conversation_image = chatMessage.getConversionImage();


        holder.txtv_name.setText(txt_name);
        holder.imgv_user.setImageBitmap(getUserImage(conversation_image));
        holder.txt_recent_message.setText(txt_recent_message);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User user= new User();
                user.name=chatMessage.conversionName;
                user.image=chatMessage.conversionImage;
                user.id=chatMessage.conversionId;
                bundle.putSerializable(Constants.KEY_USER,user);
                chatfragment.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.frl_home, chatfragment).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
        FloatingActionButton FloatingActionButton;
    ImageView imgv_user;
    TextView txtv_name, txt_recent_message;
    public ConversationViewHolder(@NonNull View itemView) {
        super(itemView);

        imgv_user = itemView.findViewById(R.id.mini_image_profile);
        txtv_name = itemView.findViewById(R.id.txtv_name);
        txt_recent_message = itemView.findViewById(R.id.textRecentMessage);
    }


}

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
