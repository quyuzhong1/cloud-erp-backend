package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * B2C虚拟仓订单跟踪 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
public interface SoB2cProcessingService extends SuperService<SoB2cProcessingEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-12-18
    * @param list
    * @return
    */
    Boolean addOrUpdate(List<SoB2cProcessingDTO.AddOrUpdateDTO> list,LocalDate startDate);

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:31
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<SoB2cProcessingDTO.ListDTO> paging(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/12/18 11:55
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(SoB2cProcessingDTO.PagingParamDTO dto);
    /**
     * 自动更新b2c销售订单跟踪
     * @author will
     * @date 2024/12/18 18:30
     * @param startDate
     */
    void autoUpdateSoB2cProcessing(LocalDate startDate);
    /**
     * b2c汇总分页查询
     * @author will
     * @date 2025/8/20 11:40
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ReportProcessingDTO.ListDTO> b2cTotalPaging(PagingDTO<ReportProcessingDTO.PagingParamDTO> dto);
}
