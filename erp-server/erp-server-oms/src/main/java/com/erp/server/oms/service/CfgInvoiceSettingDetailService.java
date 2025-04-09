package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 发票设置明细 服务类
 * </p>
 *
 * @author Lambda
 * @since 2025-04-07
 */
public interface CfgInvoiceSettingDetailService extends SuperService<CfgInvoiceSettingDetailEntity> {

    List<CfgInvoiceSettingDetailDTO.ViewDTO> view(String mainId,String key,List<String> names);

    BaseResultDTO.AddDTO add(CfgInvoiceSettingDetailDTO.AddDTO dto);

    List<CfgInvoiceSettingDetailDTO.ViewDetailShop> getDetailShop();
}
