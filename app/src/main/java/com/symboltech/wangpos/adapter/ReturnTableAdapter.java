package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.utils.ArithDouble;

import java.util.List;


public class ReturnTableAdapter extends BaseAdapter {

	private Context context;
	List<PayMentsInfo> datas;
	private LayoutInflater inflater;

	public void refreshData(List<PayMentsInfo> infos) {
		if(infos != null && infos.size() > 0){
			this.datas.addAll(infos);
			notifyDataSetChanged();
		}
	}

	public ReturnTableAdapter(Context context, List<PayMentsInfo> datas) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.datas = datas;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Viewholder holder = null;
		if(convertView == null) {
			holder = new Viewholder();
			convertView = inflater.inflate(R.layout.item_tab_comtent, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.text_sale_total);
			holder.tv_money = (TextView) convertView.findViewById(R.id.text_sale_times);
			holder.tv_count = (TextView) convertView.findViewById(R.id.text_unit_price);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		holder.tv_name.setText(datas.get(position).getName());
		holder.tv_money.setText(datas.get(position).getMoney());
		if(ArithDouble.parseDouble(datas.get(position).getOverage()) > 0)
			holder.tv_count.setText(context.getString(R.string.overrage)+datas.get(position).getOverage()+context.getString(R.string.yuan));
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_name, tv_money, tv_count;
	}

}
