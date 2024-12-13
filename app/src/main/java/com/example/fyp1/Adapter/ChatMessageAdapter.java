package com.example.fyp1.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp1.Model.ChatMessage;
import com.example.fyp1.R;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.messageTextView.setText(message.getContent());

        if (message.isUser()) {
            holder.profileImageView.setImageResource(R.drawable.defaultprofile); // Set user profile picture
            holder.messageTextView.setBackgroundResource(R.drawable.bg_message_user);
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); // User messages on the right
        } else {
            holder.profileImageView.setImageResource(R.drawable.logo_trans); // Set AI profile picture
            holder.messageTextView.setBackgroundResource(R.drawable.bg_message_ai);
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); // AI messages on the left
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView profileImageView;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            profileImageView = itemView.findViewById(R.id.profileImage);
        }
    }
}
