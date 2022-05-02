package com.example.chitchat.Tools;

import android.widget.EditText;

public class Check {

    public Check (){};

    public boolean checkEmail(EditText email){
        if(!email.getText().toString().contains("@")){
            email.setError("Invalid email !!!");
            return false;
        }else {
            return true;
        }

    }
    public boolean checkPassword(EditText password){
        if (password.getText().toString().trim().length()<7){
            password.setError("At least seven characters");
            return false;
        }else {
            return true;
        }
    }
    public boolean checkFiled(EditText edt){
        if(edt.getText().toString().isEmpty()){
            edt.setError("This field is Empty !!!");
            return false;
        }else {
            return true;
        }
    }

    public boolean checkComfirmPassword(EditText password, EditText confirmPassword){
        if(!(password.getText().toString().trim().equals(confirmPassword.getText().toString().trim()))){
            confirmPassword.setError("Password doesn't much");
            return false;
        }else {
            return true;
        }
    }
}
