package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 采购订单表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrderEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/27 12:33
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<PurchaseOrderDTO.ListDTO> paging(Page query,@Param("params") PurchaseOrderDTO.SearchParamDTO params);

    /**
     * srm 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<PurchaseOrderDTO.ListDTO> srmPaging(Page query,@Param("params") PurchaseOrderDTO.SrmSearchParamDTO params);
    /**
     * srm 待发货分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(Page query,@Param("params") PurchaseOrderDTO.SrmSearchParamDTO params);

    /**
     * @description: 查询总数量、金额
     * @author Will
     * @date: 2023/7/17 15:10
     * @param dto
     * @return PagingTotalDTO
     */
    PurchaseOrderDTO.PagingTotalDTO pagingTotal(@Param("params") PurchaseOrderDTO.SearchParamDTO dto);

    /**
     * @description: 导出查询数据
     * @author Will
     * @date: 2023/3/27 16:01
     * @param params
     * @return List<PurchaseOrderDTO.ListDTO>
     */
    List<PurchaseOrderDTO.ListDTO> listExportExcel(@Param("params") PurchaseOrderDTO.SearchParamDTO params);
    Page<PurchaseOrderDTO.ListDTO> listExportExcel(@Param("page") Page<PurchaseOrderDTO.ListDTO> page, @Param("params") PurchaseOrderDTO.SearchParamDTO params);
    /**
     * @description: 查询列表数量
     * @author Will
     * @date: 2023/3/29 10:11
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") PurchaseOrderDTO.SearchParamDTO params);

    /**
     * 下推收货单列表
     * @Author Luo_WG
     * @Date 2023/4/18 18:06
     * @param purchaseDetailIdList
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.ViewGenerateReceiveDTO>
     **/
    List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(@Param("purchaseDetailIdList") List<String> purchaseDetailIdList);

    /**
     * 获取订单信息
     * @author yl
     * @date 2023-04-23 14:10
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getPurchaseOrderByOrderIds(@Param("purchaseOrderIds") List<String> purchaseOrderIds);

    /**
     * 根据采购订单id 获取下推数据显示
     * @author yl
     * @date 2023-04-25 9:43
     * @param purchaseDetailIdList
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(@Param("purchaseDetailIdList") List<String> purchaseDetailIdList);
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/6/13 15:38
     * @param sourceDetailIds
     * @return List<ListDTO>
     */
    List<PurchaseOrderDTO.ListDTO> listBySourceDetailIds(@Param("sourceDetailIds") List<String> sourceDetailIds);
    /**
     * @description: 查询委外采购订单
     * @author Will
     * @date: 2023/6/15 15:16
     * @param sourceId
     * @return List<ViewSubcontractPoDTO>
     */
    List<PurchaseOrderDTO.ViewSubcontractPoDTO> viewSubcontractPo(@Param("sourceId") String sourceId);
    /**
     * @description: 查询委外订单所有子级SKU生成的采购订单信息
     * @author Will
     * @date: 2023/6/15 17:49
     * @param parentPodIds
     * @return List<SubcontractOrderChildDTO>
     */
    List<PurchaseOrderDTO.SubcontractOrderChildDTO> listPoRefSubChildByParentPodIds(@Param("parentPodIds") List<String> parentPodIds);

    /**
     * PDA:根据查询条件获取采购单
     * @Author Luo_WG
     * @Date 2023/8/18 10:17
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.PdaPurchaseOrder>
     **/
    List<PurchaseOrderDTO.PdaPurchaseOrder> pdaList(PurchaseOrderDTO.PdaPurchaseOrderParam dto);

    /**
     * 根据采购日期查询采购采购单
     * @Author Luo_WG
     * @Date 2023/9/13 18:46
     * @param purchaseDateList
     * @return java.util.List<com.erp.model.scm.dto.SkuCostDTO>
     **/
    List<SkuCostDTO> listPurchaseOrderByPurchaseDate(@Param("purchaseDateList") List<LocalDate> purchaseDateList);

    /**
     * 获取首批采购日期
     * @param ids
     * @return
     */
    List<FirstPlaceOrderDTO> listFirstPlaceOrderDate(@Param("ids") List<String> ids);
    /**
     * @description: 根据skuId集合查询采购成本数据
     * @author Will
     * @date: 2023/11/23 16:26
     * @param paramDTO
     * @return List<SkuCostDTO>
     */
    List<SkuCostDTO> listPurchaseOrderCost(@Param("params") SkuCostDTO.ParamDTO paramDTO);

    List<PurchaseStatisticsDTO.StatisticsMonthDTO> statisticsBySupplier(@Param("params") PurchaseStatisticsDTO.RequestDTO requestDTO);

    Integer srmPurchaseOrderCount(@Param("params") PurchaseOrderSrmDTO.SearchParamDTO params);
    /**
     * @description: 查询需要自动确认的采购订单
     * @author Will
     * @date: 2024/1/17 10:34
     * @return List<PurchaseOrderConfirmDTO>
     */
    List<PurchaseOrderDetailDTO.PurchaseOrderConfirmDTO> listPurchaseOrderAutoConfirm(@Param("list") List<CfgSettingDTO.ViewDTO> list);

    /**
     * 查询确认订单列表
     * @param params
     * @return
     */
    List<PurchaseOrderDTO.ListDTO> srmPurchaseOrderList(@Param("params") PurchaseOrderDTO.SrmSearchParamDTO params);

    /**
     * srm 待发货订单列表
     * @param params
     * @return
     */
    List<PurchaseOrderDTO.ListDTO> srmWaitDeliveryList(@Param("params") PurchaseOrderDTO.SrmSearchParamDTO params);
    List<PurchaseOrderDTO.ListDTO> listByDetailIds(@Param("detailIds") List<String> purchaseDetailIds);

    /**
     * 根据状态查询列表数据 获取近一天内的变更数据
     * @return
     */
    List<PurchaseOrderDetailDTO.PurchaseOrderConfirmDTO> getSrmPurchaseOrder();
    /**
     * 汇总待发货订单明细数量
     * @param supplierId
     * @return
     */
    Integer srmWaitDeliveryCount(@Param("supplierId")String supplierId,@Param("code") String code);
    /**
     * 采购单号分页查询
     * @author will
     * @date 2024/11/11 11:41
     * @param query
     * @param params
     * @return IPage<SourceCodeDTO>
     */
    IPage<PurchaseOrderDTO.SourceCodeDTO> purchaseCodePaging(Page query,@Param("params") PurchaseOrderDTO.SourceCodeParamDTO params);
}
