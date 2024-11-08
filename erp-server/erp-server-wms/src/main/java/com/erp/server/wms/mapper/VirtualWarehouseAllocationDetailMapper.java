package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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

    void batchManualFinish(@Param("params") VirtualWarehouseAllocationDTO.ManualFinishDto params, @Param("code") Integer code, @Param("ids") List<String> ids);
    void batchSync( @Param("code")String code, @Param("ids") List<String> ids);

    void updateSyncStatus(@Param("params")VirtualWarehouseAllocationDTO.SyncUpdateDto dto, @Param("ids")List<String> ids);
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
}
