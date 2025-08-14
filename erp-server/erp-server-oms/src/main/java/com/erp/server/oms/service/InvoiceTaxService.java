package com.erp.server.oms.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.entity.InvoiceTaxEntity;

import java.util.List;

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
    * 修改
    * @author will
    * @date: 2025-04-07
    * @param dto
    * @return
    */
    BatchResultDTO addOrUpdate(InvoiceTaxDTO.UpdateDTO dto);

    /**
     * 查看
     * @author will
     * @date 2025/4/7 16:46
     * @param listingId
     * @return com.erp.model.oms.dto.InvoiceTaxDTO.ViewDTO
     */
    InvoiceTaxDTO.ViewDTO view(String listingId);

    /**
     * 根据listingIdList查询
     * @author will
     * @date 2025/4/8 14:37
     * @param listingIdList
     * @return List<InvoiceTaxEntity>
     */
    List<InvoiceTaxEntity> listByListingIdList(List<String> listingIdList);
    /**
     * 是否生成凯珀信息
     * @author will
     * @date 2025/4/8 15:40
     * @param invoiceTaxEntity
     * @return Boolean
     */
    Boolean checkInvoiceTax(InvoiceTaxEntity invoiceTaxEntity);
    /**
     *
     * @author will
     * @date 2025/4/9 09:53
     * @param invoiceTaxList
     * @return void
     */
    void importUpdate(List<InvoiceTaxDTO.UpdateDTO> invoiceTaxList);

    /**
     * 预览
     * @param dto
     * @return
     */
    List<InvoiceTaxDTO.UpdateDTO> invoiceAddressView(BaseIdsDTO.IdsDTO dto);
}
