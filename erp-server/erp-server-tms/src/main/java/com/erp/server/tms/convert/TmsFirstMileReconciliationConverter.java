package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseChildDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 头程对账单
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface TmsFirstMileReconciliationConverter {

    TmsFirstMileReconciliationConverter INSTANCE = Mappers.getMapper(TmsFirstMileReconciliationConverter.class);


    @Mappings({
    })
    List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> convertDetailDTOList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDTOList);


    @Mappings({
            @Mapping(target = "shippingCost", source = "shippingCost"),
            @Mapping(target = "declareCost", source = "declareCost"),
            @Mapping(target = "otherCost", source = "otherCost"),
            @Mapping(target = "otherTaxCost", source = "otherTaxCost"),
    })
    TmsFirstMileReconciliationDetailDTO.UpdateDTO convertDetailDTO(TmsFirstMileReconciliationDetailDTO.ListDTO sourceDTO);

    /**
     * 转换更新到实体
     * @param updateDTO
     * @return
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "submitDate", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "reconciliationDate", ignore = true)
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "logisticsSupplierName", ignore = true)
    @Mapping(target = "logisticsSupplierId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "exchangeRate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "approveUserName", ignore = true)
    @Mapping(target = "approveUserId", ignore = true)
    @Mapping(target = "approveStatus", ignore = true)
    @Mapping(target = "approveDate", ignore = true)
    TmsFirstMileReconciliationEntity updateDtoToEntity(TmsFirstMileReconciliationDTO.UpdateDTO updateDTO);
    List<TmsFirstMileReconciliationEntity> updateDtoToEntity(List<TmsFirstMileReconciliationDTO.UpdateDTO> updateDTOList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "reconciliationMonth", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    TmsFirstMileReconciliationDetailEntity updateDetailDtoToDetailEntity(TmsFirstMileReconciliationDetailDTO.UpdateDTO detail);
    List<TmsFirstMileReconciliationDetailEntity> updateDetailDtoToDetailEntity(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList);
}
