package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.sdk.oms.amz.spapi.model.finances.*;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.CfgTimezoneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
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
public class DmpInputAmzRefundDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private CfgAppClientService appClientService;
    @Resource
    private CfgTimezoneService cfgTimezoneService;


    @Override
    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
        Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertDataMap = super.convertData(dmpInputMongoEntityList);
        if (convertDataMap.isEmpty()) {
            return convertDataMap;
        }
        // 解析所有数据来源店铺
        String shopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = appClientService.cacheAndFindShopAuth(shopId);
        // 渠道配置
        List<CfgTimezoneEntity> timeList = cfgTimezoneService.listAndCache();


        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : convertDataMap.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMap = entry.getValue();
            if (CollectionUtils.isEmpty(dmpDataMap)) {
                continue;
            }
            TreeMap<String, Object> sourceMap = dmpDataMap.get(0);
            Object shopIdObj = sourceMap.get("nextLevelId");
            if (null == shopIdObj) {
                String msg = StrUtil.format("未找到shopId:takId={}", dmpInputTaskEntity.getId());
                ServiceException.runError(msg);
            }

            List<TreeMap<String, Object>> refundDataList = new LinkedList<>();
            for (TreeMap<String, Object> treeMap : dmpDataMap) {
                // 退货数据
                Object refundEventListObj = treeMap.get("refundEventList");
                if (null == refundEventListObj) {
                    continue;
                }

                List<Map<String, Object>> refundEventList = (List<Map<String, Object>>) refundEventListObj;
                if (CollectionUtils.isEmpty(refundEventList)) {
                    continue;
                }
                // marketplaceName -> Amazon.com
                for (Map<String, Object> dataMap : refundEventList) {
                    AmazonShopInfoDTO.ShopNameDTO shopNameDTO = parseShopByChannel(dataMap, shopInfoDTO, timeList);
                    if (null == shopNameDTO) {
                        dataMap.put("shopId", shopInfoDTO.getId());
                        dataMap.put("shopName", shopInfoDTO.getName());
                    } else {
                        dataMap.put("shopId", shopNameDTO.getShopId());
                        dataMap.put("shopName", shopNameDTO.getShopName());
                    }
                    ShipmentEvent shipmentEvent = JSONUtil.toBean(JSONUtil.toJsonStr(dataMap), ShipmentEvent.class);
                    if (null != shipmentEvent) {
                        // 计算退款总金额:
                        convertAllAmount(dataMap, shipmentEvent);
                    }
                    TreeMap<String, Object> resultTreeMap = new TreeMap<>(dataMap);
                    refundDataList.add(resultTreeMap);
                }
            }
            // 修改内容(打平退款信息)
            entry.setValue(refundDataList);
        }

        return convertDataMap;
    }

    /**
     * 计算总金额
     */
    private void convertAllAmount(Map<String, Object> treeMap, ShipmentEvent shipmentEvent) {
        // 币种
        String currencyCode = "";
        // 数量
        BigDecimal allAmount = BigDecimal.ZERO;

        ShipmentItemList shipmentItemAdjustmentList = shipmentEvent.getShipmentItemAdjustmentList();
        if (CollectionUtils.isEmpty(shipmentItemAdjustmentList)) {
            return;
        }
        for (ShipmentItem shipmentItem : shipmentItemAdjustmentList) {
            ChargeComponentList itemChargeAdjustmentList = shipmentItem.getItemChargeAdjustmentList();
            if (CollectionUtils.isNotEmpty(itemChargeAdjustmentList)) {
                for (ChargeComponent chargeComponent : itemChargeAdjustmentList) {
                    Currency chargeAmount = chargeComponent.getChargeAmount();
                    if (null != chargeAmount) {
                        if (StringUtils.isBlank(currencyCode)) {
                            currencyCode = chargeAmount.getCurrencyCode();
                        }
                        allAmount = allAmount.add(chargeAmount.getCurrencyAmount());
                    }
                }
            }
            PromotionList promotionAdjustmentList = shipmentItem.getPromotionAdjustmentList();
            if (CollectionUtils.isNotEmpty(promotionAdjustmentList)) {
                for (Promotion promotion : promotionAdjustmentList) {
                    Currency promotionAmount = promotion.getPromotionAmount();
                    if (null != promotionAmount) {
                        if (StringUtils.isBlank(currencyCode)) {
                            currencyCode = promotionAmount.getCurrencyCode();
                        }
                        allAmount = allAmount.add(promotionAmount.getCurrencyAmount());
                    }
                }
            }
        }
        treeMap.put("currencyCode", currencyCode);
        // 取绝对值
        treeMap.put("allAmount", allAmount.abs());
    }

    /**
     * 解析渠道对应店铺
     */
    private static AmazonShopInfoDTO.ShopNameDTO parseShopByChannel(Map<String, Object> dataMap, AmazonShopInfoDTO shopInfoDTO, List<CfgTimezoneEntity> timeList) {
        Map<String, AmazonShopInfoDTO.ShopNameDTO> marketplaceShopIdMap = shopInfoDTO.getMarketplaceShopIdMap();
        String marketplaceName = dataMap.getOrDefault("marketplaceName", "").toString();
        if (StringUtils.isNotBlank(marketplaceName)) {
            // 补充店铺信息
            CfgTimezoneEntity timeZoneEntity = timeList.stream()
                    .filter(t -> t.getAndParseCondition().contains(marketplaceName))
                    .findFirst()
                    .orElse(null);
            if (null != timeZoneEntity) {
                return marketplaceShopIdMap.get(timeZoneEntity.getCountry());
            }
        }
        return null;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
    }

}
