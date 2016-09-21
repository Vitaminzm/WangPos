package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.LogInfo;

/**
 * Created by symbol on 2016/9/5.
 */
public class Logresult extends BaseResult {
    @SerializedName("data")
    private LogInfo logInfo;

    public LogInfo getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(LogInfo logInfo) {
        this.logInfo = logInfo;
    }
}
