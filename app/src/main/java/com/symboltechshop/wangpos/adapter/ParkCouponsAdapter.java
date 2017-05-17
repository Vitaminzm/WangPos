package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;

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
public class ParkCouponsAdapter extends RecyclerView.Adapter<ParkCouponsAdapter.ViewHolder> {

	private List<String> couponinfos;
	private Context context;

	private boolean isUseful = true;
	private ArrayList <Integer> selectPosition;
	private LayoutInflater mLayoutInflater;
	private MyItemClickListener mItemClickListener;

	public ParkCouponsAdapter(List<String> couponinfos, int flag, Context context) {
		this.couponinfos =  couponinfos;
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
		selectPosition = new ArrayList<Integer>();
		if(flag == 1){
			for(int i=0; i < couponinfos.size();i++){
				addSelect(i);
			}
		}
	}

	public ParkCouponsAdapter(List<String> couponinfos, int flag, boolean isUseful, Context context) {
		this.couponinfos =  couponinfos;
		this.context = context;
		this.isUseful = isUseful;
		mLayoutInflater = LayoutInflater.from(context);
		selectPosition = new ArrayList<Integer>();
		if(flag == 1){
			for(int i=0; i < couponinfos.size();i++){
				addSelect(i);
			}
		}
	}

	public String getItem(int position){
		return couponinfos.get(position);
	}
	
	public List<Integer> getSelect(){
		return selectPosition;
	}
	public void addSelect(int position){
		selectPosition.add((Integer)position);
	}

	public  int getSelectedCount(){
		return selectPosition.size();
	}
	public void clearSelect(){
		selectPosition.clear();
	}
	public void delSelect(int position){
		for(Integer i:selectPosition){
			if(i == position){
				selectPosition.remove(i);
				return ;
			}
		}
	}

	public boolean isSelect(int position){
		for(Integer i:selectPosition){
			if(i == position){
				return true;
			}
		}
		return false;
	}


	public class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
		public TextView coupon_value, coupon_name, coupon_unit, item_unit;
		public ImageView coupon_select;
		public RelativeLayout coupon_background;
		private MyItemClickListener mListener;
		public ViewHolder(View view, MyItemClickListener listener){
			super(view);
			this.mListener = listener; 
			coupon_background = (RelativeLayout) view.findViewById(R.id.rl_background);
			coupon_value = (TextView) view.findViewById(R.id.item_coupon_money);
			coupon_select = (ImageView) view.findViewById(R.id.imageview_coupon_selected);
			coupon_name = (TextView) view.findViewById(R.id.item_coupon_name);
			coupon_unit = (TextView) view.findViewById(R.id.item_coupon__unit);
			item_unit = (TextView) view.findViewById(R.id.item_unit);
			view.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View v) {
			if(mListener != null){
	            mListener.onItemClick(v,getPosition());  
	        }  
		}
	}


	@Override
	public int getItemCount() {
		return couponinfos.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.coupon_name.setText("停车券");
		holder.coupon_unit.setText("小时");
		holder.item_unit.setVisibility(View.INVISIBLE);
		holder.coupon_value.setText(couponinfos.get(position));
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		 View view = LayoutInflater.from(context).inflate(R.layout.item_coupon, arg0, false);
	     return new ViewHolder(view, mItemClickListener);
	}
	
	/** 
     * 设置Item点击监听 
     * @param listener 
     */  
    public void setOnItemClickListener(MyItemClickListener listener){  
        this.mItemClickListener = listener;  
    }  
    
	public interface MyItemClickListener {  
	    public void onItemClick(View view, int postion);
	}
}
