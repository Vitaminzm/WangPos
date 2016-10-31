package com.symboltech.koolcloud.interfaces;

import android.content.ComponentName;
import android.os.IBinder;

public interface RemoteServiceStateChangeListerner {

	void onServiceConnected(ComponentName name, IBinder service);
	void onServiceDisconnected(ComponentName name);
	
}
