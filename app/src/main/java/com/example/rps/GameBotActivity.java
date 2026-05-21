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
import java.util.Random;

public class GameBotActivity extends AppCompatActivity {

    private static final int[] HAND_DRAWABLE = {
        R.drawable.ic_hand_rock,
        R.drawable.ic_hand_paper,
        R.drawable.ic_hand_scissors
    };
    private String[] CHOICE_NAME;

    private ImageView ivBotHand, ivPlayerHand;
    private TextView tvScorePlayer, tvScoreBot, tvResult, tvCountdown;
    private Button btnRock, btnPaper, btnScissors;

    private int playerScore = 0, botScore = 0;
    private int difficulty;
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

        CHOICE_NAME = new String[]{
                getString(R.string.rock),
                getString(R.string.paper),
                getString(R.string.scissors)
        };
        difficulty    = getIntent().getIntExtra("difficulty", 0);
        ivBotHand     = findViewById(R.id.ivBotHand);
        ivPlayerHand  = findViewById(R.id.ivPlayerHand);
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
        ivBotHand.setImageResource(HAND_DRAWABLE[0]);
        ivPlayerHand.setImageResource(HAND_DRAWABLE[0]);
        setButtonsEnabled(false);
        ivBotHand.clearAnimation();
        ivPlayerHand.clearAnimation();

        // Countdown with pop animation each number
        tvCountdown.setVisibility(View.VISIBLE);
        showCountdown("3");
        handler.postDelayed(() -> showCountdown("2"), 700);
        handler.postDelayed(() -> showCountdown("1"), 1400);
        handler.postDelayed(() -> {
            showCountdown("GO!");
            roundActive = true;
            setButtonsEnabled(true);
            // Start shake bounce on both hands
            Animation shakeBot = AnimationUtils.loadAnimation(this, R.anim.hand_shake);
            Animation shakePlayer = AnimationUtils.loadAnimation(this, R.anim.hand_shake_player);
            ivBotHand.startAnimation(shakeBot);
            ivPlayerHand.startAnimation(shakePlayer);
            startHandEmojiCycle();
        }, 2100);
        handler.postDelayed(() -> {
            tvCountdown.setVisibility(View.INVISIBLE);
            tvCountdown.clearAnimation();
        }, 2700);
    }

    private void showCountdown(String text) {
        tvCountdown.setText(text);
        tvCountdown.clearAnimation();
        Animation pop = AnimationUtils.loadAnimation(this, R.anim.countdown_pop);
        tvCountdown.startAnimation(pop);
    }

    private void startHandEmojiCycle() {
        animFrame = 0;
        animRunnable = new Runnable() {
            @Override public void run() {
                animFrame = (animFrame + 1) % 3;
                ivBotHand.setImageResource(HAND_DRAWABLE[animFrame]);
                handler.postDelayed(this, 200);
            }
        };
        handler.post(animRunnable);

        long minDelay, range;
        switch (difficulty) {
            case 0: minDelay = 3000; range = 2000; break;
            case 1: minDelay = 1500; range = 1500; break;
            default: minDelay = 700;  range = 800;  break;
        }
        long stopDelay = minDelay + (long)(random.nextFloat() * range);

        stopRunnable = () -> {
            if (!playerHasChosen && roundActive) {
                handler.removeCallbacks(animRunnable);
                ivBotHand.clearAnimation();
                ivPlayerHand.clearAnimation();
                setButtonsEnabled(false);
                int botChoice = getBotChoice(-1);
                ivBotHand.setImageResource(HAND_DRAWABLE[botChoice]);
                Animation rev = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
                ivBotHand.startAnimation(rev);
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
        ivBotHand.clearAnimation();
        ivPlayerHand.clearAnimation();
        setButtonsEnabled(false);

        ivPlayerHand.setImageResource(HAND_DRAWABLE[choice]);
        Animation revP = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
        ivPlayerHand.startAnimation(revP);

        int botChoice = getBotChoice(choice);
        handler.postDelayed(() -> {
            ivBotHand.setImageResource(HAND_DRAWABLE[botChoice]);
            Animation revB = AnimationUtils.loadAnimation(this, R.anim.hand_reveal);
            ivBotHand.startAnimation(revB);
            handler.postDelayed(() -> resolveRound(choice, botChoice), 400);
        }, 400);

        playerLastChoice = choice;
    }

    private int getBotChoice(int playerChoice) {
        switch (difficulty) {
            case 0: return random.nextInt(3);
            case 1:
                if (playerLastChoice >= 0 && random.nextFloat() < 0.45f)
                    return (playerLastChoice + 1) % 3;
                return random.nextInt(3);
            default:
                int target = playerChoice >= 0 ? playerChoice : (playerLastChoice >= 0 ? playerLastChoice : random.nextInt(3));
                return (target + 1) % 3;
        }
    }

    private int determineWinner(int player, int bot) {
        if (player == bot) return 0;
        return (player - bot + 3) % 3 == 1 ? 1 : 2;
    }

    private void resolveRound(int player, int bot) {
        String resultText;
        int resultColor;

        if (player == -1) {
            botScore++;
            resultText = getString(R.string.too_slow);
            resultColor = 0xFFF9A825;
        } else {
            int winner = determineWinner(player, bot);
            if (winner == 0) {
                resultText = getString(R.string.draw);
                resultColor = 0xFFF9A825;
            } else if (winner == 1) {
                playerScore++;
                resultText = getString(R.string.you_win_round, CHOICE_NAME[player], CHOICE_NAME[bot]);
                resultColor = 0xFF43A047;
                Animation wb = AnimationUtils.loadAnimation(this, R.anim.win_bounce);
                ivPlayerHand.startAnimation(wb);
            } else {
                botScore++;
                resultText = getString(R.string.bot_wins_round, CHOICE_NAME[bot], CHOICE_NAME[player]);
                resultColor = 0xFFE53935;
                Animation wb = AnimationUtils.loadAnimation(this, R.anim.win_bounce);
                ivBotHand.startAnimation(wb);
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
            .setTitle(playerWon ? getString(R.string.you_win_title) : getString(R.string.bot_wins_title))
            .setMessage(getString(R.string.final_score, getString(R.string.you), playerScore, getString(R.string.bot), botScore))
            .setPositiveButton(R.string.play_again, (d, w) -> {
                playerScore = 0; botScore = 0;
                playerLastChoice = -1;
                tvScorePlayer.setText("0");
                tvScoreBot.setText("0");
                startNewRound();
            })
            .setNegativeButton(R.string.exit, (d, w) -> finish())
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
        if (ivBotHand != null) ivBotHand.clearAnimation();
        if (ivPlayerHand != null) ivPlayerHand.clearAnimation();
    }
}
