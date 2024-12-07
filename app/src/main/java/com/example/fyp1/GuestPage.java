package com.example.fyp1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class GuestPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_page);
    }

    public void toArPage(View view){
        Intent intent=new Intent(GuestPage.this, ArPage.class);
        startActivity(intent);
    }
    public void toObjectDetectionPage(View view){
        Intent intent=new Intent(GuestPage.this, ObjectDetection.class);
        startActivity(intent);


    }
    public void toQuizPage(View view){
        Intent intent=new Intent(GuestPage.this, QuizPage.class);
        startActivity(intent);
    }
}
