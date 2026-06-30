package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgTableArchiveDTO;
import com.erp.model.sys.entity.CfgTableArchiveEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 归档配置表 Mapper 接口
 * </p>
 */
@Mapper
public interface CfgTableArchiveMapper extends BaseMapper<CfgTableArchiveEntity> {

    IPage<CfgTableArchiveDTO.ListDTO> paging(Page<?> query, @Param("params") CfgTableArchiveDTO.SearchParamDTO params);
}
