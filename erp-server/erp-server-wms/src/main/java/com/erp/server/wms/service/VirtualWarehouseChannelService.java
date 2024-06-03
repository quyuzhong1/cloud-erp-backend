package com.erp.server.wms.service;

import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    BaseResultDTO.AddDTO add(VirtualWarehouseChannelDTO.AddDTO dto);

    /**
     * 批量新增
     *
     * @param batchAddDTO
     * @return
     */
    BaseResultDTO.AddDTO batchAdd(VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO);


    /**
     * 修改
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-06-02
     */
    Boolean update(VirtualWarehouseChannelDTO.UpdateDTO dto);

    /**
     * 通过虚拟仓id获取关联渠道
     *
     * @param virtualWarehouseId
     */
    List<VirtualWarehouseChannelEntity> getByVirtualWarehouseId(String virtualWarehouseId);
}
