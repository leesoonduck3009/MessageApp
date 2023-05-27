package com.example.messageapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.messageapp.Adapter.RecentConversationAdapter;
import com.example.messageapp.Models.ChatMessage;
import com.example.messageapp.Models.User;
import com.example.messageapp.databinding.ActivityMainBinding;
import com.example.messageapp.listener.RecentConversationListener;
import com.example.messageapp.ultilities.Constants;
import com.example.messageapp.ultilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements RecentConversationListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private  String conversationId;
    private  String conversationIdMain;

    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        this.loadUserDetail();
        getToken();
        setListener();
        listenConversation();
        CheckNotification();
    }
    private void CheckNotification()
    {
        if(preferenceManager.getString(Constants.KEY_NOTIFICATION_ACCEPT)==null)
            requestNotificationPermission();
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Với Android 8.0 trở lên, hiển thị cửa sổ yêu cầu quyền bật thông báo
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        } else {
            // Với Android 7.1 trở xuống, hiển thị cửa sổ yêu cầu quyền bật thông báo
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
        preferenceManager.putString(Constants.KEY_NOTIFICATION_ACCEPT,"Yes");
    }
    private void listenConversation()
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @Override
    public void onRecentConversationClicked(User user) {
/*        String userMainId = preferenceManager.getString(Constants.KEY_USER_ID);
        checkForConversation(user);
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_SEEN_MESSAGE_ID,"");*/
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        //Toast.makeText(getApplicationContext(),user.name,Toast.LENGTH_SHORT).show();
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);

    }
    private void checkForConversation(User receivedUser)
    {
        if(conversations.size()!=0)
        {
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receivedUser.id);
            checkForConversationRemotely(receivedUser.id,preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }
    private void checkForConversationRemotely(String senderId, String receiverId)
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0)
        {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();

        }
    };

    private final EventListener<QuerySnapshot> eventListener = (((value, error) -> {
        if(error!=null)
        {
            return;
        }
        if(value!=null)
        {
            int count=conversations.size();
            for(DocumentChange documentChange: value.getDocumentChanges())
            {
                if(documentChange.getType()== DocumentChange.Type.ADDED)
                {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage conversation = new ChatMessage();
                    conversation.senderId=senderId;
                    conversation.recieverId=receiverId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId))
                    {
                        conversation.conversationImage=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        conversation.conversationName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        conversation.conversationId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }
                    else{
                        conversation.conversationImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        conversation.conversationName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        conversation.conversationId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    conversation.message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    conversation.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    //conversation.conversationSeen=documentChange.getDocument().getString(Constants.KEY_SEEN_MESSAGE_ID);
                    conversations.add(conversation);
                }
                else if (documentChange.getType()== DocumentChange.Type.MODIFIED)
                {
                    for(int i=0;i<conversations.size();i++)
                    {
                        if(conversations.get(i).senderId.equals(documentChange.getDocument().getString(Constants.KEY_SENDER_ID))&&
                                conversations.get(i).recieverId.equals(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID)))
                        {
                            conversations.get(i).message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            //conversations.get(i).conversationSeen=documentChange.getDocument().getString(Constants.KEY_SEEN_MESSAGE_ID);
                            break;
                        }

                    }

                }
            }
            Collections.sort(conversations,(obj1, obj2)->obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }));
    private void init()
    {
        conversations = new ArrayList<>();
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        conversationAdapter = new RecentConversationAdapter(conversations,this,userId);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        db = FirebaseFirestore.getInstance();
    }
    private void setListener()
    {
        binding.imageSignout.setOnClickListener(v->signOut());
        binding.fabNewChat.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),UsersActivity.class));
        });
    }
    private void loadUserDetail()
    {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] byteImg = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmapImg = BitmapFactory.decodeByteArray(byteImg,0,byteImg.length);
        binding.imageProfile.setImageBitmap(bitmapImg);

    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private  void updateToken(String token)
    {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e->showToast("Unable to update token"));
    }
    private void signOut()
    {
        showToast("Signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        })
                .addOnFailureListener(e-> showToast("Unable to sign out"));
    }

}