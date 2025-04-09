package com.erp.server.oms.service;

import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;

import java.util.List;

/**
 * <p>
 * 发票设置明细 服务类
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
public interface CfgInvoiceSettingDetailService extends SuperService<CfgInvoiceSettingDetailEntity> {

    Boolean update(CfgInvoiceSettingDetailDTO.UpdateDTO addOrUpdateDTO);

    List<CfgInvoiceSettingDetailDTO.ViewDTO> view(CfgInvoiceSettingDetailDTO.ViewParamsDTO dto);

    BaseResultDTO.AddDTO add(CfgInvoiceSettingDetailDTO.AddDTO dto);

    List<CfgInvoiceSettingDetailDTO.ViewDetailShop> getDetailShop();

}
