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
}
