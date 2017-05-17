package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.SubmitGoods;

/**
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月12日 下午5:38:44
 * @version 1.0
 */
public class SubmitGoodsResult extends BaseResult {
	@SerializedName("data")
	private SubmitGoods submitgoods;

	public SubmitGoods getSubmitgoods() {
		return submitgoods;
	}

	public void setSubmitgoods(SubmitGoods submitgoods) {
		this.submitgoods = submitgoods;
	}

}
