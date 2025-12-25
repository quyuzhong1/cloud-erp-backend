package com.erp.server.dmp.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.WebhookServiceEnum;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.AwdApi;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonAwdQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonAwdSortTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.awd.InboundShipment;
import com.erp.sdk.oms.amz.spapi.model.awd.ShipmentLabels;
import com.erp.sdk.oms.amz.spapi.model.awd.ShipmentListing;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetLabelsResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.factory.WebhookHandlerFactory;
import com.erp.server.dmp.handler.WebhookHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName WebhookController
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("WebHook接收管理")
@RequestMapping("/webhook")
public class WebhookController extends BaseController {
    @Resource
    private CfgAppClientService cfgAppClientService;
    // 预先约定的Secret
    private static final String SECRET_KEY = "your_secret_key";
    // 允许的时间偏差（秒）
    private static final long MAX_AGE = 5 * 60L; // 5 minutes
    private final WebhookHandlerFactory webhookHandlerFactory;
    @Resource
    private FileFeign fileFeign;



    public WebhookController(WebhookHandlerFactory webhookHandlerFactory) {
        this.webhookHandlerFactory = webhookHandlerFactory;
    }

    /**
     * 接收Webhook请求
     *
     * @param serviceFlag 服务名称
     * @param data        传递数据
     * @param headers     请求头
     * @return
     */
    @PostMapping("/receive/{serviceFlag}")
    public String receiveWebhook(@PathVariable("serviceFlag") String serviceFlag,
                                 @RequestBody String data,
                                 @RequestHeader Map<String, String> headers) {
        log.info("========接收到webhook接口请求=======start");
        log.info("receiveWebhook:serviceFlag:{},data:{},headers:{}", serviceFlag, data, headers);
        // 解析请求中的服务标识，进行不同的处理
        String service = getService(serviceFlag, headers, data);
        // 根据不同平台的Webhook内容做处理
        WebhookHandler handler = webhookHandlerFactory.getHandler(service);
        //安全校验
        handler.verify(data, headers, serviceFlag);
        //业务处理
        String result = handler.process(data, headers, serviceFlag);
        log.info("========接收到webhook接口请求=======end");
        return result;
    }

    private String getService(String serviceFlag, Map<String, String> headers, String data) {
        WebhookServiceEnum serviceEnum = WebhookServiceEnum.getByName(serviceFlag);
        if (Objects.nonNull(serviceEnum)) {
            return serviceEnum.getCode();
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return "";
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取请求的完整URL
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }
        String platform = headers.get("X-Platform");  // 假设平台信息通过头部传递
        return "";
    }

    @PostMapping("/listInboundShipmentsTest")
    public ShipmentListing listInboundShipmentsTest(@RequestBody String shopId) throws ApiException, LWAException {
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 创建AwdApi实例
        AwdApi api = AmazonSpApiInitUtils.create(AwdApi.class, shopInfoDTO, false);
        String sortBy = AmazonAwdQueryTypeEnum.UPDATED_AT.getCode();
        String sortOrder = AmazonAwdSortTypeEnum.DESCENDING.getCode();
        String shipmentStatus = null;
        String updatedAfter = "2025-11-01T00:00:00.000Z";
        String updatedBefore = "2025-11-30T00:00:00.000Z";
        Integer maxResults = 20;
        String nextToken = null;
        ShipmentListing response = api.listInboundShipments(sortBy, sortOrder, shipmentStatus, updatedAfter, updatedBefore, maxResults, nextToken);
        System.out.println(response);
        return response;
    }

    @PostMapping("/getInboundShipment")
    public InboundShipment getInboundShipment(@RequestParam("shipmentId") String shipmentId, @RequestParam("shopId") String shopId) throws ApiException, LWAException {
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 创建AwdApi实例
        AwdApi api = AmazonSpApiInitUtils.create(AwdApi.class, shopInfoDTO, false);
        InboundShipment inboundShipment = api.getInboundShipment(shipmentId, null);
        System.out.println(inboundShipment);
        return inboundShipment;
    }
    @PostMapping("/getInboundShipmentLable")
    public ShipmentLabels getInboundShipmentLable(@RequestParam("shipmentId") String shipmentId,
                                                  @RequestParam("shopId") String shopId,
                                                  @RequestParam("pageType") String pageType,
                                                  @RequestParam("formatType") String formatType
    ) throws ApiException, LWAException, IOException {
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 创建AwdApi实例
        AwdApi api = AmazonSpApiInitUtils.create(AwdApi.class, shopInfoDTO, false);
        ShipmentLabels inboundShipmentLabels = api.getInboundShipmentLabels(shipmentId, pageType, formatType);
        System.out.println(inboundShipmentLabels);
        if (CharSequenceUtil.isNotBlank(inboundShipmentLabels.getLabelDownloadURL())){
            String pdfUrlToBase64 = FileUtil.convertPdfUrlToBase64(inboundShipmentLabels.getLabelDownloadURL());
            String url = fileFeign.uploadFileByBase64(pdfUrlToBase64);
            inboundShipmentLabels.setLabelDownloadURL(url);
        }
        return inboundShipmentLabels;
    }
    @PostMapping("/getFbaShipmentLable")
    public String getFbaShipmentLable(@RequestParam("shipmentId") String shipmentId,
                                                  @RequestParam("shopId") String shopId,
                                                  @RequestParam("pageType") String pageType
    ) throws ApiException, IOException {
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 创建AwdApi实例
        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        GetLabelsResponse response = api.getLabels(shipmentId, pageType, "BARCODE_2D", null, null, null, 100, null);
        System.out.println(response);
        String labelUrl = "";
        if (CharSequenceUtil.isNotBlank(response.getPayload().getDownloadURL())){
            String pdfUrlToBase64 = FileUtil.convertPdfUrlToBase64(response.getPayload().getDownloadURL());
            labelUrl = fileFeign.uploadFileByBase64(pdfUrlToBase64);
        }
        return labelUrl;
    }


}
