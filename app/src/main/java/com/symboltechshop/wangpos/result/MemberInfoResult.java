package com.symboltechshop.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.MemberInfo;

/** 
 * 会员信息
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年10月30日 下午5:39:04 
* @version 1.0 
*/
public class MemberInfoResult extends BaseResult{

	/**中间层data映射内部类*/
	@SerializedName("data")
	private DataMapClass getdata;
	
	
	public DataMapClass getDatamapclass() {
		return getdata;
	}


	public void setDatamapclass(DataMapClass datamapclass) {
		this.getdata = datamapclass;
	}


	public class DataMapClass{
		
		@SerializedName("member")
		private MemberInfo memberinfo;

		public MemberInfo getMemberinfo() {
			return memberinfo;
		}

		public void setMemberinfo(MemberInfo memberinfo) {
			this.memberinfo = memberinfo;
		}
	}
}
