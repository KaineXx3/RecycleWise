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

public class QuizAdvanced extends AppCompatActivity {

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
                    if (answerRadioGroup.getCheckedRadioButtonId() == -1) {
                        MotionToast.Companion.darkToast(QuizAdvanced.this,
                                "Answer Selection Required",
                                "Please select an answer",
                                MotionToastStyle.INFO,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(QuizAdvanced.this, www.sanju.motiontoast.R.font.helveticabold));
                    } else {
                        timer.cancel();
                        checkAnswer();
                    }
                } else {
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
                "Which process involves breaking down recyclable materials into their base components?",
                new String[]{"Recycling", "Composting", "Upcycling", "Incineration"},
                "Recycling"
        ));
        quizQuestions.add(new QuizQuestion(
                "What is the term for reusing materials to create products of higher value?",
                new String[]{"Upcycling", "Downcycling", "Composting", "Landfilling"},
                "Upcycling"
        ));
        quizQuestions.add(new QuizQuestion(
                "How much energy can be saved by recycling one ton of paper?",
                new String[]{"Approximately 4,000 kilowatts", "Approximately 1,000 kilowatts", "Approximately 2,000 kilowatts", "Approximately 8,000 kilowatts"},
                "Approximately 4,000 kilowatts"
        ));
        quizQuestions.add(new QuizQuestion(
                "What is the environmental impact of improperly disposing of electronic waste?",
                new String[]{"Release of toxic substances", "Reduced resource use", "Improved energy efficiency", "Increased material recovery"},
                "Release of toxic substances"
        ));
        quizQuestions.add(new QuizQuestion(
                "Which international agreement focuses on reducing hazardous waste and its disposal?",
                new String[]{"Basel Convention", "Paris Agreement", "Kyoto Protocol", "Montreal Protocol"},
                "Basel Convention"
        ));
        quizQuestions.add(new QuizQuestion(
                "What is the main difference between recycling and upcycling?",
                new String[]{"Recycling breaks down materials; upcycling creates new products from existing ones", "Upcycling breaks down materials; recycling creates new products", "They are the same process", "Recycling is more expensive than upcycling"},
                "Recycling breaks down materials; upcycling creates new products from existing ones"
        ));
        quizQuestions.add(new QuizQuestion(
                "Which material is commonly used to create new products from recycled paper?",
                new String[]{"Recycled paper pulp", "Plastic pellets", "Glass shards", "Aluminum scrap"},
                "Recycled paper pulp"
        ));
        quizQuestions.add(new QuizQuestion(
                "What is the primary goal of the circular economy model in waste management?",
                new String[]{"Minimize waste and maximize resource use", "Increase waste production", "Focus solely on recycling", "Promote single-use products"},
                "Minimize waste and maximize resource use"
        ));
        quizQuestions.add(new QuizQuestion(
                "Which of these waste management strategies involves reusing old products for new purposes?",
                new String[]{"Upcycling", "Downcycling", "Landfilling", "Incineration"},
                "Upcycling"
        ));
        quizQuestions.add(new QuizQuestion(
                "What is the primary challenge of recycling composite materials?",
                new String[]{"Difficulty in separating different materials", "Lack of demand", "High cost of recycling", "Limited technology available"},
                "Difficulty in separating different materials"
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
