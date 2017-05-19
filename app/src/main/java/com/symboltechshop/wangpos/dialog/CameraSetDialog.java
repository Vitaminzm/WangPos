package com.symboltechshop.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.app.ConstantData;
import com.symboltechshop.wangpos.utils.SpSaveUtils;
import com.symboltechshop.wangpos.utils.Utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;


/**
 * 脱机选择框
 * @author so
 *
 */
public class CameraSetDialog extends BaseDialog implements OnClickListener {

	private Context context;
	private Spinner sp_bind_plate;
	private ImageView imageview_close;
	private TextView text_cancle, text_confirm;
	private ArrayAdapter<String> bind_adapter;
	private List<String> bind_data;
	public CameraSetDialog(Context context) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCanceledOnTouchOutside(false);
		setContentView(R.layout.dialog_camera_set);
		initView();
		initEvent();
	}

	private void initView() {

		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);

		imageview_close = (ImageView) findViewById(R.id.imageview_close);

		sp_bind_plate = (Spinner) findViewById(R.id.sp_bind_plate);

		bind_data = Arrays.asList(context.getResources().getStringArray(R.array.cameraType));

		bind_adapter = new ArrayAdapter<String>(context, R.layout.item_car_plate, bind_data);
		bind_adapter.setDropDownViewResource(R.layout.item_car_plate_drop);
		sp_bind_plate.setAdapter(bind_adapter);
		int type = SpSaveUtils.readInt(context, ConstantData.CAMERATYPE, 1);
		sp_bind_plate.setSelection(type);
	}

	
	public void initEvent(){
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		switch (v.getId()) {
		case R.id.text_cancle:
		case R.id.imageview_close:
			// 取消

			break;
		case R.id.text_confirm:
			// 确定
			SpSaveUtils.writeInt(context, ConstantData.CAMERATYPE, sp_bind_plate.getSelectedItemPosition());
			break;
		default:
			break;
		}
		this.dismiss();
	}
	

	
	public void setDropDownHeight(Spinner mSpinner, int pHeight){
		try {
			Field field=Spinner.class.getDeclaredField("mPopup");
			field.setAccessible(true);
			ListPopupWindow popUp=(ListPopupWindow)field.get(mSpinner);
			popUp.setHeight(pHeight);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
