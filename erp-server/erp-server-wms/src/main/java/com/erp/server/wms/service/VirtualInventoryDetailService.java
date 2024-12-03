package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;

/**
 * <p>
 * 虚拟仓库明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualInventoryDetailService extends SuperService<VirtualInventoryDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualInventoryDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(VirtualInventoryDetailDTO.UpdateDTO dto);


}
