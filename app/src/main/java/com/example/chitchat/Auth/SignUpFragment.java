package com.example.chitchat.Auth;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chitchat.MainActivity;
import com.example.chitchat.R;
import com.example.chitchat.Tools.Check;
import com.example.chitchat.Tools.Constants;
import com.example.chitchat.Tools.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;


public class SignUpFragment extends Fragment {

    private PreferenceManager preferenceManager;
    String encodeImage;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Check check  =  new Check();
        EditText edt_fullname,edt_email,edt_dateOfBirth, edt_password, edt_confirmPassword,edt_phoneNumber;
        TextView btn_SignUp;
        FrameLayout frameLayout_image = v.findViewById(R.id.frl_user_image);
        ProgressBar progressBar = v.findViewById(R.id.prog_bar);



        edt_fullname= v.findViewById(R.id.edt_fullname);
        edt_email= v.findViewById(R.id.edt_email);
        edt_dateOfBirth= v.findViewById(R.id.edt_date_of_birth);
        edt_password= v.findViewById(R.id.edt_passowrd);
        edt_confirmPassword= v.findViewById(R.id.edt_confirmpassowrd);
        edt_phoneNumber= v.findViewById(R.id.edt_phone_number);
        btn_SignUp= v.findViewById(R.id.txt_signup);

        CountryCodePicker countryCodePicker = v.findViewById(R.id.countryCode_picker);

        preferenceManager = new PreferenceManager(getContext().getApplicationContext());

        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((
                                check.checkFiled(edt_fullname)&&
                                check.checkFiled(edt_email)&&
                                check.checkFiled(edt_dateOfBirth)&&
                                check.checkFiled(edt_password)&&
                                check.checkFiled(edt_confirmPassword)&&
                                check.checkEmail(edt_email)&&
                                check.checkPassword(edt_password)&&
                                check.checkComfirmPassword(edt_password,edt_confirmPassword)
                )==true
                ){
                    progressBar.setVisibility(View.VISIBLE);

                    FirebaseFirestore database =FirebaseFirestore.getInstance();
                    HashMap<String,Object> user = new HashMap<>();
                    user.put(Constants.KEY_IMAGE,encodeImage);
                    user.put(Constants.KEY_NAME,edt_fullname.getText().toString().trim());
                    user.put(Constants.KEY_EMAIL,edt_email.getText().toString().trim());
                    user.put(Constants.KEY_DATE_OF_BIRTH,edt_dateOfBirth.getText().toString().trim());
                    user.put(Constants.KEY_PHONE_NUMBER,countryCodePicker.getSelectedCountryCodeWithPlus().toString()+edt_phoneNumber.getText().toString());
                    user.put(Constants.KEY_PASSWORD,edt_password.getText().toString().trim());

                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .add(user)
                            .addOnSuccessListener(command -> {
                                Toast.makeText(getContext().getApplicationContext(), "Welcome "+edt_fullname.getText().toString(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                preferenceManager.putString(Constants.KEY_USER_ID,command.getId());
                                preferenceManager.putString(Constants.KEY_NAME, edt_fullname.getText().toString());
                                preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
                                Intent intent= new Intent(getContext().getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext().getApplicationContext(), "Error while inserting", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            });

                }
            }
        });

        frameLayout_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });

         return  v;
    }

    private String encodeImage(Bitmap bitmap){
      int prevWidth=150;
      int prevHeight=bitmap.getHeight() * prevWidth/bitmap.getWidth();

      Bitmap prevBitmap= Bitmap.createScaledBitmap(bitmap,prevWidth,prevHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        prevBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte [] bytes= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    };

    private final ActivityResultLauncher<Intent> pickImage= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),result -> {
                if(result.getResultCode()== RESULT_OK){
                    if(result.getData()!=null){
                        Uri imageUri = result.getData().getData();
                        try{
                            ImageView img_profile= getView().findViewById(R.id.imgv_profile);
                            TextView txt_add_photo= getView().findViewById(R.id.txt_add_photo);

                            InputStream inputStream= getContext().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            img_profile.setImageBitmap(bitmap);
                            txt_add_photo.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                                e.printStackTrace();
                        }
                    }
                }
            }
    );
}