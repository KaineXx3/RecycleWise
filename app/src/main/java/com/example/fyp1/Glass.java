package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Glass extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);
    }

    public void toRecycleLocation(View view){
        Intent intent = new Intent(Glass.this,ViewCollectorList.class);
        startActivity(intent);
    }

}
