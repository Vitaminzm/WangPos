package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 离线商品和营业员
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class GoodsAndSalerListInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	private List<GoodsAndSalerInfo> list;

	public List<GoodsAndSalerInfo> getList() {
		return list;
	}

	public void setList(List<GoodsAndSalerInfo> list) {
		this.list = list;
	}
}
