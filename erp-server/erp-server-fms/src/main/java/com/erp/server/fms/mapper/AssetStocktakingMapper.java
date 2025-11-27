package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetStocktakingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetStocktakingDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 资产盘点表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetStocktakingMapper extends BaseMapper<AssetStocktakingEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetStocktakingDTO.ListDTO> paging(Page query, @Param("params") AssetStocktakingDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetStocktakingDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    IPage<AssetStocktakingDTO.ListDTO> listExport(Page page,@Param("params") AssetStocktakingDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetStocktakingDTO.TabListDTO> tabList(@Param("params") AssetStocktakingDTO.PagingParamDTO searchParam);
}
