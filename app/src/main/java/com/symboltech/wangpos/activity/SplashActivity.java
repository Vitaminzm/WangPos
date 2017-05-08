package com.symboltech.wangpos.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.utils.SpSaveUtils;

public class SplashActivity extends BaseActivity {

    private Handler mHandler = new Handler();
    private View view;

    @Override
    protected void initData() {

        initAnimation();
    }

    @Override
    protected void initView() {
        view = View.inflate(this, R.layout.activity_splash, null);
        SpSaveUtils.writeboolean(getApplicationContext(), ConstantData.IS_NETCONNECT, true);
        SpSaveUtils.writeboolean(getApplicationContext(), ConstantData.IS_OFFLINE, false);
        SpSaveUtils.writeInt(getApplicationContext(), ConstantData.UP_STATUS, ConstantData.UPLOAD_SUCCESS);
        setContentView(view);
    }

    @Override
    protected void recycleMemery() {
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 动画效果
     */
    private void initAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        view.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goHome();
                    }
                }, 1000);
            }
        });
    }

    /**
     * go login
     */
    private synchronized void goHome() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ConstantData.LOGIN_FIRST, true);
        intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
        startActivity(intent);
        this.finish();
    }

}
