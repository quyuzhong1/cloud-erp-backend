package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingRegionCityEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingRegionCityDTO;

import java.util.List;

/**
 * <p>
 * 运费规则分区城市表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-07
 */
public interface ShippingRegionCityService extends SuperService<ShippingRegionCityEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-07
    * @param list
    * @return
    */
    Boolean add(List<ShippingRegionCityDTO.AddDTO> list);
    /**
     * @description: 根据运费规则id查询
     * @author Will
     * @date: 2023/11/8 11:41
     * @param shippingTemplateRuleId
     * @return List<ShippingRegionCityEntity>
     */
    List<ShippingRegionCityEntity> listByRuleId(String shippingTemplateRuleId);
    /**
     * @description: 根据运费规则删除
     * @author Will
     * @date: 2023/11/8 14:15
     * @param ruleIdList
     */
    void deleteByRuleIdList(List<String> ruleIdList);
}
