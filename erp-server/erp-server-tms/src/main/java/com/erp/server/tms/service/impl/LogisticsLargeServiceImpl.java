package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO.View;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.LogisticsLargeMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_LARGE;
import static com.common.core.controller.vo.ApiResult.success;

/**
 * <p>
 * 物流大表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
 */
@Slf4j
@Service
public class LogisticsLargeServiceImpl extends SuperServiceImpl<LogisticsLargeMapper, LogisticsLargeEntity> implements LogisticsLargeService {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 定义日期时间格式
    DateTimeFormatter formatterM = DateTimeFormatter.ofPattern("yyyy-MM");
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private LogisticsChannelAddressService logisticsChannelAddressService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;

    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    @Resource
    private ReportPeriodMonthService reportPeriodMonthService;

    @Resource
    private FirstMileEstimatedBillService firstMileEstimatedBillService;

    @Resource
    private LogisticsAddressService logisticsAddressService;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private LogisticsTrackService logisticsTrackService;

    @Override
    public PagingVO<LogisticsLargeDTO.PagingViewDTO> paging(PagingDTO<LogisticsLargeDTO.PagingParamDTO> dto) {
        LogisticsLargeDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<LogisticsLargeDTO.PagingViewDTO> list = pageData.getRecords();
        fillPagingData(list);
        return new PagingVO<>(pageData);
    }

