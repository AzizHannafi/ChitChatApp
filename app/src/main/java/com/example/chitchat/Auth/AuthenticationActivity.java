package com.example.chitchat.Auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.chitchat.R;


public class AuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Fragment signInFragment = new SignInFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.frl_authentication,signInFragment).commit();
    }
}