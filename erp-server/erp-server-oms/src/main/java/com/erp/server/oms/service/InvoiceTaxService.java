package com.erp.server.oms.service;
import com.erp.model.oms.entity.InvoiceTaxEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.InvoiceTaxDTO;

/**
 * <p>
 * 发票税务信息 服务类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
public interface InvoiceTaxService extends SuperService<InvoiceTaxEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InvoiceTaxDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    Boolean update(InvoiceTaxDTO.UpdateDTO dto);

    /**
     * 查看
     * @author will
     * @date 2025/4/7 16:46
     * @param id
     * @return com.erp.model.oms.dto.InvoiceTaxDTO.ViewDTO
     */
    InvoiceTaxDTO.ViewDTO view(String id);
}
