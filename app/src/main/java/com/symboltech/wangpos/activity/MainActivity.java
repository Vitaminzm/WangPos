package com.symboltech.wangpos.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpStringClient;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.result.InitializeInfResult;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {
    @Bind(R.id.post_id)Button  post;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.post_id,R.id.get_id})
    public void post(View view){
            startwaitdialog();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
               closewaitdialog();
            }
        },8000);
//        String ret="{\"retcode\":\"00\",\"retmsg\":\"成功\",\"data\":{\"brandgoodslist\":[{\"brandcode\":\"0201273\",\"brandname\":null,\"id\":\"1\",\"code\":\"100103\",\"barcode\":\"100103\",\"sptype\":\"0\",\"spmode\":\"3\",\"goodsname\":\"麦当劳汉堡\",\"pic\":\"http://192.168.7.246:82\",\"price\":\"12.50\",\"unit\":null},{\"brandcode\":\"0201273\",\"brandname\":null,\"id\":\"2\",\"code\":\"10004002\",\"barcode\":\"10004002\",\"sptype\":\"0\",\"spmode\":\"3\",\"goodsname\":\"麦当劳可乐\",\"pic\":\"http://192.168.7.246:82\",\"price\":\"9\",\"unit\":null}],\"paymentslist\":[{\"id\":\"1\",\"name\":\"现金\",\"type\":\"1\",\"couponid\":\"\",\"changetype\":\"0\",\"rate\":\"1\",\"visibled\":\"1\"},{\"id\":\"2\",\"name\":\"微信\",\"type\":\"5\",\"couponid\":\"\",\"changetype\":\"0\",\"rate\":\"1\",\"visibled\":\"0\"},{\"id\":\"3\",\"name\":\"支付宝\",\"type\":\"4\",\"couponid\":\"\",\"changetype\":\"0\",\"rate\":\"1\",\"visibled\":\"0\"},{\"id\":\"5\",\"name\":\"银联卡\",\"type\":\"3\",\"couponid\":\"\",\"changetype\":\"0\",\"rate\":\"1\",\"visibled\":\"0\"},{\"id\":\"6\",\"name\":\"优惠券\",\"type\":\"6\",\"couponid\":\"\",\"changetype\":\"2\",\"rate\":\"1\",\"visibled\":\"0\"},{\"id\":\"7\",\"name\":\"手工补录\",\"type\":\"101\",\"couponid\":\"\",\"changetype\":\"2\",\"rate\":\"1\",\"visibled\":\"0\"},{\"id\":\"8\",\"name\":\"商务卡补录\",\"type\":\"102\",\"couponid\":\"\",\"changetype\":\"2\",\"rate\":\"1\",\"visibled\":\"0\"},{\"id\":\"9\",\"name\":\"微信补录\",\"type\":\"103\",\"couponid\":\"\",\"changetype\":\"2\",\"rate\":\"1\",\"visibled\":\"0\"}],\"promlist\":[],\"refundreasonlist\":[{\"id\":1,\"name\":\"顾客要求退货\",\"showinx\":1,\"status\":0,\"modifierid\":1,\"modifiername\":\"系统管理员\",\"modifydate\":\"2016-05-17T11:43:35\"}],\"salemanlist\":[{\"person_id\":\"2\",\"person_name\":\"冯兆奎\",\"personcode\":\"10004001\"}]}}";
//        Gson gson = new GsonBuilder().serializeNulls().create();
//        InitializeInfResult result = null;
//        result = gson.fromJson(ret, InitializeInfResult.class);
//        if(result != null){
//            LogUtil.i("lgs", result.getCode()+"---"+result.getInitializeInfo().getBrandgoodslist().size());
//        }
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("topid", "5");
//        HttpStringClient.getinstance().getForObject(Logresult.class.getName(),"http://route.showapi.com/213-4", map, Logresult.class , new HttpActionHandle<Logresult>(){
//
//            @Override
//            public void handleActionStart() {
//
//            }
//
//            @Override
//            public void handleActionFinish() {
//
//            }
//
//            @Override
//            public void handleActionError(String actionName, String errmsg) {
//                LogUtil.i("lgs", errmsg);
//            }
//
//            @Override
//            public void handleActionSuccess(String actionName, Logresult result) {
//                if(result == null){
//                    LogUtil.i("lgs", "null----");
//                }else{
//                    LogUtil.i("lgs", result.toString());
//                }
//            }
//        });
    }


    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void recycleMemery() {
    }

}
