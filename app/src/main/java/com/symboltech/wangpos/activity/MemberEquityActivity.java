package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.CouponsAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.CouponWaringDialog;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.msg.entity.CheckCouponInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.ExchangeInfo;
import com.symboltech.wangpos.msg.entity.ExchangemsgInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.SubmitGoods;
import com.symboltech.wangpos.result.CheckCouponResult;
import com.symboltech.wangpos.result.CouponResult;
import com.symboltech.wangpos.result.ExchangemsgResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.zxing.app.CaptureActivity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemberEquityActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

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

    private double orderTotleValue;
    private MemberInfo member;
    private double maxScoreValue;

    private CouponsAdapter sendAdapter;
    private double orderScore;
    private double orderCoupon;
    private SubmitGoods submitgoods;
    private List<CouponInfo> couponList;

    private ExchangeInfo exchangeInfo = new ExchangeInfo();

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
        couponList = new ArrayList<>();
        exchangeInfo.setExchangemoney("0");
        exchangeInfo.setExchangepoint("0");
        title_text_content.setText(getString(R.string.number_access));
        orderTotleValue = ArithDouble.parseDouble(getIntent().getStringExtra(ConstantData.CART_ALL_MONEY));
        submitgoods = (SubmitGoods) getIntent().getSerializableExtra(ConstantData.MEMBER_EQUITY);
        member = (MemberInfo) getIntent().getSerializableExtra(ConstantData.GET_MEMBER_INFO);
        //显示积分兑换规则
        if(member != null){
            //score_rule.setText(member.getPointrule());
            text_hold_score.setText(member.getCent_total());
        }
        //显示可用的最大积分
        if (submitgoods != null){
            if (TextUtils.isEmpty(submitgoods.getLimitpoint())) {
                maxScoreValue = 0;
                text_max_score.setText(maxScoreValue +"");
            } else {
                maxScoreValue = ArithDouble.parseDouble(submitgoods.getLimitpoint());
                text_max_score.setText(maxScoreValue +"");
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
                                    scoreforhttp(member.getId(), String.valueOf(score));
                                }else{
                                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                                }

                            }
                        }
                    }
                });
            }
            // 显示会员拥有卡券
            if(submitgoods.getCouponInfos() != null && submitgoods.getCouponInfos().size() > 0){
                recycleview_hold_coupon.setVisibility(View.VISIBLE);
                sendAdapter = new CouponsAdapter(submitgoods.getCouponInfos(), 0, mContext);
                sendAdapter.setOnItemClickListener(new CouponsAdapter.MyItemClickListener() {
                    @Override
                            public void onItemClick(View view, int position) {
                                ImageView coupon_select = (ImageView) view.findViewById(R.id.imageview_coupon_selected);
                                if(coupon_select.getVisibility() == View.GONE){
                                    if(getPayTypeId(PaymentTypeEnum.COUPON)!=null){
                                        if ((ArithDouble.sub(orderTotleValue, orderScore)) <ArithDouble.add(orderCoupon, ArithDouble.parseDouble(sendAdapter.getItem(position).getAvailablemoney()))) {
                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_coupon_over_msg));
                                            return;
                                        }
                                        orderCoupon = ArithDouble.add(orderCoupon, ArithDouble.parseDouble(sendAdapter.getItem(position).getAvailablemoney()));
                                        couponList.add(sendAdapter.getItem(position));
                                    }else{
                                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_paytype_err_msg));
                                        return;
                                    }

                                    sendAdapter.addSelect(position);
                                    sendAdapter.notifyItemChanged(position);
                                }else if(coupon_select.getVisibility() == View.VISIBLE){
                                    orderCoupon = ArithDouble.sub(orderCoupon, ArithDouble.parseDouble(sendAdapter.getItem(position).getAvailablemoney()));
                                    couponList.remove(sendAdapter.getItem(position));
                                    sendAdapter.delSelect(position);
                                    sendAdapter.notifyItemChanged(position);
                                }
                            }
                        });
                recycleview_hold_coupon.setAdapter(sendAdapter);
            }
        }else{
            recycleview_hold_coupon.setVisibility(View.INVISIBLE);
        }
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
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.imageview_qr})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                if(couponList.size()>0){
                    checkPaperCoupon(AppConfigFile.getBillId(), couponList);
                }else
                {
                    goPayment(couponList, null);
                }
                break;
            case R.id.imageview_qr:
                Intent intent_qr = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent_qr, ConstantData.SCAN_CASH_COUPON);
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
                return paymentslist.get(i).getId();
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
        Map<String, String> map = new HashMap<String, String>();
        map.put("billId", billId);
        map.put("couponcode", couponcode);
        HttpRequestUtil.getinstance().getPaperCoupon(HTTP_TASK_KEY, map, CouponResult.class,
                new HttpActionHandle<CouponResult>() {

                    @Override
                    public void handleActionStart() {
                        startwaitdialog();
                    }

                    @Override
                    public void handleActionFinish() {
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

                                        if ((ArithDouble.sub(orderTotleValue, orderScore)) < orderCoupon) {
                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_coupon_over_msg));
                                            return;
                                        }
                                        orderCoupon = ArithDouble.add(orderCoupon, ArithDouble.parseDouble(result.getCouponinfo().getAvailablemoney()));
                                        sendAdapter.add(result.getCouponinfo());
                                        sendAdapter.addSelect(sendAdapter.getItemCount() - 1);
                                        couponList.add(result.getCouponinfo());
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
                                    edit_used_score.setText(exchangeInfo.getExchangepoint());
                                    text_deduction_money.setText(exchangeInfo.getExchangemoney());
                                    return;
                                }
                                if (orderScore > ArithDouble.sub(orderTotleValue, orderCoupon)) {
                                    edit_used_score.setText(exchangeInfo.getExchangepoint());
                                    text_deduction_money.setText(exchangeInfo.getExchangemoney());
                                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_score_over_msg));
                                    return;
                                }
                                edit_used_score.setText(exchangeInfo.getExchangepoint());
                                text_deduction_money.setText(exchangeInfo.getExchangemoney());
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
                            CouponWaringDialog dialog = new CouponWaringDialog(MemberEquityActivity.this, result.getCheckcouponinfo().getRulemoney(), result.getCheckcouponinfo().getCouponfacevalue(), new DialogFinishCallBack() {

                                @Override
                                public void finish(int position) {
                                    goPayment(coupons, result.getCheckcouponinfo());
                                }
                            });
                            dialog.show();
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
        setResult(ConstantData.MEMBER_EQUITY_RESULT_CODE, intent_payment);
        finish();
    }
}
