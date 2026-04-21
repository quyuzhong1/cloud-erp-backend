package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcSamplingPlanRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.QcSamplingPlanRefDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2026-03-25
 */
@Mapper
public interface QcSamplingPlanRefMapper extends BaseMapper<QcSamplingPlanRefEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<QcSamplingPlanRefDTO.ListDTO> paging(Page query, @Param("params") QcSamplingPlanRefDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") QcSamplingPlanRefDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<QcSamplingPlanRefDTO.ListDTO> listExport(@Param("params") QcSamplingPlanRefDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<QcSamplingPlanRefDTO.TabListDTO> tabList(@Param("params") QcSamplingPlanRefDTO.PagingParamDTO searchParam);
}
