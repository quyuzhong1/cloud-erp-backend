package com.erp.server.dmp.handler.mongo;


import com.common.business.constant.MongoTableNameContant;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiUnsuppressedInventoryMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportSuperMongoDTO;
import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
import com.erp.server.dmp.handler.DmpMongoHandler;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 亚马逊库存管理报告未归档
 *
 * @author Jim
 * @date 2024/1/24
 */
@Slf4j
@Component("amzReportFbaMyiUnsuppressedInventoryHandler")
public class AmzReportFbaMyiUnsuppressedInventoryHandler extends DmpMongoHandler {

    @Resource
    private DmpMongoHandleTaskService dmpMongoHandleTaskService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity, Boolean queryIsAddOrUpdate) {
        // 任务每次处理数量
        Integer handleCount = mongoHandleTaskEntity.getHandleCount();
        // 指定的mongo表
        String mongoTableName = MongoTableNameContant.DATA_REPORT_AMZ_FBA_MYI_UNSUPPRESSED_INVENTORY;
        List<ReportFbaMyiUnsuppressedInventoryMongoDTO> allList = dmpMongoHandleTaskService.findMongoData(mongoHandleTaskEntity.getLastId(), handleCount, mongoTableName, ReportFbaMyiUnsuppressedInventoryMongoDTO.class, queryIsAddOrUpdate);
        if (CollectionUtils.isEmpty(allList)) {
            log.warn("亚马逊库存管理报告未归档处理服务处理结束：处理数据为空:handleType={}", mongoHandleTaskEntity.getHandleType());
            return 0;
        }
        // 记录最大ID和下次执行时间
        String maxLastId = allList.stream().map(ReportSuperMongoDTO::getId).max(String::compareTo).orElse("0");
        dmpMongoHandleTaskService.updateMaxLastIdAndNextTime(mongoHandleTaskEntity, maxLastId);

        List<String> shopIds = allList.stream().map(ReportSuperMongoDTO::getRequestShopId).distinct().collect(Collectors.toList());
        // 库存管理数据
        // 查询当前店铺信息
        Map<String, ShopInfoEntity> shopMap = shopInfoFeign.listShopInfoByIds(shopIds).stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        // 查询SKU绑定的信息
        List<String> sellerSkuList = allList
                .stream()
                .map(ReportFbaMyiUnsuppressedInventoryMongoDTO::getSku)
                .distinct()
                .collect(Collectors.toList());

        // Sku映射信息Map<ShopId， Map<卖家sku, 映射信息>
        Map<String, Map<String, ListingInfoWithSkuMappingDTO>> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setShopIdList(shopIds);
            paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
            paramDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
            paramDTO.setIsExpire(false);
            listingInfoMap = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO)
                    .stream()
                    .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getShopId,
                            Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo,
                                    Function.identity(),
                                    // 过期时间最新优先
                                    (existing, replacement) -> replacement.getExpireTime().isAfter(existing.getExpireTime()) ? replacement : existing
                            )));

        } else {
            listingInfoMap = new HashMap<>();
        }
        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = allList
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.reportFbaMyiUnSuppressedInventoryToEntity(e,
                        shopMap.get(e.getRequestShopId()),
                        listingInfoMap.getOrDefault(e.getRequestShopId(), Collections.emptyMap()).get(e.getSku()),
                        e.getReportDataStartTime(),
                        e.getReportDataEndTime()
                ))
                .collect(Collectors.toList());
        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
        return allList.size();
    }
}
