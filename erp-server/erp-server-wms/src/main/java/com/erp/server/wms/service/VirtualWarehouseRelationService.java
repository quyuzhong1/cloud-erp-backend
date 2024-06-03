package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;

/**
 * <p>
 * 虚拟仓实体仓关联关系 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
public interface VirtualWarehouseRelationService extends SuperService<VirtualWarehouseRelationEntity> {

    /**
    * 新增
    * @author hyj
    * @date: 2024-06-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseRelationDTO.AddDTO dto);

    /**
    * 修改
    * @author hyj
    * @date: 2024-06-02
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseRelationDTO.UpdateDTO dto);


}
