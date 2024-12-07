package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Feedback extends AppCompatActivity {
    private RadioGroup radioGroup;
    private EditText descriptionInput;
    private Button sendButton;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize views
        radioGroup = findViewById(R.id.reportOptions);
        descriptionInput = findViewById(R.id.descriptionInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize Firebase Database reference
        feedbackRef = FirebaseDatabase.getInstance().getReference("Feedback");

        // Set initial states
        descriptionInput.setVisibility(View.GONE);
        sendButton.setEnabled(false);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        // Set the navigation icon click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toObjectDetection(); // Call your function when navigation icon is clicked
            }
        });

        // Set listener for radio group
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {  // Check if a button is actually selected
                RadioButton selectedRadioButton = findViewById(checkedId);
                if (selectedRadioButton != null) {
                    String selectedText = selectedRadioButton.getText().toString();

                    if (selectedText.equals("Other issue")) {
                        descriptionInput.setVisibility(View.VISIBLE);
                        descriptionInput.setText(""); // Clear any existing text
                        sendButton.setEnabled(!descriptionInput.getText().toString().trim().isEmpty());
                    } else {
                        descriptionInput.setVisibility(View.GONE);
                        sendButton.setEnabled(true);
                    }
                }
            } else {
                // No button selected (cleared selection)
                descriptionInput.setVisibility(View.GONE);
                sendButton.setEnabled(false);
            }
        });

        // Set text watcher for the EditText
        descriptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (radioGroup.getCheckedRadioButtonId() != -1) {
                    RadioButton selectedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                    if (selectedRadioButton != null &&
                            selectedRadioButton.getText().toString().equals("Other issue")) {
                        sendButton.setEnabled(s.length() > 0);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set click listener for send button
        sendButton.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadioButton = findViewById(selectedId);
                if (selectedRadioButton != null) {
                    String selectedText = selectedRadioButton.getText().toString();
                    String feedbackText;

                    if (selectedText.equals("Other issue")) {
                        feedbackText = descriptionInput.getText().toString().trim();
                        if (feedbackText.isEmpty()) {
                            Toast.makeText(Feedback.this, "Please enter a description", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        feedbackText = selectedText;
                    }

                    // Create a HashMap to store the feedback with the key "FeedbackText"
                    HashMap<String, String> feedbackMap = new HashMap<>();
                    feedbackMap.put("FeedbackText", feedbackText);

                    // Save feedback to Firebase under "Feedback" node
                    feedbackRef.push().setValue(feedbackMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(Feedback.this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                                resetForm();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Feedback.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });
    }

    private void resetForm() {
        // Reset form safely
        descriptionInput.setText("");
        descriptionInput.setVisibility(View.GONE);
        sendButton.setEnabled(false);
        radioGroup.clearCheck();  // This will trigger the OnCheckedChangeListener
    }

    public void toObjectDetection() {
        Intent intent = new Intent(Feedback.this, ObjectDetection.class);
        startActivity(intent);
        finish();
    }
}
