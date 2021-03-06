package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.msg.entity.GoodsInfo;
import com.symboltech.wangpos.view.TextScrollView;

import java.util.List;


public class AddGoodAdapter extends BaseAdapter {

	private Context context;
	List<GoodsInfo> goods;
	private int position = 0;
	private LayoutInflater inflater;

	public int getPosition(){
		return position;
	}

	public void setPosition(int position){
		this.position = position;
		notifyDataSetChanged();
	}

	public AddGoodAdapter(Context context, List<GoodsInfo> goods) {
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
			convertView = inflater.inflate(R.layout.item_add_good, null);
			holder.tv_name = (TextScrollView) convertView.findViewById(R.id.text_good_name);
			holder.tv_info = (TextView) convertView.findViewById(R.id.text_good_info);
			holder.tv_code = (TextView) convertView.findViewById(R.id.text_good_code);
			holder.ll_add_good = (LinearLayout) convertView.findViewById(R.id.ll_add_good);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		holder.tv_name.setText(goods.get(position).getGoodsname());
		if(ConstantData.GOODS_SOURCE_BY_BRAND.equals(goods.get(position).getSptype()) || ConstantData.GOODS_SOURCE_BY_BINTEGRAL.equals(goods.get(position).getSptype())){
			holder.tv_info.setText("￥" + goods.get(position).getPrice());
		}else {
			holder.tv_info.setText("￥" + goods.get(position).getPrice() + "  积分" + goods.get(position).getPoint());
		}
		holder.tv_code.setText(goods.get(position).getCode());
		if(position == this.position){
			holder.ll_add_good.setBackgroundResource(R.drawable.circle_corner_bt_shape);
		}else{
			holder.ll_add_good.setBackgroundResource(R.drawable.circle_corner_hit_bt_shape);
		}
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_code, tv_info;
		public TextScrollView tv_name;
		public LinearLayout ll_add_good;
	}

}
