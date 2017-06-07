package com.symboltech.wangpos.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.utils.MD5Utils;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePasswordActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_desk_code)TextView text_desk_code;
    @Bind(R.id.text_shop)TextView text_shop;
    @Bind(R.id.shop_mall)TextView shop_mall;

    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;

    @Bind(R.id.et_original_passeord)EditText et_original_passeord;
    @Bind(R.id.et_new_passeord)EditText et_new_passeord;
    @Bind(R.id.et_confirm_passeord)EditText et_confirm_passeord;

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
            }
        }
    }
    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
        new HorizontalKeyBoard(this, this, et_confirm_passeord, ll_keyboard);
        new HorizontalKeyBoard(this, this, et_new_passeord, ll_keyboard);
        new HorizontalKeyBoard(this, this, et_original_passeord, ll_keyboard);
        title_text_content.setText(getString(R.string.change_passeord));
        text_desk_code.setText(SpSaveUtils.read(this, ConstantData.CASHIER_NAME, ""));
        text_shop.setText(SpSaveUtils.read(this, ConstantData.CASHIER_CODE, ""));
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_change_password);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm:
                String original_passeord = et_original_passeord.getText().toString();
                if(StringUtil.isEmpty(original_passeord)){
                    ToastUtils.sendtoastbyhandler(handler,"原密码不能为空");
                    return;
                }
                String new_passeord = et_new_passeord.getText().toString();
                if(StringUtil.isEmpty(new_passeord)){
                    ToastUtils.sendtoastbyhandler(handler,"新密码不能为空");
                    return;
                }
                String confirm_passeord = et_confirm_passeord.getText().toString();
                if(StringUtil.isEmpty(confirm_passeord)){
                    ToastUtils.sendtoastbyhandler(handler,"确认密码不能为空");
                    return;
                }
                if(!confirm_passeord.equals(new_passeord)){
                    ToastUtils.sendtoastbyhandler(handler,"确认密码与新密码不一致");
                    return;
                }
                if(!original_passeord.equals(new_passeord)){
                    ToastUtils.sendtoastbyhandler(handler,"原密码与新密码一致，不能提交");
                    return;
                }
                changePasswordbyhttp(original_passeord, new_passeord);
                break;
        }
    }

    private void changePasswordbyhttp(String oldpwd, String newpwd) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("personcode", SpSaveUtils.read(this, ConstantData.CASHIER_CODE, ""));
        map.put("oldpwd", MD5Utils.md5(oldpwd));
        map.put("newpwd", MD5Utils.md5(newpwd));
        HttpRequestUtil.getinstance().repassword(HTTP_TASK_KEY, map, BaseResult.class, new HttpActionHandle<BaseResult>() {

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
                if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                    ToastUtils.sendtoastbyhandler(handler, "密码修改成功");
                    finish();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
    }
}
