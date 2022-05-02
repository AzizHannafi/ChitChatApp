package com.example.chitchat.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import com.example.chitchat.Models.User;
import com.example.chitchat.R;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.fragments.ChatFragment;
import com.example.chitchat.fragments.UsersFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.userViewHolder> {
    Fragment chatfragment = new ChatFragment();
    Bundle bundle = new Bundle();

    public final List<User> users;
    public static Context context;
   // private  final UserListener userListener;
    public  FragmentManager fragmentManager;

    public UserAdapter(List<User> users, Context context,FragmentManager fragmentManager) {
        this.users = users;
        this.context = context;
        this.fragmentManager = fragmentManager;


    }

    public static Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new userViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_container_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder holder, int position) {
        User user = users.get(position);


        String name = user.getName();
        String image = user.getImage();
        String email = user.getEmail();


        holder.txtv_name.setText(name);
        holder.imgv_user.setImageBitmap(getUserImage(image));
        holder.txtv_email.setText(email);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putSerializable(Constants.KEY_USER,user);
                chatfragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.frl_users_list, chatfragment).commit();
            }
        });


    }

    @Override
    public int getItemCount() {
        return users.size() ;
    }

    class userViewHolder extends RecyclerView.ViewHolder {
        ImageView imgv_user;
        TextView txtv_name, txtv_email;

        public userViewHolder(@NonNull View itemView) {
            super(itemView);


            imgv_user = itemView.findViewById(R.id.mini_image_profile);
            txtv_name = itemView.findViewById(R.id.txtv_name);
            txtv_email = itemView.findViewById(R.id.textEmail);




        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
