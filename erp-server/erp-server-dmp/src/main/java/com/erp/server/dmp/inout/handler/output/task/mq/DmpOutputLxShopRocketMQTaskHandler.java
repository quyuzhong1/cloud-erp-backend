package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputLxShopRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpShopInfoEntity> dmpShopInfoEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_shop_info".equals(storageName)) {
                for (BaseEntity v : value) {
                    DmpShopInfoEntity dmpShopInfoEntity = (DmpShopInfoEntity) v;
                    dmpShopInfoEntityMap.put(dmpShopInfoEntity.getId(), dmpShopInfoEntity);
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
            if ("dmp_shop_info".equals(storageName)) {
                for (BaseEntity v : value) {
                    changeIds.add(v.getId());
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpShopInfoEntity dmpShopInfoEntity = dmpShopInfoEntityMap.get(changId);
            ShopEntity shop = this.convert(dmpShopInfoEntity, cfgOutputId);
            if (shop != null) {
                map.put(dmpShopInfoEntity.getId(), JSON.toJSONString(shop));
            }
        }
        return map;
    }

    /**
     * DMP数据转换推送DTO
     **/
    public ShopEntity convert(DmpShopInfoEntity dmpShopInfoEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpShopInfoEntity, cfgOutputId)) {
            return null;
        }
        ShopEntity shop = new ShopEntity();
        BeanUtils.copyProperties(dmpShopInfoEntity, shop);
        shop.setSid(Integer.parseInt(dmpShopInfoEntity.getThirdId()));
        shop.setPlatform(PlatformEnum.LINGXING.getName());
        shop.setSellerId(dmpShopInfoEntity.getPlatformId());
        shop.setShopName(dmpShopInfoEntity.getAccountUserName());
        shop.setCountry(dmpShopInfoEntity.getSite());
        shop.setMarketplaceId(dmpShopInfoEntity.getSubPlatformId());
        return shop;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("sid");
    }
}
