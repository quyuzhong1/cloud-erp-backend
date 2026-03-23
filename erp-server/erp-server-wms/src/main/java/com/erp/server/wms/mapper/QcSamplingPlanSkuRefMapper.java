package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.SamplingPlanSkuRefDTO;
import com.erp.model.wms.entity.QcSamplingPlanSkuRefEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 抽样方案SKU白名单关联表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Mapper
public interface QcSamplingPlanSkuRefMapper extends BaseMapper<QcSamplingPlanSkuRefEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SamplingPlanSkuRefDTO.ListDTO> paging(Page query, @Param("params") SamplingPlanSkuRefDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SamplingPlanSkuRefDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<SamplingPlanSkuRefDTO.ListDTO> listExport(@Param("params") SamplingPlanSkuRefDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SamplingPlanSkuRefDTO.TabListDTO> tabList(@Param("params") SamplingPlanSkuRefDTO.PagingParamDTO searchParam);
}
