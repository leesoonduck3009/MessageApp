package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Field;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityLoginBinding;
import com.example.myapplication.ultilities.Constants;
import com.example.myapplication.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        AutoLogin();
        setListener();
    }
    private void AutoLogin()
    {
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    private void setListener()
    {
        binding.LogintextViewRegister.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
        });

        binding.buttonLogin.setOnClickListener(v->{if(this.isValidLogin()) {
            Login();
        }
        });
    }
    private void Login()
    {
        Loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL,binding.LogininputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,encodePassword(binding.LogininputPassword.getText().toString())).get().addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult() != null && task.getResult().getDocuments().size()>0)
                    {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        Loading(false);
                        showToast("Unable to login");
                    }

                });
    }
    private void Loading(boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonLogin.setVisibility(View.INVISIBLE);
            binding.LoginProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.LoginProgressBar.setVisibility(View.INVISIBLE);
            binding.buttonLogin.setVisibility(View.VISIBLE);

        }
    }
    private boolean isValidLogin()
    {
        if(binding.LogininputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Enter email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.LogininputEmail.getText().toString()).matches())
        {
            showToast("Invalid email");
            return false;
        }
        else if(binding.LogininputPassword.getText().toString().trim().isEmpty())
        {
            showToast("Enter password");
            return false;
        }
        return true;
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
}