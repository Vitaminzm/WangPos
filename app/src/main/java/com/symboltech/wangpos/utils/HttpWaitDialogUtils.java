package com.symboltech.wangpos.utils;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.symboltech.wangpos.R;

/**
 *
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月13日 上午10:56:54
 * @version 1.0
 */
public class HttpWaitDialogUtils extends Dialog{

	public HttpWaitDialogUtils(Context context) {
		super(context, R.style.wait_dialog_bg);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httpwaitdialog);
		setCanceledOnTouchOutside(false);
		setCancelable(false);
	}
	
}
