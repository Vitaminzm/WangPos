package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.TicketInfo;

public class BillResult extends BaseResult {

	@SerializedName("data")
	private TicketInfo ticketInfo;

	public TicketInfo getTicketInfo() {
		return ticketInfo;
	}

	public void setTicketInfo(TicketInfo ticketInfo) {
		this.ticketInfo = ticketInfo;
	}

}
