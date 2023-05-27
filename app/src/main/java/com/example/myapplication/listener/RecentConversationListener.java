package com.example.myapplication.listener;

import com.example.myapplication.Models.ChatMessage;
import com.example.myapplication.Models.User;

public interface RecentConversationListener {
    void onRecentConversationClicked(User user);

}
