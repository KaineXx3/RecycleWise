package com.example.fyp1;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class QuizBeginner extends AppCompatActivity {

    private TextView questionTextView;
    private TextView questionCountTextView;
    private RadioGroup answerRadioGroup;
    private RadioButton[] answerRadioButtons;
    private TextView scoreTextView;
    private TextView timerTextView;
    private Button actionButton;

    private List<QuizQuestion> quizQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer timer;
    private static final long QUESTION_TIMEOUT = 20000; // 20 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_question);

        questionTextView = findViewById(R.id.question_text_view);
        questionCountTextView = findViewById(R.id.question_count_text_view);
        answerRadioGroup = findViewById(R.id.answer_radio_group);
        answerRadioButtons = new RadioButton[4];
        answerRadioButtons[0] = findViewById(R.id.answer_radio_1);
        answerRadioButtons[1] = findViewById(R.id.answer_radio_2);
        answerRadioButtons[2] = findViewById(R.id.answer_radio_3);
        answerRadioButtons[3] = findViewById(R.id.answer_radio_4);
        scoreTextView = findViewById(R.id.score_text_view);
        timerTextView = findViewById(R.id.timer_text_view);
        actionButton = findViewById(R.id.action_button);

        initializeQuestions();
        displayQuestion();

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionButton.getText().equals("Submit Answer")) {
                    // Check if an answer has been selected
                    if (answerRadioGroup.getCheckedRadioButtonId() == -1) {
                        // No answer selected
                        MotionToast.Companion.darkToast(QuizBeginner.this,
                                "Answer Selection Required",
                                "Please select an answer",
                                MotionToastStyle.INFO,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(QuizBeginner.this, www.sanju.motiontoast.R.font.helveticabold));
                    } else {
                        // Answer selected, proceed to check the answer
                        timer.cancel();
                        checkAnswer();
                    }
                } else {
                    // Move to the next question
                    moveToNextQuestion();
                }
            }
        });

        // Add a listener to the RadioGroup to handle answer selection
        answerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Enable the Submit Answer button if an answer is selected
                if (checkedId != -1) {
                    actionButton.setEnabled(true);
                }
            }
        });

        // Initially disable the Submit Answer button
        actionButton.setEnabled(false);
    }

    private void initializeQuestions() {
        quizQuestions = new ArrayList<>();
        quizQuestions.add(new QuizQuestion(
                "Which of these items should always be recycled?",
                new String[]{"Plastic bottles", "Used tissues", "Dirty diapers", "Food waste"},
                "Plastic bottles"
        ));

        quizQuestions.add(new QuizQuestion(
                "What color bin is commonly used for general recycling?",
                new String[]{"Blue", "Red", "Yellow", "Green"},
                "Blue"
        ));

        quizQuestions.add(new QuizQuestion(
                "Which material is NOT typically recyclable?",
                new String[]{"Paper", "Glass", "Styrofoam", "Aluminum"},
                "Styrofoam"
        ));

        quizQuestions.add(new QuizQuestion(
                "What should you do with empty plastic water bottles?",
                new String[]{"Recycle them", "Throw them in the trash", "Reuse them indefinitely", "Burn them"},
                "Recycle them"
        ));

        quizQuestions.add(new QuizQuestion(
                "Which of these can usually be recycled?",
                new String[]{"Newspapers", "Paper towels", "Used napkins", "Tissues"},
                "Newspapers"
        ));

        quizQuestions.add(new QuizQuestion(
                "What's the first step before recycling containers?",
                new String[]{"Rinse them", "Crush them", "Label them", "Paint them"},
                "Rinse them"
        ));

        quizQuestions.add(new QuizQuestion(
                "Which of these is a recyclable metal?",
                new String[]{"Aluminum", "Mercury", "Uranium", "Plutonium"},
                "Aluminum"
        ));

        quizQuestions.add(new QuizQuestion(
                "What type of light bulb is safe to recycle with regular glass?",
                new String[]{"None of them", "Incandescent bulbs", "LED bulbs", "Fluorescent bulbs"},
                "None of them"
        ));

        quizQuestions.add(new QuizQuestion(
                "Which item does NOT belong in a home recycling bin?",
                new String[]{"Batteries", "Cardboard boxes", "Glass jars", "Aluminum cans"},
                "Batteries"
        ));

        quizQuestions.add(new QuizQuestion(
                "What's the best way to dispose of old magazines?",
                new String[]{"Recycle them", "Throw them in the trash", "Burn them", "Bury them"},
                "Recycle them"
        ));
        Collections.shuffle(quizQuestions);
    }

    private void displayQuestion() {
        QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.getQuestion());
        questionCountTextView.setText("Question: " + (currentQuestionIndex + 1) + "/" + quizQuestions.size());
        List<String> answers = new ArrayList<>(List.of(currentQuestion.getAnswers()));
        Collections.shuffle(answers);
        for (int i = 0; i < answerRadioButtons.length; i++) {
            answerRadioButtons[i].setText(answers.get(i));
            answerRadioButtons[i].setChecked(false);
            answerRadioButtons[i].setTextColor(getResources().getColor(android.R.color.black));
            answerRadioButtons[i].setEnabled(true);
        }
        actionButton.setText("Submit Answer");
        actionButton.setEnabled(false); // Disable until an answer is selected
        startTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(QUESTION_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                timerTextView.setText("Time left: " + secondsRemaining + "s");

                // When 10 seconds are left, change the text color to red and blink once
                if (secondsRemaining == 10) {
                    timerTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    blinkTimerOnce();
                }

                // When 5 seconds are left, keep blinking
                if (secondsRemaining <= 5) {
                    blinkTimer();
                }
            }

            public void onFinish() {
                // Stop any blinking and reset the timer's text color
                timerTextView.animate().cancel();
                timerTextView.setTextColor(getResources().getColor(android.R.color.black));
                timerTextView.setText("Time's up!");
                checkAnswer();
                actionButton.setEnabled(true);  // Enable the button when time is up
                actionButton.setText("Next Question");
            }
        }.start();
    }

    // Method to blink the timer text once (used for 10 seconds left)
    private void blinkTimerOnce() {
        timerTextView.animate().alpha(0).setDuration(500).withEndAction(new Runnable() {
            @Override
            public void run() {
                timerTextView.animate().alpha(1).setDuration(500);
            }
        });
    }

    // Method to continuously blink the timer text (used for 5 seconds left)
    private void blinkTimer() {
        timerTextView.animate().alpha(0).setDuration(500).withEndAction(new Runnable() {
            @Override
            public void run() {
                timerTextView.animate().alpha(1).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        blinkTimer(); // Recursively call to keep blinking
                    }
                });
            }
        });
    }

    private void checkAnswer() {
        // Stop any blinking when the answer is submitted
        timerTextView.animate().cancel();
        timerTextView.setTextColor(getResources().getColor(android.R.color.black)); // Reset the color

        int selectedId = answerRadioGroup.getCheckedRadioButtonId();
        QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
        boolean correct = false;

        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            String selectedAnswer = selectedRadioButton.getText().toString();
            correct = selectedAnswer.equals(currentQuestion.getCorrectAnswer());
            if (correct) {
                score++;
            }
        }

        for (RadioButton rb : answerRadioButtons) {
            rb.setEnabled(false);
            if (rb.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                rb.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (rb.isChecked() && !correct) {
                rb.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        scoreTextView.setText("Score: " + score);
        actionButton.setText("Next Question");
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < quizQuestions.size()) {
            displayQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        // Hide all other views
        findViewById(R.id.header_card).setVisibility(View.GONE);
        questionTextView.setVisibility(View.GONE);
        questionCountTextView.setVisibility(View.GONE);
        answerRadioGroup.setVisibility(View.GONE);
        actionButton.setVisibility(View.GONE);
        timerTextView.setVisibility(View.GONE);

        // Show the final results layout
        LinearLayout finalResultsLayout = findViewById(R.id.final_results_layout);
        finalResultsLayout.setVisibility(View.VISIBLE);

        TextView congratulationsMessage = findViewById(R.id.congratulations_message);
        TextView finalScoreTextView = findViewById(R.id.final_score_text_view);
        congratulationsMessage.setText("Congratulations!");
        finalScoreTextView.setText("Final Score: " + score);

        // Optionally, set up listeners for the restart and exit buttons
        Button restartButton = findViewById(R.id.restart_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartQuiz();
            }
        });

        Button exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void restartQuiz() {
        currentQuestionIndex = 0;
        score = 0; // Reset the score to 0
        initializeQuestions(); // Reinitialize the questions
        displayQuestion(); // Display the first question

        // Hide the final results layout
        findViewById(R.id.final_results_layout).setVisibility(View.GONE);

        // Show all quiz UI elements
        findViewById(R.id.header_card).setVisibility(View.VISIBLE);
        questionTextView.setVisibility(View.VISIBLE);
        questionCountTextView.setVisibility(View.VISIBLE);
        answerRadioGroup.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.VISIBLE);

        // Reset the score text view
        scoreTextView.setText("Score: 0");

        // Initially disable the Submit Answer button
        actionButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    private static class QuizQuestion {
        private String question;
        private String[] answers;
        private String correctAnswer;

        public QuizQuestion(String question, String[] answers, String correctAnswer) {
            this.question = question;
            this.answers = answers;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() { return question; }
        public String[] getAnswers() { return answers; }
        public String getCorrectAnswer() { return correctAnswer; }
    }
}
