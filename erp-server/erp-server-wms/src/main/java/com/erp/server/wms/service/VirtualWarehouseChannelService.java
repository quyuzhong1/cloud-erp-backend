package com.erp.server.wms.service;

import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;

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
     * 批量新增
     *
     * @param batchUpdateDTO
     * @return
     */
    BaseResultDTO.AddDTO batchUpdate(VirtualWarehouseChannelDTO.BatchUpdateDTO batchUpdateDTO);

    /**
     * 通过虚拟仓id获取关联渠道
     *
     * @param virtualWarehouseId
     */
    List<VirtualWarehouseChannelEntity> getByVirtualWarehouseId(String virtualWarehouseId);
    /**
     * 获取所有绑定的平台（聚合）
     */
    List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatform();
    /**
     * 获取所有绑定的平台（不聚合）
     */
    List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatformNoGroup();

    List<String> getBindedShopByDictPlatform(String dictPlatform);
    /**
     *  根据关联id、平台、实体仓库id查询
     * @author will
     * @date 2024/6/12 14:20
     * @param platformDTO
     * @return List<VirtualWarehouseRelationEntity>
     */
    List<VirtualWarehouseRelationEntity> getVirtualWarehouse(VirtualWarehouseChannelDTO.PlatformDTO platformDTO);
    /**
     * 根据关联id、平台、实体仓库id查询
     * @author will
     * @date 2024/6/19 19:28
     * @param listPlatformDTO
     * @return List<VirtualWarehouseRelationDTO.ListPlatformDTO>
     */
    List<VirtualWarehouseRelationDTO.ListPlatformDTO> listVirtualWarehouseByPlatform(VirtualWarehouseChannelDTO.ListPlatformDTO listPlatformDTO);
    /**
     * 根据平台查询虚拟仓配置信息
     * @author will
     * @date 2024/9/3 18:13
     * @param platformList
     * @return List<CfgRuleVirtualWarehouseDTO>
     */
    List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> listCfgRuleVirtualWarehouse(List<String> platformList);

    /**
     * 获取渠道配置信息
     * @param id
     * @return
     */
    VirtualWarehouseChannelDTO.ViewDTO view(String id);

    /**
     * 校验已绑定的渠道不能重复绑定
     * @param curChannelEntitieList
     * @param hasPartitionIds
     */
    void checkBoundChannel(List<VirtualWarehouseChannelEntity> curChannelEntitieList, Boolean hasPartitionIds);

    /**
     * 同一实体仓下不同虚拟仓不可重复配置B2B海外线下平台
     *
     * @param virtualWarehouseId 当前虚拟仓id
     * @param curChannelEntitieList 待保存渠道配置
     */
    void checkSameWarehouseB2bForeignPlatform(String virtualWarehouseId, List<VirtualWarehouseChannelEntity> curChannelEntitieList);
}
