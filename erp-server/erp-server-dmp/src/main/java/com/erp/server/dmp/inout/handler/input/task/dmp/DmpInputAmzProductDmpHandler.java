package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzProductDmpHandler extends DmpInputDoChildDmpHandler {

    public static final String SPU_ID = "spuId";

    public static final String PRODUCT_ID = "productId";

    public static final String AMAZON_LISTING_DETAIL_DATA = "amazon_listingDetail_data";

    public static final String AMAZON_LISTING_DATA = "amazon_listing_data";

    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        List<ParamData> paramDataList = new ArrayList<>();
        String childId = this.getDmpCfgInputChildId();
        if (StringUtils.isBlank(childId)) {
            throw new ServiceException("未查询到DmpInputAmzProductDmpHandler子类id");
        }

        List<ParamData> detailParamDataList = new ArrayList<>();
        detailParamDataList.add(new ParamData("nextLevelId", "nextLevelId", PannoEnum.EQ, nextLevelId));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(detailParamDataList, AMAZON_LISTING_DATA);

        // 按产品ID维度去重
        List<Map<String, Object>> dmpInputMongoLastList = new LinkedList<>();
        Set<String> existProductIdList = new HashSet<>();
        for (Map<String, Object> convertData : dmpInputMongoChildList) {
            // 保留第一次出现的 productId，移除后续重复
            String productId = convertData.getOrDefault(PRODUCT_ID, "").toString();
            if (!existProductIdList.contains(productId)) {
                existProductIdList.add(productId);
                dmpInputMongoLastList.add(convertData);
            }
        }

        // 查询和替换ASIN
        // 查询明细
        // 当前sku子任务明细所有结果
        List<Map<String, Object>> listingDetailMongoData = mongoService.findMongoData(detailParamDataList, AMAZON_LISTING_DETAIL_DATA);

        for (Map<String, Object> listingMongoDataItem : dmpInputMongoLastList) {
            String listingProductId = listingMongoDataItem.getOrDefault(PRODUCT_ID, "").toString();
            if (StringUtils.isBlank(listingProductId)) {
                continue;
            }
            String productIdType = listingMongoDataItem.getOrDefault("productIdType", "").toString();
            if ("1".equalsIgnoreCase(productIdType)){
                // 标准类型asin=productId
                listingMongoDataItem.put("asin", listingProductId);
            }
            String asin1 = listingMongoDataItem.getOrDefault("asin1", "").toString();
            if (StringUtils.isNotBlank(asin1)){
                // 默认asin=asin1
                listingMongoDataItem.put("asin", asin1);
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
            String asin = detailMap.getOrDefault("asin", "").toString();
            if (StringUtils.isBlank(asin)) {
                ServiceException.runError("明细asin为空：{}", listingProductId);
            }
            listingMongoDataItem.put("asin", asin);
        }
        return dmpInputMongoLastList;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzProductDmpHandler afterConvertData 处理");
    }

    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        if (CollectionUtils.isEmpty(dmpInputMongoChildEntityList)) {
            return;
        }
        List<String> reportIds = dmpInputMongoChildEntityList.stream()
                .map(e -> e.getOrDefault("reportId", "").toString())
                .distinct()
                .collect(Collectors.toList());

        DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
        String parentStorageName = mainConvertId.getStorageName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq("next_level_id", nextLevelId);
        wrapper.in("report_id", reportIds);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        Map<String, String> billNoIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                billNoIdMap.put(listMap.get("report_id").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String billNo = dmpInputMongoChildEntity.get("reportId").toString();
            String dmpId = billNoIdMap.get(billNo);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
            dmpInputMongoChildEntity.put("sourceId", dmpId);
        }
    }
}
