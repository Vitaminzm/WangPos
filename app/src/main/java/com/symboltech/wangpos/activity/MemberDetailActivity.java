package com.symboltech.wangpos.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.CouponsAdapter;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.msg.entity.AllMemberInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.List;

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
    @Bind(R.id.recycleview_hold_coupon)
    RecyclerView recycleview_hold_coupon;
    @Bind(R.id.ll_member_hold_coupon)
    LinearLayout ll_member_hold_coupon;
    private CouponsAdapter holdAdapter;
    @Bind(R.id.recycleview_recommend_good)
    RecyclerView recycleview_recommend_good;


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
    List<CouponInfo> couponlist;
    @Override
    protected void initData() {
        title_text_content.setText(getString(R.string.member_info));
        memberBigdate = (AllMemberInfo) getIntent().getSerializableExtra(ConstantData.ALLMEMBERINFO);
        MemberInfo memberInfo = memberBigdate.getMember();
        if(memberInfo != null){
            text_member_name.setText(memberInfo.getMembername());
            text_member_card_no.setText(memberInfo.getMemberno());
            text_member_score.setText(memberInfo.getCent_total());
            couponlist = memberBigdate.getCouponlist();
            MyLayoutManager linearLayoutManagerHold = new MyLayoutManager(this);
            linearLayoutManagerHold.setOrientation(LinearLayoutManager.HORIZONTAL);
            recycleview_hold_coupon.setLayoutManager(linearLayoutManagerHold);
            MyLayoutManager linearLayoutManagerHold2 = new MyLayoutManager(this);
            linearLayoutManagerHold2.setOrientation(LinearLayoutManager.HORIZONTAL);
            recycleview_recommend_good.setLayoutManager(linearLayoutManagerHold2);
            if (couponlist != null && couponlist.size() > 0){
                ll_member_hold_coupon.setVisibility(View.VISIBLE);
                holdAdapter = new CouponsAdapter(couponlist, 0, this);
                recycleview_hold_coupon.setAdapter(holdAdapter);
            }else {
                ll_member_hold_coupon.setVisibility(View.GONE);
            }
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

    public static class MyLayoutManager extends LinearLayoutManager {

        @SuppressWarnings("UnusedDeclaration")
        public MyLayoutManager(Context context) {
            super(context);
        }

        @SuppressWarnings("UnusedDeclaration")
        public MyLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        private int[] mMeasuredDimension = new int[2];


        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);
            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {

                try {
                    measureScrapChild(recycler, i,
                            widthSpec,
                            View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                            mMeasuredDimension);
                } catch (IndexOutOfBoundsException e) {

                    e.printStackTrace();
                }

                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }
            setMeasuredDimension(widthSpec, height);

        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec, int heightSpec, int[] measuredDimension) {
            View view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);
                view.measure(widthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);
        }
    }
}
