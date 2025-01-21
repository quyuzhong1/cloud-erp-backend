package com.erp.server.dmp.controller.api;


import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.sdk.third.lingxing.dto.ProductInfo;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 历史信息同记录
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@RestController
@RequestMapping("/history")
public class DmpHistorySyncThirdController extends BaseController {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 同步全量sku 到领星
     *
     * @param skuIdList skuIdList
     * @return ApiResult<String>
     */
    @PostMapping("/lingxing/product/sync")
    public ApiResult<?> add(@RequestBody List<String> skuIdList) {
        List<SkuVO> skuVOS;
        if (CollectionUtils.isEmpty(skuIdList)) {
            skuVOS = plmTaskFeign.listApproveSku();
        } else {
            skuVOS = plmTaskFeign.listBySkuNoList(skuIdList);
        }
        // 查询已有产品
        Result<Object> result = LingxingApiUtils.getAndSign(LingxingApiUtils.PRODUCT_LIST_URI, new HashMap<>());
        Object data = result.getData();
        JSONArray jsonArray = new JSONArray();
        if (null != data){
            jsonArray = JSONArray.parseArray(JSON.toJSONString(data));
        }

        List<ProductInfo> collect = skuVOS.stream().map(e -> new ProductInfo(
                LingxingApiUtils.convertLxSku(e.getSkuNo()),
                LingxingApiUtils.convertLxProductName(e.getSkuName()),
                e.getSkuId())).collect(Collectors.toList());
        for (ProductInfo productInfo : collect) {
            boolean exist = jsonArray.stream()
                    .anyMatch(e -> productInfo.getSkuIdentifier().toString().equalsIgnoreCase(((JSONObject) e).getString("sku_identifier")));
            if (exist){
                LingxingApiUtils.updateProduct(productInfo);
                continue;
            }
            if (productInfo.getSku().length() >= 50){
                log.warn("SKU长度过长: 跳过：{}", JSONUtil.toJsonStr(productInfo));
                continue;
            }
            LingxingApiUtils.addProduct(productInfo);
        }
        return success();
    }
}
