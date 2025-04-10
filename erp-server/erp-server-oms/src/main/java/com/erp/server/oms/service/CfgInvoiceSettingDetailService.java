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

    List<CfgInvoiceSettingDetailDTO.ViewDTO> view(CfgInvoiceSettingDetailDTO.ViewParamsDTO dto);

    BaseResultDTO.AddDTO addOrUpdate(List<CfgInvoiceSettingDetailDTO.AddDTO> dtoList);

    List<CfgInvoiceSettingDetailDTO.ViewDetailShop> getDetailShop();
    /**
     * 根据店铺id集合查询
     * @author will
     * @date 2025/4/9 12:20
     * @param shopIdList
     * @return List<CfgInvoiceSettingDetailEntity>
     */
    List<CfgInvoiceSettingDetailEntity> listByShopIdList(List<String> shopIdList);

    List<CfgInvoiceSettingDetailDTO.ViewShopDTO> listShopSelect(String dictplatform);
}
