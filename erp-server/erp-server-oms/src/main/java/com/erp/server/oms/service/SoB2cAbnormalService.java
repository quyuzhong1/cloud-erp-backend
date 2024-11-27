package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;

import java.util.List;

/**
 * @description: b2c异常订单接口
 * @author Will
 * @date: 2024/4/22 9:06
 */
public interface SoB2cAbnormalService {

    /**
     * 分页列表
     * @author Will
     * @date: 2024/4/22 9:29
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<SoB2cAbnormalDTO.ListDTO> abnormalPaging(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto);
    /**
     * @description: 异常订单导出
     * @author Will
     * @date: 2024/4/22 19:53
     * @param dto
     * @return Boolean
     */
    Boolean abnormalExportExcel(SoB2cAbnormalDTO.PagingParamDTO dto);
    /**
     * @description: 批量重试
     * @author Will
     * @date: 2024/4/28 9:01
     * @param id
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> batchRetry(String id);

    void clearAbnormal(List<String> ids);
}
