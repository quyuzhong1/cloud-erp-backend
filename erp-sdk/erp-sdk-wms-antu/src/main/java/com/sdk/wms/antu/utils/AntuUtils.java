package com.sdk.wms.antu.utils;

import com.alibaba.fastjson.JSON;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.sdk.wms.antu.soap.Ec;
import com.sdk.wms.antu.soap.Ec_Service;
import jodd.util.StringUtil;

public class AntuUtils {
    private AntuUtils() {
        throw new IllegalStateException("Utility AntuUtils class");
    }
    public static String callService(String service, Object obj){
        Ec_Service ecService = new Ec_Service();
        Ec ec = ecService.getEcSOAP();
        String appToken = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appToken"));
        String appKey = String.valueOf(ThirdWarehouseContext.getAuthMap().get("appKey"));
        if(StringUtil.isBlank(appKey) || StringUtil.isBlank(appToken)){
            throw new ServiceException("获取不到授权值，正确授权值为：appToken,appKey");
        }
        String param = JSON.toJSONString(obj);
        String response =  ec.callService(param,appToken,appKey,service,"zh_CN");
        ThirdWarehouseContext.setRequestJson(param);
        ThirdWarehouseContext.setResponseJson(response);
        return response;
    }

}
