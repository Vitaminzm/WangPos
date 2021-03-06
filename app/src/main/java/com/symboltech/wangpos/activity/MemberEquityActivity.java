package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.CouponsAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.CouponUsedDialog;
import com.symboltech.wangpos.dialog.CouponWaringDialog;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.CouponCallback;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.CheckCouponInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.ExchangeInfo;
import com.symboltech.wangpos.msg.entity.ExchangemsgInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.SubmitGoods;
import com.symboltech.wangpos.result.CheckCouponResult;
import com.symboltech.wangpos.result.CouponResult;
import com.symboltech.wangpos.result.CrmCouponResult;
import com.symboltech.wangpos.result.ExchangemsgResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.zxing.app.CaptureActivity;
import com.ums.upos.sdk.cardslot.CardInfoEntity;
import com.ums.upos.sdk.cardslot.CardSlotManager;
import com.ums.upos.sdk.cardslot.CardSlotTypeEnum;
import com.ums.upos.sdk.cardslot.CardTypeEnum;
import com.ums.upos.sdk.cardslot.OnCardInfoListener;
import com.ums.upos.sdk.cardslot.SwipeSlotOptions;
import com.ums.upos.sdk.exception.CallServiceException;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.scanner.OnScanListener;
import com.ums.upos.sdk.scanner.ScannerConfig;
import com.ums.upos.sdk.scanner.ScannerManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemberEquityActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
    @Bind(R.id.tv_crm_memberno)
    TextView tv_crm_memberno;

    @Bind(R.id.text_total_money)
    TextView text_total_money;
    @Bind(R.id.text_coupon_count)
    TextView text_coupon_count;
    @Bind(R.id.tv_waitpay)
    TextView tv_waitpay;

    @Bind(R.id.text_hold_score)
    TextView text_hold_score;
    @Bind(R.id.text_max_score)
    TextView text_max_score;

    @Bind(R.id.text_deduction_money)
    TextView text_deduction_money;
    @Bind(R.id.edit_used_score)
    EditText edit_used_score;

    @Bind(R.id.recycleview_hold_coupon)
    RecyclerView recycleview_hold_coupon;
    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;
    @Bind(R.id.ll_score_info)
    LinearLayout ll_score_info;

    private double orderTotleValue;
    private MemberInfo member;
    private double maxScoreValue;

    private CouponsAdapter sendAdapter;
    private List<CouponInfo> adapterData;
    private double orderScore = 0;
    private double orderCoupon = 0;
    private SubmitGoods submitgoods;
    private List<CouponInfo> couponList;


    private ExchangeInfo exchangeInfo = new ExchangeInfo();
    private ScannerManager scannerManager;
    private boolean isLoading = false;

    private CardSlotManager cardSlotManager = null;
    public static final int Vipcard = 2;
    public static final int SEARCHCARDERROR = -1;
    class MyHandler extends Handler {
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
                case SEARCHCARDERROR:
                    if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                        ToastUtils.sendtoastbyhandler(handler,"刷卡失败，请重试");
                        if(cardSlotManager == null){
                            return;
                        }
                        try {
                            cardSlotManager.stopRead();
                        } catch (SdkException e) {
                            e.printStackTrace();
                        } catch (CallServiceException e) {
                            e.printStackTrace();
                        }
                        searchCardInfo(isVerifyCoupon);
                    }
                    break;
                case Vipcard:
                    if(isVerifyCoupon){
                        memberverifymethodbyhttp((String) msg.obj);
                    }else{
                        tv_crm_memberno.setText((String) msg.obj);
                    }
                    if(cardSlotManager == null){
                        return;
                    }
                    try {
                        cardSlotManager.stopRead();
                    } catch (SdkException e) {
                        e.printStackTrace();
                    } catch (CallServiceException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private boolean isVerify = false;
    private void memberverifymethodbyhttp(final String obj) {
        if(isVerify){
            ToastUtils.sendtoastbyhandler(handler, "验证中请稍后");
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("memberno", obj);
        map.put("billid", AppConfigFile.getBillId());
        HttpRequestUtil.getinstance().getCrmhyyhq(HTTP_TASK_KEY, map, CrmCouponResult.class, new HttpActionHandle<CrmCouponResult>() {

            @Override
            public void handleActionStart() {
                isVerify = true;
                startwaitdialog();
            }

            @Override
            public void handleActionFinish() {
                isVerify = false;
                closewaitdialog();
            }
            @Override
            public void handleActionError(String actionName, String errmsg) {
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName, final CrmCouponResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    if(result.getData()!= null &&result.getData().size() >0){

                        MemberEquityActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(adapterData.containsAll(result.getData())){
                                    ToastUtils.sendtoastbyhandler(handler, "同一张卡多次刷无效");
                                    return;
                                }else{
                                    ToastUtils.sendtoastbyhandler(handler, "刷卡成功");
                                }
                                adapterData.addAll(result.getData());
                                sendAdapter.notifyDataSetChanged();
                            }
                        });
                    }else {
                        ToastUtils.sendtoastbyhandler(handler, "无可用优惠券");
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
        couponList = new ArrayList<CouponInfo>();
        adapterData = new ArrayList<CouponInfo>();
        ExchangeInfo temp = (ExchangeInfo) getIntent().getSerializableExtra(ConstantData.USE_INTERRAL);
        if(temp!= null){
            exchangeInfo.setExchangemoney(temp.getExchangemoney());
            exchangeInfo.setExchangepoint(temp.getExchangepoint());
        }else{
            exchangeInfo.setExchangemoney("0");
            exchangeInfo.setExchangepoint("0");
        }
        title_text_content.setText(getString(R.string.number_access));
        orderTotleValue = ArithDouble.parseDouble(getIntent().getStringExtra(ConstantData.CART_ALL_MONEY));
        tv_waitpay.setText(orderTotleValue+"");
        member = (MemberInfo) getIntent().getSerializableExtra(ConstantData.GET_MEMBER_INFO);
        //显示积分兑换规则
        if(member != null){
            tv_crm_memberno.setText(member.getMemberno());
            submitgoods = (SubmitGoods) getIntent().getSerializableExtra(ConstantData.MEMBER_EQUITY);
            if(submitgoods== null){
                ll_score_info.setVisibility(View.GONE);
            }
            //score_rule.setText(member.getPointrule());
            text_hold_score.setText(member.getCent_total());
        }else{
            ll_score_info.setVisibility(View.GONE);
        }
        new HorizontalKeyBoard(this, this, edit_used_score, ll_keyboard, new KeyBoardListener() {
            @Override
            public void onComfirm() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onValue(String value) {
                double score = 0;
                try {
                    score = ArithDouble.parseDouble(value);
                    BigDecimal b = new BigDecimal(score);
                    score = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                } catch (Exception e) {
                    e.printStackTrace();
                    edit_used_score.setText("");
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_format_msg));
                    return;
                }
                if (score > maxScoreValue) {
                    edit_used_score.setText("");
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg));
                } else {
                    if(member != null){
                        if(getPayTypeId(PaymentTypeEnum.SCORE)!=null){
                            edit_used_score.setText(String.valueOf(score));
                            scoreforhttp(member.getMemberno(), String.valueOf(score));
                        }else{
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                        }

                    }
                }
            }
        });
        //显示可用的最大积分
      //  if (submitgoods != null)
        {
            if(submitgoods == null){
                maxScoreValue = 0;
            }else{
                maxScoreValue = ArithDouble.parseDouble(submitgoods.getLimitpoint());
            }
            text_max_score.setText(maxScoreValue +"");
            edit_used_score.setText(exchangeInfo.getExchangepoint());
            orderScore = ArithDouble.parseDouble(exchangeInfo.getExchangemoney());
            text_deduction_money.setText(exchangeInfo.getExchangemoney());
            // 显示会员拥有卡券
            //if(submitgoods.getCouponInfos() != null && submitgoods.getCouponInfos().size() > 0)
            {
                List<CouponInfo> couponInfos = (List<CouponInfo>) getIntent().getSerializableExtra(ConstantData.ALL_COUPON);
                recycleview_hold_coupon.setVisibility(View.VISIBLE);
                if(couponInfos != null && couponInfos.size() > 0){
                    adapterData.addAll(couponInfos);
                }
                sendAdapter = new CouponsAdapter(adapterData, 0, mContext);
                sendAdapter.setOnItemClickListener(new CouponsAdapter.MyItemClickListener() {
                    @Override
                            public void onItemClick(View view, final int position) {
                                    new CouponUsedDialog(MemberEquityActivity.this, couponList,sendAdapter.getItem(position).clone(), new CouponCallback() {
                                        @Override
                                        public void doResult(CouponInfo couponInfo) {
                                            doCoupon(couponInfo, position);
                                        }
                                    }).show();
//                                ImageView coupon_select = (ImageView) view.findViewById(R.id.imageview_coupon_selected);
//                                if(coupon_select.getVisibility() == View.GONE){
//                                    if(getPayTypeId(PaymentTypeEnum.COUPON)!=null){
//                                        if ((ArithDouble.sub(orderTotleValue, orderScore)) <ArithDouble.add(orderCoupon, ArithDouble.parseDouble(sendAdapter.getItem(position).getAvailablemoney()))) {
//                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_coupon_over_msg));
//                                            return;
//                                        }
//                                        orderCoupon = ArithDouble.add(orderCoupon, ArithDouble.parseDouble(sendAdapter.getItem(position).getAvailablemoney()));
//                                        couponList.add(sendAdapter.getItem(position));
//                                    }else{
//                                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
//                                        return;
//                                    }
//
//                                    sendAdapter.addSelect(position);
//                                    sendAdapter.notifyItemChanged(position);
//                                }else if(coupon_select.getVisibility() == View.VISIBLE){
//                                    orderCoupon = ArithDouble.sub(orderCoupon, ArithDouble.parseDouble(sendAdapter.getItem(position).getAvailablemoney()));
//                                    couponList.remove(sendAdapter.getItem(position));
//                                    sendAdapter.delSelect(position);
//                                    sendAdapter.notifyItemChanged(position);
//                                }
                            }
                        });
                recycleview_hold_coupon.setAdapter(sendAdapter);
                double orderCardOverrage = getIntent().getDoubleExtra(ConstantData.GET_ORDER_COUPON_OVERAGE, 0.0);
                double orderCard = ArithDouble.sub(getIntent().getDoubleExtra(ConstantData.GET_ORDER_COUPON_INFO, 0.0), orderCardOverrage);
                List<CouponInfo> coupons = (List<CouponInfo>) getIntent().getSerializableExtra(ConstantData.CAN_USED_COUPON);
                if(coupons != null && coupons.size() > 0){
                    int scoll = sendAdapter.addAllByshow(coupons);
                    recycleview_hold_coupon.scrollToPosition(scoll);
                    couponList.addAll(coupons);
                    orderCoupon = ArithDouble.add(orderCoupon, ArithDouble.add(orderCard, orderCardOverrage));
                    text_coupon_count.setText(couponList.size() + "");
                    text_total_money.setText(orderCoupon+"");
                }
            }
        }
