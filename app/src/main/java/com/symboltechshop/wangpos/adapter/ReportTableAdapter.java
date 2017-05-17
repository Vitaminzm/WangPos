package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.msg.entity.ReportDetailInfo;

import java.util.List;


public class ReportTableAdapter extends BaseAdapter {

	private Context context;
	List<ReportDetailInfo> datas;
	private LayoutInflater inflater;

	public void refreshData(List<ReportDetailInfo> infos) {
		if(infos != null && infos.size() > 0){
			this.datas.addAll(infos);
			notifyDataSetChanged();
		}
	}

	public ReportTableAdapter(Context context, List<ReportDetailInfo> datas) {
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
		holder.tv_count.setText(datas.get(position).getCount());
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_name, tv_money, tv_count;
	}

}
