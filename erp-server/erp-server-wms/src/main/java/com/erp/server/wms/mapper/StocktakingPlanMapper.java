package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 盘点计划表 Mapper 接口
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Mapper
public interface StocktakingPlanMapper extends BaseMapper<StocktakingPlanEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<StocktakingPlanDTO.ListDTO> paging(Page query, @Param("params") StocktakingPlanDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") StocktakingPlanDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<StocktakingPlanDTO.ListDTO> listExport(@Param("params") StocktakingPlanDTO.ExportDTO params);

    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<StocktakingPlanDTO.TabListDTO> tabList(@Param("params") StocktakingPlanDTO.PagingParamDTO searchParam);

    /**
     * 允许下推列表
     * @param
     * @return
     */
    List<StocktakingPlanDTO.AllowPushDTO> allowPushStocktakingPlan();
}
