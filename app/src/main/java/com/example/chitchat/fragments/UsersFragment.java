package com.example.chitchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chitchat.Adapters.UserAdapter;

import com.example.chitchat.MainActivity;
import com.example.chitchat.Models.User;
import com.example.chitchat.R;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.Tools.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    ProgressBar prog_bar;
    private PreferenceManager preferenceManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentManager fragmentmanager;
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        preferenceManager = new PreferenceManager(getContext().getApplicationContext());
        ImageView imgv_btn_back = v.findViewById(R.id.imgv_btn_back);

        prog_bar = v.findViewById(R.id.prog_bar);

        RecyclerView user_recycler_view = v.findViewById(R.id.user_recycler_view);
        imgv_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });


        getUsers( user_recycler_view);
        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getUsers(RecyclerView user_recycler_view) {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {

                    prog_bar.setVisibility(View.VISIBLE);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                            user.setEmail(queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                            user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setToken(queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            user.setId(queryDocumentSnapshot.getId());
                            users.add(user);

                        }
                        if (users.size() > 0) {

                            UserAdapter userAdapter = new UserAdapter(users, getContext().getApplicationContext(), getParentFragmentManager());
                            user_recycler_view.setAdapter(userAdapter);
                            user_recycler_view.setVisibility(View.VISIBLE);
                            prog_bar.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(getContext().getApplicationContext(), "Error in retreiving user", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext().getApplicationContext(), "No users found", Toast.LENGTH_SHORT).show();

                    }
                });
    }
}
