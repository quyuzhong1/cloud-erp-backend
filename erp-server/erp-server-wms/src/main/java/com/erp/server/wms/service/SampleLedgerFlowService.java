package com.erp.server.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.PagingDTO;
import com.erp.model.wms.entity.SampleLedgerFlowEntity;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 样品台账流水 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleLedgerFlowService extends SuperService<SampleLedgerFlowEntity> {

    /**
     * 获取样品台账流水分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 分页参数
     * @return 分页结果
     */
    PagingVO<SampleLedgerFlowDTO.ListDTO> getSampleLedgerFlowPageData(PagingDTO<SampleLedgerFlowDTO.ExportDTO> dto);

    /**
     * 异步导出
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param response
     * @return
     */
    Boolean exportList(SampleLedgerFlowDTO.ExportDTO dto, HttpServletResponse response);

    PagingVO<SampleLedgerFlowDTO.ListDTO> paging(PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> dto);

    /**
     * 新增样品台账流水
     * @author wuhaotian
     * @date: 2025-08-21
     * @param addDTO 台账流水新增参数
     * @return 是否成功
     */
    Boolean addSampleLedgerFlow(SampleLedgerFlowDTO.AddFlowDTO addDTO);
}
