package com.example.rps;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
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

    private static final int[] HAND_DRAWABLE = {
        R.drawable.ic_hand_rock,
        R.drawable.ic_hand_paper,
        R.drawable.ic_hand_scissors
    };
    private String[] CHOICE_NAME;

    private enum TurnState {
        P2_CHOOSING, P1_CHOOSING, REVEAL
    }

    private ImageView ivP1Hand, ivP2Hand;
    private TextView tvScoreP1, tvScoreP2, tvResult, tvStatus;
    private Button btnP1Rock, btnP1Paper, btnP1Scissors, btnP2Rock, btnP2Paper, btnP2Scissors;
    private int p1Choice, p2Choice;
    private int p1Score = 0, p2Score = 0;
    private TurnState turnState;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game_friend);

        CHOICE_NAME = new String[]{
                getString(R.string.rock),
                getString(R.string.paper),
                getString(R.string.scissors)
        };

        ivP1Hand   = findViewById(R.id.ivP1Hand);
        ivP2Hand   = findViewById(R.id.ivP2Hand);
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
        ivP1Hand.setImageResource(HAND_DRAWABLE[0]);
        ivP2Hand.setImageResource(HAND_DRAWABLE[0]);
        ivP1Hand.clearAnimation();
        ivP2Hand.clearAnimation();

        turnState = TurnState.P2_CHOOSING;
        setP1ButtonsEnabled(false);
        setP2ButtonsEnabled(true);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(R.string.p2_choose);
        tvStatus.setTextColor(0xFF1E88E5);

        // Bounce P2 hand to signal their turn
        Animation shakeTop = AnimationUtils.loadAnimation(this, R.anim.hand_shake);
        ivP2Hand.startAnimation(shakeTop);

        Animation pop = AnimationUtils.loadAnimation(this, R.anim.countdown_pop);
        tvStatus.startAnimation(pop);
    }

    private void p2Choose(int choice) {
        if (turnState != TurnState.P2_CHOOSING) return;
        p2Choice = choice;
        // Show rock (hidden until reveal)
        ivP2Hand.clearAnimation();
        ivP2Hand.setImageResource(R.drawable.ic_hand_rock);
        Animation rev = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
        ivP2Hand.startAnimation(rev);
        setP2ButtonsEnabled(false);

        // Bounce P2 hand while waiting
        handler.postDelayed(() -> {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.hand_shake);
            ivP2Hand.startAnimation(shake);
        }, 350);

        turnState = TurnState.P1_CHOOSING;
        setP1ButtonsEnabled(true);
        tvStatus.setText(R.string.p1_choose);
        tvStatus.setTextColor(0xFFE53935);
        Animation pop = AnimationUtils.loadAnimation(this, R.anim.countdown_pop);
        tvStatus.startAnimation(pop);

        // Also start bouncing P1 hand to signal their turn
        Animation shakeBottom = AnimationUtils.loadAnimation(this, R.anim.hand_shake_player);
        ivP1Hand.startAnimation(shakeBottom);
    }

    private void p1Choose(int choice) {
        if (turnState != TurnState.P1_CHOOSING) return;
        p1Choice = choice;
        ivP1Hand.clearAnimation();
        ivP2Hand.clearAnimation();
        ivP1Hand.setImageResource(R.drawable.ic_hand_rock);
        Animation rev = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
        ivP1Hand.startAnimation(rev);

        // Both hands shake dramatically before reveal
        handler.postDelayed(() -> {
            ivP2Hand.startAnimation(AnimationUtils.loadAnimation(this, R.anim.hand_shake));
            ivP1Hand.startAnimation(AnimationUtils.loadAnimation(this, R.anim.hand_shake_player));
        }, 200);

        setP1ButtonsEnabled(false);
        turnState = TurnState.REVEAL;
        tvStatus.setVisibility(View.INVISIBLE);

        handler.postDelayed(this::reveal, 900);
    }

    private void reveal() {
        ivP2Hand.clearAnimation();
        ivP1Hand.clearAnimation();

        ivP2Hand.setImageResource(HAND_DRAWABLE[p2Choice]);
        ivP1Hand.setImageResource(HAND_DRAWABLE[p1Choice]);

        Animation rev1 = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
        Animation rev2 = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
        ivP2Hand.startAnimation(rev1);
        ivP1Hand.startAnimation(rev2);

        handler.postDelayed(() -> resolveRound(p1Choice, p2Choice), 500);
    }

    private void resolveRound(int p1, int p2) {
        String resultText;
        int resultColor;

        int winner = determineWinner(p1, p2);
        if (winner == 0) {
            resultText = getString(R.string.draw);
            resultColor = 0xFFF9A825;
        } else if (winner == 1) {
            p1Score++;
            resultText = getString(R.string.p1_wins_round, CHOICE_NAME[p1], CHOICE_NAME[p2]);
            resultColor = 0xFFE53935;
            Animation wb = AnimationUtils.loadAnimation(this, R.anim.win_bounce);
            ivP1Hand.startAnimation(wb);
        } else {
            p2Score++;
            resultText = getString(R.string.p2_wins_round, CHOICE_NAME[p2], CHOICE_NAME[p1]);
            resultColor = 0xFF1E88E5;
            Animation wb = AnimationUtils.loadAnimation(this, R.anim.win_bounce);
            ivP2Hand.startAnimation(wb);
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

    private int determineWinner(int p1, int p2) {
        if (p1 == p2) return 0;
        return (p1 - p2 + 3) % 3 == 1 ? 1 : 2;
    }

    private void showGameOver() {
        boolean p1Won = p1Score >= 3;
        new AlertDialog.Builder(this)
            .setTitle(p1Won ? getString(R.string.p1_wins_title) : getString(R.string.p2_wins_title))
            .setMessage(getString(R.string.final_score, getString(R.string.p1), p1Score, getString(R.string.p2), p2Score))
            .setPositiveButton(R.string.play_again, (d, w) -> {
                p1Score = 0; p2Score = 0;
                tvScoreP1.setText("0");
                tvScoreP2.setText("0");
                startNewRound();
            })
            .setNegativeButton(R.string.exit, (d, w) -> finish())
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
        if (ivP1Hand != null) ivP1Hand.clearAnimation();
        if (ivP2Hand != null) ivP2Hand.clearAnimation();
    }
}
