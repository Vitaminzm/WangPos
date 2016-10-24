package com.symboltech.wangpos.db.dao;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.db.SymboltechMallDBOpenHelper;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.msg.entity.OfflineBankInfo;
import com.symboltech.wangpos.msg.entity.OfflineBillInfo;
import com.symboltech.wangpos.msg.entity.OfflineConfirmbillinfos;
import com.symboltech.wangpos.msg.entity.OfflineGoodsInfo;
import com.symboltech.wangpos.msg.entity.OfflinePayTypeInfo;
import com.symboltech.wangpos.msg.entity.OfflineSavearticleinfos;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.RefundReportInfo;
import com.symboltech.wangpos.msg.entity.ReportDetailInfo;
import com.symboltech.wangpos.msg.entity.ReportInfo;
import com.symboltech.wangpos.msg.entity.SaleReportInfo;
import com.symboltech.wangpos.msg.entity.TotalReportInfo;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.StringUtil;


/**
 * 订单信息相关操作 simple introduction
 *
 * <p>
 * detailed comment
 *
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月2日
 * @see
 * @since 1.0
 */
public class OrderInfoDao {

	private SymboltechMallDBOpenHelper.DatabaseHelper helper;

	private Context context;
	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public OrderInfoDao(Context context) {
		SymboltechMallDBOpenHelper dbutil = new SymboltechMallDBOpenHelper(context);
		helper = dbutil.getDatabaseHelper();
		this.context = context;
	}

	public SymboltechMallDBOpenHelper.DatabaseHelper getHelper() {
		return helper;
	}



