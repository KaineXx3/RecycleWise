package com.example.fyp1.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.example.fyp1.Model.Collector;
import com.example.fyp1.R;

import java.util.ArrayList;
import java.util.List;

public class CollectorListAdapter extends RecyclerView.Adapter<CollectorListAdapter.CollectorViewHolder> {

    private List<Collector> collectors;
    private List<Collector> allCollectors;
    private Context context;
    private OnCollectorClickListener onCollectorClickListener;

    public interface OnCollectorClickListener {
        void onCollectorClick(Collector collector);
    }

    public CollectorListAdapter(Context context, List<Collector> collectors, OnCollectorClickListener listener) {
        this.context = context;
        this.collectors = collectors;
        this.allCollectors = new ArrayList<>(collectors);
        this.onCollectorClickListener = listener;
    }

    @NonNull
    @Override
    public CollectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_collector, parent, false);
        return new CollectorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectorViewHolder holder, int position) {
        Collector collector = collectors.get(position);
        holder.nameTextView.setText(collector.getCompanyName());
        holder.stateAndDistrictTextView.setText(collector.getProvince() + ", " + collector.getState());

        // Load image using Picasso
        Picasso.get()
                .load(collector.getImageUrl())
                .placeholder(R.drawable.defaultimage)
                .error(R.drawable.defaultimage)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (onCollectorClickListener != null) {
                onCollectorClickListener.onCollectorClick(collector);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collectors.size();
    }

    public void setCollectors(List<Collector> collectors) {
        this.collectors = collectors;
        this.allCollectors = new ArrayList<>(collectors);
        notifyDataSetChanged();
    }

    static class CollectorViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView stateAndDistrictTextView;

        CollectorViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewCollector);
            nameTextView = itemView.findViewById(R.id.textViewCollectorName);
            stateAndDistrictTextView = itemView.findViewById(R.id.textViewStateAndDistrict);
        }
    }
}