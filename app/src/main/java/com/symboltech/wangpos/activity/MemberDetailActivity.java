package com.symboltech.wangpos.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.CouponsAdapter;
import com.symboltech.wangpos.adapter.HistorySaleAdapter;
import com.symboltech.wangpos.adapter.RecommandGoodAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.msg.entity.AllMemberInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.HistoryAllSaleInfo;
import com.symboltech.wangpos.msg.entity.HistorySaleInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

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

    @Bind(R.id.ll_sale_info)
    LinearLayout ll_sale_info;
    @Bind(R.id.text_sale_total)
    TextView text_sale_total;
    @Bind(R.id.text_sale_times)
    TextView text_sale_times;
    @Bind(R.id.text_unit_price)
    TextView text_unit_price;
    @Bind(R.id.listview_info)
    ListView listview_info;


    @Bind(R.id.ll_member_recommand_goods)
    LinearLayout ll_member_recommand_goods;
    @Bind(R.id.recycleview_recommend_good)
    RecyclerView recycleview_recommend_good;
    private RecommandGoodAdapter recommandGoodAdapter;

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
        }

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

        List<GoodsInfo> goods = memberBigdate.getGoodslist();
        if (goods != null && goods.size() > 0) {
            recommandGoodAdapter = new RecommandGoodAdapter(goods, this);
            recycleview_recommend_good.setAdapter(recommandGoodAdapter);
            ll_member_recommand_goods.setVisibility(View.VISIBLE);
        }else{
            ll_member_recommand_goods.setVisibility(View.GONE);
        }

        HistoryAllSaleInfo allSaleInfo = memberBigdate.getHistoryallsale();
        List<HistorySaleInfo> saleInfos = memberBigdate.getHistorysalelist();
        if(allSaleInfo != null || (saleInfos != null && saleInfos.size() > 0)) {
            if(allSaleInfo !=null) {
                text_sale_total.setText(allSaleInfo.getSalemoney());
                text_sale_times.setText(allSaleInfo.getSaleamount());
                text_unit_price.setText(allSaleInfo.getAvge());
            }
            if(saleInfos != null && saleInfos.size() > 0){
                HistorySaleAdapter memberAdvanceAdapter = new HistorySaleAdapter(mContext, saleInfos);
                listview_info.setAdapter(memberAdvanceAdapter);
            }
            ll_sale_info.setVisibility(View.VISIBLE);
        }else {
            ll_sale_info.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_member_detail);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_member_activate})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_member_activate:
                MemberInfo info = memberBigdate.getMember();
                if(info != null && info.getStatus()!= null && "待验证".equals(info.getStatus())){
                    Intent intent = new Intent(this, MemberActivateActivity.class);
                    intent.putExtra(ConstantData.ALLMEMBERINFO , memberBigdate.getMember());
                    startActivity(intent);
                }else{
                    ToastUtils.sendtoastbyhandler(handler, "该会员不能激活");
                }
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
