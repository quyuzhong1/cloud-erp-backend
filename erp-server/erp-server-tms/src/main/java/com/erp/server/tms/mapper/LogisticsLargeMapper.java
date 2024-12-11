package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsLargeDTO;
import com.erp.model.tms.entity.LogisticsLargeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流大表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
@Mapper
public interface LogisticsLargeMapper extends BaseMapper<LogisticsLargeEntity> {


    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsLargeDTO.PagingViewDTO> paging(Page query, @Param("params") LogisticsLargeDTO.PagingParamDTO params);

    /**
     * tab页查询
     * @param permissionSql
     * @return
     */
    List<LogisticsLargeDTO.TabListDTO> listTabCount(@Param("permissionSql") String permissionSql);
}
