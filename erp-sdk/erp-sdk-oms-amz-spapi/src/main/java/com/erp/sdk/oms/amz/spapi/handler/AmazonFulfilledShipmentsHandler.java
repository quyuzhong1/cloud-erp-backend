package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractSoOutStockHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.sdk.oms.amz.spapi.convert.SdkSoOutStockConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFulfilledShipmentsMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 亚马逊物流销售报告处理器
 *
 * @Author Jim
 * {@Date} 2024/03/06
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.SO_OUT_STOCK)
public class AmazonFulfilledShipmentsHandler extends AbstractSoOutStockHandler<PlatformAmazonFulfilledShipmentsDTO, PlatformSoOutStockDTO> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformAmazonFulfilledShipmentsDTO> download(JobTaskDTO data) {
        // 亚马逊商品(从已下载的获取)
        List<?> genericDataList = data.getSourceList();
        if (CollectionUtils.isEmpty(genericDataList)) {
            return Collections.emptyList();
        }

        Object sourceData = data.getSourceList().stream().findFirst().orElse(null);
        if (sourceData instanceof PlatformAmazonFulfilledShipmentsDTO){
            return (List<PlatformAmazonFulfilledShipmentsDTO>) genericDataList;
        }
        if (!(sourceData instanceof ReportFulfilledShipmentsMongoDTO)) {
            throw new ServiceException(" 亚马逊物流销售:sourceData类型异常:error=" + genericDataList.getClass().toGenericString());
        }
        List<ReportFulfilledShipmentsMongoDTO> sourceDataList = (List<ReportFulfilledShipmentsMongoDTO>) genericDataList;

        // 返回下载源数据
        return sourceDataList.stream()
                .map(e -> SdkSoOutStockConverter.INSTANCE.sourceDtoToOutStockDto(e,
                        e.getReportId(),
                        e.getShopId(),
                        StrUtil.format("{}_{}_{}", e.getAmazonOrderId(), e.convertShipmentDate(), e.getShopId()),
                        AmazonHandleStatusEnum.NONE.getCode(),
                        CleanStatusEnum.UNCLEAN.getCode(),
                        e.getSalesChannel()
                ))
                .collect(Collectors.toList());
    }


    @Override
    public List<PlatformSoOutStockDTO> convert(List<PlatformAmazonFulfilledShipmentsDTO> sourceDataList) {
        if (CollectionUtils.isEmpty(sourceDataList)){
            return Collections.emptyList();
        }
        return sourceDataList.stream()
                .map(e-> SdkSoOutStockConverter.INSTANCE.amazonConvertDTO(
                        e.getAmazonOrderId(),
                        e.getShopId(),
                        e.getUniqueId(),
                        Collections.singletonList(e),
                        e.getWarehouseId(),
                        e.getWarehouseName(),
                        e.getFulfillmentCenterId(),
                        e.getSalesChannel()
                )).collect(Collectors.toList());

        // 亚马逊物流销售报告转为发送mq数据
        // 根据订单ID分组
//        Map<String, List<PlatformAmazonFulfilledShipmentsDTO>> groupMap = sourceDataList
//                .stream()
//                .collect(Collectors.groupingBy(PlatformAmazonFulfilledShipmentsDTO::getGroupId));
//
//        // 包含数据过滤数据 数据转换 数据合并拆分等操作
//        return groupMap.entrySet().stream()
//                // 组装
//                .map(e -> SdkSoOutStockConverter.INSTANCE.amazonConvertDTO(
//                        e.getValue().get(0).getAmazonOrderId(),
//                        e.getValue().get(0).getShopId(),
//                        e.getKey(),
//                        e.getValue()
//                ))
//                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}
