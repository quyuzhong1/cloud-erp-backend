package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.SearchType;
import com.common.business.dto.*;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.ShipmentTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TmsDeclareBillFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.convert.SoOutstockConverter;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_OUT_STOCK;

/**
 * <p>
 * 销售订单出库单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class SoOutstockServiceImpl extends SuperServiceImpl<SoOutstockMapper, SoOutstockEntity> implements SoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInStockService;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private ScmTaskFeign scmTaskFeign;


    @Resource
    private WorkflowFeign workflowFeign;


    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    @Resource
    private TmsDeclareBillFeign tmsDeclareBillFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;

    @Resource
    private CfgRuleOutService cfgRuleOutService;


    @Lazy
    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private AbstractWdtService abstractWdtService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private WmsPushMsgService wmsPushMsgService;



    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoOutstockEntity::getSourceId, ids).list();
    }

    @Override
    public List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds) {
        if(CollectionUtils.isEmpty(soIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE)
                .in(SoOutstockEntity::getSoId, soIds).list();
    }

    public List<SoOutstockEntity> listDbBySoIds(@RequestBody List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoOutstockEntity::getSoId, soIds).list();
    }

    /**
     * 添加销售出库单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-19 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoOutstockDTO.AddDTO dto) {
        //TODO 对应检查数量
        String id = IdWorker.getIdStr();
        //来源类型
        String sourceType = dto.getSourceType();
        if (CharSequenceUtil.isBlank(sourceType)) {
            sourceType = SourceTypeEnum.SELF_ADD.getCode();
        }
        String sourceId = dto.getSourceId();
        List<SoOutstockDetailDTO.AddDTO> detailList = dto.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }
        //销售订单详情集合
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(dto.getSoId()));
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException("销售订单详情不存在");
        }

        //检查出库数量
        List<SoOutstockDetailDTO.UpdateDTO> checkList = BeanMapper.copyList(detailList, SoOutstockDetailDTO.UpdateDTO.class);
        soOutstockDetailService.checkOutQty(dto.getWarehouseId(), dto.getSoId(), sourceId, sourceType, checkList, dto.getBatchNo());
        //销售订单的含税销售金额折扣前
        BigDecimal soAmount = BigDecimal.ZERO;
        for (SoDetailEntity soDetail : soDetailList) {
            BigDecimal taxAmountBefore = soDetail.getTaxAmountBefore();
            soAmount = soAmount.add(Objects.isNull(taxAmountBefore) ? BigDecimal.ZERO : taxAmountBefore);
        }
        //出库金额
        BigDecimal outStockAmount = BigDecimal.ZERO;
        for (SoOutstockDetailDTO.AddDTO item : detailList) {

            //实发数量
            Integer actualQty = item.getActualQty();
            String soDetailId = item.getSoDetailId();

            BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getPrice).orElse(BigDecimal.ZERO);
            //税率
            BigDecimal taxRate = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getTaxRate).orElse(BigDecimal.ZERO);

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);

            outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
        }

        //销售订单
        String soId = dto.getSoId();
        SoInfoDTO.CustomerDTO soCustomer = soInfoFeign.getSoBaseById(soId);
        if (Objects.isNull(soCustomer)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        //销售订单折扣额
        BigDecimal discountAmount = soCustomer.getDiscountAmount();
        //折扣总额占比
        BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
        //整单折扣额
        BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);

        SoOutstockEntity soOutstock = new SoOutstockEntity();
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSCK);
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setCode(code);
        soOutstock.setId(id);
        soOutstock.setTotalDiscountAmount(totalDiscountAmount);
        if(Objects.isNull(soOutstock.getBillDate())){
            soOutstock.setBillDate(LocalDate.now());
        }
        //tob 保存数据修改
        handleSaveOrUpdateDbByB2b(soOutstock, soCustomer);
        List<SoOutstockDetailDTO.AddDTO> addDetailList = dto.getDetailList();
        Boolean addResult = this.save(soOutstock);
        //添加成功
        if (addResult) {
            soOutstockDetailService.add(id, addDetailList, soOutstock.getOrderType());
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "新增操作");
            return id;
        }

        return "";
    }

    /**
     * 处理添加或修改b2b 数据
     * @param soOutstock
     */
    private void handleSaveOrUpdateDbByB2b(SoOutstockEntity soOutstock, SoInfoDTO.CustomerDTO soCustomer ) {
        WarehouseEntity warehouse = warehouseService.getById(soOutstock.getWarehouseId());
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        soOutstock.setSoCode(soCustomer.getCode());
        soOutstock.setCustomerId(soCustomer.getCustomerId());
        soOutstock.setCustomerName(soCustomer.getCustomerName());
        soOutstock.setOrderType(soCustomer.getOrderType());
        soOutstock.setSalesDeptId(soCustomer.getSalesDeptId());
        soOutstock.setWarehouseOrgId(warehouse.getOrgId());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(warehouse.getOrgId()));
        if (CollectionUtils.isNotEmpty(orgList)) {
            soOutstock.setWarehouseOrgName(orgList.get(0).getName());
        }
        soOutstock.setSalesOrgId(soCustomer.getSalesOrgId());
        soOutstock.setSalesOrgName(soCustomer.getSalesOrgName());
        soOutstock.setSellerId(soCustomer.getSellerId());
        soOutstock.setCountry(soCustomer.getCountryId());
        if(OrderTypeEnum.B2B.getCode().equals(soOutstock.getOrderType())){
            soOutstock.setDeclareStatus(WmsDeclareStatusEnum.WAIT.getCode());
        }
        if(OrderTypeEnum.B2C.getCode().equals(soOutstock.getOrderType())){
            soOutstock.setSellerId(soCustomer.getCustomerSellerId());
        }
        //仓库id
        String warehouseKeeperId = soOutstock.getWarehouseKeeperId();
        String sellerId = soOutstock.getSellerId();
        //用户信息
        if (CharSequenceUtil.isNotBlank(warehouseKeeperId) || CharSequenceUtil.isNotBlank(sellerId)) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId, sellerId));

            if (CollectionUtils.isNotEmpty(userList)) {
                //仓管员
                String warehouseKeeperName = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setWarehouseKeeperName(warehouseKeeperName);
                //销售员
                String sellerName = userList.stream().filter(obj -> obj.getUserId().equals(sellerId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setSellerName(sellerName);
                if(OrderTypeEnum.B2C.getCode().equals(soOutstock.getOrderType())){
                    String saleDeptId = userList.stream().filter(obj -> obj.getUserId().equals(sellerId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getDepartmentId())).orElse("");
                    soOutstock.setSalesDeptId(saleDeptId);
                }
            }
        }

    }


    /**
     * 批量提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:34
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoOutstockEntity> list = this.listByIds(ids);
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }

        //查询是否冻结
        List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }

        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus), "", null);
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "状态变更");
        }
        return result;
    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 10:42
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SoOutstockDTO.AddDTO dto) {
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Collections.singletonList(id));
        return result;
    }


    /**
     * 销售出库单详情
     *
     * @param id
     * @return com.erp.model.wms.dto.SoOutstockDTO.ViewDTO
     * @author yl
     * @date 2023-05-19 10:45
     */
    @Override
    public SoOutstockDTO.ViewDTO view(String id) {
        SoOutstockEntity soOutstock = this.getById(id);
        if (Objects.isNull(soOutstock)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        SoOutstockDTO.ViewDTO result = new SoOutstockDTO.ViewDTO();
        BeanMapper.copy(soOutstock, result);
        ApproveStatusEnum approveStatus = soOutstock.getApproveStatus();
        result.setApproveStatusName(approveStatus.getName());
        List<CustomerInfoEntity> customerList = customerFeign.listCustomerByIds(Collections.singletonList(soOutstock.getCustomerId()));
        //国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        if (CollectionUtils.isNotEmpty(countryList) && CollectionUtils.isNotEmpty(customerList)) {
            String countryName = countryList.stream().filter(obj -> obj.getId().equals(customerList.get(0).getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            result.setCountryId(customerList.get(0).getCountryId());
            result.setCountryName(countryName);
        }

        if (SourceTypeEnum.WDT_OUT_STOCK.getCode().equals(soOutstock.getSourceType())) {
            String countryName = countryList.stream().filter(e -> e.getId().equals(soOutstock.getCountry())).map(DictCountryDTO.ListDTO::getNameCn).findFirst().orElse("");
            result.setCountryId(soOutstock.getCountry());
            result.setCountryName(countryName);
        }

        if (CharSequenceUtil.isNotBlank(result.getCarrierId())) {
            //获取采购单供应商信息
            SupplierEntity supplierById = scmTaskFeign.getSupplierById(result.getCarrierId());
            if(Objects.nonNull(supplierById)){
                result.setCarrierName(supplierById.getName());
            }
        }

        String soId = soOutstock.getSoId();
        String orderType = soOutstock.getOrderType();
        String b2c = OrderTypeEnum.B2C.getCode();
        Boolean isB2c = b2c.equals(orderType);
        result.setTypeName(OrderTypeEnum.getName(orderType));
        result.setSoCode(soOutstock.getSoCode());
        result.setSellerId(soOutstock.getSellerId());
        if (!isB2c) {
            if (CharSequenceUtil.isNotBlank(soId)){
                SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
                if (soInfo != null) {
                    result.setCustomerName(soInfo.getCustomerName());
                    result.setSoRemark(soInfo.getSoRemark());
                    result.setReceiveAddress(soInfo.getReceiveAddress());
                    result.setReceiverName(soInfo.getReceiverName());
                    result.setDeliveryModeName(soInfo.getDeliveryModeName());
                    result.setRequireDate(soInfo.getRequireDate());
                    result.setTelNumber(soInfo.getTelNumber());
                    result.setTypeName(soInfo.getOrderTypeName());
                    result.setSellerName(soInfo.getSellerName());
                    result.setSalesDeptId(soInfo.getSalesDeptId());
                    result.setSalesDeptName(soInfo.getSalesDeptName());
                    result.setSalesOrgName(soInfo.getSalesOrgName());
                }
            }
        } else {
            if (CharSequenceUtil.isNotBlank(soId)) {
                SoB2cDTO.CustomerDTO customer = soB2cFeign.getB2cCustomerById(soId);
                result.setCustomerName(customer.getCustomerName());
                result.setReceiveAddress(customer.getReceiverAddress());
                result.setReceiverName(customer.getReceiverName());
                result.setTelNumber(customer.getTelNumber());
                result.setSellerName(customer.getSellerName());
                result.setSalesOrgName(customer.getSalesOrgName());
                result.setDeliveryModeName(customer.getDeliveryModeName());
                result.setCountryId(customer.getCountry());
                result.setCountryName(customer.getCountryName());
            }
            //要货日期通销售订单创建日期
            result.setRequireDate(soOutstock.getPlanDeliveryDate());
            String salesDeptId = soOutstock.getSalesDeptId();
            if (CharSequenceUtil.isNotBlank(salesDeptId)) {
                SysDepartmentDTO department = sysUserFeign.getUserDeptById(salesDeptId);
                if (Objects.nonNull(department)) {
                    result.setSalesDeptName(department.getName());
                }
            }

        }
        //跟踪单号
        Map<String,List<String>> trackNoMAp = logisticsBillFeign.mapTrackNoAndSoOutId(Collections.singletonList(soOutstock.getId()));
        result.setTrackNoList(trackNoMAp.get(soOutstock.getId()));
        List<SoOutstockDetailDTO.ViewDTO> detailList = soOutstockDetailService.listByMainId(id, soOutstock.getWarehouseId());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(soOutstock.getWarehouseId()));
        for (SoOutstockDetailDTO.ViewDTO viewDTO : detailList) {
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(viewDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
        result.setDetailList(detailList);
        return result;
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 11:42
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "dto.id")
    public BatchResultDTO approve(ApproveOneDTO dto) {
        SoOutstockEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());

        ApproveStatusEnum ingStatus = ApproveStatusEnum.APPROVE_ING;
        if (!ingStatus.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        if(OrderTypeEnum.B2B.getCode().equalsIgnoreCase(entity.getOrderType())){
            if (ObjectUtil.isNotEmpty(entity.getSoId())) {
                List<SoOutstockDetailEntity> detailEntities = soOutstockDetailService.listDetailBySoIds(Collections.singletonList(entity.getSoId()));
                List<SoDetailEntity> soDetails = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(entity.getSoId()));
                Map<String, Integer> detailMap = detailEntities.stream().filter(e -> Boolean.FALSE.equals(e.getInvalidStatus())).collect(Collectors.toMap(SoOutstockDetailEntity::getSkuNo, SoOutstockDetailEntity::getPlanQty, Integer::sum));
                Map<String, Integer> soDetailMap = soDetails.stream().collect(Collectors.toMap(SoDetailEntity::getSkuNo, SoDetailEntity::getQty, Integer::sum));
                for (Map.Entry<String, Integer> entry : detailMap.entrySet()) {
                    int sellQty = Optional.ofNullable(soDetailMap.get(entry.getKey())).orElse(0);
                    if (sellQty == 0) {
                        throw new ServiceException(ApiError.ERROR_99107, entry.getKey());
                    }
                    if (sellQty < entry.getValue()) {
                        throw new ServiceException(ApiError.ERROR_99103, entry.getKey());
                    }
                }
            }
        }
        //销售出库单反审核后修改出库日期审核时，需要校验是否有关联的中转调拨单
        if (CharSequenceUtil.isNotBlank(entity.getSourceId())){
            List<TransferInfoEntity> transferInfoEntities = transferInfoService.listBySourceId(entity.getSourceId());
            if (CollectionUtils.isNotEmpty(transferInfoEntities)){
                //如果调拨单没有审核，需要提示，请先审核通过关联的中转调拨单后审核出库单
                List<String> transferCodeList = transferInfoEntities.stream().filter(e -> !Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), e.getApproveStatus())).map(TransferInfoEntity::getCode).distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(transferCodeList)){
                    throw new ServiceException(ApiError.ERROR_92164,String.join(",",transferCodeList));
                }
                //需要限制出库日期不能早于最后一个（按日期排序）调拨单的调拨日期
                TransferInfoEntity transferInfoEntity = transferInfoEntities.stream().max(Comparator.comparing(TransferInfoEntity::getBillDate)).orElse(null);
                if (Objects.nonNull(transferInfoEntity) && entity.getBillDate().isBefore(transferInfoEntity.getBillDate())){
                    throw new ServiceException(ApiError.ERROR_92165, transferInfoEntity.getBillDate());
                }
            }
        }
        // 调用流程审核
        approveProcess(entity, dto);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "销售出库单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_OUT_STOCK.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));

    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    public void approveProcess(SoOutstockEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_OUTSTOCK.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        String uid = userInfo.getUid();
        if (CharSequenceUtil.isBlank(uid)) {
            uid = "system";
        }
        approveDTO.setUserId(uid);
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtils.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            this.approveEnd(dto, entity);
        }
    }

    /**
     * 流程结束
     *
     * @param dto
     * @param entity
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoOutstockEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        String orderType = entity.getOrderType();
        String b2c = OrderTypeEnum.B2C.getCode();
        Boolean isB2c = b2c.equals(orderType);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus(), isB2c);

        Boolean isPass = ApproveStatusEnum.APPROVE.equals(approveStatus);
        if (isPass) {
            //审核通过发送金蝶
            if (!isB2c) {
                // B2B物流单发货时间=销售出库单审核时间
                entity.setActualDeliveryDate(LocalDateTime.now());
                handleData(entity);
            } else {
                handleSoB2cData(entity);
            }

//            TransferDeclareDTO.UpdateOutstockStatusDTO statusDTO = new TransferDeclareDTO.UpdateOutstockStatusDTO();
//            statusDTO.setSoIds(Collections.singletonList(entity.getSoId()));
//            statusDTO.setStatus(TransferOutstockStatusEnum.OUTSTOCK.getCode());
//            //修改中转报关单订单出库状态
//            transferDeclareFeign.updateOutstockStatus(statusDTO);

            //走TMS自动生成报关单逻辑
            if (!"CN".equalsIgnoreCase(entity.getCountry()) && entity.getDeclareStatus().equals(WmsDeclareStatusEnum.WAIT.getCode()) && entity.getOrderType().equals(OrderTypeEnum.B2B.getCode())) {
                //走TMS自动生成逻辑
                AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
                        .id(entity.getId())
                        .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_APPROVE)
                        .sourceTypeEnum(SourceTypeEnum.SO_OUTSTOCK)
                        .soOutstockEntity(entity)
                        .build();
                try {
                    Boolean autoGenerateResult = tmsDeclareBillFeign.autoGenerateB2bDeclare(autoGenerateBillDTO);
                    if(autoGenerateResult){
                        TmsDeclareBillDTO.UpdateStatusDTO updateStatusDTO = new TmsDeclareBillDTO.UpdateStatusDTO();
                        updateStatusDTO.setIds(Collections.singletonList(entity.getId()));
                        updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
                        this.updateStatus(updateStatusDTO);
                    }
                }catch (Exception e){
                    log.error("销售出库单{} 审核后自动生成报关单失败>>>>>>{}", entity.getCode(), e.getMessage());
                    throw new ServiceException(CharSequenceUtil.format("销售出库单{} 审核后自动生成报关单失败>>>>>>{}", entity.getCode(), e.getMessage()));
                }
            }
            //B2B发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
            //推送旺店通
            this.syncToWdt(entity,SyncOperateEnum.OPERATE_APPROVE);
            //推送数帝云
            sdyFieldHandler(entity,SyncOperateEnum.OPERATE_APPROVE.getCode());

        }
        if (!SourceTypeEnum.SAL_OUTSTOCK.getCode().equals(entity.getSourceType())) {
            //订单推送dmp
            syncKingdeeSoOutstockService.syncOrderToDmp(entity, SyncOperateEnum.OPERATE_APPROVE.getCode());
        }
        return Boolean.TRUE;
    }

    /**
     * 处理B2c销售出库单
     *
     * @param entity
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-01 10:50
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void handleSoB2cData(SoOutstockEntity entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        List<InOutStockDTO> members = baseMapper.listInventoryInOut(Collections.singletonList(entity.getId()));
        if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(entity.getSourceType())){
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        }else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK_USABLE.getCode());
        }
        for (InOutStockDTO member : members) {
            member.setSourceType(InventorySourceTypeEnum.SO_OUTSTOCK);
            // B2C销售出库单出库等待时间20秒
            member.setLockWaitTime(20L);
        }
        if (CollectionUtils.isNotEmpty(members)) {
            //无虚拟仓无需扣减库存
            List<InOutStockDTO> virtualInOutStockList = members.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
            //B2C发货单下推的销售出库单无需扣库存
            if (CollectionUtils.isNotEmpty(virtualInOutStockList) && !SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(entity.getSourceType())) {
                VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
                List<VirtualInventoryStockDTO.OutInStockDTO> outInStockDTOS = BeanMapperUtils.copyList(VirtualInventoryStockDTO.OutInStockDTO.class, virtualInOutStockList);
                stockParamDTO.setParamList(outInStockDTOS);
                stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.OUT_USABLE.getCode());
                virtualInventoryTransCoreService.approve(stockParamDTO);
            }
            //扣实体仓库存
            inventoryInOutStockDTO.setParamList(members);
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
        //物流单添加
        saveLogisticsBill(entity);
    }


    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus, Boolean isB2c) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SoOutstockEntity::getId, id)
                .set(SoOutstockEntity::getApproveUserName, userInfo.getUserName())
                .set(SoOutstockEntity::getApproveStatus, approveStatus)
                .set(SoOutstockEntity::getApproveTime, LocalDateTime.now())
                .set(!isB2c,SoOutstockEntity::getActualDeliveryDate, LocalDateTime.now())
                .update(new SoOutstockEntity());
    }


    /**
     * 处理数据
     * 需要更改发货状态
     *
     * @param entity
     * @return void
     * @author yl
     * @date 2023-05-22 20:01
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleData(SoOutstockEntity entity) {
        if (Objects.isNull(entity)) {
            return;
        }


        //这个是销售出库单id
        List<String> allList = Collections.singletonList(entity.getId());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        String sourceType = entity.getSourceType();
        //发货通知单的 id
        List<SoOutstockEntity> noticeSoOutstockList = soDeliveryNotice.equals(sourceType) ? Collections.singletonList(entity) : Collections.emptyList();
        //发货通知单的 id
        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();

        //发货通知单详情
        for (SoDeliveryNoticeEntity item : noticeList) {
            String deliveryNoticeId = item.getId();
            SoOutstockEntity noticeSoOutstock = noticeSoOutstockList.stream().filter(o -> o.getSourceId().equals(deliveryNoticeId)).findFirst().orElse(null);
            if (noticeSoOutstock != null) {
                //更新打包时间
                item.setPackDate(noticeSoOutstock.getPackDate());
                item.setActualDeliveryDate(noticeSoOutstock.getActualDeliveryDate()!=null?noticeSoOutstock.getActualDeliveryDate().toLocalDate():null);
            }
            item.setDeliveryStatus(Boolean.TRUE);
        }
        //更改打包日期 以及发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);

        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(allList);
        soOutstockDetailList=soOutstockDetailList.stream().filter(s->CharSequenceUtil.isNotBlank(s.getSoDetailId())).collect(Collectors.toList());
        List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>();
        for (SoOutstockDetailEntity item : soOutstockDetailList) {
            SoDetailDTO.UpdateDeliveryStatusDTO param = new SoDetailDTO.UpdateDeliveryStatusDTO();
            param.setDeliveryQty(item.getActualQty());
            param.setId(item.getSoDetailId());
            paramList.add(param);
        }
        soInfoFeign.updateDeliveryStatus(paramList);
        //查询出库信息
        List<InOutStockDTO> members = baseMapper.listInventoryInOut(allList);

        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        if (ObjectUtil.isNotEmpty(entity.getBatchNo()) || soDeliveryNotice.equals(entity.getSourceType())) {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK.getCode());
        } else {
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_OUTSTOCK_USABLE.getCode());
        }

        for (InOutStockDTO member : members) {
            member.setSourceType(InventorySourceTypeEnum.SO_OUTSTOCK);
        }
        if (CollectionUtils.isNotEmpty(members)) {
            //扣实体仓库库存
            inventoryInOutStockDTO.setParamList(members);
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
        }
        //虚拟仓冻结库存扣减,(来源发货通知单且非中转)
        if (CollectionUtils.isNotEmpty(members) && CharSequenceUtil.isBlank(entity.getBatchNo()) && CollectionUtils.isNotEmpty(noticeList)) {
            //无虚拟仓无需扣减库存
            List<InOutStockDTO> virtualInOutStockList = members.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getVirtualWarehouseId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(virtualInOutStockList)) {
                VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
                List<VirtualInventoryStockDTO.OutInStockDTO> outInStockDTOS = BeanMapperUtils.copyList(VirtualInventoryStockDTO.OutInStockDTO.class, virtualInOutStockList);
                stockParamDTO.setParamList(outInStockDTOS);
                stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getCode());
                virtualInventoryTransCoreService.approve(stockParamDTO);
            }
        }
        //物流单添加
        saveLogisticsBill(entity);

    }

    //TODO 物流单
//    @Async("saveLogisticsBill")
    @Override
    public void saveLogisticsBill(SoOutstockEntity entity) {
            LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
            addDTO.setOutstockId(entity.getId());
            addDTO.setOutstockCode(entity.getCode());
            addDTO.setSourceCode(entity.getSoCode());
            String soId = entity.getSoId();
            addDTO.setSourceId(soId);
            String orderType = entity.getOrderType();
            String b2cType = OrderTypeEnum.B2C.getCode();
            addDTO.setOrderType(orderType);
            //平台订单号
            addDTO.setPlatformCode(entity.getSourceCode());
            //表明是是b2b
            if (!b2cType.equals(orderType)) {
                SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
                String salesPlatform = PlatformDictEnum.B2B_FOREIGN.getCode();
                if (Objects.nonNull(soInfo)) {
                    addDTO.setShopId(soInfo.getCustomerId());
                    addDTO.setShopName(soInfo.getCustomerName());
                    addDTO.setTelNumber(soInfo.getTelNumber());
                    addDTO.setOrderTime(soInfo.getCreateTime());
                    addDTO.setSalesPlatform(salesPlatform);
                    addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                    //国家id
                    String countryId = soInfo.getCountryId();
                    List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(countryId));
                    if (CollectionUtils.isNotEmpty(countryList)) {
                        addDTO.setToCountry(countryList.get(0).getNameCn());
                    } else {
                        addDTO.setToCountry("");
                    }
                    addDTO.setCurrency(soInfo.getCurrency());
                    addDTO.setChannelId(entity.getLogisticsChannelId());
                    addDTO.setTransportNo(entity.getTrackNo());
                    addDTO.setShipmentType(ShipmentTypeEnum.SELF_DELIVER.getCode());
                }
            } else {
                //表示是b2c
                if (ObjectUtil.isNotEmpty(soId)) {
                    SoB2cDTO.CustomerDTO customer = soB2cFeign.getB2cCustomerById(soId);
                    if (Objects.nonNull(customer)) {
                        addDTO.setShopId(customer.getShopId());
                        addDTO.setShopName(customer.getShopName());
                        addDTO.setTelNumber(customer.getTelNumber());
                        //国家
                        String country = customer.getCountry();
                        String countryName = "";
                        if (CharSequenceUtil.isNotBlank(country)) {
                            List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(country));
                            if (CollectionUtils.isNotEmpty(countryList)) {
                                countryName = countryList.get(0).getNameCn();
                            }
                        }
                        addDTO.setOrderTime(customer.getPayTime());
                        String dictPlatform = customer.getDictPlatform();
                        addDTO.setSalesPlatform(dictPlatform);
                        addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                        addDTO.setToCountry(countryName);
                        addDTO.setChannelId(customer.getLogisticsChannelId());
                        addDTO.setTransportNo(customer.getTransportNo());

                        String shipmentType = ShipmentTypeEnum.SELF_DELIVER.getCode();
                        //第三方仓
                        List<OverseasProviderWarehouseDTO.ViewDTO> warehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(Collections.singletonList(entity.getWarehouseId()));
                        if (CollectionUtils.isNotEmpty(warehouseList)) {
                            shipmentType = ShipmentTypeEnum.THIRD_WAREHOUSE_DELIVER.getCode();
                        }
                        if (customer.getHasPlatformWarehouseOrder()) {
                            shipmentType = ShipmentTypeEnum.PLATFORM_DELIVER.getCode();
                        }
                        addDTO.setShipmentType(shipmentType);
                    }
                }
            }
            LocalDateTime actualDeliveryDate = entity.getActualDeliveryDate();
            if (Objects.isNull(actualDeliveryDate)) {
                actualDeliveryDate = entity.getBillDate().atStartOfDay();
            }
            //发货时间
            addDTO.setDeliveryTime(actualDeliveryDate);
            //轨迹单号
            List<LogisticsBillDetailDTO.AddDTO> detailList = new ArrayList<>();
            String trackNo = entity.getTrackNo();
            if (CharSequenceUtil.isNotBlank(trackNo)){
                for (String s : trackNo.split(",")) {
                    LogisticsBillDetailDTO.AddDTO addDetailDTO = new LogisticsBillDetailDTO.AddDTO();
                    addDetailDTO.setTrackNo(s);
                    detailList.add(addDetailDTO);
                }
            }
            //非第三方仓和平台仓发货 则默认为自发货
            if (CharSequenceUtil.isBlank(addDTO.getShipmentType())){
                addDTO.setShipmentType(ShipmentTypeEnum.SELF_DELIVER.getCode());
            }
            addDTO.setDetailList(detailList);
            logisticsBillFeign.addLogisticsBill(addDTO);


    }


    /**
     * 处理反审核的数据
     *
     * @param list
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleDisApproveData(List<SoOutstockEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> idList = list.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
        //发货通知单
        String soDeliveryNotice = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        //发货通知单的
        List<SoOutstockEntity> noticeSoOutstockList = list.stream().filter(s -> s.getSourceType().equals(soDeliveryNotice)).
                collect(Collectors.toList());

        List<String> noticeIdList = noticeSoOutstockList.stream().map(SoOutstockEntity::getSourceId).collect(Collectors.toList());
        //发货通知集合
        List<SoDeliveryNoticeEntity> noticeList = CollectionUtils.isNotEmpty(noticeIdList) ? soDeliveryNoticeService.listByIds(noticeIdList) : Collections.emptyList();
        //发货通知单详情
        for (SoDeliveryNoticeEntity item : noticeList) {
            item.setDeliveryStatus(Boolean.FALSE);
        }
        //更改发货状态
        soDeliveryNoticeService.updateBatchById(noticeList);
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(idList);
        soOutstockDetailList = soOutstockDetailList.stream().filter(s -> CharSequenceUtil.isNotBlank(s.getSoDetailId())).collect(Collectors.toList());
        List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>(soOutstockDetailList.size());
        for (SoOutstockDetailEntity item : soOutstockDetailList) {
            SoDetailDTO.UpdateDeliveryStatusDTO paramDTO = new SoDetailDTO.UpdateDeliveryStatusDTO();
            paramDTO.setId(item.getSoDetailId());
            Integer actualQty = item.getActualQty();
            Integer deliveryQty = -actualQty;
            paramDTO.setDeliveryQty(deliveryQty);
            paramList.add(paramDTO);
        }

        soInfoFeign.updateDeliveryStatus(paramList);

        //删除物流单
        LogisticsBillDTO.RemoveDTO removeDTO = new LogisticsBillDTO.RemoveDTO();
        removeDTO.setOutstockIdList(idList);
        logisticsBillFeign.removeLogisticsBill(removeDTO);

    }


    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:10
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoOutstockEntity entity, Boolean isPushKingDee) {
        //下游单据【报关单】生成后不可操作反审核：报关单[单号]已生成不可操作反审核
        List<TmsDeclareBillEntity> tmsDeclareBillEntities = tmsDeclareBillFeign.listBySourceIds(Collections.singletonList(entity.getSoId()));
        if (CollUtil.isNotEmpty(tmsDeclareBillEntities)) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(), CharSequenceUtil.format(ApiError.TMS_DECLARE_BILL_EXISTS.msg,tmsDeclareBillEntities.get(0).getCode()));
        }

        //审核通过
        // 增加 出库单关联的自发货费用单据已确认状态下，不允许出库单反审核
        List<LogisticsBillCostDTO.OutStockDTO> outStockDTOS = logisticsBillFeign.listBillCostByOutstockIds(Collections.singletonList(entity.getId()));
        List<LogisticsBillCostDTO.OutStockDTO> outStockDTOList = outStockDTOS.stream().filter(e -> StringUtils.isNotEmpty(e.getReconciliationStatus())
                        && ReconciliationStatusEnum.CONFIRMED.getCode().equals(e.getReconciliationStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(outStockDTOList)){
            String code = outStockDTOList.stream().map(LogisticsBillCostDTO.OutStockDTO::getOutstockCode).distinct().collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_SO_OUTSTOCK_BILL_COST_NOT_DIS_APPROVE, code);
        }
        //待提交
        if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())){
            throw new ServiceException(ApiError.ERROR_98014);
        }
        Boolean result = this.updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.WAIT_SUBMIT, "", null);
        //反审核
        if (result) {
            //反审核
            InventoryBatchUnApproveDTO batchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, Collections.singletonList(entity.getId()));
            //扣虚拟仓库库存
            virtualInventoryTransCoreService.batchUnApprove(batchUnApproveDTO);
            //扣实体仓库存
            inventoryTransCoreService.batchUnApprove(batchUnApproveDTO);
//            List<SoOutstockEntity> haveSoIdList = list.stream().filter(h -> CharSequenceUtil.isNotBlank(h.getSoId())).collect(Collectors.toList());
            if (OrderTypeEnum.B2B.getCode().equalsIgnoreCase(entity.getOrderType())){
                handleDisApproveData(Collections.singletonList(entity));
            }
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.addModuleOperateLog(ingContent, ModuleTypeEnum.SO_OUT_STOCK.getCode(), entity.getId(), "状态变更");
            if (isPushKingDee) {
                //B2B发送金蝶
                sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
            }
            //订单推送dmp
            if (!SourceTypeEnum.SAL_OUTSTOCK.getCode().equals(entity.getSourceType())) {
                //订单推送dmp
                syncKingdeeSoOutstockService.syncOrderToDmp(entity, SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
            }
            //推送旺店通
            this.syncToWdt(entity,SyncOperateEnum.OPERATE_DISAPPROVE);

            //推送数帝云
            sdyFieldHandler(entity,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        }
        return BatchResultDTO.success(entity.getId(),entity.getCode(), "反审核成功");
    }

    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:13
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 撤销流程
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus), "", null);
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("销售出库单【%s】取消流程", ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "取消流程操作");
        return result;

    }


    /**
     * 删除销售出库单
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 12:16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoOutstockEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "删除");
            //推送数帝云
            list.forEach(req -> sdyFieldHandler(req,SyncOperateEnum.OPERATE_DELETE.getCode()));

            //删除明细
            soOutstockDetailService.removeByMainIdList(ids);

            //自动删除同批次的直接调拨单
            soOutstockService.deleteTransferInfo(list);

            //B2B发送金蝶
            sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        }
        return result;
    }

    /**
     * 删除直接调拨单
     * @author will
     * @date 2024/7/12 10:05
     * @param list
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void deleteTransferInfo(List<SoOutstockEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //批次号集合
        List<String> batchNoList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getBatchNo())).map(SoOutstockEntity::getBatchNo).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(batchNoList)) {
            return;
        }
        List<TransferInfoEntity> transferInfoList = transferInfoService.listByBatchNoList(batchNoList);
        if (CollectionUtils.isEmpty(transferInfoList)) {
            return;
        }
        List<String> transferIdList = transferInfoList.stream().map(TransferInfoEntity::getId).collect(Collectors.toList());
        transferInfoList.forEach(transferInfoEntity -> {
            BatchResultDTO resultDTO = transferInfoService.disApprove(transferInfoEntity, Boolean.FALSE, Boolean.FALSE);
            if (!resultDTO.getSuccess()){
                throw new ServiceException("直接调拨单反审核失败");
            }
        });
        Boolean isDelete = transferInfoService.delete(transferIdList);
        if (!isDelete) {
            throw new ServiceException("直接调拨单删除失败");
        }
    }

    @Override
    public PagingVO<SoOutstockDTO.PagingViewDTO> exportSoOutStock(PagingDTO<SoOutstockDTO.ExportDTO> dto) {
        //获取导出数据
		Page<SoOutstockDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        fillPaging(page.getRecords(),true);
        return new PagingVO<>(page);
    }

    /**
     * 作废
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-19 14:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoOutstockEntity> list = this.listByIds(ids);

        //查询是否冻结
        List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(waitSubmitStatus);
        statusList.add(rejectStatus);
        long invalidCount = list.stream().filter(d -> !d.getInvalidStatus()).count();
        if (invalidCount != list.size()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        lambdaUpdate().in(SoOutstockEntity::getId, ids).
                set(SoOutstockEntity::getInvalidStatus, Boolean.TRUE).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售出库单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), pairList, "作废");

        //自动删除同批次的直接调拨单
        soOutstockService.deleteTransferInfo(list);

        //B2B发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        return Boolean.TRUE;

    }


    /**
     * 获取tab
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.TabListDTO>
     * @author yl
     * @date 2023-05-19 14:23
     */
    @Override
    public List<SoOutstockDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<SoOutstockDTO.TabListDTO> resultList = new ArrayList<>(4);
        List<SoOutstockDTO.ApproveCountDTO> approveCountList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = approveCountList.stream().mapToInt(SoOutstockDTO.ApproveCountDTO::getCount).sum();
        SoOutstockDTO.TabListDTO all = new SoOutstockDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(SearchType.ALL);
        resultList.add(all);

        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        SoOutstockDTO.TabListDTO waitSubmit = new SoOutstockDTO.TabListDTO();
        int waitSubmitCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(waitSubmitStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitSubmit.setCount(waitSubmitCount);
        waitSubmit.setSearchType(SearchType.WAIT_SUBMIT);
        resultList.add(waitSubmit);

        //待审核
        String ing = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoOutstockDTO.TabListDTO waitApprove = new SoOutstockDTO.TabListDTO();
        int waitApproveCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(ing)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        waitApprove.setSearchType(SearchType.WAIT_APPROVE);
        resultList.add(waitApprove);

        //已审核
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        SoOutstockDTO.TabListDTO approve = new SoOutstockDTO.TabListDTO();
        int approveCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(approveStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        approve.setCount(approveCount);
        approve.setSearchType(approveStatus);
        resultList.add(approve);
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoOutstockDTO.TabListDTO reject = new SoOutstockDTO.TabListDTO();
        int rejectCount = approveCountList.stream().filter(a -> a.getInvalidStatus().equals(Boolean.FALSE) && a.getApproveStatus().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        reject.setSearchType(rejectStatus);
        resultList.add(reject);
        return resultList;


    }

    /**
     * 分页列表
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-22 8:56
     */
    @Override
    public PagingVO<SoOutstockDTO.PagingViewDTO> paging(PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        SoOutstockDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize() , dto.getIsSearchCount());
        IPage pageData = baseMapper.paging(query, params);
        List<SoOutstockDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //处理分页数据
        fillPaging(list,false);
        return new PagingVO<>(pageData);
    }

    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPaging(List<SoOutstockDTO.PagingViewDTO> list,Boolean isExport) {
        //sku id
        List<String> skuIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //部门id集合
        List<String> deptIdList = list.stream().map(SoOutstockDTO.PagingViewDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(deptIdList);
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        List<SoB2cEntity> soB2cEntities= Lists.newArrayList();
        if(Objects.nonNull(isExport) && isExport){
            //查询是否有拦截单
            List<String> soIds = list.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
            soB2cEntities = soB2cFeign.listByIds(soIds);
        }
        //销售平台字典表数据
        Map<String, String> salesPlatformMap = new HashMap<>();
        List<DictBasicEntity> salesPlatformList = FeignQuery.create(DictBasicEntity.class)
                .eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType())
                .eq(DictBasicEntity::getStatus, Boolean.TRUE)
                .eq(DictBasicEntity::getIsDeleted, Boolean.FALSE)
                .list();
        if(CollectionUtils.isNotEmpty(salesPlatformList)){
            salesPlatformMap = salesPlatformList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName));
        }
        String b2c = OrderTypeEnum.B2C.getCode();
