package com.erp.server.oms.service;
import com.erp.model.oms.entity.CfgRuleInvoiceProductAmountEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgRuleInvoiceProductAmountDTO;

import java.util.List;

/**
 * <p>
 * 发票产品总价计算规则 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-07-14
 */
public interface CfgRuleInvoiceProductAmountService extends SuperService<CfgRuleInvoiceProductAmountEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-07-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleInvoiceProductAmountDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-07-14
    * @param dto
    * @return
    */
    Boolean update(CfgRuleInvoiceProductAmountDTO.UpdateDTO dto);

    /**
     * 根据配置获取规则列表
     * @param id
     * @return
     */
    List<CfgRuleInvoiceProductAmountDTO.ViewDTO> listByCfgId(String id);

    void batchAddOrUpdate(List<CfgRuleInvoiceProductAmountDTO.ViewDTO> productAmountDTOList, String mainId);

    List<CfgRuleInvoiceProductAmountEntity> listRuleByPriority(List<String> cfgIds);
}
