package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.Adapter.UsersAdapter;
import com.example.myapplication.Models.User;
import com.example.myapplication.databinding.ActivityUsersBinding;
import com.example.myapplication.listener.UserListener;
import com.example.myapplication.ultilities.Constants;
import com.example.myapplication.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
        getUsers();
    }
    private void setListener()
    {
        binding.imageBack.setOnClickListener(v->onBackPressed());
    }
    private void getUsers()
    {
        isLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    isLoading(false);
                    String currentUserId= preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful()&& task.getResult()!=null)
                    {
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                        {
                            if(currentUserId.equals(queryDocumentSnapshot.getId()))
                            {
                                continue;
                            }
                            User user = new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email=queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image=queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size()>0)
                        {
                            UsersAdapter usersAdapter = new UsersAdapter(users,this);
                            binding.userRecyclerView.setAdapter(usersAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage()
    {
        binding.textError.setText(String.format("%s","No user available"));
        binding.textError.setVisibility(View.VISIBLE);
    }
    private void isLoading(boolean isLoading)
    {
        if(isLoading)
        {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);

        }
    }
    @Override
    public void onUserClicked(User user) // Click vaof trong hinh anh nguoi dung
    {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        Toast.makeText(getApplicationContext(),user.name,Toast.LENGTH_SHORT).show();
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}