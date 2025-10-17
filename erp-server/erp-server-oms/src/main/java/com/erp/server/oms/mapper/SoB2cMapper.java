package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    IPage<SoB2cDTO.ListDTO> paging(Page query, @Param("params") SoB2cDTO.PagingParamDTO params,  @Param("isOutStock") Boolean isOutStock);

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
    Page<SoB2cDTO.ExcelExportDTO> exportExcel(@Param("page") Page<SoB2cDTO.ExcelExportDTO> page, @Param("params") SoB2cDTO.ExportParamDTO params, @Param("isOutStock") Boolean isOutStock);
    Page<SoB2cDTO.ExcelExportDTO> exportFullyManagedExcel(@Param("page") Page<SoB2cDTO.ExcelExportDTO> page, @Param("params") SoB2cDTO.ExportParamDTO params,  @Param("isOutStock") Boolean isOutStock);

    /**
     * @description: 异常订单分页查询
     * @author Will
     * @date: 2024/4/22 17:55
     * @param params
     * @return PagingVO<ListDTO>
     */
    IPage<SoB2cAbnormalDTO.ListDTO> abnormalPaging(Page query, @Param("params") SoB2cAbnormalDTO.PagingParamDTO params);
    /**
     * @description: 异常订单导出
     * @author Will
     * @date: 2024/4/22 19:57
     * @param params
     * @return List<ListDTO>
     */
    Page<SoB2cAbnormalDTO.ListDTO> abnormalExportExcel(@Param("page") Page<SoB2cAbnormalDTO.ListDTO> page, @Param("params") SoB2cAbnormalDTO.PagingParamDTO params);
    /**
     * 批量更新审核信息
     * @param updateList
     */
    void updateBatchApproveById(@Param("updateList") List<SoB2cEntity> updateList);

    IPage<SoB2cForeignDTO.OrderDeliveryResp> getForeignOrderDeliveryInfo(Page query, @Param("params") SoB2cForeignDTO.OrderDeliveryReq orderDeliveryReq);

    List<SoB2cDTO.GenerateSoB2cReturnViewDTO> generateSoB2cReturnView(@Param("ids")List<String> ids);


    List<SoB2cDetailEntity> listTikTokOrder();
    List<SoB2cDetailEntity> listTikTokOrderAll(@Param("ids") List<String> mainIds);
    void tikTokOrderUpdate(@Param("id") String id);
    void tikTokOrderUpdateDetail(@Param("id") String id
            ,@Param("sourceDetailId") String sourceDetailId
            ,@Param("platformLineNumber") String platformLineNumber
            ,@Param("platformPackageId") String platformPackageId);

    /**
     * 更换发货sku预览
     * @param ids
     * @return
     */
    List<SoB2cDTO.ChangeDeliverySkuViewDTO> listChangeDeliverySkuView(@Param("ids") List<String> ids);

    List<SoB2cEntity> queryToSdy(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset, @Param("platformList") List<String> platformList);

    /**
     * 根据销售订单id获取订单 渠道+仓库+重量 基础信息
     * @param ids
     * @return
     */
    List<SoB2cDTO.LogisticsDTO> getB2cLogisticsByIds(@Param("ids") List<String> ids);

    /**
     * 根据店铺和启用时间更新订单vat发票状态
     * @param shopId
     * @param enableTime
     * @param vatInvoiceStatus
     */
    void updateFbaNotVatInvoice(@Param("shopId") String shopId, @Param("enableTime") LocalDateTime enableTime, @Param("vatInvoiceStatus") String vatInvoiceStatus);
    /**
     * 全托管分页查询
     *
     * @param query
     * @param params
     * @return
     */
    IPage<SoB2cDTO.ListDTO> fullyManagedPaging(@Param("query") Page query, @Param("params") SoB2cDTO.PagingParamDTO params, @Param("isOutStock") Boolean isOutStock);

    /**
     * 全托管数量查询
     * @param params
     * @return
     */
    Integer listFullManagedCount(@Param("params") SoB2cDTO.PagingParamDTO params);

    /**
     * 更新超时预警时间
     * @param platformList
     * @param offsetMinutes
     */
    void updateTimeOutConfig(@Param("platformList") List<String> platformList, @Param("offsetMinutes") Integer offsetMinutes);

    /**
     * 根据参数据查询需要发货的订单
     *
     * @param billStatusList
     * @param platformStatusList
     * @param platformList
     * @param codeList
     * @return
     */
    List<SoB2cDTO.DeliveryDTO> listDeliveryOrderByParam(@Param("billStatusList") List<String> billStatusList, @Param("platformStatusList") List<String> platformStatusList, @Param("platformList") List<String> platformList, @Param("codeList")List<String> codeList);

    /**
     * 更新
     * @param id
     * @param extendData
     */
    void updateExtendData(@Param("id") String id, @Param("extendData") String extendData);

    List<SoB2cEntity> listWaitShipByWarehouseIds(@Param("warehouseIds") List<String> warehouseIds);
    /**
     * 生成组包计划预览
     * @param soIds
     * @return
     */
    List<PackagePlanDTO.SoB2cDTO> packagePlanPreview(@Param("soIds") List<String> soIds);
}
