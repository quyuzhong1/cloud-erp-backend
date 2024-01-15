package com.erp.tms.batong.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.tms.batong.constants.BaTongConstants;
import com.erp.tms.batong.model.BaseResult;
import com.erp.tms.batong.model.order.request.OrderRequest;
import com.erp.tms.batong.model.order.response.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lambda
 * @Classname BaTongService
 * @Description 巴通服务类
 * @Date 2024-01-12 18:09
 * @Created by yl
 */
@Slf4j
@Component
public class BaTongService {


    /**
     * 创建订单
     *
     * @param
     * @return
     */
    public OrderResponse createOrder(Map<String, String> authMap, OrderRequest orderRequest) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.POST_CREATE_ORDER_URL;
        log.info("创建巴通订单url：{}", baseUrl + serviceMethod);
        String paramsJson = JSONUtil.toJsonStr(orderRequest);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        paramsMap.put("paramsJson", paramsJson);
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("创建巴通订单返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        Integer success = result.getSuccess();
        //表示成功
        if (BaTongConstants.SUCCESS.equals(success)) {
            return JSONUtil.toBean(JSONUtil.toJsonStr(result.getData()), OrderResponse.class);
        } else {
            throw new ServiceException(result.getCnmessage());
        }
    }


    /**
     * 删除订单
     *
     * @param referenceNo 客户参考号
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-15 9:40
     */
    public Boolean deleteOrder(Map<String, String> authMap, String referenceNo) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.DELETE_ORDER_URL;
        log.info("删除巴通订单url：{}", baseUrl + serviceMethod);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        Map<String, String> map = new HashMap<>();
        map.put("reference_no", referenceNo);
        paramsMap.put("paramsJson", JSONUtil.toJsonStr(map));
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("刪除巴通订单返回结果：{}", resBody);
        JSONObject jsonObject= JSONUtil.parseObj(resBody);
        Integer success = jsonObject.getInt("success",0);
        if(BaTongConstants.SUCCESS.equals(success)){
           return Boolean.TRUE;
        }else{
            throw new ServiceException(jsonObject.getStr("cnmessage","取消订单失败"));
        }
    }


    /**
     * 获取到基础的map
     *
     * @return
     */
    public Map<String, Object> getBaseMap(Map<String, String> authMap, String serviceMethod) {
        Map<String, Object> paramsMap = new HashMap<>(6);
        //API账号
        String appToken = authMap.get("clientId");
        //API密码
        String appKey = authMap.get("clientSecret");
        paramsMap.put("appToken", appToken);
        paramsMap.put("appKey", appKey);
        paramsMap.put("serviceMethod", serviceMethod);
        return paramsMap;
    }

    public static void main(String[] args) {


    }

}
