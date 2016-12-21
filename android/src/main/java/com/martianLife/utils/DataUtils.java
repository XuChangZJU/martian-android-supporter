package com.martianLife.utils;

import com.facebook.react.bridge.ReadableArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/12/19.
 */
public class DataUtils {

    public static final byte[] decodeBleReadableArray(ReadableArray array) {
        byte[] value = new byte[array.size()];
        for(int i = 0; i < array.size(); i ++) {
            value[i] = (byte) array.getInt(i);
        }

        return value;
    }
}
