package com.example.fyp1.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.R;
import com.example.fyp1.Model.ManageUserModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<ManageUserModel> userList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public UserAdapter(Context context, List<ManageUserModel> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_manage_user, parent, false);
        return new UserViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ManageUserModel user = userList.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(user.getRole());

        // Set the last sign-in time directly if it's already formatted
        String lastSignInTime = user.getLastSignInTime();
        if (lastSignInTime != null && !lastSignInTime.isEmpty()) {
            holder.tvLastActive.setText(lastSignInTime);
        } else {
            holder.tvLastActive.setText("No Data");
        }

        // Load the user's profile image using Picasso
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Picasso.get().load(user.getImageUrl()).into(holder.ivProfileImage);
        } else {
            holder.ivProfileImage.setImageResource(R.drawable.defaultprofile);
        }

        if (position == userList.size() - 1) {
            holder.itemView.setBackgroundResource(R.drawable.item_border_manage_user_last);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_border_manage_user);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole, tvLastActive;
        ImageView ivProfileImage, ivDelete;

        public UserViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvLastActive = itemView.findViewById(R.id.tv_last_active);
            ivProfileImage = itemView.findViewById(R.id.iv_profile);
            ivDelete = itemView.findViewById(R.id.iv_delete);

            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}
