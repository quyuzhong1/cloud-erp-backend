package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.ReportOrderDemandEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ReportOrderDemandDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
public interface ReportOrderDemandService extends SuperService<ReportOrderDemandEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ReportOrderDemandDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-09-23
    * @param dto
    * @return
    */
    Boolean update(ReportOrderDemandDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/9/23 17:43
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ReportOrderDemandDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto);
    /**
     * 导出
     * @author will
     * @date 2024/9/23 17:43
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(ReportOrderDemandDTO.PagingParamDTO dto);
    /**
     * 导出数据查询
     * @author will
     * @date 2024/9/24 12:20
     * @param dto
     * @return List<ListDTO>
     */
    List<ReportOrderDemandDTO.ListDTO> listReportOrderDemand(ReportOrderDemandDTO.PagingParamDTO dto);
}
