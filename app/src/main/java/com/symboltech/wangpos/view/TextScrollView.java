package com.symboltech.wangpos.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 用于textview滚动，弥补字体分行
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月5日
 * @see
 * @since 1.0
 */
public class TextScrollView extends TextView {

	
	public TextScrollView(Context context) {

		super(context);

	}

	public TextScrollView(Context context, AttributeSet attrs) {

		super(context, attrs);

		// TODO Auto-generated constructor stub
	}

	public TextScrollView(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);

	}

	@Override
	public boolean isFocused() {

		return true;

	}

}
