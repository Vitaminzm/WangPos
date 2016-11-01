package com.symboltech.wangpos.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.CouponsAdapter;
import com.symboltech.wangpos.adapter.PaymentTypeInfoDetailAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.dialog.SelectCarPlateDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.result.PrintCouponResult;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.GridViewForScrollView;

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

public class PaymentDetailActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
    @Bind(R.id.title_icon_back)
    ImageView title_icon_back;

    @Bind(R.id.text_order_id)
    TextView text_order_id;
    @Bind(R.id.text_order_time)
    TextView text_order_time;

    @Bind(R.id.text_order_money)
    TextView text_order_money;
    @Bind(R.id.text_coupon)
    TextView text_coupon;
    @Bind(R.id.ll_coupon)
    LinearLayout ll_coupon;

    @Bind(R.id.ll_score_deduction)
    LinearLayout ll_score_deduction;
    @Bind(R.id.text_real_pay_money)
    TextView text_real_pay_money;
    @Bind(R.id.text_score_deduction)
    TextView text_score_deduction;
    @Bind(R.id.ll_score_info)
    LinearLayout ll_score_info;
    @Bind(R.id.ll_member_hold_coupon)
    LinearLayout ll_member_hold_coupon;

    @Bind(R.id.text_now_score)
    TextView text_now_score;
    @Bind(R.id.text_deduction_score)
    TextView text_deduction_score;
    @Bind(R.id.text_used_score)
    TextView text_used_score;
    @Bind(R.id.text_add_score)
    TextView text_add_score;

    @Bind(R.id.activity_payment_gridview)
    GridViewForScrollView activity_payment_gridview;
    private PaymentTypeInfoDetailAdapter paymentTypeInfoDetailAdapter;

    @Bind(R.id.recycleview_send_coupon)
    RecyclerView recycleview_send_coupon;
    private CouponsAdapter sendAdapter;

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
    //打印优惠券列表
    private List<CouponInfo> couponInfos;
    private LayoutInflater inflater;
    private int isMember;
    private BillInfo bill;
    @Override
    protected void initData() {
        inflater = LayoutInflater.from(mContext);
        couponInfos = new ArrayList<>();
        title_icon_back.setVisibility(View.GONE);
        title_text_content.setText(getString(R.string.payment_info));
        text_order_id.setText(bill.getBillid());
        text_order_time.setText(bill.getSaletime());
        text_order_money.setText(bill.getTotalmoney());
        text_real_pay_money.setText(bill.getRealmoney());
        // 是否显示卡券总额
        double card_coupon = 0;
        double scoreDeduction = 0;
        for (int i = 0; i < bill.getPaymentslist().size(); i++) {
            PayMentsInfo info = bill.getPaymentslist().get(i);
            if (info.getType().equals(PaymentTypeEnum.CASH.getStyletype())) {
                if(info.getId()!=null && info.getId().equals("1")){
                    double changeMoney = 0 ;
                    changeMoney = ArithDouble.parseDouble(bill.getChangemoney());
                    addPayTypeInfo(info.getName(), ArithDouble.add(ArithDouble.parseDouble(info.getMoney()), changeMoney));
                }else{
                    addPayTypeInfo(info.getName(), ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())));
                }
            } else if (info.getType().equals(PaymentTypeEnum.SCORE.getStyletype())) {
                scoreDeduction = ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage()));
                if(scoreDeduction > 0){
                    text_score_deduction.setText("" + scoreDeduction);
                    ll_score_deduction.setVisibility(View.VISIBLE);
                }
            } else if (info.getType().equals(PaymentTypeEnum.COUPON.getStyletype())) {
                card_coupon = ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage()));
                if(ArithDouble.parseDouble(info.getOverage()) > 0){
                    text_coupon.setText("" + ArithDouble.parseDouble(info.getMoney())+"(溢余"+ArithDouble.parseDouble(info.getOverage())+")");
                    ll_coupon.setVisibility(View.VISIBLE);
                }else{
                    text_coupon.setText("" + ArithDouble.parseDouble(info.getMoney()));
                    ll_coupon.setVisibility(View.VISIBLE);
                }
            } else {
                addPayTypeInfo(info.getName(), ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())));
            }
        }
        double changeMoney = 0 ;
        changeMoney = ArithDouble.parseDouble(bill.getChangemoney());
        if (changeMoney > 0) {
            addPayTypeInfo(getString(R.string.payment_type_change),  changeMoney);
        }
        if(isMember == ConstantData.MEMBER_IS_NOT_VERITY){
            ll_score_info.setVisibility(View.GONE);
        }else{
            // 获取抵扣金额
            double awardPoint = 0, usedPoint = 0, deductPoint = 0, totalPoint = 0;
            // 增加的积分
            if (bill.getAwardpoint() != null) {
                awardPoint = ArithDouble.parseDouble(bill.getAwardpoint());
                text_add_score.setText(awardPoint+"");
            }

            //使用的积分
            if(bill.getUsedpoint()!=null){
                usedPoint = ArithDouble.parseDouble(bill.getUsedpoint());
                text_used_score.setText(usedPoint+"");
            }
            // 抵扣的积分
            if(bill.getExchangedpoint()!=null){
                deductPoint = ArithDouble.parseDouble(bill.getExchangedpoint());
                text_score_deduction.setText(deductPoint+"");
            }
            totalPoint = ArithDouble.sub(awardPoint, ArithDouble.add(deductPoint, usedPoint));
            text_now_score.setText(ArithDouble.add(ArithDouble.parseDouble(bill.getMember().getCent_total()), totalPoint)+"");
        }
        MemberDetailActivity.MyLayoutManager linearLayoutManagerHold = new MemberDetailActivity.MyLayoutManager(this);
        linearLayoutManagerHold.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleview_send_coupon.setLayoutManager(linearLayoutManagerHold);
        if(bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0){
            sendAdapter = new CouponsAdapter(bill.getGrantcouponlist(), 0, this);
            recycleview_send_coupon.setAdapter(sendAdapter);
            sendAdapter.setOnItemClickListener(new CouponsAdapter.MyItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    ImageView coupon_select = (ImageView) view.findViewById(R.id.imageview_coupon_selected);
                    if (coupon_select.getVisibility() == View.GONE) {
                        sendAdapter.addSelect(position);
                        sendAdapter.notifyItemChanged(position);
                    } else {
                        sendAdapter.delSelect(position);
                        sendAdapter.notifyItemChanged(position);
                    }
                }
            });
        }else {
            ll_member_hold_coupon.setVisibility(View.GONE);
        }
        Intent printService = new Intent(IPrinterService.class.getName());
        printService = AndroidUtils.getExplicitIntent(this, printService);
        bindService(printService, printerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void addPayTypeInfo(String name, double changeMoney) {
        PayMentsInfo info = new PayMentsInfo();
        info.setMoney(changeMoney + "");
        info.setName(name);
        paymentTypeInfoDetailAdapter.add(info);
    }


    @Override
    protected void initView() {
        setContentView(R.layout.activity_payment_detial);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        bill = (BillInfo) getIntent().getSerializableExtra(ConstantData.ORDER_INFO);
        isMember = getIntent().getIntExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_NOT_VERITY);
        paymentTypeInfoDetailAdapter = new PaymentTypeInfoDetailAdapter(getApplicationContext(), new ArrayList<PayMentsInfo>());
        activity_payment_gridview.setAdapter(paymentTypeInfoDetailAdapter);
    }
    @Override
    protected void recycleMemery() {
        if (iPrinterService != null) {
            unbindService(printerServiceConnection);
        }
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.text_print_coupon, R.id.text_selected_plate, R.id.text_done})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.text_print_coupon:
                if(isPrinting){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.printing));
                    return;
                }
                couponInfos.clear();
                if(sendAdapter == null){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_no_coupon_msg));
                    return;
                }else{
                    if(sendAdapter.getSelectedCount() <= 0){
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_please_selected));
                        return;
                    }
                    for (int i = 0; i < sendAdapter.getSelect().size(); i++) {
                        couponInfos.add(bill.getGrantcouponlist().get(sendAdapter.getSelect().get(i)));
                    }
                }
                getCouponPrintInfo();
                break;
            case R.id.text_selected_plate:
                new SelectCarPlateDialog(this).show();
                break;
            case R.id.text_done:
                printByorder(bill);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();
                break;
        }
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
                    iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printOrderList(billinfo, false)));
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

    private void getCouponPrintInfo() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("cashier", SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""));
        map.put("billid", bill.getBillid());
        if (bill.getMember() != null && bill.getMember().getId() != null) {
            map.put("cardno", bill.getMember().getId());
        }
        try {
            String json = GsonUtil.beanToJson(couponInfos);
            map.put("coupon", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().getCouponPrintInfo(map, PrintCouponResult.class, new HttpActionHandle<PrintCouponResult>() {
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
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName, final PrintCouponResult result) {
                if (result != null && ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    PaymentDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (sendAdapter != null) {
                                sendAdapter.clearSelect();
                                sendAdapter.notifyDataSetChanged();
                            }
                            couponInfos.clear();
                            String data = result.getData();
                            if (data != null) {
                                printCoupon(data);
                            } else {
                                ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_cannot_print_msg));
                            }
                        }
                    });
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    private void printCoupon(String data) {
        final String[] codes = data.split(";");
        final List<CouponInfo>couponInfos = new ArrayList<>();
        for(String src:codes){
            CouponInfo couponInfo = getCouponInfo(src);
            if(couponInfo != null){
                couponInfos.add(couponInfo);
            }
        }
        if(couponInfos.size() > 0){
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
                        iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printCoupon(couponInfos)));
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
    }

    /**
     * 获取coupon的详细信息
     * @param code
     * @return
     */
    public CouponInfo getCouponInfo(String code){
        if(code == null){
            return null;
        }
        for(CouponInfo couponInfo:bill.getGrantcouponlist()){
            if(code.equals(couponInfo.getCouponno())){
                return couponInfo;
            }
        }
        for(CouponInfo couponInfo:bill.getAllcouponlist()){
            if(code.equals(couponInfo.getCouponno())){
                return couponInfo;
            }
        }
        return null;
    }
}
