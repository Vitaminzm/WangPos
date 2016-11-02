package com.symboltech.wangpos.print;

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

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.msg.entity.CouponInfo;
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
import com.symboltech.wangpos.utils.TicketFormatEnum;
import com.symboltech.wangpos.utils.Utils;

import static com.symboltech.wangpos.utils.StringUtil.formatLString;
import static com.symboltech.wangpos.utils.StringUtil.formatRString;

public class PrepareReceiptInfo {
	/** 以下2个值为硬件厂商定制，请勿修改！ */
	private static final int FONT_DEFAULT = 0x1111;
	private static final int FONT_BIG = 0x1124;

	private static final String unit = "						元";
	/**
	 * 封装酷券核销小票打印信息
	 * @param
	 * @return
	 */
	public static JSONObject getJsonReceipt(Context context) {
		JSONArray array = new JSONArray();

		addTextJson(array, FONT_DEFAULT, "", PrinterManager.CONTENT_ALIGN_CENTER);
		addTextJson(array, FONT_BIG, "购物小票", PrinterManager.CONTENT_ALIGN_CENTER);
		addTextJson(array, FONT_DEFAULT, "用户联", PrinterManager.CONTENT_ALIGN_LEFT);
		addDashLine(array);
		addTextJson(array, FONT_DEFAULT, "商户名：测试商户demo", PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "商户号：1234567890", PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "终端号：111111", PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "流水号：12345678", PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "操作员：01", PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "交易类型：消费", PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "交易时间：2016-03-16 10:40:58",
				PrinterManager.CONTENT_ALIGN_LEFT);
		addDashLine(array);
		addMultiTextJson(array, FONT_BIG, "金额：", "¥555" ); // P8000不支持
		addTextJson(array, FONT_BIG, "金额：555.00元", PrinterManager.CONTENT_ALIGN_LEFT);
		addBitmapJson(array, BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher),
				PrinterManager.CONTENT_ALIGN_CENTER);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	
	/**
	 * 加空行
	 * @param array
	 */
	private static void addBlankLine(JSONArray array) {
		try {
			JSONObject json = new JSONObject();
			json.put("type", 3);
			array.put(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 加分割线
	 * @param array
	 */
	private static void addDashLine(JSONArray array) {
		try {
			JSONObject json = new JSONObject();
			json.put("type", 2);
			array.put(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 打印图片
	 * @param array
	 * @param bitmap
	 * @param align
	 */
	private static void addBitmapJson(JSONArray array, Bitmap bitmap, int align) {
		try {
			JSONObject json = new JSONObject();
			json.put("type", 1);
			json.put("bitmap", bitmapToBase64(bitmap));
			json.put("align", align);
			array.put(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 打印文字
	 * @param array
	 * @param size
	 * @param text
	 * @param align
	 */
	private static void addTextJson(JSONArray array, int size, String text, int align) {
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
	}
	
	/**
	 * 两端打印文字(部分机型不支持，谨慎使用)
	 * @param array
	 * @param size
	 * @param textLeftAlign
	 * @param textRightAlign
	 */
	private static void addMultiTextJson(JSONArray array, int size, String textLeftAlign, String textRightAlign) {
		
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
	public static  JSONObject printCoupon(List<CouponInfo> couponInfos) {
		JSONArray array = new JSONArray();
		for(CouponInfo couponInfo: couponInfos){
			addBlankLine(array);
			addBlankLine(array);
			addTextJson(array, FONT_DEFAULT, "优惠券名称:" + couponInfo.getName() + "	" + couponInfo.getAvailablemoney() + "元", PrinterManager.CONTENT_ALIGN_LEFT);
			addTextJson(array, FONT_DEFAULT, SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
			addBitmapJson(array, createQrcode(couponInfo.getCouponno(), 300, 300), PrinterManager.CONTENT_ALIGN_CENTER);
			addTextJson(array, FONT_DEFAULT, "起始时间:" + couponInfo.getBegindate() + ":" + couponInfo.getEnddate(), PrinterManager.CONTENT_ALIGN_LEFT);
			addBlankLine(array);
			addBlankLine(array);
			addBlankLine(array);
			addBlankLine(array);
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.e("lgs", jsonObject.toString());
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
			HashMap<EncodeHintType, Object> hints = new HashMap<>();
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
	 * 打印班报日报
	 *
	 * @param isJob
	 *            是否是班报 班报打印收款员
	 * @param reportInfo
	 *            报表信息
	 */
	public static JSONObject printReportFrom(boolean isJob, ReportInfo reportInfo){
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
					return getReport(isJob, reportInfo, ticketFormat);
				} else {
					return printReportFromDefault(isJob, reportInfo);
				}
			} else {
				return printReportFromDefault(isJob, reportInfo);
			}
		} else {
			return printReportFromDefault(isJob, reportInfo);
		}
	}
	public static JSONObject getReport(boolean isJob, ReportInfo reportInfo, Tickdatas ticketFormat){
		JSONArray array = new JSONArray();

		addBlankLine(array);
		addBlankLine(array);

		if(ticketFormat.getTickbegin()!= null){
			PrintString tickbegin = new PrintString(ticketFormat.getTickbegin());
			tickbegin.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
					.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
					.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""))
					.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
			if(isJob){
				tickbegin.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_CODE, ""))
						.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""));
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
				addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
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
				addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
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
//				addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
//			}
		}
		addDashLine(array);
		addTextJson(array, FONT_DEFAULT, "收银员签字", PrinterManager.CONTENT_ALIGN_LEFT);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addTextJson(array, FONT_DEFAULT, "财务签字", PrinterManager.CONTENT_ALIGN_LEFT);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	private static JSONObject printReportFromDefault(boolean isJob, ReportInfo reportInfo) {
		JSONArray array = new JSONArray();
		addBlankLine(array);
		addBlankLine(array);
		addTextJson(array, FONT_DEFAULT, "收款台号:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_DESK_CODE, ""), PrinterManager.CONTENT_ALIGN_LEFT);

		if (isJob) {
			addTextJson(array, FONT_DEFAULT, "收款员:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
		}
		addTextJson(array, FONT_DEFAULT, "店铺:" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "打印时间:" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), PrinterManager.CONTENT_ALIGN_LEFT);
		addDashLine(array);
		// printSale
		SaleReportInfo sale = reportInfo.getSale();
		if (sale != null) {
			if (sale.getTotalmoney() != null) {
				addTextJson(array, FONT_DEFAULT, "收款总额：" + sale.getTotalmoney(), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, "收款总额：" + 0, PrinterManager.CONTENT_ALIGN_LEFT);
			}
			if (sale.getBillcount() != null) {
				addTextJson(array, FONT_DEFAULT, "收款笔数：" + sale.getBillcount(), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, "收款笔数：" + 0, PrinterManager.CONTENT_ALIGN_LEFT);
			}
		}
		if (sale.getSalelist() != null) {
			for (ReportDetailInfo info : sale.getSalelist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addMultiTextJson(array, FONT_DEFAULT, info.getName(), info.getMoney()+ unit);
					//addTextJson(array, FONT_DEFAULT, formatLString(8, info.getName()) + info.getMoney(), PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}
		}
		addDashLine(array);
		// printRefund
		RefundReportInfo refund = reportInfo.getRefund();
		if (refund != null) {
			if (refund.getTotalmoney() != null) {
				addTextJson(array, FONT_DEFAULT, "退款总额：" + refund.getTotalmoney().replaceAll("-", ""), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, "退款总额：" + 0, PrinterManager.CONTENT_ALIGN_LEFT);
			}
			if (refund.getBillcount() != null) {
				addTextJson(array, FONT_DEFAULT, "退款笔数：" + refund.getBillcount(), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, "退款笔数：" + 0, PrinterManager.CONTENT_ALIGN_LEFT);
			}
		}
		if (refund.getRefundlist() != null) {
			for (ReportDetailInfo info : refund.getRefundlist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addMultiTextJson(array, FONT_DEFAULT,info.getName(),info.getMoney()+unit);
					//addTextJson(array, FONT_DEFAULT, formatLString(8, info.getName()) + info.getMoney().replaceAll("-", ""), PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}
		}
		addDashLine(array);
		// printTotal
		TotalReportInfo total = reportInfo.getTotal();
		if (total != null) {
			if (total.getTotalmoney() != null) {
				addTextJson(array, FONT_DEFAULT, "总金额：" + total.getTotalmoney().replaceAll("-", ""), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, "总金额：" + 0, PrinterManager.CONTENT_ALIGN_LEFT);
			}
			if (total.getBillcount() != null) {
				addTextJson(array, FONT_DEFAULT, "总笔数：" + total.getBillcount(), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, "总笔数：" + 0, PrinterManager.CONTENT_ALIGN_LEFT);
			}
		}
		if (total.getTotallist() != null) {
			for (ReportDetailInfo info : total.getTotallist()) {
				if (info.getName() != null && info.getMoney() != null) {
					addMultiTextJson(array, FONT_DEFAULT,info.getName(),info.getMoney()+unit);
					//addTextJson(array, FONT_DEFAULT, formatLString(8, info.getName()) + info.getMoney().replaceAll("-", ""), PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}
		}
		addDashLine(array);
		addTextJson(array, FONT_DEFAULT, "收银员签字", PrinterManager.CONTENT_ALIGN_LEFT);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addTextJson(array, FONT_DEFAULT, "财务签字", PrinterManager.CONTENT_ALIGN_LEFT);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
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
	public static JSONObject printOrderList(BillInfo bill, final Boolean mend) {
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
					return getOrder(bill, mend, ticketFormat);
				} else {
					return printOrderListDefault(bill, mend);
				}
			} else {
				return printOrderListDefault(bill, mend);
			}
		} else {
			return printOrderListDefault(bill, mend);
		}
	}

	private static JSONObject getOrder(BillInfo bill, Boolean mend, Tickdatas ticketFormat) {
		JSONArray array = new JSONArray();
		int goodNumber = 0;
		double scoreUsed = 0;//积分抵扣
		double scoreValue = 0;//积分抵扣
		double changeMoney = 0;//找零
		double cardValue = 0;//代金券
		double totalPoint = 0;//积分累计
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
		if(bill.getChangemoney() != null){
			changeMoney = ArithDouble.parseDouble(bill.getChangemoney());
		}
		addBlankLine(array);
		addBlankLine(array);
		for(ConditionindexEntity info:ticketFormat.getTickbasic().getConditionindex()){
			if(info.getConditionid() != null && !"0".equals(info.getYxj())){
				switch (info.getConditionid()) {
					case "1":
						if(ticketFormat.getTickbegin()!= null){
							PrintString tickbegin = new PrintString(ticketFormat.getTickbegin());
							tickbegin.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
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
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_CODE, ""))
										.replace(TicketFormatEnum.TICKET_BUDATIME.getLable(), getDateTime(date, basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDADATE.getLable(), getDateTime(date, basic.getDateformat(), true));
							}
							String[] codes = tickbegin.getString().split("\n");
							for (String code:codes){
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "2":
						if(ticketFormat.getTickend()!= null){
							PrintString tickend = new PrintString(ticketFormat.getTickend());
							tickend.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
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
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_CODE, ""))
										.replace(TicketFormatEnum.TICKET_BUDATIME.getLable(), getDateTime(date, basic.getTimeformat(), false))
										.replace(TicketFormatEnum.TICKET_BUDADATE.getLable(), getDateTime(date, basic.getDateformat(), true));
							}
							String[] codes = tickend.getString().split("\n");
							for (String code:codes){
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "3":
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
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "4":
						if(ticketFormat.getMoneys() != null){
							PrintString moneys = new PrintString(ticketFormat.getMoneys()).replace("\\s*", "")
									.replace(TicketFormatEnum.TICKET_EXCHANGE.getLable(), "\t\t" + formatRString(8, bill.getChangemoney()))
									.replace(TicketFormatEnum.TICKET_TOTAL_COUNT.getLable(), formatRString(8,""+goodNumber))
									.replace(TicketFormatEnum.TICKET_TOTAL_MONEY.getLable(), "\t"+formatRString(8,bill.getTotalmoney()))
									.replace(TicketFormatEnum.TICKET_TOTAL_USED_SCORE.getLable(), "    "+scoreUsed)
									.replace(TicketFormatEnum.TICKET_DEAL_MONEY.getLable(), "\t\t" +formatRString(8, ""+ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), scoreValue), cardValue)))
									.replace(TicketFormatEnum.TICKET_REAL_MONEY.getLable(), "\t\t" +formatRString(8, ""+ArithDouble.add(ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), scoreValue), cardValue), changeMoney)));
//
							if(scoreValue ==0){
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable()+TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
							}else{
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable(), ""+scorePay)
										.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable(), "\t" +formatRString(8, ""+scoreValue));
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
									builder.append(format(infos.getName()) +formatRString(8,infos.getAvailablemoney())+"\n");
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
										builder.append(format(infos.getName()) +formatRString(8,infos.getMoney())+"\n");
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
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "5":
						if(ticketFormat.getVip() != null){
							PrintString vip = new PrintString(ticketFormat.getVip());
							if(bill.getMember() != null){
								vip.replace(TicketFormatEnum.TICKET_MEMBER_NO.getLable(), formatRString(8, bill.getMember().getMemberno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_NAME.getLable(), formatRString(8, bill.getMember().getMembername()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TEL.getLable(), formatRString(8, bill.getMember().getPhoneno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TYPE.getLable(), formatRString(8, bill.getMember().getMembertype()));
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
								totalPoint = ArithDouble.sub(addScore, usedScore);
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					case "6":

						break;
					case "7":
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					case "8":
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					case "9":
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					case "11":
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
//								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
//							}
//						}
						break;
					default:
						break;
				}
			}
		}
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public static JSONObject printOrderListDefault(BillInfo bill, Boolean mend) {
		JSONArray array = new JSONArray();
		int goodNumber = 0;
		double score = 0;// 积分抵扣
		double changeMoney = 0;// 找零
		double cardValue = 0;// 代金券
		double totalPoint = 0;// 积分累计
		addBlankLine(array);
		addBlankLine(array);
		addTextJson(array, FONT_BIG, "欢迎光临" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""), PrinterManager.CONTENT_ALIGN_CENTER);
		addTextJson(array, FONT_DEFAULT, "店铺：" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "订单号：" + bill.getBillid(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "收款台号：" + bill.getPosno(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "收款员：" + bill.getCashiername(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "销售：" + bill.getSalemanname(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "日期：" + bill.getSaletime(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, formatLString(6, "商品") + formatLString(6, "数量")
				+ formatLString(6, "金额") + "消耗积分", PrinterManager.CONTENT_ALIGN_LEFT);
		if (bill.getGoodslist() != null) {
			for (GoodsInfo g : bill.getGoodslist()) {
				goodNumber += ArithDouble.parseInt(g.getSalecount());
				addTextJson(array, FONT_DEFAULT, g.getGoodsname(), PrinterManager.CONTENT_ALIGN_LEFT);
				addTextJson(array, FONT_DEFAULT, formatLString(10, g.getCode())
						+ formatLString(6, g.getSalecount())
						+ formatLString(10, g.getSaleamt()) + g.getUsedpoint(), PrinterManager.CONTENT_ALIGN_LEFT);
			}
		}
		if (bill.getGoodslist() != null) {
			addTextJson(array, FONT_DEFAULT, formatLString(10, "合计：") + "	" + goodNumber, PrinterManager.CONTENT_ALIGN_LEFT);
		}
		if (mend) {
			addTextJson(array, FONT_DEFAULT, "----------补打小票--------------", PrinterManager.CONTENT_ALIGN_LEFT);
		} else {
			addTextJson(array, FONT_DEFAULT, "--------------------------------", PrinterManager.CONTENT_ALIGN_LEFT);
		}

		if (!TextUtils.isEmpty(bill.getTotalmoney())) {
			addMultiTextJson(array, FONT_DEFAULT, "合计：", bill.getTotalmoney() + unit);
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
			addMultiTextJson(array, FONT_DEFAULT, "代金券：", cardValue + unit);
			//addTextJson(array, FONT_DEFAULT, formatLString(10, "代金券：") + "	" + cardValue + "元", PrinterManager.CONTENT_ALIGN_LEFT);
		}

		if (score > 0) {
			addMultiTextJson(array, FONT_DEFAULT, "积分抵扣：", score + unit);
			//addTextJson(array, FONT_DEFAULT, formatLString(10, "积分抵扣：") + "	" + score + "元", PrinterManager.CONTENT_ALIGN_LEFT);
		}

		if (bill.getChangemoney() != null) {
			changeMoney = ArithDouble.parseDouble(bill.getChangemoney());
		}
		addMultiTextJson(array, FONT_DEFAULT, "应付：", ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), score), cardValue) + unit);

		addDashLine(array);
		addMultiTextJson(array, FONT_DEFAULT, "实付：", ArithDouble.add(ArithDouble.sub(ArithDouble.sub(ArithDouble.parseDouble(bill.getTotalmoney()), score), cardValue), changeMoney) + unit);
		if (bill.getPaymentslist() != null) {
			for (PayMentsInfo info : bill.getPaymentslist()) {
				if (PaymentTypeEnum.getpaymentstyle(info.getType()) != PaymentTypeEnum.SCORE
						&& PaymentTypeEnum.getpaymentstyle(info.getType()) != PaymentTypeEnum.COUPON) {
					if (PaymentTypeEnum.getpaymentstyle(info.getType()) == PaymentTypeEnum.CASH) {
						if ("1".equals(info.getId())) {
							addMultiTextJson(array, FONT_DEFAULT, info.getName(), (ArithDouble.parseDouble(info.getMoney()) + changeMoney) + unit);
						} else {
							addMultiTextJson(array, FONT_DEFAULT, info.getName(), ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())) + unit);
						}
					} else {
						addMultiTextJson(array, FONT_DEFAULT, info.getName(), ArithDouble.sub(ArithDouble.parseDouble(info.getMoney()), ArithDouble.parseDouble(info.getOverage())) + unit);
					}
				}
			}
		}
		if (bill.getChangemoney() != null) {
			if (changeMoney > 0) {
				addMultiTextJson(array, FONT_DEFAULT, "找零", bill.getChangemoney()+unit);
			}
		}

		MemberInfo member = bill.getMember();
		if (member != null) {
			double addScore = 0, usedScore = 0, exchangeScore = 0;
			addDashLine(array);
			if (!TextUtils.isEmpty(member.getMemberno())) {
				addTextJson(array, FONT_DEFAULT, formatLString(10, "会员卡号：") + "	" + member.getMemberno(), PrinterManager.CONTENT_ALIGN_LEFT);
			}
			if (!TextUtils.isEmpty(bill.getAwardpoint())) {
				addScore = ArithDouble.parseDouble(bill.getAwardpoint());
				if (addScore > 0) {
					addTextJson(array, FONT_DEFAULT, formatLString(10, "新增积分：") + "	" + addScore, PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}

			if (!TextUtils.isEmpty(bill.getUsedpoint())) {
				usedScore = ArithDouble.parseDouble(bill.getUsedpoint());
				if (!TextUtils.isEmpty(bill.getExchangedpoint())) {
					exchangeScore = ArithDouble.parseDouble(bill.getExchangedpoint());
					usedScore = ArithDouble.add(usedScore, exchangeScore);
				}
				if (usedScore > 0) {
					addTextJson(array, FONT_DEFAULT, formatLString(10, "消耗积分：") + "	" + usedScore, PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}

			if (addScore != 0 || usedScore != 0) {
				if(mend){
					totalPoint = ArithDouble.sub(addScore, usedScore);
					addTextJson(array, FONT_DEFAULT, formatLString(10, "本次累计：") + "	" + totalPoint, PrinterManager.CONTENT_ALIGN_LEFT);
				}else{
					totalPoint = ArithDouble.sub(addScore, usedScore);
					addTextJson(array, FONT_DEFAULT, formatLString(10, "累计积分：") + "	" + ArithDouble.add(ArithDouble.parseDouble(bill.getMember().getCent_total()), totalPoint), PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}

		}
		if ((bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0)
				|| (bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0)) {
			addDashLine(array);
		}
		if (bill.getUsedcouponlist() != null && bill.getUsedcouponlist().size() > 0) {
			addTextJson(array, FONT_DEFAULT,  "使用券", PrinterManager.CONTENT_ALIGN_LEFT);
			for (CouponInfo info : bill.getUsedcouponlist()) {
				addTextJson(array, FONT_DEFAULT, formatLString(10, info.getName()) + "	" + info.getAvailablemoney() + "元" + "	" + ArithDouble.add(ArithDouble.parseDouble(bill.getMember().getCent_total()), totalPoint), PrinterManager.CONTENT_ALIGN_LEFT);
			}
		}

		if (bill.getGrantcouponlist() != null && bill.getGrantcouponlist().size() > 0) {
			addTextJson(array, FONT_DEFAULT, "新增券", PrinterManager.CONTENT_ALIGN_LEFT);
			for (CouponInfo info : bill.getGrantcouponlist()) {
				addTextJson(array, FONT_DEFAULT, formatLString(10, info.getName()) + "	" + info.getAvailablemoney() + "元", PrinterManager.CONTENT_ALIGN_LEFT);
			}
		}
		if (mend) {
			addDashLine(array);
			addTextJson(array, FONT_DEFAULT, "补打收款员：" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
			addTextJson(array, FONT_DEFAULT, "补打时间：" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), PrinterManager.CONTENT_ALIGN_LEFT);
		}
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
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
	public static JSONObject printBackOrderList(final BillInfo bill, final boolean flag) {
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
					return getBackOrder(bill, flag, ticketFormat);
				} else {
					return printBackOrderListDefault(bill, flag);
				}
			} else {
				return printBackOrderListDefault(bill, flag);
			}
		} else {
			return printBackOrderListDefault(bill, flag);
		}
	}
	public static JSONObject getBackOrder(BillInfo bill, boolean flag, Tickdatas ticketFormat){
		JSONArray array = new JSONArray();

		double couponValue = 0;//优惠券
		double couponOverrage = 0;//优惠券溢余
		double deductionValue = 0;//积分抵扣
		String scorePay = null;
		double careduction = 0;//扣减现金
		String couponPay = null;
		double compensation = 0;//补偿金额
		double realMoney = 0;//真实金额
		double total=0;
		int count = 0;
		for (GoodsInfo g : bill.getGoodslist()) {
			count += ArithDouble.parseInt(g.getSalecount());
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
		addBlankLine(array);
		addBlankLine(array);
		for(ConditionindexEntity info:ticketFormat.getTickbasic().getConditionindex()){
			if(info.getConditionid() != null && !"0".equals(info.getYxj())){
				switch (info.getConditionid()) {
					case "1":
						if(ticketFormat.getTickbegin()!= null){
							PrintString tickbegin = new PrintString(ticketFormat.getTickbegin());
							tickbegin.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
									.replace(TicketFormatEnum.TICKET_RETURN_REASON.getLable(), bill.getBackreason())
									.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
							TickbasicEntity basic = ticketFormat.getTickbasic();
							if(basic != null){
								String date = Utils.formatDate( new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
								tickbegin.replace(TicketFormatEnum.TICKET_WEB.getLable(), basic.getWebsite())
										.replace(TicketFormatEnum.TICKET_HOT_LINE.getLable(), basic.getHotline())
										.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
										.replace(TicketFormatEnum.TICKET_BUDASALEMAN.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""))
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_CODE, ""))
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
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "2":
						if(ticketFormat.getTickend()!= null){
							PrintString tickend = new PrintString(ticketFormat.getTickend());
							tickend.replace(TicketFormatEnum.TICKET_SHOP_CODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_CODE, ""))
									.replace(TicketFormatEnum.TICKET_SHOP_NAME.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""))
									.replace(TicketFormatEnum.TICKET_BILL_NO.getLable(), bill.getBillid())
									.replace(TicketFormatEnum.TICKET_DESK_CODE.getLable(), bill.getPosno())
									.replace(TicketFormatEnum.TICKET_CASHIER_CODE.getLable(), bill.getCashier())
									.replace(TicketFormatEnum.TICKET_CASHIER_NAME.getLable(), bill.getCashiername())
									.replace(TicketFormatEnum.TICKET_SALEMAN_CODE.getLable(), bill.getSaleman())
									.replace(TicketFormatEnum.TICKET_SALEMAN_NAME.getLable(), bill.getSalemanname())
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
										.replace(TicketFormatEnum.TICKET_BUDASALEMANCODE.getLable(), SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_CODE, ""))
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
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "3":
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
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "4":
						if(ticketFormat.getMoneys() != null){
							double exchange = 0.0;
							if(bill.getChangemoney() != null){
								exchange = ArithDouble.parseDouble(bill.getChangemoney());
							}
							PrintString moneys = new PrintString(ticketFormat.getMoneys()).replace("\\s*", "")
									.replace(TicketFormatEnum.TICKET_EXCHANGE.getLable(), "\t\t" + formatRString(8, ""+exchange))
									.replace(TicketFormatEnum.TICKET_TOTAL_COUNT.getLable(), "\t"+formatLString(4, ""+count))
									.replace(TicketFormatEnum.TICKET_TOTAL_MONEY.getLable(), "\t" +formatRString(8,bill.getTotalmoney()))
									.replace(TicketFormatEnum.TICKET_TOTAL_RETURN_GOOD_SCORE.getLable(), "" +total)
									.replace(TicketFormatEnum.TICKET_RETURN_DEAL_MONEY.getLable(), "\t\t"+formatRString(8, MoneyAccuracyUtils.getmoneybytwo(realMoney)))
									.replace(TicketFormatEnum.TICKET_RETURN_REAL_MONEY.getLable(), "\t\t"+formatRString(8, MoneyAccuracyUtils.getmoneybytwo(realMoney)));

							if(deductionValue <= 0){
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable()+TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable()+TicketFormatEnum.TICKET_ENTER.getLable(), "");
							}else{
								moneys.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_NAME.getLable(), ""+scorePay)
										.replace(TicketFormatEnum.TICKET_DUDUC_SOCRE_MONEY.getLable(), "\t" +formatRString(8, ""+deductionValue));
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
									builder.append(format(infos.getName()) +formatRString(8,infos.getAvailablemoney())+"\n");
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
									builder.append(format(infos.getName()) +formatRString(8,infos.getMoney())+"\n");
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
								addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
							}
						}
						break;
					case "5":
						if(ticketFormat.getVip() != null){

							PrintString vip = new PrintString(ticketFormat.getVip());
							if(bill.getMember() != null){
								vip.replace(TicketFormatEnum.TICKET_MEMBER_NO.getLable(), formatRString(8, bill.getMember().getMemberno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_NAME.getLable(), formatRString(8, bill.getMember().getMembername()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TEL.getLable(), formatRString(8, bill.getMember().getPhoneno()))
										.replace(TicketFormatEnum.TICKET_MEMBER_TYPE.getLable(), formatRString(8, bill.getMember().getMembertype()));
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
								vip.replace(TicketFormatEnum.TICKET_TOTAL_SCORE.getLable(), formatRString(8, ""+ ArithDouble.sub( ArithDouble.add(usedScore, exchangeScore), awardScore)))
										.replace(TicketFormatEnum.TICKET_ENTER.getLable(), "\n");
								TickbasicEntity basic = ticketFormat.getTickbasic();
								if(basic != null){
									vip.replace(TicketFormatEnum.TICKET_BUDALINE.getLable(), basic.getLineformat_again()+"\n")
											.replace(TicketFormatEnum.TICKET_LINE.getLable(), basic.getLineformat()+"\n")
											.replace(TicketFormatEnum.TICKET_NORMAL_LINE.getLable(), basic.getLineformat()+"\n");
								}
								String[] codes = vip.getString().split("\n");
								for (String code:codes){
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}

						}
						break;
					case "6":

						break;
					case "8":
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					case "7":
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					case "9":
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
									addTextJson(array, FONT_DEFAULT, code, PrinterManager.CONTENT_ALIGN_LEFT);
								}
							}
						}
						break;
					default:
						break;
				}
			}
		}
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public static JSONObject printBackOrderListDefault(BillInfo bill, boolean flag) {
		JSONArray array = new JSONArray();
		addBlankLine(array);
		addBlankLine(array);
		addTextJson(array, FONT_DEFAULT, "店铺:" + SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "退货单号:" + bill.getBillid(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, "收款台号:" + bill.getPosno(), PrinterManager.CONTENT_ALIGN_LEFT);
		if (bill.getCashiername() != null && !"".equals(bill.getCashiername())) {
			addTextJson(array, FONT_DEFAULT, "收款员:" + bill.getCashiername(), PrinterManager.CONTENT_ALIGN_LEFT);
		} else {
			addTextJson(array, FONT_DEFAULT, "收款员:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
		}
		if (bill.getSalemanname() != null) {
			addTextJson(array, FONT_DEFAULT, "销售:" + bill.getSalemanname(), PrinterManager.CONTENT_ALIGN_LEFT);
		} else {
			addTextJson(array, FONT_DEFAULT, "销售:" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
		}
		addTextJson(array, FONT_DEFAULT, "日期：" + bill.getSaletime(), PrinterManager.CONTENT_ALIGN_LEFT);
		addTextJson(array, FONT_DEFAULT, formatLString(8, "商品") + formatLString(4, "数量") + formatLString(6, "金额") + "消耗积分", PrinterManager.CONTENT_ALIGN_LEFT);

		double total = 0;
		int count = 0;
		for (GoodsInfo g : bill.getGoodslist()) {
			addTextJson(array, FONT_DEFAULT, g.getGoodsname(), PrinterManager.CONTENT_ALIGN_LEFT);
			count += ArithDouble.parseInt(g.getSalecount());
			double grantPoint = 0;
			double usedPoint = 0;
			grantPoint = ArithDouble.parseDouble(g.getGrantpoint());
			usedPoint = ArithDouble.parseDouble(g.getUsedpoint());
			addTextJson(array, FONT_DEFAULT, formatLString(10, g.getCode())
					+ formatLString(4, "-" + g.getSalecount().replaceAll("-", ""))
					+ formatLString(10, "-" + g.getSaleamt().replaceAll("-", ""))
					+ formatLString(6, ArithDouble.sub(grantPoint, usedPoint) + ""), PrinterManager.CONTENT_ALIGN_LEFT);
			total = ArithDouble.add(total, ArithDouble.sub(grantPoint, usedPoint));
		}
		if (flag) {
			addTextJson(array, FONT_DEFAULT, "----------补打小票--------------", PrinterManager.CONTENT_ALIGN_LEFT);
		} else {
			addTextJson(array, FONT_DEFAULT, "--------------------------------", PrinterManager.CONTENT_ALIGN_LEFT);
		}
		addTextJson(array, FONT_DEFAULT, formatLString(8, "合计") + formatLString(4, "-" + count)
				+ formatLString(10, "-" + bill.getTotalmoney().replaceAll("-", ""))
				+ formatLString(6, total + ""), PrinterManager.CONTENT_ALIGN_LEFT);
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
				addMultiTextJson(array, FONT_DEFAULT, "代金券：", couponValue + "(溢余" + couponOverrage + ")" + unit);
			}else{
				addMultiTextJson(array, FONT_DEFAULT, "代金券：", couponValue + unit);
			}
		}
		if (deductionValue > 0) {
			addMultiTextJson(array, FONT_DEFAULT, "积分抵扣：", deductionValue + unit);
		}
		if (careduction > 0) {
			addMultiTextJson(array, FONT_DEFAULT, "扣减现金：", careduction + unit);
		}
		if (compensation > 0) {
			addMultiTextJson(array, FONT_DEFAULT, "补偿金额：", compensation + unit);
		}
		try {
			BigDecimal decimal = new BigDecimal(bill.getTotalmoney().replaceAll("-", ""));
			realMoney = decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
			realMoney = realMoney - couponValue - deductionValue - careduction + compensation + couponOverrage;
		} catch (Exception e) {
		}
		addMultiTextJson(array, FONT_DEFAULT, "应退：", MoneyAccuracyUtils.getmoneybytwo(realMoney) + unit);
		addDashLine(array);
		addMultiTextJson(array, FONT_DEFAULT, "实退：", MoneyAccuracyUtils.getmoneybytwo(realMoney) + unit);
		for (PayMentsInfo info : bill.getPaymentslist()) {
			String type = info.getType();
			if (PaymentTypeEnum.COUPON.equals(type) || PaymentTypeEnum.SCORE.equals(type)
					|| PaymentTypeEnum.RECORDED_CAREDUCTION.equals(type)
					|| PaymentTypeEnum.ALLWANCE_COMPENSATION.equals(type))
				continue;
			addMultiTextJson(array, FONT_DEFAULT, info.getName(), info.getMoney().replaceAll("-", "") + unit);
		}
		addDashLine(array);
		MemberInfo member = bill.getMember();
		if (member != null) {
			if (member.getMemberno() != null) {
				addTextJson(array, FONT_DEFAULT, formatLString(10, "会员卡号:") + member.getMemberno(), PrinterManager.CONTENT_ALIGN_LEFT);
			} else {
				addTextJson(array, FONT_DEFAULT, formatLString(10, "会员卡号:") + 0, PrinterManager.CONTENT_ALIGN_LEFT);
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
			addTextJson(array, FONT_DEFAULT, formatLString(10, "退货积分:") + awardScore, PrinterManager.CONTENT_ALIGN_LEFT);
			addTextJson(array, FONT_DEFAULT, formatLString(10, "退回积分:") + ArithDouble.add(usedScore, exchangeScore), PrinterManager.CONTENT_ALIGN_LEFT);
			addTextJson(array, FONT_DEFAULT, formatLString(10, "累计积分:") + ArithDouble.sub(ArithDouble.add(usedScore, exchangeScore), awardScore), PrinterManager.CONTENT_ALIGN_LEFT);
			addDashLine(array);
		}
		List<CouponInfo> usedCoupon = bill.getUsedcouponlist();
		if (usedCoupon != null && usedCoupon.size() != 0) {
			addTextJson(array, FONT_DEFAULT, "退回券", PrinterManager.CONTENT_ALIGN_LEFT);
			for (CouponInfo info : usedCoupon) {
				if (info.getName() != null && info.getAvailablemoney() != null) {
					addTextJson(array, FONT_DEFAULT, info.getName() + "	" + info.getAvailablemoney().replaceAll("-", "") + "元", PrinterManager.CONTENT_ALIGN_LEFT);
				}
			}
		}
		List<CouponInfo> grantCoupon = bill.getGrantcouponlist();
		if (usedCoupon != null && grantCoupon.size() != 0) {
			List<CouponInfo> printCoupon = null;
			for (CouponInfo info : grantCoupon) {
				if ("0".equals(info.getIsused())) {
					if (printCoupon == null) {
						printCoupon = new ArrayList<>();
					}
					printCoupon.add(info);
				}
			}
			if (printCoupon != null && printCoupon.size() != 0) {
				addTextJson(array, FONT_DEFAULT, "收回券", PrinterManager.CONTENT_ALIGN_LEFT);
				for (CouponInfo info : printCoupon) {
					if (info.getName() != null && info.getAvailablemoney() != null) {
						addTextJson(array, FONT_DEFAULT, info.getName() + "	" + info.getAvailablemoney().replaceAll("-", "") + "元", PrinterManager.CONTENT_ALIGN_LEFT);
					}
				}
			}
		}

		if (flag) {
			addBlankLine(array);
			addBlankLine(array);
			addTextJson(array, FONT_DEFAULT, "补打收款员：" + SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_NAME, ""), PrinterManager.CONTENT_ALIGN_LEFT);
			addTextJson(array, FONT_DEFAULT, "补打时间：" + Utils.formatDate(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"), PrinterManager.CONTENT_ALIGN_LEFT);
		}
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		addBlankLine(array);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("page", array);
		} catch (Exception e) {
			e.printStackTrace();
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
