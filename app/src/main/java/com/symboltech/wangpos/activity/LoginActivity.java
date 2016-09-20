package com.symboltech.wangpos.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.db.dao.UserNameDao;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.OnDrawableClickListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.ConfigList;
import com.symboltech.wangpos.msg.entity.LoginInfo;
import com.symboltech.wangpos.result.LoginResult;
import com.symboltech.wangpos.result.UnLockResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.DButils;
import com.symboltech.wangpos.utils.MD5Utils;
import com.symboltech.wangpos.utils.MachineUtils;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.DrawableEditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    @Bind(R.id.edit_username)DrawableEditText edit_username;
    @Bind(R.id.edit_password)EditText edit_password;
    @Bind(R.id.lock_info)LinearLayout lock_info;

    @Bind(R.id.login)TextView login;
    @Bind(R.id.about)ImageView about;
    @Bind(R.id.loginout)ImageView loginout;

    /** login db dao */
    private UserNameDao und;
    private ListView listview;
    private View mContainer;
    private PopupWindow mPopupWindow;

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(LoginActivity.this, msg);
                    break;

                case 2:

                    break;

                default:
                    break;
            }
        };
    };
    private boolean iscashier;
    private int loginrole;

    @Override
    protected void initData() {
        und = new UserNameDao(this);
        loginrole = getIntent().getIntExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
        LogUtil.i("lgs", ""+Utils.px2dip(getApplicationContext(),200));
        LogUtil.i("lgs", ""+Utils.px2dip(getApplicationContext(),160));
//        about.setDrawingCacheEnabled(true);
//        Bitmap b = about.getDrawingCache();
//        int w = b.getWidth();
//        int h = b.getHeight();

        switchrole();
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_login);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
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
    }
    /**
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(start by role)
     */
    private void switchrole() {
        // TODO Auto-generated method stub
        if (loginrole == ConstantData.LOGIN_WITH_CASHIER) {
            lock_info.setVisibility(View.GONE);
            edit_username.setVisibility(View.VISIBLE);
            iscashier = true;
        } else if (loginrole == ConstantData.LOGIN_WITH_LOCKSCREEN) {
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
//        LogUtil.i("lgs", "" + w+"-----"+ h);
                LogUtil.i("lgs", "" + about.getWidth()+"-----"+ about.getHeight());
                if (iscashier) {
                    poslogin();// 登录
                } else {
                    posunlock();// 解锁
                }
                break;
            case R.id.loginout:
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
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(popup show)
     */
    private void showPopupList() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(edit_username.getWindowToken(), 0); // 强制隐藏键盘
        }
        if (DButils.getUserNames(und).size() > 0) {
            if (null == mPopupWindow) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this, R.layout.popup_item,
                        DButils.getUserNames(und));
                listview.setAdapter(adapter);
                mPopupWindow = new PopupWindow(listview, 300, MachineUtils.dip2px(MyApplication.context, 180), true);
                mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
                mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            }
            mPopupWindow.showAtLocation(edit_password, Gravity.LEFT | Gravity.TOP,
                    MachineUtils.dip2px(MyApplication.context, 80) + 90,
                    MachineUtils.dip2px(MyApplication.context, 321));
        }
    }

    /**
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(unlock controller)
     * @param password
     */
    private void unlockforhttp(final String password) {
        // TODO Auto-generated method stub
        Map<String, String> map = new HashMap<String, String>();
        map.put("password", MD5Utils.md5(password));
        HttpRequestUtil.getinstance().unlock(map, UnLockResult.class, new HttpActionHandle<UnLockResult>() {

            @Override
            public void handleActionStart() {
                // TODO Auto-generated method stub
                startwaitdialog();
            }

            @Override
            public void handleActionFinish() {
                closewaitdialog();
            }

            @Override
            public void handleActionError(String actionName, String errmsg) {
                // TODO Auto-generated method stub
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName, UnLockResult result) {
                // TODO Auto-generated method stub
                if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                    startforcashier();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    /**
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(start cashierActivity)
     */
    private void startforcashier() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
    /**
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(login)
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
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(解锁)
     */
    private void posunlock() {
        // TODO Auto-generated method stub
        if (!StringUtil.isEmpty(this.edit_password.getText().toString())) {
            unlockforhttp(this.edit_password.getText().toString());
        } else {
            ToastUtils.sendtoastbyhandler(handler, getResources().getString(R.string.waring_null_password));
        }
    }


    /**
     * login for http verification
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(用一句话描述该文件做什么)
     * @param username
     * @param password
     */
    private void loginforhttp(final String username, final String password) {
        // TODO Auto-generated method stub
        Map<String, String> map = new HashMap<String, String>();
        map.put("machinecode", MachineUtils.getUid(MyApplication.context));
        map.put("personcode", username);
        map.put("password", MD5Utils.md5(password));
        LogUtil.i("lgs", "response==========" + MachineUtils.getUid(MyApplication.context));
        HttpRequestUtil.getinstance().login(map, LoginResult.class, new HttpActionHandle<LoginResult>() {

            @Override
            public void handleActionStart() {
                // TODO Auto-generated method stub
                startwaitdialog();
            }

            @Override
            public void handleActionFinish() {
                // TODO Auto-generated method stub
                closewaitdialog();
                startforcashier();
            }

            @Override
            public void handleActionError(String actionName, String errmsg) {
                // TODO Auto-generated method stub
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName, LoginResult result) {
                // TODO Auto-generated method stub
                if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                    SpSaveUtils.writeboolean(MyApplication.context, ConstantData.IS_CONFIG_DOWNLOAD, true);
                    und.add(result.getLogininfo().getPersoncode());
                    savelogininfo(result.getLogininfo());
                    startforcashier();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }

    /**
     * save temp
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(save logininfo)
     * @param logininfo
     */
    private void savelogininfo(LoginInfo logininfo) {
        // TODO Auto-generated method stub
        OrderInfoDao dao = new OrderInfoDao(mContext);
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
        SpSaveUtils.write(MyApplication.context, ConstantData.MALL_MONEY_OMIT, "");
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
}
