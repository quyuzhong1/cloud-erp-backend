package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
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
    IPage<SoB2cDTO.ListDTO> paging(Page query, @Param("params") SoB2cDTO.PagingParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO, @Param("isOutStock") Boolean isOutStock);

    /**
     * 状态数量
     *
     * @param params
     * @return
     */
    Integer listCount(@Param("params") SoB2cDTO.PagingParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);

    /**
     * @param query
     * @param params
     * @return IPage<MergeListDTO>
     * @description: 合并分页查询
     * @author Will
     * @date: 2023/8/22 16:10
     */
    IPage<SoB2cDTO.MergeListDTO> mergePaging(Page query, @Param("params") SoB2cDTO.MergePagingParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);

    /**
     * @param params
     * @return IPage<MergeListDTO>
     * @description: 合并分页查询数量
     * @author Will
     * @date: 2023/8/22 16:10
     */
    List<Integer> mergePagingCount(@Param("params") SoB2cDTO.MergePagingParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);

    /**
     * @param mergeParamDTO
     * @return List<MergeMainDTO>
     * @description: 合并数据查询
     * @author Will
     * @date: 2023/8/22 18:36
     */
    List<SoB2cDTO.MergeMainDTO> listMerge(@Param("params") SoB2cDTO.MergeParamDTO mergeParamDTO, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);

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
    Page<ReportDTO.ProductSalesPagingViewDTO> listProductSalesExport(@Param("page")Page<ReportDTO.ProductSalesPagingViewDTO> page, @Param("params") ReportDTO.ProductSalesPagingParamDTO params, @Param("skuIdList") List<String> skuIdList);

    /**
     * 获取标记发货的信息
     *
     * @param soB2cId
     * @return
     */
    SoB2cDTO.SignShipOrderDTO getSignShipParam(@Param("id") String soB2cId);

    /**
     * 获取客户信息
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-01 9:49
     */
    List<SoB2cDTO.CustomerDTO> listCustomer(@Param("idList") List<String> soIdList);

    List<SoB2cEntity> listWarehouseIsEmpty(@Param("idList") List<String> soIdList);

    /**
     * 根据渠道id查询需要生成中转报关单的数据
     *
     * @param channelIds
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDetailDTO.AddDTO>
     * @Author Luo_WG
     * @Date 2024/1/25 19:11
     **/
    List<TransferDeclareDetailDTO.AddDTO> listByLogisticsSupplier(@Param("channelIds") List<String> channelIds);


    /**
     * @param code
     * @return
     * @description
     * @date 2024-01-26 15:51
     * @author Lambda
     */
    PackageDTO.ScanResultDTO packageScanByCode(@Param("code") String code);

    /**
     * 分拨组包 分页
     *
     * @param query
     * @param params
     * @return
     */
    IPage<PackageDTO.PagingViewDTO> packagePing(Page query, @Param("params") PackageDTO.PagingParamDTO params);

    /**
     * 根据销售订单ids 获取到合并的数据
     *
     * @param ids
     * @return
     */
    List<PackageDTO.ScanResultDTO> listMergePackageBySoIds(@Param("ids") List<String> ids);

    /**
     * 根据条件获取数据对比系统数据
     *
     * @param params
     * @return
     */
    List<WmsDataCompareTaskDTO.SoB2cDTO> getDataCompareByCondition(@Param("params") WmsDataCompareTaskDTO.SoOutstockDTO params);

    /**
     * @param params
     * @return List<ExcelExportDTO>
     * @description: 导出excel
     * @author Will
     * @date: 2024/4/16 15:10
     */
    List<SoB2cDTO.ExcelExportDTO> exportExcel(@Param("params") SoB2cDTO.ExportParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO, @Param("isOutStock") Boolean isOutStock);
    Page<SoB2cDTO.ExcelExportDTO> exportExcel(@Param("page") Page<SoB2cDTO.ExcelExportDTO> page, @Param("params") SoB2cDTO.ExportParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO, @Param("isOutStock") Boolean isOutStock);

    /**
     * @param params
     * @param shopAuthResultDTO
     * @param isOutStock
     * @return Integer
     * @description: 导出excel数量查询
     * @author Will
     * @date: 2024/4/29 17:55
     */
    Integer countExportExcel(@Param("params") SoB2cDTO.ExportParamDTO params, @Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO, @Param("isOutStock") Boolean isOutStock);
    List<SoB2cDTO.ExcelExportDTO> exportExcel(@Param("params") SoB2cDTO.PagingParamDTO params,@Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);
    Page<SoB2cDTO.ExcelExportDTO> exportExcel(@Param("page") Page<SoB2cDTO.ExcelExportDTO> page, @Param("params") SoB2cDTO.PagingParamDTO params,@Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);
    /**
     * @description: 异常订单分页查询
     * @author Will
     * @date: 2024/4/22 17:55
     * @param params
     * @param shopAuthResultDTO
     * @return PagingVO<ListDTO>
     */
    IPage<SoB2cAbnormalDTO.ListDTO> abnormalPaging(Page query, @Param("params") SoB2cAbnormalDTO.PagingParamDTO params,@Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);
    /**
     * @description: 异常订单导出
     * @author Will
     * @date: 2024/4/22 19:57
     * @param params
     * @param shopAuthResultDTO
     * @return List<ListDTO>
     */
    List<SoB2cAbnormalDTO.ListDTO> abnormalExportExcel( @Param("params") SoB2cAbnormalDTO.PagingParamDTO params,@Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);
    Page<SoB2cAbnormalDTO.ListDTO> abnormalExportExcel( @Param("page") Page<SoB2cAbnormalDTO.ListDTO> page,@Param("params") SoB2cAbnormalDTO.PagingParamDTO params,@Param("shopAuthResultDTO") SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO);

    /**
     * 批量更新审核信息
     * @param updateList
     */
    void updateBatchApproveById(@Param("updateList") List<SoB2cEntity> updateList);

    IPage<SoB2cForeignDTO.OrderDeliveryResp> getForeignOrderDeliveryInfo(Page query, @Param("params") SoB2cForeignDTO.OrderDeliveryReq orderDeliveryReq);
}
