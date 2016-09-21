package com.symboltech.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.TicketFormatInfo;

/**
 * 小票格式返回结果
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm
 * @see
 * @since 1.0
 */
public class TicketFormatResult extends BaseResult {

	@SerializedName("data")
	private TicketFormatInfo ticketformat;

	public TicketFormatInfo getTicketformat() {
		return ticketformat;
	}

	public void setTicketformat(TicketFormatInfo ticketformat) {
		this.ticketformat = ticketformat;
	}

	
}
