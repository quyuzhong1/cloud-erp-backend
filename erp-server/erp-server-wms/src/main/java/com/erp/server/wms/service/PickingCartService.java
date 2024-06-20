package com.erp.server.wms.service;
import com.erp.model.wms.entity.PickingCartEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PickingCartDTO;

/**
 * <p>
 * 拣货车管理 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
public interface PickingCartService extends SuperService<PickingCartEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PickingCartDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    Boolean update(PickingCartDTO.UpdateDTO dto);


}
