package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.InvoiceUpdateHisDTO;
import com.erp.model.oms.entity.InvoiceUpdateHisEntity;

import java.util.List;

/**
 * <p>
 * 发票更新历史 服务类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
public interface InvoiceUpdateHisService extends SuperService<InvoiceUpdateHisEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InvoiceUpdateHisDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    Boolean update(InvoiceUpdateHisDTO.UpdateDTO dto);

    /**
     * 列表查询
     * @author will
     * @date 2025/4/7 17:21
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.InvoiceUpdateHisDTO.ListDTO>
     */
    List<InvoiceUpdateHisDTO.ListDTO> list(InvoiceUpdateHisDTO.IdDTO dto);
    /**
     * 根据发票id查询修改的历史数量
     * @author will
     * @date 2025/4/9 14:58
     * @param invoiceInfoId
     * @return Integer
     */
    Integer countByInvoiceInfoId(String invoiceInfoId);
}
