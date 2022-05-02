package com.example.chitchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Adapters.RecentConversationAdapter;
import com.example.chitchat.Auth.AuthenticationActivity;
import com.example.chitchat.Models.ChatMessage;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.Tools.PreferenceManager;
import com.example.chitchat.fragments.UsersFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity {


    public TextView txtv_name;
    public ImageView img_profile;
    public ImageView img_sign_out;
    private RecyclerView conversationRecyclerView;
    private ProgressBar prog_bar;
    private ConstraintLayout constraintLayoutHandler;


    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());
        img_sign_out = findViewById(R.id.imgv_sign_out);
        conversationRecyclerView = findViewById(R.id.conversation_recycler_view);
        constraintLayoutHandler= findViewById(R.id.constarine_handler);
        FloatingActionButton floatingActionButton = findViewById(R.id.fabNewChat);
        prog_bar = findViewById(R.id.prog_bar);

        init();
        loadUserDetails();
        listenConversation();


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionButton.setVisibility(View.GONE);
                constraintLayoutHandler.setVisibility(View.GONE);
                Fragment userFragment = new UsersFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.frl_home, userFragment).commit();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, getApplicationContext(), getSupportFragmentManager());
        conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }


    public void loadUserDetails() {
        img_profile = findViewById(R.id.image_user_profile);
        txtv_name = findViewById(R.id.txtv_name);
        txtv_name.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        img_profile.setImageBitmap(bitmap);
        getToken();

        img_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });



    }

    private void listenConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo("recieveriD", preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString("recieveriD");

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId=senderId;
                    chatMessage.recieiveriD=receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_RECIEVRE_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString("recieveriD");
                    } else {
                        chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }

                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMPS));


                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString("recieveriD");

                  for (int i = 0; i < conversations.size(); i++) {


                      if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).recieiveriD.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMPS);
                            break;
                        }
                    }
                }

            }
            Collections.sort(conversations, (o1, o2) -> o2.dateObject.compareTo(o1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            conversationRecyclerView.smoothScrollToPosition(0);
            conversationRecyclerView.setVisibility(View.VISIBLE);
            prog_bar.setVisibility(View.GONE);
        }

    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    public void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> {

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Unable Token updated ", Toast.LENGTH_SHORT).show();
                });
    }

    public void signOut() {
        Toast.makeText(getApplicationContext(), "Hope to see you again soon ...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), AuthenticationActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Unabale to sign out", Toast.LENGTH_SHORT).show();
                });
    }


}