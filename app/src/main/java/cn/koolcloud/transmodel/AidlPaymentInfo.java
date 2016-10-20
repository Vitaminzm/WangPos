package cn.koolcloud.transmodel;

import java.io.Serializable;

public class AidlPaymentInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String acquId;
	private String paymentId;
	private String paymentName;
	private String paymentIconCode;
	private String productNo;
	/**
	 * @param acquId
	 *            支付机构编号
	 * @param paymentId
	 *            支付活动编号
	 * @param paymentName
	 *            支付活动名称
	 * @param paymentIconCode
	 */
	public AidlPaymentInfo(String acquId, String paymentId, String paymentName,
			String paymentIconCode, String productNo) {
		this.acquId = acquId;
		this.paymentId = paymentId;
		this.paymentName = paymentName;
		this.paymentIconCode = paymentIconCode;
		this.productNo = productNo;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public String getAcquId() {
		return acquId;
	}

	public void setAcquId(String acquId) {
		this.acquId = acquId;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentName() {
		return paymentName;
	}

	public void setPaymentName(String paymentName) {
		this.paymentName = paymentName;
	}

	public String getPaymentIconCode() {
		return paymentIconCode;
	}

	public void setPaymentIconCode(String paymentIconCode) {
		this.paymentIconCode = paymentIconCode;
	}

}
