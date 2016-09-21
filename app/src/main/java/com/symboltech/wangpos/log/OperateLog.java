package com.symboltech.wangpos.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.msg.entity.LogInfo;
import com.symboltech.wangpos.msg.entity.OptLogInfo;
import com.symboltech.wangpos.result.LogResult;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
/**
 * 操作日志的上传
 * 
 * @author zmm
 * 
 */
public class OperateLog {
	private static Context context = MyApplication.context;
	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat formatContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static OperateLog operateLog;
	
	//用来记录当前存储日志总数
	private static int count = 0;
	//用来控制线程退出的
	private static boolean isRun = true;
	
	private Thread thread = null;
	public void setIsRun(boolean isrun){
		isRun = isrun;
	}
	
	private OperateLog(){
	}

	public static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;
			default:
				break;
			}
		};
	};
	public static OperateLog getInstance() {
		if (operateLog == null) {
			synchronized (OperateLog.class) {
				if (operateLog == null) {
					operateLog = new OperateLog();
				}
			}
		}
		return operateLog;
	}
	
	/**
	 * 保存离线数据
	 * TODO
	 */
//	public boolean saveLog2File(String opType,List<OfflineBillInfo> datas){
//		boolean ret = true;
//		for (OfflineBillInfo data : datas) {
//			if(!saveLog2File(opType, new Gson().toJson(data))) {
//				ret = false;
//				break;
//			}
//		}
//		return ret;
//	}
	
	/**
	 * 保存操作日志
	 * @param opType 操作日志类型
	 * @param msg	日志具体信息
	 *
	 */
	public boolean saveLog2File(String opType,String msg){
		
		Date date = new Date();
		String time = format.format(date);
		//当程序是刚刚启动需要，创建新的文件
		File dir = new File(context.getFilesDir(), "Log");
		dir.mkdirs();
		if(count == 0){
			File[] files = dir.listFiles();
			if(files != null && files.length > 0){
				//按时间先后顺序排序，先取出最后一次的写入的文件
				Collections.sort(Arrays.asList(files), new Comparator<File>() {
	                public int compare(File file, File newFile) {
	                    if (file.lastModified() < newFile.lastModified()) {
	                        return 1;
	                    } else if (file.lastModified() == newFile.lastModified()) {
	                        return 0;
	                    } else {
	                        return -1;
	                    }
	 
	                }
	            });
				String dex = "" + files.length;
				String[] fileName = files[0].getName().split("-");
				if(fileName.length >= 4){
					dex = fileName[3];
				}
				if(ArithDouble.parseInt(dex) >= 1000){
					count = AppConfigFile.OPERATE_LOG_SIZE;
				}else{
					count = (ArithDouble.parseInt(dex) + 1) * AppConfigFile.OPERATE_LOG_SIZE;
				}
			}
		}
		int num = count / AppConfigFile.OPERATE_LOG_SIZE;
		String fileName = time +"-"+ num;
		
		File file = new File(dir, fileName);
		OptLogInfo optLogInfo =new OptLogInfo();
		optLogInfo.setOperCode(SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, "0"));
		optLogInfo.setOpMsg(msg);
		//optLogInfo.setPosno(SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, "0"));
		optLogInfo.setOpType(opType);
		optLogInfo.setOpTime(formatContent.format(date));
		String json = null;
		if(!file.exists()){
			json = "[" + new Gson().toJson(optLogInfo);
		}else{
			json = "," + new Gson().toJson(optLogInfo);
		}
		count++;
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file.getAbsoluteFile(), true);//context.openFileOutput(file.getName(),  Context.MODE_PRIVATE + Context.MODE_APPEND);
			outputStream.write(json.getBytes());
			outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
            //关闭文件流
            if (outputStream != null){
                try {
                	outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
		return true;
	}
	
	/**
	 * 上传操作日志
	 * @param time 单个日志文件上传时间间隔  单位：ms
	 */
	public void DeleteLog(final long time){
		if(thread == null || !thread.isAlive()){
			thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					isRun = true;
					File file = new File(context.getFilesDir(), "Log");
					
					File[] files = file.listFiles();
					if (files == null || files.length <= 0) {
						return;
					}
					//按时间先后顺序排序
					Collections.sort(Arrays.asList(files), new Comparator<File>() {
		                public int compare(File file, File newFile) {
		                    if (file.lastModified() < newFile.lastModified()) {
		                        return -1;
		                    } else if (file.lastModified() == newFile.lastModified()) {
		                        return 0;
		                    } else {
		                        return 1;
		                    }
		 
		                }
		            });
					//尽量保留最后一个日志文件，因为程序在不停的写日志
					for (int i = 0;i < files.length - 1 ;i++) {
						File f = files[i];
						
						//服务退出时，能尽量及时退出线程
						if(!isRun || MyApplication.isOffLineMode() || !MyApplication.isNetConnect()){
							break;
						}
						saveOperationLog(f);
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();
		}
	}
	/**
	 * 读取日志文件并上传
	 * @param file 当前上传的文件
	 * 
	 */
	public static void saveOperationLog(File file){
		if(file == null){
			return;
		}
		if(file.isFile()){
			BufferedReader reader = null;
			StringBuffer data = new StringBuffer();
			try {
				FileInputStream inputStream = new FileInputStream(file);//context.openFileInput(file.getName());
				reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
				String temp = null;
	            while((temp = reader.readLine()) != null){
	                data.append(temp);
	            }
	            
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
	            //关闭文件流
	            if (reader != null){
	                try {
	                    reader.close();
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }
			if(data.toString()!=null && !TextUtils.isEmpty(data.toString())){
				Date date = new Date();
				String time = format.format(date);
				int num = count / AppConfigFile.OPERATE_LOG_SIZE;
				String fileName = time +"-"+ num;
				File fileTemp = new File(context.getFilesDir(), fileName);
				if(!fileTemp.getName().equals(file.getName()))
					send2server(file.getName(), data.toString()+"]");
			}else{
				file.delete();
			}
		}
		
	}
	
	/**
	 * 上传日志，成功后删除
	 * @param filename 发送文件名
	 * @param data 发送数组
	 */
	public static void send2server(String filename, String data){
		Map<String, String> map = new HashMap<String, String>();
		map.put("signid", filename);
		map.put("operations", data);
		HttpRequestUtil.getinstance().saveOperationLog(map, LogResult.class, new HttpActionHandle<LogResult>(){

			@Override
			public void handleActionStart() {

			}

			@Override
			public void handleActionFinish() {

			}

			@Override
			public void handleActionError(String actionName,
					String errmsg) {
				ToastUtils.sendtoastbyhandler(handler, errmsg);
			}

			@Override
			public void handleActionSuccess(String actionName,
					LogResult result) {
				if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())){
					LogInfo info = result.getLogInfo();
					if(info == null || info.getSignid()== null){
						return;
					}
					File file = new File(context.getFilesDir(), "Log");
					File fileResult = new File(file, info.getSignid());
					fileResult.delete();
				}else{
					ToastUtils.sendtoastbyhandler(handler, result.getMsg());
				}
			}
		});
	}
}
