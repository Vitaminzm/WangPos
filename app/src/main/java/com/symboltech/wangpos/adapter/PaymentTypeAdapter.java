package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.utils.PaymentTypeEnum;

import java.util.List;


public class PaymentTypeAdapter extends BaseAdapter {

	private Context context;
	List<PayMentsInfo> paymentsInfo;
	private PayMentsInfo payType;
	private LayoutInflater inflater;

	private Handler mHandler = new Handler();
	/**
	 * 获取支付方式
	 * @return
	 */
	public PayMentsInfo getPayType(){
		return payType;
	}

	public void setPayTpye(int position){
		payType = paymentsInfo.get(position);
		notifyDataSetChanged();
	}

	public void setPayTpyeNull(){
		if(payType != null && PaymentTypeEnum.getpaymentstyle(payType.getType()) == PaymentTypeEnum.CASH){
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					payType = null;
					//支付宝，微钱包会稍后使用ID，所以ID不能置空
					notifyDataSetChanged();
				}
			}, 300);
		}else{
			payType = null;
			//支付宝，微钱包会稍后使用ID，所以ID不能置空
			notifyDataSetChanged();
		}
	}

	public PaymentTypeAdapter(Context context, List<PayMentsInfo> datas) {
		super();
		inflater = LayoutInflater.from(context);
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
			convertView = inflater.inflate(R.layout.item_pay_type, null);
			holder.image_key = (ImageView) convertView.findViewById(R.id.item_image_key);
			holder.name_key = (TextView) convertView.findViewById(R.id.item_name_key);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		switchUI(position, holder);
		return convertView;
	}

	private void switchUI(int position, Viewholder holder) {
		switch (PaymentTypeEnum.getpaymentstyle(paymentsInfo.get(position).getType().trim())) {
			case WECHAT:
			case ALIPAY:
				if(ConstantData.WECHAT_ID.equals(paymentsInfo.get(position).getId())){
					if (payType != null && PaymentTypeEnum.getpaymentstyle(payType.getType()) == PaymentTypeEnum.WECHAT && paymentsInfo.get(position).getId().equals(payType.getId())) {
						holder.image_key.setImageResource(R.mipmap.weixin_icon_select);
						holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
					} else {
						holder.image_key.setImageResource(R.mipmap.weixin_icon);
						holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
					}
					holder.name_key.setText(paymentsInfo.get(position).getName());
					break;
				}else if(ConstantData.ALPAY_ID.equals(paymentsInfo.get(position).getId())){
					if (payType != null && PaymentTypeEnum.getpaymentstyle(payType.getType()) == PaymentTypeEnum.ALIPAY && paymentsInfo.get(position).getId().equals(payType.getId())) {
						holder.image_key.setImageResource(R.mipmap.zhifubao_icon_select);
						holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
					} else {
						holder.image_key.setImageResource(R.mipmap.zhifubao_icon);
						holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
					}
					holder.name_key.setText(paymentsInfo.get(position).getName());
					break;
				}
			case BANK:
				if (payType != null && PaymentTypeEnum.getpaymentstyle(payType.getType()) == PaymentTypeEnum.BANK && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.card_icon_selcet);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.card_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
			case CASH:
				if (payType != null && PaymentTypeEnum.getpaymentstyle(payType.getType()) == PaymentTypeEnum.CASH && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.xianjin_icon_select);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.xianjin_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
			default:
				break;
		}
	}

	public void add(PayMentsInfo couponInfo){
		paymentsInfo.add(couponInfo);
		notifyDataSetChanged();
	}

	public void setPayTpye(PayMentsInfo value){
		payType = value;
	}

	private class Viewholder {
		public ImageView image_key;
		public TextView name_key;
	}

}
