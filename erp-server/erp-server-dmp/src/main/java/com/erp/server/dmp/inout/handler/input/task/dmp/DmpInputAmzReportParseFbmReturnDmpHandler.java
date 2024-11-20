package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class DmpInputAmzReportParseFbmReturnDmpHandler extends DmpInputDbConvertDmpHandler {

    public static final String DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final String NEXT_LEVEL_ID = "nextLevelId";
    public static final String SHOP_ID = "shopId";
    public static final String SHOP_NAME = "shopName";
    public static final String RETURN_REQUEST_DATE = "returnRequestDate";
    public static final String RETURN_TIME = "returnTime";
    public static final String ORDER_ID = "orderId";
    public static final String PLATFORM_SHOP_CODE = "platformShopCode";
    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportParseFbmReturnDmpHandler afterConvertData 处理");
        // 根据账号和订单ID查询源订单信息
        List<SoB2cEntity> orderList = queryOrderList(dmpInputDataDmpRelationMaps);
        // 查询授权店铺信息
        List<ShopInfoEntity> shopList = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null));


        for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            // 解析对应店铺(默认-请求的店铺)
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                String shopId = dmpDataMap.getOrDefault(NEXT_LEVEL_ID, "").toString();
                String shopName = "";
                ShopInfoEntity shopInfo = checkShopInfo(dmpDataMap, shopId, orderList, shopList);
                dmpDataMap.put(SHOP_ID, shopId);
                if (null != shopInfo){
                    shopName = shopInfo.getName();
                }
                dmpDataMap.put(SHOP_NAME, shopName);

                String returnDateStr = dmpDataMap.getOrDefault(RETURN_REQUEST_DATE, "").toString();
                LocalDateTime returnLocalDateTime = parseToTime(returnDateStr);
                dmpDataMap.put(RETURN_TIME, returnLocalDateTime);
            }
        }
    }

    /**
     * 获取店铺信息
     */
    private ShopInfoEntity checkShopInfo(TreeMap<String, Object> dmpDataMap, String shopId, List<SoB2cEntity> orderList, List<ShopInfoEntity> shopList) {
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
    private List<SoB2cEntity> queryOrderList(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
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


    public LocalDateTime parseToTime(String dateStr){
        if (StringUtils.isBlank(dateStr)){
            return null;
        }
        // 18-Nov-2024
        return LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.ENGLISH));
    }
}
