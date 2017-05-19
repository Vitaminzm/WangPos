package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.DemandNoteActivity;
import com.symboltech.wangpos.activity.WorkLogActivity;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.Utils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class ChangeManagerDialog extends BaseDialog implements View.OnClickListener {
	private  DialogFinishCallBack callBack;
	public Context context;
	private LinearLayout ll_dialog_change_manager;
	private TextView text_job_report, text_day_report, text_change_manager, dialog_change_manager_demand;
	private ImageView imageview_close;

	public ChangeManagerDialog(Context context, DialogFinishCallBack callBack) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.callBack =  callBack;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_change_manager);
		this.setCanceledOnTouchOutside(true);
		initUI();
	}

	private void initUI() {
		ll_dialog_change_manager = (LinearLayout) findViewById(R.id.ll_dialog_change_manager);
		dialog_change_manager_demand = (TextView) findViewById(R.id.dialog_change_manager_demand);
		text_job_report = (TextView) findViewById(R.id.text_job_report);
		text_day_report = (TextView) findViewById(R.id.text_day_report);
		text_change_manager = (TextView) findViewById(R.id.text_change_manager);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		dialog_change_manager_demand.setOnClickListener(this);
		text_job_report.setOnClickListener(this);
		text_day_report.setOnClickListener(this);
		text_change_manager.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
		if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(context, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
			ll_dialog_change_manager.setVisibility(View.GONE);
			dialog_change_manager_demand.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.text_job_report:
				Intent intentJob = new Intent(context, WorkLogActivity.class);
				intentJob.putExtra(ConstantData.FLAG, ConstantData.JOB);
				context.startActivity(intentJob);
				break;
			case R.id.text_day_report:
				Intent intentDay = new Intent(context, WorkLogActivity.class);
				intentDay.putExtra(ConstantData.FLAG, ConstantData.DAY);
				context.startActivity(intentDay);
				break;
			case R.id.text_change_manager:
				if (callBack!= null){
					callBack.finish(0);
				}
				break;
			case R.id.imageview_close:
				break;
			case R.id.dialog_change_manager_demand:
				Intent intent = new Intent(context, DemandNoteActivity.class);
				context.startActivity(intent);
				break;
		}
		dismiss();
	}
}
