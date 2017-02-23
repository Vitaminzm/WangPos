package com.symboltech.wangpos.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.PaymentTypeInfoDetailAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.SaveOrderResultInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.GridViewForScrollView;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import java.lang.ref.WeakReference;
import java.util.List;

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

public class ReturnGoodSucceedActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
    @Bind(R.id.text_print)
    TextView text_print;
    @Bind(R.id.title_icon_back)
    ImageView title_icon_back;

    @Bind(R.id.text_return_order_id)
    TextView text_return_order_id;
    @Bind(R.id.text_return_order_time)
    TextView text_return_order_time;
    @Bind(R.id.text_return_money)
    TextView text_return_money;
    @Bind(R.id.text_return_coupon)
    TextView text_return_coupon;
    @Bind(R.id.text_return_back_coupon)
    TextView text_return_back_coupon;
    @Bind(R.id.ll_return_back_coupon)
    LinearLayout ll_return_back_coupon;
    @Bind(R.id.ll_return_coupon)
    LinearLayout ll_return_coupon;
    @Bind(R.id.ll_return_score)
    LinearLayout ll_return_score;
    @Bind(R.id.text_return_score)
    TextView text_return_score;
    @Bind(R.id.text_return_real_money)
    TextView text_return_real_money;

    @Bind(R.id.activity_payment_gridview)
    GridViewForScrollView activity_payment_gridview;
    private PaymentTypeInfoDetailAdapter paymentTypeInfoDetailAdapter;

    @Bind(R.id.ll_score_info)
    LinearLayout ll_score_info;
    @Bind(R.id.text_return_used_score)
    TextView text_return_used_score;
    @Bind(R.id.text_score_good_return)
    TextView text_score_good_return;
    @Bind(R.id.text_score_deduction_return)
    TextView text_score_deduction_return;
    private BillInfo bill;
    private float couponMoney;
    private SaveOrderResultInfo resultInfo;

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
                    break;
                case printEnd:
                    break;
                case printError:
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

    @Override
    protected void initData() {
        title_text_content.setText(getString(R.string.return_succeed));
        bill = (BillInfo) getIntent().getSerializableExtra(ConstantData.BILL);
        resultInfo = (SaveOrderResultInfo) getIntent().getSerializableExtra(ConstantData.SAVE_ORDER_RESULT_INFO);
        text_return_order_id.setText(bill.getBillid());
        text_return_order_time.setText(bill.getSaletime());
        text_return_real_money.setText(MoneyAccuracyUtils.formatMoneyByTwo(bill.getRealmoney()));
        text_return_money.setText(MoneyAccuracyUtils.formatMoneyByTwo(bill.getTotalmoney()));

        if(bill.getMember() != null){
            text_print.setVisibility(View.VISIBLE);
            for(PayMentsInfo data :bill.getPaymentslist()){
                if(data.getType().equals(PaymentTypeEnum.COUPON)){
                    if(ArithDouble.parseDouble(data.getOverage()) > 0){
                        text_return_coupon.setText("" + ArithDouble.parseDouble(data.getMoney())+"(溢余"+ArithDouble.parseDouble(data.getOverage())+")");
                        ll_return_coupon.setVisibility(View.VISIBLE);
                    }else{
                        text_return_coupon.setText("" + ArithDouble.parseDouble(data.getMoney()));
                        ll_return_coupon.setVisibility(View.VISIBLE);
                    }
                }
            }
            getCouponMoney(resultInfo != null ? resultInfo.getGrantcouponlist() : null);
            if(couponMoney > 0 ){
                ll_return_back_coupon.setVisibility(View.VISIBLE);
                text_return_back_coupon.setText(couponMoney+"");
            }
            if(bill.getExchange()!= null){
                if(ArithDouble.parseDouble(bill.getExchange().getExchangemoney()) > 0){
                    ll_return_score.setVisibility(View.VISIBLE);
                    text_return_score.setText(bill.getExchange().getExchangemoney());
                }
                text_score_deduction_return.setText(bill.getExchange().getExchangepoint());
            }
            if(ArithDouble.parseDouble(bill.getAwardpoint()) > 0){
                text_return_used_score.setText("-"+bill.getAwardpoint());
            }else {
                text_return_used_score.setText(ArithDouble.parseInt(bill.getAwardpoint())+"");
            }

            text_score_good_return.setText(ArithDouble.parseInt(bill.getUsedpoint())+"");

            ll_score_info.setVisibility(View.VISIBLE);
        }else{
            ll_score_info.setVisibility(View.GONE);
        }

        paymentTypeInfoDetailAdapter = new PaymentTypeInfoDetailAdapter(this, bill.getPaymentslist(), true);
        activity_payment_gridview.setAdapter(paymentTypeInfoDetailAdapter);
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
        setContentView(R.layout.activity_return_good_success);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        title_icon_back.setVisibility(View.GONE);
    }

    /**
     * 获取本次退货回收券的总金额
     * @param grantcouponlist
     */
    private void getCouponMoney(List<CouponInfo> grantcouponlist) {
        try {
            if (grantcouponlist != null) {
                for (CouponInfo couponInfo : grantcouponlist) {
                    couponMoney += Float.parseFloat(couponInfo.getFacevalue());
                }
            }
        } catch (Exception e) {
            LogUtil.v("lgs", "算回收代金券金额失败");
        }
    }
    
    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.text_confirm, R.id.text_print})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.text_confirm:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.text_print:
                printBackByorder(bill);
                break;
        }
    }

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

    public void printBackByorder(final BillInfo billinfo){
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            if(latticePrinter == null){
                ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                return;
            }
            PrepareReceiptInfo.printBackOrderList(billinfo, false, latticePrinter);
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
                        iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printBackOrderList(billinfo, false, latticePrinter)));
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
                        ReturnGoodSucceedActivity.this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                    PrepareReceiptInfo.printBackOrderList(billinfo, false, latticePrinter);
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
}
