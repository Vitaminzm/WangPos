package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.AddGoodAdapter;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.msg.entity.GoodsInfo;

import java.util.List;

/**
 * Description
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class AddScoreGoodDialog extends Dialog implements View.OnClickListener {
	private Context context;
	private ImageView imageview_close;
	private TextView text_title, text_confirm;
	private GridView gridview_good;
	private DialogFinishCallBack finishcallback;
	private AddGoodAdapter adapter;
	private List<GoodsInfo> datas;

	/**
	 *
	 * @param context
	 */
	public AddScoreGoodDialog(Context context, List<GoodsInfo> datas, DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.datas = datas;
		this.finishcallback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_add_score_good);
		this.setCanceledOnTouchOutside(false);
		initUI();
		setdata();
	}

	private void setdata() {
		adapter = new AddGoodAdapter(context, datas);
		gridview_good.setAdapter(adapter);
		gridview_good.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				adapter.setPosition(position);
			}
		});
	}

	private void initUI() {
		text_title = (TextView) findViewById(R.id.text_title);
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		gridview_good = (GridView) findViewById(R.id.gridview_score_good);
		imageview_close.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.imageview_close:
				dismiss();
				break;
			case R.id.text_confirm:
				finishcallback.finish(adapter.getPosition());
				dismiss();
				break;
		}
	}
}
