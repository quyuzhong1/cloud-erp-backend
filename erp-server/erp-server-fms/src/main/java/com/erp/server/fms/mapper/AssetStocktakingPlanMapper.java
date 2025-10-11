package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetStocktakingPlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 资产盘点方案表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetStocktakingPlanMapper extends BaseMapper<AssetStocktakingPlanEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetStocktakingPlanDTO.ListDTO> paging(Page query, @Param("params") AssetStocktakingPlanDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetStocktakingPlanDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AssetStocktakingPlanDTO.ListDTO> listExport(@Param("params") AssetStocktakingPlanDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetStocktakingPlanDTO.TabListDTO> tabList(@Param("params") AssetStocktakingPlanDTO.PagingParamDTO searchParam);
}
