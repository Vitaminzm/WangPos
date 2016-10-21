package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.msg.entity.CouponInfo;
import com.symboltech.wangpos.msg.entity.GoodsInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于扫纸券后添加纸券信息的适配器
 * 
 * <p>
 * detailed comment
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 2015年11月11日
 * @see
 * @since 1.0
 */
public class RecommandGoodAdapter extends RecyclerView.Adapter<RecommandGoodAdapter.ViewHolder> {

	private Context context;
	List<GoodsInfo> goods;
	private LayoutInflater mLayoutInflater;

	public RecommandGoodAdapter(List<GoodsInfo> goods, Context context) {
		this.goods =  goods;
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
	}


	public GoodsInfo getItem(int position){
		return goods.get(position);
	}
	

	public class ViewHolder extends RecyclerView.ViewHolder{
		public TextView tv_name, tv_info;
		public LinearLayout ll_add_good;
		public ViewHolder(View view){
			super(view);
			tv_name = (TextView) view.findViewById(R.id.text_good_name);
			tv_info = (TextView) view.findViewById(R.id.text_good_info);
			ll_add_good = (LinearLayout) view.findViewById(R.id.ll_add_good);
		}
	}


	@Override
	public int getItemCount() {
		return goods.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.tv_name.setText(goods.get(position).getGoodsname());
		if(goods.get(position).getSptype().equals("0")){
			holder.tv_info.setText("￥" + goods.get(position).getPrice());
		}else {
			holder.tv_info.setText("￥" + goods.get(position).getPrice() + "  积分" + goods.get(position).getPoint());
		}
		holder.ll_add_good.setBackgroundResource(R.drawable.circle_corner_bt_shape);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		 View view = LayoutInflater.from(context).inflate(R.layout.item_recommand_good, arg0, false);
	     return new ViewHolder(view);
	}
	
}
