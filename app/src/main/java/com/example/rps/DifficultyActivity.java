package com.example.rps;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {

    private int difficulty = 0;

    private static final String[] LABELS = {"EASY", "NORMAL", "HARD"};
    // Updated to more expressive emojis
    private static final String[] FACES   = {"😇",   "😎",    "💀"};
    private static final int[]    COLORS  = {
        0xFF4CAF50,  // Material Green 500
        0xFFFFB300,  // Material Amber 600
        0xFFE91E63   // Material Pink 500 (Better for Hard than Purple)
    };

    private TextView tvDifficultyLabel;
    private TextView tvBotFaceFront, tvBotFaceBack;
    private View sliderFill, botFaceContainer, botFaceGlow;
    private Button btnPlay;
    private int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_difficulty);

        tvDifficultyLabel = findViewById(R.id.tvDifficultyLabel);
        tvBotFaceFront    = findViewById(R.id.tvBotFaceFront);
        tvBotFaceBack     = findViewById(R.id.tvBotFaceBack);
        sliderFill        = findViewById(R.id.sliderFill);
        btnPlay           = findViewById(R.id.btnPlay);
        botFaceContainer  = findViewById(R.id.botFaceContainer);
        botFaceGlow       = findViewById(R.id.botFaceGlow);
        SeekBar seekBar   = findViewById(R.id.seekDifficulty);

        currentColor = COLORS[0];
        tvBotFaceFront.setText(FACES[0]);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newDiff = progress < 34 ? 0 : progress < 67 ? 1 : 2;
                if (newDiff != difficulty) {
                    difficulty = newDiff;
                    updateUIAnimated();
                }
                updateFillWidth(seekBar, progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                int snapped = difficulty == 0 ? 0 : difficulty == 1 ? 50 : 100;
                animateSeekBar(seekBar, snapped);
            }
        });

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameBotActivity.class);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        updateUI();
        updateFillWidth(seekBar, seekBar.getProgress());
    }

    private void animateSeekBar(SeekBar seekBar, int targetProgress) {
        ValueAnimator animator = ValueAnimator.ofInt(seekBar.getProgress(), targetProgress);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            seekBar.setProgress(val);
        });
        animator.start();
    }

    private void updateUI() {
        tvDifficultyLabel.setText(LABELS[difficulty]);
        tvBotFaceFront.setText(FACES[difficulty]);
        currentColor = COLORS[difficulty];

        tvDifficultyLabel.setTextColor(currentColor);
        btnPlay.setBackgroundTintList(ColorStateList.valueOf(currentColor));
        sliderFill.setBackgroundTintList(ColorStateList.valueOf(currentColor));
        botFaceGlow.setBackgroundTintList(ColorStateList.valueOf(currentColor));
    }

    private void updateUIAnimated() {
        int targetColor = COLORS[difficulty];
        String targetFace = FACES[difficulty];

        // 1. Smooth Color Cross-fade
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), currentColor, targetColor);
        colorAnimation.setDuration(400);
        colorAnimation.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            tvDifficultyLabel.setTextColor(color);
            btnPlay.setBackgroundTintList(ColorStateList.valueOf(color));
            sliderFill.setBackgroundTintList(ColorStateList.valueOf(color));
            botFaceGlow.setBackgroundTintList(ColorStateList.valueOf(color));
        });
        colorAnimation.start();
        currentColor = targetColor;

        // 2. Smooth Emoji Cross-fade & Scale
        tvBotFaceBack.setText(tvBotFaceFront.getText());
        tvBotFaceBack.setAlpha(1f);
        tvBotFaceBack.setScaleX(1f);
        tvBotFaceBack.setScaleY(1f);

        tvBotFaceFront.setText(targetFace);
        tvBotFaceFront.setAlpha(0f);
        tvBotFaceFront.setScaleX(0.5f);
        tvBotFaceFront.setScaleY(0.5f);

        tvBotFaceBack.animate()
                .alpha(0f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        tvBotFaceFront.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 3. Label Transition (Slide + Fade)
        tvDifficultyLabel.animate()
                .alpha(0f)
                .translationY(-20f)
                .setDuration(150)
                .withEndAction(() -> {
                    tvDifficultyLabel.setText(LABELS[difficulty]);
                    tvDifficultyLabel.setTranslationY(20f);
                    tvDifficultyLabel.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();

        // 4. Glow Pulse
        botFaceGlow.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .alpha(1.0f)
                .setDuration(200)
                .withEndAction(() -> botFaceGlow.animate().scaleX(1.0f).scaleY(1.0f).alpha(0.8f).setDuration(200).start())
                .start();
    }

    private void updateFillWidth(SeekBar seekBar, int progress) {
        sliderFill.post(() -> {
            int totalWidth = seekBar.getWidth() - seekBar.getPaddingLeft() - seekBar.getPaddingRight();
            int fillWidth = (int) (totalWidth * progress / 100f);
            ViewGroup.LayoutParams params = sliderFill.getLayoutParams();
            params.width = fillWidth;
            sliderFill.setLayoutParams(params);
            sliderFill.setTranslationX(seekBar.getPaddingLeft());
        });
    }
}
