package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateOtherCostService extends SuperService<ShippingTemplateOtherCostEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShippingTemplateOtherCostDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    Boolean update(ShippingTemplateOtherCostDTO.UpdateDTO dto);


}
