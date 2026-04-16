package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.QcSamplingPlanQcTypeRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SamplingPlanQcTypeRefDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 抽样方案质检类型关联表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Mapper
public interface QcSamplingPlanQcTypeRefMapper extends BaseMapper<QcSamplingPlanQcTypeRefEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SamplingPlanQcTypeRefDTO.ListDTO> paging(Page query, @Param("params") SamplingPlanQcTypeRefDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SamplingPlanQcTypeRefDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SamplingPlanQcTypeRefDTO.ListDTO> listExport(@Param("params") SamplingPlanQcTypeRefDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SamplingPlanQcTypeRefDTO.TabListDTO> tabList(@Param("params") SamplingPlanQcTypeRefDTO.PagingParamDTO searchParam);
}
