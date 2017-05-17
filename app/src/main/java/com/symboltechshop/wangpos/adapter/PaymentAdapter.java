package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.msg.entity.PayMentsInfo;

import java.util.List;

public class PaymentAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<PayMentsInfo> payments;
	
	public PaymentAdapter(Context context, List<PayMentsInfo> payments) {
		super();
		this.payments = payments;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return payments == null ? 0 : payments.size();
	}

	@Override
	public Object getItem(int position) {
		return payments == null ? null : payments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.return_reason_item, null);
			holder.tv = (TextView) convertView.findViewById(R.id.return_reason_item_tv);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		PayMentsInfo info = payments.get(position);
		holder.tv.setText(info.getName());
		return convertView;
	}

	private class ViewHolder {
		TextView tv;
	}

}
