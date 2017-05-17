package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.msg.entity.RefundReasonInfo;

import java.util.List;

public class ReturnReasonAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<RefundReasonInfo> reasons;
	
	public ReturnReasonAdapter(Context context, List<RefundReasonInfo> reasons) {
		super();
		this.reasons = reasons;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return reasons == null ? 0 : reasons.size();
	}

	@Override
	public Object getItem(int position) {
		return reasons == null ? null : reasons.get(position);
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
		holder.tv.setText(reasons.get(position).getName());
		return convertView;
	}
	
	private class ViewHolder {
		TextView tv;
	}

}
