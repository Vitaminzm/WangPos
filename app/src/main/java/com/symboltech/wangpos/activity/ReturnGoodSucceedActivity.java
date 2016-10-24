package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.GridViewForScrollView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReturnGoodSucceedActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
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
        title_text_content.setText(getString(R.string.return_succeed));
        bill = (BillInfo) getIntent().getSerializableExtra(ConstantData.BILL);
        resultInfo = (SaveOrderResultInfo) getIntent().getSerializableExtra(ConstantData.SAVE_ORDER_RESULT_INFO);
        text_return_order_id.setText(bill.getBillid());
        text_return_order_time.setText(bill.getSaletime());
        text_return_real_money.setText(MoneyAccuracyUtils.formatMoneyByTwo(bill.getRealmoney()));
        text_return_money.setText(MoneyAccuracyUtils.formatMoneyByTwo(bill.getTotalmoney()));

        if(bill.getMember() != null){
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
                text_return_used_score.setText(bill.getAwardpoint());
            }
            text_score_good_return.setText(bill.getUsedpoint());

            ll_score_info.setVisibility(View.VISIBLE);
        }else{
            ll_score_info.setVisibility(View.GONE);
        }

        paymentTypeInfoDetailAdapter = new PaymentTypeInfoDetailAdapter(this, bill.getPaymentslist(), true);
        activity_payment_gridview.setAdapter(paymentTypeInfoDetailAdapter);
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

    @OnClick({R.id.text_confirm})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.text_confirm:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();
                break;
        }
    }
}
