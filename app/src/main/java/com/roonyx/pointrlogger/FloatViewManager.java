package com.roonyx.pointrlogger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

import static android.content.Context.WINDOW_SERVICE;

public class FloatViewManager {

    private boolean mIsFloatViewShowing;

    private Activity mActivity;
    private WindowManager mWindowManager;
    private View mFloatView;
    private WindowManager.LayoutParams mFloatViewLayoutParams;
    private boolean mFloatViewTouchConsumedByMove = false;
    private int mFloatViewLastX;
    private int mFloatViewLastY;
    private int mFloatViewFirstX;
    private int mFloatViewFirstY;

    @SuppressLint("InflateParams")
    public FloatViewManager(Activity activity) {
        mActivity = activity;
        LayoutInflater inflater = LayoutInflater.from(activity);
        mFloatView = inflater.inflate(R.layout.float_view_layout, null);

        AppCompatImageView imageButton = mFloatView.findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissFloatView();
            }
        });

        mFloatView.setOnClickListener(mFloatViewOnClickListener);
        mFloatView.setOnTouchListener(mFloatViewOnTouchListener);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mFloatViewLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                flags,
                PixelFormat.TRANSLUCENT);

    }

    public void dismissFloatView() {
        if (mIsFloatViewShowing) {
            mIsFloatViewShowing = false;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mWindowManager != null) {
                        mWindowManager.removeViewImmediate(mFloatView);
                    }
                }
            });
        }
    }

    public void showFloatView() {
        if (!mIsFloatViewShowing) {
            mIsFloatViewShowing = true;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mActivity.isFinishing()) {
                        mWindowManager = (WindowManager) mActivity.getSystemService(WINDOW_SERVICE);
                        if (mWindowManager != null) {
                            mWindowManager.addView(mFloatView, mFloatViewLayoutParams);
                        }
                    }
                }
            });
        }
    }

    public boolean isFloatViewShowing() {
        return mIsFloatViewShowing;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private View.OnClickListener mFloatViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "Float view is clicked!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private View.OnTouchListener mFloatViewOnTouchListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            WindowManager.LayoutParams prm = mFloatViewLayoutParams;
            int totalDeltaX = mFloatViewLastX - mFloatViewFirstX;
            int totalDeltaY = mFloatViewLastY - mFloatViewFirstY;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mFloatViewLastX = (int) event.getRawX();
                    mFloatViewLastY = (int) event.getRawY();
                    mFloatViewFirstX = mFloatViewLastX;
                    mFloatViewFirstY = mFloatViewLastY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) event.getRawX() - mFloatViewLastX;
                    int deltaY = (int) event.getRawY() - mFloatViewLastY;
                    mFloatViewLastX = (int) event.getRawX();
                    mFloatViewLastY = (int) event.getRawY();
                    if (Math.abs(totalDeltaX) >= 5 || Math.abs(totalDeltaY) >= 5) {
                        if (event.getPointerCount() == 1) {
                            prm.x += deltaX;
                            prm.y += deltaY;
                            mFloatViewTouchConsumedByMove = true;
                            if (mWindowManager != null) {
                                mWindowManager.updateViewLayout(mFloatView, prm);
                            }
                        } else {
                            mFloatViewTouchConsumedByMove = false;
                        }
                    } else {
                        mFloatViewTouchConsumedByMove = false;
                    }
                    break;
                default:
                    break;
            }
            return mFloatViewTouchConsumedByMove;
        }
    };
}