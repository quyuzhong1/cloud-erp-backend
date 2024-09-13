package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PermissionsDTO;
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
     * @param params
     * @return Integer
     */
    Integer pagingCount(@Param("params") VirtualInventoryDTO.SearchParamDTO params);


    /**
     * 虚拟库存分页查询
     * @author will
     * @date 2024/6/3 15:15
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualInventoryDTO.ListDTO> paging(Page query,@Param("params") VirtualInventoryDTO.SearchParamDTO params, @Param("lastId") String lastId);
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
     * 库存差异数量
     * @author will
     * @date 2024/6/11 10:06
     * @param params
     * @return Integer
     */
    Integer diffPagingCount(@Param("params") PermissionsDTO params);
    /**
     * 库存差异分页明细查询
     * @author will
     * @date 2024/6/3 16:47
     * @param query
     * @param params
     * @return IPage<ListDetailQtyDTO>
     */
    IPage<VirtualInventoryDiffDTO.ListDetailQtyDTO> diffDetailPaging(Page query,@Param("params") VirtualInventoryDiffDTO.SearchParamDetailDTO params);
    /**
     * 查询导出数据
     * @author will
     * @date 2024/6/11 15:29
     * @param params
     * @return List<ListDiffExportDataDTO>
     */
    List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> listDiffExportData(@Param("params") VirtualInventoryDiffDTO.SearchParamDTO params);
    Page<VirtualInventoryDiffDTO.ListDiffExportDataDTO> listDiffExportData(@Param("page") Page<VirtualInventoryDiffDTO.ListDiffExportDataDTO> page, @Param("params") VirtualInventoryDiffDTO.SearchParamDTO params);

    /**
     * 获取虚拟仓可用数量
     * @param params
     * @return
     */
    List<VirtualInventoryDTO.ViewQtyDTO> getUsableQty(@Param("params")VirtualInventoryDTO.ParamDTO params);

    /**
     * 获取虚拟仓实际数量
     * @param params
     * @return
     */
    List<VirtualInventoryDTO.ViewQtyDTO> getRealQty(@Param("params")VirtualInventoryDTO.ParamDTO params);
    /**
     * 根据实体仓库id集合查询虚拟可用库存数量
     * @author will
     * @date 2024/6/12 17:28
     * @param params
     * @return List<VirtualInventoryQtyDTO>
     */
    List<VirtualInventoryDTO.VirtualInventoryQtyDTO> listInventoryQty(@Param("params")VirtualInventoryDTO.VirtualInventoryParamDTO params);

    /**
     * 获取可用数量
     */
    Integer findUsableQtyByQtyDto(@Param("params") VirtualInventoryDTO.VirtualInventoryQtyDTO params);
    /**
     * 根据实体仓库查询虚拟库存数量
     * @author will
     * @date 2024/6/18 9:24
     * @param warehouseId
     * @return Integer
     */
    Integer getInventoryQtyByWarehouseId(@Param("warehouseId") String warehouseId,@Param("skuId")String skuId);

    /**
     * 根据实体仓库查询虚拟库存数量
     * @author will
     * @date 2024/6/18 9:24
     * @param warehouseIdList
     * @param skuIdList
     * @return Integer
     */
    List<VirtualInventoryDTO.WarehouseInventoryQtyDTO> listInventoryQtyByWarehouseId(@Param("warehouseIdList") List<String> warehouseIdList,@Param("skuIdList")List<String> skuIdList);

    List<VirtualInventoryDTO.CommonDTO> getBySkuIdAndVwId(@Param("skuId")String skuId, @Param("virtualWarehouseId")String virtualWarehouseId);
    /**
     * 查询仓库统计数据
     * @author will
     * @date 2024/7/24 17:59
     * @return List<WarehouseStatisticsExcelDTO>
     */
    List<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> listWarehouseStatistics(@Param("params") VirtualInventoryDiffDTO.SearchParamDTO params, @Param("lastId") String lastId);
    Page<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> listWarehouseStatistics(@Param("page")  Page<VirtualInventoryDiffDTO.SearchParamDTO> page, @Param("params") VirtualInventoryDiffDTO.SearchParamDTO params, @Param("lastId") String lastId);
}
