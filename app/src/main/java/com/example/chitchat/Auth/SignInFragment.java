package com.example.chitchat.Auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chitchat.MainActivity;
import com.example.chitchat.R;
import com.example.chitchat.Tools.Check;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.Tools.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class SignInFragment extends Fragment {

    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
        TextView edt_CreateOne = v.findViewById(R.id.txt_createOne);
        EditText edt_email = v.findViewById(R.id.edt_email);
        EditText edt_password = v.findViewById(R.id.edt_passowrd);
        TextView btn_signIn = v.findViewById(R.id.txt_signin);
        TextView txt_forgetPaswword = v.findViewById(R.id.txt_forget_password);
        ProgressBar prog_bar = v.findViewById(R.id.prog_bar);


        preferenceManager = new PreferenceManager(getContext().getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        Check check = new Check();

        edt_CreateOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment signUpFragment = new SignUpFragment();
                getFragmentManager().beginTransaction().replace(R.id.frl_authentication, signUpFragment).commit();
            }
        });

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((
                        check.checkFiled(edt_email) && check.checkFiled(edt_password) && check.checkEmail(edt_email) && check.checkPassword(edt_password)

                ) == true
                ) {
                    prog_bar.setVisibility(View.VISIBLE);
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo(Constants.KEY_EMAIL, edt_email.getText().toString())
                            .whereEqualTo(Constants.KEY_PASSWORD, edt_password.getText().toString())
                            .get()
                            .addOnCompleteListener(command -> {
                                if (command.isSuccessful() && command.getResult() != null && command.getResult().getDocuments().size() > 0) {
                                    DocumentSnapshot documentSnapshot = command.getResult().getDocuments().get(0);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                    Toast.makeText(getContext().getApplicationContext(), "Welcome Back " + documentSnapshot.getString(Constants.KEY_NAME), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    prog_bar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getContext().getApplicationContext(), "Wrong Email or password", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        return v;
    }


}