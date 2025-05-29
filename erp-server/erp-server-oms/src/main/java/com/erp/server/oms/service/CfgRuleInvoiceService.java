package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.CfgRuleInvoiceEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CfgRuleInvoiceDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 开票规则 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-05-23
 */
public interface CfgRuleInvoiceService extends SuperService<CfgRuleInvoiceEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    String add(CfgRuleInvoiceDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-05-23
    * @param dto
    * @return
    */
    Boolean update(CfgRuleInvoiceDTO.UpdateDTO dto);


    PagingVO<CfgRuleInvoiceDTO.PagingViewDTO> paging(PagingDTO<CfgRuleInvoiceDTO.PagingParamDTO> dto);

    CfgRuleInvoiceDTO.ViewDTO view(String id);

    BatchResultDTO updateStatus(CfgRuleInvoiceEntity entity, Boolean state);

    BatchResultDTO delete(CfgRuleInvoiceEntity entity);

    CfgInvoiceSettingDTO.RuleMatchDTO getRuleInvoiceMatchResult(Map<String, Object> map);

    Map<String, Boolean> invoiceCfgRule(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> detailList, Map<String, Object> map);

    SoB2cDTO.InvoiceResult invoiceRule(SoB2cEntity soB2cEntity);
}
