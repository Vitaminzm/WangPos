package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.ReturnReasonAdapter;
import com.symboltech.wangpos.adapter.ReturnTableAdapter;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.ExchangeInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.RefundReasonInfo;
import com.symboltech.wangpos.result.SaveOrderResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.ListViewForScrollView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReturnMoneyByOrderActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnTouchListener {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.edit_input_money)
    EditText edit_input_reason;
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
    @Bind(R.id.text_score_good)
    TextView text_score_good;
    @Bind(R.id.text_return_score)
    TextView text_return_score;
    @Bind(R.id.text_score_deduction)
    TextView text_score_deduction;

    private View reasonPop;
    private ListView reasonList;
    private PopupWindow PopupWindowReason;
    private List<RefundReasonInfo> reasons;
    private LayoutInflater inflater;
    private BillInfo billInfo;

    private boolean flag = false;//是否已经处理过 默认是false
    /**commitStyles - 提交的退款方式*/
    private List<PayMentsInfo> commitStyles = new ArrayList<>();
    /** 退货原因-原因id */
    private Map<String, String> reasonIds = new HashMap<>();

    /**
     * bankStyle-银行卡支付
     * alipayStyle-支付宝支付
     * weixinStyle-微信支付
     * cashStyle-现金支付
     */
    private List<PayMentsInfo> bankStyle, alipayStyle, weixinStyle, cashStyle;
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
        title_text_content.setText(getString(R.string.return_order_info));
        billInfo = (BillInfo) getIntent().getSerializableExtra(ConstantData.BILL);
        payMentsInfos = billInfo.getPaymentslist();
        text_return_total_money.setText(billInfo.getTotalmoney());
        if(billInfo.getMember() != null){
            ll_return_score_info.setVisibility(View.VISIBLE);
            double score_deduction = 0,score_good = 0,return_score = 0;
            if(billInfo.getExchange()!= null){
                score_deduction = ArithDouble.parseDouble(billInfo.getExchange().getExchangepoint());
                text_score_deduction.setText(billInfo.getExchange().getExchangepoint());
            }
            return_score = ArithDouble.parseDouble(billInfo.getAwardpoint());
            if(return_score > 0){
                text_return_score.setText("-"+billInfo.getAwardpoint());
            }else{
                text_return_score.setText(billInfo.getAwardpoint());
            }
            score_good = ArithDouble.parseDouble(billInfo.getUsedpoint());
            text_score_good.setText(billInfo.getUsedpoint());
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
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_return_money_by_order);
        mContext = ReturnMoneyByOrderActivity.this;
        inflater = LayoutInflater.from(mContext);
        MyApplication.addActivity(this);
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
                PopupWindowReason = new PopupWindow(reasonPop, 250, 200, true);
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
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_submit_return_order})
    public void click(View view){
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
            if(edit_input_reason.getText().equals(getString(R.string.warning_no))){
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.please_select_return_reason));
                return;
            }
            commitStyles.clear();
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
            PayMentsInfo entity = bankStyle.get(bankFlag);
            double money = ArithDouble.parseDouble(entity.getMoney());
            entity.setMoney("-"+money);
            commitStyles.add(entity);
            bankFlag += 1;
            if (bankFlag < bankStyle.size()) {
                returnBank();
            } else {
                returnAlipay();
            }
        } else {
            returnAlipay();
        }
    }

    /**
     * 退微钱包
     */
    private void returnWeiXinPay() {
        if (weixinStyle != null && weixinFlag < weixinStyle.size()) {
            allowanceWeixinFlag += 1;
            if (weixinFlag < weixinStyle.size()) {
                returnWeiXinPay();
            } else {
                returnAllowanceBank();
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
        if (alipayStyle != null && alipayFlag < alipayStyle.size()) {
            alipayFlag += 1;
            if(alipayFlag < alipayStyle.size()){
                returnAlipay();
            }else{
                returnWeiXinPay();
            }
        } else {
            returnWeiXinPay();
        }
    }

    /**
     * 手工补录
     */
    private void returnAllowanceBank() {
        if (allowanceBankStyle != null && allowanceBankFlag < allowanceBankStyle.size()) {
            PayMentsInfo entity = allowanceBankStyle.get(allowanceBankFlag);
            double money = ArithDouble.parseDouble(entity.getMoney());
            entity.setMoney("-"+money);
            commitStyles.add(entity);
            allowanceBankFlag += 1;
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
            double money = ArithDouble.parseDouble(entity.getMoney());
            entity.setMoney("-"+money);
            commitStyles.add(entity);
            allowanceAlipayFlag += 1;
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
            double money = ArithDouble.parseDouble(entity.getMoney());
            entity.setMoney("-"+money);
            commitStyles.add(entity);
            allowanceWeixinFlag += 1;
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
            double money = ArithDouble.parseDouble(entity.getMoney());
            entity.setMoney("-"+money);
            commitStyles.add(entity);
            cashFlag += 1;
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
        bill.setBillid(MyApplication.getBillId());
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
        HttpRequestUtil.getinstance().saveReturnOrder(map, SaveOrderResult.class, new HttpActionHandle<SaveOrderResult>() {

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
                    billInfo.setBillid(MyApplication.getBillId());
                    billInfo.setPaymentslist(commitStyles);
                    MyApplication.setLast_billid(MyApplication.getBillId());
                    MyApplication.setBillId(result.getSaveOrderInfo().getBillid());
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
        for (PayMentsInfo info: payMentsInfos) {
            String type = info.getType();
            if (type != null) {
                if (PaymentTypeEnum.BANK.getStyletype().equals(type)) {
                    if (bankStyle == null) {
                        bankStyle = new ArrayList<>();
                    }
                    bankStyle.add(info);
                } else if (PaymentTypeEnum.ALIPAY.getStyletype().equals(type)) {
                    if (alipayStyle == null) {
                        alipayStyle = new ArrayList<>();
                    }
                    alipayStyle.add(info);
                } else if (PaymentTypeEnum.WECHAT.getStyletype().equals(type)) {
                    if (weixinStyle == null) {
                        weixinStyle = new ArrayList<>();
                    }
                    weixinStyle.add(info);
                } else if (PaymentTypeEnum.CASH.getStyletype().equals(type)) {
                    if(cashStyle == null) {
                        cashStyle = new ArrayList<>();
                    }
                    cashStyle.add(info);
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
                    allowanceWeixinStyle.add(info);
                } else if (PaymentTypeEnum.COUPON.getStyletype().equals(type)) {
                    couponMoney = ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage()));
                    coupon = info.clone();
                }else if (PaymentTypeEnum.SCORE.getStyletype().equals(type)) {
                    scoreMoney = ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage()));
                   deduction = info.clone();
                }
            }
        }
    }

    /**
     * 重置支付方式
     */
    private void resetStyle() {
        resetStyleByStyle(bankStyle);
        bankFlag = 0;
        resetStyleByStyle(alipayStyle);
        alipayFlag = 0;
        resetStyleByStyle(weixinStyle);
        weixinFlag = 0;
        resetStyleByStyle(cashStyle);
        cashFlag = 0;
        resetStyleByStyle(allowanceBankStyle);
        allowanceBankFlag = 0;
        resetStyleByStyle(allowanceAlipayStyle);
        allowanceAlipayFlag = 0;
        resetStyleByStyle(allowanceWeixinStyle);
        allowanceWeixinFlag = 0;
    }

    /**
     * 重置退款方式
     *
     * @param style
     *            付款方式
     */
    private void resetStyleByStyle(List style) {
        if (style != null) {
            style.clear();
        }
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
        info.setOverage("0"); //用于补全字段
        commitStyles.add(info);
    }
}
