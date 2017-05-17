package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.msg.entity.CashierInfo;

import java.util.List;


public class AddSalemanAdapter extends BaseAdapter {

	private Context context;
	List<CashierInfo> datas;
	private int position = 0;
	private LayoutInflater inflater;

	public int getPosition(){
		return position;
	}

	public void setPosition(int position){
		this.position = position;
		notifyDataSetChanged();
	}

	public AddSalemanAdapter(Context context, List<CashierInfo> datas) {
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
			convertView = inflater.inflate(R.layout.item_add_saleman, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.text_saleman_name);
			holder.ll_add_saleman = (LinearLayout) convertView.findViewById(R.id.ll_add_saleman);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		holder.tv_name.setText(datas.get(position).getCashiername());
		if(position == this.position){
			holder.ll_add_saleman.setBackgroundResource(R.drawable.circle_corner_bt_shape);
		}else{
			holder.ll_add_saleman.setBackgroundResource(R.drawable.circle_corner_hit_bt_shape);
		}
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_name;
		public LinearLayout ll_add_saleman;
	}

}
