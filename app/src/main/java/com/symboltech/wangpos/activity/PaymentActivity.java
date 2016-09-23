package com.symboltech.wangpos.activity;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.GoodsAdapter;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PaymentActivity extends BaseActivity {

    @Bind(R.id.title_text_content)TextView title_text_content;
    @Bind(R.id.radioGroup_function)RadioGroup radioGroup_function;

    @Bind(R.id.text_consume_score)TextView text_consume_score;
    @Bind(R.id.text_total_money)TextView text_total_money;
    @Bind(R.id.edit_input_money)EditText edit_input_money;

    @Bind(R.id.text_cashier_name)TextView text_cashier_name;
    @Bind(R.id.text_bill_id)TextView text_bill_id;
    @Bind(R.id.text_saleman_name)TextView text_saleman_name;
    @Bind(R.id.text_desk_code)TextView text_desk_code;

    @Bind(R.id.goods_listview)SwipeMenuListView goods_listview;

    static class MyHandler extends Handler {
        WeakReference<BaseActivity> mActivity;

        MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity theActivity = mActivity.get();
            switch (msg.what) {
                case ToastUtils.TOAST_WHAT:
                    ToastUtils.showtaostbyhandler(theActivity,msg);
                    break;
            }
        }
    }

    MyHandler handler = new MyHandler(this);

    GoodsAdapter adapter;
    ArrayList<String> list = new ArrayList<>();
    @Override
    protected void initData() {
        title_text_content.setText(getString(R.string.pay));

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
//                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(
//                        getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
//                        0xCE)));
//                // set item width
//                openItem.setWidth(20);
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(R.color.red);
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // set item width
                deleteItem.setWidth(150);
                // set a icon
               // deleteItem.setIcon(R.mipmap.btn_bank_icon);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        goods_listview.setMenuCreator(creator);
        goods_listview.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        goods_listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                list.remove(position);
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        list.add("11");
        list.add("22");
        list.add("33");
        list.add("44");
        list.add("55");
        list.add("66");
        adapter = new GoodsAdapter(getApplicationContext(), list);
        goods_listview.setAdapter(adapter);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_payment);
        MyApplication.addActivity(this);
        ButterKnife.bind(this);
        radioGroup_function.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio_add_score_good:
                        break;
                    case R.id.radio_add_salesman:
                        break;
                    case R.id.radio_look_member:
                        break;
                    case R.id.radio_select_good:
                        break;
                }
            }
        });
    }

    @Override
    protected void recycleMemery() {
        handler.removeCallbacksAndMessages(null);
        MyApplication.delActivity(this);
    }

    @OnClick({R.id.title_icon_back, R.id.text_confirm})
    public void back(View view){
        int id = view.getId();
        switch (id){
            case R.id.title_icon_back:
                this.finish();
                break;
            case R.id.text_confirm:
                break;
        }
    }

    /*
//Touch事件
class CompentOnTouch implements OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            //这是btnMius下的一个层，为了增强易点击性
            case R.id.linearBtnMius:
                onTouchChange("mius", event.getAction());
                break;
            //这里也写，是为了增强易点击性
            case R.id.btnMius:
                onTouchChange("mius", event.getAction());
                break;
            case R.id.linearBtnPlus:
                onTouchChange("plus", event.getAction());
                break;
            case R.id.btnPlus:
                onTouchChange("plus", event.getAction());
                break;
        }
        return true;
    }
}

    private void onTouchChange(String methodName, int eventAction) {
        //按下松开分别对应启动停止减线程方法
        if ("mius".equals(methodName)) {
            if (eventAction == MotionEvent.ACTION_DOWN) {
                miusThread = new MiusThread();
                isOnLongClick = true;
                miusThread.start();
            } else if (eventAction == MotionEvent.ACTION_UP) {
                if (miusThread != null) {
                    isOnLongClick = false;
                }
            } else if (eventAction == MotionEvent.ACTION_MOVE) {
                if (miusThread != null) {
                    isOnLongClick = true;
                }
            }
        }
        //按下松开分别对应启动停止加线程方法
        else if ("plus".equals(methodName)) {
            if (eventAction == MotionEvent.ACTION_DOWN) {
                plusThread = new PlusThread();
                isOnLongClick = true;
                plusThread.start();
            } else if (eventAction == MotionEvent.ACTION_UP) {
                if (plusThread != null) {
                    isOnLongClick = false;
                }
            } else if (eventAction == MotionEvent.ACTION_MOVE) {
                if (plusThread != null) {
                    isOnLongClick = true;
                }
            }
        }
    }

//减操作
class MiusThread extends Thread {
    @Override
    public void run() {
        while (isOnLongClick) {
            try {
                Thread.sleep(200);
                myHandler.sendEmptyMessage(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }
}

//加操作
class PlusThread extends Thread {
    @Override
    public void run() {
        while (isOnLongClick) {
            try {
                Thread.sleep(200);
                myHandler.sendEmptyMessage(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }
}

//更新文本框的值
Handler myHandler = new Handler() {
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (miusEnable) {
                    buttonHolder.input.setText((new BigDecimal(
                            buttonHolder.input.getText().toString())
                            .subtract(new BigDecimal("0.1")))
                            + "");
                }
                break;
            case 2:
                if (plusEnable) {
                    buttonHolder.input.setText((new BigDecimal(
                            buttonHolder.input.getText().toString())
                            .add(new BigDecimal("0.1")))
                            + "");
                }
                break;
        }
        setBtnEnable();
    };
};

    //超出最大最小值范围按钮的可能与不可用
    private void setBtnEnable() {
        if (new BigDecimal(buttonHolder.input.getText().toString())
                .compareTo(new BigDecimal(minvalue + "")) > 0) {
            miusEnable = true;
            buttonHolder.btnMius
                    .setBackgroundResource(R.drawable.nurse_symp_mius);
        } else {
            miusEnable = false;
            buttonHolder.btnMius
                    .setBackgroundResource(R.drawable.nurse_symp_mius_max);
        }
        if (new BigDecimal(buttonHolder.input.getText().toString())
                .compareTo(new BigDecimal(maxvalue + "")) < 0) {
            plusEnable = true;
            buttonHolder.btnPlus
                    .setBackgroundResource(R.drawable.nurse_symp_plus);
        } else {
            plusEnable = false;
            buttonHolder.btnPlus
                    .setBackgroundResource(R.drawable.nurse_symp_plus_max);
        }
    }


看完上面代码你一定会觉得，怎么没有单击事件，我单击也可以增加减小文本框的值嘛，哈哈，单击事件已经合并在Touch事件里啦，单击也是要按下弹起的不是么，至此最大的问题就解决了，这个还有个小问题，因为文本框里的值都是小数，如果你直接35.5-0.1，35.5-0.2，35.5-0.3  ... ...，你会发现会得到类似35.29999999904这些类似的情况，这里大概是因为计算机进行二进制分数运算时，造成了不够精度的bug，这里，我们可以使用BigDecimal，它是java专门用于解决大小数(大分数)的一个类，它支持任何精度的定点数，因此，我们的加减法可以写成：

        buttonHolder.input.setText((new BigDecimal(
        buttonHolder.input.getText().toString())
        .subtract(new BigDecimal("0.1")))
        + "");

        buttonHolder.input.setText((new BigDecimal(
        buttonHolder.input.getText().toString())
        .add(new BigDecimal("0.1")))
        + "")
        */
}
