package com.symboltechshop.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.OfflineDataInfo;

/** 脱机上传返回结果
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm
 * @see
 * @since 1.0
 */
public class OfflineDataResult extends BaseResult {

	@SerializedName("data")
	private OfflineDataInfo offlineDatainfo;

	public OfflineDataInfo getOfflineDatainfo() {
		return offlineDatainfo;
	}

	public void setOfflineDatainfo(OfflineDataInfo offlineDatainfo) {
		this.offlineDatainfo = offlineDatainfo;
	}
	
}
