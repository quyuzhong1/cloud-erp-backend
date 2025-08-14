package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundDetailEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
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
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpThirdReturnInboundEntity dmpMainEntity = dmpMainEntityMap.get(changId);
            List<DmpThirdReturnInboundDetailEntity> dmpDetailEntityList = dmpDetailEntityMap.getOrDefault(changId, Collections.emptyList());
            PlatformReturnInstockDTO dto = this.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId);
            if (null != dto) {
                map.put(dmpMainEntity.getId(), JSON.toJSONString(dto));
            }
        }
        return map;
    }

    /**
     * 解析退货单入库数据
     **/
    public PlatformReturnInstockDTO convert(DmpThirdReturnInboundEntity dmpMainEntity, List<DmpThirdReturnInboundDetailEntity> dmpDetailList, String cfgOutputId) {
        if (this.validateDataBlack(dmpMainEntity, cfgOutputId)) {
            return null;
        }

        PlatformReturnInstockDTO dto = BeanUtil.copyProperties(dmpMainEntity, PlatformReturnInstockDTO.class);
        String sourcePlatform = dmpMainEntity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setPutawayTime(dmpMainEntity.getPutAwayTime());
        // 固定退货退款
        dto.setReturnType(ReturnTypeEnum.DEDUCTION.getCode());
        dto.setUniqueId(CharSequenceUtil.format("{}_{}", dmpMainEntity.getPlatformOrderNo(), dmpMainEntity.getAuthId()));
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
