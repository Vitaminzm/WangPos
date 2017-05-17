package com.symboltechshop.wangpos.view;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.interfaces.KeyBoardListener;

import java.lang.reflect.Method;


public class HorizontalKeyBoard extends Dialog implements OnClickListener, OnTouchListener, OnDismissListener {

	private ViewGroup ll_keyboard;
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
	boolean isFocusOutside = false;

	/**
	 * 
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText 编辑框
	 * @param listener 确定按钮监听, 不监听传null
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText, ViewGroup ll_keyboard, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText1 = editText;
		this.listener = listener;
		this.ll_keyboard = ll_keyboard;
		flag = FLAG_EDIT;
		initView();
	}

	/**
	 *
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param listener 确定按钮监听, 不监听传null
	 */
	private boolean isDemand = false;
	public HorizontalKeyBoard(Context context, Object mWindow, boolean isdemand, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.listener = listener;
		this.isDemand = isdemand;
		flag = FLAG_EDIT;
		initView();
		setCanceledOnTouchOutside(false);
	}
	/**
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param editText 编辑框
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText, ViewGroup ll_keyboard ) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.editText1 = editText;
		this.ll_keyboard = ll_keyboard;
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
	public HorizontalKeyBoard(Context context, Object mWindow, EditText editText1,EditText editText2, ViewGroup ll_keyboard,KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.listener = listener;
		this.editText1 = editText1;
		this.editText2 = editText2;
		this.ll_keyboard = ll_keyboard;
		flag = FLAG_EDIT;
		isFocusOutside = true;
		initView();
	}

	/**
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param textView 文本框
	 * @param listener 确定按钮监听, 不监听传null
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, TextView textView, ViewGroup ll_keyboard, KeyBoardListener listener) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.textView = textView;
		this.listener = listener;
		this.ll_keyboard = ll_keyboard;
		flag = FLAG_TEXT;
		initView();
	}
	
	/**
	 * 
	 * @param context 上下文
	 * @param mWindow 当前activity或者dialog（不能放其他）
	 * @param textView 文本框
	 */
	public HorizontalKeyBoard(Context context, Object mWindow, TextView textView, ViewGroup ll_keyboard) {
		super(context, R.style.keyboard_dialog);
		requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
		this.context = context;
		this.mObject = mWindow;
		this.textView = textView;
		this.ll_keyboard = ll_keyboard;
		flag = FLAG_TEXT;
		initView();
	}
	
