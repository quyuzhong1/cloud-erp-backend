package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcApplicationDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 质检申请单主表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Mapper
public interface QcApplicationMapper extends BaseMapper<QcApplicationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcApplicationDTO.ListDTO> paging(Page query, @Param("params") QcApplicationDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") QcApplicationDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<QcApplicationDTO.ListDTO> listExport(@Param("params") QcApplicationDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcApplicationDTO.TabListDTO> tabList(@Param("params") QcApplicationDTO.PagingParamDTO searchParam);
}