    private void fillPagingData(List<LogisticsLargeDTO.PagingViewDTO> list) {
        List<String> skuIds = list.stream().map(LogisticsLargeDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getId, skuIds).list();

        List<String> logisticsBillIds = list.stream().map(req -> req.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostEntities = logisticsBillCostService.listByLogisticsBillIdList(logisticsBillIds);


        List<String> ids = list.stream()
                .filter(req -> SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode().equals(req.getSourceType()))
                .map(LogisticsLargeDTO.PagingViewDTO::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        List<SmallBagCostAllocationMainEntity> costAllocationMainEntities = new ArrayList<>();
        if (CollUtil.isNotEmpty(ids)) {
            costAllocationMainEntities = smallBagCostAllocationMainService.listByIds(ids);
        }

        Map<String, String> payStatusNameMap = new HashMap<>();
        payStatusNameMap.put("pay_payment", "待付款");
        payStatusNameMap.put("pay_paid", "已付款");
        payStatusNameMap.put("refund_payment", "待退款");
        payStatusNameMap.put("refund_paid", "已退款");
        for (LogisticsLargeDTO.PagingViewDTO pagingViewDTO : list) {
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(req -> req.getId().equals(pagingViewDTO.getSkuId())).findFirst().orElse(null);
            if (productDetailEntity != null) {
                pagingViewDTO.setProductName(productDetailEntity.getName());
                pagingViewDTO.setSkuNo(productDetailEntity.getSkuNo());
            }

            if (SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode().equals(pagingViewDTO.getSourceType())) {
                SmallBagCostAllocationMainEntity entity = costAllocationMainEntities.stream().filter(req -> req.getId().equals(pagingViewDTO.getSourceId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(entity)) {
                    LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntities.stream().filter(req -> req.getId().equals(entity.getCostId())).findFirst().orElse(null);
                    pagingViewDTO.setShippingMethodName(LogisticsLargeShippingMethodEnum.getName(pagingViewDTO.getShippingMethod()));
                    pagingViewDTO.setPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getPayStatus()));
                    pagingViewDTO.setDeductibleTaxPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getDeductibleTaxPayStatus()));
                    pagingViewDTO.setDestDutyPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getDestDutyPayStatus()));
                    pagingViewDTO.setOtherTaxPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getOtherTaxPayStatus()));
                    pagingViewDTO.setDestMiscFeePayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getDestMiscFeePayStatus()));
                }
            } else {
                LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntities.stream().filter(req -> req.getLogisticsBillId().equals(pagingViewDTO.getLogisticsBillId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(logisticsBillCostEntity)) {
                    pagingViewDTO.setShippingMethodName(LogisticsLargeShippingMethodEnum.getName(pagingViewDTO.getShippingMethod()));
                    pagingViewDTO.setPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getPayStatus()));
                    pagingViewDTO.setDeductibleTaxPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getDeductibleTaxPayStatus()));
                    pagingViewDTO.setDestDutyPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getDestDutyPayStatus()));
                    pagingViewDTO.setOtherTaxPayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getOtherTaxPayStatus()));
                    pagingViewDTO.setDestMiscFeePayStatusName(payStatusNameMap.get(logisticsBillCostEntity.getPayType() + "_" + pagingViewDTO.getDestMiscFeePayStatus()));
                }
            }


            if (SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode().equals(pagingViewDTO.getSourceType())) {
                pagingViewDTO.setDeductibleTaxPayStatusName("");
                pagingViewDTO.setDeductibleTaxPayTime(null);
                pagingViewDTO.setDeductibleTaxPayStatus("");
                pagingViewDTO.setDeductibleTaxPayTime(null);
            } else if (SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode().equals(pagingViewDTO.getSourceType())) {
                pagingViewDTO.setDeductibleTaxPayStatusName("");
                pagingViewDTO.setDeductibleTaxPayTime(null);
                pagingViewDTO.setDeductibleTaxPayStatus("");
                pagingViewDTO.setDeductibleTaxPayTime(null);
                pagingViewDTO.setOtherTaxPayStatusName("");
                pagingViewDTO.setOtherTaxPayTime(null);
                pagingViewDTO.setOtherTaxPayStatus("");
                pagingViewDTO.setOtherTaxPayTime(null);
            } else {
                pagingViewDTO.setOtherTaxPayStatusName("");
                pagingViewDTO.setOtherTaxPayTime(null);
                pagingViewDTO.setOtherTaxPayStatus("");
                pagingViewDTO.setOtherTaxPayTime(null);
            }
        }
    }

    @Override
    public List<LogisticsLargeDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsLargeDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<LogisticsLargeDTO.TabListDTO> tabListDTOList = baseMapper.listTabCount(dto.getPermissionSql());
        int allCount = tabListDTOList.stream().mapToInt(LogisticsLargeDTO.TabListDTO::getCount).sum();
        LogisticsLargeDTO.TabListDTO all = new LogisticsLargeDTO.TabListDTO();
        all.setCount(allCount);
        all.setTabFlag(SearchType.ALL);
        resultList.add(all);
        return resultList;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsLargeDTO.AddDTO addDTO) {
        LogisticsLargeEntity logisticsLargeEntity = new LogisticsLargeEntity();
        BeanMapperUtils.copy(addDTO, logisticsLargeEntity);

        // 数据处理
        handleData(logisticsLargeEntity);

        log.info("开始新增物流大单");
        boolean save = super.save(logisticsLargeEntity);
        if (!save) {
            throw new ServiceException("物流大单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流大单", logisticsLargeEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, logisticsLargeEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(logisticsLargeEntity.getId(), logisticsLargeEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsLargeDTO.UpdateDTO updateDTO) {
        LogisticsLargeEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流大单"));
        LogisticsLargeEntity logisticsLargeEntity = BeanMapperUtils.map(LogisticsLargeEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsLargeEntity);
        log.info("编辑 开始修改物流大单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsLargeEntity);
        if (!save) {
            throw new ServiceException("物流大单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录物流大单日志数据，id：【{}】", logisticsLargeEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsLargeEntity.getId(), "物流大单");
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO deleteBySourceId(String sourceId) {
        List<LogisticsLargeEntity> list = this.lambdaQuery().eq(LogisticsLargeEntity::getSourceId, sourceId).list();
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("单据未存在!");
        }

        this.lambdaUpdate().eq(LogisticsLargeEntity::getSourceId, sourceId).remove();

        if (SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode().equals(list.get(0).getSourceType())) {
            transferDeclareCostAllocationMainService.updateBigTableStatus(list.get(0).getSourceId(), TransferDeclareCostAllocationMainBigTableStatusEnum.TODO.getCode());
        } else if (SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode().equals(list.get(0).getSourceType())) {
            smallBagCostAllocationMainService.updateBigTableStatus(list.get(0).getSourceId(), SmallBagCostAllocationMainBigTableStatusEnum.TODO.getCode());
        }

        return BatchResultDTO.success(list.get(0).getId(), list.get(0).getOutstockCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsLargeEntity logisticsLargeEntity) {
        // 验证数据 & 数据赋值
    }


    private void firstMileLogisticsTableHandler(FirstMileCostAllocationEntity entity, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, List<FirstMileSkuCostAllocationDetailEntity> skuCostDetailEntityList, FirstMileDeliveryEntity deliveryEntity, List<FirstMileDeliveryDetailEntity> deliveryDetailEntities) {


        //查询物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(entity.getLogisticsBillId());
        if (ObjectUtil.isEmpty(logisticsBillEntity)) {
            throw new ServiceException("物流单信息未找到");
        }

        //自发货费用
        List<LogisticsBillCostEntity> logisticsBillCostEntities = logisticsBillCostService.listByLogisticsBillIdList(Arrays.asList(logisticsBillEntity.getId()));
        if (CollUtil.isEmpty(logisticsBillCostEntities)) {
            throw new ServiceException("自发货费用未找到");
        }
        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntities.get(0);

        //查询物流详情
        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillEntity.getId()));

        //查询仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(deliveryEntity.getDeliveryWarehouseId(), deliveryEntity.getDestWarehouseId()));
        ReportPeriodMonthEntity monthEntity = reportPeriodMonthService.getById(entity.getReportPeriodId());

        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode());
        addDTO.setSourceDetailId(firstMileSkuCostAllocationEntity.getSourceDetailId());
        addDTO.setReconciliationMonth(monthEntity.getMonth().format(formatterM));
        addDTO.setOutstockCode(deliveryEntity.getCode());
        addDTO.setOutstockTime(deliveryEntity.getApproveTime());
        addDTO.setLogisticsBillId(logisticsBillEntity.getId());
        addDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
        addDTO.setTransportNo(logisticsBillEntity.getCounterNo());
        addDTO.setLogisticsBillCostId(logisticsBillCostEntity.getId());
        List<LogisticsTrackEntity> logisticsTrackEntities = logisticsTrackService.listByTrackNoList(Arrays.asList(logisticsBillEntity.getCounterNo()));
        LogisticsTrackEntity logisticsTrackEntity = logisticsTrackEntities.stream().filter(req -> FmLogisticTrackStatusEnum.PICKUP.getCode().equals(req.getStatus())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(logisticsTrackEntity)) {
            addDTO.setPickupTime(logisticsTrackEntity.getTrackTime());
        }

        //付款方式
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(logisticsBillEntity.getChannelId());
        if (ObjectUtil.isNotEmpty(channelEntity)) {
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(channelEntity.getMainId());
            if (ObjectUtil.isNotEmpty(logisticsSupplierEntity)) {

                addDTO.setLogisticsSupplierId(logisticsSupplierEntity.getId());
                addDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());

                SupplierEntity supplierEntity = supplierFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());
                if (ObjectUtil.isNotEmpty(supplierEntity)) {
                    List<BaseDropDownDTO.DisabledDTO> disabledDTOS = scmTaskFeign.listPaymentCondition();
                    String paymentCondition = disabledDTOS.stream().
                            filter(req -> supplierEntity.getPaymentCondition() != null
                                    && supplierEntity.getPaymentCondition().equals(req.getCode()))
                            .map(BaseDropDownDTO.DisabledDTO::getValue)
                            .findFirst().orElse("");
                    addDTO.setPayTermsDays(paymentCondition);
                    addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
                    addDTO.setTaxRate(supplierEntity.getTaxRate());

                }
            }
        }
        addDTO.setSkuId(firstMileSkuCostAllocationEntity.getSkuId());
        addDTO.setSkuNo(firstMileSkuCostAllocationEntity.getSkuNo());
        addDTO.setDeliveryQty(firstMileSkuCostAllocationEntity.getDeliveryQty());

        //起运地
        addDTO.setOriginPort("");

        //目的港
        addDTO.setDestinationPort("");
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> deliveryEntity.getDeliveryWarehouseId().equals(req.getId())).findFirst().orElse(null);
        if (deliveryWarehouse != null) {
            addDTO.setPickupAddress(deliveryWarehouse.getAddress());
        }
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream().filter(req -> deliveryEntity.getDestWarehouseId().equals(req.getId())).findFirst().orElse(null);
        if (destWarehouse != null) {
            addDTO.setDeliveryAddress(destWarehouse.getAddress());
        }

        if (CollUtil.isNotEmpty(detailEntityList)) {
            addDTO.setActualDeliveryTime(detailEntityList.get(0).getSignTime());
        }

        //税率
        BigDecimal taxRate = addDTO.getTaxRate().divide(MathUtil.BigDecimal_100);
        //汇率
        BigDecimal rate = dmpTaskFeign.getRate(monthEntity.getMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), logisticsBillCostEntity.getCurrency());

        //金额
        //总金额
        BigDecimal billTotalAmount = skuCostDetailEntityList.stream().map(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        addDTO.setBillTotalAmount(billTotalAmount.divide(rate, 4, RoundingMode.DOWN));

        List<FirstMileSkuCostAllocationDetailEntity> costAllocationDetailEntities = skuCostDetailEntityList.stream()
                .filter(req -> req.getCostMainId().equals(firstMileSkuCostAllocationEntity.getId()))
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(detailEntityList)) {
            throw new ServiceException("头程费用SKU分摊明细记录不存在");
        }

        TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity = null;
        addDTO.setFreightCurrency("CNY");
        addDTO.setMiscFeeCurrency("CNY");
        addDTO.setDutyCurrency("CNY");
        addDTO.setOtherTaxCurrency("CNY");
        if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(firstMileSkuCostAllocationEntity.getBillSourceType())) {
        	reconciliationDetailEntity = tmsFirstMileReconciliationDetailService.getById(firstMileSkuCostAllocationEntity.getReconciliationDetailId());
        	if(reconciliationDetailEntity != null) {
            	addDTO.setFreightCurrency(reconciliationDetailEntity.getShippingCostCurrency());
                addDTO.setMiscFeeCurrency(reconciliationDetailEntity.getOtherCostCurrency());
                addDTO.setDutyCurrency(reconciliationDetailEntity.getDeclareCostCurrency());
                addDTO.setOtherTaxCurrency(reconciliationDetailEntity.getOtherTaxCurrency());
            }
        }else {
        	List<FirstMileEstimatedBillDTO.View> estimatedBillEntityList = firstMileEstimatedBillService.listByLogisticsBillIds(Collections.singletonList(entity.getLogisticsBillId()), ConfirmStatusEnum.CONFIRM.getCode());
        	if(CollUtil.isNotEmpty(estimatedBillEntityList)) {
        		View estimatedBillEntity = estimatedBillEntityList.get(0);
        		addDTO.setFreightCurrency(estimatedBillEntity.getLogisticsCostCurrency());
                addDTO.setMiscFeeCurrency(estimatedBillEntity.getOtherCostCurrency());
                addDTO.setDutyCurrency(estimatedBillEntity.getCustomsClearanceCostCurrency());
                addDTO.setOtherTaxCurrency(estimatedBillEntity.getOtherTaxCostCurrency());
        	}
        }
        
        //头程运费
        rate = dmpTaskFeign.getRate(monthEntity.getMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), addDTO.getFreightCurrency());
        FirstMileSkuCostAllocationDetailEntity detailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (detailEntity != null && detailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setFreightCalculationFactor(detailEntity.getAllocatedAmount().divide(detailEntity.getAmount(), 4, RoundingMode.DOWN));
        }
        if (ObjectUtil.isNotEmpty(detailEntity)) {
            BigDecimal firstMileFreightAmount = detailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN);
            addDTO.setFirstMileEstimatedFreightTax(firstMileFreightAmount);
            addDTO.setFirstMileEstimatedFreight(firstMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN));

            addDTO.setFirstMileActualFreightTax(firstMileFreightAmount);
            addDTO.setFirstMileActualFreight(firstMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN));
            addDTO.setFirstMileFreightVatAmount(firstMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN).multiply(taxRate));
        }

        //杂费
        rate = dmpTaskFeign.getRate(monthEntity.getMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), addDTO.getMiscFeeCurrency());
        FirstMileSkuCostAllocationDetailEntity otherCostDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.OTHER_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (otherCostDetailEntity != null && otherCostDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDestMiscFeeFactor(otherCostDetailEntity.getAllocatedAmount().divide(otherCostDetailEntity.getAmount(), 4, RoundingMode.DOWN));
        }
        
        if (ObjectUtil.isNotEmpty(otherCostDetailEntity)) {
            addDTO.setEstimatedDestMiscFee(otherCostDetailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN));
            addDTO.setActualDestMiscFee(otherCostDetailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN));
        }

        //关税
        rate = dmpTaskFeign.getRate(monthEntity.getMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), addDTO.getDutyCurrency());
        FirstMileSkuCostAllocationDetailEntity declareCostDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (declareCostDetailEntity != null && declareCostDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDutyCalculationFactor(declareCostDetailEntity.getAllocatedAmount().divide(declareCostDetailEntity.getAmount(), 4, RoundingMode.DOWN));
        }
        if (ObjectUtil.isNotEmpty(declareCostDetailEntity)) {
            addDTO.setEstimatedDutyAmount(declareCostDetailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN));
            addDTO.setActualDutyAmount(declareCostDetailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN));
        }

        //其他税金
        rate = dmpTaskFeign.getRate(monthEntity.getMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), addDTO.getOtherTaxCurrency());
        FirstMileSkuCostAllocationDetailEntity otherTaxFeeDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (otherTaxFeeDetailEntity != null && otherTaxFeeDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setOtherTaxCalculationFactor(otherTaxFeeDetailEntity.getAllocatedAmount().divide(otherTaxFeeDetailEntity.getAmount(), 4, RoundingMode.DOWN));
        }

        if (ObjectUtil.isNotEmpty(otherTaxFeeDetailEntity)) {
            addDTO.setEstimatedTaxOtherTax(otherTaxFeeDetailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN));
            addDTO.setActualTaxOtherTax(otherTaxFeeDetailEntity.getAllocatedAmount().divide(rate, 4, RoundingMode.DOWN));
        }

        if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(firstMileSkuCostAllocationEntity.getBillSourceType())) {
        	//头程对账单主信息
            TmsFirstMileReconciliationEntity reconciliationEntity = tmsFirstMileReconciliationService.getById(entity.getReconciliationId());
            if (ObjectUtil.isEmpty(reconciliationEntity)) {
                throw new ServiceException("头程对账单主信息未找到");
            }
            //实际账单
            addDTO.setReconciliationBillType(ReconciliationBillTypeEnum.ACTUAL.getCode());

            //重量
            if (ObjectUtil.isNotEmpty(reconciliationDetailEntity) &&  reconciliationDetailEntity.getBillingWeight().compareTo(BigDecimal.ZERO) > 0) {
                addDTO.setWeight(reconciliationDetailEntity.getBillingWeight());
                addDTO.setLogisticsBillingWeight(reconciliationDetailEntity.getBillingWeight());
            } else {
                List<ProductPackEntity> packEntityList = FeignQuery.create(ProductPackEntity.class).eq(ProductPackEntity::getSkuId, firstMileSkuCostAllocationEntity.getSkuId()).list();
                if (CollUtil.isNotEmpty(packEntityList)) {
                    BigDecimal grossWeight = MathUtil.divide(MathUtil.multiplyWithTwo(packEntityList.get(0).getGrossWeight(), firstMileSkuCostAllocationEntity.getDeliveryQty()), MathUtil.BigDecimal_1000);
                    addDTO.setWeight(grossWeight);
                    addDTO.setLogisticsBillingWeight(grossWeight);
                }
            }
            
            //付款状态
            addDTO.setPayStatus(reconciliationEntity.getPayStatus());
            addDTO.setDeductibleTaxPayStatus(reconciliationEntity.getPayStatus());
            addDTO.setDestDutyPayStatus(reconciliationEntity.getPayStatus());
            addDTO.setOtherTaxPayStatus(reconciliationEntity.getPayStatus());
            addDTO.setDestMiscFeePayStatus(reconciliationEntity.getPayStatus());

            addDTO.setDestMiscFeePayTime(reconciliationEntity.getPayTime());
            addDTO.setDestTaxPayTime(reconciliationEntity.getPayTime());
            addDTO.setOtherTaxPayTime(reconciliationEntity.getPayTime());
            //查询是否有预估账单
            String outstockCode = deliveryEntity.getCode();
            String skuId = firstMileSkuCostAllocationEntity.getSkuId();

            List<LogisticsLargeEntity> list = this.lambdaQuery()
                    .eq(LogisticsLargeEntity::getOutstockCode, outstockCode)
                    .eq(LogisticsLargeEntity::getSkuId, skuId)
                    .eq(LogisticsLargeEntity::getSourceType, SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())
                    .orderByDesc(LogisticsLargeEntity::getReconciliationMonth)
                    .list();
            List<LogisticsLargeEntity> estimatedList = list.stream().filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(estimatedList)) {
                //如果有需要生成负数的对冲预估账单
                hedgingEstimated(estimatedList.get(0), addDTO.getReconciliationMonth());
            }
        } else {
            //预估账单
            addDTO.setReconciliationBillType(ReconciliationBillTypeEnum.ESTIMATED.getCode());

            //毛重
            List<ProductPackEntity> packEntityList = FeignQuery.create(ProductPackEntity.class).eq(ProductPackEntity::getSkuId, firstMileSkuCostAllocationEntity.getSkuId()).list();
            if (CollUtil.isNotEmpty(packEntityList)) {
                BigDecimal grossWeight = MathUtil.divide(MathUtil.multiplyWithTwo(packEntityList.get(0).getGrossWeight(), firstMileSkuCostAllocationEntity.getDeliveryQty()), MathUtil.BigDecimal_1000);
                addDTO.setWeight(grossWeight);
            }

            //付款状态
            addDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDeductibleTaxPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDestDutyPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setOtherTaxPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDestMiscFeePayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());

            //预估账单不需要实际的金额
            addDTO.setFirstMileActualFreightTax(BigDecimal.ZERO);
            addDTO.setFirstMileActualFreight(BigDecimal.ZERO);
            addDTO.setActualDestMiscFee(BigDecimal.ZERO);
            addDTO.setActualDutyAmount(BigDecimal.ZERO);
            addDTO.setActualTaxOtherTax(BigDecimal.ZERO);
        }

        //添加
        this.add(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateFirstMileLogistics(FirstMileCostAllocationEntity entity,
                                                     List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList,
                                                     List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities,
                                                     FirstMileDeliveryEntity firstMileDeliveryEntity,
                                                     List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntities) {


        for (FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity : skuCostAllocationEntityList) {
            List<FirstMileSkuCostAllocationDetailEntity> costAllocationDetailEntities = skuCostAllocationDetailEntities.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());

            this.firstMileLogisticsTableHandler(entity, firstMileSkuCostAllocationEntity, costAllocationDetailEntities, firstMileDeliveryEntity, firstMileDeliveryDetailEntities);
        }
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.ADD);
    }

    @Override
    public List<LogisticsLargeEntity> listByIdSourceId(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsLargeEntity::getSourceId, ids).list();
    }

    @Override
    public List<LogisticsLargeEntity> listByIdOutstockCode(List<String> outstockCode) {
        if (CollUtil.isEmpty(outstockCode)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsLargeEntity::getOutstockCode, outstockCode).list();
    }


    @Override
    public List<LogisticsLargeEntity> listByIdSourceDetailIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsLargeEntity::getSourceDetailId, ids).list();
    }


    /**
     * 小包分摊下推物流大表
     *
     * @param smallBagCostAllocationMainEntity 小包分摊
     * @param costAllocationEntity             小包分摊明细
     * @param costAllocationDetailEntities     小包分摊明细费用
     * @param soOutstockEntity                 销售出库主表
     * @param soOutstockDetailEntity           销售出库明细信息
     * @return com.common.business.dto.base.BatchResultDTO
     * @Author Luo_WG
     * @Date 2024/12/3 15:37
     **/
    private void smallBagCostAllocationHandler(SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity,
                                               SmallBagCostAllocationEntity costAllocationEntity,
                                               List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntities,
                                               SoOutstockEntity soOutstockEntity,
                                               SoOutstockDetailEntity soOutstockDetailEntity,
                                               SoB2cEntity soB2cEntity) {

        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();
        addDTO.setSourceId(smallBagCostAllocationMainEntity.getId());
        addDTO.setSourceType(SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode());
        addDTO.setSourceDetailId(costAllocationEntity.getId());


        if (CharSequenceUtil.isNotBlank(smallBagCostAllocationMainEntity.getReportDate())) {

            addDTO.setReconciliationMonth(smallBagCostAllocationMainEntity.getReportDate());
        }

        addDTO.setOutstockCode(soOutstockEntity.getCode());
        addDTO.setOutstockTime(soOutstockEntity.getBillDate().atStartOfDay());

        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostService.getById(smallBagCostAllocationMainEntity.getCostId());
        if (ObjectUtil.isEmpty(logisticsBillCostEntity)) {
            throw new ServiceException("物流单费用信息未找到");
        }
        addDTO.setLogisticsBillCostId(logisticsBillCostEntity.getId());
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillCostEntity.getLogisticsBillId());
        if (ObjectUtil.isEmpty(logisticsBillCostEntity)) {
            throw new ServiceException("物流单信息未找到");
        }
        List<LogisticsBillDetailEntity> billDetailEntities = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillCostEntity.getLogisticsBillId()));
        if (ObjectUtil.isEmpty(billDetailEntities)) {
            throw new ServiceException("物流单详情信息未找到");
        }

        if (ShipmentTypeEnum.PLATFORM_DELIVER.getCode().equals(logisticsBillEntity.getShipmentType())) {
            throw new ServiceException("平台仓发货不需要推送物流大表");
        }
        addDTO.setLogisticsBillId(logisticsBillEntity.getId());

        //付款方式
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(soOutstockEntity.getLogisticsChannelId());
        if (ObjectUtil.isNotEmpty(channelEntity)) {
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(channelEntity.getMainId());
            if (ObjectUtil.isNotEmpty(logisticsSupplierEntity)) {
                SupplierEntity supplierEntity = supplierFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());
                if (ObjectUtil.isNotEmpty(supplierEntity)) {
                    addDTO.setLogisticsSupplierId(supplierEntity.getId());
                    addDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
                    List<BaseDropDownDTO.DisabledDTO> disabledDTOS = scmTaskFeign.listPaymentCondition();
                    String paymentCondition = disabledDTOS.stream().
                            filter(req -> supplierEntity.getPaymentCondition() != null
                                    && supplierEntity.getPaymentCondition().equals(req.getCode()))
                            .map(BaseDropDownDTO.DisabledDTO::getValue)
                            .findFirst().orElse("");
                    addDTO.setPayTermsDays(paymentCondition);
                    addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
                    addDTO.setTaxRate(supplierEntity.getTaxRate());

                }
            }
        }
        if (OrderTypeEnum.B2B.getCode().equals(soOutstockEntity.getOrderType())) {
            SupplierEntity supplierEntity = supplierFeign.getSupplierById(soOutstockEntity.getCarrierId());
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                addDTO.setLogisticsSupplierId(supplierEntity.getId());
                addDTO.setLogisticsSupplierName(supplierEntity.getName());
                List<BaseDropDownDTO.DisabledDTO> disabledDTOS = scmTaskFeign.listPaymentCondition();
                String paymentCondition = disabledDTOS.stream().
                        filter(req -> supplierEntity.getPaymentCondition() != null
                                && supplierEntity.getPaymentCondition().equals(req.getCode()))
                        .map(BaseDropDownDTO.DisabledDTO::getValue)
                        .findFirst().orElse("");
                addDTO.setPayTermsDays(paymentCondition);
                addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
                addDTO.setTaxRate(supplierEntity.getTaxRate());
            }
        }

        addDTO.setSkuId(soOutstockDetailEntity.getSkuId());
        addDTO.setSkuNo(soOutstockDetailEntity.getSkuNo());
        addDTO.setDeliveryQty(costAllocationEntity.getDeliveryQty());
        //平台订单号
        String platformCode = "";
        if (SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.SO_B2C.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode().equals(soOutstockEntity.getSourceType())
        ) {
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                platformCode = soB2cEntity.getPlatformCode();
            }
        } else {
            platformCode = "";
        }

        addDTO.setPlatformOrderCode(platformCode);
        addDTO.setWeight(logisticsBillCostEntity.getBillingWeightLogistics());
        addDTO.setLogisticsBillingWeight(logisticsBillCostEntity.getBillingWeightLogistics());
        addDTO.setShippingMethod(LogisticsLargeShippingMethodEnum.EXPRESS_DELIVERY.getCode());
        if (CharSequenceUtil.isNotBlank(billDetailEntities.get(0).getTrackNo())) {
            addDTO.setTransportNo(billDetailEntities.get(0).getTrackNo());
        } else {
            addDTO.setTransportNo(logisticsBillEntity.getTransportNo());
        }

        //启运地
        addDTO.setOriginPort("");

        //地址
        if (CharSequenceUtil.isNotBlank(soOutstockEntity.getSoId())) {
            if (OrderTypeEnum.B2C.getCode().equals(soOutstockEntity.getOrderType())) {
                SoB2cDTO.CustomerDTO customerDTO = soB2cFeign.getB2cCustomerById(soOutstockEntity.getSoId());
                addDTO.setDeliveryAddress(customerDTO.getReceiverAddress());
                addDTO.setDestinationPort(customerDTO.getCountryName());

            } else {
                SoInfoDTO.CustomerDTO customerDTO = soInfoFeign.getSoBaseById(soOutstockEntity.getSoId());
                addDTO.setDeliveryAddress(customerDTO.getReceiveAddress());
                if (CharSequenceUtil.isNotBlank(customerDTO.getCountryId())) {
                    DictCountryEntity countryEntity = FeignQuery.getById(DictCountryEntity.class, customerDTO.getCountryId());
                    if (ObjectUtil.isNotEmpty(countryEntity)) {
                        addDTO.setDestinationPort(countryEntity.getNameCn());
                    }
                }
            }
        }

        List<LogisticsChannelAddressDTO.ViewDTO> viewDTOS = logisticsChannelAddressService.listByChannelId(soOutstockEntity.getLogisticsChannelId());
        LogisticsChannelAddressDTO.ViewDTO viewDTO = viewDTOS.stream().filter(req -> LogisticsAddressTypeEnum.DELIVER.getCode().equals(req.getLogisticsAddressType())).findFirst().orElse(null);
        if (viewDTO != null) {
            LogisticsAddressEntity addressEntity = logisticsAddressService.getById(viewDTO.getAddressId());
            if (ObjectUtil.isNotEmpty(addressEntity)) {
                addDTO.setPickupAddress(addressEntity.getAddressFirst());
            }
        }
        addDTO.setPickupTime(soOutstockEntity.getBillDate().atStartOfDay());

        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillEntity.getId()));
        if (CollUtil.isNotEmpty(detailEntityList)) {
            addDTO.setActualDeliveryTime(detailEntityList.get(0).getSignTime());
        }

        //账单总金额
        BigDecimal billAmountTotal = costAllocationDetailEntities.stream().map(SmallBagCostAllocationDetailEntity::getBillAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        addDTO.setBillTotalAmount(billAmountTotal);

        //税率
        BigDecimal taxRate = addDTO.getTaxRate().divide(MathUtil.BigDecimal_100);

        //运费
        SmallBagCostAllocationDetailEntity detailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (detailEntity != null && detailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setFreightCalculationFactor(detailEntity.getAllocatedAmount().divide(detailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        addDTO.setFreightCurrency(detailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(detailEntity)) {
            BigDecimal lastMileFreightAmount = detailEntity.getAllocatedAmount();
            addDTO.setLastMileFreightAmountTax(detailEntity.getAllocatedAmount());
            addDTO.setLastMileFreightAmount(lastMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN));
            addDTO.setLastMileFreightVatAmount(lastMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN).multiply(taxRate));
        }

        //杂费
        SmallBagCostAllocationDetailEntity otherCostDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.OTHER_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (otherCostDetailEntity != null && otherCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDestMiscFeeFactor(otherCostDetailEntity.getAllocatedAmount().divide(otherCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        addDTO.setMiscFeeCurrency(otherCostDetailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(otherCostDetailEntity)) {
            addDTO.setEstimatedDestMiscFee(otherCostDetailEntity.getAllocatedAmount());
            addDTO.setActualDestMiscFee(otherCostDetailEntity.getAllocatedAmount());
        }
        addDTO.setDestMiscFeePayTime(logisticsBillCostEntity.getPayTime());

        //关税
        SmallBagCostAllocationDetailEntity declareCostDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (declareCostDetailEntity.getBillAmount() != null && declareCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDutyCalculationFactor(declareCostDetailEntity.getAllocatedAmount().divide(declareCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        addDTO.setDutyCurrency(declareCostDetailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(declareCostDetailEntity)) {
            addDTO.setEstimatedDutyAmount(declareCostDetailEntity.getAllocatedAmount());
            addDTO.setActualDutyAmount(declareCostDetailEntity.getAllocatedAmount());
        }
        addDTO.setDestTaxPayTime(logisticsBillCostEntity.getPayTime());

        //可抵扣税金
        SmallBagCostAllocationDetailEntity deductibleTaxDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.DEDUCTIBLE_TAX.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (deductibleTaxDetailEntity != null && deductibleTaxDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDeductibleTaxFactor(deductibleTaxDetailEntity.getAllocatedAmount().divide(deductibleTaxDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        addDTO.setDeductibleTaxCurrency(deductibleTaxDetailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(deductibleTaxDetailEntity)) {
            addDTO.setEstimatedDeductibleTax(deductibleTaxDetailEntity.getAllocatedAmount());
            addDTO.setActualDeductibleTax(deductibleTaxDetailEntity.getAllocatedAmount());
        }
        addDTO.setDeductibleTaxPayTime(logisticsBillCostEntity.getPayTime());

        //其他税金
        SmallBagCostAllocationDetailEntity otherTaxFeeDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (otherTaxFeeDetailEntity != null && otherTaxFeeDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setOtherTaxCalculationFactor(otherTaxFeeDetailEntity.getAllocatedAmount().divide(otherTaxFeeDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }

        //如果是退款的金额转换成负数
        if (LogisticsBillCostPayTypeEnum.REFUND.getCode().equals(logisticsBillCostEntity.getPayType())) {
            addDTO.setBillTotalAmount(addDTO.getBillTotalAmount().negate());
            addDTO.setLastMileFreightAmount(addDTO.getLastMileFreightAmount().negate());
            addDTO.setLastMileFreightAmountTax(addDTO.getLastMileFreightAmountTax().negate());
            addDTO.setLastMileFreightVatAmount(addDTO.getLastMileFreightVatAmount().negate());
            addDTO.setEstimatedDestMiscFee(addDTO.getEstimatedDestMiscFee().negate());
            addDTO.setActualDestMiscFee(addDTO.getActualDestMiscFee().negate());
            addDTO.setEstimatedDutyAmount(addDTO.getEstimatedDutyAmount().negate());
            addDTO.setActualDutyAmount(addDTO.getActualDutyAmount().negate());
            addDTO.setEstimatedDeductibleTax(addDTO.getEstimatedDeductibleTax().negate());
            addDTO.setActualDeductibleTax(addDTO.getActualDeductibleTax().negate());
        }


        if (SmallBagCostAllocationMainFeeSourceEnum.CONFIRMED.getCode().equals(smallBagCostAllocationMainEntity.getFeeSource())) {
            //实际账单
            addDTO.setReconciliationBillType(ReconciliationBillTypeEnum.ACTUAL.getCode());
            //付款状态
            addDTO.setPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setDeductibleTaxPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setDestDutyPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setOtherTaxPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setDestMiscFeePayStatus(logisticsBillCostEntity.getPayStatus());

            if (logisticsBillCostEntity.getBillingWeightLogistics() == null || logisticsBillCostEntity.getBillingWeightLogistics().compareTo(BigDecimal.ZERO) <= 0) {
                //重量
                List<ProductPackEntity> packEntityList = FeignQuery.create(ProductPackEntity.class).eq(ProductPackEntity::getSkuId, costAllocationEntity.getSkuId()).list();
                if (CollUtil.isNotEmpty(packEntityList)) {
                    BigDecimal grossWeight = MathUtil.divide(MathUtil.multiplyWithTwo(packEntityList.get(0).getGrossWeight(), costAllocationEntity.getDeliveryQty()), MathUtil.BigDecimal_1000);
                    addDTO.setWeight(grossWeight);
                }
            }
        } else {
            //预估账单
            addDTO.setReconciliationBillType(ReconciliationBillTypeEnum.ESTIMATED.getCode());
            //付款状态
            addDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDeductibleTaxPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDestDutyPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setOtherTaxPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDestMiscFeePayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            //预估账单不需要实际的金额
            addDTO.setActualDestMiscFee(BigDecimal.ZERO);
            addDTO.setActualDutyAmount(BigDecimal.ZERO);
            addDTO.setActualDeductibleTax(BigDecimal.ZERO);

            if (logisticsBillCostEntity.getBillingWeightLogistics() == null || logisticsBillCostEntity.getBillingWeightLogistics().compareTo(BigDecimal.ZERO) <= 0) {
                //重量
                List<ProductPackEntity> packEntityList = FeignQuery.create(ProductPackEntity.class).eq(ProductPackEntity::getSkuId, costAllocationEntity.getSkuId()).list();
                if (CollUtil.isNotEmpty(packEntityList)) {
                    BigDecimal grossWeight = MathUtil.divide(MathUtil.multiplyWithTwo(packEntityList.get(0).getGrossWeight(), costAllocationEntity.getDeliveryQty()), MathUtil.BigDecimal_1000);
                    addDTO.setWeight(grossWeight);
                }
            }
        }

        //添加
        this.add(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateSmallBagCostAllocationTable(SmallBagCostAllocationMainEntity mainEntity, List<SmallBagCostAllocationEntity> costAllocationEntityList, List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntityList, SoOutstockEntity soOutstockEntity, List<SoOutstockDetailEntity> soOutstockDetailEntities, List<SoB2cEntity> soB2cEntities) {


        mainEntity = smallBagCostAllocationMainService.getById(mainEntity.getId());
        //已确认才能下推
        if (!SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(mainEntity.getReportStatus())) {
            throw new ServiceException(ApiError.ERROR_SMALL_BAG_NOT_CONFIRMED);
        }
        //已生成物流大表不能再次生成
        if (SmallBagCostAllocationBigTableStatusEnum.DONE.getCode().equals(mainEntity.getBigTableStatus())) {
            throw new ServiceException(ApiError.ERROR_EXISTS_LOGISTICS_LARGE);
        }

        for (SmallBagCostAllocationEntity costAllocationEntity : costAllocationEntityList) {
            List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntities = costAllocationDetailEntityList.stream()
                    .filter(req -> req.getMainId().equals(costAllocationEntity.getId()))
                    .collect(Collectors.toList());

            SoOutstockDetailEntity soOutstockDetailEntity = soOutstockDetailEntities.stream().filter(req -> req.getId().equals(costAllocationEntity.getOutstockDetailId())).findFirst().orElse(null);
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(soOutstockEntity.getSoId())).findFirst().orElse(null);
            this.smallBagCostAllocationHandler(mainEntity, costAllocationEntity, costAllocationDetailEntities, soOutstockEntity, soOutstockDetailEntity, soB2cEntity);
        }

        if (SmallBagCostAllocationMainFeeSourceEnum.CONFIRMED.getCode().equals(mainEntity.getFeeSource())) {
            //查询是否有预估账单
            List<LogisticsLargeEntity> list = this.lambdaQuery()
                    .eq(LogisticsLargeEntity::getLogisticsBillCostId, mainEntity.getCostId())
                    .eq(LogisticsLargeEntity::getSourceType, SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())
                    .list();

            List<LogisticsLargeEntity> estimatedList = list.stream()
                    .filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType()))
                    .collect(Collectors.toList());
            //如果有需要生成负数的对冲预估账单
            if (CollUtil.isNotEmpty(estimatedList)) {
                for (LogisticsLargeEntity logisticsLargeEntity : estimatedList) {
                    //冲预估账单
                    hedgingEstimated(logisticsLargeEntity, logisticsLargeEntity.getReconciliationMonth());
                }
            }
        }

        //修改小包费用分摊生成状态
        smallBagCostAllocationMainService.updateBigTableStatus(mainEntity.getId(), SmallBagCostAllocationBigTableStatusEnum.DONE.getCode());

        return BatchResultDTO.success(mainEntity.getId(), soOutstockEntity.getCode(), OperationTypeEnum.ADD);
    }

    /**
     * 对冲物流大表预估账单
     */
    private void hedgingEstimated(LogisticsLargeEntity entity, String reconciliationMonth) {
        entity.setId(null);
        entity.setReconciliationMonth(reconciliationMonth);
        entity.setBillTotalAmount(entity.getBillTotalAmount().negate());
        entity.setFirstMileEstimatedFreightTax(entity.getFirstMileEstimatedFreightTax().negate());
        entity.setFirstMileEstimatedFreight(entity.getFirstMileEstimatedFreight().negate());
        entity.setFirstMileActualFreightTax(entity.getFirstMileActualFreightTax().negate());
        entity.setFirstMileActualFreight(entity.getFirstMileActualFreight().negate());
        entity.setFirstMileFreightVatAmount(entity.getFirstMileFreightVatAmount().negate());
        entity.setLastMileFreightAmountTax(entity.getLastMileFreightAmountTax().negate());
        entity.setLastMileFreightAmount(entity.getLastMileFreightAmount().negate());
        entity.setLastMileFreightVatAmount(entity.getLastMileFreightVatAmount().negate());
        entity.setEstimatedDestMiscFee(entity.getEstimatedDestMiscFee().negate());
        entity.setActualDestMiscFee(entity.getActualDestMiscFee().negate());
        entity.setEstimatedDutyAmount(entity.getEstimatedDutyAmount().negate());
        entity.setActualDutyAmount(entity.getActualDutyAmount().negate());
        entity.setEstimatedDeductibleTax(entity.getEstimatedDeductibleTax().negate());
        entity.setActualDeductibleTax(entity.getActualDeductibleTax().negate());
        entity.setEstimatedTaxOtherTax(entity.getEstimatedTaxOtherTax().negate());
        entity.setActualTaxOtherTax(entity.getActualTaxOtherTax().negate());
        entity.setIsHedging(Boolean.TRUE);
        super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateTransferCostAllocationTable(TransferDeclareCostAllocationMainEntity mainEntity,
                                                              List<TransferDeclareCostAllocationEntity> costAllocationEntityList,
                                                              List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntityList,
                                                              TmsB2cDeclareReconciliationEntity reconciliationEntity,
                                                              TmsB2cDeclareReconciliationDetailEntity reconciliationDetailEntity,
                                                              SoOutstockEntity soOutstockEntity) {

        mainEntity = transferDeclareCostAllocationMainService.getById(mainEntity.getId());
        //已确认才能下推
        if (!SmallBagCostAllocationReportStatusEnum.CONFIRMED.getCode().equals(mainEntity.getReportStatus())) {
            throw new ServiceException("中转费用分摊未确认，不能生成物流大表");
        }
        //已生成物流大表不能再次生成
        if (SmallBagCostAllocationBigTableStatusEnum.DONE.getCode().equals(mainEntity.getBigTableStatus())) {
            throw new ServiceException(ApiError.ERROR_EXISTS_LOGISTICS_LARGE);
        }

        List<LogisticsLargeEntity> logisticsLargeEntities = this.listByIdSourceId(Arrays.asList(mainEntity.getId()));

        //只能下推一个实际账单
        LogisticsLargeEntity logisticsLargeActualEntity = logisticsLargeEntities.stream()
                .filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())
                        && soOutstockEntity.getCode().equals(req.getOutstockCode())
                        && SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode().equals(req.getSourceType()))
                .findFirst().orElse(null);
        if (logisticsLargeActualEntity != null) {
            throw new ServiceException(ApiError.ERROR_EXISTS_LOGISTICS_LARGE);
        }
        //预估账单只能推送一个
        LogisticsLargeEntity logisticsLargeEstimatedEntity = logisticsLargeEntities.stream()
                .filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())
                        && soOutstockEntity.getCode().equals(req.getOutstockCode())
                        && SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode().equals(req.getSourceType()))
                .findFirst().orElse(null);
        if (logisticsLargeEstimatedEntity != null) {
            throw new ServiceException(ApiError.ERROR_EXISTS_ESTIMATED_LOGISTICS_LARGE);
        }

        for (TransferDeclareCostAllocationEntity entity : costAllocationEntityList) {
            List<TransferDeclareCostAllocationDetailEntity> costAllocationDetailEntities = costAllocationDetailEntityList.stream()
                    .filter(req -> req.getMainId().equals(entity.getId()))
                    .collect(Collectors.toList());


            //处理数据
            this.generateTransferCostAllocationHandler(mainEntity, entity, costAllocationDetailEntities, reconciliationEntity, reconciliationDetailEntity, soOutstockEntity);
        }


        //更新中转费用分摊生成大表状态
        transferDeclareCostAllocationMainService.updateBigTableStatus(mainEntity.getId(), SmallBagCostAllocationBigTableStatusEnum.DONE.getCode());

        return BatchResultDTO.success(mainEntity.getId(), soOutstockEntity.getCode(), OperationTypeEnum.ADD);
    }

    private void generateTransferCostAllocationHandler(TransferDeclareCostAllocationMainEntity mainEntity,
                                                       TransferDeclareCostAllocationEntity entity,
                                                       List<TransferDeclareCostAllocationDetailEntity> detailEntityList,
                                                       TmsB2cDeclareReconciliationEntity declareReconciliationEntity,
                                                       TmsB2cDeclareReconciliationDetailEntity declareReconciliationDetailEntity,
                                                       SoOutstockEntity soOutstockEntity) {

        //根据销售出库单id查询物流单
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByOutstockIdList(Arrays.asList(soOutstockEntity.getId()));
        if (logisticsBillEntityList == null) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.get(0);
        List<LogisticsBillDetailEntity> billDetailEntities = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillEntityList.get(0).getId()));
        List<LogisticsBillCostEntity> logisticsBillCostEntities = logisticsBillCostService.listByLogisticsBillIdList(Arrays.asList(logisticsBillEntityList.get(0).getId()));
        if (CollUtil.isEmpty(logisticsBillCostEntities)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXISTS);
        }
        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntities.get(0);

        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();
        addDTO.setSourceId(mainEntity.getId());
        addDTO.setSourceType(SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode());
        addDTO.setSourceDetailId(entity.getId());
        addDTO.setLogisticsBillId(logisticsBillEntity.getId());
        addDTO.setLogisticsBillCostId(logisticsBillCostEntity.getId());
        if (CharSequenceUtil.isNotBlank(mainEntity.getReportDate())) {
            addDTO.setReconciliationMonth(mainEntity.getReportDate());
        }

        addDTO.setReconciliationBillType(ReconciliationBillTypeEnum.ACTUAL.getCode());
        addDTO.setOutstockCode(soOutstockEntity.getCode());
        addDTO.setOutstockTime(soOutstockEntity.getBillDate().atStartOfDay());

        addDTO.setPayStatus(declareReconciliationEntity.getPayStatus());

        //物流商信息
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(soOutstockEntity.getLogisticsChannelId());
        if (ObjectUtil.isNotEmpty(channelEntity)) {

            //销售出库单-关联物流单单号【渠道设置查询单号】
            if (CollUtil.isNotEmpty(logisticsBillEntityList)) {
                if (TrackQueryTypeEnum.TRANSPORT_NO.getCode().equals(channelEntity.getTrackQueryType())) {
                    addDTO.setTransportNo(logisticsBillEntityList.get(0).getTransportNo());
                } else {
                    if (CollUtil.isNotEmpty(billDetailEntities)) {
                        addDTO.setTransportNo(billDetailEntities.get(0).getTrackNo());
                    }
                }
            }
            addDTO.setLogisticsSupplierId(declareReconciliationDetailEntity.getLogisticsSupplierId());
            addDTO.setLogisticsSupplierName(declareReconciliationDetailEntity.getLogisticsSupplierName());


            //供应商信息
            TransferLogisticsSupplierEntity logisticsSupplierEntity = transferLogisticsSupplierService.getById(declareReconciliationDetailEntity.getLogisticsSupplierId());
            if (ObjectUtil.isNotEmpty(logisticsSupplierEntity)) {
                SupplierEntity supplierEntity = supplierFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());
                if (ObjectUtil.isNotEmpty(supplierEntity)) {
                    List<BaseDropDownDTO.DisabledDTO> disabledDTOS = scmTaskFeign.listPaymentCondition();
                    String paymentCondition = disabledDTOS.stream().
                            filter(req -> supplierEntity.getPaymentCondition() != null
                                    && supplierEntity.getPaymentCondition().equals(req.getCode()))
                            .map(BaseDropDownDTO.DisabledDTO::getValue)
                            .findFirst().orElse("");
                    addDTO.setPayTermsDays(paymentCondition);
                    addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
                    addDTO.setTaxRate(supplierEntity.getTaxRate());
                }
            }
        }
        addDTO.setSkuId(entity.getSkuId());
        addDTO.setSkuNo(entity.getSkuNo());
        addDTO.setDeliveryQty(entity.getDeliveryQty());

        //平台订单号
        SoB2cEntity soB2cEntity = soB2cFeign.getById(soOutstockEntity.getSoId());
        String platformCode = "";
        if (SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.SO_B2C.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode().equals(soOutstockEntity.getSourceType())
        ) {
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                platformCode = soB2cEntity.getPlatformCode();
            }
        } else {
            platformCode = soOutstockEntity.getSoCode();
        }

        TransferLogisticsChannelEntity transferLogisticsChannelEntity = transferLogisticsChannelService.getById(declareReconciliationDetailEntity.getLogisticsChannelId());
        if (ObjectUtil.isNotEmpty(transferLogisticsChannelEntity) && CharSequenceUtil.isNotBlank(transferLogisticsChannelEntity.getDeliveryCountry())) {
            DictCountryEntity countryEntity = FeignQuery.getById(DictCountryEntity.class, transferLogisticsChannelEntity.getDeliveryCountry());
            if (ObjectUtil.isNotEmpty(countryEntity)) {
                addDTO.setTransitPort(countryEntity.getNameCn());
            }
        }

        addDTO.setPlatformOrderCode(platformCode);
        //重量
        if (declareReconciliationDetailEntity.getActualBillingWeight().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setWeight(declareReconciliationDetailEntity.getActualBillingWeight());
            addDTO.setLogisticsBillingWeight(declareReconciliationDetailEntity.getActualBillingWeight());

        } else {
            List<ProductPackEntity> packEntityList = FeignQuery.create(ProductPackEntity.class).eq(ProductPackEntity::getSkuId, entity.getSkuId()).list();
            if (CollUtil.isNotEmpty(packEntityList)) {
                BigDecimal grossWeight = MathUtil.divide(MathUtil.multiplyWithTwo(packEntityList.get(0).getGrossWeight(), entity.getDeliveryQty()), MathUtil.BigDecimal_1000);
                addDTO.setWeight(grossWeight);
            }
        }
        addDTO.setShippingMethod(LogisticsLargeShippingMethodEnum.EXPRESS_DELIVERY.getCode());

        //启运地
        addDTO.setOriginPort("");

        //地址
        if (CharSequenceUtil.isNotBlank(soOutstockEntity.getSoId())) {
            if (OrderTypeEnum.B2C.getCode().equals(soOutstockEntity.getOrderType())) {
                SoB2cDTO.CustomerDTO customerDTO = soB2cFeign.getB2cCustomerById(soOutstockEntity.getSoId());
                addDTO.setDeliveryAddress(customerDTO.getReceiverAddress());
                addDTO.setDestinationPort(customerDTO.getCountryName());

            } else {
                SoInfoDTO.CustomerDTO customerDTO = soInfoFeign.getSoBaseById(soOutstockEntity.getSoId());
                addDTO.setDeliveryAddress(customerDTO.getReceiveAddress());
                if (CharSequenceUtil.isNotBlank(customerDTO.getCountryId())) {
                    DictCountryEntity countryEntity = FeignQuery.getById(DictCountryEntity.class, customerDTO.getCountryId());
                    if (ObjectUtil.isNotEmpty(countryEntity)) {
                        addDTO.setDestinationPort(countryEntity.getNameCn());
                    }
                }
            }
        }

        List<LogisticsChannelAddressDTO.ViewDTO> viewDTOS = logisticsChannelAddressService.listByChannelId(soOutstockEntity.getLogisticsChannelId());
        LogisticsChannelAddressDTO.ViewDTO viewDTO = viewDTOS.stream().filter(req -> LogisticsAddressTypeEnum.DELIVER.getCode().equals(req.getLogisticsAddressType())).findFirst().orElse(null);
        if (viewDTO != null) {
            LogisticsAddressEntity addressEntity = logisticsAddressService.getById(viewDTO.getAddressId());
            if (ObjectUtil.isNotEmpty(addressEntity)) {
                addDTO.setPickupAddress(addressEntity.getAddressFirst());
            }
        }
        addDTO.setPickupTime(soOutstockEntity.getBillDate().atStartOfDay());
        if (CollUtil.isNotEmpty(billDetailEntities)) {
            addDTO.setActualDeliveryTime(billDetailEntities.get(0).getSignTime());
        }

        //-----金额计算

        //总金额
        BigDecimal billAmountTotal = detailEntityList.stream().map(TransferDeclareCostAllocationDetailEntity::getBillAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        addDTO.setBillTotalAmount(billAmountTotal);

        //运费
        TransferDeclareCostAllocationDetailEntity shippingCostDetailEntity = detailEntityList.stream()
                .filter(req -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);

        if (shippingCostDetailEntity != null && shippingCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setFreightCalculationFactor(shippingCostDetailEntity.getAllocatedAmount().divide(shippingCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        //税率
        BigDecimal taxRate = addDTO.getTaxRate().divide(MathUtil.BigDecimal_100);
        addDTO.setFreightCurrency(shippingCostDetailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(shippingCostDetailEntity)) {
            BigDecimal lastMileFreightAmount = shippingCostDetailEntity.getAllocatedAmount();
            addDTO.setLastMileFreightAmountTax(shippingCostDetailEntity.getAllocatedAmount());
            addDTO.setLastMileFreightAmount(lastMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN));
            addDTO.setLastMileFreightVatAmount(lastMileFreightAmount.divide(BigDecimal.ONE.add(taxRate), 4, RoundingMode.DOWN).multiply(taxRate));
        }

        //杂费
        TransferDeclareCostAllocationDetailEntity otherCostDetailEntity = detailEntityList.stream()
                .filter(req -> AllocationFeeTypeEnum.OTHER_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (otherCostDetailEntity != null && otherCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDestMiscFeeFactor(otherCostDetailEntity.getAllocatedAmount().divide(otherCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        addDTO.setMiscFeeCurrency(otherCostDetailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(otherCostDetailEntity)) {
            addDTO.setEstimatedDestMiscFee(otherCostDetailEntity.getAllocatedAmount());
            addDTO.setActualDestMiscFee(otherCostDetailEntity.getAllocatedAmount());
        }
        addDTO.setDestMiscFeePayTime(logisticsBillCostEntity.getPayTime());

        //关税
        TransferDeclareCostAllocationDetailEntity declareCostDetailEntity = detailEntityList.stream()
                .filter(req -> AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (declareCostDetailEntity != null && declareCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDutyCalculationFactor(declareCostDetailEntity.getAllocatedAmount().divide(declareCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        addDTO.setDutyCurrency(declareCostDetailEntity.getAllocatedCurrency());
        if (ObjectUtil.isNotEmpty(declareCostDetailEntity)) {
            addDTO.setEstimatedDutyAmount(declareCostDetailEntity.getAllocatedAmount());
            addDTO.setActualDutyAmount(declareCostDetailEntity.getAllocatedAmount());
        }
        addDTO.setDestTaxPayTime(logisticsBillCostEntity.getPayTime());

        //添加
        this.add(addDTO);
    }

    @Override
    public Boolean exportLogisticsLarge(LogisticsLargeDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("物流大表", EXPORT_TMS_LOGISTICS_LARGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<String> listFirstMileCostAllocationIsExists() {
        return baseMapper.listFirstMileCostAllocationIsExists();
    }


    @Override
    public Boolean updatePayStatusBySourceId(List<String> sourceIds, String status, LocalDateTime payTime) {
        if (CollUtil.isEmpty(sourceIds)) {
            return Boolean.FALSE;
        }

        return this.lambdaUpdate()
                .set(LogisticsLargeEntity::getPayStatus, status)
                .set(LogisticsLargeEntity::getDeductibleTaxPayStatus, status)
                .set(LogisticsLargeEntity::getDestDutyPayStatus, status)
                .set(LogisticsLargeEntity::getOtherTaxPayStatus, status)
                .set(LogisticsLargeEntity::getDestMiscFeePayStatus, status)
                .set(LogisticsLargeEntity::getDeductibleTaxPayTime, payTime)
                .set(LogisticsLargeEntity::getDestTaxPayTime, payTime)
                .set(LogisticsLargeEntity::getFirstMilePayTime, payTime)
                .set(LogisticsLargeEntity::getOtherTaxPayTime, payTime)
                .set(LogisticsLargeEntity::getDestMiscFeePayTime, payTime)
                .in(LogisticsLargeEntity::getSourceId, sourceIds)
                .update();

    }

    @Override
    public Boolean updatePickupTimeBySourceId(List<String> sourceIds, LocalDateTime dateTime) {
        if (CollUtil.isEmpty(sourceIds)) {
            return Boolean.FALSE;
        }

        return this.lambdaUpdate()
                .set(LogisticsLargeEntity::getPickupTime, dateTime)
                .in(LogisticsLargeEntity::getSourceId, sourceIds)
                .update();
    }
}
