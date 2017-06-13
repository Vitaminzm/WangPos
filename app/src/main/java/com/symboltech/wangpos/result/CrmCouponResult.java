package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.CouponInfo;

import java.util.List;

/**
 * Created by symbol on 2016/9/5.
 */
public class CrmCouponResult extends BaseResult {
    @SerializedName("data")
    private List<CouponInfo> data;

    public List<CouponInfo> getData() {
        return data;
    }

    public void setData(List<CouponInfo> data) {
        this.data = data;
    }
}
