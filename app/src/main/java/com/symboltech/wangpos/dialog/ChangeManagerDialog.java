package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.LoginActivity;
import com.symboltech.wangpos.activity.WorkLogActivity;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class ChangeManagerDialog extends Dialog implements View.OnClickListener {
	private  DialogFinishCallBack callBack;
	public Context context;
	private TextView text_job_report, text_day_report, text_change_manager;
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
		text_job_report = (TextView) findViewById(R.id.text_job_report);
		text_day_report = (TextView) findViewById(R.id.text_day_report);
		text_change_manager = (TextView) findViewById(R.id.text_change_manager);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		text_job_report.setOnClickListener(this);
		text_day_report.setOnClickListener(this);
		text_change_manager.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
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
		}
		dismiss();
	}
}
