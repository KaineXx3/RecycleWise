package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class QuizPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_quiz_page);
    }

    public void toBeginner(View view){
        Intent intent=new Intent(QuizPage.this, QuizBeginner.class);
        startActivity(intent);
    }
    public void toIntermediate(View view){
        Intent intent=new Intent(QuizPage.this, QuizIntermediate.class);
        startActivity(intent);
    }
    public void toAdvanced(View view){
        Intent intent=new Intent(QuizPage.this, QuizAdvanced.class);
        startActivity(intent);
    }
}