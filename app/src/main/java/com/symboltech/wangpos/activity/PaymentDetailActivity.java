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
import com.symboltech.wangpos.adapter.ParkCouponsAdapter;
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
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.GridViewForScrollView;
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

public class PaymentDetailActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
    @Bind(R.id.title_icon_back)
    ImageView title_icon_back;

    @Bind(R.id.text_order_id)
    TextView text_order_id;
    @Bind(R.id.text_order_time)
    TextView text_order_time;

    @Bind(R.id.text_print)
    TextView text_print;

    @Bind(R.id.text_order_money)
    TextView text_order_money;
    @Bind(R.id.text_coupon)
    TextView text_coupon;
    @Bind(R.id.ll_coupon)
    LinearLayout ll_coupon;
    @Bind(R.id.text_manjian)
    TextView text_manjian;
    @Bind(R.id.ll_manjian)
    LinearLayout ll_manjian;

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
    @Bind(R.id.ll_member_park_coupon)
    LinearLayout ll_member_park_coupon;

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
    @Bind(R.id.recycleview_park_coupon)
    RecyclerView recycleview_park_coupon;
    private ParkCouponsAdapter parkAdapter;

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

    private LatticePrinter latticePrinter;// 点阵打印
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
        couponInfos = new ArrayList<CouponInfo>();
        title_icon_back.setVisibility(View.GONE);
        title_text_content.setText(getString(R.string.payment_info));
        text_order_id.setText(bill.getBillid());
        text_order_time.setText(bill.getSaletime());
        text_order_money.setText(bill.getTotalmoney());


        // 是否显示卡券总额
        double card_coupon = 0;
        double scoreDeduction = 0;
        double manjian = 0;
        manjian = ArithDouble.parseDouble(bill.getTotalmbjmoney());
        if(manjian > 0){
            ll_manjian.setVisibility(View.VISIBLE);
            text_manjian.setText(manjian+"");
        }
        text_real_pay_money.setText(ArithDouble.sub(ArithDouble.parseDouble(bill.getRealmoney()), manjian)+"");
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
            text_print.setVisibility(View.GONE);
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
                text_deduction_score.setText(deductPoint+"");
            }
            totalPoint = ArithDouble.sub(awardPoint, ArithDouble.add(deductPoint, usedPoint));
            text_now_score.setText(ArithDouble.add(ArithDouble.parseDouble(bill.getMember().getCent_total()), totalPoint) + "");
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
        MemberDetailActivity.MyLayoutManager linearLayoutManagerPark = new MemberDetailActivity.MyLayoutManager(this);
        linearLayoutManagerPark.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleview_park_coupon.setLayoutManager(linearLayoutManagerPark);
        if(ArithDouble.parseDouble(bill.getParkcouponhour()) > 0){
            List<String> carCoupons = new ArrayList<String>();
            carCoupons.add(bill.getParkcouponhour());
            ParkCouponsAdapter parkCouponsAdapter = new ParkCouponsAdapter(carCoupons, 0, false, mContext);
            recycleview_park_coupon.setAdapter(parkCouponsAdapter);
        }else{
            ll_member_park_coupon.setVisibility(View.GONE);
        }

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

        //if(isMember == ConstantData.MEMBER_IS_NOT_VERITY){
            printByorder(bill);
       // }
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

    private boolean isSended = false;
    @OnClick({R.id.text_print_coupon, R.id.text_selected_plate, R.id.text_done, R.id.text_print})
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
                if(isSended){
                    ToastUtils.sendtoastbyhandler(handler, "不能重复发放！");
                }else{
                    if(ArithDouble.parseDouble(bill.getParkcouponhour()) > 0){
                        SelectCarPlateDialog selectcarplateDialog = new SelectCarPlateDialog(PaymentDetailActivity.this, bill.getMember(), bill.getParkcouponhour(), bill.getParkcouponaddhour(), bill.getBillid(), new SelectCarPlateDialog.FinishSendCoupon() {

                            @Override
                            public void finishSendCoupon(String carNo, String hour) {
                                // TODO Auto-generated method stub
                                isSended = true;
                                bill.setCarno(carNo);
                            }
                        });
                        selectcarplateDialog.show();
                    }else{
                        ToastUtils.sendtoastbyhandler(handler, "没有赠送停车券");
                    }
                }
                break;
            case R.id.text_done:
                if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                    if(MyApplication.isPrint){
                        ToastUtils.sendtoastbyhandler(handler, "打印中，请稍后");
                    }else{
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        this.finish();
                    }
                }else{
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    this.finish();
                }
                break;
            case R.id.text_print:
                printByorder(bill);
                break;
        }
    }

    public void printByorder(final BillInfo billinfo){
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            if(latticePrinter == null){
                ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                return;
            }
            PrepareReceiptInfo.printOrderList(billinfo, false, latticePrinter);
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
                        iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printOrderList(billinfo, false, latticePrinter)));
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
                        PaymentDetailActivity.this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                    MyApplication.isPrint = true;
                                    PrepareReceiptInfo.printOrderList(billinfo, false, latticePrinter);
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

    private void getCouponPrintInfo() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("cashier", SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""));
        map.put("billid", bill.getBillid());
        if (bill.getMember() != null && bill.getMember().getMemberno() != null) {
            map.put("cardno", bill.getMember().getMemberno());
        }
        try {
            String json = GsonUtil.beanToJson(couponInfos);
            map.put("coupon", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().getCouponPrintInfo(HTTP_TASK_KEY, map, PrintCouponResult.class, new HttpActionHandle<PrintCouponResult>() {
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
        final List<CouponInfo>couponInfos = new ArrayList<CouponInfo>();
        for(String src:codes){
            CouponInfo couponInfo = getCouponInfo(src);
            if(couponInfo != null){
                couponInfos.add(couponInfo);
            }
        }
        if(couponInfos.size() > 0){
            if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                if(latticePrinter == null){
                    ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                    return;
                }
                PrepareReceiptInfo.printCoupon(couponInfos, latticePrinter);
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
                            iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printCoupon(couponInfos, latticePrinter)));
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
                            PaymentDetailActivity.this, null, "99999998",//设备ID，生产找后台配置
                            new OnServiceStatusListener() {
                                @Override
                                public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                    if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                        MyApplication.isPrint = true;
                                        PrepareReceiptInfo.printCoupon(couponInfos, latticePrinter);
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
