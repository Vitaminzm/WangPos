package com.symboltechshop.wangpos.activity;

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

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.adapter.ReportTableAdapter;
import com.symboltechshop.wangpos.app.AppConfigFile;
import com.symboltechshop.wangpos.app.ConstantData;
import com.symboltechshop.wangpos.app.MyApplication;
import com.symboltechshop.wangpos.db.dao.OrderInfoDao;
import com.symboltechshop.wangpos.dialog.ChangeModeDialog;
import com.symboltechshop.wangpos.http.HttpActionHandle;
import com.symboltechshop.wangpos.http.HttpRequestUtil;
import com.symboltechshop.wangpos.log.LogUtil;
import com.symboltechshop.wangpos.msg.entity.RefundReportInfo;
import com.symboltechshop.wangpos.msg.entity.ReportDetailInfo;
import com.symboltechshop.wangpos.msg.entity.ReportInfo;
import com.symboltechshop.wangpos.msg.entity.TotalReportInfo;
import com.symboltechshop.wangpos.result.ReportResult;
import com.symboltechshop.wangpos.utils.AndroidUtils;
import com.symboltechshop.wangpos.utils.SpSaveUtils;
import com.symboltechshop.wangpos.utils.ToastUtils;
import com.symboltechshop.wangpos.utils.Utils;
import com.symboltechshop.wangpos.view.DecoratorViewPager;
import com.symboltechshop.wangpos.view.ScllorTabView;
import com.symboltechshop.wangpos.msg.entity.SaleReportInfo;
import com.symboltechshop.wangpos.print.PrepareReceiptInfo;
import com.symboltechshop.wangpos.utils.StringUtil;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

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
import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.impl.WeiposImpl;

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
            mActivity = new WeakReference<BaseActivity>(activity);
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

    private LatticePrinter latticePrinter;// 点阵打印
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
        Map<String, String> map = new HashMap<String, String>();
        if (ConstantData.JOB.equals(flag)) {
            title_text_content.setText(getString(R.string.job_report));
            map.put("person_id", SpSaveUtils.read(WorkLogActivity.this, ConstantData.CASHIER_ID, ""));
        } else {
            title_text_content.setText(getString(R.string.day_report));
        }
        HttpRequestUtil.getinstance().getReportInfo(HTTP_TASK_KEY, map, ReportResult.class, new HttpActionHandle<ReportResult>() {

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
            @Override
            public void handleActionOffLine() {
                OrderInfoDao dao = new OrderInfoDao(mContext);
                if("job".equals(flag)) {
                    reportInfo = dao.findPaytypeBytime(System.currentTimeMillis());
                }else {
                    reportInfo = dao.findPaytypeBytime(SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""), System.currentTimeMillis());
                }
                if(reportInfo == null) {
                    Toast.makeText(mContext, "报表没有数据", Toast.LENGTH_SHORT).show();
                }else {
                    myPagerAdapter.refreshData(reportInfo.getSale(), reportInfo.getRefund(), reportInfo.getTotal());
                }
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(WorkLogActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                WorkLogActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(WorkLogActivity.this, httpActionHandle).show();
                    }
                });
            }
        });
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            try {
                // 设备可能没有打印机，open会抛异常
                latticePrinter = WeiposImpl.as().openLatticePrinter();
            } catch (Exception e) {
                // TODO: handle exception
            }
            if(latticePrinter != null){
                latticePrinter.setOnEventListener(new IPrint.OnEventListener() {

                    @Override
                    public void onEvent(final int what, String in) {
                        if(!StringUtil.isEmpty(PrepareReceiptInfo.getPrintErrorInfo(what, in)))
                            ToastUtils.sendtoastbyhandler(handler, PrepareReceiptInfo.getPrintErrorInfo(what, in));
                    }
                });
            }
        }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
            Intent printService = new Intent(IPrinterService.class.getName());
            printService = AndroidUtils.getExplicitIntent(this, printService);
            if (printService != null)
                bindService(printService, printerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_work_log);
        AppConfigFile.addActivity(this);
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
               // LogUtil.i("lgs",position+"-----:"+positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int position = view_pager_statistics.getCurrentItem();
              //  LogUtil.i("lgs","---onPageScrollStateChanged--"+position);
//                scllortabview.setCurrentNum(position);
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
        AppConfigFile.delActivity(this);
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
        if(Utils.isFastClick()){
            return;
        }
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
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            if(latticePrinter == null){
                ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                return;
            }
            PrepareReceiptInfo.printReportFrom(flag, reportInfo, latticePrinter);
        }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
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
                        iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printReportFrom(flag, reportInfo, latticePrinter)));
                    } catch (Exception e) {
                        Message msg2 = new Message();
                        msg2.what = printError;
                        msg2.arg1 = -100;
                        handler.sendMessage(msg2);
                        e.printStackTrace();
                    }
                }
            }).start();
        }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
            try {
                BaseSystemManager.getInstance().deviceServiceLogin(
                        WorkLogActivity.this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                    MyApplication.isPrint = true;
                                    PrepareReceiptInfo.printReportFrom(flag, reportInfo, latticePrinter);
                                }else{
                                    ToastUtils.sendtoastbyhandler(handler, "打印登录失败");
                                }
                            }
                        });
            } catch (SdkException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            views = new ArrayList<View>();
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
