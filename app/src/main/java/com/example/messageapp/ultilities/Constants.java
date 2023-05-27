package com.example.messageapp.ultilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS="users";
    public static final String KEY_NAME="name";
    public static final String KEY_EMAIL="email";
    public static final String KEY_PASSWORD="password";
    public static final String KEY_PREFERENCE_NAME="chatAppPreference";
    public static final String KEY_IS_SIGNED_IN="isSignedIn";
    public static final String KEY_USER_ID="userId";
    public static final String KEY_IMAGE="image";
    public static final String KEY_FCM_TOKEN= "fcmToken";
    public static final String KEY_USER="user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "sender_id";
    public static final String KEY_RECEIVER_ID = "receiver_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conservations";
    public static final String KEY_SENDER_NAME = "sender_name";
    public static final String KEY_RECEIVER_NAME = "receiver_name";
    public static final String KEY_SENDER_IMAGE = "sender_image";
    public static final String KEY_RECEIVER_IMAGE = "receiver_image";
    public static final String KEY_LAST_MESSAGE = "last_message";
    public static final String KEY_SEEN_MESSAGE_ID = "seen_message";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String KEY_NOTIFICATION_ACCEPT = "notification_accept";
    public static HashMap<String,String> remoteMsgHeader=null;
    public static HashMap<String,String> getRemoteMsgHeader()
    {
        if(remoteMsgHeader==null)
        {
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(REMOTE_MSG_AUTHORIZATION,"key=AAAAxTC8KEY:APA91bH_hTpq1akXG8hqt0t3QqabOeE7GMJ3dB2fegJ5mZskttU9VCHy6GOJecmHMopzgNXmRGXczF11xIbjj_M4u10zml0f5lf-xwZjMrs8nNvg43Iagb0Xl260rrZpKCqzKAt2ak4B");
            remoteMsgHeader.put(REMOTE_MSG_CONTENT_TYPE,"application/json");
        }
        return remoteMsgHeader;
    }

}
