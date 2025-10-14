package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetDisposalEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetDisposalDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 资产处置单主表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetDisposalMapper extends BaseMapper<AssetDisposalEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetDisposalDTO.ListDTO> paging(Page query, @Param("params") AssetDisposalDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetDisposalDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AssetDisposalDTO.ListDTO> listExport(@Param("params") AssetDisposalDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetDisposalDTO.TabListDTO> tabList(@Param("params") AssetDisposalDTO.PagingParamDTO searchParam);
}
