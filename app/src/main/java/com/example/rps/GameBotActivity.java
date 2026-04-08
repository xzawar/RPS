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
import java.util.Random;

public class GameBotActivity extends AppCompatActivity {

    // 0=Rock ✊, 1=Paper ✋, 2=Scissors ✌
    private static final String[] HAND_EMOJI = {"✊", "✋", "✌"};
    private static final String[] CHOICE_NAME = {"ROCK", "PAPER", "SCISSORS"};

    private TextView tvBotHand, tvPlayerHand, tvScorePlayer, tvScoreBot, tvResult, tvCountdown;
    private Button btnRock, btnPaper, btnScissors;

    private int playerScore = 0, botScore = 0;
    private int difficulty; // 0=easy, 1=normal, 2=hard
    private int playerLastChoice = -1;
    private boolean playerHasChosen = false;
    private boolean roundActive = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private Runnable animRunnable;
    private Runnable stopRunnable;
    private int animFrame = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game_bot);

        difficulty    = getIntent().getIntExtra("difficulty", 0);
        tvBotHand     = findViewById(R.id.tvBotHand);
        tvPlayerHand  = findViewById(R.id.tvPlayerHand);
        tvScorePlayer = findViewById(R.id.tvScorePlayer);
        tvScoreBot    = findViewById(R.id.tvScoreBot);
        tvResult      = findViewById(R.id.tvResult);
        tvCountdown   = findViewById(R.id.tvCountdown);
        btnRock       = findViewById(R.id.btnRock);
        btnPaper      = findViewById(R.id.btnPaper);
        btnScissors   = findViewById(R.id.btnScissors);

        btnRock.setOnClickListener(v -> playerChoose(0));
        btnPaper.setOnClickListener(v -> playerChoose(1));
        btnScissors.setOnClickListener(v -> playerChoose(2));

        findViewById(R.id.btnExit).setOnClickListener(v -> finish());

        startNewRound();
    }

    private void startNewRound() {
        playerHasChosen = false;
        roundActive = false;
        tvResult.setVisibility(View.INVISIBLE);
        tvBotHand.setText("✊");
        tvPlayerHand.setText("✊");
        setButtonsEnabled(false);

        // Countdown 3 → 2 → 1 → GO!
        tvCountdown.setVisibility(View.VISIBLE);
        tvCountdown.setText("3");
        handler.postDelayed(() -> tvCountdown.setText("2"), 700);
        handler.postDelayed(() -> tvCountdown.setText("1"), 1400);
        handler.postDelayed(() -> {
            tvCountdown.setText("GO!");
            roundActive = true;
            setButtonsEnabled(true);
            startHandAnimation();
        }, 2100);
        handler.postDelayed(() -> tvCountdown.setVisibility(View.INVISIBLE), 2600);
    }

    private void startHandAnimation() {
        animFrame = 0;
        animRunnable = new Runnable() {
            @Override
            public void run() {
                animFrame = (animFrame + 1) % 3;
                tvBotHand.setText(HAND_EMOJI[animFrame]);
                // Bounce animation on the hand
                tvBotHand.animate().scaleX(1.08f).scaleY(1.08f).setDuration(60)
                    .withEndAction(() -> tvBotHand.animate().scaleX(1f).scaleY(1f).setDuration(60).start())
                    .start();
                handler.postDelayed(this, 180);
            }
        };
        handler.post(animRunnable);

        // Stop delay based on difficulty (Easy=long, Hard=short)
        long minDelay, range;
        switch (difficulty) {
            case 0: minDelay = 3000; range = 2000; break; // 3–5s
            case 1: minDelay = 1500; range = 1500; break; // 1.5–3s
            default: minDelay = 700;  range = 800;  break; // 0.7–1.5s
        }
        long stopDelay = minDelay + (long)(random.nextFloat() * range);

        stopRunnable = () -> {
            if (!playerHasChosen && roundActive) {
                // Time's up — player too slow
                handler.removeCallbacks(animRunnable);
                setButtonsEnabled(false);
                int botChoice = getBotChoice(-1);
                tvBotHand.setText(HAND_EMOJI[botChoice]);
                resolveRound(-1, botChoice);
            }
        };
        handler.postDelayed(stopRunnable, stopDelay);
    }

    private void playerChoose(int choice) {
        if (!roundActive || playerHasChosen) return;
        playerHasChosen = true;
        roundActive = false;

        handler.removeCallbacks(animRunnable);
        handler.removeCallbacks(stopRunnable);
        setButtonsEnabled(false);

        // Show player's hand (flipped upward to face screen)
        tvPlayerHand.setText(HAND_EMOJI[choice]);
        animatePop(tvPlayerHand);

        // Brief pause then reveal bot hand
        int botChoice = getBotChoice(choice);
        handler.postDelayed(() -> {
            tvBotHand.setText(HAND_EMOJI[botChoice]);
            animatePop(tvBotHand);
            handler.postDelayed(() -> resolveRound(choice, botChoice), 500);
        }, 400);

        playerLastChoice = choice;
    }

    private int getBotChoice(int playerChoice) {
        switch (difficulty) {
            case 0:
                // Easy: pure random
                return random.nextInt(3);
            case 1:
                // Normal: 45% chance to counter last move, else random
                if (playerLastChoice >= 0 && random.nextFloat() < 0.45f) {
                    return (playerLastChoice + 1) % 3;
                }
                return random.nextInt(3);
            default:
                // Hard: always counters current/last choice
                int target = playerChoice >= 0 ? playerChoice : (playerLastChoice >= 0 ? playerLastChoice : random.nextInt(3));
                return (target + 1) % 3;
        }
    }

    // Returns 1=player wins, 2=bot wins, 0=draw
    private int determineWinner(int player, int bot) {
        if (player == bot) return 0;
        return (player - bot + 3) % 3 == 1 ? 1 : 2;
    }

    private void resolveRound(int player, int bot) {
        String resultText;
        int resultColor;

        if (player == -1) {
            botScore++;
            resultText = "TOO SLOW! ⏰";
            resultColor = 0xFFF9A825;
        } else {
            int winner = determineWinner(player, bot);
            if (winner == 0) {
                resultText = "DRAW! 🤝";
                resultColor = 0xFFF9A825;
            } else if (winner == 1) {
                playerScore++;
                resultText = CHOICE_NAME[player] + " BEATS " + CHOICE_NAME[bot] + "!\nYOU WIN! 🎉";
                resultColor = 0xFF43A047;
            } else {
                botScore++;
                resultText = CHOICE_NAME[bot] + " BEATS " + CHOICE_NAME[player] + "!\nBOT WINS! 🤖";
                resultColor = 0xFFE53935;
            }
        }

        tvScorePlayer.setText(String.valueOf(playerScore));
        tvScoreBot.setText(String.valueOf(botScore));
        tvResult.setTextColor(resultColor);
        tvResult.setText(resultText);
        tvResult.setVisibility(View.VISIBLE);
        animatePop(tvResult);

        if (playerScore >= 3 || botScore >= 3) {
            handler.postDelayed(this::showGameOver, 2000);
        } else {
            handler.postDelayed(this::startNewRound, 2200);
        }
    }

    private void showGameOver() {
        boolean playerWon = playerScore >= 3;
        new AlertDialog.Builder(this)
            .setTitle(playerWon ? "🏆 YOU WIN!" : "🤖 BOT WINS!")
            .setMessage("Final Score\nYou: " + playerScore + "  —  Bot: " + botScore)
            .setPositiveButton("PLAY AGAIN", (d, w) -> {
                playerScore = 0; botScore = 0;
                playerLastChoice = -1;
                tvScorePlayer.setText("0");
                tvScoreBot.setText("0");
                startNewRound();
            })
            .setNegativeButton("EXIT", (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    private void setButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1f : 0.45f;
        btnRock.setEnabled(enabled);     btnRock.setAlpha(alpha);
        btnPaper.setEnabled(enabled);    btnPaper.setAlpha(alpha);
        btnScissors.setEnabled(enabled); btnScissors.setAlpha(alpha);
    }

    private void animatePop(View v) {
        v.setScaleX(0.6f); v.setScaleY(0.6f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(v, "scaleX", 0.6f, 1.1f, 1f),
            ObjectAnimator.ofFloat(v, "scaleY", 0.6f, 1.1f, 1f)
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
