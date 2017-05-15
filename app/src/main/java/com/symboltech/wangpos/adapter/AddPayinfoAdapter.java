package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.ThirdPayInfo;
import com.symboltech.wangpos.utils.SpSaveUtils;

import java.util.List;


public class AddPayinfoAdapter extends BaseAdapter {

	private Context context;
	List<ThirdPayInfo> thirdPayInfos;
	private int position = 0;
	private LayoutInflater inflater;

	public int getPosition(){
		return position;
	}

	public void setPosition(int position){
		this.position = position;
		notifyDataSetChanged();
	}

	public AddPayinfoAdapter(Context context, List<ThirdPayInfo> goods) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.thirdPayInfos = goods;
	}

	@Override
	public int getCount() {
		return thirdPayInfos == null ? 0 : thirdPayInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return thirdPayInfos == null ? null : thirdPayInfos.get(position);
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
			convertView = inflater.inflate(R.layout.item_thirdpay_info, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.text_payment_type);
			holder.tv_info = (TextView) convertView.findViewById(R.id.text_payment_money);
			holder.text_cancle = (TextView) convertView.findViewById(R.id.text_cancle);
			convertView.setTag(holder);
		}else {
			holder = (Viewholder) convertView.getTag();
		}
		holder.tv_name.setText(getPayNameById(thirdPayInfos.get(position).getSkfsid()));
		holder.tv_info.setText(thirdPayInfos.get(position).getJe());
		return convertView;
	}
	
	private class Viewholder {
		public TextView tv_name, tv_info, text_cancle;
	}

	private String getPayNameById(String id){
		List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(context, ConstantData.PAYMENTSLIST);
		if(paymentslist == null)
			return null;
		for (int i = 0; i < paymentslist.size(); i++) {
			if (paymentslist.get(i).getId().equals(id)) {
				return paymentslist.get(i).getName();
			}
		}
		return null;
	}
}
