package com.example.messageapp.activities;

import androidx.annotation.NonNull;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.messageapp.Adapter.ChatAdapter;
import com.example.messageapp.Models.ChatMessage;
import com.example.messageapp.Models.User;
import com.example.messageapp.R;
import com.example.messageapp.databinding.ActivityChatBinding;
import com.example.messageapp.network.ApiClient;
import com.example.messageapp.network.ApiService;
import com.example.messageapp.ultilities.Constants;
import com.example.messageapp.ultilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receivedUser;
    private List<ChatMessage> chatMessageList;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private String conversationId=null;
    private FirebaseMessaging firebaseMessaging;
    private FirebaseFirestore db;
    private boolean isReceivedAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        firebaseMessaging=FirebaseMessaging.getInstance();
        setContentView(binding.getRoot());
        setListener();
        LoadReceivedDetail();
        initChatConversation();
        listenMessages();
    }
    private void initChatConversation()
    {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter=new ChatAdapter(getBitMapFromString(receivedUser.image),chatMessageList,
                preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();

    }
    private void sendNotification(String deviceToken, String message) {
        firebaseMessaging.send(new RemoteMessage.Builder(deviceToken + "@gcm.googleapis.com")
                .setMessageId(String.valueOf(System.currentTimeMillis()))
                .setData(Collections.singletonMap("message", message))
                .build());
    }
    private void sendMessage()
    {
        HashMap<String, Object> messages = new HashMap<>();
        messages.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        messages.put(Constants.KEY_RECEIVER_ID,receivedUser.id);
        messages.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        messages.put(Constants.KEY_TIMESTAMP,new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(messages);
        if(conversationId!=null)
        {
            UpdateConversation(binding.inputMessage.getText().toString());
        }
        else
        {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID,receivedUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME,receivedUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE,receivedUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_SEEN_MESSAGE_ID,receivedUser.id);
            conversation.put(Constants.KEY_TIMESTAMP,new Date());
            AddConversation(conversation);
        }
        if(!isReceivedAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receivedUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());

            }
            catch (Exception ex)
            {
                showToast(ex.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private void sendNotification(String messageBody)
    {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeader(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful())
                {
                    try{
                        if(response.body()!=null)
                        {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure")==1)
                            {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException ex)
                    {
                        ex.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }
                else {
                    showToast("ERROR: " +  response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
    private void listenAvailabilityOfReceiver()
    {
        db.collection(Constants.KEY_COLLECTION_USERS).document(receivedUser.id)
                .addSnapshotListener(ChatActivity.this,((value, error) -> {
                    if(error!=null)
                    {
                        return;
                    }
                    if(value!=null)
                    {
                        if(value.getLong(Constants.KEY_AVAILABILITY)!=null)
                        {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceivedAvailable= availability==1;
                        }
                        receivedUser.token=value.getString(Constants.KEY_FCM_TOKEN);
                        if(receivedUser.image==null)
                        {
                            receivedUser.image= value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceivedProfileImage(getBitMapFromString(receivedUser.image));
                            chatAdapter.notifyItemRangeChanged(0,chatMessageList.size());
                        }
                        if(isReceivedAvailable)
                        {
                            binding.textAvailability.setVisibility(View.VISIBLE);
                        }else{
                            binding.textAvailability.setVisibility(View.GONE);
                        }
                    }
                }));
    }
    private void listenMessages()
    {
        db.collection(Constants.KEY_COLLECTION_CHAT)// Lấy message từ phía user sender
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receivedUser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT)// Lấy message từ phía bên kia
                .whereEqualTo(Constants.KEY_SENDER_ID,receivedUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }
    private final EventListener<QuerySnapshot> eventListener  = ((value, error) -> // Check realtime chat
    {
       if(error!=null)
       {
           return;
       }
       if(value!=null)
       {
           int count = chatMessageList.size();
           for(DocumentChange documentChange: value.getDocumentChanges())
           {
               if(documentChange.getType()== DocumentChange.Type.ADDED)
               {
                   ChatMessage chatMessage = new ChatMessage();
                   chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                   chatMessage.recieverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                   chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                   chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                   chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                   chatMessageList.add(chatMessage);
               }
           }
           Collections.sort(chatMessageList,(obj1,obj2)->obj1.dateObject.compareTo(obj2.dateObject));
           if(count==0)
           {
               chatAdapter.notifyDataSetChanged();
           }
           else {
               chatAdapter.notifyItemRangeInserted(chatMessageList.size(),chatMessageList.size());
               binding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size()-1);
           }
           binding.chatRecyclerView.setVisibility(View.VISIBLE);
       }
       binding.progressBar.setVisibility(View.GONE);
       if(conversationId==null)
       {
           checkForConversation();
       }
    });
    private Bitmap getBitMapFromString(String encodedImage)
    {
        if(encodedImage!=null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
            return  null;
    }
    private void LoadReceivedDetail()
    {
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receivedUser.name);

    }
    private void setListener()
    {
        binding.imageBack.setOnClickListener(v->onBackPressed());
        binding.layoutSend.setOnClickListener(v->sendMessage());
    }
    private String getReadableDateTime(Date date)
    {
        return new SimpleDateFormat("hh:mm a - dd, MMMM, yyyy  ", Locale.getDefault()).format(date);
    }
    private void checkForConversation()
    {
        if(chatMessageList.size()!=0)
        {
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receivedUser.id);
            checkForConversationRemotely(receivedUser.id,preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }
    private void AddConversation(HashMap<String,Object> conversation)
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(conversation)
                .addOnSuccessListener(documentReference -> conversationId=documentReference.getId());
    }
    private void UpdateConversation(String message)
    {
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,new Date(),Constants.KEY_SEEN_MESSAGE_ID,receivedUser.id
        );
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

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}