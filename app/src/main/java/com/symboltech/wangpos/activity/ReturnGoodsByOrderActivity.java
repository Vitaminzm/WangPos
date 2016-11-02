package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.GoodsAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReturnGoodsByOrderActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;
    @Bind(R.id.text_cashier_name)
    TextView text_cashier_name;
    @Bind(R.id.text_bill_id)
    TextView text_bill_id;
    @Bind(R.id.text_saleman_name)
    TextView text_saleman_name;
    @Bind(R.id.text_desk_code)
    TextView text_desk_code;

    @Bind(R.id.text_cashier_name_tip)
    TextView text_cashier_name_tip;
    @Bind(R.id.text_bill_id_tip)
    TextView text_bill_id_tip;
    @Bind(R.id.text_saleman_name_tip)
    TextView text_saleman_name_tip;
    @Bind(R.id.text_desk_code_tip)
    TextView text_desk_code_tip;

    @Bind(R.id.text_return_money)
    TextView text_return_money;
    @Bind(R.id.text_return_score)
    TextView text_return_score;
    @Bind(R.id.ll_return_score)
    LinearLayout ll_return_score;

    @Bind(R.id.goods_listview)
    ListView goods_listview;
    private GoodsAdapter goodsAdapter;
    ArrayList<GoodsInfo> shopCarList = new ArrayList<>();

    private MemberInfo member;
    private BillInfo billInfo;
    private boolean flag = false;//商品金额是否已经处理过
    private List<CashierInfo> sales;

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
        text_cashier_name_tip.setText("原收款员：");
        text_bill_id_tip.setText("原订单号：");
        text_saleman_name_tip.setText("原销售员：");
        text_desk_code_tip.setText("原款台号：");
        title_text_content.setText(getString(R.string.return_order));
        billInfo = (BillInfo) getIntent().getSerializableExtra(ConstantData.BILL);
        if(billInfo != null) {
            member = billInfo.getMember();
        }
        text_desk_code.setText(billInfo.getOldposno());
        text_bill_id.setText(billInfo.getOldbillid());
        text_cashier_name.setText(billInfo.getCashiername());
        sales = (List<CashierInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.SALEMANLIST);
        if(sales != null && sales.size() > 0&& billInfo.getSaleman()!= null)
            text_saleman_name.setText(getSalemanNameByid(billInfo.getSaleman()));
        if(billInfo.getGoodslist() != null && billInfo.getGoodslist().size() > 0){
            shopCarList.addAll(billInfo.getGoodslist());
        }
        goodsAdapter = new GoodsAdapter(this, shopCarList, false);
        goods_listview.setAdapter(goodsAdapter);

        text_return_money.setText(billInfo.getTotalmoney());
        double score = 0;
        if(billInfo != null && billInfo.getGoodslist() != null && billInfo.getGoodslist().size() != 0) {
            for (GoodsInfo info : billInfo.getGoodslist()) {
                double usedFen = 0;//用的积分
                double grantFen = 0;//获得的积分
                if(info.getUsedpoint() != null && !"".equals(info.getUsedpoint())) {
                    try {
                        usedFen = Double.parseDouble(info.getUsedpoint());
                        usedFen = usedFen > 0 ? -usedFen : usedFen;
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.v("lgs", "rerturnGoodsByOrder format error");
                    }
                }
                if(info.getGrantpoint() != null && !"".equals(info.getGrantpoint())) {
                    try {
                        grantFen = Double.parseDouble(info.getGrantpoint());
                        grantFen = Math.abs(grantFen);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.v("lgs", "rerturnGoodsByOrder format error");
                    }
                }
                score += (usedFen+grantFen);
            }
        }
        if(score != 0 ){
            text_return_score.setText(Math.abs(score)+"");
        }else{
            ll_return_score.setVisibility(View.GONE);
        }
    }

    private String getSalemanNameByid(String id){
        String ret = "";
        for(CashierInfo info: sales){
            if(info.getCashierid().equals(id)){
                ret = info.getCashiername();
                break;
            }
        }
        return ret;
    }
    @Override
    protected void initView() {
        setContentView(R.layout.activity_return_goods_by_order);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm_return_order})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm_return_order:
                saveArticles();
                break;
        }
    }

    /**
     * 提交商品
     */
    private void saveArticles() {
        BillInfo bill = new BillInfo();
        bill.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
        bill.setBillid(AppConfigFile.getBillId());
        bill.setSaletype(ConstantData.SALETYPE_SALE_RETURN_ORDER);
        bill.setOldposno(billInfo.getOldposno());
        bill.setOldbillid(billInfo.getOldbillid());
        //修正原订单数据
        billInfo.setPosno(SpSaveUtils.read(mContext, ConstantData.CASHIER_DESK_CODE, ""));
        billInfo.setBillid(AppConfigFile.getBillId());
        bill.setCashier(SpSaveUtils.read(mContext, ConstantData.CASHIER_ID, ""));
        bill.setTotalmoney("-" + billInfo.getTotalmoney());
        bill.setSaletime(Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"));
        if(member != null) {
            member.setPoint(billInfo.getAwardpoint());
        }
        bill.setMember(member);
        bill.setGoodslist(dealGoodsMoney(billInfo.getGoodslist()));
        Map<String, String> map = new HashMap<String, String>();
        try {
            LogUtil.v("lgs",  GsonUtil.beanToJson(billInfo));
            map.put("billInfo", GsonUtil.beanToJson(bill));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().commitReturnOrder(HTTP_TASK_KEY, map, BaseResult.class, new HttpActionHandle<BaseResult>() {

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
            public void handleActionSuccess(String actionName, BaseResult result) {
                if(ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    Intent intent = new Intent(ReturnGoodsByOrderActivity.this, ReturnMoneyByOrderActivity.class);
                    intent.putExtra(ConstantData.BILL, billInfo);
                    startActivity(intent);
                }else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    /**
     * 处理退货金额
     * @param goods
     * @return
     */
    private List<GoodsInfo> dealGoodsMoney(List<GoodsInfo> goods) {
        if(!flag) {
            flag = true;
            for (GoodsInfo good : goods) {
                good.setSaleamt("-"+good.getSaleamt());
                good.setPrice("-"+good.getPrice());
            }
        }
        return goods;
    }
}
