package com.erp.server.oms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.common.business.service.SuperService;

import java.util.List;
import java.util.Map;

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

    /**
     * @description: 获取发票设置公司名
     * @author: hcg
     * @date: 2025/4/17 13:22
     **/
    List<CfgInvoiceInvalidDTO.DropDownDTO> getCompanyName();

    void updateSerialNo(CfgInvoiceSettingDTO.UpdateSerialDTO dto);

    void updateSerialNoById(String id, Integer no, Integer startCode);

    /**
     * 初始化公司列表
     * 从第三方系统获取公司列表并初始化到cfg_invoice_setting表
     * 
     * @return 初始化结果信息
     */
    Map<String, Object> initCompanyList();
}
