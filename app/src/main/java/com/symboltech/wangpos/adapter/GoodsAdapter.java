package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;

import java.util.List;


public class GoodsAdapter extends BaseAdapter {

	private Context context;
	private List<String> goods;
	private LayoutInflater inflater;
	
	public GoodsAdapter(Context context, List<String> goods) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.goods = goods;
	}

	@Override
	public int getCount() {
		return goods == null ? 0 : goods.size();
	}

	@Override
	public Object getItem(int position) {
		return goods == null ? null : goods.get(position);
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
			convertView = inflater.inflate(R.layout.item_shoppinggood, null);
			holder.tv = (TextView) convertView.findViewById(R.id.text_good_name);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		holder.tv.setText(goods.get(position));
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv;
	}

}
