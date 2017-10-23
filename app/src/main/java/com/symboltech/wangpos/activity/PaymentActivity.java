package com.symboltech.wangpos.activity;

import android.app.AlertDialog;
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
import com.symboltech.wangpos.dialog.InputDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.msg.entity.AllMemberInfo;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.GoodsAndSalerInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.result.InitializeInfResult;
import com.symboltech.wangpos.result.SubmitGoodsResult;
import com.symboltech.wangpos.result.ZklResult;
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
                    ToastUtils.showtaostbyhandler(theActivity, msg);
                    break;
                case ToastUtils.TOAST_WHAT_DIALOG:
                    new AlertDialog.Builder(theActivity).setTitle("错误提示").setMessage(msg.obj.toString()).setPositiveButton("确定", null).setCancelable(false).show();
                    break;
                case 3:
                    int position = (int) msg.obj;
                    theActivity.closewaitdialog();
                    addcartgoods(null, null, position, ConstantData.GOOD_PRICE_NO_CHANGE);
                    break;
                case 4:
                    int positiongood = (int) msg.obj;
                    theActivity.closewaitdialog();
                    getGoodsZkl(positiongood, null);
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
    ArrayList<GoodsInfo> shopCarList = new ArrayList<GoodsInfo>();
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
                    edit_input_money.setText("");
                }

                @Override
                public void onValue(String value) {
                    if(ArithDouble.parseDouble(value) == 0){
                        ToastUtils.sendtoastbyhandler(handler,getString(R.string.warning_no_input_format));
                        return;
                    }
                    if(ArithDouble.parseDouble(value) > 1000000){
                        ToastUtils.sendtoastbyhandler(handler, "价格太大");
                        return;
                    }
                    if(goodinfos != null && goodinfos.size() > 0){
                        if(AppConfigFile.isOffLineMode() || memberBigdate== null){
                            addcartgoods(value, null, position, ConstantData.GOOD_PRICE_CAN_CHANGE);
                        }else{
                            getGoodsZkl(position, value);
                        }
                        edit_input_money.setText("");
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
        goodinfos = new ArrayList<GoodsInfo>();
        if (SpSaveUtils.getObject(MyApplication.context, ConstantData.BRANDGOODSLIST) != null) {
            goodinfos = (List<GoodsInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.BRANDGOODSLIST);
        }
        sales = new ArrayList<CashierInfo>();
        if (SpSaveUtils.getObject(MyApplication.context, ConstantData.SALEMANLIST) != null) {
            sales = (List<CashierInfo>) SpSaveUtils.getObject(MyApplication.context, ConstantData.SALEMANLIST);
        }
        title_text_content.setText(getString(R.string.pay));
        text_desk_code.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_DESK_CODE, ""));
        text_bill_id.setText(AppConfigFile.getBillId());
        text_cashier_name.setText(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_NAME, ""));
        if (sales != null && sales.size() > 0) {
            CashierInfo  info = getCashierInfo(SpSaveUtils.read(getApplication(), ConstantData.CASHIER_NAME, ""));
            if(info!= null){
                text_saleman_name.setText(info.getCashiername());
                text_saleman_name.setTag(info.getCashierid());
            }else{
                text_saleman_name.setText(sales.get(0).getCashiername());
                text_saleman_name.setTag(sales.get(0).getCashierid());
            }
        }

    }

    private CashierInfo getCashierInfo(String name){
        CashierInfo  info = null;
        if(sales == null || sales.size()==0){
            return  info;
        }
        for(CashierInfo  infoTmp:sales){
            if(name.equals(infoTmp.getCashiername())){
                info = infoTmp;
                break;
            }
        }
        return info;
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
        Intent intent = new Intent(this, MemberDetailActivity.class);
        intent.putExtra(ConstantData.ALLMEMBERINFO, memberBigdate);
        startActivity(intent);
    }
    /**
     *
     * @param value  金额
     * @param position 位置
     * @param tag   是否改价
     */
    protected void addcartgoods(String value, String zk, int position, int tag) {
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
        if (!StringUtil.isEmpty(zk)){
            if(!StringUtil.isEmpty(goodsInfo.getZkprice())){
                goodsInfo.setZkprice(MoneyAccuracyUtils.formatMoneyByTwo(""+ArithDouble.add(ArithDouble.parseDouble(zk), ArithDouble.parseDouble(goodsInfo.getZkprice()))));
            }else{
                goodsInfo.setZkprice(MoneyAccuracyUtils.formatMoneyByTwo(zk));
            }
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

    @OnClick({R.id.title_icon_back, R.id.text_confirm_order, R.id.radio_add_score_good, R.id.radio_select_good, R.id.radio_add_good, R.id.radio_add_salesman, R.id.radio_look_member})
    public void click(View view) {
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id) {
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm_order:
                submitgoodsorder();
                break;
            case R.id.radio_add_good:
                if (goodinfos != null && goodinfos.size() > 0) {
                    new InputDialog(this, "请输入商品码", "请输入商品码", new GeneralEditListener(){

                        @Override
                        public void editinput(final String edit) {
                            getGoodsBycode(edit);
                        }
                    }).show();
                }else{
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_good));
                }
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
                            if(AppConfigFile.isOffLineMode() || memberBigdate== null){
                                if (goodinfos.get(p).getSpmode().trim().equals("0")) {
                                    addcartgoods(null, null, p, ConstantData.GOOD_PRICE_NO_CHANGE);
                                } else {
                                    position = p;
                                    keyboard.show();
                                }
                            }else{
                                if (goodinfos.get(p).getSpmode().trim().equals("0")) {
                                    Message msg = Message.obtain();
                                    msg.what = 4;
                                    msg.obj = p;
                                    handler.sendMessage(msg);
                                } else {
                                    position = p;
                                    keyboard.show();
                                }

                            }

                        }
                    }).show();

                } else {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_good));
                }
                break;
            case R.id.radio_add_salesman:
                if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(mContext, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
                    new InputDialog(this, "请输入销售员编码", "请输入销售员编码", new GeneralEditListener(){

                        @Override
                        public void editinput(final String edit) {
                            if(AppConfigFile.isOffLineMode()){
                                PaymentActivity.this.runOnUiThread(new Runnable(){

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
                }
                break;
            case R.id.radio_look_member:
                lookMember();
                break;
        }
    }

    public void getGoodsBycode(final String code){
        if(StringUtil.isEmpty(code)){
            ToastUtils.sendtoastbyhandler(handler, "输入商品码为空");
            return;
        }
        startwaitdialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isFind = false;
                for(int i = 0;i<goodinfos.size();i++){
                    GoodsInfo info = goodinfos.get(i);
                    if(code.equals(info.getCode())){
                        isFind = true;
                        if(AppConfigFile.isOffLineMode() || memberBigdate== null){
                            if (info.getSpmode().trim().equals("0")) {
                                position = i;
                                Message msg = Message.obtain();
                                msg.what = 3;
                                msg.obj = i;
                                handler.sendMessage(msg);
                            } else {
                                closewaitdialog();
                                position = i;
                                keyboard.show();
                            }
                        }else{
                            Message msg = Message.obtain();
                            msg.what = 4;
                            msg.obj = i;
                            handler.sendMessage(msg);
                        }
                        break;
                    }
                }
                if(!isFind){
                    closewaitdialog();
                    ToastUtils.sendtoastbyhandler(handler, "未找到该商品");
                }
            }
        }).start();
    }

    public void getGoodsZkl(final int position, final String price){
        if(memberBigdate == null || memberBigdate.getMember()==null){
            ToastUtils.sendtoastbyhandler(handler, "会员信息有误不能查询折扣");
            return;
        }
        final GoodsInfo goodsInfo = goodinfos.get(position);
        Map<String, String> map = new HashMap<String, String>();
        map.put("spid", goodsInfo.getId());
        if(StringUtil.isEmpty(price)){
            map.put("money", ""+ArithDouble.sub(ArithDouble.parseDouble(goodsInfo.getPrice()), ArithDouble.parseDouble(goodsInfo.getZkprice())));
        }else{
            map.put("money", ""+ArithDouble.sub(ArithDouble.parseDouble(price), ArithDouble.parseDouble(goodsInfo.getZkprice())));
        }
        map.put("membertypename", memberBigdate.getMember().getMembertypename());
        HttpRequestUtil.getinstance().getgoodszkl(map, ZklResult.class, new HttpActionHandle<ZklResult>() {
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
            public void handleActionSuccess(String actionName, final ZklResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    PaymentActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(result.getData()!= null){
                                if(StringUtil.isEmpty(price)){
                                    addcartgoods(null, ArithDouble.parseDouble(result.getData().getHyzkmoney()) + "", position, ConstantData.GOOD_PRICE_NO_CHANGE);
                                }else{
                                    addcartgoods(price, ArithDouble.parseDouble(result.getData().getHyzkmoney()) + "", position, ConstantData.GOOD_PRICE_CAN_CHANGE);
                                }
                            }else{
                                ToastUtils.sendtoastbyhandler(handler, "数据异常");
                            }
                        }
                    });
                }else{
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
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
                            text_saleman_name.setText(cashier.getCashiername());
                            text_saleman_name.setTag(cashier.getCashierid());
                            List<GoodsInfo> goods = info.getBrandgoodlist();
                            goodinfos.clear();
                            goodinfos.addAll(goods);
                            if(goodinfos != null && goodinfos.size() > 0){
                                new AddGoodDialog(PaymentActivity.this, goodinfos, new DialogFinishCallBack() {
                                    @Override
                                    public void finish(int p) {
                                        if (goodinfos.get(p).getSpmode().trim().equals("0")) {
                                            addcartgoods(null, null, p, ConstantData.GOOD_PRICE_NO_CHANGE);
                                        } else {
                                            position = p;
                                            keyboard.show();
                                        }
                                    }
                                }).show();
                            }else{
                                ToastUtils.sendtoastbyhandler(handler, "该销售人员没有商品");
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
                PaymentActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            List<CashierInfo> salesinfo= result.getInitializeInfo().getSalemanlist();
                            List<GoodsInfo> goods = result.getInitializeInfo().getBrandgoodslist();
                            if(goods != null && goods.size()>0){
                                goodinfos.clear();
                                goodinfos.addAll(goods);
                            }
                            if(salesinfo != null && salesinfo.size()>0){
                                sales.clear();
                                sales.addAll(salesinfo);
                                text_saleman_name.setText(sales.get(0).getCashiername());
                                text_saleman_name.setTag(sales.get(0).getCashierid());
                            }
                            if(goodinfos != null && goodinfos.size() > 0){
                                new AddGoodDialog(PaymentActivity.this, goodinfos, new DialogFinishCallBack() {
                                    @Override
                                    public void finish(int p) {
                                        if (goodinfos.get(p).getSpmode().trim().equals("0")) {
                                            addcartgoods(null, null, p, ConstantData.GOOD_PRICE_NO_CHANGE);
                                        } else {
                                            position = p;
                                            keyboard.show();
                                        }
                                    }
                                }).show();
                            }
                        }else {
                            goodinfos.clear();
                            text_saleman_name.setText("");
                            text_saleman_name.setTag("");
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
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
        billinfo.setCashiername(text_saleman_name.getText().toString());
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
        for(int i= 0;i<shopCarList.size();i++){
            shopCarList.get(i).setInx((i+1)+"");
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
        HttpRequestUtil.getinstance().submitgoods(HTTP_TASK_KEY, map, SubmitGoodsResult.class, new HttpActionHandle<SubmitGoodsResult>() {

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
                        intent_payment.putExtra(ConstantData.GET_ORDER_MANJIAN_VALUE_INFO, ArithDouble.parseDouble(result.getSubmitgoods().getTotalmbjmoney()));
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
                        intent_payment.putExtra(ConstantData.GET_ORDER_MANJIAN_VALUE_INFO, ArithDouble.parseDouble(result.getSubmitgoods().getTotalmbjmoney()));
                        intent_payment.putExtra(ConstantData.CART_HAVE_GOODS, (Serializable) shopCarList);
                        startActivity(intent_payment);
                    }
                } else {
                    ToastUtils.sendtoastdialogbyhandler(handler, result.getMsg());
                    //ToastUtils.sendtoastbyhandler(handler, result.getMsg());
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
                MemberInfo memberInfo = null;
                if(memberBigdate != null){
                    memberInfo = memberBigdate.getMember();
                }
                boolean result = dao.addOrderGoodsInfo(AppConfigFile.getBillId(),
                        SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""),
                        String.valueOf(text_saleman_name.getTag()),
                        String.valueOf(text_saleman_name.getText()), "0",
                        ArithDouble.parseDoubleByType(text_total_money.getText().toString(), type), shopCarList, memberInfo);
                if (result) {
                    Intent intent_payment = new Intent(PaymentActivity.this, CheckOutActivity.class);
                    if (enterFlag == ConstantData.ENTER_CASHIER_BY_MEMBER) {
                        intent_payment.putExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_VERITY);
                        intent_payment.putExtra(ConstantData.GET_MEMBER_INFO, memberBigdate.getMember());
                    }else {
                        // 是否是会员标识
                        intent_payment.putExtra(ConstantData.VERIFY_IS_MEMBER, ConstantData.MEMBER_IS_NOT_VERITY);
                    }
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
                    if (!StringUtil.isEmpty(goodinfo.getSptype()) && (ConstantData.GOODS_SOURCE_BY_INTEGRAL.equals(goodinfo.getSptype()) || ConstantData.GOODS_SOURCE_BY_SINTEGRAL.equals(goodinfo.getSptype()))) {
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
