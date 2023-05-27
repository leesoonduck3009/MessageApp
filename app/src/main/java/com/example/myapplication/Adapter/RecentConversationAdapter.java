package com.example.myapplication.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.ChatMessage;
import com.example.myapplication.Models.User;
import com.example.myapplication.databinding.ItemContainerConversationBinding;
import com.example.myapplication.databinding.ItemContainerSendMessageBinding;
import com.example.myapplication.listener.RecentConversationListener;
import com.example.myapplication.ultilities.PreferenceManager;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final RecentConversationListener recentConversationListener;
    private String userId=null;
    public final int HAVE_SEEN_MESSAGE = 1;
    public final int HAVE_NOT_SEEN_MESSAGE = 2;

    public RecentConversationAdapter(List<ChatMessage> chatMessageList,RecentConversationListener recentConversationListener,String userId) {
        this.recentConversationListener=recentConversationListener;
        this.chatMessageList = chatMessageList;
        this.userId=userId;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(ItemContainerConversationBinding
                .inflate(LayoutInflater.from(parent.getContext()),parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {

        holder.setData(chatMessageList.get(position),getItemViewType(position));
    }
/*    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).conversationSeen.equals(userId))
        {
            return HAVE_NOT_SEEN_MESSAGE;
        }
        else
            return HAVE_SEEN_MESSAGE;
    }*/
    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerConversationBinding binding;
        ConversationViewHolder(ItemContainerConversationBinding itemContainerConversationBinding)
        {
            super(itemContainerConversationBinding.getRoot());
            binding=itemContainerConversationBinding;
        }
        void setData(ChatMessage chatMessage,int type)
        {
/*           if(type==HAVE_NOT_SEEN_MESSAGE)
            {
                binding.textName.setTypeface(Typeface.DEFAULT_BOLD);
                binding.textRecentMessage.setTypeface(Typeface.DEFAULT_BOLD);
            }
            else
            {
                binding.textName.setTypeface(Typeface.DEFAULT);
                binding.textRecentMessage.setTypeface(Typeface.DEFAULT);
            }*/
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversationImage));
            binding.textName.setText(chatMessage.conversationName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.id=chatMessage.conversationId;
                user.name = chatMessage.conversationName;
                user.image=chatMessage.conversationImage;
                recentConversationListener.onRecentConversationClicked(user);
            });
        }

    }
    private Bitmap getConversationImage(String encodedImage)
    {
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
