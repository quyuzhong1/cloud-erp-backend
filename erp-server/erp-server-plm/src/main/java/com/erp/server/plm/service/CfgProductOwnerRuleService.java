package com.erp.server.plm.service;
import com.erp.model.plm.dto.CfgProductOwnerRuleDTO;
import com.erp.model.plm.entity.CfgProductOwnerRuleEntity;
import com.common.business.service.SuperService;

import java.util.List;


/**
 * <p>
 * 产品归属规则配置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
public interface CfgProductOwnerRuleService extends SuperService<CfgProductOwnerRuleEntity> {


    
    /**
     * 添加规则
     * @author yl
     * @date 2023-06-13 16:56
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(CfgProductOwnerRuleDTO.AddDTO dto);

    
    /**
     * 根据分类id获取到配置信息
     * 因为 对应一个分类id 会有父 子
     * @author yl
     * @date 2023-06-15 11:34
     * @param categoryIdList
     * @return com.erp.model.plm.entity.CfgProductOwnerRuleEntity
     */
    CfgProductOwnerRuleEntity getByCategoryIdList(List<String> categoryIdList);
}
