package com.erp.server.oms.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * B2c销售订单
 *
 * @author Will
 * @date: 2023/11/20 11:51
 */
@Slf4j
@RestController
@RequestMapping("/feign/soB2c")
public class SoB2cFeignController extends BaseController {

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cStatusService b2cStatusService;

    @Resource
    private SoB2cSplitService soB2cSplitService;


    /**
     * 根据b2c订单id获取物流信息
     *
     * @param mainIdList
     * @return ApiResult<List < SoB2cLogisticsEntity>>
     * @author Will
     * @date: 2023/11/20 11:53
     */
    @PostMapping("/listSoB2cLogisticsByMainIdList")
    public List<SoB2cLogisticsEntity> listSoB2cLogisticsByMainIdList(@RequestBody List<String> mainIdList) {
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(mainIdList);
        return soB2cLogisticsList;
    }

    /**
     * 根据跟踪单号查询订单物流信息
     * @param trackNo
     * @return
     */
    @PostMapping("/getSoB2cLogisticsByTrackNo")
    public SoB2cLogisticsEntity getSoB2cLogisticsByTrackNo(@RequestBody String trackNo) {
        SoB2cLogisticsEntity soB2cLogistics = soB2cLogisticsService.getSoB2cLogisticsByTrackNo(trackNo);
        return soB2cLogistics;
    }

    /**
     * 根据跟踪单号查询订单物流信息
     * @author will
     * @date 2024/7/5 10:27
     * @param logisticsCode
     * @return SoB2cLogisticsEntity
     */
    @PostMapping("/getByTrackNoOrTransportNo")
    public SoB2cLogisticsEntity getByTrackNoOrTransportNo(@RequestBody String logisticsCode) {
        SoB2cLogisticsEntity soB2cLogistics = soB2cLogisticsService.getByTrackNoOrTransportNo(logisticsCode);
        return soB2cLogistics;
    }

    /**
     * 根据订单id获取物流费用的参数
     *
     * @param orderId
     * @return
     */
    @PostMapping("/getShippingCalculationByOrderId")
    public SoB2cDTO.ShippingCalculationDTO getShippingCalculationByOrderId(@RequestBody String orderId) {
        SoB2cDTO.ShippingCalculationDTO result = soB2cLogisticsService.getShippingCalculationByOrderId(orderId);
        return result;
    }

    /**
     * 根据b2c详情id获取详情
     *
     * @param detailIdList
     * @return ApiResult<List < SoB2cLogisticsEntity>>
     * @author Will
     * @date: 2023/11/20 11:53
     */
    @PostMapping("/listDetailByIds")
    public List<SoB2cDetailEntity> listDetailByIds(@RequestBody List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cDetailEntity> list = soB2cDetailService.listByIds(detailIdList);
        return list;
    }

    /**
     * 根据b2c详情id获取详情（包含删除）
     */
    @PostMapping("/listDetailContainDeleted")
    public List<SoB2cDetailEntity> listDetailContainDeleted(@RequestBody List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cDetailEntity> list = soB2cDetailService.listContainDeleted(detailIdList);
        return list;
    }

    @GetMapping("/listRefBomSplit")
    public SoB2cDTO.CombinationDTO listRefBomSplit(@RequestParam("detailId") String detailId){
        return soB2cSplitService.listRefBomSplit(detailId);
    }

    /**
     * 更新标记发货标识
     * @param detailIdList
     * @return
     */
    @PostMapping("/updateSignShippedByDetailId")
    public void updateSignShippedByDetailId(@RequestBody List<String> detailIdList) {
        if (CollectionUtils.isEmpty(detailIdList)) {
            return ;
        }
        soB2cDetailService.updateSignShippedByDetailId(detailIdList);
    }
    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soIds
     * @return
     */
    @PostMapping("/listByIds")
    public List<SoB2cEntity> listByIds(@RequestBody List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        List<SoB2cEntity> list = soB2cService.listByIds(soIds);
        return list;
    }
    /**
     * 查询已拦截的订单
     *
     * @return
     */
    @PostMapping("/listWithIsIntercept")
    public List<SoB2cEntity> listWithIsIntercept() {
        List<SoB2cEntity> list = soB2cService.listWithIsIntercept();
        return list;
    }
    /**
     * 根据主表id查询B2C订单主表信息
     *
     * @param soId
     * @return
     */
    @GetMapping("/getById")
    public SoB2cEntity getById(@RequestParam("soId") String soId) {
        if (StringUtils.isBlank(soId)) {
            return null;
        }
        return soB2cService.getById(soId);
    }

