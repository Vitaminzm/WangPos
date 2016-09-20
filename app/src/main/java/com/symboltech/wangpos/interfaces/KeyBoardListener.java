package com.symboltech.wangpos.interfaces;

/**
 * 自定义键盘回调
 * @author so
 *
 */
public interface KeyBoardListener {
	/**
	 * 确认按钮
	 */
	void onComfirm();
	/**
	 * 取消按钮
	 */
	void onCancel();
	/**
	 * 没有控件的时候值回调
	 * @param value 值
	 */
	void onValue(String value);
}
