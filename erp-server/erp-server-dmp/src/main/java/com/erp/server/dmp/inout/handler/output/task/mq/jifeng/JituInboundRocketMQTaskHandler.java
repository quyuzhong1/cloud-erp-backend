package com.erp.server.dmp.inout.handler.output.task.mq.jifeng;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.JituInboundReturnDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.erp.server.dmp.service.DmpThirdInboundService;
import com.taobao.api.request.FenxiaoOrderRemarkUpdateRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class JituInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private DmpThirdInboundService dmpThirdInboundService;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpThirdInboundEntity> dmpThirdInboundEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_inbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpThirdInboundEntity dmpThirdInboundEntity = (DmpThirdInboundEntity) v;
                        dmpThirdInboundEntityMap.put(dmpThirdInboundEntity.getId(), dmpThirdInboundEntity);
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
                if ("dmp_third_inbound".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            DmpThirdInboundEntity dmpThirdInboundEntity = dmpThirdInboundEntityMap.get(changId);
            PlatformInboundDTO platformInboundDTO = this.convert(dmpThirdInboundEntity, cfgOutputId);
            if (platformInboundDTO != null) {
                map.put(dmpThirdInboundEntity.getId(), JSON.toJSONString(platformInboundDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformInboundDTO convert(DmpThirdInboundEntity dmpThirdInboundEntity, String cfgOutputId) {
        if (this.validateDataBlack(dmpThirdInboundEntity, cfgOutputId)) {
            return null;
        }
        if (!"1".equals(dmpThirdInboundEntity.getReceivingStatus())) {
            return null;
        }
        PlatformInboundDTO platformInboundDTO = BeanUtil.copyProperties(dmpThirdInboundEntity, PlatformInboundDTO.class);
        String sourcePlatform = dmpThirdInboundEntity.getSourcePlatform();
        platformInboundDTO.setPlatform(sourcePlatform);
        platformInboundDTO.setProvider(sourcePlatform);
        platformInboundDTO.setReceivingStatus(this.convertStatus(dmpThirdInboundEntity.getReceivingStatus()));
        List<Receiving> receivingDataList = new ArrayList<>();

        //获取订单详情数据
        List<DmpThirdInboundEntity> entityList = dmpThirdInboundService.listBySourceCode(dmpThirdInboundEntity.getSourceCode());
        List<String> detailJsonList = entityList.stream().filter(e -> dmpThirdInboundEntity.getSourcePlatform().equals(e.getSourcePlatform())).map(DmpThirdInboundEntity::getDetailListJson).filter(Objects::nonNull).collect(Collectors.toList());
        //批量转换detailJsonList为List<JituInboundReturnDTO.Item>
        detailJsonList.forEach(detailJson -> {
                List<JituInboundReturnDTO.Item> items = JSON.parseArray(detailJson, JituInboundReturnDTO.Item.class);
            for (JituInboundReturnDTO.Item item : items) {
                    if (!item.getInventoryType().equals("ZP")) {
                        continue;
                    }
                    LocalDateTime operateTime = item.getOperateTime();
                    String lineNo = item.getLineNo();
                    for (JituInboundReturnDTO.Item.Batche batch : item.getBatches()) {
                        Receiving receiving = new Receiving();
                        platformInboundDTO.setDownloadTime(LocalDateTime.now());
                        receiving.setProductSku(item.getItemCode());
                        receiving.setReceiveQty(batch.getActualQty());
                        receiving.setReceiveTime(operateTime);
                        receiving.setThirdId(lineNo);
                        receivingDataList.add(receiving);
                    }
                }
        });
        platformInboundDTO.setReceivingDataList(receivingDataList);
        if (CollUtil.isNotEmpty(receivingDataList)) {
            platformInboundDTO.setHasReceivedData(true);
        }
        this.groupBySku(platformInboundDTO);

        return platformInboundDTO;
    }

    private void groupBySku(PlatformInboundDTO dto) {
        Map<String, Integer> receivedQuantityMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(Receiving::getProductSku, Collectors.summingInt(Receiving::getReceiveQty)));

        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
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
        return Arrays.asList("receivingCode");
    }
}
