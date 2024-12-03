package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;

/**
 * <p>
 * 虚拟仓库存历史信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualInventoryHisService extends SuperService<VirtualInventoryHisEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualInventoryHisDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(VirtualInventoryHisDTO.UpdateDTO dto);


}
