package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.sdk.oms.amz.spapi.convert.SdkSoOutStockConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFulfilledShipmentsMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.AmazonDownloadService;
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
        Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> convertInputMongoEntityListMaps = dmpRequest.getConvertInputMongoEntityListMaps();
        Map<String, ReportFulfilledShipmentsMongoDTO> mongoEntityMap = new HashMap<>();

        for (Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> dmpCfgInputConvertEntityListEntry : convertInputMongoEntityListMaps.entrySet()) {
            List<Map<String, Object>> value = dmpCfgInputConvertEntityListEntry.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String convertClass = dmpCfgInputConvertEntityListEntry.getKey().getConvertClass();
                if ("DmpInputAmzSoOutStockMongoHandler".equals(convertClass)) {
                    for (Map<String, Object> objectMap : value) {
                        ReportFulfilledShipmentsMongoDTO mongoDTO = JSON.parseObject(JSON.toJSONString(objectMap), ReportFulfilledShipmentsMongoDTO.class);
                        mongoEntityMap.put(mongoDTO.getId(), mongoDTO);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputMongoBaseEntityListMaps = dmpRequest.getChangeConvertInputMongoEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<Map<String, Object>>> changeConvertInputDmpBaseEntityListMap : changeConvertInputMongoBaseEntityListMaps.entrySet()) {
            List<Map<String, Object>> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String convertClass = changeConvertInputDmpBaseEntityListMap.getKey().getConvertClass();
                if ("DmpInputAmzSoOutStockMongoHandler".equals(convertClass)) {
                    for (Map<String, Object> objectMap : value) {
                        ReportFulfilledShipmentsMongoDTO mongoDTO = JSON.parseObject(JSON.toJSONString(objectMap), ReportFulfilledShipmentsMongoDTO.class);
                        changeIds.add(mongoDTO.getId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        List<ReportFulfilledShipmentsMongoDTO> changeList = mongoEntityMap.entrySet().stream()
                .filter(e -> changeIds.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(changeList)) {
            return Collections.emptyMap();
        }
        // 填充其他信息
        List<ReportFulfilledShipmentsMongoDTO> dataList = amazonDownloadService.reportFulfillmentFillData(changeList);

        for (ReportFulfilledShipmentsMongoDTO mongoDTO : dataList) {
            PlatformAmazonFulfilledShipmentsDTO itemDTO = this.convert(mongoDTO, cfgOutputId);
            if (itemDTO != null) {
                map.put(mongoDTO.getId(), JSON.toJSONString(itemDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    private PlatformAmazonFulfilledShipmentsDTO convert(ReportFulfilledShipmentsMongoDTO mongoDTO, String cfgOutputId) {
        if (this.validateDataBlack(mongoDTO, cfgOutputId)) {
            return null;
        }
        return SdkSoOutStockConverter.INSTANCE.sourceDtoToOutStockDto(mongoDTO,
                mongoDTO.getReportId(),
                mongoDTO.getShopId(),
                StrUtil.format("{}_{}_{}", mongoDTO.getAmazonOrderId(), mongoDTO.convertShipmentDate(), mongoDTO.getShopId()),
                AmazonHandleStatusEnum.NONE.getCode(),
                CleanStatusEnum.UNCLEAN.getCode()
        );
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Collections.singletonList("shipmentItemId");
    }

}
