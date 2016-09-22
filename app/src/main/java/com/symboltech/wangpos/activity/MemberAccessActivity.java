package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.dialog.UniversalHintDialog;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.result.AllMemeberInfoResult;
import com.symboltech.wangpos.result.MemberInfoResult;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.zxing.app.CaptureActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MemberAccessActivity extends BaseActivity {

    @Bind(R.id.imageview_phone)ImageView imageview_phone;
    @Bind(R.id.edit_phone_number)EditText edit_phone_number;
    @Bind(R.id.view_line)View view_line;
    @Bind(R.id.text_phone_number)TextView text_phone_number;
    @Bind(R.id.text_vip_card)TextView text_vip_card;
    @Bind(R.id.text_sacn_QR)TextView text_sacn_QR;
    @Bind(R.id.ll_phone_number)LinearLayout ll_phone_number;
    @Bind(R.id.ll_swaip_card)LinearLayout ll_swaip_card;
    private MemberInfo memberinfo;

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
    private Animation right_In_Animation;
    MyHandler handler = new MyHandler(this);
    @Override
    protected void initData() {
        right_In_Animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_member_access);
        MyApplication.addActivity(this);
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
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick(R.id.title_icon_back)
    public void back(View view){
        this.finish();
    }

    @OnClick({R.id.text_add_or_verify_member, R.id.text_phone_number, R.id.text_vip_card, R.id.text_sacn_QR})
    public void click(View view){
        int id = view.getId();
        switch (id){
            case R.id.text_add_or_verify_member:

                break;
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

    public void verifyByPhone(){
        if(ll_phone_number.getVisibility() == View.GONE){
            right_In_Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ll_swaip_card.setVisibility(View.GONE);
                    ll_phone_number.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_phone_number.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            ll_phone_number.startAnimation(right_In_Animation);
        }

       // ll_phone_number.setVisibility(View.VISIBLE);

        Drawable phone_number_drawable= getResources().getDrawable(R.mipmap.phone_number_after);
        phone_number_drawable.setBounds(0, 0, phone_number_drawable.getMinimumWidth(), phone_number_drawable.getMinimumHeight());
        text_phone_number.setTextColor(getResources().getColor(R.color.red));
        text_phone_number.setCompoundDrawables(null, phone_number_drawable, null, null);

        Drawable scan_qr_drawable= getResources().getDrawable(R.mipmap.scan_qr_before);
        scan_qr_drawable.setBounds(0, 0, scan_qr_drawable.getMinimumWidth(), scan_qr_drawable.getMinimumHeight());
        text_sacn_QR.setTextColor(getResources().getColor(R.color.green));
        text_sacn_QR.setCompoundDrawables(null, scan_qr_drawable, null, null);

        Drawable vip_card_drawable= getResources().getDrawable(R.mipmap.vip_card_before);
        vip_card_drawable.setBounds(0, 0, vip_card_drawable.getMinimumWidth(), vip_card_drawable.getMinimumHeight());
        text_vip_card.setTextColor(getResources().getColor(R.color.green));
        text_vip_card.setCompoundDrawables(null, vip_card_drawable, null, null);
    }

    public void verifyByVipCard() {
        if(ll_swaip_card.getVisibility() == View.GONE){
            right_In_Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ll_phone_number.setVisibility(View.GONE);
                    ll_swaip_card.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_swaip_card.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            ll_swaip_card.startAnimation(right_In_Animation);
        }
        Drawable phone_number_drawable= getResources().getDrawable(R.mipmap.phone_number_before);
        phone_number_drawable.setBounds(0, 0, phone_number_drawable.getMinimumWidth(), phone_number_drawable.getMinimumHeight());
        text_phone_number.setTextColor(getResources().getColor(R.color.green));
        text_phone_number.setCompoundDrawables(null, phone_number_drawable, null, null);

        Drawable scan_qr_drawable= getResources().getDrawable(R.mipmap.scan_qr_before);
        scan_qr_drawable.setBounds(0, 0, scan_qr_drawable.getMinimumWidth(), scan_qr_drawable.getMinimumHeight());
        text_sacn_QR.setTextColor(getResources().getColor(R.color.green));
        text_sacn_QR.setCompoundDrawables(null, scan_qr_drawable, null, null);

        Drawable vip_card_drawable = getResources().getDrawable(R.mipmap.vip_card_after);
        vip_card_drawable.setBounds(0, 0, vip_card_drawable.getMinimumWidth(), vip_card_drawable.getMinimumHeight());
        text_vip_card.setTextColor(getResources().getColor(R.color.red));
        text_vip_card.setCompoundDrawables(null, vip_card_drawable, null, null);
    }

    public void verifyByQR(){
        Drawable phone_number_drawable= getResources().getDrawable(R.mipmap.phone_number_before);
        phone_number_drawable.setBounds(0, 0, phone_number_drawable.getMinimumWidth(), phone_number_drawable.getMinimumHeight());
        text_phone_number.setTextColor(getResources().getColor(R.color.green));
        text_phone_number.setCompoundDrawables(null, phone_number_drawable, null, null);

        Drawable scan_qr_drawable= getResources().getDrawable(R.mipmap.scan_qr_after);
        scan_qr_drawable.setBounds(0, 0, scan_qr_drawable.getMinimumWidth(), scan_qr_drawable.getMinimumHeight());
        text_sacn_QR.setTextColor(getResources().getColor(R.color.red));
        text_sacn_QR.setCompoundDrawables(null, scan_qr_drawable, null, null);

        Drawable vip_card_drawable = getResources().getDrawable(R.mipmap.vip_card_before);
        vip_card_drawable.setBounds(0, 0, vip_card_drawable.getMinimumWidth(), vip_card_drawable.getMinimumHeight());
        text_vip_card.setTextColor(getResources().getColor(R.color.green));
        text_vip_card.setCompoundDrawables(null, vip_card_drawable, null, null);
        Intent intent_qr = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_MEMBER_VERIFY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY) {
            switch (requestCode) {
                case ConstantData.QRCODE_REQURST_MEMBER_VERIFY:
                    verifyByPhone();
                    if (!StringUtil.isEmpty(data.getExtras().getString("QRcode"))) {
                        memberverifymethodbyhttp(ConstantData.MEMBER_VERIFY_BY_QR, data.getExtras().getString("QRcode"));
                    }
                    break;
                case ConstantData.QRCODE_REQURST_QR_PAY:
                    if (!StringUtil.isEmpty(data.getExtras().getString("QRcode"))) {
//                        AlipayAndWeixinPayControllerDialog paydialog = new AlipayAndWeixinPayControllerDialog(this, paytype,
//                                ConstantData.THIRD_OPERATION_PAY, data.getExtras().getString("QRcode"));
//                        paydialog.show();
                    }
                    break;
                default:
                    break;
            }
        }else if(resultCode == ConstantData.MEMBER_RESULT_CODE){
            if(requestCode == ConstantData.BOOT_MEMBER_REQUEST_CODE){
//                MyApplication.memberinfo = (MemberInfo) data.getExtras().getSerializable(ConstantData.MEMBER);
//                getAllMemberInfo(MyApplication.memberinfo.getId(), MyApplication.memberinfo.getIschecked(), ConstantData.MEMBER_VERIFY_BY_MAGCARD);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 会员验证
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(member verify method)
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
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(member verify by server)
     * @param verifyType
     *            会员验证类型
     * @param verifyValue
     *            验证value
     */
    private void memberverifymethodbyhttp(final String verifyType, String verifyValue) {
        Map<String, String> map = new HashMap<>();
        map.put("condType", verifyType);
        map.put("condValue", verifyValue);
        HttpRequestUtil.getinstance().getmemberinfo(map, MemberInfoResult.class,
                new HttpActionHandle<MemberInfoResult>() {

                    @Override
                    public void handleActionStart() {
                        startwaitdialog();
                    }

                    @Override
                    public void handleActionFinish() {
                    }

                    @Override
                    public void handleActionError(String actionName, String errmsg) {
                        // TODO Auto-generated method stub
                        ToastUtils.sendtoastbyhandler(handler, errmsg);
                        closewaitdialog();
                    }

                    @Override
                    public void handleActionSuccess(String actionName, final MemberInfoResult result) {
                        // TODO Auto-generated method stub
                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            //OperateLog.getInstance().saveLog2File(OptLogEnum.MEMBER_VERIFY_SUCCCESS.getOptLogCode(), getString(R.string.member_verify_succcess));
                            memberinfo = result.getDatamapclass().getMemberinfo();
                            //getAllMemberInfo(.memberinfo.getId(), memberinfo.getIschecked(), verifyType);
                        } else if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK_ADD_MEMBER)) {
                            //OperateLog.getInstance().saveLog2File(OptLogEnum.MEMBER_ADD_SUCCESS.getOptLogCode(), getString(R.string.member_add_success));
                            UniversalHintDialog uhd = new UniversalHintDialog(MemberAccessActivity.this, null, null,
                                    new DialogFinishCallBack() {
                                    @Override
                                    public void finish() {
                                        if (result.getDatamapclass() != null
                                                && result.getDatamapclass().getMemberinfo() != null
                                                && !StringUtil.isEmpty(result.getDatamapclass().getMemberinfo().getId())) {
                                            memberinfo = result.getDatamapclass().getMemberinfo();
                                            //getAllMemberInfo(memberinfo.getId(), memberinfo.getIschecked(), verifyType);
                                        } else {
                                            closewaitdialog();
                                            ToastUtils.sendtoastbyhandler(handler, getString(R.string.member_id_is_not_null));
                                        }
                                    }
                                });
                            uhd.show();
                        } else {
                            closewaitdialog();
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                });
    }

    /**
     * 获取会员所有信息
     */
    private void getAllMemberInfo(String memberID, final String isChecked, final String verifyType) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", memberID);
        HttpRequestUtil.getinstance().getAllMemberInfo(map, AllMemeberInfoResult.class,
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
//                            Intent shoppingCardIntent = new Intent(MainActivity.this, ShoppingCartActivity.class);
//                            shoppingCardIntent.putExtra(ConstantData.ALLMEMBERINFO, result.getAllInfo());
//                            shoppingCardIntent.putExtra(ConstantData.ENTER_CASHIER_WAY_FLAG, ConstantData.ENTER_CASHIER_BY_MEMBER);
//                            shoppingCardIntent.putExtra(ConstantData.MEMBER_IS_SECOND_VERIFY, isChecked);
//                            shoppingCardIntent.putExtra(ConstantData.MEMBER_VERIFY, verifyType);
//                            MainActivity.this.startActivity(shoppingCardIntent);
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }
                });
    }
}
