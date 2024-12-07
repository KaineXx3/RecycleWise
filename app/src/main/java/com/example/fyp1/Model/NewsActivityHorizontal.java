package com.example.fyp1.Model;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp1.Adapter.newsInfoAdapter;
import com.example.fyp1.Model.NewsModel;
import com.example.fyp1.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class NewsActivityHorizontal extends AppCompatActivity {
    private RecyclerView recyclerView;
    private newsInfoAdapter adapter;
    private ArrayList<NewsModel> newsList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity_recyclerview);

        recyclerView = findViewById(R.id.recyclerView);
        // Set the layout manager to horizontal
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
                    NewsModel newsItem = snapshot.getValue(NewsModel.class);
                    if (newsItem != null) {
                        newsList.add(newsItem);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
