package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.CouponCallback;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.wangpos.view.TextScrollView;

import java.util.List;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class CouponUsedDialog extends BaseDialog implements View.OnClickListener {
	public Context context;
	private EditText edit_money;
	private TextView tv_coupon_date, tv_coupon_big_money, text_cancle, text_confirm;
	private TextScrollView tv_coupon_money, tv_coupon_name;
	private ImageView imageview_close;
	private CouponCallback couponCallback;
	private CouponInfo couponInfo;
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(context, msg);
					break;
				case 3:
					CouponUsedDialog.this.dismiss();
					break;
				default:
					break;
			}
		};
	};
	private HorizontalKeyBoard keyboard;

	private List<CouponInfo> list;
	public CouponUsedDialog(Context context, List<CouponInfo> list,CouponInfo couponInfo, CouponCallback couponCallback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.couponCallback = couponCallback;
		this.couponInfo = couponInfo;
		this.list= list;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_coupon_used);
		this.setCanceledOnTouchOutside(false);
		initUI();
		initData();
	}

	private void initData() {
		tv_coupon_money.setText(couponInfo.getAvailablemoney()+"元");
		tv_coupon_name.setText(couponInfo.getName());
		tv_coupon_date.setText(couponInfo.getEnddate());
		tv_coupon_big_money.setText(couponInfo.getAvailablemoney());
		if(list!= null && list.size()>0){
			for(CouponInfo info:list){
				if(info.getCouponno().equals(couponInfo.getCouponno())){
					edit_money.setText(info.getAvailablemoney());
					break;
				}
			}
		}else{
			edit_money.setText(couponInfo.getAvailablemoney());
		}
		keyboard = new HorizontalKeyBoard(context, this, edit_money, null, new KeyBoardListener() {
			@Override
			public void onComfirm() {

			}

			@Override
			public void onCancel() {

			}

			@Override
			public void onValue(String value) {
				if(StringUtil.isEmpty(edit_money.getText().toString())){
					ToastUtils.sendtoastbyhandler(handler,"输入不能为空");
					edit_money.setText("");
					return;
				}
				double money = -1;
				try {
					money = Double.parseDouble(value);
				}catch (Exception e){

				}
				if(money < 0){
					ToastUtils.sendtoastbyhandler(handler,"输入格式异常");
					edit_money.setText("");
				}else{
					if(money> Double.parseDouble(tv_coupon_big_money.getText().toString())){
						ToastUtils.sendtoastbyhandler(handler,"输入值不能大于最大可用金额");
						edit_money.setText("");
					}
				}
			}
		});
	}

	private void initUI() {
		edit_money = (EditText) findViewById(R.id.edit_money);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		tv_coupon_money = (TextScrollView) findViewById(R.id.tv_coupon_money);
		tv_coupon_name = (TextScrollView) findViewById(R.id.tv_coupon_name);
		tv_coupon_date = (TextView) findViewById(R.id.tv_coupon_date);
		tv_coupon_big_money = (TextView) findViewById(R.id.tv_coupon_big_money);
		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		handler.removeCallbacksAndMessages(null);
	}


	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.text_cancle:
				if(keyboard.isShowing()){
					keyboard.dismiss();
				}
				dismiss();
				break;
			case R.id.text_confirm:
				if(StringUtil.isEmpty(edit_money.getText().toString())){
					ToastUtils.sendtoastbyhandler(handler, "输入不能为空");
					edit_money.setText("");
					return;
				}
				double money = -1;
				try {
					money = Double.parseDouble(edit_money.getText().toString());
				}catch (Exception e){

				}
				if(money < 0){
					ToastUtils.sendtoastbyhandler(handler,"输入格式异常");
					edit_money.setText("");
				}else {
					if(money> Double.parseDouble(tv_coupon_big_money.getText().toString())){
						ToastUtils.sendtoastbyhandler(handler,"输入值不能大于最大可用金额");
						edit_money.setText("");
					}else {
						couponInfo.setAvailablemoney(money+"");
						couponCallback.doResult(couponInfo);
						dismiss();
					}
				}

				break;
			case R.id.imageview_close:
				dismiss();
				break;
		}
	}
}
