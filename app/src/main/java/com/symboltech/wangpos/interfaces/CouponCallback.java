package com.symboltech.wangpos.interfaces;

import com.symboltech.wangpos.msg.entity.CouponInfo;

/**
 * 用于会员验证相关选择
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月2日 下午8:26:40
 * @version 1.0
 */
public interface CouponCallback {

	public void doResult(CouponInfo couponInfo);
}
