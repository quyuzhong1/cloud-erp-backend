package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxCommonSkuDmpHandler extends DmpInputDoNextDmpHandler {
    @Resource
    private ThirdShopService thirdShopService;
    @Resource
    private ThirdMappingService thirdMappingService;


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputLxCommonSkuDmpHandler 处理完成: taskId={}", dmpInputTaskEntity.getId());

        // 店铺和映射
        List<ThirdShopEntity> shopList = thirdShopService.lambdaQuery()
                .eq(ThirdShopEntity::getSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
                .list();
        List<ThirdMappingEntity> mappingList = thirdMappingService.lambdaQuery()
                .eq(ThirdMappingEntity::getType, "shop")
                .eq(ThirdMappingEntity::getThirdSysType, DmpBasicSystemCodeEnum.LING_XING.getCode())
                .list();

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                // 店铺ID
                String storeId = dmpDataMap.getOrDefault("storeId", "").toString();
                // 校验和获取ERP店铺
                ThirdMappingEntity mappingEntity = checkAndGetErpShopId(shopList, mappingList, storeId);
                dmpDataMap.put("nextLevelId", mappingEntity.getSysId());
            }
        }
    }


    /**
     * 校验和获取ERP店铺
     */
    private ThirdMappingEntity checkAndGetErpShopId(List<ThirdShopEntity> shopList, List<ThirdMappingEntity> mappingList, String storeId) {
        ThirdShopEntity thirdShopEntity = shopList.stream().filter(e -> e.getCode().equalsIgnoreCase(storeId)).findFirst().orElse(null);
        if (null == thirdShopEntity) {
            ServiceException.runError("未找到领星店铺对应映射记录:领星店铺ID=" + storeId);
        }
        ThirdMappingEntity mappingEntity = mappingList.stream().filter(e -> e.getThirdInfoId().equalsIgnoreCase(thirdShopEntity.getId())).findFirst().orElse(null);
        if (null == mappingEntity) {
            ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
        }
        if (StringUtils.isBlank(mappingEntity.getSysId())) {
            ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
        }
        return mappingEntity;
    }

}
