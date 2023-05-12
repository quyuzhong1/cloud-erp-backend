package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.entity.CustomerInvoiceEntity;

import java.util.List;

/**
 * <p>
 * 客户发票信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerInvoiceService extends SuperService<CustomerInvoiceEntity> {

    /**
     * 检查默认银行是否是多个
     * @author yl
     * @date 2023-05-12 14:59
     * @param invoiceList
     * @return void
     */
    void checkIsDefault(List<InvoiceDTO.AddDTO> invoiceList);

    
    /**
     * 批量保存发票信息
     * @author yl
     * @date 2023-05-12 16:01
     * @param id
     * @param invoiceList
     * @return void
     */
    void saveBatchInvoice(String id, List<InvoiceDTO.AddDTO> invoiceList);
}
