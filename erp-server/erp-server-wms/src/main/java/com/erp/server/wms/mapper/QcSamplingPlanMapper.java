package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.SamplingPlanDTO;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 抽样方案表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
@Mapper
public interface QcSamplingPlanMapper extends BaseMapper<QcSamplingPlanEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SamplingPlanDTO.ListDTO> paging(Page query, @Param("params") SamplingPlanDTO.PagingParamDTO params);


    List<SamplingPlanDTO.ListDTO> listByQcTypeList(@Param("qcTypeList")List<String> qcTypeList);
}
