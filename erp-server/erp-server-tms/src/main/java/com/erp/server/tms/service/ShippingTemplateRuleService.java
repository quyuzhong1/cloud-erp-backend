package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateRuleDTO;

/**
 * <p>
 * 运费模板渠道关联表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateRuleService extends SuperService<ShippingTemplateRuleEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShippingTemplateRuleDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    Boolean update(ShippingTemplateRuleDTO.UpdateDTO dto);


}
