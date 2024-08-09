package com.erp.oms.aliexpress.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.ProductRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressItem;
import com.erp.oms.aliexpress.dto.response.AliExpressProduct;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        request.addApiParameter("aeop_a_e_product_list_query", JSONUtil.toJsonStr(paramMap));
        String token = productRequest.getToken();
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        JSONObject resultJsONObject = jsonObject.getJSONObject("result");
        if (null == resultJsONObject) {
            String msg = StrUtil.format("拉取速卖通商品失败:无result, response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        boolean success = resultJsONObject.getBool("success", false);
        //失败
        if (!success) {
            log.error("拉取速卖通商品失败>>>>>>>{}", resultJsONObject.getOrDefault("error_message", "").toString());
            String msg = StrUtil.format("拉取速卖通商品失败: response={}", JSONUtil.toJsonStr(response));
            throw new ServiceException(msg);
        }
        resultJsONObject.get("aeop_a_e_product_display_d_t_o_list");
        JSONArray jsonArray = resultJsONObject.getJSONArray("aeop_a_e_product_display_d_t_o_list");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return;
        }
        //产品ids
        List<AliExpressItem> productInfoList = JSONUtil.toList(jsonArray, AliExpressItem.class);

        for (AliExpressItem item : productInfoList) {
            Long productId = item.getProductId();
            AliExpressProduct product = this.getProductInfo(productId, productRequest);
            if (Objects.nonNull(product)) {
                productList.add(product);
            }
        }

        //总页数
        Integer totalPage = resultJsONObject.getInt("total_page");
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
        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        //表示成功
        if (null != jsonObject) {
            JSONObject json = jsonObject.getJSONObject("aliexpress_offer_product_query_response");
            if (null == json){
                log.error("【速卖通】获取到具体的产品信息异常：productId={}, response={}", productId, JSONUtil.toJsonStr(response.getBody()));
                // 临时跳过
                return null;
            }
            JSONObject json2 = json.getJSONObject("result");
            if (null == json2){
                log.error("【速卖通】获取到具体的产品信息异常：productId={}, response={}", productId, JSONUtil.toJsonStr(response.getBody()));
                // 临时跳过
                return null;
            }
            return JSONUtil.toBean(json2, AliExpressProduct.class);
        }
        log.error("【速卖通】获取到具体的产品信息异常：productId={}, response={}", productId, JSONUtil.toJsonStr(response.getBody()));
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
            token("50000700312cJ4nYbrzErAqH159364aboXoPdAcJwjMuEzrGXEAudlrVcjGsjo4bIr30").
            apiName("aliexpress.postproduct.redefining.findproductinfolistquery").
            baseUrl("https://api-sg.aliexpress.com")
            .build();

    // 32818224525
    //1005002505576388
    try {
        AliExpressProduct productInfo = service.getProductInfo(1005002505576388L, productRequest);
        System.out.println(JSONUtil.toJsonStr(productInfo));
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
