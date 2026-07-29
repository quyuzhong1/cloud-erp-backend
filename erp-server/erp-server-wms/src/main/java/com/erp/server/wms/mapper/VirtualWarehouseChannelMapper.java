package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓渠道 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Mapper
public interface VirtualWarehouseChannelMapper extends BaseMapper<VirtualWarehouseChannelEntity> {
    /**
     * 获取所有绑定的平台（聚合）
     */
    List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatform();

    /**
     * 获取所有绑定的平台（不聚合）
     */
    List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatformNoGroup();

    /**
     * 获取当前渠道绑定的店铺
     *
     * @param dictPlatform
     * @return
     */
    List<String> getBindedShopByDictPlatform(@Param("dictPlatform") String dictPlatform);

    /**
     * 按平台/店铺/分区/实体仓匹配虚拟仓渠道，直接返回命中虚拟仓与入参实体仓的关联列表。
     * 多条渠道时：精确店铺/分区优先于空通配，再按渠道 id；仅取一个虚拟仓下的关联行。
     *
     * @param platformDTO 平台、店铺、分区、实体仓条件
     * @return 虚拟仓-实体仓关联列表，无匹配时为空列表
     */
    List<VirtualWarehouseRelationEntity> listRelationByPlatform(@Param("platformDTO") VirtualWarehouseChannelDTO.PlatformDTO platformDTO);

    /**
     * 根据平台参数查询
     *
     * @param listPlatformDTO
     * @return List<VirtualWarehouseChannelEntity>
     * @author will
     * @date 2024/6/13 12:27
     */
    List<VirtualWarehouseChannelEntity> listVirtualWarehouseByPlatform(@Param("listPlatformDTO") VirtualWarehouseChannelDTO.ListPlatformDTO listPlatformDTO);

    /**
     * 查询同一实体仓下已配置 B2B 海外线下平台的其他虚拟仓冲突信息。
     * SQL 见 VirtualWarehouseChannelMapper.xml#findB2bForeignConflicts，参数均使用 #{} 预编译占位。
     */
    List<VirtualWarehouseDTO.B2bForeignConflictDTO> findB2bForeignConflicts(@Param("virtualWarehouseId") String virtualWarehouseId,
                                                                              @Param("b2bForeignPlatform") String b2bForeignPlatform);
}
