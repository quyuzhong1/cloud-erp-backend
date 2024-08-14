package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.dto.StocktakingProfitLossDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 盘盈盘亏单 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Mapper
public interface StocktakingProfitLossMapper extends BaseMapper<StocktakingProfitLossEntity> {
     /**
      * 以单据类型分组
      * @author yl
      * @date 2023-08-10 17:58
      * @param permissionSql
      * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDTO.TabDTO>
      */
    List<StocktakingProfitLossDTO.TabDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<StocktakingProfitLossDTO.PagingViewDTO> paging(Page query, @Param("params")StocktakingProfitLossDTO.PagingParamDTO params);

    /**
     * 获取到导出数据
     * @author yl
     * @date 2023-08-11 12:16
     * @param params
     * @return java.util.List<com.erp.model.wms.dto.StocktakingProfitLossDTO.PagingViewDTO>
     */
    List<StocktakingProfitLossDTO.ExportViewDTO> listExport(@Param("params") StocktakingProfitLossDTO.ExportDTO params);
    Page<StocktakingProfitLossDTO.ExportViewDTO> listExport(@Param("page") Page<StocktakingProfitLossDTO.ExportViewDTO> page, @Param("params") StocktakingProfitLossDTO.ExportDTO params);
    /**
     * 获取到扣减库存所需的参数
     * @author yl
     * @date 2023-08-15 16:46
     * @param idList
     * @return java.util.List<com.erp.model.wms.dto.inventory.InOutStockDTO>
     */
    List<InOutStockDTO> listInventoryInOut(@Param("idList") List<String> idList);

    /**
     * 库存IDS
     * 库存组织IDS
     * SKUIdS
     * @author Jim
     * @date 2023-03-05
     */
    List<StocktakingProfitLossDetailDTO.LastDTO> maxDateByParams(@Param("warehouseIdList")List<String> warehouseIdList,
                                                                 @Param("orgIdList") List<String> orgIdList,
                                                                 @Param("skuIdList") List<String> skuIdList
    );


    /**
     * 仓库ID和SkuId,单据时间查询最新的单号
     *
     * @author Jim
     * @date 2023-03-05
     */
    List<String> findLastOneCode(@Param("warehouseId")String warehouseId, @Param("skuId")String skuId, @Param("billDate")LocalDate billDate);
}
