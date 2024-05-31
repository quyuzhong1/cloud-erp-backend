//package com.erp.server.dmp.handler.report;
//
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONUtil;
//import com.common.business.constant.MongoTableNameContant;
//import com.common.business.enums.PlatformDictEnum;
//import com.erp.model.dmp.entity.AmzReportInfoEntity;
//import com.erp.model.dmp.entity.AmzReportTaskEntity;
//import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
//import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
//import com.erp.model.oms.dto.ListingInfoParamDTO;
//import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
//import com.erp.model.oms.entity.ShopInfoEntity;
//import com.erp.model.oms.enums.RuleTypeEnum;
//import com.erp.model.wms.entity.FbaInventoryEntity;
//import com.erp.rpc.oms.feign.ShopInfoFeign;
//import com.erp.rpc.oms.feign.SkuMappingFeign;
//import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
//import com.erp.sdk.oms.amz.spapi.csv.ReportFbaInventoryPlanningCsvEntity;
//import com.erp.sdk.oms.amz.spapi.dto.ReportFbaInventoryPlanningMongoDTO;
//import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
//import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
//import com.erp.server.dmp.handler.DmpMongoHandler;
//import com.erp.server.dmp.service.DmpMongoHandleTaskService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.CollectionUtils;
//
//import javax.annotation.Resource;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * 库存管理报告处理
// *
// * @author Jim
// * @date 2024/1/24
// */
//@Slf4j
//@Component("amzReportFbaInventoryPlanningHandler")
//public class AmzReportFbaInventoryPlanningHandler extends DmpMongoHandler {
//
//    @Resource
//    private ShopInfoFeign shopInfoFeign;
//    @Resource
//    private SkuMappingFeign skuMappingFeign;
//    @Resource
//    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
//    @Resource
//    private DmpMongoHandleTaskService dmpMongoHandleTaskService;
//
//
//    @Override
//    public Integer findAndFillDataOrHandle(DmpMongoHandleTaskEntity mongoHandleTaskEntity) {
//
//        // 任务每次处理数量
//        Integer handleCount = mongoHandleTaskEntity.getHandleCount();
//        // 指定的mongo表
//        String mongoTableName = MongoTableNameContant.DATA_REPORT_AMZ_FBA_INVENTORY_PLANNING;
//        List<ReportFbaInventoryPlanningMongoDTO> allList = dmpMongoHandleTaskService.findMongoData(mongoHandleTaskEntity.getLastId(), handleCount, mongoTableName, ReportFbaInventoryPlanningMongoDTO.class);
//        if (CollectionUtils.isEmpty(allList)) {
//            log.warn("库存管理报告处理处理结束：处理数据为空:handleType={}", mongoHandleTaskEntity.getHandleType());
//            return 0;
//        }
//        // 查询SKU绑定的信息
//        List<String> sellerSkuList = allList
//                .stream()
//                .map(ReportFbaInventoryPlanningMongoDTO::getSku)
//                .distinct()
//                .collect(Collectors.toList());
//
//        Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap;
//        if (!CollectionUtils.isEmpty(sellerSkuList)) {
//            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
//            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
//            paramDTO.setPlatformSkuNoList(sellerSkuList);
//            paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
//            paramDTO.setShopIdList(Collections.singletonList(taskEntity.getShopId()));
//            paramDTO.setMatchResult(true);
//            paramDTO.setIsExpire(false);
//            listingInfoMap = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO)
//                    .stream()
//                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));
//        } else {
//            listingInfoMap = new HashMap<>();
//        }
//
//        // 转换实体
//        List<FbaInventoryEntity> fbaInventoryEntityList = list
//                .stream()
//                .map(e -> DmpFbaInventoryConverter.INSTANCE.reportFbaInventoryPlanningToEntity(e,
//                        shopInfoEntity,
//                        listingInfoMap.get(e.getSku()),
//                        reportInfo.getDataStartTime(),
//                        reportInfo.getDataEndTime()
//                ))
//                .collect(Collectors.toList());
//
//        // 保存到WMS
//        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
//        return null;
//    }
//}
