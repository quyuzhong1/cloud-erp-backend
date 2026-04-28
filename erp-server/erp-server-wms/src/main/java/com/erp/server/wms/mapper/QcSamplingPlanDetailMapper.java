package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcSamplingPlanDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SamplingPlanDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Mapper
public interface QcSamplingPlanDetailMapper extends BaseMapper<QcSamplingPlanDetailEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SamplingPlanDetailDTO.ListDTO> paging(Page query, @Param("params") SamplingPlanDetailDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SamplingPlanDetailDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SamplingPlanDetailDTO.ListDTO> listExport(@Param("params") SamplingPlanDetailDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SamplingPlanDetailDTO.TabListDTO> tabList(@Param("params") SamplingPlanDetailDTO.PagingParamDTO searchParam);
}
