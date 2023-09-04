package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.dto.inventory.InventorySaveDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingPlanEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Classname: InventoryService
 * @Description: 实时库存服务接口类
 * @CreateTime: 2023-04-25  12:15
 * @Author: zhangchunlin
 */
public interface InventoryService extends SuperService<InventoryEntity> {

    /**
     * 根据组织、仓库、库位、状态判断库存是否存在记录（库位为空也作为条件）;不对外使用
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocation
     * @param status
     * @return
     */
    InventoryEntity findInventory(String orgId,String warehouseId,String skuId, String warehouseLocation,String status);


    /**
     * 根据组织、仓库、库位、状态判断库存是否存在记录，带分布式锁，会根据是否控制库位查询（不控制库位则将库位查询条件置为空字符串）
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocation
     * @param status
     * @return
     */
    InventoryEntity findInventoryLock(String orgId,String warehouseId,String skuId, String warehouseLocation,String status);


    /**
     * 根据组织、仓库、库位、状态判断库存数据；特别注意：如果库位为空，则库位赋值空库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocation
     * @param status
     * @return
     */
    InventoryEntity findInventoryIncLocation(String orgId,String warehouseId,String skuId, String warehouseLocation,String status);

    /**
     * 根据组织、仓库、库位、状态判断库存数据；特别注意：如果库位为空，则不带库位库位查询条件
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocation
     * @param status
     * @return
     */
    List<InventoryEntity> findInventoryCheckLocation(String orgId,String warehouseId,String skuId, String warehouseLocation,String status);

    /**
     * 根据仓库、库位、状态获取可用库存数量；如果库位为空，则赋值空库位
     * @param warehouseId
     * @param skuId
     * @param warehouseLocation
     * @return
     */
    Integer getUsableInventoryTotal(String warehouseId,String skuId, String warehouseLocation);

    /**
     * 根据仓库、SKU获取可用库存数量；特别注意：不带库位查询条件
     * @param warehouseId
     * @param skuId
     * @return
     */
    Integer getUsableInventoryTotal(String warehouseId,String skuId);

    /**
     * 新增或修改库存
     * @param warehouseId
     * @param orgId
     * @param warehouseLocation
     * @param skuId
     * @param skuNo
     * @param inventoryStatus
     * @param qty
     * @return
     */
    InventorySaveDTO addOrUpdate(String warehouseId, String orgId, String warehouseLocation, String skuId, String skuNo, String inventoryStatus, Integer qty);



    /**
     * 根据skuIds 仓库 ，组织 仓位 获取到 sku即时库存（库位没传，则查询空库位）
     * @author yl
     * @date 2023-05-16 17:06
     * @param skuIds
     * @param warehouseId
     * @param warehouseLocation
     * @return java.util.List<com.erp.model.wms.dto.InventoryDTO.SkuInventoryTotalDTO>
     */
    List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(List<String> skuIds, String warehouseId, String warehouseLocation, String status);


    /**
     *
     * @author yl
     * @date 2023-05-22 12:26
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.inventory.InventoryQtyDTO.SkuInventoryTotalDTO>
     */
    List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryParamDTO dto);

    /**
     * @description: 根据skuIds、仓库 、组织、仓位、库存状态 批量获取到 sku即时库存（库位没传，则查询空库位）
     * @author Will
     * @date: 2023/8/21 16:13
     * @param dto
     * @return List<SkuInventoryStatusTotalDTO>
     */
    List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryStatusParamDTO dto);


    /**
     * 根据组织、仓库、库位、状态、SKU获取库存数量；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocation
     * @param status
     * @return
     */
    Integer getInventoryTotal(String orgId,String warehouseId,String skuId, String warehouseLocation,String status);

    /**
     * 修改库存表数量
     * @param id
     * @param qty
     * @return
     */
    boolean updateQtyById(String id, Integer qty);

    /**
     * @description: 查询拣货数据
     * @author Will
     * @date: 2023/5/12 15:48
     * @param dto
     * @return List<InventoryEntity>
     */
    List<InventoryEntity> listPickingDetailInventory(PickingDetailDTO.InventoryParamDTO dto);


    /**
     * 即时库存分页列表
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InventoryDTO.PagingViewDTO> paging(PagingDTO<InventoryDTO.SearchParamDTO> pagingParamDTO);

    /**
     * 导出即时库存Excel
     * @param param
     * @param response
     */
    void exportExcel(InventoryDTO.ExportSearchParamDTO param, HttpServletResponse response);

    /**
     * 库龄计算表分页列表
     * @param pagingParamDTO
     * @return
     */
    PagingVO<LinkedHashMap> inventoryAgePaging(PagingDTO<InventoryReportDTO.InventoryAgeSearchParamDTO> pagingParamDTO);

    /**
     * 库龄计算表导出
     * @param paramDTO
     * @return
     */
    void exportInventoryAge(InventoryReportDTO.ExportInventoryAgeSearchParamDTO paramDTO, HttpServletResponse response);

    /**
     * 根据仓库id查询库存信息
     * @Author Luo_WG
     * @Date 2023/8/10 10:23
     * @param warehouseId
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaHomeInventoryBalanceDTO
     **/
    InventoryDTO.PdaHomeInventoryBalanceDTO getInventoryByWarehouseId(String warehouseId);
    /**
     * 根据盘点类型查询库存
     * @param entity
     * @param detailEntityList
     * @return
     */
    List<InventoryEntity> listByStocktakingType(StocktakingPlanEntity entity, List<StocktakingPlanDetailEntity> detailEntityList);

    /**
     * 根据条件查询库存信息
     * @Author Luo_WG
     * @Date 2023/8/25 18:00
     * @param list
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventoryDTO
     **/
    List<InventoryDTO.PdaInventoryDTO> getInventoryByParam(InventoryDTO.PdaSearchParamDTO list);

    /**
     * PDA:库存查询
     * @Author Luo_WG
     * @Date 2023/8/30 11:39
     * @param skuNo
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventorySearch
     **/
    InventoryDTO.PdaInventorySearch getInventoryBySkuNo(String skuNo);
}
