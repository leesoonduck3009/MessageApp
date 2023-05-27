package com.example.myapplication.activities;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import com.example.myapplication.ultilities.Constants;
import com.example.myapplication.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private String encoded_image;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListener();
    }
    private void setListener()
    {

        binding.AlreadyHaveAcc.setOnClickListener(v->onBackPressed());
        binding.buttonRes.setOnClickListener(v-> {if(isValidResDetail()) {
            register();
        }});
        binding.ResLayoutImg.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private String encodePassword( String password)
    {
        String encodedPass = null;
        try
        {
            /* MessageDigest instance for MD5. */
            MessageDigest m = MessageDigest.getInstance("MD5");

            /* Add plain-text password bytes to digest using MD5 update() method. */
            m.update(password.getBytes());

            /* Convert the hash value into bytes */
            byte[] bytes = m.digest();

            /* The bytes array has bytes in decimal form. Converting it into hexadecimal format. */
            StringBuilder s = new StringBuilder();
            for (byte aByte : bytes) {
                s.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            /* Complete hashed password in hexadecimal format */
            encodedPass = s.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } try
    {
        /* MessageDigest instance for MD5. */
        MessageDigest m = MessageDigest.getInstance("MD5");

        /* Add plain-text password bytes to digest using MD5 update() method. */
        m.update(password.getBytes());

        /* Convert the hash value into bytes */
        byte[] bytes = m.digest();

        /* The bytes array has bytes in decimal form. Converting it into hexadecimal format. */
        StringBuilder s = new StringBuilder();
        for (byte aByte : bytes) {
            s.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        /* Complete hashed password in hexadecimal format */
        encodedPass = s.toString();
    }
    catch (NoSuchAlgorithmException e)
    {
        e.printStackTrace();
    }

        return encodedPass;
    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private String encode_img(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap preivewBitmap= Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        preivewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage =registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),result->{
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData()!=null)
                    {
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.ResTextViewAddImg.setVisibility(View.GONE);
                            encoded_image=encode_img(bitmap);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }

    );
    private boolean isValidResDetail()
    {
        if(encoded_image==null)
        {
            showToast("Select a picture");
            return false;
        }
        else if(binding.ResinputName.getText().toString().trim().isEmpty())
        {
            showToast("Enter your name");
            return false;
        }
        else if(binding.ResinputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Enter your email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.ResinputEmail.getText().toString()).matches())
        {
            showToast("Enter valid email");
            return false;
        }
        else if(binding.ResinputPassword.getText().toString().trim().isEmpty())
        {
            showToast("Enter your password");
            return false;
        }
        else if(binding.ResinputConfirmPassword.getText().toString().trim().isEmpty())
        {
            showToast("Enter your confirm password");
            return false;
        }
        else if(!binding.ResinputConfirmPassword.getText().toString().equals(binding.ResinputPassword.getText().toString()))
        {
            showToast("Password and confirm password are not same");
            return false;
        }
        return true;
    }
    private void register()
    {

        this.Loading(true);
        FirebaseFirestore db =  FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL,binding.ResinputEmail.getText().toString())
                .get().addOnSuccessListener(task->{
                    if(!(task.getDocuments().size()>0))
                    {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_NAME, binding.ResinputName.getText().toString());
                        user.put(Constants.KEY_EMAIL,binding.ResinputEmail.getText().toString());
                        user.put(Constants.KEY_PASSWORD,encodePassword(binding.ResinputPassword.getText().toString()));
                        user.put(Constants.KEY_IMAGE,this.encoded_image);
                        db.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
                            this.Loading(false);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                            preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                            preferenceManager.putString(Constants.KEY_NAME, binding.ResinputName.getText().toString());
                            preferenceManager.putString(Constants.KEY_IMAGE,this.encoded_image);
                            Intent intend = new Intent(getApplicationContext(),MainActivity.class);
                            intend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intend);
                        }).addOnFailureListener(exception->{
                            Loading(false);
                            showToast(exception.getMessage());
                        });
                    }
                    else {
                        showToast("Email already existed");
                        this.Loading(false
                        );

                    }
                });

    }
    private void Loading(boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonRes.setVisibility(View.INVISIBLE);
            binding.ResprogressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.ResprogressBar.setVisibility(View.INVISIBLE);
            binding.buttonRes.setVisibility(View.VISIBLE);

        }
    }
}