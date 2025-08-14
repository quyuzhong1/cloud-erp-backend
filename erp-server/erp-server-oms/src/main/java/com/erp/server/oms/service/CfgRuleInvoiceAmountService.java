package com.erp.server.oms.service;
import com.erp.model.oms.dto.CfgRuleInvoiceAmountDTO;
import com.erp.model.oms.entity.CfgRuleInvoiceAmountEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;

import java.util.List;

/**
 * <p>
 * 发票产品总价计算规则 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-07-14
 */
public interface CfgRuleInvoiceAmountService extends SuperService<CfgRuleInvoiceAmountEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-07-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleInvoiceAmountDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-07-14
    * @param dto
    * @return
    */
    Boolean update(CfgRuleInvoiceAmountDTO.UpdateDTO dto);

    /**
     * 根据配置获取规则列表
     * @param id
     * @return
     */
    List<CfgRuleInvoiceAmountDTO.ViewDTO> listByCfgId(String id);

    void batchAddOrUpdate(List<CfgRuleInvoiceAmountDTO.ViewDTO> productAmountDTOList, String mainId);

    List<CfgRuleInvoiceAmountEntity> listRuleByPriority(List<String> cfgIds);
}
