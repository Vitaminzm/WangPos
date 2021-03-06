package com.symboltech.wangpos.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.DemandNoteTableAdapter;
import com.symboltech.wangpos.adapter.ReportTableAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.RefundReportInfo;
import com.symboltech.wangpos.msg.entity.ReportDetailInfo;
import com.symboltech.wangpos.msg.entity.ReportInfo;
import com.symboltech.wangpos.msg.entity.SaleReportInfo;
import com.symboltech.wangpos.msg.entity.TotalReportInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.result.ReportResult;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.service.aidl.IPrintCallback;
import cn.koolcloud.engine.service.aidl.IPrinterService;
import cn.koolcloud.engine.service.aidlbean.ApmpRequest;
import cn.koolcloud.engine.service.aidlbean.IMessage;
import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.impl.WeiposImpl;

public class DemandNoteActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_desk_code)TextView text_desk_code;
    @Bind(R.id.text_shop)TextView text_shop;
    @Bind(R.id.text_shop_tip)TextView text_shop_tip;
    @Bind(R.id.tv_count_total)TextView tv_count_total;
    @Bind(R.id.tv_money_total)TextView tv_money_total;
    @Bind(R.id.listview_statistics)ListView listview_statistics;

    private ReportInfo reportInfo; // 报表数据
    private DemandNoteTableAdapter myAdapter;
    protected static final int printStart = 0;
    protected static final int printEnd = 1;
    protected static final int printError = 2;
    private ArrayList<ReportDetailInfo> infos;
    private HorizontalKeyBoard keyboard;

    /** refresh UI By handler */
    static class MyHandler extends Handler {
        WeakReference<BaseActivity> mActivity;

        MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<BaseActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity theActivity = mActivity.get();
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(theActivity,msg);
                    break;
                case printStart:
                    isPrinting = true;
                    break;
                case printEnd:
                    isPrinting = false;
                    break;
                case printError:
                    isPrinting = false;
                    // 0：正常 -1：缺纸 -2：未合盖 -3：卡纸 -4 初始化异常 -100：其他故障
                    // -999：不支持该功能（可以不支持）
                    String errorMsg = "";
                    LogUtil.e("lgs", "printer errorCode is " + msg.arg1);
                    switch (msg.arg1) {
                        case -1:
                            errorMsg = "result=-1：缺纸";
                            break;
                        case -2:
                            errorMsg = "result=-2：未合盖";
                            break;
                        case -3:
                            errorMsg = "result=-3：卡纸";
                            break;
                        case -4:
                            errorMsg = "result=-4 初始化异常";
                            break;
                        case -999:
                            errorMsg = "result=-999：不支持该功能";
                            break;
                        default:
                            errorMsg = "result=-100：其他故障";
                            break;
                    }
                    msg.obj = errorMsg;
                    ToastUtils.showtaostbyhandler(theActivity,msg);
                    break;
            }
        }
    }

    static public boolean isPrinting = false;
    MyHandler handler = new MyHandler(this);

    private LatticePrinter latticePrinter;// 点阵打印
     // 打印服务
    private static IPrinterService iPrinterService;
    private ServiceConnection printerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iPrinterService = IPrinterService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iPrinterService = null;
        }
    };

    IPrintCallback.Stub callback = new IPrintCallback.Stub() {
        @Override
        public void handleMessage(IMessage message) throws RemoteException {
            int ret = message.what;
            LogUtil.d("lgs", "handleMessage ret:" + ret);
            try {
                iPrinterService.unRegisterPrintCallback();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ret == 0) {
                Message msg3 = new Message();
                msg3.what = printEnd;
                handler.sendMessage(msg3);
            } else {
                Message msg3 = new Message();
                msg3.what = printError;
                msg3.arg1 = ret;
                handler.sendMessage(msg3);
            }
        }
    };
    @Override
    protected void initData() {
        infos = new ArrayList<ReportDetailInfo>();
        initDataByPaymentId(infos);
        myAdapter = new DemandNoteTableAdapter(getApplicationContext(), infos, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(!keyboard.isShowing()){
                        keyboard.setEdittext((EditText)v);
                        keyboard.show();
                    }
                    return false;
                }else{
                    return false;
                }
            }
        });
        myAdapter.registerDataSetObserver(new MoneyDataObserver());
        listview_statistics.setAdapter(myAdapter);

        if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(mContext, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
            text_shop_tip.setVisibility(View.GONE);
            text_shop.setVisibility(View.GONE);
        }
        text_desk_code.setText(SpSaveUtils.read(this, ConstantData.CASHIER_DESK_CODE, ""));
        text_shop.setText(SpSaveUtils.read(this, ConstantData.SHOP_NAME, ""));
        Map<String, String> map = new HashMap<String, String>();
        title_text_content.setText(getString(R.string.desk_demand));
        map.put("person_id", SpSaveUtils.read(DemandNoteActivity.this, ConstantData.CASHIER_ID, ""));
        HttpRequestUtil.getinstance().getReportInfo(HTTP_TASK_KEY, map, ReportResult.class, new HttpActionHandle<ReportResult>() {

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
            public void handleActionSuccess(String actionName, ReportResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    reportInfo = result.getReportInfo();
                    DemandNoteActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switchUI("total");
                        }
                    });
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

            @Override
            public void handleActionOffLine() {
                ToastUtils.sendtoastbyhandler(handler, "离线模式无数据");
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(DemandNoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        keyboard = new HorizontalKeyBoard(this, this, true, new KeyBoardListener(){

            @Override
            public void onComfirm() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onValue(String value) {
                double money ;
                try{
                    money = Double.parseDouble(value);;
                }catch(Exception e){
                    e.printStackTrace();
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_format_msg));
                    return;
                }
                if(money >= 0){
                    EditText edit = keyboard.getEdittext();
                    if(edit != null && edit.getTag() != null){
                        String[] flag = ((String)edit.getTag()).split("-");
                        if("1".equals(flag[0])){
                            infos.get(Integer.parseInt(flag[1])).setMoney(MoneyAccuracyUtils.getmoneybytwo(money));
                            myAdapter.notifyDataSetChanged();
                        }else if("2".equals(flag[0])){
                            infos.get(Integer.parseInt(flag[1])).setCount(ArithDouble.parseInt(value)+"");
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_format_msg));
                }
            }
        });
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            try {
                // 设备可能没有打印机，open会抛异常
                latticePrinter = WeiposImpl.as().openLatticePrinter();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
            Intent printService = new Intent(IPrinterService.class.getName());
            printService = AndroidUtils.getExplicitIntent(this, printService);
            if (printService != null)
                bindService(printService, printerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void initDataByPaymentId(ArrayList<ReportDetailInfo> infos) {
        for(int i=0;i<5;i++){
            ReportDetailInfo temp  = new ReportDetailInfo();
            String id = null;
            switch (i) {
                case 0:
                    id = getSKFSByType(PaymentTypeEnum.CASH.getStyletype());
                    if(id != null){
                        temp.setCode(id);
                        temp.setName("现金");
                        infos.add(temp);
                    }
                    break;
                case 1:
                    id = getSKFSByType(PaymentTypeEnum.BANK.getStyletype());
                    if(id != null){
                        temp.setCode(id);
                        temp.setName("银行卡");
                        infos.add(temp);
                    }
                    break;
                case 2:
                    id = getSKFSByType(PaymentTypeEnum.WECHAT.getStyletype());
                    if(id != null){
                        temp.setCode(id);
                        temp.setName("微信");
                        infos.add(temp);
                    }
                    break;
                case 3:
                    id = getSKFSByType(PaymentTypeEnum.STORE.getStyletype());
                    if(id != null){
                        temp.setCode(id);
                        temp.setName("储值卡");
                        temp.setCount("0");
                        temp.setMoney("0");
                        infos.add(temp);
                    }
                    break;
                case 4:
                    id = getSKFSByType(PaymentTypeEnum.COUPON.getStyletype());
                    if(id != null){
                        temp.setCode(id);
                        temp.setName("电子券");
                        temp.setCount("0");
                        temp.setMoney("0");
                        infos.add(temp);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private String getSKFSByType(String type) {
        List<PayMentsInfo> paymentInfos = (List<PayMentsInfo>) SpSaveUtils.getObject(getApplicationContext(), ConstantData.PAYMENTSLIST);
        for (PayMentsInfo payMentsInfo : paymentInfos) {
            if(type.equals(payMentsInfo.getType())) {
                return payMentsInfo.getId();
            }
        }
        return null;
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_demand_note);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        if (iPrinterService != null) {
            unbindService(printerServiceConnection);
        }
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    public void switchUI(String flag){
        if ("total".equals(flag)) {
            if(reportInfo != null && reportInfo.getTotal() != null) {
                dealDatabyPaymentid(reportInfo.getTotal().getTotallist());
                myAdapter.notifyDataSetChanged();
            }
        }
    }
    private void dealDatabyPaymentid(List<ReportDetailInfo> datas) {
        for(ReportDetailInfo info:infos){
            for(ReportDetailInfo infotemp:datas){
                if(info.getCode().equals(infotemp.getCode()) &&(PaymentTypeEnum.COUPON.getStyletype().equals(info.getCode())
                        ||PaymentTypeEnum.STORE.getStyletype().equals(info.getCode())) ){
                    info.setMoney(MoneyAccuracyUtils.formatMoneyByTwo(infotemp.getMoney()));
                    info.setCount(infotemp.getCount());
                    break;
                }
            }
        }
    }
    @OnClick({R.id.title_icon_back, R.id.text_print, R.id.text_submit})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_print:
                if(isPrinting){
                    ToastUtils.sendtoastbyhandler(handler,getString(R.string.printing));
                    return;
                }
                printReport();
                break;
            case R.id.text_submit:
                submitJkd();
                break;
        }
    }

    public void submitJkd(){
        final TotalReportInfo info = new TotalReportInfo();
        ArrayList<ReportDetailInfo> infoList = new ArrayList<ReportDetailInfo>();
        for(ReportDetailInfo data:infos){
            infoList.add(data.clone());
        }
        for (Iterator<ReportDetailInfo> iter = infoList.iterator(); iter.hasNext();) {
            ReportDetailInfo data = (ReportDetailInfo)iter.next();
            if((StringUtil.isEmpty(data.getMoney()) || ArithDouble.parseDouble(data.getMoney()) == 0) && ((StringUtil.isEmpty(data.getCount()))|| ArithDouble.parseInt(data.getCount()) == 0)){
                iter.remove();
            }else{
                if(StringUtil.isEmpty(data.getMoney())){
                    data.setMoney("0");
                }
                if(StringUtil.isEmpty(data.getCount())){
                    data.setCount("0");
                }
            }
        }

        if(infoList.size() ==0){
            ToastUtils.sendtoastbyhandler(handler, "数据为空,不能提交");
            return;
        }
        info.setTotallist(infoList);
        info.setBillcount(tv_count_total.getText().toString());
        info.setTotalmoney(tv_money_total.getText().toString());
        Map<String, String> map = new HashMap<String, String>();
        map.put("personid", SpSaveUtils.read(DemandNoteActivity.this, ConstantData.CASHIER_ID, ""));
        try {
            map.put("payments", GsonUtil.beanToJson(info));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().jkd(map, BaseResult.class, new HttpActionHandle<BaseResult>(){

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
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    ToastUtils.sendtoastbyhandler(handler, "提交成功");
                    if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                        if(latticePrinter == null){
                            ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                            DemandNoteActivity.this.finish();
                            return;
                        }
                        PrepareReceiptInfo.printDemandNote(info, latticePrinter);
                    }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message msg1 = new Message();
                                msg1.what = printStart;
                                handler.sendMessage(msg1);
                                try {
                                    iPrinterService.registerPrintCallback(callback);
                                    // 0：正常 -1：缺纸 -2：未合盖 -3：卡纸 -4 初始化异常 -100：其他故障
                                    // -999：不支持该功能（可以不支持）
                                    iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printDemandNote(info, latticePrinter)));
                                } catch (Exception e) {
                                    Message msg2 = new Message();
                                    msg2.what = printError;
                                    msg2.arg1 = -100;
                                    handler.sendMessage(msg2);
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    DemandNoteActivity.this.finish();
                }else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

            @Override
            public void handleActionOffLine() {
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_waring));
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(DemandNoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        } );
    }

    public void printReport(){
        final TotalReportInfo info = new TotalReportInfo();
        ArrayList<ReportDetailInfo> infoList = new ArrayList<ReportDetailInfo>();
        for(ReportDetailInfo data:infos){
            infoList.add(data.clone());
        }
        for (Iterator<ReportDetailInfo> iter = infoList.iterator(); iter.hasNext();) {
            ReportDetailInfo data = (ReportDetailInfo)iter.next();
            if((StringUtil.isEmpty(data.getMoney()) || ArithDouble.parseDouble(data.getMoney()) == 0) && ((StringUtil.isEmpty(data.getCount()))|| ArithDouble.parseInt(data.getCount()) == 0)){
                iter.remove();
            }else{
                if(StringUtil.isEmpty(data.getMoney())){
                    data.setMoney("0");
                }
                if(StringUtil.isEmpty(data.getCount())){
                    data.setCount("0");
                }
            }
        }
        if(infoList.size() ==0){
            ToastUtils.sendtoastbyhandler(handler, "数据为空,不能打印");
            return;
        }
        info.setTotallist(infoList);
        info.setBillcount(tv_count_total.getText().toString());
        info.setTotalmoney(tv_money_total.getText().toString());
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            if(latticePrinter == null){
                ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                return;
            }
            latticePrinter.setOnEventListener(new IPrint.OnEventListener() {

                @Override
                public void onEvent(final int what, String in) {
                    if(!StringUtil.isEmpty(PrepareReceiptInfo.getPrintErrorInfo(what, in)))
                        ToastUtils.sendtoastbyhandler(handler, PrepareReceiptInfo.getPrintErrorInfo(what, in));
                }
            });
            PrepareReceiptInfo.printDemandNote(info, latticePrinter);
        }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg1 = new Message();
                    msg1.what = printStart;
                    handler.sendMessage(msg1);
                    try {
                        iPrinterService.registerPrintCallback(callback);
                        // 0：正常 -1：缺纸 -2：未合盖 -3：卡纸 -4 初始化异常 -100：其他故障
                        // -999：不支持该功能（可以不支持）
                        iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printDemandNote(info, latticePrinter)));
                    } catch (Exception e) {
                        Message msg2 = new Message();
                        msg2.what = printError;
                        msg2.arg1 = -100;
                        handler.sendMessage(msg2);
                        e.printStackTrace();
                    }
                }
            }).start();
        }else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
            try {
                BaseSystemManager.getInstance().deviceServiceLogin(
                        DemandNoteActivity.this, null, "99999998",//设备ID，生产找后台配置
                        new OnServiceStatusListener() {
                            @Override
                            public void onStatus(int arg0) {//arg0可见ServiceResult.java
                                if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
                                    MyApplication.isPrint = true;
                                    PrepareReceiptInfo.printDemandNote(info, latticePrinter);
                                }else{
                                    ToastUtils.sendtoastbyhandler(handler, "打印登录失败");
                                }
                            }
                        });
            } catch (SdkException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    class MyPagerAdapter extends PagerAdapter {
        public Context mContext;
        public LayoutInflater mLayoutInflater;
        public List<View> views;
        public ListView listview_collection, listview_return, listview_total;
        public ReportTableAdapter collection, returns, total;
        public TextView text_return_count_total, text_return_money_total, text_collection_money_total, text_collection_count_total, text_count_total, text_money_total;
        public MyPagerAdapter(Context context){
            super();
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            initView();
        }

        public void refreshData(SaleReportInfo sale,RefundReportInfo refund, TotalReportInfo total){
            this.collection.refreshData(sale.getSalelist());
            this.returns.refreshData(refund.getRefundlist());
            this.total.refreshData(total.getTotallist());
            if(sale != null){
                text_collection_money_total.setText(sale.getTotalmoney());
                text_collection_count_total.setText(sale.getBillcount());
            }
            if(refund != null){
                text_return_count_total.setText(refund.getBillcount());
                text_return_money_total.setText(refund.getTotalmoney());
            }
            if(total != null){
                text_money_total.setText(total.getTotalmoney());
                text_count_total.setText(total.getBillcount());
            }
        }
        public void initView() {
            views = new ArrayList<View>();
            View v1 = mLayoutInflater.inflate(R.layout.view_collect, null);
            listview_collection = ButterKnife.findById(v1, R.id.listview);
            collection = new ReportTableAdapter(getApplicationContext(),new ArrayList<ReportDetailInfo>());
            listview_collection.setAdapter(collection);
            text_collection_money_total = ButterKnife.findById(v1, R.id.text_collection_money_total);
            text_collection_count_total = ButterKnife.findById(v1, R.id.text_collection_count_total);
            views.add(v1);
            View v2 = mLayoutInflater.inflate(R.layout.view_return, null);
            listview_return = ButterKnife.findById(v2, R.id.listview);
            returns = new ReportTableAdapter(getApplicationContext(),new ArrayList<ReportDetailInfo>());
            listview_return.setAdapter(returns);
            text_return_count_total = ButterKnife.findById(v2, R.id.text_return_count_total);
            text_return_money_total = ButterKnife.findById(v2, R.id.text_return_money_total);
            views.add(v2);
            View v3 = mLayoutInflater.inflate(R.layout.view_total, null);
            listview_total = ButterKnife.findById(v3, R.id.listview);
            total = new ReportTableAdapter(getApplicationContext(),new ArrayList<ReportDetailInfo>());
            listview_total.setAdapter(total);
            text_count_total = ButterKnife.findById(v3, R.id.text_count_total);
            text_money_total = ButterKnife.findById(v3, R.id.text_money_total);
            views.add(v3);
        }
        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    class MoneyDataObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            double sum = 0;
            int count = 0;
            for(ReportDetailInfo info:infos){
                sum = ArithDouble.add(sum, ArithDouble.parseDouble(info.getMoney()));
                count = count+ ArithDouble.parseInt(info.getCount());
            }
            tv_money_total.setText(MoneyAccuracyUtils.getmoneybytwo(sum));
            tv_count_total.setText(count+"");
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    }
}
