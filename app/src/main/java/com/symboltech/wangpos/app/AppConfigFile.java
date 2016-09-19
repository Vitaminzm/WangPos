package com.symboltech.wangpos.app;
/** 
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2016年1月19日 下午3:09:59 
* @version 1.0 
* 用于配置APP运行相关参数
*/
public class AppConfigFile {

	/**监测网络状态发送间隔*/
	public static final long NETWORK_STATUS_INTERVAL = 2 * 60 * 1000;
	
	/**用户登录信息保存时间*/
	public static final long USER_LOGIN_SAVE_TIME = 1000 * 60 * 60 * 24 * 15;
	
	/**操作日志上传时间间隔*/
	public static final long OPT_LOG_INTERVAL = 1000 * 10;
	
	/**离线数据保存时间*/
	public static final int OFFLINE_DATA_TIME = -7;
	
	/**离线数据单次上传条数*/
	public static final int OFFLINE_DATA_COUNT = 15;
	
	/**单个操作日志文件保存条数*/
	public static final int OPERATE_LOG_SIZE = 50;
	
}
