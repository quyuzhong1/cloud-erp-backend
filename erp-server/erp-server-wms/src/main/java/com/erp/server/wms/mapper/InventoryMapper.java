package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Classname: InventoryMapper

 * @CreateTime: 2023-04-25  15:30
 * @Author: zhangchunlin
 */
@Mapper
@Repository
public interface InventoryMapper extends BaseMapper<InventoryEntity> {

    /**
     * 修改库存表数量
     * @param id
     * @param qty
     * @return
     */
    int updateQtyById(@Param(value = "id") String id, @Param(value = "qty") Integer qty,
                      @Param(value = "updateTime") LocalDateTime updateTime, @Param(value = "updateUserId") String updateUserId, @Param(value = "updateUserName") String updateUserName);


    /**
     * 分页查询即时库存
     * @param query
     * @param params
     * @return
     */
    IPage<InventoryDTO.PagingViewDTO> page(Page query, @Param("params") InventoryDTO.SearchParamDTO params);

    /**
     * 即时库存导出
     * @param params
     * @return
     */
    List<InventoryDTO.PagingViewDTO> exportInv(@Param("params") InventoryDTO.ExportSearchParamDTO params);

    /**
     * 库龄计算表分页查询
     * @param params
     * @return
     */
    IPage<LinkedHashMap> inventoryAgePage(Page query, @Param("params") InventoryReportDTO.InventoryAgeSearchParamDTO params);

    /**
     * 库龄计算表导出
     * @param params
     * @return
     */
    Page<LinkedHashMap> exportInventoryPage(@Param("page") Page<LinkedHashMap> page,@Param("params") InventoryReportDTO.ExportInventoryAgeSearchParamDTO params);

    /**
     * 库存分页查询
     * @param type
     * @param startTime
     * @param endTime
     * @param detailEntityList
     * @return
     */
    List<InventoryEntity> listByStocktakingType(@Param("type") String type,@Param("startTime")LocalDateTime startTime,
                                                @Param("endTime")LocalDateTime endTime, @Param("params") List<StocktakingPlanDetailEntity> detailEntityList);
    /**
     * 根据仓库id查询库存信息
     * @Author Luo_WG
     * @Date 2023/8/10 10:23
     * @param warehouseId
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaHomeInventoryBalanceDTO
     **/
    List<InventoryDTO.PdaHomeInventoryBalanceDTO> getInventoryByWarehouseId(@Param("warehouseId") String warehouseId, @Param("status") String status);

    /**
     * 根据条件查询库存信息
     * @Author Luo_WG
     * @Date 2023/8/25 18:13
     * @param params
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventoryDTO
     **/
    List<InventoryDTO.PdaInventoryDTO> getInventoryByParam(@Param("params") InventoryDTO.PdaSearchParamDTO params);

    /**
     * 根据条件查询库存信息
     * @author hyj
     * @date 2024/4/17 10:53
     * @param params
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventoryDTO
     **/
    InventoryDTO.InventoryViewQtyDTO getInventoryInfoByParam(@Param("params") InventoryDTO.InventoryBySkuIdAndWarehouseDTO params);

    /**
     * 根据条件查询仓库信息（分页）
     * @Author Luo_WG
     * @Date 2023/8/25 18:13
     * @param params
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventoryDTO
     **/
    IPage<InventoryDTO.PdaInventoryWarehouseDTO> pageInventoryWarehouseByParam(Page query,@Param("params") InventoryDTO.InventoryBySkuNoDTO params);

    /**
     * 根据条件查询仓库信息（列表）
     * @param params
     * @return
     */
    List<InventoryDTO.PdaInventoryWarehouseLocationDTO> listInventoryWarehouseByParam(@Param("params") InventoryDTO.InventoryBySkuNoDTO params);

    /**
     * 根据条件查询仓库信息（分页）
     * @Author Luo_WG
     * @Date 2023/8/25 18:13
     * @param params
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventoryDTO
     **/
    IPage<InventoryDTO.PdaInventoryPageDTO> pageInventoryWarehouseBySkuId(Page query,@Param("params") InventoryDTO.InventoryBySkuNoDTO params);
    /**
     * 根据条件查询仓库信息（列表）
     * @param params
     * @return
     */
    List<InventoryDTO.PdaInventoryWarehouseLocationDTO> listInventoryWarehouseBySkuId(@Param("params") InventoryDTO.InventoryBySkuNoDTO params);
    /**
     * 根据条件查询仓库下的仓位库存信息
     * @Author Luo_WG
     * @Date 2023/8/25 18:13
     * @param params
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.PdaInventoryDTO
     **/
    List<InventoryDTO.PdaInventoryWarehouseLocationDTO> listInventoryWarehouseLocationByParam(@Param("params") InventoryDTO.InventoryBySkuNoDTO params);


