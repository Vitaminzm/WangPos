package com.symboltech.wangpos.http;



/**
 * 网络执行动作callback
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月23日
 * @see
 * @since 1.0
 * @param <T>
 */
public abstract class HttpActionHandle<T>  {
	//请求动作开始调用
	public abstract  void handleActionStart();

	//请求动作结束调用
	public abstract void handleActionFinish();

	//请求动作错误调用
	public void handleActionError(String actionName, String errmsg){

	}

	//请求动作成功时调用
	public void handleActionSuccess(String actionName, T result){

	}

}
