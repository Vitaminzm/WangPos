package com.symboltech.wangpos.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.ReturnReasonAdapter;
import com.symboltech.wangpos.adapter.ReturnTableAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.dialog.AlipayAndWeixinPayReturnDialog;
import com.symboltech.wangpos.dialog.BankreturnDialog;
import com.symboltech.wangpos.dialog.ThirdPayReturnDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.OnReturnFinishListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BankPayInfo;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.ExchangeInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.RefundReasonInfo;
import com.symboltech.wangpos.msg.entity.ThirdPay;
import com.symboltech.wangpos.msg.entity.WposBankRefundInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.result.SaveOrderResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.CashierSign;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.ListViewForScrollView;
import com.ums.AppHelper;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
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
import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

public class ReturnMoneyByOrderActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnTouchListener {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.edit_input_money)
    TextView edit_input_reason;
    @Bind(R.id.imageview_drop_arrow)
    ImageView imageview_drop_arrow;

    @Bind(R.id.text_return_total_money)
    TextView text_return_total_money;
    @Bind(R.id.listview_return_type)
    ListViewForScrollView listview_return_type;

    @Bind(R.id.ll_return_score_info)
    LinearLayout ll_return_score_info;
    @Bind(R.id.text_add_used_score_total)
    TextView text_add_used_score_total;

    @Bind(R.id.ll_score_good)
    LinearLayout ll_score_good;
    @Bind(R.id.text_score_good)
    TextView text_score_good;

    @Bind(R.id.ll_return_score)
    LinearLayout ll_return_score;
    @Bind(R.id.text_return_score)
    TextView text_return_score;

    @Bind(R.id.ll_score_deduction)
    LinearLayout ll_score_deduction;
    @Bind(R.id.text_score_deduction)
    TextView text_score_deduction;

    private View reasonPop;
    private ListView reasonList;
    private PopupWindow PopupWindowReason;
    private List<RefundReasonInfo> reasons;
    private LayoutInflater inflater;
    private BillInfo billInfo;

    private Map<String, String> idTypes = new HashMap<String, String>();
    private Map<String, String> idNames = new HashMap<String, String>();

    private boolean flag = false;//是否已经处理过 默认是false
    /**commitStyles - 提交的退款方式*/
    private List<PayMentsInfo> commitStyles = new ArrayList<>();
    /** 退货原因-原因id */
    private Map<String, String> reasonIds = new HashMap<>();

    /**
     * alipayStyle-支付宝支付
     * weixinStyle-微信支付
     * cashStyle-现金支付
     */
    private List<PayMentsInfo>  alipayStyle, weixinStyle, cashStyle;
    /**支付宝id -- 对应id的支付详情集合*/
    private List<ThirdPay> thirdAlipays = new ArrayList<ThirdPay>();
    /**微信id -- 对应id的微信详情集合*/
    private List<ThirdPay> thirdWeixins = new ArrayList<ThirdPay>();
    /**
     * bankStyle-银行卡支付
     */
    private List<BankPayInfo> bankStyle;
    /**
     * allowanceBankStyle-银行卡补录
     * allowanceAlipayStyle-支付宝补录
     * allowanceWeixinStyle-微信补录
     */
    private List<PayMentsInfo> allowanceBankStyle, allowanceAlipayStyle, allowanceWeixinStyle;
    private int bankFlag = 0, alipayFlag = 0, weixinFlag = 0, cashFlag,
            allowanceBankFlag = 0, allowanceAlipayFlag = 0, allowanceWeixinFlag = 0;
    /**积分抵扣信息*/
    private ExchangeInfo exchangeInfo;
    /**coupon-优惠券 */
    private PayMentsInfo coupon;
    /**deduction-积分抵扣*/
    private PayMentsInfo deduction;

    private List<PayMentsInfo> payMentsInfos;
    private double realMoney;
    private double couponMoney;
    private double scoreMoney;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        edit_input_reason.setText(reasons.get(position).getName());
        PopupWindowReason.dismiss();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN)
            showReason(edit_input_reason);
        return false;
    }

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
        title_text_content.setText(getString(R.string.return_order_info));
        billInfo = (BillInfo) getIntent().getSerializableExtra(ConstantData.BILL);
        payMentsInfos = billInfo.getPaymentslist();
        if(payMentsInfos != null && payMentsInfos.size() != 0) {
            for (PayMentsInfo info : payMentsInfos) {
                idTypes.put(info.getId(), info.getType());
                idNames.put(info.getId(), info.getName());
            }
        }
        text_return_total_money.setText(billInfo.getTotalmoney());
        if(billInfo.getMember() != null){
            ll_return_score_info.setVisibility(View.VISIBLE);
            double score_deduction = 0,score_good = 0,return_score = 0;
            if(billInfo.getExchange()!= null){
                score_deduction = ArithDouble.parseDouble(billInfo.getExchange().getExchangepoint());
                text_score_deduction.setText(billInfo.getExchange().getExchangepoint());
            }else{
                ll_score_deduction.setVisibility(View.GONE);
            }
            return_score = ArithDouble.parseDouble(billInfo.getAwardpoint());
            if(return_score > 0){
                text_return_score.setText("-"+billInfo.getAwardpoint());
            }else{
                text_return_score.setText(billInfo.getAwardpoint());
            }
            if(return_score == 0){
                ll_return_score.setVisibility(View.GONE);
            }
            score_good = ArithDouble.parseDouble(billInfo.getUsedpoint());
            if(score_good != 0){
                text_score_good.setText(billInfo.getUsedpoint());
            }else{
                ll_score_good.setVisibility(View.GONE);
            }
            text_add_used_score_total.setText(ArithDouble.sub(ArithDouble.add(score_deduction, score_good),return_score)+"");
        }else{
            ll_return_score_info.setVisibility(View.GONE);
        }
        if (reasons != null && reasons.size() > 0) {
            edit_input_reason.setText(reasons.get(0).getName());
        }else {
            edit_input_reason.setText(R.string.warning_no);
        }
        getReturnMoneyInfo();
        realMoney = ArithDouble.sub(ArithDouble.parseDouble(billInfo.getTotalmoney()), ArithDouble.add(couponMoney, scoreMoney));
        listview_return_type.setAdapter(new ReturnTableAdapter(this, payMentsInfos));
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
        setContentView(R.layout.activity_return_money_by_order);
        mContext = ReturnMoneyByOrderActivity.this;
        inflater = LayoutInflater.from(mContext);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        initReasonView();
        edit_input_reason.setOnTouchListener(this);
        imageview_drop_arrow.setOnTouchListener(this);
    }

    /**
     * 初始化退货原因view
     */
    private void initReasonView() {
        reasonPop = inflater.inflate(R.layout.pop_list, null);
        reasonList = (ListView) reasonPop.findViewById(R.id.pop_list);
        reasonList.setOnItemClickListener(this);
        reasons = (List<RefundReasonInfo>) SpSaveUtils.getObject(mContext, ConstantData.REFUNDREASONLIST);
        ReturnReasonAdapter adapter = new ReturnReasonAdapter(mContext, reasons);
        reasonList.setAdapter(adapter);
        if(reasons != null && reasons.size() != 0) {
            for (RefundReasonInfo info : reasons) {
                reasonIds.put(info.getName(), info.getId());
            }
        }
    }

    /**
     * 显示退货原因
     * @param v 显示位置相对于v
     */
    private void showReason(View v) {
        if(reasons!= null && reasons.size() > 0){
            if (null == PopupWindowReason) {
                PopupWindowReason = new PopupWindow(reasonPop, (int)getResources().getDimension(R.dimen.height_tnz), (int)getResources().getDimension(R.dimen.height_tzz), true);
                PopupWindowReason.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
                PopupWindowReason.setAnimationStyle(R.style.PopupAnimation);
            }
            PopupWindowReason.showAsDropDown(v);
        }else{
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_return_reason));
        }
    }
    @Override
    protected void recycleMemery() {
        if (iPrinterService != null) {
            unbindService(printerServiceConnection);
        }
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_submit_return_order})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_submit_return_order:
                saveOrder();
                break;
        }
    }

    /**
     * 提交退货单
     */
    private void saveOrder() {
        if(reasons!= null && reasons.size() > 0){
            if(edit_input_reason.getText().toString().equals(getString(R.string.warning_no))){
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.please_select_return_reason));
                return;
            }
            resetStyle();
            //commitStyles.clear();
            returnMoney();
        }else{
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_return_reason));
        }
    }

    /**
     * 退款
     *
     * @return 退款完成返回true
     */
    private void returnMoney() {
        if (bankStyle != null) {
            returnBank();
        } else {
            returnAlipay();
        }
    }
    /**
     * 退银行卡
     *
     */
    private void returnBank() {
        if (bankFlag < bankStyle.size()) {
            final BankPayInfo entity = bankStyle.get(bankFlag);
            if("true".equals(entity.getDes())){
                bankFlag += 1;
                if (bankFlag < bankStyle.size()) {
                    returnBank();
                } else {
                    returnAlipay();
                }
            }else{
                if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                    requestCashier(entity.getTradeno());
                }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
                    double money = ArithDouble.parseDouble(entity.getAmount());
                    Intent intentBank = new Intent(mContext, ThirdPayReturnDialog.class);
                    intentBank.putExtra(ConstantData.PAY_MONEY, money);
                    intentBank.putExtra(ConstantData.PAY_TYPE, getPayTypeById(entity.getSkfsid()));
                    intentBank.putExtra(ConstantData.PAY_ID, entity.getTradeno());
                    startActivityForResult(intentBank, ConstantData.THRID_CANCLE_REQUEST_CODE);
                }else if (MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                    String type = getPayTypeById(entity.getSkfsid());
                    final JSONObject json = new JSONObject();
                    final String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();

                    if(StringUtil.isEmpty(type)){
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_cannot_return));
                        return;
                    }else if(PaymentTypeEnum.STORE.getStyletype().equals(type)){
                        new BankreturnDialog(this, getString(R.string.store_return), entity.getCardno(), entity.getAmount(), new DialogFinishCallBack() {
                            @Override
                            public void finish(int position) {
                               if(position == 1){
                                   try {
                                       json.put("amt",CurrencyUnit.yuan2fenStr(entity.getAmount()));
                                       json.put("refNo",entity.getRefno());
                                       json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
                                       json.put("extOrderNo",tradeNo);
                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                                   AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.STORE, ConstantData.YHK_TH, json);
                               }else if(position == 2){
                                   try {
                                       json.put("orgTraceNo",entity.getBatchno());
                                       json.put("extOrderNo", tradeNo);
                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                                   AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.STORE, ConstantData.YHK_CX, json);
                               }
                            }
                        }).show();
                    }else if(PaymentTypeEnum.BANK.getStyletype().equals(type)){
                        new BankreturnDialog(this, getString(R.string.bank_return), entity.getCardno(), entity.getAmount(), new DialogFinishCallBack() {
                            @Override
                            public void finish(int position) {
                                if(position == 1){
                                    try {
                                        json.put("amt",CurrencyUnit.yuan2fenStr(entity.getAmount()));
                                        json.put("refNo",entity.getRefno());
                                        json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
                                        json.put("extOrderNo",tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.YHK_SK, ConstantData.YHK_TH, json);
                                }else if(position == 2){
                                    try {
                                        json.put("orgTraceNo",entity.getBatchno());
                                        json.put("extOrderNo", tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.YHK_SK, ConstantData.YHK_CX, json);
                                }
                            }
                        }).show();
                    }else if(ConstantData.WECHAT_ID.equals(entity.getSkfsid())){
                        new BankreturnDialog(this, getString(R.string.wechat_return), entity.getCardno(), entity.getAmount(), new DialogFinishCallBack() {
                            @Override
                            public void finish(int position) {
                                if(position == 1){
                                    try {
                                        json.put("amt",CurrencyUnit.yuan2fenStr(entity.getAmount()));
                                        json.put("refNo",entity.getRefno());
                                        json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
                                        json.put("extOrderNo",tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.POS_TONG, ConstantData.YHK_TH, json);
                                }else if(position == 2){
                                    try {
                                        json.put("oldTraceNo",entity.getBatchno());
                                        json.put("extOrderNo", tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.POS_TONG, ConstantData.POS_XFCX, json);
                                }
                            }
                        }).show();
                    }else if(ConstantData.ALPAY_ID.equals(entity.getSkfsid())){
                        new BankreturnDialog(this, getString(R.string.alipay_return), entity.getCardno(), entity.getAmount(), new DialogFinishCallBack() {
                            @Override
                            public void finish(int position) {
                                if(position == 1){
                                    try {
                                        json.put("amt",CurrencyUnit.yuan2fenStr(entity.getAmount()));
                                        json.put("refNo",entity.getRefno());
                                        json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
                                        json.put("extOrderNo",tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.POS_TONG, ConstantData.YHK_TH, json);
                                }else if(position == 2){
                                    try {
                                        json.put("oldTraceNo",entity.getBatchno());
                                        json.put("extOrderNo", tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.POS_TONG, ConstantData.POS_XFCX, json);
                                }
                            }
                        }).show();
                    }else if(ConstantData.YXLM_ID.equals(entity.getSkfsid())){
                        new BankreturnDialog(this, getString(R.string.qmh_return), entity.getCardno(), entity.getAmount(), new DialogFinishCallBack() {
                            @Override
                            public void finish(int position) {
                                if(position == 1){
                                    try {
                                        json.put("amt",CurrencyUnit.yuan2fenStr(entity.getAmount()));
                                        json.put("refNo",entity.getRefno());
                                        json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
                                        json.put("extOrderNo",tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.QMH, ConstantData.YHK_TH, json);
                                }else if(position == 2){
                                    try {
                                        json.put("orgTraceNo",entity.getBatchno());
                                        json.put("extOrderNo", tradeNo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AppHelper.callTrans(ReturnMoneyByOrderActivity.this, ConstantData.QMH, ConstantData.YHK_CX, json);
                                }
                            }
                        }).show();
                    }
                }
            }
        } else {
            returnAlipay();
        }
    }

    /**
     * 退微钱包
     */
    private void returnWeiXinPay() {
        if (thirdWeixins != null && weixinFlag < thirdWeixins.size()) {
            final ThirdPay thirdPay = thirdWeixins.get(weixinFlag);
            if("true".equals(thirdPay.getPay_buyer())){
                weixinFlag += 1;
                if (weixinFlag < thirdWeixins.size()) {
                    returnWeiXinPay();
                } else {
                    returnAllowanceBank();
                }
            }else {
                new AlipayAndWeixinPayReturnDialog(this, thirdPay.getPay_type(), thirdPay.getTrade_no(), AppConfigFile.getBillId(), thirdPay.getPay_total_fee(), new OnReturnFinishListener() {

                    @Override
                    public void finish(boolean isSuccess) {
                        thirdPay.setPay_buyer("true");
                        if(isSuccess) {
                            putPayments(thirdPay.getSkfsid(), idNames.get(thirdPay.getSkfsid()), PaymentTypeEnum.WECHAT.getStyletype(), "-"+ thirdPay.getPay_total_fee());
                        }else {
                            putPayments(getPayTypeId(PaymentTypeEnum.WECHATRECORDED),  idNames.get(getPayTypeId(PaymentTypeEnum.WECHATRECORDED)), PaymentTypeEnum.WECHATRECORDED.getStyletype(), "-"+ thirdPay.getPay_total_fee());
                        }
                        weixinFlag += 1;
                        if (weixinFlag < thirdWeixins.size()) {
                            returnWeiXinPay();
                        } else {
                            returnAllowanceBank();
                        }
                    }
                }).show();
            }
        } else {
            returnAllowanceBank();
        }
    }

    /**
     * 退支付宝
     *
     */
    private void returnAlipay() {
        if (thirdAlipays != null && alipayFlag < thirdAlipays.size()) {
            final ThirdPay thirdPay = thirdAlipays.get(alipayFlag);
            if ("true".equals(thirdPay.getPay_buyer())) {
                alipayFlag += 1;
                if (alipayFlag < thirdAlipays.size()) {
                    returnAlipay();
                } else {
                    returnWeiXinPay();
                }
            } else {
                new AlipayAndWeixinPayReturnDialog(this, thirdPay.getPay_type(), thirdPay.getTrade_no(), AppConfigFile.getBillId(), thirdPay.getPay_total_fee(), new OnReturnFinishListener() {

                    @Override
                    public void finish(boolean isSuccess) {
                        thirdPay.setPay_buyer("true");
                        if (isSuccess) {
                            putPayments(thirdPay.getSkfsid(), idNames.get(thirdPay.getSkfsid()), PaymentTypeEnum.ALIPAY.getStyletype(), "-" + thirdPay.getPay_total_fee());
                        } else {
                            putPayments(getPayTypeId(PaymentTypeEnum.ALIPAYRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.ALIPAYRECORDED)), PaymentTypeEnum.ALIPAYRECORDED.getStyletype(), "-" + thirdPay.getPay_total_fee());
                        }
                        alipayFlag += 1;
                        if (alipayFlag < thirdAlipays.size()) {
                            returnAlipay();
                        } else {
                            returnWeiXinPay();
                        }
                    }
                }).show();
            }
        } else{
            returnWeiXinPay();
        }
    }

    /**
     * 手工补录
     */
    private void returnAllowanceBank() {
        if (allowanceBankStyle != null && allowanceBankFlag < allowanceBankStyle.size()) {
            PayMentsInfo entity = allowanceBankStyle.get(allowanceBankFlag);
            if("true".equals(entity.getDes())){
                allowanceBankFlag += 1;
            }else{
                double money = ArithDouble.parseDouble(entity.getMoney());
                entity.setMoney("-"+money);
                entity.setDes("true");
                commitStyles.add(entity);
                allowanceBankFlag += 1;
            }
            if (allowanceBankFlag < allowanceBankStyle.size()) {
                returnAllowanceBank();
            } else {
                returnAllowanceAlipay();
            }
        } else {
            returnAllowanceAlipay();
        }
    }

    /**
     * 支付宝补录
     */
    private void returnAllowanceAlipay() {
        if (allowanceAlipayStyle != null && allowanceAlipayFlag < allowanceAlipayStyle.size()) {
            PayMentsInfo entity = allowanceAlipayStyle.get(allowanceAlipayFlag);
            if("true".equals(entity.getDes())){
                allowanceAlipayFlag += 1;
            }else{
                double money = ArithDouble.parseDouble(entity.getMoney());
                entity.setMoney("-"+money);
                entity.setDes("true");
                commitStyles.add(entity);
                allowanceAlipayFlag += 1;
            }
            if (allowanceAlipayFlag < allowanceAlipayStyle.size()) {
                returnAllowanceAlipay();
            } else {
                returnAllowanceWeixin();
            }
        } else {
            returnAllowanceWeixin();
        }
    }

    /**
     * 微信补录
     */
    private void returnAllowanceWeixin() {
        if (allowanceWeixinStyle != null && allowanceWeixinFlag < allowanceWeixinStyle.size()) {
            PayMentsInfo entity = allowanceWeixinStyle.get(allowanceWeixinFlag);
            if("true".equals(entity.getDes())){
                allowanceWeixinFlag += 1;
            }else{
                double money = ArithDouble.parseDouble(entity.getMoney());
                entity.setMoney("-"+money);
                entity.setDes("true");
                commitStyles.add(entity);
                allowanceWeixinFlag += 1;
            }
            if (allowanceWeixinFlag < allowanceWeixinStyle.size()) {
                returnAllowanceWeixin();
            } else {
                returnCash();
            }
        } else {
            returnCash();
        }
    }

    /**
     * 退现金
     */
    private void returnCash() {
        if (cashStyle != null && cashFlag < cashStyle.size()) {
            PayMentsInfo entity = cashStyle.get(cashFlag);
            if("true".equals(entity.getDes())){
                cashFlag += 1;
            }else{
                double money = ArithDouble.parseDouble(entity.getMoney());
                entity.setMoney("-"+money);
                entity.setDes("true");
                commitStyles.add(entity);
                cashFlag += 1;
            }
            if(cashFlag < cashStyle.size()) {
                returnCash();
            }else {
                addCouponDeduction();
                commitOrder();
            }
        }else {
            addCouponDeduction();
            commitOrder();
        }
    }

    private void addCouponDeduction(){
        if(coupon != null) {
            coupon.setMoney("-"+coupon.getMoney().replaceAll("-", ""));
            coupon.setOverage("-"+coupon.getOverage().replaceAll("-", ""));
            commitStyles.add(coupon);
        }
        if(deduction != null) {
            if(exchangeInfo == null) {
                exchangeInfo = new ExchangeInfo();
                exchangeInfo.setExchangemoney(billInfo.getUsedpointmoney());
                exchangeInfo.setExchangepoint(billInfo.getExchangedpoint());
            }
            deduction.setMoney("-" + deduction.getMoney().replaceAll("-", ""));
            commitStyles.add(deduction);
        }
    }

    /**
     * 提交退货单
     */
    private void commitOrder() {
        dealCoupon(billInfo.getUsedcouponlist());
        BillInfo bill = new BillInfo();
        bill.setBillid(AppConfigFile.getBillId());
        bill.setBackreason(reasonIds.get(edit_input_reason.getText().toString()));
        if(exchangeInfo != null) {
            bill.setExchange(exchangeInfo);
        }
        bill.setPaymentslist(commitStyles);
        bill.setUsedcouponlist(billInfo.getUsedcouponlist());
        billInfo.setRealmoney(realMoney+"");
        billInfo.setBackreason(reasonIds.get(edit_input_reason.getText().toString()));
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put("billInfo", GsonUtil.beanToJson(bill));
            LogUtil.i("lgs", GsonUtil.beanToJson(bill));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().saveReturnOrder(HTTP_TASK_KEY, map, SaveOrderResult.class, new HttpActionHandle<SaveOrderResult>() {

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
            public void handleActionSuccess(String actionName, SaveOrderResult result) {
                if(ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    billInfo.setBillid(AppConfigFile.getBillId());
                    billInfo.setPaymentslist(commitStyles);
//                    if(billInfo.getMember() == null){
//                        printBackByorder(billInfo);
//                    }
                    AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                    AppConfigFile.setBillId(result.getSaveOrderInfo().getBillid());
                    Intent intent = new Intent(mContext, ReturnGoodSucceedActivity.class);
                    intent.putExtra(ConstantData.BILL, billInfo);
                    intent.putExtra(ConstantData.SAVE_ORDER_RESULT_INFO, result.getSaveOrderInfo());
                    startActivity(intent);
                }else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

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
                        ReturnMoneyByOrderActivity.this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                    MyApplication.isPrint = true;
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

    /**
     * 处理用券信息(金额变为负)
     * @param list
     */
    private void dealCoupon(List<CouponInfo> list) {
        if(list != null) {
            try {
                if (!flag) {
                    flag = true;
                    for (CouponInfo info : list) {
                        info.setFacevalue("-" + info.getFacevalue());
                        info.setMoney("-" + info.getMoney());
                        info.setAvailablemoney("-"+info.getAvailablemoney());
                    }
                }
            } catch (Exception e) {
                LogUtil.v("lgs", "券金额转换异常!!!!!!");
            }
        }
    }

    /**
     * 获取退款方式
     */
    private void getReturnMoneyInfo() {
        resetStyle();
        for (PayMentsInfo infoy: payMentsInfos) {
            PayMentsInfo info = infoy.clone();
            info.setDes("false");
            String type = info.getType();
            if (type != null) {
//                if (PaymentTypeEnum.BANK.getStyletype().equals(type)) {
//                    if (bankStyle == null) {
//                        bankStyle = new ArrayList<>();
//                    }
//                    bankStyle.add(info);
//                } else if (PaymentTypeEnum.ALIPAY.getStyletype().equals(type)) {
//                    if (alipayStyle == null) {
//                        alipayStyle = new ArrayList<>();
//                    }
//                    alipayStyle.add(info);
//                } else if (PaymentTypeEnum.WECHAT.getStyletype().equals(type)) {
//                    if (weixinStyle == null) {
//                        weixinStyle = new ArrayList<>();
//                    }
//                    weixinStyle.add(info);
//                } else
                if (PaymentTypeEnum.CASH.getStyletype().equals(type)) {
                    if(cashStyle == null) {
                        cashStyle = new ArrayList<>();
                    }
                    if(!ConstantData.YXLM_ID.equals(info.getId())){
                        cashStyle.add(info);
                    }
                } else if (PaymentTypeEnum.HANDRECORDED.getStyletype().equals(type)) {
                    if (allowanceBankStyle == null) {
                        allowanceBankStyle = new ArrayList<>();
                    }
                    allowanceBankStyle.add(info);
                } else if (PaymentTypeEnum.ALIPAYRECORDED.getStyletype().equals(type)) {
                    if (allowanceAlipayStyle == null) {
                        allowanceAlipayStyle = new ArrayList<>();
                    }
                    allowanceAlipayStyle.add(info);
                } else if (PaymentTypeEnum.WECHATRECORDED.getStyletype().equals(type)) {
                    if (allowanceWeixinStyle == null) {
                        allowanceWeixinStyle = new ArrayList<>();
                    }
                    allowanceWeixinStyle.add(info.clone());
                } else if (PaymentTypeEnum.COUPON.getStyletype().equals(type)) {
                    couponMoney = ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage()));
                    coupon = info;
                }else if (PaymentTypeEnum.SCORE.getStyletype().equals(type)) {
                    scoreMoney = ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage()));
                   deduction = info;
                }
            }
        }
        thirdAlipays.clear();
        thirdWeixins.clear();
        if(billInfo.getThirdpartypaylist() != null && billInfo.getThirdpartypaylist().size() != 0) {
            List<ThirdPay> list = billInfo.getThirdpartypaylist();
            for (ThirdPay thirdPay : list) {
                if((ConstantData.PAYMODE_BY_ALIPAY+"").equals(thirdPay.getPay_type())) {
                    ThirdPay info = thirdPay.clone();
                    info.setPay_buyer("false");
                    thirdAlipays.add(info);
                }else if((ConstantData.PAYMODE_BY_WEIXIN+"").equals(thirdPay.getPay_type())) {
                    ThirdPay info = thirdPay.clone();
                    info.setPay_buyer("false");
                    thirdWeixins.add(info);
                }
            }
        }
        for(BankPayInfo infoy:billInfo.getBankpaylist()){
            BankPayInfo info = infoy.clone();
            info.setDes("false");
            if (bankStyle == null) {
                bankStyle = new ArrayList<>();
            }
            bankStyle.add(info);
        }
    }

    /**
     * 重置支付方式
     */
    private void resetStyle() {
        bankFlag = 0;
        alipayFlag = 0;
        weixinFlag = 0;
        cashFlag = 0;
        allowanceBankFlag = 0;
        allowanceAlipayFlag = 0;
        allowanceWeixinFlag = 0;
    }

    /**
     * 存放退款渠道信息
     * @param id 退款方式id
     * @param name 退款方式name
     * @param type 退款方式type
     * @param money 退款方式money
     */
    private void putPayments(String id, String name, String type, String money) {
        PayMentsInfo info = new PayMentsInfo();
        info.setId(id);
        info.setName(name);
        info.setType(type);
        info.setMoney(money);
        info.setDes("true");
        info.setOverage("0"); //用于补全字段
        commitStyles.add(info);
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
     * @Description: 获取支付方式TYPE
     *
     */
    private String getPayTypeById(String id) {

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

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
     * @Description: 获取支付方式名称
     *
     */
    private String getPayNameById(String id) {

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ConstantData.THRID_CANCLE_REQUEST_CODE){
            if(resultCode == ConstantData.THRID_CANCLE_RESULT_CODE){
                BankPayInfo entity = bankStyle.get(bankFlag);
                entity.setDes("true");
                putPayments(entity.getSkfsid(), getPayNameById(entity.getSkfsid()), getPayTypeById(entity.getSkfsid()), "-" + entity.getAmount());
                OrderBean resultBean = (OrderBean) data.getSerializableExtra(ConstantData.ORDER_BEAN);
                resultBean.setPaymentId(entity.getSkfsid());
                resultBean.setTransType(ConstantData.TRANS_RETURN);
                resultBean.setTraceId(AppConfigFile.getBillId());
                Intent serviceintent = new Intent(mContext, RunTimeService.class);
                serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                serviceintent.putExtra(ConstantData.THIRD_DATA, resultBean);
                startService(serviceintent);
                bankFlag += 1;
                if (bankFlag < bankStyle.size()) {
                    returnBank();
                } else {
                    returnAlipay();
                }
            }
        }
        if(Activity.RESULT_OK == resultCode){
            if(AppHelper.TRANS_REQUEST_CODE == requestCode){
                BankPayInfo entity = bankStyle.get(bankFlag);
                if (null != data) {
                    StringBuilder result = new StringBuilder();
                    Map<String,String> map = AppHelper.filterTransResult(data);
                    Type type =new TypeToken<Map<String, String>>(){}.getType();
                    try {
                        Map<String, String> transData = GsonUtil.jsonToObect(map.get(AppHelper.TRANS_DATA), type);
                        if("00".equals(transData.get("resCode"))){
                            OrderBean orderBean= new OrderBean();
                            entity.setDes("true");
                            putPayments(entity.getSkfsid(), getPayNameById(entity.getSkfsid()), getPayTypeById(entity.getSkfsid()), "-" + entity.getAmount());
                            orderBean.setAccountNo(CurrencyUnit.yuan2fenStr(entity.getAmount()));
                            orderBean.setTxnId(transData.get("extOrderNo"));
                            orderBean.setAccountNo(transData.get("cardNo"));
                            orderBean.setAcquId(transData.get("cardIssuerCode"));
                            orderBean.setBatchId(transData.get("traceNo"));
                            orderBean.setRefNo(transData.get("refNo"));
                            orderBean.setPaymentId(entity.getSkfsid());
                            orderBean.setTransType(ConstantData.TRANS_RETURN);
                            orderBean.setTraceId(AppConfigFile.getBillId());
                            Intent serviceintent = new Intent(mContext, RunTimeService.class);
                            serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                            serviceintent.putExtra(ConstantData.THIRD_DATA, orderBean);
                            startService(serviceintent);
                            bankFlag += 1;
                            if (bankFlag < bankStyle.size()) {
                                returnBank();
                            } else {
                                returnAlipay();
                            }
                        }else{
                            ToastUtils.sendtoastbyhandler(handler, transData.get("resDesc"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, "银行卡撤销异常！");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BizServiceInvoker mBizServiceInvoker;
    // 1.执行调用之前需要调用WeiposImpl.as().init()方法，保证sdk初始化成功。
    //
    // 2.调用收银支付成功后，收银支付结果页面完成后，BizServiceInvoker.OnResponseListener后收到响应的结果
    //
    // 3.如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
    private void innerRequestCashier(String tradeNo) {
        String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
        try {
            RequestInvoke cashierReq = new RequestInvoke();
            cashierReq.pkgName = this.getPackageName();
            cashierReq.sdCode = CashierSign.Cashier_sdCode;// 收银服务的sdcode信息
            cashierReq.bpId = AppConfigFile.InvokeCashier_BPID;
            cashierReq.launchType = CashierSign.launchType;

            cashierReq.params = CashierSign.refundsign(AppConfigFile.InvokeCashier_BPID, AppConfigFile.InvokeCashier_KEY, tradeNo);
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
    private BizServiceInvoker.OnResponseListener mOnResponseListener = new BizServiceInvoker.OnResponseListener() {

        @Override
        public void onResponse(String sdCode, String token, byte[] data) {
            // 收银服务调用完成后的返回方法
            String result = new String(data);
            WposBankRefundInfo info = null;
            try {
                info = GsonUtil.jsonToBean(result, WposBankRefundInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtil.i("lgs",
                    "sdCode = " + sdCode + " , token = " + token + " , data = " + new String(data));
            if(info != null){
                if(info.getErrCode().equals("0")){
                    BankPayInfo entity = bankStyle.get(bankFlag);
                    entity.setDes("true");
                    putPayments(entity.getSkfsid(), getPayNameById(entity.getSkfsid()), getPayTypeById(entity.getSkfsid()), "-" + entity.getAmount());
                    OrderBean orderBean= new OrderBean();
                    orderBean.setAccountNo(CurrencyUnit.yuan2fenStr(entity.getAmount()));
                    orderBean.setTxnId(info.getCashier_trade_no());
                    orderBean.setRefNo(info.getRefund_ref_no());
                    orderBean.setBatchId(info.getRefund_vouch_no());
                    orderBean.setPaymentId(entity.getSkfsid());
                    orderBean.setTransType(ConstantData.TRANS_RETURN);
                    orderBean.setTraceId(AppConfigFile.getBillId());
                    Intent serviceintent = new Intent(mContext, RunTimeService.class);
                    serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                    serviceintent.putExtra(ConstantData.THIRD_DATA, orderBean);
                    startService(serviceintent);
                    bankFlag += 1;
                    if (bankFlag < bankStyle.size()) {
                        returnBank();
                    } else {
                        returnAlipay();
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
    private void requestCashier(String tradeNo) {
        if (mBizServiceInvoker == null) {
            try {
                // 初始化服务调用
                mBizServiceInvoker = WeiposImpl.as().getService(BizServiceInvoker.class);
                if (mBizServiceInvoker == null) {
                    ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
                    return;
                }else{
                    // 设置请求订阅服务监听结果的回调方法
                    mBizServiceInvoker.setOnResponseListener(mOnResponseListener);
                }
            } catch (Exception e) {
                ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
            }
        }else{
            innerRequestCashier(tradeNo);
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

}
