package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.AddPayinfoAdapter;
import com.symboltech.wangpos.interfaces.CancleAndConfirmback;
import com.symboltech.wangpos.msg.entity.ThirdPayInfo;
import com.symboltech.wangpos.utils.Utils;

import java.util.List;

/**
 *
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class AddPayinfoDialog extends BaseDialog implements View.OnClickListener {
	private Context context;
	private ImageView imageview_close;
	private TextView text_title, text_cancle_pay, text_submit_order;
	private ListView listview_pay_info;
	private CancleAndConfirmback cancleAndConfirmback;
	private AddPayinfoAdapter adapter;
	private List<ThirdPayInfo> datas;

	public AddPayinfoDialog(Context context, List<ThirdPayInfo> datas, CancleAndConfirmback callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.datas = datas;
		this.cancleAndConfirmback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_add_payinfo);
		this.setCanceledOnTouchOutside(false);
		initUI();
		setdata();
	}

	private void setdata() {
		text_title.setText("支付信息");
		adapter = new AddPayinfoAdapter(context, datas);
		listview_pay_info.setAdapter(adapter);
	}

	private void initUI() {
		text_title = (TextView) findViewById(R.id.text_title);
		text_cancle_pay = (TextView) findViewById(R.id.text_cancle_pay);
		text_submit_order = (TextView) findViewById(R.id.text_submit_order);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		listview_pay_info = (ListView) findViewById(R.id.listview_pay_info);
		imageview_close.setOnClickListener(this);
		text_cancle_pay.setOnClickListener(this);
		text_submit_order.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.imageview_close:
				break;
			case R.id.text_cancle_pay:
				cancleAndConfirmback.doCancle();
				break;
			case R.id.text_submit_order:
				cancleAndConfirmback.doConfirm(null);
				break;
		}
		dismiss();
	}
}
