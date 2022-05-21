package com.example.chitchat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Adapters.ChatAdapter;
import com.example.chitchat.MainActivity;
import com.example.chitchat.Models.ChatMessage;
import com.example.chitchat.Models.User;
import com.example.chitchat.R;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.Tools.PreferenceManager;
import com.example.chitchat.fierbase.FCMSend;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.N)
public class ChatFragment extends Fragment {

    private String id = "";
    private TextView txtv_name;
    private EditText edt_inputMessage;
    private ImageView btn_send;
    private ProgressBar prog_bar;
    private ImageView userImage;
    private TextView txt_online;
    private TextView txt_offline;

    private User reciveUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    public String conversationId = null;

    private RecyclerView recyclerViewChat;
    private Boolean isRecieverAvailable = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        txtv_name = v.findViewById(R.id.txtv_name);
        userImage = v.findViewById(R.id.mini_image_profile);
        recyclerViewChat = v.findViewById(R.id.chat_recycler_view);
        edt_inputMessage = v.findViewById(R.id.edt_inputMessage);
        ImageView imgv_btn_back = v.findViewById(R.id.imgv_btn_back);

        txt_online = v.findViewById(R.id.txtv_availability);
        txt_offline = v.findViewById(R.id.txtv_offline);

        btn_send = v.findViewById(R.id.btn_send);
        prog_bar = v.findViewById(R.id.prog_bar);

        imgv_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        loadRecieveDetails(txtv_name);
        init();
        listenMessage();
        getRecieverFclToken();
        System.out.println("reciver token :"+reciveUser.token);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });


        return v;
    }


    private void init() {
        preferenceManager = new PreferenceManager(getContext().getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapEncoderString(reciveUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID),
                getContext().getApplicationContext()
        );
        recyclerViewChat.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }

    @Override
    public void onResume() {
        super.onResume();
        listenAvailabilityOfReciever(txt_online, txt_offline);
    }

    private void listenMessage() {

        database.collection(Constants.KEY_COLLECTION_CAHT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, reciveUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CAHT)
                .whereEqualTo(Constants.KEY_SENDER_ID, reciveUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private  void getRecieverFclToken (){
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(String.valueOf(reciveUser.id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (task.isSuccessful()) {
                    reciveUser.token  = document.getString(Constants.KEY_FCM_TOKEN);
                }

                }
        });
    }

    private void listenAvailabilityOfReciever(TextView txt_online, TextView txt_offline) {

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(String.valueOf(reciveUser.id)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (task.isSuccessful()) {

                    //System.out.println("doc :"+document.getString(Constants.KEY_AVAILABILITY));
                    if (document.getString(Constants.KEY_AVAILABILITY).equals("1")) {
                        isRecieverAvailable = true;
                        System.out.println("isRecieverAvailable :" + isRecieverAvailable);
                        reciveUser.token = document.getString(Constants.KEY_FCM_TOKEN);

                        txt_online.setVisibility(View.VISIBLE);
                        txt_offline.setVisibility(View.GONE);
                    } else {
                        isRecieverAvailable = false;
                        txt_online.setVisibility(View.GONE);
                        txt_offline.setVisibility(View.VISIBLE);
                        System.out.println("isRecieverAvailable :" + isRecieverAvailable);
                    }
                    ;
                }
                ;

            }

        });
    }

    private void sendMessage() {

        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, reciveUser.id);
        message.put(Constants.KEY_MESSAGE, edt_inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMPS, new Date());
        database.collection(Constants.KEY_COLLECTION_CAHT).add(message);


        if (conversationId != null) {
            updateConversation(edt_inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECIEVRE_ID, reciveUser.id);
            conversation.put(Constants.KEY_RECIEVRE_NAME, reciveUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, reciveUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, edt_inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMPS, new Date());
            addConversation(conversation);
        }


        if(isRecieverAvailable== false){
            FCMSend.pushNotification( getContext().getApplicationContext()
                    ,reciveUser.token
                    ,preferenceManager.getString(Constants.KEY_NAME)
                    ,edt_inputMessage.getText().toString()

            ) ;

        }
        edt_inputMessage.setText(null);
    }


    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        } else {

            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.recieiveriD = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadbleDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMPS));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMPS);

                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (o1, o2) -> o1.dateObject.compareTo(o2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                recyclerViewChat.smoothScrollToPosition(chatMessages.size() - 1);
            }
            recyclerViewChat.setVisibility(View.VISIBLE);

        }
        if (conversationId == null) {
            checkForConversation();
        }
        prog_bar.setVisibility(View.GONE);


    };


    private void loadRecieveDetails(TextView txtv_name) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            reciveUser = (User) bundle.getSerializable(Constants.KEY_USER);
            txtv_name.setText(reciveUser.name);
            userImage.setImageBitmap(getUserImage(reciveUser.image));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getReadbleDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(getConversationId(reciveUser.id, preferenceManager.getString(Constants.KEY_USER_ID)));
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMPS, new Date()

        );
    }

    private void checkForConversation() {


        if (chatMessages.size() != 0) {
            setConversationId(getConversationId(reciveUser.id, preferenceManager.getString(Constants.KEY_USER_ID)));

        }


    }

    ;

    private String getConversationId(String sender, String receiver) {

        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, sender)
                .whereEqualTo(Constants.KEY_RECIEVRE_ID, receiver)
                .get()
                .addOnCompleteListener(command -> {
                            if (command.isSuccessful() && command.getResult() != null && command.getResult().getDocuments().size() > 0) {
                                DocumentSnapshot documentSnapshot = command.getResult().getDocuments().get(0);
                                id = documentSnapshot.getId();

                            } else {

                                database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                        .whereEqualTo(Constants.KEY_SENDER_ID, receiver)
                                        .whereEqualTo(Constants.KEY_RECIEVRE_ID, sender)
                                        .get()
                                        .addOnCompleteListener(command2 -> {
                                            if (command2.isSuccessful() && command2.getResult() != null && command2.getResult().getDocuments().size() > 0) {
                                                DocumentSnapshot documentSnapshot = command2.getResult().getDocuments().get(0);
                                                id = documentSnapshot.getId();

                                            } else {
                                                id = "null";
                                                System.out.println("conversation not found");
                                            }
                                        });
                            }
                        }
                );

        return id;


    }

    ;

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private Bitmap getBitmapEncoderString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    ;
}