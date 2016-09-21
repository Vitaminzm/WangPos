package com.symboltech.wangpos.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.config.InitializeConfig;
import com.symboltech.wangpos.db.dao.LoginDao;
import com.symboltech.wangpos.db.dao.UserNameDao;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.interfaces.OnDrawableClickListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.ConfigList;
import com.symboltech.wangpos.msg.entity.LoginInfo;
import com.symboltech.wangpos.result.LoginResult;
import com.symboltech.wangpos.result.UnLockResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.DButils;
import com.symboltech.wangpos.utils.MD5Utils;
import com.symboltech.wangpos.utils.MachineUtils;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.DrawableEditText;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener, AdapterView.OnItemClickListener {

    @Bind(R.id.edit_username)DrawableEditText edit_username;
    @Bind(R.id.edit_password)EditText edit_password;
    @Bind(R.id.lock_info)LinearLayout lock_info;
    @Bind(R.id.ll_desk_code)LinearLayout ll_desk_code;
    @Bind(R.id.ll_cashier_name)LinearLayout ll_cashier_name;
    @Bind(R.id.cashier_name)TextView cashier_name;
    @Bind(R.id.desk_code)TextView desk_code;

    @Bind(R.id.login)TextView login;
    @Bind(R.id.about)ImageView about;
    @Bind(R.id.loginout)ImageView loginout;

    /** login db dao */
    private UserNameDao und;
    private ListView listview;
    private PopupWindow mPopupWindow;

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
    private boolean iscashier;
    private int loginrole;

    @Override
    protected void initData() {
        und = new UserNameDao(this);
        loginrole = getIntent().getIntExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
        LogUtil.i("lgs", "" + Utils.px2dip(getApplicationContext(), 200));
        LogUtil.i("lgs", "" + Utils.px2dip(getApplicationContext(), 160));
        switchrole();
        if (getIntent().getBooleanExtra(ConstantData.LOGIN_FIRST, false)) {
            //开启服务(上传日志及pos状态监听)
            Intent service = new Intent(getApplicationContext(), RunTimeService.class);
            startService(service);
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_login);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        new HorizontalKeyBoard(this, this, edit_username, edit_password, new KeyBoardListener() {
            @Override
            public void onComfirm() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onValue(String value) {
                if (iscashier) {
                    if(!StringUtil.isEmpty(edit_password.getText().toString())){
                        poslogin();// 登录
                    }
                } else {
                    posunlock();// 解锁
                }
            }
        });
        edit_password.setOnFocusChangeListener(this);
        edit_username.setOnFocusChangeListener(this);
        edit_username.setOnDrawableClickListener(new OnDrawableClickListener() {
            @Override
            public void onDrawableclick() {
                showPopupList();
            }
        });
        login.setOnClickListener(this);
        about.setOnClickListener(this);
        loginout.setOnClickListener(this);

        listview = (ListView) LayoutInflater.from(this).inflate(R.layout.popup_list, null);
        listview.setOnItemClickListener(this);

    }

    /**
     * start by role
     */
    private void switchrole() {
        String deskCode = SpSaveUtils.read(LoginActivity.this, ConstantData.CASHIER_DESK_CODE, "");
        if(!StringUtil.isEmpty(deskCode)){
            ll_desk_code.setVisibility(View.VISIBLE);
            desk_code.setText(deskCode);
        }
        if (loginrole == ConstantData.LOGIN_WITH_CASHIER) {
            lock_info.setVisibility(View.GONE);
            edit_username.setVisibility(View.VISIBLE);
            iscashier = true;
        } else if (loginrole == ConstantData.LOGIN_WITH_LOCKSCREEN) {
            String cashierName = SpSaveUtils.read(LoginActivity.this, ConstantData.CASHIER_NAME, "");
            if(!StringUtil.isEmpty(cashierName)){
                ll_cashier_name.setVisibility(View.VISIBLE);
                cashier_name.setText(deskCode);
            }
            lock_info.setVisibility(View.VISIBLE);
            edit_username.setVisibility(View.GONE);
            iscashier = false;
        }
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (iscashier) {
                    poslogin();// 登录
                } else {
                    posunlock();// 解锁
                }
                break;
            case R.id.loginout:
                Intent service = new Intent(getApplicationContext(), RunTimeService.class);
                stopService(service);
                this.finish();
                InitializeConfig.clearCash(LoginActivity.this);
                MyApplication.exit();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus){
            ((EditText) v).setHintTextColor(getResources().getColor(R.color.colorPrimary));
        }else{
            ((EditText) v).setHintTextColor(getResources().getColor(R.color.gray));
        }
    }

    /**
     * popup show
     */
    private void showPopupList() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(edit_username.getWindowToken(), 0); // 强制隐藏键盘
        }
        if (DButils.getUserNames(und).size() > 0) {
            if (null == mPopupWindow) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this, R.layout.popup_item,
                        DButils.getUserNames(und));
                listview.setAdapter(adapter);
                mPopupWindow = new PopupWindow(listview, 250, MachineUtils.dip2px(MyApplication.context, 110), true);
                mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
                mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            }
            if(!mPopupWindow.isShowing()){
                int popupWidth = mPopupWindow.getWidth();
                //int popupHeight = mPopupWindow.getHeight();
                int width = edit_username.getWidth();
                //int height = edit_username.getHeight();
                mPopupWindow.showAsDropDown(edit_username, (width - popupWidth) / 2, 0);
            }
        }
    }

    /**
     * unlock controller
     * @param password 密码
     */
    private void unlockforhttp(final String password) {
        Map<String, String> map = new HashMap<>();
        map.put("password", MD5Utils.md5(password));
        HttpRequestUtil.getinstance().unlock(map, UnLockResult.class, new HttpActionHandle<UnLockResult>() {

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
            public void handleActionSuccess(String actionName, UnLockResult result) {
                if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                    Intent service = new Intent(getApplicationContext(), RunTimeService.class);
                    service.putExtra(ConstantData.UPDATE_STATUS, true);
                    service.putExtra(ConstantData.CASHIER_ID, SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""));
                    startService(service);
                    startforcashier();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    /**
     * start cashierActivity
     */
    private void startforcashier() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
    /**
     * login
     */
    private void poslogin() {
        if(StringUtil.isEmpty(edit_username.getText().toString())){
            ToastUtils.sendtoastbyhandler(handler, getResources().getString(R.string.waring_null_username));
        }else if(StringUtil.isEmpty(edit_password.getText().toString())){
            ToastUtils.sendtoastbyhandler(handler, getResources().getString(R.string.waring_null_password));
        }else{
            loginforhttp(edit_username.getText().toString(), edit_password.getText().toString());
        }
    }

    /**
     *
     *解锁
     */
    private void posunlock() {
        if (!StringUtil.isEmpty(this.edit_password.getText().toString())) {
            unlockforhttp(this.edit_password.getText().toString());
        } else {
            ToastUtils.sendtoastbyhandler(handler, getResources().getString(R.string.waring_null_password));
        }
    }


    /**
     * login for http verification
     */
    private void loginforhttp(final String username, final String password) {
        Map<String, String> map = new HashMap<>();
        map.put("machinecode", MachineUtils.getUid(MyApplication.context));
        map.put("personcode", username);
        map.put("password", MD5Utils.md5(password));
        LogUtil.i("lgs", "MachineUtils.getUid==========" + MachineUtils.getUid(MyApplication.context));
        HttpRequestUtil.getinstance().login(map, LoginResult.class, new HttpActionHandle<LoginResult>() {

            @Override
            public void handleActionStart() {
                startwaitdialog();
            }

            @Override
            public void handleActionFinish() {
                closewaitdialog();startforcashier();
            }

            @Override
            public void handleActionError(String actionName, String errmsg) {
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName, LoginResult result) {
                if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                    SpSaveUtils.writeboolean(MyApplication.context, ConstantData.IS_CONFIG_DOWNLOAD, true);
                    und.add(result.getLogininfo().getPersoncode());
                    savelogininfo(result.getLogininfo());
                    Intent serviceintent = new Intent(getApplicationContext(), RunTimeService.class);
                    serviceintent.putExtra(ConstantData.UPLOAD_LOG, true);
                    serviceintent.putExtra(ConstantData.CASHIER_ID, result.getLogininfo().getPerson_id());
                    startService(serviceintent);

                    startforcashier();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    /**
     * save temp
     * @param logininfo 登录信息
     */
    private void savelogininfo(LoginInfo logininfo) {
//        OrderInfoDao dao = new OrderInfoDao(mContext);
//        long billid = Math.max(ArithDouble.parseLong(dao.getMaxbillid()) + 1,
//                 ArithDouble.parseLong(logininfo.getBillid()));
        long billid = ArithDouble.parseLong(logininfo.getBillid());
        MyApplication.setBillId(String.valueOf(billid));
        SpSaveUtils.write(MyApplication.context, ConstantData.CASHIER_DESK_CODE, logininfo.getPosno());
        SpSaveUtils.write(MyApplication.context, ConstantData.RECEIPT_NUMBER, logininfo.getBillid());
        SpSaveUtils.write(MyApplication.context, ConstantData.SHOP_ID, logininfo.getShopinfo().getId());
        SpSaveUtils.write(MyApplication.context, ConstantData.SHOP_CODE, logininfo.getShopinfo().getCode());
        SpSaveUtils.write(MyApplication.context, ConstantData.SHOP_NAME, logininfo.getShopinfo().getName());
        SpSaveUtils.write(MyApplication.context, ConstantData.MALL_ID, logininfo.getMallinfo().getId());
        SpSaveUtils.write(MyApplication.context, ConstantData.MALL_CODE, logininfo.getMallinfo().getCode());
        SpSaveUtils.write(MyApplication.context, ConstantData.MALL_NAME, logininfo.getMallinfo().getName());
        SpSaveUtils.write(MyApplication.context, ConstantData.CASHIER_NAME, logininfo.getPerson_name());
        SpSaveUtils.write(MyApplication.context, ConstantData.CASHIER_ID, logininfo.getPerson_id());
        SpSaveUtils.write(MyApplication.context, ConstantData.CASHIER_CODE, logininfo.getPersoncode());
        SpSaveUtils.write(MyApplication.context, ConstantData.LOGIN_TOKEN, logininfo.getToken());
        if (logininfo.getConfiglists() != null && logininfo.getConfiglists().size() > 0) {
            for (ConfigList cl : logininfo.getConfiglists()) {
                if ("1".equals(cl.getId().trim())) {
                    /** 获取舍零方式 */
                    SpSaveUtils.write(MyApplication.context, ConstantData.MALL_MONEY_OMIT, cl.getValue());
                } else if ("3".equals(cl.getId().trim())) {
                    /** 支付宝是否输入单号 */
                    SpSaveUtils.write(MyApplication.context, ConstantData.MALL_ALIPAY_IS_INPUT, cl.getValue());
                } else if ("4".equals(cl.getId().trim())) {
                    /** 微信是否输入单号 */
                    SpSaveUtils.write(MyApplication.context, ConstantData.MALL_WEIXIN_IS_INPUT, cl.getValue());
                }
            }
        }
        if (logininfo.getUserlists() != null && logininfo.getUserlists().size() > 0) {
            LoginDao logindao = new LoginDao(mContext);
            logindao.add(logininfo.getUserlists());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String defalutStr = DButils.getUserNames(und).get(position);
        if(defalutStr != null && defalutStr.length() > 0){
            edit_username.setText(defalutStr);
            edit_username.setSelection(defalutStr.length());
        }
        mPopupWindow.dismiss();
    }
}
