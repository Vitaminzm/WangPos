package com.symboltech.wangpos.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.view.MyscollView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.view_pager) ViewPager view_pager;
    @Bind(R.id.myscollview) MyscollView myscollView;

    @Override
    protected void initData() {
        myscollView.setView_pager(view_pager);
        view_pager.setPageTransformer(true, new DepthPageTransformer());
        view_pager.setAdapter(new HorizontalPagerAdapter(getApplicationContext(), this));
        view_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                myscollView.setOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                myscollView.setPosition(view_pager.getCurrentItem());
            }
        });
    }

    @Override
    protected void initView() {
        MyApplication.addActivity(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @Override
    protected void recycleMemery() {
        MyApplication.delActivity(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.rl_lockscreen:
                lockscreen();
                break;

        }
    }

    /**
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(lockscreen)
     */
    private void lockscreen() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_LOCKSCREEN);
        startActivity(intent);
        this.finish();
    }
    public class HorizontalPagerAdapter extends PagerAdapter {

        public Context mContext;
        public LayoutInflater mLayoutInflater;
        public List<View> views;
        public View.OnClickListener onClickListener;

        public HorizontalPagerAdapter(Context context, View.OnClickListener onClickListener) {
            super();
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            this.onClickListener = onClickListener;
            initView();
        }

        public void initView() {
            views = new ArrayList<View>();
            View v1 = mLayoutInflater.inflate(R.layout.view_button_main, null);
            ButterKnife.findById(v1, R.id.rl_member).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_pay).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_billprint).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_sendcarcoupon).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_lockscreen).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_change).setOnClickListener(onClickListener);
            views.add(v1);
            View v2 = mLayoutInflater.inflate(R.layout.view_button_offline_main, null);
            ButterKnife.findById(v2, R.id.rl_upload).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_offline).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_weichat).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_bank).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_salereturn).setOnClickListener(onClickListener);
            views.add(v2);
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

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @SuppressLint("NewApi")
        public void transformPage(View view, float position)
        {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();


            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0)
                {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else
                {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else {
                view.setAlpha(0);
            }
        }
    }
}
