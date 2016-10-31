package com.symboltech.koolcloud.transmodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class OrderBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7643651095060323726L;
	private String resultCode = ""; // 00：业务正常，
	private String resultMsg = ""; // 错误信息（比如：密码错，金额不足、联网超时~）
	private String refNo = "";// 交易参考号
	private String transTime = "";// 交易时间
	private String orderNo = "";// 交易订单号
	private String acquId = "";// 支付机构号
	private String paymentIdDesc = "";// 交易支付活动名称
	private String paymentId = "";// 交易支付活动编号
	private String txnId = "";// 交易交易号
	private String orderState = "";// 交易状态
	private String transAmount = "";// 交易金额
	private String transType = "";// 交易类型编号
	private String operatorName = "";// 交易操作员
	private String merchantId = "";// 商户号
	private String terminalId = "";// 终端号
	private String accountNo = "";// 付款账号
	private String batchId = "";// 交易批次号
	private String traceId = "";// 交易流水号
	private String eWalletTraceId = "";// 互联网钱包流水号

	// =======================================以下为因界面需求添加的额外参数======================================//
//	private boolean isSuccess = false; // 交易是否成功
//	private AidlPaymentInfo aidlPaymentInfo = new AidlPaymentInfo("", "", "",
//			""); // 支付活动信息

//	public boolean isSuccess() {
//		return isSuccess;
//	}
//
//	public void setSuccess(boolean isSuccess) {
//		this.isSuccess = isSuccess;
//	}
//
//	public AidlPaymentInfo getAidlPaymentInfo() {
//		return aidlPaymentInfo;
//	}
//
//	public void setAidlPaymentInfo(AidlPaymentInfo aidlPaymentInfo) {
//		this.aidlPaymentInfo = aidlPaymentInfo;
//	}

	// =======================================以上为因界面需求添加的额外参数======================================//

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getAcquId() {
		return acquId;
	}

	public void setAcquId(String acquId) {
		this.acquId = acquId;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getTransTime() {
		return transTime;
	}

	public void setTransTime(String transTime) {
		this.transTime = transTime;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getPaymentName() {
		return paymentIdDesc;
	}

	public void setPaymentName(String paymentName) {
		this.paymentIdDesc = paymentName;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getOrderState() {
		return orderState;
	}

	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}

	public String getTransAmount() {
		return transAmount;
	}

	public void setTransAmount(String transAmount) {
		this.transAmount = transAmount;
	}

	public String getTransType() {
		return transType;
	}

	public void setTransType(String transType) {
		this.transType = transType;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String geteWalletTraceId() {
		return eWalletTraceId;
	}

	public void seteWalletTraceId(String eWalletTraceId) {
		this.eWalletTraceId = eWalletTraceId;
	}

	public static OrderBean getOrderBean(String jsonOrdInfo) {
		OrderBean orderBean = new OrderBean();
		try {
			JSONObject jsonObject = new JSONObject(jsonOrdInfo);
			orderBean.setTransTime(jsonObject.optString("transTime"));
			orderBean.setRefNo(jsonObject.optString("refNo"));
			orderBean.setPaymentName(jsonObject.optString("paymentIdDesc"));
			orderBean.setPaymentId(jsonObject.optString("paymentId"));
			orderBean.setTxnId(jsonObject.optString("txnId"));
			orderBean.setTransAmount(jsonObject.optString("transAmount"));
			orderBean.setTransType(jsonObject.optString("transType"));
			orderBean.setOperatorName(jsonObject.optString("operatorName"));
			orderBean.setAcquId(jsonObject.optString("acquId"));
			orderBean.setTraceId(jsonObject.optString("traceId"));
			orderBean.setBatchId(jsonObject.optString("batchId"));
			orderBean.setMerchantId(jsonObject.optString("merchantId"));
			orderBean.setAccountNo(jsonObject.optString("accountNo"));
			orderBean.setOrderState(jsonObject.optString("orderState"));
			orderBean.setOrderNo(jsonObject.optString("orderNo"));
			orderBean.setTerminalId(jsonObject.optString("terminalId"));
			orderBean.seteWalletTraceId(jsonObject.optString("eWalletTraceId"));

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return orderBean;
	}
}
