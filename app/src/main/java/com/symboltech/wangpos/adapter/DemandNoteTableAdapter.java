package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.ReportDetailInfo;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;

import java.util.List;


public class DemandNoteTableAdapter extends BaseAdapter {

	private Context context;
	List<ReportDetailInfo> datas;
	private View.OnTouchListener listener;
	private LayoutInflater inflater;

	public void refreshData(List<ReportDetailInfo> infos) {
		if(infos != null && infos.size() > 0){
			this.datas.addAll(infos);
			notifyDataSetChanged();
		}
	}

	String coupon;
	String store;
	public DemandNoteTableAdapter(Context context, List<ReportDetailInfo> datas, View.OnTouchListener listener) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.datas = datas;
		this.listener = listener;
		coupon = getSKFSByType(PaymentTypeEnum.COUPON.getStyletype());
		store = getSKFSByType(PaymentTypeEnum.STORE.getStyletype());
	}

	@Override
	public int getCount() {
		return datas == null ? 0 : datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas == null ? null : datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private String getSKFSByType(String type) {
		List<PayMentsInfo> paymentInfos = (List<PayMentsInfo>) SpSaveUtils.getObject(context, ConstantData.PAYMENTSLIST);
		for (PayMentsInfo payMentsInfo : paymentInfos) {
			if(type.equals(payMentsInfo.getType())) {
				return payMentsInfo.getId();
			}
		}
		return null;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Viewholder holder = null;
		if(convertView == null) {
			holder = new Viewholder();
			convertView = inflater.inflate(R.layout.item_edtab_comtent, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.text_sale_total);
			holder.tv_money = (TextView) convertView.findViewById(R.id.text_sale_times);
			holder.tv_count = (TextView) convertView.findViewById(R.id.text_unit_price);
			holder.tv_money.setTag("1-"+position);
			holder.tv_count.setTag("2-"+position);
			if(listener != null){
				holder.tv_money.setOnTouchListener(listener);
				holder.tv_count.setOnTouchListener(listener);
			}
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
			holder.tv_money.setTag("1-"+position);
			holder.tv_count.setTag("2-"+position);
		}
		ReportDetailInfo info = datas.get(position);
//		if(PaymentTypeEnum.ALIPAY.getStyletype().equals(info.getCode())
//				|| PaymentTypeEnum.BANK.getStyletype().equals(info.getCode())
//				||PaymentTypeEnum.WECHAT.getStyletype().equals(info.getCode())){
//			holder.count.setEnabled(true);
//			holder.money.setEnabled(true);
//		}else

		if((coupon!= null && coupon.equals(info.getCode()))||(store!= null && store.equals(info.getCode()))){
			holder.tv_money.setEnabled(false);
			holder.tv_count.setEnabled(false);
		}else{
			holder.tv_money.setEnabled(true);
			holder.tv_count.setEnabled(true);
		}
		holder.tv_name.setText(datas.get(position).getName());
		holder.tv_money.setText(datas.get(position).getMoney());
		holder.tv_count.setText(datas.get(position).getCount());
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_name, tv_money, tv_count;
	}

}
