package com.symboltech.wangpos.print;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.DfqCoupon;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.MemberInfo;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.RefundReportInfo;
import com.symboltech.wangpos.msg.entity.ReportDetailInfo;
import com.symboltech.wangpos.msg.entity.ReportInfo;
import com.symboltech.wangpos.msg.entity.SaleReportInfo;
import com.symboltech.wangpos.msg.entity.TicketFormatInfo.Tickdatas;
import com.symboltech.wangpos.msg.entity.TicketFormatInfo.Tickdatas.TickbasicEntity;
import com.symboltech.wangpos.msg.entity.TicketFormatInfo.Tickdatas.TickbasicEntity.ConditionindexEntity;
import com.symboltech.wangpos.msg.entity.TotalReportInfo;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.TicketFormatEnum;
import com.symboltech.wangpos.utils.Utils;
import com.ums.upos.sdk.exception.CallServiceException;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.printer.BoldEnum;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.FontSizeEnum;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;
import com.ums.upos.sdk.system.BaseSystemManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.LatticePrinter.FontFamily;
import cn.weipass.pos.sdk.LatticePrinter.FontSize;
import cn.weipass.pos.sdk.LatticePrinter.FontStyle;

import static com.symboltech.wangpos.utils.StringUtil.formatLString;
import static com.symboltech.wangpos.utils.StringUtil.formatRString;

public class PrepareReceiptInfo {

	/** 以下2个值为硬件厂商定制，请勿修改！ */
	private static final int FONT_DEFAULT = 0x1111;
	private static final int FONT_BIG = 0x1124;

