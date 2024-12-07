package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ArPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_ar_page);
    }
    public void toRecyclePaper(View view){
        Intent intent =new Intent(ArPage.this,ArPaper.class);
        startActivity(intent);
    }

    public void toRecycleAluminum(View view){
        Intent intent =new Intent(ArPage.this,ArAluminum.class);
        startActivity(intent);
    }

    public void toRecyclePlastic(View view){
        Intent intent =new Intent(ArPage.this,ArPlastic.class);
        startActivity(intent);
    }
}