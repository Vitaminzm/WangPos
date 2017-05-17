package com.symboltechshop.wangpos.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.app.AppConfigFile;
import com.symboltechshop.wangpos.app.ConstantData;
import com.symboltechshop.wangpos.dialog.ChangeModeDialog;
import com.symboltechshop.wangpos.http.HttpActionHandle;
import com.symboltechshop.wangpos.http.HttpRequestUtil;
import com.symboltechshop.wangpos.msg.entity.MemberInfo;
import com.symboltechshop.wangpos.result.BaseResult;
import com.symboltechshop.wangpos.utils.ToastUtils;
import com.symboltechshop.wangpos.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemberActivateActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.text_send_code)
    TextView text_send_code;

    @Bind(R.id.edit_input_code)
    EditText edit_input_code;
    @Bind(R.id.edit_input_phone)
    EditText edit_input_phone;
    @Bind(R.id.edit_input_shenfen)
    EditText edit_input_shenfen;
    @Bind(R.id.edit_input_name)
    EditText edit_input_name;

    private TimeCount timeCount;
    private MemberInfo memberInfo;

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
        title_text_content.setText(getString(R.string.member_activate));
        timeCount = new TimeCount(60000, 1000);
        memberInfo = (MemberInfo) getIntent().getSerializableExtra(ConstantData.ALLMEMBERINFO);
        if(memberInfo.getMembername() != null){
            edit_input_name.setText(memberInfo.getMembername());
        }
        if(memberInfo.getPhoneno() != null){
            edit_input_phone.setText(memberInfo.getPhoneno());
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_member_activate);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
        if(timeCount != null){
            timeCount.cancel();
        }
    }

    @OnClick({R.id.title_icon_back, R.id.text_cancle, R.id.text_confirm, R.id.text_send_code})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
            case R.id.text_cancle:
                this.finish();
                break;
            case R.id.text_confirm:
                if(edit_input_phone.getText().toString().equals("") || edit_input_shenfen.getText().toString().equals("") || edit_input_name.getText().toString().equals("")){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.param_null));
                    return;
                }
                if(!Utils.isMobileNO(edit_input_phone.getText().toString())){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.please_input_right_phoneNo));
                    return;
                }
                actiateMember(edit_input_phone.getText().toString(), edit_input_shenfen.getText().toString(), edit_input_name.getText().toString(), memberInfo.getMemberno());
                break;
            case R.id.text_send_code:
                timeCount.start();
                break;
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }
        @Override
        public void onFinish() {//计时完毕时触发
            text_send_code.setText("重新验证");
            text_send_code.setClickable(true);
        }
        @Override
        public void onTick(long millisUntilFinished){//计时过程显示
            text_send_code.setClickable(false);
            text_send_code.setText("请等待("+millisUntilFinished /1000+")");
        }
    }

    private void actiateMember(String phone_no, String shenfen_no, final String name, String card_no){
        Map<String, String> map = new HashMap<String, String>();
        map.put("phone", phone_no);
        map.put("certnum", shenfen_no);
        map.put("name", name);
        map.put("cardnum", card_no);
        HttpRequestUtil.getinstance().actiateMember(HTTP_TASK_KEY, map, BaseResult.class, new HttpActionHandle<BaseResult>(){

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
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())){
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                    MemberActivateActivity.this.finish();
                }else{
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

            @Override
            public void startChangeMode() {
                final HttpActionHandle httpActionHandle = this;
                MemberActivateActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ChangeModeDialog(MemberActivateActivity.this, httpActionHandle).show();
                    }
                });
            }

            @Override
            public void handleActionChangeToOffLine() {
                Intent intent = new Intent(MemberActivateActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
