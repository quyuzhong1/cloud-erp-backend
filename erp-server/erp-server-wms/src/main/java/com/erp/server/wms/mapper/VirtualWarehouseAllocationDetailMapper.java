package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓分货单明细 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Mapper
public interface VirtualWarehouseAllocationDetailMapper extends BaseMapper<VirtualWarehouseAllocationDetailEntity> {

    /**
     * 查询分货信息
     * @author will
     * @date 2024/9/29 14:49
     * @param skuIdList
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @return List<AllocationDataDTO>
     */
    List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> listAllocationData(@Param("skuIdList")List<String> skuIdList,@Param("warehouseIdList") List<String> warehouseIdList,@Param("virtualWarehouseIdList") List<String> virtualWarehouseIdList);
   /**
     * 查询重复处理的分货明细
     * @author will 
     * @date 2025/12/29 14:10
     * @param fromWarehouseIdList
     * @return VirtualWarehouseAllocationDetailEntity
     */
    List<VirtualWarehouseAllocationDetailEntity> listRepeatHandleDetail(@Param("fromWarehouseIdList")List<String> fromWarehouseIdList,@Param("fromVirtualWarehouseIdList") List<String> fromVirtualWarehouseIdList,@Param("skuIdList") List<String> skuIdList,@Param("detailIdList") List<String> detailIdList);
}
