package com.erp.server.tms.service.carrier;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.entity.TmsCarrierEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.TmsCarrierService;
import com.erp.server.tms.service.logistics.AliExpressLogisticsHandlerImpl;
import com.erp.tms.aliexpress.constants.PathConstants;
import com.erp.tms.aliexpress.model.handover.*;
import com.erp.tms.aliexpress.model.handover.request.*;
import com.erp.tms.aliexpress.model.handover.response.*;
import com.erp.tms.aliexpress.model.order.request.QueryOrderRequest;
import com.erp.tms.aliexpress.model.order.response.AllCarrierResponse;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class AliExpressCarrierTest {

    @Resource
    private TmsCarrierService tmsCarrierService;


    @Test
    public void testExpressCarrierAdd() {
        String appKey = "503630";
        String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String baseUrl = "https://api-sg.aliexpress.com";
        String apiName = AliexpressConstants.ALIEXPRESS_CARRIER_QUERY_LIST;
        String token = "50000101714jiEhoattd9gAtAs1b36f6d2wukiGxo0EzuFCyEOscLSZUnQkqkvOeUsJk";

        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        request.addApiParameter("locale", "zh_CN");
        log.warn("速卖通实际承运商:请求参数={}",  JSONUtil.toJsonStr(request));
        IopResponse response = null;
        try {
            response = client.execute(request, token, Protocol.TOP);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        System.out.println(JSONUtil.toJsonStr(response.getBody()));
        cn.hutool.json.JSONObject jsonObject = new cn.hutool.json.JSONObject(response.getBody());
        JSONObject queryListResponse = jsonObject.getJSONObject("cainiao_global_logistics_carrier_querylist_response");
        AllCarrierResponse bean = JSONUtil.toBean(queryListResponse, AllCarrierResponse.class);


    }
}
