package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.dialog.CanclePayDialog;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.PayMentsCancleInfo;
import com.symboltech.wangpos.utils.PaymentTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 撤销adapter
 * 
 * <p>
 * detailed comment
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 2015年11月14日
 * @see
 * @since 1.0
 */
public class CanclePayAdapter extends BaseAdapter {

	
	private List<PayMentsCancleInfo> paymentsInfo;
	private List<PayMentsCancleInfo> paymentsInfoAdapter;
	private Context context;

	private LayoutInflater mLayoutInflater;
	public CanclePayAdapter(List<PayMentsCancleInfo> paymentsInfo, Context context) {
		this.paymentsInfo = paymentsInfo;
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
		initSourceData();
		
	}
	
	/**
	 * 初始化撤销的笔数
	 * 目前只有支付宝，微钱包才显示撤销
	 * @return
	 */
	
	public void initSourceData(){
		paymentsInfoAdapter = new ArrayList<PayMentsCancleInfo>();
		if(paymentsInfo!= null){
			for(int i=0;i<paymentsInfo.size();i++){
				if(paymentsInfo.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype()) ||
						paymentsInfo.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype())|| paymentsInfo.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype()) ||
						paymentsInfo.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype()) ||paymentsInfo.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())){
					//paymentsInfo.get(i).setIsCancle(true);
				}else{
						paymentsInfoAdapter.add(paymentsInfo.get(i));
				}
			}
		}
		LogUtil.i("lgs", "------====="+paymentsInfoAdapter.size());
	}
	
	
	@Override
	public int getCount() {
		return paymentsInfoAdapter.size();
	}

	@Override
	public Object getItem(int position) {
		return paymentsInfoAdapter == null ? null : paymentsInfoAdapter.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void add(PayMentsCancleInfo couponInfo){
		paymentsInfoAdapter.add(couponInfo);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mLayoutInflater.inflate(R.layout.item_cancle_pay, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		switchUI(position, holder);
		return convertView;
	}

	private void switchUI(int position, ViewHolder holder) {
		switch (PaymentTypeEnum.getpaymentstyle(paymentsInfoAdapter.get(position).getType().trim())) {
		case WECHAT:
			if(paymentsInfoAdapter.get(position).getId().equals(ConstantData.YIPAY_ID)){
				holder.name_key.setText("翼支付");
			}else if(paymentsInfoAdapter.get(position).getId().equals(ConstantData.WECHAT_ID)){
				holder.name_key.setText(R.string.weichat);
			}else if(paymentsInfoAdapter.get(position).getId().equals(ConstantData.ALPAY_ID)){
				holder.name_key.setText(R.string.alipay);
			}else if(paymentsInfoAdapter.get(position).getId().equals(ConstantData.BANKCODE_ID)){
				holder.name_key.setText("银联扫码");
			}
			break;
		case ALIPAY:
			holder.name_key.setText(R.string.alipay);
			break;
		case BANK:
			holder.name_key.setText(R.string.bank);
			break;
		case STORE:
			holder.name_key.setText(R.string.store);
			break;
		case CASH:
			if(ConstantData.YXLM_ID.equals(paymentsInfoAdapter.get(position).getId())){
				holder.name_key.setText(R.string.yxlm);
			}else{
				holder.name_key.setText(R.string.cash);
			}

			break;
		case HANDRECORDED:
			holder.name_key.setText(R.string.recorded);
			break;
		default:
			break;
		}
		holder.name_value.setText(paymentsInfoAdapter.get(position).getMoney());
		holder.name_opt.setText(paymentsInfoAdapter.get(position).getDes());
		if(paymentsInfoAdapter.get(position).getDes().equals(context.getString(R.string.cancled))
				||paymentsInfoAdapter.get(position).getDes().equals(context.getString(R.string.cancled_failed))
				||paymentsInfoAdapter.get(position).getDes().equals(context.getString(R.string.cancled_query))){
			holder.name_opt.setClickable(true);
			holder.name_opt.setTextColor(context.getResources().getColor(R.color.white));
			holder.name_opt.setBackgroundColor(context.getResources().getColor(R.color.green));
		}if(paymentsInfoAdapter.get(position).getDes().equals(context.getString(R.string.cancleing_pay))){
			holder.name_opt.setTextColor(context.getResources().getColor(R.color.orange));
			holder.name_opt.setBackgroundColor(context.getResources().getColor(R.color.white));
			holder.name_opt.setClickable(false);
		}else if(paymentsInfoAdapter.get(position).getDes().equals(context.getString(R.string.cancled_pay))){
			holder.name_opt.setTextColor(context.getResources().getColor(R.color.green));
			holder.name_opt.setBackgroundColor(context.getResources().getColor(R.color.white));
			holder.name_opt.setClickable(false);
		}
		holder.name_opt.setTag(position);
	}
	
	class ViewHolder {
		public TextView name_key, name_value, name_opt;

		public ViewHolder(View view){
			name_key = (TextView) view.findViewById(R.id.text_payment_type);
			name_value = (TextView) view.findViewById(R.id.text_payment_money);
			name_opt = (TextView) view.findViewById(R.id.text_cancle);
			name_opt.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((CanclePayDialog)context).saleVoid((Integer) name_opt.getTag());
				}
			});
		}

	}
}
