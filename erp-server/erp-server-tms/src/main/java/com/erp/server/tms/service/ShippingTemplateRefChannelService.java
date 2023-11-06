package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateRefChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateRefChannelDTO;

import java.util.List;

/**
 * <p>
 * 运费模板渠道关联表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateRefChannelService extends SuperService<ShippingTemplateRefChannelEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShippingTemplateRefChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    Boolean update(ShippingTemplateRefChannelDTO.UpdateDTO dto);

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/11/6 16:58
     * @param idList
     * @return List<ShippingTemplateRefChannelDTO.ViewDTO>
     */
    List<ShippingTemplateRefChannelDTO.ViewDTO> listByMainIds(List<String> idList);
}
