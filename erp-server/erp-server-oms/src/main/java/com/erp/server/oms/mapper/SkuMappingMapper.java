package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * sku 对照表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Mapper
public interface SkuMappingMapper extends BaseMapper<SkuMappingEntity> {

    IPage<SkuMappingDTO.PagingViewDTO> paging(Page query, @Param("params") SkuMappingDTO.PagingParamDTO params);

     /**
      * 库存SKU 分页
      * @author yl
      * @date 2023-08-22 12:25
      * @param query
      * @param params
      * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.SkuMappingDTO.WarehousePagingViewDTO>
      */
    IPage<SkuMappingDTO.WarehousePagingViewDTO> warehousePaging(Page query, @Param("params") SkuMappingDTO.WarehousePagingParamDTO params);

    List<SkuMappingDTO.PagingViewDTO> listExport(@Param("params") SkuMappingDTO.ExportDTO params);
    Page<SkuMappingDTO.PagingViewDTO> listExport(@Param("page")Page<SkuMappingDTO.PagingViewDTO> page, @Param("params") SkuMappingDTO.ExportDTO params);
    /**
     * 获取到tab 统计数据
     * @author yl
     * @date 2023-08-18 11:17
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.MatchCountDTO>
     */
    List<SkuMappingDTO.MatchCountDTO> listMatchCount(@Param("params") SkuMappingDTO.FindTabDTO dto);

    IPage<SkuMappingDTO.ProductSkuInfoDTO> listPaging(Page query, @Param("params") SkuMappingDTO.ListParamDTO params);

    /**
     * 库存 SKU 导出
     * @author yl
     * @date 2023-08-21 9:48
     * @param dto
     * @param matchResult
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.PagingViewDTO>
     */
    List<SkuMappingDTO.WarehousePagingViewDTO> listWarehouseExport(@Param("params")SkuMappingDTO.ExportWarehouseSkuDTO dto);
    Page<SkuMappingDTO.WarehousePagingViewDTO> listWarehouseExport(@Param("page") Page<SkuMappingDTO.WarehousePagingViewDTO> page, @Param("params")SkuMappingDTO.ExportWarehouseSkuDTO dto);

    /**
     * 跟哭库存sku no list获取
     * @author yl
     * @date 2023-09-04 17:15
     * @param platformSkuNoList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.ListSkuDTO>
     */
    List<SkuMappingDTO.SkuDTO> listByPlatformSkuNoList(@Param("platformSkuNoList") List<String> platformSkuNoList);

    /**
     * 根据平台sku查询sku映射信息
     * @Author Luo_WG
     * @Date 2023/11/15 15:28
     * @param listingInfoParamDTO
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.SkuDTO>
     **/
    List<SkuMappingDTO.MappingSkuViewDTO> listByPlatformSkuNoAndPlatform(@Param("params") ListingInfoParamDTO listingInfoParamDTO);

    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:20
     * @param productSkuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(@Param("productSkuIdList") List<String> productSkuIdList);

    /**
     * 根据条件查询映射
     * @param params
     * @return
     */
    List<ListingInfoWithSkuMappingDTO> listByParams(@Param("params") ListingInfoParamDTO params);

    List<ListingInfoWithSkuMappingDTO> listByErpSkuIdAndType(@Param("erpSkuIdList") List<String> erpSkuIdList,@Param("provideCode") String provideCode,@Param("warehouseId") String warehouseId,@Param("shopId") String shopId);

    List<ListingAdvanceQueryDTO> advanceQuerySku(@Param("params") AdvanceQueryContainer advanceQueryContainer);

    List<SkuMappingEntity> findHistory(@Param("id")String id, @Param("listingId")String listingId, @Param("shopId")String shopId, @Param("productSkuId")String productSkuId);

    /**
     * 根据listingId获取修改记录
     * @param listingId
     * @return
     */
    List<SkuMappingEntity> listHistoryByListingId(@Param("listingId") String listingId);

    List<SkuMappingDTO.WarehouseSkuDTO> listByWarehouseAndPlatformSku(@Param("warehouseId") String warehouseId,@Param("platformSkuNoList") List<String> platformSkuNoList);

    List<SkuMappingDTO.ProductSkuInfoDTO> listSkuBySkuNos(@Param("params")SkuMappingDTO.SkuParamDTO params);
}
