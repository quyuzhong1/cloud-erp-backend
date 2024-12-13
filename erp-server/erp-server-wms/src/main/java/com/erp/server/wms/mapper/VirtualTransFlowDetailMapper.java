package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.entity.VirtualTransFlowDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 虚拟仓库存流水明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualTransFlowDetailMapper extends BaseMapper<VirtualTransFlowDetailEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/5 10:57
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualTransFlowDetailDTO.ListDTO> paging(Page<VirtualTransFlowDetailDTO.SearchParamDTO> page,@Param("params") VirtualTransFlowDetailDTO.SearchParamDTO params);
    /**
     * 查询虚拟仓库存明细id集合
     * @author will
     * @date 2024/12/12 14:53
     * @param virtualInventoryId
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param fromTable
     * @return List<String>
     */
    List<String> listVirtualInventoryDetailIdList(@Param("virtualInventoryId")String virtualInventoryId,@Param("virtualWarehouseId") String virtualWarehouseId,@Param("warehouseId") String warehouseId,@Param("skuId") String skuId,@Param("fromTable") Boolean fromTable);
    /**
     * 查询虚拟仓明细库存
     * @author will
     * @date 2024/12/12 15:26
     * @param virtualInvDetailId
     * @param startDate
     * @return Integer
     */
    Integer virtualDetailQty(@Param("virtualInvDetailId")String virtualInvDetailId,@Param("startDate") LocalDate startDate);
}
