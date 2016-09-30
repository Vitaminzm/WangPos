package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.utils.StringUtil;

import java.util.List;


public class PaymentTypeInfoDetailAdapter extends BaseAdapter {

	private List<PayMentsInfo> paymentsInfo;
	private Context context;

	private LayoutInflater mLayoutInflater;

	public PaymentTypeInfoDetailAdapter(Context context, List<PayMentsInfo> datas) {
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

	public void add(PayMentsInfo info){
		paymentsInfo.add(info);
		this.notifyDataSetChanged();
	}
}
