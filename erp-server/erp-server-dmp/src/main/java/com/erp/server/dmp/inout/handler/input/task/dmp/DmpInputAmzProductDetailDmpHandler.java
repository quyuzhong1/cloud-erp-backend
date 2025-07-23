package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.*;
import com.erp.sdk.oms.amz.spapi.model.productpricing.ASINIdentifier;
import com.erp.sdk.oms.amz.spapi.model.productpricing.IdentifierType;
import com.erp.server.dmp.service.DmpProductInfoService;
import com.erp.server.dmp.service.DmpSkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 亚马逊Listing下一步字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzProductDetailDmpHandler extends DmpInputDoChildDmpHandler {

    public static final String PRODUCT_ID = "productId";
    public static final String NEXT_LEVEL_ID = "nextLevelId";
    public static final String REPORT_ID = "reportId";
    public static final String SHOP_ID = "shopId";
    public static final String SPU_ID = "spu_id";
    public static final String SELLER_SKU = "sellerSKU";

    public static final String SELLER_SKU_OTHER = "sellerSku";

    public static final String AMAZON_LISTING_DATA = "amazon_listing_data";

    public static final String AMAZON_LISTING_PRICING_DATA = "amazon_listing_pricing_data";


    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Resource
    private DmpProductInfoService dmpProductInfoService;

    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        if (CollectionUtils.isEmpty(dmpInputMongoEntityList)) {
            return Collections.emptyList();
        }
        Map<String, Object> reportInfoMap = dmpInputMongoEntityList.get(0);
        String reportId = reportInfoMap.getOrDefault(REPORT_ID, "").toString();
        String shopId = reportInfoMap.getOrDefault(SHOP_ID, "").toString();
        String marketplaceIds = reportInfoMap.getOrDefault("marketplaceIds", "").toString();
        String marketplaceId = Arrays.stream(marketplaceIds.split(",")).findFirst().orElse(null);
        if (StringUtils.isBlank(marketplaceId)) {
            ServiceException.runError("listing解析异常:marketplaceIds丢失:{}", reportInfoMap.toString());
        }

        // 当前sku子任务明细所有结果
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(NEXT_LEVEL_ID, NEXT_LEVEL_ID, PannoEnum.EQ, shopId));
        List<Map<String, Object>> listingDetailMongoData = mongoService.findMongoData(paramDataList, childMongoStorageName);

        // 当前站点所有价格信息
