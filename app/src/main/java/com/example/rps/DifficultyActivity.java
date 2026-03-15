package com.example.rps;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {

    // difficulty: 0=EASY, 1=NORMAL, 2=HARD
    private int difficulty = 0;

    private static final String[] LABELS = {"EASY", "NORMAL", "HARD"};
    private static final String[] FACES   = {"😊",   "😤",    "😈"};
    private static final int[]    COLORS  = {
        0xFF43A047,  // easy green
        0xFFF9A825,  // normal amber
        0xFF8E24AA   // hard purple
    };

    private TextView tvDifficultyLabel, tvBotFace;
    private View sliderFill;
    private Button btnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_difficulty);

        tvDifficultyLabel = findViewById(R.id.tvDifficultyLabel);
        tvBotFace         = findViewById(R.id.tvBotFace);
        sliderFill        = findViewById(R.id.sliderFill);
        btnPlay           = findViewById(R.id.btnPlay);
        SeekBar seekBar   = findViewById(R.id.seekDifficulty);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Map 0-100 → 0,1,2
                int newDiff = progress < 34 ? 0 : progress < 67 ? 1 : 2;
                if (newDiff != difficulty) {
                    difficulty = newDiff;
                    updateUI();
                }
                // Update fill width proportionally
                updateFillWidth(seekBar, progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                // Snap to nearest difficulty
                int snapped = difficulty == 0 ? 0 : difficulty == 1 ? 50 : 100;
                seekBar.setProgress(snapped);
                updateFillWidth(seekBar, snapped);
            }
        });

        // PLAY
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameBotActivity.class);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        });

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        updateUI();
        updateFillWidth(seekBar, 0);
    }

    private void updateUI() {
        tvDifficultyLabel.setText(LABELS[difficulty]);
        tvBotFace.setText(FACES[difficulty]);
        int col = COLORS[difficulty];

        // Color the label and play button
        tvDifficultyLabel.setTextColor(col);
        btnPlay.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(col));

        // Color the slider fill
        sliderFill.setBackgroundColor(col);
    }

    private void updateFillWidth(SeekBar seekBar, int progress) {
        // Update fill view width proportionally to progress
        sliderFill.post(() -> {
            int totalWidth = seekBar.getWidth() - seekBar.getPaddingLeft() - seekBar.getPaddingRight();
            int fillWidth = (int) (totalWidth * progress / 100f);
            ViewGroup.LayoutParams params = sliderFill.getLayoutParams();
            params.width = Math.max(fillWidth, 44); // minimum = thumb size
            sliderFill.setLayoutParams(params);
        });
    }
}
