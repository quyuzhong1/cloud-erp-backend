package com.erp.server.tms.service;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.SuperService;
import com.common.core.enums.ApiError;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 报关规则主表 service
 */
public interface CfgDeclareRuleService extends SuperService<CfgDeclareRuleEntity> {

    Boolean add(CfgDeclareRuleDTO.SaveListDTO dto);

    CfgDeclareRuleDTO.SaveListDTO paging(CfgDeclareRuleDTO.ListParamDTO dto);

    List<BaseDropDownDTO.Tree> dropDownList(String type,Boolean isShowCustomerId, String name);

    /**
     * 根据规则类型和条件参数匹配一条报关规则。
     *
     * <p>paramMap 必须包含 ruleType，其它 key 由规则条件配置决定；value 支持英文逗号聚合多个取值。
     * 当多个启用规则同时满足条件时，返回 {@link CfgDeclareRuleEntity#getIndex()} 最小的规则。</p>
     *
     * @param paramMap 参数集合，需包含 ruleType
     * @return 命中的报关规则；没有匹配结果时返回 null
     */
    CfgDeclareRuleEntity listMatchedRule(Map<String, String> paramMap);

    /**
     * 按来源单逐单匹配报关规则，校验境外收货人类型一致并返回统一类型。
     *
     * @param declareBillType 报关单类型
     * @param sourceDetailList 来源明细
     * @param paramMapBuilder 构造 {@link #listMatchedRule(Map)} 入参
     * @param ruleNotFoundError 某来源单无匹配规则时抛出
     * @param receiverTypeConflictError 境外收货人类型不一致时抛出
     * @return 统一的境外收货人类型；均未配置时返回空字符串
     */
    String resolveConsistentReceiverType(String declareBillType,
                                         List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList,
                                         BiFunction<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>, Map<String, String>> paramMapBuilder,
                                         ApiError ruleNotFoundError,
                                         ApiError receiverTypeConflictError);
}
