package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.MapUtil;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.FbaInventoryReservedEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaInventoryPlanningMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFbaMyiAllInventoryMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportInventoryCombineMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportReservedMongoDTO;
import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ReportHandleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 亚马逊报告计划表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Slf4j
@Service
public class ReportHandleServiceImpl implements ReportHandleService {

    @Resource
    private MongoService mongoService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void combineInventory(ReportInventoryCombineMongoDTO combineInventoryDTO,
                                 List<ReportFbaMyiAllInventoryMongoDTO> fbaMyiAllInventoryMongoDTOList,
                                 List<ReportReservedMongoDTO> reportReservedMongoDTOList,
                                 List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList
    ) {
        // 报告组合状态: 0=未组合，1=可组合, 2=已组合, 必须是可组合的记录
        if (1 != combineInventoryDTO.getCombineStatus()){
            return;
        }
        // 修改记录为已组合
        // 修改数据
        combineInventoryDTO.setCombineStatus(2);
        MapUtil mapUtil = JSONUtil.toBean(JSONUtil.toJsonStr(combineInventoryDTO), MapUtil.class);
        ReportInventoryCombineMongoDTO queryCombineInventoryDTO = new ReportInventoryCombineMongoDTO(combineInventoryDTO.getDataStartTime(), combineInventoryDTO.getDataEndTime(), combineInventoryDTO.getMarketplaceIds());
        mongoService.updateMongoData(queryCombineInventoryDTO, mapUtil, MongoTableNameContant.REPORT_AMAZON_COMBINE_INVENTORY, ReportInventoryCombineMongoDTO.class);

        // 查询当前店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(combineInventoryDTO.getShopId());
        // 查询SKU绑定的信息
        List<String> sellerSkuList = fbaMyiAllInventoryMongoDTOList
                .stream()
                .map(ReportFbaMyiAllInventoryMongoDTO::getSku)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ListingInfoEntity> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)){
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setMatchResult(true);
            listingInfoMap = omsListingInfoFeign.list(paramDTO)
                    .stream()
                    .collect(Collectors.toMap(ListingInfoEntity::getSkuNo, Function.identity()));
        } else {
            listingInfoMap = new HashMap<>();
        }

        // 转换
        Map<String, ReportFbaMyiAllInventoryMongoDTO> myiAllInventoryMap = fbaMyiAllInventoryMongoDTOList
                .stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}_{}", e.getAsin(), e.getFnsku(), e.getSku()), Function.identity()));

        Map<String, ReportReservedMongoDTO> reservedMap = reportReservedMongoDTOList
                .stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}_{}", e.getAsin(), e.getFnsku(), e.getSku()), Function.identity()));

        Map<String, ReportFbaInventoryPlanningMongoDTO> planningMap = planningMongoDTOList
                .stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}_{}", e.getAsin(), e.getFnsku(), e.getSku()), Function.identity()));

        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = myiAllInventoryMap
                .entrySet()
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.mergeToFbaInventoryEntity(combineInventoryDTO,
                        e.getValue(),
                        reservedMap.get(e.getKey()),
                        planningMap.get(e.getKey()),
                        shopInfoEntity,
                        listingInfoMap.get(e.getValue().getSku())))
                .collect(Collectors.toList());
        // TODO 防止低时间数据修改校验？

        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
    }

    // TODO 转换切换mapstruct接口

}
