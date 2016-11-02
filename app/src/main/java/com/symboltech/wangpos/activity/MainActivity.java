package com.symboltech.wangpos.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.google.gson.GsonBuilder;
import com.symboltech.koolcloud.aidl.AidlRequestManager;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.config.InitializeConfig;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.ChangeManagerDialog;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.OfflineReturnDialog;
import com.symboltech.wangpos.dialog.OfflineUpdateByLogDialog;
import com.symboltech.wangpos.dialog.OfflineUploadDialog;
import com.symboltech.wangpos.dialog.PrintOrderDialog;
import com.symboltech.wangpos.dialog.ReturnDialog;
import com.symboltech.wangpos.dialog.ThirdPayControllerDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.OfflineBankInfo;
import com.symboltech.wangpos.msg.entity.OfflineBillInfo;
import com.symboltech.wangpos.msg.entity.OfflineGoodsInfo;
import com.symboltech.wangpos.msg.entity.OfflinePayTypeInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.result.BillResult;
import com.symboltech.wangpos.result.InitializeInfResult;
import com.symboltech.wangpos.result.OfflineDataResult;
import com.symboltech.wangpos.result.TicketFormatResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.MyscollView;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.koolcloud.engine.service.aidl.IPrintCallback;
import cn.koolcloud.engine.service.aidl.IPrinterService;
import cn.koolcloud.engine.service.aidlbean.ApmpRequest;
import cn.koolcloud.engine.service.aidlbean.IMessage;
import cn.koolcloud.engine.thirdparty.aidl.IKuYunThirdPartyService;
import cn.koolcloud.engine.thirdparty.aidlbean.TransPrintRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.view_pager) ViewPager view_pager;
    @Bind(R.id.myscollview) MyscollView myscollView;

    static public boolean isPrinting = false;
    /** used by receiver */
    private IntentFilter filter = new IntentFilter();

    public BroadcastReceiver receiver =new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantData.OFFLINE_MODE.equals(intent.getAction())) {
                int mode = intent.getIntExtra(ConstantData.OFFLINE_MODE_INFO, -1);
                if(mode == ConstantData.CHANGE_MODE_STATE){
                    new ChangeModeDialog(MainActivity.this, new HttpActionHandle() {
                        @Override
                        public void handleActionError(String actionName, String errmsg) {

                        }

                        @Override
                        public void handleActionSuccess(String actionName, Object result) {

                        }

                        @Override
                        public void handleActionChangeToOffLine() {
                            ChangeUI(0);
                        }
                    }).show();
                }
            }
        }
    };
    protected static final int printStart = 0;
    protected static final int printEnd = 1;
    protected static final int printError = 2;
    private OfflineUploadDialog offlineUploadDialog;

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

    protected IKuYunThirdPartyService mYunService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mYunService = IKuYunThirdPartyService.Stub.asInterface(service);
        }
            @Override
            public void onServiceDisconnected (ComponentName name){
                mYunService = null;
        }
    };
    @Override
    protected void initData() {
        if(AppConfigFile.isOffLineMode()){
            //手动监测网络状态
            Intent serviceintent = new Intent(MainActivity.this, RunTimeService.class);
            serviceintent.putExtra(ConstantData.CHECK_NET, true);
            mContext.startService(serviceintent);
            ChangeUI(1);
        }else{
            ChangeUI(0);
        }
        if (SpSaveUtils.readboolean(MyApplication.context, ConstantData.IS_CONFIG_DOWNLOAD, true)) {
            //尝试删除已经上传成功的过期数据
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            OrderInfoDao dao = new OrderInfoDao(mContext);
            Date dNow = new Date();
            Date dBefore = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dNow);
            calendar.add(Calendar.DAY_OF_MONTH, AppConfigFile.OFFLINE_DATA_TIME);
            dBefore = calendar.getTime();
            dao.deleteOrderBytime(format.format(dBefore.getTime()));
            //删除那些交易未成功的数据
            dao.deleteOrderbyState();
            getconfigInfo();
            //获取小票格式
          //  getTickdatas();
            uploadOfflineData(true);

        }
        Intent printService = new Intent(IPrinterService.class.getName());
        printService = AndroidUtils.getExplicitIntent(this, printService);
        bindService(printService, printerServiceConnection, Context.BIND_AUTO_CREATE);
        Intent yunIntent = new Intent(IKuYunThirdPartyService.class.getName());
        yunIntent = AndroidUtils.getExplicitIntent(this, yunIntent);
        if (yunIntent == null) {
        } else {
            bindService(yunIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void ChangeUI(int mode){
        if(view_pager != null){
            view_pager.setCurrentItem(mode);
        }
    }

    @Override
    protected void onResume() {
//        if(AppConfigFile.isOffLineMode()){
//            ChangeUI(1);
//        }else {
//            ChangeUI(0);
//        }
        filter.addAction(ConstantData.OFFLINE_MODE);
        registerReceiver(receiver, filter);
        registerReceiver(broadcastReceiver, new IntentFilter(
                "cn.koolcloud.engine.ThirdPartyTrans"));
        super.onResume();
    }


    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void initView() {
        AppConfigFile.addActivity(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
//                myscollView.setPosition(view_pager.getCurrentItem());
            }
        });
    }

    @Override
    protected void recycleMemery() {
        if (iPrinterService != null) {
            unbindService(printerServiceConnection);
        }
        if(mYunService != null){
            try {
                unbindService(connection);
            } catch (Exception e) {
                // 如果重复解绑会抛异常
            }
        }
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }
    public void print_last(String id){
        if(id == null || "".equals(id)){
            AidlRequestManager.getInstance().aidlLastTransPrintRequest(mYunService, new AidlRequestManager.AidlRequestCallBack() {

                @Override
                public void onTaskStart() {
                }

                @Override
                public void onTaskFinish(JSONObject rspJSON) {
                    if (!rspJSON.optString("responseCode").equals("00")) {
                        ToastUtils.sendtoastbyhandler(handler, "打印失败：" + rspJSON.optString("errorMsg"));
                    }
                }

                @Override
                public void onTaskCancelled() {
                }

                @Override
                public void onException(Exception e) {
                    LogUtil.i("lgs","---"+e.toString());
                    ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
                }
            });
        }else{
            TransPrintRequest request = new TransPrintRequest(id);
            AidlRequestManager aidlManager = AidlRequestManager.getInstance();
            aidlManager.aidlTransPrintRequest(mYunService, request, new AidlRequestManager.AidlRequestCallBack() {
                @Override
                public void onTaskStart() {

                }

                @Override
                public void onTaskCancelled() {
                    ToastUtils.sendtoastbyhandler(handler, "打印取消");
                }

                @Override
                public void onTaskFinish(JSONObject rspJSON) {
                    if (!rspJSON.optString("responseCode").equals("00")) {
                        ToastUtils.sendtoastbyhandler(handler, "打印失败：" + rspJSON.optString("errorMsg"));
                    }
                }

                @Override
                public void onException(Exception e) {
                    ToastUtils.sendtoastbyhandler(handler, "打印异常");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if(Utils.isFastClick()){
            return;
        }
        int id = v.getId();
        switch (id){
            case R.id.rl_lockscreen:
                lockscreen();
                break;
            case R.id.rl_member:
                if(AppConfigFile.isOffLineMode()){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.offline_waring));
                    return;
                }
                gotoFunction(MemberAccessActivity.class);
                break;
            case R.id.rl_sendcarcoupon:
                if(AppConfigFile.isOffLineMode()){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.offline_waring));
                    return;
                }
                break;
            case R.id.rl_pay:
                if(AppConfigFile.isOffLineMode()){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.offline_waring));
                    return;
                }
                gotoPay();
                break;
            case R.id.rl_salereturn:
                if(AppConfigFile.isOffLineMode()){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.offline_waring));
                    return;
                }
                new ReturnDialog(this).show();
                break;
            case R.id.rl_change:
                new ChangeManagerDialog(this, new DialogFinishCallBack() {
                    @Override
                    public void finish(int position) {
                        if(AppConfigFile.getUploadStatus() == ConstantData.UPLOAD_ING){
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_uploading));
                            return;
                        }
                        if(AppConfigFile.isNetConnect()) {
                            OrderInfoDao dao = new OrderInfoDao(MainActivity.this);
                            if(dao.getOffLineDataCount() > 0 || dao.getBankOffLineDataCount() > 0) {
                                AppConfigFile.setUploadStatus(ConstantData.UPLOAD_ING);
                                offlineUploadDialog = new OfflineUploadDialog(MainActivity.this, true);
                                offlineUploadDialog.show();
                                uploadOfflineData(null, AppConfigFile.OFFLINE_DATA_COUNT);
                            }else {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
                                MainActivity.this.startActivity(intent);
                                MainActivity.this.finish();
                            }
                        }else {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
                                MainActivity.this.startActivity(intent);
                                MainActivity.this.finish();
                        }
                    }
                }).show();
                break;
            case R.id.rl_billprint:
                if(isPrinting){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.printing));
                    return;
                }
                new PrintOrderDialog(this, new GeneralEditListener() {
                    @Override
                    public void editinput(String edit) {
                        printByorderforHttp(edit);
                    }
                }).show();
                break;
            case R.id.rl_upload:
                uploadOfflineData(false);
                break;
            case R.id.rl_offline:
                new OfflineReturnDialog(this).show();
                break;
            case R.id.rl_bank:
                if(AppConfigFile.isOffLineMode()){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.offline_waring));
                    return;
                }
                if(!getPayType(PaymentTypeEnum.BANK)){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.no_config));
                    return;
                }
                Type = PaymentTypeEnum.BANK.getStyletype();
                Intent intentBank = new Intent(this,ThirdPayControllerDialog.class);
                intentBank.putExtra(ConstantData.PAY_TYPE, PaymentTypeEnum.BANK.getStyletype());
                startActivity(intentBank);
                break;
            case R.id.rl_weichat:
                if(AppConfigFile.isOffLineMode()){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.offline_waring));
                    return;
                }
                if(!getPayType(PaymentTypeEnum.WECHAT)){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.no_config));
                    return;
                }
                Type = PaymentTypeEnum.WECHAT.getStyletype();
                Intent intent = new Intent(this,ThirdPayControllerDialog.class);
                intent.putExtra(ConstantData.PAY_TYPE, PaymentTypeEnum.WECHAT.getStyletype());
                startActivity(intent);
                break;
        }
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
     * @Description: 获取支付方式Id
     *
     */
    private Boolean getPayType(PaymentTypeEnum typeEnum) {
        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null)
            return false;
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getType().equals(typeEnum.getStyletype())) {
                return true;
            }
        }
        return false;
    }

    public void gotoPay(){
        Intent intent = new Intent(this,PaymentActivity.class);
        intent.putExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_ACCOUNTS);
        startActivity(intent);
    }
    /**
     * gotoFunction
     */
    private void gotoFunction(Class t) {
        Intent intent = new Intent(this,t);
        startActivity(intent);
    }

    /**
     * lockscreen
     */
    private void lockscreen() {
        Intent serviceintent = new Intent(getApplicationContext(), RunTimeService.class);
        serviceintent.putExtra(ConstantData.UPDATE_STATUS, true);
        serviceintent.putExtra(ConstantData.CASHIER_ID, ConstantData.POS_STATUS_LOCK);
        startService(serviceintent);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_LOCKSCREEN);
        startActivity(intent);
        this.finish();
    }

    public void printBackByorder(final BillInfo billinfo){
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
                    iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printBackOrderList(billinfo, true)));
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

    public void printByorder(final BillInfo billinfo){
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
                    iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printOrderList(billinfo,true)));
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
    private void printByorderforHttp(final String edit) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("billid", edit);
        HttpRequestUtil.getinstance().printerOrderagain(map, BillResult.class, new HttpActionHandle<BillResult>() {

            @Override
            public void handleActionStart() {
                startwaitdialog();
            }

            @Override
            public void handleActionFinish() {
                closewaitdialog();
            }

            @Override
            public void handleActionError(String actionName, String errmsg) {
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName, BillResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    BillInfo billinfo = result.getTicketInfo().getBillinfo();
                    if (billinfo != null) {
                        if ("0".equals(billinfo.getSaletype())) {
                            printByorder(billinfo);
                        } else if ("1".equals(billinfo.getSaletype()) || "2".equals(billinfo.getSaletype())) {
                            printBackByorder(billinfo);
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_param_err));
                        }
                    }
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(MainActivity.this, httpActionHandle).show();
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                ChangeUI(1);
            }

            @Override
            public void handleActionOffLine() {
                BillInfo billinfo = offlineInfo2billInfo(edit);
                if (billinfo != null) {
                    if ("0".equals(billinfo.getSaletype())) {
                        printByorder(billinfo);
                    } else if ("1".equals(billinfo.getSaletype()) || "2".equals(billinfo.getSaletype())) {
                        printBackByorder(billinfo);
                    }
                } else {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_no_found_bill));
                }
            }
        });
    }

    /**
     * get config info by server
     */
    protected void getconfigInfo() {
        HttpRequestUtil.getinstance().initialize(null, InitializeInfResult.class,
                new HttpActionHandle<InitializeInfResult>() {

                    @Override
                    public void handleActionStart() {
                        startwaitdialog();
                    }

                    @Override
                    public void handleActionFinish() {
                        closewaitdialog();
                    }

                    @Override
                    public void handleActionError(String actionName, String errmsg) {
                        ToastUtils.sendtoastbyhandler(handler, errmsg);
                    }

                    @Override
                    public void handleActionSuccess(String actionName, InitializeInfResult result) {
                        if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                            InitializeConfig.initialize(MainActivity.this, result.getInitializeInfo());
                            SpSaveUtils.writeboolean(MyApplication.context, ConstantData.IS_CONFIG_DOWNLOAD, false);
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                    @Override
                    public void startChangeMode() {
                        final HttpActionHandle httpActionHandle = this;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangeModeDialog(MainActivity.this, httpActionHandle).show();
                            }
                        });
                    }

                    @Override
                    public void handleActionOffLine() {
                        ChangeUI(1);
                    }
                });
    }

    protected void getTickdatas(){
        HttpRequestUtil.getinstance().getTicketFormat(null, TicketFormatResult.class, new HttpActionHandle<TicketFormatResult>() {

            @Override
            public void handleActionError(String actionName, String errmsg) {
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName,
                                            TicketFormatResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    if (result.getTicketformat() != null && result.getTicketformat().getTickdatas() != null
                            && result.getTicketformat().getTickdatas().size() > 0) {
                        SpSaveUtils.saveObject(MyApplication.context, ConstantData.TICKET_FORMAT_LIST, result.getTicketformat().getTickdatas());
                    } else {
                        SpSaveUtils.delete(MyApplication.context, ConstantData.TICKET_FORMAT_LIST);
                    }
                } else {
                    SpSaveUtils.delete(MyApplication.context, ConstantData.TICKET_FORMAT_LIST);
                }
            }
        });
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
            views = new ArrayList<>();
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

    /**
     * 离线数据转成正常数据
     * @param billId
     * @return
     */
    public BillInfo offlineInfo2billInfo(String billId){
        BillInfo bill = null;
        if(billId == null){
            return bill;
        }
        OrderInfoDao dao = new OrderInfoDao(mContext);
        OfflineBillInfo info = dao.geOfflineBillInfo(billId);
        if(info == null){
            return bill;
        }
        bill = new BillInfo();
        bill.setBillid(billId);
        bill.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
        bill.setCashiername(SpSaveUtils.read(mContext, ConstantData.CASHIER_NAME, ""));
        bill.setSalemanname(info.getSavearticleinfos().getCashiername());
        bill.setSaletime(info.getConfirmbillinfos().getSaletime());
        bill.setChangemoney(info.getConfirmbillinfos().getChangemoney());
        bill.setTotalmoney(info.getSavearticleinfos().getTotalmoney());
        bill.setSaletype(info.getSavearticleinfos().getSaletype());
        List<PayMentsInfo> payMentsInfos = new ArrayList<PayMentsInfo>();
        for(OfflinePayTypeInfo billinfo:info.getConfirmbillinfos().getPaymentslist()){
            PayMentsInfo payMentsInfo = new PayMentsInfo();
            payMentsInfo.setMoney(billinfo.getMoney());
            payMentsInfo.setId(billinfo.getId());
            payMentsInfo.setOverage(billinfo.getOverage());
            payMentsInfo.setName(billinfo.getName());
            payMentsInfo.setType(billinfo.getType());
            payMentsInfos.add(payMentsInfo);
        }
        bill.setPaymentslist(payMentsInfos);
        List<GoodsInfo> goodslist = new ArrayList<GoodsInfo>();
        for(OfflineGoodsInfo goodsInfo:info.getSavearticleinfos().getGoodslist()){
            GoodsInfo goodInfo = new GoodsInfo();
            goodInfo.setCode(goodsInfo.getCode());
            goodInfo.setSalecount(goodsInfo.getSalecount());
            goodInfo.setSaleamt(goodsInfo.getSaleamt());
            goodInfo.setUsedpoint(goodsInfo.getUsedpoint());
            goodInfo.setGoodsname(goodsInfo.getGoodsname());
            goodslist.add(goodInfo);
        }
        bill.setGoodslist(goodslist);
        return bill;
    }

    /**
     * 上传离线数据
     * @param isAuto 是不是自动上传  false 是手动上传
     */
    public void uploadOfflineData(boolean isAuto){
        if(AppConfigFile.isNetConnect()){
            if(AppConfigFile.getUploadStatus() == ConstantData.UPLOAD_ING){
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_uploading));
            }else{
                OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
                int count = dao.getOffLineDataCount();
                int countBank = dao.getBankOffLineDataCount();
                if(count > 0 || countBank > 0){
                    AppConfigFile.setUploadStatus(ConstantData.UPLOAD_ING);
                    if(isAuto) {
                        Intent serviceintent = new Intent(mContext, RunTimeService.class);
                        serviceintent.putExtra(ConstantData.UPLOAD_OFFLINE_DATA, true);
                        mContext.startService(serviceintent);
                    }else {
                        new OfflineUploadDialog(MainActivity.this).show();
                    }
                }else{
                    if(!isAuto) {
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_no_data));
                    }
                }
            }
        }else{
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.no_netconnect_waring));
        }
    }

    public void uploadOfflineData(String beginBillID, final int count) {
        OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
        List<OfflineBillInfo> billinfos = null;
        if(AppConfigFile.isNetConnect()) {
            billinfos = dao.getOfflineOrderInfo(beginBillID, count);
        }
        if (billinfos != null && billinfos.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            //重新复制开始的订单号
            final String newBillID = billinfos.get(billinfos.size() - 1).getConfirmbillinfos().getBillid();
            String json = new GsonBuilder().serializeNulls().create().toJson(billinfos);
            map.put("billinfo", json);
            HttpRequestUtil.getinstance().uploadOfflineData(map, OfflineDataResult.class,
                    new HttpActionHandle<OfflineDataResult>() {

                        @Override
                        public void handleActionError(String actionName, String errmsg) {
                            AppConfigFile.setUploadStatus(ConstantData.UPLOAD_SUCCESS);
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
                            MainActivity.this.startActivity(intent);
                            MainActivity.this.finish();
                        }

                        @Override
                        public void handleActionSuccess(String actionName, OfflineDataResult result) {
                            if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode()) || ConstantData.HTTP_RESPONSE_PART_OK.equals(result.getCode())) {
                                if (result != null || result.getOfflineDatainfo() != null) {
                                    OrderInfoDao dao = new OrderInfoDao(MainActivity.this);
                                    dao.setOfflineStatus(result.getOfflineDatainfo().getSucbillidlist());
                                }
                            } else {
                                LogUtil.v("lgs", result.getMsg());
                            }
                            //不管成功还是不成功，都进行下次上传
                            uploadOfflineData(newBillID, count);
                        }
                    });
        } else {
            uploadOfflineBankData(null, AppConfigFile.OFFLINE_DATA_COUNT);
        }
    }

    /**
     * 上传离线数据
     *
     * @param beginBillID
     *            上传开始订单号，传空即是从头开始
     * @param count
     *            每次上传条数
     */
    public void uploadOfflineBankData(String beginBillID, final int count) {
        OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
        List<OfflineBankInfo> bankinfos = null;
        if (AppConfigFile.isNetConnect()) {
            bankinfos = dao.getOfflineBankInfo(beginBillID, count);
        }
        if (bankinfos != null && bankinfos.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            // 重新复制开始的订单号
            final String newBillID = bankinfos.get(bankinfos.size() - 1).getTradeno();
            String json = new GsonBuilder().serializeNulls().create().toJson(bankinfos);
            LogUtil.v("lgs", "error = " + json);
            map.put("data", json);
            HttpRequestUtil.getinstance().uploadOfflineBankData(map, OfflineDataResult.class,
                    new HttpActionHandle<OfflineDataResult>() {
                        @Override
                        public void handleActionError(String actionName, String errmsg) {
                            AppConfigFile.setUploadStatus(ConstantData.UPLOAD_SUCCESS);
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
                            MainActivity.this.startActivity(intent);
                            MainActivity.this.finish();
                        }

                        @Override
                        public void handleActionSuccess(String actionName, OfflineDataResult result) {
                            if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())
                                    || ConstantData.HTTP_RESPONSE_PART_OK.equals(result.getCode())) {
                                if (result != null || result.getOfflineDatainfo() != null) {
                                    OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
                                    dao.setBankOfflineStatus(result.getOfflineDatainfo().getSucbillidlist());
                                }
                            } else {
                                LogUtil.v("lgs", result.getMsg());
                            }
                            // 不管成功还是不成功，都进行下次上传
                            uploadOfflineBankData(newBillID, count);
                        }
                    });
        } else {
            AppConfigFile.setUploadStatus(ConstantData.UPLOAD_SUCCESS);
            //TODO 上传提示框
            if(offlineUploadDialog != null && offlineUploadDialog.isShowing()){
                offlineUploadDialog.dismiss();
            }
            if(dao.getOffLineDataCount() > 0 || dao.getBankOffLineDataCount() > 0 ) {
                new OfflineUpdateByLogDialog(MainActivity.this).show();
            }else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final Message message = intent.getParcelableExtra(Message.class
                    .getName());
            LogUtil.d("lgs", "handleMessage" + message.what + ":" + message.toString());
            String dataString = "";
            if (message.getData() != null) {
                dataString = message.getData().getString("data");
                try {
                    LogUtil.d("lgs", "data:" + dataString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            switch (message.what) {
                case TransState.STATE_CSB_FINISH: {
                    handleTransResult(true, dataString);
                    break;
                }
                case TransState.STATE_CSB_FAILURE: {
                    break;
                }
                default:
                    break;
            }

        }
    };
    private String Type;
     /**
     * 处理交易结果
     *
     * @param isSuccess
     * @param data
     */

    private void handleTransResult(boolean isSuccess, String data) {
        if (isSuccess) {
            AidlRequestManager.getInstance().aidlLastTransPrintRequest(mYunService, new AidlRequestManager.AidlRequestCallBack() {

                @Override
                public void onTaskStart() {
                }

                @Override
                public void onTaskFinish(JSONObject rspJSON) {
                    if (!rspJSON.optString("responseCode").equals("00")) {
                        ToastUtils.sendtoastbyhandler(handler, "打印失败：" + rspJSON.optString("errorMsg"));
                    }
                }

                @Override
                public void onTaskCancelled() {
                }

                @Override
                public void onException(Exception e) {
                    ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
                }
            });
        }
        // 待嫁接
        if (isSuccess) {
            OrderBean resultBean = parse(data, isSuccess);
            LogUtil.e("lgs", resultBean.toString());
            if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
                SpSaveUtils.write(getApplicationContext(),ConstantData.LAST_BANK_TRANS, resultBean.getTxnId());
            }
            if (resultBean.getTransType().equals(ConstantData.SALE)) {
                resultBean.setPaymentId(getPayTypeId(Type));
                resultBean.setTransType(ConstantData.TRANS_SALE);
                resultBean.setTraceId("-" + AppConfigFile.getBillId());
                Intent serviceintent = new Intent(mContext, RunTimeService.class);
                serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                serviceintent.putExtra(ConstantData.THIRD_DATA, resultBean);
                startService(serviceintent);
            } else if (resultBean.getTransType().equals(ConstantData.SALE_VOID)) {
                if (resultBean.getResultCode().equals("00")) {
                    resultBean.setPaymentId(getPayTypeId(Type));
                    resultBean.setTransType(ConstantData.TRANS_REVOKE);
                    resultBean.setTraceId("-"+AppConfigFile.getBillId());
                    Intent serviceintent = new Intent(mContext, RunTimeService.class);
                    serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                    serviceintent.putExtra(ConstantData.THIRD_DATA, resultBean);
                    startService(serviceintent);
                }else{
                    LogUtil.e("lgs", "退款失败" + data);
                }
            } else {
                LogUtil.e("lgs", data);
                return;
            }
        }
    }

    /**
     * 解析交易结果
     *
     * @param jsonStr
     * @param isSuccess
     * @return
     */

    private OrderBean parse(String jsonStr, boolean isSuccess) {
        OrderBean newBean = null;
        try {
            newBean = (OrderBean) GsonUtil.jsonToBean(jsonStr, OrderBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(newBean != null){
            if (newBean.getOrderState().isEmpty()) {
                newBean.setOrderState(isSuccess ? "0" : "");
            }
        }
        return newBean;
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
     * @Description: 获取支付方式Id
     *
     */
    private String getPayTypeId(String typeEnum) {

        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null)
            return null;
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getType().equals(typeEnum)) {
                return paymentslist.get(i).getId();
            }
        }
        return null;
    }
}
