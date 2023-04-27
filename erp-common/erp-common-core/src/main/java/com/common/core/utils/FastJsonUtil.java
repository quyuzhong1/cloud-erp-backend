package com.common.core.utils;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;

import java.util.Iterator;

public class FastJsonUtil {
    /**
     * 递归读取所有的key
     *
     * @param jsonObject
     */
    public static StringBuffer getAllKey(JSONObject jsonObject) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator<String> keys = jsonObject.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            stringBuffer.append(key).append(",");
            if (jsonObject.get(key) instanceof JSONObject) {
                JSONObject innerObject = (JSONObject) jsonObject.get(key);
                stringBuffer.append(getAllKey(innerObject));
            } else if (jsonObject.get(key) instanceof JSONArray) {
                JSONArray innerObject = JSONUtil.parseArray(JSON.toJSONString(jsonObject.get(key)));
                stringBuffer.append(getAllKey(innerObject));
            }
        }
        return stringBuffer;
    }

    public static StringBuffer getAllKey(JSONArray jsonArray) {
        StringBuffer stringBuffer = new StringBuffer();
        if (jsonArray != null ) {
            Iterator i1 = jsonArray.iterator();
            while (i1.hasNext()) {
                Object key = i1.next();
                if (key instanceof JSONObject) {
                    JSONObject innerObject = (JSONObject) key;
                    stringBuffer.append(getAllKey(innerObject));
                } else if (key instanceof JSONArray) {
                    JSONArray innerObject = JSONUtil.parseArray(JSON.toJSONString(key));
                    stringBuffer.append(getAllKey(innerObject));
                } else {
                }
            }
        }
        return stringBuffer;
    }

}
