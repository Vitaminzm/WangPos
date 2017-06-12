package com.symboltech.wangpos.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.PaymentTypeHolderAdapter;
import com.symboltech.wangpos.adapter.PaymentTypeInfoAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.AddPayinfoDialog;
import com.symboltech.wangpos.dialog.AlipayAndWeixinPayControllerInterfaceDialog;
import com.symboltech.wangpos.dialog.CanclePayDialog;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.RecordPayDialog;
import com.symboltech.wangpos.dialog.ThirdPayDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.CancleAndConfirmback;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.ExchangeInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.msg.entity.PayMentsCancleInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.SubmitGoods;
import com.symboltech.wangpos.msg.entity.ThirdPay;
import com.symboltech.wangpos.msg.entity.ThirdPayInfo;
import com.symboltech.wangpos.msg.entity.WposPayInfo;
import com.symboltech.wangpos.result.SaveOrderResult;
import com.symboltech.wangpos.result.ThirdPayInfoResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.CashierSign;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.zxing.app.CaptureActivity;
import com.ums.AppHelper;
import com.ums.upos.sdk.exception.CallServiceException;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.scanner.OnScanListener;
import com.ums.upos.sdk.scanner.ScannerConfig;
import com.ums.upos.sdk.scanner.ScannerManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;
import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.BizServiceInvoker.OnResponseListener;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

;

