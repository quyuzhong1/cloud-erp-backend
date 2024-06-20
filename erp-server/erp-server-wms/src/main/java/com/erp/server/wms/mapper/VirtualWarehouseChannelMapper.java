package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
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

    List<VirtualWarehouseDTO.BindChannelDto> getBindedDictPlatform();

    List<String> getBindedShopByDictPlatform(@Param("dictPlatform") String dictPlatform);
    /**
     * 根据平台参数查询
     * @author will
     * @date 2024/6/13 12:27
     * @param platformDTO
     * @return VirtualWarehouseChannelEntity
     */
    VirtualWarehouseChannelEntity getByPlatform(@Param("platformDTO")VirtualWarehouseChannelDTO.PlatformDTO platformDTO);
    /**
     * 根据平台参数查询
     * @author will
     * @date 2024/6/13 12:27
     * @param listPlatformDTO
     * @return List<VirtualWarehouseChannelEntity>
     */
    List<VirtualWarehouseChannelEntity> listVirtualWarehouseByPlatform(@Param("listPlatformDTO")VirtualWarehouseChannelDTO.ListPlatformDTO listPlatformDTO);
}
