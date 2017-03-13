package com.symboltech.wangpos.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.AddGoodDialog;
import com.symboltech.wangpos.dialog.AddSalemanDialog;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.InputDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.GoodsAndSalerInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.result.CommitOrderResult;
import com.symboltech.wangpos.result.InitializeInfResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;
    @Bind(R.id.text_desk_code)
    TextView text_desk_code;

    @Bind(R.id.edit_return_handperson)
    TextView edit_return_handperson;
    @Bind(R.id.edit_return_money)
    EditText edit_return_money;
    @Bind(R.id.edit_return_good)
    TextView edit_return_good;
    private List<GoodsInfo> goodinfos;
    private List<CashierInfo> sales;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
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
                if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(mContext, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
                    new InputDialog(this, "请输入销售员编码", "请输入销售员编码", new GeneralEditListener(){

                        @Override
                        public void editinput(final String edit) {
                            if(AppConfigFile.isOffLineMode()){
                                ReturnGoodsByNormalActivity.this.runOnUiThread(new Runnable(){

                                    @Override
                                    public void run() {
                                        getGoodsByrydmOffline(edit);
                                    }});
                            }else{
                                getGoodsFromRydm(edit);
                            }
                        }
                    }).show();

                }else{
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
            }
        }
        return false;
    }

    public void getGoodsByrydmOffline(String rydm){
        boolean isFind = false;
        List<GoodsAndSalerInfo> datas =(List<GoodsAndSalerInfo>)SpSaveUtils.getObject(getApplicationContext(), SpSaveUtils.SAVE_FOR_SP_KEY_OFFLINE, ConstantData.OFFLINE_CASH);
        if(datas!= null && datas.size() > 0){
            for(GoodsAndSalerInfo info:datas){
                if(info.getSalemanlist()!= null && info.getSalemanlist().size() > 0){
                    for(CashierInfo cashier:info.getSalemanlist()){
                        if(cashier!= null && cashier.getCashiercode().equals(rydm)){
                            sales.clear();
                            sales.add(cashier);
                            edit_return_handperson.setText(cashier.getCashiername());
                            edit_return_handperson.setTag(0);
                            List<GoodsInfo> goods = info.getBrandgoodlist();
                            if(goods != null && goods.size()>0){
                                goodinfos.clear();
                                goodinfos.addAll(goods);
                                new AddGoodDialog(ReturnGoodsByNormalActivity.this, goodinfos, new DialogFinishCallBack() {
                                    @Override
                                    public void finish(int p) {
                                        edit_return_good.setTag(p);
                                        edit_return_good.setText(goodinfos.get(p).getGoodsname());
                                    }
                                }).show();
                            }
                            isFind = true;
                            break;
                        }
                    }
                }
                if(isFind){
                    break;
                }
            }
        }
        if(!isFind){
            sales.clear();
            edit_return_handperson.setText("");
            edit_return_handperson.setTag("");
            goodinfos.clear();
            edit_return_good.setTag("");
            edit_return_good.setText("");
            ToastUtils.sendtoastbyhandler(handler, "该销售人员不存在");
        }
    }

    private void getGoodsFromRydm(String rydm){
        Map<String, String> map = new HashMap<String, String>();
        map.put("rydm", rydm);
        HttpRequestUtil.getinstance().getgoodsfromrydm(map, InitializeInfResult.class, new HttpActionHandle<InitializeInfResult>(){

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
            public void handleActionSuccess(String actionName,
                                            final InitializeInfResult result) {
                ReturnGoodsByNormalActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            List<CashierInfo> salers = result.getInitializeInfo().getSalemanlist();
                            if(salers != null && salers.size()> 0){
                                sales.clear();
                                sales.addAll(salers);
                                edit_return_handperson.setText(sales.get(0).getCashiername());
                                edit_return_handperson.setTag(0);
                            }else{
                                sales.clear();
                                edit_return_handperson.setText("");
                                edit_return_handperson.setTag("");
                            }
                            List<GoodsInfo> goods = result.getInitializeInfo().getBrandgoodslist();
                            if(goods != null && goods.size()>0){
                                goodinfos.clear();
                                goodinfos.addAll(goods);
                                new AddGoodDialog(ReturnGoodsByNormalActivity.this, goodinfos, new DialogFinishCallBack() {
                                    @Override
                                    public void finish(int p) {
                                        edit_return_good.setTag(p);
                                        edit_return_good.setText(goodinfos.get(p).getGoodsname());
                                    }
                                }).show();
                            }

                        }else {
                            sales.clear();
                            edit_return_handperson.setText("");
                            edit_return_handperson.setTag("");
                            goodinfos.clear();
                            edit_return_good.setTag("");
                            edit_return_good.setText("");
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(ReturnGoodsByNormalActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
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
                    ToastUtils.showtaostbyhandler(theActivity, msg);
                    break;
            }
        }
    }

    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
        goodinfos = new ArrayList<GoodsInfo>();
        if(SpSaveUtils.getObject(ReturnGoodsByNormalActivity.this, ConstantData.BRANDGOODSLIST) != null){
            goodinfos = (List<GoodsInfo>) SpSaveUtils.getObject(ReturnGoodsByNormalActivity.this, ConstantData.BRANDGOODSLIST);
        }
        sales = new ArrayList<CashierInfo>();
        if(SpSaveUtils.getObject(ReturnGoodsByNormalActivity.this, ConstantData.SALEMANLIST) != null){
            sales = (List<CashierInfo>) SpSaveUtils.getObject(ReturnGoodsByNormalActivity.this, ConstantData.SALEMANLIST);
        }
        ll_saleman.setVisibility(View.INVISIBLE);
        title_text_content.setText(getString(R.string.return_normal));
        text_desk_code.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_DESK_CODE, ""));
        text_bill_id.setText(AppConfigFile.getBillId());
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
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        edit_return_handperson.setOnTouchListener(this);
        edit_return_good.setOnTouchListener(this);
        new HorizontalKeyBoard(this, this, edit_return_money,ll_keyboard);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm_return_order})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
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
                billInfo.setBillid(AppConfigFile.getBillId());
                billInfo.setSaletype(ConstantData.SALETYPE_SALE_RETURN_NORMAL);
                billInfo.setOldposno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
                billInfo.setOldbillid(AppConfigFile.getBillId());
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
                HttpRequestUtil.getinstance().commitReturnOrder(HTTP_TASK_KEY, map, CommitOrderResult.class, new HttpActionHandle<CommitOrderResult>() {

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
                            Intent intent = new Intent(ReturnGoodsByNormalActivity.this, ReturnMoneyByNormalActivity.class);
                            intent.putExtra(ConstantData.BILL, billInfo);
                            startActivity(intent);
                        }else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }

                    @Override
                    public void startChangeMode() {
                        final HttpActionHandle httpActionHandle = this;
                        ReturnGoodsByNormalActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangeModeDialog(ReturnGoodsByNormalActivity.this, httpActionHandle).show();
                            }
                        });
                    }

                    @Override
                    public void handleActionOffLine() {
                        OrderInfoDao dao = new OrderInfoDao(mContext);
                        if(dao.addOrderGoodsInfo(billInfo.getBillid(), SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""), billInfo.getCashier(), SpSaveUtils.read(mContext, ConstantData.CASHIER_NAME, ""), billInfo.getSaletype(), Double.parseDouble(billInfo.getTotalmoney()), billInfo.getGoodslist())) {
                            Intent intent = new Intent(mContext, ReturnMoneyByNormalActivity.class);
                            intent.putExtra(ConstantData.TOTAL_RETURN_MONEY, billInfo.getTotalmoney());
                            intent.putExtra(ConstantData.BILL, billInfo);
                            startActivity(intent);
                        }else {
                            Toast.makeText(mContext, getString(R.string.offline_save_goods_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void handleActionChangeToOffLine() {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        startActivity(intent);
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
    private boolean checkParams(TextView view) {
        if(TextUtils.isEmpty(view.getText().toString()) || "".equals(view.getText().toString())) {
            return false;
        }
        return true;
    }
}
