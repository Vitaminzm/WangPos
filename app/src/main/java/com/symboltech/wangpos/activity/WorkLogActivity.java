package com.symboltech.wangpos.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.ReportTableAdapter;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.RefundReportInfo;
import com.symboltech.wangpos.msg.entity.ReportDetailInfo;
import com.symboltech.wangpos.msg.entity.ReportInfo;
import com.symboltech.wangpos.msg.entity.SaleReportInfo;
import com.symboltech.wangpos.msg.entity.TotalReportInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.result.ReportResult;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.DecoratorViewPager;
import com.symboltech.wangpos.view.ScllorTabView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.service.aidl.IPrintCallback;
import cn.koolcloud.engine.service.aidl.IPrinterService;
import cn.koolcloud.engine.service.aidlbean.ApmpRequest;
import cn.koolcloud.engine.service.aidlbean.IMessage;

public class WorkLogActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_collection)TextView text_collection;
    @Bind(R.id.text_return)TextView text_return;
    @Bind(R.id.text_total)TextView text_total;
    @Bind(R.id.text_desk_code)TextView text_desk_code;
    @Bind(R.id.text_shop)TextView text_shop;
    @Bind(R.id.view_pager_statistics)DecoratorViewPager view_pager_statistics;
    @Bind(R.id.scllortabview)ScllorTabView scllortabview;

    private ReportInfo reportInfo; // 报表数据
    private String flag;  //当前是班报还是日报   job - 班报
    private MyPagerAdapter myPagerAdapter;

    protected static final int printStart = 0;
    protected static final int printEnd = 1;
    protected static final int printError = 2;
    /** refresh UI By handler */
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
                case printStart:
                    isPrinting = true;
                    break;
                case printEnd:
                    isPrinting = false;
                    break;
                case printError:
                    isPrinting = false;
                    // 0：正常 -1：缺纸 -2：未合盖 -3：卡纸 -4 初始化异常 -100：其他故障
                    // -999：不支持该功能（可以不支持）
                    String errorMsg = "";
                    LogUtil.e("lgs", "printer errorCode is " + msg.arg1);
                    switch (msg.arg1) {
                        case -1:
                            errorMsg = "result=-1：缺纸";
                            break;
                        case -2:
                            errorMsg = "result=-2：未合盖";
                            break;
                        case -3:
                            errorMsg = "result=-3：卡纸";
                            break;
                        case -4:
                            errorMsg = "result=-4 初始化异常";
                            break;
                        case -999:
                            errorMsg = "result=-999：不支持该功能";
                            break;
                        default:
                            errorMsg = "result=-100：其他故障";
                            break;
                    }
                    msg.obj = errorMsg;
                    ToastUtils.showtaostbyhandler(theActivity,msg);
                    break;
            }
        }
    }

    static public boolean isPrinting = false;
    MyHandler handler = new MyHandler(this);

     // 打印服务
    private static IPrinterService iPrinterService;
    private ServiceConnection printerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iPrinterService = IPrinterService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iPrinterService = null;
        }
    };

    IPrintCallback.Stub callback = new IPrintCallback.Stub() {
        @Override
        public void handleMessage(IMessage message) throws RemoteException {
            int ret = message.what;
            LogUtil.d("lgs", "handleMessage ret:" + ret);
            try {
                iPrinterService.unRegisterPrintCallback();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ret == 0) {
                Message msg3 = new Message();
                msg3.what = printEnd;
                handler.sendMessage(msg3);
            } else {
                Message msg3 = new Message();
                msg3.what = printError;
                msg3.arg1 = ret;
                handler.sendMessage(msg3);
            }
        }
    };
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
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
        Intent printService = new Intent(IPrinterService.class.getName());
        printService = AndroidUtils.getExplicitIntent(this, printService);
        bindService(printService, printerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_work_log);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        scllortabview.setTabNum(3);
        scllortabview.setSelectedColor(getResources().getColor(R.color.green), getResources().getColor(R.color.green));
        myPagerAdapter = new MyPagerAdapter(getApplicationContext());
       // view_pager_statistics.setPageTransformer(true, new DepthPageTransformer());
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
        if (iPrinterService != null) {
            unbindService(printerServiceConnection);
        }
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
                if(isPrinting){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.printing));
                    return;
                }
                if(reportInfo == null) {
                    ToastUtils.sendtoastbyhandler(handler,"暂无数据");
                    return;
                }
                if(ConstantData.JOB.equals(flag)) {
                    printReport(true, reportInfo);
                }else {
                    printReport(false, reportInfo);
                }
                break;
        }
    }
    public void printReport(final boolean flag, final ReportInfo reportInfo){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg1 = new Message();
                msg1.what = printStart;
                handler.sendMessage(msg1);
                try {
                    iPrinterService.registerPrintCallback(callback);
                    // 0：正常 -1：缺纸 -2：未合盖 -3：卡纸 -4 初始化异常 -100：其他故障
                    // -999：不支持该功能（可以不支持）
                    iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printReportFrom(flag, reportInfo)));
                } catch (Exception e) {
                    Message msg2 = new Message();
                    msg2.what = printError;
                    msg2.arg1 = -100;
                    handler.sendMessage(msg2);
                    e.printStackTrace();
                }
            }
        }).start();
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
        /**
         * position参数指明给定页面相对于屏幕中心的位置。它是一个动态属性，会随着页面的滚动而改变。当一个页面填充整个屏幕是，它的值是0，
         * 当一个页面刚刚离开屏幕的右边时，它的值是1。当两个也页面分别滚动到一半时，其中一个页面的位置是-0.5，另一个页面的位置是0.5。基于屏幕上页面的位置
         * ，通过使用诸如setAlpha()、setTranslationX()、或setScaleY()方法来设置页面的属性，来创建自定义的滑动动画。
         */
        @Override
        public void transformPage(View view, float position) {
            if (position <= 0) {
                //从右向左滑动为当前View

                //设置旋转中心点；
                ViewHelper.setPivotX(view, view.getMeasuredWidth());
                ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5f);

                //只在Y轴做旋转操作
                ViewHelper.setRotationY(view, 90f * position);
            } else if (position <= 1) {
                //从左向右滑动为当前View
                ViewHelper.setPivotX(view, 0);
                ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5f);
                ViewHelper.setRotationY(view, 90f * position);
            }
        }
    }
}
