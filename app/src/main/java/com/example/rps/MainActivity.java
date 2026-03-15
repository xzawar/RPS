package com.example.rps;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep screen on and full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // Play vs Friend
        LinearLayout btnFriend = findViewById(R.id.btnFriend);
        btnFriend.setOnClickListener(v ->
            startActivity(new Intent(this, GameFriendActivity.class)));

        // Play vs Bot → go to difficulty screen
        LinearLayout btnBot = findViewById(R.id.btnBot);
        btnBot.setOnClickListener(v ->
            startActivity(new Intent(this, DifficultyActivity.class)));

        // HOW TO PLAY
        findViewById(R.id.btnHowToPlay).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("How to Play")
                .setMessage(
                    "🎮 ROCK PAPER SCISSORS\n\n" +
                    "✊ Rock beats ✌ Scissors\n" +
                    "✋ Paper beats ✊ Rock\n" +
                    "✌ Scissors beats ✋ Paper\n\n" +
                    "⏱ Press your choice before the hand stops!\n\n" +
                    "🏆 First player to WIN 3 rounds wins the match."
                )
                .setPositiveButton("GOT IT!", null)
                .show()
        );

        // Back = exit app
        findViewById(R.id.btnBack).setOnClickListener(v -> finishAffinity());
    }
}
