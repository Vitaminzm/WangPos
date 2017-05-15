package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.ThirdPayInfo;

import java.util.List;

/**
 * Created by symbol on 2016/9/5.
 */
public class ThirdPayInfoResult extends BaseResult {
    @SerializedName("data")
    private List<ThirdPayInfo> thirdPayInfo;

    public List<ThirdPayInfo> getThirdPayInfo() {
        return thirdPayInfo;
    }

    public void setThirdPayInfo(List<ThirdPayInfo> thirdPayInfo) {
        this.thirdPayInfo = thirdPayInfo;
    }
}
