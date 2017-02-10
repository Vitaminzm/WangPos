package com.symboltech.wangpos.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.PaymentAdapter;
import com.symboltech.wangpos.adapter.ReturnReasonAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.AlipayAndWeixinPayReturnDialog;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.InputDialog;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.interfaces.OnReturnFinishListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.RefundReasonInfo;
import com.symboltech.wangpos.msg.entity.ReturnEntity;
import com.symboltech.wangpos.msg.entity.WposBankRefundInfo;
import com.symboltech.wangpos.print.PrepareReceiptInfo;
import com.symboltech.wangpos.result.SaveOrderResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.CashierSign;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.service.aidl.IPrintCallback;
import cn.koolcloud.engine.service.aidl.IPrinterService;
import cn.koolcloud.engine.service.aidlbean.ApmpRequest;
import cn.koolcloud.engine.service.aidlbean.IMessage;
import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

public class ReturnMoneyByNormalActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnTouchListener {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.text_return_total_money)
    TextView text_return_total_money;
    @Bind(R.id.text_edit)
    TextView text_edit;
    @Bind(R.id.image_add)
    ImageView image_add;
    @Bind(R.id.ll_add_return_payInfo)
    LinearLayout ll_add_return_payInfo;

    @Bind(R.id.edit_input_reason)
    TextView edit_input_reason;
    @Bind(R.id.imageview_drop_arrow)
    ImageView imageview_drop_arrow;
    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;

    private View reasonPop, stylePop;
    private ListView reasonList, styleList;
    private PopupWindow PopupWindowReason, PopupWindowStyle;
    private List<RefundReasonInfo> reasons;
    private LayoutInflater inflater;
    private BillInfo billInfo;
    /** paymentInfos-系统支付方式类型
     *  payments-用于提交的付款类型
     */
    private List<PayMentsInfo> paymentInfos, payments = new ArrayList<>();
    private PaymentAdapter paymentAdapter;
    private boolean isEdit = false;

    private TextView currentView;
    /**
     * bankStyle-银行卡支付
     * alipayStyle-支付宝支付
     * weixinStyle-微信支付
     * cashStyle-现金支付
     */
    private List<ReturnEntity> bankStyle, alipayStyle, weixinStyle, cashStyle;
    /**
     * allowanceBankStyle-银行卡补录
     * allowanceAlipayStyle-支付宝补录
     * allowanceWeixinStyle-微信补录
     */
    private List<BigDecimal> allowanceBankStyle, allowanceAlipayStyle, allowanceWeixinStyle;
    private int bankFlag = 0, alipayFlag = 0, weixinFlag = 0, cashFlag,
            allowanceBankFlag = 0, allowanceAlipayFlag = 0, allowanceWeixinFlag = 0;

    /** 支付名称-支付id */
    private Map<String, String> nameIds = new HashMap<>();
    /** 支付id-支付类型 */
    private Map<String, String> idTypes = new HashMap<>();
    /** 支付id - 支付名称*/
    private Map<String, String> idNames = new HashMap<>();
    /** 支付名称-支付类型 */
    private Map<String, String> nameTypes = new HashMap<>();
    /** 退货原因-原因id */
    private Map<String, String> reasonIds = new HashMap<>();
    private double totalReturnMoney;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getAdapter() instanceof ReturnReasonAdapter) {
            try {
                edit_input_reason.setText(reasons.get(position).getName());
                PopupWindowReason.dismiss();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }else if (parent.getAdapter() instanceof PaymentAdapter) {
            currentView.setText(((TextView) ((ViewGroup) view).getChildAt(0)).getText().toString());
            PopupWindowStyle.dismiss();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN)
            showReason(edit_input_reason);
        return false;
    }

    protected static final int printStart = 0;
    protected static final int printEnd = 1;
    protected static final int printError = 2;
    /** refresh UI By handler */
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
                case printStart:
                    break;
                case printEnd:
                    break;
                case printError:
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
        billInfo = (BillInfo) getIntent().getSerializableExtra(ConstantData.BILL);
        initReasonView();
        initStyleView();
        initids();
        totalReturnMoney = Math.abs(ArithDouble.parseDouble(billInfo.getTotalmoney()));
        title_text_content.setText(getString(R.string.return_order_info));
        edit_input_reason.setOnTouchListener(this);
        imageview_drop_arrow.setOnTouchListener(this);
        text_return_total_money.setText(totalReturnMoney + "");
        if (reasons != null && reasons.size() > 0) {
            edit_input_reason.setText(reasons.get(0).getName());
        }else {
            edit_input_reason.setText(R.string.warning_no);
        }
        addReturnInfo(true);
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            try {
                // 设备可能没有打印机，open会抛异常
                latticePrinter = WeiposImpl.as().openLatticePrinter();
            } catch (Exception e) {
                // TODO: handle exception
            }
            if(latticePrinter != null){
                latticePrinter.setOnEventListener(new IPrint.OnEventListener() {

                    @Override
                    public void onEvent(final int what, String in) {
                        if(!StringUtil.isEmpty(PrepareReceiptInfo.getPrintErrorInfo(what, in)))
                            ToastUtils.sendtoastbyhandler(handler, PrepareReceiptInfo.getPrintErrorInfo(what, in));
                    }
                });
            }
        }else {
            Intent printService = new Intent(IPrinterService.class.getName());
            printService = AndroidUtils.getExplicitIntent(this, printService);
            if (printService != null)
                bindService(printService, printerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_return_money_by_normal);
        mContext = ReturnMoneyByNormalActivity.this;
        inflater = LayoutInflater.from(mContext);
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

    @OnClick({R.id.title_icon_back, R.id.text_edit, R.id.image_add, R.id.text_submit_return_order})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_edit:
                if(text_edit.getText().toString().equals(getString(R.string.edit))){
                    text_edit.setText(R.string.done);
                    image_add.setVisibility(View.VISIBLE);
                    isEdit = true;
                    showStyleIcon(isEdit);
                }else{
                    text_edit.setText(R.string.edit);
                    image_add.setVisibility(View.INVISIBLE);
                    isEdit = false;
                    showStyleIcon(isEdit);
                }
                break;
            case R.id.image_add:
                    addReturnInfo(false);
                break;
            case R.id.text_submit_return_order:
                    saveOrder();
                break;
        }
    }

    /**
     * 获取退款渠道对应id
     * typeIds 类型-id
     * nameTypes 名称 -id
     * reasonIds 退货原因-id
     */
    private void initids() {
        if(paymentInfos != null && paymentInfos.size() != 0) {
            for (PayMentsInfo info : paymentInfos) {
                idTypes.put(info.getId(), info.getType());
                nameIds.put(info.getName(), info.getId());
                idNames.put(info.getId(), info.getName());
                nameTypes.put(info.getName(), info.getType());
            }
        }
        if(reasons != null && reasons.size() != 0) {
            for (RefundReasonInfo info : reasons) {
                reasonIds.put(info.getName(), info.getId());
            }
        }
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
    }
    /**
     * 显示退货原因
     * @param v 显示位置相对于v
     */
    private void showReason(View v) {
        if(reasons!= null && reasons.size() > 0){
            if (null == PopupWindowReason) {
                PopupWindowReason = new PopupWindow(reasonPop, (int)getResources().getDimension(R.dimen.height_tnz), (int)getResources().getDimension(R.dimen.height_tzz), true);
                PopupWindowReason.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
                PopupWindowReason.setAnimationStyle(R.style.PopupAnimation);
            }
            PopupWindowReason.showAsDropDown(v);
        }else{
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.warning_no_return_reason));
        }
    }

    /**
     * 初始化退款渠道
     */
    private void initStyleView() {
        stylePop = inflater.inflate(R.layout.pop_list, null);
        styleList = (ListView) stylePop.findViewById(R.id.pop_list);
        styleList.setOnItemClickListener(this);
        paymentInfos = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
        Collections.sort(paymentInfos, new Comparator<PayMentsInfo>() {

            @Override
            public int compare(PayMentsInfo lhs, PayMentsInfo rhs) {
                return lhs.getType().compareTo(rhs.getType());
            }

        });
        List<PayMentsInfo> visiblePayments = new ArrayList<>();
        if(paymentInfos != null && paymentInfos.size() != 0) {
            for (PayMentsInfo pay : paymentInfos) {
                if(PaymentTypeEnum.isPaymentstyle(pay.getType())){
                    //过滤类型大于100的收款方式 以及优惠券方式
                    if(Integer.parseInt(pay.getType()) > 100 || pay.getType().equals(PaymentTypeEnum.COUPON.getStyletype())){
                        continue;
                    }
                    if(AppConfigFile.isOffLineMode()) {
                        /**保证现金类收款方式 */
                        if(PaymentTypeEnum.CASH.getStyletype().equals(pay.getType())) {
                            visiblePayments.add(pay);
                        }
                    }else {
                        visiblePayments.add(pay);
                    }
                }
            }
        }
        paymentAdapter = new PaymentAdapter(mContext, visiblePayments);
        styleList.setAdapter(paymentAdapter);
    }
    /**
     * 显示退款方式
     * @param v 显示位置相对于v
     */
    private void showStyle(View v) {
        if (null == PopupWindowStyle) {
            PopupWindowStyle = new PopupWindow(stylePop, (int)getResources().getDimension(R.dimen.height_ofz), (int)getResources().getDimension(R.dimen.height_osz), true);
            PopupWindowStyle.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
            PopupWindowStyle.setAnimationStyle(R.style.PopupAnimation);
        }
        PopupWindowStyle.showAsDropDown(v);
    }

    /**
     * 添加退款信息
     */
    private void addReturnInfo(boolean isFirst) {
        final View view = inflater.inflate(R.layout.item_pay_info, null);
        final TextView style = (TextView) view.findViewById(R.id.text_pay_type);
        final EditText money = (EditText) view.findViewById(R.id.edit_pay_money);
        new HorizontalKeyBoard(this, this, money,ll_keyboard);
        ImageView icon = (ImageView) view.findViewById(R.id.imageview_opt);
        if (paymentInfos != null && paymentInfos.size() > 0) {
            style.setText(paymentInfos.get(0).getName());
        }else {
            style.setText("无");
        }
         if(isFirst) {
             money.setText(totalReturnMoney+"");
         }
        if(isEdit) {
            icon.setVisibility(View.VISIBLE);
        }else {
            icon.setVisibility(View.INVISIBLE);
        }
        style.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentView = style;
                showStyle(style);
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ll_add_return_payInfo.removeView(view);
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 0);
        ll_add_return_payInfo.addView(view, params);
    }

    /**
     * 显示编辑图标
     * @param flag true显示  false 不显示
     */
    private void showStyleIcon(boolean flag) {
        for (int i = 0; i < ll_add_return_payInfo.getChildCount(); i++) {
            ImageView icon = (ImageView) ((LinearLayout) ll_add_return_payInfo.getChildAt(i)).getChildAt(5);
            if(flag) {
                icon.setVisibility(View.VISIBLE);
            }else {
                icon.setVisibility(View.INVISIBLE);
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
     * 获取退款方式
     */
    private boolean getReturnMoneyInfo() {
        resetStyle();
        for (int i = 0; i < ll_add_return_payInfo.getChildCount(); i++) {
            String type = null;
            // 得到支付名字
            String payName = ((TextView) ((LinearLayout)((LinearLayout) ll_add_return_payInfo.getChildAt(i)).getChildAt(1)).getChildAt(0)).getText().toString();
            String value = ((EditText) (((LinearLayout)((LinearLayout) ll_add_return_payInfo.getChildAt(i)).getChildAt(3)).getChildAt(0))).getText().toString();
            BigDecimal decimal;
            try {
                decimal = new BigDecimal(value);
                if(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue() == 0) {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_return_moeny_null));
                    return false;
                }
            } catch (Exception e) {
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_return_moeny_right_err));
                return false;
            }
            // 查看是否有这种支付类型
            if (nameTypes.containsKey(payName)) {
                type = nameTypes.get(payName);
            }
            if (type != null) {
                if (PaymentTypeEnum.BANK.getStyletype().equals(type)) {
                    if (bankStyle == null) {
                        bankStyle = new ArrayList<>();
                    }
                    bankStyle.add(new ReturnEntity(nameIds.get(payName), decimal));
                } else if (PaymentTypeEnum.ALIPAY.getStyletype().equals(type)) {
                    if (alipayStyle == null) {
                        alipayStyle = new ArrayList<>();
                    }
                    alipayStyle.add(new ReturnEntity(nameIds.get(payName), decimal));
                } else if (PaymentTypeEnum.WECHAT.getStyletype().equals(type)) {
                    if (weixinStyle == null) {
                        weixinStyle = new ArrayList<>();
                    }
                    weixinStyle.add(new ReturnEntity(nameIds.get(payName), decimal));
                } else if (PaymentTypeEnum.CASH.getStyletype().equals(type)) {
                    if(cashStyle == null) {
                        cashStyle = new ArrayList<>();
                    }
                    cashStyle.add(new ReturnEntity(nameIds.get(payName), decimal));
                } else if (PaymentTypeEnum.HANDRECORDED.getStyletype().equals(type)) {
                    if (allowanceBankStyle == null) {
                        allowanceBankStyle = new ArrayList<>();
                    }
                    allowanceBankStyle.add(decimal);
                } else if (PaymentTypeEnum.ALIPAYRECORDED.getStyletype().equals(type)) {
                    if (allowanceAlipayStyle == null) {
                        allowanceAlipayStyle = new ArrayList<>();
                    }
                    allowanceAlipayStyle.add(decimal);
                } else if (PaymentTypeEnum.WECHATRECORDED.getStyletype().equals(type)) {
                    if (allowanceWeixinStyle == null) {
                        allowanceWeixinStyle = new ArrayList<>();
                    }
                    allowanceWeixinStyle.add(decimal);
                } else {
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.cannot_support) + payName + getString(R.string.return_msg));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检测金额匹配
     *
     * @return
     */
    private boolean checkMoney() {
        BigDecimal total = new BigDecimal(0);
        total = calculateMoney(bankStyle, total);
        total = calculateMoney(alipayStyle, total);
        total = calculateMoney(weixinStyle, total);
        total = calculateMoney(cashStyle, total);
        total = calculateMoney(total, allowanceBankStyle);
        total = calculateMoney(total, allowanceAlipayStyle);
        total = calculateMoney(total, allowanceWeixinStyle);
        BigDecimal totalMoney = new BigDecimal(totalReturnMoney);
        return totalMoney.setScale(2, RoundingMode.HALF_UP).doubleValue() == total.setScale(2, RoundingMode.HALF_UP).doubleValue() ? true : false;
    }

    /**
     * 计算退款金额
     * @param entitys
     * @param total
     * @return
     */
    private BigDecimal calculateMoney(List<ReturnEntity> entitys, BigDecimal total) {
        if (entitys != null) {
            for (ReturnEntity entity : entitys) {
                total = total.add(entity.getMoney());
            }
        }
        return total;
    }

    /**
     * 计算退款金额
     *
     * @param total
     *            当前总金额
     * @param style
     *            退款方式
     * @return 当前总金额
     */
    private BigDecimal calculateMoney(BigDecimal total, List<BigDecimal> style) {
        if (style != null) {
            for (BigDecimal value : style) {
                total = total.add(value);
            }
        }
        return total;
    }
    /**
     * 提交退货单
     */
    private void saveOrder() {
        if(reasons!= null && reasons.size() > 0){
            if(edit_input_reason.getText().toString().equals(getString(R.string.warning_no))){
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.please_select_return_reason));
                return;
            }
            if(text_edit.getText().toString().equals(getString(R.string.done))){
                ToastUtils.sendtoastbyhandler(handler, "请先完成支付方式编辑");
                return;
            }
            if (!getReturnMoneyInfo())
                return;
            if (checkMoney()) {
                payments.clear();
                returnMoney();
            } else {
                ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_return_moeny_mate_err));
            }
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
            final ReturnEntity entity = bankStyle.get(bankFlag);
            new InputDialog(this, "银行卡退款", new GeneralEditListener(){
                @Override
                public void editinput(String edit) {
                    if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                        requestCashier(edit);
                    }else{
                        double money = getDoubleMoney(entity.getMoney());
                        putPayments(getPayTypeId(PaymentTypeEnum.HANDRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.HANDRECORDED)), PaymentTypeEnum.HANDRECORDED.getStyletype(), "-" + money);
                        bankFlag += 1;
                        if (bankFlag < bankStyle.size()) {
                            returnBank();
                        } else {
                            returnAlipay();
                        }
                    }
                }
            }).show();
        } else {
            returnAlipay();
        }
    }

    private void putPayments(String id, String name, String type, String money) {
        PayMentsInfo info = new PayMentsInfo();
        info.setId(id);
        info.setName(name);
        info.setType(type);
        info.setMoney(money);
        info.setOverage("0");
        payments.add(info);
    }

    /**
     * 获取bigdecimal处理后的金钱
     * @param decimal
     * @return
     */
    private double getDoubleMoney(BigDecimal decimal) {
        if(decimal != null) {
            return decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0;
    }

    /**
     * 退支付宝
     *
     */
    private void returnAlipay() {
        if (alipayStyle != null && alipayFlag < alipayStyle.size()) {
            final String id = alipayStyle.get(alipayFlag).getId();
            final double money = getDoubleMoney(alipayStyle.get(alipayFlag).getMoney());
            new AlipayAndWeixinPayReturnDialog(this, ConstantData.PAYMODE_BY_ALIPAY + "", null, AppConfigFile.getBillId(),
                    money + "", new OnReturnFinishListener() {

                @Override
                public void finish(boolean isSuccess) {
                    alipayFlag += 1;
                    if (isSuccess) {
                        putPayments(id, idNames.get(id), PaymentTypeEnum.ALIPAY.getStyletype(), "-" + money);
                    } else {
                        putPayments(getPayTypeId(PaymentTypeEnum.ALIPAYRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.ALIPAYRECORDED)),
                                PaymentTypeEnum.ALIPAYRECORDED.getStyletype(), "-" + money);
                    }
                    if (alipayFlag < alipayStyle.size()) {
                        returnAlipay();
                    } else {
                        returnWeiXinPay();
                    }
                }
            }).show();
        } else {
            returnWeiXinPay();
        }
    }

    /**
     * 退微钱包
     */
    private void returnWeiXinPay() {
        if (weixinStyle != null && weixinFlag < weixinStyle.size()) {
            final String id = weixinStyle.get(weixinFlag).getId();
            final double money = getDoubleMoney(weixinStyle.get(weixinFlag).getMoney());
            new AlipayAndWeixinPayReturnDialog(this, ConstantData.PAYMODE_BY_WEIXIN + "", null, AppConfigFile.getBillId(), money + "",
                    new OnReturnFinishListener() {

                        @Override
                        public void finish(boolean isSuccess) {
                            weixinFlag += 1;
                            if (isSuccess) {
                                putPayments(id, idNames.get(id), PaymentTypeEnum.WECHAT.getStyletype(), "-" + money);
                            } else {
                                putPayments(getPayTypeId(PaymentTypeEnum.WECHATRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.WECHATRECORDED)),
                                        PaymentTypeEnum.WECHATRECORDED.getStyletype(), "-" + money);
                            }
                            if (weixinFlag < weixinStyle.size()) {
                                returnWeiXinPay();
                            } else {
                                returnAllowanceBank();
                            }
                        }
                    }).show();
        } else {
            returnAllowanceBank();
        }
    }

    /**
     * 手工补录
     */
    private void returnAllowanceBank() {
        if (allowanceBankStyle != null && allowanceBankFlag < allowanceBankStyle.size()) {
            double money = getDoubleMoney(allowanceBankStyle.get(allowanceBankFlag));
            putPayments(getPayTypeId(PaymentTypeEnum.HANDRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.HANDRECORDED)), PaymentTypeEnum.HANDRECORDED.getStyletype(), "-" + money);
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
            double money = getDoubleMoney(allowanceAlipayStyle.get(allowanceAlipayFlag));
            putPayments(getPayTypeId(PaymentTypeEnum.ALIPAYRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.ALIPAYRECORDED)), PaymentTypeEnum.ALIPAYRECORDED.getStyletype(), "-" + money);
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
            double money = getDoubleMoney(allowanceWeixinStyle.get(allowanceWeixinFlag));
            putPayments(getPayTypeId(PaymentTypeEnum.WECHATRECORDED), idNames.get(getPayTypeId(PaymentTypeEnum.WECHATRECORDED)), PaymentTypeEnum.WECHATRECORDED.getStyletype(), "-" + money);
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
            ReturnEntity entity = cashStyle.get(cashFlag);
            String id = entity.getId();
            double money = getDoubleMoney(entity.getMoney());
            putPayments(id, idNames.get(id), PaymentTypeEnum.CASH.getStyletype(), "-" + money);
            cashFlag += 1;
            if(cashFlag < cashStyle.size()) {
                returnCash();
            }else {
                commitOrder();
            }
        }else {
            commitOrder();
        }
    }

    /**
     * 提交退货单
     *
     */
    private void commitOrder() {
        BillInfo bill = new BillInfo();
        bill.setBillid(AppConfigFile.getBillId());
        bill.setBackreason(reasonIds.get(edit_input_reason.getText().toString()));
        bill.setPaymentslist(payments);
        billInfo.setRealmoney(totalReturnMoney+"");
        billInfo.setCashiername(SpSaveUtils.read(getApplicationContext(), ConstantData.CASHIER_NAME, ""));
        billInfo.setBackreason(reasonIds.get(edit_input_reason.getText().toString()));
        Map<String, String> map = new HashMap<>();
        try {
            map.put("billInfo", GsonUtil.beanToJson(bill));
            LogUtil.v("lgs", GsonUtil.beanToJson(bill));
        } catch (Exception e) {
            LogUtil.v("lgs", "订单转换失败");
            e.printStackTrace();
        }
        HttpRequestUtil.getinstance().saveReturnOrder(HTTP_TASK_KEY, map, SaveOrderResult.class,
                new HttpActionHandle<SaveOrderResult>() {

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
                            billInfo.setBillid(AppConfigFile.getBillId());
                            billInfo.setPaymentslist(payments);
                            billInfo.setAwardpoint(result.getSaveOrderInfo().getGainpoint());
                            printBackByorder(billInfo);
                            AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                            AppConfigFile.setBillId(result.getSaveOrderInfo().getBillid());
                            Intent intent = new Intent(mContext, ReturnGoodSucceedActivity.class);
                            intent.putExtra(ConstantData.BILL, billInfo);
                            startActivity(intent);
                        }else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }

                    @Override
                    public void startChangeMode() {
                        final HttpActionHandle httpActionHandle = this;
                        ReturnMoneyByNormalActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangeModeDialog(ReturnMoneyByNormalActivity.this, httpActionHandle).show();
                            }
                        });
                    }

                    @Override
                    public void handleActionChangeToOffLine() {
                        AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                        AppConfigFile.setBillId(String.valueOf(Long.parseLong(AppConfigFile.getBillId()) + 1));
                        Intent intent = new Intent(mContext, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void handleActionOffLine() {
                        OrderInfoDao dao = new OrderInfoDao(mContext);
                        if(dao.addOrderPaytypeinfo(AppConfigFile.getBillId(), null, null, reasonIds.get(edit_input_reason.getText().toString()), 0, "1", SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""), payments)) {
                            billInfo.setPaymentslist(payments);
                            if(billInfo.getMember() == null){
                                printBackByorder(billInfo);
                            }
                            AppConfigFile.setLast_billid(AppConfigFile.getBillId());
                            AppConfigFile.setBillId(String.valueOf(Long.parseLong(AppConfigFile.getBillId()) + 1));
                            Intent intent = new Intent(mContext, ReturnGoodSucceedActivity.class);
                            intent.putExtra(ConstantData.BILL, billInfo);
                            startActivity(intent);
                        }else {
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_save_order_failed));
                        }
                    }
                });
    }

    public void printBackByorder(final BillInfo billinfo){
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            if(latticePrinter == null){
                ToastUtils.sendtoastbyhandler(handler, "尚未初始化点阵打印sdk，请稍后再试");
                return;
            }
            PrepareReceiptInfo.printBackOrderList(billinfo, false, latticePrinter);
        }else {
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
                        iPrinterService.printPage(new ApmpRequest(PrepareReceiptInfo.printBackOrderList(billinfo, false, latticePrinter)));
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
    }

    private BizServiceInvoker mBizServiceInvoker;

    // 1.执行调用之前需要调用WeiposImpl.as().init()方法，保证sdk初始化成功。
    //
    // 2.调用收银支付成功后，收银支付结果页面完成后，BizServiceInvoker.OnResponseListener后收到响应的结果
    //
    // 3.如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
    private void innerRequestCashier(String tradeNo) {
        String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
        try {
            RequestInvoke cashierReq = new RequestInvoke();
            cashierReq.pkgName = this.getPackageName();
            cashierReq.sdCode = CashierSign.Cashier_sdCode;// 收银服务的sdcode信息
            cashierReq.bpId = AppConfigFile.InvokeCashier_BPID;
            cashierReq.launchType = CashierSign.launchType;

            cashierReq.params = CashierSign.refundsign(AppConfigFile.InvokeCashier_BPID, AppConfigFile.InvokeCashier_KEY, tradeNo);
            cashierReq.seqNo = seqNo;

            RequestResult r = mBizServiceInvoker.request(cashierReq);
            LogUtil.i("lgs", r.token + "," + r.seqNo + "," + r.result);
            // 发送调用请求
            if (r != null) {
                LogUtil.i("lgs", "request result:" + r.result + "|launchType:" + cashierReq.launchType);
                String err = null;
                switch (r.result) {
                    case BizServiceInvoker.REQ_SUCCESS: {
                        // 调用成功
                        //ToastUtils.sendtoastbyhandler(handler, "收银服务调用成功");
                        break;
                    }
                    case BizServiceInvoker.REQ_ERR_INVAILD_PARAM: {
                        ToastUtils.sendtoastbyhandler(handler, "请求参数错误！");
                        break;
                    }
                    case BizServiceInvoker.REQ_ERR_NO_BP: {
                        ToastUtils.sendtoastbyhandler(handler, "未知的合作伙伴！");
                        break;
                    }
                    case BizServiceInvoker.REQ_ERR_NO_SERVICE: {
                        //调用结果返回，没有订阅对应bp账号中的收银服务，则去调用sdk主动订阅收银服务
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                ToastUtils.sendtoastbyhandler(handler, "正在申请订阅收银服务...");
                                // 如果没有订阅，则主动请求订阅服务
                                mBizServiceInvoker.subscribeService(CashierSign.Cashier_sdCode,
                                        AppConfigFile.InvokeCashier_BPID);
                            }
                        });
                        break;
                    }
                    case BizServiceInvoker.REQ_NONE: {
                        ToastUtils.sendtoastbyhandler(handler, "请求未知错误！");
                        break;
                    }
                }
                if (err != null) {
                    LogUtil.i("lgs", "serviceInvoker request err:" + err);
                }
            }else{
                ToastUtils.sendtoastbyhandler(handler, "请求结果对象为空！");
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这个是服务调用完成后的响应监听方法
     */
    private BizServiceInvoker.OnResponseListener mOnResponseListener = new BizServiceInvoker.OnResponseListener() {

        @Override
        public void onResponse(String sdCode, String token, byte[] data) {
            // 收银服务调用完成后的返回方法
            String result = new String(data);
            WposBankRefundInfo info = null;
            try {
                info = GsonUtil.jsonToBean(result, WposBankRefundInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtil.i("lgs",
                    "sdCode = " + sdCode + " , token = " + token + " , data = " + new String(data));
            final ReturnEntity entity = bankStyle.get(bankFlag);
            if(info != null){
                if(info.getErrCode().equals("0")){
                    putPayments(entity.getId(), idNames.get(entity.getId()), idTypes.get(entity.getId()), "-" + getDoubleMoney(entity.getMoney()));
                    OrderBean orderBean= new OrderBean();
                    orderBean.setAccountNo(CurrencyUnit.yuan2fenStr(getDoubleMoney(entity.getMoney())+""));
                    orderBean.setTxnId(info.getCashier_trade_no());
                    orderBean.setRefNo(info.getRefund_ref_no());
                    orderBean.setBatchId(info.getRefund_vouch_no());
                    orderBean.setPaymentId(entity.getId());
                    orderBean.setTransType(ConstantData.TRANS_RETURN);
                    orderBean.setTraceId(AppConfigFile.getBillId());
                    Intent serviceintent = new Intent(mContext, RunTimeService.class);
                    serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
                    serviceintent.putExtra(ConstantData.THIRD_DATA, orderBean);
                    startService(serviceintent);
                    bankFlag += 1;
                    if (bankFlag < bankStyle.size()) {
                        returnBank();
                    } else {
                        returnAlipay();
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, info.getErrMsg());
                }
            }else{
                ToastUtils.sendtoastbyhandler(handler, "未知错误");
            }
        }

        @Override
        public void onFinishSubscribeService(boolean result, String err) {
            // TODO Auto-generated method stub
            // 申请订阅收银服务结果返回
            // bp订阅收银服务返回结果
            if (!result) {
                //订阅失败
                ToastUtils.sendtoastbyhandler(handler, err);
            }else{
                //订阅成功
                ToastUtils.sendtoastbyhandler(handler, "订阅收银服务成功，请按home键回调主页刷新订阅数据后重新进入调用收银");
            }
        }
    };
    /**
     * 本地调用收银服务
     */
    private void requestCashier(String tradeNo) {
        if (mBizServiceInvoker == null) {
            try {
                // 初始化服务调用
                mBizServiceInvoker = WeiposImpl.as().getService(BizServiceInvoker.class);
                if (mBizServiceInvoker == null) {
                    ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
                    return;
                }else{
                    // 设置请求订阅服务监听结果的回调方法
                    mBizServiceInvoker.setOnResponseListener(mOnResponseListener);
                    innerRequestCashier(tradeNo);
                }
            } catch (Exception e) {
                ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
            }
        }else{
            innerRequestCashier(tradeNo);
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
}
