package in.digitaldealsolution.quizapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.Lottie;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class QuizActivity extends AppCompatActivity {
    private Boolean timer;
    private CountDownTimer countDownTimer;
    private ArrayList<QuestionModel> questionModels;
    private QuestionModel questionModel;
    private TextView question,counter,questionNo;
    private CardView  countercard;
    private int currentQuestion =0,score;
    private ArrayList<Button> optionBtn;
    private Button option1,option2,option3,option4;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = database.getReference("Questions");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        timer = getIntent().getBooleanExtra("timer", true);
        score =0;
        questionModels = new ArrayList<>();
        questionNo = findViewById(R.id.qestion_No);
        countercard = findViewById(R.id.counter_card);
        counter = findViewById(R.id.counter);
        question = findViewById(R.id.question_txt);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 =findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        optionBtn = new ArrayList<>(Arrays.asList(option1,option2,option3,option4));
        ProgressDialog Dialog = new ProgressDialog(QuizActivity.this);
        Dialog.setMessage("Loading Questions");
        Dialog.show();
        countDownTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished/1000<5){
                    countercard.setCardBackgroundColor(Color.RED);
                }
                else{
                    countercard.setCardBackgroundColor(Color.GREEN);
                }
                counter.setText(String.valueOf(millisUntilFinished/1000));

            }

            @Override
            public void onFinish() {
                if(currentQuestion<questionModels.size()-1)
                { currentQuestion++;
                    getNewQuestion();}
                else{
                    endquiz();
                }
            }
        };
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String question = dataSnapshot.child("question").getValue(String.class);
                    String option1 = dataSnapshot.child("option1").getValue(String.class);
                    String option2 = dataSnapshot.child("option2").getValue(String.class);
                    String option3 = dataSnapshot.child("option3").getValue(String.class);
                    String option4 = dataSnapshot.child("option4").getValue(String.class);
                    Integer answer = dataSnapshot.child("answer").getValue(Integer.class);
                    questionModel = new QuestionModel(question,option1,option2,option3,option4,answer);
                    questionModels.add(questionModel);
                }
                getNewQuestion();
                Dialog.hide();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


       option1.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               checkAnswer(0);
           }
       });
        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(1);
            }
        });
        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(2);
            }
        });
        option4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(3);
            }
        });

    }

    private void getNewQuestion() {
        questionNo.setText("Question " + (currentQuestion + 1) + "/" + questionModels.size());
        question.setText(questionModels.get(currentQuestion).getQuestion());
        option1.setText(questionModels.get(currentQuestion).getOption1());
        option2.setText(questionModels.get(currentQuestion).getOption2());
        option3.setText(questionModels.get(currentQuestion).getOption3());
        option4.setText(questionModels.get(currentQuestion).getOption4());

        option1.setBackground(getResources().getDrawable(R.drawable.btn_bg));
        option2.setBackground(getResources().getDrawable(R.drawable.btn_bg));
        option3.setBackground(getResources().getDrawable(R.drawable.btn_bg));
        option4.setBackground(getResources().getDrawable(R.drawable.btn_bg));
        if(timer){
            setTimer();
        }
        else{
            countercard.setVisibility(View.INVISIBLE);
            counter.setVisibility(View.INVISIBLE);
        }
    }

    public void setTimer() {
        countDownTimer.start();
    }

    private void checkAnswer(int x) {
        int correctAns = questionModels.get(currentQuestion).getAnswer();
        if(x == correctAns){
            optionBtn.get(x).setBackgroundColor(Color.GREEN);
            score++;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(currentQuestion<questionModels.size()-1)
                    { currentQuestion++;
                    getNewQuestion();}

                    else{
                        endquiz();
                    }
                }
            },200);
        }
        else{
            optionBtn.get(x).setBackgroundColor(Color.RED);
            optionBtn.get(correctAns).setBackgroundColor(Color.GREEN);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    optionBtn.get(x).setBackground(getResources().getDrawable(R.drawable.btn_bg));
                    if(currentQuestion<questionModels.size()-1)
                    { currentQuestion++;
                        getNewQuestion();}
                    else{
                      endquiz();
                    }
                }
            },200);
        }
    }

    private void endquiz() {
        countDownTimer.cancel();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(QuizActivity.this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.score_dialog,(ConstraintLayout) findViewById(R.id.score_dialog));
        TextView score_txt = bottomSheetView.findViewById(R.id.score);
        TextView result = bottomSheetView.findViewById(R.id.result);
        Button play_again = bottomSheetView.findViewById(R.id.play_again);
        Button exit = bottomSheetView.findViewById(R.id.exit);
        LottieAnimationView pass =bottomSheetView.findViewById(R.id.pass);
        LottieAnimationView fail = bottomSheetView.findViewById(R.id.fail);
        play_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestion = 0;
                score = 0;
                fail.setVisibility(View.GONE);
                pass.setVisibility(View.GONE);
                option1.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                option2.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                option3.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                option4.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                bottomSheetDialog.hide();
                getNewQuestion();
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        score_txt.setText("Your score is "+ score + " out of " + questionModels.size());
        if(((float)score/questionModels.size())*100>33.33){
            result.setText("PASS");
            pass.setVisibility(View.VISIBLE);
        }
        else{
            fail.setVisibility(View.VISIBLE);
            result.setText("FAIL");
            result.setTextColor(Color.RED);
        }
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setDismissWithAnimation(true);
        bottomSheetDialog.show();

    }
}
