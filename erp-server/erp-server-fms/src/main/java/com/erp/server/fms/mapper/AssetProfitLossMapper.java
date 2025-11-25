package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetProfitLossEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetProfitLossDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 盘盈盘亏单主表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetProfitLossMapper extends BaseMapper<AssetProfitLossEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetProfitLossDTO.ListDTO> paging(Page query, @Param("params") AssetProfitLossDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetProfitLossDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AssetProfitLossDTO.ListDTO> listExport(@Param("params") AssetProfitLossDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetProfitLossDTO.TabListDTO> tabList(@Param("params") AssetProfitLossDTO.PagingParamDTO searchParam);
}
