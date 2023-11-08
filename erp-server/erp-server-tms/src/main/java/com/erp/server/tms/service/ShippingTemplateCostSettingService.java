package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;

import java.util.List;

/**
 * <p>
 * 运费模板其他费用选值表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateCostSettingService extends SuperService<ShippingTemplateCostSettingEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param addList
    * @return
    */
    Boolean add(List<ShippingTemplateCostSettingDTO.AddDTO> addList,List<String> otherCostIdList );
    /**
     * @description: 根据其他费用id查询
     * @author Will
     * @date: 2023/11/8 11:50
     * @param otherCostIdList
     * @return List<ShippingTemplateCostSettingEntity>
     */
    List<ShippingTemplateCostSettingEntity> listByOtherCostIds(List<String> otherCostIdList);
    /**
     * @description: 根据其他费用ids删除
     * @author Will
     * @date: 2023/11/8 14:27
     * @param otherCostIdList
     */
    void deleteByOtherCostIds(List<String> otherCostIdList);
}
