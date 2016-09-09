package com.symboltech.wangpos.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Gson 解析工具 simple introduction
 * <p/>
 * <p/>
 * detailed comment
 *
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月5日
 * @see
 * @since 1.0
 */
public class GsonUtil {
    private static Gson gson;

    public static synchronized Gson getGsonInstance(boolean createNew) {
        if (createNew) {
            return new GsonBuilder().serializeNulls().create();
        } else if (gson == null) {
            gson = new GsonBuilder().serializeNulls().create();
        }
        return gson;
    }

    /**
     * beanToJson
     *
     * @param obj
     * @return
     * @throws Exception
     * @author CWI-APST email:26873204@qq.com
     * @Description: TODO beanToJson
     */
    public static String beanToJson(Object obj) throws Exception {
        try {
            Gson gson = getGsonInstance(false);
            String json = gson.toJson(obj);
            return json;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * @param list
     * @return
     * @throws Exception
     */
    public static String beanToJson(List list) throws Exception {
        try {
            Gson gson = getGsonInstance(false);
            String json = gson.toJson(list);
            return json;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * jsonToBean
     *
     * @return
     * @throws Exception
     * @author CWI-APST email:26873204@qq.com
     * @Description: TODO jsonToBean
     */
    public static Object jsonToBean(String json, Class<?> cls) throws Exception {
        try {
            Gson gson = getGsonInstance(false);
            Object vo = gson.fromJson(json, cls);
            return vo;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}