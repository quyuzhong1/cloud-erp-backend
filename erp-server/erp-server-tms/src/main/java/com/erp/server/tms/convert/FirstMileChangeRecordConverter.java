package com.erp.server.tms.convert;

import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface FirstMileChangeRecordConverter {
    FirstMileChangeRecordConverter INSTANCE = Mappers.getMapper(FirstMileChangeRecordConverter.class);

    @Mappings({
            @Mapping(target = "category", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryEnum.BOXNO.getCode())"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.PRODUCT_WEIGHT.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newProductWeight"),
            @Mapping(target = "oldValue", source = "dto.productWeight"),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id")
    })
    FirstMileChangeRecordEntity changeProductWeightDtoToEntityConvert(FirstMileWeightAllocationDTO.ProductWeightDTO dto);
    List<FirstMileChangeRecordEntity> changeProductWeightDtoToEntityConvert(List<FirstMileWeightAllocationDTO.ProductWeightDTO> dtoValidList);

    @Mappings({
            @Mapping(target = "category", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryEnum.BOXNO.getCode())"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.CHARGED_WEIGHT.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newOutStockWeight"),
            @Mapping(target = "oldValue", source = "dto.outStockWeight"),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true)
    })
    FirstMileChangeRecordEntity changePackageOutStockWeightDtoToEntityConvert(FirstMileWeightAllocationDTO.PackageSizeDTO dto);
    @Mappings({
            @Mapping(target = "category", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryEnum.BOXNO.getCode())"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.BOX_LENGTH.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newBoxLength"),
            @Mapping(target = "oldValue", source = "dto.boxLength"),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true)
    })
    FirstMileChangeRecordEntity changePackageSizeLengthDtoToEntityConvert(FirstMileWeightAllocationDTO.PackageSizeDTO dto);
    @Mappings({
            @Mapping(target = "category", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryEnum.BOXNO.getCode())"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.BOX_WIDTH.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newBoxWidth"),
            @Mapping(target = "oldValue", source = "dto.boxWidth"),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true)
    })
    FirstMileChangeRecordEntity changePackageSizeWidthDtoToEntityConvert(FirstMileWeightAllocationDTO.PackageSizeDTO dto);
    @Mappings({
            @Mapping(target = "category", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryEnum.BOXNO.getCode())"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.BOX_HEIGHT.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newBoxHeight"),
            @Mapping(target = "oldValue", source = "dto.boxHeight"),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true)
    })
    FirstMileChangeRecordEntity changePackageSizeHeightDtoToEntityConvert(FirstMileWeightAllocationDTO.PackageSizeDTO dto);
    @Mappings({
            @Mapping(target = "category", source = "dto.feeType"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.MID_PERIOD_TRANSIT_COST.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newMidPeriodTransitCost"),
            @Mapping(target = "oldValue", source = "dto.midPeriodTransitCost"),
            @Mapping(target = "reportPeriod", source = "dto.reportPeriodMonth"),
            @Mapping(target = "reportPeriodId", source = "dto.reportPeriodId"),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILECOST.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true),
            @Mapping(target = "boxId", ignore = true)
    })
    FirstMileChangeRecordEntity changeCostMidPeriodTransitDtoToEntityConvert(FirstMileCostAllocationDTO.CostAllocationDTO dto);
    @Mappings({
            @Mapping(target = "category", source = "dto.feeType"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.CURRENT_PERIOD_ALLOCATED_COST.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newCurrentPeriodAllocatedCost"),
            @Mapping(target = "oldValue", source = "dto.currentPeriodAllocatedCost"),
            @Mapping(target = "reportPeriod", source = "dto.reportPeriodMonth"),
            @Mapping(target = "reportPeriodId", source = "dto.reportPeriodId"),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILECOST.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true),
            @Mapping(target = "boxId", ignore = true)
    })
    FirstMileChangeRecordEntity changeCostCurrentPeriodAllocatedDtoToEntityConvert(FirstMileCostAllocationDTO.CostAllocationDTO dto);
    @Mappings({
            @Mapping(target = "category", source = "dto.feeType"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.END_PERIOD_TRANSIT_COST.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newEndPeriodTransitCost"),
            @Mapping(target = "oldValue", source = "dto.endPeriodTransitCost"),
            @Mapping(target = "reportPeriod", source = "dto.reportPeriodMonth"),
            @Mapping(target = "reportPeriodId", source = "dto.reportPeriodId"),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILECOST.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true),
            @Mapping(target = "boxId", ignore = true)
    })
    FirstMileChangeRecordEntity changeCostEndPeriodTransitDtoToEntityConvert(FirstMileCostAllocationDTO.CostAllocationDTO dto);
    @Mappings({
            @Mapping(target = "category", source = "dto.feeType"),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.END_PERIOD_ESTIMATED_COST.getCode())"),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "deliveryCode", source = "dto.sourceCode"),
            @Mapping(target = "deliveryId", source = "dto.sourceId"),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "dto.newEndPeriodEstimatedCost"),
            @Mapping(target = "oldValue", source = "dto.endPeriodEstimatedCost"),
            @Mapping(target = "reportPeriod", source = "dto.reportPeriodMonth"),
            @Mapping(target = "reportPeriodId", source = "dto.reportPeriodId"),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.FIRSTMILECOST.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "sourceId", source = "dto.id"),
            @Mapping(target = "changeRange", ignore = true),
            @Mapping(target = "boxId", ignore = true)
    })
    FirstMileChangeRecordEntity changeCostEndPeriodEstimatedDtoToEntityConvert(FirstMileCostAllocationDTO.CostAllocationDTO dto);
}