//        List<String> ids = list.stream().map(SoOutstockDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //跟踪单号
//        Map<String,List<String>> trackNoMAp = logisticsBillFeign.mapTrackNoAndSoOutId(ids);
        for (SoOutstockDTO.PagingViewDTO item : list) {
            //设置跟踪单号
//            if(trackNoMAp.containsKey(item.getId())){
//                item.setTrackNo(trackNoMAp.get(item.getId()));
//            }
            if (StringUtils.isNotEmpty(item.getTrackNos())){
                String[] split = item.getTrackNos().split(",");
                item.setTrackNo(Arrays.stream(split).collect(Collectors.toList()));
            }
            //设置拦截标识
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(item.getSoId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                item.setIsIntercept(soB2cEntity.getIsIntercept());
            }

            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String soId = item.getSoId();
            //部门id
            String salesDeptId = item.getSalesDeptId();
            String salesDeptName = deptList.stream().filter(d -> d.getId().equals(salesDeptId)).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(salesDeptName);
            String orderType = item.getOrderType();
            Boolean isB2c = b2c.equals(orderType);

            //国家名称
            String countryName = countryList.stream().filter(obj -> obj.getId().equals(item.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            item.setCountryName(countryName);
            String orderTypeName = OrderTypeEnum.getName(orderType);
            item.setOrderTypeName(orderTypeName);
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            item.setProductName(sku.getSkuName());
            item.setUnit(sku.getUnitName());

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal price = item.getPrice();
            item.setPrice(price);
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }
            //销售单价(本位币)
            item.setCnyPrice(MathUtil.multiply(price, exchangeRate,4));

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax,4);
            item.setTaxPrice(taxPrice);
            //含税单价(本位币)
            item.setCnyTaxPrice(MathUtil.multiply(taxPrice, exchangeRate,4));
            item.setCurrency(item.getCurrency());
            item.setCurrencySymbol(item.getCurrencySymbol());
            item.setAllAmountLocalCurrency(item.getAllAmountLocalCurrency());

            //装箱状态
            item.setPackingStatusName(PackingTaskStatusEnum.getName(item.getPackingStatus()));
            //销售平台名称
            item.setDictPlatformName(salesPlatformMap.get(item.getDictPlatform()));
        }
    }


    /**
     * 导出销售出库单
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-22 11:41
     */
    @Override
    public Boolean exportExcel(SoOutstockDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售订单出库列表", EXPORT_WMS_SO_OUT_STOCK.getCode(), dto);
        return Boolean.TRUE;
    }


    /**
     * 修改
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-22 18:00
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public String updateSoOutstock(SoOutstockDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoOutstockEntity soOutstock = this.getById(id);
        if (Objects.isNull(soOutstock)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        List<SoOutstockDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        String sourceType = soOutstock.getSourceType();
        if (CharSequenceUtil.isBlank(sourceType)) {
            sourceType = SourceTypeEnum.SELF_ADD.getCode();
        }
        String orderType = soOutstock.getOrderType();
        String b2c = OrderTypeEnum.B2C.getCode();
        Boolean isB2c = b2c.equals(orderType);
        if(isB2c){
           throw new ServiceException("B2C订单不允许修改出库单");
        }
        List<SoDetailEntity> soDetailList = Collections.emptyList();
        if (!isB2c) {
            //销售订单详情集合
            soDetailList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(dto.getSoId()));
            if (CollectionUtils.isEmpty(soDetailList)) {
                throw new ServiceException("销售订单详情不存在");
            }
            //检查出库数量
//            soOutstockDetailService.checkOutQty(dto.getWarehouseId(), dto.getSoId(), dto.getSourceId(), sourceType, detailList, null);
        } else {
            soOutstockDetailService.checkB2cOrderQty(dto.getWarehouseId(), dto.getSoId(), dto.getSourceId(), sourceType, detailList);
        }
        //待提交状态
        String tradeLabel = dto.getTradeLabel();
        String oldTradeLabel = soOutstock.getTradeLabel();
        if(!tradeLabel.equals(oldTradeLabel)){
            if(!soOutstock.getApproveStatus().getCode().equals(ApproveStatusEnum.WAIT_SUBMIT.getCode())){
                throw new ServiceException("订单标签只有提交状态下可编辑");
            }
        }

        String code = soOutstock.getCode();
        LocalDate billDate = dto.getBillDate();
        //旧的
        SoOutstockEntity old = new SoOutstockEntity();
        BeanMapper.copy(soOutstock, old);

        String soId = dto.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoFeign.getSoBaseById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }

        //销售订单的总金额
        BigDecimal soAmount = BigDecimal.ZERO;
        for (SoDetailEntity soDetail : soDetailList) {
            BigDecimal taxAmountBefore = soDetail.getTaxAmountBefore();
            soAmount = soAmount.add(Objects.isNull(taxAmountBefore) ? BigDecimal.ZERO : taxAmountBefore);
        }
        //出库金额
        BigDecimal outStockAmount = BigDecimal.ZERO;
        for (SoOutstockDetailDTO.UpdateDTO item : detailList) {
            //sku id
            String skuId = item.getSkuId();
            //实发数量
            Integer actualQty = item.getActualQty();

            String soDetailId = item.getSoDetailId();

            BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getPrice).orElse(BigDecimal.ZERO);
            //税率
            BigDecimal taxRate = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                    findFirst().map(SoDetailEntity::getTaxRate).orElse(BigDecimal.ZERO);

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);
            outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
        }
        //销售订单折扣额
        BigDecimal discountAmount = soInfo.getDiscountAmount();
        //折扣总额占比
        BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
        //整单折扣额
        BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);

        BeanMapper.copy(dto, soOutstock);
        soOutstock.setCode(code);
        soOutstock.setTotalDiscountAmount(totalDiscountAmount);
        // 出库日期
        soOutstock.setBillDate(billDate);
        handleSaveOrUpdateDbByB2b(soOutstock,soInfo);
        Boolean updateResult = this.updateById(soOutstock);
        if (updateResult) {
            //更新TMS物流跟踪号
            LogisticsBillDTO.BatchUpdateTrackNoDTO batchUpdateTrackNoDTO = new LogisticsBillDTO.BatchUpdateTrackNoDTO();
            batchUpdateTrackNoDTO.setSoOutstockEntity(soOutstock);
            batchUpdateTrackNoDTO.setLogisticsChannelId(soOutstock.getLogisticsChannelId());
            batchUpdateTrackNoDTO.setTrackNoList(dto.getTrackNoList());
            List<BatchResultDTO> batchResultDTOList = logisticsBillFeign.updateBatchTrackNo(Collections.singletonList(batchUpdateTrackNoDTO),false);
            if(CollectionUtils.isNotEmpty(batchResultDTOList) && !batchResultDTOList.get(0).getSuccess()){
                throw new ServiceException("更新跟踪单号失败:"+batchResultDTOList.get(0).getMsg());
            }
            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, soOutstock, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "", "");
            soOutstockDetailService.updateDetail(id, detailList);
            return id;
        }
        return "";
    }


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-22 19:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(SoOutstockDTO.UpdateDTO dto) {
        String id = this.updateSoOutstock(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(id));
    }


    /**
     * 销售出库单保存下推单据
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-23 14:30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addB2bPushDownNo(List<SoOutstockDTO.GenerateSoOutstockViewDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<String> soIdList = list.stream().map(SoOutstockDTO.GenerateSoOutstockViewDTO::getSoId).collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(soIdList);
        if (CollectionUtils.isEmpty(soInfoList)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        List<SoInfoDTO.CustomerDTO> customerDTOS = soInfoFeign.listSoCustomer(soIdList);
        Map<String, List<SoOutstockDTO.GenerateSoOutstockViewDTO>> map = list.stream().collect(Collectors.groupingBy(SoOutstockDTO.GenerateSoOutstockViewDTO::getSourceId));
        List<SoOutstockDTO.AddDTO> addList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<SoOutstockDTO.GenerateSoOutstockViewDTO>> entry : map.entrySet()) {
            //来源id
            List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateInfoList = entry.getValue();
            SoOutstockDTO.GenerateSoOutstockViewDTO generateInfo = generateInfoList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getSourceCode())).findFirst().orElse(null);
            if (generateInfo != null) {
                SoInfoEntity soInfo = soInfoList.stream().filter(v -> v.getId().equals(generateInfo.getSoId())).findFirst().orElse(new SoInfoEntity());
                SoInfoDTO.CustomerDTO customerDTO = customerDTOS.stream().filter(v -> v.getCustomerId().equals(soInfo.getCustomerId())).findFirst().orElse(new SoInfoDTO.CustomerDTO());
                //是否中转
                CfgRuleOutDTO.MatchTransferRuleDTO ruleDTO = new CfgRuleOutDTO.MatchTransferRuleDTO();
                ruleDTO.setType(StockOutTransferTypeEnum.B2B.getCode());
                ruleDTO.setReceiveCountry(customerDTO.getCountryId());
                ruleDTO.setFromWarehouse(generateInfo.getWarehouseId());
                ruleDTO.setSalesOrgId(soInfo.getSalesOrgId());
                CfgRuleOutDTO.MatchTransferResultDTO resultDTO = cfgRuleOutService.matchTransferRule(ruleDTO);
                String warehouseId;
                String batchNo = "";
                if (Boolean.TRUE.equals(resultDTO.getIsTransit())) {
                    batchNo = IdUtil.getSnowflake().nextIdStr();
                    generateTransferInfo(generateInfo, batchNo, generateInfoList, resultDTO.getTransferWarehouseIdList());
                    warehouseId = resultDTO.getTransferWarehouseIdList().get(resultDTO.getTransferWarehouseIdList().size() - 1);
                }else {
                    warehouseId = generateInfo.getWarehouseId();
                }
                SoOutstockDTO.AddDTO add = new SoOutstockDTO.AddDTO();
                //客户订单号
                String customerOrderNo = soInfoList.stream().filter(obj -> obj.getId().equals(generateInfo.getSoId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getCustomerOrderNo())).orElse("");

                add.setSoId(generateInfo.getSoId());
                add.setSourceId(generateInfo.getSourceId());
                add.setSourceCode(generateInfo.getSourceCode());
                add.setSourceType(generateInfo.getSourceType());
                add.setCarrierId(generateInfo.getCarrierId());
                add.setPlanDeliveryDate(generateInfo.getPlanDeliveryDate());
                add.setWarehouseId(warehouseId);
                add.setTrackNo(generateInfo.getTrackNo());
                add.setSellerId(generateInfo.getSellerId());
                add.setCustomerOrderNo(customerOrderNo);
                add.setBatchNo(batchNo);
                add.setBillDate(generateInfo.getBillDate());
                List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (SoOutstockDTO.GenerateSoOutstockViewDTO item : generateInfoList) {
                    SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
                    detail.setSoDetailId(item.getSoDetailId());
                    detail.setSourceDetailId(item.getSourceDetailId());
                    detail.setSkuId(item.getSkuId());
                    detail.setSkuNo(item.getSkuNo());
                    detail.setRemark(item.getRemark());
                    detail.setWarehouseId(warehouseId);
                    if (Boolean.TRUE.equals(resultDTO.getIsTransit())) {
                        detail.setWarehouseLocation("");
                    }else {
                        detail.setWarehouseLocation(item.getWarehouseLocation());
                    }
                    detail.setActualQty(item.getQty());
                    detail.setPlanQty(item.getQty());
                    detail.setAttachNameList(item.getAttachNameList());
                    detail.setAttachUrlList(item.getAttachUrlList());
                    detailList.add(detail);
                }
                add.setDetailList(detailList);
                addList.add(add);
            }

        }
        return this.batchAdd(addList);
    }

    private void generateTransferInfo(SoOutstockDTO.GenerateSoOutstockViewDTO dto, String batchNo, List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateInfoList, List<String> transferWarehouseIdList) {

        //订单调出仓和第一个中转仓一致时从第二个中转仓开始
        boolean firstWarehouseSame = transferWarehouseIdList.get(0).equals(dto.getWarehouseId());
        for (int i = 0; i < transferWarehouseIdList.size(); i++) {
            if (firstWarehouseSame && 0 == i){
                continue;//跳过第一个仓库 从第二个开始
            }
            if (0 == i || firstWarehouseSame){
                addTransferOrder(Boolean.TRUE, dto.getWarehouseId(),transferWarehouseIdList.get(i), dto, generateInfoList, batchNo,i);
            }else {
                addTransferOrder(Boolean.FALSE, transferWarehouseIdList.get(i - 1),transferWarehouseIdList.get(i), dto, generateInfoList, batchNo, i);
            }
        }
    }

    private void addTransferOrder(Boolean isFirst, String fromWarehouseId, String toWarehouseId, SoOutstockDTO.GenerateSoOutstockViewDTO dto, List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateInfoList, String batchNo, int i) {
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(Arrays.asList(fromWarehouseId, toWarehouseId));
        //获取仓库信息
        if (CollectionUtils.isEmpty(warehouseEntityList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调出仓库
        WarehouseEntity fromWarehouseEntity = warehouseEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), fromWarehouseId)).findFirst().orElse(null);
        if (Objects.isNull(fromWarehouseEntity)) {
            throw new ServiceException("调出仓库不能为空");
        }
        //调入仓库
        WarehouseEntity toWarehouseEntity = warehouseEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), toWarehouseId)).findFirst().orElse(null);
        if (Objects.isNull(toWarehouseEntity)) {
            throw new ServiceException("调入仓库不能为空");
        }

        TransferInfoDTO.AddDTO transferDto = new TransferInfoDTO.AddDTO();
        transferDto.setType(TransferTypeEnum.CROSS_ORG.getCode());
        transferDto.setBillDate(LocalDate.now());
        transferDto.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        transferDto.setInOrgId(toWarehouseEntity.getOrgId());
        transferDto.setOutOrgId(fromWarehouseEntity.getOrgId());
        transferDto.setSourceId(dto.getSourceId());
        transferDto.setSourceCode(dto.getSourceCode());
        if (Objects.equals(SourceTypeEnum.SO_INFO.getCode(), dto.getSourceType())){
            if (isFirst){
                transferDto.setSourceType(dto.getSourceType());
            }else {
                transferDto.setSourceType(SourceTypeEnum.SO_INFO_TRANSFER_INFP.getCode());
            }
        }else {
            transferDto.setSourceType(dto.getSourceType());
        }
        transferDto.setBatchNo(batchNo);
        transferDto.setIndex(i);
        List<TransferInfoDetailDTO.AddDTO> detailList = getAddDTOS(generateInfoList, fromWarehouseId, toWarehouseId,isFirst);
        transferDto.setDetailList(detailList);
        transferInfoService.addAndApprove(transferDto);
    }

    private static List<TransferInfoDetailDTO.AddDTO> getAddDTOS(List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateInfoList, String fromWarehouseId, String toWarehouseId,Boolean isFirst) {
        List<TransferInfoDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (SoOutstockDTO.GenerateSoOutstockViewDTO viewDTO : generateInfoList) {
            TransferInfoDetailDTO.AddDTO transferInfoDetail = new TransferInfoDetailDTO.AddDTO();
            transferInfoDetail.setSkuId(viewDTO.getSkuId());
            transferInfoDetail.setSkuNo(viewDTO.getSkuNo());
            transferInfoDetail.setQty(viewDTO.getQty());
            if (isFirst){
                transferInfoDetail.setOutWarehouseLocation(viewDTO.getWarehouseLocation());
            }else {
                transferInfoDetail.setOutWarehouseLocation("");
            }
            transferInfoDetail.setOutWarehouseId(fromWarehouseId);
            transferInfoDetail.setInWarehouseId(toWarehouseId);
            transferInfoDetail.setSourceDetailId(viewDTO.getSourceDetailId());
            detailList.add(transferInfoDetail);
        }
        return detailList;
    }

    /**
     * 销售订单获取销售出库单的数据
     *
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.SoRefDTO>
     * @author yl
     * @date 2023-05-23 18:37
     */
    @Override
    public List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(String soId) {
        SoInfoDTO.CustomerDTO soCustomer = soInfoFeign.getSoBaseById(soId);
        List<SoOutstockDTO.SoRefDTO> resultList = baseMapper.listSoRefSoOutstockBySoId(soId);
        List<String> skuIdList = resultList.stream().map(SoOutstockDTO.SoRefDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        for (SoOutstockDTO.SoRefDTO item : resultList) {
            String skuId = item.getSkuId();
            LocalDateTime actualDeliveryDate = item.getActualDeliveryDate();
            item.setOutStockDate(actualDeliveryDate);
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            item.setOrderType(soCustomer.getOrderType());
            item.setOrderTypeName(soCustomer.getOrderTypeName());
            item.setSalesOrgName(soCustomer.getSalesOrgName());
            item.setCustomerId(soCustomer.getCustomerId());
            item.setCustomerName(soCustomer.getCustomerName());
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            String productName = "";
            String unit = "";
            if (sku != null) {
                productName = sku.getSkuName();
                unit = sku.getUnitName();
            }
            item.setProductName(productName);
            item.setUnit(unit);
        }
        return resultList;
    }


    /**
     * 保存销售订单下推销售出库单
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-25 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoSave(ValidList<SoInfoDTO.GenerateDeliveryView> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<String> soIdList = list.stream().map(SoInfoDTO.GenerateDeliveryView::getSoId).collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoFeign.listSoInfoByIds(soIdList);
        if (CollectionUtils.isEmpty(soInfoList)) {
            log.info("销售订单不存在，soIdList = {}", soInfoList);
            throw new ServiceException(ApiError.ERROR_92016);
        }

        Map<String, List<SoInfoDTO.GenerateDeliveryView>> map = list.stream().collect(Collectors.groupingBy(SoInfoDTO.GenerateDeliveryView::getSoId));
        List<SoOutstockDTO.AddDTO> addList = new ArrayList<>(map.size());
        String sourceType = SourceTypeEnum.SO_INFO.getCode();
        for (Map.Entry<String, List<SoInfoDTO.GenerateDeliveryView>> entry : map.entrySet()) {
            //来源id
            String soId = entry.getKey();
            List<SoInfoDTO.GenerateDeliveryView> generateInfoList = entry.getValue();

            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> obj.getId().equals(soId)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soInfoEntity)) {
                log.info("销售订单不存在，soId = {}", soId);
                throw new ServiceException(ApiError.ERROR_92016);
            }
            SoInfoDTO.GenerateDeliveryView generateInfo = generateInfoList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getSoId())).findFirst().orElse(null);
            if (generateInfo != null) {
                SoOutstockDTO.AddDTO add = new SoOutstockDTO.AddDTO();
                add.setSoId(soId);
                add.setSourceId(soId);
                add.setSourceCode(generateInfo.getSoCode());
                add.setSourceType(sourceType);
                add.setPlanDeliveryDate(generateInfo.getPlanDeliveryDate());
                add.setWarehouseId(generateInfo.getWarehouseId());
                add.setCustomerOrderNo(soInfoEntity.getCustomerOrderNo());
                List<SoOutstockDetailDTO.AddDTO> detailList = new ArrayList<>(generateInfoList.size());
                for (SoInfoDTO.GenerateDeliveryView item : generateInfoList) {
                    SoOutstockDetailDTO.AddDTO detail = new SoOutstockDetailDTO.AddDTO();
                    detail.setSourceDetailId(item.getDetailId());
                    detail.setSkuId(item.getSkuId());
                    detail.setRemark(item.getRemark());
                    detail.setActualQty(item.getDeliveryQty());
                    detail.setPlanQty(item.getDeliveryQty());
                    detail.setAttachNameList(item.getAttachmentNameList());
                    detail.setAttachUrlList(item.getAttachmentUrlList());
                    detail.setWarehouseLocation("");
                    detailList.add(detail);
                }
                add.setDetailList(detailList);
                addList.add(add);
            }
        }
        return this.batchAdd(addList);
    }

    /**
     * @return
     * @parms
     * @author yl
     * @date
     */
    @Override
    public Integer getPushDownCountBySoIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return 0;
        }
        Integer count = this.lambdaQuery().in(SoOutstockEntity::getSoId, soIds).
                eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE).
                count();
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAdd(List<SoOutstockDTO.AddDTO> addList) {
        if (CollectionUtils.isEmpty(addList)) {
            return Boolean.FALSE;
        }
        addList.forEach(this::add);
        return Boolean.TRUE;
    }

    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return
     */
    private Boolean updateApproveStatus(List<SoOutstockEntity> list, ApproveStatusEnum statusEnum, String approveUserName, LocalDateTime approveTime) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoOutstockEntity item : list) {
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
                item.setApproveTime(approveTime);
                if (approveTime != null) {
                    item.setActualDeliveryDate(approveTime);
                }
            }
            return this.updateBatchById(list);
        }
        return true;
    }

    /**
     * 修改金蝶同步状态
     *
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     **/
    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(SoOutstockEntity::getId, id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), SoOutstockEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }


    /**
     * 根据code 获取到销售出库单信息
     *
     * @param code
     * @return com.erp.model.wms.entity.SoOutstockEntity
     * @author yl
     * @date 2023-06-28 10:17
     */
    @Override
    public String getByCode(String code) {
        LambdaQueryWrapper<SoOutstockEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoOutstockEntity::getCode, code);
        queryWrapper.last("LIMIT 1");
        SoOutstockEntity entity = this.getOne(queryWrapper);
        if (!Objects.isNull(entity)) {
            String id = entity.getId();
            return id;
        }
        return "";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> pagingUpdate(List<SoOutstockDTO.PagingUpdateDTO> dtoList) {
        List<SoOutstockEntity> list = this.listByIds(dtoList.stream().map(SoOutstockDTO.PagingUpdateDTO::getId).collect(Collectors.toList()));
        if (ObjectUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99058);
        }
        List<String> channelIds = dtoList.stream().map(SoOutstockDTO.PagingUpdateDTO::getLogisticsChannelId).collect(Collectors.toList());
        //物流供应商信息
        List<LogisticsChannelDTO.BaseDTO> logisticsInfoList = logisticsFeign.listChannelInfoById(channelIds);
        List<LogisticsBillDTO.BatchUpdateTrackNoDTO> batchUpdateTrackNoDTOList = new ArrayList<>();
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<SoOutstockEntity> updateList = new ArrayList<>();

        for(SoOutstockDTO.PagingUpdateDTO pagingUpdateDTO : dtoList){
            SoOutstockEntity soOutstock = list.stream().filter(v->v.getId().equals(pagingUpdateDTO.getId())).findFirst().orElse(null);
            if(Objects.isNull(soOutstock)){
                BatchResultDTO batchResultDTO = BatchResultDTO.fail(pagingUpdateDTO.getId(),pagingUpdateDTO.getId(),ApiError.ERROR_99058.msg);
                batchResultDTOList.add(batchResultDTO);
            }else{
                //2024.09.11 jack 旺店通的销售出库单，不允许操作更新物流渠道字段，提示：第三方平台单据不允许修改
                String createUserName = soOutstock.getCreateUserName();
                String qimen = PlatformDictEnum.QI_MEN.getCode();
                boolean isQimen = createUserName.equals(qimen);
                if(isQimen){
                    BatchResultDTO batchResultDTO = BatchResultDTO.fail(soOutstock.getId(),soOutstock.getCode(),ApiError.ERROR_99142.msg);
                    batchResultDTOList.add(batchResultDTO);
                    continue;
                }
                LogisticsBillDTO.BatchUpdateTrackNoDTO batchUpdateTrackNoDTO = new LogisticsBillDTO.BatchUpdateTrackNoDTO();
                batchUpdateTrackNoDTO.setTrackNoList(pagingUpdateDTO.getTrackNoList());
                batchUpdateTrackNoDTO.setSoOutstockEntity(soOutstock);
                batchUpdateTrackNoDTO.setLogisticsChannelId(pagingUpdateDTO.getLogisticsChannelId());
                batchUpdateTrackNoDTOList.add(batchUpdateTrackNoDTO);
                LogisticsChannelDTO.BaseDTO logisticsInfo = logisticsInfoList.stream().filter(v->v.getId().equals(pagingUpdateDTO.getLogisticsChannelId())).findFirst().orElse(null);
                if(Objects.nonNull(logisticsInfo)){
                    soOutstock.setCarrierId(logisticsInfo.getSupplierId());
                    //记录 运输单号和渠道信息
                    if (CollectionUtils.isNotEmpty(pagingUpdateDTO.getTrackNoList())){
                        soOutstock.setTrackNo(String.join(",", pagingUpdateDTO.getTrackNoList()));
                    }
                    soOutstock.setLogisticsChannelId(pagingUpdateDTO.getLogisticsChannelId());
                    //2024.09.11 jack 销售出库单增加物流渠道名称logisticsChannelName
                    soOutstock.setLogisticsChannelName(logisticsInfo.getName());
                    updateList.add(soOutstock);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(batchUpdateTrackNoDTOList)){
            batchResultDTOList.addAll(logisticsBillFeign.updateBatchTrackNo(batchUpdateTrackNoDTOList,false));
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            boolean update = this.updateBatchById(updateList);
            //只批量同步更新审核通过的销售出库单
            updateList = updateList.stream().filter(v -> v.getApproveStatus().getCode().equalsIgnoreCase(ApproveStatusEnum.APPROVE.getCode())).collect(Collectors.toList());
            if(update && CollectionUtils.isNotEmpty(updateList)){
                //推送金蝶同步任务
                sendPushTask(updateList,SyncOperateEnum.OPERATE_APPROVE.getCode());
            }
        }
        return batchResultDTOList;
    }

    @Override
    public List<SoOutstockDTO.PrintDTO> print(List<String> ids) {
        List<SoOutstockDTO.PrintDTO> printDTOList = new ArrayList<>();
        List<SoOutstockEntity> soOutstockEntities = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soOutstockEntities)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //获取客户id集合
        List<String> customerIds = soOutstockEntities.stream().map(SoOutstockEntity::getCustomerId).distinct().collect(Collectors.toList());
        //根据客户id集合查询客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomerByIds(customerIds);
        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(ids);
        //获取销售单id集合
        List<String> soList = soOutstockEntities.stream().map(SoOutstockEntity::getSoId).collect(Collectors.toList());
        //获取销售单集合
        List<SoInfoEntity> soInfoEntities = soInfoFeign.listSoInfoByIds(soList);
        //获取sku的id集合
        List<String> skuIdList = soOutstockDetailEntities.stream().map(SoOutstockDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //根据skuId查询sku信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //获取销售单详情id
        List<String> SoDeliveryNoticeDetailIds = soOutstockDetailEntities.stream().map(SoOutstockDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listByIds(SoDeliveryNoticeDetailIds);
        List<String> soDetailIds = noticeDetailEntities.stream().map(SoDeliveryNoticeDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIds);
        List<String> receiveAddressId = soInfoEntities.stream().map(SoInfoEntity::getReceiveAddressId).collect(Collectors.toList());
        List<CustomerAddressEntity> customerAddressEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(receiveAddressId)) {
            customerAddressEntities.addAll(customerFeign.listCustomerAddressByIds(receiveAddressId));
        }
        for (SoOutstockEntity soOutstockEntity : soOutstockEntities) {
            //根据客户id获取客户信息
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soOutstockEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            //根据销售单id获取销售单信息
            SoInfoEntity soInfoEntity = soInfoEntities.stream().filter(req -> req.getId().equals(soOutstockEntity.getSoId())).findFirst().orElse(new SoInfoEntity());
            SoOutstockDTO.PrintDTO printDTO = new SoOutstockDTO.PrintDTO();
            printDTO.setCustomerName(customerInfoEntity.getName());
            printDTO.setSellerName(soInfoEntity.getSellerName());
            CustomerAddressEntity customerAddressEntity = customerAddressEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getReceiveAddressId())).findFirst().orElse(new CustomerAddressEntity());
            printDTO.setReceiveAddress(customerAddressEntity.getAddress());
            printDTO.setTelNumber(soInfoEntity.getTelNumber());
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailEntities.stream().filter(req -> req.getMainId().equals(soOutstockEntity.getId())).collect(Collectors.toList());
            printDTO.setSumNumber(soOutstockDetailEntityList.stream().mapToInt(SoOutstockDetailEntity::getActualQty).sum());
            soOutstockDetailEntityList.sort(Comparator.comparing(SoOutstockDetailEntity::getId));
            List<SoOutstockDTO.PrintDetailDTO> printDetailDTOList = new ArrayList<>();
            for (SoOutstockDetailEntity soOutstockDetailEntity : soOutstockDetailEntityList) {
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDeliveryNoticeDetailEntity());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(req -> req.getId().equals(soDeliveryNoticeDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                SoOutstockDTO.PrintDetailDTO printDetailDTO = new SoOutstockDTO.PrintDetailDTO();
                printDetailDTO.setPlatformSkuNo(soDetailEntity.getPlatformSkuNo());
                printDetailDTO.setProductSkuNo(soOutstockDetailEntity.getSkuNo());
                SkuVO skuVO = skuList.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                printDetailDTO.setProductName(skuVO.getSkuName());
                printDetailDTO.setRemark(soOutstockDetailEntity.getRemark());
                printDetailDTO.setQty(soOutstockDetailEntity.getActualQty());
                printDetailDTOList.add(printDetailDTO);
            }

            printDTO.setPrintDetailList(printDetailDTOList);
            printDTOList.add(printDTO);
        }
        return printDTOList;
    }

    @Override
    public List<String> getIdsByTemp(String tableName) {
        return baseMapper.getIdsByTemp(tableName);
    }


    /**
     * 金蝶同步到系统
     *
     * @param soOutstock 销售出库单
     * @param detailList 销售出库详情
     * @param flagId     已存在的flagId
     * @return void
     * @author yl
     * @date 2023-07-21 14:44
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void handleKingdeeToErp(SoOutstockEntity soOutstock, List<SoOutstockDetailEntity> detailList, String flagId) {
        if (CharSequenceUtil.isNotBlank(flagId)) {
            //回滚库存
            InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, Collections.singletonList(flagId));
           //回滚虚拟库存
            virtualInventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

            inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
            this.removeById(flagId);
            soOutstockDetailService.removeByMainIdList(Collections.singletonList(flagId));
        }

        //保存销售出库单
        this.save(soOutstock);
        //保存销售出库单详情
        soOutstockDetailService.saveBatch(detailList);
    }

    /**
     * 金蝶同步到系统，不单独事务
     *
     * @param soOutstock 销售出库单
     * @param detailList 销售出库详情
     * @param flagId     已存在的flagId
     * @return void
     * @author yl
     * @date 2023-07-21 14:44
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNewKingdeeToErp(SoOutstockEntity soOutstock, List<SoOutstockDetailEntity> detailList, String flagId) {
    	if (CharSequenceUtil.isNotBlank(flagId)) {
    		//回滚库存
    		InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_OUTSTOCK, Collections.singletonList(flagId));
    		//回滚虚拟库存
    		virtualInventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

    		inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
    		this.removeById(flagId);
    		soOutstockDetailService.removeByMainIdList(Collections.singletonList(flagId));
    	}

    	//保存销售出库单
    	this.save(soOutstock);
    	//保存销售出库单详情
    	soOutstockDetailService.saveBatch(detailList);
    }


    @Override
    public PagingVO<SoOutstockDTO.PdaPagingViewDTO> pdaPaging(PagingDTO<SoOutstockDTO.PdaPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        SoOutstockDTO.PdaPagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDateTime> dateList = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setActualDeliveryDateList(dateList);
        }
        IPage<SoOutstockDTO.PdaPagingViewDTO> pageData = this.baseMapper.pdaPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<SoOutstockDTO.PdaPagingViewDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(ids);
        for (SoOutstockDTO.PdaPagingViewDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<SoOutstockDetailEntity> detailEntities = soOutstockDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<SoOutstockDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, SoOutstockDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoOutstockDTO.PdaCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<SoOutstockDTO.PdaCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            SoOutstockDTO.PagingParamDTO pagingParamDTO = new SoOutstockDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            SoOutstockDTO.PdaCountDTO resultDTO = new SoOutstockDTO.PdaCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String pdaAdd(SoOutstockDTO.AddDTO dto) {
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainId(dto.getSourceId());
        for (SoOutstockDetailDTO.AddDTO addDTO : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(addDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_SOOUTSTOCK_DETAIL_SKU_NOT_EXIST, addDTO.getSkuNo());
            }
        }
        return this.add(dto);
    }

    @Override
    public String pdaUpdate(SoOutstockDTO.UpdateDTO dto) {
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainId(dto.getSourceId());
        for (SoOutstockDetailDTO.UpdateDTO updateDTO : dto.getDetailList()) {
            SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = noticeDetailEntities.stream().filter(req -> req.getId().equals(updateDTO.getSourceDetailId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDeliveryNoticeDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_SOOUTSTOCK_DETAIL_SKU_NOT_EXIST, updateDTO.getSkuNo());
            }
        }
        return this.updateSoOutstock(dto);
    }


    @Override
    public Boolean pdaAddAndSubmit(SoOutstockDTO.AddDTO dto) {
        String id = this.pdaAdd(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Collections.singletonList(id));
        return result;
    }

    @Override
    public Boolean pdaUpdateAndSubmit(SoOutstockDTO.UpdateDTO dto) {
        String id = this.pdaUpdate(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Collections.singletonList(id));
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void tempRepairHistoryDb() {
        List<SoInfoDTO.ListDTO> soList = soInfoFeign.listRepairHistoryDb();
        List<String> soIdList = soList.stream().map(SoInfoDTO.ListDTO::getSoId).collect(Collectors.toList());
        //销售出库单
        List<SoOutstockEntity> soOutstockList = this.listDbBySoIds(soIdList);
        List<String> soOutstockIdList = soOutstockList.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
        //销售出库详情
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockDetailService.listByMainIds(soOutstockIdList);

        //通知单详情
        List<String> noticeDetailIdList = soOutstockDetailList.stream().map(SoOutstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntityList = CollectionUtils.isNotEmpty(noticeDetailIdList) ? soDeliveryNoticeDetailService.listByIds(noticeDetailIdList) : Collections.emptyList();

        for (SoOutstockEntity item : soOutstockList) {
            String soId = item.getSoId();
            String id = item.getId();
            List<SoInfoDTO.ListDTO> soDetailList = soList.stream().filter(s -> s.getSoId().equals(soId))
                    .collect(Collectors.toList());
            //销售订单的总金额
            BigDecimal soAmount = BigDecimal.ZERO;
            for (SoInfoDTO.ListDTO soDetail : soDetailList) {
                BigDecimal taxAmountBefore = soDetail.getTaxAmountBefore();
                soAmount = soAmount.add(Objects.isNull(taxAmountBefore) ? BigDecimal.ZERO : taxAmountBefore);
            }
            List<SoOutstockDetailEntity> detailList = soOutstockDetailList.stream().
                    filter(d -> d.getMainId().equals(id)).collect(Collectors.toList());
            //出库金额
            BigDecimal outStockAmount = BigDecimal.ZERO;
            for (SoOutstockDetailEntity itemDetail : detailList) {
                //sku id
                String skuId = itemDetail.getSkuId();

                String sourceDetailId = itemDetail.getSourceDetailId();
                String soDetailId = soDeliveryNoticeDetailEntityList.stream().filter(d -> d.getId().equals(sourceDetailId)).
                        map(SoDeliveryNoticeDetailEntity::getSourceDetailId).findFirst().orElse("");

                BigDecimal price = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                        findFirst().map(SoInfoDTO.ListDTO::getPrice).orElse(BigDecimal.ZERO);
                //税率
                BigDecimal taxRate = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).
                        findFirst().map(SoInfoDTO.ListDTO::getTaxRate).orElse(BigDecimal.ZERO);

                //实发数量
                Integer actualQty = itemDetail.getActualQty();


                BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
                BigDecimal taxPrice = MathUtil.getTaxValue(price, flagTaxRate, 4);

                outStockAmount = outStockAmount.add(MathUtil.multiply(taxPrice, actualQty));
            }
            //销售订单折扣额
            BigDecimal discountAmount = soDetailList.get(0).getDiscountAmount();
            //折扣总额占比
            BigDecimal discountAmountRate = MathUtil.divide(outStockAmount, soAmount, 6);
            //整单折扣额
            BigDecimal totalDiscountAmount = MathUtil.multiply(discountAmount, discountAmountRate, 2);
            item.setTotalDiscountAmount(totalDiscountAmount);

        }
        baseMapper.updateBatch(soOutstockList);


    }

    @Override
    public List<SoOutstockEntity> listByTrackNo(String trackNo) {
        if (CharSequenceUtil.isBlank(trackNo)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(SoOutstockEntity::getInvalidStatus, Boolean.FALSE).
                like(SoOutstockEntity::getTrackNo, trackNo).list();
    }

    @Override
    public SoOutstockDTO.PagingTotalDTO getTotalByQuery(SoOutstockDTO.PagingParamDTO params) {
        SoOutstockDTO.PagingTotalDTO pagingTotalDTO = baseMapper.getTotalByQuery(params);
        return pagingTotalDTO;
    }

    /**
     * 生成销售出库单
     *
     * @param soB2cId 销售订单id
     * @return
     * @author yl
     * @date 2023-12-11 16:17
     */
    @Override
    public Boolean generateB2cSoOutstock(String soB2cId) {
        SoOutstockEntity outstock = this.getBySoId(soB2cId);
        if (Objects.isNull(outstock)) {
            SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cFeign.getSoOutstockInfoById(soB2cId);
            if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(generateB2cDTO.getSourceType())) {
                SoB2cDeliveryEntity notCancelBySoId = soB2cDeliveryService.getNotCancelBySoId(soB2cId);
                List<TransferInfoEntity> entities = transferInfoService.listBySourceId(notCancelBySoId.getId());
                generateB2cDTO.setSourceId(notCancelBySoId.getId());
                generateB2cDTO.setSourceCode(notCancelBySoId.getCode());
                if(notCancelBySoId.getDeliveryTime() == null){
                   throw new ServiceException("发货单发货时间不能为空");
                }
                //重试时需要按照发货单发货时间扣减
                generateB2cDTO.setBillDate(notCancelBySoId.getDeliveryTime().toLocalDate());
                List<SoB2cDeliveryDetailEntity> deliveryDetailList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(notCancelBySoId.getId()));
                List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(notCancelBySoId.getId()));
                List<SoOutstockDetailDTO.AddDTO> detailList = generateB2cDTO.getDetailList();
                LinkedList<SoOutstockDetailDTO.AddDTO> newDetailList = new LinkedList<>();
                for (PickingListsDTO.SourceView view : views) {
                    SoB2cDeliveryDetailEntity detailEntity = deliveryDetailList.stream().filter(v -> v.getId().equals(view.getSourceDetailId()))
                            .findFirst().orElse(new SoB2cDeliveryDetailEntity());
                    SoOutstockDetailDTO.AddDTO dto = detailList.stream().filter(d -> d.getSoDetailId().equals(detailEntity.getSourceDetailId()))
                            .findFirst().orElse(new SoOutstockDetailDTO.AddDTO());
                    SoOutstockDetailDTO.AddDTO addDTO = BeanMapperUtils.map(SoOutstockDetailDTO.AddDTO.class, dto);
                    addDTO.setWarehouseLocation(CollectionUtils.isEmpty(entities) ? view.getWarehouseLocation() : "");
                    addDTO.setSkuNo(view.getSkuNo());
                    addDTO.setSkuId(view.getSkuId());
                    addDTO.setActualQty(view.getQty());
                    addDTO.setPlanQty(view.getQty());
                    addDTO.setSourceDetailId(detailEntity.getId());
                    newDetailList.add(addDTO);
                }
                generateB2cDTO.setDetailList(newDetailList);
            } else if (SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode().equals(generateB2cDTO.getSourceType())){
                // 海外仓出库信息补充
                List<DmpThirdOutboundEntity> list = FeignQuery.create(DmpThirdOutboundEntity.class)
                        .eq(DmpThirdOutboundEntity::getReferenceNo, generateB2cDTO.getSoCode())
                        .list();
                if (CollectionUtils.isEmpty(list)){
                    ServiceException.runError("未找到海外仓出库信息:ReferenceNo=" + generateB2cDTO.getSourceCode());
                }
                DmpThirdOutboundEntity outboundEntity = list.get(0);
                LocalDateTime outBoundTime = outboundEntity.getDateShipping();
                if(Objects.nonNull(outBoundTime)){
                    generateB2cDTO.setBillDate(outBoundTime.toLocalDate());
                }
                //跟踪号
                generateB2cDTO.setTrackNo(outboundEntity.getTrackingNo());
                //运单号
                generateB2cDTO.setTransportNo(outboundEntity.getTrackingNo());
            }
            Boolean result = createB2cSoOutstock(generateB2cDTO);
            return result;
        } else {
            String id = outstock.getId();
            ApproveStatusEnum approveStatus = outstock.getApproveStatus();
            // 检查关账时间
            LocalDate closedDate = inventoryClosedRecordService.checkClosed(outstock.getWarehouseOrgId(), outstock.getBillDate());
            if (null != closedDate){
                // 已关账
                return Boolean.TRUE;
            }
            //待提交
            if (ApproveStatusEnum.WAIT_SUBMIT.equals(approveStatus)) {
                soOutstockService.submit(Collections.singletonList(id));
            }
            //审核中
            if (ApproveStatusEnum.APPROVE_ING.equals(approveStatus)) {
                soOutstockService.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(), ""));
            }
            return Boolean.TRUE;
        }

    }

    @Override
    public Boolean generateB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO) {
        Boolean result = createB2cSoOutstock(generateB2cDTO);
        if (result) {
            this.removeSoB2cOutstockError(generateB2cDTO.getSoId());
        }
        return result;
    }

    @Override
    public SoOutstockEntity getBySoId(String soB2cId) {
        return this.lambdaQuery().eq(SoOutstockEntity::getSoId, soB2cId).
                last("LIMIT 1").one();
    }

    private List<SoOutstockEntity> getBySoIdAndWarehouseId(String soB2cId,String warehouseId) {
        return this.lambdaQuery().eq(SoOutstockEntity::getSoId, soB2cId).eq(SoOutstockEntity::getWarehouseId,warehouseId).list();
    }

    /**
     * 创建B2C销售出库单
     * 1.先添加
     * 2.提交审核
     * 3.审核通过
     *
     * @param dto
     * @return
     */
    public Boolean createB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto) {
        try {
            if (dto.isHasPlatformWarehouseOrder()){
                // 非自发货订单独立事务
                soOutstockService.handleCreateB2cSoOutstockWithoutTx(dto);
            } else {
                // 自发货订单事务一起
                soOutstockService.handleCreateB2cSoOutstock(dto);
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            String soB2cId = dto.getSoId();
            String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
            String paramJson = JSONUtil.toJsonStr(dto);
            String message = e.getMessage();
            log.error("创建B2C销售出库单失败,soB2cId:{},paramJson:{} 错误信息:{}", soB2cId, paramJson, message);
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
            addError.setType(type);
            addError.setMainId(soB2cId);
            addError.setMessage(message);
            addError.setParamJson(paramJson);
            soB2cFeign.addSoB2cError(addError);
        }
        return Boolean.FALSE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleCreateB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto) {
        String id = this.addB2cSoOutstock(dto);
        //表示添加成功
        if (CharSequenceUtil.isNotBlank(id)) {
            // 检查关账或已有盘盈盘亏单据
            if (checkClosedAndUpdateRemark(dto, id)){
                return true;
            }
            this.submitAndApprove(id);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkClosedAndUpdateRemark(SoOutstockDTO.GenerateB2cDTO dto, String soOutStockId) {
        if (null != dto.getWarehouseOrgId() && null != dto.getBillDate()){
            // 校验是否存在已库存关账时间
            LocalDate existClosedDate = inventoryClosedRecordService.checkClosed(dto.getWarehouseOrgId(), dto.getBillDate());
            if (null != existClosedDate){
                // 库存关账时间之前的单据不提交
                // 记录明细(事务分开)
                soOutstockDetailService.updateDetailRemark(soOutStockId, "因库存关账时间停止提交", false);
                return true;
            }
            // 仓位信息
            List<String> warehourseLocationList = dto.getDetailList().stream().map(SoOutstockDetailDTO.AddDTO::getWarehouseLocation).distinct().collect(Collectors.toList());
            // SKU信息
            List<String> skuIds = dto.getDetailList().stream().map(SoOutstockDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
            boolean closed = stocktakingProfitLossService.checkClosed(
                    Collections.singletonList(dto.getWarehouseId()),
                    warehourseLocationList,
                    Collections.singletonList(dto.getWarehouseOrgId()),
                    skuIds,
                    dto.getBillDate());
            if (closed){
                // 已有盘盈盘亏单不提交
                // 记录明细(事务分开)
                soOutstockDetailService.updateDetailRemark(soOutStockId, "因库已有盘盈盘亏单据时间停止提交",false);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAndApprove(String id) {
        //提交
        Boolean submitResult = soOutstockService.submit(Collections.singletonList(id));
        if (submitResult) {
            soOutstockService.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(), ""));
        }
    }

    @Override
    public Boolean handleCreateB2cSoOutstockWithoutTx(SoOutstockDTO.GenerateB2cDTO dto) {
        SoOutstockEntity soOutstock = this.getBySoId(dto.getSoId());
        if(Objects.nonNull(soOutstock)
        && (PlatformDictEnum.TIK_TOK.getCode().equals(dto.getDictPlatform())
        || PlatformDictEnum.MERCADOLIBRE.getCode().equals(dto.getDictPlatform()))){
            return true;
        }
        String id = soOutstockService.addB2cSoOutstock(dto);
        //表示添加成功
        if (CharSequenceUtil.isNotBlank(id)) {
            try {
                // 事务分开
                // 检查关账或已有盘盈盘亏单据
                if (soOutstockService.checkClosedAndUpdateRemark(dto, id)){
                    return true;
                }
                soOutstockService.submitAndApprove(id);
            } catch (Exception e) {
                log.error("B2C订单生成销售出库单提交或审核失败：error={}", ExceptionUtil.stacktraceToString(e));
                // 记录明细(事务分开)
                soOutstockDetailService.updateDetailRemark(id, e.getMessage(),false);
            }
        }
        return true;
    }


    /**
     * 删除B2C销售订单 生成销售出库单异常
     * @param soB2cId
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void  removeSoB2cOutstockError(String soB2cId) {
        String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
        SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
        deleteDTO.setMainId(soB2cId);
        deleteDTO.setType(type);
        soB2cFeign.deleteError(deleteDTO);
    }
    /**
     * 添加B2C销售出库单
     *
     * @param dto
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-29 11:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addB2cSoOutstock(SoOutstockDTO.GenerateB2cDTO dto) {
        //来源类型
        String sourceType = dto.getSourceType();
        if (CharSequenceUtil.isBlank(sourceType)) {
            sourceType = SourceTypeEnum.SELF_ADD.getCode();
        }
        String sourceId = dto.getSourceId();
        List<SoOutstockDetailDTO.AddDTO> detailList = dto.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }

        if(dto.isHasPlatformWarehouseOrder()) {
            // 平台仓订单
            // 根据历史映射和当前库存重新生成明细并检查出库数量
            detailList = soOutstockDetailService.checkAndGenerateDetail(dto);
        } else {
            // 非平台仓订单
            //检查出库数量
            List<SoOutstockDetailDTO.UpdateDTO> checkList = BeanMapper.copyList(detailList, SoOutstockDetailDTO.UpdateDTO.class);
            soOutstockDetailService.checkB2cOrderQty(dto.getWarehouseId(), dto.getSoId(), sourceId, sourceType, checkList);
        }

        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        //处理保存或者修改数据
        BusinessNoTypeEnum businessNoType = BusinessNoTypeEnum.CODE_XSCK;
        String code = docNoGenHelper.generateCode(businessNoType);
        soOutstock.setCode(code);
        LocalDate billDate = dto.getBillDate();
        boolean billDateIsNull = Objects.isNull(billDate);
        if(billDateIsNull){
            //速卖通菜鸟仓发货单生产的销售出库单，发货日期都取平台出库日期
            SoB2cEntity soB2cEntity = soB2cFeign.getById(dto.getSoId());
            if (soB2cEntity.hasPlatformWarehouseOrder()) {
                List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(soB2cEntity.getId()));
                if (ObjectUtil.isNotEmpty(soB2cLogisticsEntities)) {
                    LocalDateTime deliveryTime = soB2cLogisticsEntities.get(0).getDeliveryTime();
                    if(Objects.isNull(deliveryTime)){
                       throw new ServiceException("发货日期不能为空");
                    }
                    // 速卖通GMT时区转北京时区
                    LocalDateTime targetDeliveryTime = DateUtil.convertZoneTime(deliveryTime,
                            ZoneId.of("America/Los_Angeles"),
                            ZoneId.of("Asia/Shanghai"));
                    billDate = targetDeliveryTime.toLocalDate();
                }
            }
        }
        if (Objects.isNull(billDate)) {
            billDate = LocalDate.now();
        }
        dto.setBillDate(billDate);
        // 出库日期
        soOutstock.setBillDate(billDate);
        soOutstock.setPlanDeliveryDate(billDate);
        // 实际发货实际
        if (null != dto.getActualDeliveryDate()){
            soOutstock.setActualDeliveryDate(dto.getActualDeliveryDate());
        } else {
            soOutstock.setActualDeliveryDate(billDate.atStartOfDay());
        }
        //取直接调拨单的流水号
        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(Collections.singletonList(dto.getSourceId()));
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            soOutstock.setBatchNo(transferInfoList.get(0).getBatchNo());
        }
        soOutstock.setPackDate(billDate);
        Boolean addResult = super.save(soOutstock);
        if (addResult) {
            soOutstockDetailService.add(soOutstock.getId(), detailList, OrderTypeEnum.B2C.getCode());
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), soOutstock.getId(), "新增操作");

            return soOutstock.getId();
        }
        return "";
    }

    @Override
    public Boolean generateB2cSoOutstockByCode(String soB2cCode) {
        SoOutstockEntity outstock = this.getBySoCode(soB2cCode);
        if (Objects.isNull(outstock)) {
            SoOutstockDTO.GenerateB2cDTO dto = soB2cFeign.getSoOutstockInfoByCode(soB2cCode);
            return this.createB2cSoOutstock(dto);
        }
        return Boolean.TRUE;

    }

    @Override
    public List<SoOutstockEntity> listByAdvanceQuery(AdvanceQueryContainer container) {
        return this.lambdaQuery()
                .last(" and "+container.getSqlMap().get("default"))
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean checkAndGenerate(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformSoOutStockDTO dto, SoB2cEntity soB2cEntity) {
        // 补充来源
        // 根据销售订单生成的销售出库单DTO != 平台的销售出库单
        generateB2cDTO.setSourceCode(soB2cEntity.getPlatformCode());
        generateB2cDTO.setSourceId(soB2cEntity.getId());
        generateB2cDTO.setSourceType(SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode());

        // 1,需要生成销售出库单的明细
        List<PlatformSoOutStockDetailDTO> generateSourceDetailList = new LinkedList<>();

        // 2, 需要更新的销售出库单明细(目前单个明细对应一个订单)
        List<PlatformSoOutStockDetailDTO> updateGenerateSourceDetailList = new LinkedList<>();

        // 3, 已存在的销售出库单明细(用于检查清理历史异常信息)
        List<PlatformSoOutStockDetailDTO> existSourceDetailList = new LinkedList<>();

        // 销售订单明细
        Map<String, SoOutstockDetailEntity> detailEntityListMap = new HashMap<>();

        // 销售出库单主体
        Map<String, SoOutstockEntity> mainEntityMap = new HashMap<>();


        // 判断过滤/新增/更新销售出库单
        List<SoOutstockEntity> entityList = this.listBySourceId(Collections.singletonList(soB2cEntity.getId()));

        if (CollectionUtils.isNotEmpty(entityList)) {
            mainEntityMap =  entityList.stream()
                    .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

            List<String> mainIds = entityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            detailEntityListMap = soOutstockDetailService.listByMainIds(mainIds)
                    .stream()
                    .collect(Collectors.toMap(SoOutstockDetailEntity::getPlatformDetailId, Function.identity()));
        }

        // 判断明细处理
        if (CollectionUtils.isNotEmpty(detailEntityListMap.keySet())){
            for (PlatformSoOutStockDetailDTO detailDTO : dto.getDetailList()) {
                if (null == detailDTO.getPlatformDeliveryTime()){
                    ServiceException.runError("仓储中心解析发货时间时区失败");
                }
                if (!detailEntityListMap.containsKey(detailDTO.getPlatformDetailId())){
                    // 1：明细不存在新增
                    generateSourceDetailList.add(detailDTO);
                    continue;
                }
                // 明细已存在
                SoOutstockDetailEntity detailEntity = detailEntityListMap.get(detailDTO.getPlatformDetailId());
                SoOutstockEntity soOutstockEntity = mainEntityMap.get(detailEntity.getMainId());
                if (Objects.equals(detailEntity.getActualQty(), detailDTO.getQtyShipped())
                        && soOutstockEntity.getBillDate().isEqual(detailDTO.convertPlatformDeliveryDateTime())){
                    // 3, 明细已存在且信息未变更
                    existSourceDetailList.add(detailDTO);
                } else {
                    // 2，明细存在日期或数量变更
                    updateGenerateSourceDetailList.add(detailDTO);
                }
            }

        } else {
            // 所有平台明细ID不存在=新增
            generateSourceDetailList = dto.getDetailList();
        }

        // 新增
        Boolean addResult = soOutstockService.generatePlatformB2cOutStock(generateB2cDTO, dto, soB2cEntity, generateSourceDetailList);

        // 更新
        Boolean updateResult = soOutstockService.updatePlatformB2cOutStock(generateB2cDTO, dto, soB2cEntity, updateGenerateSourceDetailList, mainEntityMap, detailEntityListMap);

        if (addResult && updateResult){
            // 检查清理历史异常信息
            soOutstockService.checkAndDeletePlatformB2cOutStock(existSourceDetailList, soB2cEntity, detailEntityListMap.values());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean generatePlatformB2cOutStock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformSoOutStockDTO dto, SoB2cEntity soB2cEntity, Collection<PlatformSoOutStockDetailDTO> generateSourceDetailList) {
        if (CollectionUtils.isEmpty(generateSourceDetailList)){
            // 无新增
            return true;
        }

        // 按来源日期分组
        Map<LocalDate, List<PlatformSoOutStockDetailDTO>> gourpMap = generateSourceDetailList.stream()
                .collect(Collectors.groupingBy(PlatformSoOutStockDetailDTO::convertPlatformDeliveryDateTime));

        // 分组后的生成销售出库单DTO
        List<SoOutstockDTO.GenerateB2cDTO> generateB2cList = new LinkedList<>();

        boolean result = true;

        // 检查添加的明细数据
        for (Map.Entry<LocalDate, List<PlatformSoOutStockDetailDTO>> entry : gourpMap.entrySet()) {
            SoOutstockDTO.GenerateB2cDTO currentGenerateB2cDTO = new SoOutstockDTO.GenerateB2cDTO();
            BeanUtils.copyProperties(generateB2cDTO, currentGenerateB2cDTO);
            currentGenerateB2cDTO.setBillDate(entry.getKey());
            //跟踪单单号
            currentGenerateB2cDTO.setTrackNo(entry.getValue().get(0).getTrackNo());
            //运单号
            currentGenerateB2cDTO.setTransportNo(entry.getValue().get(0).getTrackNo());
            // 时间发货时间
            currentGenerateB2cDTO.setActualDeliveryDate(DateUtil.parseLocalDateTimeWithOffset(entry.getValue().get(0).getPlatformDeliveryTime()));

            LinkedList<SoOutstockDetailDTO.AddDTO> currentAddDTOList = new LinkedList<>();
            for (PlatformSoOutStockDetailDTO detailDTO : entry.getValue()) {
                SoOutstockDetailDTO.AddDTO activeAddDTO = generateB2cDTO.getDetailList()
                        .stream()
                        .filter(e -> detailDTO.getPlatformOrderDetailId().equalsIgnoreCase(e.getSourceDetailId()))
                        .findFirst()
                        .orElse(null);
                if (null == activeAddDTO){
                   String msg = CharSequenceUtil.format("找不到B2C订单明细：平台={}，平台单号={}，平台单号明细Id={}", dto.getDictPlatform(), detailDTO.getPlatformCode(), detailDTO.getPlatformOrderDetailId());
                   throw new ServiceException(msg);
                }
                SoOutstockDetailDTO.AddDTO currentAddDTO = new SoOutstockDetailDTO.AddDTO();
                BeanUtils.copyProperties(activeAddDTO, currentAddDTO);
                // 平台销售订单明细ID
                currentAddDTO.setSourceDetailId(detailDTO.getPlatformOrderDetailId());
                // 平台销售出库单明细ID
                currentAddDTO.setPlatformDetailId(detailDTO.getPlatformDetailId());
                currentAddDTO.setActualQty(detailDTO.getQtyShipped());
                currentAddDTOList.add(currentAddDTO);
            }
            currentGenerateB2cDTO.setDetailList(currentAddDTOList);
            generateB2cList.add(currentGenerateB2cDTO);
        }

        for (SoOutstockDTO.GenerateB2cDTO genDTO : generateB2cList) {
            try {
                if (!soOutstockService.handleCreateB2cSoOutstockWithoutTx(genDTO)){
                    log.warn("【亚马逊物流销售报告】生成销售出库单失败:dto={}", JSONUtil.toJsonStr(genDTO));
                } else {
                    String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
                    SoB2cErrorDTO.DeleteDetailDTO deleteDTO = new SoB2cErrorDTO.DeleteDetailDTO();
                    deleteDTO.setMainId(soB2cEntity.getId());
                    deleteDTO.setType(type);
                    List<String> detailIds = genDTO.getDetailList().stream()
                            .map(SoOutstockDetailDTO.AddDTO::getSoDetailId)
                            .collect(Collectors.toList());
                    deleteDTO.setDetailIdList(detailIds);
                    soB2cFeign.deleteDetailError(deleteDTO);
                }
            } catch (Exception e) {
                log.warn("自动生成销售出库单失败：dto={}, error={}", JSONUtil.toJsonStr(dto), ExceptionUtil.stacktraceToString(e));
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setParamJson(JSONUtil.toJsonStr(genDTO));
                addError.setReturnJson("");
                addError.setMainId(soB2cEntity.getId());
                addError.setDetailId(genDTO.getDetailList().stream().map(SoOutstockDetailDTO.AddDTO::getSoDetailId).findFirst().orElse(""));
                addError.setMessage(CharSequenceUtil.format("自动生成销售出库单失败：{}", e.getMessage()));
                soB2cFeign.addSoB2cError(addError);
                result = false;
            }
        }
        return result;
    }



    @Override
    public Boolean defaultHandleRetry(SoB2cEntity currentEntity, List<SoB2cEntity> soB2cList) {
        String id = currentEntity.getId();
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        SoB2cEntity soB2c = soB2cList.stream().filter(s ->
                s.getId().equals(id)&&
                        shipped.equals(s.getBillStatus())&&
                        s.hasPlatformWarehouseOrder()
        ).findFirst().orElse(null);
        /**
         * 表示有仓库为空且是已发货并且是平台仓订单
         * 那么就要去找店铺的仓库 然后匹配上仓库
         * [排除速卖通订单]
         */
        if (Objects.nonNull(soB2c) && !PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2c.getDictPlatform())) {
            soB2cFeign.updateWarehouseByShopId(soB2c.getId(), soB2c.getShopId());
        }

        //速卖通是否重试成功表示
        Boolean flag = Boolean.TRUE;

        //速卖通异常订单重新生成需要查询速卖通平台发货单获取仓库
        if (currentEntity.hasPlatformWarehouseOrder() && PlatformDictEnum.ALI_EXPRESS.getCode().equals(currentEntity.getDictPlatform())) {
            flag = soB2cFeign.updateAliExpressOrderWarehouse(currentEntity.getId(), currentEntity.getShopId());
        }

        Boolean result = false;
        if (!currentEntity.hasPlatformWarehouseOrder()){
            // 检查非平台仓订单(不包含海外仓)的必须存在已发货状态的B2C发货单, 记录日志
            boolean hasNotGenB2cSoOutStock = soB2cDeliveryService.hasNotGenB2cSoOutStockAndLog(currentEntity);
            if (hasNotGenB2cSoOutStock){
                // 无已发货的发货单只清理历史异常信息
                result = true;
            } else {
                result = this.generateB2cSoOutstock(id);
            }
        } else {
            //速卖通平台仓订单的销售出库在处理类生成
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(currentEntity.getDictPlatform())) {
                result = flag;
            }else{
                result = this.generateB2cSoOutstock(id);
            }
        }
        boolean allResult = result && flag;
        if (allResult) {
            String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
            SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
            deleteDTO.setMainId(id);
            deleteDTO.setType(type);
            soB2cFeign.deleteError(deleteDTO);
        }
        return allResult;
    }

    private SoOutstockEntity getBySoCode(String soB2cCode) {
        return this.lambdaQuery().eq(SoOutstockEntity::getSoCode, soB2cCode).
                last("LIMIT 1").one();
    }

    private void handleSaveOrUpdateDb(SoOutstockEntity soOutstock) {
        //仓库id
        String warehouseId = soOutstock.getWarehouseId();
        //用户信息
        if (CharSequenceUtil.isNotBlank(soOutstock.getSellerId())) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Collections.singletonList(soOutstock.getSellerId()));
            if (CollectionUtils.isNotEmpty(userList)) {
                //销售员
                String sellerName = userList.stream().filter(obj -> obj.getUserId().equals(soOutstock.getSellerId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                soOutstock.setSellerName(sellerName);
            }
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        String orgId = soOutstock.getWarehouseOrgId();
        if (CharSequenceUtil.isNotBlank(orgId)) {
            SysAccountingCompanyEntity org = sysUserFeign.getCompanyById(orgId);
            if (Objects.nonNull(org)) {
                soOutstock.setWarehouseOrgName(org.getCompanyName());
            }
        }
    }
    @Override
    public List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateDeclare(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        List<TmsDeclareBillDTO.SoOutDTO> result = baseMapper.getCanGenerateDeclare(querySourceDTO);
        if(CollectionUtils.isEmpty(result)){
            return new ArrayList<>();
        }
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();

        List<String> ids = result.stream().map(TmsDeclareBillDTO.SoOutDTO::getSourceId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> allDetailEntityList = soOutstockDetailService.listByMainIds(ids);
        //查询物流产品信息
        List<String> skuIds = allDetailEntityList.stream().map(SoOutstockDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailDTO.ProductLogisticDTO> allProductLogisticDTOList = plmTaskFeign.listProductLogisticsByIds(skuIds);

        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailList = baseMapper.listPackingDetail(ids);
        Map<String,List<WmsCartonDetailDTO.ListPackingDetailDTO>> packingDetailMap = packingDetailList.stream().collect(Collectors.groupingBy(WmsCartonDetailDTO.ListPackingDetailDTO::getId));

        for (TmsDeclareBillDTO.SoOutDTO deliveryDTO : result) {
            String countryName = countryList.stream().filter(obj -> obj.getId().equals(deliveryDTO.getCountry())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            deliveryDTO.setCountryName(countryName);

            List<SoOutstockDetailEntity> detailEntityList = allDetailEntityList.stream().filter(entity -> entity.getMainId().equals(deliveryDTO.getSourceId())).collect(Collectors.toList());
            //处理产品信息
            if(CollectionUtils.isNotEmpty(detailEntityList)){
                //转成MAP，相同sku数量相加
                Map<String,SoOutstockDetailEntity> detailEntityMap = detailEntityList.stream().collect(Collectors.toMap(SoOutstockDetailEntity::getSkuId,
                        Function.identity(),(o1, o2)->{
                            SoOutstockDetailEntity mergeDetail = new SoOutstockDetailEntity();
                            mergeDetail.setSkuId(o1.getSkuId());
                            mergeDetail.setPlanQty(o1.getPlanQty()+o2.getPlanQty());
                            return mergeDetail;
                        }));
                List<TmsDeclareBillDTO.ProductDetail> productDetailList = new ArrayList<>();
                List<ProductDetailDTO.ProductLogisticDTO> productLogisticDTOList = allProductLogisticDTOList.stream().filter(v->detailEntityMap.containsKey(v.getSkuId())).collect(Collectors.toList());
                for (ProductDetailDTO.ProductLogisticDTO productLogisticDTO : productLogisticDTOList) {
                    SoOutstockDetailEntity detailEntity = detailEntityMap.get(productLogisticDTO.getSkuId());
                    if(productLogisticDTO.getCombinationDeclareType().equals(CombinationDeclareTypeEnums.SPLIT.getCode()) && productLogisticDTO.getIsCombination()){
                        //拆分申报的组合品，拆成子SKU
                        for (ProductDetailDTO.ProductLogisticDTO logisticDTO : productLogisticDTO.getChildList()) {
                            TmsDeclareBillDTO.ProductDetail productDetail = BeanUtil.copyProperties(logisticDTO,TmsDeclareBillDTO.ProductDetail.class);
                            productDetail.setQty(detailEntity.getPlanQty() * logisticDTO.getChildQty());
                            productDetail.setToCountry(deliveryDTO.getCountry());
                            productDetail.setToCountryName(deliveryDTO.getCountryName());
                            productDetailList.add(productDetail);
                        }
                    }else{
                        TmsDeclareBillDTO.ProductDetail productDetail = BeanUtil.copyProperties(productLogisticDTO,TmsDeclareBillDTO.ProductDetail.class);
                        productDetail.setQty(detailEntity.getPlanQty());
                        productDetail.setToCountry(deliveryDTO.getCountry());
                        productDetail.setToCountryName(deliveryDTO.getCountryName());
                        productDetailList.add(productDetail);
                    }
                }
                deliveryDTO.setNetWeight(productDetailList.stream().filter(v->Objects.nonNull(v.getNetWeight())).map(v->v.getNetWeight().multiply(new BigDecimal(v.getQty())).divide(new BigDecimal(1000),4, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add));
                deliveryDTO.setProductDetailList(productDetailList);
            }

            //设置装箱信息
            List<WmsCartonDetailDTO.ListPackingDetailDTO> list = packingDetailMap.getOrDefault(deliveryDTO.getSourceId(),new ArrayList<>());
            if(CollectionUtils.isNotEmpty(list)){
                List<TmsDeclareBillDTO.PackingDTO> packingDTOList = BeanUtil.copyToList(list,TmsDeclareBillDTO.PackingDTO.class);
                packingDTOList.forEach(t->t.setCode(deliveryDTO.getSourceCode()));
                deliveryDTO.setPackingDTOList(packingDTOList);
            }
            deliveryDTO.setBoxQty(list.size());
            deliveryDTO.setGrossWeight(list.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        //合并相同的sku
        for (TmsDeclareBillDTO.SoOutDTO deliveryDTO : result) {
            List<TmsDeclareBillDTO.ProductDetail> productDetails = deliveryDTO.getProductDetailList();
            if(productDetails == null){
                productDetails = new ArrayList<>();
            }
            // 根据 skuId 进行分组，并对数量进行求和
            List<TmsDeclareBillDTO.ProductDetail> mergedDetails = new ArrayList<>(productDetails.stream()
                    .collect(Collectors.toMap(
                            TmsDeclareBillDTO.ProductDetail::getSkuId,
                            Function.identity(),
                            (existing, replacement) -> {
                                // 合并数量
                                existing.setQty(existing.getQty() + replacement.getQty());
                                // 其他字段取第一个出现的值
                                return existing;
                            }
                    ))
                    .values());
            mergedDetails.forEach(v->{
                if(Objects.nonNull(v.getPrice())){
                    v.setTotalPrice(v.getPrice().multiply(new BigDecimal(v.getQty())));
                }
            });
            deliveryDTO.setProductDetailList(mergedDetails);
        }

        return result;
    }

    @Override
    public List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq) {
        return baseMapper.logisticStatistics(deliveryStaticsReq);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateStatus(TmsDeclareBillDTO.UpdateStatusDTO dto) {
        if(CharSequenceUtil.isBlank(dto.getDeclareStatus()) && CharSequenceUtil.isBlank(dto.getLogisticsStatus())){
            return false;
        }
        return this.lambdaUpdate()
                .in(SoOutstockEntity :: getId,dto.getIds())
                .set(CharSequenceUtil.isNotBlank(dto.getDeclareStatus()),SoOutstockEntity::getDeclareStatus,dto.getDeclareStatus())
                .update();

    }

    /**
     * 根据单号查询出库单
     * @param codes
     * @return
     */
    @Override
    public List<SoOutstockEntity> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoOutstockEntity::getCode, codes).list();
    }

    /**
     * 平台拉取数据生成销售出库单 (根据平台发货的sku生成对应销售出库单)
     * 注意 List<PlatformDeliveryDetailDTO> 是同个销售订单下相同仓库的明细
     *
     * @param platformGenerateSoOutstockDTO
     * @return
     */
    @Override
    @DataIdempotent(keyIdName = "redissonKey", waitTime = 10)
    public Boolean generateB2cSoOutstockByPlatformData(PlatformGenerateSoOutstockDTO platformGenerateSoOutstockDTO, String redissonKey) {
        List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTO = platformGenerateSoOutstockDTO.getPlatformDeliveryDetailDTOList();
        if(CollectionUtils.isEmpty(platformDeliveryDetailDTO)){
            return false;
        }
        String warehouseId = platformDeliveryDetailDTO.get(0).getWarehouseId();
        String soB2cId = platformDeliveryDetailDTO.get(0).getMainId();
        List<SoOutstockEntity> outstockList = this.getBySoIdAndWarehouseId(soB2cId,warehouseId);
        List<String> dbMainIds = outstockList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> outstockDetailList = soOutstockDetailService.listByMainIds(dbMainIds);
        List<String> existSkuIds = outstockDetailList.stream().map(SoOutstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<String> skuIds = platformDeliveryDetailDTO.stream().map(PlatformDeliveryDetailDTO::getSkuId).collect(Collectors.toList());
        //已存在的主表ID
        List<String> existMainIds = outstockDetailList.stream().filter(v->skuIds.contains(v.getSkuId())).map(SoOutstockDetailEntity::getMainId).collect(Collectors.toList());
        List<SoOutstockEntity> existMains = outstockList.stream().filter(v->existMainIds.contains(v.getId())).collect(Collectors.toList());
        platformDeliveryDetailDTO = platformDeliveryDetailDTO.stream().filter(v->!existSkuIds.contains(v.getSkuId())).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(platformDeliveryDetailDTO)) {
            SoOutstockDTO.GenerateB2cDTO dto = platformGenerateSoOutstockDTO.getGenerateB2cDTO();
            //重新赋值仓库 因为可能销售订单是仓库A 速卖通发货是仓库B
            dto.setWarehouseId(warehouseId);
            dto.setWarehouseName(platformDeliveryDetailDTO.get(0).getWarehouseName());
            dto.setWarehouseOrgId(platformDeliveryDetailDTO.get(0).getWarehouseOrgId());
            dto.setWarehouseOrgName(platformDeliveryDetailDTO.get(0).getWarehouseOrgName());

            // 临时跳过生成已关账之前的销售出库单
            // 查询订单发货时间
            List<SoB2cLogisticsEntity> list = FeignQuery.create(SoB2cLogisticsEntity.class)
                    .eq(SoB2cLogisticsEntity::getMainId, soB2cId)
                    .list();
            if (CollectionUtils.isNotEmpty(list)){
                LocalDateTime deliveryTime = list.stream()
                        .map(SoB2cLogisticsEntity::getDeliveryTime)
                        .findFirst()
                        .orElse(null);
                if (null != deliveryTime){
                    // 速卖通GMT时区转北京时区
                    LocalDateTime targetDeliveryTime = DateUtil.convertZoneTime(deliveryTime,
                            ZoneId.of("America/Los_Angeles"),
                            ZoneId.of("Asia/Shanghai"));
                    // 检查关账时间
                    LocalDate closedDate = inventoryClosedRecordService.checkClosed(dto.getWarehouseOrgId(), targetDeliveryTime.toLocalDate());
                    if (null != closedDate){
                        // 临时跳过生成已关账之前的销售出库单
                        return true;
                    }
                }
            }


            //通过平台发货单生成销售出库单，不与销售订单明细关联
            LinkedList<SoOutstockDetailDTO.AddDTO> skuList = new LinkedList<>();
            for (PlatformDeliveryDetailDTO deliveryDetailDTO : platformDeliveryDetailDTO) {
                skuList.add(SoOutstockConverter.INSTANCE.platformDetailToOutDetail(deliveryDetailDTO));
            }
            dto.setDetailList(skuList);
            dto.setCheckSkuHistory(false);
            Boolean result = createB2cSoOutstock(dto);
            return result;
        }
        existMains.forEach(outstock -> {
            String id = outstock.getId();
            ApproveStatusEnum approveStatus = outstock.getApproveStatus();
            // 检查关账时间
            LocalDate closedDate = inventoryClosedRecordService.checkClosed(outstock.getWarehouseOrgId(), outstock.getBillDate());
            if (null != closedDate){
                // 已关账
                return;
            }
            //待提交
            if (ApproveStatusEnum.WAIT_SUBMIT.equals(approveStatus)) {
                soOutstockService.submit(Collections.singletonList(id));
            }
            //审核中
            if (ApproveStatusEnum.APPROVE_ING.equals(approveStatus)) {
                soOutstockService.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(), ""));
            }
        });

        return Boolean.TRUE;

    }
    @Override
    public Boolean afreshGenerateB2cOutstock(List<String> ids) {
        Map<String, SoB2cEntity> mainMap = soB2cFeign.listByIds(ids)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        for (String id : ids) {
            try {
                SoB2cEntity currentEntity = mainMap.get(id);
                if (null == currentEntity){
                    throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
                }
                PlatformRetryHandler.retrySoOutStock(currentEntity, Collections.singletonList(currentEntity));
            } catch (Exception e) {
                String message = e.getMessage();
                log.error("重新创建或者修改B2C销售出库单失败,soB2cId:{},paramJson:{} 错误信息:{}", id, id, message);
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setMainId(id);
                addError.setMessage(message);
                addError.setParamJson(id);
                soB2cFeign.addSoB2cError(addError);
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public void updateRemarkBySoId(String id, String remark) {
        SoOutstockEntity soOutStock = this.getBySoId(id);
        if(Objects.nonNull(soOutStock)){
            soOutstockDetailService.updateDetailRemark(soOutStock.getId(),remark,false);
        }
    }

    @Override
    public boolean checkExist(String soCode, String sourceType, String orderType) {
        return this.lambdaQuery()
                .eq(SoOutstockEntity::getSoCode, soCode)
                .eq(CharSequenceUtil.isNotBlank(sourceType), SoOutstockEntity::getSourceType, sourceType)
                .eq(CharSequenceUtil.isNotBlank(orderType), SoOutstockEntity::getOrderType, orderType)
                .eq(SoOutstockEntity::getInvalidStatus, false)
                .count() > 0;
    }



    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */

    private void sendPushTask (List<SoOutstockEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            Boolean isB2c = OrderTypeEnum.B2C.getCode().equals(obj.getOrderType());
            DmpPushTaskEntity pushTaskEntity ;
            if (!isB2c) {
                pushTaskEntity = syncKingdeeSoOutstockService.syncDataToKingdee(obj, operate);
            } else {
                if (SourceTypeEnum.WDT_OUT_STOCK.getCode().equals(obj.getSourceType())){
                    pushTaskEntity = syncKingdeeSoOutstockService.syncWdtDataToKingdee(obj, operate);
                }else {
                    pushTaskEntity = syncKingdeeSoOutstockService.syncB2cDataToKingdee(obj, operate);
                }
            }
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }


    @Override
    public void thirdWarehouseCheckAndGenerate(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformOutboundDTO dto) {
        try {
            LocalDateTime outBoundTime = dto.getOutBoundTime();
            if(Objects.nonNull(outBoundTime)){
                generateB2cDTO.setBillDate(outBoundTime.toLocalDate());
            }
            //跟踪号
            generateB2cDTO.setTrackNo(dto.getTrackNo());
            //运单号
            generateB2cDTO.setTransportNo(dto.getTrackNo());
            this.generateB2cSoOutstock(generateB2cDTO);
        } catch (Exception e) {
            log.error("销售订单{} 生成销售出库单失败>>>>>>{}", generateB2cDTO.getSoCode(), e.getMessage());
        }
    }

    @Override
    public Boolean updatePlatformB2cOutStock(SoOutstockDTO.GenerateB2cDTO generateB2cDTO, PlatformSoOutStockDTO dto, SoB2cEntity soB2cEntity, Collection<PlatformSoOutStockDetailDTO> updateGenerateSourceDetailList, Map<String, SoOutstockEntity> mainEntityMap, Map<String, SoOutstockDetailEntity> detailEntityListMap) {
        if (CollectionUtils.isEmpty(updateGenerateSourceDetailList)){
            // 无新增
            return true;
        }
        // 需要检查清除异常的明细IDS
        List<String> checkErrorDetailIds = new LinkedList<>();

        boolean result = true;

        for (PlatformSoOutStockDetailDTO detailDTO : updateGenerateSourceDetailList) {
            SoOutstockDetailEntity detailEntity = detailEntityListMap.get(detailDTO.getPlatformDetailId());
            SoOutstockEntity soOutstockEntity = mainEntityMap.get(detailEntity.getMainId());
            try {
                // 反审核(独立事务)
                if (ApproveStatusEnum.APPROVE.equals(soOutstockEntity.getApproveStatus())){
//                    BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
//                    idsDTO.setIds(Collections.singletonList(soOutstockEntity.getId()));
                    soOutstockService.disApprove(soOutstockEntity, Boolean.TRUE);
                }
                // 审核中撤销(独立事务)
                if (ApproveStatusEnum.APPROVE_ING.equals(soOutstockEntity.getApproveStatus())){
                    soOutstockService.cancelProcess(Collections.singletonList(soOutstockEntity.getId()));
                }
                // 删除历史(独立事务)
                soOutstockService.delete(Collections.singletonList(soOutstockEntity.getId()));

                // 重新新增(独立事务)
                soOutstockService.generatePlatformB2cOutStock(generateB2cDTO, dto, soB2cEntity, updateGenerateSourceDetailList);

                checkErrorDetailIds.add(detailEntity.getId());
            } catch (Exception e) {
                log.error("【平台销售出库单生成失败】dto={},error={}", JSONUtil.toJsonStr(dto), ExceptionUtil.stacktraceToString(e));
                // 记录信息(独立事务)
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
                addError.setParamJson(JSONUtil.toJsonStr(generateB2cDTO));
                addError.setReturnJson("");
                addError.setMainId(soB2cEntity.getId());
                addError.setDetailId(detailEntity.getId());
                addError.setMessage(CharSequenceUtil.format("自动生成销售出库单失败：{}", e.getMessage()));
                soB2cFeign.addSoB2cError(addError);
                result = false;
            }
        }

        // 检查清除异常信息
        if (CollectionUtils.isNotEmpty(checkErrorDetailIds)){
            String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
            SoB2cErrorDTO.DeleteDetailDTO deleteDTO = new SoB2cErrorDTO.DeleteDetailDTO();
            deleteDTO.setMainId(soB2cEntity.getId());
            deleteDTO.setType(type);
            deleteDTO.setDetailIdList(checkErrorDetailIds);
            soB2cFeign.deleteDetailError(deleteDTO);
        }
        return result;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void checkAndDeletePlatformB2cOutStock(List<PlatformSoOutStockDetailDTO> existSourceDetailList, SoB2cEntity soB2cEntity, Collection<SoOutstockDetailEntity> detailEntityList) {
        if (CollectionUtils.isEmpty(existSourceDetailList) || CollectionUtils.isEmpty(detailEntityList) ) {
            return;
        }
        List<String> existSoDetailIds = detailEntityList.stream().map(SoOutstockDetailEntity::getSoDetailId).distinct().collect(Collectors.toList());
        log.warn("所有明细已生成销售出库单忽略处理, B2C销售订单={}, 来源明细IDS={}", soB2cEntity.getPlatformCode(), existSoDetailIds);
        String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
        SoB2cErrorDTO.DeleteDetailDTO deleteDTO = new SoB2cErrorDTO.DeleteDetailDTO();
        deleteDTO.setMainId(soB2cEntity.getId());
        deleteDTO.setType(type);
        deleteDTO.setDetailIdList(existSoDetailIds);
        soB2cFeign.checkAndDeleteAllError(deleteDTO);
    }

    @Override
    public int countNotVoided(String id) {
        return count(Wrappers.<SoOutstockEntity>lambdaQuery().eq(SoOutstockEntity::getSourceId, id)
                .eq(SoOutstockEntity::getInvalidStatus, false));
    }



    @Override
    public LocalDate getStopSoOutStockDate() {
        List<DictBasicDTO.ListDTO> stopGenReceivedTimeList = dictBasicService.getByKey(DictBasicEnum.STOP_GEN_SO_OUT_STOCK_TIME.getKey());
        if (!CollectionUtils.isEmpty(stopGenReceivedTimeList)) {
            DictBasicDTO.ListDTO configDTO = stopGenReceivedTimeList.stream().findFirst().orElse(null);
            LocalDateTime stopTime;
            if (null != configDTO && CharSequenceUtil.isNotBlank(configDTO.getValue())) {
                // 配置时间为主
                stopTime = LocalDateTime.parse(configDTO.getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return stopTime.toLocalDate();
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMainAndDetail(SoOutstockEntity soOutstockEntity, List<SoOutstockDetailEntity> list) {
        // 更新
        soOutstockService.updateById(soOutstockEntity);
        soOutstockDetailService.updateBatchById(list);
    }

    private void syncToWdt(SoOutstockEntity entity,SyncOperateEnum operateEnum) {
        if(CharSequenceUtil.isBlank(entity.getWarehouseId())){
            return;
        }
        List<SoOutstockDetailEntity> detailEntityList = soOutstockDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        HashSet<String> warehouseIdSet = new HashSet<>();
        warehouseIdSet.add(entity.getWarehouseId());
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }

        if(SyncOperateEnum.OPERATE_APPROVE.equals(operateEnum)){
            List<CreateOtherStockoutRequest.GoodsList> outGoodsList = new ArrayList<>();
            for (SoOutstockDetailEntity detailEntity : detailEntityList) {
                CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
                outGoods.setSpecNo(detailEntity.getSkuNo());
                outGoods.setNum(BigDecimal.valueOf(detailEntity.getActualQty()));
                outGoods.setPositionNo(detailEntity.getWarehouseLocation());
                outGoods.setWarehouseId(entity.getWarehouseId());
                outGoodsList.add(outGoods);
            }
            abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), outGoodsList, SourceTypeEnum.OTHER_OUTSTOCK);
        }
        if(SyncOperateEnum.OPERATE_DISAPPROVE.equals(operateEnum)){
            List<CreateOtherStockinRequest.GoodsList> inGoodsList = new ArrayList<>();
            for (SoOutstockDetailEntity detailEntity : detailEntityList) {
                CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
                inGoods.setSpecNo(detailEntity.getSkuNo());
                inGoods.setNum(BigDecimal.valueOf(detailEntity.getActualQty()));
                inGoods.setPositionNo(detailEntity.getWarehouseLocation());
                inGoods.setWarehouseId(entity.getWarehouseId());
                inGoodsList.add(inGoods);
            }
            abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), inGoodsList, SourceTypeEnum.OTHER_INSTOCK);
        }
    }

    /**
     * 生成旺店通中间表数据
     */
    private DmpPushWdtDTO.AddDTO generateWdtInterim(String sourceId, String sourceCode, String operateCode, String warehouseId, String outCode, String thirdWarehouseCode, List<? extends CommonCreateBillGoodsReq> outGoods, SourceTypeEnum sourceTypeEnum) {
        DmpPushWdtDTO.AddDTO pushWdtDTO = new DmpPushWdtDTO.AddDTO();
        pushWdtDTO.setSourceId(sourceId);
        pushWdtDTO.setSourceCode(sourceCode);
        pushWdtDTO.setThirdCode(outCode);
        pushWdtDTO.setWarehouseId(warehouseId);
        pushWdtDTO.setThirdWarehouseCode(thirdWarehouseCode);
        pushWdtDTO.setThirdType(sourceTypeEnum.getCode());
        pushWdtDTO.setOperateType(operateCode);
        List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(outGoods, DmpPushWdtDetailDTO.class);
        pushWdtDTO.setDetailDTOList(detailDTOList);
        return pushWdtDTO;
    }


    /**
     * 同步速递云销售出库单
     * @param entity
     * @param operateEnum
     */
    public void sdyFieldHandler(SoOutstockEntity entity, String operateEnum) {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(Arrays.asList(entity.getId()));
        List<String> skuNos = soOutstockDetailEntities.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = soOutstockDetailEntities.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomBySkuIds(skuIds);

        List<String> currencyCodeList = soOutstockDetailEntities.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                .in(ProductDetailEntity::getId, parentSkuId)
                .list();


        String transactionSubType = "";
        if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
            SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSoId());
            transactionSubType = soB2cEntity.getTransactionSubType();
        } else if (OrderTypeEnum.B2B.getCode().equals(entity.getOrderType())) {
            SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(entity.getSoId());
            transactionSubType = soInfoEntity.getTransactionSubType();
        }

        //组织信息
        CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, entity.getCustomerId());



        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization(), entity.getSalesOrgId()));

        for (int i = 0; i < soOutstockDetailEntities.size(); i++) {
            SoOutstockDetailEntity soOutstockDetailEntity = soOutstockDetailEntities.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

            shudiyunB2cOrderDTO.setTransaction_unique_key(entity.getId()+soOutstockDetailEntity.getId());
            shudiyunB2cOrderDTO.setBiz_no(entity.getCode());
            shudiyunB2cOrderDTO.setBiz_time(localDate.format(entity.getBillDate()));
            //默认出库单
            shudiyunB2cOrderDTO.setTransaction_type("200.10");

            shudiyunB2cOrderDTO.setTransaction_sub_type(convertOutstockTransactionSubType(transactionSubType));
            shudiyunB2cOrderDTO.setBiz_status(shudiyunB2cOrderDTO.sdyStatusHandle(operateEnum));

            //组织信息
            if (ObjectUtil.isNotEmpty(customerInfo)) {
                String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(entity.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
                BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                    shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                    shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                    shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                }

                if (customerInfo.getCurrency() == null) {
                    shudiyunB2cOrderDTO.setSettlement_currency_code("");
                } else {
                    shudiyunB2cOrderDTO.setSettlement_currency_code(customerInfo.getCurrency());
                }

                if (customerInfo.getTradeCurrency() == null) {
                    shudiyunB2cOrderDTO.setSettlement_currency_code("");
                } else {
                    shudiyunB2cOrderDTO.setTransaction_currency_code(customerInfo.getTradeCurrency());
                }

                shudiyunB2cOrderDTO.setPlatform_id(customerInfo.getPlatformType());
                shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.checkAndGetByCode(customerInfo.getPlatformType()).getName());

            }

            shudiyunB2cOrderDTO.setShop_no(entity.getCustomerId());
            shudiyunB2cOrderDTO.setShop_name(entity.getCustomerName());

            shudiyunB2cOrderDTO.setShop_no(entity.getCustomerId());
            shudiyunB2cOrderDTO.setShop_name(entity.getCustomerName());
            shudiyunB2cOrderDTO.setRoot_node_no(entity.getCode());
            shudiyunB2cOrderDTO.setGoods_no(soOutstockDetailEntity.getSkuNo());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());
            if (skuVO.getSpuNo() == null) {
                shudiyunB2cOrderDTO.setSpec_no("");
                shudiyunB2cOrderDTO.setSpec_name("");
            } else {
                shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
                shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
            }


            if (soOutstockDetailEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(null);
            shudiyunB2cOrderDTO.setIs_comb(0);

            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
                shudiyunB2cOrderDTO.setIs_comb(1);
                shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
                ProductDetailEntity productDetailEntity = parentSkuList.stream().filter(req -> req.getId().equals(bomChildrenSkuDTO.getParentSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                    shudiyunB2cOrderDTO.setSuite_name(productDetailEntity.getName());
                }
            }

            shudiyunB2cOrderDTO.setRemark(soOutstockDetailEntity.getRemark());
            shudiyunB2cOrderDTO.setWarehouse_no(entity.getWarehouseId());
            shudiyunB2cOrderDTO.setWarehouse_name(entity.getWarehouseName());

            // 商品状态
            shudiyunB2cOrderDTO.setGoods_status("10.10");

            shudiyunB2cOrderDTO.setDelivery_time(localDateTime.format(entity.getActualDeliveryDate()));
            shudiyunB2cOrderDTO.setGoods_transaction_quantity(soOutstockDetailEntity.getActualQty());
            shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());
            if (skuVO.getRetailPrice() != null) {
                shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
            } else {
                shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(BigDecimal.ZERO);
            }

            if (CollectionUtils.isNotEmpty(currencyList)) {
                shudiyunB2cOrderDTO.setTransaction_currency(currencyList.get(0).getName());
                shudiyunB2cOrderDTO.setTransaction_currency_code(currencyList.get(0).getId());
            }

            shudiyunB2cOrderDTO.setMsku_code(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setMsku_name(skuVO.getSpuName());
            shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

            shudiyunB2cOrderDTO.setSource_system("SDC");

            shudiyunB2cOrderDTO.setRoot_node_no_initial(entity.getSoCode());

            WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
            wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            wmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SO_OUTSTOCK.getCode());
            wmsPushMsgEntity.setSourceId(soOutstockDetailEntity.getId());
            wmsPushMsgEntity.setSourceCode(entity.getCode());
            wmsPushMsgEntity.setSyncOperate(operateEnum);
            wmsPushMsgEntity.setPushData(JSON.toJSONString(shudiyunB2cOrderDTO));

            wmsPushMsgService.save(wmsPushMsgEntity);
        }
    }

    /**
     * 出库单子状态转换
     * @param transactionSubType
     * @return
     */
    private String convertOutstockTransactionSubType(String transactionSubType) {
        if (OrderSubTypeEnum.OFFLINE_ORDER.getCode().equals(transactionSubType) || OrderSubTypeEnum.ONLINE_ORDER.getCode().equals(transactionSubType)) {
            //普通出库
            transactionSubType = "200.10.01";
        } else if (OrderSubTypeEnum.GIFT_ORDER.getCode().equals(transactionSubType) || OrderSubTypeEnum.GIFT_REPLENISHMENT.getCode().equals(transactionSubType)) {
            //赠品出库
            transactionSubType = "200.10.02";
        } else if (OrderSubTypeEnum.EXCHANGE_REPLACEMENT.getCode().equals(transactionSubType)) {
            //换货补发
            transactionSubType = "200.10.03";
        } else if (OrderSubTypeEnum.REPLENISHMENT.getCode().equals(transactionSubType)) {
            //补发出库
            transactionSubType = "200.10.04";
        } else {
            //其他
            transactionSubType = "200.10.05";
        }
        return transactionSubType;
    }

}
