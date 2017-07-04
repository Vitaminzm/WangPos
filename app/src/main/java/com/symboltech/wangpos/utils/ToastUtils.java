package com.symboltech.wangpos.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.symboltech.wangpos.log.LogUtil;

/** 
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年10月27日 上午11:17:01 
* @version 1.0  toast tools used diy tools
*/
public class ToastUtils {

	/** 用于响应show toast by handler obj.waht*/
	public static final int TOAST_WHAT = 618;
	public static final int TOAST_WHAT_DIALOG = 619;

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(sendtoastbyhandler)
	 * @param handler
	 * @param content
	 */
	public static void sendtoastbyhandler(Handler handler, String content){
		if(content == null || content.length() <= 0)
			return;
		Message msg = new Message();
		msg.what = TOAST_WHAT;
		msg.obj = content;
		handler.sendMessage(msg);
	}

	/**
	 *
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(sendtoastbyhandler)
	 * @param handler
	 * @param content
	 */
	public static void sendtoastdialogbyhandler(Handler handler, String content){
		if(content == null || content.length() <= 0)
			return;
		Message msg = new Message();
		msg.what = TOAST_WHAT_DIALOG;
		msg.obj = content;
		handler.sendMessage(msg);
	}
	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(showtaostbyhandler)
	 * @param context
	 * @param msg
	 */
	public static void showtaostbyhandler(Context context, Message msg){
		if(msg.obj != null && !StringUtil.isEmpty(msg.obj.toString())){
			Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
		}else {
			LogUtil.e(context.getPackageName(), "-------------showtaostbyhandler--msg---err");
		}
	}
	
	
}
