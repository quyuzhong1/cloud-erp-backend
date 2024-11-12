package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.vo.WarehouseLocationExportVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 仓库仓位表 Mapper 接口
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@Mapper
public interface WarehouseLocationMapper extends BaseMapper<WarehouseLocationEntity> {

    IPage<WarehouseLocationDTO.LocationSelectDTO> paging(Page query, @Param("params") WarehouseLocationDTO.WarehouseLocationSearchParamDTO dto);

    List<WarehouseLocationEntity> list(@Param(value = "warehouseIds") List<String> warehouseIds);
    /**
     * @description: 根据仓库id和库位集合查询
     * @author Will
     * @date: 2023/8/2 17:52
     * @param listParam
     * @return List<WarehouseLocationEntity>
     */
    List<WarehouseLocationEntity> listByWarehouseIdAndCode(@Param("listParam") List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> listParam);

    /**
     * 根据仓库id和仓位编码获取
     * @param query
     * @param params
     * @return
     */
    Page<WarehouseLocationDTO.PagingViewDTO> pagingByParams(Page<WarehouseLocationDTO.PagingViewDTO> query,@Param("params") WarehouseLocationDTO.PagingParamDTO params);
    /**
     * @description: 分页远程查询下拉
     * @author Will
     * @date: 2024/5/16 15:20
     * @param query
     * @param params
     * @return IPage<LocationListDTO>
     */
    IPage<WarehouseLocationDTO.LocationListDTO> pagingSelect(Page query,@Param("params") WarehouseLocationDTO.SelectDTO params);

    IPage<WarehouseLocationDTO.ViewDto> pagingByArgs(@Param("query") Page<WarehouseLocationDTO.ViewDto> query, @Param("params") WarehouseLocationDTO.SearchParamDTO params);

    IPage<WarehouseAreaDTO.PagingView> areaPaging(@Param("page") Page<WarehouseAreaDTO.PagingView> page,@Param("params") WarehouseAreaDTO.PagingParam params);

    List<WarehouseLocationExportVo> listByIds(@Param("ids") List<String> ids);

    List<WarehouseLocationExportVo> selectAllByParam(@Param("dto") WarehouseLocationDTO.exportParamDto dto);

    List<WarehouseLocationDTO.ViewDto> listAreaByWarehouseId(@Param("warehouseId") String warehouseId);

    List<WarehouseLocationExportVo> listAllByParam(@Param("dto") WarehouseLocationDTO.exportParamDto dto);
    Page<WarehouseLocationExportVo> listAllByParam(@Param("page") Page<WarehouseLocationExportVo> page, @Param("dto") WarehouseLocationDTO.exportParamDto dto);

    IPage<WarehouseLocationDTO.LocationListDTO> pagingSelectBySku(Page query, WarehouseLocationDTO.SelectDTO params);

    @Update("update warehouse_location set status = #{status} where type = 'location' and warehouse_id = #{warehouseId} and code = #{warehouseLocation}")
    Integer updateLocationStatus(String warehouseId, String warehouseLocation, String status);

    /**
     * 根据仓位状态统计仓位数量
     * @param statusCode 仓位状态编码
     * @return 统计数量
     * @date: 2024-06-13
     * @author: tanmujin
     */
    Integer countByStatus(@Param("statusCode") String statusCode);

    List<WarehouseLocationEntity> listLocation(@Param("warehouseId") String warehouseId, @Param("warehouseArea") String warehouseArea);

    WarehouseLocationEntity findWarehouseArea(@Param("warehouseId") String warehouseId, @Param("warehouseLocation") String warehouseLocation);

    List<WarehouseLocationDTO.MappingDTO> listArea2LocationMapping(@Param("warehouseId") String warehouseId);

    /**
     * 根据SKU找出非拣货区的所有仓位，按库存数量降序排序
     * @param warehouseId
     * @param skuNo
     * @date: 2024-11-05
     * @author: jack
     */
    WarehouseLocationDTO.WareInventoryQtyDTO getOneWareInventoryQty(@Param("warehouseId") String warehouseId,@Param("skuNo") String skuNo);
}
