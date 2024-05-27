package com.erp.tms.batong.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.common.core.utils.OkHttpUtils;
import com.erp.tms.batong.constants.BaTongConstants;
import com.erp.tms.batong.model.label.base.BaseData;
import com.erp.tms.batong.model.label.base.BaseResult;
import com.erp.tms.batong.model.label.request.LabelRequest;
import com.erp.tms.batong.model.label.request.ListOrder;
import com.erp.tms.batong.model.label.response.LabelResponse;
import com.erp.tms.batong.model.order.request.BaTongUpdateWeightReq;
import com.erp.tms.batong.model.order.request.OrderRequest;
import com.erp.tms.batong.model.order.response.OrderResponse;
import com.erp.tms.batong.model.order.response.TrackBase;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
     * 获取基础数据 -运输方式
     */
    public List<BaseData> listShippingMethod(Map<String, String> authMap) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.GET_SHIPPING_METHOD;
        log.info("获取巴通基础信息url：{}", baseUrl + serviceMethod);
        String paramsJson = "";
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        paramsMap.put("paramsJson", paramsJson);
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("创建巴通订单返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        Integer success = result.getSuccess();
        //表示成功
        if (BaTongConstants.SUCCESS.equals(success)) {
            return JSONUtil.toList(result.getData().toString(), BaseData.class);
        } else {
            throw new ServiceException(result.getCnMessage());
        }
    }


    /**
     * 创建订单
     *
     * @param
     * @return
     */
    public BaseResult createOrder(Map<String, String> authMap, OrderRequest orderRequest) {
        log.info("==========BaTongService.createOrder==========start");
        log.info("authMap:{}, orderRequest:{}",authMap, orderRequest);
        String serviceMethod = BaTongConstants.POST_CREATE_ORDER_URL;
        String baseUrl = BaTongConstants.BASE_URL;
        log.info("创建巴通订单url：{}", baseUrl + serviceMethod);
        String paramsJson = JSONUtil.toJsonStr(orderRequest);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        paramsMap.put("paramsJson", paramsJson);
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("创建巴通订单返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        return  result;

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
    public BaseResult deleteOrder(Map<String, String> authMap, String referenceNo) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.DELETE_ORDER_URL;
        log.info("删除巴通订单url：{}", baseUrl + serviceMethod);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        Map<String, String> map = new HashMap<>();
        map.put("reference_no", referenceNo);
        paramsMap.put("paramsJson", JSONUtil.toJsonStr(map));
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("刪除巴通订单返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        return  result;

    }
    /**
     * 更新重量
     */
    public BaseResult updateWeight(Map<String, String> authMap, BaTongUpdateWeightReq baTongUpdateWeightReq) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.UPDATE_ORDER;
        log.info("更新巴通订单重量：{}", baseUrl + serviceMethod);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        paramsMap.put("paramsJson", JSONUtil.toJsonStr(baTongUpdateWeightReq));
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("更新巴通订单重量返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        return  result;
    }

    /**
     * @return
     * @description 获取标签信息
     * @author Lambda
     * @create 2024-01-15 11:42
     */
    public BaseResult getLabel(Map<String, String> authMap, LabelRequest labelRequest) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.LABEL_URL;
        log.info("获取巴通标签url：{}", baseUrl + serviceMethod);
        String paramsJson = JSONUtil.toJsonStr(labelRequest);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        paramsMap.put("paramsJson", paramsJson);
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("获取巴通标签返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        return result;

    }

    /**
     * 获取跟踪单号
     *
     * @param authMap
     * @param order
     * @return
     */
    public TrackBase getTrack(Map<String, String> authMap, ListOrder order) {
        String baseUrl = BaTongConstants.BASE_URL;
        String serviceMethod = BaTongConstants.GET_TRACK_URL;
        log.info("获取跟踪单号url：{}", baseUrl + serviceMethod);
        String paramsJson = JSONUtil.toJsonStr(order);
        Map<String, Object> paramsMap = getBaseMap(authMap, serviceMethod);
        paramsMap.put("paramsJson", paramsJson);
        String resBody = OkHttpUtils.doPost(baseUrl, paramsMap, MapUtil.empty());
        log.info("获取巴通标签返回结果：{}", resBody);
        BaseResult result = JSONUtil.toBean(resBody, BaseResult.class);
        Integer success = result.getSuccess();
        //表示成功
        if (BaTongConstants.SUCCESS.equals(success)) {
            return JSONUtil.toBean(JSONUtil.toJsonStr(result.getData()), TrackBase.class);
        } else {
            throw new ServiceException(result.getCnMessage());
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
        validate(appToken, appKey, serviceMethod);
        paramsMap.put("appToken", appToken);
        paramsMap.put("appKey", appKey);
        paramsMap.put("serviceMethod", serviceMethod);
        return paramsMap;
    }


    private void validate(String token, String key, String url) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(key) || StringUtils.isEmpty(url)) {
            throw new ServiceException("授权信息不能为空");
        }
    }

    public static void main(String[] args) {
        BaTongService service = new BaTongService();
        Map<String, String> authMap = new HashMap<>();
        authMap.put("clientId", "681425f7eb33b64f3f809d97b56c46cf");
        authMap.put("clientSecret", "d85a27ff8690a777ee6485ebff679792d85a27ff8690a777ee6485ebff679792");
        BaTongUpdateWeightReq baTongUpdateWeightReq = new BaTongUpdateWeightReq();
        baTongUpdateWeightReq.setReferenceNo("XSDS24022900002D782");
        baTongUpdateWeightReq.setOrderWeight("0.6");
        BaseResult result = service.updateWeight(authMap,baTongUpdateWeightReq);
        System.out.println(JSONUtil.toJsonStr(result));


    }


}
