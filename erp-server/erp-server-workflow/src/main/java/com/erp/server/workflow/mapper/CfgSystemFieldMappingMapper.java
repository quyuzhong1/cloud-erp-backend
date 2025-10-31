package com.erp.server.workflow.mapper;
import com.erp.model.workflow.dto.CfgSystemFieldMappingDTO;
import com.erp.model.workflow.entity.CfgSystemFieldMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 远程查询配置 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-10-17
 */
@Mapper
public interface CfgSystemFieldMappingMapper extends BaseMapper<CfgSystemFieldMappingEntity> {
    /**
     * 根据字段映射查询
     * @author will
     * @date 2025/10/20 09:36
     * @param params
     * @return List<CfgSystemFieldMappingEntity>
     */
    List<CfgSystemFieldMappingEntity> listSystemFieldMapping(@Param("params") List<CfgSystemFieldMappingDTO.FieldMappingParamDTO> params);
}
