package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;

import java.util.List;

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

    /**
     * 根据paramDTO参数查询
     * @author will
     * @date 2024/12/6 11:50
     * @param paramDTO
     * @return List<VirtualInventoryHisEntity>
     */
    List<VirtualInventoryHisEntity> listByParam(VirtualInventoryHisDTO.ParamDTO paramDTO);
    /**
     * 历史库存任务保存
     * @author will
     * @date 2024/12/9 19:23
     */
    void hisVirtualInventoryJob();
}
