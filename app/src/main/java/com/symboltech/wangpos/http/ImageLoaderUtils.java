package com.symboltech.wangpos.http;

/**
 * Created by symbol on 2016/12/14.
 */
public class ImageLoaderUtils {
    private static ImageLoaderUtils utils;

    private ImageLoaderUtils() {
    }

    public static ImageLoaderUtils getInstance() {
        if(utils == null) {
            utils = new ImageLoaderUtils();
        }
        return utils;
    }
}
