package com.example.snake;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * 独立的滑动手势监听器（不依赖 androidx）。
 */
public class SwipeListener implements View.OnTouchListener {

    public interface OnSwipe {
        void onSwipe(int dx, int dy);
        void onTap();
    }

    private final GestureDetector detector;

    public SwipeListener(Context context, final OnSwipe callback) {
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_MIN_DISTANCE = 60;
            private static final int SWIPE_MIN_VELOCITY = 80;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                callback.onTap();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (Math.abs(dx) > SWIPE_MIN_DISTANCE && Math.abs(vx) > SWIPE_MIN_VELOCITY) {
                        callback.onSwipe(dx > 0 ? 1 : -1, 0);
                        return true;
                    }
                } else {
                    if (Math.abs(dy) > SWIPE_MIN_DISTANCE && Math.abs(vy) > SWIPE_MIN_VELOCITY) {
                        callback.onSwipe(0, dy > 0 ? 1 : -1);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return detector.onTouchEvent(event);
    }
}
