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

    List<VirtualWarehouseDTO.BindChannelDto> getByParams(VirtualWarehouseChannelDTO.ChannelAddDTO newChannel);
    /**
     * 根据平台查询虚拟仓配置信息
     * @author will
     * @date 2024/9/3 18:13
     * @param platformList
     * @return List<CfgRuleVirtualWarehouseDTO>
     */
    List<VirtualWarehouseDTO.CfgRuleVirtualWarehouseDTO> listCfgRuleVirtualWarehouse(List<String> platformList);
}
