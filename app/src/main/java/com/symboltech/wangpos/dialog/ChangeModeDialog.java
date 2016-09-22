package com.symboltech.wangpos.dialog;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;

/**
 * 切换销售模式
 * 
 */
public class ChangeModeDialog extends Activity implements OnClickListener {

	private TextView bt_yes, bt_no, tips;
	/**是否切换到在线状态  默认不切换*/
	private boolean isOnLine = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isOnLine = getIntent().getBooleanExtra(ConstantData.CHANGE_ONLINE_MODE, false);
		setContentView(R.layout.dialog_change_mode);
		initView();
	}

	private void initView() {
		bt_yes = (TextView) findViewById(R.id.dialog_change_mode_yes);
		bt_no = (TextView) findViewById(R.id.dialog_change_mode_no);
		tips = (TextView) findViewById(R.id.dialog_change_mode_tips);
		if(isOnLine) {
			tips.setText(getResources().getString(R.string.change_mode_on_tips));
		}
		bt_yes.setOnClickListener(this);
		bt_no.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_change_mode_yes:
			if(isOnLine) {
				//OperateLog.getInstance().saveLog2File(OptLogEnum.OFFLINE_OUT_OPT.getOptLogCode(), getString(R.string.offline_out_opt));
				MyApplication.setOffLineMode(false);
			}else {
				//OperateLog.getInstance().saveLog2File(OptLogEnum.OFFLINE_IN_OPT.getOptLogCode(), getString(R.string.offline_in_opt));
				MyApplication.setOffLineMode(true);
			}
			break;
		case R.id.dialog_change_mode_no:
			if(isOnLine) {
				//OperateLog.getInstance().saveLog2File(OptLogEnum.OFFLINE_OUT_CANCLEOPT.getOptLogCode(), getString(R.string.offline_out_cancleopt));
			}else{
				//OperateLog.getInstance().saveLog2File(OptLogEnum.OFFLINE_IN_CANCLEOPT.getOptLogCode(), getString(R.string.offline_in_cancleopt));
			}
			break;
		}
		finish();
	}

}
