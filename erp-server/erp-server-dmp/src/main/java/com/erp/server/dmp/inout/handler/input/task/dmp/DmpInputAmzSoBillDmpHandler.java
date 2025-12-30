package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.CfgTimezoneService;
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
 * <p>
 * 根据配送渠道解析ERP shopId
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzSoBillDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private CfgTimezoneService cfgTimezoneService;
    @Resource
    private CfgAppClientService appClientService;


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzErpShopParseDmpHandler afterConvertData 处理");
        // 解析所有数据来源店铺
        String shopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = appClientService.cacheAndFindShopAuth(shopId);
        // 渠道配置
        List<CfgTimezoneEntity> timeList = cfgTimezoneService.listAndCache();

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            for (TreeMap<String, Object> dataMap : entry.getValue()) {
                AmazonShopInfoDTO.ShopNameDTO shopNameDTO = parseShopByChannel(dataMap, shopInfoDTO, timeList);
                if (null != shopNameDTO){
                    dataMap.put("shopId", shopNameDTO.getShopId());
                    dataMap.put("shopName", shopNameDTO.getShopName());
                }
            }
        }
    }

    /**
     * 解析渠道对应店铺
     */
    private static AmazonShopInfoDTO.ShopNameDTO parseShopByChannel(Map<String, Object> dataMap, AmazonShopInfoDTO shopInfoDTO, List<CfgTimezoneEntity> timeList) {
        Map<String, AmazonShopInfoDTO.ShopNameDTO> marketplaceShopIdMap = shopInfoDTO.getMarketplaceShopIdMap();
        String salesChannel = dataMap.getOrDefault("salesChannel", "").toString();
        if (StringUtils.isBlank(salesChannel)) {
            ServiceException.runError("来源渠道为空:订单ID={}", dataMap.getOrDefault("platformCode", "").toString());
        }
        // 跳过FBA配送
        if (salesChannel.contains("Non-Amazon")){
            log.warn("FBA配送/多渠道订单发票信息跳过， data={}", dataMap);
            return null;
        }

        // 补充店铺信息
        CfgTimezoneEntity timeZoneEntity = timeList.stream()
                .filter(t -> t.getAndParseCondition().contains(salesChannel))
                .findFirst()
                .orElse(null);
        if (null == timeZoneEntity) {
            ServiceException.runError("未解析到对应渠道为空:渠道={}", salesChannel);
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(timeZoneEntity.getCountry());
        AmazonShopInfoDTO.ShopNameDTO shopNameDTO = marketplaceShopIdMap.get(marketplaceEnum.getMarketplaceId());
        if (null == shopNameDTO) {
            ServiceException.runError("未解析到渠道对应ERP店铺ID:渠道={},平台账号代号={}", salesChannel, shopInfoDTO.getPlatformShopCode());
        }
        return shopNameDTO;
    }
}