public class CheckOutActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
    @Bind(R.id.text_cancle_pay)
    TextView text_cancle_pay;

    @Bind(R.id.text_order_total_money)
    TextView text_order_total_money;
    @Bind(R.id.text_order_manjian_money)
    TextView text_order_manjian_money;
    @Bind(R.id.rl_order_manjian_money)
    RelativeLayout rl_order_manjian_money;

    @Bind(R.id.rl_member_equity)
    RelativeLayout rl_member_equity;

    @Bind(R.id.text_coupon_deduction_money)
    TextView text_coupon_deduction_money;
    @Bind(R.id.text_score_deduction_money)
    TextView text_score_deduction_money;

    @Bind(R.id.text_wait_money)
    TextView text_wait_money;
    @Bind(R.id.edit_input_money)
    EditText edit_input_money;
    @Bind(R.id.view_line)
    View view_line;

    @Bind(R.id.activity_payment_gridview)
    RecyclerView activity_payment_gridview;
    @Bind(R.id.listview_pay_info)
    ListView listview_pay_info;
    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;
    //订单总额
    private double orderTotleValue;
    //订单满减金额
    private double orderManjianValue;
    //待支付金额
    private double waitPayValue;
    private HorizontalKeyBoard keyboard;
    //支付方式适配器
    private PaymentTypeHolderAdapter paymentTypeAdapter;
    private ArrayList<PayMentsInfo> paymentTypes;

    // 支付方式标识
    private int paytype;
    //支付信息适配
    private List<PayMentsCancleInfo> payMentsCancle = new ArrayList<PayMentsCancleInfo>();
    private PaymentTypeInfoAdapter paymentTypeInfoadapter;
    private double payLing = 0;
    private double paymentMoney;

    public static final int THIRD_PAY_INFO = 1;
    public static final int ALREADY_THIRD_PAY_INFO = 2;
    public static final int PAY_SUCCESS = 900;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(CheckOutActivity.this, msg);
                    break;
                case PAY_SUCCESS:
                    if (waitPayValue == 0) {
                        clickCommitOrder();
                    }
                    break;
                case ALREADY_THIRD_PAY_INFO:
                    final List<ThirdPayInfo> thirdPayInfoList = (List<ThirdPayInfo>) msg.obj;
                    new AddPayinfoDialog(CheckOutActivity.this, thirdPayInfoList, new CancleAndConfirmback() {
                        @Override
                        public void doCancle() {

                        }

                        @Override
                        public void doConfirm(String num) {
                            for (ThirdPayInfo thirdPayInfo : thirdPayInfoList) {
                                if (!isContain(thirdPayInfo.getAuth_code(), payMentsCancle)) {
                                    PayMentsCancleInfo info = new PayMentsCancleInfo();
                                    info.setId(thirdPayInfo.getSkfsid());
                                    info.setName(getPayNameById(thirdPayInfo.getSkfsid()));
                                    info.setType(getPayTypeById(thirdPayInfo.getSkfsid()));
                                    info.setIsCancle(false);
                                    info.setMoney(thirdPayInfo.getJe());

                                    ThirdPay value = new ThirdPay();
                                    value.setTrade_no(thirdPayInfo.getAuth_code());
                                    info.setThridPay(value);
                                    info.setOverage("0");
                                    addPayTypeInfo(PaymentTypeEnum.ALIPAY, 0, 0, null, info);
                                }
                            }
                            waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                            text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                            edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                            if (waitPayValue == 0) {
                                clickCommitOrder();
                            }
                        }
                    }).show();
                    break;
                case THIRD_PAY_INFO:
                    OrderBean orderBean = (OrderBean)msg.obj;
                    orderBean.setPaymentId(paymentTypeAdapter.getPayType().getId());
                    orderBean.setTransType(ConstantData.TRANS_SALE);
                    orderBean.setTraceId(AppConfigFile.getBillId());
                    Intent serviceintent = new Intent(mContext, RunTimeService.class);
                    serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                    serviceintent.putExtra(ConstantData.THIRD_DATA, orderBean);
                    startService(serviceintent);
                    PayMentsCancleInfo infobank = new PayMentsCancleInfo();
                    infobank.setId(paymentTypeAdapter.getPayType().getId());
                    infobank.setName(paymentTypeAdapter.getPayType().getName());
                    infobank.setType(paymentTypeAdapter.getPayType().getType());
                    infobank.setIsCancle(false);
                    infobank.setTxnid(orderBean.getTxnId());
                    infobank.setTraceNo(orderBean.getBatchId());
                    infobank.setMoney(MoneyAccuracyUtils.makeRealAmount(orderBean.getTransAmount()));
                    infobank.setOverage("0");
                    addPayTypeInfo(PaymentTypeEnum.BANK, 0, 0, null, infobank);
                    waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                    text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                    edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                    if (waitPayValue == 0) {
                        clickCommitOrder();
                    }
                    break;
            }
        }
    };
    //会员验证方式
    private String member_type = ConstantData.MEMBER_VERIFY_BY_PHONE;
    // 会员标识
    private int isMember = ConstantData.MEMBER_IS_NOT_VERITY;
    // 会员信息
    private MemberInfo member;

    //会员权益
    private SubmitGoods member_equity = null;
    //销售人员名字
    private String salesman;
    //积分和用券金额
    private double orderScore = 0;
    private double orderCoupon = 0;
    private double orderScoreOverrage = 0;
    private double orderCouponOverrage = 0;

    // 账单信息
    private BillInfo bill;
    // 商品信息
    private List<GoodsInfo> cartgoods;

    // 使用卡券信息
    private List<CouponInfo> coupons;
    // 使用积分信息
    private ExchangeInfo exchangeInfo;
    private List<PayMentsInfo> payments = new ArrayList<PayMentsInfo>();

    private BizServiceInvoker mBizServiceInvoker;
    private ScannerManager scannerManager;

    @Override
    protected void initData() {
        exchangeInfo = new ExchangeInfo();
        exchangeInfo.setExchangemoney("0");
        exchangeInfo.setExchangepoint("0");
        title_text_content.setText(getString(R.string.checkout));

        text_cancle_pay.setBackgroundResource(R.drawable.btn_gray_bg);
        text_cancle_pay.setEnabled(false);
        cartgoods = (List<GoodsInfo>) getIntent().getSerializableExtra(ConstantData.CART_HAVE_GOODS);
        salesman = getIntent().getStringExtra(ConstantData.SALESMAN_NAME);
        isMember = getIntent().getIntExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_NOT_VERITY);
        if (isMember == ConstantData.MEMBER_IS_VERITY){
            member = (MemberInfo) getIntent().getSerializableExtra(ConstantData.GET_MEMBER_INFO);
            member_type =  getIntent().getStringExtra(ConstantData.MEMBER_VERIFY);
            member_equity =  (SubmitGoods) getIntent().getSerializableExtra(ConstantData.MEMBER_EQUITY);
            if(member_equity != null && (member_equity.getCouponInfos() != null || member_equity.getLimitpoint()!= null)){
                rl_member_equity.setVisibility(View.VISIBLE);
            }else{
                rl_member_equity.setVisibility(View.GONE);
            }
        }else{
            rl_member_equity.setVisibility(View.GONE);
        }
        orderTotleValue = getIntent().getDoubleExtra(ConstantData.GET_ORDER_VALUE_INFO, 0.0);
        text_order_total_money.setText(MoneyAccuracyUtils.getmoneybytwo(orderTotleValue));
        orderManjianValue = getIntent().getDoubleExtra(ConstantData.GET_ORDER_MANJIAN_VALUE_INFO, 0.0);
        if(orderManjianValue != 0){
            text_order_manjian_money.setText(MoneyAccuracyUtils.getmoneybytwo(orderManjianValue));
        }else{
            rl_order_manjian_money.setVisibility(View.GONE);
        }
        waitPayValue = ArithDouble.sub(orderTotleValue, orderManjianValue);
        edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
        text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));

        paymentTypes = new ArrayList<PayMentsInfo>();
        paymentTypeAdapter = new PaymentTypeHolderAdapter(paymentTypes, getApplicationContext());
        GridLayoutManager mgr=new GridLayoutManager(this, 4);
        mgr.setOrientation(LinearLayoutManager.VERTICAL);
        activity_payment_gridview.setLayoutManager(mgr);
        paymentTypeAdapter.setOnItemClickListener(new PaymentTypeHolderAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(Utils.isFastClick()){
                    return;
                }
                if (null == edit_input_money.getText() || "".equals(edit_input_money.getText().toString())) {
                    edit_input_money.setText("");
                    ToastUtils.sendtoastbyhandler(handler, "请先输入金额");
                    return;
                }
                double money = ArithDouble.parseDouble(edit_input_money.getText().toString());
                if (money <= 0) {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_money_not_zero));
                    return;
                }
                // 设置当前支付方式
                if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                    if(scannerManager != null){
                        try {
                            scannerManager.stopScan();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                paymentTypeAdapter.setPayTpye(position);
                doPay(money);
            }
        });
        activity_payment_gridview.setAdapter(paymentTypeAdapter);

        getPayType();
        paymentTypeInfoadapter = new PaymentTypeInfoAdapter(getApplicationContext(), payMentsCancle);
        listview_pay_info.setAdapter(paymentTypeInfoadapter);
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
            try {
                BaseSystemManager.getInstance().deviceServiceLogin(
                        this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                }
                            }
                        });
            } catch (SdkException e) {
                e.printStackTrace();
            }
        }
    }

    private void doPay(double money) {
        String type = paymentTypeAdapter.getPayType().getType();
        String payid = paymentTypeAdapter.getPayType().getId();
        //待支付的金额为0时，不允许继续支付
        if (waitPayValue <= 0) {
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_success));
            edit_input_money.setText("");
            paymentTypeAdapter.setPayTpyeNull();
            return;
        }
        paymentMoney = money;
        Intent intent_qr;
        switch (PaymentTypeEnum.getpaymentstyle(type.trim())){
            case RECORD:
                if(paymentMoney >waitPayValue){
                    edit_input_money.setText("");
                    paymentTypeAdapter.setPayTpyeNull();
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_large));
                }else{
                    new RecordPayDialog(this, new GeneralEditListener() {
                        @Override
                        public void editinput(String edit) {
                            if(edit == null){
                                paymentTypeAdapter.setPayTpyeNull();
                                ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                            }else{
                                PayMentsCancleInfo infobank = new PayMentsCancleInfo();
                                PayMentsInfo info = getPayInfoById(edit);
                                if(info != null){
                                    infobank.setId(info.getId());
                                    infobank.setName(info.getName());
                                    infobank.setType(info.getType());
                                    infobank.setIsCancle(false);
                                    infobank.setMoney(paymentMoney + "");
                                    infobank.setOverage("0");
                                    addPayTypeInfo(PaymentTypeEnum.RECORD, 0, 0, null, infobank);
                                    waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                                    text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                                    edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                                    handler.sendEmptyMessage(PAY_SUCCESS);
                                }else{
                                    paymentTypeAdapter.setPayTpyeNull();
                                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                                }
                            }
                        }
                    }).show();
                }
                break;
            case CASH:
                if(ConstantData.YXLM_ID.equals(paymentTypeAdapter.getPayType().getId())){
                    if(paymentMoney >waitPayValue){
                        edit_input_money.setText("");
                        paymentTypeAdapter.setPayTpyeNull();
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_large));
                    }else{
                        if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                            JSONObject json = new JSONObject();
                            String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
                            try {
                                json.put("amt",CurrencyUnit.yuan2fenStr(paymentMoney + ""));//TODO 金额格式
                                json.put("extOrderNo",tradeNo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            AppHelper.callTrans(CheckOutActivity.this, ConstantData.QMH, ConstantData.YHK_XF, json);
                        }else{
                            paymentTypeAdapter.setPayTpyeNull();
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_not_support));
                        }
                    }
                }else{
                    double cash = ArithDouble.parseDouble(paymentTypeInfoadapter.getMoneyById(paymentTypeAdapter.getPayType().getId()));
                    double ling = ArithDouble.parseDouble(paymentTypeInfoadapter.getMoneyById(PaymentTypeEnum.LING.getStyletype()));
                    if("1".equals(paymentTypeAdapter.getPayType().getId())){
                        if(money > (ArithDouble.sub(ArithDouble.add(waitPayValue, cash), ling))){
                            addPayTypeInfo(PaymentTypeEnum.CASH, money, 0, paymentTypeAdapter.getPayType(), null);
                            addPayTypeInfo(PaymentTypeEnum.LING, ArithDouble.sub(money, ArithDouble.sub(ArithDouble.add(waitPayValue, cash), ling)), 0, paymentTypeAdapter.getPayType(), null);
                        }else{
                            addPayTypeInfo(PaymentTypeEnum.CASH, money, 0, paymentTypeAdapter.getPayType(), null);
                            paymentTypeInfoadapter.removeLing();
                            paymentTypeAdapter.setPayTpyeNull();
                        }
                    }else{
                        if(money > ArithDouble.add(waitPayValue, cash)){
                            edit_input_money.setText("");
                            paymentTypeAdapter.setPayTpyeNull();
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_large));
                            return;
                        }else{
                            addPayTypeInfo(PaymentTypeEnum.CASH, money, 0, paymentTypeAdapter.getPayType(), null);
                        }
                    }
                    waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                    text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                    edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                    handler.sendEmptyMessage(PAY_SUCCESS);
                }
                break;
            case WECHAT:
