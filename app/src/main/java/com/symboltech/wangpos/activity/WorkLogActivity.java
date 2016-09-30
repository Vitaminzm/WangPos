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
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.ReportTableAdapter;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.RefundReportInfo;
import com.symboltech.wangpos.msg.entity.ReportDetailInfo;
import com.symboltech.wangpos.msg.entity.ReportInfo;
import com.symboltech.wangpos.msg.entity.SaleReportInfo;
import com.symboltech.wangpos.msg.entity.TotalReportInfo;
import com.symboltech.wangpos.result.ReportResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.ScllorTabView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkLogActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_collection)TextView text_collection;
    @Bind(R.id.text_return)TextView text_return;
    @Bind(R.id.text_total)TextView text_total;
    @Bind(R.id.text_desk_code)TextView text_desk_code;
    @Bind(R.id.text_shop)TextView text_shop;
    @Bind(R.id.view_pager_statistics)ViewPager view_pager_statistics;
    @Bind(R.id.scllortabview)ScllorTabView scllortabview;

    private ReportInfo reportInfo; // 报表数据
    private String flag;  //当前是班报还是日报   job - 班报
    private MyPagerAdapter myPagerAdapter;

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
        text_desk_code.setText(SpSaveUtils.read(this, ConstantData.CASHIER_DESK_CODE, ""));
        text_shop.setText(SpSaveUtils.read(this, ConstantData.SHOP_NAME, ""));
        flag = getIntent().getStringExtra(ConstantData.FLAG);
        Map<String, String> map = new HashMap<>();
        if (ConstantData.JOB.equals(flag)) {
            title_text_content.setText(getString(R.string.job_report));
            map.put("person_id", SpSaveUtils.read(WorkLogActivity.this, ConstantData.CASHIER_ID, ""));
        } else {
            title_text_content.setText(getString(R.string.day_report));
        }
        HttpRequestUtil.getinstance().getReportInfo(map, ReportResult.class, new HttpActionHandle<ReportResult>() {

            @Override
            public void handleActionStart() {
                super.handleActionStart();
                startwaitdialog();
            }

            @Override
            public void handleActionFinish() {
                super.handleActionFinish();
                closewaitdialog();
            }

            @Override
            public void handleActionError(String actionName, String errmsg) {
                Toast.makeText(mContext, errmsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleActionSuccess(String actionName, ReportResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    reportInfo = result.getReportInfo();
                    WorkLogActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myPagerAdapter.refreshData(reportInfo.getSale(), reportInfo.getRefund(), reportInfo.getTotal());
                        }
                    });
                }else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_work_log);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        scllortabview.setTabNum(3);
        scllortabview.setSelectedColor(getResources().getColor(R.color.green), getResources().getColor(R.color.green));
        myPagerAdapter = new MyPagerAdapter(getApplicationContext());
        view_pager_statistics.setPageTransformer(true, new DepthPageTransformer());
        view_pager_statistics.setAdapter(myPagerAdapter);
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
        text_collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager_statistics.setCurrentItem(0);
            }
        });
        text_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager_statistics.setCurrentItem(1);
            }
        });
        text_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager_statistics.setCurrentItem(2);
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
    @OnClick({R.id.title_icon_back, R.id.text_print})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_print:
                if(reportInfo == null) {
                    ToastUtils.sendtoastbyhandler(handler,"暂无数据");
                    return;
                }
                if(ConstantData.JOB.equals(flag)) {
                   // PrintUtils.getinstance().printReportFrom(true, reportInfo);
                }else {
                   // PrintUtils.getinstance().printReportFrom(false, reportInfo);
                }
                break;
        }
    }

    class MyPagerAdapter extends PagerAdapter {
        public Context mContext;
        public LayoutInflater mLayoutInflater;
        public List<View> views;
        public ListView listview_collection, listview_return, listview_total;
        public ReportTableAdapter collection, returns, total;
        public TextView text_return_count_total, text_return_money_total, text_collection_money_total, text_collection_count_total, text_count_total, text_money_total;
        public MyPagerAdapter(Context context){
            super();
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            initView();
        }

        public void refreshData(SaleReportInfo sale,RefundReportInfo refund, TotalReportInfo total){
            this.collection.refreshData(sale.getSalelist());
            this.returns.refreshData(refund.getRefundlist());
            this.total.refreshData(total.getTotallist());
            if(sale != null){
                text_collection_money_total.setText(sale.getTotalmoney());
                text_collection_count_total.setText(sale.getBillcount());
            }
            if(refund != null){
                text_return_count_total.setText(refund.getBillcount());
                text_return_money_total.setText(refund.getTotalmoney());
            }
            if(total != null){
                text_money_total.setText(total.getTotalmoney());
                text_count_total.setText(total.getBillcount());
            }
        }
        public void initView() {
            views = new ArrayList<>();
            View v1 = mLayoutInflater.inflate(R.layout.view_collect, null);
            listview_collection = ButterKnife.findById(v1, R.id.listview);
            collection = new ReportTableAdapter(getApplicationContext(),new ArrayList<ReportDetailInfo>());
            listview_collection.setAdapter(collection);
            text_collection_money_total = ButterKnife.findById(v1, R.id.text_collection_money_total);
            text_collection_count_total = ButterKnife.findById(v1, R.id.text_collection_count_total);
            views.add(v1);
            View v2 = mLayoutInflater.inflate(R.layout.view_return, null);
            listview_return = ButterKnife.findById(v2, R.id.listview);
            returns = new ReportTableAdapter(getApplicationContext(),new ArrayList<ReportDetailInfo>());
            listview_return.setAdapter(returns);
            text_return_count_total = ButterKnife.findById(v2, R.id.text_return_count_total);
            text_return_money_total = ButterKnife.findById(v2, R.id.text_return_money_total);
            views.add(v2);
            View v3 = mLayoutInflater.inflate(R.layout.view_total, null);
            listview_total = ButterKnife.findById(v3, R.id.listview);
            total = new ReportTableAdapter(getApplicationContext(),new ArrayList<ReportDetailInfo>());
            listview_total.setAdapter(total);
            text_count_total = ButterKnife.findById(v3, R.id.text_count_total);
            text_money_total = ButterKnife.findById(v3, R.id.text_money_total);
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

    public static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
