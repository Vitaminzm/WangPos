package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.LogInfo;

/**
 * Created by symbol on 2016/9/5.
 */
public class LogResult extends BaseResult {
    @SerializedName("data")
    private LogInfo logInfo;

    public LogInfo getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(LogInfo logInfo) {
        this.logInfo = logInfo;
    }
}
