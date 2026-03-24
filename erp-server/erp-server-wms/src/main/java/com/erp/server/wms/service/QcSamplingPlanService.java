package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SamplingPlanDTO;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.model.wms.entity.QcSamplingPlanQcTypeRefEntity;

/**
 * <p>
 * 抽样方案表 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
public interface QcSamplingPlanService extends SuperService<QcSamplingPlanEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author zdy
     * @date: 2026-03-20
     */
    BaseResultDTO.AddDTO add(SamplingPlanDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author zdy
     * @date: 2026-03-20
     */
    Boolean update(SamplingPlanDTO.UpdateDTO dto);


    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<SamplingPlanDTO.ListDTO>>
     * @author zdy
     * @date: 2026-03-20
     */
    PagingVO<SamplingPlanDTO.ListDTO> paging(PagingDTO<SamplingPlanDTO.PagingParamDTO> pagingParamDTO);


    /**
     * 详情
     *
     * @param id
     * @return
     * @author zdy
     * @date: 2026-03-20
     */
    SamplingPlanDTO.ViewDTO view(String id);

    /**
     * 启用/禁用
     *
     * @param id
     * @param disabled
     * @param entity
     * @return
     * @author zdy
     * @date: 2026-03-20
     */
    BatchResultDTO updateStatus(String id, Boolean disabled, QcSamplingPlanEntity entity);

    BatchResultDTO delete(String id, QcSamplingPlanQcTypeRefEntity qcTypeRefEntity);

    /**
     * 获取抽样方案
     * @param planDTO
     * @return
     */
    SamplingPlanDTO.PlanDTO getSamplingPlan(SamplingPlanDTO.PlanParamDTO planDTO);
}
