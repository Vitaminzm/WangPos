package com.symboltechshop.wangpos.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.app.AppConfigFile;
import com.symboltechshop.wangpos.http.HttpActionHandle;
import com.symboltechshop.wangpos.utils.Utils;

/**
 * 切换销售模式
 * 
 */
public class ChangeModeDialog extends BaseDialog implements OnClickListener {

	private HttpActionHandle mHttpactionhandler;
	private TextView bt_yes, bt_no, tips;
	Context  mContext;
	/**是否切换到在线状态  默认不切换*/
	private boolean isOnLine = false;


	public ChangeModeDialog(Context context, HttpActionHandle httpactionhandler) {
		super(context, R.style.dialog_login_bg);
		this.mHttpactionhandler = httpactionhandler;
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_change_mode);
		isOnLine = AppConfigFile.isNetConnect();
		initView();
	}

	private void initView() {
		bt_yes = (TextView) findViewById(R.id.dialog_change_mode_yes);
		bt_no = (TextView) findViewById(R.id.dialog_change_mode_no);
		tips = (TextView) findViewById(R.id.dialog_change_mode_tips);
		if(isOnLine) {
			tips.setText(mContext.getResources().getString(R.string.change_mode_on_tips));
		}
		bt_yes.setOnClickListener(this);
		bt_no.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		switch (v.getId()) {
		case R.id.dialog_change_mode_yes:
			if(isOnLine) {
				//OperateLog.getInstance().saveLog2File(OptLogEnum.OFFLINE_OUT_OPT.getOptLogCode(), getString(R.string.offline_out_opt));
				AppConfigFile.setOffLineMode(false);
				if(mHttpactionhandler != null) {
					mHttpactionhandler.handleActionChangeToOffLine();
				}
			}else {
				//OperateLog.getInstance().saveLog2File(OptLogEnum.OFFLINE_IN_OPT.getOptLogCode(), getString(R.string.offline_in_opt));
				AppConfigFile.setOffLineMode(true);
				if(mHttpactionhandler != null) {
					mHttpactionhandler.handleActionChangeToOffLine();
				}
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
		dismiss();
	}

}
