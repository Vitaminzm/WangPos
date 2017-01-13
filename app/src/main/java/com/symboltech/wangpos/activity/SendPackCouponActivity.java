package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.dialog.VerifyMemberDialog;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.CancleAndConfirmback;
import com.symboltech.wangpos.msg.entity.CarInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.result.MemberInfoResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.wangpos.view.MyRadioGroup;
import com.symboltech.zxing.app.CaptureActivity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendPackCouponActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.text_desk_code)TextView text_desk_code;
    @Bind(R.id.text_shop)TextView text_shop;
    @Bind(R.id.shop_mall)TextView shop_mall;

    @Bind(R.id.ll_keyboard)
    RelativeLayout ll_keyboard;

    @Bind(R.id.select_mode)MyRadioGroup radioGroup;
    @Bind(R.id.btn_bind)RadioButton btn_bind;
    @Bind(R.id.btn_emporary)RadioButton btn_emporary;
    @Bind(R.id.bind_palte)Spinner bind_plate;
    @Bind(R.id.emporary_plate)Spinner emporary_plate;
    @Bind(R.id.ll_bind_car_plate)LinearLayout ll_bind_car_plate;
    @Bind(R.id.pack_coupon_time)EditText packCouponTime;
    @Bind(R.id.et_emporary_plate)EditText carPlateNo;
    private HorizontalKeyBoard keyboard;

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
    private MemberInfo member;

    private ArrayAdapter<String> bind_adapter;
    private List<String> bind_data;
    private ArrayAdapter<String> emporary_adapter;
    private List<String> emporary_data;

    @Override
    protected void initData() {
        keyboard = new HorizontalKeyBoard(this, this, packCouponTime, ll_keyboard);
        title_text_content.setText(getString(R.string.send_pack_coupon));
        text_desk_code.setText(SpSaveUtils.read(this, ConstantData.CASHIER_DESK_CODE, ""));
        if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(mContext, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
            shop_mall.setText("门店");
            text_shop.setText(SpSaveUtils.read(this, ConstantData.MALL_NAME, ""));
        }else{
            text_shop.setText(SpSaveUtils.read(this, ConstantData.SHOP_NAME, ""));
        }
        emporary_data = Arrays.asList(mContext.getResources().getStringArray(R.array.cities));
        emporary_adapter = new ArrayAdapter<String>(mContext, R.layout.item_car_plate, emporary_data);
        emporary_adapter.setDropDownViewResource(R.layout.item_car_plate_drop);
        emporary_plate.setAdapter(emporary_adapter);
        setDropDownHeight(emporary_plate, 250);
        //初始化显示
        radioGroup.setOnCheckedChangeListener(new MyRadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(MyRadioGroup group, int checkedId) {
                if (checkedId == R.id.btn_bind) {
                    bind_plate.setEnabled(true);
                    emporary_plate.setFocusable(false);
                    emporary_plate.setEnabled(false);
                    carPlateNo.setEnabled(false);
                } else {
                    bind_plate.setEnabled(false);
                    emporary_plate.setEnabled(true);
                    emporary_plate.setFocusable(true);
                    carPlateNo.setEnabled(true);
                }
            }
        });
    }

    public void setDropDownHeight(Spinner mSpinner, int pHeight){
        try {
            Field field=Spinner.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            ListPopupWindow popUp=(ListPopupWindow)field.get(mSpinner);
            popUp.setHeight(pHeight);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void initView() {
        setContentView(R.layout.activity_send_parkcoupon);
        AppConfigFile.addActivity(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        AppConfigFile.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_add_member, R.id.text_send_parkcoupon})
    public void click(View view){
        if(Utils.isFastClick()){
            return;
        }
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_add_member:
                new VerifyMemberDialog(this, new CancleAndConfirmback(){
                    @Override
                    public void doCancle() {
                        Intent intent_qr = new Intent(mContext, CaptureActivity.class);
                        startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_MEMBER_VERIFY);
                    }

                    @Override
                    public void doConfirm(String num) {
                        memberverifymethodbyhttp(ConstantData.MEMBER_VERIFY_BY_PHONE, num);
                    }
                }).show();
                break;
            case R.id.text_send_parkcoupon:
                String plate = null;
                if(TextUtils.isEmpty(packCouponTime.getText().toString())){
                    ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_input_park_coupon_time));
                    return;
                }
                if(radioGroup.getCheckedRadioButtonId() == R.id.btn_bind){
                    if(bind_plate.getSelectedItem() != null && bind_plate.getSelectedItem().toString() !=null){
                        if(member == null){
                            manualParkCouponbyhttp(packCouponTime.getText().toString(), bind_plate.getSelectedItem().toString(), null);
                        }else{
                            manualParkCouponbyhttp(packCouponTime.getText().toString(), bind_plate.getSelectedItem().toString(), member.getMemberno());
                        }
                    }else{
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_no_bind_plate));
                        return;
                    }
                }else{
                    plate = carPlateNo.getText().toString();
                    if(plate != null && plate.length() == 6){
                        if(member == null){
                            manualParkCouponbyhttp(packCouponTime.getText().toString(), emporary_plate.getSelectedItem().toString()+plate, null);
                        }else{
                            manualParkCouponbyhttp(packCouponTime.getText().toString(), emporary_plate.getSelectedItem().toString()+plate, member.getMemberno());
                        }
                    }else{
                        ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_input_plate));
                        return;
                    }
                }
                break;
        }
    }

    private void manualParkCouponbyhttp(String hour, String carno, String cardno) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("hour", hour);
        map.put("carno", carno);
        if (cardno != null)
            map.put("cardno", cardno);
        HttpRequestUtil.getinstance().manualParkCoupon(HTTP_TASK_KEY, map, BaseResult.class, new HttpActionHandle<BaseResult>() {

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
                    ToastUtils.sendtoastbyhandler(handler, "停车券发放成功");
                    finish();
                } else {
                    ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                }
            }
        });
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
        HttpRequestUtil.getinstance().getmemberinfo(HTTP_TASK_KEY, map, MemberInfoResult.class,
                new HttpActionHandle<MemberInfoResult>() {

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
                    public void handleActionSuccess(String actionName, final MemberInfoResult result) {
                        if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
                            SendPackCouponActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    member = result.getDatamapclass().getMemberinfo();
                                    if(member.getListcar() != null && member.getListcar().size() > 0){
                                        btn_bind.setChecked(true);
                                        bind_plate.setEnabled(true);
                                        emporary_plate.setFocusable(false);
                                        emporary_plate.setEnabled(false);
                                        carPlateNo.setEnabled(false);
                                        bind_data = new ArrayList<String>();
                                        for(CarInfo carinfo:member.getListcar()){
                                            bind_data.add(carinfo.getCarnum());
                                        }
                                        bind_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_car_plate, bind_data);
                                        bind_adapter.setDropDownViewResource(R.layout.item_car_plate_drop);
                                        bind_plate.setAdapter(bind_adapter);
                                        bind_plate.setEnabled(false);
                                        btn_emporary.setVisibility(View.VISIBLE);
                                        ll_bind_car_plate.setVisibility(View.VISIBLE);
                                    }else{
                                        ToastUtils.sendtoastbyhandler(handler, "该会员没有绑定车牌");
                                    }
                                }
                            });
                        } else if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK_ADD_MEMBER)) {
                            ToastUtils.sendtoastbyhandler(handler, "新注册的会员没有绑定车牌");
                        } else {
                            ToastUtils.sendtoastbyhandler(handler, result.getMsg());
                        }
                    }

                    @Override
                    public void handleActionChangeToOffLine() {
                        Intent intent = new Intent(SendPackCouponActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY) {
            switch (requestCode) {
                case ConstantData.QRCODE_REQURST_MEMBER_VERIFY:
                    String value = data.getExtras().getString("QRcode");
                    if (!StringUtil.isEmpty(value)) {
                        memberverifymethodbyhttp(ConstantData.MEMBER_VERIFY_BY_QR, value);
                    }
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
