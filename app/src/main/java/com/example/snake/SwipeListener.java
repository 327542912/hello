package com.example.snake;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 主流贪吃蛇滑动监听器：按一下、滑一下、松手，一次滑动只允许一个方向。
 * 阈值按屏幕密度换算（约 30dp），避免误触。
 */
public class SwipeListener implements View.OnTouchListener {

    public interface OnSwipe {
        void onSwipe(int dx, int dy);
        void onTap();
    }

    /** 触发一次转向所需的最小位移（dp） */
    private static final float SWIPE_DP = 30f;
    /** 判定为点击的最大位移（dp） */
    private static final float TAP_SLOP_DP = 20f;

    private final OnSwipe callback;
    private final float density;
    private final float swipeThreshold;
    private final float tapSlop;

    private float downX;
    private float downY;
    private boolean moved;

    public SwipeListener(Context context, OnSwipe callback) {
        this.callback = callback;
        this.density = context.getResources().getDisplayMetrics().density;
        this.swipeThreshold = SWIPE_DP * density;
        this.tapSlop = Math.max(TAP_SLOP_DP * density,
                ViewConfiguration.get(context).getScaledTouchSlop());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                moved = false;
                return true;

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                if (Math.abs(x - downX) > tapSlop || Math.abs(y - downY) > tapSlop) {
                    moved = true;
                }
                // 一次滑动只允许一个方向，基于起始点判断
                float dx = x - downX;
                float dy = y - downY;
                if (Math.abs(dx) >= swipeThreshold || Math.abs(dy) >= swipeThreshold) {
                    if (Math.abs(dx) > Math.abs(dy)) {
                        callback.onSwipe(dx > 0 ? 1 : -1, 0);
                    } else {
                        callback.onSwipe(0, dy > 0 ? 1 : -1);
                    }
                }
                return true;
            }

            case MotionEvent.ACTION_UP: {
                float x = event.getX();
                float y = event.getY();
                // 抬手前若还没触发过转向，再补判一次（慢速短滑也能识别）
                float dx = x - downX;
                float dy = y - downY;
                if (Math.abs(dx) >= swipeThreshold || Math.abs(dy) >= swipeThreshold) {
                    if (Math.abs(dx) > Math.abs(dy)) {
                        callback.onSwipe(dx > 0 ? 1 : -1, 0);
                    } else {
                        callback.onSwipe(0, dy > 0 ? 1 : -1);
                    }
                } else if (!moved) {
                    callback.onTap();
                }
                return true;
            }

            case MotionEvent.ACTION_CANCEL:
                return true;

            default:
                return true;
        }
    }
}
