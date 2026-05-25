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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {

    private int difficulty = 0;

    private static final String[] LABELS = {"EASY", "NORMAL", "HARD"};
    // PNG drawable resources
    private static final int[] DRAWABLE_IDS = {R.drawable.smile, R.drawable.cool, R.drawable.skull};
    private static final int[]    COLORS  = {
            0xFF4CAF50,  // Material Green 500
            0xFFFFB300,  // Material Amber 600
            0xFFD32F2F,  // Material Pink 500 (Better for Hard than Purple)
    };

    private TextView tvDifficultyLabel;
    private ImageView ivBotFaceFront, ivBotFaceBack;
    private View botFaceContainer;
    private Button btnPlay;
    private int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_difficulty);

        tvDifficultyLabel = findViewById(R.id.tvDifficultyLabel);
        ivBotFaceFront    = findViewById(R.id.tvBotFaceFront);
        ivBotFaceBack     = findViewById(R.id.tvBotFaceBack);
        btnPlay           = findViewById(R.id.btnPlay);
        botFaceContainer  = findViewById(R.id.botFaceContainer);
        SeekBar seekBar   = findViewById(R.id.seekDifficulty);

        currentColor = COLORS[0];
        ivBotFaceFront.setImageResource(DRAWABLE_IDS[0]);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newDiff = progress < 34 ? 0 : progress < 67 ? 1 : 2;
                if (newDiff != difficulty) {
                    difficulty = newDiff;
                    updateUIAnimated();
                }
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
        tvDifficultyLabel.setText(difficulty == 0 ? getString(R.string.easy) : difficulty == 1 ? getString(R.string.normal) : getString(R.string.hard));
<<<<<<< HEAD
        ivBotFaceFront.setImageResource(DRAWABLE_IDS[difficulty]);
=======
        tvBotFaceFront.setText(FACES[difficulty]);
>>>>>>> ac07274fe90e5a6ba2b1457c467004f3ef5e6bd3
        currentColor = COLORS[difficulty];

        tvDifficultyLabel.setTextColor(currentColor);
    }

    private void updateUIAnimated() {
<<<<<<< HEAD
        int targetDrawable = DRAWABLE_IDS[difficulty];
=======
        int targetColor = COLORS[difficulty];
        String targetFace = FACES[difficulty];
>>>>>>> ac07274fe90e5a6ba2b1457c467004f3ef5e6bd3
        String targetLabel = difficulty == 0 ? getString(R.string.easy) : difficulty == 1 ? getString(R.string.normal) : getString(R.string.hard);

        currentColor = COLORS[difficulty];

        // Cancel any running animations first to avoid overlap
        ivBotFaceBack.animate().cancel();
        ivBotFaceFront.animate().cancel();
        tvDifficultyLabel.animate().cancel();

        // 1. Image: crossfade with a gentle scale-up pop on the incoming image
        ivBotFaceBack.setImageDrawable(ivBotFaceFront.getDrawable());
        ivBotFaceBack.setAlpha(1f);
        ivBotFaceBack.setScaleX(1f);
        ivBotFaceBack.setScaleY(1f);

        ivBotFaceFront.setImageResource(targetDrawable);
        ivBotFaceFront.setAlpha(0f);
        ivBotFaceFront.setScaleX(0.75f);
        ivBotFaceFront.setScaleY(0.75f);

        // Old image shrinks and fades out
        ivBotFaceBack.animate()
                .alpha(0f)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // New image grows and fades in — delayed slightly so the swap feels intentional
        ivBotFaceFront.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(100)
                .setDuration(350)
                .setInterpolator(new android.view.animation.OvershootInterpolator(0.8f))
                .withEndAction(() -> ivBotFaceFront.setScaleX(1f))
                .start();

        // 2. Label: fade out down, swap text, fade in from above
        tvDifficultyLabel.animate()
                .alpha(0f)
                .translationY(12f)
                .setDuration(160)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    tvDifficultyLabel.setText(targetLabel);
<<<<<<< HEAD
                    tvDifficultyLabel.setTextColor(currentColor);
                    tvDifficultyLabel.setTranslationY(-12f);
=======
                    tvDifficultyLabel.setTranslationY(20f);
>>>>>>> ac07274fe90e5a6ba2b1457c467004f3ef5e6bd3
                    tvDifficultyLabel.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(220)
                            .setStartDelay(0)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
    }
}