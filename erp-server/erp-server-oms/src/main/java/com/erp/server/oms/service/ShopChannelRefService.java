package com.erp.server.oms.service;
import com.erp.model.oms.entity.ShopChannelRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.ShopChannelRefDTO;
import com.erp.model.oms.entity.ShopInfoEntity;

import java.util.List;

/**
 * <p>
 * 店铺渠道关联表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-02-14
 */
public interface ShopChannelRefService extends SuperService<ShopChannelRefEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-02-14
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShopChannelRefDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-02-14
    * @param dto
    * @return
    */
    Boolean update(ShopChannelRefDTO.UpdateDTO dto);


    void batchUpdate(ShopInfoEntity shop, List<String> channelIdList);

    List<ShopChannelRefDTO.ViewDTO> getViewByShopId(String id);
}
