package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetLocationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetLocationDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 资产位置表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetLocationMapper extends BaseMapper<AssetLocationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetLocationDTO.ListDTO> paging(Page query, @Param("params") AssetLocationDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetLocationDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AssetLocationDTO.ListDTO> listExport(@Param("params") AssetLocationDTO.ExportDTO params);

    /**
    * 导出Excel查询（分页）
    * @param query
    * @param params
    * @return
    */
    IPage<AssetLocationDTO.ListDTO> listExport(Page<AssetLocationDTO.ExportDTO> query, @Param("params") AssetLocationDTO.ExportDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetLocationDTO.TabListDTO> tabList(@Param("params") AssetLocationDTO.PagingParamDTO searchParam);
}