//                if("1".equals(SpSaveUtils.read(getApplicationContext(), ConstantData.MALL_WEIXIN_IS_INPUT, "0"))){
//                    Intent intent_qr = new Intent(this, CaptureActivity.class);
//                    startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
//                }else{
//                    Intent intent = new Intent(this, ThirdPayDialog.class);
//                    intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
//                    intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
//                    startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
//                }
           //     break;
            case ALIPAY:
//                if(ConstantData.WECHAT_ID.equals(payid)){
//                    paytype = ConstantData.PAYMODE_BY_WEIXIN;
//                }else if(ConstantData.ALPAY_ID.equals(payid)){
//                    paytype = ConstantData.PAYMODE_BY_ALIPAY;
//                }else if(ConstantData.BANKCODE_ID.equals(payid)){
//                    paytype = ConstantData.PAYMODE_BY_BANKCODE;
//                }else if(ConstantData.YIPAY_ID.equals(payid)){
//                    paytype = ConstantData.PAYMODE_BY_YIPAY;
//                }
                if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                    int cameraType = SpSaveUtils.readInt(getApplicationContext(), ConstantData.CAMERATYPE, 1);
                    if(cameraType == 0){
                        ToastUtils.sendtoastbyhandler(handler, "请将二维码放置在摄像头前");
                        scannerManager = new ScannerManager();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ScannerConfig.COMM_SCANNER_TYPE, ConstantData.scanner_type);
                        bundle.putBoolean(ScannerConfig.COMM_ISCONTINUOUS_SCAN, false);
                        try {
                            scannerManager.stopScan();
                            scannerManager.initScanner(bundle);
                            scannerManager.startScan(30000, new OnScanListener() {
                                @Override
                                public void onScanResult(int i, byte[] bytes) {
                                    //防止用户未扫描直接返回，导致bytes为空
                                    if (bytes != null && !bytes.equals("")) {
                                        doThirdPay(new String(bytes));
                                    }
                                }
                            });
                        } catch (SdkException e) {
                            e.printStackTrace();
                        } catch (CallServiceException e) {
                            e.printStackTrace();
                        }
                    }else{
                        intent_qr = new Intent(mContext, CaptureActivity.class);
                        startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
                    }
                }else{
                    intent_qr = new Intent(mContext, CaptureActivity.class);
                    startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
                }
