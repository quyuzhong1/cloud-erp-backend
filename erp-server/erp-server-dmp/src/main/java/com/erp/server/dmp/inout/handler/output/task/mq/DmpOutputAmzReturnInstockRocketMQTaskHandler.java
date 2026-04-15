package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundDetailEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputAmzReturnInstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdReturnInboundEntity> dmpMainEntityMap = new HashMap<>();
        Map<String, List<DmpThirdReturnInboundDetailEntity>> dmpDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_return_inbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdReturnInboundEntity dmpMainEntity = (DmpThirdReturnInboundEntity) v;
                        dmpMainEntityMap.put(dmpMainEntity.getId(), dmpMainEntity);
                    }
                } else if ("dmp_third_return_inbound_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdReturnInboundDetailEntity dmpDetailEntity = (DmpThirdReturnInboundDetailEntity) v;
                        String mainId = dmpDetailEntity.getMainId();
                        List<DmpThirdReturnInboundDetailEntity> list = dmpDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpDetailEntity);
                        dmpDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_return_inbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_third_return_inbound_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdReturnInboundDetailEntity dmpSoReturnDetailEntity = (DmpThirdReturnInboundDetailEntity) v;
                        changeIds.add(dmpSoReturnDetailEntity.getMainId());
                    }
                }
            }
        }

        // 店铺信息
        List<ShopInfoEntity> shopList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.AMAZON.getCode())
                .ne(ShopInfoEntity::getPlatformShopCode, "")
                .list();
        Map<String, ShopInfoEntity> shopMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(shopList)) {
            shopMap = shopList.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpThirdReturnInboundEntity dmpMainEntity = dmpMainEntityMap.get(changId);
            List<DmpThirdReturnInboundDetailEntity> dmpDetailEntityList = dmpDetailEntityMap.getOrDefault(changId, Collections.emptyList());
            PlatformReturnInstockDTO dto = this.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId, shopMap.get(dmpMainEntity.getNextLevelId()));
            if (null != dto) {
                map.put(dmpMainEntity.getId(), JSON.toJSONString(dto));
            }
        }
        return map;
    }

    /**
     * 解析退货单入库数据
     **/
    public PlatformReturnInstockDTO convert(DmpThirdReturnInboundEntity dmpMainEntity, List<DmpThirdReturnInboundDetailEntity> dmpDetailList, String cfgOutputId, ShopInfoEntity shopInfoEntity) {
        if (this.validateDataBlack(dmpMainEntity, cfgOutputId)) {
            return null;
        }
        if (CollectionUtils.isEmpty(dmpDetailList)) {
            return null;
        }
        PlatformReturnInstockDTO dto = BeanUtil.copyProperties(dmpMainEntity, PlatformReturnInstockDTO.class);
        String sourcePlatform = dmpMainEntity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        LocalDateTime putAwayTime = dmpMainEntity.getPutAwayTime();
        if (null != shopInfoEntity && null != putAwayTime) {
            if (StringUtils.isNotBlank(shopInfoEntity.getReturnTimeZone())){
                // 转换为店铺所在时区的时间
                OffsetDateTime offsetDateTime = putAwayTime.atOffset(ZoneOffset.UTC);
                putAwayTime = offsetDateTime.atZoneSameInstant(ZoneId.of(shopInfoEntity.getReturnTimeZone())).toLocalDateTime();
            }
        }
        dto.setPutawayTime(putAwayTime);

        // 固定退货退款
        dto.setReturnType(ReturnTypeEnum.DEDUCTION.getCode());
        dto.setUniqueId(dmpDetailList.get(0).getThirdDetailId());
        // 明细
        List<PlatformReturnInstockDTO.Detail> detailList = dmpDetailList.stream().map(this::convertDetail).collect(Collectors.toList());
        dto.setProductDetailList(detailList);
        return dto;
    }

    /**
     * 明细转换
     */
    private PlatformReturnInstockDTO.Detail convertDetail(DmpThirdReturnInboundDetailEntity detailEntity) {
        return BeanUtil.copyProperties(detailEntity, PlatformReturnInstockDTO.Detail.class);
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }

    @Override
    public void getRetryPushSourceData(List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList,
                                       DmpOutputTaskRequest dmpOutputTaskRequest) {
        super.getRetryPushSourceData(dmpCfgInputConvertEntityList, dmpOutputTaskRequest);
    }
}
