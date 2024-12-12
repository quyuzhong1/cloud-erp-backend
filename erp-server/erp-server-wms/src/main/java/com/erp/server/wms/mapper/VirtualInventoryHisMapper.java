package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 虚拟仓库存历史信息 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-10
 */
@Mapper
public interface VirtualInventoryHisMapper extends BaseMapper<VirtualInventoryHisEntity> {
    /**
     * 查询每日库存结余数据
     * @author will
     * @date 2024/12/11 10:12
     * @return List<AddDTO>
     */
    List<VirtualInventoryHisDTO.AddDTO> listVirtualInventoryHis(@Param("localDate") LocalDate localDate);
    /**
     * 根据关联id和日期查询
     * @author will
     * @date 2024/12/11 11:13
     * @param virtualInventoryIdList
     * @param dateList
     * @return List<VirtualInventoryHisEntity>
     */
    List<VirtualInventoryHisEntity> listByVirtualInventoryIdList(@Param("virtualInventoryIdList")List<String> virtualInventoryIdList,@Param("dateList") List<LocalDate> dateList);
    /**
     * 根据sku、仓库、虚拟仓、快照日期查询库存
     * @author will
     * @date 2024/12/11 12:20
     * @param skuIdList
     * @param warehouseIdList
     * @param virtualWarehouseIdList
     * @param localDate
     * @return List<VirtualQtyDTO>
     */
    List<VirtualInventoryHisDTO.VirtualQtyDTO> listInventoryQty(@Param("skuIdList")List<String> skuIdList,@Param("warehouseIdList") List<String> warehouseIdList,@Param("virtualWarehouseIdList") List<String> virtualWarehouseIdList,@Param("localDate")LocalDate localDate);
}
