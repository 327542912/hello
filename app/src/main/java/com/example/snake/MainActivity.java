package com.example.snake;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class MainActivity extends Activity {

    private GameView gameView;
    private TextView tvScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = (GameView) findViewById(R.id.gameView);
        tvScore = (TextView) findViewById(R.id.tvScore);

        gameView.setOnScoreChanged(new GameView.OnScoreChanged() {
            @Override
            public void onScore(int score) {
                tvScore.setText("分数: " + score);
            }
        });

        gameView.setOnTouchListener(new SwipeListener(this, new SwipeListener.OnSwipe() {
            @Override
            public void onSwipe(int dx, int dy) {
                gameView.changeDirection(dx, dy);
            }

            @Override
            public void onTap() {
                if (gameView.isGameOver()) {
                    gameView.reset();
                    gameView.resume();
                }
            }
        }));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                gameView.changeDirection(0, -1);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                gameView.changeDirection(0, 1);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                gameView.changeDirection(-1, 0);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                gameView.changeDirection(1, 0);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
    }
}
