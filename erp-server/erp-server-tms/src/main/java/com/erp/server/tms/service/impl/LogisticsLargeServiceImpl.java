package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.SearchType;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.LogisticsLargeMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;

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
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

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
    private FirstMileEstimatedBillService firstMileEstimatedBillService;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private TmsCostDetailService logisticsBillCostDetailService;
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
        for (LogisticsLargeDTO.PagingViewDTO pagingViewDTO : list) {
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(req -> req.getId().equals(pagingViewDTO.getSkuId())).findFirst().orElse(null);
            if (productDetailEntity != null) {
                pagingViewDTO.setProductName(productDetailEntity.getName());
                pagingViewDTO.setSkuNo(productDetailEntity.getSkuNo());
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
        if(!save) {
            throw new ServiceException("物流大单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流大单" , logisticsLargeEntity.getId());
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流大单"));
        LogisticsLargeEntity logisticsLargeEntity =  BeanMapperUtils.map(LogisticsLargeEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsLargeEntity);
        log.info("编辑 开始修改物流大单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsLargeEntity);
        if(!save) {
            throw new ServiceException("物流大单保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录物流大单日志数据，id：【{}】", logisticsLargeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsLargeEntity.getId(), "物流大单");
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO delete(String id) {
        LogisticsLargeEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("单据未存在!");
        }

        this.lambdaUpdate().eq(LogisticsLargeEntity::getId, id).remove();

        return BatchResultDTO.success(entity.getId(), entity.getOutstockCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsLargeEntity logisticsLargeEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO generateFirstMileLogisticsTable(FirstMileCostAllocationEntity entity, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, List<FirstMileSkuCostAllocationDetailEntity> skuCostDetailEntityList, FirstMileDeliveryEntity deliveryEntity, List<FirstMileDeliveryDetailEntity> deliveryDetailEntities) {
        /*List<LogisticsLargeEntity> list = this.lambdaQuery().eq(LogisticsLargeEntity::getOutstockCode, entity.getSourceCode()).list();
        LogisticsLargeEntity logisticsLargeEntity = list.stream().filter(req -> ReconciliationBillTypeEnum.ACTUAL.getCode().equals(req.getReconciliationBillType())).findFirst().orElse(null);
        if (logisticsLargeEntity != null) {
            throw new ServiceException("实际账单已添加物流大表，请不要重复添加");
        }
        LogisticsLargeEntity largeEntity = list.stream().filter(req -> ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(req.getReconciliationBillType())).findFirst().orElse(null);
        if (logisticsLargeEntity != null) {
            throw new ServiceException("预估账单已添加物流大表，请不要重复添加");
        }

        //查询物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(entity.getLogisticsBillId());

        //查询物流详情
        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillEntity.getId()));

        //自发货费用
        List<LogisticsBillCostEntity> logisticsBillCostEntities = logisticsBillCostService.listByLogisticsBillIdList(Arrays.asList(logisticsBillEntity.getId()));


        //物流商信息
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(logisticsBillEntity.getId());

        //查询供应商
        SupplierEntity supplierEntity = supplierFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());

        //头程对账单主信息
        TmsFirstMileReconciliationEntity reconciliationEntity = tmsFirstMileReconciliationService.getById(entity.getReconciliationId());

        //头程对账明细信息
        TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity = tmsFirstMileReconciliationDetailService.getById(firstMileSkuCostAllocationEntity.getReconciliationDetailId());


        //查询仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(deliveryEntity.getDeliveryWarehouseId(), deliveryEntity.getDestWarehouseId()));

        //头程费用分摊费用明细表
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntities = firstMileSkuCostAllocationDetailService.listByMainIds(Arrays.asList(entity.getId()));

        //物流暂估账单
        List<FirstMileEstimatedBillDTO.View> estimatedBillView = firstMileEstimatedBillService.listByLogisticsBillIds(Arrays.asList(entity.getLogisticsBillId()), ConfirmStatusEnum.CONFIRM.getCode());

        //运费
        FirstMileSkuCostAllocationDetailEntity costAllocationDetailEntity = skuCostAllocationDetailEntities.stream().filter(req -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(req.getFeeType())).findFirst().orElse(null);
        List<String> ids = logisticsBillCostEntities.stream().map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
        List<TmsCostDetailDTO.CostCompareDTO> costCompareDTOList = logisticsBillCostDetailService.getCostCompareListByIds(ids);

        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();
        addDTO.setOutstockCode(deliveryEntity.getCode());
        addDTO.setOutstockTime(deliveryEntity.getApproveTime());
        //付款状态

        if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(firstMileSkuCostAllocationEntity.getBillSourceType())) {
            addDTO.setPayStatus(reconciliationEntity.getPayStatus());
            addDTO.setFreightCurrency(reconciliationEntity.getCurrency());
            //实际账单取【头程对账单】的汇率
            BigDecimal exchangeRate = reconciliationEntity.getExchangeRate();
            addDTO.setFirstMileEstimatedFreightTax(costAllocationDetailEntity.getAllocatedAmount().divide(exchangeRate, 4, RoundingMode.DOWN));
            addDTO.setFirstMileActualFreight(costAllocationDetailEntity.getAllocatedAmount().divide(exchangeRate, 4, RoundingMode.DOWN));

        } else {
            addDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            BigDecimal exchangeRate = BigDecimal.ONE;
            //预估账单取【物流单】的汇率
            if (CollUtil.isNotEmpty(logisticsBillCostEntities)) {
                String currency = logisticsBillCostEntities.get(0).getCurrency();
                exchangeRate = dmpTaskFeign.getRate(logisticsBillEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), currency);
            }
            addDTO.setFirstMileEstimatedFreightTax(costAllocationDetailEntity.getAllocatedAmount().divide(exchangeRate, 4, RoundingMode.DOWN));
            addDTO.setFirstMileActualFreight(costAllocationDetailEntity.getAllocatedAmount().divide(exchangeRate, 4, RoundingMode.DOWN));
        }

        BigDecimal firstMileEstimatedFreight = BigDecimal.ZERO;
        BigDecimal firstMileActualFreight = BigDecimal.ZERO;

        for (TmsCostDetailDTO.CostCompareDTO costCompareDTO : costCompareDTOList) {
            firstMileEstimatedFreight = firstMileEstimatedFreight.add(costCompareDTO.getEstimatedFee().divide(BigDecimal.ONE.add(exchangeRate)));


            //头程物流单实际运费
            firstMileActualFreight = firstMileActualFreight.add(costCompareDTO.getActualFee().divide(BigDecimal.ONE.add(exchangeRate)));
            addDTO.setFirstMileActualFreight(firstMileActualFreight);
        }

        addDTO.setSkuId(firstMileSkuCostAllocationEntity.getSkuId());
        addDTO.setSkuNo(firstMileSkuCostAllocationEntity.getSkuNo());
        addDTO.setDeliveryQty(firstMileSkuCostAllocationEntity.getDeliveryQty());
        addDTO.setWeight(reconciliationDetailEntity.getBillingWeight());
        addDTO.setLogisticsBillingWeight(reconciliationDetailEntity.getVolumeWeight());
        addDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
        addDTO.setLogisticsSupplierId(logisticsSupplierEntity.getId());
        addDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
        addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
        addDTO.setTransportNo(logisticsBillEntity.getCounterNo());
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> deliveryEntity.getDeliveryWarehouseId().equals(req.getId())).findFirst().orElse(null);
        if (deliveryWarehouse != null) {
            addDTO.setOriginPort(deliveryWarehouse.getAddress());
            addDTO.setPickupAddress(deliveryWarehouse.getAddress());
        }
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream().filter(req -> deliveryEntity.getDestWarehouseId().equals(req.getId())).findFirst().orElse(null);
        if (destWarehouse != null) {
            addDTO.setDestinationPort(destWarehouse.getAddress());
            addDTO.setDeliveryAddress(destWarehouse.getAddress());
        }

        LogisticsBillDetailEntity billDetailEntity = detailEntityList.stream().filter(req -> FmLogisticTrackStatusEnum.PICKUP.getCode().equals(req.getTrackStatus())).findFirst().orElse(null);
        if (billDetailEntity != null) {
            addDTO.setPickupTime(billDetailEntity.getTrackTime());
        }
        if (CollUtil.isNotEmpty(detailEntityList)) {
            addDTO.setActualDeliveryTime(detailEntityList.get(0).getSignTime());
        }

        if (costAllocationDetailEntity != null && costAllocationDetailEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            //[运费计算系数]头程分摊金额/头程金额
            addDTO.setFreightCalculationFactor(costAllocationDetailEntity.getAllocatedAmount().divide(costAllocationDetailEntity.getAmount(), 6, RoundingMode.DOWN));
        }

        // TODO
        addDTO.setBillTotalAmount(BigDecimal.ZERO);

        addDTO.setFirstMileActualFreightTax(costAllocationDetailEntity.getAllocatedAmount());

        addDTO.setFirstMileEstimatedFreight(firstMileEstimatedFreight);*/

//        tmsFirstMileReconciliationService.set
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public List<LogisticsLargeEntity> listByIdSourceId(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(LogisticsLargeEntity::getSourceId, ids).list();
    }

    @Override
    public BatchResultDTO generateSmallBagCostAllocationTable(SmallBagCostAllocationEntity costAllocationEntity, List<SmallBagCostAllocationDetailEntity> costAllocationDetailEntities, SoOutstockEntity soOutstockEntity, SoOutstockDetailEntity soOutstockDetailEntity) {
        LogisticsLargeDTO.AddDTO addDTO = new LogisticsLargeDTO.AddDTO();
        addDTO.setOutstockCode(soOutstockEntity.getCode());
        addDTO.setOutstockTime(soOutstockEntity.getApproveTime());

        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostService.getById(costAllocationEntity.getCostId());
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillCostEntity.getLogisticsBillId());


        if (ReconciliationStatusEnum.CONFIRMED.getCode().equals(logisticsBillCostEntity.getReconciliationStatus())) {
            //实际账单
            addDTO.setPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setDeductibleTaxPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setDestDutyPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setOtherTaxPayStatus(logisticsBillCostEntity.getPayStatus());
            addDTO.setDestMiscFeePayStatus(logisticsBillCostEntity.getPayStatus());


        } else {
            //预估账单
            addDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDeductibleTaxPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDestDutyPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setOtherTaxPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            addDTO.setDestMiscFeePayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        }

        //付款方式
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(soOutstockEntity.getLogisticsChannelId());
        if (ObjectUtil.isNotEmpty(channelEntity)) {
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(channelEntity.getMainId());
            if (ObjectUtil.isNotEmpty(logisticsSupplierEntity)) {

                addDTO.setLogisticsSupplierId(logisticsSupplierEntity.getId());
                addDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());

                SupplierEntity supplierEntity = supplierFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());
                if (ObjectUtil.isNotEmpty(supplierEntity)) {
                    List<BaseDropDownDTO.DisabledDTO> disabledDTOS = scmTaskFeign.listPaymentCondition();
                    String paymentCondition = disabledDTOS.stream().filter(req -> supplierEntity.getPaymentCondition().equals(req.getCode())).map(BaseDropDownDTO.DisabledDTO::getValue).findFirst().orElse("");
                    addDTO.setPayTermsDays(paymentCondition);
                    addDTO.setPaymentCompanyName(supplierEntity.getPaymentCompanyName());
                    addDTO.setTaxRate(supplierEntity.getTaxRate());

                }
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
            String sourceId = soOutstockEntity.getSourceId();
            SoB2cEntity soB2cEntity = soB2cFeign.getById(sourceId);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                platformCode = soB2cEntity.getPlatformCode();

                if (!TransferStatusEnum.NOT.getCode().equals(soB2cEntity.getTransferStatus())) {
                    addDTO.setTransitPort("中国-香港");
                }
            }
        }  else {
            platformCode = soOutstockEntity.getSoCode();
            addDTO.setTransitPort("中国");
        }

        addDTO.setPlatformOrderCode(platformCode);

        addDTO.setWeight(logisticsBillCostEntity.getBillingWeightLogistics());
        addDTO.setLogisticsBillingWeight(logisticsBillCostEntity.getBillingWeightLogistics());
        addDTO.setShippingMethod(LogisticsLargeShippingMethodEnum.EXPRESS_DELIVERY.getCode());

        addDTO.setTransportNo(logisticsBillCostEntity.getTransportNo());

        //自发货是直发，默认东莞
        if (ShipmentTypeEnum.SELF_DELIVER.getCode().equals(logisticsBillEntity.getShipmentType())) {
            addDTO.setOriginPort("东莞");
        } else {
            List<WarehouseEntity> list = FeignQuery.create(WarehouseEntity.class).eq(WarehouseEntity::getId, soOutstockEntity.getWarehouseId()).list();
            if (CollUtil.isNotEmpty(list)) {
                addDTO.setOriginPort(list.get(0).getAddress());
            }
        }

        //地址
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        if (CharSequenceUtil.isNotBlank(soOutstockEntity.getCustomerId())) {
            List<CustomerInfoEntity> customerList = FeignQuery.create(CustomerInfoEntity.class).eq(CustomerInfoEntity::getId, soOutstockEntity.getCustomerId()).list();
            if (CollUtil.isNotEmpty(customerList)) {
                String countryName = countryList.stream().filter(obj -> obj.getId().equals(customerList.get(0).getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                if (CharSequenceUtil.isNotBlank(countryName)) {
                    addDTO.setDestinationPort(countryName);
                } else {
                    addDTO.setDestinationPort(customerList.get(0).getMailAddress());
                }

                addDTO.setDeliveryAddress(customerList.get(0).getMailAddress());
            }
        } else {
            String countryName = countryList.stream().filter(obj -> obj.getId().equals(logisticsBillEntity.getToCountry())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            addDTO.setDestinationPort(countryName);
        }
        List<LogisticsChannelAddressDTO.ViewDTO> viewDTOS = logisticsChannelAddressService.listByChannelId(soOutstockEntity.getLogisticsChannelId());
        LogisticsChannelAddressDTO.ViewDTO viewDTO = viewDTOS.stream().filter(req -> LogisticsAddressTypeEnum.DELIVER.getCode().equals(req.getLogisticsAddressType())).findFirst().orElse(null);
        if (viewDTO != null) {
            addDTO.setPickupAddress(viewDTO.getLogisticsAddressName());
        }
        addDTO.setPickupTime(soOutstockEntity.getBillDate().atStartOfDay());

        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(logisticsBillEntity.getId()));
        if (CollUtil.isNotEmpty(detailEntityList)) {
            addDTO.setActualDeliveryTime(detailEntityList.get(0).getSignTime());
        }
        //账单总金额
        BigDecimal billAmountTotal = costAllocationDetailEntities.stream().map(SmallBagCostAllocationDetailEntity::getBillAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        addDTO.setBillTotalAmount(billAmountTotal);

        //运费
        addDTO.setFreightCurrency(logisticsBillCostEntity.getCurrency());
        SmallBagCostAllocationDetailEntity detailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (detailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setFreightCalculationFactor(detailEntity.getAllocatedAmount().divide(detailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        BigDecimal rate = dmpTaskFeign.getRate(logisticsBillEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), logisticsBillCostEntity.getCurrency());

        addDTO.setFreightCurrency(logisticsBillCostEntity.getCurrency());
        if (ObjectUtil.isNotEmpty(detailEntity)) {
            BigDecimal lastMileFreightAmount = detailEntity.getAllocatedAmount().multiply(rate);
            addDTO.setLastMileFreightAmount(detailEntity.getAllocatedAmount().multiply(rate));
            addDTO.setLastMileFreightAmountTax(lastMileFreightAmount.divide(BigDecimal.ONE.add(addDTO.getTaxRate()), 4, RoundingMode.DOWN));
            addDTO.setLastMileFreightVatAmount(lastMileFreightAmount.divide(BigDecimal.ONE.add(addDTO.getTaxRate()), 4, RoundingMode.DOWN).multiply(addDTO.getTaxRate()));
        }

        //杂费
        SmallBagCostAllocationDetailEntity otherCostDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.OTHER_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (otherCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDestMiscFeeFactor(otherCostDetailEntity.getAllocatedAmount().divide(otherCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        // TODO 暂时取物流单的 后期取大类的
        addDTO.setMiscFeeCurrency(logisticsBillCostEntity.getCurrency());
        if (ObjectUtil.isNotEmpty(otherCostDetailEntity)) {
            addDTO.setEstimatedDestMiscFee(otherCostDetailEntity.getAllocatedAmount().multiply(rate));
            addDTO.setActualDestMiscFee(otherCostDetailEntity.getAllocatedAmount().multiply(rate));
        }
        addDTO.setDestMiscFeePayTime(logisticsBillCostEntity.getPayTime());

        //关税
        SmallBagCostAllocationDetailEntity declareCostDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (declareCostDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDutyCalculationFactor(declareCostDetailEntity.getAllocatedAmount().divide(declareCostDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        // TODO 暂时取物流单的 后期取大类的
        addDTO.setDutyCurrency(logisticsBillCostEntity.getCurrency());
        if (ObjectUtil.isNotEmpty(declareCostDetailEntity)) {
            addDTO.setEstimatedDutyAmount(declareCostDetailEntity.getAllocatedAmount().multiply(rate));
            addDTO.setActualDutyAmount(declareCostDetailEntity.getAllocatedAmount().multiply(rate));
        }
        addDTO.setDestTaxPayTime(logisticsBillCostEntity.getPayTime());

        //可抵扣税金
        SmallBagCostAllocationDetailEntity deductibleTaxDetailEntity = costAllocationDetailEntities.stream()
                .filter(req -> AllocationFeeTypeEnum.DEDUCTIBLE_TAX.getCode().equals(req.getFeeType()))
                .findFirst().orElse(null);
        if (deductibleTaxDetailEntity.getBillAmount().compareTo(BigDecimal.ZERO) > 0) {
            addDTO.setDeductibleTaxFactor(deductibleTaxDetailEntity.getAllocatedAmount().divide(deductibleTaxDetailEntity.getBillAmount(), 4, RoundingMode.DOWN));
        }
        // TODO 暂时取物流单的 后期取大类的
        addDTO.setDeductibleTaxCurrency(logisticsBillCostEntity.getCurrency());
        if (ObjectUtil.isNotEmpty(deductibleTaxDetailEntity)) {
            addDTO.setEstimatedDeductibleTax(deductibleTaxDetailEntity.getAllocatedAmount().multiply(rate));
            addDTO.setActualDeductibleTax(deductibleTaxDetailEntity.getAllocatedAmount().multiply(rate));
        }


        return null;
    }

    private String getOutstockPlatformOrderCode(SoOutstockEntity soOutstockEntity) {
        String platformCode = "";
        if (SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.SO_B2C.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(soOutstockEntity.getSourceType())
                || SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode().equals(soOutstockEntity.getSourceType())
        ) {
            String sourceId = soOutstockEntity.getSourceId();
            SoB2cEntity soB2cEntity = soB2cFeign.getById(sourceId);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                platformCode = soB2cEntity.getPlatformCode();
            }
        }  else {
            platformCode = soOutstockEntity.getSoCode();
        }
        return platformCode;
    }
}
