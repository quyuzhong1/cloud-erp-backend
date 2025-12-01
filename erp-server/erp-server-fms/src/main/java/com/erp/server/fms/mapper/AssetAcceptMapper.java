package com.erp.server.fms.mapper;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.fms.dto.AssetAcceptDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 资产验收表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetAcceptMapper extends BaseMapper<AssetAcceptEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AssetAcceptDTO.ListDTO> paging(Page query, @Param("params") AssetAcceptDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AssetAcceptDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param page 分页对象
    * @param params 查询参数
    * @return
    */
    IPage<AssetAcceptDTO.ListDTO> listExport(Page page, @Param("params") AssetAcceptDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AssetAcceptDTO.TabListDTO> tabList(@Param("params") AssetAcceptDTO.PagingParamDTO searchParam);
}
