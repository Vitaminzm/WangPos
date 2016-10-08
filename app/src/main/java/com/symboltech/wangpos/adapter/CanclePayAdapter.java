package com.symboltech.wangpos.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.msg.entity.PayMentsCancleInfo;
import com.symboltech.wangpos.result.ThirdPayCancelResult;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 撤销adapter
 * 
 * <p>
 * detailed comment
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 2015年11月14日
 * @see
 * @since 1.0
 */
public class CanclePayAdapter extends BaseAdapter {

	
	private List<PayMentsCancleInfo> paymentsInfo;
	private List<PayMentsCancleInfo> paymentsInfoAdapter;
	private Context context;
	
	private LayoutInflater mLayoutInflater;
	private Boolean isCancled = true;

	private int isCancleCount = 0;
	
	public int getIsCancleCount(){
		return isCancleCount;
	}
	
	public Boolean getIsCancled() {
		return isCancled;
	}

	public void setIsCancled(Boolean isCancled) {
		this.isCancled = isCancled;
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;

			case 2:
				paymentsInfoAdapter.get(msg.arg1).setIsCancle(true);;
				((TextView)msg.obj).setText(R.string.thirdpay_cancle_succeed);
				((TextView)msg.obj).setTextColor(context.getResources().getColor(R.color.green));
				break;
			case 3:
				((TextView)msg.obj).setClickable(true);
				((TextView)msg.obj).setText(R.string.waring_cancle_failed_msg);
				((TextView)msg.obj).setTextColor(context.getResources().getColor(R.color.white));
				((TextView)msg.obj).setBackgroundColor(context.getResources().getColor(R.color.green));
				break;
			default:
				break;
			}
		};
	};
	
	public CanclePayAdapter(List<PayMentsCancleInfo> paymentsInfo, Context context) {
		this.paymentsInfo = paymentsInfo;
		this.context = context;
		mLayoutInflater = LayoutInflater.from(context);
		initSourceData();
		
	}
	
	/**
	 * 初始化撤销的笔数
	 * 目前只有支付宝，微钱包才显示撤销
	 * @return
	 */
	
	public void initSourceData(){
		paymentsInfoAdapter = new ArrayList<>();
		if(paymentsInfo!= null){
			for(int i=0;i<paymentsInfo.size();i++){
				if(paymentsInfo.get(i).getType().equals(PaymentTypeEnum.BANK.getStyletype()) || paymentsInfo.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype()) ||
						paymentsInfo.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype())|| paymentsInfo.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype()) ||
						paymentsInfo.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype()) ||paymentsInfo.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())){
					//paymentsInfo.get(i).setIsCancle(true);
				}else{
						paymentsInfoAdapter.add(paymentsInfo.get(i));
				}
			}
		}
	}
	
	
	@Override
	public int getCount() {
		return paymentsInfoAdapter.size();
	}

	@Override
	public Object getItem(int position) {
		return paymentsInfoAdapter == null ? null : paymentsInfoAdapter.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void add(PayMentsCancleInfo couponInfo){
		paymentsInfoAdapter.add(couponInfo);
		notifyDataSetChanged();
	}
	
	public void clear(Collection<PayMentsCancleInfo> collection){
		paymentsInfoAdapter.clear();
        addAll(collection);
    }
	
	public void addAll(Collection<PayMentsCancleInfo> collection){
		paymentsInfoAdapter.addAll(collection);
		notifyDataSetChanged();
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mLayoutInflater.inflate(R.layout.item_cancle_pay, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		switchUI(position, holder);
		return convertView;
	}

	private void switchUI(int position, ViewHolder holder) {
		switch (PaymentTypeEnum.getpaymentstyle(paymentsInfoAdapter.get(position).getType().trim())) {
		case WECHAT:
			holder.name_key.setText(R.string.weichat);
			break;
		case ALIPAY:
			holder.name_key.setText(R.string.alipay);
			break;
		case BANK:
			holder.name_key.setText(R.string.bank);
			break;
		case CASH:
			holder.name_key.setText(R.string.cash);
			break;
		case HANDRECORDED:
			holder.name_key.setText(R.string.recorded);
			break;
		default:
			break;
		}
		holder.name_value.setText(paymentsInfoAdapter.get(position).getMoney());
		holder.name_opt.setTag(position);
	}
	
	class ViewHolder {
		public TextView name_key, name_value, name_opt;

		public ViewHolder(View view){
			name_key = (TextView) view.findViewById(R.id.text_payment_type);
			name_value = (TextView) view.findViewById(R.id.text_payment_money);
			name_opt = (TextView) view.findViewById(R.id.text_cancle);
			name_opt.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					v.setClickable(false);
					((TextView)v).setText(R.string.waring_cancle_msg);
					((TextView)v).setTextColor(context.getResources().getColor(R.color.orange));
					((TextView)v).setBackgroundColor(context.getResources().getColor(R.color.popup_default_bg));
					int position = (int) v.getTag();
					canclePay(position, v);
				}
			});
		}

	}
	/**
	 * 按钮点击撤销支付
	 * @param position
	 */
	public void canclePay(int position, View view){
		thirdcancle(position, view);
	}
	
	/**
	 * 支付撤销
	 * 
	 * @author zmm Email:mingming.zhang@symboltech.com 2015年11月14日
	 * @Description: 
	 */
	private void thirdcancle(final int position, final View view) {
		//LogUtil.i("lgs", "-----"+position);
		isCancleCount++;
		PayMentsCancleInfo info = paymentsInfoAdapter.get(position);
		Map<String, String> map = new HashMap<String, String>();
		if(info.getType().equals(PaymentTypeEnum.ALIPAY.getStyletype())){
			map.put("pay_type", ConstantData.PAYMODE_BY_ALIPAY+"");
		}else if(info.getType().equals(PaymentTypeEnum.WECHAT.getStyletype())){
			map.put("pay_type", ConstantData.PAYMODE_BY_WEIXIN+"");
		}
		map.put("old_trade_no", info.getThridPay().getTrade_no());
		map.put("billid", MyApplication.getBillId());
		HttpRequestUtil.getinstance().thirdpaycancel(map, ThirdPayCancelResult.class,
				new HttpActionHandle<ThirdPayCancelResult>() {

					@Override
					public void handleActionStart() {

					}

					@Override
					public void handleActionFinish() {
						isCancleCount--;
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						((TextView)view).setClickable(true);
						((TextView)view).setText(R.string.waring_cancle_failed_msg);
						((TextView)view).setTextColor(context.getResources().getColor(R.color.white));
						((TextView)view).setBackgroundColor(context.getResources().getColor(R.color.green));
					}

					@Override
					public void handleActionSuccess(String actionName, ThirdPayCancelResult result) {
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
							paymentsInfoAdapter.get(position).setIsCancle(true);;
							((TextView)view).setText(R.string.thirdpay_cancle_succeed);
							((TextView)view).setTextColor(context.getResources().getColor(R.color.green));
							handler.sendEmptyMessageDelayed(4, 1000 * 2);
						} else {
							((TextView)view).setClickable(true);
							((TextView)view).setText(R.string.waring_cancle_failed_msg);
							((TextView)view).setTextColor(context.getResources().getColor(R.color.white));
							((TextView)view).setBackgroundColor(context.getResources().getColor(R.color.green));
						}
					}

				});
	}
}
