package com.symboltech.wangpos.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.ScllorTabView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkLogActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_collection)TextView text_collection;
    @Bind(R.id.text_return)TextView text_return;
    @Bind(R.id.text_total)TextView text_total;
    @Bind(R.id.view_pager_statistics)ViewPager view_pager_statistics;
    @Bind(R.id.scllortabview)ScllorTabView scllortabview;

    static class MyHandler extends Handler {
        WeakReference<BaseActivity> mActivity;

        MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity theActivity = mActivity.get();
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(theActivity,msg);
                    break;
            }
        }
    }

    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
        title_text_content.setText(getString(R.string.job_report));

    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_return_goods_by_normal);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        scllortabview.setTabNum(3);
        view_pager_statistics.setAdapter(new MyPagerAdapter(getApplicationContext()));
        view_pager_statistics.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scllortabview.setOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int position = view_pager_statistics.getCurrentItem();
                scllortabview.setCurrentNum(position);
                switchUI(position);
            }
        });
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    public void switchUI(int position){
        if(position == 0){
            text_collection.setTextColor(getResources().getColor(R.color.green));
            text_return.setTextColor(getResources().getColor(R.color.white_font_color));
            text_total.setTextColor(getResources().getColor(R.color.white_font_color));
        }else if(position == 1){
            text_collection.setTextColor(getResources().getColor(R.color.white_font_color));
            text_return.setTextColor(getResources().getColor(R.color.green));
            text_total.setTextColor(getResources().getColor(R.color.white_font_color));
        }else if(position == 2){
            text_collection.setTextColor(getResources().getColor(R.color.white_font_color));
            text_return.setTextColor(getResources().getColor(R.color.white_font_color));
            text_total.setTextColor(getResources().getColor(R.color.green));
        }
    }
    @OnClick({R.id.title_icon_back})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
        }
    }

    class MyPagerAdapter extends PagerAdapter {
        public Context mContext;
        public LayoutInflater mLayoutInflater;
        public List<View> views;
        public ListView listview_collection, listview_return, listview_total;
        public TextView text_return_count_total, text_return_money_total, text_collection_money_total, text_collection_count_total, text_count_total, text_money_total;
        public MyPagerAdapter(Context context){
            super();
            initView();
        }
        public void initView() {
            views = new ArrayList<>();
            View v1 = mLayoutInflater.inflate(R.layout.view_collect, null);
            listview_collection = ButterKnife.findById(v1, R.id.listview);
            text_collection_money_total = ButterKnife.findById(v1, R.id.text_collection_money_total);
            text_collection_count_total = ButterKnife.findById(v1, R.id.text_collection_count_total);
            views.add(v1);
            View v2 = mLayoutInflater.inflate(R.layout.view_return, null);
            listview_return = ButterKnife.findById(v1, R.id.listview);
            text_return_count_total = ButterKnife.findById(v1, R.id.text_return_count_total);
            text_return_money_total = ButterKnife.findById(v1, R.id.text_return_money_total);
            views.add(v2);
            View v3 = mLayoutInflater.inflate(R.layout.view_button_offline_main, null);
            listview_total = ButterKnife.findById(v1, R.id.listview);
            text_count_total = ButterKnife.findById(v1, R.id.text_count_total);
            text_money_total = ButterKnife.findById(v1, R.id.text_money_total);
            views.add(v3);
        }
        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
