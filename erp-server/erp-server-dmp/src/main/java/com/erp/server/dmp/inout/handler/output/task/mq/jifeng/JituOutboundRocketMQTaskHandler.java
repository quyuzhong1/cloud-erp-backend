package com.erp.server.dmp.inout.handler.output.task.mq.jifeng;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.JituOutboundReturnDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class JituOutboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdOutboundEntity> dmpThirdInboundEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_outbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdOutboundEntity DmpThirdOutboundEntity = (DmpThirdOutboundEntity) v;
                        dmpThirdInboundEntityMap.put(DmpThirdOutboundEntity.getId(), DmpThirdOutboundEntity);
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
                if ("dmp_third_outbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpThirdOutboundEntity DmpThirdOutboundEntity = dmpThirdInboundEntityMap.get(changId);
            PlatformOutboundDTO dto = this.convert(DmpThirdOutboundEntity, cfgOutputId);
            if (dto != null) {
                map.put(DmpThirdOutboundEntity.getId(), JSON.toJSONString(dto));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformOutboundDTO convert(DmpThirdOutboundEntity dmpThirdOutboundEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpThirdOutboundEntity, cfgOutputId)) {
            return null;
        }
        PlatformOutboundDTO dto = BeanUtil.copyProperties(dmpThirdOutboundEntity, PlatformOutboundDTO.class);
        String sourcePlatform = dmpThirdOutboundEntity.getSourcePlatform();
        dto.setPlatform(sourcePlatform);
        dto.setProvider(sourcePlatform);
        dto.setOutBoundTime(dmpThirdOutboundEntity.getDateShipping());
        dto.setUniqueId(dmpThirdOutboundEntity.getUniqueEncrypt());
        dto.setTrackNo(dmpThirdOutboundEntity.getTrackingNo());
        dto.setOrderStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        List<JituOutboundReturnDTO.Item> boxListDTOS = JSON.parseArray(dmpThirdOutboundEntity.getDetailListJson(), JituOutboundReturnDTO.Item.class);

        List<PlatformOutboundDTO.Receiving> receivingList = new ArrayList<>();

        for (JituOutboundReturnDTO.Item item : boxListDTOS) {
            if (!item.getInventoryType().equals("ZP")) {
                continue;
            }
            String itemCode = item.getItemCode();
            if (CollUtil.isNotEmpty(item.getBatches())) {
                for (JituOutboundReturnDTO.Item.Batche batch : item.getBatches()) {
                    PlatformOutboundDTO.Receiving receiving = new PlatformOutboundDTO.Receiving();
                    receiving.setProductSku(itemCode);
                    receiving.setActualQty(batch.getActualQty());
                    receiving.setBatchCode(batch.getBatchCode());
                    receivingList.add(receiving);
                }
            }else {
                PlatformOutboundDTO.Receiving receiving = new PlatformOutboundDTO.Receiving();
                receiving.setProductSku(itemCode);
                receiving.setActualQty(item.getActualQty());
                receivingList.add(receiving);
            }
        }
        dto.setReceivingDataList(receivingList);
        this.groupBySku(dto);
        return dto;
    }

    private void groupBySku(PlatformOutboundDTO dto) {
        Map<String, Integer> actualQtyMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(PlatformOutboundDTO.Receiving::getProductSku, Collectors.summingInt(PlatformOutboundDTO.Receiving::getActualQty)));

        List<PlatformOutboundDTO.Item> items = actualQtyMap.entrySet().stream()
                .map(entry -> new PlatformOutboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        dto.setItems(items);
    }

    private String convertStatus(String status) {
        if (status.equals("0")) {
            return OverseasInstockStatusEnum.SIGNED.getCode();
        } else if (status.equals("1")) {
            return OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode();
        }
        return null;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("referenceNo");
    }
}
