package com.erp.server.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import com.erp.model.wms.dto.SupplierInventoryDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 虚拟库存表 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
public interface VirtualInventoryService extends SuperService<VirtualInventoryEntity> {

    /**
     * 虚拟库存列表
     * @author will
     * @date 2024/6/3 14:58
     * @param dto PagingVO<ListDTO>
     */
    PagingVO<VirtualInventoryDTO.ListDTO> paging(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto);
    /**
     * 库存差异明细导出
     *
     * @param dto
     * @return Boolean
     * @author will
     * @date 2024/6/3 17:17
     */
    Boolean exportExcel(VirtualInventoryDTO.SearchParamDTO dto);
    /**
     * 根据仓库、sku、库存状态查询
     * @author will
     * @date 2024/6/4 12:19
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param inventoryStatus
     * @return VirtualInventoryEntity
     */
    VirtualInventoryEntity findVirtualInventoryStock(String virtualWarehouseId,String warehouseId, String skuId, String inventoryStatus);
    /**
     * 保存虚拟库存数据
     * @author will
     * @date 2024/6/4 14:14
     * @param virtualWarehouseId
     * @param warehouseId
     * @param skuId
     * @param skuNo
     * @param inventoryStatus
     * @param qty
     * @return VirtualInventoryEntity
     */
    VirtualInventoryEntity addOrUpdate(String virtualWarehouseId,String warehouseId , String skuId, String skuNo, String inventoryStatus, Integer qty);

    /**
     * 更新库存数量
     * @author will
     * @date 2024/6/4 14:37
     * @param id
     * @param qty
     * @return boolean
     */
    boolean updateQtyById(String id, Integer qty);

    /**
     * 根据条件查询库存信息
     *
     * @param qtyTypeDTO
     * @author hyj
     * @date 2024/6/6
     */
    List<VirtualInventoryDTO.ViewQtyDTO> getQty(VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO);

    /**
     * 根据skuIds warehouseIds vmIds获取虚拟仓可用库存
     *
     * @param vmParamDto
     * @return
     */
    List<VirtualInventoryDTO.ViewQtyDTO> getVmUsableQtyBySkuIdsAndWIdsAndVmIds(VirtualInventoryDTO.ParamDTO vmParamDto);

    /**
     * 根据实体仓库id集合查询虚拟库存可用数量
     * @author will
     * @date 2024/6/12 17:26
     * @param paramDTO
     * @return List<VirtualInventoryQtyDTO>
     */
    List<VirtualInventoryDTO.VirtualInventoryQtyDTO> listInventoryQty(VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO);

    /**
     * 获取可用数量
     */
    Integer findUsableQtyByQtyDto(VirtualInventoryDTO.VirtualInventoryQtyDTO virtualInventoryQtyDTO);
    /**
     * 根据实体仓库查询虚拟库存数量
     * @author will
     * @date 2024/6/18 9:23
     * @param warehouseId
     * @param skuId
     * @return Integer
     */
    Integer getInventoryQtyByWarehouseId(String warehouseId,String skuId);

    /**
     * 根据实体仓库查询虚拟库存数量
     * @author will
     * @date 2024/8/8 9:57
     * @param warehouseIdList
     * @param skuIdList
     * @return List<WarehouseInventoryQtyDTO>
     */
    List<VirtualInventoryDTO.WarehouseInventoryQtyDTO> listInventoryQtyByWarehouseId(List<String> warehouseIdList, List<String> skuIdList);

    /**
     * 根据sku和虚拟仓id获取是否存在关联关系
     * @param skuId
     * @param id
     * @return
     */
    List<VirtualInventoryDTO.CommonDTO> getBySkuIdAndVwId(String skuId, String id);
    /**
     * bom库存信息
     * @author will
     * @date 2024/8/6 15:11
     * @param paramDTO
     * @return List<BomReturnDTO>
     */
    List<VirtualInventoryDTO.BomReturnDTO> listBomVirtual(VirtualInventoryDTO.BomParamDTO paramDTO);
    /**
     * SKU可用库存
     * @author will
     * @date 2024/8/8 18:28
     * @param paramList
     * @return SkuReturnDTO
     */
    List<VirtualInventoryDTO.SkuReturnDTO> listSkuVirtualInventoryQty(List<VirtualInventoryDTO.BomParamDTO> paramList);

    PagingVO<VirtualInventoryDTO.ListDTO> getVirtualInventory(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto);

    /**
     * 获取虚拟仓实际数量
     * @param params
     * @return
     */
    List<VirtualInventoryDTO.ViewQtyDTO> getRealQty(VirtualInventoryDTO.ParamDTO params);


    /**
     * 分页查询
     * @author will
     * @date 2025/6/19 15:18
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<SupplierInventoryDTO.ListDTO> supplierInventoryPaging(Page query, SupplierInventoryDTO.PagingParamDTO params, List<SupplierRefWarehouseEntity> supplierRefWarehouseList);
}
