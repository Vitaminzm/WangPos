package com.symboltechshop.wangpos.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.msg.entity.GoodsInfo;
import com.symboltechshop.wangpos.utils.ArithDouble;
import com.symboltechshop.wangpos.utils.MoneyAccuracyUtils;

import java.util.List;


public class GoodsAdapter extends BaseAdapter {

	private Context context;
	private List<GoodsInfo> goods;
	private LayoutInflater inflater;
	public int position;
	private Boolean flag = true;//数量是否允许增减
	public CompentOnTouch compentOntouch;
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int count = ArithDouble.parseInt(goods.get(position).getSalecount());
			switch (msg.what) {
				case 1:
					if(count > 1){
						goods.get(position).setSalecount((count-1)+"");
						GoodsAdapter.this.notifyDataSetChanged();
					}
					break;
				case 2:
					goods.get(position).setSalecount((count+1)+"");
					GoodsAdapter.this.notifyDataSetChanged();
					break;
			}
			super.handleMessage(msg);
		}
	};
	public GoodsAdapter(Context context, List<GoodsInfo> goods, Boolean flag) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.goods = goods;
		this.flag = flag;
		compentOntouch = new CompentOnTouch();
	}
	public GoodsAdapter(Context context, List<GoodsInfo> goods) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.goods = goods;
		compentOntouch = new CompentOnTouch();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		Viewholder holder = null;
		GoodsInfo goodsInfo = goods.get(position);
		if(convertView == null) {
			holder = new Viewholder();
			convertView = inflater.inflate(R.layout.item_shoppinggood, null);
			holder.text_good_name = (TextView) convertView.findViewById(R.id.text_good_name);
			holder.ll_bg = (LinearLayout) convertView.findViewById(R.id.ll_bg);
			holder.text_money = (TextView) convertView.findViewById(R.id.text_money);
			holder.text_score = (TextView) convertView.findViewById(R.id.text_score);
			holder.text_good_code = (TextView) convertView.findViewById(R.id.text_good_code);
			holder.text_good_count = (TextView) convertView.findViewById(R.id.text_good_count);
			holder.imageview_quantity_plus = (ImageView) convertView.findViewById(R.id.imageview_quantity_plus);
			holder.imageview_quantity_minus = (ImageView) convertView.findViewById(R.id.imageview_quantity_minus);
			holder.ll_score_info = (LinearLayout) convertView.findViewById(R.id.ll_score_info);
			if(flag){
				holder.imageview_quantity_plus.setOnTouchListener(compentOntouch);
				holder.imageview_quantity_minus.setOnTouchListener(compentOntouch);
			}
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		if((position%2)== 1){
			holder.ll_bg.setBackgroundColor(context.getResources().getColor(R.color.good_bg));
		}else{
			holder.ll_bg.setBackgroundColor(context.getResources().getColor(R.color.white));
		}
		holder.imageview_quantity_plus.setTag(position);
		holder.imageview_quantity_minus.setTag(position);
		holder.text_good_name.setText(goodsInfo.getGoodsname());
		int type = Integer.parseInt(goodsInfo.getSptype().trim());
		switch (type) {
			case 1://ConstantData.GOODS_SOURCE_BY_INTEGRAL:
				holder.text_good_code.setText(goodsInfo.getBarcode() + "/" + context.getString(R.string.score_good));
				break;
			case 0://ConstantData.GOODS_SOURCE_BY_BRAND:
				holder.text_good_code.setText(goodsInfo.getBarcode()+"/"+context.getString(R.string.brand_good));
				holder.ll_score_info.setVisibility(View.GONE);
				break;
			case 2://ConstantData.GOODS_SOURCE_BY_SINTEGRAL:
				holder.text_good_code.setText(goodsInfo.getBarcode() + "/" + context.getString(R.string.sscore_good));
				break;
			case 3://ConstantData.GOODS_SOURCE_BY_BINTEGRAL:
				holder.text_good_code.setText(goodsInfo.getBarcode()+"/"+context.getString(R.string.brandbig_good));
				holder.ll_score_info.setVisibility(View.GONE);
				break;
			default:
				break;
		}
		if(flag){
			holder.text_score.setText(goodsInfo.getUsedpointtemp());
		}else{
			holder.text_score.setText(goodsInfo.getUsedpoint());
		}
		holder.text_good_count.setText(goodsInfo.getSalecount());
		holder.text_money.setText(MoneyAccuracyUtils.formatMoneyByTwo(goodsInfo.getSaleamt()));
		return convertView;
	}


	private class Viewholder {
		public TextView text_good_name, text_money, text_score, text_good_code, text_good_count;
		public ImageView imageview_quantity_plus, imageview_quantity_minus;
		public LinearLayout ll_score_info, ll_bg;
	}
	class CompentOnTouch implements View.OnTouchListener {

		private  boolean isOnLongClick = false;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			position = (Integer) v.getTag();
			switch (v.getId()) {
				//这是btnMius下的一个层，为了增强易点击性
				case R.id.imageview_quantity_plus:
					onTouchChange("plus", event.getAction());
					break;
				//这里也写，是为了增强易点击性
				case R.id.imageview_quantity_minus:
					onTouchChange("mius", event.getAction());
					break;
			}
			return true;
		}

		private void onTouchChange(String methodName, int eventAction) {
			//按下松开分别对应启动停止减线程方法
			if ("mius".equals(methodName)) {
				if (eventAction == MotionEvent.ACTION_DOWN) {
					isOnLongClick = true;
					new MiusThread().start();
				} else if (eventAction == MotionEvent.ACTION_UP) {
						isOnLongClick = false;
				} else if (eventAction == MotionEvent.ACTION_MOVE ) {
						isOnLongClick = true;
				}else if(eventAction ==MotionEvent.ACTION_CANCEL){
					isOnLongClick = false;
				}
			}
			//按下松开分别对应启动停止加线程方法
			else if ("plus".equals(methodName)) {
				if (eventAction == MotionEvent.ACTION_DOWN) {
					isOnLongClick = true;
					new PlusThread().start();
				} else if (eventAction == MotionEvent.ACTION_UP) {
						isOnLongClick = false;
				} else if (eventAction == MotionEvent.ACTION_MOVE) {
						isOnLongClick = true;
				}else if(eventAction ==MotionEvent.ACTION_CANCEL){
					isOnLongClick = false;
				}
			}
		}
		//减操作
		class MiusThread extends Thread {
			@Override
			public void run() {
				while (isOnLongClick) {
					try {
						Thread.sleep(200);
						mHandler.sendEmptyMessage(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					super.run();
				}
			}
		}

		//加操作
		class PlusThread extends Thread {
			@Override
			public void run() {
				while (isOnLongClick) {
					try {
						Thread.sleep(200);
						mHandler.sendEmptyMessage(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					super.run();
				}
			}
		}
	}
}
