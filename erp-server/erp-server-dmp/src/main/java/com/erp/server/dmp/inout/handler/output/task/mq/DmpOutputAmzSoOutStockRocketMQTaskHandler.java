package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpAmzSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.sdk.oms.amz.spapi.convert.SdkSoOutStockConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFulfilledShipmentsMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.AmazonDownloadService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class DmpOutputAmzSoOutStockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Lazy
    @Resource
    private AmazonDownloadService amazonDownloadService;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpAmzSoOutstockDetailEntity> dmpMainEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_amz_so_outstock_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpAmzSoOutstockDetailEntity dmpMainEntity = (DmpAmzSoOutstockDetailEntity) v;
                        dmpMainEntityMap.put(dmpMainEntity.getId(), dmpMainEntity);
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
                if ("dmp_amz_so_outstock_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        List<DmpAmzSoOutstockDetailEntity> changeList = dmpMainEntityMap.entrySet().stream()
                .filter(e -> changeIds.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(changeList)) {
            return Collections.emptyMap();
        }
        List<ReportFulfilledShipmentsMongoDTO> list = dmpMainEntityMap.values().stream().map(e -> {
            ReportFulfilledShipmentsMongoDTO dto = new ReportFulfilledShipmentsMongoDTO();
            BeanUtils.copyProperties(e, dto);
            return dto;
        }).collect(Collectors.toList());

        // 填充其他信息
        List<ReportFulfilledShipmentsMongoDTO> dataList = amazonDownloadService.reportFulfillmentFillData(list);

        for (String changId : changeIds) {
            PlatformSoOutStockDTO orderDTO = this.convert(dmpMainEntityMap.get(changId),
                    dataList,
                    cfgOutputId);
            if (null != orderDTO) {
                map.put(changId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    private PlatformSoOutStockDTO convert(DmpAmzSoOutstockDetailEntity dmpEntity, List<ReportFulfilledShipmentsMongoDTO> dataList, String cfgOutputId) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        ReportFulfilledShipmentsMongoDTO mongoDTO = dataList.stream()
                .filter(e-> e.getShipmentItemId().equalsIgnoreCase(dmpEntity.getShipmentItemId()))
                .findFirst().orElse(null);
        if (null == mongoDTO){
            ServiceException.runError("亚马逊销售出库单填充数据失败:");
        }
        if (mongoDTO.hasMultiChannel()){
            // 多渠道订单不推送
            return null;
        }

        PlatformAmazonFulfilledShipmentsDTO fulfilledShipmentsDTO = SdkSoOutStockConverter.INSTANCE.sourceDtoToOutStockDto(mongoDTO,
                mongoDTO.getReportId(),
                mongoDTO.getShopId(),
                StrUtil.format("{}_{}_{}", mongoDTO.getAmazonOrderId(), mongoDTO.convertShipmentDate(), mongoDTO.getShopId()),
                AmazonHandleStatusEnum.NONE.getCode(),
                CleanStatusEnum.UNCLEAN.getCode()
        );
        PlatformSoOutStockDTO platformSoOutStockDTO = SdkSoOutStockConverter.INSTANCE.amazonConvertDTO(
                fulfilledShipmentsDTO.getAmazonOrderId(),
                fulfilledShipmentsDTO.getShopId(),
                fulfilledShipmentsDTO.getUniqueId(),
                Collections.singletonList(fulfilledShipmentsDTO),
                fulfilledShipmentsDTO.getWarehouseId(),
                fulfilledShipmentsDTO.getWarehouseName(),
                fulfilledShipmentsDTO.getFulfillmentCenterId());
        if (null == mongoDTO.getShopId()){
            // 解析不到对应店铺, 默认推送
            platformSoOutStockDTO.setShopId(dmpEntity.getRequestShopId());
        }
        platformSoOutStockDTO.setCountry(dmpEntity.getShipCountry());
        return platformSoOutStockDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("uniqueId");
    }

}
