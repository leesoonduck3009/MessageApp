package com.example.messageapp.Adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messageapp.Models.ChatMessage;
import com.example.messageapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.messageapp.databinding.ItemContainerSendMessageBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Bitmap receivedProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECEIVED=2;
    public void setReceivedProfileImage(Bitmap bitmap)
    {
        receivedProfileImage = bitmap;
    }
    public ChatAdapter(Bitmap receivedProfileImage, List<ChatMessage> chatMessages, String senderId) {
        this.receivedProfileImage = receivedProfileImage;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT)
        {
            return new SentMessageViewHolder(ItemContainerSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==VIEW_TYPE_SENT)
        {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else
            ((ReceivedMessageViewHolder) holder).setData((chatMessages.get(position)),receivedProfileImage);
    }
    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId))
        {
            return VIEW_TYPE_SENT;
        }
        else
            return  VIEW_TYPE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    static  class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSendMessageBinding binding;
        SentMessageViewHolder(ItemContainerSendMessageBinding itemContainerSendMessageBinding)
        {
            super(itemContainerSendMessageBinding.getRoot());
            binding=itemContainerSendMessageBinding;
        }
        void setData(ChatMessage chatMessage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    static class ReceivedMessageViewHolder extends  RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;
        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding)
        {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding=itemContainerReceivedMessageBinding;
        }
        void setData(ChatMessage chatMessage, Bitmap receivedProfileImage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receivedProfileImage != null)
            {
                binding.imageProfile.setImageBitmap(receivedProfileImage);
            }

        }

    }
}
