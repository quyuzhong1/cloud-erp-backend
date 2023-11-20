package com.sdk.wms.iml.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.iml.soap.Ec;
import com.sdk.wms.iml.soap.Ec_Service;

public class ImlUtils {

    public static String callService(String service, Object obj){
        Ec_Service ecService = new Ec_Service();
        Ec ec = ecService.getEcSOAP();
        String appToken = ThirdWarehouseContext.getAuthMap().get("appToken");
        String appKey = ThirdWarehouseContext.getAuthMap().get("appKey");
        String param = JSON.toJSONString(obj);
        String response =  ec.callService(param,appToken,appKey,service);
        ThirdWarehouseContext.setRequestJson(param);
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

}
