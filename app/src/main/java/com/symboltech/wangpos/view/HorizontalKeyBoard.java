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
	private EditText editText1, editText2;
	private TextView textView;
	private KeyBoardListener listener;
	private TextView bt0, bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, bt00;
	private TextView dot, clean, back, cancle, confirm;
	/**编辑框触发键盘*/
	private final int FLAG_EDIT = 0;
	/**文本框触发键盘*/
	private final int FLAG_TEXT = 1;
	/**自动触发键盘*/
	private final int FLAG_NULL = 2;
	/**编辑框触发键盘*/
	private final int FLAG_EDIT2 = 3;
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
		this.editText1 = editText;
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
		this.editText1 = editText;
		flag = FLAG_EDIT;
		initView();
	}
	/**
	 *
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText1 编辑框1
     * @param editText2 编辑框2
	 * @param listener 确定按钮监听, 不监听传null
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText1,EditText editText2, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.listener = listener;
		this.editText1 = editText1;
		this.editText2 = editText2;
		flag = FLAG_EDIT;
		initView();
	}

	/**
	 *
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText
	 * @param isFocusOutside 点击外部是否消失
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText, boolean isFocusOutside) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText1 = editText;
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
		bt00 = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_00);
		dot = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_dot);
		back = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_c);
		cancle = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_cancle);
		clean = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_clean);
		confirm = (TextView) findViewById(R.id.dialog_horizontal_keyboard_bt_confirm);
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
		mWindow.setWindowAnimations(R.style.AnimationFade); //设置窗口弹出动画
		if(null != mContentView){
			  int[] pos=new int[2];
			  if(mObject instanceof Dialog) {
				  mWindow.getDecorView().getLocationOnScreen(pos);
			  }else {
				  if(flag == FLAG_EDIT) {
					  editText1.getLocationOnScreen(pos);
				  }else if(flag == FLAG_EDIT2){
					  editText2.getLocationOnScreen(pos);
				  }else if(flag == FLAG_TEXT) {
					  textView.getLocationOnScreen(pos);
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
					  length=(int)((pos[1]+editText1.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }else if(flag == FLAG_EDIT2) {
					  length=(int)((pos[1]+editText2.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }else if(flag == FLAG_TEXT) {
					  length=(int)((pos[1]+textView.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }
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
			if(editText1 != null) {
				Class<EditText> cls = EditText.class;
				Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setShowSoftInputOnFocus.setAccessible(true);
				setShowSoftInputOnFocus.invoke(editText1, false);
			}
			if(editText2 != null) {
				Class<EditText> cls = EditText.class;
				Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setShowSoftInputOnFocus.setAccessible(true);
				setShowSoftInputOnFocus.invoke(editText2, false);
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
		if(editText1 != null) {
			editText1.setOnTouchListener(this);
		}
		if(editText2 != null) {
			editText2.setOnTouchListener(this);
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
		bt00.setOnClickListener(this);
		dot.setOnClickListener(this);
		back.setOnClickListener(this);
		cancle.setOnClickListener(this);
		clean.setOnClickListener(this);
		confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == back.getId()) {
			if(flag == FLAG_EDIT && editText1.getSelectionStart() > 0) {
				editText1.getText().delete(editText1.getSelectionStart() - 1, editText1.getSelectionStart());
			}else if(flag == FLAG_EDIT2 && editText2.getSelectionStart() > 0) {
				editText2.getText().delete(editText2.getSelectionStart() - 1, editText2.getSelectionStart());
			}else if(flag == FLAG_TEXT && textView.getText() != null) {
				textView.getEditableText().delete(textView.getText().length() - 1, textView.getText().length());
			}
		}else if(v.getId() == cancle.getId()) {
			if(flag == FLAG_EDIT) {
				editText1.setText(defalutStr);
				if(defalutStr != null)
					editText1.setSelection(defalutStr.length());
			}else if(flag == FLAG_EDIT2) {
				editText2.setText(defalutStr);
				if(defalutStr != null)
					editText2.setSelection(defalutStr.length());
			}else if(flag == FLAG_TEXT) {
				textView.setText(defalutStr);
			}
			defalutStr = "";
			dismiss();
			if(listener != null) {
				listener.onCancel();
			}
		}else if(v.getId() == clean.getId()) {
			if(flag == FLAG_EDIT && editText1.getSelectionStart() > 0) {
				editText1.getText().clear();
			}else if(flag == FLAG_EDIT2 && editText2.getSelectionStart() > 0) {
				editText2.getText().clear();
			}else if(flag == FLAG_TEXT && textView.getText() != null) {
				textView.setText("");
			}
		}else if(v.getId() == confirm.getId()) {
			if(flag == FLAG_EDIT && (TextUtils.isEmpty(editText1.getText().toString()) || "".equals(editText1.getText().toString()))) {
				Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
				return;
			}else if(flag == FLAG_EDIT2 && (TextUtils.isEmpty(editText2.getText().toString()) || "".equals(editText2.getText().toString()))) {
				Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
				return;
			}else if(flag == FLAG_TEXT && (TextUtils.isEmpty(textView.getText().toString()) || "".equals(textView.getText().toString()))) {
				Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
				return;
			}
			if(listener != null) {
				listener.onComfirm();
				if(flag == FLAG_EDIT) {
					listener.onValue(editText1.getText().toString().trim());
				}else if(flag == FLAG_EDIT2) {
					listener.onValue(editText2.getText().toString().trim());
				}else if(flag == FLAG_TEXT) {
					listener.onValue(textView.getText().toString().trim());
				}
			}
			dismiss();
		}else {
			if(flag == FLAG_EDIT) {
				editText1.getText().insert(editText1.getSelectionStart(), (String) v.getTag());
			}else if(flag == FLAG_EDIT2) {
				editText2.getText().insert(editText2.getSelectionStart(), (String) v.getTag());
			} else if (flag == FLAG_TEXT) {
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
			if(editText1 != null && editText1.getId() == v.getId()) {
				flag = FLAG_EDIT;
				editText1.requestFocus();
				if(isShowing()) {
					return true;
				}
				if(editText1.getCompoundDrawables()[2] != null && event.getX() > (editText1.getWidth() - editText1.getTotalPaddingRight()+ 60) && (event.getX() < ((editText1.getWidth() - editText1.getPaddingRight()+ 80)))) {
					return false;
				}else {
					defalutStr = editText1.getText().toString();
					if(defalutStr != null)
						editText1.setSelection(defalutStr.length());
					show();
				}
			}else if(editText2 != null && editText2.getId() == v.getId()) {
				flag = FLAG_EDIT2;
				editText2.requestFocus();
				if(isShowing()) {
					return true;
				}
				defalutStr = editText2.getText().toString();
				if(defalutStr != null)
					editText2.setSelection(defalutStr.length());
				show();
			}else if(textView != null && textView.getId() == v.getId()) {
				if(isShowing()) {
					return true;
				}
				flag = FLAG_TEXT;
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
