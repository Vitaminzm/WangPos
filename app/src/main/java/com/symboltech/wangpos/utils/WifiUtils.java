package com.symboltech.wangpos.utils;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by Administrator on 2016/8/28.
 */
public class WifiUtils {

    public WifiManager mWifiManager;

    private WifiInfo mWifiInfo;

    private List<ScanResult> mWifiList;

    private List<WifiConfiguration> mWificonfiguration;

    private WifiManager.WifiLock mWifiLock;


    public WifiUtils(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public boolean isWifiEnabled(){
        return mWifiManager.isWifiEnabled();
    }
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    public List<WifiConfiguration> getConfigurations () {
        return  mWificonfiguration;
    }

    public Boolean connectConfiguration(int index) {

//        if(index > mWificonfiguration.size()) {
//            return;
//        }
        mWifiManager.enableNetwork(index, true);
        mWifiManager.saveConfiguration();
        mWifiManager.reconnect();

        return true;
    }

    public void removeNetwork(int id){
        mWifiManager.removeNetwork(id);
        mWifiManager.saveConfiguration();
    }

    public void startScan() {
        mWifiManager.startScan();
        mWificonfiguration = mWifiManager.getConfiguredNetworks();
        mWifiList = mWifiManager.getScanResults();
    }

    public List<ScanResult> getmWifiList() {
        return mWifiList;
    }

    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + String.valueOf(i+1)+":");
            stringBuilder.append(mWifiList.get(i).toString());
            stringBuilder.append("/n");
        }
        return  stringBuilder;
    }

    public String getMacAddress() {
        return  (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getSSID() {
        return  (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }

    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();

    }

    public  String getWifiInfoS() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    public  WifiInfo getWifiInfo() {
        return (mWifiInfo == null) ? null : mWifiInfo;
    }

    public boolean  addNetWork(WifiConfiguration wifiConfiguration) {
        int wcgID = mWifiManager.addNetwork(wifiConfiguration);
        Log.e("wcgID", wcgID+"true");
        mWifiManager.enableNetwork(wcgID, true);
        mWifiManager.saveConfiguration();
        mWifiManager.reconnect();
        return  true;

    }

    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.isExsits(SSID);
        if(tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        switch (Type) {
            case 1:
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                configuration.status = WifiConfiguration.Status.ENABLED;
                break;
            case 2:
                configuration.hiddenSSID = false;
                configuration.wepKeys[0] = "\"" + Password +"\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            case 3:

                configuration.preSharedKey = "\"" + Password + "\"";
                configuration.hiddenSSID = false;
                // configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return  configuration;
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig:
                existingConfigs) {
            if (existingConfig.SSID.equals("\"" +SSID+"\"")) {
                return  existingConfig;
            }

        }
        return null;
    }




}