package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.LingxingPlatformCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderDetailNextDmpHandler extends DmpInputDoNextDmpHandler {

    public static final String LINGXING_TEMU_LISTING_DATA = "lingxing_temu_listing_data";

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("item_info");
        if (null == detailListObj) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(detailListObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> skuMongoData = new LinkedList<>();
        Object platformInfoObj = dmpInputMongoEntity.getOrDefault("platform_info", "[]");
        JSONArray parseArray = JSON.parseArray(JSON.toJSONString(platformInfoObj));
        if (CollectionUtils.isEmpty(parseArray)) {
            return Collections.emptyList();
        }
        String platformCode = parseArray.getJSONObject(0).getString("platform_code");
        if (LingxingPlatformCodeEnum.TEMU_FBP.getCode().equalsIgnoreCase(platformCode)) {
            // 如果是TeMu平台，查询spu_id信息
            String storeId = dmpInputMongoEntity.getOrDefault("store_id", "").toString();
            List<String> skuIds = jsonArray.stream()
                    .map(e -> JSON.parseObject(JSON.toJSONString(e)).getString("product_no"))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            // 查询sku信息
            List<ParamData> skuParamDataList = new ArrayList<>();
            skuParamDataList.add(new ParamData("storeId", "storeId", PannoEnum.EQ, storeId));
            skuParamDataList.add(new ParamData("mskuId", "mskuId", PannoEnum.IN, skuIds));
            skuMongoData = mongoService.findMongoData(skuParamDataList, LINGXING_TEMU_LISTING_DATA);
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Object detailObj : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(detailObj);
            // 如果是TeMu平台，添加spu_id信息
            if (LingxingPlatformCodeEnum.TEMU_FBP.getCode().equalsIgnoreCase(platformCode)) {
                String orderDetailProductNo = jsonObject.getOrDefault("product_no", "").toString();
                String platformSubSoCode = jsonObject.getOrDefault("order_item_no", "").toString();
                jsonObject.put("platformSkuId", orderDetailProductNo);
                Map<String, Object> skuInfoMap = skuMongoData.stream()
                        .filter(e -> e.get("mskuId").equals(orderDetailProductNo))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("SKU信息不存在，product_no: " + orderDetailProductNo));
                // 将产品ID信息添加到jsonObject中
                jsonObject.put("product_no", skuInfoMap.get("spuId"));
                jsonObject.put("platformSubSoCode", platformSubSoCode);
            }
            resultList.add(jsonObject);
        }
        return resultList;
    }

}
