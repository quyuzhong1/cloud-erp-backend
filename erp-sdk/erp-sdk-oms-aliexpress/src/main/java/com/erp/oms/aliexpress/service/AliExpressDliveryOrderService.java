package com.erp.oms.aliexpress.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressDeliveryDetail;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class AliExpressDliveryOrderService {

    public IopResponse getDelivery(Map<String, String> authMap, List<String> orderIds) throws ApiException {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = AliexpressConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.ALIEXPRESS_ASCP_FFO_QUERY);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("customer_order_number_list", orderIds);
        request.addApiParameter("fulfillment_forward_order_query", JSONObject.toJSONString(paramMap));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        return response;
//        return JSONObject.parseObject(response.getBody(), LabelResult.class);
    }

    public List<AliExpressDeliveryDetail> getDeliveryDetail(Map<String, String> authMap, String fulfillmentOrderNo) {
        String appKey = authMap.get("clientId");
        String appSecret = authMap.get("clientSecret");
        String token = authMap.get("token");
        String url = authMap.get("url");
        if (StringUtils.isBlank(url)){
            url = AliexpressConstants.BASE_URL;
        }
        validate(appKey,appSecret,token,url);
        IopClient client = new IopClientImpl(url, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.ALIEXPRESS_ASCP_FFO_ITEM_QUERY);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("fulfillment_order_no", fulfillmentOrderNo);
        request.addApiParameter("fulfillment_forward_order_item_query", com.alibaba.fastjson.JSONObject.toJSONString(paramMap));
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            log.error("查询速卖通发货单明细请求失败>>>>>>>{}", request.toString());
        }
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        cn.hutool.json.JSONObject resultJsONObject = jsonObject.getJSONObject("aliexpress_ascp_ffo_item_query_response");
        cn.hutool.json.JSONObject resultJson = JSONUtil.parseObj(resultJsONObject.get("result"));
        cn.hutool.json.JSONObject dataListJson = JSONUtil.parseObj(resultJson.get("data_list"));
        Boolean success = resultJson.getBool("success", Boolean.FALSE);
        //失败
        if (!success) {
            log.error("查询速卖通发货单明细失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            return Collections.emptyList();
        }
        List<AliExpressDeliveryDetail> detailList = dataListJson.getBeanList("data",AliExpressDeliveryDetail.class);
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        return detailList;
    }


    private void validate(String appKey,String appSecret,String token,String url){
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(token) || StringUtils.isBlank(url) ) throw new ServiceException("授权信息不能为空");
    }


    public static void main(String[] args) throws ApiException {
        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String baseUrl = "https://api-sg.aliexpress.com";
        String token = "50000200216zwXSmacvxdR9mlN3Q173edb18whDaGtElRAyxCAEBR9sxVko62BrXG7tj";
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(AliexpressConstants.ALIEXPRESS_ASCP_FFO_ITEM_QUERY);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("biz_type", 288000);
        paramMap.put("fulfillment_order_no", "WH0569510380903244");
        System.out.println();
        request.addApiParameter("fulfillment_forward_order_item_query", JSONObject.toJSONString(paramMap));
        IopResponse response = client.execute(request, token, Protocol.TOP);
        System.out.println(response.getBody());

    }

    public List<PlatformDeliveryDetailDTO> handleData(List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList) {
        List<PlatformDeliveryDetailDTO> resultList = new ArrayList<>();
        //相同平台skuId汇总后将数量除以scItemId 的种类数
        Map<String,List<PlatformDeliveryDetailDTO>> groupMap = platformDeliveryDetailDTOList.stream().collect(Collectors.groupingBy(PlatformDeliveryDetailDTO::getPlatformSkuId));
        groupMap.forEach((key,val)->{
            PlatformDeliveryDetailDTO platformDeliveryDetailDTO = val.get(0);
            Integer allQty = val.stream().mapToInt(PlatformDeliveryDetailDTO::getQty).sum();
            Integer scItemIdCount = Math.toIntExact(val.stream().map(PlatformDeliveryDetailDTO::getScItemId).distinct().count());
            platformDeliveryDetailDTO.setQty(allQty/scItemIdCount);
            resultList.add(platformDeliveryDetailDTO);
        });
        return resultList;
    }
}