    /**
     * @param soId 销售订单id
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-18 11:09
     */
    @PostMapping("/orderShipped")
    public Boolean orderShipped(@RequestBody String soId) {
        Boolean result = soB2cService.orderShipped(soId);
        return result;
    }

    /**
     * 根据code 获取到需要销售出单的数据
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-27 19:54
     */
    @PostMapping("/getSoOutstockInfoByCode")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(@RequestBody String soCode) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.getSoOutstockInfoByCode(soCode);
        return result;
    }

    /**
     * 根据id 获取到需要销售出单的数据
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-27 19:54
     */
    @PostMapping("/getSoOutstockInfoById")
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(@RequestBody String soId) {
        SoOutstockDTO.GenerateB2cDTO result = soB2cService.getSoOutstockInfoById(soId);
        return result;
    }


    /**
     * 根据b2c订单id查询详情信息
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoB2cDetailEntity>
     * @Author Luo_WG
     * @Date 2023/12/19 15:22
     **/
    @PostMapping("/listDetailByMainIds")
    public List<SoB2cDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        List<SoB2cDetailEntity> list = soB2cDetailService.listByMainIds(mainIds);
        return list;
    }

    /**
     * 根据b2c订单id获取买家信息
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.oms.entity.SoB2cLogisticsEntity>
     * @Author Luo_WG
     * @Date 2023/12/22 9:20
     **/
    @PostMapping("/listSoB2cReceiverByMainIdList")
    public List<SoB2cReceiverEntity> listSoB2cReceiverByMainIdList(@RequestBody List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cReceiverEntity> list = soB2cReceiverService.listByMainIds(mainIdList);
        return list;
    }

    /**
     * 获取标记发货参数
     *
     * @return
     */
    @PostMapping("/getSignShipParam")
    public SoB2cDTO.SignShipOrderDTO getSignShipParam(@RequestBody String soB2cId) {
        return soB2cService.getSignShipParam(soB2cId);
    }

    /**
     * 校验是否需要调用第三方标记发货
     *
     * @param soB2cId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 11:29
     **/
    @PostMapping("/checkPlatformShipOrder")
    public Boolean checkPlatformShipOrder(@RequestBody String soB2cId) {
        return soB2cService.checkPlatformShipOrder(soB2cId);
    }

    /**
     * 修改b2c销售单状态
     *
     * @param soB2cIds
     * @param status
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     **/
    @PostMapping("/updateSoB2cStatus")
    Boolean updateSoB2cStatus(@RequestParam("soB2cIds") List<String> soB2cIds, @RequestParam("status") String status, @RequestParam("isManualDelivery")Boolean isManualDelivery) {
        return soB2cService.updateSoB2cStatus(soB2cIds, status, isManualDelivery);
    }

    /**
     * 修改b2c销售单状态发货时间
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     * @param deliveryTimeDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/updateSoB2cStatusAndDeliveryTime")
    Boolean updateSoB2cStatusAndDeliveryTime(@RequestBody SoB2cDTO.UpdateDeliveryTimeDTO deliveryTimeDTO) {
        return soB2cService.updateSoB2cStatusAndDeliveryTime(deliveryTimeDTO);
    }

    /**
     * 获取销售订单物流渠道 根据渠道id
     *
     * @param channelId
     * @return java.lang.Boolean
     * @Author yl
     * @Date 2023/12/27 11:29
     **/
    @PostMapping("/listSoB2cLogisticsByChannelId")
    public List<SoB2cLogisticsEntity> listSoB2cLogisticsByChannelId(@RequestBody String channelId) {
        return soB2cLogisticsService.listByChannelId(channelId);
    }

    /**
     * 设置打印面单需要的字段
     *
     * @param soIds
     * @return java.util.List<com.common.business.dto.PrintWayBillPdfDTO>
     * @Author Luo_WG
     * @Date 2023/12/28 15:37
     **/
    @PostMapping("/printWayBillPdf")
    public List<PrintWayBillPdfDTO> printWayBillPdf(@RequestBody List<String> soIds) {
        return soB2cService.printWayBillPdf(soIds);
    }

    /**
     * 更改订单状态
     *
     * @param dto
     * @Author yl
     * @Date 2023/12/28 15:37
     **/
    @PostMapping("/updateSoB2cStatusByParams")
    public Boolean updateSoB2cStatusByParams(@RequestBody SoB2cDTO.UpdateStatusDTO dto) {
        return soB2cService.updateSoB2cStatusByParams(dto);
    }

    /**
     * 获取客户信息
     *
     * @param soId
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-29 16:42
     */
    @PostMapping("/getB2cCustomerById")
    public SoB2cDTO.CustomerDTO getB2cCustomerById(@RequestBody String soId) {
        return soB2cService.getB2cCustomerById(soId);
    }

    /**
     * @param soIdList
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-01 9:38
     */
    @PostMapping("/listCustomer")
    public List<SoB2cDTO.CustomerDTO> listCustomer(@RequestBody List<String> soIdList) {
        return soB2cService.listCustomer(soIdList);
    }

    /**
     * 获取沃尔玛发货参数
     *
     * @param soId
     * @return com.common.business.dto.WalmartShipDTO
     * @Author Luo_WG
     * @Date 2024/1/3 17:23
     **/
    @PostMapping("/getWalmartShipOrderParam")
    public List<WalmartShipDTO> getWalmartShipOrderParam(@RequestBody String soId) {
        return soB2cService.getWalmartShipOrderParam(soId);
    }

    /**
     * 获取b2c 销售订单信息 仓库为空
     *
     * @param soIdList
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-03 16:47
     */
    @PostMapping("/listWarehouseIsEmpty")
    public List<SoB2cEntity> listWarehouseIsEmpty(@RequestBody List<String> soIdList) {
        return soB2cService.listWarehouseIsEmpty(soIdList);
    }

    /**
     * 根据店铺仓库更新仓库
     *
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-03 17:27
     */
    @PostMapping("updateWarehouseByShopId")
    public Boolean updateWarehouseByShopId(@RequestParam("id") String id, @RequestParam("shopId") String shopId) {
        return soB2cService.updateWarehouseByShopId(id, shopId);
    }

    /**
     * 查询合并来源关系
     *
     * @return
     * @description
     * @author Jim
     * @create 2024-01-03
     */
    @GetMapping("/findMergeByTargetId")
    public List<SoB2cRefEntity> findMergeByTargetId(@RequestParam("targetId") String targetId) {
        return soB2cRefService.listByTargetId(targetId, SoB2cOptionTypeEnum.ENUM_MERGE);
    }

    /**
     * 获取到b2c 销售订单物流跟踪号为空的
     *
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-05 9:44
     */
    @GetMapping("/listTrackNoEmptyList")
    public List<SoB2cLogisticsDTO.TrackNoDTO> listTrackNoEmptyList() {
        return soB2cLogisticsService.listTrackNoEmptyList();
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return com.erp.model.oms.dto.SoB2cDTO.ViewDTO
     * @Author Luo_WG
     * @Date 2024/1/5 9:50
     **/
    @GetMapping("/view")
    public SoB2cDTO.ViewDTO view(@RequestParam("id") String id) {
        return soB2cService.view(id);
    }

    /**
     * 拦截打标识，冻结订单
     *
     * @param interceptUpdateOrderDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/17 18:54
     **/
    @PostMapping("/updateIntercept")
    public Boolean updateIntercept(@RequestBody SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO) {
        return soB2cService.updateIntercept(interceptUpdateOrderDTO);
    }

    /**
     * 修改订单异常原因
     *
     * @param id
     * @param soB2cAbnormalType
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/19 10:51
     **/
    @GetMapping("/updateAbnormalType")
    public Boolean updateAbnormalType(@RequestParam("id") String id, @RequestParam("soB2cAbnormalType") String soB2cAbnormalType) {
        return soB2cService.updateAbnormalType(id, soB2cAbnormalType);
    }


    /**
     * 更新组包状态
     *
     * @param dto
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-19 10:57
     */
    @PostMapping("/updatePackageStatus")
    public Boolean updatePackageStatus(@RequestBody @Validated UpdateStateDTO.UpdateByStrStatusDTO dto) {
        return soB2cService.updatePackageStatus(dto);
    }

    /**
     * 更新中转状态
     *
     * @param dto
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-19 10:57
     */
    @PostMapping("/updateTransferStatus")
    public Boolean updateTransferStatus(@RequestBody @Validated UpdateStateDTO.UpdateByStrStatusDTO dto) {
        return soB2cService.updateTransferStatus(dto);
    }

    /**
     * 根据报关设置生成报关单信息
     *
     * @param viewDTOList
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.AddDTO>
     * @Author Luo_WG
     * @Date 2024/1/25 17:29
     **/
    @PostMapping("/generateTransferDeclareView")
    public List<TransferDeclareDTO.AddDTO> generateTransferDeclareView(@RequestBody @Validated List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList) {
        return soB2cService.generateTransferDeclareView(viewDTOList);
    }

    /**
     * 更改订单的中转状态
     *
     * @param soIds  订单id
     * @param status 中转状态
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/25 19:39
     **/
    @PostMapping("/updateTransferStatusBatch")
    public Boolean updateTransferStatusBatch(@RequestParam("soIds") List<String> soIds, @RequestParam("status") String status) {
        return soB2cService.updateTransferStatusBatch(soIds, status);
    }

    /**
     * 修改订单的第三方物流单号
     *
     * @param list
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/1/29 17:04
     **/
    @PostMapping("/updateShippingOrderNo")
    public Boolean updateShippingOrderNo(@RequestBody List<TransferDeclareDTO.ShippingOrderDTO> list) {
        return soB2cService.updateShippingOrderNo(list);
    }

    /**
     * 单个更新订单预报号
     * @param shippingOrderDTO
     * @return
     */
    @PostMapping("/updateShippingOrderNoBySoId")
    Boolean updateShippingOrderNoBySoId(@RequestBody TransferDeclareDTO.ShippingOrderDTO shippingOrderDTO){
        return soB2cService.updateShippingOrderNoBySoId(shippingOrderDTO);
    }
    /**
     * 根据销售订单拆分sku
     * 拆分逻辑为 物流产品 为拆分 sku为组合时进行拆分
     *
     * @param soIds
     * @return
     */
    @PostMapping("/getTransferDeclareProductBySoIds")
    public List<SplitSkuDTO> getTransferDeclareProductBySoIds(@RequestBody List<String> soIds) {
        return soB2cService.getTransferDeclareProductBySoIds(soIds);
    }

    /**
     * 修改速卖通订单仓库
     *
     * @param soId
     * @return void
     * @Author Luo_WG
     * @Date 2024/2/1 10:44
     **/
    @PostMapping("/updateAliExpressOrderWarehouse")
    public Boolean updateAliExpressOrderWarehouse(@RequestParam("soId") String soId, @RequestParam("shopId") String shopId) {
        return soB2cService.updateAliExpressOrderWarehouse(soId, shopId);
    }

    @PostMapping("/batchUpdateLogistics")
    public Boolean batchUpdateLogistics(@RequestBody List<SoB2cLogisticsEntity> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return soB2cLogisticsService.updateBatchById(list);
        } else {
            return true;
        }
    }

    /**
     * 根据跟踪单号进行物流跟踪号更新
     * @description
     * @param trackDTOS
     * @return
     * @date 2024-02-26 16:41
     * @author Lambda
     */
    @PostMapping("/updateTrackNoByTransportNo")
    public Boolean updateTrackNoByTransportNo(@RequestBody List<LogisticsBillDTO.TrackDTO> trackDTOS){
        if (CollectionUtils.isEmpty(trackDTOS)){
            return Boolean.TRUE;
        }
         soB2cLogisticsService.updateTrackNoByTransportNo(trackDTOS);
        return Boolean.TRUE;
    }
    /**
     * 根据销售订单更新跟踪单号
     * @param soId
     * @param trackNo
     * @return
     */
    @PostMapping("/updateLogisticsBySoId")
    public void updateLogisticsBySoId(@RequestParam("soId") String soId, @RequestParam("trackNo") String trackNo) {
        soB2cLogisticsService.updateLogisticsBySoId(soId, trackNo);
    }

    /**
     * 根据平台单号和平台查询B2C销售订单
     *
     * @date 2024-03-07
     * @author Jim
     */
    @GetMapping("/getByPlatformCode")
    public List<SoB2cEntity> getByPlatformCode(@RequestParam("platformCodeList") List<String> platformCodeList,
                                               @RequestParam("dictPlatform") String dictPlatform,
                                               @RequestParam("shopId") String shopId,
                                               @RequestParam("sourceType") String sourceType
    ){
        return soB2cService.getByPlatformCodeList(platformCodeList, dictPlatform, shopId, sourceType);
    }

    /**
     * 根据平台单号和平台查询B2C销售订单
     *
     * @date 2024-03-07
     * @author Jim
     */
    @PostMapping("/updateById")
    public Boolean updateById(@RequestBody SoB2cEntity soB2cEntity) {
        return soB2cService.updateById(soB2cEntity);
    }

    @PostMapping("/updateStatus")
    public Boolean updateStatus(@RequestBody SoB2cEntity soB2cEntity) {
        return soB2cService.updateStatus(soB2cEntity);
    }

    @PostMapping("/checkAndFillBySoOutStock")
    public Boolean checkAndFillBySoOutStock(@RequestBody PlatformSoOutStockDTO dto){
        return soB2cService.checkAndFillBySoOutStock(dto);
    }
        
    @PostMapping("/getDataCompareByCondition")
    public List<WmsDataCompareTaskDTO.SoB2cDTO> getDataCompareByCondition(@RequestBody WmsDataCompareTaskDTO.SoOutstockDTO soOutstockDTO) {
        return soB2cService.getDataCompareByCondition(soOutstockDTO);
    }

    /**
     * 清除订单物流信息的发货信息
     * @Author Luo_WG
     * @Date 2024/4/18 16:23
     * @param soIdList
     * @return java.lang.Boolean
     **/
    @PostMapping("/clearB2cLogisticsCode")
    public Boolean clearB2cLogisticsCode(@RequestBody List<String> soIdList) {
        return soB2cLogisticsService.clearB2cLogisticsCode(soIdList);
    }

    /**
     * 订单拦截
     * @author Will
     * @date: 2024/4/24 19:05
     * @param dto
     * @return BatchResultDTO
     */
    @PostMapping("/deliveryIntercept")
    public BatchResultDTO deliveryIntercept(@RequestBody @Validated SoB2cDTO.RemarkDTO dto) {
        return soB2cService.deliveryIntercept(dto.getId(),dto.getRemark());
    }


    /**
     * 更新平台订单取消状态
     */
    @PostMapping("/updateCancelAndLog")
    public Boolean updateCancelAndLog(@RequestBody @Validated PlatformDeliveryInterceptDTO dto){
        return b2cStatusService.updateCancelAndLog(dto);
    }


    /**
     * 添加操作日志
     */
    @PostMapping("/addModuleOperateLog")
    public Boolean addModuleOperateLog(@RequestBody OperateLogDTO.AddModuleOperateLogDTO operateLogDTO){
        return operateLogService.addModuleOperateLog(operateLogDTO.getContent(),
                operateLogDTO.getModuleType(),
                operateLogDTO.getBusinessId(),
                operateLogDTO.getOperation());
    }


    /**
     * 根据扫描的单号获取订单
     * @param code
     * @return
     */
    @PostMapping("/packageScanByCode")
    public PackageDTO.ScanResultDTO packageScanByCode(@RequestBody String code) {
        return soB2cService.packageScanByCode(code);
    }

    /**
     * 修改订单物流重量
     * @param soId 订单id
     * @param id 订单物流表id
     * @param weightByG 重量（g）
     * @return
     */
    @GetMapping("/updateWeight")
    public Boolean updateWeight(@RequestParam("soId") String soId,
                                @RequestParam("id") String id,
                                @RequestParam("weightByG") BigDecimal weightByG) {
        return soB2cLogisticsService.updateWeight(soId, id, weightByG, "组包称重");
    }

    /**
     * 根据销售订单ids 获取到合并的数据
     * @param ids
     * @return
     */
    @PostMapping("/listMergePackageBySoIds")
    public List<PackageDTO.ScanResultDTO> listMergePackageBySoIds(@RequestBody List<String> ids) {
        return soB2cService.listMergePackageBySoIds(ids);
    }


    /**
     * 更新平台订单取消状态
     */
    @PostMapping("/batchUpdateCancelAndLog")
    public Boolean batchUpdateCancelAndLog(@RequestBody List<String> soB2cIdList){
        return b2cStatusService.batchUpdateCancelAndLog(soB2cIdList);
    }


    /**
     * 更新平台订单取消状态
     */
    @GetMapping("/getSoCode")
    public SoB2cEntity getSoCode(@RequestParam("soB2cCode") String soB2cCode) {
        return soB2cService.getByCode(soB2cCode);
    }


    /**
     * 根据id和当前仓库ID获取到需要销售出单的数据
     */
    @GetMapping("/getSoOutStockByIdAndWarehouseId")
    public SoOutstockDTO.GenerateB2cDTO getSoOutStockByIdAndWarehouseId(@RequestParam(value = "soId") String soId,
                                                              @RequestParam(value = "warehouseId", required = false) String warehouseId
    ) {
       return soB2cService.getSoOutstockByIdAndWarehouseId(soId, warehouseId);
    }


    /**
     * 查询b2c销售订单数据
     * @author will
     * @date 2024/6/25 20:14
     * @param paramDTO
     * @return SoB2cEntity
     */
    @PostMapping("/listSoB2cData")
    public SoB2cDTO.SoB2cDataDTO listSoB2cData(@RequestBody @Validated SoB2cDTO.SoB2cDataParamDTO paramDTO) {
        return soB2cService.listSoB2cData(paramDTO);
    }

    /**
     * 取消订单预报
     */
    @PostMapping("/cancelOrderForecast")
    public ApiResult<List<BatchResultDTO>> cancelOrderForecast(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = soB2cService.cancelOrderForecast(dto.getIds(), false);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消物流单
     * @return
     */
    @PostMapping("/cancelLogistic")
    public ApiResult<List<BatchResultDTO>> cancelLogistic(@RequestBody @Validated BaseIdsDTO.IdsDTO idDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(idDTO.getIds().size());
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(idDTO.getIds());
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(idDTO.getIds());
        for (String id : idDTO.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cLogisticsService.cancelLogistic(id,soB2cEntityList,soB2cLogisticsEntityList, false);
            } catch (Exception e) {
                log.error("B2C销售订单取消物流单失败", e);
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "B2C销售订单不存在, 获取物流单号失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查询所有虚拟仓B2C销售订单数据
     * @author will
     * @date 2024/9/26 17:10
     * @return List<ViewDTO>
     */
    @GetMapping("/listAllVirtualSoB2cDetail")
    public List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoB2cDetail(){
        return soB2cDetailService.listAllVirtualSoB2cDetail();
    }

    /**
     * 根据销售订单id获取订单 渠道+仓库+重量 基础信息
     * @param ids
     * @return
     */
    @PostMapping("/getB2cLogisticsByIds")
    public List<SoB2cDTO.LogisticsDTO> getB2cLogisticsByIds(@RequestBody List<String> ids){
        return soB2cService.getB2cLogisticsByIds(ids);
    }
    /**
     * 更新物流预估费用
     * @param b2cSoId
     * @param totalShippingCost
     */
    @GetMapping("/updateLogisticsFee")
    public void updateLogisticsFee(@RequestParam(value = "b2cSoId")String b2cSoId,
                                   @RequestParam(value = "totalShippingCost") BigDecimal totalShippingCost,
                                   @RequestParam(value = "currency") String currency){
        soB2cLogisticsService.updateLogisticsFee(b2cSoId, totalShippingCost,currency);
    }

    /**
     * 更新 超过订单金额比例标识
     * @param b2cSoId
     * @param isOverEstimatedShipCost
     */
    @GetMapping("/updateOverEstimatedShipCost")
    public void updateOverEstimatedShipCost(@RequestParam(value = "b2cSoId") String b2cSoId,
                                     @RequestParam(value = "isOverEstimatedShipCost") Boolean isOverEstimatedShipCost){
        soB2cService.updateOverEstimatedShipCost(b2cSoId, isOverEstimatedShipCost);
    }

    /**
     * 同步速递云线上订单/配货单
     * @param soId
     * @param operateEnum
     */
    @GetMapping("/syncSdyOrderHandler")
    public void syncSdyOrderHandler(@RequestParam("soId") String soId, @RequestParam("operateEnum") String operateEnum) {
        soB2cService.syncSdyOrderHandler(soId, operateEnum);
    }
}
