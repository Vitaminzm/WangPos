package com.symboltech.wangpos.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.symboltech.wangpos.interfaces.OnDrawableClickListener;


/**
 * 拓展edittext  可以实现drawable 点击
 * @author so
 *
 */
public class DrawableEditText extends EditText {

	private OnDrawableClickListener onDrawableClickListener;

	public DrawableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public DrawableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawableEditText(Context context) {
		super(context);
	}

	public OnDrawableClickListener getOnDrawableClickListener() {
		return onDrawableClickListener;
	}

	public void setOnDrawableClickListener(OnDrawableClickListener onDrawableClickListener) {
		this.onDrawableClickListener = onDrawableClickListener;
	}

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
		super.setCompoundDrawables(left, top, right, bottom);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getCompoundDrawables()[2] != null) {
			if (event.getX() > (getWidth() - getTotalPaddingRight()+ 60) && (event.getX() < ((getWidth() - getPaddingRight())+80))) {
				if (onDrawableClickListener != null) {
					onDrawableClickListener.onDrawableclick();
				}
				return false;
			}
		}
		return super.onTouchEvent(event);
	}

}
