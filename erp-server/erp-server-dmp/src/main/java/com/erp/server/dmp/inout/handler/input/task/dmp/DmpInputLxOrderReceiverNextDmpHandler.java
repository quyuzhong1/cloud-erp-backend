package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.LingxingPlatformCodeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.sdk.oms.temu.dto.TemuShippingDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxOrderReceiverNextDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        //如果是TEMU半托管并且是平台仓的数据，查询TEMU的地址信息
        String deliveryTypeStr = dmpInputMongoEntity.getOrDefault("delivery_type", "").toString();
        Boolean isPlatformWarehouseOrder = convertIsPlatformWarehouseOrder(deliveryTypeStr);
        Boolean isTemu = false;
        String platformCode = "";

        // 平台原始信息
        Object platformInfoListObj = dmpInputMongoEntity.get("platform_info");
        if (null != platformInfoListObj) {
            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(platformInfoListObj));
            if (CollectionUtils.isNotEmpty(jsonArray)) {
                Object platformInfoObjIndex1 = jsonArray.get(0);
                Map<String, Object> platformInfoMap = (JSONObject) platformInfoObjIndex1;
                // 平台订单
                platformCode = platformInfoMap.getOrDefault("platform_order_no", "").toString();
                // 解析来源平台
                String platformCodeStr = platformInfoMap.getOrDefault("platform_code", "").toString();
                isTemu = platformCodeStr.equals(LingxingPlatformCodeEnum.TEMU_FBP.getCode());
            }
        }
        // 校验和获取ERP店铺
        if(isTemu&&!isPlatformWarehouseOrder && StringUtils.isNotBlank(platformCode)){
            List<ParamData> paramDataList = new ArrayList<>();
            List<Map<String, Object>> shipmentMongoData = new ArrayList<>();
            paramDataList.add(new ParamData("orderId", "orderId", PannoEnum.EQ, platformCode));
            shipmentMongoData = mongoService.findMongoData(paramDataList,"lingxing_orderAddress_data" );
            if (CollectionUtils.isEmpty(shipmentMongoData)) {
                //如果已发货，不更新订单异常
                Map<String, Object> map = new HashMap<>();
                map.put("is_update_error",true);
                return Collections.singletonList(map);
            }
            Map<String, Object> map = shipmentMongoData.get(0);
            if(!map.containsKey("orderId") || Objects.isNull(map.get("orderId"))){
                Map<String, Object> map1 = new HashMap<>();
                if(map.containsKey("is_update_error") && Objects.nonNull(map.get("is_update_error")) && !(Boolean) map.get("is_update_error")){
                    map.put("is_update_error",false);
                }else{
                    //如果已发货，不更新订单异常
                    map1.put("is_update_error",true);
                }
                return Collections.singletonList(map1);
            }
            TemuShippingDTO temuShippingDTO = JSON.parseObject(JSON.toJSONString(shipmentMongoData.get(0)), TemuShippingDTO.class);
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("address_line1",temuShippingDTO.getAddressLine1());
            addressMap.put("address_line2",temuShippingDTO.getAddressLine2());
            addressMap.put("receiver_mobile",temuShippingDTO.getMobile());
            addressMap.put("receiver_name",StringUtils.isNotBlank(temuShippingDTO.getReceiptName())?temuShippingDTO.getReceiptName():temuShippingDTO.getReceiptAdditionalName());
            addressMap.put("postal_code",temuShippingDTO.getPostCode());
            addressMap.put("city",temuShippingDTO.getRegionName3());
            addressMap.put("district",temuShippingDTO.getRegionName4());
            addressMap.put("fullAddress",temuShippingDTO.getAddressLineAll());
            addressMap.put("state_or_region",temuShippingDTO.getRegionName2());
            addressMap.put("receiver_country_code",getCountryByEnName(temuShippingDTO.getRegionName1()));
            addressMap.put("receiver_tel",temuShippingDTO.getMobile());
            addressMap.put("email",temuShippingDTO.getMail());
            return Collections.singletonList(addressMap);
        }else{
            Object addressObj = dmpInputMongoEntity.get("address_info");
            if (null == addressObj) {
                return Collections.emptyList();
            }
            Map<String, Object> addressMap = (Map<String, Object>) addressObj;
            return Collections.singletonList(addressMap);
        }

    }

    private String getCountryByEnName(String enName){
        List<DictCountryEntity> dictCountryEntityList = FeignQuery.create(DictCountryEntity.class)
                .eq(DictCountryEntity::getNameEn, enName)
                .list();
        if(CollectionUtils.isEmpty(dictCountryEntityList)){
            log.error("Temu半托管查询地址，未找到ERP国家映射关系,国家英文名={}", enName);
            return null;
        }
        return dictCountryEntityList.get(0).getId();
    }

    /**
     * 转换是否是平台仓
     */
    private Boolean convertIsPlatformWarehouseOrder(String deliveryTypeStr) {
        // 对应ERP的订单发货类型：
        // 1 混合方式【中转值，最终会转为2或3】
        // 2 自发货：对应ERP自发货订单
        // 3 平台发货【指由平台仓库自动完成履约的订单，如Walmart的WFS订单】：对应ERP平台仓发货订单
        if ("2".equalsIgnoreCase(deliveryTypeStr)) {
            return false;
        }
        if ("3".equalsIgnoreCase(deliveryTypeStr)) {
            return true;
        }
        // 无法判断为null, 黑名单拦截
        return false;
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
        if (StringUtils.isBlank(mappingEntity.getSysId())){
            ServiceException.runError("领星店铺未映射:领星店铺ID=" + storeId);
        }
        return mappingEntity;
    }
}
