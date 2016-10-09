package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.dialog.AddGoodDialog;
import com.symboltech.wangpos.dialog.AddSalemanDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.result.CommitOrderResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReturnGoodsByNormalActivity extends BaseActivity implements View.OnTouchListener {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.text_cashier_name)
    TextView text_cashier_name;
    @Bind(R.id.text_bill_id)
    TextView text_bill_id;
    @Bind(R.id.ll_saleman)
    LinearLayout ll_saleman;
    @Bind(R.id.text_desk_code)
    TextView text_desk_code;

    @Bind(R.id.edit_return_handperson)
    EditText edit_return_handperson;
    @Bind(R.id.edit_return_money)
    EditText edit_return_money;
    @Bind(R.id.edit_return_good)
    EditText edit_return_good;
    private List<GoodsInfo> goodinfos;
    private List<CashierInfo> sales;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if(id == R.id.edit_return_good){
            if(goodinfos != null && goodinfos.size() > 0){
                new AddGoodDialog(ReturnGoodsByNormalActivity.this, goodinfos, new DialogFinishCallBack() {
                    @Override
                    public void finish(int p) {
                        edit_return_good.setTag(p);
                        edit_return_good.setText(goodinfos.get(p).getGoodsname());
                    }
                }).show();
            }else {
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_good));
            }
        }else if(id == R.id.edit_return_handperson){
            if(sales != null && sales.size() > 0){
                new AddSalemanDialog(ReturnGoodsByNormalActivity.this, sales, new DialogFinishCallBack() {
                    @Override
                    public void finish(int position) {
                        edit_return_handperson.setText(sales.get(position).getCashiername());
                        edit_return_handperson.setTag(position);
                    }
                }).show();
            }else{
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_saleman));
            }

        }
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
        sales = (List<CashierInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.SALEMANLIST);
        goodinfos = (List<GoodsInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.BRANDGOODSLIST);
        ll_saleman.setVisibility(View.INVISIBLE);
        title_text_content.setText(getString(R.string.return_normal));
        text_desk_code.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_DESK_CODE, ""));
        text_bill_id.setText(MyApplication.getBillId());
        text_cashier_name.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_NAME, ""));

        if(goodinfos != null && goodinfos.size() > 0){
            edit_return_good.setTag(0);
            edit_return_good.setText(goodinfos.get(0).getGoodsname());
        }
        if(sales != null && sales.size() > 0){
            edit_return_handperson.setText(sales.get(0).getCashiername());
            edit_return_handperson.setTag(0);
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_return_goods_by_normal);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        edit_return_handperson.setOnTouchListener(this);
        edit_return_good.setOnTouchListener(this);
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit_return_handperson.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edit_return_good.getWindowToken(), 0);

    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm_return_order})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm_return_order:
                commitGoods();
                break;
        }
    }

    /**
     * 提交商品
     */
    private void commitGoods() {
        if(checkParams(edit_return_good) && checkParams(edit_return_handperson) && checkParams(edit_return_money)) {
            float money = 0;
            try {
                money = new BigDecimal(edit_return_money.getText().toString()).setScale(2, RoundingMode.HALF_UP).floatValue();
            } catch (Exception e) {
            }
            if(money > 0) {
                final BillInfo billInfo = new BillInfo();
                billInfo.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
                billInfo.setBillid(MyApplication.getBillId());
                billInfo.setSaletype(ConstantData.SALETYPE_SALE_RETURN_NORMAL);
                billInfo.setOldposno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
                billInfo.setOldbillid(MyApplication.getBillId());
                billInfo.setCashier(SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""));
                billInfo.setSalemanname(edit_return_handperson.getText().toString());
                billInfo.setTotalmoney("-" + money);
                billInfo.setSaletime(Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"));
                //补全goods字段
                GoodsInfo currentGoods = goodinfos.get((Integer) edit_return_good.getTag());
                if(currentGoods != null) {
                    currentGoods.setInx("1");
                    currentGoods.setUsedpoint("0");
                    currentGoods.setSalecount("1");
                    currentGoods.setDiscmoney("0");
                    currentGoods.setPreferentialmoney("0");
                    currentGoods.setSaleamt("-"+money);
                    billInfo.bindGoods(currentGoods);
                }
                Map<String, String> map = new HashMap<String, String>();
                try {
                    LogUtil.v("lgs", GsonUtil.beanToJson(billInfo));
                    map.put("billInfo", GsonUtil.beanToJson(billInfo));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HttpRequestUtil.getinstance().commitReturnOrder(map, CommitOrderResult.class, new HttpActionHandle<CommitOrderResult>() {

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
                    public void handleActionSuccess(String actionName, CommitOrderResult result) {
                        if(result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            Intent intent = new Intent(mContext, ReturnMoneyByNormalActivity.class);
                            intent.putExtra(ConstantData.BILL, billInfo);
                            startActivity(intent);
                        }else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                });

            }else {
                Toast.makeText(mContext, R.string.waring_return_moeny_right_err, Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(mContext, R.string.waring_putfull_roder, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查控件是否有内容
     * @param view textview
     * @return  如果没有内容 返回false
     */
    private boolean checkParams(EditText view) {
        if(TextUtils.isEmpty(view.getText().toString()) || "".equals(view.getText().toString())) {
            return false;
        }
        return true;
    }
}
