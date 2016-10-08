package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

public class ExchangeScoreInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ExchangemsgInfo exchmsg;

	public ExchangemsgInfo getExchmsg() {
		return exchmsg;
	}

	public void setExchmsg(ExchangemsgInfo exchmsg) {
		this.exchmsg = exchmsg;
	}
	
}
