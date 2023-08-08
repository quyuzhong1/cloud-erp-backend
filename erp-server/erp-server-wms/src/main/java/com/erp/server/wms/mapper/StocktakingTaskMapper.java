package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 盘点任务表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Mapper
public interface StocktakingTaskMapper extends BaseMapper<StocktakingTaskEntity> {

    List<StocktakingTaskDTO.TabDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页获取
     * @param query
     * @param params
     * @param tabList
     * @return
     */
    IPage<StocktakingTaskDTO.PagingViewDTO> paging(Page query, @Param("params") StocktakingTaskDTO.PagingParamDTO params, @Param("tabList")List<String> tabList,@Param("mainIdList")List<String> mainIdList);
    /**
     * 导出列表
     * @author yl
     * @date 2023-08-08 14:20
     * @param params
     * @param tabList
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDTO.PagingViewDTO>
     */
    List<StocktakingTaskDTO.PagingViewDTO> listExport(@Param("params") StocktakingTaskDTO.ExportDTO params, @Param("tabList")List<String> tabList, @Param("mainIdList")List<String> mainIdList);
}
