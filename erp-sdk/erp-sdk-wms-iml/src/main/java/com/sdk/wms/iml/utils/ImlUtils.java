package com.sdk.wms.iml.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdk.wms.iml.soap.Ec;
import com.sdk.wms.iml.soap.Ec_Service;

public class ImlUtils {

    public static String callService(String service, Object obj, String appToken, String appKey){
        Ec_Service ecService = new Ec_Service();
        Ec ec = ecService.getEcSOAP();
        System.out.println(JSON.toJSONString(obj));
        return ec.callService(JSON.toJSONString(obj),"44ac3ae1211d416a080858e57833cc14","fa0c90d7dbb434fa2160209756db677c",service);
    }

    public static void main(String[] args) {
        System.out.println(callService("getShippingMethod",null,null,null));
    }
}
