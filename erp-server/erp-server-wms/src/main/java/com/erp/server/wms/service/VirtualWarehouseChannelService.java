package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;

/**
 * <p>
 * 虚拟仓渠道 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
public interface VirtualWarehouseChannelService extends SuperService<VirtualWarehouseChannelEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-02
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseChannelDTO.UpdateDTO dto);


}
