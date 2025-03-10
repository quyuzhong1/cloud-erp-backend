package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.InvoiceDetailEntity;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.InvoiceInfoDTO;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 上传记录 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
public interface InvoiceInfoService extends SuperService<InvoiceInfoEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InvoiceInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    Boolean update(InvoiceInfoDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<InvoiceInfoDTO.PagingViewDTO> paging(PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto);

    String downloadInvoice(String id);

    List<BatchResultDTO> batchGenerateInvoice(List<String> ids);

    void batchSave(List<InvoiceInfoEntity> addList, List<InvoiceDetailEntity> addDetailList);

    /**
     * 批量生成发票
     * @param ids
     * @param response
     */
    void exportInvoicePdf(List<String> ids, HttpServletResponse response);
}