// else{
//            recycleview_hold_coupon.setVisibility(View.INVISIBLE);
//        }

        if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
            try {
                BaseSystemManager.getInstance().deviceServiceLogin(
                        this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                    cardSlotManager = new CardSlotManager();
                                }else{
                                    ToastUtils.sendtoastbyhandler(handler, "扫码登录失败");

                                }
                            }
                        });
            } catch (SdkException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isVerifyCoupon = true;
    private void searchCardInfo(boolean flag) {
        if(cardSlotManager == null){
            return;
        }
        ToastUtils.sendtoastbyhandler(handler,"请刷卡！");
        isVerifyCoupon = flag;
        Set<CardSlotTypeEnum> slotTypes = new HashSet<CardSlotTypeEnum>();
        slotTypes.add(CardSlotTypeEnum.SWIPE);
        Set<CardTypeEnum> cardTypes = new HashSet<CardTypeEnum>();
        cardTypes.add(CardTypeEnum.MAG_CARD);
        int timeout = 0;
        try {
            cardSlotManager.stopRead();
            Map<CardSlotTypeEnum, Bundle> options = new HashMap<CardSlotTypeEnum, Bundle>();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SwipeSlotOptions.LRC_CHECK, false);
            options.put(CardSlotTypeEnum.SWIPE, bundle);
            cardSlotManager.setConfig(options);
            cardSlotManager.readCard(slotTypes, cardTypes, timeout,
                    new OnCardInfoListener() {

                        @Override
                        public void onCardInfo(int arg0, CardInfoEntity arg1) {
                            if (0 != arg0) {
                                handler.sendEmptyMessage(SEARCHCARDERROR);
                            } else {
                                switch (arg1.getActuralEnterType()) {
                                    case MAG_CARD:
                                        LogUtil.i("lgs", "磁道1：" + arg1.getTk1()
                                                + "\n" + "磁道2：" + arg1.getTk2()
                                                + "\n" + "磁道3：" + arg1.getTk3());
                                        Message msg = Message.obtain();
                                        msg.obj = arg1.getTk2();
                                        msg.what = Vipcard;
                                        handler.sendMessage(msg);
                                        break;
                                    default:
                                        break;
                                }
//                                try {
//                                    cardSlotManager.stopRead();
//                                } catch (SdkException e) {
//                                    e.printStackTrace();
//                                } catch (CallServiceException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }
                    }, null);
        } catch (SdkException e) {
            e.printStackTrace();
        } catch (CallServiceException e) {
            e.printStackTrace();
        }

    }

    private void doCoupon(CouponInfo couponInfo, int position){
        if(getPayTypeId(PaymentTypeEnum.COUPON) == null){
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
            return;
        }
        if ((ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderScore), orderCoupon)) < ArithDouble.parseDouble(couponInfo.getAvailablemoney())) {
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_coupon_over_msg));
            return;
        }
        if(couponList.size() == 0){
            if(ArithDouble.parseDouble(couponInfo.getAvailablemoney()) > 0){
                couponList.add(couponInfo);
                orderCoupon = ArithDouble.parseDouble(couponInfo.getAvailablemoney());
                sendAdapter.addSelect(position);
                sendAdapter.notifyItemChanged(position);
                text_coupon_count.setText(couponList.size() + "");
                text_total_money.setText(orderCoupon+"");
                tv_waitpay.setText(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderScore), orderCoupon)+"");
            }
            return;
        }
        for(CouponInfo info:couponList){
            if(info.getCouponno().equals(couponInfo.getCouponno())){
                couponList.remove(info);
                break;
            }
        }
        if(ArithDouble.parseDouble(couponInfo.getAvailablemoney()) == 0){
            sendAdapter.delSelect(position);
        }else{
            sendAdapter.addSelect(position);
            couponList.add(couponInfo);
        }
        orderCoupon = 0;
        for(CouponInfo info:couponList){
            orderCoupon = ArithDouble.add(orderCoupon, ArithDouble.parseDouble(info.getAvailablemoney()));
        }
        sendAdapter.notifyItemChanged(position);
        text_coupon_count.setText(couponList.size() + "");
        text_total_money.setText(orderCoupon + "");
        tv_waitpay.setText(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderScore), orderCoupon)+"");
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_member_equity);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        MemberDetailActivity.MyLayoutManager linearLayoutManagerHold = new MemberDetailActivity.MyLayoutManager(this);
        linearLayoutManagerHold.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleview_hold_coupon.setLayoutManager(linearLayoutManagerHold);
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
            if(cardSlotManager != null){
                try {
                    cardSlotManager.stopRead();
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

    @OnClick({R.id.title_icon_back, R.id.imageview_qr, R.id.text_confirm, R.id.tv_swipcard_coupon, R.id.tv_swipcard_memberno})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                finish();
                break;
            case R.id.tv_swipcard_coupon:
                searchCardInfo(true);
                break;
            case R.id.tv_swipcard_memberno:
                searchCardInfo(false);
                break;

            case R.id.text_confirm:
//                if(couponList.size()>0){
//                    checkPaperCoupon(AppConfigFile.getBillId(), couponList);
//                }else
                {
                    goPayment(couponList, null);
                }
                break;
            case R.id.imageview_qr:
                if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
                    int type = SpSaveUtils.readInt(getApplicationContext(), ConstantData.CAMERATYPE, 1);
                    if(type == 0){
                        scannerManager = new ScannerManager();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ScannerConfig.COMM_SCANNER_TYPE, ConstantData.scanner_type);
                        bundle.putBoolean(ScannerConfig.COMM_ISCONTINUOUS_SCAN, true);
                        try {
                            scannerManager.stopScan();
                            scannerManager.initScanner(bundle);
                            scannerManager.startScan(30000, new OnScanListener() {
                                @Override
                                public void onScanResult(int i, byte[] bytes) {
                                    //防止用户未扫描直接返回，导致bytes为空
                                    if (bytes != null && !bytes.equals("")) {
                                        if(getPayTypeId(PaymentTypeEnum.COUPON)!=null){
                                            couponforhttp(AppConfigFile.getBillId(), new String(bytes));
                                        }else{
                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                                        }
                                    }
                                }
                            });
                        } catch (SdkException e) {
                            e.printStackTrace();
                        } catch (CallServiceException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Intent intent_qr = new Intent(this, CaptureActivity.class);
                        startActivityForResult(intent_qr, ConstantData.SCAN_CASH_COUPON);
                    }
                }else{
                    Intent intent_qr = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent_qr, ConstantData.SCAN_CASH_COUPON);
                }
                break;
        }
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
     * @Description: 获取支付方式Id
     *
     */
    private String getPayTypeId(PaymentTypeEnum typeEnum) {

        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist == null){
            return null;
        }
        for (int i = 0; i < paymentslist.size(); i++) {
            if (paymentslist.get(i).getType().equals(typeEnum.getStyletype())) {
                if(PaymentTypeEnum.SCORE == typeEnum){
                    if(ConstantData.BERRERZK_ID.equals(paymentslist.get(i).getId())){
                        return paymentslist.get(i).getId();
                    }
                }else{
                    return paymentslist.get(i).getId();
                }
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY) {
            switch (requestCode) {
                case ConstantData.SCAN_CASH_COUPON:
                    if (!StringUtil.isEmpty(data.getExtras().getString("QRcode"))) {
                        if(getPayTypeId(PaymentTypeEnum.COUPON)!=null){
                            couponforhttp(AppConfigFile.getBillId(), data.getExtras().getString("QRcode"));
                        }else{
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                        }
                    }
                    break;

                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月11日
     * @Description: 验证纸券是否可用
     *
     * @param billId
     * @param couponcode
     */
    private void couponforhttp(String billId, String couponcode) {
        if(isLoading){
            ToastUtils.sendtoastbyhandler(handler, "验证中请稍后");
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("billId", billId);
        map.put("couponcode", couponcode);
        HttpRequestUtil.getinstance().getPaperCoupon(HTTP_TASK_KEY, map, CouponResult.class,
                new HttpActionHandle<CouponResult>() {

                    @Override
                    public void handleActionStart() {
                        isLoading = true;
                        startwaitdialog();
                    }

                    @Override
                    public void handleActionFinish() {
                        isLoading = false;
                        closewaitdialog();
                    }

                    @Override
                    public void handleActionError(String actionName,
                                                  String errmsg) {
                        ToastUtils.sendtoastbyhandler(handler, errmsg);
                    }

                    @Override
                    public void handleActionSuccess(String actionName,
                                                    final CouponResult result) {
                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            // 获取数据，刷新
                            if (result != null && result.getCouponinfo() != null) {
                                MemberEquityActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        recycleview_hold_coupon.setVisibility(View.VISIBLE);
                                        if (sendAdapter.isExit(result.getCouponinfo())) {
                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_scan_coupon_msg));
                                            return;
                                        }

                                        if ((ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderScore), orderCoupon)) < ArithDouble.parseDouble(result.getCouponinfo().getAvailablemoney())) {
                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_coupon_over_msg));
                                            return;
                                        }
                                        orderCoupon = ArithDouble.add(orderCoupon, ArithDouble.parseDouble(result.getCouponinfo().getAvailablemoney()));
                                        sendAdapter.add(result.getCouponinfo());
                                        sendAdapter.addSelect(sendAdapter.getItemCount() - 1);
                                        couponList.add(result.getCouponinfo());
                                        recycleview_hold_coupon.scrollToPosition(sendAdapter.getItemCount() - 1);
                                        tv_waitpay.setText(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderScore), orderCoupon) + "");
                                    }
                                });
                            }

                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                    @Override
                    public void startChangeMode() {
                        final HttpActionHandle httpActionHandle = this;
                        MemberEquityActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangeModeDialog(MemberEquityActivity.this, httpActionHandle).show();
                            }
                        });
                    }

                    @Override
                    public void handleActionChangeToOffLine() {
                        Intent intent = new Intent(MemberEquityActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月11日
     * @Description: 计算可用积分
     *
     * @param id
     * @param point
     */
    private void scoreforhttp(String id, String point) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id);
        map.put("membertype", member.getMembertype());
        map.put("point", point);
        HttpRequestUtil.getinstance().calcutePointExchange(HTTP_TASK_KEY, map, ExchangemsgResult.class, new HttpActionHandle<ExchangemsgResult>() {

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
            public void handleActionSuccess(String actionName, final ExchangemsgResult result) {
                MemberEquityActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            ExchangemsgInfo info = result.getInfo().getExchmsg();
                            if (info != null) {
                                exchangeInfo.setExchangemoney(info.getExchangemoney());
                                exchangeInfo.setExchangepoint(info.getUsepoint());
                            }


                            orderScore = ArithDouble.parseDouble(exchangeInfo.getExchangemoney());
                            //抵扣值大于0才显示
                            if (orderScore >= 0) {
                                if (orderScore == 0) {
                                    exchangeInfo.setExchangemoney("0");
                                    exchangeInfo.setExchangepoint("0");
                                    edit_used_score.setText(exchangeInfo.getExchangepoint());
                                    text_deduction_money.setText(exchangeInfo.getExchangemoney());
                                    return;
                                }
                                if (orderScore > ArithDouble.sub(orderTotleValue, orderCoupon)) {
                                    orderScore = 0;
                                    exchangeInfo.setExchangemoney("0");
                                    exchangeInfo.setExchangepoint("0");
                                    edit_used_score.setText(exchangeInfo.getExchangepoint());
                                    text_deduction_money.setText(exchangeInfo.getExchangemoney());
                                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_score_over_msg));
                                    return;
                                }
                                edit_used_score.setText(exchangeInfo.getExchangepoint());
                                text_deduction_money.setText(exchangeInfo.getExchangemoney());
                                tv_waitpay.setText(ArithDouble.sub(ArithDouble.sub(orderTotleValue, orderScore), orderCoupon) + "");
                            } else {
                                ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_cannot_use_msg));
                            }
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                            orderScore = 0;
                            exchangeInfo.setExchangemoney("0");
                            exchangeInfo.setExchangepoint("0");
                            edit_used_score.setText(exchangeInfo.getExchangepoint());
                            text_deduction_money.setText(exchangeInfo.getExchangemoney());
                        }
                    }
                });
            }
            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                MemberEquityActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(MemberEquityActivity.this, httpActionHandle).show();
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(MemberEquityActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * @author zmm emial:mingming.zhang@symboltech.com 2015年11月11日
     * @Description: 提交订单前的券验证
     *
     * @param id
     * @param coupons
     */
    private void checkPaperCoupon(String id, final List<CouponInfo> coupons){
        Map<String, String> map = new HashMap<String, String>();
        map.put("billId", id);
        String json = new Gson().toJson(coupons);
        map.put("coupon", json);
        HttpRequestUtil.getinstance().checkPaperCoupon(HTTP_TASK_KEY, map, CheckCouponResult.class, new HttpActionHandle<CheckCouponResult>() {

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
            public void handleActionSuccess(String actionName, final CheckCouponResult result) {
                if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                    if (result.getCheckcouponinfo() != null) {
                        double overflow = ArithDouble.parseDouble(result.getCheckcouponinfo().getOveragemoney());
                        if (overflow > 0) {
                           MemberEquityActivity.this.runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   CouponWaringDialog dialog = new CouponWaringDialog(MemberEquityActivity.this, result.getCheckcouponinfo().getRulemoney(), result.getCheckcouponinfo().getCouponfacevalue(), new DialogFinishCallBack() {

                                       @Override
                                       public void finish(int position) {
                                           goPayment(coupons, result.getCheckcouponinfo());
                                       }
                                   });
                                   dialog.show();
                               }
                           });
                        } else {
                            goPayment(coupons, result.getCheckcouponinfo());
                        }
                    }
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                MemberEquityActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(MemberEquityActivity.this, httpActionHandle).show();
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(MemberEquityActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void goPayment(List<CouponInfo> coupons, CheckCouponInfo checkcouponinfo){
        // 跳转到支付界面
        Intent intent_payment = new Intent();
        //纸券溢余
        double overflow = 0;
        if(checkcouponinfo != null){
            overflow = ArithDouble.parseDouble(checkcouponinfo.getOveragemoney());
            intent_payment.putExtra(ConstantData.GET_ORDER_COUPON_OVERAGE, overflow);
            // 纸券总额
            intent_payment.putExtra(ConstantData.GET_ORDER_COUPON_INFO, ArithDouble.parseDouble(checkcouponinfo.getCouponfacevalue()));
        }else{
            intent_payment.putExtra(ConstantData.GET_ORDER_COUPON_OVERAGE, overflow);
            // 纸券总额
            intent_payment.putExtra(ConstantData.GET_ORDER_COUPON_INFO, orderCoupon);
        }
        intent_payment.putExtra(ConstantData.GET_ORDER_SCORE_OVERAGE, 0.0);
        // 积分抵扣金额
        intent_payment.putExtra(ConstantData.GET_ORDER_SCORE_INFO, orderScore);
        // 积分抵扣信息
        intent_payment.putExtra(ConstantData.USE_INTERRAL, exchangeInfo);
        intent_payment.putExtra(ConstantData.CAN_USED_COUPON, (Serializable) coupons);
        intent_payment.putExtra(ConstantData.ALL_COUPON, (Serializable) adapterData);
        intent_payment.putExtra(ConstantData.CRM_COUPON_NO, tv_crm_memberno.getText().toString());
        setResult(ConstantData.MEMBER_EQUITY_RESULT_CODE, intent_payment);
        finish();
    }
}
