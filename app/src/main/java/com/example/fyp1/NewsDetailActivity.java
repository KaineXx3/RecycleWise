package com.example.fyp1;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class NewsDetailActivity extends AppCompatActivity {

    private ImageView newsImageView;
    private TextView newsTitleTextView, newsDateTextView, newsInformationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_news_information);

        newsImageView = findViewById(R.id.eventImageDisplay);
        newsTitleTextView = findViewById(R.id.titleDisplay);
        newsDateTextView = findViewById(R.id.displaydate);
        newsInformationTextView = findViewById(R.id.infoDisplay);


        String imageUrl = getIntent().getStringExtra("imageUrl");
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String information = getIntent().getStringExtra("news_information");
        newsTitleTextView.setText(title);
        newsDateTextView.setText(date);
        String datesubstring = information != null ? date.substring(0, Math.min(information.length(), 10)) : "";
        newsDateTextView.setText(datesubstring);
        newsInformationTextView.setText(information);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.defaultimage)
                    .error(R.drawable.defaultimage)
                    .into(newsImageView);
        } else {
            newsImageView.setImageResource(R.drawable.defaultimage);
        }
    }
}
