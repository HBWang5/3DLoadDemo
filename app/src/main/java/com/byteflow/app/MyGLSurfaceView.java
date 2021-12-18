package com.byteflow.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_3D_MODEL;

@SuppressLint("ViewConstructor")
public class MyGLSurfaceView extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener {

    private float mPreviousY;
    private float mPreviousX;
    private int mXAngle;
    private int mYAngle;

    private final MyGLRender mGLRender;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private final ScaleGestureDetector mScaleGestureDetector;
    private float mPreScale = 1.0f;
    private float mCurScale = 1.0f;
    private long mLastMultiTouchTime;


    public MyGLSurfaceView(Context context, MyGLRender glRender) {
        this(context, glRender, null);
    }

    public MyGLSurfaceView(Context context, MyGLRender glRender, AttributeSet attrs) {
        super(context, attrs);
        this.setEGLContextClientVersion(2);
        mGLRender = glRender;
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        setRenderer(mGLRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getPointerCount() == 1) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - mLastMultiTouchTime > 200) {
                float y = e.getY();
                float x = e.getX();
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    float dy = y - mPreviousY;
                    float dx = x - mPreviousX;
                    float TOUCH_SCALE_FACTOR = 180.0f / 320;
                    mYAngle += dx * TOUCH_SCALE_FACTOR;
                    mXAngle += dy * TOUCH_SCALE_FACTOR;
                }
                mPreviousY = y;
                mPreviousX = x;

                if (mGLRender.getSampleType() == SAMPLE_TYPE_3D_MODEL) {
                    mGLRender.updateTransformMatrix(mXAngle, mYAngle, mCurScale, mCurScale);
                    requestRender();
                }
            }

        } else {
            mScaleGestureDetector.onTouchEvent(e);
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }

        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    public MyGLRender getGLRender() {
        return mGLRender;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mGLRender.getSampleType() == SAMPLE_TYPE_3D_MODEL) {
            float preSpan = detector.getPreviousSpan();
            float curSpan = detector.getCurrentSpan();
            if (curSpan < preSpan) {
                mCurScale = mPreScale - (preSpan - curSpan) / 200;
            } else {
                mCurScale = mPreScale + (curSpan - preSpan) / 200;
            }
            mCurScale = Math.max(0.05f, Math.min(mCurScale, 80.0f));
            mGLRender.updateTransformMatrix(mXAngle, mYAngle, mCurScale, mCurScale);
            requestRender();
        }

        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mPreScale = mCurScale;
        mLastMultiTouchTime = System.currentTimeMillis();

    }

}
