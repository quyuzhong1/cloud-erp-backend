package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.CfgRuleInvoiceProductAmountDTO;
import com.erp.model.oms.entity.CfgRuleInvoiceProductAmountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 发票产品总价计算规则 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-07-14
 */
@Mapper
public interface CfgRuleInvoiceProductAmountMapper extends BaseMapper<CfgRuleInvoiceProductAmountEntity> {

    /**
     * 根据配置获取规则列表
     * @param id
     * @return
     */
    List<CfgRuleInvoiceProductAmountDTO.ViewDTO> listByCfgId(String id);
}
