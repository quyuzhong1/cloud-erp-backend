package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.model.wms.entity.QcRuleEntity;

/**
 * <p>
 * 质检规则 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
public interface QcRuleService extends SuperService<QcRuleEntity> {

    
    /**
     * 添加质检规则
     * @author yl
     * @date 2023-04-13 10:18
     * @param dto
     * @return java.lang.String
     */
    String add(QcRuleDTO.AddDTO dto);
}