	/**
	 * 获取离线的账单信息 BY billId
	 * @param billId
	 * @return
	 */
	public OfflineBillInfo geOfflineBillInfo(String billId){
		OfflineBillInfo result = null;
		if(billId ==null){
			return result;
		}
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("user_order", null, "billid = ? and status = ?", new String[] { billId, "1" }, null, null, null);
		while(cursor.moveToNext()){
			result = new OfflineBillInfo();
			OfflineConfirmbillinfos confirmbill = new OfflineConfirmbillinfos();
			OfflineSavearticleinfos savearticle = new OfflineSavearticleinfos();
			savearticle.setBillid(billId);
			savearticle.setCashiername(cursor.getString(cursor.getColumnIndex("cashiername")));
			savearticle.setCashier(cursor.getString(cursor.getColumnIndex("cashier")));
			savearticle.setOldbillid(cursor.getString(cursor.getColumnIndex("oldbillid")));
			savearticle.setOldposno(cursor.getString(cursor.getColumnIndex("oldposno")));
			savearticle.setPersonid(cursor.getString(cursor.getColumnIndex("personid")));
			savearticle.setSaletime(cursor.getString(cursor.getColumnIndex("time")));
			savearticle.setSaletype(cursor.getString(cursor.getColumnIndex("saletype")));
			savearticle.setTotalmoney(cursor.getString(cursor.getColumnIndex("totalmoney")));
			savearticle.setGoodslist(getGoodsinfo(db, billId));
			confirmbill.setBackreason(cursor.getString(cursor.getColumnIndex("backreason")));
			confirmbill.setBillid(billId);
			confirmbill.setChangemoney(cursor.getString(cursor.getColumnIndex("changemoney")));
			confirmbill.setSaletime(cursor.getString(cursor.getColumnIndex("saletime")));
			confirmbill.setPaymentslist(findPaytype(db, billId));
			result.setConfirmbillinfos(confirmbill);
			result.setSavearticleinfos(savearticle);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 获取离线的账单信息
	 * @param beginBillID 开始位置订单号
	 * 			beginBillID 为空，表示重头开始，给出具体位置，标识从具体位置开始
	 * @param count  查询条数
	 * @return
	 */
	public List<OfflineBillInfo> getOfflineOrderInfo(String beginBillID, int count){
		List<OfflineBillInfo> result = new ArrayList<OfflineBillInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = null;
		if(beginBillID == null) {
			cursor = db.query("user_order", null, "offline = ? and status = ?", new String[] { "0", "1"}, null, null, " billid asc limit " + count);
		}else {
			cursor = db.query("user_order", null, "offline = ? and billid > ? and status = ?", new String[] { "0", beginBillID, "1" }, null, null, " billid asc limit " + count);
		}
		while(cursor.moveToNext()){
			OfflineBillInfo info = new OfflineBillInfo();
			OfflineConfirmbillinfos confirmbill = new OfflineConfirmbillinfos();
			OfflineSavearticleinfos savearticle = new OfflineSavearticleinfos();
			String orderid = cursor.getString(cursor.getColumnIndex("billid"));
			savearticle.setBillid(orderid);
			savearticle.setCashier(cursor.getString(cursor.getColumnIndex("cashier")));
			savearticle.setCashiername(cursor.getString(cursor.getColumnIndex("cashiername")));
			savearticle.setOldbillid(cursor.getString(cursor.getColumnIndex("oldbillid")));
			savearticle.setOldposno(cursor.getString(cursor.getColumnIndex("oldposno")));
			savearticle.setPersonid(cursor.getString(cursor.getColumnIndex("personid")));
			savearticle.setSaletime(cursor.getString(cursor.getColumnIndex("time")));
			savearticle.setSaletype(cursor.getString(cursor.getColumnIndex("saletype")));
			savearticle.setTotalmoney(cursor.getString(cursor.getColumnIndex("totalmoney")));
			savearticle.setGoodslist(getGoodsinfo(db, orderid));
			confirmbill.setBackreason(cursor.getString(cursor.getColumnIndex("backreason")));
			confirmbill.setBillid(orderid);
			confirmbill.setChangemoney(cursor.getString(cursor.getColumnIndex("changemoney")));
			confirmbill.setSaletime(cursor.getString(cursor.getColumnIndex("saletime")));
			confirmbill.setPaymentslist(findPaytype(db, orderid));
			info.setConfirmbillinfos(confirmbill);
			info.setSavearticleinfos(savearticle);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 获取离线的账单信息
	 * @param count 取出个数
	 * @return
	 */
	public List<OfflineBillInfo> getOfflineOrderInfo(int count){
		List<OfflineBillInfo> result = new ArrayList<OfflineBillInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("user_order", null, "offline = ? and status = ?", new String[] { "0", "1" }, null, null, " billid asc limit " + count);

		while(cursor.moveToNext()){
			OfflineBillInfo info = new OfflineBillInfo();
			OfflineConfirmbillinfos confirmbill = new OfflineConfirmbillinfos();
			OfflineSavearticleinfos savearticle = new OfflineSavearticleinfos();
			String orderid = cursor.getString(cursor.getColumnIndex("billid"));
			savearticle.setBillid(orderid);
			savearticle.setCashier(cursor.getString(cursor.getColumnIndex("cashier")));
			savearticle.setCashiername(cursor.getString(cursor.getColumnIndex("cashiername")));
			savearticle.setOldbillid(cursor.getString(cursor.getColumnIndex("oldbillid")));
			savearticle.setOldposno(cursor.getString(cursor.getColumnIndex("oldposno")));
			savearticle.setPersonid(cursor.getString(cursor.getColumnIndex("personid")));
			savearticle.setSaletime(cursor.getString(cursor.getColumnIndex("time")));
			savearticle.setSaletype(cursor.getString(cursor.getColumnIndex("saletype")));
			savearticle.setTotalmoney(cursor.getString(cursor.getColumnIndex("totalmoney")));
			savearticle.setGoodslist(getGoodsinfo(db, orderid));
			confirmbill.setBackreason(cursor.getString(cursor.getColumnIndex("backreason")));
			confirmbill.setBillid(orderid);
			confirmbill.setChangemoney(cursor.getString(cursor.getColumnIndex("changemoney")));
			confirmbill.setSaletime(cursor.getString(cursor.getColumnIndex("saletime")));
			confirmbill.setPaymentslist(findPaytype(db, orderid));
			info.setConfirmbillinfos(confirmbill);
			info.setSavearticleinfos(savearticle);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 查询最大的小票号
	 * @return
	 */
	public String getMaxbillid(){
		String result = "0";
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select max(billid) AS maxId from user_order where status = ?", new String[]{ "1" });
		if (cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 查找支付状态BY orderid
	 *
	 * @param orderid
	 * @return
	 */
	public String findOrderStatus(String orderid) {
		String result = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("user_order", new String[]{"status"}, "billid = ?", new String[] { orderid }, null, null, null);
		if (cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;

	}

	/**
	 * 查找上传状态BY orderid
	 *
	 * @param orderid
	 * @return
	 */
	public String findOfflineStatus(String orderid) {
		String result = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("user_order", new String[]{"offline"}, "billid = ?", new String[] { orderid }, null, null, null);
		if (cursor.moveToFirst()) {
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;

	}

	/**
	 * 商品提交的信息
	 * @param billid 小票号
	 * @param personid 登录人员id
	 * @param cashier 收款员
	 * @param saletype 交易类型
	 * @param totalmoney 总销售额
	 * @param goodsInfo 商品信息列表
	 * @return
	 */
	public boolean addOrderGoodsInfo(String billid, String personid, String cashier, String cashiername, String saletype, double totalmoney, List<GoodsInfo> goodsInfo) {
		boolean ret = false;
		if (!StringUtil.isEmpty(billid) && !StringUtil.isEmpty(personid) && goodsInfo != null && goodsInfo.size() > 0
				&& !StringUtil.isEmpty(cashier) && !StringUtil.isEmpty(saletype)) {
			String status = findOrderStatus(billid);
			if(status != null){
				if(status.equals("0")){
					deleteOrder(billid);
				}else if(status.equals("1")){
					return ret;
				}
			}
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
			ContentValues values=new ContentValues();
			values.put("billid", ArithDouble.parseInt(billid));
			values.put("personid", personid);
			values.put("cashier", cashier);
			values.put("cashiername", cashiername);
			values.put("saletype", saletype);
			values.put("totalmoney", totalmoney);
			values.put("time", format.format(new Date()));
			long success = -1;
			success = db.insert("user_order", null, values);
			if(success != -1){
				ret = true;
			}
			if(ret){
				int count = 0;
				for(GoodsInfo info: goodsInfo){
					ContentValues valuesgood = new ContentValues();
					valuesgood.put("billid", ArithDouble.parseInt(billid));
					valuesgood.put("id", info.getId());
					valuesgood.put("goodsname", info.getGoodsname());
					valuesgood.put("usedpoint", info.getUsedpoint());
					valuesgood.put("barcode", info.getBarcode());
					valuesgood.put("code", info.getCode());
					valuesgood.put("price", ArithDouble.parseDouble(info.getPrice()));
					valuesgood.put("salecount", ArithDouble.parseInt(info.getSalecount()));
					valuesgood.put("inx", info.getInx());
					valuesgood.put("discmoney", ArithDouble.parseDouble(info.getDiscmoney()));
					valuesgood.put("preferentialmoney", ArithDouble.parseDouble(info.getPreferentialmoney()));
					valuesgood.put("unit", info.getUnit());
					valuesgood.put("saleamt", ArithDouble.parseDouble(info.getSaleamt()));
					success = -1;
					success = db.insert("goods_info", null, valuesgood);
					if (success != -1) {
						count++;
					}
				}
				if(count != goodsInfo.size()){
					ret = false;
				}else{
					db.setTransactionSuccessful();
				}
			}
			db.endTransaction();
			db.close();
		}
		LogUtil.i("lgs", "addOrderGoodsInfo---"+ ret);
		return ret;
	}

	/**
	 * 保存交易信息
	 * @param billid 小票号
	 * @param oldbillid 原小票号 可空
	 * @param oldposno 原款台号 可空
	 * @param backreason 退货原因 可空
	 * @param changemoney 找零
	 * @param status 交易状态
	 * @param personid 收款员
	 * @param payInfos 支付信息列表
	 * @return
	 */
	public boolean addOrderPaytypeinfo(String billid, String oldbillid, String oldposno, String backreason, double changemoney, String status, String personid, List<PayMentsInfo> payInfos){
		boolean ret = false;
		if (!StringUtil.isEmpty(billid) && !StringUtil.isEmpty(status)&& !StringUtil.isEmpty(personid)
				&& payInfos != null && payInfos.size() > 0){
			String time =  format.format(new Date());
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
			ContentValues values=new ContentValues();
			values.put("oldbillid", oldbillid);
			values.put("oldposno", oldposno);
			values.put("backreason", backreason);
			values.put("changemoney", changemoney);
			values.put("status", status);
			values.put("saletime", time);
			int success = 0;
			success = db.update("user_order", values, "billid = ?", new String[]{ billid });

			if(success > 0){
				ret = true;
			}
			if(ret){
				int count = 0;
				for(PayMentsInfo info:payInfos){
					ContentValues valuespaytype=new ContentValues();
					valuespaytype.put("billid", ArithDouble.parseInt(billid));
					valuespaytype.put("type", info.getType());
					valuespaytype.put("id", info.getId());
					valuespaytype.put("money", ArithDouble.parseDouble(info.getMoney()));
					valuespaytype.put("overage", ArithDouble.parseDouble(info.getOverage()));
					valuespaytype.put("name", info.getName());
					valuespaytype.put("personid", personid);
					valuespaytype.put("skrq", time);
					success = -1;
					success = (int) db.insert("pay_type", null, valuespaytype);
					if(success != -1){
						count++;
					}
				}
				if(count != payInfos.size()){
					ret = false;
				}else{
					db.setTransactionSuccessful();
				}

			}
			db.endTransaction();
			db.close();
		}
		LogUtil.i("lgs", "addOrderPaytypeinfo---"+ ret);
		return ret;
	}


	/**
	 * 删除账单信息 BY小票号（会级联删除商品以及支付表）
	 * @return
	 */
	public boolean deleteOrderbyState(){
		boolean ret = false;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("pragma foreign_keys=on");
		int success = 0;
		success = db.delete("user_order", "status = ?", new String[]{ "0" });
		db.close();
		if(success > 0){
			ret = true;
		}
		LogUtil.i("lgs", "deleteOrder---"+ ret);
		return ret;
	}
	/**
	 * 删除账单信息 BY小票号（会级联删除商品以及支付表）
	 * @param billid
	 * @return
	 */
	public boolean deleteOrder(String billid){
		boolean ret = false;
		if (!StringUtil.isEmpty(billid)){
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL("pragma foreign_keys=on");
			int success = 0;
			success = db.delete("user_order", "billid = ?", new String[]{ billid });
			db.close();
			if(success > 0){
				ret = true;
			}
		}
		LogUtil.i("lgs", "deleteOrder---"+ ret);
		return ret;
	}


	public void setOfflineStatus(List<String>list){
		if(list!=null && list.size() > 0){
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
			for(String id:list){
				ContentValues values=new ContentValues();
				values.put("offline", "1");
				db.update("user_order", values, "billid = ?", new String[]{ id });
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * 删除账单信息 BY时间（会级联删除商品以及支付表）
	 * @param date
	 * @return
	 */
	public boolean deleteOrderBytime(String date){
		boolean ret = false;
		if (!StringUtil.isEmpty(date)){
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL("pragma foreign_keys=on");
			int success = 0;
			success = db.delete("user_order", "date(saletime) <= date(?) and offline = ? ", new String[]{ date, "1" });
			db.close();
			if(success > 0){
				ret = true;
			}
		}
		LogUtil.i("lgs", "deleteOrderBytime---"+ ret);
		return ret;
	}

	/**
	 * 获取离线商品信息BY orderid
	 *
	 * @param orderid
	 * @return 离线商品信息
	 */
	private List<OfflineGoodsInfo> getGoodsinfo(SQLiteDatabase db, String orderid) {
		List<OfflineGoodsInfo> result = new ArrayList<OfflineGoodsInfo>();
		Cursor cursor = db.query("goods_info", new String[]{"id", "barcode", "price", "goodsname", "usedpoint", "code", "salecount", "inx", "discmoney", "preferentialmoney","unit", "saleamt"}, "billid = ?", new String[] { orderid }, null, null, null);
		while(cursor.moveToNext()) {
			OfflineGoodsInfo info = new OfflineGoodsInfo();
			for(int i=0;i<cursor.getColumnCount();i++){
				try {
					Field field = OfflineGoodsInfo.class.getDeclaredField(cursor.getColumnName(i));
					if(field!=null){
						field.setAccessible(true);
						field.set(info, cursor.getString(i));
					}
				} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			result.add(info);
		}
		cursor.close();
		return result;
	}

	/**
	 * 删除商品信息
	 * @param billid
	 * @return
	 */
	public boolean deleteGoods(String billid){
		boolean ret = false;
		if (!StringUtil.isEmpty(billid)){
			SQLiteDatabase db = helper.getWritableDatabase();
			int success = 0;
			success = db.delete("goods_info", "billid = ?", new String[]{ billid });
			db.close();
			if(success > 0){
				ret = true;
			}
		}
		LogUtil.i("lgs", "deleteGoods---"+ ret);
		return ret;
	}

	/**
	 * 获取离线支付信息BY orderid
	 *
	 * @param orderid
	 * @return 离线支付信息
	 */
	private List<OfflinePayTypeInfo> findPaytype(SQLiteDatabase db, String orderid) {
		List<OfflinePayTypeInfo> result = new ArrayList<>();
		Cursor cursor = db.query("pay_type", new String[]{"id", "money", "type", "overage", "name"}, "billid = ?", new String[] { orderid }, null, null, null);
		while(cursor.moveToNext()) {
			OfflinePayTypeInfo info = new OfflinePayTypeInfo();
			for(int i=0;i<cursor.getColumnCount();i++){
				try {
					Field field = OfflinePayTypeInfo.class.getDeclaredField(cursor.getColumnName(i));
					if(field!=null){
						field.setAccessible(true);
						field.set(info, cursor.getString(i));
					}
				} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			result.add(info);
		}
		cursor.close();
		return result;

	}

	/**
	 * 删除支付信息
	 * @param billid
	 * @return
	 */
	public boolean deletePaytype(String billid){
		boolean ret = false;
		if (!StringUtil.isEmpty(billid)){
			SQLiteDatabase db = helper.getWritableDatabase();
			int success = 0;
			success = db.delete("pay_type", "billid = ?", new String[]{ billid });
			db.close();
			if(success > 0){
				ret = true;
			}
		}
		LogUtil.i("lgs", "deletePaytype---" + ret);
		return ret;
	}

	/**
	 * 查询日报
	 * @param date 查询日期
	 * @return 当日报表信息
	 */
	public ReportInfo findPaytypeBytime(long date) {
		SQLiteDatabase db = helper.getReadableDatabase();

		//查销售数据
		SaleReportInfo saleReportInfo = null;
		List<ReportDetailInfo> sales = findLogData(date, db, ConstantData.LOG_SALE, null);
		String saleMoney = findLogTotalMoney(date, db, ConstantData.LOG_SALE, null);
		int saleCount = findLogTotalCount(date, db, ConstantData.LOG_SALE, null);
		saleReportInfo = new SaleReportInfo();
		if(sales != null && sales.size() > 0) {
			saleReportInfo.setSalelist(sales);
		}
		saleReportInfo.setTotalmoney(TextUtils.isEmpty(saleMoney)?"0":saleMoney);
		saleReportInfo.setBillcount(saleCount+"");


		//查退货数据
		RefundReportInfo refundReportInfo = null;
		List<ReportDetailInfo> returns = findLogData(date, db, ConstantData.LOG_RETURN, null);
		String returnMoney = findLogTotalMoney(date, db, ConstantData.LOG_RETURN, null);
		int returnCount = findLogTotalCount(date, db, ConstantData.LOG_RETURN, null);
		refundReportInfo = new RefundReportInfo();
		if(returns != null && returns.size() > 0) {
			refundReportInfo.setRefundlist(returns);
		}
		refundReportInfo.setTotalmoney(TextUtils.isEmpty(returnMoney)?"0":returnMoney);
		refundReportInfo.setBillcount(returnCount+"");


		//差合计数据
		TotalReportInfo totalReportInfo = null;
		List<ReportDetailInfo> totols = findLogData(date, db, ConstantData.LOG_ALL, null);
		String totalMoney = findLogTotalMoney(date, db, ConstantData.LOG_ALL, null);
		int totalCount = findLogTotalCount(date, db, ConstantData.LOG_ALL, null);
		totalReportInfo = new TotalReportInfo();
		if(totols != null && totols.size() > 0) {
			totalReportInfo.setTotallist(totols);
		}
		totalReportInfo.setTotalmoney(TextUtils.isEmpty(totalMoney)?"0":totalMoney);
		totalReportInfo.setBillcount(totalCount+"");

		ReportInfo report = null;
		if((saleReportInfo.getSalelist() != null && saleReportInfo.getSalelist().size() > 0) || (refundReportInfo.getRefundlist() != null && refundReportInfo.getRefundlist().size() > 0) || (totalReportInfo.getTotallist() != null && totalReportInfo.getTotallist().size() > 0)) {
			report = new ReportInfo();
			report.setSale(saleReportInfo);
			report.setRefund(refundReportInfo);
			report.setTotal(totalReportInfo);
		}
		return report;

	}

	/**
	 * 查询日志数据
	 * @param date 日期
	 * @param db
	 * @param condition 查询类型 
	 * @param personid 销售人员
	 * @return
	 */
	private List<ReportDetailInfo> findLogData(long date, SQLiteDatabase db, int condition, String personid) {
		List<ReportDetailInfo> infos = new ArrayList<ReportDetailInfo>();
		Cursor cursor = null;
		if(personid != null) {
			if(ConstantData.LOG_SALE == condition) {
				cursor = db.query("pay_type", new String[] {"name", "sum(money) AS total", "count(*) AS count"}, "date(skrq) = date(?) and money >= 0 and personid = ?", new String[] { format.format(new Date(date)), personid }, "name", null, null);
			}else if(ConstantData.LOG_RETURN == condition) {
				cursor = db.query("pay_type", new String[] {"name", "sum(money) AS total", "count(*) AS count"}, "date(skrq) = date(?) and money < 0 and personid = ?", new String[] { format.format(new Date(date)), personid }, "name", null, null);
			}else if(ConstantData.LOG_ALL == condition) {
				cursor = db.query("pay_type", new String[] {"name", "sum(money) AS total", "count(*) AS count"}, "date(skrq) = date(?) and personid = ?", new String[] { format.format(new Date(date)), personid }, "name", null, null);
			}
		}else {
			if(ConstantData.LOG_SALE == condition) {
				cursor = db.query("pay_type", new String[] {"name", "sum(money) AS total", "count(*) AS count"}, "date(skrq) = date(?) and money >= 0", new String[] { format.format(new Date(date)) }, "name", null, null);
			}else if(ConstantData.LOG_RETURN == condition) {
				cursor = db.query("pay_type", new String[] {"name", "sum(money) AS total", "count(*) AS count"}, "date(skrq) = date(?) and money < 0", new String[] { format.format(new Date(date)) }, "name", null, null);
			}else if(ConstantData.LOG_ALL == condition) {
				cursor = db.query("pay_type", new String[] {"name", "sum(money) AS total", "count(*) AS count"}, "date(skrq) = date(?)", new String[] { format.format(new Date(date)) }, "name", null, null);
			}
		}
		while(cursor.moveToNext()) {
			ReportDetailInfo detail = new ReportDetailInfo();
			detail.setMoney(cursor.getString(cursor.getColumnIndex("total")));
			detail.setName(cursor.getString(cursor.getColumnIndex("name")));
			//detail.setCount(cursor.getString(cursor.getColumnIndex("count")));
			infos.add(detail);
		}
		cursor.close();
		return infos;
	}

	/**
	 * 获取报表总金额
	 * @param date
	 * @param db
	 * @param condition
	 * @param personid
	 * @return
	 */
	private String findLogTotalMoney(long date, SQLiteDatabase db, int condition, String personid) {
		String sum = null;
		Cursor cursor = null;
		if(personid != null) {
			if(ConstantData.LOG_SALE == condition) {
				cursor = db.query("pay_type", new String[] {"sum(money)"}, "date(skrq) = date(?) and money >= 0 and personid = ?", new String[] { format.format(new Date(date)), personid }, null, null, null);
			}else if(ConstantData.LOG_RETURN == condition) {
				cursor = db.query("pay_type", new String[] {"sum(money)"}, "date(skrq) = date(?) and money < 0 and personid = ?", new String[] { format.format(new Date(date)), personid }, null, null, null);
			}else if(ConstantData.LOG_ALL == condition) {
				cursor = db.query("pay_type", new String[] {"sum(money)"}, "date(skrq) = date(?) and personid = ?", new String[] { format.format(new Date(date)), personid }, null, null, null);
			}
		}else {
			if(ConstantData.LOG_SALE == condition) {
				cursor = db.query("pay_type", new String[] {"sum(money)"}, "date(skrq) = date(?) and money >= 0", new String[] { format.format(new Date(date)) }, null, null, null);
			}else if(ConstantData.LOG_RETURN == condition) {
				cursor = db.query("pay_type", new String[] {"sum(money)"}, "date(skrq) = date(?) and money < 0", new String[] { format.format(new Date(date)) }, null, null, null);
			}else if(ConstantData.LOG_ALL == condition) {
				cursor = db.query("pay_type", new String[] {"sum(money)"}, "date(skrq) = date(?)", new String[] { format.format(new Date(date)) }, null, null, null);
			}
		}
		while(cursor.moveToNext()) {
			sum = cursor.getString(0);
		}
		cursor.close();
		return sum;
	}

	/**
	 * 获取报表交易条数
	 * @param date
	 * @param db
	 * @param condition
	 * @param personid
	 * @return
	 */
	private int findLogTotalCount(long date, SQLiteDatabase db, int condition, String personid) {
		int count = 0;
		Cursor cursor = null;
		if(personid != null) {
			if(ConstantData.LOG_SALE == condition) {
				cursor = db.query("user_order", new String[] {"count(*)"}, "date(saletime) = date(?) and totalmoney >= 0 and personid = ?", new String[] { format.format(new Date(date)), personid }, null, null, null);
			}else if(ConstantData.LOG_RETURN == condition) {
				cursor = db.query("user_order", new String[] {"count(*)"}, "date(saletime) = date(?) and totalmoney < 0 and personid = ?", new String[] { format.format(new Date(date)), personid }, null, null, null);
			}else if(ConstantData.LOG_ALL == condition) {
				cursor = db.query("user_order", new String[] {"count(*)"}, "date(saletime) = date(?) and personid = ?", new String[] { format.format(new Date(date)), personid }, null, null, null);
			}
		}else {
			if(ConstantData.LOG_SALE == condition) {
				cursor = db.query("user_order", new String[] {"count(*)"}, "date(saletime) = date(?) and totalmoney >= 0", new String[] { format.format(new Date(date)) }, null, null, null);
			}else if(ConstantData.LOG_RETURN == condition) {
				cursor = db.query("user_order", new String[] {"count(*)"}, "date(saletime) = date(?) and totalmoney < 0", new String[] { format.format(new Date(date)) }, null, null, null);
			}else if(ConstantData.LOG_ALL == condition) {
				cursor = db.query("user_order", new String[] {"count(*)"}, "date(saletime) = date(?)", new String[] { format.format(new Date(date)) }, null, null, null);
			}
		}
		while(cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	/**
	 * 获取离线支付信息BY time 提供给班报
	 *
	 * @return 离线支付信息
	 */
	public ReportInfo findPaytypeBytime(String personid,long date) {
		if(StringUtil.isEmpty(personid))
			return null;
		SQLiteDatabase db = helper.getReadableDatabase();
		//查销售数据
		SaleReportInfo saleReportInfo = null;
		List<ReportDetailInfo> sales = findLogData(date, db, ConstantData.LOG_SALE, personid);
		String saleMoney = findLogTotalMoney(date, db, ConstantData.LOG_SALE, personid);
		int saleCount = findLogTotalCount(date, db, ConstantData.LOG_SALE, personid);
		saleReportInfo = new SaleReportInfo();
		if(sales != null && sales.size() > 0) {
			saleReportInfo.setSalelist(sales);
		}
		saleReportInfo.setTotalmoney(TextUtils.isEmpty(saleMoney)?"0":saleMoney);
		saleReportInfo.setBillcount(saleCount+"");

		//查退货数据
		RefundReportInfo refundReportInfo = null;
		List<ReportDetailInfo> returns = findLogData(date, db, ConstantData.LOG_RETURN, personid);
		String returnMoney = findLogTotalMoney(date, db, ConstantData.LOG_RETURN, personid);
		int returnCount = findLogTotalCount(date, db, ConstantData.LOG_RETURN, personid);
		refundReportInfo = new RefundReportInfo();
		if(returns != null && returns.size() > 0) {
			refundReportInfo.setRefundlist(returns);
		}
		refundReportInfo.setTotalmoney(TextUtils.isEmpty(returnMoney)?"0":returnMoney);
		refundReportInfo.setBillcount(returnCount+"");

		//差合计数据
		TotalReportInfo totalReportInfo = null;
		List<ReportDetailInfo> totols = findLogData(date, db, ConstantData.LOG_ALL, personid);
		String totalMoney = findLogTotalMoney(date, db, ConstantData.LOG_ALL, personid);
		int totalCount = findLogTotalCount(date, db, ConstantData.LOG_ALL, personid);
		totalReportInfo = new TotalReportInfo();
		if(totols != null && totols.size() > 0) {
			totalReportInfo.setTotallist(totols);
		}
		totalReportInfo.setTotalmoney(TextUtils.isEmpty(totalMoney)?"0":totalMoney);
		totalReportInfo.setBillcount(totalCount+"");

		ReportInfo report = null;
		if((saleReportInfo.getSalelist() != null && saleReportInfo.getSalelist().size() > 0) || (refundReportInfo.getRefundlist() != null && refundReportInfo.getRefundlist().size() > 0) || (totalReportInfo.getTotallist() != null && totalReportInfo.getTotallist().size() > 0)) {
			report = new ReportInfo();
			report.setSale(saleReportInfo);
			report.setRefund(refundReportInfo);
			report.setTotal(totalReportInfo);
		}
		return report;
	}

	/**
	 * 测试数据完整性
	 * @param db
	 * @param billid
	 * @param personid
	 * @param cashier
	 * @param cashiername
	 * @param saletype
	 * @param totalmoney
	 * @param oldbillid
	 * @param oldposno
	 * @param backreason
	 * @param changemoney
	 * @param status
	 * @return
	 */
	public boolean textDB(SQLiteDatabase db, String billid, String personid, String cashier, String cashiername, String saletype, double totalmoney, String oldbillid, String oldposno, String backreason, double changemoney, String status) {
		boolean ret = false;
		ContentValues values=new ContentValues();
		values.put("billid", ArithDouble.parseInt(billid));
		values.put("personid", personid);
		values.put("cashier", cashier);
		values.put("cashiername", cashiername);
		values.put("saletype", saletype);
		values.put("totalmoney", totalmoney);
		values.put("time", format.format(new Date()));
		values.put("oldbillid", oldbillid);
		values.put("oldposno", oldposno);
		values.put("backreason", backreason);
		values.put("changemoney", changemoney);
		values.put("status", status);
		values.put("saletime", format.format(new Date()));
		if(db.insert("user_order", null, values) != -1) {
			ContentValues valuesgood = new ContentValues();
			valuesgood.put("billid", ArithDouble.parseInt(billid));
			valuesgood.put("id", "200001");
			valuesgood.put("goodsname", "HM");
			valuesgood.put("usedpoint", "0");
			valuesgood.put("barcode", "123");
			valuesgood.put("code", "000004");
			valuesgood.put("price", totalmoney);
			valuesgood.put("salecount", "1");
			valuesgood.put("inx", "1");
			valuesgood.put("discmoney", "0");
			valuesgood.put("preferentialmoney", "0");
			valuesgood.put("unit", "");
			valuesgood.put("saleamt", totalmoney);
			if(db.insert("goods_info", null, valuesgood) != -1) {
				ContentValues valuespaytype=new ContentValues();
				valuespaytype.put("billid", ArithDouble.parseInt(billid));
				valuespaytype.put("type", "1");
				valuespaytype.put("id", "1");
				valuespaytype.put("money", totalmoney);
				valuespaytype.put("overage", "0");
				valuespaytype.put("name", "现金");
				valuespaytype.put("personid", personid);
				valuespaytype.put("skrq", format.format(new Date()));
				if(db.insert("pay_type", null, valuespaytype) != -1) {
					ret = true;
				}else {
					LogUtil.v("lgs", "pay表插入错误");
				}
			}else {
				LogUtil.v("lgs", "goods表插入错误");
			}
		}else {
			LogUtil.v("lgs", "order表插入错误");
		}
		return ret;
	}

	/**
	 * 获取离线未上传数据条数
	 * @return
	 */
	public int getOffLineDataCount() {
		int ret = -1;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("user_order", new String[]{"count(*) AS count"}, "offline = ?", new String[]{"0"}, null, null, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return ret;
	}

	/**
	 * 获取离线未上传银行数据条数
	 * @return
	 */
	public int getBankOffLineDataCount() {
		int ret = -1;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("bank_info", new String[]{"count(*) AS count"}, "offline = ?", new String[]{"0"}, null, null, null);
		if (cursor.moveToFirst()) {
			ret = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return ret;
	}

	/**
	 * 删除首页操作银行卡的银行卡消费信息
	 * @param date
	 * @return
	 */
	public boolean deleteBankinfo(String date){
		boolean ret = false;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("pragma foreign_keys=on");
		int success = 0;
		success = db.delete("bank_info", "date(skrq) <= date(?) and offline = ? ", new String[]{ date, "1" });
		db.close();
		if(success > 0){
			ret = true;
		}
		LogUtil.i("lgs", "deleteOrder---"+ ret);
		return ret;
	}

	/**
	 *
	 * @param posno 款台号
	 * @param billid	小票号
	 * @param transtype  交易类型(1消费,2当日撤销,3隔日退货)
	 * @param cardno	卡号(支付宝账号、微信账号)
	 * @param bankcode	银行代码
	 * @param batchno	批次号
	 * @param refno	参考号
	 * @param tradeno	流水号
	 * @param amount	金额
	 * @param decmoney	扣减金额(预留，如果银行有扣减活动，则记录)
	 * @return
	 */
	public boolean addBankinfo(String posno, String billid, String transtype,
							   String cardno, String bankcode, String batchno,
							   String refno, String tradeno, String skfsid,
							   double amount, double decmoney){
		boolean ret = false;
		String time =  format.format(new Date());
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		ContentValues values=new ContentValues();
		values.put("posno", posno);
		values.put("billid", ArithDouble.parseInt(billid));
		values.put("transtype", transtype);
		values.put("cardno", cardno);
		values.put("bankcode", bankcode);
		values.put("batchno", batchno);
		values.put("refno", refno);
		values.put("tradeno", tradeno);
		values.put("skfsid", skfsid);
		values.put("amount", amount);
		values.put("decmoney", decmoney);
		values.put("skrq", time);
		int success = -1;
		success = (int) db.insert("bank_info", null, values);
		if(success != -1){
			ret = true;
			db.setTransactionSuccessful();
		}
		db.endTransaction();
		db.close();
		LogUtil.i("lgs", "addBankinfo---"+ ret);
		return ret;
	}



	public void setBankOfflineStatus(List<String>list){
		if(list!=null && list.size() > 0){
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
			for(String id:list){
				ContentValues values=new ContentValues();
				values.put("offline", "1");
				db.update("bank_info", values, "tradeno = ?", new String[]{ id });
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * 获取离线的银行信息
	 * @param beginBillID 开始位置订单号
	 * 			beginBillID 为空，表示重头开始，给出具体位置，标识从具体位置开始
	 * @param count  查询条数
	 * @return
	 */
	public List<OfflineBankInfo> getOfflineBankInfo(String beginBillID, int count){
		List<OfflineBankInfo> result = new ArrayList<OfflineBankInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = null;
		if(beginBillID == null) {
			cursor = db.query("bank_info", new String[]{"posno", "billid", "transtype", "cardno", "bankcode", "batchno", "refno", "tradeno", "amount", "skfsid","decmoney"}, "offline = ?", new String[] { "0"}, null, null, " tradeno asc limit " + count);
		}else {
			cursor = db.query("bank_info", new String[]{"posno", "billid", "transtype", "cardno", "bankcode", "batchno", "refno", "tradeno", "amount", "skfsid","decmoney"}, "offline = ? and tradeno > ?", new String[] { "0", beginBillID }, null, null, " tradeno asc limit " + count);
		}
		while(cursor.moveToNext()) {
			OfflineBankInfo info = new OfflineBankInfo();
			for(int i=0;i<cursor.getColumnCount();i++){
				try {
					Field field = OfflineBankInfo.class.getDeclaredField(cursor.getColumnName(i));
					if(field!=null){
						field.setAccessible(true);
						field.set(info, cursor.getString(i));
					}
				} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}
}
