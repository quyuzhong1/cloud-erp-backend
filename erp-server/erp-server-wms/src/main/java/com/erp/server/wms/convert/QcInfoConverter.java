package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.QcInfoEntity;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 海外发货计划实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface QcInfoConverter {
    QcInfoConverter INSTANCE = Mappers.getMapper(QcInfoConverter.class);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "qcUserName", ignore = true)
    @Mapping(target = "qcFinishTime", ignore = true)
    @Mapping(target = "qcDeptName", ignore = true)
    @Mapping(target = "purchaseOrderDetailId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "invalidStatus", ignore = true)
    @Mapping(target = "invalidRemark", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "qcStatus", expression = "java(com.erp.model.wms.enums.QcBillStatusEnum.getByCode(dto.getQcStatus()))")
    QcInfoEntity toEntity(QcInfoDTO.SaveOrUpdateDTO dto);
}