//        List<ParamData> paramDataPricingList = new ArrayList<>();
//        paramDataPricingList.add(new ParamData(SHOP_ID, SHOP_ID, PannoEnum.EQ, shopId));
//        List<Map<String, Object>> listingPricingMongoData = mongoService.findMongoData(paramDataPricingList, AMAZON_LISTING_PRICING_DATA);

        // 按listing报告内容
        List<ParamData> chlidParamDataList = new ArrayList<>();
        chlidParamDataList.add(new ParamData(REPORT_ID, REPORT_ID, PannoEnum.EQ, reportId));
        List<Map<String, Object>> listingMongoData = mongoService.findMongoData(chlidParamDataList, AMAZON_LISTING_DATA);
        if (CollectionUtils.isEmpty(listingMongoData)) {
            return Collections.emptyList();
        }

        // 组合按listing报告内容和明细
        for (Map<String, Object> listingMongoDataItem : listingMongoData) {
            String listingProductId = listingMongoDataItem.getOrDefault(PRODUCT_ID, "").toString();
            if (StringUtils.isBlank(listingProductId)) {
                continue;
            }
            String sellerSku = listingMongoDataItem.getOrDefault(SELLER_SKU, "").toString();
            if (StringUtils.isBlank(sellerSku)) {
                // 兼容来源驼峰
                sellerSku = listingMongoDataItem.getOrDefault(SELLER_SKU_OTHER, "").toString();
            }
            if (StringUtils.isBlank(sellerSku)){
               ServiceException.runError(" sellerSku 解析为空");
            }

            String productIdType = listingMongoDataItem.getOrDefault("productIdType", "").toString();
            if ("1".equalsIgnoreCase(productIdType)) {
                // 标准类型asin=productId
                listingMongoDataItem.put("asin", listingProductId);
            }
            String asin1 = listingMongoDataItem.getOrDefault("asin1", "").toString();
            if (StringUtils.isNotBlank(asin1)) {
                // 默认asin=asin1
                listingMongoDataItem.put("asin", asin1);
            } else {
                // 匹配明细信息补充ASIN
                String finalSellerSku = sellerSku;
                Map<String, Object> detailMap = listingDetailMongoData
                        .stream()
                        .filter(e -> e.getOrDefault(SELLER_SKU, "").toString().equalsIgnoreCase(finalSellerSku))
                        .findFirst()
                        .orElse(null);
                if (null != detailMap){
                    Object productObj = detailMap.get("product");
                    if (null != productObj){
                        JSONObject productJsonObj = JSONObject.parseObject(JSONUtil.toJsonStr(productObj));
                        JSONObject identifiersObj = productJsonObj.getJSONObject("identifiers");
                        if (null != identifiersObj){
                            IdentifierType identifiertype = identifiersObj.toJavaObject(IdentifierType.class);
                            ASINIdentifier marketplaceASIN = identifiertype.getMarketplaceASIN();
                            if (null != marketplaceASIN){
                                listingMongoDataItem.put("asin", marketplaceASIN.getASIN());
                            }
                        }
                    }
                }
            }

            String listingMongoDataReportId = listingMongoDataItem.getOrDefault(REPORT_ID, "").toString();
            if(StringUtils.isNotBlank(listingMongoDataReportId) && StringUtils.isNotBlank(reportId) && !listingMongoDataReportId.equalsIgnoreCase(reportId)){
                //标记为删除状态
                listingMongoDataItem.put("status", "Delete");
            }

            // 匹配明细
            Map<String, Object> detailMap = listingDetailMongoData
                    .stream()
                    .filter(e -> e.getOrDefault(PRODUCT_ID, "").toString().equalsIgnoreCase(listingProductId))
                    .findFirst()
                    .orElse(null);
            if (null == detailMap) {
                continue;
            }
            // 拼接产品规格信息
            Object summariesObj = detailMap.get("summaries");
            if (null != summariesObj) {
                List<ItemSummaryByMarketplace> summaries = JSONUtil.toList(JSONUtil.toJsonStr(summariesObj), ItemSummaryByMarketplace.class);
                String productSpec = summaries.stream()
                        .filter(e -> e.getMarketplaceId().equalsIgnoreCase(marketplaceId))
                        .map(e -> StrUtil.format("{}/{}", e.getColor(), e.getSize()).replace("null", "-"))
                        .collect(Collectors.joining("\n"));
                listingMongoDataItem.put("productSpec", productSpec);
            }

            // 找出第一张图片信息
            Object imagesObj = detailMap.get("images");
            if (null != imagesObj) {
                List<ItemImagesByMarketplace> images = JSONUtil.toList(JSONUtil.toJsonStr(imagesObj), ItemImagesByMarketplace.class);
                String itemImage = parseFirstImage(images, marketplaceId);
                listingMongoDataItem.put("imageUrl", itemImage);
            }
            Object dimensionsObj = detailMap.get("dimensions");
            List<ItemDimensionsByMarketplace> dimensionList = JSONUtil.toList(JSONUtil.toJsonStr(dimensionsObj), ItemDimensionsByMarketplace.class);
            if (null != dimensionList) {
                ItemDimensionsByMarketplace itemDimensionsByMarketplace = dimensionList.stream()
                        .filter(e -> e.getMarketplaceId().equalsIgnoreCase(marketplaceId))
                        .findFirst()
                        .orElse(null);
                if (null != itemDimensionsByMarketplace) {
                    Dimensions dimensions = itemDimensionsByMarketplace.getPackage();
                    if (null != dimensions) {
                        Dimension height = dimensions.getHeight();
                        Dimension length = dimensions.getLength();
                        Dimension weight = dimensions.getWeight();
                        Dimension width = dimensions.getWidth();
                        //  （重量统一换算成KG，pounds=lb=0.453KG，1oz=0.028KG；尺寸统一换算成CM，inches=2.54CM）
                        String heightStr = null == height ? "0" : height.getValue().multiply(new BigDecimal("2.54")).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
                        String lengthStr = null == length ? "0" : length.getValue().multiply(new BigDecimal("2.54")).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
                        String widthStr = null == width ? "0" : width.getValue().multiply(new BigDecimal("2.54")).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
                        String weightStr = null == weight ? "0" : weightKg(dimensions.getWeight()).setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
                        dimensions.weight(dimensions.getWeight());
                        listingMongoDataItem.put("packageHeight", heightStr);
                        listingMongoDataItem.put("packageLength", lengthStr);
                        listingMongoDataItem.put("packageWidth", widthStr);
                        listingMongoDataItem.put("grossWeight", weightStr);
                    }
                }
            }
            //记录父平台产品id
            Object relationshipsObj = detailMap.get("relationships");
            if(null != relationshipsObj){
                List<ItemRelationshipsByMarketplace> itemRelationshipsByMarketplace = JSONUtil.toList(JSONUtil.toJsonStr(relationshipsObj), ItemRelationshipsByMarketplace.class);
                if(CollectionUtils.isNotEmpty(itemRelationshipsByMarketplace)){
                    List<ItemRelationship> relationships = itemRelationshipsByMarketplace.get(0).getRelationships();
                    if(CollectionUtils.isNotEmpty(relationships)){
                        List<String> parentAsins = relationships.get(0).getParentAsins();
                        if(CollectionUtils.isNotEmpty(parentAsins) && StringUtils.isNotBlank(parentAsins.get(0))){
                            listingMongoDataItem.put("platformParentSkuId", parentAsins.get(0));
                        }
                    }
                }
            }

            listingMongoDataItem.put("packageUnit", "cm");
            listingMongoDataItem.put("weightUnit", "kg");
            // 移除公共字段
            detailMap.remove("_id");
            detailMap.remove("dataEncrypt");
            detailMap.remove("convertId");
            detailMap.remove("inputTaskId");
            detailMap.remove("mongoCreateTime");
            detailMap.remove("mongoUpdateTime");
            detailMap.remove("nextLevelId");
            detailMap.remove("uniqueEncrypt");
            // 添加到当前
            listingMongoDataItem.putAll(detailMap);
        }
        return listingMongoData;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzProductDetailDmpHandler afterConvertData 处理");
    }

    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        ServiceImpl parentServiceImpl = this.getServiceImpl("dmp_product_info");
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        Map<String, String> billNoIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                billNoIdMap.put(listMap.get(SPU_ID).toString(), listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String billNo = dmpInputMongoChildEntity.get(PRODUCT_ID).toString();
            String dmpId = billNoIdMap.get(billNo);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
        }
    }

    public static BigDecimal weightKg(Dimension weight) {
        if ("kilograms".equalsIgnoreCase(weight.getUnit())) {
            return weight.getValue();
        }
        if ("pounds".equalsIgnoreCase(weight.getUnit())) {
            return weight.getValue().multiply(new BigDecimal("0.453"));
        }
        if ("1oz".equalsIgnoreCase(weight.getUnit())) {
            return weight.getValue().multiply(new BigDecimal("0.028"));
        }
        return weight.getValue();
    }

    /**
     * 解析第一张图片
     */
    private static String parseFirstImage(List<ItemImagesByMarketplace> images, String marketplaceId) {
        ItemImagesByMarketplace itemImagesByMarketplace = images.stream()
                .filter(e -> e.getMarketplaceId().equalsIgnoreCase(marketplaceId))
                .findFirst()
                .orElse(null);
        if (null != itemImagesByMarketplace) {
            if (!CollectionUtils.isEmpty(itemImagesByMarketplace.getImages())) {
                ItemImage itemImage = itemImagesByMarketplace.getImages()
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (null != itemImage) {
                    return itemImage.getLink();
                }
            }
        }
        return "";
    }
}
