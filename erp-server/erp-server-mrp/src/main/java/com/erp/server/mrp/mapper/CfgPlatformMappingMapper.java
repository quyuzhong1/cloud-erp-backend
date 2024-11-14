package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 平台映射表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Mapper
public interface CfgPlatformMappingMapper extends BaseMapper<CfgPlatformMappingEntity> {
    /**
     * 下拉
     * @author will
     * @date 2024/8/29 17:18
     * @param params
     * @return IPage<ListDTO>
     */
    List<CfgPlatformMappingDTO.ListDTO> selectPlatformMapping(@Param("params") CfgPlatformMappingDTO.SelectDTO params);
}
