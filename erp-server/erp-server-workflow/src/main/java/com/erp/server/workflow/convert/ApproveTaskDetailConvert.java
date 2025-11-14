package com.erp.server.workflow.convert;

import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.entity.ApproveTaskDetailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据转换
 * @author will
 * @date 2025/10/15 16:51
 */
@Mapper
@Component
public interface ApproveTaskDetailConvert {
    ApproveTaskDetailConvert INSTANCE = Mappers.getMapper(ApproveTaskDetailConvert.class);
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "sysField", source = "sysField"),
            @Mapping(target = "sysFieldValue", source = "sysFieldValue"),
            @Mapping(target = "entityCode", source = "sysParentId")
    })
    ApproveTaskDetailEntity approveTaskDetailUpdateToEntity(ApproveTaskDetailDTO.UpdateDTO dto);
    List<ApproveTaskDetailEntity> approveTaskDetailUpdateToEntity(List<ApproveTaskDetailDTO.UpdateDTO> sourceDataList);
}
