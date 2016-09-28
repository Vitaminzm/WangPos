package com.symboltech.wangpos.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.msg.entity.AllMemberInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.utils.ToastUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemberDetailActivity extends BaseActivity {

    @Bind(R.id.title_text_content)
    TextView title_text_content;

    @Bind(R.id.text_member_name)
    TextView text_member_name;
    @Bind(R.id.text_member_card_no)
    TextView text_member_card_no;
    @Bind(R.id.text_member_score)
    TextView text_member_score;

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

    //会员信息
    private AllMemberInfo memberBigdate;
    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
        title_text_content.setText(getString(R.string.member_info));
        memberBigdate = (AllMemberInfo) getIntent().getSerializableExtra(ConstantData.ALLMEMBERINFO);
        MemberInfo memberInfo = memberBigdate.getMember();
        if(memberInfo != null){
            text_member_name.setText(memberInfo.getMembername());
            text_member_card_no.setText(memberInfo.getMemberno());
            text_member_score.setText(memberInfo.getCent_total());
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_member_detail);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.title_icon_back})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
        }
    }
}
