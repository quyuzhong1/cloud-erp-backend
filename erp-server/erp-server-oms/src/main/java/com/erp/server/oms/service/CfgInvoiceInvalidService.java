package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CfgInvoiceInvalidEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;

import javax.validation.Valid;

/**
 * <p>
 * 作废发票号 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-04-14
 */
public interface CfgInvoiceInvalidService extends SuperService<CfgInvoiceInvalidEntity> {

    /**
    * 新增
    * @author hcg
    * @date: 2025-04-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgInvoiceInvalidDTO.AddDTO dto);

    PagingVO<CfgInvoiceInvalidDTO.PagingViewDTO> paging(PagingDTO<CfgInvoiceInvalidDTO.PagingParamDTO> dto);

    Boolean export(CustomerDTO.@Valid ExportDTO dto);
}