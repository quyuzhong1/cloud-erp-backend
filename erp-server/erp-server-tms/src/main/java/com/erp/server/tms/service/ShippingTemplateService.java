package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateDTO;

/**
 * <p>
 * 运费模板 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateService extends SuperService<ShippingTemplateEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShippingTemplateDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    Boolean update(ShippingTemplateDTO.UpdateDTO dto);


}
