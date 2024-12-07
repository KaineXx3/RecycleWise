package com.example.fyp1;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp1.Adapter.newsInfoAdapter;
import com.example.fyp1.Model.NewsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private newsInfoAdapter adapter;
    private ArrayList<NewsModel> newsList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity_recyclerview);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsList = new ArrayList<>();
        adapter = new newsInfoAdapter(newsList, this);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("news");

        // Fetch data from Firebase
        fetchNewsData();
    }

    private void fetchNewsData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("NewsActivity", "Raw data: " + snapshot.getValue());
                    NewsModel newsItem = snapshot.getValue(NewsModel.class);
                    if (newsItem != null) {
                        newsList.add(newsItem);
                        Log.d("NewsActivity", "Parsed news item: Title=" + newsItem.getTitle() +
                                ", Image URL=" + newsItem.getImageUrl() +
                                ", Date=" + newsItem.getDate());
                    } else {
                        Log.e("NewsActivity", "Failed to parse news item from snapshot: " + snapshot.getKey());
                    }
                }
                Log.d("NewsActivity", "Total news items fetched: " + newsList.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NewsActivity", "Failed to read data: " + databaseError.toException());
            }
        });
    }
}