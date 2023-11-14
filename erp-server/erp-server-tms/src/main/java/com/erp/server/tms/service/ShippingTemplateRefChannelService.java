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
    * @param addList
    * @return
    */
    Boolean add(List<ShippingTemplateRefChannelDTO.AddDTO> addList,String mainId);

    /**
     * @description: 根据主表ids查询
    * @author Will
     * @date: 2023/11/6 16:58
     * @param idList
     * @return List<ShippingTemplateRefChannelDTO.ViewDTO>
    */
    List<ShippingTemplateRefChannelDTO.ViewDTO> listByMainIds(List<String> idList);

    /**
     * 根据渠道id 集合获取对应数据
     *
     *@parms channelIdList
     *@return 
     *@author yl
     *@date 2023-11-14
     */
    List<ShippingTemplateRefChannelEntity> listChannelIdList(List<String> channelIdList);
}
