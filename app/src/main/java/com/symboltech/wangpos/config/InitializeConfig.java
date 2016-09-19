package com.symboltech.wangpos.config;

import android.content.Context;
import android.widget.Toast;

import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.msg.entity.InitializeInfo;
import com.symboltech.wangpos.utils.SpSaveUtils;


/**
 * 初始化pos数据
 * @author so
 *
 */
public class InitializeConfig {

	/**
	 * 店铺pos初始化信息
	 * @param info  包括 商品列表、 收款方式列表、促销信息 、 退货原因、营业员信息
	 */
	public static void initialize(Context context, InitializeInfo info) {
		//商品信息
		if(info.getBrandgoodslist() == null || info.getBrandgoodslist().size() == 0) {
			Toast.makeText(context, "商品初始化失败", Toast.LENGTH_SHORT).show();
		}else {
			SpSaveUtils.saveObject(context, ConstantData.BRANDGOODSLIST, info.getBrandgoodslist());
		}
		//收款方式
		if(info.getPaymentslist() == null || info.getPaymentslist().size() == 0) {
			Toast.makeText(context, "收款方式初始化失败", Toast.LENGTH_SHORT).show();
		}else {
			SpSaveUtils.saveObject(context, ConstantData.PAYMENTSLIST, info.getPaymentslist());
		}
		//促销信息
		if(info.getPromlist() == null || info.getPromlist().size() == 0) {
			//Toast.makeText(context, "促销信息初始化失败", Toast.LENGTH_SHORT).show();
		}else {
			SpSaveUtils.saveObject(context, ConstantData.PROMOTIONINFOLIST, info.getPromlist());
		}
		//退货原因
		if(info.getRefundreasonlist() == null || info.getRefundreasonlist().size() == 0) {
			Toast.makeText(context, "退款原因初始化失败", Toast.LENGTH_SHORT).show();
		}else {
			SpSaveUtils.saveObject(context, ConstantData.REFUNDREASONLIST, info.getRefundreasonlist());
		}
		//营业员
		if(info.getSalemanlist() == null || info.getSalemanlist().size() == 0) {
			Toast.makeText(context, "营业员初始化失败", Toast.LENGTH_SHORT).show();
		}else {
			SpSaveUtils.saveObject(context, ConstantData.SALEMANLIST, info.getSalemanlist());
		}
	}
	
	/**
	 * 清除登录时缓存的数据
	 */
	public static void clearCash(Context context) {
		//存在离线模式，部分数据不能清除
		return;
//		SpSaveUtils.delete(context, ConstantData.BRANDGOODSLIST);//商品缓存
//		SpSaveUtils.delete(context, ConstantData.PAYMENTSLIST);//收款方式缓存
//		SpSaveUtils.delete(context, ConstantData.PROMOTIONINFOLIST);//促销信息缓存
//		SpSaveUtils.delete(context, ConstantData.REFUNDREASONLIST);//退货原因缓存
//		SpSaveUtils.delete(context, ConstantData.SALEMANLIST);//营业员缓存
	}
}
