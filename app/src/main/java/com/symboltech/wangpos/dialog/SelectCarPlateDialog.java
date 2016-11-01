package com.symboltech.wangpos.dialog;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.MyRadioGroup;


/**
 * 脱机选择框
 * @author so
 *
 */
public class SelectCarPlateDialog extends Dialog implements OnClickListener {

	private Context context;
	private MyRadioGroup radioGroup;
	private Spinner sp_emporary_plate, sp_bind_plate;
	private EditText et_emporary_plate;
	private ImageView imageview_close;
	private TextView text_cancle, text_confirm;
	private ArrayAdapter<String> bind_adapter;
	private List<String> bind_data;
	private ArrayAdapter<String> emporary_adapter;
	private List<String> emporary_data;
	
	private String hour = null;
	private String addhour = null;
	private String billId = null;
	private MemberInfo member = null;
	private RelativeLayout rl_sendcarcoupon;
	private LinearLayout ll_status;
	private FinishSendCoupon finish;
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;

			default:
				break;
			}
		}
	};
	
	public SelectCarPlateDialog(Context context) {
		super(context, R.style.alert_dialog);
		this.context = context;
	}

	public SelectCarPlateDialog(Context context, MemberInfo member, String hour, String addhour, String billId,FinishSendCoupon finish) {
		super(context, R.style.alert_dialog);
		this.context = context;
		this.member = member;
		this.hour = hour;
		this.addhour = addhour;
		this.finish = finish;
		this.billId = billId;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCanceledOnTouchOutside(false);
		setContentView(R.layout.dialog_select_car_plate);
		emporary_data = Arrays.asList(context.getResources().getStringArray(R.array.cities));
		initView();
		initEvent();
	}

	private void initView() {

		rl_sendcarcoupon = (RelativeLayout) findViewById(R.id.rl_sendcarcoupon);
		ll_status = (LinearLayout) findViewById(R.id.ll_status);

		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);

		imageview_close = (ImageView) findViewById(R.id.imageview_close);

		sp_emporary_plate = (Spinner) findViewById(R.id.sp_emporary_plate);
		emporary_adapter = new ArrayAdapter<String>(context, R.layout.item_car_plate, emporary_data);
		emporary_adapter.setDropDownViewResource(R.layout.item_car_plate_drop);
		sp_emporary_plate.setAdapter(emporary_adapter);

		sp_bind_plate = (Spinner) findViewById(R.id.sp_bind_plate);
		et_emporary_plate = (EditText) findViewById(R.id.et_emporary_plate);
		
		bind_data = new ArrayList<String>();
//		if(member != null && member.getListcar() != null && member.getListcar().size() > 0){
//			for(CarInfo carinfo:member.getListcar()){
//				bind_data.add(carinfo.getCarnum());
//			}
//			et_emporary_plate.setEnabled(false);
//			sp_bind_plate.setEnabled(true);
//			sp_emporary_plate.setEnabled(false);
//		}else
		{
			((RadioButton)findViewById(R.id.btn_emporary)).setChecked(true);
			sp_bind_plate.setEnabled(false);
			sp_emporary_plate.setEnabled(true);
			sp_emporary_plate.setFocusable(true);
			et_emporary_plate.setEnabled(true);
		}
		bind_adapter = new ArrayAdapter<String>(context, R.layout.item_car_plate, bind_data);
		bind_adapter.setDropDownViewResource(R.layout.item_car_plate_drop);
		sp_bind_plate.setAdapter(bind_adapter);
		
		setDropDownHeight(sp_emporary_plate, 250);
		setDropDownHeight(sp_bind_plate, 200);
		
		radioGroup = (MyRadioGroup) findViewById(R.id.select_mode);
		
	}

	
	public void initEvent(){
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
		radioGroup.setOnCheckedChangeListener(new MyRadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(MyRadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == R.id.btn_bind){
					sp_bind_plate.setEnabled(true);
					sp_emporary_plate.setFocusable(false);
					sp_emporary_plate.setEnabled(false);
					et_emporary_plate.setEnabled(false);
				}else{
					sp_bind_plate.setEnabled(false);
					sp_emporary_plate.setEnabled(true);
					sp_emporary_plate.setFocusable(true);
					et_emporary_plate.setEnabled(true);
				}
			}
		});
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.text_cancle:
		case R.id.imageview_close:
			// 取消
			this.dismiss();
			break;
		case R.id.text_confirm:
			// 确定
			String plate = null;
			if(hour == null || addhour == null){
				ToastUtils.sendtoastbyhandler(mHandler, context.getResources().getString(R.string.waring_input_park_coupon_time));
				return;
			}
			if(radioGroup.getCheckedRadioButtonId() == R.id.btn_bind){
				if(sp_bind_plate.getSelectedItem() != null && sp_bind_plate.getSelectedItem().toString() !=null){
					if(member == null){
						manualParkCouponbyhttp(hour, addhour, sp_bind_plate.getSelectedItem().toString(), null);
					}else{
						manualParkCouponbyhttp(hour, addhour, sp_bind_plate.getSelectedItem().toString(), member.getMemberno());
					}
				}else{
					ToastUtils.sendtoastbyhandler(mHandler, context.getResources().getString(R.string.waring_no_bind_plate));
					return;
				}
			}else{
				plate = et_emporary_plate.getText().toString();
				if(plate != null){
					if(member == null){
						manualParkCouponbyhttp(hour, addhour, sp_emporary_plate.getSelectedItem().toString()+plate, null);
					}else{
						manualParkCouponbyhttp(hour, addhour, sp_emporary_plate.getSelectedItem().toString()+plate, member.getMemberno());
					}
				}else{
					ToastUtils.sendtoastbyhandler(mHandler, context.getResources().getString(R.string.waring_input_plate));
					return;
				}
			}
			
			break;
		default:
			break;
		}
	}
	
	private void manualParkCouponbyhttp(final String hour, String addhour, final String carno, String cardno){
		Map<String, String> map = new HashMap<String, String>();
		map.put("hour", hour);
		map.put("addhour", addhour);
		map.put("carno", carno);
		if(cardno != null)
			map.put("cardno", cardno);
		map.put("billid", billId);
		HttpRequestUtil.getinstance().manualParkCoupon(map, BaseResult.class, new HttpActionHandle<BaseResult>() {

			@Override
			public void handleActionStart() {
				imageview_close.setVisibility(View.GONE);
				rl_sendcarcoupon.setVisibility(View.GONE);
				ll_status.setVisibility(View.VISIBLE);
			}

			@Override
			public void handleActionFinish() {
				imageview_close.setVisibility(View.VISIBLE);
				rl_sendcarcoupon.setVisibility(View.VISIBLE);
				ll_status.setVisibility(View.GONE);
			}

			@Override
			public void handleActionError(String actionName, String errmsg) {
				Toast.makeText(context, errmsg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void handleActionSuccess(String actionName, BaseResult result) {
				if (result.getCode().equals(ConstantData.HTTP_RESPONSE_OK)) {
					if(finish != null){
						finish.finishSendCoupon(carno, hour);
					}
					ToastUtils.sendtoastbyhandler(mHandler, context.getString(R.string.send_success));
					dismiss();
				}else{
					ToastUtils.sendtoastbyhandler(mHandler, context.getString(R.string.send_failed));
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
	public void dismiss() {
		mHandler.removeCallbacksAndMessages(null);
		super.dismiss();
	}
	
	public interface FinishSendCoupon{
		public void finishSendCoupon(String carNo, String hour);
	}
}
