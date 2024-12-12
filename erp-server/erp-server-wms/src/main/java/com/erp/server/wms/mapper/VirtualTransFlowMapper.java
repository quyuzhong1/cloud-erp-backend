package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 虚拟库存交易流水表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Mapper
public interface VirtualTransFlowMapper extends BaseMapper<VirtualTransFlowEntity> {

    /**
     * 查询列表数据总数
     * @author will
     * @date 2024/6/6 18:07
     * @param params
     * @return Integer
     */
    Integer pagingCount(@Param("params") VirtualTransFlowDTO.SearchParamDTO params);

    /**
     * 虚拟库存流水分页查询
     * @author will
     * @date 2024/6/3 17:10
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualTransFlowDTO.ListDTO> paging(Page query,@Param("params") VirtualTransFlowDTO.SearchParamDTO params);
    /**
     * 更新流水状态为反审核
     * @author will
     * @date 2024/6/5 10:13
     * @param id
     * @param version
     * @param updateTime
     * @param updateUserId
     * @param updateUserName
     * @return Boolean
     */
    Boolean updateUnapprovedById(@Param("id")String id, @Param("version")Integer version, @Param("updateTime")LocalDateTime updateTime
            , @Param("updateUserId")String updateUserId, @Param("updateUserName")String updateUserName);
    /**
     * 虚拟库存明细列表
     * @author will
     * @date 2024/6/5 16:09
     * @param query
     * @param params
     * @return IPage<InventoryDetailDTO>
     */
    IPage<VirtualTransFlowDTO.InventoryDetailDTO> detailPaging(Page query,@Param("params") VirtualTransFlowDTO.InventoryDetailParamDTO params);
    /**
     * 查询虚拟仓即时库存id
     * @author will
     * @date 2024/12/12 11:47
     * @param virtualInventoryId
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param fromTable
     * @return List<String>
     */
    List<String> listVirtualInventoryId(@Param("virtualInventoryId")String virtualInventoryId,@Param("virtualWarehouseId") String virtualWarehouseId,@Param("warehouseId") String warehouseId,@Param("skuId") String skuId,@Param("fromTable") Boolean fromTable);
    /**
     * 查询流水虚拟仓库存数量
     * @author will
     * @date 2024/12/12 14:34
     * @param virtualInvId
     * @param startDate
     * @return Integer
     */
    Integer getVirtualQty(@Param("virtualInvId")String virtualInvId,@Param("startDate") LocalDate startDate);
}
