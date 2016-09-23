package com.symboltech.wangpos.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.utils.ToastUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PaymentActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.radioGroup_function)RadioGroup radioGroup_function;

    @Bind(R.id.text_consume_score)TextView text_consume_score;
    @Bind(R.id.text_total_money)TextView text_total_money;
    @Bind(R.id.edit_input_money)EditText edit_input_money;

    @Bind(R.id.text_cashier_name)TextView text_cashier_name;
    @Bind(R.id.text_bill_id)TextView text_bill_id;
    @Bind(R.id.text_saleman_name)TextView text_saleman_name;
    @Bind(R.id.text_desk_code)TextView text_desk_code;

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
        title_text_content.setText(getString(R.string.pay));
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_payment);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        radioGroup_function.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_add_score_good:
                        break;
                    case R.id.radio_add_salesman:
                        break;
                    case R.id.radio_look_member:
                        break;
                    case R.id.radio_select_good:
                        break;
                }
            }
        });
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm})
    public void back(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm:
                break;
        }
    }
}
