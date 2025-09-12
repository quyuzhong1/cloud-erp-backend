package com.erp.server.oms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.WalmartShipDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cCategoryTypeEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * B2C销售订单表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cService extends SuperService<SoB2cEntity> {

      /**
      * 分页列表查询
      * @author Will
      * @date: 2023-08-18
      * @param pagingParamDTO
      * @return PagingVO<SoB2cDTO.ListDTO>>
      */
      PagingVO<SoB2cDTO.ListDTO> paging(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return List<SoB2cDTO.TabListDTO>>
     */
     List<SoB2cDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Will
     * @date: 2023-08-18
     * @param id
     * @return
     */
     SoB2cDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
     SoB2cEntity add(SoB2cDTO.AddDTO dto,String code);

     /**
     * 修改
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
    BatchResultDTO update(SoB2cDTO.UpdateDTO dto);

     /**
      * 提交审核
      *
      * @param entity
      * @param error
      * @param soB2cLogisticsEntity
      * @return
      * @author Will
      * @date: 2023-08-18
      */
    ApproveResultDTO submit(SoB2cEntity entity, SoB2cErrorEntity error,SoB2cLogisticsEntity soB2cLogisticsEntity, Boolean isProcess);

    /**
    * 审核
    * @author Will
    * @date: 2023-08-18
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto,Boolean isMatch,String ruleName);

    /**
    * 作废
    * @author Will
    * @date: 2023-08-18
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum);

    /**
     * @description: 反作废
     * @author Will
     * @date: 2023/8/18 15:33
     * @param id
     * @param soB2cInvalidTypeEnum
     * @return BatchResultDTO
     */
    BatchResultDTO unInvalid(String id, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity,Boolean isMatch);
    /**
     * 修改订单备注
     * @author Will
     * @date: 2023/8/18 15:38
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO updateRemark(String id, String remark);
    /**
     * 编辑分类
     * @author Will
     * @date: 2023/8/18 15:47
     * @param id
     * @param typeEnum
     * @param categoryIdList
     * @return BatchResultDTO
     */
    BatchResultDTO updateCategory(String id, SoB2cCategoryTypeEnum typeEnum, List<String> categoryIdList);
    /**
     * @description: 订单配货数据显示
     * @author Will
     * @date: 2023/8/18 16:35
     * @param dto 
     * @return List<ViewSoB2cDistributionDTO> 
     */
    List<SoB2cDTO.ViewSoB2cDistributionDTO> viewSoB2cDistribution(BaseIdsDTO.IdsDTO dto);
    /**
     * @description: 订单配货保存
     * @author Will
     * @date: 2023/8/18 16:42
     * @param id
     * @param dto
     * @return BatchResultDTO
     */
    BatchResultDTO saveSoB2cDistribution(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto);

    /**
     * 检查订单是否符合物流黑名单限制
     * @param soId
     * @param logisticsChannelId
     * @param existChannelId
     */
    void checkLogisticsChannelBlacklist(String soId, String logisticsChannelId, String existChannelId);

    /**
     * @description: 获取物流单号
     * @author Will
     * @date: 2023/8/18 16:47
     * @param id
     * @param isDelivery
     * @return BatchResultDTO
     */
    BatchResultDTO getLogisticsCode(String id, Boolean isDelivery);
    /**
     * @param id
     * @param channelId
     * @return BatchResultDTO
     * @description: 提交发货
     * @author Will
     * @date: 2023/8/18 16:49
     */
    BatchResultDTO submitDelivery(String id, String channelId);
    /**
     * @description: 发货拦截
     * @author Will
     * @date: 2023/8/18 16:52
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO deliveryIntercept(String id, String remark);
    /**
     * @description: 取消发货拦截
     * @author Will
     * @date: 2023/8/18 16:53
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelDeliveryIntercept(String id);
    /**
     * @description: 合并列表
     * @author Will
     * @date: 2023/8/18 18:32
     * @param dto
     * @return PagingVO<MergeListDTO>
     */
    PagingVO<SoB2cDTO.MergeListDTO> mergePaging(PagingDTO<SoB2cDTO.MergePagingParamDTO> dto);
    /**
     * @description: 合并列表数量
     * @author Will
     * @date: 2023/8/24 16:17
     * @param pagingParamDTO
     * @return Integer
     */
    Integer mergePagingCount(SoB2cDTO.MergePagingParamDTO pagingParamDTO);
    /**
     * @description:合并保存
     * @author Will
     * @date: 2023/8/21 9:02
     * @param ids
     */
    String mergeSave(List<String> ids);
    /**
     * @description: 取消合并
     * @author Will
     * @date: 2023/8/21 9:09
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelMerge(String id);

    /**
     * 删除订单关联信息
     * @param ids 需要删除的销售订单数据
     * @param code 关联销售订单编码
     */
    void deleteById(List<String> ids, String code);

    void handleData(SoB2cEntity soB2cEntity, Boolean exchangeRateThrow, Boolean checkPayTime);

    /**
     * 匹配审核规则
     */
    Map<String,Boolean> approveRule(String id, List<SoB2cDetailEntity> detailList, Map<String,Object> map);


    /**
     * @description: 查看财务信息
     * @author Will
     * @date: 2023/9/6 15:44
     * @param dto
     * @return FinancialInfoDTO
     */
    SoB2cDTO.FinancialInfoDTO getFinancialInfoById(SoB2cDTO.FinancialParamDTO dto);

    /**
     * @description: 查看财务信息
     * @author Will
     * @date: 2023/9/6 15:44
     * @param dto
     * @return FinancialInfoDTO
     */
    SoB2cDTO.FinancialInfoDTO getFinancialInfo(SoB2cDTO.FinancialParamDTO dto,Boolean isAdd);
    /**
     * @description: 标记不合并
     * @author Will
     * @date: 2023/9/11 9:26
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO isNotNeedMerge(String id);

    /**
     * 平台订单更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cDTO.PullOrderResultDTO saveOrUpdateEntity(PlatformOrderDTO dto, ShopInfoEntity shopInfo);

    /**
     * 通过哟平台订单ID和类型查询
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cEntity getByPlatformInfo(String platformCode, String dictPlatform, String shopId, String code);

    Map<String,Object> getJson(String id);

    /**
     * 当拉取订单后 未自动匹配成功的 自动匹配
     * @author yl
     * @date 2023-12-08 9:21
     * @param dto
     * @return
     */
    Boolean matchSku(SoB2cDTO.MatchSkuDTO dto);

    void updateLingXingOrder(SoB2cEntity entity, List<SoB2cDetailEntity> detailEntityList);

    /**
     * 获取销售出库单需要的数据
     * @description
     * @param id
     * @author Lambda
     * @return 
     * @create 2023-12-13 17:40
     */
    Boolean  orderShipped(String  id);

    /**
     * @description 运费测算后选择物流渠道
     * @param dto
     * @author Lambda
     * @return Boolean
     * @create 2023-12-15 12:27
     */
    Boolean selectLogisticsChannel(SoB2cLogisticsDTO.SelectChannelDTO dto);

    /**
     * 平台仓订单处理
     * 走仓库规则 通过就是审核通过 并待发货
     * 没有通过就是审核通过有待配货
     * @description
     * @param id
     * @param  map
     * @author Lambda
     * @return 
     * @create 2023-12-18 14:06
     */
    Boolean platformWarehouseOrderHandle(String id , Map<String,Object> map);

  
    /** 
     * @description 正常订单拉取处理规则
     * @param id
     * @author Lambda
     * @return 
     * @create 2023-12-18 14:52
     */
    Boolean pullOrderHandle(String id, List<SoB2cDetailEntity> detailList,Map<String, Object> map);

    /** 
     * @description 获取规则需要的map
     * @param id 订单id
     * @param detailList 详情
     * @author Lambda
     * @return 
     * @create 2023-12-18 15:03
     */
    Map<String, Object> handleMatchJson(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map);

    /**
     * 添加销售订单异常标示
     * @description
     * @param id
     * @param sign
     * @author Lambda
     * @return
     * @create 2023-12-20 11:43
     */
    void addSignError(String id, String sign);

    /**
     * 删除异常的标示
     * @description
     * @param id
     * @param sign
     * @author Lambda
     * @return 
     * @create 2023-12-20 15:48
     */
    void removeSignError(String id, String sign);

    /**
     * 获取标记发货需要的参数
     * @description
     * @param soB2cId
     * @author Lambda
     * @return 
     * @create 2023-12-22 16:02
     */
    SoB2cDTO.SignShipOrderDTO getSignShipParam(String soB2cId);

    /**
     * 校验是否需要调用第三方标记发货
     * @Author Luo_WG
     * @Date 2023/12/27 11:30
     * @param soB2cId
     * @return java.lang.Boolean
     **/
    Boolean checkPlatformShipOrder(String soB2cId);

    /**
     * 手动标发
     * @Author Luo_WG
     * @Date 2023/12/27 15:07
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO falseDelivery(String id);

    /**
     * 根据销售单code 获取
     * @description
     * @param soCode
     * @author Lambda
     * @return
     * @create 2023-12-27 19:55
     */
    SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoByCode(String soCode);

    /**
     * 根据销售订单id 获取销售出库详情
     * @description
     * @param soId
     * @author Lambda
     * @return
     * @create 2023-12-28 12:12
     */
    SoOutstockDTO.GenerateB2cDTO getSoOutstockInfoById(String soId);

    /**
     * 修改b2c销售单状态
     *
     * @param soB2cIds
     * @param status
     * @param isManualDelivery
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/27 20:19
     **/
    Boolean updateSoB2cStatus(List<String> soB2cIds, String status, Boolean isManualDelivery);

    /**
     * 修改b2c销售单状态发货时间
     * @Author Luo_WG
     * @Date 2023/12/27 20:14
     * @param deliveryTimeDTO
     * @return java.lang.Boolean
     **/
    Boolean updateSoB2cStatusAndDeliveryTime(SoB2cDTO.UpdateDeliveryTimeDTO deliveryTimeDTO);

    /**
     * 设置打印面单需要的字段
     * @Author Luo_WG
     * @Date 2023/12/28 15:39
     * @param soIds
     * @return java.util.List<com.common.business.dto.PrintWayBillPdfDTO>
     **/
    List<PrintWayBillPdfDTO> printWayBillPdf(List<String> soIds);

    /** 更改订单状态 根据code
     * @description
     * @param
     * @author Lambda
     * @return 
     * @create 2023-12-28 18:58
     */
    Boolean updateSoB2cStatusByParams(SoB2cDTO.UpdateStatusDTO dto);

    /**
     * 订单规则
     * @description
     * @param id
     * @author Lambda
     * @return 
     * @create 2023-12-28 20:57
     */
    SoB2cDTO.RuleResultDTO orderRule(String id);

    /**
     * 仓库规则
     * @param id
     * @param soB2cDetailList
     * @param map
     * @return
     */
    SoB2cDTO.RuleResultDTO warehouseRule(String id, List<SoB2cDetailEntity> soB2cDetailList, Map<String, Object> map);

    /**
     * 物流规则
     *
     * @param
     * @param isCheckProductRegistration
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-29 8:41
     */
    SoB2cDTO.RuleResultDTO logisticsRule(String id, Map<String, Object> map, Boolean isCheckProductRegistration);

    /**
     * 获取客户信息
     * @description
     * @param soId
     * @author Lambda
     * @return
     * @create 2023-12-29 16:42
     */
    SoB2cDTO.CustomerDTO getB2cCustomerById(String soId);

    /**
     * 获取客户信息
     * @param soIdList
     * @return
     */
    List<SoB2cDTO.CustomerDTO> listCustomer(List<String> soIdList);

    /**
     * 获取沃尔玛发货参数
     * @Author Luo_WG
     * @Date 2024/1/3 17:23
     * @param soId
     * @return java.util.List<com.common.business.dto.WalmartShipDTO>
     **/
    List<WalmartShipDTO> getWalmartShipOrderParam(String soId);

    /**
     * 获取b2c 销售订单仓库为空列表
     * @description
     * @param soIdList
     * @author Lambda
     * @return
     * @create 2024-01-03 16:48
     */
    List<SoB2cEntity> listWarehouseIsEmpty(List<String> soIdList);

    /**
     * 根据店铺的仓库更改仓库
     * @description
     * @author Lambda
     * @return
     * @create 2024-01-03 17:37
     */
    Boolean updateWarehouseByShopId(String id, String shopId);

    /**
     * 撤销流程
     * @description
     * @param id
     * @author Lambda
     * @return
     * @create 2024-01-09 12:06
     */
    BatchResultDTO cancelProcess(String id);

    /**
     * 反审核
     * @description
     * @param id
     * @author Lambda
     * @return
     * @create 2024-01-09 14:17
     */
    BatchResultDTO disApprove(String id);

    /**
     * 拦截打标识，冻结订单
     * @Author Luo_WG
     * @Date 2024/1/17 18:54
     * @param interceptUpdateOrderDTO
     * @return java.lang.Boolean
     **/
    Boolean updateIntercept(SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO);

    /**
     * 修改异常原因
     * @Author Luo_WG
     * @Date 2024/1/19 10:47
     * @param id
     * @param soB2cAbnormalType
     * @return java.lang.Boolean
     **/
    Boolean updateAbnormalType(String id, String soB2cAbnormalType);

    /**
     * 更新组包状态
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2024-01-19 10:58
     */
    Boolean updatePackageStatus(UpdateStateDTO.UpdateByStrStatusDTO dto);

    /**
     * 更新中转状态
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2024-01-19 11:07
     */
    Boolean updateTransferStatus(UpdateStateDTO.UpdateByStrStatusDTO dto);


    /**
     * 预报状态统计
     * @param dto
     * @return
     */
    List<SoB2cDTO.ForecastCountDTO> forecastCount(PermissionsDTO dto);

    /**
     * 中转报关
     * @description
     * @param ids
     * @author Lambda
     * @return
     * @create 2024-01-20 15:47
     */
    List<BatchResultDTO> transferDeclare(List<String> ids);

    /**
     * 根据物流商查询待中转的订单
     * @Author Luo_WG
     * @Date 2024/1/25 18:53
     * @param deliveryLogisticsSupplierId 物流服务商id
     * @return
     **/
    List<TransferDeclareDetailDTO.AddDTO> listByLogisticsSupplier(String deliveryLogisticsSupplierId);

    /**
     * 根据报关设置生成报关单信息
     * @Author Luo_WG
     * @Date 2024/1/25 17:29
     * @param viewDTOList
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.AddDTO>
     **/
    List<TransferDeclareDTO.AddDTO> generateTransferDeclareView(List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList);

    /**
     * 更改订单的中转状态
     * @Author Luo_WG
     * @Date 2024/1/25 19:39
     * @param soIds 订单id
     * @param status 中转状态
     * @return java.lang.Boolean
     **/
    Boolean updateTransferStatusBatch(List<String> soIds, String status);

    /**
     * 组包分页
     * @description
     * @param dto
     * @return
     * @date 2024-01-26 18:40
     * @author Lambda
     */
    PagingVO<PackageDTO.PagingViewDTO> packagePing(PagingDTO<PackageDTO.PagingParamDTO> dto);

    /**
     * 校验尺寸
     * @param id
     * @param dto
     * @return
     */
    BatchResultDTO checkLength(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto);


    /**
     * 检查产品是否备案
     * @description
     * @param id 销售订单id
     * @return
     * @date 2024-01-27 18:14
     * @author Lambda
     */
    void checkProductRegistrationAndUpdate(String id, String logisticsChannelId);


    /**
     * 检查产品是否备案
     * @description
     * @param id 销售订单id
     * @return
     * @date 2024-01-27 18:14
     * @author Lambda
     */
    SettingForecastDTO.CheckRegistrationResultDTO getCheckRegistrationResult(String id, String logisticsChannelId);



    /**
     * 修改订单的第三方物流单号
     * @Author Luo_WG
     * @Date 2024/1/29 17:04
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean updateShippingOrderNo(List<TransferDeclareDTO.ShippingOrderDTO> list);

    boolean isFullyManagedOrder(String platform);

    /**
     * 根据订单拆分sku
     */
    List<SplitSkuDTO> getTransferDeclareProductBySoInfo(String soId);

    /**
     * 根据销售订单id批量拆分
     * @param soIds
     * @return
     */
    List<SplitSkuDTO> getTransferDeclareProductBySoIds(List<String> soIds);

    /**
     * 修改速卖通订单仓库
     * @Author Luo_WG
     * @Date 2024/2/1 11:09
     * @param soId
     * @return void
     **/
    Boolean updateAliExpressOrderWarehouse(String soId, String shopId);

    /**
     * 批量删除订单异常标识
     * @param mainIds
     * @param type
     */
    void batchRemoveSignError(List<String> mainIds, String type);

    /**
     * 单个更新订单预报号
     * @param shippingOrderDTO
     * @return
     */
    Boolean updateShippingOrderNoBySoId(TransferDeclareDTO.ShippingOrderDTO shippingOrderDTO);
    /**
     * @description: 库存缺货校验
     * @author Will
     * @date: 2024/3/12 17:52
     * @param dto
     * @param detailList
     * @return String
     */
    String checkSkuInventory(SoB2cDTO.AddDTO dto, List<SoB2cDetailDTO.AddDTO> detailList);

    /**
     * 根据平台单号和平台查询B2C销售订单
     *
     * @date 2024-03-07
     * @author Jim
     */
    List<SoB2cEntity> getByPlatformCodeList(List<String> platformCodeList, String dictPlatform, String shopId, String sourceType);

    /**
     * 根据销售出库单信息检查和补充
     *
     * @date 2024-03-07
     * @author Jim
     */
    Boolean checkAndFillBySoOutStock(PlatformSoOutStockDTO dto);


    /**
     * 批量更新B2C订单映射关系
     *
     * @date 2024-03-19
     * @author Jim
     */
    BatchResultDTO skuMappingBatch(String soId);


    /**
     * 根据sku拆分订单
     *
     * @param soB2cDetailEntities
     * @param skuIds
     * @param soCode
     * @param judgeCombinationFlag
     * @return
     */
    List<SplitSkuDTO> splitBySoDetail(List<SoB2cDetailEntity> soB2cDetailEntities, List<String> skuIds, String soCode, boolean judgeCombinationFlag);

    /**
     * 根据条件获取数据对比系统数据
     * @param params
     * @return
     */
    List<WmsDataCompareTaskDTO.SoB2cDTO> getDataCompareByCondition(WmsDataCompareTaskDTO.SoOutstockDTO params);

    List<SoB2cEntity> listWithIsIntercept();
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/4/16 15:07
     * @param dto
     * @return Boolean
     */
    Boolean exportExcel(SoB2cDTO.ExportParamDTO dto);

    List<BatchResultDTO> orderForecast(SoB2cDTO.TransferDeclareDTO dto);

    List<BatchResultDTO> autoOrderForecast(List<String> soIdList);

    List<BatchResultDTO> cancelOrderForecast(List<String> ids, Boolean checkPackageStatus);

    List<BatchResultDTO> retryOrderForecast(List<String> ids);
    /**
     * @description: 异常订单分页查询
     * @author Will
     * @date: 2024/4/22 17:53
     * @param pagingParamDTO
     * @return PagingVO<ListDTO>
     */
    PagingVO<SoB2cAbnormalDTO.ListDTO> abnormalPaging(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> pagingParamDTO);
    /**
     * @description: 异常订单导出
     * @author Will
     * @date: 2024/4/22 19:54
     * @param dto
     * @return Boolean
     */
    Boolean abnormalExportExcel(SoB2cAbnormalDTO.PagingParamDTO dto);
    /**
     * 扫描单号匹配订单
     * @param code
     * @return
     */
    PackageDTO.ScanResultDTO packageScanByCode(String code);

    /**
     * 根据销售订单ids 获取到合并的数据
     * @param ids
     * @return
     */
    List<PackageDTO.ScanResultDTO> listMergePackageBySoIds(List<String> ids);

    Boolean autoCancelOrderForecast(SoB2cEntity mainEntity);

    List<BatchResultDTO> deliveryWithNotOutbound(List<SoB2cDTO.DeliveryWithNotOutboundDTO> ids);

    /**
     * 申报信息规则信息整理
     *
     * @param id
     * @param map
     * @param isUpdatePackingWeight
     * @return
     */
    BatchResultDTO declareRule(String id, HashMap<String, Object> map, Boolean isUpdate, Boolean isUpdatePackingWeight);

    /**
     * 根据订单拆分 申报明细
     *
     * @param detailList
     * @param soB2cEntity
     * @param logisticsPlatform
     * @return
     */
    List<LogisticsDeclareProductDTO> splitLogisticsBySoDetail(List<SoB2cDetailEntity> detailList, SoB2cEntity soB2cEntity, String logisticsPlatform);

    /**
     * 修复历史平均成本数据数据
     * @param dto
     */
    void initCostPrice(SoB2cDTO.CostPriceDTO dto);


    /**
     * 根据单号查询销售订单
     */
    SoB2cEntity getByCode(String soCode);

    /**
     * 同步订单到DMP
     */
    void syncOrderToDmp(String id, String syncOperate);
    /**
     * 获取目的国申报信息
     * @param country
     * @param skuId
     * @param productCustomsList
     * @return
     */
    ProductCustomsEntity getCustomsByCountry(String country, String skuId, List<ProductCustomsEntity> productCustomsList);
    SoOutstockDTO.GenerateB2cDTO getSoOutstockByIdAndWarehouseId(String id,String warehouseId);

    /**
     * 校验是否缺货状态
     *
     * @param inventoryList
     * @param waitDeliveryQtyList
     * @param ignoreInventorySkuIds
     * @param skuId
     * @param warehouseId
     * @param qty
     * @param skuMappingDTOList
     * @param warehouseList
     * @param bomChildrenSkuDTOList
     * @return
     */
    Boolean isChildOutStock(List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList,
                            List<SoB2cDetailDTO.WaitDeliveryQtyDTO> waitDeliveryQtyList, List<String> ignoreInventorySkuIds,
                            SoB2cDetailEntity soDetailEntity, String warehouseId, Integer qty, List<ListingInfoWithSkuMappingDTO> skuMappingDTOList, List<WarehouseDTO.UpdateDTO> warehouseList, List<BomChildrenSkuDTO> bomChildrenSkuDTOList);


    void updatePackageAndTransferStatus(String soId, String packageStatus, String transferStatus, Boolean isRegistration,Boolean isUpdateTransferStatus);



    void orderForecastUpdateSoAndError(List<SoB2cEntity> updateB2cList, List<String> deleteErrorIds, List<SoB2cErrorEntity> addOrUpdateErrors, List<SoB2cLogisticsEntity> updateLogisticList);
    /**
     * 同步处理历史审核订单数据到订单表
     */
    void processOrderApproveData();
    /**
     * 查询b2c销售订单数据
     * @author will
     * @date 2024/6/26 8:59
     * @param paramDTO
     * @return SoB2cDataDTO
     */
    SoB2cDTO.SoB2cDataDTO listSoB2cData(SoB2cDTO.SoB2cDataParamDTO paramDTO);

    /**
     * 添加赠品
     * @param entity
     * @param detailEntityList
     * @param dtoList
     * @param LogisticsEntity
     * @return
     */
    BatchResultDTO addGift(SoB2cEntity entity,List<SoB2cDTO.GiftDTO> dtoList,SoB2cLogisticsEntity LogisticsEntity,List<SoB2cDetailEntity> detailEntityList);

    /**
     * 根据销售订单id获取买家信息
     * @param ids
     * @return
     */
    List<SoB2cReceiverDTO.ViewDTO> getReceiverInfo(List<String> ids);

    /**
     * 更新买家信息
     * @param dto
     * @return
     */
    BatchResultDTO updateReceiverInfo(SoB2cReceiverDTO.UpdateBaseDTO dto);

    /**
     * 根据订单创建时间查询订单
     */
    List<SoB2cEntity> listByCreateTime(LocalDateTime startTime, LocalDateTime endTime);

    Boolean updateStatus(SoB2cEntity soB2cEntity);
    /**
     * 导出异常订单
     */
    PagingVO<SoB2cAbnormalDTO.ListDTO> exportSoB2CAbnormal(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto);
    /**
     * 导出订单
     */
    PagingVO<SoB2cDTO.ExcelExportDTO> exportSoB2C(PagingDTO<SoB2cDTO.ExportParamDTO> dto);

    /**
     * 查询所有虚拟仓B2C销售订单数据
     * @author will
     * @date 2024/9/26 17:11
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoB2cDetail();

    List<SoB2cDTO.GenerateSoB2cReturnViewDTO> generateSoB2cReturnView(List<String> ids);

    Boolean generateSoB2cReturn(List<SoB2cDTO.GenerateSoB2cReturnViewDTO> list);

    List<SoB2cEntity> getByPlatformCode(String platformCode);

    /**
     * 更换发货sku预览
     * @param ids
     * @return
     */
    List<SoB2cDTO.ChangeDeliverySkuViewDTO> changeDeliverySkuView(List<String> ids);

    /**
     * 更新是否更换sku状态
     *
     * @param ids
     * @param isChangeSku
     */
    void updateIsChangeSku(List<String> ids, Boolean isChangeSku);
    /**
     * 拉取订单 -- dmp创建任务拉取
     * @author jack
     * @param ids
     */
    List<BatchResultDTO> fetchOrder(List<String> ids);

    Boolean tempTikTokOrderDate();

    /**
     * 根据销售订单id获取订单 渠道+仓库+重量 基础信息
     * @param ids
     * @return
     */
    List<SoB2cDTO.LogisticsDTO> getB2cLogisticsByIds(List<String> ids);

    /**
     * 重算订单预估费用
     * @param sob2cIds
     */
    void autoCalcEstimatedShippingCost(List<String> sob2cIds);

    /**
     * 更新 超过订单金额比例标识
     * @param b2cSoId
     * @param isOverEstimatedShipCost
     */
    void updateOverEstimatedShipCost(String b2cSoId, Boolean isOverEstimatedShipCost);

    List<SoB2cEntity> queryToSdy(LocalDate startDate, LocalDate endDate, Integer pageSize, int offset, List<String> platformList);
    /**
     * 同步销售出库单的单据日期
     * @param soId
     * @param soOutstockDate
     */
    void writeBackSoOutstockDate(String soId, String soOutstockDate);

    String uploadLogisticLabel(SoB2cDTO.UploadFileDTO dto) throws IOException;

    /**
     * 获取物流面单
     *
     * @param entity
     * @param soB2cLogisticsEntity
     * @return
     */
    BatchResultDTO getLogisticsLabel(SoB2cEntity entity, SoB2cLogisticsEntity soB2cLogisticsEntity);

    /**
     * 销售统计
     */
    IPage<?> productSalesPaging(Page<T> query, ReportDTO.ProductSalesPagingParamDTO params, List<String> skuIdList);

    /**
     * 销售统计导出查询
     */
    Page<ReportDTO.ProductSalesPagingViewDTO> listProductSalesExport(Page<ReportDTO.ProductSalesPagingViewDTO> query, ReportDTO.ProductSalesPagingParamDTO params, List<String> skuIdList);

    /**
     * 根据店铺更新未配置vat的订单
     *
     * @param shopId
     * @param enableTime
     * @param vatInvoiceStatus
     * @return
     */
    void updateFbaNotVatInvoice(String shopId, LocalDateTime enableTime, String vatInvoiceStatus);

    void importB2cFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 根据nfe发票状态
     * @author will
     * @date 2025/4/11 16:35
     * @param soId
     * @param nfeInvoiceStatus
     * @return void
     */
    void updateNfeInvoiceStatus(String soId, String nfeInvoiceStatus);

    /**
     * 根据销售订单id和平台获取分区id
     * @param soId
     * @param platform
     * @return
     */
    String getPartitionId(String soId, String platform);

    /**
     * 根据条件查询销售订单
     * @param billStatusList
     * @param platformStatusList
     * @param platformList
     * @return
     */
    List<SoB2cDTO.DeliveryDTO> listDeliveryOrderByParam(List<String> billStatusList, List<String> platformStatusList, List<String> platformList, List<String> codeList);

    /**
     * 根据主表更新扩展字段
     * @param id
     * @param extendDataDTO
     */
    void updateExtendData(String id, SoB2cDTO.ExtendDataDTO extendDataDTO);

    /**
     * 根据物流id标识是否匹配渠道规则
     * @param dto
     */
    void uploadLogisticsStatus(SoB2cDTO.UpdateDTO dto);

    List<SoB2cEntity> listWaitShipByWarehouseIds(List<String> warehouseId);

    /**
     * 根据订单更新金额
     * @param id
     * @param amount
     */
    void updateAmount(String id, BigDecimal amount);

    void clearOutDateBySoIds(List<String> clearOutDateSoIds);

    /**
     * 更新销售订单渠道信息
     * @param soMultiChannelEntity
     */
    void updateSoB2cDistribution(SoMultiChannelEntity soMultiChannelEntity);
}
