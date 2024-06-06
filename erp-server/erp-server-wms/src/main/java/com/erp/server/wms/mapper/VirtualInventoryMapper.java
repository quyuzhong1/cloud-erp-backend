package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 虚拟库存表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Mapper
public interface VirtualInventoryMapper extends BaseMapper<VirtualInventoryEntity> {

    /**
     * 查询列表数据总数
     * @author will
     * @date 2024/6/6 18:10
     * @param dto
     * @return Integer
     */
    Integer pagingCount(VirtualInventoryDTO.SearchParamDTO dto);


    /**
     * 虚拟库存分页查询
     * @author will
     * @date 2024/6/3 15:15
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualInventoryDTO.ListDTO> paging(Page query,@Param("params") VirtualInventoryDTO.SearchParamDTO params);
    /**
     * 根据SKU和虚拟仓库查询
     * @author will
     * @date 2024/6/3 15:46
     * @param skuIdList
     * @param virtualWarehouseIdList
     * @return List<VirtualInventoryDTO.ListInventoryDTO>
     */
    List<VirtualInventoryDTO.ListInventoryDTO> listVirtualWarehouseIdListAndSkuIdList(@Param("skuIdList") List<String> skuIdList,@Param("virtualWarehouseIdList") List<String> virtualWarehouseIdList);
    /**
     * 库存差异分页查询
     * @author will
     * @date 2024/6/3 16:38
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualInventoryDiffDTO.ListDTO> diffPaging(Page query,@Param("params") VirtualInventoryDiffDTO.SearchParamDTO params);
    /**
     * 库存差异分页明细查询
     * @author will
     * @date 2024/6/3 16:47
     * @param query
     * @param params
     * @return IPage<ListDetailDTO>
     */
    IPage<VirtualInventoryDiffDTO.ListDetailDTO> diffDetailPaging(Page query,@Param("params") VirtualInventoryDiffDTO.SearchParamDetailDTO params);

}
