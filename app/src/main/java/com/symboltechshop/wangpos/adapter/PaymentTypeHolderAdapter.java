package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.msg.entity.PayMentsInfo;
import com.symboltechshop.wangpos.utils.PaymentTypeEnum;

import java.util.List;

/**
 * 用于扫纸券后添加纸券信息的适配器
 * 
 * <p>
 * detailed comment
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 2015年11月11日
 * @see
 * @since 1.0
 */
public class PaymentTypeHolderAdapter extends RecyclerView.Adapter<PaymentTypeHolderAdapter.ViewHolder> {

	private List<PayMentsInfo> paymentsInfo;
	private Context context;

	private LayoutInflater mLayoutInflater;
	private MyItemClickListener mItemClickListener;

	private PayMentsInfo payType;
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
	public PaymentTypeHolderAdapter(List<PayMentsInfo> datas, Context context) {
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
		this.paymentsInfo = datas;
	}

	public void add(PayMentsInfo couponInfo){
		paymentsInfo.add(couponInfo);
		notifyDataSetChanged();
	}

	public void setPayTpye(PayMentsInfo value){
		payType = value;
	}

	public PayMentsInfo getItem(int position){
		return paymentsInfo.get(position);
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
		public TextView name_key;
		public ImageView image_key;
		private MyItemClickListener mListener;
		public ViewHolder(View view, MyItemClickListener listener){
			super(view);
			this.mListener = listener;
			name_key = (TextView) view.findViewById(R.id.item_name_key);
			image_key = (ImageView) view.findViewById(R.id.item_image_key);
			view.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View v) {
			if(mListener != null){
	            mListener.onItemClick(v,getPosition());  
	        }  
		}
	}


	@Override
	public int getItemCount() {
		return paymentsInfo.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		switchUI(position, holder);
	}

	private void switchUI(int position, ViewHolder holder) {
		switch (PaymentTypeEnum.getpaymentstyle(paymentsInfo.get(position).getType().trim())) {
			case WECHAT:
				if (payType != null && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.weixin_icon_select);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.weixin_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
			case ALIPAY:
				if (payType != null && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.zhifubao_icon_select);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.zhifubao_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
			case BANK:
			case STORE:
				if (payType != null  && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.card_icon_selcet);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.card_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
			case CASH:
				if (payType != null && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.xianjin_icon_select);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.xianjin_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
			default:
				if (payType != null  && paymentsInfo.get(position).getId().equals(payType.getId())) {
					holder.image_key.setImageResource(R.mipmap.xianjin_icon_select);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.orange));
				} else {
					holder.image_key.setImageResource(R.mipmap.xianjin_icon);
					holder.name_key.setTextColor(context.getResources().getColor(R.color.font_color));
				}
				holder.name_key.setText(paymentsInfo.get(position).getName());
				break;
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		 View view = LayoutInflater.from(context).inflate(R.layout.item_pay_type, arg0, false);
	     return new ViewHolder(view, mItemClickListener);
	}
	
	/** 
     * 设置Item点击监听 
     * @param listener 
     */  
    public void setOnItemClickListener(MyItemClickListener listener){  
        this.mItemClickListener = listener;  
    }  
    
	public interface MyItemClickListener {  
	    public void onItemClick(View view, int postion);
	}
}
