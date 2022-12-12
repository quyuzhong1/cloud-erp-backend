package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.LinkedMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ttc {

    public static void main(String[] args) throws Exception {
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();

        String formId = "SAL_OUTSTOCK";


        String jsonData = "{\"CreateOrgId\":0,\"Number\":\"\",\"Id\":\"2739839\",\"IsSortBySeq\":\"false\"}";
//调用接口
        String resultJson = client.view(formId,jsonData);

        System.out.println(resultJson);

        String s = JSONObject.toJSONString(resultJson);


        Object parse = JSONObject.parse(s);


    }
}
