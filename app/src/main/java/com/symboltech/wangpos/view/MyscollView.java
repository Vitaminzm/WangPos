package com.symboltech.wangpos.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.symboltech.wangpos.R;

/**
 * Created by ZM on 2016/9/12 0012.
 */
public class MyscollView extends View
{

    private int mWidth;
    private int mHeight;

    private int mItemWidth = 112;
    private int mItemHeight = 6;
    private boolean once;
    private boolean isfinish;

    private int mMargin = 16;
    private int mCurrentNum;
    private int mCurrentNum1;
    private float mOffset;

    public ViewPager view_pager;

    int leftMargin;
    int topMargin;
    private Paint mPaint;
    public MyscollView(Context context) {
        super(context);
    }

    public MyscollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyscollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setView_pager(ViewPager view_pager) {
        this.view_pager = view_pager;
    }

    public void setOffset(int position, float offset) {
//        if (offset == 0) {
//            return;
//        }
        mCurrentNum = position;
        mOffset = offset;
        invalidate();
    }

    public void setPosition(int position) {
        mCurrentNum1 = position;
        isfinish = true;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();
        if (!once)
        {
            initBitmap();
            float top = topMargin;
            float bottom = topMargin + mItemHeight;
            mPaint.setColor(getResources().getColor(R.color.colorPrimary));
            canvas.drawRect(leftMargin, top, leftMargin + mItemWidth, bottom, mPaint);
            mPaint.setColor(getResources().getColor(R.color.scoll_color));
            canvas.drawRect(leftMargin + mItemWidth + mMargin, top, leftMargin + mItemWidth * 2 + mMargin, bottom, mPaint);
        }else
        {
            if(isfinish){
                float left = leftMargin;
                final float right = leftMargin + mItemWidth;
                final float top = topMargin;
                final float bottom = topMargin + mItemHeight;
                if(mCurrentNum1 == 0){
                    mPaint.setColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    mPaint.setColor(getResources().getColor(R.color.scoll_color));
                }
                canvas.drawRect(left, top, right, bottom, mPaint);

                float left1 = leftMargin + mItemWidth + mMargin;
                float right1 = left1 + mItemWidth;
                if(mCurrentNum1 == 0){
                    mPaint.setColor(getResources().getColor(R.color.scoll_color));
                }else{
                    mPaint.setColor(getResources().getColor(R.color.colorPrimary));
                }
                canvas.drawRect(left1, top, right1, bottom, mPaint);
                isfinish = false;
            }else{
                float top = topMargin;
                float bottom = topMargin + mItemHeight;
                mPaint.setColor(getResources().getColor(R.color.scoll_color));
                canvas.drawRect(leftMargin, top, leftMargin + mItemWidth, bottom, mPaint);
                canvas.drawRect(leftMargin + mItemWidth + mMargin, top, leftMargin + mItemWidth*2 + mMargin, bottom, mPaint);

                //根据位置和偏移量来计算滑动条的位置
                mPaint.setColor(getResources().getColor(R.color.colorPrimary));
                float left = (mCurrentNum + mOffset ) * mItemWidth + leftMargin;
                final float right = leftMargin + mItemWidth;
                canvas.drawRect(left, top, right, bottom, mPaint);

                //根据位置和偏移量来计算滑动条的位置
                float left1 = leftMargin + mItemWidth + mMargin;
                float right1 = left1+ (mCurrentNum + mOffset ) * mItemWidth;
                canvas.drawRect(left1, top, right1, bottom, mPaint);
            }

        }
        once = true;


    }

    private void initBitmap() {

        mPaint = new Paint();

        mPaint.setStyle(Paint.Style.FILL);

        leftMargin = (mWidth - mMargin)/2-mItemWidth;
        topMargin = (mHeight - mItemHeight)/2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                float x=event.getX();
                float y = event.getY();
                if(x>leftMargin && x <(leftMargin + mItemWidth)){

                    view_pager.setCurrentItem(0);
                    setPosition(0);
                    return true;
                }else if(x>leftMargin + mItemWidth + mMargin && x <(leftMargin + mItemWidth*2 + mMargin)) {
                    view_pager.setCurrentItem(1);
                    setPosition(1);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }
}
