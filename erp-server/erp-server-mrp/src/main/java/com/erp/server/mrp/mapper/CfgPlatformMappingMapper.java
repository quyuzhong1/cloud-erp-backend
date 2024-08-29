package com.erp.server.mrp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


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
     * 远程下拉
     * @author will
     * @date 2024/8/29 17:18
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<CfgPlatformMappingDTO.ListDTO> pagingSelect(Page query,@Param("params") CfgPlatformMappingDTO.SelectDTO params);
}
