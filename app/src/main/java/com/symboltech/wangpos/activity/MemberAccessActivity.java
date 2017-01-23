package com.symboltech.wangpos.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.dialog.ChangeModeDialog;
import com.symboltech.wangpos.dialog.UniversalHintDialog;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.result.AllMemeberInfoResult;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.zxing.app.CaptureActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.service.aidl.IMemberCardService;
import cn.weipass.pos.sdk.MagneticReader;
import cn.weipass.pos.sdk.impl.WeiposImpl;

public class MemberAccessActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @Bind(R.id.imageview_phone)ImageView imageview_phone;
    @Bind(R.id.edit_phone_number)EditText edit_phone_number;
    @Bind(R.id.view_line)View view_line;

    @Bind(R.id.radioGroup_type)RadioGroup radioGroup_type;
    @Bind(R.id.text_phone_number)RadioButton text_phone_number;

    @Bind(R.id.ll_phone_number)LinearLayout ll_phone_number;
    @Bind(R.id.ll_swaip_card)LinearLayout ll_swaip_card;

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_msg)TextView text_msg;
    private MemberInfo memberinfo;
    private Animation left_In_Animation;
    private Animation right_In_Animation;
    private HorizontalKeyBoard keyBoard;

    private String verify_type = ConstantData.MEMBER_VERIFY_BY_PHONE;
    private boolean isSwipVipCard =false;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.text_phone_number:
                verifyByPhone();
                break;
            case R.id.text_vip_card:
                verifyByVipCard();
                break;
            case R.id.text_sacn_QR:
                verifyByQR();
                break;
        }
    }

    public static final int Qrcode = 1;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(MemberAccessActivity.this,msg);
                    break;
                case Qrcode:
                    memberverifymethodbyhttp(ConstantData.MEMBER_VERIFY_BY_QR, (String) msg.obj);
                    break;
                case Vipcard:
                    memberverifymethodbyhttp(ConstantData.MEMBER_VERIFY_BY_MAGCARD, (String) msg.obj);
                    if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){

                    }else{
                        try {
                            if (mCardService != null) {
                                isSwipVipCard = true;
                                mCardService.startReadCard();
                            } else {
                                LogUtil.e("lgs", "mCardService==null");
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };


    public static final int Vipcard = 2;
    private IMemberCardService mCardService = null;
    private ServiceConnection mMemberCardConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCardService = null;

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCardService = IMemberCardService.Stub.asInterface(service);
        }
    };
    private MsrBroadcastReceiver msrReceiver = new MsrBroadcastReceiver();
    MagneticReader mMagneticReader;
    @Override
    protected void initData() {
        title_text_content.setText(getString(R.string.number_access));
        right_In_Animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in);
        left_In_Animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            mMagneticReader = WeiposImpl.as().openMagneticReader();
            if (mMagneticReader == null) {
                ToastUtils.sendtoastbyhandler(handler, "磁条卡读取服务不可用！");
            }
        }else{
            Intent intent = new Intent(IMemberCardService.class.getName());
            intent = AndroidUtils.getExplicitIntent(this, intent);
            if (intent != null) {
                bindService(intent, mMemberCardConnection, Context.BIND_AUTO_CREATE);
            } else {
                ToastUtils.sendtoastbyhandler(handler, "Check engine application version");
            }
            registerReceiver(msrReceiver, new IntentFilter("cn.koolcloud.engine.memberCard"));
        }
    }


    @Override
    protected void initView() {
        setContentView(R.layout.activity_member_access);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
        edit_phone_number.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imageview_phone.setImageResource(R.mipmap.phone_after_selected);
                    view_line.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.green));
                } else {
                    imageview_phone.setImageResource(R.mipmap.phone_before_selected);
                    view_line.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.line_color));
                }
            }
        });
        keyBoard = new HorizontalKeyBoard(this, this, edit_phone_number, ll_phone_number, new KeyBoardListener(){

            @Override
            public void onComfirm() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onValue(String value) {
                memberverifymethod(ConstantData.MEMBER_VERIFY_BY_PHONE, value);
            }
        });
        radioGroup_type.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        text_phone_number.setChecked(true);
        ll_swaip_card.setVisibility(View.GONE);
        ll_phone_number.setVisibility(View.VISIBLE);

    }

    @Override
    protected void recycleMemery() {
        try {
            if (mCardService != null) {
                if(isSwipVipCard){
                    isSwipVipCard = false;
                    mCardService.stopReadCard();
                }
            } else {
                LogUtil.e("lgs", "mCardService==null");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mCardService != null) {
            unbindService(mMemberCardConnection);
            unregisterReceiver(msrReceiver);
        }

        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.text_add_or_verify_member, R.id.title_icon_back})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.text_add_or_verify_member:
                keyBoard.dismiss();
                memberverifymethod(verify_type, edit_phone_number.getText().toString());
                break;
            case R.id.title_icon_back:
                this.finish();
                break;
        }
    }

    public void verifyByPhone(){
        if(radioGroup_type.getCheckedRadioButtonId() == R.id.text_phone_number){
            if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                if (mReadMagTask != null) {
                    mReadMagTask.interrupt();
                    mReadMagTask = null;
                }
            }else{
                if(isSwipVipCard){
                    try {
                        if (mCardService != null) {
                            isSwipVipCard = false;
                            mCardService.stopReadCard();
                        } else {
                            LogUtil.e("lgs", "mCardService==null");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            verify_type = ConstantData.MEMBER_VERIFY_BY_PHONE;
            right_In_Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ll_swaip_card.setVisibility(View.GONE);
                    ll_phone_number.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_swaip_card.setVisibility(View.GONE);
                    ll_phone_number.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            ll_phone_number.startAnimation(right_In_Animation);
        }
    }
    private ReadMagTask mReadMagTask = null;
    public void verifyByVipCard() {
        if(radioGroup_type.getCheckedRadioButtonId() == R.id.text_vip_card){
            if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
                if (mReadMagTask == null) {
                    mReadMagTask = new ReadMagTask();
                    mReadMagTask.start();
                }
            }else{
                try {
                    if (mCardService != null) {
                        isSwipVipCard = true;
                        mCardService.startReadCard();
                    } else {
                        LogUtil.e("lgs", "mCardService==null");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            verify_type = ConstantData.MEMBER_VERIFY_BY_MEMBERCARD;
            left_In_Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ll_phone_number.setVisibility(View.GONE);
                    ll_swaip_card.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_phone_number.setVisibility(View.GONE);
                    ll_swaip_card.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            ll_swaip_card.startAnimation(left_In_Animation);
        }
    }

    public void verifyByQR(){
        if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
            if (mReadMagTask != null) {
                mReadMagTask.interrupt();
                mReadMagTask = null;
            }
        }else{
            try {
                if (mCardService != null) {
                    if(isSwipVipCard){
                        isSwipVipCard = false;
                        mCardService.stopReadCard();
                    }
                } else {
                    LogUtil.e("lgs", "mCardService==null");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Intent intent_qr = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_MEMBER_VERIFY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY) {
            switch (requestCode) {
                case ConstantData.QRCODE_REQURST_MEMBER_VERIFY:
                    if (!StringUtil.isEmpty(data.getExtras().getString("QRcode"))) {
                        Message msg = Message.obtain();
                        msg.obj = data.getExtras().getString("QRcode");
                        msg.what = Qrcode;
                        handler.sendMessage(msg);
                    }
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 会员验证
     * @param verifyType
     *            会员验证类型
     * @param verifyValue
     *            验证value
     */
    private void memberverifymethod(String verifyType, String verifyValue) {
        // TODO Auto-generated method stub
        if (!StringUtil.isEmpty(edit_phone_number.getText().toString().trim())) {
            if(ConstantData.MEMBER_VERIFY_BY_PHONE.equals(verifyType)){
                if(Utils.isMobileNO(edit_phone_number.getText().toString().trim())){
                    memberverifymethodbyhttp(verifyType, edit_phone_number.getText().toString().trim());
                }else{
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.please_input_right_phoneNo));
                }
            }else{
                memberverifymethodbyhttp(verifyType, edit_phone_number.getText().toString().trim());
            }
        } else {
            ToastUtils.sendtoastbyhandler(handler, getString(R.string.please_input_right_phoneNo));
        }
    }

    /**
     *
     * @param verifyType
     *            会员验证类型
     * @param verifyValue
     *            验证value
     */
    private boolean isVerify = false;
    private  void memberverifymethodbyhttp(final String verifyType, String verifyValue) {
        if(isVerify){
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("condType", verifyType);
        map.put("condValue", verifyValue);
        HttpRequestUtil.getinstance().getAllMemberInfo(HTTP_TASK_KEY, map, AllMemeberInfoResult.class,
                new HttpActionHandle<AllMemeberInfoResult>() {

                    @Override
                    public void handleActionStart() {
                        isVerify = true;
                        startwaitdialog();
                    }

                    @Override
                    public void handleActionFinish() {
                        isVerify = false;
                        closewaitdialog();
                    }

                    @Override
                    public void handleActionError(String actionName, String errmsg) {
                        ToastUtils.sendtoastbyhandler(handler, errmsg);
                    }

                    @Override
                    public void handleActionSuccess(String actionName, final AllMemeberInfoResult result) {
                        if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                            Intent paymentIntent = new Intent(MemberAccessActivity.this, PaymentActivity.class);
                            paymentIntent.putExtra(ConstantData.ALLMEMBERINFO, result.getAllInfo());
                            paymentIntent.putExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_MEMBER);
//                            paymentIntent.putExtra(ConstantData.MEMBER_IS_SECOND_VERIFY, isChecked);
                            paymentIntent.putExtra(ConstantData.MEMBER_VERIFY, verifyType);
                            MemberAccessActivity.this.startActivity(paymentIntent);
                        } else if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK_ADD_MEMBER)){
                            MemberAccessActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    UniversalHintDialog uhd = new UniversalHintDialog(MemberAccessActivity.this, null, null,
                                            new DialogFinishCallBack() {
                                                @Override
                                                public void finish(int p) {
                                                    Intent paymentIntent = new Intent(MemberAccessActivity.this, PaymentActivity.class);
                                                    paymentIntent.putExtra(ConstantData.ALLMEMBERINFO, result.getAllInfo());
                                                    paymentIntent.putExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_MEMBER);
                                                    paymentIntent.putExtra(ConstantData.MEMBER_VERIFY, verifyType);
                                                    MemberAccessActivity.this.startActivity(paymentIntent);
                                                }
                                            });
                                    uhd.show();
                                }
                            });
                        }else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }

                    @Override
                    public void startChangeMode() {
                        final HttpActionHandle httpActionHandle = this;
                        MemberAccessActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangeModeDialog(MemberAccessActivity.this, httpActionHandle).show();
                            }
                        });
                    }

                    @Override
                    public void handleActionChangeToOffLine() {
                        Intent intent = new Intent(MemberAccessActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
//        HttpRequestUtil.getinstance().getmemberinfo(HTTP_TASK_KEY, map, MemberInfoResult.class,
//                new HttpActionHandle<MemberInfoResult>() {
//
//                    @Override
//                    public void handleActionStart() {
//                        startwaitdialog();
//                    }
//
//                    @Override
//                    public void handleActionFinish() {
//                    }
//
//                    @Override
//                    public void handleActionError(String actionName, String errmsg) {
//                        ToastUtils.sendtoastbyhandler(handler, errmsg);
//                        closewaitdialog();
//                    }
//
//                    @Override
//                    public void handleActionSuccess(String actionName, final MemberInfoResult result) {
//                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
//                            //OperateLog.getInstance().saveLog2File(OptLogEnum.MEMBER_VERIFY_SUCCCESS.getOptLogCode(), getString(R.string.member_verify_succcess));
//                            memberinfo = result.getDatamapclass().getMemberinfo();
//                            getAllMemberInfo(memberinfo.getId(), memberinfo.getIschecked(), verifyType);
//                        } else if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK_ADD_MEMBER)) {
//                            //OperateLog.getInstance().saveLog2File(OptLogEnum.MEMBER_ADD_SUCCESS.getOptLogCode(), getString(R.string.member_add_success));
//                            MemberAccessActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    UniversalHintDialog uhd = new UniversalHintDialog(MemberAccessActivity.this, null, null,
//                                            new DialogFinishCallBack() {
//                                                @Override
//                                                public void finish(int p) {
//                                                    if (result.getDatamapclass() != null
//                                                            && result.getDatamapclass().getMemberinfo() != null
//                                                            && !StringUtil.isEmpty(result.getDatamapclass().getMemberinfo().getId())) {
//                                                        memberinfo = result.getDatamapclass().getMemberinfo();
//                                                        getAllMemberInfo(memberinfo.getId(), memberinfo.getIschecked(), verifyType);
//                                                    } else {
//                                                        closewaitdialog();
//                                                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.member_id_is_not_null));
//                                                    }
//                                                }
//                                            });
//                                    uhd.show();
//                                }
//                            });
//                        } else {
//                            closewaitdialog();
//                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
//                        }
//                    }
//                    @Override
//                    public void startChangeMode() {
//                        final HttpActionHandle httpActionHandle = this;
//                        MemberAccessActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                new ChangeModeDialog(MemberAccessActivity.this, httpActionHandle).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void handleActionChangeToOffLine() {
//                        Intent intent = new Intent(MemberAccessActivity.this, MainActivity.class);
//                        startActivity(intent);
//                    }
//                });
    }

    /**
     * 获取会员所有信息
     */
    private void getAllMemberInfo(String memberID, final String isChecked, final String verifyType) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", memberID);
        HttpRequestUtil.getinstance().getAllMemberInfo(HTTP_TASK_KEY, map, AllMemeberInfoResult.class,
                new HttpActionHandle<AllMemeberInfoResult>() {

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
                    public void handleActionSuccess(String actionName, AllMemeberInfoResult result) {
                        if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
                            Intent paymentIntent = new Intent(MemberAccessActivity.this, PaymentActivity.class);
                            paymentIntent.putExtra(ConstantData.ALLMEMBERINFO, result.getAllInfo());
                            paymentIntent.putExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_MEMBER);
                            paymentIntent.putExtra(ConstantData.MEMBER_IS_SECOND_VERIFY, isChecked);
                            paymentIntent.putExtra(ConstantData.MEMBER_VERIFY, verifyType);
                            MemberAccessActivity.this.startActivity(paymentIntent);
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                    @Override
                    public void startChangeMode() {
                        final HttpActionHandle httpActionHandle = this;
                        MemberAccessActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ChangeModeDialog(MemberAccessActivity.this, httpActionHandle).show();
                            }
                        });
                    }
                    @Override
                    public void handleActionChangeToOffLine() {
                        Intent intent = new Intent(MemberAccessActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
    }

    class ReadMagTask extends Thread {
        private boolean isRun = false;
        @Override
        public void run() {
            isRun = true;
            // 磁卡刷卡后，主动获取解码后的字符串数据信息
            try {
                while (isRun) {
                    String decodeData = getMagneticReaderInfo();
                    if (decodeData != null && decodeData.length() != 0) {
                        System.out.println("final============>>>" + decodeData);
                        Message m = Message.obtain();
                        m.obj = decodeData;
                        m.what = Vipcard;
                        handler.sendMessage(m);
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isRun = false;
            }
        }
    }

    public String getMagneticReaderInfo() {
        if (mMagneticReader == null) {
            ToastUtils.sendtoastbyhandler(handler, "初始化磁条卡sdk失败");
            return "";
        }
        // 刷卡后，主动获取磁卡的byte[]数据
        // byte[] cardByte = mMagneticReader.readCard();
        // String decodeData = mMagneticReader.getCardDecodeData();
        // 磁卡刷卡后，主动获取解码后的字符串数据信息
        String[] decodeData = mMagneticReader.getCardDecodeThreeTrackData();//
        if (decodeData != null && decodeData.length > 0) {
            /**
             * 1：刷会员卡返回会员卡号后面变动的卡号，前面为固定卡号（没有写入到磁卡中）
             * 如会员卡号：9999100100030318，读卡返回数据为00030318，前面99991001在磁卡中没有写入
             * 2：刷银行卡返回数据格式为：卡号=有效期。
             */
            for (int i = 0; i < decodeData.length; i++) {
                if (decodeData[i] == null)
                    continue;
                return decodeData[i];
            }
            return "";
        } else {
            return "";
        }
    }
    class MsrBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Message msgContext = (Message) intent.getParcelableExtra(Message.class
                        .getName());
                Bundle data = msgContext.getData();

                if (data != null) {
                    JSONObject jsonData = new JSONObject(data.getString("data"));
                    if(!StringUtil.isEmpty(jsonData.optString("cardNo"))){
                        Message msg = Message.obtain();
                        msg.obj = jsonData.optString("cardNo");
                        msg.what = Vipcard;
                        handler.sendMessage(msg);
                    }
                    LogUtil.i("lgs", "dataStr is :" + jsonData.toString());
                    // 处理返回结果
                    LogUtil.i("lgs","\n\ntrack1 : "
                            + jsonData.optString("track1") + "\n\n"
                            + "track2 : " + jsonData.optString("track2")
                            + "\n\n" + "track3 : "
                            + jsonData.optString("track3") + "\n\n"
                            + "cardNo : " + jsonData.optString("cardNo")
                            + "\n\n" + "validTime : "
                            + jsonData.optString("validTime") + "\n\n"
                            + "serviceCode : "
                            + jsonData.optString("serviceCode") + "\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
