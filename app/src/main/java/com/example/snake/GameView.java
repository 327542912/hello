package com.example.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

/**
 * 贪吃蛇游戏视图：纯 framework 自绘，不依赖任何三方库。
 */
public class GameView extends View {

    public interface OnScoreChanged {
        void onScore(int score);
    }

    private static final int COLS = 20;
    private static final int ROWS = 26;

    private static final int DIR_UP = 0;
    private static final int DIR_RIGHT = 1;
    private static final int DIR_DOWN = 2;
    private static final int DIR_LEFT = 3;

    private final ArrayList<int[]> snake = new ArrayList<>();
    private final int[] food = new int[2];
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int direction = DIR_RIGHT;
    /** 待生效方向：一次滑动只允许一个方向 */
    private int pendingDirection = -1;
    private int score = 0;
    private long stepMs = 180;
    private boolean running = false;
    private boolean gameOver = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float cellW;
    private float cellH;
    private float originX;
    private float originY;

    private OnScoreChanged scoreListener;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnScoreChanged(OnScoreChanged l) {
        this.scoreListener = l;
        notifyScore();
    }

    private void init() {
        setFocusable(true);
        setBackgroundColor(0xFF1B2A20);
        reset();
    }

    public void reset() {
        snake.clear();
        int cx = COLS / 2;
        int cy = ROWS / 2;
        snake.add(new int[]{cx, cy});
        snake.add(new int[]{cx - 1, cy});
        snake.add(new int[]{cx - 2, cy});
        direction = DIR_RIGHT;
        pendingDirection = -1;
        score = 0;
        stepMs = 180;
        gameOver = false;
        spawnFood();
        notifyScore();
        invalidate();
    }

    private void notifyScore() {
        if (scoreListener != null) {
            scoreListener.onScore(score);
        }
    }

    private void spawnFood() {
        int tries = 0;
        do {
            food[0] = random.nextInt(COLS);
            food[1] = random.nextInt(ROWS);
            tries++;
        } while (occupied(food[0], food[1]) && tries < 2000);
    }

    private boolean occupied(int x, int y) {
        for (int i = 0; i < snake.size(); i++) {
            int[] s = snake.get(i);
            if (s[0] == x && s[1] == y) return true;
        }
        return false;
    }

    /**
     * 由外部（滑动/按键）调用。一次滑动只允许一个方向，进入 pendingDirection。
     */
    public void changeDirection(int dx, int dy) {
        if (gameOver) return;
        int nd;
        if (dx == 1) nd = DIR_RIGHT;
        else if (dx == -1) nd = DIR_LEFT;
        else if (dy == 1) nd = DIR_DOWN;
        else if (dy == -1) nd = DIR_UP;
        else return;

        // 与当前方向比较，禁止 180 度反向和重复同向
        if (nd == direction) return;
        if ((direction == DIR_UP && nd == DIR_DOWN)
                || (direction == DIR_DOWN && nd == DIR_UP)
                || (direction == DIR_LEFT && nd == DIR_RIGHT)
                || (direction == DIR_RIGHT && nd == DIR_LEFT)) {
            return;
        }
        // 一次滑动只允许一个方向
        pendingDirection = nd;
    }

    public void resume() {
        if (running) return;
        running = true;
        handler.postDelayed(stepRunnable, stepMs);
    }

    public void pause() {
        running = false;
        handler.removeCallbacks(stepRunnable);
    }

    private final Runnable stepRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            step();
            invalidate();
            if (running) {
                handler.postDelayed(this, stepMs);
            }
        }
    };

    private void step() {
        if (gameOver) return;
        // 消费 pendingDirection
        if (pendingDirection != -1) {
            direction = pendingDirection;
            pendingDirection = -1;
        }

        int[] head = snake.get(0);
        int hx = head[0];
        int hy = head[1];
        switch (direction) {
            case DIR_UP: hy--; break;
            case DIR_DOWN: hy++; break;
            case DIR_LEFT: hx--; break;
            default: hx++; break;
        }

        // 撞墙
        if (hx < 0 || hx >= COLS || hy < 0 || hy >= ROWS) {
            gameOver = true;
            running = false;
            return;
        }
        // 撞自己（尾巴即将移走的那格允许进入）
        boolean eat = (hx == food[0] && hy == food[1]);
        int limit = eat ? snake.size() : snake.size() - 1;
        for (int i = 0; i < limit; i++) {
            int[] s = snake.get(i);
            if (s[0] == hx && s[1] == hy) {
                gameOver = true;
                running = false;
                return;
            }
        }

        snake.add(0, new int[]{hx, hy});
        if (eat) {
            score += 10;
            notifyScore();
            if (stepMs > 70) stepMs -= 4;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 保持格子为正方形，居中绘制
        cellW = (float) w / COLS;
        cellH = (float) h / ROWS;
        float cell = Math.min(cellW, cellH);
        cellW = cell;
        cellH = cell;
        originX = (w - cell * COLS) / 2f;
        originY = (h - cell * ROWS) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (cellW <= 0) return;

        // 棋盘底
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF223528);
        rect.set(originX, originY, originX + cellW * COLS, originY + cellH * ROWS);
        canvas.drawRect(rect, paint);

        // 网格
        paint.setColor(0xFF2A4030);
        paint.setStrokeWidth(1f);
        for (int i = 1; i < COLS; i++) {
            float x = originX + i * cellW;
            canvas.drawLine(x, originY, x, originY + cellH * ROWS, paint);
        }
        for (int j = 1; j < ROWS; j++) {
            float y = originY + j * cellH;
            canvas.drawLine(originX, y, originX + cellW * COLS, y, paint);
        }

        // 食物
        paint.setColor(0xFFFF5252);
        rect.set(originX + food[0] * cellW + cellW * 0.12f,
                originY + food[1] * cellH + cellH * 0.12f,
                originX + (food[0] + 1) * cellW - cellW * 0.12f,
                originY + (food[1] + 1) * cellH - cellH * 0.12f);
        canvas.drawOval(rect, paint);

        // 蛇
        for (int i = 0; i < snake.size(); i++) {
            int[] s = snake.get(i);
            paint.setColor(i == 0 ? 0xFF9CFF6E : 0xFF4CD964);
            float pad = i == 0 ? 0.04f : 0.10f;
            rect.set(originX + s[0] * cellW + cellW * pad,
                    originY + s[1] * cellH + cellH * pad,
                    originX + (s[0] + 1) * cellW - cellW * pad,
                    originY + (s[1] + 1) * cellH - cellH * pad);
            canvas.drawRoundRect(rect, cellW * 0.22f, cellH * 0.22f, paint);
        }

        // 结束遮罩
        if (gameOver) {
            paint.setColor(0xCC000000);
            rect.set(originX, originY, originX + cellW * COLS, originY + cellH * ROWS);
            canvas.drawRect(rect, paint);

            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(cellW * 2.2f);
            paint.setFakeBoldText(true);
            float cx = originX + cellW * COLS / 2f;
            float cy = originY + cellH * ROWS / 2f;
            canvas.drawText("游戏结束", cx, cy - cellW * 0.6f, paint);

            paint.setTextSize(cellW * 1.3f);
            paint.setFakeBoldText(false);
            canvas.drawText("得分 " + score, cx, cy + cellW * 1.2f, paint);
            canvas.drawText("点击屏幕重新开始", cx, cy + cellW * 3.0f, paint);
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getScore() {
        return score;
    }
}
