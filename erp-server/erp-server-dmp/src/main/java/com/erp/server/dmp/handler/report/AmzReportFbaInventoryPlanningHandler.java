package com.erp.server.dmp.handler.report;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.csv.ReportFbaInventoryPlanningCsvEntity;
import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
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
 * Listing报告处理服务
 *
 * @author Jim
 * @date 2024/1/24
 */
@Component("amzReportFbaInventoryPlanningHandler")
public class AmzReportFbaInventoryPlanningHandler extends AmzReportBusinessHandler {

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void businessHandler(AmzReportTaskEntity taskEntity, AmzReportInfoEntity reportInfo, JSONArray jsonArray) {
        List<ReportFbaInventoryPlanningCsvEntity> list = JSONUtil.toList(jsonArray, ReportFbaInventoryPlanningCsvEntity.class);

        // 查询当前店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(taskEntity.getShopId());
        // 查询SKU绑定的信息
        List<String> sellerSkuList = list
                .stream()
                .map(ReportFbaInventoryPlanningCsvEntity::getSku)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
            paramDTO.setShopIdList(Collections.singletonList(taskEntity.getShopId()));
            paramDTO.setMatchResult(true);
            listingInfoMap = skuMappingFeign.listingInfoWithSkuMappingList(paramDTO)
                    .stream()
                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));
        } else {
            listingInfoMap = new HashMap<>();
        }

        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = list
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.reportFbaInventoryPlanningToEntity(e,
                        shopInfoEntity,
                        listingInfoMap.get(e.getSku()),
                        reportInfo.getDataStartTime(),
                        reportInfo.getDataEndTime()
                ))
                .collect(Collectors.toList());

        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
    }
}
