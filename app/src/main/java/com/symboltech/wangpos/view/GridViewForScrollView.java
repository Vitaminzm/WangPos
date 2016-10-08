package com.symboltech.wangpos.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListView;

/**
 * 用于可以在ScrollView中根据内容大小自动扩展的ListView
 * Created by ZM on 2015/9/19 0019 13:10.
 */
public class GridViewForScrollView  extends GridView {
    public GridViewForScrollView(Context context) {
        super(context);
    }
    public GridViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public GridViewForScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    /**
     * 重写该方法，达到使ListView适应ScrollView的效果
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
