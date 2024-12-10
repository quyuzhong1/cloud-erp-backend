package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟仓库明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualInventoryDetailMapper extends BaseMapper<VirtualInventoryDetailEntity> {
    /**
     * 分页列表
     * @author will
     * @date 2024/12/3 17:41
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualInventoryAgeDTO.ListDTO> paging(Page<VirtualInventoryAgeDTO.SearchParamDTO> page,@Param("params") VirtualInventoryAgeDTO.SearchParamDTO params);
    /**
     * 历史库龄
     * @author will
     * @date 2024/12/5 9:45
     * @param page
     * @param params
     * @return IPage<HisInventoryAgeDTO>
     */
    IPage<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(Page<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> page, @Param("params") VirtualInventoryAgeDTO.HisInventoryAgeParamDTO params);
    /**
     * 历史库龄明细
     * @author will
     * @date 2024/12/5 10:18
     * @param page
     * @param params
     * @return IPage<HisInventoryAgeDetailDTO>
     */
    IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(Page<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> page,@Param("params") VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO params);
    /**
     * 查询历史库龄图数据
     * @author will
     * @date 2024/12/9 9:51
     * @param params
     * @return List<HisInventoryAgeDTO>
     */
    List<VirtualInventoryAgeDTO.HisInventoryAgeDTO> getHisInventoryAgeChart(@Param("params")VirtualInventoryAgeDTO.HisInventoryAgeParamDTO params);
    /**
     * 导出历史库龄明细
     * @author will
     * @date 2024/12/9 10:57
     * @param page
     * @param params
     * @return IPage<HisInventoryAgeDetailDTO>
     */
    IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> exportHisInventoryAgeDetailPaging(Page<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> page,@Param("params") VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO params);
    /**
     * 根据出库查询
     * @author will
     * @date 2024/12/10 16:27
     * @param skuId
     * @param warehouseId
     * @param virtualWarehouseId
     * @return List<VirtualInventoryDetailEntity>
     */
    List<VirtualInventoryDetailEntity> getByOutParam(@Param("skuId")String skuId,@Param("warehouseId") String warehouseId,@Param("virtualWarehouseId") String virtualWarehouseId);
}
