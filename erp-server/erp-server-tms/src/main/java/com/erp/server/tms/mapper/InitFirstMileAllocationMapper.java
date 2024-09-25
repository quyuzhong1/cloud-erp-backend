package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.tms.entity.InitFirstMileAllocationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 期初头程分摊 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
 */
@Mapper
public interface InitFirstMileAllocationMapper extends BaseMapper<InitFirstMileAllocationEntity> {
    /**
     * 统计tab
     *@parms permissionSql
     *@return
     *@author zdy
     *@date 2024-8-15
     */
    List<InitFirstMileAllocationDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<InitFirstMileAllocationDTO.PagingVO> paging(Page<InitFirstMileAllocationDTO.PagingVO> query, @Param("params") InitFirstMileAllocationDTO.PagingParamDTO params);

    /**
     * 导出
     * @param params
     * @return
     */
    List<InitFirstMileAllocationDTO.PagingVO> exportList(@Param("params") InitFirstMileAllocationDTO.PagingParamDTO params);
}
