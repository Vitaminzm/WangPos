package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.msg.entity.CashierInfo;
import com.symboltech.wangpos.msg.entity.PayMentsCancleInfo;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.StringUtil;

import java.util.Collection;
import java.util.List;


public class PaymentTypeInfoAdapter extends BaseAdapter {

	private List<PayMentsCancleInfo> paymentsInfo;
	private Context context;

	private LayoutInflater mLayoutInflater;
	private Boolean isCancled = true;

	public Boolean getIsCancled() {
		return isCancled;
	}

	public void setIsCancled(Boolean isCancled) {
		this.isCancled = isCancled;
	}

	public PaymentTypeInfoAdapter(Context context, List<PayMentsCancleInfo> datas) {
		super();
		mLayoutInflater = LayoutInflater.from(context);
		this.context = context;
		this.paymentsInfo = datas;
	}

	@Override
	public int getCount() {
		return paymentsInfo == null ? 0 : paymentsInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return paymentsInfo == null ? null : paymentsInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Viewholder holder = null;
		if(convertView == null) {
			holder = new Viewholder();
			convertView = mLayoutInflater.inflate(R.layout.item_payment_info, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.name_key);
			holder.tv_value = (TextView) convertView.findViewById(R.id.name_value);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		holder.tv_name.setText(StringUtil.formatRString(8,paymentsInfo.get(position).getName()+":"));
		holder.tv_value.setText(StringUtil.formatRString(8, paymentsInfo.get(position).getMoney()));
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_name,tv_value;
	}

	/**
	 * 当类型是现金，补录找零的时候，只显示一次，不会重复添加，但银行卡，支付宝，微钱包会重复添加
	 * @param couponInfo
	 */
	public void add(PayMentsCancleInfo couponInfo){
		for(int i=0;i<paymentsInfo.size();i++){
			if( paymentsInfo.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype())
					|| paymentsInfo.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype())
					|| paymentsInfo.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype())
					||paymentsInfo.get(i).getType().equals( PaymentTypeEnum.LING.getStyletype())
					||paymentsInfo.get(i).getType().equals( PaymentTypeEnum.CASH.getStyletype())){
				if(couponInfo.getId().equals(paymentsInfo.get(i).getId())){
					paymentsInfo.get(i).setMoney(couponInfo.getMoney());
					notifyDataSetChanged();
					return;
				}
			}
		}
		paymentsInfo.add(couponInfo);
		notifyDataSetChanged();
	}

	public void clear(Collection<PayMentsCancleInfo> collection){
		paymentsInfo.clear();
		addAll(collection);
	}
	public void remove(PayMentsCancleInfo couponInfo){
		for(int i=0;i<paymentsInfo.size();i++){
			if(couponInfo.getType().equals(paymentsInfo.get(i).getType())){
				paymentsInfo.remove(i);
				notifyDataSetChanged();
				return;
			}
		}
	}

	//去掉找零部分
	public void removeLing(){
		for(int i=0;i<paymentsInfo.size();i++){
			if(PaymentTypeEnum.LING.getStyletype().equals(paymentsInfo.get(i).getType())){
				paymentsInfo.remove(i);
				notifyDataSetChanged();
				return;
			}
		}
	}

	// 计算支付总金额
	public double getPayMoney(){
		double ret = 0;
		for(int i=0;i<paymentsInfo.size();i++){
			if(PaymentTypeEnum.LING.getStyletype().equals(paymentsInfo.get(i).getType())){
				ret = ArithDouble.sub(ret, ArithDouble.parseDouble(paymentsInfo.get(i).getMoney()));
			}else{
				ret = ArithDouble.add(ret, ArithDouble.parseDouble(paymentsInfo.get(i).getMoney()));
			}
		}
		return ret;
	}
	public void addAll(Collection<PayMentsCancleInfo> collection){
		paymentsInfo.addAll(collection);
		notifyDataSetChanged();
	}

	public String getMoneyById(String id){
		if(id ==null){
			return null;
		}
		for(int i=0;i<paymentsInfo.size();i++){
			if(id.equals(paymentsInfo.get(i).getId())){
				return paymentsInfo.get(i).getMoney();
			}
		}
		return null;
	}
}
