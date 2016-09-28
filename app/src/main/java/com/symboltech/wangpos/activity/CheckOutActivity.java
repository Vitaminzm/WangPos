package com.symboltech.wangpos.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.PaymentTypeAdapter;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.SubmitGoods;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    //订单总额
    private double orderTotleValue;
    //
    private double nowWaitPay;
    //待支付金额
    private double waitPayValue;
    private HorizontalKeyBoard keyboard;
    //支付方式适配器
    private PaymentTypeAdapter paymentTypeAdapter;
    private ArrayList<PayMentsInfo> paymentType;

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
    // 账单信息
    private BillInfo bill;
    // 商品信息
    private List<GoodsInfo> cartgoods;
    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
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
        text_wait_money.setText(MoneyAccuracyUtils.getmoneybytwo(waitPayValue));

        nowWaitPay = waitPayValue;
        paymentType = new ArrayList<>();
        paymentTypeAdapter = new PaymentTypeAdapter(getApplicationContext(),paymentType);
        activity_payment_gridview.setAdapter(paymentTypeAdapter);
        activity_payment_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        getPayType();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_check_out);
        MyApplication.addActivity(this);
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
        keyboard = new HorizontalKeyBoard(this, this, edit_input_money, new KeyBoardListener() {
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
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.text_cancle_pay, R.id.title_icon_back, R.id.imageview_more, R.id.text_submit_order})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.text_cancle_pay:
                break;
            case R.id.text_submit_order:
                break;
            case R.id.imageview_more:
                break;
            case R.id.title_icon_back:
                this.finish();
                break;
        }
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
                    if(!MyApplication.isOffLineMode()){
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
}
