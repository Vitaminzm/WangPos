package com.symboltech.wangpos.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    **
    * @param json
    * @param clazz
    * @return
     */
    public static <T> List<T> jsonToArrayList(String json, Class<T> clazz)
    {
        Type type = new TypeToken<ArrayList<JsonObject>>()
        {}.getType();
        List<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        List<T> arrayList = new ArrayList<T>();
        for (JsonObject jsonObject : jsonObjects)
        {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }

    /**
     * jsonToBean
     *
     * @return
     * @throws Exception
     * @author CWI-APST email:26873204@qq.com
     * @Description: TODO jsonToBean
     */
    public static <T> T jsonToBean(String json, Class<T> cls) throws Exception {
        try {
            Gson gson = getGsonInstance(false);
            T vo = gson.fromJson(json, cls);
            return vo;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static <T> T jsonToObect(String json, Type cls) throws Exception {
        try {
            Gson gson = getGsonInstance(false);
            T vo = gson.fromJson(json, cls);
            return vo;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static Map<String,Object>jsonToObect (String json) throws Exception{
        try {
            Gson gson = getGsonInstance(false);
            Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
            return map;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}