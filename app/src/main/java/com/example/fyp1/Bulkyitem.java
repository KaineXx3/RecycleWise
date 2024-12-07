package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Bulkyitem extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulky_item);
    }

    public void toRecycleLocation(View view){
        Intent intent = new Intent(Bulkyitem.this,ViewCollectorList.class);
        startActivity(intent);
    }
}
