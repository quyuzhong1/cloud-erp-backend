package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public abstract class DmpInputAmzReportReturnCommonDmpHandler extends DmpInputDbConvertDmpHandler {

    protected static final String NEXT_LEVEL_ID = "nextLevelId";
    protected static final String SHOP_ID = "shopId";
    protected static final String SHOP_NAME = "shopName";
    protected static final String RETURN_REQUEST_DATE = "returnRequestDate";
    protected static final String RETURN_DELIVERY_DATE = "returnDeliveryDate";
    protected static final String RETURN_TIME = "returnTime";
    protected static final String ORDER_ID = "orderId";
    protected static final String PLATFORM_SHOP_CODE = "platformShopCode";
    protected static final String STATUS = "status";
    protected static final String PLATFORM_CREATE_TIME = "platformCreateTime";
    protected static final String PLATFORM_UPDATE_TIME = "platformUpdateTime";

    protected static final String COUNTRY = "country";

    @Resource
    protected ShopInfoFeign shopInfoFeign;

    /**
     * 获取店铺信息
     */
    protected ShopInfoEntity checkShopInfo(TreeMap<String, Object> dmpDataMap, String shopId, List<SoB2cEntity> orderList, List<ShopInfoEntity> shopList) {
        String orderId = dmpDataMap.getOrDefault(ORDER_ID, "").toString();
        String platformShopCode = dmpDataMap.getOrDefault(PLATFORM_SHOP_CODE, "").toString();
        List<ShopInfoEntity> sameAccountShopInfo = shopList.stream().filter(e -> e.getPlatformShopCode().equalsIgnoreCase(platformShopCode)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sameAccountShopInfo)){
            return shopList.stream().filter(e->e.getId().equalsIgnoreCase(shopId)).findFirst().orElse(null);
        }
        List<String> sameAccountShopIds = sameAccountShopInfo.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());

        SoB2cEntity soB2cEntity = orderList.stream()
                .filter(e -> e.getPlatformCode().equalsIgnoreCase(orderId) && sameAccountShopIds.contains(e.getShopId()))
                .findFirst()
                .orElse(null);
        if (null == soB2cEntity){
            return shopList.stream().filter(e->e.getId().equalsIgnoreCase(shopId)).findFirst().orElse(null);
        } else {
            return shopList.stream().filter(e->e.getId().equalsIgnoreCase(soB2cEntity.getShopId())).findFirst().orElse(null);
        }
    }


    /**
     * 查询订单
     */
    protected List<SoB2cEntity> queryOrderList(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        List<String> orderIds = new ArrayList<>();
        // 来源账号一定唯一
        String platformShopCode = "";
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            for (TreeMap<String, Object> treeMap : entry.getValue()) {
                if (StringUtils.isBlank(platformShopCode)){
                    platformShopCode = treeMap.getOrDefault(PLATFORM_SHOP_CODE, "").toString();
                }
                String orderId = treeMap.getOrDefault(ORDER_ID, "").toString();
                if (StringUtils.isNotBlank(orderId)){
                    orderIds.add(orderId);
                }
            }
        }
        if (CollectionUtils.isEmpty(orderIds) || StringUtils.isBlank(platformShopCode)){
            return Collections.emptyList();
        }
        return FeignQuery.create(SoB2cEntity.class)
                .in(SoB2cEntity::getPlatformCode, orderIds)
                .list();
    }
}