//                if("1".equals(SpSaveUtils.read(getApplicationContext(), ConstantData.MALL_ALIPAY_IS_INPUT, "0"))){
//                    Intent intent_qr = new Intent(this, CaptureActivity.class);
//                    startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
//                }else{
//                    Intent intent = new Intent(this, ThirdPayDialog.class);
//                    intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
//                    intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
//                    startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
//                }
                break;
            case BANK:
                if(paymentMoney >waitPayValue){
                    edit_input_money.setText("");
                    paymentTypeAdapter.setPayTpyeNull();
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_large));
                }else{
                    if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                        requestCashier(CurrencyUnit.yuan2fenStr(paymentMoney + ""));
                    }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
                        Intent intent = new Intent(this, ThirdPayDialog.class);
                        intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
                        intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
                        startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
                    }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){

                        JSONObject json = new JSONObject();
                        String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
                        try {
                            json.put("amt",CurrencyUnit.yuan2fenStr(paymentMoney + ""));//TODO 金额格式
                            json.put("extOrderNo",tradeNo);
                            AppHelper.callTrans(CheckOutActivity.this, ConstantData.YHK_SK, ConstantData.YHK_XF, json);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case STORE:
                if(paymentMoney >waitPayValue){
                    edit_input_money.setText("");
                    paymentTypeAdapter.setPayTpyeNull();
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_large));
                }else{
                    if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                        ToastUtils.sendtoastbyhandler(handler,"暂不支持！");
                    }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
                        ToastUtils.sendtoastbyhandler(handler,"暂不支持！");
                    }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                        JSONObject json = new JSONObject();
                        String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
                        try {
                            json.put("amt",CurrencyUnit.yuan2fenStr(paymentMoney + ""));//TODO 金额格式
                            json.put("extOrderNo",tradeNo);
                            AppHelper.callTrans(CheckOutActivity.this, ConstantData.STORE, ConstantData.YHK_XF, json);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    // 1.执行调用之前需要调用WeiposImpl.as().init()方法，保证sdk初始化成功。
    //
    // 2.调用收银支付成功后，收银支付结果页面完成后，BizServiceInvoker.OnResponseListener后收到响应的结果
    //
    // 3.如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
    private void innerRequestCashier(String total_fee) {
        // 1001 现金
        // 1003 微信
        // 1004 支付宝
        // 1005 百度钱包
        // 1006 银行卡
        // 1007 易付宝
        // 1009 京东钱包
        // 1011 QQ钱包
        String pay_type = "1006";
        String channel = "POS";//标明是pos调用，不需改变
        String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
        // String total_fee = "1";//支付金额，单位为分，1=0.01元，100=1元，不可空
        // 如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
        // 如果不需要回调页面，则backPkgName和backClassPath需要同时设置为空字符串 ："";
        String backPkgName = null;//，可空
        String backClassPath = null;//，可空
        //指定接收收银结果的url地址默认为："http://apps.weipass.cn/pay/notify"，可填写自己服务器接收地址
        String notifyUrl = null;//，可空
        String body = SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, "")+"店铺商品";//订单body描述信息 ，不可空
        String attach = "备注信息";//备注信息，可空，订单信息原样返回，可空
        // 第三方订单流水号，非空,发起请求，tradeNo不能相同，相同在收银会提示有存在订单
        String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
        try {
            RequestInvoke cashierReq = new RequestInvoke();
            cashierReq.pkgName = this.getPackageName();
            cashierReq.sdCode = CashierSign.Cashier_sdCode;// 收银服务的sdcode信息
            cashierReq.bpId = AppConfigFile.InvokeCashier_BPID;
            cashierReq.launchType = CashierSign.launchType;

            cashierReq.params = CashierSign.sign(AppConfigFile.InvokeCashier_BPID, AppConfigFile.InvokeCashier_KEY, channel,
                    pay_type, tradeNo, body, attach, total_fee, backPkgName, backClassPath, notifyUrl);
            cashierReq.seqNo = seqNo;

            RequestResult r = mBizServiceInvoker.request(cashierReq);
            LogUtil.i("lgs", r.token + "," + r.seqNo + "," + r.result);
            // 发送调用请求
            if (r != null) {
                LogUtil.i("lgs", "request result:" + r.result + "|launchType:" + cashierReq.launchType);
                String err = null;
                switch (r.result) {
                    case BizServiceInvoker.REQ_SUCCESS: {
                        // 调用成功
                        //ToastUtils.sendtoastbyhandler(handler, "收银服务调用成功");
                        break;
                    }
                    case BizServiceInvoker.REQ_ERR_INVAILD_PARAM: {
                        ToastUtils.sendtoastbyhandler(handler, "请求参数错误！");
                        break;
                    }
                    case BizServiceInvoker.REQ_ERR_NO_BP: {
                        ToastUtils.sendtoastbyhandler(handler, "未知的合作伙伴！");
                        break;
                    }
                    case BizServiceInvoker.REQ_ERR_NO_SERVICE: {
                        //调用结果返回，没有订阅对应bp账号中的收银服务，则去调用sdk主动订阅收银服务
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                ToastUtils.sendtoastbyhandler(handler, "正在申请订阅收银服务...");
                                // 如果没有订阅，则主动请求订阅服务
                                mBizServiceInvoker.subscribeService(CashierSign.Cashier_sdCode,
                                        AppConfigFile.InvokeCashier_BPID);
                            }
                        });
                        break;
                    }
                    case BizServiceInvoker.REQ_NONE: {
                        ToastUtils.sendtoastbyhandler(handler, "请求未知错误！");
                        break;
                    }
                }
                if (err != null) {
                    LogUtil.i("lgs", "serviceInvoker request err:" + err);
                }
            }else{
                ToastUtils.sendtoastbyhandler(handler, "请求结果对象为空！");
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这个是服务调用完成后的响应监听方法
     */
    private OnResponseListener mOnResponseListener = new OnResponseListener() {

        @Override
        public void onResponse(String sdCode, String token, byte[] data) {
            // 收银服务调用完成后的返回方法
            String result = new String(data);
            WposPayInfo info = null;
            try {
                info = GsonUtil.jsonToBean(result, WposPayInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtil.i("lgs",
                   "sdCode = " + sdCode + " , token = " + token + " , data = " + new String(data));
            if(info != null){
                if(info.getErrCode().equals("0")){
                    if(!"1001".equals(info.getPay_type())){
                        if("PAY".equals(info.getTrade_status())){
                            OrderBean orderBean= new OrderBean();
                            orderBean.setAccountNo(CurrencyUnit.yuan2fenStr(edit_input_money.getText().toString()));
                            orderBean.setTxnId(info.getCashier_trade_no());
                            if(info.getBuy_user_info() != null){
                                orderBean.setAccountNo(info.getBuy_user_info().getBank_no());
                                orderBean.setRefNo(info.getBuy_user_info().getRef_no());
                                orderBean.setBatchId(info.getBuy_user_info().getVoucher_no());
                            }
                            Message msg = Message.obtain();
                            msg.what = THIRD_PAY_INFO;
                            msg.obj = orderBean;
                            handler.sendMessage(msg);
                        }else{
                            //TODO
                            ToastUtils.sendtoastbyhandler(handler, info.getPay_info());
                        }
                    }else{
                        ToastUtils.sendtoastbyhandler(handler, "使用了现金交易，该交易不记账，小票无效");
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, info.getErrMsg());
                }
            }else{
                ToastUtils.sendtoastbyhandler(handler, "未知错误");
            }
        }

        @Override
        public void onFinishSubscribeService(boolean result, String err) {
            // TODO Auto-generated method stub
            // 申请订阅收银服务结果返回
            // bp订阅收银服务返回结果
            if (!result) {
                //订阅失败
                ToastUtils.sendtoastbyhandler(handler, err);
            }else{
                //订阅成功
                ToastUtils.sendtoastbyhandler(handler, "订阅收银服务成功，请按home键回调主页刷新订阅数据后重新进入调用收银");
            }
        }
    };

    /**
     * 本地调用收银服务
     */
    private void requestCashier(String money) {

        try {
            // 初始化服务调用
            mBizServiceInvoker = WeiposImpl.as().getService(BizServiceInvoker.class);
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (mBizServiceInvoker == null) {
            ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
            return;
        }
        // 设置请求订阅服务监听结果的回调方法
        mBizServiceInvoker.setOnResponseListener(mOnResponseListener);
        innerRequestCashier(money);
    }

    private void addPayTypeInfo(PaymentTypeEnum enumValue, double money, int Overage, PayMentsInfo payType, PayMentsCancleInfo info) {
        if (info != null) {
            paymentTypeInfoadapter.add(info);
            paymentTypeAdapter.setPayTpyeNull();
            waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
            text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
            edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
            return;
        }
        PayMentsCancleInfo payMentsInfo = new PayMentsCancleInfo();
        payMentsInfo.setName(payType.getName());
        payMentsInfo.setOverage(Overage+"");
        if(enumValue != PaymentTypeEnum.LING){
            payMentsInfo.setId(paymentTypeAdapter.getPayType().getId());
        }else{
            //对找零单独做处理
            payMentsInfo.setId(PaymentTypeEnum.LING.getStyletype());
            payMentsInfo.setName(getResources().getString(R.string.change));
            payMentsInfo.setOverage("0");
        }

        //因为现金存在一个找零的操作，所以不能制空，需要手动置空
        if(!(enumValue == PaymentTypeEnum.CASH)){
            paymentTypeAdapter.setPayTpyeNull();
        }
        payMentsInfo.setIsCancle(false);
        payMentsInfo.setMoney(String.valueOf(money));
        payMentsInfo.setType(enumValue.getStyletype());
        paymentTypeInfoadapter.add(payMentsInfo);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_check_out);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        edit_input_money.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    view_line.setBackgroundResource(R.color.green);
                } else {
                    view_line.setBackgroundResource(R.color.view_black_line_color);
                }
            }
        });
        keyboard = new HorizontalKeyBoard(this, this, edit_input_money, ll_keyboard, new KeyBoardListener() {
            @Override
            public void onComfirm() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onValue(String value) {
                double money;
                try {
                    money = Double.parseDouble(value);
                    BigDecimal b = new BigDecimal(money);
                    money = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                } catch (Exception e) {
                    edit_input_money.setText("");
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_format_msg));
                    return;
                }
                if(money > 1000000){
                    edit_input_money.setText("");
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_money_over));
                    return;
                }
                edit_input_money.setText(""+money);
            }
        });

    }

    @Override
    protected void recycleMemery() {
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
        if(scannerManager != null){
            try {
                scannerManager.stopScan();
            } catch (SdkException e) {
                e.printStackTrace();
            } catch (CallServiceException e) {
                e.printStackTrace();
            }
        }
        try {
            BaseSystemManager.getInstance().deviceServiceLogout();
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.tv_pay_search, R.id.text_cancle_pay, R.id.title_icon_back, R.id.imageview_more, R.id.text_submit_order})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.text_cancle_pay:
                if(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), waitPayValue) <= 0){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_return_no));
                    return;
                }
                Intent intentCancle = new Intent(this, CanclePayDialog.class);
                intentCancle.putExtra(ConstantData.CANCLE_LIST, (Serializable)payMentsCancle);
                startActivityForResult(intentCancle, ConstantData.THRID_CANCLE_REQUEST_CODE);
                break;
            case R.id.text_submit_order:
                if (waitPayValue >= 0) {
                    clickCommitOrder();
                }else{
                    ToastUtils.sendtoastbyhandler(handler,"支付信息异常");
                }
                break;
            case R.id.imageview_more:
            case R.id.ll_member_equity:
                if(member_type != null && !member_type.equals(ConstantData.MEMBER_VERIFY_BY_PHONE)){
                    if(member_equity != null && (member_equity.getCouponInfos() != null || member_equity.getLimitpoint()!= null)){
                        Intent intent = new Intent(this, MemberEquityActivity.class);
                        intent.putExtra(ConstantData.CART_ALL_MONEY, ArithDouble.add(ArithDouble.add(waitPayValue, orderScore), orderCoupon)+"");
                        intent.putExtra(ConstantData.MEMBER_EQUITY, member_equity);
                        intent.putExtra(ConstantData.GET_MEMBER_INFO, member);

                        intent.putExtra(ConstantData.GET_ORDER_SCORE_OVERAGE, orderScoreOverrage);
                        intent.putExtra(ConstantData.GET_ORDER_SCORE_INFO, orderScore);
                        intent.putExtra(ConstantData.USE_INTERRAL, (Serializable)exchangeInfo);

                        intent.putExtra(ConstantData.GET_ORDER_COUPON_OVERAGE, orderCouponOverrage);
                        intent.putExtra(ConstantData.GET_ORDER_COUPON_INFO, orderCoupon);
                        intent.putExtra(ConstantData.CAN_USED_COUPON, (Serializable)coupons);
                        startActivityForResult(intent, ConstantData.MEMBER_EQUITY_REQUEST_CODE);
                    }else {
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_no_equity));
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, "手机号验证会员不允许使用权益");
                    return;
                }
                break;
            case R.id.title_icon_back:
                if (payMentsCancle.size()>0) {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_return_failed));
                    return;
                }
                this.finish();
                break;
            case R.id.tv_pay_search:
                Map<String, String> map = new HashMap<String, String>();
                map.put("billid", AppConfigFile.getBillId());
                HttpRequestUtil.getinstance().searchPayinfo(HTTP_TASK_KEY, map, ThirdPayInfoResult.class, new HttpActionHandle<ThirdPayInfoResult>() {
                    @Override
                    public void handleActionError(String actionName, String errmsg) {
                        ToastUtils.sendtoastbyhandler(handler, errmsg);
                    }

                    @Override
                    public void handleActionSuccess(String actionName, ThirdPayInfoResult result) {

                        if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                            if (result != null && result.getThirdPayInfo() != null && result.getThirdPayInfo().size() > 0) {
                                Message msg = Message.obtain();
                                msg.what = ALREADY_THIRD_PAY_INFO;
                                msg.obj = result.getThirdPayInfo();
                                handler.sendMessage(msg);
                            } else {
                                ToastUtils.sendtoastbyhandler(handler, "没有交易记录");
                            }
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }

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
                });
                break;
        }
    }

    private void clickCommitOrder(){
        payments.clear();
        if(waitPayValue > 0.0){
            if(paymentTypeAdapter.getPayType() != null && !edit_input_money.getText().toString().equals("") && paymentTypeAdapter.getPayType().getType().equals(PaymentTypeEnum.CASH.getStyletype())){

                PayMentsInfo payMentsInfoCoupon = new PayMentsInfo();
                payMentsInfoCoupon.setId(getPayTypeId(PaymentTypeEnum.CASH));
                payMentsInfoCoupon.setType(PaymentTypeEnum.CASH.getStyletype());
                payMentsInfoCoupon.setName(paymentTypeAdapter.getPayType().getName());
                payMentsInfoCoupon.setMoney(String.valueOf(ArithDouble.add(waitPayValue, 0)));
                payMentsInfoCoupon.setOverage(String.valueOf(0));
                payments.add(payMentsInfoCoupon);
            }else{
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_failed));
                return;
            }
        }
        //增加积分抵扣
        if (orderScore > 0) {
            PayMentsInfo payMentsInfoCoupon = new PayMentsInfo();
            payMentsInfoCoupon.setId(getPayTypeId(PaymentTypeEnum.SCORE));
            payMentsInfoCoupon.setType(PaymentTypeEnum.SCORE.getStyletype());
            payMentsInfoCoupon.setMoney(String.valueOf(ArithDouble.add(orderScore, orderScoreOverrage)));
            payMentsInfoCoupon.setOverage(String.valueOf(orderScoreOverrage));
            payments.add(payMentsInfoCoupon);
        }
        //增加优惠券
        if (orderCoupon > 0.0) {
            PayMentsInfo payMentsInfoCoupon = new PayMentsInfo();
            payMentsInfoCoupon.setId(getPayTypeId(PaymentTypeEnum.COUPON));
            payMentsInfoCoupon.setType(PaymentTypeEnum.COUPON.getStyletype());
            payMentsInfoCoupon.setMoney(String.valueOf(ArithDouble.add(orderCoupon, orderCouponOverrage)));
            payMentsInfoCoupon.setOverage(String.valueOf(orderCouponOverrage));
            payments.add(payMentsInfoCoupon);
        }
        //除去找零，其他方式都加入到支付方式中
        for (int i = 0; i < payMentsCancle.size(); i++){
            if (payMentsCancle.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())) {
                payLing = ArithDouble.parseDouble(payMentsCancle.get(i).getMoney());
                break;
            }
        }
        for (int i = 0; i < payMentsCancle.size(); i++) {
            if (payMentsCancle.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())) {
                continue;
            }
            PayMentsInfo payMentsInfoCoupon = new PayMentsInfo();

            if (payMentsCancle.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype()) && payMentsCancle.get(i).getId().equals("1")) {
                //减去找零金额
                payMentsInfoCoupon.setMoney(String.valueOf(ArithDouble.sub(ArithDouble.parseDouble(payMentsCancle.get(i).getMoney()), payLing)));
            } else {
                payMentsInfoCoupon.setMoney(String.valueOf(ArithDouble.add(ArithDouble.parseDouble(payMentsCancle.get(i).getMoney()), ArithDouble.parseDouble(payMentsCancle.get(i).getOverage()))));
            }
            payMentsInfoCoupon.setId(payMentsCancle.get(i).getId());
            payMentsInfoCoupon.setName(payMentsCancle.get(i).getName());
            payMentsInfoCoupon.setType(payMentsCancle.get(i).getType());
            payMentsInfoCoupon.setOverage(payMentsCancle.get(i).getOverage());
            payments.add(payMentsInfoCoupon);
        }
        commitOrder();
    }

    private boolean isContain(String code, List<PayMentsCancleInfo> payMentsCancle){
        boolean ret = false;
        if(StringUtil.isEmpty(code) || payMentsCancle.size() == 0){
            return ret;
        }
        for (PayMentsCancleInfo info : payMentsCancle) {
            if (info.getThridPay() != null && !StringUtil.isEmpty(info.getThridPay().getTrade_no())) {
                if(code.equals(info.getThridPay().getTrade_no())){
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    // 清空左面的显示支付方式（已经撤销的）
    public void deletepayMentsInfo(List<PayMentsCancleInfo> cancleInfoList) {
        payMentsCancle.clear();
        payMentsCancle.addAll(cancleInfoList);
        double cancle = 0;
        for (int i = 0; i < payMentsCancle.size(); i++) {
            PayMentsCancleInfo info = payMentsCancle.get(i);
            if (info.getIsCancle()) {
                cancle = ArithDouble.add(cancle, ArithDouble.parseDouble(info.getMoney()));
                payMentsCancle.remove(i);
                i--;
            }
        }
        if(cancle > 0){
            //如果有找零，对找零做处理
            for (int i = 0; i < payMentsCancle.size(); i++) {
                PayMentsCancleInfo info = payMentsCancle.get(i);
                if (info.getType().equals(PaymentTypeEnum.LING.getStyletype())) {
                    if(cancle >= ArithDouble.parseDouble(info.getMoney())){
                        payMentsCancle.remove(i);
                    }else{
                        info.setMoney(""+ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), cancle));
                    }
                    break;
                }
            }
        }

        paymentTypeInfoadapter.notifyDataSetChanged();
        waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
        text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
        edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(resultCode == ConstantData.MEMBER_EQUITY_RESULT_CODE){
            if(requestCode == ConstantData.MEMBER_EQUITY_REQUEST_CODE){
                orderScoreOverrage = data.getDoubleExtra(ConstantData.GET_ORDER_SCORE_OVERAGE, 0.0);
                // 使用的积分抵扣金额
                orderScore = ArithDouble.sub(data.getDoubleExtra(ConstantData.GET_ORDER_SCORE_INFO, 0.0), orderScoreOverrage);
                text_score_deduction_money.setText(orderScore + "");
                exchangeInfo = (ExchangeInfo) data.getSerializableExtra(ConstantData.USE_INTERRAL);
                // 使用的卡券
                // 获取使用卡券信息
                coupons = (List<CouponInfo>) data.getSerializableExtra(ConstantData.CAN_USED_COUPON);
                orderCouponOverrage = data.getDoubleExtra(ConstantData.GET_ORDER_COUPON_OVERAGE, 0.0);
                orderCoupon = ArithDouble.sub(data.getDoubleExtra(ConstantData.GET_ORDER_COUPON_INFO, 0.0), orderCouponOverrage);
                text_coupon_deduction_money.setText(orderCoupon + "");
                // 设置实付金额和待付金额
                waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                handler.sendEmptyMessage(PAY_SUCCESS);
            }
        }else if(resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY){
            if(requestCode == ConstantData.QRCODE_REQURST_QR_PAY){
                String qrCode = data.getExtras().getString("QRcode");
                if(StringUtil.isEmpty(qrCode)){
                    ToastUtils.sendtoastbyhandler(handler, "扫描失败");
                }else{
                    doThirdPay(qrCode);
                }
            }
        }else if(resultCode == ConstantData.THRID_PAY_RESULT_CODE){
            if(requestCode == ConstantData.THRID_PAY_REQUEST_CODE){
                Message msg = Message.obtain();
                msg.what = THIRD_PAY_INFO;
                msg.obj = data.getSerializableExtra(ConstantData.ORDER_BEAN);
                handler.sendMessage(msg);
            }
        }else if(resultCode == ConstantData.THRID_CANCLE_RESULT_CODE){
            if(requestCode == ConstantData.THRID_CANCLE_REQUEST_CODE){
                deletepayMentsInfo((List<PayMentsCancleInfo>) data.getSerializableExtra(ConstantData.CANCLE_LIST));
            }
        }else if(Activity.RESULT_OK == resultCode){
            if(AppHelper.TRANS_REQUEST_CODE == requestCode){
                if (null != data) {
                    StringBuilder result = new StringBuilder();
                    Map<String,String> map = AppHelper.filterTransResult(data);
                    if("0".equals(map.get(AppHelper.RESULT_CODE))){
                        Type type =new TypeToken<Map<String, String>>(){}.getType();
                        try {
                            Map<String, String> transData = GsonUtil.jsonToObect(map.get(AppHelper.TRANS_DATA), type);
                            if("00".equals(transData.get("resCode"))){
                                OrderBean orderBean= new OrderBean();
                                orderBean.setTransAmount(transData.get("amt"));
                                orderBean.setTxnId(transData.get("extOrderNo"));
                                orderBean.setAccountNo(transData.get("cardNo"));
                                orderBean.setAcquId(transData.get("cardIssuerCode"));
                                orderBean.setBatchId(transData.get("traceNo"));
                                orderBean.setRefNo(transData.get("refNo"));
                                Message msg = Message.obtain();
                                msg.what = THIRD_PAY_INFO;
                                msg.obj = orderBean;
                                handler.sendMessage(msg);
                            }else{
                                ToastUtils.sendtoastbyhandler(handler, transData.get("resDesc"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler,"支付异常！");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doThirdPay(String qrCode){
        AlipayAndWeixinPayControllerInterfaceDialog paydialog = new AlipayAndWeixinPayControllerInterfaceDialog(this, paymentTypeAdapter.getPayType().getId(), paytype, ConstantData.THIRD_OPERATION_PAY, qrCode, paymentMoney, true,
                new AlipayAndWeixinPayControllerInterfaceDialog.GetPayValue() {

                    @Override
                    public void getPayValue(ThirdPay value) {
                        LogUtil.i("lgs","--------1-------");
                        PayMentsInfo payMentsInfo = getPayInfoById(value.getSkfsid());
                        PayMentsCancleInfo info = new PayMentsCancleInfo();
                        if(payMentsInfo == null){
                            info.setId(value.getSkfsid());
                            info.setName("异常支付");
                            info.setType(PaymentTypeEnum.ALIPAY.getStyletype());
                        }else{
                            info.setId(payMentsInfo.getId());
                            info.setName(payMentsInfo.getName());
                            info.setType(payMentsInfo.getType());
                        }
                        info.setIsCancle(false);
                        info.setMoney(String.valueOf(ArithDouble.parseDouble(value.getPay_total_fee()) / 100));
                        info.setThridPay(value);
                        info.setOverage("0");
                        addPayTypeInfo(PaymentTypeEnum.ALIPAY, 0, 0, null, info);
                        waitPayValue = ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderManjianValue), ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                        text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                        edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                        handler.sendEmptyMessage(PAY_SUCCESS);
                    }
                });
        paydialog.show();
//                Intent intent = new Intent(this, ThirdPayDialog.class);
//                intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
//                intent.putExtra(ConstantData.BSC, data.getExtras().getString("QRcode"));
//                intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
//                startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
    }
    private void commitOrder() {
        bill = new BillInfo();
        bill.setBillid(AppConfigFile.getBillId());
        if(coupons != null){
            bill.setUsedcouponlist(coupons);
        }
        bill.setBackreason("null");
        bill.setSaletime(Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"));
        if (payLing > 0) {
            bill.setChangemoney(String.valueOf(payLing));
        } else {
            bill.setChangemoney("0");
        }
        bill.setPaymentslist(payments);
        bill.setExchange(exchangeInfo);
        bill.setTotalmbjmoney(orderManjianValue+"");
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put("billInfo", GsonUtil.beanToJson(bill));
        } catch (Exception e) {
            LogUtil.v("lgs", "订单转换失败");
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().saveReturnOrder(HTTP_TASK_KEY, map, SaveOrderResult.class, new HttpActionHandle<SaveOrderResult>() {
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
            public void handleActionSuccess(String actionName, SaveOrderResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    if (isMember == ConstantData.MEMBER_IS_VERITY) {
                        bill.setMember(member);
                        bill.setUsedpoint(getExchangPoint(cartgoods) + "");
                        bill.setExchangedpoint(exchangeInfo.getExchangepoint());
                        bill.setTotalpoint(result.getSaveOrderInfo().getTotalpoint());
                    }
                    bill.setParkcouponhour(result.getSaveOrderInfo().getParkcouponhour());
                    bill.setParkcouponaddhour(result.getSaveOrderInfo().getParkcouponaddhour());
                    bill.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
                    bill.setCashier(SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""));
                    bill.setCashiername(SpSaveUtils.read(mContext, ConstantData.CASHIER_NAME, ""));
                    bill.setCashierxtm(SpSaveUtils.read(mContext, ConstantData.PERSON_XTM, ""));
                    bill.setSalemanname(salesman);
                    CashierInfo cashierInfo = getSalemanCode(salesman);
                    if(cashierInfo != null){
                        bill.setSaleman(cashierInfo.getCashierid());
                        bill.setSalemanxtm(cashierInfo.getPersonxtm());
                    }
                    bill.setRealmoney("" + ArithDouble.sub(orderTotleValue, ArithDouble.add(orderCoupon, orderScore)));

                    bill.setRandomcode(result.getSaveOrderInfo().getRandomcode());
                    bill.setAwardpoint(result.getSaveOrderInfo().getGainpoint());
                    bill.setTotalmoney(String.valueOf(orderTotleValue));
                    bill.setGoodslist(cartgoods);
                    bill.setGrantcouponlist(result.getSaveOrderInfo().getGrantcouponlist());
                    bill.setAllcouponlist(result.getSaveOrderInfo().getAllcouponlist());
                    bill.setDfqlist(result.getSaveOrderInfo().getDfqlist());
                    AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                    AppConfigFile.setBillId(result.getSaveOrderInfo().getBillid());
                    Intent intent_pay_detail = new Intent(CheckOutActivity.this, PaymentDetailActivity.class);
                    intent_pay_detail.putExtra(ConstantData.VERIFY_IS_MEMBER, isMember);
                    intent_pay_detail.putExtra(ConstantData.ORDER_INFO, bill);
                    startActivity(intent_pay_detail);
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                CheckOutActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(CheckOutActivity.this, httpActionHandle).show();
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                AppConfigFile.setBillId(String.valueOf(Long.parseLong(AppConfigFile.getBillId()) + 1));
                Intent intent = new Intent(CheckOutActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void handleActionOffLine() {
                OrderInfoDao dao = new OrderInfoDao(mContext);
                boolean result = dao.addOrderPaytypeinfo(AppConfigFile.getBillId(), null, null, null, payLing, "1", SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""), payments);
                if(result){
                    bill.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
                    bill.setCashier(SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""));
                    bill.setCashiername(SpSaveUtils.read(mContext, ConstantData.CASHIER_NAME, ""));
                    bill.setCashierxtm(SpSaveUtils.read(mContext, ConstantData.PERSON_XTM, ""));
                    bill.setSalemanname(salesman);
                    CashierInfo cashierInfo = getSalemanCode(salesman);
                    if(cashierInfo != null){
                        bill.setSaleman(cashierInfo.getCashierid());
                        bill.setSalemanxtm(cashierInfo.getPersonxtm());
                    }
                    bill.setRealmoney("" + ArithDouble.sub(orderTotleValue, ArithDouble.add(orderCoupon, orderScore)));
                    bill.setTotalmoney(String.valueOf(orderTotleValue));
                    bill.setGoodslist(cartgoods);
                    AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                    AppConfigFile.setBillId(String.valueOf(Long.parseLong(AppConfigFile.getBillId()) + 1));

                    Intent intent_pay_detail = new Intent(CheckOutActivity.this, PaymentDetailActivity.class);
                    intent_pay_detail.putExtra(ConstantData.VERIFY_IS_MEMBER, isMember);
                    intent_pay_detail.putExtra(ConstantData.ORDER_INFO, bill);
                    startActivity(intent_pay_detail);
                }else{
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_save_order_failed));
                }
            }
        });
    }

    /**
     * 获取支付方式
     *
     */
    private void getPayType() {

        boolean isRecorded = false;
        boolean isThird = false;
        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist != null){
            for (int i = 0; i < paymentslist.size(); i++) {
                if (paymentslist.get(i).getType().equals(PaymentTypeEnum.ALIPAY.getStyletype())
                        || paymentslist.get(i).getType().equals(PaymentTypeEnum.WECHAT.getStyletype())) {
                    if(!AppConfigFile.isOffLineMode()){
                        if(!isThird){
                            PayMentsInfo info = new PayMentsInfo();
                            info.setName("融合支付");
                            info.setType(paymentslist.get(i).getType());
                            info.setId(paymentslist.get(i).getId());
                            paymentTypeAdapter.add(info);
                            isThird = true;
                        }
                    }
                }else if(paymentslist.get(i).getType().equals(PaymentTypeEnum.BANK.getStyletype())
                        || paymentslist.get(i).getType().equals(PaymentTypeEnum.STORE.getStyletype())){
                    if(!AppConfigFile.isOffLineMode()){
                        paymentTypeAdapter.add(paymentslist.get(i));
                    }
                }else if(paymentslist.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype())){
                    if(ConstantData.YXLM_ID.equals(paymentslist.get(i).getId())){
                        if(!AppConfigFile.isOffLineMode()){
                            paymentTypeAdapter.add(paymentslist.get(i));
                        }
                    }else{
                        paymentTypeAdapter.setPayTpye(paymentslist.get(i));
                        paymentTypeAdapter.notifyDataSetChanged();
                        paymentTypeAdapter.add(paymentslist.get(i));
                    }
                }
//                else if(paymentslist.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype())
//                        || paymentslist.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype())
//                        || paymentslist.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype())){
//                    if(!isRecorded){
//                        paymentTypeAdapter.add(paymentslist.get(i));
//                        isRecorded = true;
//                    }
//                }
            }
            PayMentsInfo info = new PayMentsInfo();
            info.setName("补录");
            info.setType(PaymentTypeEnum.RECORD.getStyletype());
            info.setId(PaymentTypeEnum.RECORD.getStyletype());
            paymentTypeAdapter.add(info);
        }
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
     * @Description: 获取支付方式Id
     *
     */
    private String getPayTypeId(PaymentTypeEnum typeEnum) {

        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null)
            return null;
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getType().equals(typeEnum.getStyletype())) {
                return paymentslist.get(i).getId();
            }
        }
        return null;
    }

    private PayMentsInfo getPayInfoById(String id){
        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null)
            return null;
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getId().equals(id)) {
                return paymentslist.get(i);
            }
        }
        return null;
    }

    private String getPayTypeById(String id){
        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null)
            return null;
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getId().equals(id)) {
                return paymentslist.get(i).getType();
            }
        }
        return null;
    }

    private String getPayNameById(String id){
        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null)
            return null;
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getId().equals(id)) {
                return paymentslist.get(i).getName();
            }
        }
        return null;
    }
    /**
     * 计算总积分
     */
    public double getExchangPoint(List<GoodsInfo> cartgoods){
        double ret = 0;
        if(cartgoods == null){
            return ret;
        }
        for(GoodsInfo goods:cartgoods){
            ret = ArithDouble.add(ret, ArithDouble.parseDouble(goods.getUsedpoint()));
        }
        return ret;
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2016年1月20日
     * @Description: 获取销售人员Code
     *
     */
    private CashierInfo getSalemanCode(String typeEnum) {

        List<CashierInfo> sales = (List<CashierInfo>) SpSaveUtils.getObject(mContext, ConstantData.SALEMANLIST);
        if(sales == null)
            return null;
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).getCashiername().equals(typeEnum)) {
                return sales.get(i);
            }
        }
        return null;
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

    @Override
    public void onResume() {
        registerReceiver(broadcastReceiver, new IntentFilter(
                "cn.koolcloud.engine.ThirdPartyTrans"));
        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    /**
     * 处理交易结果
     *
     * @param isSuccess
     * @param data
     */

    private void handleTransResult(boolean isSuccess, String data) {
        if (isSuccess) {
            OrderBean resultBean = parse(data, isSuccess);
            LogUtil.e("lgs", resultBean.toString());
            Intent intent = new Intent();
            if (resultBean.getTransType().equals(ConstantData.SALE)) {
                Message msg = Message.obtain();
                msg.what = THIRD_PAY_INFO;
                msg.obj = resultBean;
                handler.sendMessage(msg);
            } else {
                LogUtil.e("lgs", data);
                ToastUtils.sendtoastbyhandler(handler,data);
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
}
