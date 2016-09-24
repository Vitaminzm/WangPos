package com.symboltech.wangpos.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.utils.ToastUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckOutActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
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
        title_text_content.setText(getString(R.string.checkout));
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_check_out);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.text_cancle_pay, R.id.title_icon_back, R.id.imageview_more})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.text_cancle_pay:
                break;
            case R.id.text_submit_order:
                break;
            case R.id.imageview_more:
                break;
            case R.id.title_icon_back:
                this.finish();
                break;
        }
    }
}
