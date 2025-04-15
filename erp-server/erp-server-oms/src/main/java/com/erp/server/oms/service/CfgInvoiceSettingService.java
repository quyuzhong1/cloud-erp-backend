package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * 服务类
 *
 * @author Lambda
 * @since 2025-04-07
 */
public interface CfgInvoiceSettingService extends SuperService<CfgInvoiceSettingEntity> {

    PagingVO<CfgInvoiceSettingDTO.PagingViewDTO> paging(PagingDTO<CfgInvoiceSettingDTO.PagingParamDTO> dto);

    BaseResultDTO.AddDTO add(CfgInvoiceSettingDTO.AddDTO dto);

    Boolean update(CfgInvoiceSettingDTO.UpdateDTO dto);

    Boolean delete(List<String> ids);

    CfgInvoiceSettingDTO.ViewDTO view(String id);

    Boolean updateStatus(CfgInvoiceSettingDTO.UpdateStatusDTO dto);

    List<CfgInvoiceInvalidDTO.DropDownDTO> getCompanyName();
}
