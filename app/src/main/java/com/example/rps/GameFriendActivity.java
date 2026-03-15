package com.example.rps;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameFriendActivity extends AppCompatActivity {

    private static final String[] HAND_EMOJI  = {"✊", "✋", "✌"};
    private static final String[] CHOICE_NAME = {"ROCK", "PAPER", "SCISSORS"};

    // Game state
    private enum TurnState { P2_CHOOSING, P1_CHOOSING, REVEAL }
    private TurnState turnState = TurnState.P2_CHOOSING;

    private int p1Score = 0, p2Score = 0;
    private int p1Choice = -1, p2Choice = -1;

    private TextView tvP1Hand, tvP2Hand;
    private TextView tvScoreP1, tvScoreP2;
    private TextView tvResult, tvStatus;
    private Button btnP1Rock, btnP1Paper, btnP1Scissors;
    private Button btnP2Rock, btnP2Paper, btnP2Scissors;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game_friend);

        tvP1Hand   = findViewById(R.id.tvP1Hand);
        tvP2Hand   = findViewById(R.id.tvP2Hand);
        tvScoreP1  = findViewById(R.id.tvScoreP1);
        tvScoreP2  = findViewById(R.id.tvScoreP2);
        tvResult   = findViewById(R.id.tvResult);
        tvStatus   = findViewById(R.id.tvStatus);

        btnP1Rock      = findViewById(R.id.btnP1Rock);
        btnP1Paper     = findViewById(R.id.btnP1Paper);
        btnP1Scissors  = findViewById(R.id.btnP1Scissors);
        btnP2Rock      = findViewById(R.id.btnP2Rock);
        btnP2Paper     = findViewById(R.id.btnP2Paper);
        btnP2Scissors  = findViewById(R.id.btnP2Scissors);

        btnP1Rock.setOnClickListener(v -> p1Choose(0));
        btnP1Paper.setOnClickListener(v -> p1Choose(1));
        btnP1Scissors.setOnClickListener(v -> p1Choose(2));

        btnP2Rock.setOnClickListener(v -> p2Choose(0));
        btnP2Paper.setOnClickListener(v -> p2Choose(1));
        btnP2Scissors.setOnClickListener(v -> p2Choose(2));

        findViewById(R.id.btnExit).setOnClickListener(v -> finish());

        startNewRound();
    }

    private void startNewRound() {
        p1Choice = -1;
        p2Choice = -1;
        tvResult.setVisibility(View.INVISIBLE);
        tvP1Hand.setText("❓");
        tvP2Hand.setText("❓");

        // P2 chooses first (they are at the top)
        turnState = TurnState.P2_CHOOSING;
        setP1ButtonsEnabled(false);
        setP2ButtonsEnabled(true);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("🔵 P2: Choose!");
        tvStatus.setTextColor(0xFF1E88E5);
    }

    private void p2Choose(int choice) {
        if (turnState != TurnState.P2_CHOOSING) return;
        p2Choice = choice;
        tvP2Hand.setText("🤜"); // hidden fist – reveals later
        animatePop(tvP2Hand);
        setP2ButtonsEnabled(false);

        // Now P1's turn
        turnState = TurnState.P1_CHOOSING;
        setP1ButtonsEnabled(true);
        tvStatus.setText("🔴 P1: Choose!");
        tvStatus.setTextColor(0xFFE53935);
    }

    private void p1Choose(int choice) {
        if (turnState != TurnState.P1_CHOOSING) return;
        p1Choice = choice;
        tvP1Hand.setText("🤜");
        animatePop(tvP1Hand);
        setP1ButtonsEnabled(false);
        turnState = TurnState.REVEAL;
        tvStatus.setVisibility(View.INVISIBLE);

        // Brief dramatic pause then reveal
        handler.postDelayed(this::reveal, 600);
    }

    private void reveal() {
        // Reveal both hands
        tvP2Hand.setText(HAND_EMOJI[p2Choice]);
        tvP1Hand.setText(HAND_EMOJI[p1Choice]);
        animatePop(tvP2Hand);
        animatePop(tvP1Hand);

        handler.postDelayed(() -> {
            resolveRound(p1Choice, p2Choice);
        }, 500);
    }

    private void resolveRound(int p1, int p2) {
        String resultText;
        int resultColor;

        int winner = determineWinner(p1, p2);
        if (winner == 0) {
            resultText = "DRAW! 🤝";
            resultColor = 0xFFF9A825;
        } else if (winner == 1) {
            p1Score++;
            resultText = "🔴 P1 WINS!\n" + CHOICE_NAME[p1] + " beats " + CHOICE_NAME[p2];
            resultColor = 0xFFE53935;
        } else {
            p2Score++;
            resultText = "🔵 P2 WINS!\n" + CHOICE_NAME[p2] + " beats " + CHOICE_NAME[p1];
            resultColor = 0xFF1E88E5;
        }

        tvScoreP1.setText(String.valueOf(p1Score));
        tvScoreP2.setText(String.valueOf(p2Score));
        tvResult.setTextColor(resultColor);
        tvResult.setText(resultText);
        tvResult.setVisibility(View.VISIBLE);
        animatePop(tvResult);

        if (p1Score >= 3 || p2Score >= 3) {
            handler.postDelayed(this::showGameOver, 2000);
        } else {
            handler.postDelayed(this::startNewRound, 2200);
        }
    }

    // Returns 1 if p1 wins, 2 if p2 wins, 0 draw
    private int determineWinner(int p1, int p2) {
        if (p1 == p2) return 0;
        return (p1 - p2 + 3) % 3 == 1 ? 1 : 2;
    }

    private void showGameOver() {
        boolean p1Won = p1Score >= 3;
        new AlertDialog.Builder(this)
            .setTitle(p1Won ? "🔴 PLAYER 1 WINS!" : "🔵 PLAYER 2 WINS!")
            .setMessage("Final Score\nP1: " + p1Score + "  —  P2: " + p2Score)
            .setPositiveButton("PLAY AGAIN", (d, w) -> {
                p1Score = 0; p2Score = 0;
                tvScoreP1.setText("0");
                tvScoreP2.setText("0");
                startNewRound();
            })
            .setNegativeButton("EXIT", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    private void setP1ButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1f : 0.4f;
        btnP1Rock.setEnabled(enabled);      btnP1Rock.setAlpha(alpha);
        btnP1Paper.setEnabled(enabled);     btnP1Paper.setAlpha(alpha);
        btnP1Scissors.setEnabled(enabled);  btnP1Scissors.setAlpha(alpha);
    }

    private void setP2ButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1f : 0.4f;
        btnP2Rock.setEnabled(enabled);      btnP2Rock.setAlpha(alpha);
        btnP2Paper.setEnabled(enabled);     btnP2Paper.setAlpha(alpha);
        btnP2Scissors.setEnabled(enabled);  btnP2Scissors.setAlpha(alpha);
    }

    private void animatePop(View v) {
        v.setScaleX(0.5f); v.setScaleY(0.5f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(v, "scaleX", 0.5f, 1.15f, 1f),
            ObjectAnimator.ofFloat(v, "scaleY", 0.5f, 1.15f, 1f)
        );
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
