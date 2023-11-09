package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateRuleDTO;

import java.util.List;

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
     * @description: 新增
     * @author Will
     * @date: 2023/11/7 10:47
     * @param detailList
     * @param mainId
     */
    Boolean add(List<ShippingTemplateRuleDTO.AddDTO> detailList, String mainId);

    /**
     * @description:修改
    * @author Will
     * @date: 2023/11/7 10:47
     * @param detailList
     * @param mainId
     */
    Boolean update(List<ShippingTemplateRuleDTO.UpdateDTO> detailList, String mainId);

    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/11/8 9:36
     * @param mainId
     * @return List<ShippingTemplateRuleEntity>
    */
     List<ShippingTemplateRuleEntity> listByMainId(String mainId);
    /**
     * @description: 根据主表id删除
    * @author Will
     * @date: 2023/11/8 14:09
     * @param mainId
    */
    void deleteByMainId(String mainId);
}
