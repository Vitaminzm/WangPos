package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.GoodsAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.AddGoodDialog;
import com.symboltech.wangpos.dialog.AddSalemanDialog;
import com.symboltech.wangpos.dialog.AddScoreGoodDialog;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.msg.entity.AllMemberInfo;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.result.SubmitGoodsResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PaymentActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.text_consume_score)
    TextView text_consume_score;
    @Bind(R.id.text_total_money)
    TextView text_total_money;
    @Bind(R.id.edit_input_money)
    EditText edit_input_money;
    @Bind(R.id.view_line)
    View view_line;

    @Bind(R.id.radio_look_member)
    TextView radio_look_member;
    @Bind(R.id.radio_add_score_good)
    TextView radio_add_score_good;
    @Bind(R.id.ll_used_score)
    LinearLayout ll_used_score;

    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;

    @Bind(R.id.text_cashier_name)
    TextView text_cashier_name;
    @Bind(R.id.text_bill_id)
    TextView text_bill_id;
    @Bind(R.id.text_saleman_name)
    TextView text_saleman_name;
    @Bind(R.id.text_desk_code)
    TextView text_desk_code;


    @Bind(R.id.goods_listview)
    SwipeMenuListView goods_listview;
    private HorizontalKeyBoard keyboard;
    private double summoney;
    private int sumintegral;

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

    /** 进入途径初始化 */
    private int enterFlag = ConstantData.ENTER_CASHIER_BY_ACCOUNTS;
    //会员验证方式
    private String member_type = ConstantData.MEMBER_VERIFY_BY_PHONE;
    //会员信息
    private AllMemberInfo memberBigdate;
    //用来记录哪个商品被选中
    private int position = 0;
    //该店铺拥有的商品
    private List<GoodsInfo> goodinfos;

    MyHandler handler = new MyHandler(this);

    //购物车相关的商品
    private GoodsAdapter goodsAdapter;
    ArrayList<GoodsInfo> shopCarList = new ArrayList<>();
    //销售员列表
    private List<CashierInfo> sales;

    @Override
    protected void initData() {
        cartuicontroller();
        initsalesandgoods();
        if(isHavePriceChangeGood()){
            keyboard = new HorizontalKeyBoard(this, this, edit_input_money,ll_keyboard, new KeyBoardListener() {
                @Override
                public void onComfirm() {

                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onValue(String value) {
                    if(ArithDouble.parseDouble(value) == 0){
                        ToastUtils.sendtoastbyhandler(handler,getString(R.string.warning_no_input_format));
                        return;
                    }
                    if(goodinfos != null && goodinfos.size() > 0){
                        addcartgoods(value, position, ConstantData.GOOD_PRICE_CAN_CHANGE);
                    }else{
                        ToastUtils.sendtoastbyhandler(handler,getString(R.string.warning_no_good));
                    }
                }
            });
        }else{
            edit_input_money.setEnabled(false);
        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
//                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(
//                        getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
//                        0xCE)));
//                // set item width
//                openItem.setWidth(20);
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(R.color.red);
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // set item width
                deleteItem.setWidth(150);
                // set a icon
                // deleteItem.setIcon(R.mipmap.btn_bank_icon);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        goods_listview.setMenuCreator(creator);
        goods_listview.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        goods_listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                shopCarList.remove(position);
                goodsAdapter.notifyDataSetChanged();
                return false;
            }
        });
        goodsAdapter = new GoodsAdapter(getApplicationContext(), shopCarList);
        goodsAdapter.registerDataSetObserver(new GoodsDataObserver());
        goods_listview.setAdapter(goodsAdapter);
        goods_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                goods_listview.smoothOpenMenu(position);
                return false;
            }
        });
    }

    /**
     *
     *  进入收银页面途径
     */
    private void cartuicontroller(){
        enterFlag = getIntent().getIntExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_ACCOUNTS);
        if(enterFlag == ConstantData.ENTER_CASHIER_BY_ACCOUNTS){
            radio_add_score_good.setVisibility(View.GONE);
            radio_look_member.setVisibility(View.GONE);
            ll_used_score.setVisibility(View.GONE);
        }else{
            memberBigdate = (AllMemberInfo) getIntent().getSerializableExtra(ConstantData.ALLMEMBERINFO);
            member_type = getIntent().getStringExtra(ConstantData.MEMBER_VERIFY);
        }
    }
    /**
     * 初始化销售员和商品
     */
    public void initsalesandgoods() {
        goodinfos = (List<GoodsInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.BRANDGOODSLIST);
        sales = (List<CashierInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.SALEMANLIST);
        title_text_content.setText(getString(R.string.pay));
        text_desk_code.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_DESK_CODE, ""));
        text_bill_id.setText(AppConfigFile.getBillId());
        text_cashier_name.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_NAME, ""));
        if (sales != null && sales.size() > 0) {
            text_saleman_name.setText(sales.get(0).getCashiername());
            text_saleman_name.setTag(sales.get(0).getCashierid());
        }

    }

    /**
     * 是否存在可以改价格的商品
     * @return
     */
    public boolean isHavePriceChangeGood(){
        boolean ret = false;
        for(int i= 0; i<goodinfos.size();i++){
            if(!goodinfos.get(i).getSpmode().trim().equals("0")){
                position = i;
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_payment);
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
    }

    public void lookMember(){
        Intent intent = new Intent(this,MemberDetailActivity.class);
        intent.putExtra(ConstantData.ALLMEMBERINFO, memberBigdate);
        startActivity(intent);
    }
    /**
     *
     * @param value  金额
     * @param position 位置
     * @param tag   是否改价
     */
    protected void addcartgoods(String value, int position, int tag) {
        GoodsInfo goodsInfo = null;
        GoodsInfo gs = goodinfos.get(position);
        try {
            goodsInfo = (GoodsInfo) gs.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        goodsInfo.setSalecount("1");
        if(tag == ConstantData.GOOD_PRICE_CAN_CHANGE){
            goodsInfo.setPrice(MoneyAccuracyUtils.formatMoneyByTwo(value));
        }
        shopCarList.add(goodsInfo);
        goodsAdapter.notifyDataSetChanged();
    }

    protected void addcartScoregoods(String value, int position, int tag) {
        GoodsInfo goodsInfo = null;
        GoodsInfo gs = memberBigdate.getGoodslist().get(position);
        try {
            goodsInfo = (GoodsInfo) gs.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        goodsInfo.setSalecount("1");
        if(tag == ConstantData.GOOD_PRICE_CAN_CHANGE){
            goodsInfo.setPrice(MoneyAccuracyUtils.formatMoneyByTwo(value));
        }
        shopCarList.add(goodsInfo);
        goodsAdapter.notifyDataSetChanged();
    }
    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm_order, R.id.radio_add_score_good, R.id.radio_select_good, R.id.radio_add_salesman, R.id.radio_look_member})
    public void back(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm_order:
                submitgoodsorder();
                break;
            case R.id.radio_add_score_good:
                if(memberBigdate !=null && memberBigdate.getGoodslist() != null && memberBigdate.getGoodslist().size() > 0){
                    new AddScoreGoodDialog(PaymentActivity.this, memberBigdate.getGoodslist(), new DialogFinishCallBack() {
                        @Override
                        public void finish(int p) {
                            addcartScoregoods(null, p, ConstantData.GOOD_PRICE_NO_CHANGE);
                        }
                    }).show();
                }else{
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_good));
                }
                break;
            case R.id.radio_select_good:
                if (goodinfos != null && goodinfos.size() > 0) {
                    new AddGoodDialog(PaymentActivity.this, goodinfos, new DialogFinishCallBack() {
                        @Override
                        public void finish(int p) {
                            if (goodinfos.get(p).getSpmode().trim().equals("0")) {
                                addcartgoods(null, p, ConstantData.GOOD_PRICE_NO_CHANGE);
                            } else {
                                position = p;
                                keyboard.show();
                            }
                        }
                    }).show();

                } else {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_good));
                }
                break;
            case R.id.radio_add_salesman:
                if (sales != null && sales.size() > 0) {
                    new AddSalemanDialog(PaymentActivity.this, sales, new DialogFinishCallBack() {
                        @Override
                        public void finish(int position) {
                            text_saleman_name.setText(sales.get(position).getCashiername());
                            text_saleman_name.setTag(sales.get(position).getCashierid());
                        }
                    }).show();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_saleman));
                }
                break;
            case R.id.radio_look_member:
                lookMember();
                break;
        }
    }

    /**
     * 提交商品前做检查
     */
    private void submitgoodsorder() {
        if(StringUtil.isEmpty(text_saleman_name.getText().toString())){
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_select_saleman));
            return;
        }
        if(shopCarList.size() <= 0){
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_select_good));
            return;
        }
        if (enterFlag == ConstantData.ENTER_CASHIER_BY_ACCOUNTS) {
            submitgoodsorderforhttp(false);
        }else{
            if(sumintegral > 0){
                if(sumintegral > ArithDouble.parseDouble(memberBigdate.getMember().getCent_total())){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_score_notfull));
                    return;
                }
            }
            submitgoodsorderforhttp(true);
        }
    }

    /**
     * 提交商品
     * @param flag
     */
    public void submitgoodsorderforhttp(boolean flag){
        Map<String, String> map = new HashMap<String, String>();
        BillInfo billinfo = new BillInfo();
        billinfo.setBillid(AppConfigFile.getBillId());
        billinfo.setSaletype("0");
        billinfo.setCashier(String.valueOf(text_saleman_name.getTag()));
        String type = SpSaveUtils.read(MyApplication.context, ConstantData.MALL_MONEY_OMIT, "0");
        billinfo.setTotalmoney(MoneyAccuracyUtils
                .getmoneybytwo(ArithDouble.parseDoubleByType(text_total_money.getText().toString(), type)));
        billinfo.setSaletime(Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"));
        if (enterFlag == ConstantData.ENTER_CASHIER_BY_ACCOUNTS) {
            billinfo.setMember(null);
        } else {
            if (memberBigdate != null && memberBigdate.getMember() != null) {
                if (!StringUtil.isEmpty(text_consume_score.getText().toString())) {
                    memberBigdate.getMember().setPoint(text_consume_score.getText().toString());
                } else {
                    memberBigdate.getMember().setPoint("0");
                }
                billinfo.setMember(memberBigdate.getMember());
            }
        }
        billinfo.setGoodslist(shopCarList);
        for (GoodsInfo gi : billinfo.getGoodslist()) {
            gi.setUsedpoint(gi.getUsedpointtemp());
        }
        try {
            map.put("billinfo", GsonUtil.beanToJson(billinfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().submitgoods(map, SubmitGoodsResult.class, new HttpActionHandle<SubmitGoodsResult>() {

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
            public void handleActionSuccess(String actionName, SubmitGoodsResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    String type = SpSaveUtils.read(MyApplication.context, ConstantData.MALL_MONEY_OMIT, "0");
                    if (enterFlag == ConstantData.ENTER_CASHIER_BY_MEMBER) {
                        Intent intent_payment = new Intent(PaymentActivity.this, CheckOutActivity.class);
                        // 是否是会员标识
                        intent_payment.putExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_VERITY);
                        intent_payment.putExtra(ConstantData.SALESMAN_NAME, text_saleman_name.getText().toString());
                        // 订单总额
                        intent_payment.putExtra(ConstantData.GET_ORDER_VALUE_INFO, ArithDouble.parseDouble(MoneyAccuracyUtils.getmoneybytwo(
                                ArithDouble.parseDoubleByType(text_total_money.getText().toString(), type))));
                        intent_payment.putExtra(ConstantData.MEMBER_VERIFY, member_type);
                        intent_payment.putExtra(ConstantData.MEMBER_EQUITY, result.getSubmitgoods());
                        intent_payment.putExtra(ConstantData.GET_MEMBER_INFO, memberBigdate.getMember());
                        intent_payment.putExtra(ConstantData.CART_HAVE_GOODS, (Serializable) shopCarList);
                        startActivity(intent_payment);
                    } else {
                        Intent 	intent_payment = new Intent(PaymentActivity.this, CheckOutActivity.class);
                        // 是否是会员标识
                        intent_payment.putExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_NOT_VERITY);
                        intent_payment.putExtra(ConstantData.SALESMAN_NAME, text_saleman_name.getText().toString());
                        // 订单总额
                        intent_payment.putExtra(ConstantData.GET_ORDER_VALUE_INFO, ArithDouble.parseDouble(MoneyAccuracyUtils.getmoneybytwo(
                                ArithDouble.parseDoubleByType(text_total_money.getText().toString(), type))));
                        intent_payment.putExtra(ConstantData.CART_HAVE_GOODS, (Serializable) shopCarList);
                        startActivity(intent_payment);
                    }
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void handleActionOffLine() {
                String type = SpSaveUtils.read(MyApplication.context, ConstantData.MALL_MONEY_OMIT, "0");
                OrderInfoDao dao = new OrderInfoDao(mContext);
                boolean result = dao.addOrderGoodsInfo(AppConfigFile.getBillId(),
                        SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""),
                        String.valueOf(text_saleman_name.getTag()),
                        String.valueOf(text_saleman_name.getText()), "0",
                        ArithDouble.parseDoubleByType(text_total_money.getText().toString(), type), shopCarList);
                if (result) {
                    Intent intent_payment = new Intent(PaymentActivity.this, CheckOutActivity.class);
                    // 是否是会员标识
                    intent_payment.putExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_NOT_VERITY);
                    intent_payment.putExtra(ConstantData.SALESMAN_NAME, text_saleman_name.getText().toString());
                    // 订单总额
                    intent_payment.putExtra(ConstantData.GET_ORDER_VALUE_INFO, ArithDouble.parseDouble(MoneyAccuracyUtils.getmoneybytwo(
                            ArithDouble.parseDoubleByType(text_total_money.getText().toString(), type))));
                    intent_payment.putExtra(ConstantData.CART_HAVE_GOODS, (Serializable) shopCarList);
                    startActivity(intent_payment);
                } else {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_save_goods_failed));
                }
            }
            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                PaymentActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(PaymentActivity.this, httpActionHandle).show();
                    }
                });
            }
        });
    }
    class GoodsDataObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            // TODO Auto-generated method stub
            super.onChanged();
            sumintegral = 0;
            summoney = 0;
            if (shopCarList != null && shopCarList.size() > 0) {
                for (GoodsInfo goodinfo : shopCarList) {
                    summoney += ArithDouble.parseDouble(goodinfo.getSaleamt());
                    if (!StringUtil.isEmpty(goodinfo.getSptype()) && "1".equals(goodinfo.getSptype())) {
                        sumintegral += ArithDouble.parseInt(goodinfo.getUsedpointtemp());
                    }
                }

            }
            text_consume_score.setText(sumintegral + "");
            text_total_money.setText(MoneyAccuracyUtils.getmoneybytwo(summoney));
            if (sumintegral > 0) {
                ll_used_score.setVisibility(View.VISIBLE);
            } else {
                ll_used_score.setVisibility(View.GONE);
            }
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    }
}
