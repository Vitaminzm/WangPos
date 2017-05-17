package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 离线账单info
 * 
 * @author so
 * 
 */
public class OfflineBillInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private OfflineConfirmbillinfos confirmbillinfos; // 收款方式ID
	private OfflineSavearticleinfos savearticleinfos; // 收款方式名称
	public OfflineConfirmbillinfos getConfirmbillinfos() {
		return confirmbillinfos;
	}
	public void setConfirmbillinfos(OfflineConfirmbillinfos confirmbillinfos) {
		this.confirmbillinfos = confirmbillinfos;
	}
	public OfflineSavearticleinfos getSavearticleinfos() {
		return savearticleinfos;
	}
	public void setSavearticleinfos(OfflineSavearticleinfos savearticleinfos) {
		this.savearticleinfos = savearticleinfos;
	}
	
}
