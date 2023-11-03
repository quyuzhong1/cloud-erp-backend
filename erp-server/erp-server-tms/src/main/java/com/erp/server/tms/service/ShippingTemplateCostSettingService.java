package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;

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
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShippingTemplateCostSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    Boolean update(ShippingTemplateCostSettingDTO.UpdateDTO dto);


}
