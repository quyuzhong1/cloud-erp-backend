package com.erp.server.wms.aliExpress;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.tms.aliexpress.constants.PathConstants;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.OffsetDateTime;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class AliExpressTests {

    @Resource
    private FbaShipmentService fbaShipmentService;
    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;


    @Test
    public void shipOrder() {
        DeclareDeliverRequest declareDeliverRequest = DeclareDeliverRequest.builder().
                outRef("5388490211908536").
                logisticsNo("CNG00659918457200").
                shopId("1735117862084808706").
                serviceName("AliExpress Standard Shipping").
                sendType("all").
                build();
//        标准-AliExpress Standard Shipping(菜鸟无忧物流-标准)

        try {
            String appKey = "502978";
            String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
            String baseUrl = "https://api-sg.aliexpress.com";
            String apiName = AliexpressConstants.DECLARE_DELIVER;
            String token = "50000200123dJAvRobgSKEtBJjvZtxEAZfV17b52f96gJQg0OG9CCvBqT1l8Mocp35cG";
            IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
            IopRequest request = new IopRequest();
            request.addApiParameter("simplify", "true");
            request.addApiParameter("logistics_no", declareDeliverRequest.getLogisticsNo());
            request.addApiParameter("send_type", declareDeliverRequest.getSendType());
            request.addApiParameter("out_ref", declareDeliverRequest.getOutRef());
            request.addApiParameter("service_name", declareDeliverRequest.getServiceName());
            request.setApiName(apiName);
            log.warn("【{}】速卖通标记发货:请求参数={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(request));
            IopResponse response = client.execute(request, token, Protocol.TOP);
            log.warn("【{}】速卖通标记发货:响应结果={}", declareDeliverRequest.getOutRef(), JSONUtil.toJsonStr(response));
            String body = response.getBody();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            Boolean success = jsonObject.getBool("result_success", Boolean.FALSE);
            if (!success) {
                String msg = jsonObject.getOrDefault("result_error_desc", "").toString();
                throw new ServiceException(ApiError.Default, msg);
            }
        } catch (Exception e) {
            throw new ServiceException("查询速卖通订单地址失败" + JSONUtil.toJsonStr(e));
        }
    }

    @Test
    public void testService() throws Exception{
        String appKey = "502978";
        String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String url = "https://api-sg.aliexpress.com";
        com.erp.tms.aliexpress.api.IopClient client = new com.erp.tms.aliexpress.api.IopClientImpl(url, appKey, appSecret);
        com.erp.tms.aliexpress.api.IopRequest request = new com.erp.tms.aliexpress.api.IopRequest();
        request.setApiName("aliexpress.logistics.redefining.listlogisticsservice");
        request.addApiParameter("simplify", "true");
        com.erp.tms.aliexpress.api.IopResponse response = client.execute(request, "50000200123dJAvRobgSKEtBJjvZtxEAZfV17b52f96gJQg0OG9CCvBqT1l8Mocp35cG", com.erp.tms.aliexpress.domain.Protocol.TOP);
        System.out.println("响应结果");
        System.out.println(JSONUtil.toJsonStr(response));
    }
}