	public static String getPrintErrorInfo(int what, String info) {
		String message = "";
		switch (what) {
			case IPrint.EVENT_CONNECT_FAILD:
				message = "连接打印机失败";
				break;
			case IPrint.EVENT_CONNECTED:
				// Log.e("subscribe_msg", "连接打印机成功");
				break;
			case IPrint.EVENT_PAPER_JAM:
				message = "打印机卡纸";
				break;
			case IPrint.EVENT_UNKNOW:
				message = "打印机未知错误";
				break;
			case IPrint.EVENT_STATE_OK:
				//打印机状态正常
				break;
			case IPrint.EVENT_OK://
				// 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
				// 打印完成结束
				break;
			case IPrint.EVENT_NO_PAPER:
				message = "打印机缺纸";
				break;
			case IPrint.EVENT_HIGH_TEMP:
				message = "打印机高温";
				break;
			case IPrint.EVENT_PRINT_FAILD:
				message = "打印失败";
				break;
		}
		return message;
	}
	private static final String unit = "						元";
	/**
	 * 封装酷券核销小票打印信息
	 * @param
	 * @return
	 */
//	public static JSONObject getJsonReceipt(Context context, LatticePrinter latticePrinter, printer, fontConfig) {
//		JSONArray array = new JSONArray();
//
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "", KposPrinterManager.CONTENT_ALIGN_CENTER);
//		addTextJson(array, latticePrinter, FONT_BIG, "购物小票", KposPrinterManager.CONTENT_ALIGN_CENTER);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "用户联", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addDashLine(array, latticePrinter, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "商户名：测试商户demo", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "商户号：1234567890", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "终端号：111111", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "流水号：12345678", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "操作员：01", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "交易类型：消费", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addTextJson(array, latticePrinter, FONT_DEFAULT, "交易时间：2016-03-16 10:40:58",
//				KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addDashLine(array, latticePrinter, printer, fontConfig);
//		addMultiTextJson(array, latticePrinter, FONT_BIG, "金额：", "¥555"); // P8000不支持
//		addTextJson(array, latticePrinter, FONT_BIG, "金额：555.00元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//		addBitmapJson(array, BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher),
//				KposPrinterManager.CONTENT_ALIGN_CENTER);
//
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put("page", array);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return jsonObject;
//	}


	/**
	 * 加空行
	 * @param array
	 * @param latticePrinter
	 * @param printer
	 * @param fontConfig
	 */
	private static void addBlankLine(JSONArray array, LatticePrinter latticePrinter, PrinterManager printer, FontConfig fontConfig) {
		if(latticePrinter != null){
			latticePrinter.printText("\n", FontFamily.SONG,
					FontSize.MEDIUM, FontStyle.NORMAL);
		}else{
			if(printer == null){
				try {
					JSONObject json = new JSONObject();
					json.put("type", 3);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.setPrnText("\n", fontConfig);
				} catch (CallServiceException e) {
					e.printStackTrace();
				} catch (SdkException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 加分割线
	 * @param array
	 */
	private static void addDashLine(JSONArray array, LatticePrinter latticePrinter, PrinterManager printer, FontConfig fontConfig) {
		if(latticePrinter != null){
			latticePrinter.printText("------------------------------\n", FontFamily.SONG,
					FontSize.MEDIUM, FontStyle.BOLD);
		}else {
			if(printer == null){
				try {
					JSONObject json = new JSONObject();
					json.put("type", 2);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				fontConfig.setSize(FontSizeEnum.MIDDLE);
				try {
					printer.setPrnText("-------------------------------", fontConfig);
				} catch (CallServiceException e) {
					e.printStackTrace();
				} catch (SdkException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 打印图片
	 * @param array
	 * @param bitmap
	 * @param align
	 */
	private static void addBitmapJson(JSONArray array, Bitmap bitmap, int align, LatticePrinter latticePrinter, PrinterManager printer) {
		if(latticePrinter != null){
		}else {
			if(printer == null){
				try {
					JSONObject json = new JSONObject();
					json.put("type", 1);
					json.put("bitmap", bitmapToBase64(bitmap));
					json.put("align", align);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.setBitmap(bitmap);
				} catch (CallServiceException e) {
					e.printStackTrace();
				} catch (SdkException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 打印文字
	 * @param array
	 * @param size
	 * @param text
	 * @param align
	 */
	private static void addTextJson(JSONArray array, LatticePrinter latticePrinter, int size, String text, int align, PrinterManager printer, FontConfig fontConfig) {
		if(latticePrinter != null){
			if(FONT_BIG == size){
				latticePrinter.printText(text+"\n", FontFamily.SONG, FontSize.LARGE, FontStyle.NORMAL);
			}else{
				latticePrinter.printText(text+"\n", FontFamily.SONG, FontSize.MEDIUM, FontStyle.NORMAL);
			}
		}else {
			if(printer == null){
				try {
					JSONObject json = new JSONObject();
					json.put("type", 0);
					json.put("size", size);
					json.put("text", text);
					json.put("align", align);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				if(FONT_BIG == size){
					fontConfig.setSize(FontSizeEnum.BIG);
				}else{
					fontConfig.setSize(FontSizeEnum.MIDDLE);
				}
				try {
					printer.setPrnText(text, fontConfig);
				} catch (CallServiceException e) {
					e.printStackTrace();
				} catch (SdkException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 两端打印文字(部分机型不支持，谨慎使用)
	 * @param array
	 * @param size
	 * @param textLeftAlign
	 * @param textRightAlign
	 */
	private static void addMultiTextJson(JSONArray array, LatticePrinter latticePrinter, int size, String textLeftAlign, String textRightAlign, PrinterManager printer, FontConfig fontConfig) {
		if(latticePrinter != null){
			latticePrinter.printText(StringUtil.formatLString(16, textLeftAlign)+StringUtil.formatLString(10, textRightAlign.replace("	", ""))+"\n", FontFamily.SONG, FontSize.MEDIUM, FontStyle.NORMAL);
		}else {
			if(printer == null){
				try {
					JSONObject json = new JSONObject();
					json.put("type", 4);
					json.put("size", size);
					json.put("textLeftAlign", textLeftAlign);
					json.put("textRightAlign", textRightAlign);
					array.put(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				if(FONT_BIG == size){
					fontConfig.setSize(FontSizeEnum.BIG);
				}else{
					fontConfig.setSize(FontSizeEnum.MIDDLE);
				}
				try {
					printer.setPrnText(StringUtil.formatLString(16, textLeftAlign)+StringUtil.formatLString(10, textRightAlign.replace("	", "")), fontConfig);
				} catch (CallServiceException e) {
					e.printStackTrace();
				} catch (SdkException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * bitmap转为base64
	 * @param bitmap
	 * @return
	 */
	private static String bitmapToBase64(Bitmap bitmap) {
	    String result = null;
	    ByteArrayOutputStream baos = null;
	    try {
	        if (bitmap != null) {
	            baos = new ByteArrayOutputStream();
	            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

	            baos.flush();
	            baos.close();

	            byte[] bitmapBytes = baos.toByteArray();
	            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (baos != null) {
	                baos.flush();
	                baos.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return result;
	}


	/**
	 * 打印优惠券详情
	 */
	public static  JSONObject printCoupon(List<CouponInfo> couponInfos, LatticePrinter latticePrinter) {
		PrinterManager printer = null;
		FontConfig fontConfig = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
			fontConfig = new FontConfig();
			fontConfig.setBold(BoldEnum.BOLD);//不加粗
			fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		}
		JSONArray array = new JSONArray();
		for(CouponInfo couponInfo: couponInfos){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "优惠券名称:" + couponInfo.getName() + "	" + couponInfo.getAvailablemoney() + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);

			if(latticePrinter == null){
				if(printer == null){
					addBitmapJson(array, createQrcode(couponInfo.getCouponno(), 300, 300), KposPrinterManager.CONTENT_ALIGN_CENTER, latticePrinter, printer);
				}else{
					try {
						printer.setBitmap(createQrcode(couponInfo.getCouponno(), 300, 300));
					} catch (SdkException e) {
						e.printStackTrace();
					} catch (CallServiceException e) {
						e.printStackTrace();
					}
				}
			} else {
				latticePrinter.printQrCode(couponInfo.getCouponno(), 400, IPrint.Gravity.CENTER);
			}
			addTextJson(array, latticePrinter, FONT_DEFAULT, "起始时间:" + couponInfo.getBegindate() + ":" + couponInfo.getEnddate(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			if(!TextUtils.isEmpty(couponInfo.getContent()))
				addTextJson(array, latticePrinter, FONT_DEFAULT, "用券说明:" + couponInfo.getContent()	, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addBlankLine(array, latticePrinter, printer, fontConfig);
		}
		JSONObject jsonObject = new JSONObject();
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (SdkException e) {
					e.printStackTrace();
				} catch (CallServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}

	/**
	 * 生成二维码
	 * @param text
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap createQrcode(String text, int width, int height){
		Bitmap bitmap = null;
		QRCodeWriter writer = new QRCodeWriter();
		try {
			HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
			//提高容错等级
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			BitMatrix encode = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (encode.get(i, j)){
						bitmap.setPixel(i, j, Color.BLACK);
					} else {
						bitmap.setPixel(i, j, Color.WHITE);
					}
				}
			}
//			Bitmap ic = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//			Rect s = new Rect(0, 0, ic.getWidth(), ic.getHeight());
//			Rect dst = new Rect(80, 80, 120, 120);
//			new Canvas(bitmap).drawBitmap(ic, s, dst, null);
//			image.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 打印收款台缴款单
	 * @param total
	 */
	public static JSONObject printDemandNote(TotalReportInfo total, LatticePrinter latticePrinter){
		PrinterManager printer = null;
		FontConfig fontConfig = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
			fontConfig = new FontConfig();
			fontConfig.setBold(BoldEnum.BOLD);//不加粗
			fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		}
		JSONArray array = new JSONArray();
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款台号:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员系统码：" + SpSaveUtils.read(MyApplication.context, ConstantData.PERSON_XTM, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(MyApplication.context, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "门店:" + SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}else{
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "店铺:" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		addTextJson(array, latticePrinter, FONT_DEFAULT, "打印时间:" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addDashLine(array, latticePrinter, printer, fontConfig);
		if (total != null) {
			if (total.getTotalmoney() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总金额		" + total.getTotalmoney(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总金额		" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (total.getBillcount() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总笔数		" + total.getBillcount(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总笔数		" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		if (total.getTotallist() != null) {
			for (ReportDetailInfo info : total.getTotallist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addTextJson(array, latticePrinter, FONT_DEFAULT, StringUtil.formatLString(8, info.getName()) + "	"
							+ StringUtil.formatLString(8, info.getMoney().replaceAll("-", "")) + " 笔数：" + info.getCount(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}else{
					addTextJson(array, latticePrinter, FONT_DEFAULT, StringUtil.formatLString(8, info.getName()) + "	"
							+ StringUtil.formatLString(8, "0.00") + " 笔数：" + info.getCount(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}
		}
		addDashLine(array, latticePrinter, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收银员签字:", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "财务签字:", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		JSONObject jsonObject = new JSONObject();
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (SdkException e) {
					e.printStackTrace();
				} catch (CallServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}
	/**
	 * 打印班报日报
	 *
	 * @param isJob
	 *            是否是班报 班报打印收款员
	 * @param reportInfo
	 *            报表信息
	 */
	public static JSONObject printReportFrom(boolean isJob, ReportInfo reportInfo, LatticePrinter latticePrinter){
		PrinterManager printer = null;
		FontConfig fontConfig = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
			fontConfig = new FontConfig();
			fontConfig.setBold(BoldEnum.BOLD);//不加粗
			fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		}
		List<Tickdatas> ticketDatas = (List<Tickdatas>) SpSaveUtils.getObject(MyApplication.context,
				ConstantData.TICKET_FORMAT_LIST);
		if (ticketDatas != null && ticketDatas.size() > 0) {
			Tickdatas ticketFormat = null;
			for (Tickdatas info : ticketDatas) {
				if (isJob && info.getTicktype() != null && "5".equals(info.getTicktype().getId())) {
					ticketFormat = info;
				} else if (!isJob && info.getTicktype() != null && "6".equals(info.getTicktype().getId())) {
					ticketFormat = info;
				}
			}
			if (ticketFormat != null) {
				if (ticketFormat.getTickbasic() != null) {
					return getReport(isJob, reportInfo, ticketFormat, latticePrinter, printer, fontConfig);
				} else {
					return printReportFromDefault(isJob, reportInfo, latticePrinter, printer, fontConfig);
				}
			} else {
				return printReportFromDefault(isJob, reportInfo, latticePrinter, printer, fontConfig);
			}
		} else {
			return printReportFromDefault(isJob, reportInfo, latticePrinter, printer, fontConfig);
		}
	}
	public static JSONObject getReport(boolean isJob, ReportInfo reportInfo, Tickdatas ticketFormat, LatticePrinter latticePrinter, PrinterManager printer, FontConfig fontConfig){
		JSONArray array = new JSONArray();
		if(ticketFormat.getTickbegin()!= null){
			PrintString tickbegin = new PrintString(ticketFormat.getTickbegin());
			tickbegin.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
					.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
					.replace(TicketFormatEnum.TICKET_MALL_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))
					.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""))
					.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
			if(isJob){
				tickbegin.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""))
						.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""))
						.replace(TicketFormatEnum.TICKET_CASHER_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.PERSON_XTM, ""));

			}
			Tickdatas.TickbasicEntity basic = ticketFormat.getTickbasic();
			if(basic != null){
				String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
				tickbegin.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
						.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
						.replace(TicketFormatEnum.TICKET_PRINT_DATE.getLable(), getDateTime(date, basic.getDateformat(), true))
						.replace(TicketFormatEnum.TICKET_PRINT_TIME.getLable(), getDateTime(date, basic.getTimeformat(), false));
			}
			String[] codes = tickbegin.getString().split("\n");
			for (String code:codes){
				addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}

		if(ticketFormat.getReport() != null){

			PrintString report = new PrintString(ticketFormat.getReport());
			SaleReportInfo sale = reportInfo.getSale();
			if (sale != null) {
				if (sale.getTotalmoney() != null) {
					report.replace(TicketFormatEnum.TICKET_GET_TOTAL_MONEY.getLable(), formatRString(8,sale.getTotalmoney()));
				} else {
					report.replace(TicketFormatEnum.TICKET_GET_TOTAL_MONEY.getLable(), formatRString(8,"0"));
				}
				if (sale.getBillcount() != null) {
					report.replace(TicketFormatEnum.TICKET_GET_TOTAL_COUNT.getLable(), formatRString(8,sale.getBillcount()));
				} else {
					report.replace(TicketFormatEnum.TICKET_GET_TOTAL_COUNT.getLable(), formatRString(8,"0"));
				}
				Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_PAY_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_PAY_END.getLable());
				Matcher matcher = pattern.matcher(report.getString());
				String reportFormatallsall = null;
				String reportFormatsall = null;
				if(matcher.find()) {
					reportFormatallsall = matcher.group(0);
					reportFormatsall = matcher.group(1);
				}
				if(reportFormatsall != null && reportFormatallsall!= null){
					PrintString moneytemp = new PrintString(reportFormatallsall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
					if(sale.getSalelist() != null && sale.getSalelist().size() > 0){
						StringBuilder builder = new StringBuilder("");
						for(ReportDetailInfo infos:sale.getSalelist()){
							builder.append(formatLString(8, infos.getName()) +formatRString(8,infos.getMoney().replaceAll("-", "")));
//							if(reportFormatsall.contains("[笔数]")){
//								builder.append(" 笔数：").append(infos.getCount()+"\n");
//							}else{
//								builder.append("\n");
//							}
						}
						report.replace(moneytemp.getString(), builder.toString());
					}else{
						report.replace(moneytemp.getString(), "");
					}
				}

			}
			RefundReportInfo refund = reportInfo.getRefund();
			if (refund != null) {
				if (refund.getTotalmoney() != null) {
					report.replace(TicketFormatEnum.TICKET_RETURN_TOTAL_MONEY.getLable(), formatRString(8,refund.getTotalmoney().replaceAll("-", "")));
				} else {
					report.replace(TicketFormatEnum.TICKET_RETURN_TOTAL_MONEY.getLable(), formatRString(8,"0"));
				}
				if (refund.getBillcount() != null) {
					report.replace(TicketFormatEnum.TICKET_RETURN_TOTAL_COUNT.getLable(), formatRString(8,refund.getBillcount()));
				} else {
					report.replace(TicketFormatEnum.TICKET_RETURN_TOTAL_COUNT.getLable(), formatRString(8,"0"));
				}
				Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_RETURN_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_RETURN_END.getLable());
				Matcher matcher = pattern.matcher(report.getString());
				String reportFormatallref = null;
				String reportFormatref = null;
				if(matcher.find()) {
					reportFormatallref = matcher.group(0);
					reportFormatref = matcher.group(1);
				}
				if(reportFormatref != null && reportFormatallref!= null){
					PrintString moneytemp = new PrintString(reportFormatallref).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
					if(refund.getRefundlist() != null && refund.getRefundlist().size() > 0){
						StringBuilder builder = new StringBuilder("");
						for(ReportDetailInfo infos:refund.getRefundlist()){
							builder.append(formatLString(8, infos.getName()) +formatRString(8,infos.getMoney().replaceAll("-", "")));
//							if(reportFormatref.contains("[笔数]")){
//								builder.append(" 笔数：").append(infos.getCount()+"\n");
//							}else{
//								builder.append("\n");
//							}
						}
						report.replace(moneytemp.getString(), builder.toString());
					}else{
						report.replace(moneytemp.getString(), "");
					}
				}

			}

			TotalReportInfo total = reportInfo.getTotal();
			if (total != null) {
				if (total.getTotalmoney() != null) {
					report.replace(TicketFormatEnum.TICKET_TOTAL_ADD_MONEY.getLable(), formatRString(8, total.getTotalmoney().replaceAll("-", "")));
				} else {
					report.replace(TicketFormatEnum.TICKET_TOTAL_ADD_MONEY.getLable(), formatRString(8, "0"));
				}
				if (total.getBillcount() != null) {
					report.replace(TicketFormatEnum.TICKET_TOTAL_ADD_COUNT.getLable(), formatRString(8,total.getBillcount()));
				} else {
					report.replace(TicketFormatEnum.TICKET_TOTAL_ADD_COUNT.getLable(), formatRString(8,"0"));
				}
				Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_TOTAL_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_TOTAL_END.getLable());
				Matcher matcher = pattern.matcher(report.getString());
				String reportFormatalltol = null;
				String reportFormattol = null;
				if(matcher.find()) {
					reportFormatalltol = matcher.group(0);
					reportFormattol = matcher.group(1);
				}
				if(reportFormattol != null && reportFormatalltol != null){
					PrintString moneytemp = new PrintString(reportFormatalltol).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
					if(total.getTotallist() != null && total.getTotallist().size() > 0){
						StringBuilder builder = new StringBuilder("");
						for(ReportDetailInfo infos:total.getTotallist()){
							builder.append(formatLString(8, infos.getName()) +formatRString(8,infos.getMoney().replaceAll("-", "")));
//							if(reportFormattol.contains("[笔数]")){
//								builder.append(" 笔数：").append(infos.getCount()+"\n");
//							}else{
//								builder.append("\n");
//							}
						}
						report.replace(moneytemp.getString(), builder.toString());
					}else{
						report.replace(moneytemp.getString(), "");
					}
				}
			}
			TickbasicEntity basic = ticketFormat.getTickbasic();
			if(basic != null){
				report.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
						.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
			}
			report.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
			String[] codes = report.getString().split("\n");
			for (String code:codes){
				addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}

		if(ticketFormat.getTickend()!= null){
//			PrintString tickend = new PrintString(ticketFormat.getTickend());
//			tickend.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
//					.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
//					.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""))
//					.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
//			if(isJob){
//				tickend.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_CODE, ""))
//						.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""));
//			}
//			TickbasicEntity basic = ticketFormat.getTickbasic();
//			if(basic != null){
//				String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
//				tickend.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
//						.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
//						.replace(TicketFormatEnum.TICKET_PRINT_DATE.getLable(), getDateTime(date, basic.getDateformat(), true))
//						.replace(TicketFormatEnum.TICKET_PRINT_TIME.getLable(), getDateTime(date, basic.getTimeformat(), false));
//			}
//			String[] codes = tickend.getString().split("\n");
//			for (String code:codes){
//				addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//			}
		}
		addDashLine(array, latticePrinter, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收银员签字", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "财务签字", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		JSONObject jsonObject = new JSONObject();
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (SdkException e) {
					e.printStackTrace();
				} catch (CallServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}

	private static JSONObject printReportFromDefault(boolean isJob, ReportInfo reportInfo, LatticePrinter latticePrinter, PrinterManager printer, FontConfig fontConfig) {
		JSONArray array = new JSONArray();
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款台号:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);

		if (isJob) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}
		if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(MyApplication.context, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "门店:" + SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}else{
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "店铺:" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		addTextJson(array, latticePrinter, FONT_DEFAULT, "打印时间:" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addDashLine(array, latticePrinter, printer, fontConfig);
		// printSale
		SaleReportInfo sale = reportInfo.getSale();
		if (sale != null) {
			if (sale.getTotalmoney() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "收款总额：" + sale.getTotalmoney(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "收款总额：" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (sale.getBillcount() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "收款笔数：" + sale.getBillcount(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "收款笔数：" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		if (sale.getSalelist() != null) {
			for (ReportDetailInfo info : sale.getSalelist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addMultiTextJson(array, latticePrinter, FONT_DEFAULT, info.getName(), info.getMoney()+ unit, printer, fontConfig);
					//addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(8, info.getName()) + info.getMoney(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}
		}
		addDashLine(array, latticePrinter, printer, fontConfig);
		// printRefund
		RefundReportInfo refund = reportInfo.getRefund();
		if (refund != null) {
			if (refund.getTotalmoney() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "退款总额：" + refund.getTotalmoney().replaceAll("-", ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "退款总额：" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (refund.getBillcount() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "退款笔数：" + refund.getBillcount(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "退款笔数：" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		if (refund.getRefundlist() != null) {
			for (ReportDetailInfo info : refund.getRefundlist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addMultiTextJson(array, latticePrinter, FONT_DEFAULT,info.getName(),info.getMoney()+unit, printer, fontConfig);
					//addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(8, info.getName()) + info.getMoney().replaceAll("-", ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}
		}
		addDashLine(array, latticePrinter, printer, fontConfig);
		// printTotal
		TotalReportInfo total = reportInfo.getTotal();
		if (total != null) {
			if (total.getTotalmoney() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总金额：" + total.getTotalmoney().replaceAll("-", ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总金额：" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (total.getBillcount() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总笔数：" + total.getBillcount(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "总笔数：" + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		if (total.getTotallist() != null) {
			for (ReportDetailInfo info : total.getTotallist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addMultiTextJson(array, latticePrinter, FONT_DEFAULT,info.getName(),info.getMoney()+unit, printer, fontConfig);
					//addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(8, info.getName()) + info.getMoney().replaceAll("-", ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}
		}
		addDashLine(array, latticePrinter, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收银员签字", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "财务签字", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		JSONObject jsonObject = new JSONObject();
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (SdkException e) {
					e.printStackTrace();
				} catch (CallServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}
	/**
	* 打印订单
	*
	* @param bill
	* @param mend
	*            flase:打印 true:补打
	*/
	public static JSONObject printOrderList(BillInfo bill, final Boolean mend, LatticePrinter latticePrinter) {
		List<Tickdatas> ticketDatas = (List<Tickdatas>) SpSaveUtils.getObject(MyApplication.context, ConstantData.TICKET_FORMAT_LIST);
		if (ticketDatas != null && ticketDatas.size() > 0) {
			Tickdatas ticketFormat = null;
			for (Tickdatas info : ticketDatas) {
				if (!mend && info.getTicktype() != null && "1".equals(info.getTicktype().getId())) {
					ticketFormat = info;
				} else if (mend && info.getTicktype() != null && "2".equals(info.getTicktype().getId())) {
					ticketFormat = info;
				}
			}
			if (ticketFormat != null) {
				if (ticketFormat.getTickbasic() != null
						&& ticketFormat.getTickbasic().getConditionindex() != null
						&& ticketFormat.getTickbasic().getConditionindex().size() > 0) {
					return getOrder(bill, mend, ticketFormat, latticePrinter);
				} else {
					return printOrderListDefault(bill, mend, latticePrinter);
				}
			} else {
				return printOrderListDefault(bill, mend, latticePrinter);
			}
		} else {
			return printOrderListDefault(bill, mend, latticePrinter);
		}
	}

	private static void getOrderByCount(final int count, final JSONObject jsonObject, final BillInfo bill, final Boolean mend, final Tickdatas ticketFormat, final LatticePrinter latticePrinter) {
		JSONArray array = new JSONArray();
		PrinterManager printer = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		FontConfig fontConfig = new FontConfig();
		fontConfig.setBold(BoldEnum.BOLD);//不加粗
		fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		if(count == 1){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "[顾客联]"+"\n\n", KposPrinterManager.CONTENT_ALIGN_RIGHT, printer, fontConfig);
		}else if(count == 2){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "[专柜联]"+"\n\n", KposPrinterManager.CONTENT_ALIGN_RIGHT, printer, fontConfig);
		}else if(count == 3){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "[商场联]"+"\n\n", KposPrinterManager.CONTENT_ALIGN_RIGHT, printer, fontConfig);
		}
		int goodNumber = 0;
		double scoreUsed = 0;//积分抵扣
		double scoreValue = 0;//积分抵扣
		double changeMoney = 0;//找零
		double cardValue = 0;//代金券
		double totalPoint = 0;//积分累计
		double manjianMoney = 0;//满减金额
		String scorePay = null;
		String cardPay = null;
		if (bill.getGoodslist() != null) {
			for (GoodsInfo g : bill.getGoodslist()) {
				goodNumber += ArithDouble.parseInt(g.getSalecount());
				scoreUsed = ArithDouble.add(scoreUsed, ArithDouble.parseDouble(g.getUsedpoint()));
			}
		}
		if (bill.getPaymentslist() != null) {
			for (PayMentsInfo info : bill.getPaymentslist()) {
				if(PaymentTypeEnum.getpaymentstyle(info.getType()) == PaymentTypeEnum.COUPON){
					cardPay = info.getName();
					cardValue = ArithDouble.add(cardValue, ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())));
					if(cardPay == null){
						cardPay = "优惠券";
					}
				}else if (PaymentTypeEnum.getpaymentstyle(info.getType()) == PaymentTypeEnum.SCORE) {
					scorePay = info.getName();
					scoreValue = ArithDouble.add(scoreValue, ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())));
					if(scorePay == null){
						scorePay = "积分抵扣";
					}
				}
			}
		}
		manjianMoney = ArithDouble.parseDouble(bill.getTotalmbjmoney());
		if(bill.getChangemoney() != null){
			changeMoney = ArithDouble.parseDouble(bill.getChangemoney());
		}
		for(ConditionindexEntity info:ticketFormat.getTickbasic().getConditionindex()){
			if(info.getConditionid() != null && !"0".equals(info.getYxj())){
				int id = Integer.parseInt(info.getConditionid());
				switch (id) {
					case 1:
						if(ticketFormat.getTickbegin()!= null){
							PrintString tickbegin = new PrintString(ticketFormat.getTickbegin());
							tickbegin.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_MALL_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))
									.replace(TicketFormatEnum.TICKET_AUTH_CODE.getLable(), bill.getRandomcode())
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_CASHER_CODE.getLable(), bill.getCashierxtm())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
									.replace(TicketFormatEnum.TICKET_SALE_CODE.getLable(), bill.getSalemanxtm())
									.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
								tickbegin.replace(TicketFormatEnum.TICKET_WEB.getLable(), basic.getWebsite())
										.replace(TicketFormatEnum.TICKET_HOT_LINE.getLable(), basic.getHotline())
										.replace(TicketFormatEnum.TICKET_SALE_DATE.getLable(), getDateTime(bill.getSaletime(), basic.getDateformat(), true))
										.replace(TicketFormatEnum.TICKET_SALE_TIME.getLable(), getDateTime(bill.getSaletime(), basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDASALEMAN.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""))
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""))
										.replace(TicketFormatEnum.TICKET_BUDATIME.getLable(), getDateTime(date, basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDADATE.getLable(), getDateTime(date, basic.getDateformat(), true));
							}
							String[] codes = tickbegin.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 2:
						if(ticketFormat.getTickend()!= null){
							PrintString tickend = new PrintString(ticketFormat.getTickend());

							Pattern patternuse = Pattern.compile(TicketFormatEnum.TICKET_DFQ_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_DFQ_COUPON_END.getLable());
							Matcher matcheruse = patternuse.matcher(tickend.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcheruse.find()) {
								couponFormatall = matcheruse.group(0);
								couponFormat = matcheruse.group(1);
							}
							if(couponFormat != null && bill.getDfqlist() != null && bill.getDfqlist().size() > 0){
								StringBuilder builder = new StringBuilder().append("请确认待返券信息\n");
								for(DfqCoupon infos:bill.getDfqlist()){
									PrintString coupon = new PrintString(couponFormat).replace(TicketFormatEnum.TICKET_DFQ_COUPON_NAME.getLable(), infos.getDfqgzname())
											.replace(TicketFormatEnum.TICKET_DFQ_COUPON_MONEY.getLable(), formatLString(8, infos.getDfqmoney()));
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								tickend.replace(coupontemp.getString(), builder.toString());
							}else if(couponFormatall != null){
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								tickend.replace(coupontemp.getString(), "");
							}
							tickend.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_MALL_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_AUTH_CODE.getLable(), bill.getRandomcode())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_CASHER_CODE.getLable(), bill.getCashierxtm())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
									.replace(TicketFormatEnum.TICKET_SALE_CODE.getLable(), bill.getSalemanxtm())
									.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
								tickend.replace(TicketFormatEnum.TICKET_WEB.getLable(), basic.getWebsite())
										.replace(TicketFormatEnum.TICKET_HOT_LINE.getLable(), basic.getHotline())
										.replace(TicketFormatEnum.TICKET_SALE_DATE.getLable(), getDateTime(bill.getSaletime(), basic.getDateformat(), true))
										.replace(TicketFormatEnum.TICKET_SALE_TIME.getLable(), getDateTime(bill.getSaletime(), basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDASALEMAN.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""))
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""))
										.replace(TicketFormatEnum.TICKET_BUDATIME.getLable(), getDateTime(date, basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDADATE.getLable(), getDateTime(date, basic.getDateformat(), true));
							}
							String[] codes = tickend.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 3:
						if(ticketFormat.getGoods() != null){
							PrintString goods = new PrintString(ticketFormat.getGoods());
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								goods.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n");
							}
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_GOOD_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_GOOD_END.getLable());
							Matcher matcher = pattern.matcher(goods.getString());
							String goodFormat = null;
							String goodFormatall = null;
							if(matcher.find()) {
								goodFormatall = matcher.group(0);
								goodFormat = matcher.group(1);
							}
							if(goodFormat != null && bill.getGoodslist() != null && bill.getGoodslist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(GoodsInfo infos:bill.getGoodslist()){
									PrintString good = new PrintString(goodFormat).replace(TicketFormatEnum.TICKET_GOOD_NAME.getLable(), infos.getGoodsname())
											.replace(TicketFormatEnum.TICKET_GOOD_CODE.getLable(), formatLString(8, infos.getCode()))
											.replace(TicketFormatEnum.TICKET_COUNT.getLable(),  formatRString(4, infos.getSalecount()))
											.replace(TicketFormatEnum.TICKET_MONEY.getLable(), formatRString(8, infos.getSaleamt()))
											.replace(TicketFormatEnum.TICKET_USED_SCORE.getLable(), infos.getUsedpoint());
									builder.append(good.getString());
								}
								PrintString goodtemp = new PrintString(goodFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								goods.replace(goodtemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							}
							String[] codes = goods.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 4:
						if(ticketFormat.getMoneys() != null){
							PrintString moneys = new PrintString(ticketFormat.getMoneys()).replace("\\s*", "")
									.replace(TicketFormatEnum.TICKET_EXCHANGE.getLable(), "\t\t" + formatRString(8, bill.getChangemoney()))
									.replace(TicketFormatEnum.TICKET_TOTAL_COUNT.getLable(), formatRString(8, "" + goodNumber))
									.replace(TicketFormatEnum.TICKET_TOTAL_MONEY.getLable(), "\t"+formatRString(8,bill.getTotalmoney()))
									.replace(TicketFormatEnum.TICKET_TOTAL_USED_SCORE.getLable(), "    "+scoreUsed)
									.replace(TicketFormatEnum.TICKET_DEAL_MONEY.getLable(), "\t\t" +formatRString(8, ""+ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), scoreValue), cardValue), manjianMoney)))
									//.replace(TicketFormatEnum.TICKET_MANJIAN_MONEY.getLable(), "\t\t" +formatRString(8, ""+manjianMoney))
									.replace(TicketFormatEnum.TICKET_REAL_MONEY.getLable(), "\t\t" + formatRString(8, "" + ArithDouble.sub(ArithDouble.add(ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), scoreValue), cardValue), changeMoney),manjianMoney)));
//
							if(manjianMoney == 0){
								moneys.replace(TicketFormatEnum.TICKET_MANJIAN_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
							}else{
								moneys.replace(TicketFormatEnum.TICKET_MANJIAN_MONEY.getLable(), StringUtil.formatLString(16, "满减金额") + StringUtil.formatLString(10, "-" + manjianMoney));
							}
							if(scoreValue ==0){
								//moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable()+TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
							}else{
//								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable(), ""+scorePay)
//										.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable(), "\t" +formatRString(8, ""+scoreValue));
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable(), StringUtil.formatLString(16, "积分抵扣") + StringUtil.formatLString(10, "折扣:-" + scoreValue));
							}

							Pattern patternuse = Pattern.compile(TicketFormatEnum.TICKET_CASH_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_CASH_COUPON_END.getLable());
							Matcher matcheruse = patternuse.matcher(moneys.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcheruse.find()) {
								couponFormatall = matcheruse.group(0);
								couponFormat = matcheruse.group(1);
							}
							if(couponFormat != null && bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder().append("");
								int len = couponFormat.length();
								for(CouponInfo infos:bill.getUsedcouponlist()){
									builder.append(StringUtil.formatLString(16, infos.getName())+StringUtil.formatLString(10, "折扣:-"+infos.getAvailablemoney())+"\n");
									//builder.append(format(infos.getName()) +formatRString(8,infos.getAvailablemoney())+"\n");
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								moneys.replace(coupontemp.getString(), builder.toString());
							}else if(couponFormatall != null){
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								moneys.replace(coupontemp.getString(), "");
							}

							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_PAYTYPE_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_PAYTYPE_END.getLable());
							Matcher matcher = pattern.matcher(moneys.getString());
							String moneyFormatall = null;
							String moneyFormat = null;
							if(matcher.find()) {
								moneyFormatall = matcher.group(0);
								moneyFormat = matcher.group(1);
							}
							if(moneyFormat != null && bill.getPaymentslist() != null && bill.getPaymentslist().size() > 0){
								StringBuilder builder = new StringBuilder();
								int len = moneyFormat.length()/3;
								for(PayMentsInfo infos:bill.getPaymentslist()){
									if (PaymentTypeEnum.getpaymentstyle(infos.getType()) != PaymentTypeEnum.SCORE && PaymentTypeEnum.getpaymentstyle(infos.getType()) != PaymentTypeEnum.COUPON) {
										//builder.append(format(infos.getName()) +formatRString(8,infos.getMoney())+"\n");
										builder.append(StringUtil.formatLString(16, infos.getName())+StringUtil.formatLString(10, infos.getMoney().replace("	", ""))+"\n");
									}

								}
								PrintString moneytemp = new PrintString(moneyFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								moneys.replace(moneytemp.getString(), builder.toString());
							}
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								moneys.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n").replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n").replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							}
							String[] codes = moneys.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 5:
						if(ticketFormat.getVip() != null){
							PrintString vip = new PrintString(ticketFormat.getVip());
							if(bill.getMember() != null){
								vip.replace(TicketFormatEnum.TICKET_MEMBER_NO.getLable(), formatRString(8, bill.getMember().getMemberno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_NAME.getLable(), formatRString(8, bill.getMember().getMembername()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TEL.getLable(), formatRString(8, bill.getMember().getPhoneno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TYPE.getLable(), bill.getMember().getMembertypename());
								double addScore = 0, usedScore = 0, exchangeScore = 0;
								if (!TextUtils.isEmpty(bill.getAwardpoint())) {
									addScore = ArithDouble.parseDouble(bill.getAwardpoint());
								}
								if (!TextUtils.isEmpty(bill.getExchangedpoint())) {
									exchangeScore = ArithDouble.parseDouble(bill.getExchangedpoint());
								}
								if (!TextUtils.isEmpty(bill.getUsedpoint())) {
									usedScore = ArithDouble.parseDouble(bill.getUsedpoint());
									usedScore = ArithDouble.add(usedScore, exchangeScore);
								}

								if(addScore > 0){
									vip.replace(TicketFormatEnum.TICKET_ADD_SCORE.getLable(), formatRString(8, ""+ addScore));
								}else{
									vip.replace(TicketFormatEnum.TICKET_ADD_SCORE.getLable(), formatRString(8, "0.0"));
								}
								if(usedScore > 0){
									vip.replace(TicketFormatEnum.TICKET_USED_SCORE.getLable(), formatRString(8, ""+ usedScore));
								}else{
									vip.replace(TicketFormatEnum.TICKET_USED_SCORE.getLable(), formatRString(8, "0.0"));
								}
								totalPoint = ArithDouble.parseDouble(bill.getTotalpoint());
								vip.replace(TicketFormatEnum.TICKET_TOTAL_SCORE.getLable(), formatRString(8, ""+ totalPoint))
										.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									if(basic != null){
										vip.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
												.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
												.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n");
									}
								}
								String[] codes = vip.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					case 6:

						break;
					case 7:
						if(ticketFormat.getSendcoupon() != null){
							PrintString sendcoupon = new PrintString(ticketFormat.getSendcoupon());
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_COUPON_END.getLable());
							Matcher matcher = pattern.matcher(sendcoupon.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcher.find()) {
								couponFormatall = matcher.group(0);
								couponFormat = matcher.group(1);
							}
							if(couponFormat != null && bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(CouponInfo infos:bill.getGrantcouponlist()){
									PrintString coupon = new PrintString(couponFormat)
											.replace(TicketFormatEnum.TICKET_COUPON_NAME.getLable(), infos.getName())
											.replace(TicketFormatEnum.TICKET_COUPON_MONEY.getLable(), infos.getAvailablemoney()+"元")
											.replace(TicketFormatEnum.TICKET_COUPON_ENDDATE.getLable(), infos.getEnddate());
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								sendcoupon.replace(coupontemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									sendcoupon.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n");
								}
								String[] codes = sendcoupon.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					case 8:
						if(ticketFormat.getUsecoupon() != null){
							PrintString usedcoupon = new PrintString(ticketFormat.getUsecoupon());
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_COUPON_END.getLable());
							Matcher matcher = pattern.matcher(usedcoupon.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcher.find()) {
								couponFormatall = matcher.group(0);
								couponFormat = matcher.group(1);
							}
							if(couponFormat != null && bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(CouponInfo infos:bill.getUsedcouponlist()){
									PrintString coupon = new PrintString(couponFormat)
											.replace(TicketFormatEnum.TICKET_COUPON_NAME.getLable(), infos.getName())
											.replace(TicketFormatEnum.TICKET_COUPON_MONEY.getLable(), infos.getAvailablemoney()+"元");
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								usedcoupon.replace(coupontemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									usedcoupon.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n");
								}
								String[] codes = usedcoupon.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					case 9:
						if(ticketFormat.getOwncoupon() != null){
							PrintString owncoupon = new PrintString(ticketFormat.getOwncoupon());
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_COUPON_END.getLable());
							Matcher matcher = pattern.matcher(owncoupon.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcher.find()) {
								couponFormatall = matcher.group(0);
								couponFormat = matcher.group(1);
							}
							if(couponFormat != null && bill.getAllcouponlist() != null && bill.getAllcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(CouponInfo infos:bill.getAllcouponlist()){
									PrintString coupon = new PrintString(couponFormat)
											.replace(TicketFormatEnum.TICKET_COUPON_NAME.getLable(), infos.getName())
											.replace(TicketFormatEnum.TICKET_COUPON_YUE.getLable(), infos.getAvailablemoney()+"元")
											.replace(TicketFormatEnum.TICKET_COUPON_ENDDATE.getLable(), infos.getEnddate());
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								owncoupon.replace(coupontemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									owncoupon.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n");
								}
								String[] codes = owncoupon.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					case 11:
//						if(ticketFormat.getPark() != null && bill.getCarno() != null){
//							PrintString park = new PrintString(ticketFormat.getPark()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
//							park.replace(TicketFormatEnum.TICKET_PARK_COUPON_HOUR.getLable(), bill.getParkcouponhour())
//									.replace(TicketFormatEnum.TICKET_PARK_COUPON_NO.getLable(), bill.getCarno());
//							TickbasicEntity basic = ticketFormat.getTickbasic();
//							if(basic != null){
//								park.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n");
//							}
//							String[] codes = park.getString().split("\n");
//							for (String code:codes){
//								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//							}
//						}
						break;
					default:
						break;
				}
			}
		}
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							if(count != 0){
								if(count < ArithDouble.parseInt(ticketFormat.getPrintcount())){
									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											getOrderByCount(count + 1, jsonObject, bill, mend, ticketFormat, latticePrinter);
										}
									},2000);
									return;
								}
							}
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static JSONObject getOrder(BillInfo bill, Boolean mend, Tickdatas ticketFormat, LatticePrinter latticePrinter) {
		JSONObject jsonObject = new JSONObject();
		if(ArithDouble.parseInt(ticketFormat.getPrintcount()) > 1){
			getOrderByCount(1, jsonObject, bill, mend, ticketFormat, latticePrinter);
//			for(int i=1;i<ArithDouble.parseInt(ticketFormat.getPrintcount());i++){
//				if(i == 1){
//					getOrderByCount(2,jsonObject, bill, mend, ticketFormat, latticePrinter);
//				}else if(i == 2){
//					getOrderByCount(3,jsonObject, bill, mend, ticketFormat, latticePrinter);
//				}
//			}
		}else{
			getOrderByCount(0,jsonObject, bill, mend, ticketFormat, latticePrinter);
		}
		return jsonObject;
	}

	public static JSONObject printOrderListDefault(BillInfo bill, Boolean mend, LatticePrinter latticePrinter) {
		PrinterManager printer = null;
		FontConfig fontConfig = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
			fontConfig = new FontConfig();
			fontConfig.setBold(BoldEnum.BOLD);//不加粗
			fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		}
		JSONArray array = new JSONArray();
		int goodNumber = 0;
		double score = 0;// 积分抵扣
		double changeMoney = 0;// 找零
		double cardValue = 0;// 代金券
		double totalPoint = 0;// 积分累计
		addTextJson(array, latticePrinter, FONT_BIG, "欢迎光临" + SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""), KposPrinterManager.CONTENT_ALIGN_CENTER, printer, fontConfig);
		if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(MyApplication.context, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "门店:" + SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}else{
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "店铺:" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		//addTextJson(array, latticePrinter, FONT_DEFAULT, "小票号：" + bill.getBillid()+, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "小票号：" + bill.getBillid() + "    发票验证码:" + bill.getRandomcode(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款台号：" + bill.getPosno(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员：" + bill.getCashiername(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员系统码：" + bill.getCashierxtm(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "销售：" + bill.getSalemanname(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "日期：" + bill.getSaletime(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(6, "商品") + formatLString(6, "数量")
				+ formatLString(6, "金额") + "消耗积分", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		if (bill.getGoodslist() != null) {
			for (GoodsInfo g : bill.getGoodslist()) {
				goodNumber += ArithDouble.parseInt(g.getSalecount());
				addTextJson(array, latticePrinter, FONT_DEFAULT, g.getGoodsname(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, g.getCode())
						+ formatLString(6, g.getSalecount())
						+ formatLString(10, g.getSaleamt()) + g.getUsedpoint(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		if (bill.getGoodslist() != null) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "合计：") + goodNumber, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}
		if (mend) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "-----------补打小票-----------", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		} else {
			addDashLine(array, latticePrinter, printer, fontConfig);
		}

		if (!TextUtils.isEmpty(bill.getTotalmoney())) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "合计：", bill.getTotalmoney() + unit, printer, fontConfig);
		}
		if (bill.getPaymentslist() != null) {
			for (PayMentsInfo info : bill.getPaymentslist()) {
				if (PaymentTypeEnum.getpaymentstyle(info.getType()) == PaymentTypeEnum.COUPON) {
					cardValue = ArithDouble.add(cardValue, ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()),
							ArithDouble.parseDouble(info.getOverage())));
				} else if (PaymentTypeEnum.getpaymentstyle(info.getType()) == PaymentTypeEnum.SCORE) {
					score = ArithDouble.add(score, ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()),
							ArithDouble.parseDouble(info.getOverage())));
				}
			}
		}
		if (cardValue > 0) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "代金券：", cardValue + unit, printer, fontConfig);
			//addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "代金券：") + "	" + cardValue + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}

		if (score > 0) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "积分抵扣：", score + unit, printer, fontConfig);
			//addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "积分抵扣：") + "	" + score + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}

		if (bill.getChangemoney() != null) {
			changeMoney = ArithDouble.parseDouble(bill.getChangemoney());
		}
		if (ArithDouble.parseDouble(bill.getTotalmbjmoney()) > 0) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "满减金额：", bill.getTotalmbjmoney() + unit, printer, fontConfig);
		}
		addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "应付：", ArithDouble.sub(ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), score), cardValue), ArithDouble.parseDouble(bill.getTotalmbjmoney())) + unit, printer, fontConfig);

		addDashLine(array, latticePrinter, printer, fontConfig);
		addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "实付：", ArithDouble.sub(ArithDouble.add(ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), score), cardValue), changeMoney), ArithDouble.parseDouble(bill.getTotalmbjmoney())) + unit, printer, fontConfig);
		if (bill.getPaymentslist() != null) {
			for (PayMentsInfo info : bill.getPaymentslist()) {
				if (PaymentTypeEnum.getpaymentstyle(info.getType()) != PaymentTypeEnum.SCORE
						&& PaymentTypeEnum.getpaymentstyle(info.getType()) != PaymentTypeEnum.COUPON) {
					if (PaymentTypeEnum.getpaymentstyle(info.getType()) == PaymentTypeEnum.CASH) {
						if ("1".equals(info.getId())) {
							addMultiTextJson(array, latticePrinter, FONT_DEFAULT, info.getName(), (ArithDouble.parseDouble(info.getMoney()) + changeMoney) + unit, printer, fontConfig);
						} else {
							addMultiTextJson(array, latticePrinter, FONT_DEFAULT, info.getName(), ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())) + unit, printer, fontConfig);
						}
					} else {
						addMultiTextJson(array, latticePrinter, FONT_DEFAULT, info.getName(), ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())) + unit, printer, fontConfig);
					}
				}
			}
		}
		if (bill.getChangemoney() != null) {
			if (changeMoney > 0) {
				addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "找零", bill.getChangemoney()+unit, printer, fontConfig);
			}
		}

		MemberInfo member = bill.getMember();
		if (member != null) {
			double addScore = 0, usedScore = 0, exchangeScore = 0;
			addDashLine(array, latticePrinter, printer, fontConfig);
			if (!TextUtils.isEmpty(member.getMembertypename())) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "会员卡类型：") + "	" + member.getMembertypename(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (!TextUtils.isEmpty(member.getMemberno())) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "会员卡号：") + "	" + member.getMemberno(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (!TextUtils.isEmpty(bill.getAwardpoint())) {
				addScore = ArithDouble.parseDouble(bill.getAwardpoint());
				if (addScore > 0) {
					addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "新增积分：") + "	" + addScore, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}

			if (!TextUtils.isEmpty(bill.getUsedpoint())) {
				usedScore = ArithDouble.parseDouble(bill.getUsedpoint());
				if (!TextUtils.isEmpty(bill.getExchangedpoint())) {
					exchangeScore = ArithDouble.parseDouble(bill.getExchangedpoint());
					usedScore = ArithDouble.add(usedScore, exchangeScore);
				}
				if (usedScore > 0) {
					addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "消耗积分：") + "	" + usedScore, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}

			if(ArithDouble.parseDouble(bill.getTotalpoint()) >0){
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "累计积分：") + "	" + totalPoint, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
//			if (addScore != 0 || usedScore != 0) {
//				if(mend){
//					totalPoint = ArithDouble.sub(addScore, usedScore);
//					addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "本次累计：") + "	" + totalPoint, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//				}else{
//					totalPoint = ArithDouble.sub(addScore, usedScore);
//					addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "累计积分：") + "	" + ArithDouble.add(ArithDouble.parseDouble(bill.getMember().getCent_total()), totalPoint), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
//				}
//			}

		}
		if ((bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0)
				|| (bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0)) {
			addDashLine(array, latticePrinter, printer, fontConfig);
		}
		if (bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "使用券信息", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			for (CouponInfo info : bill.getUsedcouponlist()) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, info.getName()) + "	" + info.getAvailablemoney() + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}

		if (bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "新增券信息", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			for (CouponInfo info : bill.getGrantcouponlist()) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, info.getName()) + "	" + info.getAvailablemoney() + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		if(bill.getDfqlist() != null && bill.getDfqlist().size() >0){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "待返券信息", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			for (DfqCoupon info : bill.getDfqlist()) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, info.getDfqgzname()) + "	" + info.getDfqmoney() + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		//addTextJson(array, latticePrinter, FONT_DEFAULT, "发票验证码:" + bill.getRandomcode(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		if (mend) {
			addDashLine(array, latticePrinter, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, "补打收款员：" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, "补打时间：" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}
		addBlankLine(array, latticePrinter, printer, fontConfig);
		JSONObject jsonObject = new JSONObject();
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (SdkException e) {
					e.printStackTrace();
				} catch (CallServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}

	/**
	 * 打印退货单
	 *
	 * @param bill
	 * @param flag
	 *            flase:打印 true:补打
	 */
	public static JSONObject printBackOrderList(final BillInfo bill, final boolean flag, LatticePrinter latticePrinter) {

		List<Tickdatas> ticketDatas = (List<Tickdatas>) SpSaveUtils.getObject(MyApplication.context, ConstantData.TICKET_FORMAT_LIST);
		if (ticketDatas != null && ticketDatas.size() > 0) {
			Tickdatas ticketFormat = null;
			for (Tickdatas info : ticketDatas) {
				if (flag && info.getTicktype() != null && "4".equals(info.getTicktype().getId())) {
					ticketFormat = info;
				} else if (!flag && info.getTicktype() != null && "3".equals(info.getTicktype().getId())) {
					ticketFormat = info;
				}
			}
			if (ticketFormat != null) {
				if (ticketFormat.getTickbasic() != null
						&& ticketFormat.getTickbasic().getConditionindex() != null
						&& ticketFormat.getTickbasic().getConditionindex().size() > 0) {
					return getBackOrder(bill, flag, ticketFormat, latticePrinter);
				} else {
					return printBackOrderListDefault(bill, flag, latticePrinter);
				}
			} else {
				return printBackOrderListDefault(bill, flag, latticePrinter);
			}
		} else {
			return printBackOrderListDefault(bill, flag, latticePrinter);
		}
	}

	public static void getBackOrderByCount(final int count, final JSONObject jsonObject, final BillInfo bill, final boolean flag, final Tickdatas ticketFormat, final LatticePrinter latticePrinter){
		JSONArray array = new JSONArray();
		PrinterManager printer = null;
		FontConfig fontConfig = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
			fontConfig = new FontConfig();
			fontConfig.setBold(BoldEnum.BOLD);//不加粗
			fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		}
		if(count == 1){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "[顾客联]"+"\n\n", KposPrinterManager.CONTENT_ALIGN_RIGHT, printer, fontConfig);
		}else if(count == 2){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "[专柜联]"+"\n\n", KposPrinterManager.CONTENT_ALIGN_RIGHT, printer, fontConfig);
		}else if(count == 3){
			addTextJson(array, latticePrinter, FONT_DEFAULT, "[商场联]"+"\n\n", KposPrinterManager.CONTENT_ALIGN_RIGHT, printer, fontConfig);
		}
		double couponValue = 0;//优惠券
		double couponOverrage = 0;//优惠券溢余
		double deductionValue = 0;//积分抵扣
		String scorePay = null;
		double careduction = 0;//扣减现金
		String couponPay = null;
		double compensation = 0;//补偿金额
		double realMoney = 0;//真实金额
		double total=0;
		int goodCount = 0;
		for (GoodsInfo g : bill.getGoodslist()) {
			goodCount += ArithDouble.parseInt(g.getSalecount());
			double grantPoint = 0;
			double usedPoint = 0;
			grantPoint = ArithDouble.parseDouble(g.getGrantpoint());
			usedPoint = ArithDouble.parseDouble(g.getUsedpoint());
			total = ArithDouble.add(total, ArithDouble.sub(grantPoint, usedPoint));
		}
		List<PayMentsInfo> payments = bill.getPaymentslist();
		if (payments != null) {
			for (int i = 0; i < payments.size(); i++) {
				PayMentsInfo info = payments.get(i);
				if(PaymentTypeEnum.COUPON.equals(info.getType())) {
					try {
						couponValue = Math.abs(ArithDouble.parseDouble(info.getMoney()));
						couponPay = info.getName();
						couponOverrage = Double.parseDouble(info.getOverage());
					} catch (Exception e) {
					}
				}
				if(PaymentTypeEnum.SCORE.equals(info.getType())) {
					try {
						BigDecimal decimal = new BigDecimal(info.getMoney());
						deductionValue = Math.abs(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
						scorePay = info.getName();
						if(scorePay == null){
							scorePay = "积分抵扣";
						}
					} catch (Exception e) {
					}
				}
				if(PaymentTypeEnum.RECORDED_CAREDUCTION.equals(info.getType())) {
					try {
						BigDecimal decimal = new BigDecimal(info.getMoney());
						careduction = Math.abs(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
					} catch (Exception e) {
					}
				}
				if(PaymentTypeEnum.ALLWANCE_COMPENSATION.equals(info.getType())) {
					try {
						BigDecimal decimal = new BigDecimal(info.getMoney());
						compensation = Math.abs(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
					} catch (Exception e) {
					}
				}

			}
			try {
				BigDecimal decimal = new BigDecimal(bill.getTotalmoney().replaceAll("-", ""));
				realMoney = decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
				realMoney = realMoney -couponValue - deductionValue - careduction + compensation + couponOverrage;
			} catch (Exception e) {
			}
		}
		for(ConditionindexEntity info:ticketFormat.getTickbasic().getConditionindex()){
			if(info.getConditionid() != null && !"0".equals(info.getYxj())){
				int id  = Integer.parseInt(info.getConditionid());
				switch (id) {
					case 1:
						if(ticketFormat.getTickbegin()!= null){
							PrintString tickbegin = new PrintString(ticketFormat.getTickbegin());
							tickbegin.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_MALL_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_CASHER_CODE.getLable(), bill.getCashierxtm())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
									.replace(TicketFormatEnum.TICKET_SALE_CODE.getLable(), bill.getSalemanxtm())
									.replace(TicketFormatEnum.TICKET_RETURN_REASON.getLable(), bill.getBackreason())
									.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
								tickbegin.replace(TicketFormatEnum.TICKET_WEB.getLable(), basic.getWebsite())
										.replace(TicketFormatEnum.TICKET_HOT_LINE.getLable(), basic.getHotline())
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDASALEMAN.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""))
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""))
										.replace(TicketFormatEnum.TICKET_BUDATIME.getLable(), getDateTime(date, basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDADATE.getLable(), getDateTime(date, basic.getDateformat(), true))
										.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								if(flag){
									tickbegin.replace(TicketFormatEnum.TICKET_SALE_DATE.getLable(), getDateTime(bill.getSaletime(), basic.getDateformat(), true))
											.replace(TicketFormatEnum.TICKET_SALE_TIME.getLable(), getDateTime(bill.getSaletime(), basic.getTimeformat(), false));
								}else{
									tickbegin.replace(TicketFormatEnum.TICKET_SALE_DATE.getLable(), getDateTime(date, basic.getDateformat(), true))
											.replace(TicketFormatEnum.TICKET_SALE_TIME.getLable(), getDateTime(date, basic.getTimeformat(), false));
								}
							}
							String[] codes = tickbegin.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 2:
						if(ticketFormat.getTickend()!= null){
							PrintString tickend = new PrintString(ticketFormat.getTickend());
							Pattern patternuse = Pattern.compile(TicketFormatEnum.TICKET_DFQ_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_DFQ_COUPON_END.getLable());
							Matcher matcheruse = patternuse.matcher(tickend.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcheruse.find()) {
								couponFormatall = matcheruse.group(0);
								couponFormat = matcheruse.group(1);
							}
							if(couponFormat != null && bill.getDfqlist() != null && bill.getDfqlist().size() > 0){
								StringBuilder builder = new StringBuilder().append("请确认待返券信息\n");
								for(DfqCoupon infos:bill.getDfqlist()){
									PrintString coupon = new PrintString(couponFormat).replace(TicketFormatEnum.TICKET_DFQ_COUPON_NAME.getLable(), infos.getDfqgzname())
											.replace(TicketFormatEnum.TICKET_DFQ_COUPON_MONEY.getLable(), formatLString(8, infos.getDfqmoney()));
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								tickend.replace(coupontemp.getString(), builder.toString());
							}else if(couponFormatall != null){
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								tickend.replace(coupontemp.getString(), "");
							}
							tickend.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_MALL_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_CASHER_CODE.getLable(), bill.getCashierxtm())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
									.replace(TicketFormatEnum.TICKET_SALE_CODE.getLable(), bill.getSalemanxtm())
									.replace(TicketFormatEnum.TICKET_RETURN_REASON.getLable(), bill.getBackreason())
									.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), "\n")
									.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
								tickend.replace(TicketFormatEnum.TICKET_WEB.getLable(), basic.getWebsite())
										.replace(TicketFormatEnum.TICKET_HOT_LINE.getLable(), basic.getHotline())
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDASALEMAN.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""))
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, ""))
										.replace(TicketFormatEnum.TICKET_BUDATIME.getLable(), getDateTime(date, basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDADATE.getLable(), getDateTime(date, basic.getDateformat(), true))
										.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								if(flag){
									tickend.replace(TicketFormatEnum.TICKET_SALE_DATE.getLable(), getDateTime(bill.getSaletime(), basic.getDateformat(), true))
											.replace(TicketFormatEnum.TICKET_SALE_TIME.getLable(), getDateTime(bill.getSaletime(), basic.getTimeformat(), false));
								}else{
									tickend.replace(TicketFormatEnum.TICKET_SALE_DATE.getLable(), getDateTime(date, basic.getDateformat(), true))
											.replace(TicketFormatEnum.TICKET_SALE_TIME.getLable(), getDateTime(date, basic.getTimeformat(), false));
								}
							}
							String[] codes = tickend.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 3:
						if(ticketFormat.getGoods() != null){
							PrintString goods = new PrintString(ticketFormat.getGoods());
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								goods.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
							}
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_GOOD_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_GOOD_END.getLable());
							Matcher matcher = pattern.matcher(goods.getString());
							String goodFormatall = null;
							String goodFormat = null;
							if(matcher.find()) {
								goodFormatall = matcher.group(0);
								goodFormat = matcher.group(1);
							}
							if(goodFormat != null && bill.getGoodslist() != null && bill.getGoodslist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(GoodsInfo infos:bill.getGoodslist()){
									double grantPoint = 0;
									double usedPoint = 0;
									grantPoint = ArithDouble.parseDouble(infos.getGrantpoint());
									usedPoint = ArithDouble.parseDouble(infos.getUsedpoint());
									PrintString good = new PrintString(goodFormat).replace(TicketFormatEnum.TICKET_GOOD_NAME.getLable(), infos.getGoodsname())
											.replace(TicketFormatEnum.TICKET_GOOD_CODE.getLable(), formatLString(8, infos.getCode()))
											.replace(TicketFormatEnum.TICKET_COUNT.getLable(),  formatLString(4, infos.getSalecount()))
											.replace(TicketFormatEnum.TICKET_MONEY.getLable(), formatRString(8, infos.getSaleamt()))
											.replace(TicketFormatEnum.TICKET_RETURN_GOOD_SCORE.getLable(), ArithDouble.sub(grantPoint, usedPoint)+"");
									builder.append(good.getString());
								}
								PrintString goodstemp = new PrintString(goodFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								goods.replace(goodstemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							}
							String[] codes = goods.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 4:
						if(ticketFormat.getMoneys() != null){
							double exchange = 0.0;
							if(bill.getChangemoney() != null){
								exchange = ArithDouble.parseDouble(bill.getChangemoney());
							}
							PrintString moneys = new PrintString(ticketFormat.getMoneys()).replace("\\s*", "")
									.replace(TicketFormatEnum.TICKET_EXCHANGE.getLable(), "\t\t" + formatRString(8, ""+exchange))
									.replace(TicketFormatEnum.TICKET_TOTAL_COUNT.getLable(), "\t"+formatLString(4, ""+goodCount))
									.replace(TicketFormatEnum.TICKET_TOTAL_MONEY.getLable(), "\t" +formatRString(8,bill.getTotalmoney()))
									.replace(TicketFormatEnum.TICKET_TOTAL_RETURN_GOOD_SCORE.getLable(), "" +total)
									.replace(TicketFormatEnum.TICKET_RETURN_DEAL_MONEY.getLable(), "\t\t"+formatRString(8, MoneyAccuracyUtils.getmoneybytwo(realMoney)))
									.replace(TicketFormatEnum.TICKET_RETURN_REAL_MONEY.getLable(), "\t\t"+formatRString(8, MoneyAccuracyUtils.getmoneybytwo(realMoney)));
							if(deductionValue ==0){
								//moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable()+TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
							}else{
//								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable(), ""+scorePay)
//										.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable(), "\t" +formatRString(8, ""+scoreValue));
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable(), StringUtil.formatLString(16, "积分抵扣") + StringUtil.formatLString(10, "折扣:-" + deductionValue));
							}


							Pattern patternuse = Pattern.compile(TicketFormatEnum.TICKET_CASH_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_CASH_COUPON_END.getLable());
							Matcher matcheruse = patternuse.matcher(moneys.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcheruse.find()) {
								couponFormatall = matcheruse.group(0);
								couponFormat = matcheruse.group(1);
							}
							if(couponFormat != null && bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder().append("");
								if(couponOverrage > 0){
									builder.append("优惠券存在溢余("+couponOverrage+"元)");
								}
								for(CouponInfo infos:bill.getUsedcouponlist()){
									builder.append(StringUtil.formatLString(16, infos.getName())+StringUtil.formatLString(10, "折扣:-"+infos.getAvailablemoney().replace("	", ""))+"\n");
									//builder.append(format(infos.getName()) +formatRString(8,infos.getAvailablemoney())+"\n");
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								moneys.replace(coupontemp.getString(), builder.toString());
							}else if(couponFormatall != null){
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								moneys.replace(coupontemp.getString(), "");
							}

							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_RETURN_PAYTYPE_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_RETURN_PAYTYPE_END.getLable());
							Matcher matcher = pattern.matcher(moneys.getString());
							String moneyFormat = null;
							String moneyFormatall = null;
							if(matcher.find()) {
								moneyFormatall = matcher.group(0);
								moneyFormat = matcher.group(1);
							}
							if(moneyFormat != null && bill.getPaymentslist() != null && bill.getPaymentslist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(PayMentsInfo infos:bill.getPaymentslist()){
									String type = infos.getType();
									if(PaymentTypeEnum.COUPON.equals(type) || PaymentTypeEnum.SCORE.equals(type))
										continue;
									builder.append(StringUtil.formatLString(16, infos.getName())+StringUtil.formatLString(10, infos.getMoney().replace("	", ""))+"\n");
									//builder.append(format(infos.getName()) +formatRString(8,infos.getMoney())+"\n");
								}
								PrintString moneytemp = new PrintString(moneyFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								moneys.replace(moneytemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							}

							moneys.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								moneys.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
										.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
							}
							String[] codes = moneys.getString().split("\n");
							for (String code:codes){
								addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
							}
						}
						break;
					case 5:
						if(ticketFormat.getVip() != null){

							PrintString vip = new PrintString(ticketFormat.getVip());
							if(bill.getMember() != null){
								vip.replace(TicketFormatEnum.TICKET_MEMBER_NO.getLable(), formatRString(8, bill.getMember().getMemberno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_NAME.getLable(), formatRString(8, bill.getMember().getMembername()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TEL.getLable(), formatRString(8, bill.getMember().getPhoneno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TYPE.getLable(), bill.getMember().getMembertypename());
								double awardScore = 0;
								double usedScore = 0;
								double exchangeScore = 0;
								if (bill.getAwardpoint() != null) {
									awardScore = ArithDouble.parseDouble(bill.getAwardpoint().replaceAll("-", ""));
								}
								if (bill.getUsedpoint() != null) {
									usedScore = ArithDouble.parseDouble(bill.getUsedpoint().replaceAll("-", ""));
								}
								if (bill.getExchangedpoint() != null) {
									exchangeScore = ArithDouble.parseDouble(bill.getExchangedpoint().replaceAll("-", ""));
								}
								if(awardScore > 0){
									vip.replace(TicketFormatEnum.TICKET_RETURN_GOOD_SCORE.getLable(), formatRString(8, ""+ awardScore));
								}else{
									vip.replace(TicketFormatEnum.TICKET_RETURN_GOOD_SCORE.getLable(), formatRString(8, "0.0"));
								}
								if(usedScore > 0){
									vip.replace(TicketFormatEnum.TICKET_RETURN_SCORE.getLable(), formatRString(8, ""+ usedScore));
								}else{
									vip.replace(TicketFormatEnum.TICKET_RETURN_SCORE.getLable(), formatRString(8, "0.0"));
								}
								if(exchangeScore > 0){
									//积分抵扣没有result.append(StringFormatUtils.formatString(10, "抵扣积分：") + "	" + exchangeScore + "\n");
								}
								vip.replace(TicketFormatEnum.TICKET_TOTAL_SCORE.getLable(), formatRString(8, ""+ ArithDouble.parseDouble(bill.getTotalpoint())))
										.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									vip.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
											.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								}
								String[] codes = vip.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}

						}
						break;
					case 6:

						break;
					case 8:
						if(ticketFormat.getSendcoupon() != null){
							PrintString sendcoupon = new PrintString(ticketFormat.getSendcoupon());
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_COUPON_END.getLable());
							Matcher matcher = pattern.matcher(sendcoupon.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcher.find()) {
								couponFormatall = matcher.group(0);
								couponFormat = matcher.group(1);
							}
							if(couponFormat != null && bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(CouponInfo infos:bill.getGrantcouponlist()){
									PrintString coupon = new PrintString(couponFormat)
											.replace(TicketFormatEnum.TICKET_COUPON_NAME.getLable(), infos.getName())
											.replace(TicketFormatEnum.TICKET_COUPON_MONEY.getLable(), infos.getAvailablemoney()+"元");
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								sendcoupon.replace(coupontemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									sendcoupon.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
											.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								}
								String[] codes = sendcoupon.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					case 7:
						if(ticketFormat.getUsecoupon() != null){
							PrintString usedcoupon = new PrintString(ticketFormat.getUsecoupon());
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_COUPON_END.getLable());
							Matcher matcher = pattern.matcher(usedcoupon.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcher.find()) {
								couponFormatall = matcher.group(0);
								couponFormat = matcher.group(1);
							}
							if(couponFormat != null && bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(CouponInfo infos:bill.getUsedcouponlist()){
									PrintString coupon = new PrintString(couponFormat)
											.replace(TicketFormatEnum.TICKET_COUPON_NAME.getLable(), infos.getName())
											.replace(TicketFormatEnum.TICKET_COUPON_MONEY.getLable(), infos.getAvailablemoney()+"元")
											.replace(TicketFormatEnum.TICKET_COUPON_ENDDATE.getLable(), infos.getEnddate());
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								usedcoupon.replace(coupontemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									usedcoupon.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
											.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								}
								String[] codes = usedcoupon.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					case 9:
						if(ticketFormat.getOwncoupon() != null){
							PrintString owncoupon = new PrintString(ticketFormat.getOwncoupon());
							Pattern pattern = Pattern.compile(TicketFormatEnum.TICKET_COUPON_BEGIN.getLable()+"(.*)"+TicketFormatEnum.TICKET_COUPON_END.getLable());
							Matcher matcher = pattern.matcher(owncoupon.getString());
							String couponFormatall = null;
							String couponFormat = null;
							if(matcher.find()) {
								couponFormatall = matcher.group(0);
								couponFormat = matcher.group(1);
							}
							if(couponFormat != null && bill.getAllcouponlist() != null && bill.getAllcouponlist().size() > 0){
								StringBuilder builder = new StringBuilder();
								for(CouponInfo infos:bill.getAllcouponlist()){
									PrintString coupon = new PrintString(couponFormat)
											.replace(TicketFormatEnum.TICKET_COUPON_NAME.getLable(), infos.getName())
											.replace(TicketFormatEnum.TICKET_COUPON_YUE.getLable(), infos.getAvailablemoney()+"元")
											.replace(TicketFormatEnum.TICKET_COUPON_ENDDATE.getLable(), infos.getEnddate());
									builder.append(coupon.getString());
								}
								PrintString coupontemp = new PrintString(couponFormatall).replace("\\[", "\\\\[").replace("\\]", "\\\\]");
								owncoupon.replace(coupontemp.getString(), builder.toString()).replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									owncoupon.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
											.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								}
								String[] codes = owncoupon.getString().split("\n");
								for (String code:codes){
									addTextJson(array, latticePrinter, FONT_DEFAULT, code, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
								}
							}
						}
						break;
					default:
						break;
				}
			}
		}
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		addBlankLine(array, latticePrinter, printer, fontConfig);
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							if(count != 0){
								if(count < ArithDouble.parseInt(ticketFormat.getPrintcount())){
									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											getBackOrderByCount(count + 1, jsonObject, bill, flag, ticketFormat, latticePrinter);
										}
									}, 2000);
									return;
								}
							}
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static JSONObject getBackOrder(BillInfo bill, boolean mend, Tickdatas ticketFormat, LatticePrinter latticePrinter){
		JSONObject jsonObject = new JSONObject();
		if(ArithDouble.parseInt(ticketFormat.getPrintcount()) > 1){
			getBackOrderByCount(1, jsonObject, bill, mend, ticketFormat, latticePrinter);
		}else{
			getBackOrderByCount(0, jsonObject, bill, mend, ticketFormat, latticePrinter);
		}

		return jsonObject;
	}

	public static JSONObject printBackOrderListDefault(BillInfo bill, final boolean flag, LatticePrinter latticePrinter) {
		PrinterManager printer = null;
		FontConfig fontConfig = null;
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			printer = new PrinterManager();
			try {
				printer.initPrinter();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
			fontConfig = new FontConfig();
			fontConfig.setBold(BoldEnum.BOLD);//不加粗
			fontConfig.setSize(FontSizeEnum.MIDDLE);//小号字体
		}
		JSONArray array = new JSONArray();
		if(ConstantData.CASH_COLLECT.equals(SpSaveUtils.read(MyApplication.context, ConstantData.CASH_TYPE, ConstantData.CASH_NORMAL))){
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "门店:" + SpSaveUtils.read(MyApplication.context, ConstantData.MALL_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}else{
			if(StringUtil.isEmpty(SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))){
				addTextJson(array, latticePrinter, FONT_DEFAULT, "店铺:" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, "") , KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
		}
		addTextJson(array, latticePrinter, FONT_DEFAULT, "退货单号:" + bill.getBillid(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款台号:" + bill.getPosno(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		if (bill.getCashiername() != null && !"".equals(bill.getCashiername())) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员:" + bill.getCashiername(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		} else {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}
		if (bill.getSalemanname() != null) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "销售:" + bill.getSalemanname(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		} else {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "销售:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}
		addTextJson(array, latticePrinter, FONT_DEFAULT, "收款员系统码：" + bill.getCashierxtm(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, "日期：" + bill.getSaletime(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(8, "商品") + formatLString(4, "数量") + formatLString(6, "金额") + "消耗积分", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);

		double total = 0;
		int count = 0;
		for (GoodsInfo g : bill.getGoodslist()) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, g.getGoodsname(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			count += ArithDouble.parseInt(g.getSalecount());
			double grantPoint = 0;
			double usedPoint = 0;
			grantPoint = ArithDouble.parseDouble(g.getGrantpoint());
			usedPoint = ArithDouble.parseDouble(g.getUsedpoint());
			addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, g.getCode())
					+ formatLString(4, "-" + g.getSalecount().replaceAll("-", ""))
					+ formatLString(10, "-" + g.getSaleamt().replaceAll("-", ""))
					+ formatLString(6, ArithDouble.sub(grantPoint, usedPoint) + ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			total = ArithDouble.add(total, ArithDouble.sub(grantPoint, usedPoint));
		}
		if (flag) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "-----------补打小票-----------", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		} else {
			addDashLine(array, latticePrinter, printer, fontConfig);
		}
		addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(8, "合计") + formatLString(4, "-" + count)
				+ formatLString(10, "-" + bill.getTotalmoney().replaceAll("-", ""))
				+ formatLString(6, total + ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		double couponValue = 0;// 优惠券
		double couponOverrage = 0;//优惠券溢余
		double deductionValue = 0;// 积分抵扣
		double careduction = 0;// 扣减现金
		double compensation = 0;// 补偿金额
		double realMoney = 0;// 真实金额
		List<PayMentsInfo> payments = bill.getPaymentslist();
		if (payments != null) {
			for (int i = 0; i < payments.size(); i++) {
				PayMentsInfo info = payments.get(i);
				if (PaymentTypeEnum.COUPON.equals(info.getType())) {
					try {
						couponValue = Math.abs(Double.parseDouble(info.getMoney()));
						couponOverrage = Double.parseDouble(info.getOverage());
					} catch (Exception e) {
					}
				}
				if (PaymentTypeEnum.SCORE.equals(info.getType())) {
					try {
						BigDecimal decimal = new BigDecimal(info.getMoney());
						deductionValue = Math.abs(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
					} catch (Exception e) {
					}
				}
				if (PaymentTypeEnum.RECORDED_CAREDUCTION.equals(info.getType())) {
					try {
						BigDecimal decimal = new BigDecimal(info.getMoney());
						careduction = Math.abs(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
					} catch (Exception e) {
					}
				}
				if (PaymentTypeEnum.ALLWANCE_COMPENSATION.equals(info.getType())) {
					try {
						BigDecimal decimal = new BigDecimal(info.getMoney());
						compensation = Math.abs(decimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
					} catch (Exception e) {
					}
				}
			}
		}
		if (couponValue > 0) {
			if(couponOverrage > 0){
				addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "代金券：", couponValue + "(溢余" + couponOverrage + ")" + unit, printer, fontConfig);
			}else{
				addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "代金券：", couponValue + unit, printer, fontConfig);
			}
		}
		if (deductionValue > 0) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "积分抵扣：", deductionValue + unit, printer, fontConfig);
		}
		if (careduction > 0) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "扣减现金：", careduction + unit, printer, fontConfig);
		}
		if (compensation > 0) {
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "补偿金额：", compensation + unit, printer, fontConfig);
		}
		try {
			BigDecimal decimal = new BigDecimal(bill.getTotalmoney().replaceAll("-", ""));
			realMoney = decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
			realMoney = realMoney - couponValue - deductionValue - careduction + compensation + couponOverrage;
		} catch (Exception e) {
		}
		addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "应退：", MoneyAccuracyUtils.getmoneybytwo(realMoney) + unit, printer, fontConfig);
		addDashLine(array, latticePrinter, printer, fontConfig);
		addMultiTextJson(array, latticePrinter, FONT_DEFAULT, "实退：", MoneyAccuracyUtils.getmoneybytwo(realMoney) + unit, printer, fontConfig);
		for (PayMentsInfo info : bill.getPaymentslist()) {
			String type = info.getType();
			if (PaymentTypeEnum.COUPON.equals(type) || PaymentTypeEnum.SCORE.equals(type)
					|| PaymentTypeEnum.RECORDED_CAREDUCTION.equals(type)
					|| PaymentTypeEnum.ALLWANCE_COMPENSATION.equals(type))
				continue;
			addMultiTextJson(array, latticePrinter, FONT_DEFAULT, info.getName(), info.getMoney().replaceAll("-", "") + unit, printer, fontConfig);
		}
		addDashLine(array, latticePrinter, printer, fontConfig);
		MemberInfo member = bill.getMember();
		if (member != null) {
			if (member.getMemberno() != null) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "会员卡号:") + member.getMemberno(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			} else {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "会员卡号:") + 0, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			if (!TextUtils.isEmpty(member.getMembertypename())) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "会员卡类型：") + "	" + member.getMembertypename(), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			}
			double awardScore = 0;
			double usedScore = 0;
			double exchangeScore = 0;
			if (bill.getAwardpoint() != null) {
				awardScore = ArithDouble.parseDouble(bill.getAwardpoint().replaceAll("-", ""));
			}
			if (bill.getUsedpoint() != null) {
				usedScore = ArithDouble.parseDouble(bill.getUsedpoint().replaceAll("-", ""));
			}
			if (bill.getExchangedpoint() != null) {
				exchangeScore = ArithDouble.parseFloat(bill.getExchangedpoint().replaceAll("-", ""));
			}
			addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "退货积分:") + awardScore, KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "退回积分:") + ArithDouble.add(usedScore, exchangeScore), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, formatLString(10, "累计积分:") + ArithDouble.sub(ArithDouble.add(usedScore, exchangeScore), awardScore), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addDashLine(array, latticePrinter, printer, fontConfig);
		}
		List<CouponInfo> usedCoupon = bill.getUsedcouponlist();
		if (usedCoupon != null && usedCoupon.size() != 0) {
			addTextJson(array, latticePrinter, FONT_DEFAULT, "退回券", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			for (CouponInfo info : usedCoupon) {
				if (info.getName() != null && info.getAvailablemoney() != null) {
					addTextJson(array, latticePrinter, FONT_DEFAULT, info.getName() + "	" + info.getAvailablemoney().replaceAll("-", "") + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				}
			}
		}
		List<CouponInfo> grantCoupon = bill.getGrantcouponlist();
		if (usedCoupon != null && grantCoupon.size() != 0) {
			List<CouponInfo> printCoupon = null;
			for (CouponInfo info : grantCoupon) {
				if ("0".equals(info.getIsused())) {
					if (printCoupon == null) {
						printCoupon = new ArrayList<CouponInfo>();
					}
					printCoupon.add(info);
				}
			}
			if (printCoupon != null && printCoupon.size() != 0) {
				addTextJson(array, latticePrinter, FONT_DEFAULT, "收回券", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
				for (CouponInfo info : printCoupon) {
					if (info.getName() != null && info.getAvailablemoney() != null) {
						addTextJson(array, latticePrinter, FONT_DEFAULT, info.getName() + "	" + info.getAvailablemoney().replaceAll("-", "") + "元", KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
					}
				}
			}
		}

		if (flag) {
			addBlankLine(array, latticePrinter, printer, fontConfig);
			addBlankLine(array, latticePrinter, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, "补打收款员：" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
			addTextJson(array, latticePrinter, FONT_DEFAULT, "补打时间：" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), KposPrinterManager.CONTENT_ALIGN_LEFT, printer, fontConfig);
		}
		addBlankLine(array, latticePrinter, printer, fontConfig);
		JSONObject jsonObject = new JSONObject();
		if(latticePrinter!= null){
			// 真正提交打印事件
			latticePrinter.submitPrint();
		}else{
			if(printer == null){
				try {
					jsonObject.put("page", array);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				try {
					printer.startPrint(new OnPrintResultListener(){
						@Override
						public void onPrintResult(int arg0) {//arg0可见ServiceResult.java
							//登出，以免占用U架构服务
							try {
								MyApplication.isPrint = false;
								BaseSystemManager.getInstance().deviceServiceLogout();
							} catch (SdkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (SdkException e) {
					e.printStackTrace();
				} catch (CallServiceException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}


	public static class PrintString{
		private String print;
		PrintString(String print){
			this.print = print;
		}

		public PrintString replace(String target, String replacement){
			if(print == null || target == null){
				return this;
			}
			if(replacement == null){
				replacement = "null";
			}
			this.print = print.replaceAll(target, replacement);
			return this;
		}

		public String getString(){
			return print;
		}
	}

	/**
	 *
	 * @param date 时间字符串
	 * @param dateFormat 转换格式
	 * @param isDate 是否是日期，否则是时间
	 * @return
	 */
	public static String getDateTime(String date, String dateFormat, boolean isDate){
		String result =null;
		if(date == null){
			return result;
		}
		if(dateFormat == null){
			if(isDate){
				dateFormat = "yyyy-MM-dd";
			}else{
				dateFormat = "HH:mm:ss";
			}

		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			Date dateTime = simpleDateFormat.parse(date);
			result = new SimpleDateFormat(dateFormat).format(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String format(String str){
		if(str.length()<=3){
			str= str+"\t";
		}
		return str+"\t";
	}
}
