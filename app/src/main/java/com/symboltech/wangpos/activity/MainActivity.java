package com.symboltech.wangpos.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.config.InitializeConfig;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.dialog.ChangeManagerDialog;
import com.symboltech.wangpos.dialog.PrintOrderDialog;
import com.symboltech.wangpos.dialog.ReturnDialog;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.result.BillResult;
import com.symboltech.wangpos.result.InitializeInfResult;
import com.symboltech.wangpos.result.TicketFormatResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.MyscollView;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.view_pager) ViewPager view_pager;
    @Bind(R.id.myscollview) MyscollView myscollView;
    /** used by receiver */
    private IntentFilter filter = new IntentFilter();

    public BroadcastReceiver receiver =new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantData.OFFLINE_MODE.equals(intent.getAction())) {
                int mode = intent.getIntExtra(ConstantData.OFFLINE_MODE_INFO, -1);
                if(mode == ConstantData.ONLINE_STATE || mode == ConstantData.OFFLINE_STATE){
                    //修改显示界面
                    ChangeUI(mode);
                }else if(mode == ConstantData.CHANGE_MODE_STATE){
                    //弹网络切换提示框
//                    Intent changeMode = new Intent(MainActivity.this, ChangeModeDialog.class);
//                    changeMode.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    changeMode.putExtra(ConstantData.CHANGE_ONLINE_MODE, true);
//                    startActivity(changeMode);
                }
            }
        }
    };
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
            }
        }
    }

    MyHandler handler = new MyHandler(this);

    @Override
    protected void initData() {
        if(MyApplication.isOffLineMode()){
            //手动监测网络状态
            Intent serviceintent = new Intent(MainActivity.this, RunTimeService.class);
            serviceintent.putExtra(ConstantData.CHECK_NET, true);
            mContext.startService(serviceintent);
            ChangeUI(1);
        }else{
            ChangeUI(0);
        }
        if (SpSaveUtils.readboolean(MyApplication.context, ConstantData.IS_CONFIG_DOWNLOAD, true)) {
            //尝试删除已经上传成功的过期数据
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            OrderInfoDao dao = new OrderInfoDao(mContext);
            Date dNow = new Date();
            Date dBefore = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dNow);
            calendar.add(Calendar.DAY_OF_MONTH, AppConfigFile.OFFLINE_DATA_TIME);
            dBefore = calendar.getTime();
//            dao.deleteOrderBytime(format.format(dBefore.getTime()));
//            //删除那些交易未成功的数据
//            dao.deleteOrderbyState();
            getconfigInfo();
            //获取小票格式
          //  getTickdatas();
           // uploadOfflineData(true);
        }
    }

    public void ChangeUI(int mode){
        if(view_pager != null){
            view_pager.setCurrentItem(mode);
        }
    }

    @Override
    protected void onResume() {
        if(MyApplication.isOffLineMode()){
            ChangeUI(1);
        }else {
            ChangeUI(0);
        }
        filter.addAction(ConstantData.OFFLINE_MODE);
        registerReceiver(receiver, filter);
        super.onResume();
    }


    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void initView() {
        MyApplication.addActivity(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        myscollView.setView_pager(view_pager);
        view_pager.setPageTransformer(true, new DepthPageTransformer());
        view_pager.setAdapter(new HorizontalPagerAdapter(getApplicationContext(), this));
        view_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                myscollView.setOffset(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                myscollView.setPosition(view_pager.getCurrentItem());
            }
        });
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.rl_lockscreen:
                lockscreen();
                break;
            case R.id.rl_member:
                gotoFunction(MemberAccessActivity.class);
                break;
            case R.id.rl_sendcarcoupon:

                break;
            case R.id.rl_pay:
                gotoPay();
                break;
            case R.id.rl_salereturn:
                new ReturnDialog(this).show();
                break;
            case R.id.rl_change:
                new ChangeManagerDialog(this).show();
                break;
            case R.id.rl_billprint:
                new PrintOrderDialog(this, new GeneralEditListener() {
                    @Override
                    public void editinput(String edit) {
                        printByorder(edit);
                    }
                }).show();
                break;
        }
    }

    public void gotoPay(){
        Intent intent = new Intent(this,PaymentActivity.class);
        intent.putExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_ACCOUNTS);
        startActivity(intent);
    }
    /**
     * gotoFunction
     */
    private void gotoFunction(Class t) {
        Intent intent = new Intent(this,t);
        startActivity(intent);
    }

    /**
     * lockscreen
     */
    private void lockscreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_LOCKSCREEN);
        startActivity(intent);
        this.finish();
    }

    private void printByorder(final String edit) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("billid", edit);
        HttpRequestUtil.getinstance().printerOrderagain(map, BillResult.class, new HttpActionHandle<BillResult>() {

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
            public void handleActionSuccess(String actionName, BillResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    BillInfo billinfo = result.getTicketInfo().getBillinfo();
                    if (billinfo != null) {
                        if ("0".equals(billinfo.getSaletype())) {

                        } else if ("1".equals(billinfo.getSaletype()) || "2".equals(billinfo.getSaletype())) {

                        } else {
                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_param_err));
                        }
                    }
                }else{
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }

        });
    }

    /**
     * get config info by server
     */
    protected void getconfigInfo() {
        HttpRequestUtil.getinstance().initialize(null, InitializeInfResult.class,
                new HttpActionHandle<InitializeInfResult>() {

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
                    public void handleActionSuccess(String actionName, InitializeInfResult result) {
                        if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                            InitializeConfig.initialize(MainActivity.this, result.getInitializeInfo());
                            SpSaveUtils.writeboolean(MyApplication.context, ConstantData.IS_CONFIG_DOWNLOAD, false);
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                });
    }

    protected void getTickdatas(){
        HttpRequestUtil.getinstance().getTicketFormat(null, TicketFormatResult.class, new HttpActionHandle<TicketFormatResult>() {

            @Override
            public void handleActionError(String actionName, String errmsg) {
                ToastUtils.sendtoastbyhandler(handler, errmsg);
            }

            @Override
            public void handleActionSuccess(String actionName,
                                            TicketFormatResult result) {
                if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                    if (result.getTicketformat() != null && result.getTicketformat().getTickdatas() != null
                            && result.getTicketformat().getTickdatas().size() > 0) {
                        SpSaveUtils.saveObject(MyApplication.context, ConstantData.TICKET_FORMAT_LIST, result.getTicketformat().getTickdatas());
                    } else {
                        SpSaveUtils.delete(MyApplication.context, ConstantData.TICKET_FORMAT_LIST);
                    }
                } else {
                    SpSaveUtils.delete(MyApplication.context, ConstantData.TICKET_FORMAT_LIST);
                }
            }
        });
    }

    public class HorizontalPagerAdapter extends PagerAdapter {

        public Context mContext;
        public LayoutInflater mLayoutInflater;
        public List<View> views;
        public View.OnClickListener onClickListener;

        public HorizontalPagerAdapter(Context context, View.OnClickListener onClickListener) {
            super();
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            this.onClickListener = onClickListener;
            initView();
        }

        public void initView() {
            views = new ArrayList<>();
            View v1 = mLayoutInflater.inflate(R.layout.view_button_main, null);
            ButterKnife.findById(v1, R.id.rl_member).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_pay).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_billprint).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_sendcarcoupon).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_lockscreen).setOnClickListener(onClickListener);
            ButterKnife.findById(v1, R.id.rl_change).setOnClickListener(onClickListener);
            views.add(v1);
            View v2 = mLayoutInflater.inflate(R.layout.view_button_offline_main, null);
            ButterKnife.findById(v2, R.id.rl_upload).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_offline).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_weichat).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_bank).setOnClickListener(onClickListener);
            ButterKnife.findById(v2, R.id.rl_salereturn).setOnClickListener(onClickListener);
            views.add(v2);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @SuppressLint("NewApi")
        public void transformPage(View view, float position)
        {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();


            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0)
                {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else
                {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else {
                view.setAlpha(0);
            }
        }
    }
}
