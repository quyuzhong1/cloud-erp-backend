package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * B2C销售订单表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Mapper
public interface SoB2cMapper extends BaseMapper<SoB2cEntity> {

    /**
     * 分页查询
     *
     * @param query
     * @param params
     * @return
     */
    IPage<SoB2cDTO.ListDTO> paging(Page query, @Param("params") SoB2cDTO.PagingParamDTO params);

    /**
     * 状态数量
     *
     * @param params
     * @return
     */
    Integer listCount(@Param("params") SoB2cDTO.PagingParamDTO params);

    /**
     * @param query
     * @param params
     * @return IPage<MergeListDTO>
     * @description: 合并分页查询
     * @author Will
     * @date: 2023/8/22 16:10
     */
    IPage<SoB2cDTO.MergeListDTO> mergePaging(Page query, @Param("params") SoB2cDTO.MergePagingParamDTO params);

    /**
     * @param params
     * @return IPage<MergeListDTO>
     * @description: 合并分页查询数量
     * @author Will
     * @date: 2023/8/22 16:10
     */
    List<Integer> mergePagingCount(@Param("params") SoB2cDTO.MergePagingParamDTO params);

    /**
     * @param mergeParamDTO
     * @return List<MergeMainDTO>
     * @description: 合并数据查询
     * @author Will
     * @date: 2023/8/22 18:36
     */
    List<SoB2cDTO.MergeMainDTO> listMerge(@Param("params") SoB2cDTO.MergeParamDTO mergeParamDTO);

    /**
     * 销售订单统计
     *
     * @param query
     * @param params
     * @return
     */
    IPage<ReportDTO.ProductSalesPagingViewDTO> productSalesPaging(Page query, @Param("params") ReportDTO.ProductSalesPagingParamDTO params, @Param("skuIdList") List<String> skuIdList);


    /**
     * 报表管理 导出销售订单统计数据
     *
     * @param params
     * @param skuIdList
     * @return
     */
    List<ReportDTO.ProductSalesPagingViewDTO> listProductSalesExport(@Param("params") ReportDTO.ProductSalesPagingParamDTO params, @Param("skuIdList") List<String> skuIdList);
}
