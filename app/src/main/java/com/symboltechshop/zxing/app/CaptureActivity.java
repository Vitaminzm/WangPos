package com.symboltechshop.zxing.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.app.ConstantData;
import com.symboltechshop.zxing.camera.CameraManager;
import com.symboltechshop.zxing.decoding.CaptureActivityHandler;
import com.symboltechshop.zxing.decoding.InactivityTimer;
import com.symboltechshop.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

public class CaptureActivity extends Activity implements Callback, OnClickListener {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private ImageView iv_qr_back;

	private StringBuffer mStringBufferResult;
	private boolean mCaps = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_qrcode);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		iv_qr_back = (ImageView) findViewById(R.id.title_icon_back);
		iv_qr_back.setOnClickListener(this);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(final Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("QRcode", obj.getText());
		intent.putExtras(bundle);
		setResult(ConstantData.QRCODE_RESULT_MEMBER_VERIFY, intent);
		finish();
	}
	
	

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_icon_back:
			CaptureActivity.this.finish();
			break;

		default:
			break;
		}
	}

	//获取扫描内容
    private char getInputCode(KeyEvent event) {

        int keyCode = event.getKeyCode();

        char aChar;

        if(keyCode == KeyEvent.KEYCODE_SHIFT_LEFT){
        	mCaps = true;
        	return 0;
        }
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
        	 switch (keyCode) {
	             case KeyEvent.KEYCODE_0:
	                 aChar = mCaps ? ')' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_1:
	                 aChar = mCaps ? '!' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_2:
	                 aChar = mCaps ? '@' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_3:
	                 aChar = mCaps ? '#' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_4:
	                 aChar = mCaps ? '$' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_5:
	                 aChar = mCaps ? '%' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_6:
	                 aChar = mCaps ? '^' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_7:
	                 aChar = mCaps ? '&' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_8:
	                 aChar = mCaps ? '*' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             case KeyEvent.KEYCODE_9:
	                 aChar = mCaps ? '(' : (char)('0' + keyCode - KeyEvent.KEYCODE_0);
	                 break;
	             default:
	                 aChar = 0;
	                 break;
	         }
        } else {
            //其他符号
            switch (keyCode) {
                case KeyEvent.KEYCODE_MINUS:
                    aChar = mCaps ? '_' : '-';
                    break;
                case KeyEvent.KEYCODE_SLASH:
                    aChar = mCaps ? '?' : '/';
                    break;
                case KeyEvent.KEYCODE_BACKSLASH:
                    aChar = mCaps ? '|' : '\\';
                    break;
                case KeyEvent.KEYCODE_EQUALS:
                    aChar = mCaps ? '+' : '=';
                    break;
                case KeyEvent.KEYCODE_LEFT_BRACKET:
                    aChar = mCaps ? '{' : '[';
                    break;
                case KeyEvent.KEYCODE_RIGHT_BRACKET:
                    aChar = mCaps ? '}' : ']';
                    break;
                case KeyEvent.KEYCODE_SEMICOLON:
                	aChar = mCaps ? ':' : ';';
                	break;
                case KeyEvent.KEYCODE_APOSTROPHE:
                	aChar = mCaps ? '"' : '\'';
                	break;
                case KeyEvent.KEYCODE_COMMA:
                	aChar = mCaps ? '<' : ',';
                	break;
                case KeyEvent.KEYCODE_PERIOD:
                	aChar = mCaps ? '>' : '.';
                	break;
                default:
                    aChar = 0;
                    break;
            }
        }
        mCaps = false;
        if(aChar != 0){
			mStringBufferResult.append(aChar);
		}
        return aChar;

    }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_HOME:
				return true;
			case KeyEvent.KEYCODE_MENU:
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
				playBeepSoundAndVibrate();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("QRcode", mStringBufferResult.toString());
				intent.putExtras(bundle);
				setResult(ConstantData.QRCODE_RESULT_MEMBER_VERIFY, intent);
				finish();
				return true;
			}
			getInputCode(event);
			
		}
		return super.dispatchKeyEvent(event);
	}
	
}