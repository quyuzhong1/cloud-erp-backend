package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

public class ttc {

    public static void main(String[] args) throws Exception {
        //读取配置，初始化SDK
        K3CloudApi client = new K3CloudApi();

        String formId = "BD_MATERIAL";


        String jsonData = "{\"CreateOrgId\":0,\"Number\":\"0005\",\"Id\":\"\",\"IsSortBySeq\":\"false\"}";
//调用接口
        String resultJson = client.view(formId,jsonData);

        System.out.println(resultJson);

        String s = JSONObject.toJSONString(resultJson);


        Object parse = JSONObject.parseObject(s);


    }
}
