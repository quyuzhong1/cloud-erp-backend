package com.erp.server.wms.service;
import com.erp.model.wms.entity.VirtualWarehouseChannelPartitionRefEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseChannelPartitionRefDTO;

import java.util.List;

/**
 * <p>
 * 虚拟仓渠道分区关联表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-01-03
 */
public interface VirtualWarehouseChannelPartitionRefService extends SuperService<VirtualWarehouseChannelPartitionRefEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-01-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(VirtualWarehouseChannelPartitionRefDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-01-03
    * @param dto
    * @return
    */
    Boolean update(VirtualWarehouseChannelPartitionRefDTO.UpdateDTO dto);

    /**
     * 根据mainId移除记录
     * @param oldChannelIds
     */
    void deleteByMainIds(List<String> oldChannelIds);

    /**
     * 根据mainId保存关系记录
     * @param id
     * @param partitionIds
     */
    void saveByMainId(String id, List<String> partitionIds);

    /**
     * 根据主键获取关联记录
     * @param ids
     * @return
     */
    List<VirtualWarehouseChannelPartitionRefEntity> listByMainIds(List<String> ids);
}
