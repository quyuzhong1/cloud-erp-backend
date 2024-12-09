package com.erp.rpc.oms.feign;

import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soB2c")
public interface SoB2cFeign {

    /**
     * 根据b2c订单id获取物流信息
     */
    @PostMapping("/feign/soB2c/listSoB2cLogisticsByMainIdList")
    List<SoB2cLogisticsEntity> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList);

    /**
     * 根据跟踪单号查询订单物流信息
     * @param trackNo
     * @return
     */
    @PostMapping("/feign/soB2c/getSoB2cLogisticsByTrackNo")
    SoB2cLogisticsEntity getSoB2cLogisticsByTrackNo(@RequestBody String trackNo);

    /**
     * 根据跟踪单号查询订单物流信息
     * @author will
     * @date 2024/7/5 10:26
     * @param logisticsCode
     * @return SoB2cLogisticsEntity
     */
    @PostMapping("/feign/soB2c/getByTrackNoOrTransportNo")
    SoB2cLogisticsEntity getByTrackNoOrTransportNo(@RequestBody String logisticsCode);

    /**
     * 根据订单id 获取到运费估算的参数值
     *
     * @param orderId
     * @return
     */
    @PostMapping("/feign/soB2c/getShippingCalculationByOrderId")
    SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(@RequestBody String orderId);

    /**
     * 获取明细信息
     *
     * @param soDetailIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listDetailByIds")
    List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> soDetailIdList);


    /**
     * 获取明细信息
     *
     * @param soDetailIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listDetailContainDeleted")
    List<SoB2cDetailEntity> listDetailContainDeleted(@RequestBody List<String> soDetailIdList);


    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soIds
     * @return
     */
    @PostMapping("/feign/soB2c/listByIds")
    List<SoB2cEntity> listByIds(@RequestBody List<String> soIds);

    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @return
     */
    @PostMapping("/feign/soB2c/listWithIsIntercept")
    List<SoB2cEntity> listWithIsIntercept();

    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soId
     * @return
     */
    @GetMapping("/feign/soB2c/getById")
    SoB2cEntity getById(@RequestParam("soId") String soId);

    /**
     * 获取销售出库的需要的参数 根据销售code
     */
    @PostMapping("/feign/soB2c/getSoOutstockInfoByCode")
    SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(@RequestBody String code);

    /**
     * 取销售出库的需要的参数 根据销售单id
     */
    @PostMapping("/feign/soB2c/getSoOutstockInfoById")
    SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(@RequestBody String soId);

    /**
     * 获取销售出库的需要的参数
     */
    @PostMapping("/feign/soB2c/orderShipped")
    Boolean orderShipped(@RequestBody String soId);

    /**
     * 根据b2c订单id查询详情信息
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     **/
    @PostMapping("/feign/soB2c/listDetailByMainIds")
    List<SoB2cDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds);

    /**
     * @param dto
     * @return
     * @description 添加异常订单信息
     * @author Lambda
     * @create 2023-12-20 11:06
     */
    @PostMapping("/feign/soB2cError/add")
    void addSoB2cError(@RequestBody SoB2cErrorDTO.AddDTO dto);

    /**
     * @param batchAdd
     * @return
     * @description 批量添加异常订单信息  一个请求中包含多个订单
     * @author Lambda
     * @create 2023-12-20 11:06
     */
    @PostMapping("/feign/soB2cError/batchAdd")
    void batchAddSoB2cError(@RequestBody SoB2cErrorDTO.BatchAdd batchAdd);
    /**
     * 删除异常信息
     *
     * @param deleteDTO
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-20 11:20
     */
    @PostMapping("/feign/soB2cError/delete")
    void deleteError(@RequestBody SoB2cErrorDTO.DeleteDTO deleteDTO);


    /**
     * 根据订单ID和明细ID删除
     *
     * @date 2024-03-07
     * @author Jim
     */
    @PostMapping("/feign/soB2cError/deleteDetail")
    void deleteDetailError(@RequestBody SoB2cErrorDTO.DeleteDetailDTO deleteDTO);

    /**
     * 批量删除异常信息
     *
     * @param batchDeleteDTO
     * @return
     * @description
     * @author zdy
     * @create 2023-12-20 11:20
     */
    @PostMapping("/feign/soB2cError/deleteErrorByMainIds")
    void deleteErrorByMainIds(@RequestBody SoB2cErrorDTO.BatchDeleteDTO batchDeleteDTO);
    /**
     * 根据b2c订单id获取买家信息
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
     * @Author Luo_WG
     * @Date 2023/12/22 9:20
     **/
    @PostMapping("/feign/soB2c/listSoB2cReceiverByMainIdList")
    List<SoB2cReceiverEntity> listSoB2cReceiverByMainIdList(@RequestBody List<String> mainIdList);

    /**
     * 获取标记发货 需要的参数
     *
     * @param soB2cId 销售订单id
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-22 15:33
     */
    @PostMapping("/feign/soB2c/getSignShipParam")
    SoB2cDTO.SignShipOrderDTO getSignShipParam(@RequestBody String soB2cId);

    /**
     * 校验是否需要调用第三方标记发货
     * @Author Luo_WG
     * @Date 2023/12/27 11:29
     * @param soB2cId
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/checkPlatformShipOrder")
    Boolean checkPlatformShipOrder(@RequestBody String soB2cId);

    /**
     * 修改b2c销售单状态
     *
     * @param soB2cIds
     * @param status
     * @param isManualDelivery
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     **/
    @PostMapping("/feign/soB2c/updateSoB2cStatus")
    Boolean updateSoB2cStatus(@RequestParam("soB2cIds") List<String> soB2cIds, @RequestParam("status") String status, @RequestParam("isManualDelivery")Boolean isManualDelivery);

    /**
     * 修改b2c销售单状态发货时间
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     * @param deliveryTimeDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateSoB2cStatusAndDeliveryTime")
    Boolean updateSoB2cStatusAndDeliveryTime(@RequestBody SoB2cDTO.UpdateDeliveryTimeDTO deliveryTimeDTO);

    /**
     * 获取销售订单物流渠道
     * @Author yl
     * @Date 2023/12/28 11:29
     * @param channelId
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/listSoB2cLogisticsByChannelId")
    List<SoB2cLogisticsEntity> listSoB2cLogisticsByChannelId(@RequestBody String channelId);

    /**
     * 设置打印面单需要的字段
     * @Author Luo_WG
     * @Date 2023/12/28 15:37
     * @param soIds
     * @return java.util.List<com.common.business.dto.PrintWayBillPdfDTO>
     **/
    @PostMapping("/feign/soB2c/printWayBillPdf")
    List<PrintWayBillPdfDTO> printWayBillPdf(@RequestBody List<String> soIds);


    /**
     * 修改b2c销售单状态
     * @Author yl
     * @Date 2023/12/27 20:14
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateSoB2cStatusByParams")
    Boolean updateSoB2cStatusByParams(@RequestBody SoB2cDTO.UpdateStatusDTO dto);


    /**
     * 获取销售订单客户信息
     * @param soId
     * @return
     */
    @PostMapping("/feign/soB2c/getB2cCustomerById")
    SoB2cDTO.CustomerDTO getB2cCustomerById(@RequestBody String soId);

    /**
     * 根据销售订单idlist 获取到客户信息
     * @param soIdList
     * @return
     */
    @PostMapping("/feign/soB2c/listCustomer")
    List<SoB2cDTO.CustomerDTO> listCustomer(@RequestBody List<String> soIdList);

    /**
     * 获取异常信息
     * @description
     * @param mainId
     * @param errorType
     * @author Lambda
     * @return
     * @create 2024-01-01 12:27
     */
    @PostMapping("/feign/soB2cError/getB2cError")
    SoB2cErrorEntity getB2cError(@RequestParam("mainId")String mainId, @RequestParam("errorType") String errorType);

    /**
     * 获取沃尔玛发货参数
     * @Author Luo_WG
     * @Date 2024/1/3 17:23
     * @param soId
     * @return com.common.business.dto.WalmartShipDTO
     **/
    @PostMapping("/feign/soB2c/getWalmartShipOrderParam")
    List<WalmartShipDTO> getWalmartShipOrderParam(@RequestBody String soId);

    /**
     * 查询仓库为空的销售订单
     * @description
     * @param ids
     * @author Lambda
     * @return
     * @create 2024-01-03 17:12
     */
    @PostMapping("/feign/soB2c/listWarehouseIsEmpty")
    List<SoB2cEntity> listWarehouseIsEmpty(@RequestBody List<String> ids);

    @PostMapping("/feign/soB2c/updateWarehouseByShopId")
    Boolean updateWarehouseByShopId(@RequestParam("id")String id,@RequestParam("shopId") String shopId);

    @GetMapping("/feign/soB2c/findMergeByTargetId")
    List<SoB2cRefEntity> findMergeByTargetId(@RequestParam("targetId")String targetId);

    /**
     * 获取跟踪单号为空的
     * @description
     * @author Lambda
     * @return
     * @create 2024-01-05 9:54
     */
    @GetMapping("/feign/soB2c/listTrackNoEmptyList")
    List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList();


    @GetMapping("/feign/soB2c/view")
    SoB2cDTO.ViewDTO view(@RequestParam("id") String id);

    /**
     * 拦截打标识，冻结订单
     * @Author Luo_WG
     * @Date 2024/1/17 18:54
     * @param interceptUpdateOrderDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateIntercept")
    Boolean updateIntercept(@RequestBody SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO);

    /**
     * 修改订单异常原因
     * @Author Luo_WG
     * @Date 2024/1/19 10:51
     * @param id
     * @param soB2cAbnormalType
     * @return java.lang.Boolean
     **/
    @GetMapping("/feign/soB2c/updateAbnormalType")
    Boolean updateAbnormalType(@RequestParam("id") String id, @RequestParam("soB2cAbnormalType") String soB2cAbnormalType);



    /**
     * 更改订单的组包状态
     * @return
     */
    @PostMapping("/feign/soB2c/updatePackageStatus")
    Boolean updatePackageStatus(@RequestBody UpdateStateDTO.UpdateByStrStatusDTO dto);

    /**
     * 更改订单的中转状态
     * @return
     */
    @PostMapping("/feign/soB2c/updateTransferStatus")
    Boolean updateTransferStatus(@RequestBody UpdateStateDTO.UpdateByStrStatusDTO dto);

    /**
     * 根据报关设置生成报关单信息
     * @Author Luo_WG
     * @Date 2024/1/25 17:29
     * @param viewDTOList
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.AddDTO>
     **/
    @PostMapping("/feign/soB2c/generateTransferDeclareView")
    List<TransferDeclareDTO.AddDTO> generateTransferDeclareView(@RequestBody @Validated List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList);

    /**
     * 更改订单的中转状态
     * @Author Luo_WG
     * @Date 2024/1/25 19:39
     * @param soIds 订单id
     * @param status 中转状态
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateTransferStatusBatch")
    Boolean updateTransferStatusBatch(@RequestParam("soIds") List<String> soIds, @RequestParam("status") String status);

    /**
     * 修改订单的第三方物流单号
     * @Author Luo_WG
     * @Date 2024/1/29 17:04
     * @param list
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateShippingOrderNo")
    Boolean updateShippingOrderNo(@RequestBody List<TransferDeclareDTO.ShippingOrderDTO> list);

    /**
     * 修改订单的第三方物流单号
     * @Author zdy
     * @Date 2024/1/29 17:04
     * @param shippingOrderDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/updateShippingOrderNoBySoId")
    Boolean updateShippingOrderNoBySoId(@RequestBody TransferDeclareDTO.ShippingOrderDTO shippingOrderDTO);
    /**
     * 根据销售订单拆分sku
     * 拆分逻辑为 物流产品 为拆分 sku为组合时进行拆分
     * @param soIds
     * @return
     */
    @PostMapping("/feign/soB2c/getTransferDeclareProductBySoIds")
    List<TransferDeclareProductDTO> getTransferDeclareProductBySoIds(@RequestBody List<String> soIds) ;

    /**
     * 修改速卖通订单仓库
     * @Author Luo_WG
     * @Date 2024/2/1 10:44
     * @param soId
     * @return void
     **/
    @PostMapping("/feign/soB2c/updateAliExpressOrderWarehouse")
    Boolean updateAliExpressOrderWarehouse(@RequestParam("soId") String soId, @RequestParam("shopId") String shopId);

    /**
     * 修改b2c 销售订单物流信息
     * @description
     * @param list
     * @return
     * @date 2024-02-26 16:41
     * @author Lambda
     */
    @PostMapping("/feign/soB2c/batchUpdateLogistics")
    Boolean batchUpdateLogistics(@RequestBody List<SoB2cLogisticsEntity> list);

    /**
     * 根据跟踪单号进行物流跟踪号更新
     * @description
     * @param trackDTOS
     * @return
     * @date 2024-02-26 16:41
     * @author Lambda
     */
    @PostMapping("/feign/soB2c/updateTrackNoByTransportNo")
    Boolean updateTrackNoByTransportNo(@RequestBody List<LogisticsBillDTO.TrackDTO> trackDTOS);
    /**
     * 根据平台单号和平台查询B2C销售订单
     *
     * @date 2024-03-07
     * @author Jim
     */
    @GetMapping("/feign/soB2c/getByPlatformCode")
    List<SoB2cEntity>  getByPlatformCode(@RequestParam("platformCodeList") List<String> platformCodeList,
                                         @RequestParam("dictPlatform") String dictPlatform,
                                         @RequestParam("shopId") String shopId,
                                         @RequestParam("sourceType") String sourceType);

    @PostMapping("/feign/soB2c/updateById")
    Boolean updateById(@RequestBody SoB2cEntity soB2cEntity);

    @PostMapping("/feign/soB2c/updateStatus")
    Boolean updateStatus(@RequestBody SoB2cEntity soB2cEntity);

    @PostMapping("/feign/soB2c/checkAndFillBySoOutStock")
    Boolean checkAndFillBySoOutStock(@RequestBody PlatformSoOutStockDTO dto);


    @PostMapping("/feign/soB2cError/deleteAll")
    void checkAndDeleteAllError(@RequestBody SoB2cErrorDTO.DeleteDetailDTO deleteDTO);

    /**
     * 修改速卖通订单仓库
     * @Author Luo_WG
     * @Date 2024/2/1 10:44
     * @param soOutstockDTO
     * @return void
     **/
    @PostMapping("/feign/soB2c/getDataCompareByCondition")
    List<WmsDataCompareTaskDTO.SoB2cDTO> getDataCompareByCondition(@RequestBody WmsDataCompareTaskDTO.SoOutstockDTO soOutstockDTO);

    @PostMapping("/feign/soB2cLabel/saveSoB2cLabel")
    Boolean saveSoB2cLabel(@RequestBody List<SoB2cLabelDTO.UpdateDTO> dtoList);

    /**
     * 清除订单物流信息的发货信息
     * @Author Luo_WG
     * @Date 2024/4/18 16:23
     * @param soIdList
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/soB2c/clearB2cLogisticsCode")
    Boolean clearB2cLogisticsCode(@RequestBody List<String> soIdList);

    /**
     * 清除新增订单异常
     * @Author Luo_WG
     * @Date 2024/4/19 10:12
     * @param addAndDeleteDTO
     * @return void
     **/
    @PostMapping("/feign/soB2cError/deleteAndAddErrorBatch")
    void deleteAndAddErrorBatch(@RequestBody SoB2cErrorDTO.AddAndDeleteDTO addAndDeleteDTO);

    /**
     * @description: 订单拦截
     * @author Will
     * @date: 2024/4/24 19:07
     * @param dto
     * @return BatchResultDTO
     */
    @PostMapping("/feign/soB2c/deliveryIntercept")
    BatchResultDTO deliveryIntercept(@RequestBody @Validated SoB2cDTO.RemarkDTO dto);


    /**
     * 更新平台订单取消状态
     */
    @PostMapping("/feign/soB2c/updateCancelAndLog")
    Boolean updateCancelAndLog(@RequestBody @Validated PlatformDeliveryInterceptDTO dto);


    /**
     * 添加销售订单日志
     */
    @PostMapping("/feign/soB2c/addModuleOperateLog")
    Boolean addModuleOperateLog(@RequestBody OperateLogDTO.AddModuleOperateLogDTO operateLogDTO);

    /**
     * 根据扫描的单号获取订单
     * @param code
     * @return
     */
    @PostMapping("/feign/soB2c/packageScanByCode")
    PackageDTO.ScanResultDTO packageScanByCode(@RequestBody String code);

    /**
     * 修改订单物流重量
     * @param soId 订单id
     * @param id 订单物流表id
     * @param weightByG 重量（g）
     * @return
     */
    @GetMapping("/feign/soB2c/updateWeight")
    Boolean updateWeight(@RequestParam("soId") String soId,
                         @RequestParam("id") String id,
                         @RequestParam("weightByG") BigDecimal weightByG);

    /**
     * 根据销售订单ids 获取到合并的数据
     * @param ids
     * @return
     */
    @PostMapping("/feign/soB2c/listMergePackageBySoIds")
    List<PackageDTO.ScanResultDTO> listMergePackageBySoIds(@RequestBody List<String> ids);


    /**
     * 批量更新平台订单取消状态
     */
    @PostMapping("/feign/soB2c/batchUpdateCancelAndLog")
    Boolean batchUpdateCancelAndLog(@RequestBody List<String> soB2cIdList);


    /**
     * 根据销售单号查询订单
     */
    @GetMapping("/feign/soB2c/getSoCode")
    SoB2cEntity getSoCode(@RequestParam("soB2cCode") String soB2cCode);

    @GetMapping("/feign/soB2c/listRefBomSplit")
    SoB2cDTO.CombinationDTO listRefBomSplit(@RequestParam("detailId") String detailId);

    @PostMapping("/feign/soB2c/updateSignShippedByDetailId")
    void updateSignShippedByDetailId(@RequestBody List<String> detailIds);


    /**
     * 根据id和当前仓库ID获取到需要销售出单的数据
     *
     * @param soId 销售订单ID
     * @param warehouseId 仓库ID(非必传：默认第一个明细记录的仓库)
     * @return 新增销售出库单DTO
     */
    @GetMapping("/feign/soB2c/getSoOutStockByIdAndWarehouseId")
    SoOutstockDTO.GenerateB2cDTO getSoOutStockByIdAndWarehouseId(@RequestParam(value = "soId") String soId,
                                                                 @RequestParam(value = "warehouseId", required = false) String warehouseId);
    /**
     * 根据数据类型集合查询
     * @author will
     * @date 2024/6/25 20:06
     * @return SoB2cDataDTO
     */
    @PostMapping("/feign/soB2c/listSoB2cData")
    SoB2cDTO.SoB2cDataDTO listSoB2cData(@RequestBody @Validated SoB2cDTO.SoB2cDataParamDTO paramDTO);


    /**
     * 根据销售订单更新跟踪单号
     * @param soId
     * @param trackNo
     */
    @PostMapping("/feign/soB2c/updateLogisticsBySoId")
    void updateLogisticsBySoId(@RequestParam("soId") String soId, @RequestParam("trackNo") String trackNo);

    /**
     * 取消订单预报
     */
    @PostMapping("/feign/soB2c/cancelOrderForecast")
    ApiResult<List<BatchResultDTO>> cancelOrderForecast(@RequestBody BaseIdsDTO.IdsDTO dto);

    /**
     * 取消物流单
     */
    @PostMapping("/feign/soB2c/cancelLogistic")
    ApiResult<List<BatchResultDTO>> cancelLogistic(@RequestBody BaseIdsDTO.IdsDTO idDTO);
    /**
     * 查询所有虚拟仓B2C销售订单数据
     * @author will
     * @date 2024/9/26 14:47
     * @return List<ViewDTO>
     */
    @GetMapping("feign/soB2c/listAllVirtualSoB2cDetail")
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoB2cDetail();

    /**
     * 根据销售订单id获取订单 渠道+仓库+重量 基础信息
     * @param ids
     * @return
     */
    @PostMapping("feign/soB2c/getB2cLogisticsByIds")
    List<SoB2cDTO.LogisticsDTO> getB2cLogisticsByIds(@RequestBody List<String> ids);

    /**
     * 更新物流预估费用
     *
     * @param b2cSoId
     * @param totalShippingCost
     * @param currency
     */
    @GetMapping("feign/soB2c/updateLogisticsFee")
    void updateLogisticsFee(@RequestParam(value = "b2cSoId")String b2cSoId,
                            @RequestParam(value = "totalShippingCost") BigDecimal totalShippingCost,
                            @RequestParam(value = "currency") String currency);

    /**
     * 更新 超过订单金额比例标识
     * @param b2cSoId
     * @param isOverEstimatedShipCost
     */
    @GetMapping("feign/soB2c/updateOverEstimatedShipCost")
    void updateOverEstimatedShipCost(@RequestParam(value = "b2cSoId") String b2cSoId,
                                     @RequestParam(value = "isOverEstimatedShipCost") Boolean isOverEstimatedShipCost);

    /**
     * 同步速递云线上订单/配货单
     * @param soId
     * @param operateEnum
     */
    @GetMapping("feign/soB2c/syncSdyOrderHandler")
    void syncSdyOrderHandler(@RequestParam("soId") String soId, @RequestParam("operateEnum") String operateEnum);
}
