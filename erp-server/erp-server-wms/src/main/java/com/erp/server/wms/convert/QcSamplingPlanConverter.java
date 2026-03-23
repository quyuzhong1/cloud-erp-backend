package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FBA货件实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface QcSamplingPlanConverter {
    QcSamplingPlanConverter INSTANCE = Mappers.getMapper(QcSamplingPlanConverter.class);

    List<QcSamplingPlanSkuRefEntity> qcSamplingPlanSkuRefToAdd(List<SamplingPlanSkuRefDTO.AddDTO> skuRefDTOList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcSamplingPlanSkuRefEntity qcSamplingPlanSkuRefToAdd(SamplingPlanSkuRefDTO.AddDTO skuRefDTO);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcSamplingPlanDetailEntity qcSamplingPlanDetailToAdd(SamplingPlanDetailDTO.AddDTO detailDTO);
    List<QcSamplingPlanDetailEntity> qcSamplingPlanDetailToAdd(List<SamplingPlanDetailDTO.AddDTO> detailList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcSamplingPlanQcTypeRefEntity qcSamplingPlanQcTypeRefToAdd(SamplingPlanQcTypeRefDTO.AddDTO qcTypeDTO);
    List<QcSamplingPlanQcTypeRefEntity> qcSamplingPlanQcTypeRefToAdd(List<SamplingPlanQcTypeRefDTO.AddDTO> qcTypeList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcSamplingPlanSkuRefEntity qcSamplingPlanSkuRefToUpdateUpdate(SamplingPlanSkuRefDTO.UpdateDTO skuRefDTO);
    List<QcSamplingPlanSkuRefEntity> qcSamplingPlanSkuRefToUpdateUpdate(List<SamplingPlanSkuRefDTO.UpdateDTO> skuRefDTOList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcSamplingPlanDetailEntity qcSamplingPlanDetailToUpdateUpdate(SamplingPlanDetailDTO.UpdateDTO detailDTO);
    List<QcSamplingPlanDetailEntity> qcSamplingPlanDetailToUpdateUpdate(List<SamplingPlanDetailDTO.UpdateDTO> detailList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QcSamplingPlanQcTypeRefEntity qcSamplingPlanQcTypeRefToUpdateUpdate(SamplingPlanQcTypeRefDTO.UpdateDTO qcTypeDTO);
    List<QcSamplingPlanQcTypeRefEntity> qcSamplingPlanQcTypeRefToUpdateUpdate(List<SamplingPlanQcTypeRefDTO.UpdateDTO> qcTypeList);

    @Mapping(target = "skuRefDTOList", ignore = true)
    @Mapping(target = "qcTypeList", ignore = true)
    @Mapping(target = "qcLevelName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.wms.enums.QcLevelEnum.class, qcSamplingPlanEntity.getQcLevel()))")
    @Mapping(target = "planTypeName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.wms.enums.PlanTypeEnum.class, qcSamplingPlanEntity.getPlanType()))")
    @Mapping(target = "detailList", ignore = true)
    SamplingPlanDTO.ViewDTO qcSamplingPlanEntityToViewDTO(QcSamplingPlanEntity qcSamplingPlanEntity);

    SamplingPlanQcTypeRefDTO.ViewDTO qcSamplingPlanQcTypeRefEntityToViewDTO(QcSamplingPlanQcTypeRefEntity qcTypeEntity);
    List<SamplingPlanQcTypeRefDTO.ViewDTO> qcSamplingPlanQcTypeRefEntityToViewDTO(List<QcSamplingPlanQcTypeRefEntity> qcTypeList);

    SamplingPlanSkuRefDTO.ViewDTO qcSamplingPlanSkuRefEntityToViewDTO(QcSamplingPlanSkuRefEntity skuRefEntity);
    List<SamplingPlanSkuRefDTO.ViewDTO> qcSamplingPlanSkuRefEntityToViewDTO(List<QcSamplingPlanSkuRefEntity> skuRefList);

    SamplingPlanDetailDTO.ViewDTO qcSamplingPlanDetailEntityToViewDTO(QcSamplingPlanDetailEntity detailEntity);
    List<SamplingPlanDetailDTO.ViewDTO> qcSamplingPlanDetailEntityToViewDTO(List<QcSamplingPlanDetailEntity> detailList);
}
