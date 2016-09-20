package com.symboltech.wangpos.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.KeyBoardListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class HorizontalKeyBoard extends Dialog implements OnClickListener, OnTouchListener, OnDismissListener {

	private Context context;
	private EditText editText;
	private TextView textView;
	private KeyBoardListener listener;
	private TextView bt0, bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9;
	private TextView dot, clean, back, cancel, comfirm;
	/**编辑框触发键盘*/
	private final int FLAG_EDIT = 0;
	/**文本框触发键盘*/
	private final int FLAG_TEXT = 1;
	/**自动触发键盘*/
	private final int FLAG_NULL = 2;
	private int flag = FLAG_EDIT;
	private String defalutStr = "";   //默认文本
	
	private int length = 0;	//输入框在键盘被弹出时，要被推上去的距离
	private Window mWindow;
	private Object mObject;
	public static int mScreenWidth=-1;//未知宽高
	public static int mScreenHeight=-1;
	public static int screenh_nonavbar=-1;	//不包含导航栏的高度 
	public static int real_scontenth =-1;	//实际内容高度，  计算公式:屏幕高度-导航栏高度-电量栏高度
	private View mContentView;
	private WindowManager mManager;
	boolean isFocusOutside = true;
	int isActivity = 0;
	/**
	 * 
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param listener
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.listener = listener;
		flag = FLAG_NULL;
		initView();
	}

	/**
	 * 
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText 编辑框
	 * @param listener 确定按钮监听, 不监听传null
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText = editText;
		this.listener = listener;
		flag = FLAG_EDIT;
		initView();
	}
	
	/**
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText 编辑框
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText = editText;
		flag = FLAG_EDIT;
		initView();
	}
	/**
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText 编辑框
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText,int isActivity) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText = editText;
		this.isActivity = isActivity;
		flag = FLAG_EDIT;
		initView();
	}
	
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText, boolean isFocusOutside) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText = editText;
		flag = FLAG_EDIT;
		this.isFocusOutside = isFocusOutside;
		initView();
	}
	
	/**
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param textView 文本框
	 * @param listener 确定按钮监听, 不监听传null
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, TextView textView, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.textView = textView;
		this.listener = listener;
		flag = FLAG_TEXT;
		initView();
	}
	
	/**
	 * 
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param textView 文本框
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, TextView textView) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.textView = textView;
		flag = FLAG_TEXT;
		initView();
	}
	
	private void initView() {
		setContentView(R.layout.dialog_horizontal_keyboard);
		bt0 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_0);
		bt1 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_1);
		bt2 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_2);
		bt3 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_3);
		bt4 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_4);
		bt5 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_5);
		bt6 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_6);
		bt7 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_7);
		bt8 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_8);
		bt9 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_9);
		dot = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_dot);
		back = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_back);
		cancel = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_cancel);
		clean = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_c);
		comfirm = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_comfirm);
		initEvent();
		initSetting();
		initScreenParams(context);
	}
	
	@Override
	public void show() {
			try {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			} catch (Exception e) {
		}
		if(null != mContentView){
			  int[] pos=new int[2];
			  if(mObject instanceof Dialog) {
				  mWindow.getDecorView().getLocationOnScreen(pos);
			  }else {
				  if(flag == FLAG_EDIT) {
					  editText.getLocationOnScreen(pos);
				  }else if(flag == FLAG_TEXT) {
					  textView.getLocationOnScreen(pos);
				  }
				  if(isActivity == 1){
					  mWindow.getDecorView().getLocationOnScreen(pos);
				  }
			  }
			  float height = dpToPx(getContext(), 500);

			  Rect outRect = new Rect();
			  mContentView.getWindowVisibleDisplayFrame(outRect);

			  length = 0;
			  if(mObject instanceof Dialog) {
				  length=(int)((pos[1]+mWindow.getDecorView().getHeight()-outRect.top)-(mScreenHeight-height));
			  }else {
				  if(flag == FLAG_EDIT) {
					  length=(int)((pos[1]+editText.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }else if(flag == FLAG_TEXT) {
					  length=(int)((pos[1]+textView.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }
			  }
			  if(isActivity == 1){
				  length=(int)((pos[1]+mWindow.getDecorView().getHeight()-outRect.top)-(mScreenHeight-height));
			  }
			  if(length>0){
				 if(!(mObject instanceof Dialog)){
					 mContentView.scrollBy(0, length);
				 }else{
					 mManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
					 mManager.updateViewLayout(mWindow.getDecorView(), getParams(-length));
				 }
			  }
		  }
		super.show();
	}
	
	private LayoutParams getParams(int speed) {
		LayoutParams params = mWindow.getAttributes();
		params.y = speed;
		return params;
	}

	@Override
	public void dismiss() {
		try {
			if (length > 0) {
				if(!(mObject instanceof Dialog)){
					 mContentView.scrollBy(0, -length);
					 length = 0;
				 }else{
					 length = 0;
					 mManager.updateViewLayout(mWindow.getDecorView(), getParams(length));
				 }
				
			}
		} catch (Exception e) {
		}
		super.dismiss();
	}
	/**
	 * 密度转换为像素值
	 * 
	 * @param dp
	 * @return
	 */
	public static int dpToPx(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
	
	private void initScreenParams(Context context){
		DisplayMetrics dMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(dMetrics); 
        
        mScreenWidth = dMetrics.widthPixels;
        mScreenHeight = dMetrics.heightPixels;
        
       /* screenh_nonavbar = mScreenHeight;
		
        int ver = Build.VERSION.SDK_INT;
        
        // 新版本的android 系统有导航栏，造成无法正确获取高度
        if (ver == 13){
            try {
                Method mt = display.getClass().getMethod("getRealHeight");
                screenh_nonavbar = (Integer)mt.invoke(display);
            }catch (Exception e){
            }
        } else if (ver > 13){
            try{
                Method mt = display.getClass().getMethod("getRawHeight");
                screenh_nonavbar = (Integer)mt.invoke(display);
            }catch (Exception e){
            }
        }
        
        real_scontenth = screenh_nonavbar-getStatusBarHeight(context);*/
        
  
	}
	
	/**
	 * 电量栏高度
	 * @return
	 */
	public static int getStatusBarHeight(Context context){
	       Class<?> c = null; 
			 Object obj = null; 
			 Field field = null;
			 int x = 0,
			 sbar = 0; 
			 try { 
				 c = Class.forName("com.android.internal.R$dimen");
				 obj = c.newInstance();
				 field = c.getField("status_bar_height");
				 x = Integer.parseInt(field.get(obj).toString()); 
				 sbar = context.getResources().getDimensionPixelSize(x);
			 } catch (Exception e1) {
				 e1.printStackTrace();
			 }
			 
			 return sbar;
	}

	private void initSetting() {
		((Activity) context).getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		try {
			if(editText != null) {
				Class<EditText> cls = EditText.class;
				Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setShowSoftInputOnFocus.setAccessible(true);
				setShowSoftInputOnFocus.invoke(editText, false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if(mObject instanceof Dialog) {
			mWindow = ((Dialog)mObject).getWindow();
			((Dialog) mObject).setOnDismissListener(this);
		}else {
			mWindow = ((Activity) mObject).getWindow();
		}
		LayoutParams attributes = mWindow.getAttributes();
		attributes.format = 1;
		mWindow.setAttributes(attributes);
		mContentView = mWindow.findViewById(Window.ID_ANDROID_CONTENT);
		Window window = this.getWindow();
		window.setGravity(Gravity.LEFT|Gravity.BOTTOM);
		LayoutParams params = window.getAttributes();
		params.windowAnimations = R.anim.slide_up_in;
		if(isFocusOutside)
			params.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		params.x = 7;
		params.y = 0;
		window.setAttributes(params);
	}

	private void initEvent() {
		if(editText != null) {
			editText.setOnTouchListener(this);
		}
		if(textView != null) {
			textView.setOnTouchListener(this);
		}
		bt0.setOnClickListener(this);
		bt1.setOnClickListener(this);
		bt2.setOnClickListener(this);
		bt3.setOnClickListener(this);
		bt4.setOnClickListener(this);
		bt5.setOnClickListener(this);
		bt6.setOnClickListener(this);
		bt7.setOnClickListener(this);
		bt8.setOnClickListener(this);
		bt9.setOnClickListener(this);
		dot.setOnClickListener(this);
		back.setOnClickListener(this);
		cancel.setOnClickListener(this);
		clean.setOnClickListener(this);
		comfirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == back.getId()) {
			if(flag == FLAG_EDIT && editText.getSelectionStart() > 0) {
				editText.getText().delete(editText.getSelectionStart() - 1, editText.getSelectionStart());
			}else if(flag == FLAG_TEXT && textView.getText() != null) {
				textView.getEditableText().delete(textView.getText().length() - 1, textView.getText().length());
			}
		}else if(v.getId() == cancel.getId()) {
			if(flag == FLAG_EDIT) {
				editText.setText(defalutStr);
				if(defalutStr != null)
					editText.setSelection(defalutStr.length());
			}else if(flag == FLAG_TEXT) {
				textView.setText(defalutStr);
			}
			defalutStr = "";
			dismiss();
			if(listener != null) {
				listener.onCancel();
			}
		}else if(v.getId() == clean.getId()) {
			if(flag == FLAG_EDIT && editText.getSelectionStart() > 0) {
				editText.getText().clear();
			}else if(flag == FLAG_TEXT && textView.getText() != null) {
				textView.setText("");
			}
		}else if(v.getId() == comfirm.getId()) {
			if(flag == FLAG_EDIT && (TextUtils.isEmpty(editText.getText().toString()) || "".equals(editText.getText().toString()))) {
				Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
				return;
			}else if(flag == FLAG_TEXT && (TextUtils.isEmpty(textView.getText().toString()) || "".equals(textView.getText().toString()))) {
				Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
				return;
			}
			if(listener != null) {
				listener.onComfirm();
				if(flag == FLAG_EDIT) {
					listener.onValue(editText.getText().toString().trim());
				}else if(flag == FLAG_TEXT) {
					listener.onValue(textView.getText().toString().trim());
				}
			}
			dismiss();
		}else {
			if(flag == FLAG_EDIT) {
				editText.getText().insert(editText.getSelectionStart(), (String) v.getTag());
			}else if(flag == FLAG_TEXT) {
				textView.getEditableText().insert(textView.getText() == null ? 0 : textView.getText().length(), (String) v.getTag());
			}
		}
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		dismiss();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			if(editText != null && editText.getId() == v.getId()) {
				editText.requestFocus();
				if(editText.getCompoundDrawables()[2] != null && event.getX() > (editText.getWidth() - editText.getTotalPaddingRight()) && (event.getX() < ((editText.getWidth() - editText.getPaddingRight())))) {
					return false;
				}else {
					defalutStr = editText.getText().toString();
					if(defalutStr != null)
						editText.setSelection(defalutStr.length());
					//editText.setText("");
					show();
				}
			}else if(textView != null && textView.getId() == v.getId()) {
				if(textView.getCompoundDrawables()[2] != null && event.getX() > (textView.getWidth() - textView.getTotalPaddingRight()) && (event.getX() < ((textView.getWidth() - textView.getPaddingRight())))) {
					return false;
				}else {
					defalutStr = textView.getText().toString();
					textView.setText("");
					show();
				}
			}
			return true;
		}else{
			return false;
		}
	}
	
}
