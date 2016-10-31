package com.symboltech.wangpos.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.PaymentTypeAdapter;
import com.symboltech.wangpos.adapter.PaymentTypeInfoAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.CanclePayDialog;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.ThirdPayDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
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
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.result.SaveOrderResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.CodeBitmap;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.zxing.app.CaptureActivity;
;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;

public class CheckOutActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.text_order_total_money)
    TextView text_order_total_money;

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
    GridView activity_payment_gridview;
    @Bind(R.id.listview_pay_info)
    ListView listview_pay_info;
    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;
    //订单总额
    private double orderTotleValue;
    //待支付金额
    private double waitPayValue;
    private HorizontalKeyBoard keyboard;
    //支付方式适配器
    private PaymentTypeAdapter paymentTypeAdapter;
    private ArrayList<PayMentsInfo> paymentTypes;

    //支付信息适配
    private List<PayMentsCancleInfo> payMentsCancle = new ArrayList<PayMentsCancleInfo>();
    private PaymentTypeInfoAdapter paymentTypeInfoadapter;
    private double payLing = 0;
    private double paymentMoney;

    public static final int THIRD_PAY_INFO = 1;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(CheckOutActivity.this, msg);
                    break;
                case THIRD_PAY_INFO:
                    OrderBean orderBean = (OrderBean)msg.obj;
                    saveBanInfo(paymentTypeAdapter.getPayType().getId(), orderBean);
                    PayMentsCancleInfo infobank = new PayMentsCancleInfo();
                    infobank.setId(paymentTypeAdapter.getPayType().getId());
                    infobank.setName(paymentTypeAdapter.getPayType().getName());
                    infobank.setType(paymentTypeAdapter.getPayType().getType());
                    infobank.setIsCancle(false);
                    infobank.setTxnid(orderBean.getTxnId());
                    infobank.setMoney(MoneyAccuracyUtils.makeRealAmount(orderBean.getTransAmount()));
                    infobank.setOverage("0");
                    addPayTypeInfo(PaymentTypeEnum.BANK, 0, 0, null, infobank);
                    waitPayValue = ArithDouble.sub(ArithDouble.sub(orderTotleValue, ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                    text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                    edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
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
    @Override
    protected void initData() {
        exchangeInfo = new ExchangeInfo();
        exchangeInfo.setExchangemoney("0");
        exchangeInfo.setExchangepoint("0");
        title_text_content.setText(getString(R.string.checkout));
        cartgoods = (List<GoodsInfo>) getIntent().getSerializableExtra(ConstantData.CART_HAVE_GOODS);
        salesman = getIntent().getStringExtra(ConstantData.SALESMAN_NAME);
        isMember = getIntent().getIntExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_NOT_VERITY);
        if (isMember == ConstantData.MEMBER_IS_VERITY){
            member = (MemberInfo) getIntent().getSerializableExtra(ConstantData.GET_MEMBER_INFO);
            member_type =  getIntent().getStringExtra(ConstantData.MEMBER_VERIFY);
            member_equity =  (SubmitGoods) getIntent().getSerializableExtra(ConstantData.MEMBER_EQUITY);
        }else{
            rl_member_equity.setVisibility(View.GONE);
        }
        orderTotleValue = getIntent().getDoubleExtra(ConstantData.GET_ORDER_VALUE_INFO, 0.0);
        text_order_total_money.setText(MoneyAccuracyUtils.getmoneybytwo(orderTotleValue));

        waitPayValue = orderTotleValue;
        edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(orderTotleValue));
        text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));

        paymentTypes = new ArrayList<>();
        paymentTypeAdapter = new PaymentTypeAdapter(getApplicationContext(), paymentTypes);
        activity_payment_gridview.setAdapter(paymentTypeAdapter);
        activity_payment_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(Utils.isFastClick()){
                    return;
                }
                if (null == edit_input_money.getText() || "".equals(edit_input_money.getText())) {
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
                paymentTypeAdapter.setPayTpye(position);
                doPay(money);
            }
        });
        getPayType();
        paymentTypeInfoadapter = new PaymentTypeInfoAdapter(getApplicationContext(), payMentsCancle);
        listview_pay_info.setAdapter(paymentTypeInfoadapter);
    }

    private void doPay(double money) {
        String type = paymentTypeAdapter.getPayType().getType();
        //待支付的金额为0时，不允许继续支付
        if (waitPayValue <= 0) {
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_success));
            edit_input_money.setText("");
            paymentTypeAdapter.setPayTpyeNull();
            return;
        }
        paymentMoney = money;
        switch (PaymentTypeEnum.getpaymentstyle(type.trim())){
            case CASH:
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
                waitPayValue = ArithDouble.sub(ArithDouble.sub(orderTotleValue, ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                break;
            case WECHAT:
                if("1".equals(SpSaveUtils.read(getApplicationContext(), ConstantData.MALL_WEIXIN_IS_INPUT, "0"))){
                    Intent intent_qr = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
                }else{
                    Intent intent = new Intent(this, ThirdPayDialog.class);
                    intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
                    intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
                    startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
                }
                break;
            case ALIPAY:
                if("1".equals(SpSaveUtils.read(getApplicationContext(), ConstantData.MALL_ALIPAY_IS_INPUT, "0"))){
                    Intent intent_qr = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
                }else{
                    Intent intent = new Intent(this, ThirdPayDialog.class);
                    intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
                    intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
                    startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
                }
                break;
            case BANK:
                    Intent intent = new Intent(this, ThirdPayDialog.class);
                    intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
                    intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
                    startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
                break;
        }
    }

    private void addPayTypeInfo(PaymentTypeEnum enumValue, double money, int Overage, PayMentsInfo payType, PayMentsCancleInfo info) {
        if (info != null) {
            paymentTypeInfoadapter.add(info);
            paymentTypeAdapter.setPayTpyeNull();
            waitPayValue = ArithDouble.sub(ArithDouble.sub(orderTotleValue, ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
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
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.text_cancle_pay, R.id.title_icon_back, R.id.imageview_more, R.id.text_submit_order})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.text_cancle_pay:
                if(ArithDouble.sub(orderTotleValue, waitPayValue) <= 0){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_return_no));
                    return;
                }
                Intent intentCancle = new Intent(this, CanclePayDialog.class);
                intentCancle.putExtra(ConstantData.CANCLE_LIST, (Serializable)payMentsCancle);
                startActivityForResult(intentCancle, ConstantData.THRID_CANCLE_REQUEST_CODE);
                break;
            case R.id.text_submit_order:
                payments.clear();
                if(waitPayValue > 0.0){
                    if(paymentTypeAdapter.getPayType() != null && !edit_input_money.getText().equals("") && paymentTypeAdapter.getPayType().getType().equals(PaymentTypeEnum.CASH.getStyletype())){

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
                break;
            case R.id.imageview_more:
                if(member_equity != null && member_equity.getCouponInfos() != null && member_equity.getLimitpoint()!= null){
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
                break;
            case R.id.title_icon_back:
                if(ArithDouble.sub(orderTotleValue, waitPayValue) > 0){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_return_failed));
                    return;
                }
                this.finish();
                break;
        }
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
        waitPayValue = ArithDouble.sub(ArithDouble.sub(orderTotleValue, ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
        text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
        edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == ConstantData.MEMBER_EQUITY_RESULT_CODE){
            if(requestCode == ConstantData.MEMBER_EQUITY_REQUEST_CODE){
                orderScoreOverrage = data.getDoubleExtra(ConstantData.GET_ORDER_SCORE_OVERAGE, 0.0);
                // 使用的积分抵扣金额
                orderScore = ArithDouble.sub(data.getDoubleExtra(ConstantData.GET_ORDER_SCORE_INFO, 0.0), orderScoreOverrage);
                if (orderScore > 0) {
                    text_score_deduction_money.setText(orderScore + "");
                }
                exchangeInfo = (ExchangeInfo) data.getSerializableExtra(ConstantData.USE_INTERRAL);
                // 使用的卡券
                // 获取使用卡券信息
                coupons = (List<CouponInfo>) data.getSerializableExtra(ConstantData.CAN_USED_COUPON);
                orderCouponOverrage = data.getDoubleExtra(ConstantData.GET_ORDER_COUPON_OVERAGE, 0.0);
                orderCoupon = ArithDouble.sub(data.getDoubleExtra(ConstantData.GET_ORDER_COUPON_INFO, 0.0), orderCouponOverrage);
                if (orderCoupon > 0.0) {
                    text_coupon_deduction_money.setText(orderCoupon + "");
                }
                // 设置实付金额和待付金额
                waitPayValue = ArithDouble.sub(ArithDouble.sub(orderTotleValue, ArithDouble.add(orderScore, orderCoupon)), paymentTypeInfoadapter.getPayMoney());
                text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));
                edit_input_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));

            }
        }else if(resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY){
            if(requestCode == ConstantData.QRCODE_REQURST_QR_PAY){
                Intent intent = new Intent(this, ThirdPayDialog.class);
                intent.putExtra(ConstantData.PAY_MONEY, paymentMoney);
                intent.putExtra(ConstantData.BSC, data.getExtras().getString("QRcode"));
                intent.putExtra(ConstantData.PAY_TYPE, paymentTypeAdapter.getPayType().getType());
                startActivityForResult(intent, ConstantData.THRID_PAY_REQUEST_CODE);
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
        }
        else {
            paymentTypeAdapter.setPayTpyeNull();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put("billInfo", GsonUtil.beanToJson(bill));
        } catch (Exception e) {
            LogUtil.v("lgs", "订单转换失败");
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().saveReturnOrder(map, SaveOrderResult.class, new HttpActionHandle<SaveOrderResult>() {
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

                    bill.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
                    bill.setCashier(SpSaveUtils.read(mContext, ConstantData.CASHIER_CODE, ""));
                    bill.setCashiername(SpSaveUtils.read(mContext, ConstantData.CASHIER_NAME, ""));
                    bill.setSalemanname(salesman);
                    bill.setSaleman(getSalemanCode(salesman));
                    bill.setRealmoney("" + ArithDouble.sub(orderTotleValue, ArithDouble.add(orderCoupon, orderScore)));

                    bill.setAwardpoint(result.getSaveOrderInfo().getGainpoint());
                    bill.setTotalmoney(String.valueOf(orderTotleValue));
                    bill.setGoodslist(cartgoods);
                    bill.setGrantcouponlist(result.getSaveOrderInfo().getGrantcouponlist());
                    bill.setAllcouponlist(result.getSaveOrderInfo().getAllcouponlist());
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
                    bill.setCashiername(SpSaveUtils.read(mContext, ConstantData.CASHIER_NAME, ""));
                    bill.setSalemanname(salesman);
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
        List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        if(paymentslist != null){
            for (int i = 0; i < paymentslist.size(); i++) {
                if (paymentslist.get(i).getType().equals(PaymentTypeEnum.ALIPAY.getStyletype())
                        || paymentslist.get(i).getType().equals(PaymentTypeEnum.WECHAT.getStyletype())
                        || paymentslist.get(i).getType().equals(PaymentTypeEnum.BANK.getStyletype())) {
                    if(!AppConfigFile.isOffLineMode()){
                        paymentTypeAdapter.add(paymentslist.get(i));
                    }
                }else if(paymentslist.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype())){
                    paymentTypeAdapter.add(paymentslist.get(i));
                    paymentTypeAdapter.setPayTpye(paymentslist.get(i));
                    paymentTypeAdapter.notifyDataSetChanged();
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
    private String getSalemanCode(String typeEnum) {

        List<CashierInfo> sales = (List<CashierInfo>) SpSaveUtils.getObject(mContext, ConstantData.SALEMANLIST);
        if(sales == null)
            return null;
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).getCashiername().equals(typeEnum)) {
                return sales.get(i).getCashiercode();
            }
        }
        return null;
    }
    private void saveBanInfo(final String payTypeId, final OrderBean orderBean){
        Map<String, String> map = new HashMap<String, String>();
        map.put("skfsid", payTypeId);
        map.put("posno", SpSaveUtils.read(getApplicationContext(), ConstantData.CASHIER_DESK_CODE, ""));
        map.put("billid", AppConfigFile.getBillId());
        map.put("transtype", ConstantData.TRANS_SALE);
        map.put("tradeno", orderBean.getTxnId());
        map.put("amount", MoneyAccuracyUtils.makeRealAmount(orderBean.getTransAmount()));
        map.put("decmoney", "0");
        map.put("cardno", orderBean.getAccountNo()== null? "null":orderBean.getAccountNo());
        map.put("bankcode",orderBean.getAcquId()== null? "null":orderBean.getAcquId());
        map.put("batchno", orderBean.getBatchId()== null? "null":orderBean.getBatchId());
        map.put("refno", orderBean.getRefNo()== null? "null":orderBean.getRefNo());
        HttpRequestUtil.getinstance().saveBankInfo(map, BaseResult.class, new HttpActionHandle<BaseResult>(){
            @Override
            public void handleActionError(String actionName, String errmsg) {
                ToastUtils.sendtoastbyhandler(handler, errmsg);
                OrderInfoDao dao = new OrderInfoDao(getApplicationContext());
                dao.addBankinfo(SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""), AppConfigFile.getBillId(), ConstantData.TRANS_SALE, orderBean.getAccountNo()== null? "null":orderBean.getAccountNo(),
                        orderBean.getAcquId()== null? "null":orderBean.getAcquId(), orderBean.getBatchId()== null? "null":orderBean.getBatchId(),
                        orderBean.getRefNo()== null? "null":orderBean.getRefNo(), orderBean.getTxnId(), payTypeId,
                        ArithDouble.parseDouble(MoneyAccuracyUtils.makeRealAmount(orderBean.getTransAmount())), 0.0);
                ToastUtils.sendtoastbyhandler(handler, "三方交易信息保存失败！");
            }

            @Override
            public void handleActionSuccess(String actionName, BaseResult result) {
                if (!ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())){
                    OrderInfoDao dao = new OrderInfoDao(getApplicationContext());
                    dao.addBankinfo(SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""), AppConfigFile.getBillId(), ConstantData.TRANS_SALE, orderBean.getAccountNo()== null? "null":orderBean.getAccountNo(),
                            orderBean.getAcquId()== null? "null":orderBean.getAcquId(), orderBean.getBatchId()== null? "null":orderBean.getBatchId(),
                            orderBean.getRefNo()== null? "null":orderBean.getRefNo(), orderBean.getTxnId(), payTypeId,
                            ArithDouble.parseDouble(MoneyAccuracyUtils.makeRealAmount(orderBean.getTransAmount())), 0.0);
                    ToastUtils.sendtoastbyhandler(handler, "三方交易信息保存失败！");
                }
            }
        });
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
        if (newBean.getOrderState().isEmpty()) {
            newBean.setOrderState(isSuccess ? "0" : "");
        }
        return newBean;
    }
}
