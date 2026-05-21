package com.example.rps;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // Play vs Friend
        LinearLayout btnFriend = findViewById(R.id.btnFriend);
        if (btnFriend != null) {
            btnFriend.setOnClickListener(v ->
                startActivity(new Intent(this, GameFriendActivity.class)));
        }

        // Play vs Bot -> go to difficulty screen
        LinearLayout btnBot = findViewById(R.id.btnBot);
        if (btnBot != null) {
            btnBot.setOnClickListener(v ->
                startActivity(new Intent(this, DifficultyActivity.class)));
        }

        // HOW TO PLAY
            LinearLayout btnHowToPlay = findViewById(R.id.btnHowToPlay);
        if (btnHowToPlay != null) {
            btnHowToPlay.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setTitle(R.string.how_to_play_title)
                    .setMessage(R.string.how_to_play_message)
                    .setPositiveButton(R.string.got_it, null)
                    .show()
            );
        }

        // Back = exit app
        findViewById(R.id.btnBack).setOnClickListener(v -> finishAffinity());
    }
}
