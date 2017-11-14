package com.symboltech.wangpos.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.symboltech.wangpos.log.LogUtil;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by symbol on 2016/9/14.
 */
public class Utils {
    private static SimpleDateFormat timeFormat;

    static {
        timeFormat = new SimpleDateFormat("HH:mm");
    }

    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static  boolean isRoot(){
        Process process = null;
        try
        {
            process  = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            int i = process.waitFor();
            if(0 == i){
                process = Runtime.getRuntime().exec("su");
                return true;
            }

        } catch (Exception e)
        {
            return false;
        }
        return false;

    }

    public static final Map filterTransResult(String var2) {
        HashMap var1 = new HashMap();
        if(var2 != null && !var2.isEmpty()) {
            System.out.println(var2);
            try {
                JSONObject var3 = new JSONObject(var2);
                if(!var3.isNull("appName")) {
                    var1.put("appName", var3.getString("appName"));
                }

                if(!var3.isNull("transId")) {
                    var1.put("transId", var3.getString("transId"));
                }

                if(!var3.isNull("resultCode")) {
                    var1.put("resultCode", var3.getString("resultCode"));
                }

                if(!var3.isNull("resultMsg")) {
                    var1.put("resultMsg", var3.getString("resultMsg"));
                }

                if(!var3.isNull("transData")) {
                    var1.put("transData", var3.getString("transData"));
                }
            } catch (JSONException var4) {
                var4.printStackTrace();
            }

            return var1;
        } else {
            return var1;
        }
    }
    /**
     * 获取随机的UUID
     *
     * @return
     */
    public String getMyUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * get phone uid ,if IMEI is exit get IMEI other get mac.
     *
     * @author CWI-APST
     * @param context
     * @return gid
     */
    public static String getUid(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String mID = android.os.Build.SERIAL;
        if (mID != null && !"".equals(mID)) {
            return mID;
        } else {
            mID = info.getMacAddress();
            if (mID != null && !"".equals(mID)) {
                return mID.replace(":", "");
            } else {
                return "";
            }
        }
    }

    /**
     *
     * @param asc
     *            码 表 转16
     * @return
     */
    public static byte[] Str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }

        return bbt;
    }

    /**
     * 银行卡输入账户隐藏处理
     *
     * @param cardcodetemp
     *            bankcode
     * @return
     */
    public static String getbankcardcode(String cardcodetemp) {
        LogUtil.i("lgg", "cardcode===" + cardcodetemp.trim() + "=====");
        try {
            if (cardcodetemp != null) {
                String cardcode = cardcodetemp.trim();
                if (cardcode.length() > 10) {
                    return cardcode.substring(0, 5) + "  ******  "
                            + cardcode.substring(cardcode.length() - 4, cardcode.length());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(getMetaValue获取配置信息)
     * @param context
     * @param metaKey
     * @return
     */
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String metavalue = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                metavalue = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return metavalue;
    }

    /**
     * 获取本机手机号
     *
     * @param context
     * @return
     */
    public static String getNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneId = tm.getLine1Number();
        if (phoneId == null) {
            return "";
        }
        return phoneId;
    }

    /**
     * MD5 32位加密算法
     *
     * @param str
     * @return
     */
    public static String MD5(String str) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        char[] charArray = str.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 可逆的MD5加密算法
     *
     * @param str
     * @return
     */
    public String encryptMD5(String str) {
        char[] a = str.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 'l');
        }
        String s = new String(a);
        return s;
    }

    /**
     * 删除指定文件夹下的所有文件
     *
     * @param path
     *            完整文件夹的绝对路径
     * @return
     */
    public boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                // delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            // 该异常是发生的
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        int versionCode = -1;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取机器IMEI号
     *
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }

    /**
     * 保留N位小数
     *
     * @param src
     *            需要格式化的小数
     * @param n
     *            保留的位数
     * @return 格式化后的数字字符串
     */
    public static String keepNDouble(double src, int n) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(n);
        return nf.format(src);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 格式化时间
     *
     * @return
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return timeFormat.format(date);
    }

    /**
     * 根据手机的分辨率从dp -> px的转化
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从px -> dp的转化
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 保留18字符 + 省略号
     *
     * @param printText
     * @return
     */
    public static String CutPrintutils(String printText, int size) {
        if (printText.length() > size) {
            return printText.substring(0, size) + "...";
        } else {
            return printText;
        }
    }

    /**
     * 15字符长度 +""
     *
     * @author CWI-APST
     * @param barcodeText
     * @return
     */
    public static String Cutbarcode(String barcodeText) {
        if (barcodeText.length() > 15) {
            return barcodeText.substring(0, 15);
        } else {
            for (int i = 0; i < (15 - barcodeText.length()); i++) {
                barcodeText += " ";
            }
            return barcodeText;
        }
    }

    /**
     * 保留15字符 活动
     *
     * @param printText
     * @return
     */
    public static String CutPrintutilsactivites(String printText, int size) {
        try {
            if (printText.length() > size) {
                return printText.substring(0, size);
            } else {
                return printText;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断该网页是否含有标签<div></div>
     *
     * @param str
     * @return
     */
    public static boolean isHtml5Video(String str) {
        if (str == null) {
            return false;
        }
        if (str.trim().startsWith("<div")) {
            return true;
        }
        return false;
    }

    /**
     * 判断该网页中是否含有多媒体播放标签<embed></embed>
     *
     * @param str
     * @return
     */
    public static boolean isFlashVideo(String str) {
        if (str == null) {
            return false;
        }
        if (str.trim().startsWith("<embed")) {
            return true;
        }
        return false;
    }

    /**
     * 判断一个字符串是否是个网址
     *
     * @param str
     * @return
     */
    public static boolean isUrl(String str) {
        if (str == null) {
            return false;
        }
        if (str.trim().startsWith("http://")) {
            return true;
        }
        return false;
    }

    /**
     * 获取IP地址
     *
     * @param context
     * @return
     */
    public static String getIpAddress(Context context) {
        URL infoUrl = null;
        InputStream inStream = null;
        try {
            infoUrl = new URL("http://iframe.ip138.com/ic.asp");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                // 从反馈的结果中提取出IP地址
                int start = strber.indexOf("[");
                int end = strber.indexOf("]", start + 1);
                line = strber.substring(start + 1, end);
                return line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 获取文件大小
     *
     * @param path
     * @return
     */
    public static int getFileSize(String path) {

        File file = new File(path);

        if (file.exists() && file.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);

                return fis.available() / 1024;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    /**
     * 获取文件后缀名
     *
     * @param file
     * @return
     */
    public static String getExtName(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    /**
     * 判断手机号是否符合手机号标准 只判断位数
     *
     * @param mobiles  手机号
     * @return
     */
    public static boolean isMobileNO(String mobiles){
        if(StringUtil.isEmpty(mobiles)){
            return false;
        }
        if(mobiles.length() != 11){
            return false;
        }else{
            return isNumeric(mobiles);
        }
//		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0-1,8]))\\d{8}$");
//		Matcher m = p.matcher(mobiles);
//		return m.matches();
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }
    /**
     * 获取IP地址
     *
     * @return
     */
    public static String getLocalIpAddress() {

        try {
            String ipv4;
            List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni:nilist)
            {
                List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist)
                {
                    if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress()))
                    {
                        return ipv4;
                    }
                }
            }
        } catch (SocketException ex) {
            LogUtil.i("lgs", "----ip:"+ex.toString());
        }
        return "";

    }
}
