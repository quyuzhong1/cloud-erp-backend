package com.erp.server.dmp.inout.handler.output.task.mq.jifeng;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundDetailEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.jifeng.enums.JiFengEnums;
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
public class DaMaiReturnInstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

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

        if(Objects.isNull(dmpMainEntity.getPutAwayTime())){
            return null;
        }
        PlatformReturnInstockDTO dto = BeanUtil.copyProperties(dmpMainEntity, PlatformReturnInstockDTO.class);
        String sourcePlatform = dmpMainEntity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setPutawayTime(dmpMainEntity.getPutAwayTime());
        List<DmpThirdReturnInboundDetailEntity> filterDmpDetaiList = dmpDetailList.stream()
                .filter(e -> null != e.getRealQty() && e.getRealQty() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterDmpDetaiList)){
            log.warn("大卖仓退货找不到上架数量大于0的明细: {}", JSONUtil.toJsonStr(dmpMainEntity));
            return null;
        }
        // 明细
        List<PlatformReturnInstockDTO.Detail> detailList = filterDmpDetaiList.stream().map(this::convertDetail).collect(Collectors.toList());
        dto.setProductDetailList(detailList);
        return dto;
    }

    /**
     * 明细转换
     */
    private PlatformReturnInstockDTO.Detail convertDetail(DmpThirdReturnInboundDetailEntity detailEntity) {
        PlatformReturnInstockDTO.Detail detail = BeanUtil.copyProperties(detailEntity, PlatformReturnInstockDTO.Detail.class);
        detail.setThirdId(detailEntity.getThirdDetailId());
        return detail;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("platformReturnOrderNo");
    }
}