	private void initView() {
		setCanceledOnTouchOutside(true);
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

	public EditText getEdittext(){
		return editText1;
	}

	public void setEdittext(EditText edit){
		editText1 = edit;
		if(editText1 != null) {
			Class<EditText> cls = EditText.class;
			try {
				Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setShowSoftInputOnFocus.setAccessible(true);
				setShowSoftInputOnFocus.invoke(editText1, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			flag = FLAG_EDIT;
			editText1.requestFocus();
			defalutStr = editText1.getText().toString();
			if(defalutStr != null)
				editText1.setSelection(defalutStr.length());
			editText1.cancelLongPress();
		}
	}

	@Override
	public void show() {
		super.show();
		getWindow().setWindowAnimations(R.style.AnimationFade); //设置窗口弹出动画
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
			  float height = context.getResources().getDimension(R.dimen.keyboard_height);
			  Rect outRect = new Rect();
			  mContentView.getWindowVisibleDisplayFrame(outRect);

			  length = 0;
			  if(mObject instanceof Dialog) {
				  length=(int)((pos[1]+mWindow.getDecorView().getHeight()-outRect.top)-(mScreenHeight-height));
			  }else if(ll_keyboard == null){
				  length=(int)((pos[1]+mWindow.getDecorView().getHeight()-outRect.top)-(mScreenHeight-height));
			  }else{
				  if(flag == FLAG_EDIT) {
					  length=(int)((pos[1]+editText1.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }else if(flag == FLAG_EDIT2) {
					  length=(int)((pos[1]+editText2.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }else if(flag == FLAG_TEXT) {
					  length=(int)((pos[1]+textView.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
				  }
			  }
			if(isDemand){
				length=(int)((pos[1]+editText1.getMeasuredHeight()-outRect.top)-(mScreenHeight-height));
			}
			  if(length>0){
				 if(!(mObject instanceof Dialog)){
					 if(ll_keyboard != null){
						 ValueAnimator valueAnimator = ValueAnimator.ofInt(0,length);
						 valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
							 private IntEvaluator intEvaluator = new IntEvaluator();

							 @Override
							 public void onAnimationUpdate(ValueAnimator animation) {
								 int currentValue = (Integer) animation.getAnimatedValue();
								 float fraction = animation.getAnimatedFraction();
								 ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) ll_keyboard.getLayoutParams();
								 lp1.topMargin = -intEvaluator.evaluate(fraction,0,length);
								 ll_keyboard.requestLayout();
							 }
						 });
						 valueAnimator.setDuration(300).start();
					 }if(!isDemand){
						 mManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
						 ValueAnimator valueAnimator = ValueAnimator.ofInt(0,length-100);
						 valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
							 private IntEvaluator intEvaluator = new IntEvaluator();

							 @Override
							 public void onAnimationUpdate(ValueAnimator animation) {
								 int currentValue = (Integer) animation.getAnimatedValue();
								 float fraction = animation.getAnimatedFraction();
								 mManager.updateViewLayout(mWindow.getDecorView(), getParams(-intEvaluator.evaluate(fraction, 0, length-100)));
							 }
						 });
						 valueAnimator.setDuration(300).start();
					 }else{
						 mContentView.scrollBy(0, length);
					 }
				 }else{
					 mManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
					 ValueAnimator valueAnimator = ValueAnimator.ofInt(0,length);
					 valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						 private IntEvaluator intEvaluator = new IntEvaluator();

						 @Override
						 public void onAnimationUpdate(ValueAnimator animation) {
							 int currentValue = (Integer) animation.getAnimatedValue();
							 float fraction = animation.getAnimatedFraction();
							 mManager.updateViewLayout(mWindow.getDecorView(), getParams(-intEvaluator.evaluate(fraction, 0, length)));
						 }
					 });
					 valueAnimator.setDuration(300).start();
				 }
			  }
//			if(length>0){
//				LogUtil.i("lgs",length+"---------");
//				mManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//				mManager.updateViewLayout(mContentView, getParams(-length));
//			}
		  }
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
					if(ll_keyboard != null){
						ValueAnimator valueAnimator = ValueAnimator.ofInt(length,0);
						valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
							private IntEvaluator intEvaluator = new IntEvaluator();

							@Override
							public void onAnimationUpdate(ValueAnimator animation) {
								int currentValue = (Integer) animation.getAnimatedValue();
								float fraction = animation.getAnimatedFraction();
								ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) ll_keyboard.getLayoutParams();
								lp1.topMargin = -intEvaluator.evaluate(fraction,length,0);
								ll_keyboard.requestLayout();
							}
						});
						valueAnimator.setDuration(300).start();
					}if(!isDemand){
						length = 0;
						mManager.updateViewLayout(mWindow.getDecorView(), getParams(length));
					}else{
						mContentView.scrollBy(0, -length);
						length = 0;
					}
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
			isFocusOutside = true;
		}else {
			mWindow = ((Activity) mObject).getWindow();
		}
		if(ll_keyboard == null){
			isFocusOutside = true;
		}
		if(isDemand){
			isFocusOutside = false;
		}
		LayoutParams attributes = mWindow.getAttributes();
		attributes.format = 1;
		mWindow.setAttributes(attributes);
		mContentView = mWindow.findViewById(Window.ID_ANDROID_CONTENT);
		Window window = this.getWindow();
		window.setGravity(Gravity.LEFT|Gravity.BOTTOM);
		LayoutParams params = window.getAttributes();
		if(isFocusOutside)
			params.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
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
			}else if(flag == FLAG_TEXT && textView.getText() != null && textView.getText().length() >0) {
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
					editText1.setSelection(editText1.getText().toString().trim().length());
				}else if(flag == FLAG_EDIT2) {
					listener.onValue(editText2.getText().toString().trim());
					editText2.setSelection(editText2.getText().toString().trim().length());
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
				defalutStr = editText1.getText().toString();
				if(isShowing()) {
					return true;
				}
				if(editText1.getCompoundDrawables()[2] != null && event.getX() > (editText1.getWidth() - editText1.getTotalPaddingRight()+ 60) && (event.getX() < ((editText1.getWidth() - editText1.getPaddingRight()+ 80)))) {
					return false;
				}else {
					if(defalutStr != null){
						editText1.setSelection(defalutStr.length());
					}
					editText1.cancelLongPress();
					show();
				}
			}else if(editText2 != null && editText2.getId() == v.getId()) {
				flag = FLAG_EDIT2;
				editText2.requestFocus();
				defalutStr = editText2.getText().toString();
				if(isShowing()) {
					return true;
				}
				if(defalutStr != null)
					editText2.setSelection(defalutStr.length());
				editText2.cancelLongPress();
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