    /**
     * 根据条件查询库存信息
     * @return
     */
    List<InventoryEntity> listByParam(@Param("params")InventoryDTO.ParamDTO params );

    List<InventoryDTO.InventoryViewQtyDTO> getUsableQtyBySkuIdsAndWarehouseIds(@Param("params")InventoryDTO.ParamDTO params);

    /**
     * 以库区的维度查询库存信息
     * @date 2024/06/04
     */
    IPage<InventoryDTO.PagingViewDTO> pageByArea(@Param("query") Page query, @Param("params") InventoryDTO.SearchParamDTO params);

    /**
     * 以仓位的维度查询库存信息
     * @date 2024/06/04
     */
    IPage<InventoryDTO.PagingViewDTO> pageByLocation(@Param("query") Page query, @Param("params") InventoryDTO.SearchParamDTO params, @Param("codeList") List<String> codeList);

    /**
     * 按仓库统计数量
     */
    Long countByWarehouse(@Param("params") InventoryDTO.SearchParamDTO params);

    /**
     * 按库区统计数量
     */
    Long countByArea(@Param("params") InventoryDTO.SearchParamDTO params);

    /**
     * 按仓位统计数量
     */
    Long countByLocation(@Param("params") InventoryDTO.SearchParamDTO params);

    /**
     * 查询仓位下是否有库存
     * @return 库存数量
     */
    @Select("select sum(qty) from inventory where warehouse_id = #{warehouseId} and warehouse_location = #{warehouseLocation}")
    Integer getQtyByLocation(@Param("warehouseId") String warehouseId, @Param("warehouseLocation") String warehouseLocation);

    /**
     * 按仓库导出数据
     */
    List<InventoryDTO.PagingViewDTO> exportByWarehouse(@Param("params") InventoryDTO.SearchParamDTO searchParamDTO, @Param("lastId") String lastId);
    /**
     * 按仓库导出数据
     */
    Page<InventoryDTO.PagingViewDTO> exportByWarehouse(@Param("page") Page<InventoryDTO.PagingViewDTO> page, @Param("params") InventoryDTO.SearchParamDTO searchParamDTO, @Param("lastId") String lastId);

    /**
     * 按库区导出数据
     */
    List<InventoryDTO.PagingViewDTO> exportByArea(@Param("params") InventoryDTO.SearchParamDTO searchParamDTO, @Param("warehouseAreaCodeList") List<String> warehouseAreaCodeList, @Param("lastId") String lastId);
    /**
     * 按库区导出数据
     */
    Page<InventoryDTO.PagingViewDTO> exportByArea(@Param("page") Page<InventoryDTO.PagingViewDTO> page, @Param("params") InventoryDTO.SearchParamDTO searchParamDTO, @Param("warehouseAreaCodeList") List<String> warehouseAreaCodeList, @Param("lastId") String lastId);

    /**
     * 按仓位导出数据
     */
    List<InventoryDTO.PagingViewDTO> exportByLocation(@Param("params") InventoryDTO.SearchParamDTO searchParamDTO, @Param("warehouseLocationCodeList") List<String> warehouseLocationCodeList, @Param("lastId") String lastId);

    /**
     * 按仓位导出数据
     */
    Page<InventoryDTO.PagingViewDTO> exportByLocation(@Param("page") Page<InventoryDTO.PagingViewDTO> page, @Param("params") InventoryDTO.SearchParamDTO searchParamDTO, @Param("warehouseLocationCodeList") List<String> warehouseLocationCodeList, @Param("lastId") String lastId);

    List<InventoryDTO.RealQtyDTO> getRealQty(@Param("skuIds") List<String> skuIds, @Param("warehouseIds") List<String> warehouseIds, @Param("inventoryStatusList") List<String> inventoryStatusList);
}
