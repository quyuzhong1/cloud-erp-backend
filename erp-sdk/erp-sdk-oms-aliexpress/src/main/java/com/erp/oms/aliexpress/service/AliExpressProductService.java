package com.erp.oms.aliexpress.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.request.ProductRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressItem;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.AliExpressProduct;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static com.erp.oms.aliexpress.constants.AliexpressConstants.pageSize;

/**
 * 速卖通商品服务
 *
 * @author yl
 * @date 2023-11-22
 */
@Slf4j
@Component
public class AliExpressProductService {


    public void listProduct(ProductRequest productRequest, List<AliExpressProduct> productList) throws ApiException {
        String appKey = productRequest.getClientId();
        String appSecret = productRequest.getClientSecret();
        String baseUrl = productRequest.getBaseUrl();
        String apiName = productRequest.getApiName();
        Integer currentPage = productRequest.getCurrentPage();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("current_page", currentPage);
        paramMap.put("page_size", pageSize);
        paramMap.put("product_status_type", AliexpressConstants.ON_SELLING);
        paramMap.put("gmt_modified_start", productRequest.getStartTime());
        paramMap.put("gmt_modified_end", productRequest.getEndTime());
        request.addApiParameter("simplify", "true");
        request.addApiParameter("aeop_a_e_product_list_query", JSONObject.toJSONString(paramMap));
        String token = productRequest.getToken();
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("result");
        Boolean success = resultJsONObject.getBooleanValue("success");
        //失败
        if (!success) {
            log.error("拉取速卖通商品失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            return;
        }
        JSONArray jsonArray = (JSONArray) resultJsONObject.get("aeop_a_e_product_display_d_t_o_list");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return;
        }
        //产品ids
        List<AliExpressItem> productInfoList = JSONObject.parseArray(jsonArray.toJSONString(), AliExpressItem.class);
        for (AliExpressItem item : productInfoList) {
            Long productId = item.getProductId();
            AliExpressProduct product = this.getProductInfo(productId, productRequest);
            if (Objects.nonNull(product)) {
                productList.add(product);
            }
        }

        //总页数
        Integer totalPage = resultJsONObject.getInteger("total_page");
        //表示还有
        if (Objects.nonNull(totalPage) && !totalPage.equals(currentPage)) {
            productRequest.setCurrentPage(currentPage + 1);
            listProduct(productRequest, productList);
        }
    }

    /**
     * 获取到具体的产品信息
     *
     * @param productId
     * @param productRequest
     * @return
     */
    private AliExpressProduct getProductInfo(Long productId, ProductRequest productRequest) throws ApiException {
        String appKey = productRequest.getClientId();
        String appSecret = productRequest.getClientSecret();
        String baseUrl = productRequest.getBaseUrl();
        String apiName = AliexpressConstants.ALIEXPRESS_OFFER_PRODUCT_QUERY;
        String token = productRequest.getToken();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
//        request.addApiParameter("simplify", "true");
        request.addApiParameter("product_id", productId.toString());
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        //表示成功
        if (Objects.nonNull(jsonObject)) {
            JSONObject json = jsonObject.getJSONObject("aliexpress_offer_product_query_response");
            JSONObject json2 = json.getJSONObject("result");
            AliExpressProduct product = JSONObject.parseObject(json2.toJSONString(), AliExpressProduct.class);
            return product;
        }
        return null;
    }

//    public static void main(String[] args) {
//        AliExpressProductService service=new AliExpressProductService();
//        ProductRequest productRequest =ProductRequest.builder().
//                startTime("2022-11-23 00:00:00").
//                endTime("2022-11-25 00:00:00").
//                clientId("502978").
//                clientSecret("DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY").
//                token("50000200216zwXSmacvxdR9mlN3Q173edb18whDaGtElRAyxCAEBR9sxVko62BrXG7tj").
//                apiName("aliexpress.postproduct.redefining.findproductinfolistquery").
//                baseUrl("https://api-sg.aliexpress.com")
//                .build();
//
//        try {
//            service.listProduct(productRequest,new ArrayList<>());
//        } catch (ApiException e) {
//            e.printStackTrace();
//        }
//
//    }
public static void main(String[] args) {
    AliExpressProductService service=new AliExpressProductService();
    ProductRequest productRequest =ProductRequest.builder().
            startTime("2022-11-23 00:00:00").
            endTime("2022-11-25 00:00:00").
            clientId("502978").
            clientSecret("DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY").
            token("50000200216zwXSmacvxdR9mlN3Q173edb18whDaGtElRAyxCAEBR9sxVko62BrXG7tj").
            apiName("aliexpress.postproduct.redefining.findproductinfolistquery").
            baseUrl("https://api-sg.aliexpress.com")
            .build();

    try {
        service.getProductInfo(1005004988974205l,productRequest);
    } catch (ApiException e) {
        e.printStackTrace();
    }

}
//public static void main(String[] args) throws ApiException {
//    String appKey = "502978";
//    String appSecret = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
//    String baseUrl = "https://api-sg.aliexpress.com";
//    String token = "50000200216zwXSmacvxdR9mlN3Q173edb18whDaGtElRAyxCAEBR9sxVko62BrXG7tj";
//    IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
//    IopRequest request = new IopRequest();
//    request.setApiName(AliexpressConstants.ALIEXPRESS_OFFER_PRODUCT_QUERY);
//    Map<String, Object> paramMap = new HashMap<>();
////        paramMap.put("biz_type", 288000);
////        paramMap.put("fulfillment_order_no", "WH0569510380903244");
//    System.out.println();
////        request.addApiParameter("fulfillment_forward_order_item_query", JSONObject.toJSONString(paramMap));
//    request.addApiParameter("product_id", "1005004996572648");
//    IopResponse response = client.execute(request, token, Protocol.TOP);
//    System.out.println(response.getBody());
//
//}
}
