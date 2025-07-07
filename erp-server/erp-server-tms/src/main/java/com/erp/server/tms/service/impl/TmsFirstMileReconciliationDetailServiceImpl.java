package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.dto.excel.FirstMileReconciliationStandardExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.ProductPackFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.PackingTaskFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.convert.TmsFirstMileReconciliationConverter;
import com.erp.server.tms.listener.FirstMileReconciliationConfigExcelListener;
import com.erp.server.tms.listener.FirstMileReconciliationStandardExcelListener;
import com.erp.server.tms.mapper.TmsFirstMileReconciliationDetailMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_FIRST_MILE_RECONCILIATION_DETAIL;

/**
 * <p>
 * 头程对账单明细 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class TmsFirstMileReconciliationDetailServiceImpl extends SuperServiceImpl<TmsFirstMileReconciliationDetailMapper, TmsFirstMileReconciliationDetailEntity> implements TmsFirstMileReconciliationDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Lazy
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;
    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;
    @Lazy
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private PackingTaskFeign packingTaskFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ProductPackFeign productPackFeign;
    @Resource
    private InventorySkuCostService inventorySkuCostService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO addDTO) {
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = new TmsFirstMileReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileReconciliationDetailEntity);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);

        log.info("开始新增头程对账单明细");
        boolean save = super.save(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程对账单明细", tmsFirstMileReconciliationDetailEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileReconciliationDetailEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(tmsFirstMileReconciliationDetailEntity.getId(), tmsFirstMileReconciliationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单明细"));
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = BeanMapperUtils.map(TmsFirstMileReconciliationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);
        log.info("编辑 开始修改头程对账单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }
        

        // 记录主单操作日志
        log.info("编辑 开始记录头程对账单明细日志数据，id：【{}】", tmsFirstMileReconciliationDetailEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), tmsFirstMileReconciliationDetailEntity.getId(), "头程对账单明细");
        
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationDetailEntity, null, tmsFirstMileReconciliationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> records) {
        // 查询
        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, String status) {
        TmsFirstMileReconciliationDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单明细数据"));
        if (!CharSequenceUtil.equals(entity.getStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId, id)
                .set(TmsFirstMileReconciliationDetailEntity::getStatus, status)
                .update();
        // 记录主单操作日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("头程对账单【{}】更新状态为【{}】", entity.getSourceCode(), ReconciliationStatusEnum.getName(status)), ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "更新状态操作");
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsFirstMileReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        TmsFirstMileReconciliationEntity mainEntity = tmsFirstMileReconciliationService.getByIdOpt(excelImportDTO.getId()).orElseThrow(() -> new ServiceException("未找到头程对账单主数据"));
        switch (excelImportDTO.getTypeEnum()) {
            case STANDARD:
                return importStandardFile(excelImportDTO.getExcelFile(), mainEntity);
            case CONFIG:
                return importConfigFile(excelImportDTO.getExcelFile(), response, mainEntity);
            default:
                throw new ServiceException("输入导入的类型有误");
        }
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        // 查询已签收（待对账）
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = tmsFirstMileLogisticService.waitReconciliationPaging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(TmsFirstMileReconciliationDetailDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask("头程对账单明细导出", EXPORT_TMS_TMS_FIRST_MILE_RECONCILIATION_DETAIL.getCode(), param);
    }

    private void fillExportInfo(List<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> list) {
        List<String> supplierIds = list.stream().map(TmsFirstMileReconciliationDetailDTO.ExportDetailDTO::getLogisticsSupplierId).distinct().collect(Collectors.toList());
        // 物流商
        Map<String, LogisticsSupplierEntity> supplierMap = logisticsSupplierService.mapByIds(supplierIds);

        // 计费方式
        List<String> channelIds = list.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        Map<String, LogisticsChannelEntity> channelMap = logisticsChannelService.listByIds(channelIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        //发货单
        List<String> deliveryCodes = list.stream().map(TmsFirstMileReconciliationDetailDTO.ExportDetailDTO::getRelationCode).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDTO.BusinessDTO> businessDTOList = wmsFirstMileDeliveryFeign.getBusinessCodeByCodes(deliveryCodes);
        Map<String, String> idSymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
        for (TmsFirstMileReconciliationDetailDTO.ExportDetailDTO data : list) {
            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            LogisticsSupplierEntity supplierEntity = supplierMap.get(data.getLogisticsSupplierId());
            // 物流商
            data.setLogisticsSupplierName(null == supplierEntity ? "" : supplierEntity.getSupplierName());

            // 计费方式
            LogisticsChannelEntity logisticsChannelEntity = channelMap.get(data.getLogisticsChannelId());
            if (null != logisticsChannelEntity) {
                data.setFeeRule(logisticsChannelEntity.getFeeRule());
                data.setFeeRuleName(ShippingFeeRuleEnum.getName(logisticsChannelEntity.getFeeRule()));
            } else {
                data.setFeeRule("");
                data.setFeeRuleName("");
            }

            // 待对账类型
            data.setTypeName(DetailReconciliationTypeEnum.getNameByCode(data.getType()));

            // 对账状态
            data.setStatusName(ReconciliationStatusEnum.getName(data.getStatus()));

            if (Objects.nonNull(data.getReconciliationCount())){
                if (Objects.equals(0, data.getReconciliationCount())){
                    data.setReconciliationCountName("");
                }else if (Objects.equals(1, data.getReconciliationCount())){
                    data.setReconciliationCountName("首次对账");
                }else {
                    data.setReconciliationCountName(data.getReconciliationCount()+"次对账");
                }

            }
            //业务单号查询逻辑修改 展示FBA发货单号和第三方货号-同期初展示逻辑
            FirstMileDeliveryDTO.BusinessDTO businessDTO = businessDTOList.stream().filter(e -> Objects.equals(e.getCode(), data.getRelationCode())).findFirst().orElse(null);
            if (Objects.nonNull(businessDTO)){
                data.setBusinessCode(businessDTO.getBusinessCode());
            }else {
                data.setBusinessCode("");
            }
            data.setShippingCostStr(idSymbolMap.get(data.getShippingCostCurrency()) + data.getShippingCost());
            data.setDeclareCostStr(idSymbolMap.get(data.getDeclareCostCurrency()) + data.getDeclareCost());
            data.setOtherTaxCostStr(idSymbolMap.get(data.getOtherTaxCurrency()) + data.getOtherTaxCost());
            data.setOtherCostStr(idSymbolMap.get(data.getOtherCostCurrency()) + data.getOtherCost());
        }
    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery()
                .in(TmsFirstMileReconciliationDetailEntity::getMainId, mainIds)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList, TmsFirstMileReconciliationEntity mainEntity) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("明细不能为空");
        }
        String mainId = mainEntity.getId();
        List<TmsFirstMileReconciliationDetailEntity> list = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailEntity.class, detailList);

        //原明细数据被删除的需要清除mainId
        List<TmsFirstMileReconciliationDetailEntity> oldList = this.listByMainIds(Collections.singletonList(mainId));

        Map<String, TmsFirstMileReconciliationDetailEntity> actualMap = handleUpdateData(list, mainId, oldList);

        List<String> deleteIds = getDeleteIds(list, oldList);

        if (!CollectionUtils.isEmpty(deleteIds)) {
            List<TmsFirstMileReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getTransportNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个头程对账明细【%s】", ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), pairList, "编辑操作");
            //更新主表id
            if (!CollectionUtils.isEmpty(deleteList)) {
                deleteList.forEach(obj -> {
                    //更新其他对账单对账次数
                    updateReconciliationDetailCount(obj.getId(),obj.getSourceId(),obj.getReconciliationCount());
                    //这里不移除物流费用单中的对账单id是为了不让对账单中的数据影响 物流单关联其他对账单时重新创建物流单费用记录
                    this.lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId, obj.getId()).remove();
                    if (1== obj.getReconciliationCount()){
                        //首次对账单 修改费用状态
                        logisticsBillCostService.updateStatusByLogisticsBillIdsAndReconciliationId(Collections.singletonList(obj.getSourceId()), mainEntity.getId(),ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
                    }else {
                        //删除费用明细记录
                        logisticsBillCostService.removeByReconciliationIds(mainEntity.getId(), Collections.singletonList(obj.getSourceId()));
                    }
                });
            }
        }

        // 更新总数
        BigDecimal totalCost = list.stream().filter(e -> DetailReconciliationTypeEnum.ACTUAL.getCode().equalsIgnoreCase(e.getType()))
                .map(TmsFirstMileReconciliationDetailEntity::getTotalLogisticsCost)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        mainEntity.setTotalCost(totalCost);


        tmsFirstMileReconciliationService.updateById(mainEntity);
        log.info("编辑 开始修改头程对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if (!save) {
            throw new ServiceException("头程对账单明细保存或更新失败");
        }
        // 需要变动的物流单
        List<String> billIds = list.stream()
                .filter(e -> e.getType().equalsIgnoreCase(DetailReconciliationTypeEnum.ESTIMATED.getCode()))
                .map(TmsFirstMileReconciliationDetailEntity::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        List<LogisticsBillCostEntity> billEntityList = logisticsBillCostService.listByLogisticsBillIdList(billIds);
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(billIds);
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByIds(billIds);
        //过滤首次对账时账单/已存在对账单账单
        //不存在费用表记录，新增一个费用列表
        List<LogisticsBillCostEntity> updateBillList = getBillCostList(mainEntity, billIds, logisticsBillEntityList, billEntityList, mainId, actualMap, list);
        // 批量更新物流单状态
        if (!CollectionUtils.isEmpty(updateBillList)) {
            // 更新费用信息
            logisticsBillCostService.saveOrUpdateBatch(updateBillList);
            // 更新明细信息
            List<String> delActualCostIds = new LinkedList<>();
            for (LogisticsBillCostEntity costEntity : updateBillList) {
                // 检查历史明细是否需要移除
                if (ReconciliationStatusEnum.TO_BE_GENERATED.getCode().equalsIgnoreCase(costEntity.getReconciliationStatus())){
                    delActualCostIds.add(costEntity.getId());
                    continue;
                }
                List<TmsCostDetailDTO.UpdateDTO> updateList = costEntity.getUpdateList();
                if (CollectionUtils.isEmpty(updateList)) {
                    continue;
                }
                tmsCostDetailService.batchUpdate(updateList, costEntity.getId(), DictCostAttributionEnum.FIRST_MILE,Boolean.FALSE);
            }
            // 移除实际费用
            if (!CollectionUtils.isEmpty(delActualCostIds)){
                tmsCostDetailService.updateActual0ByMainId(delActualCostIds);
            }
        }
        // 修改或添加实际费用

        //更新费用信息
        this.addOrUpdateCost(list);

        return Boolean.TRUE;
    }

    private List<LogisticsBillCostEntity> getBillCostList(TmsFirstMileReconciliationEntity mainEntity, List<String> billIds, List<LogisticsBillEntity> logisticsBillEntityList, List<LogisticsBillCostEntity> billEntityList, String mainId, Map<String, TmsFirstMileReconciliationDetailEntity> actualMap, List<TmsFirstMileReconciliationDetailEntity> list) {
        if (CollUtil.isEmpty(billIds)) {
            return Collections.emptyList();
        }
        Map<String, ShopInfoEntity> shopMap = null;
        if (CollUtil.isNotEmpty(logisticsBillEntityList)) {
            List<String> shopIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(shopIds)) {
                List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
                if (CollUtil.isNotEmpty(shopInfoEntityList)) {
                    shopMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity()));
                }
            }
        }
        List<LogisticsBillCostEntity> updateBillList = new ArrayList<>(billIds.size());
        for (String billId : billIds) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(e -> Objects.equals(billId, e.getId())).findFirst().orElse(null);
            if (Objects.isNull(logisticsBillEntity)) {
                continue;
            }
            LogisticsBillCostEntity entity = billEntityList.stream()
                    .filter(e -> Objects.nonNull(e) && Objects.equals(billId, e.getLogisticsBillId())
                            && (CharSequenceUtil.isBlank(e.getReconciliationId()) || Objects.equals(mainId, e.getReconciliationId()))).findFirst().orElse(new LogisticsBillCostEntity());
            entity.setLogisticsBillId(billId);
            entity.setTransportNo(logisticsBillEntity.getTransportNo());
            TmsFirstMileReconciliationDetailEntity actualDetailEntity = actualMap.get(entity.getLogisticsBillId());
            if (null == actualDetailEntity) {
                // 移除
                entity.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
                continue;
            }
            // 更新实际重量和体积重, 计费重
            entity.setVolumeWeightLogistics(actualDetailEntity.getVolumeWeight());
            entity.setWeightLogistics(actualDetailEntity.getActualWeight());
            // 费用重取最大
            entity.setBillingWeight(actualDetailEntity.getVolumeWeight().max(actualDetailEntity.getActualWeight()));
            //默认kg
            entity.setWeightUnit(CharSequenceUtil.isBlank(actualDetailEntity.getActualWeightUnit()) ? UnitEnum.WeightUnitEnum.KG.code : actualDetailEntity.getActualWeightUnit());
            // 设置实际费用明细
            if (!CollectionUtils.isEmpty(actualDetailEntity.getUpdateList())) {
                List<TmsCostDetailDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(TmsCostDetailDTO.UpdateDTO.class, actualDetailEntity.getUpdateList());
                updateList.forEach(e -> e.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode()));
                entity.setUpdateList(updateList);
            }

            entity.setReconciliationStatus(actualDetailEntity.getStatus());
            //填充下推对账单id
            entity.setReconciliationId(mainId);
            //获取对应明细数据
            TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = list.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getTransportNo(), logisticsBillEntity.getTransportNo())).findFirst().orElse(null);
            if (Objects.isNull(tmsFirstMileReconciliationDetailEntity)) {
                continue;
            }
            entity.setChannelId(tmsFirstMileReconciliationDetailEntity.getLogisticsChannelId()).setCurrency(mainEntity.getCurrency()).setTransportNo(logisticsBillEntity.getTransportNo()).setType(DictCostAttributionEnum.FIRST_MILE.getCode());
            //店铺信息
            if (CharSequenceUtil.isNotBlank(logisticsBillEntity.getShopId()) && Objects.nonNull(shopMap)) {
                ShopInfoEntity shopInfoEntity = shopMap.get(logisticsBillEntity.getShopId());
                if (Objects.nonNull(shopInfoEntity)) {
                    entity.setShopChargeId(shopInfoEntity.getChargeId());
                    entity.setShopChargeName(shopInfoEntity.getChargeName());
                }
            }
            entity.setType(DictCostAttributionEnum.FIRST_MILE.getCode());
            updateBillList.add(entity);
        }
        return updateBillList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateReconciliationDetailCount(String id, String sourceId, Integer reconciliationCount) {
        //头程对账单明细查询
        List<TmsFirstMileReconciliationDetailEntity> tmsFirstMileReconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIds(Collections.singletonList(sourceId), null);
        if (CollectionUtils.isEmpty(tmsFirstMileReconciliationDetailEntityList)){
            return;
        }
        Integer finalReconciliationCount = reconciliationCount;
        List<TmsFirstMileReconciliationDetailEntity> collect = tmsFirstMileReconciliationDetailEntityList.stream().filter(e -> Objects.nonNull(e)
                && StringUtils.isNotBlank(e.getMainId()) && !Objects.equals(e.getId(), id) && e.getReconciliationCount() > finalReconciliationCount)
                .sorted(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)){
            return;
        }

        collect.forEach(e -> {
            e.setReconciliationCount(e.getReconciliationCount() -1);
        });
        this.updateBatchById(collect);
    }

    @Override
    public void fillDetailList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList, String currency, CurrencyDTO.ViewDTO currencyViewDTO) {
        if (CollectionUtils.isEmpty(viewDTOList)) {
            return;
        }
        
        List<String> currencyIds = viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getShippingCostCurrency).collect(Collectors.toList());
        currencyIds.addAll(viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getDeclareCostCurrency).collect(Collectors.toList()));
        currencyIds.addAll(viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getOtherCostCurrency).collect(Collectors.toList()));
        currencyIds.addAll(viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getOtherTaxCurrency).collect(Collectors.toList()));
        
        Map<String, String> idSymbolMap = FeignQuery.getByIds(DictCurrencyEntity.class, currencyIds).stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
        
        //店铺信息
        List<String> shopIdList = viewDTOList.stream()
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getShopId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> shopMap = shopInfoFeign.listShopInfoByIds(shopIdList)
                .stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        //国家信息
        List<String> countryIdList = viewDTOList.stream()
                .flatMap(route -> Stream.of(route.getFromCountry(), route.getToCountry()))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(countryIdList)) {
            countryMap = sysDictFeign.listCountryByIds(countryIdList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }
        //发货单
        List<String> deliveryCodes = viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getRelationCode).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDTO.BusinessDTO> businessDTOList = wmsFirstMileDeliveryFeign.getBusinessCodeByCodes(deliveryCodes);
        // 计费方式
        List<String> channelIds = viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        Map<String, LogisticsChannelEntity> channelMap = logisticsChannelService.listByIds(channelIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        for (TmsFirstMileReconciliationDetailDTO.ListDTO viewDTO : viewDTOList) {
            // 计费方式
            LogisticsChannelEntity logisticsChannelEntity = channelMap.get(viewDTO.getLogisticsChannelId());
            if (null != logisticsChannelEntity) {
                viewDTO.setFeeRule(logisticsChannelEntity.getFeeRule());
                viewDTO.setFeeRuleName(ShippingFeeRuleEnum.getName(logisticsChannelEntity.getFeeRule()));
            } else {
                viewDTO.setFeeRule("");
                viewDTO.setFeeRuleName("");
            }
            if (StringUtils.isBlank(viewDTO.getSourceType())) {
                viewDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL.getCode());
            }
            //店铺名称
            viewDTO.setShopName(shopMap.getOrDefault(viewDTO.getShopId(), ""));
            //国家名称
            viewDTO.setFromCountryName(countryMap.getOrDefault(viewDTO.getFromCountry(), ""));
            viewDTO.setToCountryName(countryMap.getOrDefault(viewDTO.getToCountry(), ""));
            // 默认预计
            if (null == viewDTO.getType()) {
                viewDTO.setType(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            }
            viewDTO.setTypeName(DetailReconciliationTypeEnum.getNameByCode(viewDTO.getType()));

            // 运输状态
            FmLogisticTrackStatusEnum statusEnum = FmLogisticTrackStatusEnum.getNameByCode(viewDTO.getTransportStatus());
            viewDTO.setTransportStatusName(null == statusEnum ? "" : statusEnum.getName());

            // 对账状态
            viewDTO.setStatusName(ReconciliationStatusEnum.getName(viewDTO.getStatus()));
            // 明细币别
            if (StringUtils.isBlank(viewDTO.getCurrency())) {
                viewDTO.setCurrency(currency);
            }
            if (null != currencyViewDTO) {
                viewDTO.setCurrencySymbol(currencyViewDTO.getSymbol());
                viewDTO.setCurrencyName(currencyViewDTO.getName());
            } else {
                viewDTO.setCurrencySymbol("");
                viewDTO.setCurrencyName("");
            }
            // 默认
            if (null == viewDTO.getStatus()) {
                viewDTO.setStatus(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
                viewDTO.setStatusName(ReconciliationStatusEnum.TO_BE_CONFIRM.getName());
            }
            if (Objects.nonNull(viewDTO.getReconciliationCount())){
                if (Objects.equals(0, viewDTO.getReconciliationCount())){
                    viewDTO.setReconciliationCountName("");
                }else if (Objects.equals(1, viewDTO.getReconciliationCount())){
                    viewDTO.setReconciliationCountName("首次对账");
                }else {
                    viewDTO.setReconciliationCountName(viewDTO.getReconciliationCount()+"次对账");
                }
            }
            //业务单号查询逻辑修改 展示FBA发货单号和第三方货号-同期初展示逻辑
            FirstMileDeliveryDTO.BusinessDTO businessDTO = businessDTOList.stream().filter(e -> Objects.equals(e.getCode(), viewDTO.getRelationCode())).findFirst().orElse(null);
            if (Objects.nonNull(businessDTO)){
                viewDTO.setBusinessCode(businessDTO.getBusinessCode());
            }else {
                viewDTO.setBusinessCode("");
            }
            viewDTO.setShippingCostCurrencySymbol(idSymbolMap.get(viewDTO.getShippingCostCurrency()));
            viewDTO.setDeclareCostCurrencySymbol(idSymbolMap.get(viewDTO.getDeclareCostCurrency()));
            viewDTO.setOtherCostCurrencySymbol(idSymbolMap.get(viewDTO.getOtherCostCurrency()));
            viewDTO.setOtherTaxCurrencySymbol(idSymbolMap.get(viewDTO.getOtherTaxCurrency()));
        }
    }

    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> addWaitReconciliation(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        // 查询已签收（待对账）
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceList = tmsFirstMileLogisticService.listReconciliationByMainIds(sourceIds);
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        // 只显示已签收未生成对账单
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceFilterList = sourceList.stream()
                .filter(e -> e.getReconciliationStatus().equalsIgnoreCase(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()) && e.getTransportStatus().equalsIgnoreCase(FmLogisticTrackStatusEnum.SIGN.getCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceFilterList)) {
            return Collections.emptyList();
        }
        // 数据处理
        fillWaitReconciliationData(sourceFilterList);
        //查询生成对账明细记录
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = this.listBySourceIds(sourceIds, DetailReconciliationTypeEnum.ACTUAL.getCode());
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> resultList = new LinkedList<>();
        // 生成差异和对比数据
        for (TmsFirstMileReconciliationDetailDTO.ListDTO sourceListDTO : sourceFilterList) {
            //当前明细对账单次数
            int reconciliationCount = 1;
            TmsFirstMileReconciliationDetailEntity maxDetailEntity = detailEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getSourceId(),sourceListDTO.getSourceId()))
                    .max(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount)).orElse(null);
            if (Objects.nonNull(maxDetailEntity)){
                reconciliationCount = maxDetailEntity.getReconciliationCount() + 1;
            }
            // 根据预计DTO生成：预计, 实际, 差异
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> curAllTyoeList = generateAllTypeDTO(sourceListDTO, reconciliationCount,Boolean.FALSE);
            // 添加到结果
            resultList.addAll(curAllTyoeList);
        }
        return resultList;
    }

    /**
     * 根据预计DTO生成：预计, 实际, 差异
     */
    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> generateAllTypeDTO(TmsFirstMileReconciliationDetailDTO.ListDTO sourceListDTO,int reconciliationCount,boolean keepActual) {
        // 实际
        TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO = new TmsFirstMileReconciliationDetailDTO.ListDTO();
        BeanUtils.copyProperties(sourceListDTO, actualListDTO);
        actualListDTO.setShippingCost(BigDecimal.ZERO);
        actualListDTO.setDeclareCost(BigDecimal.ZERO);
        actualListDTO.setOtherCost(BigDecimal.ZERO);
        actualListDTO.setOtherTaxCost(BigDecimal.ZERO);
        // 总物流费用
        actualListDTO.setTotalLogisticsCost(BigDecimal.ZERO);
        // 实际重量【箱包装重量】（取物流单输入的重量）
        actualListDTO.setActualWeight(null == sourceListDTO.getWeightLogistics() ? BigDecimal.ZERO : sourceListDTO.getWeightLogistics());
        // 体积重 （取物流单输入的体积重）
        actualListDTO.setVolumeWeight(null == sourceListDTO.getVolumeWeightLogistics() ? BigDecimal.ZERO : sourceListDTO.getVolumeWeightLogistics());
        // 计费重
        actualListDTO.setBillingWeight(actualListDTO.getActualWeight().max(actualListDTO.getVolumeWeight()));
        actualListDTO.setType(DetailReconciliationTypeEnum.ACTUAL.getCode());
        actualListDTO.setTypeName(DetailReconciliationTypeEnum.ACTUAL.getName());

        // 差异
        TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO = new TmsFirstMileReconciliationDetailDTO.ListDTO();
        BeanUtils.copyProperties(sourceListDTO, diffListDTO);
        diffListDTO.setType(DetailReconciliationTypeEnum.DIFF.getCode());
        diffListDTO.setTypeName(DetailReconciliationTypeEnum.DIFF.getName());

        //首次对账正常取值预估和实际费用，N次对账则所有预计+实际费用取值为0
        if (!Objects.equals(1, reconciliationCount)){
            initSourceDataDTO(sourceListDTO);
            if (!keepActual){
                initSourceDataDTO(actualListDTO);
            }
        }
        // 计算差异值
        generateDiff(sourceListDTO, actualListDTO, diffListDTO);
        sourceListDTO.setReconciliationCount(reconciliationCount);
        actualListDTO.setReconciliationCount(reconciliationCount);
        diffListDTO.setReconciliationCount(reconciliationCount);
        return Arrays.asList(sourceListDTO, actualListDTO, diffListDTO);
    }

    private void initSourceDataDTO(TmsFirstMileReconciliationDetailDTO.ListDTO sourceListDTO) {
        sourceListDTO.setTotalLogisticsCost(BigDecimal.ZERO);
        sourceListDTO.setActualWeight(BigDecimal.ZERO);
        sourceListDTO.setVolumeWeight(BigDecimal.ZERO);
        sourceListDTO.setBillingWeight(BigDecimal.ZERO);
        sourceListDTO.setShippingCost(BigDecimal.ZERO);
        sourceListDTO.setDeclareCost(BigDecimal.ZERO);
        sourceListDTO.setOtherCost(BigDecimal.ZERO);
        sourceListDTO.setOtherTaxCost(BigDecimal.ZERO);
        sourceListDTO.setVolumeWeightLogistics(BigDecimal.ZERO);
        sourceListDTO.setWeightLogistics(BigDecimal.ZERO);
    }


    /**
     * 重新计算差异值并检查自动确认
     */
    private void generateDiffAndCheckConfirm(TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO,
                                             TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO,
                                             TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO) {
        // 重新计算实际计费重
        actualListDTO.setBillingWeight(actualListDTO.getActualWeight().max(actualListDTO.getVolumeWeight()));

        // 重新计算差异
        generateDiff(estimatedListDTO, actualListDTO, diffListDTO);
        // 比较值是否都一致
        boolean hasSame = checkHasSame(estimatedListDTO, actualListDTO);
        if (hasSame){
            estimatedListDTO.setStatus(ReconciliationStatusEnum.CONFIRMED.getCode());
            actualListDTO.setStatus(ReconciliationStatusEnum.CONFIRMED.getCode());
            diffListDTO.setStatus(ReconciliationStatusEnum.CONFIRMED.getCode());
        }
    }

    private boolean checkHasSame(TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO, TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO) {
        return 0 == estimatedListDTO.getTotalLogisticsCost().compareTo(actualListDTO.getTotalLogisticsCost())
                && 0 == estimatedListDTO.getShippingCost().compareTo(actualListDTO.getShippingCost())
                && 0 == estimatedListDTO.getDeclareCost().compareTo(actualListDTO.getDeclareCost())
                && 0 == estimatedListDTO.getOtherTaxCost().compareTo(actualListDTO.getOtherTaxCost())
                && 0 == estimatedListDTO.getOtherCost().compareTo(actualListDTO.getOtherCost());
    }

    /**
     * 重新计算差异值
     */
    private void generateDiff(
            TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO,
            TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO,
            TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO
    ) {
    	LocalDate receiveDate = estimatedListDTO.getReceiveDate();
    	if(receiveDate == null) {
    		receiveDate = actualListDTO.getReceiveDate();
    	}
    	if(receiveDate == null) {
    		receiveDate = diffListDTO.getReceiveDate();
    	}
		String currentDate = receiveDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DmpTaskFeign dmpTaskFeign = ApplicationContextUtils.getBean(DmpTaskFeign.class);
    	Map<String, BigDecimal> rateMap = new HashMap<>();
    	rateMap.put("CNY", BigDecimal.ONE);
    	
    	Map<String, String> idSymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol));
    	
        BigDecimal actualShippingCost = Objects.nonNull(actualListDTO.getShippingCost())? actualListDTO.getShippingCost() : BigDecimal.ZERO;
        BigDecimal actualShippingCostExchange = BigDecimal.ZERO;
        String currency = actualListDTO.getShippingCostCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	actualListDTO.setShippingCostCurrency(currency);
        }
        
        BigDecimal rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		actualShippingCostExchange = actualShippingCost.multiply(rate);
		actualListDTO.setShippingCostCurrencySymbol(idSymbolMap.get(currency));
        
        BigDecimal actualDeclareCost = Objects.nonNull(actualListDTO.getDeclareCost()) ? actualListDTO.getDeclareCost():BigDecimal.ZERO;
        BigDecimal actualDeclareCostExchange = BigDecimal.ZERO;
        currency = actualListDTO.getDeclareCostCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	actualListDTO.setDeclareCostCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		actualDeclareCostExchange = actualDeclareCost.multiply(rate);
		actualListDTO.setDeclareCostCurrencySymbol(idSymbolMap.get(currency));
        
        BigDecimal actualOtherCost = Objects.nonNull(actualListDTO.getOtherCost()) ? actualListDTO.getOtherCost():BigDecimal.ZERO;
        BigDecimal actualOtherCostExchange = BigDecimal.ZERO;
        currency = actualListDTO.getOtherCostCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	actualListDTO.setOtherCostCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		actualOtherCostExchange = actualOtherCost.multiply(rate);
		actualListDTO.setOtherCostCurrencySymbol(idSymbolMap.get(currency));
		
        BigDecimal actualOtherTaxCost = Objects.nonNull(actualListDTO.getOtherTaxCost()) ? actualListDTO.getOtherTaxCost():BigDecimal.ZERO;
        BigDecimal actualOtherTaxCostExchange = BigDecimal.ZERO;
        currency = actualListDTO.getOtherTaxCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	actualListDTO.setOtherTaxCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		actualOtherTaxCostExchange = actualOtherTaxCost.multiply(rate);
		actualListDTO.setOtherTaxCurrencySymbol(idSymbolMap.get(currency));
        
        BigDecimal actualWeight = Objects.nonNull(actualListDTO.getActualWeight()) ? actualListDTO.getActualWeight():BigDecimal.ZERO;
        BigDecimal actualVolumeWeight = Objects.nonNull(actualListDTO.getVolumeWeight()) ? actualListDTO.getVolumeWeight():BigDecimal.ZERO;
        BigDecimal actualBillingWeight = Objects.nonNull(actualListDTO.getBillingWeight()) ? actualListDTO.getBillingWeight():BigDecimal.ZERO;

        BigDecimal estimatedTotalLogisticsCost = Objects.nonNull(estimatedListDTO.getTotalLogisticsCost()) ? estimatedListDTO.getTotalLogisticsCost():BigDecimal.ZERO;
        BigDecimal estimatedShippingCost = Objects.nonNull(estimatedListDTO.getShippingCost()) ? estimatedListDTO.getShippingCost():BigDecimal.ZERO;
        BigDecimal estimatedShippingCostExchange = BigDecimal.ZERO;
        currency = estimatedListDTO.getShippingCostCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	estimatedListDTO.setShippingCostCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		estimatedShippingCostExchange = estimatedShippingCost.multiply(rate);
		estimatedListDTO.setShippingCostCurrencySymbol(idSymbolMap.get(currency));
		
        BigDecimal estimatedDeclareCost = Objects.nonNull(estimatedListDTO.getDeclareCost()) ? estimatedListDTO.getDeclareCost():BigDecimal.ZERO;
        BigDecimal estimatedDeclareCostExchange = BigDecimal.ZERO;
        currency = estimatedListDTO.getDeclareCostCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	estimatedListDTO.setDeclareCostCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		estimatedDeclareCostExchange = estimatedDeclareCost.multiply(rate);
		estimatedListDTO.setDeclareCostCurrencySymbol(idSymbolMap.get(currency));
        BigDecimal estimatedOtherCost = Objects.nonNull(estimatedListDTO.getOtherCost()) ? estimatedListDTO.getOtherCost():BigDecimal.ZERO;
        BigDecimal estimatedOtherCostExchange = BigDecimal.ZERO;
        currency = estimatedListDTO.getOtherCostCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	estimatedListDTO.setOtherCostCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		estimatedOtherCostExchange = estimatedOtherCost.multiply(rate);
		estimatedListDTO.setOtherCostCurrencySymbol(idSymbolMap.get(currency));
        BigDecimal estimatedOtherTaxCost = Objects.nonNull(estimatedListDTO.getOtherTaxCost()) ? estimatedListDTO.getOtherTaxCost():BigDecimal.ZERO;
        BigDecimal estimatedOtherTaxCostExchange = BigDecimal.ZERO;
        currency = estimatedListDTO.getOtherTaxCurrency();
        if(StringUtils.isBlank(currency)) {
        	currency = "CNY";
        	estimatedListDTO.setOtherTaxCurrency(currency);
        }
        rate = rateMap.get(currency);
		if(rate == null) {
			rate = dmpTaskFeign.getRate(currentDate, currency);
			if(rate == null) {
				log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
			}
			rateMap.put(currency, rate);
		}
		estimatedOtherTaxCostExchange = estimatedOtherTaxCost.multiply(rate);
		estimatedListDTO.setOtherTaxCurrencySymbol(idSymbolMap.get(currency));
        BigDecimal estimatedWeight = Objects.nonNull(estimatedListDTO.getActualWeight()) ? estimatedListDTO.getActualWeight():BigDecimal.ZERO;
        BigDecimal estimatedVolumeWeight = Objects.nonNull(estimatedListDTO.getVolumeWeight()) ? estimatedListDTO.getVolumeWeight():BigDecimal.ZERO;
        BigDecimal estimatedBillingWeight = Objects.nonNull(estimatedListDTO.getBillingWeight()) ? estimatedListDTO.getBillingWeight():BigDecimal.ZERO;

        // 重新计算实际总数
        actualListDTO.setTotalLogisticsCost(actualShippingCostExchange.add(actualDeclareCostExchange).add(actualOtherCostExchange).add(actualOtherTaxCostExchange).setScale(4, RoundingMode.DOWN));
        estimatedListDTO.setTotalLogisticsCost(estimatedShippingCostExchange.add(estimatedDeclareCostExchange).add(estimatedOtherCostExchange).add(estimatedOtherTaxCostExchange).setScale(4, RoundingMode.DOWN));
        // 重新计算差异值
        // 总物流费用
        // 实际重量【箱包装重量】
        diffListDTO.setActualWeight(actualWeight.subtract(estimatedWeight));
        // 体积重
        diffListDTO.setVolumeWeight(actualVolumeWeight.subtract(estimatedVolumeWeight));
        // 计费重
        diffListDTO.setBillingWeight(actualBillingWeight.subtract(estimatedBillingWeight));
        // 物流运费用【预计物流费用】
        diffListDTO.setShippingCost(actualShippingCostExchange.subtract(estimatedShippingCostExchange).setScale(4, RoundingMode.DOWN));
        diffListDTO.setShippingCostCurrency("CNY");
        diffListDTO.setShippingCostCurrencySymbol(idSymbolMap.get("CNY"));
        // 报关费用【预计报关费用】
        diffListDTO.setDeclareCost(actualDeclareCostExchange.subtract(estimatedDeclareCostExchange).setScale(4, RoundingMode.DOWN));
        diffListDTO.setDeclareCostCurrency("CNY");
        diffListDTO.setDeclareCostCurrencySymbol(idSymbolMap.get("CNY"));
        // 其他费用【预计其他费用】
        diffListDTO.setOtherCost(actualOtherCostExchange.subtract(estimatedOtherCostExchange).setScale(4, RoundingMode.DOWN));
        diffListDTO.setOtherCostCurrency("CNY");
        diffListDTO.setOtherCostCurrencySymbol(idSymbolMap.get("CNY"));
        //其他税费
        diffListDTO.setOtherTaxCost(actualOtherTaxCostExchange.subtract(estimatedOtherTaxCostExchange).setScale(4, RoundingMode.DOWN));
        diffListDTO.setOtherTaxCurrency("CNY");
        diffListDTO.setOtherTaxCurrencySymbol(idSymbolMap.get("CNY"));
        diffListDTO.setTotalLogisticsCost(actualListDTO.getTotalLogisticsCost().subtract(estimatedListDTO.getTotalLogisticsCost()));
    }


    private List<String> getDeleteIds(List<TmsFirstMileReconciliationDetailEntity> newList, List<TmsFirstMileReconciliationDetailEntity> oldList) {
        List<String> newIds = newList.stream()
                .map(TmsFirstMileReconciliationDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> oldIds = oldList.stream()
                .map(TmsFirstMileReconciliationDetailEntity::getId)
                .collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     *
     * @param list 修改后
     * @param mainId 对账单主表id
     * @param oldList 修改前
     * @return
     */
    @Override
    public Map<String, TmsFirstMileReconciliationDetailEntity> handleUpdateData(List<TmsFirstMileReconciliationDetailEntity> list, String mainId, List<TmsFirstMileReconciliationDetailEntity> oldList) {
        // 当前已有的明细
        // 按sourceId分组
        Map<String, Map<String, TmsFirstMileReconciliationDetailEntity>> sourceListMap = oldList.stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailEntity::getSourceId,
                        Collectors.toMap(TmsFirstMileReconciliationDetailEntity::getType, Function.identity())));

        // 统计预计费用
        Map<String, List<TmsFirstMileReconciliationDetailEntity>> sourceDetailMap = list.stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailEntity::getSourceId));

        // 查询对应物流单信息
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceList = tmsFirstMileLogisticService.listReconciliationByMainIds(new ArrayList<>(sourceDetailMap.keySet()));
        //兼容二次下推对账单场景
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> sourceMap = sourceList
                .stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId));
        // 校验状态
        for (Map.Entry<String, List<TmsFirstMileReconciliationDetailEntity>> entry : sourceDetailMap.entrySet()) {
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> listDTO = sourceMap.get(entry.getKey());
            if (CollectionUtils.isEmpty(listDTO)) {
                throw new ServiceException("物流单不存在,sourceId=" + entry.getKey());
            }
            if (!FmLogisticTrackStatusEnum.SIGN.getCode().equalsIgnoreCase(listDTO.get(0).getTransportStatus())) {
                throw new ServiceException("该物流单未签收完成,物流运单号=" + listDTO.get(0).getTransportNo());
            }
            if (CollectionUtils.isEmpty(sourceListMap)) {
                continue;
            }
            if (sourceListMap.containsKey(listDTO.get(0).getSourceId())) {
//                Map<String, TmsFirstMileReconciliationDetailEntity> oldEntityMap = sourceListMap.get(listDTO.getSourceId());
//                if (!CollectionUtils.isEmpty(oldEntityMap)) {
//                    // 数据库的明细
//                    TmsFirstMileReconciliationDetailEntity detailEntity = oldEntityMap.values().stream().findFirst().orElse(null);
//                    String status = detailEntity.getStatus();
//                    // 提交的明细
//                    List<TmsFirstMileReconciliationDetailEntity> detailList = sourceDetailMap.getOrDefault(listDTO.getSourceId(), new ArrayList<>());
//                    TmsFirstMileReconciliationDetailEntity sourceDetailEntity = detailList.stream().findFirst().orElse(null);
//                    if (null == sourceDetailEntity) {
//                        throw new ServiceException("数据异常:提交的明细为空");
//                    }
                    // 非确认状态校验
//                    if (ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equalsIgnoreCase(status) || status.equalsIgnoreCase(sourceDetailEntity.getStatus())) {
//                        continue;
//                    } else {
//                        throw new ServiceException("仅{待确认}可提交确认,物流运单号=" + listDTO.getTransportNo());
//                    }
//                }
                continue;
            }
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> collect = listDTO.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getReconciliationId()) && Objects.equals(e.getReconciliationId(), mainId) && !Objects.equals(e.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_GENERATED.getCode())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                throw new ServiceException("该物流单已生成对账单,物流运单号=" + listDTO.get(0).getTransportNo());
            }
        }
        // 补充基础信息
        fillWaitReconciliationList(sourceList, mainId);

        // 复制
        BeanUtils.copyProperties(sourceList, list);
        list.forEach(e ->
                e.setMainId(mainId)
        );

        // 返回实际信息
        return list.stream()
                .filter(e -> DetailReconciliationTypeEnum.ACTUAL.getCode().equalsIgnoreCase(e.getType()))
                .collect(Collectors.toMap(TmsFirstMileReconciliationDetailEntity::getSourceId, Function.identity()));
    }

    @Override
    public void fillWaitReconciliationList(List<? extends TmsFirstMileReconciliationDetailDTO.ListDTO> records, String mainId) {
        // 统计预计费用
        List<String> logisticsBillIds = records.stream()
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        // 物流费用
        List<LogisticsBillCostEntity> logisticsBillCostEntityAllList = logisticsBillCostService.listByLogisticsBillIdList(logisticsBillIds);
        //过滤符合要求的物流账单
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = getBillCostList(logisticsBillCostEntityAllList, mainId,logisticsBillIds);
        Map<String, String> billIdCostIdMap = logisticsBillCostEntityList
                .stream().filter(e -> Objects.nonNull(e) && (Objects.equals(mainId, e.getReconciliationId()) || CharSequenceUtil.isBlank(e.getReconciliationId())))
                .collect(Collectors.toMap(LogisticsBillCostEntity::getLogisticsBillId, BaseEntity::getId));

        List<String> costBillIds = logisticsBillCostEntityList.stream().filter(Objects::nonNull).map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
        List<TmsCostDetailEntity> costList = tmsCostDetailService.sumCostByMainIdAndCostId(LogisticsBillCostTypeEnum.ESTIMATED.getCode(),
                costBillIds,
                null
        );

        // 计费方式
        List<String> channelIds = records.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        Map<String, LogisticsChannelEntity> channelMap = logisticsChannelService.listByIds(channelIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        // 国家信息
        List<DictCountryDTO.ListDTO> conuntryList = sysUserFeign.countryList();

        // 发货单ID
        List<String> deliveryIds = records.stream()
                .filter(e -> SourceTypeEnum.DELIVERY_PLAN.getCode().equals(e.getSourceType()))
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getDeliveryId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<DictCountryDTO.ListDTO>> deliveryCountryMap = new HashMap<>();

        List<FirstMileDeliveryEntity> deliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        if (!CollectionUtils.isEmpty(deliveryEntityList)) {
            List<CfgAmzFulfillmentCenterEntity> centerEntityList = wmsFirstMileDeliveryFeign.getCfgAmzCenter();
            // 根据来源类型查询目的国家和发货国家
            // Map<发货单ID, List<目的国，发货国>>
            deliveryCountryMap = deliveryEntityList.stream()
                    .collect(Collectors.toMap(BaseEntity::getId, e -> this.checkAndFindCountry(e, centerEntityList, conuntryList)));
        }

        // 头程物流费用配置
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.FIRST_MILE.getCode());
        Map<String, List<TmsCfgCostEntity>> tmsCfgCostGroupMap = tmsCfgCostList
                .stream()
                .collect(Collectors.groupingBy(TmsCfgCostEntity::getDictCostCategory));
        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {
            record.setSourceType(SourceTypeEnum.LOGISTICS_BILL.getCode());
            // 出库单号=发货单号
            // 待对账类型都是预估
            if (null == record.getType()) {
                record.setType(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            }
            record.setTypeName(DetailReconciliationTypeEnum.getNameByCode(record.getType()));
            // 补充单位
            if (StringUtils.isBlank(record.getVolumeWeightUnit())) {
                record.setVolumeWeightUnit(record.getActualWeightUnit());
            }
            if (StringUtils.isBlank(record.getBillingWeightUnit())) {
                record.setBillingWeightUnit(record.getActualWeightUnit());
            }

            // 运输状态
            FmLogisticTrackStatusEnum statusEnum = FmLogisticTrackStatusEnum.getNameByCode(record.getTransportStatus());
            record.setTransportStatusName(null == statusEnum ? "" : statusEnum.getName());
            // 计费方式
            LogisticsChannelEntity logisticsChannelEntity = channelMap.get(record.getLogisticsChannelId());
            if (null != logisticsChannelEntity) {
                record.setFeeRule(logisticsChannelEntity.getFeeRule());
                record.setFeeRuleName(ShippingFeeRuleEnum.getName(logisticsChannelEntity.getFeeRule()));
            } else {
                record.setFeeRule("");
                record.setFeeRuleName("");
            }
            List<DictCountryDTO.ListDTO> listDTOS = deliveryCountryMap.getOrDefault(record.getDeliveryId(), this.defaultCountry(record.getToCountry(), conuntryList));
            DictCountryDTO.ListDTO toCountry = !listDTOS.isEmpty() ? listDTOS.get(0) : null;
            DictCountryDTO.ListDTO fromCountry = listDTOS.size() > 1 ? listDTOS.get(1) : null;
            record.setToCountry(null == toCountry ? "" : toCountry.getId());
            record.setToCountryName(null == toCountry ? "" : toCountry.getNameCn());
            record.setFromCountry(null == fromCountry ? "" : fromCountry.getId());
            record.setFromCountryName(null == fromCountry ? "" : fromCountry.getNameCn());

            if (DetailReconciliationTypeEnum.ESTIMATED.getCode().equalsIgnoreCase(record.getType())){
                handleEstimatedCost(record, billIdCostIdMap, costList, tmsCfgCostGroupMap);
            }

            // 对账状态
            String status = StringUtils.isBlank(record.getStatus()) ? ReconciliationStatusEnum.TO_BE_CONFIRM.getCode() : record.getStatus();
            record.setStatus(status);
            record.setStatusName(ReconciliationStatusEnum.getName(record.getStatus()));
        }

    }

    private List<LogisticsBillCostEntity> getBillCostList(List<LogisticsBillCostEntity> logisticsBillCostEntityAllList, String mainId, List<String> logisticsBillIds) {
        if (CollectionUtils.isEmpty(logisticsBillIds)){
            return Collections.emptyList();
        }
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = new ArrayList<>();
        for (String logisticsBillId : logisticsBillIds) {
            List<LogisticsBillCostEntity> billCostEntityList = logisticsBillCostEntityAllList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getLogisticsBillId()) && Objects.equals(e.getLogisticsBillId(), logisticsBillId)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(billCostEntityList)){
                LogisticsBillCostEntity billCostEntity = billCostEntityList.stream().filter(e -> (CharSequenceUtil.isBlank(mainId) && CharSequenceUtil.isBlank(e.getReconciliationId())) || (CharSequenceUtil.isNotBlank(e.getReconciliationId()) && Objects.equals(mainId, e.getReconciliationId()))).findFirst().orElse(null);
                if (Objects.nonNull(billCostEntity)){
                    logisticsBillCostEntityList.add(billCostEntity);
                }else {
                    //数据补偿，根据条件未匹配到费用项时，获取费用项列表符合条件的进行下推
                    LogisticsBillCostEntity billCostEntity1 = billCostEntityList.stream().filter(e -> CharSequenceUtil.isBlank(e.getReconciliationId())).findFirst().orElse(null);
                    if (Objects.nonNull(billCostEntity1)){
                        logisticsBillCostEntityList.add(billCostEntity1);
                    }
                }
            }
        }
        return logisticsBillCostEntityList;
    }

    @Override
    public void fillWaitReconciliationData(List<? extends TmsFirstMileReconciliationDetailDTO.ListDTO> records) {
        // 统计预计费用
        List<String> logisticsBillIds = records.stream()
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        // 物流费用 有物流单没有关联对账单数据
        List<LogisticsBillCostEntity> logisticsBillCostEntityAllList = logisticsBillCostService.listByLogisticsBillIdList(logisticsBillIds);
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = new ArrayList<>();
        for (String logisticsBillId : logisticsBillIds) {
            TmsFirstMileReconciliationDetailDTO.ListDTO listDTO = records.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getSourceId())
                    && Objects.equals(e.getSourceId(), logisticsBillId)).findFirst().orElse(null);
            if (Objects.isNull(listDTO)){
                continue;
            }
            LogisticsBillCostEntity billCostEntity = null;
            if (CharSequenceUtil.isBlank(listDTO.getReconciliationId())){
                billCostEntity = logisticsBillCostEntityAllList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getLogisticsBillId())
                        && Objects.equals(e.getLogisticsBillId(), logisticsBillId) && CharSequenceUtil.isBlank(e.getReconciliationId())).findFirst().orElse(null);
            }else {
                billCostEntity = logisticsBillCostEntityAllList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getLogisticsBillId())
                        && Objects.equals(e.getLogisticsBillId(), logisticsBillId)
                        && CharSequenceUtil.isNotBlank(e.getReconciliationId()) && Objects.equals(listDTO.getReconciliationId(), e.getReconciliationId())).findFirst().orElse(null);
            }
            if (Objects.nonNull(billCostEntity)){
                logisticsBillCostEntityList.add(billCostEntity);
            }
        }
        Map<String, String> billIdCostIdMap = logisticsBillCostEntityList
                .stream().collect(Collectors.toMap(LogisticsBillCostEntity::getLogisticsBillId, BaseEntity::getId));

        List<TmsCostDetailEntity> costList = tmsCostDetailService.sumCostByMainIdAndCostId(LogisticsBillCostTypeEnum.ESTIMATED.getCode(),
                billIdCostIdMap.values(),
                null
        );

        // 计费方式
        List<String> channelIds = records.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        Map<String, LogisticsChannelEntity> channelMap = null;
        if (!CollectionUtils.isEmpty(channelIds)){
            channelMap = logisticsChannelService.listByIds(channelIds)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        }

        // 国家信息
        List<DictCountryDTO.ListDTO> conuntryList = sysUserFeign.countryList();

        // 发货单ID
        List<String> deliveryIds = records.stream()
                .filter(e -> SourceTypeEnum.DELIVERY_PLAN.getCode().equals(e.getSourceType()))
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getDeliveryId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<DictCountryDTO.ListDTO>> deliveryCountryMap = new HashMap<>();

        List<FirstMileDeliveryEntity> deliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        if (!CollectionUtils.isEmpty(deliveryEntityList)) {
            List<CfgAmzFulfillmentCenterEntity> centerEntityList = wmsFirstMileDeliveryFeign.getCfgAmzCenter();
            // 根据来源类型查询目的国家和发货国家
            // Map<发货单ID, List<目的国，发货国>>
            deliveryCountryMap = deliveryEntityList.stream()
                    .collect(Collectors.toMap(BaseEntity::getId, e -> this.checkAndFindCountry(e, centerEntityList, conuntryList)));
        }

        // 头程物流费用配置
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.FIRST_MILE.getCode());
        Map<String, List<TmsCfgCostEntity>> tmsCfgCostGroupMap = tmsCfgCostList
                .stream()
                .collect(Collectors.groupingBy(TmsCfgCostEntity::getDictCostCategory));
        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {
            record.setSourceType(SourceTypeEnum.LOGISTICS_BILL.getCode());
            // 出库单号=发货单号
            // 待对账类型都是预估
            if (null == record.getType()) {
                record.setType(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            }
            record.setTypeName(DetailReconciliationTypeEnum.getNameByCode(record.getType()));
            // 补充单位
            if (StringUtils.isBlank(record.getVolumeWeightUnit())) {
                record.setVolumeWeightUnit(record.getActualWeightUnit());
            }
            if (StringUtils.isBlank(record.getBillingWeightUnit())) {
                record.setBillingWeightUnit(record.getActualWeightUnit());
            }

            // 运输状态
            FmLogisticTrackStatusEnum statusEnum = FmLogisticTrackStatusEnum.getNameByCode(record.getTransportStatus());
            record.setTransportStatusName(null == statusEnum ? "" : statusEnum.getName());
            // 计费方式
            LogisticsChannelEntity logisticsChannelEntity = null;
            if (Objects.nonNull(channelMap)){
                logisticsChannelEntity = channelMap.get(record.getLogisticsChannelId());
            }
            if (null != logisticsChannelEntity) {
                record.setFeeRule(logisticsChannelEntity.getFeeRule());
                record.setFeeRuleName(ShippingFeeRuleEnum.getName(logisticsChannelEntity.getFeeRule()));
            } else {
                record.setFeeRule("");
                record.setFeeRuleName("");
            }
            List<DictCountryDTO.ListDTO> listDTOS = deliveryCountryMap.getOrDefault(record.getDeliveryId(), this.defaultCountry(record.getToCountry(), conuntryList));
            DictCountryDTO.ListDTO toCountry = !listDTOS.isEmpty() ? listDTOS.get(0) : null;
            DictCountryDTO.ListDTO fromCountry = listDTOS.size() > 1 ? listDTOS.get(1) : null;
            record.setToCountry(null == toCountry ? "" : toCountry.getId());
            record.setToCountryName(null == toCountry ? "" : toCountry.getNameCn());
            record.setFromCountry(null == fromCountry ? "" : fromCountry.getId());
            record.setFromCountryName(null == fromCountry ? "" : fromCountry.getNameCn());

            if (DetailReconciliationTypeEnum.ESTIMATED.getCode().equalsIgnoreCase(record.getType())){
                handleEstimatedCost(record, billIdCostIdMap, costList, tmsCfgCostGroupMap);
            }

            // 对账状态
            String status = StringUtils.isBlank(record.getStatus()) ? ReconciliationStatusEnum.TO_BE_CONFIRM.getCode() : record.getStatus();
            record.setStatus(status);
            record.setStatusName(ReconciliationStatusEnum.getName(record.getStatus()));
        }

    }

    private static void handleEstimatedCost(TmsFirstMileReconciliationDetailDTO.ListDTO record, Map<String, String> billIdCostIdMap, List<TmsCostDetailEntity> costList, Map<String, List<TmsCfgCostEntity>> tmsCfgCostGroupMap) {
        // 物流费用ID
        String costId = billIdCostIdMap.getOrDefault(record.getSourceId(), "");

        // 按配置分组统计费用
        List<TmsCostDetailEntity> currentCostList = costList.stream()
                .filter(e -> e.getMainId().equalsIgnoreCase(costId))
                .collect(Collectors.toList());

        BigDecimal totalCost = BigDecimal.ZERO;
        String currentDate = record.getReceiveDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DmpTaskFeign dmpTaskFeign = ApplicationContextUtils.getBean(DmpTaskFeign.class);
        Map<String, BigDecimal> rateMap = new HashMap<>();
        rateMap.put("CNY", BigDecimal.ONE);
        // 物流运费
        List<String> shippingCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.SHIPPING_COST.getCode(), Collections.emptyList())
        		.stream().map(TmsCfgCostEntity::getId).collect(Collectors.toList());
        List<TmsCostDetailEntity> costDetailList = currentCostList.stream().filter(c -> shippingCostList.contains(c.getCfgCostId())).collect(Collectors.toList());
        BigDecimal shippingCost = costDetailList.stream()
        		.map(TmsCostDetailEntity::getCostValue)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        record.setShippingCost(shippingCost);
        if(CollUtil.isNotEmpty(costDetailList)) {
            String currency = costDetailList.get(0).getCurrency();
			record.setShippingCostCurrency(currency);
			BigDecimal rate = rateMap.get(currency);
			if(rate == null) {
				rate = dmpTaskFeign.getRate(currentDate, currency);
				if(rate == null) {
					log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                    throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
				}
				rateMap.put(currency, rate);
			}
			totalCost = totalCost.add(shippingCost.multiply(rate));
        }else {
            record.setShippingCostCurrency("CNY");
        }

        // 报关费
        List<String> declareCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.DECLARE_COST.getCode(), Collections.emptyList())
        		.stream().map(TmsCfgCostEntity::getId).collect(Collectors.toList());
        costDetailList = currentCostList.stream().filter(c -> declareCostList.contains(c.getCfgCostId())).collect(Collectors.toList());
        BigDecimal declareCost = costDetailList.stream()
        		.map(TmsCostDetailEntity::getCostValue)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        record.setDeclareCost(declareCost);
        if(CollUtil.isNotEmpty(costDetailList)) {
            String currency = costDetailList.get(0).getCurrency();
			record.setDeclareCostCurrency(currency);
			BigDecimal rate = rateMap.get(currency);
			if(rate == null) {
				rate = dmpTaskFeign.getRate(currentDate, currency);
				if(rate == null) {
					log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                    throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
				}
				rateMap.put(currency, rate);
			}
			totalCost = totalCost.add(shippingCost.multiply(rate));
        }else {
            record.setDeclareCostCurrency("CNY");
        }

        // 其他费用
        List<String> otherCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.OTHER_COST.getCode(), Collections.emptyList())
        		.stream().map(TmsCfgCostEntity::getId).collect(Collectors.toList());
        costDetailList = currentCostList.stream().filter(c -> otherCostList.contains(c.getCfgCostId())).collect(Collectors.toList());
        BigDecimal otherCost = costDetailList.stream()
        		.map(TmsCostDetailEntity::getCostValue)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        record.setOtherCost(otherCost);
        if(CollUtil.isNotEmpty(costDetailList)) {
            String currency = costDetailList.get(0).getCurrency();
			record.setOtherCostCurrency(currency);
			BigDecimal rate = rateMap.get(currency);
			if(rate == null) {
				rate = dmpTaskFeign.getRate(currentDate, currency);
				if(rate == null) {
					log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                    throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
				}
				rateMap.put(currency, rate);
			}
			totalCost = totalCost.add(shippingCost.multiply(rate));
        }else {
            record.setOtherCostCurrency("CNY");
        }

        //其他税费
        List<String> otherTaxCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.OTHER_TAX_FEE.getCode(), Collections.emptyList())
        		.stream().map(TmsCfgCostEntity::getId).collect(Collectors.toList());
        costDetailList = currentCostList.stream().filter(c -> otherTaxCostList.contains(c.getCfgCostId())).collect(Collectors.toList());
        BigDecimal otherTaxCost = costDetailList.stream()
        		.map(TmsCostDetailEntity::getCostValue)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        record.setOtherTaxCost(otherTaxCost);
        if(CollUtil.isNotEmpty(costDetailList)) {
            String currency = costDetailList.get(0).getCurrency();
			record.setOtherTaxCurrency(currency);
			BigDecimal rate = rateMap.get(currency);
			if(rate == null) {
				rate = dmpTaskFeign.getRate(currentDate, currency);
				if(rate == null) {
					log.error("币别【{}】,汇率为空，请维护汇率后再提交",currency);
                    throw new ServiceException(currency + "汇率为空，请维护汇率后再提交");
				}
				rateMap.put(currency, rate);
			}
			totalCost = totalCost.add(shippingCost.multiply(rate));
        }else {
            record.setOtherTaxCurrency("CNY");
        }

        // 合计费用
        record.setTotalLogisticsCost(totalCost.setScale(4, RoundingMode.DOWN));
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity) {
        // 验证数据 & 数据赋值
    }

    private TmsFirstMileReconciliationDetailDTO.ImportDTO importStandardFile(MultipartFile excelFile, TmsFirstMileReconciliationEntity mainEntity) {
        FirstMileReconciliationStandardExcelListener excelListenerUtil = new FirstMileReconciliationStandardExcelListener(mainEntity);
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileReconciliationStandardExcelDTO.class, excelListenerUtil).sheet(0).headRowNumber(2) .doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        TmsFirstMileReconciliationDetailDTO.ImportDTO importDTO = new TmsFirstMileReconciliationDetailDTO.ImportDTO();
        //导入数据处理
        List<FirstMileReconciliationStandardExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<FirstMileReconciliationStandardExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理数据(对相同序号的数据进行成本分摊和重量分摊)
        List<FirstMileReconciliationStandardExcelDTO> handlerList = splitWeightAndCost(successList, errorList, mainEntity);
        //导入数据保存
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> successImortList = handleImportStandardData(handlerList, errorList, mainEntity);

        String url = "";
        if (!CollectionUtils.isEmpty(errorList)) {
            String fileName = "头程对账单错误数据.xlsx";
            // 根据 no 进行数值排序
            errorList.sort(Comparator.comparingInt(dto -> Integer.parseInt(dto.getNo())));
            File file = ExcelUtil.exportFile(fileName, "error", errorList, FirstMileReconciliationStandardExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successImortList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }


    /**
     * 标准版导入数据处理
     */
    private List<TmsFirstMileReconciliationDetailDTO.ListDTO> handleImportStandardData(List<FirstMileReconciliationStandardExcelDTO> successList,
                                                                                       List<FirstMileReconciliationStandardExcelDTO> errorList,
                                                                                       TmsFirstMileReconciliationEntity mainEntity) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.emptyList();
        }

        // 结果
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> resultMap = new HashMap<>();
        // 币种
        String currency = mainEntity.getCurrency();
        CurrencyDTO.ViewDTO currencyView = this.getCurrencyView(currency);
        // 对应物流单(可能未保存)
        List<String> transportNoList = successList.stream().map(FirstMileReconciliationStandardExcelDTO::getTransportNo).distinct().collect(Collectors.toList());

        // 查询原物流单信息
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceLogisticList = tmsFirstMileLogisticService.listByTransportNoListAndSupplierIds(
                transportNoList,
                Collections.singletonList(mainEntity.getLogisticsSupplierId()));
        sourceLogisticList = sourceLogisticList.stream().filter(e -> Objects.equals(mainEntity.getId(), e.getReconciliationId())).collect(Collectors.toList());
        // 补充来源信息
        this.fillWaitReconciliationData(sourceLogisticList);
        // 物流跟踪单
        // 原对数据库账明细信息
        List<TmsFirstMileReconciliationDetailEntity> oldDetailList = this.listByMainIds(Collections.singletonList(mainEntity.getId()));

        List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailDTO.ListDTO.class, oldDetailList);
        // 补充基础信息
        this.fillDetailList(viewDTOList, currency, currencyView);
        // 按分组Map<物流运单号, 当前明细数组>
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> oldDbGroupMap = viewDTOList
                .stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTransportNo));

        //配置信息
        Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> cfgErpFieldMap = cfgReconciliationFieldService.erpFieldList(Collections.singletonList(CfgReconciliationTypeEnum.FIRST_MILE.getCode()))
                .stream()
                .collect(Collectors.toMap(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getErpFieldName, Function.identity()));

        // 历史的明细费用ID
        Map<String, Map<String, TmsCostDetailDTO.UpdateDTO>> costDetailMap = this.convertUpdateDTOAndMap(oldDetailList);

        checkAndConvertResult(successList, errorList, sourceLogisticList, resultMap, oldDbGroupMap, cfgErpFieldMap, costDetailMap, mainEntity);

        return resultMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    //执行重量分摊以及费用分摊
    private List<FirstMileReconciliationStandardExcelDTO> splitWeightAndCost(
            List<FirstMileReconciliationStandardExcelDTO> successList,
            List<FirstMileReconciliationStandardExcelDTO> errorList,
            TmsFirstMileReconciliationEntity mainEntity) {

        // 重量校验：当存在一致的序号时，是否有装箱重量
        if (CollUtil.isEmpty(successList)) {
            return Collections.emptyList();
        }

        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.FIRST_MILE.getCode());
        if (CollUtil.isEmpty(tmsCfgCostList)) {
            throw new ServiceException("费用项未配置，请联系管理员");
        }

        Map<String, String> costCategoryMap = groupCostCategories(tmsCfgCostList);
        List<String> costNameList = successList.stream().map(FirstMileReconciliationStandardExcelDTO::getCostName).distinct().collect(Collectors.toList());
        boolean b = costNameList.stream().allMatch(e -> costCategoryMap.containsKey(e));
        if (Boolean.FALSE.equals(b)) {
            throw new ServiceException("费用项未配置，请联系管理员");
        }

        CfgSettingValueDTO.AllocationSettingDTO allocationSetting = cfgSettingService.getCfgSettingByAllocationSetting();
        if (Objects.isNull(allocationSetting)) {
            throw new ServiceException("分摊设置未配置，请联系管理员");
        }
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.BUILD_DONGGUANG.getCode());
        if (Objects.isNull(cfgSettingEntity)) {
            throw new ServiceException("sku成本来源设置未配置，请联系管理员");
        }
        CfgSettingValueDTO.DongGuanSettingDTO dongGuanSetting = JSON.parseObject(String.valueOf(cfgSettingEntity.getDataJson()), CfgSettingValueDTO.DongGuanSettingDTO.class);

        // 发货单code集合
        List<String> sourceCodeList = successList.stream().map(FirstMileReconciliationStandardExcelDTO::getSourceCode).distinct().collect(Collectors.toList());
        // 物流单号集合
        List<LogisticsBillEntity> logisticsBillList = tmsFirstMileLogisticService.lambdaQuery().in(LogisticsBillEntity::getOutstockCode, sourceCodeList).list();
        Map<String, String> firstMileLogisticMap = logisticsBillList.stream().collect(Collectors.toMap(LogisticsBillEntity::getOutstockCode, LogisticsBillEntity::getChannelId, (k1, k2) -> k1));

        //发货单维度下的物流单计费重之和
        Map<String, BigDecimal> weightMap = new HashMap<>();
        //来源：头程物流单下推对账单计费重逻辑
        List<String> logisticsBillIdList = logisticsBillList.stream().map(LogisticsBillEntity::getId).collect(Collectors.toList());
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDetailList = tmsFirstMileLogisticService.listReconciliationByMainIds(logisticsBillIdList);
        //key：发货单号 value 预估账单（的计费重）
        Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO> estimatedMap = sourceDetailList.stream().collect(Collectors.toMap(TmsFirstMileReconciliationDetailDTO.ListDTO::getRelationCode, t -> t, (k1, k2) -> k1));
        for (Map.Entry<String, TmsFirstMileReconciliationDetailDTO.ListDTO> entry : estimatedMap.entrySet()) {
            String sourceCode = entry.getKey();
            TmsFirstMileReconciliationDetailDTO.ListDTO listDTO = entry.getValue();
            if (Objects.nonNull(listDTO.getWeightLogistics()) //实际重量【箱包装重量】
                    && Objects.nonNull(listDTO.getVolumeWeightLogistics())//体积重
            ) {
                weightMap.put(sourceCode, listDTO.getActualWeight().max(listDTO.getVolumeWeight()));
            }else if(Objects.nonNull(listDTO.getWeightLogistics())){
                weightMap.put(sourceCode, listDTO.getWeightLogistics());
            }else if(Objects.nonNull(listDTO.getVolumeWeightLogistics())){
                weightMap.put(sourceCode, listDTO.getVolumeWeightLogistics());
            }
        }


        //来源：头程物流单详情计费重逻辑
//        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTOS = wmsFirstMileDeliveryFeign.listGenerateLogisticDTO(sourceCodeList);
//        List<TmsFirstMileLogisticDTO.DeliveryDTO> deliveryDTOList = BeanUtil.copyToList(generateLogisticDTOS,TmsFirstMileLogisticDTO.DeliveryDTO.class);
//        if(CollUtil.isNotEmpty(deliveryDTOList)){
//            for (TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO : deliveryDTOList) {
//                if(CollUtil.isNotEmpty(deliveryDTO.getPackingDTOList())){
//                    LogisticsChannelEntity channelEntity = logisticsChannelService.getById(firstMileLogisticMap.get(deliveryDTO.getOutstockCode()));
//                    if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() != null && channelEntity.getVolumeSetting() > 0){
//                        deliveryDTO.getPackingDTOList().forEach(v -> {
//                            v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()), 4, RoundingMode.HALF_UP));
//                        });
//                    }
//
//                    BigDecimal volumeWeight = BigDecimal.ZERO;
//                    BigDecimal packageWeight = BigDecimal.ZERO;
//                    for (TmsFirstMileLogisticDTO.PackingDTO dto : deliveryDTO.getPackingDTOList()) {
//                        if(Objects.nonNull(dto.getVolumeWeight())){
//                            volumeWeight = volumeWeight.add(dto.getVolumeWeight());
//                        }
//                        if(StringUtils.isNotBlank(dto.getPackageWeight())){
//                            packageWeight = packageWeight.add(new BigDecimal(dto.getPackageWeight()));
//                        }
//
//                    }
//                    if(volumeWeight.compareTo(packageWeight) > 0){
//                        weightMap.put(deliveryDTO.getOutstockCode(), volumeWeight);
//                    }else {
//                        weightMap.put(deliveryDTO.getOutstockCode(), packageWeight);
//                    }
//                }
//            }
//        }

        //发货单明细
        List<FirstMileDeliveryDTO.ListFirstMileDTO> firstMileDetailList = wmsFirstMileDeliveryFeign.listDetailByCodes(sourceCodeList);
        List<String> skuIdList = firstMileDetailList.stream().map(FirstMileDeliveryDTO.ListFirstMileDTO::getSkuId).distinct().collect(Collectors.toList());
        //sku的毛重
        Map<String, BigDecimal> skuIdToGrossWeightMap = productPackFeign.listSingleBySkuIds(skuIdList);

        //序号唯一的设置到结果集合里
        Map<String, List<FirstMileReconciliationStandardExcelDTO>> successMap = successList.stream()
                .collect(Collectors.groupingBy(FirstMileReconciliationStandardExcelDTO::getNo));

        List<FirstMileReconciliationStandardExcelDTO> resultList = new ArrayList<>();

        for (Map.Entry<String, List<FirstMileReconciliationStandardExcelDTO>> entry : successMap.entrySet()) {
            List<FirstMileReconciliationStandardExcelDTO> value = entry.getValue();
            if(value.size() <= 1){
                resultList.addAll(value);
                continue;
            }
            //错误信息
            List<String> errorMsgList = new ArrayList<>();
            //同一序号下：来源单号不能重复
            int sourceCodeCount = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getSourceCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList()).size();
            if(value.size() != sourceCodeCount){
                errorMsgList.add("同一序号下，来源单号不能相同");
            }

            //同一序号下：业务单号不能重复
            int businessCodeCount = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getBusinessCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList()).size();
            if(value.size() != businessCodeCount){
                errorMsgList.add("同一序号下，业务单号不能相同");
            }

            // 实际实重（若有），实际体积重（若有），费用项，费用金额，币种一致
            //费用金额
            boolean allSkuCostsEqual = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getCostValue)
                    .distinct()
                    .limit(2) // 如果有超过一个不同的值，则不需要继续检查
                    .count() <= 1;
            //币种
            boolean allCurrencyEqual = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getCurrency)
                    .distinct()
                    .limit(2) // 如果有超过一个不同的值，则不需要继续检查
                    .count() <= 1;
            //实际实重
            String actualWeightStr = value.stream().map(FirstMileReconciliationStandardExcelDTO::getActualWeight).filter(StringUtils::isNotBlank).findFirst().orElse(null);
            boolean allActualWeightEqual = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getActualWeight)
                    .distinct()
                    .limit(2) // 如果有超过一个不同的值，则不需要继续检查
                    .count() <= 1;
            //实际体积重
            String volumeWeightStr = value.stream().map(FirstMileReconciliationStandardExcelDTO::getVolumeWeight).filter(StringUtils::isNotBlank).findFirst().orElse(null);
            boolean allVolumeWeightEqual = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getVolumeWeight)
                    .distinct()
                    .limit(2) // 如果有超过一个不同的值，则不需要继续检查
                    .count() <= 1;
            //费用项
            String costName = value.stream().map(FirstMileReconciliationStandardExcelDTO::getCostName).filter(StringUtils::isNotBlank).findFirst().orElse(null);
            boolean allCostNameEqual = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getCostName)
                    .distinct()
                    .limit(2) // 如果有超过一个不同的值，则不需要继续检查
                    .count() <= 1;
            if(Boolean.FALSE.equals(allSkuCostsEqual)
                    ||Boolean.FALSE.equals(allCurrencyEqual)
                    ||Boolean.FALSE.equals(allActualWeightEqual)
                    ||Boolean.FALSE.equals(allVolumeWeightEqual)
                    ||Boolean.FALSE.equals(allCostNameEqual)){
                errorMsgList.add("相同序号单据的实际实重，实际体积重，费用项，费用金额，币种不一致");
            }
            //费用分摊类型
            Boolean hasWeightAllocation = Boolean.FALSE;
            Boolean hasCostAllocation = Boolean.FALSE;
            //分摊方式
            String allocationType ="";
            String dictCostCategory = costCategoryMap.get(costName);
            switch (AllocationFeeTypeEnum.getByCode(dictCostCategory)) {
                case SHIPPING_COST:
                    if(allocationSetting.getFirstShippingCost().equals(CostAllocationEnum.WEIGHT_ALLOCATION.getCode())) {
                        hasWeightAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.WEIGHT_ALLOCATION.getCode();
                    }else {
                        hasCostAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.COST_ALLOCATION.getCode();
                    }
                    break;
                case DECLARE_COST:
                    if(allocationSetting.getFirstTariffFee().equals(CostAllocationEnum.WEIGHT_ALLOCATION.getCode())) {
                        hasWeightAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.WEIGHT_ALLOCATION.getCode();
                    }else {
                        hasCostAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.COST_ALLOCATION.getCode();
                    }
                    break;
                case OTHER_TAX_FEE:
                    if(allocationSetting.getFirstOtherTaxFee().equals(CostAllocationEnum.WEIGHT_ALLOCATION.getCode())) {
                        hasWeightAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.WEIGHT_ALLOCATION.getCode();
                    }else {
                        hasCostAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.COST_ALLOCATION.getCode();
                    }
                    break;
                case OTHER_COST:
                    if(allocationSetting.getFirstOtherFee().equals(CostAllocationEnum.WEIGHT_ALLOCATION.getCode())) {
                        hasWeightAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.WEIGHT_ALLOCATION.getCode();
                    }else {
                        hasCostAllocation = Boolean.TRUE;
                        allocationType = CostAllocationEnum.COST_ALLOCATION.getCode();
                    }
                    break;
                default:
                    // 处理未知费用类型的情况
                    break;
            }

            //同一序号下：若存在实际实重、实际体积重不为空
            if(StringUtils.isNotBlank(volumeWeightStr)
                    || StringUtils.isNotBlank(actualWeightStr)){
                hasWeightAllocation = Boolean.TRUE;
            }

            //同一序号下：若存在实际实重、实际体积重、分摊设置为重量分摊则需要校验装箱重量（来源：头程物流单计费重之和(取每个箱号中体积重量、包装重量较大者)），并且根据重量比例进行计算
            //粗略判断是否有设置成本分摊的方式
            String sourceCode = value.stream().map(FirstMileReconciliationStandardExcelDTO::getSourceCode).filter(StringUtils::isNotBlank).findFirst().orElse(null);
            if(hasWeightAllocation
               && (!weightMap.containsKey(sourceCode) || weightMap.getOrDefault(sourceCode,BigDecimal.ZERO).compareTo(BigDecimal.ZERO)<= 0)
            ){
                errorMsgList.add("相同序号单据的装箱重量不存在");
            }
            Map<String, BigDecimal> deliveryCodeCostMap;
            Map<String, BigDecimal> deliveryCodeWeightMap;
            if(hasWeightAllocation){
                //重量分摊逻辑
                //发货单的重量之和
                deliveryCodeWeightMap = calculateDeliveryCodeWeight(allocationSetting, errorMsgList,firstMileDetailList,skuIdToGrossWeightMap, weightMap);
                value.forEach(e -> e.setGrossWeigh(deliveryCodeWeightMap.getOrDefault(e.getSourceCode(), BigDecimal.ZERO)));
            }

            if(hasCostAllocation){
                //成本分摊逻辑
                InventorySkuCostDTO.QueryB2BDTO queryB2BDTO = buildQueryB2BDTO(skuIdList, dongGuanSetting ,mainEntity.getReconciliationMonth());
                List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = inventorySkuCostService.listSkuCostBySkuIds(queryB2BDTO);
                if(CollUtil.isEmpty(skuCostDTOS)){
                    errorMsgList.add("sku成本不能为空");
                }
                Map<String, BigDecimal> skuCostMap = skuCostDTOS.stream()
                        .collect(Collectors.toMap(InventorySkuCostDTO.SkuCostDTO::getSkuId, InventorySkuCostDTO.SkuCostDTO::getProductCost,(o1,o2)->o1));
                //发货单的sku成本之和
                deliveryCodeCostMap = calculateDeliveryCodeCost(errorMsgList,firstMileDetailList, skuCostMap);
                value.forEach(e -> e.setSkuCost(deliveryCodeCostMap.getOrDefault(e.getSourceCode(), BigDecimal.ZERO)));
            }

            if (CollUtil.isNotEmpty(errorMsgList)) {
                value.forEach(e->{
                    e.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(e);
                });
                continue;
            }

            value.sort(Comparator.comparing(FirstMileReconciliationStandardExcelDTO::getGrossWeigh));
            //发货单的重量之和
            BigDecimal totalWeightByCode = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getGrossWeigh)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            //发货单的sku成本之和
            BigDecimal totalCostByCode = value.stream()
                    .map(FirstMileReconciliationStandardExcelDTO::getSkuCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            //最后剩余
            BigDecimal leftActualWeight = BigDecimal.ZERO;
            BigDecimal leftVolumeWeight= BigDecimal.ZERO;
            BigDecimal leftCostValue = BigDecimal.ZERO;

            for (int i = 0; i < value.size(); i++) {
                FirstMileReconciliationStandardExcelDTO excelDTO = value.get(i);
                BigDecimal weightRate = BigDecimal.ZERO;
                if (i == value.size() - 1) {
                    if(hasWeightAllocation){
                        if(StringUtils.isNotBlank(excelDTO.getActualWeight())){
                            //实重
                            BigDecimal actualWeight = MathUtil.getBigDecimalByStr(excelDTO.getActualWeight());
                            excelDTO.setActualWeight(actualWeight.subtract(leftActualWeight).toString());
                        }
                        if(StringUtils.isNotBlank(excelDTO.getVolumeWeight())){
                            //实际体积重
                            BigDecimal volumeWeight = MathUtil.getBigDecimalByStr(excelDTO.getVolumeWeight());
                            excelDTO.setVolumeWeight(volumeWeight.subtract(leftVolumeWeight).toString());
                        }
                    }
                    //费用金额
                    BigDecimal costValue = MathUtil.getBigDecimalByStr(excelDTO.getCostValue());
                    excelDTO.setCostValue(costValue.subtract(leftCostValue).toString());
                }else{
                    if(hasWeightAllocation){
                        weightRate = excelDTO.getGrossWeigh().divide(totalWeightByCode, 8, RoundingMode.DOWN);

                        if(StringUtils.isNotBlank(excelDTO.getActualWeight())){
                            //实重
                            BigDecimal actualWeight = MathUtil.getBigDecimalByStr(excelDTO.getActualWeight());
                            BigDecimal dtoActualWeight = actualWeight.multiply(weightRate).setScale(4, RoundingMode.DOWN);
                            leftActualWeight = leftActualWeight.add(dtoActualWeight);
                            excelDTO.setActualWeight(dtoActualWeight.toString());
                        }
                        if(StringUtils.isNotBlank(excelDTO.getVolumeWeight())){
                            //实际体积重
                            BigDecimal volumeWeight = MathUtil.getBigDecimalByStr(excelDTO.getVolumeWeight());
                            BigDecimal dtoVolumeWeight = volumeWeight.multiply(weightRate).setScale(4, RoundingMode.DOWN);
                            leftVolumeWeight = leftVolumeWeight.add(dtoVolumeWeight);
                            excelDTO.setVolumeWeight(dtoVolumeWeight.toString());
                        }
                    }
                    //费用金额
                    BigDecimal costValue = MathUtil.getBigDecimalByStr(excelDTO.getCostValue());
                    BigDecimal dtoCostValue = setCostValueByCostCategory(allocationType,costValue,weightRate,excelDTO.getSkuCost() , totalCostByCode);
                    leftCostValue = leftCostValue.add(dtoCostValue);
                    excelDTO.setCostValue(dtoCostValue.toString());
                }
                resultList.add(excelDTO);
            }
        }
        return resultList;
    }

    // 提取公共逻辑：计算发货单重量
    private Map<String, BigDecimal> calculateDeliveryCodeWeight(CfgSettingValueDTO.AllocationSettingDTO cfgSetting,
                                                                List<String> errorMsgList,
                                                                List<FirstMileDeliveryDTO.ListFirstMileDTO> firstMileDetailList,
                                                                Map<String, BigDecimal> skuIdToGrossWeightMap ,
                                                                Map<String, BigDecimal> weightMap) {
        //出库计费重分摊方式
        if(cfgSetting.getWeightFirstAllocation().equals(WeightAllocationSmallBagEnum.OUTSTOCK_CHARGED_WEIGHT.getCode())){
            return weightMap;
        }

        //单产品重量分摊方式
        Map<String, BigDecimal> deliveryCodeWeightMap = new HashMap<>();
        Map<String, List<FirstMileDeliveryDTO.ListFirstMileDTO>> collect = firstMileDetailList.stream().collect(Collectors.groupingBy(FirstMileDeliveryDTO.ListFirstMileDTO::getSourceCode));
        for (Map.Entry<String, List<FirstMileDeliveryDTO.ListFirstMileDTO>> entry : collect.entrySet()) {
            List<FirstMileDeliveryDTO.ListFirstMileDTO> value = entry.getValue();
            BigDecimal totalWeight = BigDecimal.ZERO;
            StringBuffer sb = new  StringBuffer();
            //单产品重量分摊方式
            if (cfgSetting.getWeightFirstAllocation().equals(WeightAllocationSmallBagEnum.SINGLE_PRODUCT_WEIGHT.getCode())) {
                for (FirstMileDeliveryDTO.ListFirstMileDTO dto : value) {
                    BigDecimal orDefault = skuIdToGrossWeightMap.getOrDefault(dto.getSkuId(), BigDecimal.ZERO);
                    if(orDefault.compareTo(BigDecimal.ZERO) <= 0){
                        sb.append(dto.getSkuNo());
                        sb.append(";");
                    }else{
                        totalWeight = totalWeight.add(orDefault.multiply(BigDecimal.valueOf(dto.getDeliveryQty())));
                    }
                }
            }

            if(StringUtils.isNotBlank(sb.toString())){
                errorMsgList.add("发货单【"+entry.getKey()+"】下的【"+sb.toString()+"】SKU成本不存在");
            }
            deliveryCodeWeightMap.put(entry.getKey(), totalWeight);
        }
        return deliveryCodeWeightMap;
    }


    // 提取公共逻辑：分组物流费用类别
    private Map<String, String> groupCostCategories(List<TmsCfgCostEntity> tmsCfgCostList) {
        return tmsCfgCostList.stream().collect(Collectors.toMap(TmsCfgCostEntity::getCostName, TmsCfgCostEntity::getDictCostCategory, (o1, o2) -> o1));
    }

    // 提取公共逻辑：构建查询参数
    private InventorySkuCostDTO.QueryB2BDTO buildQueryB2BDTO(List<String> skuIdList,CfgSettingValueDTO.DongGuanSettingDTO dongGuanSetting, LocalDate reconciliationMonth) {
        InventorySkuCostDTO.QueryB2BDTO queryB2BDTO = new InventorySkuCostDTO.QueryB2BDTO();
        queryB2BDTO.setSkuIds(skuIdList);
        queryB2BDTO.setWarehouseId(dongGuanSetting.getWarehouseId());
        queryB2BDTO.setSalesOrgId(dongGuanSetting.getCompanyId());
        queryB2BDTO.setBillDate(reconciliationMonth);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String month = reconciliationMonth.format(formatter);
        queryB2BDTO.setMonth(month);
        return queryB2BDTO;
    }

    // 提取公共逻辑：计算发货单成本
    private Map<String, BigDecimal> calculateDeliveryCodeCost(List<String> errorMsgList,List<FirstMileDeliveryDTO.ListFirstMileDTO> firstMileDetailList, Map<String, BigDecimal> skuCostMap) {
        Map<String, BigDecimal> deliveryCodeCostMap = new HashMap<>();
        Map<String, List<FirstMileDeliveryDTO.ListFirstMileDTO>> collect = firstMileDetailList.stream().collect(Collectors.groupingBy(FirstMileDeliveryDTO.ListFirstMileDTO::getSourceCode));
        for (Map.Entry<String, List<FirstMileDeliveryDTO.ListFirstMileDTO>> entry : collect.entrySet()) {
            List<FirstMileDeliveryDTO.ListFirstMileDTO> value = entry.getValue();
            BigDecimal totalCost = BigDecimal.ZERO;
            StringBuffer sb = new  StringBuffer();
            for (FirstMileDeliveryDTO.ListFirstMileDTO dto : value) {
                BigDecimal orDefault = skuCostMap.getOrDefault(dto.getSkuId(), BigDecimal.ZERO);
                if(orDefault.compareTo(BigDecimal.ZERO) <= 0){
                    sb.append(dto.getSkuNo());
                    sb.append(";");
                }else{
                    totalCost = totalCost.add(orDefault.multiply(BigDecimal.valueOf(dto.getDeliveryQty())));
                }
            }
            if(StringUtils.isNotBlank(sb.toString())){
                errorMsgList.add("发货单【"+entry.getKey()+"】下的【"+sb.toString()+"】SKU成本不存在");
            }
            deliveryCodeCostMap.put(entry.getKey(), totalCost);
        }
        return deliveryCodeCostMap;
    }

    /**
     * 根据分摊方式计算费用金额
     */
    private static BigDecimal setCostValueByCostCategory(String allocationType,BigDecimal costValue,BigDecimal weightRate,BigDecimal skuCost ,BigDecimal totalCostByCode) {
        BigDecimal dtoCostValue = BigDecimal.ZERO;
        if(allocationType.equals(CostAllocationEnum.WEIGHT_ALLOCATION.getCode())){
            dtoCostValue = costValue.multiply(weightRate).setScale(4, RoundingMode.DOWN);
        }else {
            BigDecimal costRate = skuCost.divide(totalCostByCode, 4, RoundingMode.DOWN);
            dtoCostValue = costValue.multiply(costRate).setScale(4, RoundingMode.DOWN);
        }
        return dtoCostValue;
    }

    private void checkAndConvertResult(List<FirstMileReconciliationStandardExcelDTO> successList, List<FirstMileReconciliationStandardExcelDTO> errorList,
                                       List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceLogisticList,
                                       Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> resultMap,
                                       Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> oldDbGroupMap,
                                       Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> cfgErpFieldMap,
                                       Map<String, Map<String, TmsCostDetailDTO.UpdateDTO>> costDetailMap,
                                       TmsFirstMileReconciliationEntity mainEntity) {
    	if(CollUtil.isEmpty(successList)) {
    		return;
    	}
    	// 配置来源分组
        Map<String, List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO>> sourceTypeGroupMap = cfgErpFieldMap.values()
                .stream()
                .collect(Collectors.groupingBy(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceType));
        
        Map<String, List<FirstMileReconciliationStandardExcelDTO>> transportNoMaps = successList.stream().collect(Collectors.groupingBy(FirstMileReconciliationStandardExcelDTO::getTransportNo));
        successList = new ArrayList<>();
        Map<String, TmsFirstMileReconciliationDetailEntity> transportNoDetailMap = lambdaQuery().in(TmsFirstMileReconciliationDetailEntity::getTransportNo, transportNoMaps.keySet())
        		.ne(TmsFirstMileReconciliationDetailEntity::getMainId, mainEntity.getId())
        		.eq(TmsFirstMileReconciliationDetailEntity::getType, "actual").list()
        		.stream().collect(Collectors.toMap(TmsFirstMileReconciliationDetailEntity::getTransportNo, t -> t , (t1 , t2) -> t1));
        
        for(Map.Entry<String, List<FirstMileReconciliationStandardExcelDTO>> transportNoMap : transportNoMaps.entrySet()) {
        	String key = transportNoMap.getKey();
        	List<FirstMileReconciliationStandardExcelDTO> value = transportNoMap.getValue();
        	successList.addAll(value.stream().filter(v -> cfgErpFieldMap.get(v.getCostName()) == null).collect(Collectors.toList()));
        	Map<String, List<FirstMileReconciliationStandardExcelDTO>> dictMaps = value.stream().filter(v -> cfgErpFieldMap.get(v.getCostName()) != null).collect(Collectors.groupingBy(v -> cfgErpFieldMap.get(v.getCostName()).getSourceCodeValue()));
        	for(Map.Entry<String, List<FirstMileReconciliationStandardExcelDTO>> dictMap : dictMaps.entrySet()) {
        		String dict = dictMap.getKey();
        		List<FirstMileReconciliationStandardExcelDTO> dictList = dictMap.getValue();
        		String currency = dictList.get(0).getCurrency();
        		if(dictList.stream().allMatch(d -> currency.equals(d.getCurrency()))) {
        			TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = transportNoDetailMap.get(key);
        			boolean isValiDate = true;
        			if(tmsFirstMileReconciliationDetailEntity != null) {
        				String confirmedCurrency = "CNY";
        				if(AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(dict)) {
        					confirmedCurrency = tmsFirstMileReconciliationDetailEntity.getShippingCostCurrency();
        				}
        				if(AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(dict)) {
        					confirmedCurrency = tmsFirstMileReconciliationDetailEntity.getDeclareCostCurrency();
        				}
        				if(AllocationFeeTypeEnum.OTHER_COST.getCode().equals(dict)) {
        					confirmedCurrency = tmsFirstMileReconciliationDetailEntity.getOtherCostCurrency();
        				}
        				if(AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode().equals(dict)) {
        					confirmedCurrency = tmsFirstMileReconciliationDetailEntity.getOtherTaxCurrency();
        				}
        				if(!confirmedCurrency.equals(currency)) {
        					isValiDate = false;
        				}
        			}
        			if(isValiDate) {
        				successList.addAll(dictList);
        			}else {
        				dictList.forEach(d -> d.setErrorMsg(CharSequenceUtil.format("物流运单号【{}】下的【{}】分类费用币种与其他周期对账单币别不一致", key , DictCostCategoryEnum.getName(dict))));
        				errorList.addAll(dictList);
        			}
        		}else {
        			dictList.forEach(d -> d.setErrorMsg(CharSequenceUtil.format("物流运单号【{}】下的【{}】分类费用币种不一致", key , DictCostCategoryEnum.getName(dict))));
        			errorList.addAll(dictList);
        		}
        	}
        }
        for (FirstMileReconciliationStandardExcelDTO excelDTO : successList) {
            // 对应物流单
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDetailDTO = sourceLogisticList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(excelDTO.getTransportNo(),e.getTransportNo())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(sourceDetailDTO)) {
                excelDTO.setErrorMsg(CharSequenceUtil.format("未找到物流运单号【{}】的物流单", excelDTO.getTransportNo()));
                errorList.add(excelDTO);
                continue;
            }
            List<String> sourceIds = sourceDetailDTO.stream().filter(Objects::nonNull).map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId).distinct().collect(Collectors.toList());
            List<TmsFirstMileReconciliationDetailEntity> detailEntityList = this.listBySourceIds(sourceIds, DetailReconciliationTypeEnum.ACTUAL.getCode());
            detailEntityList = detailEntityList.stream().filter(e -> Objects.equals(e.getMainId(), mainEntity.getId())).collect(Collectors.toList());
            // 先从结果集获取
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> currentTrackNoList = resultMap.get(excelDTO.getTransportNo());
            if (null == currentTrackNoList) {
                currentTrackNoList = oldDbGroupMap.get(excelDTO.getTransportNo());
                if (CollectionUtils.isEmpty(currentTrackNoList)) {
                    // 生成当前物流单的所有明细
                    List<TmsFirstMileReconciliationDetailEntity> finalDetailEntityList = detailEntityList;
                    currentTrackNoList = sourceDetailDTO.stream()
                            .map(e ->{
                                //当前明细对账单次数
                                int reconciliationCount = 1;
                                TmsFirstMileReconciliationDetailEntity maxDetailEntity = finalDetailEntityList.stream().filter(f -> Objects.nonNull(f) && Objects.equals(e.getSourceId(),f.getSourceId()))
                                        .max(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount)).orElse(null);
                                if (Objects.nonNull(maxDetailEntity)){
                                    reconciliationCount = maxDetailEntity.getReconciliationCount() + 1;
                                }
                                return  generateAllTypeDTO(e, reconciliationCount, Boolean.TRUE);
                            })
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                } else {
                    // 历史记录校验状态
                    if (currentTrackNoList.stream()
                            .anyMatch(e ->ReconciliationStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(e.getStatus()) ||
                                    ReconciliationStatusEnum.DIFF_CONFIRM.getCode().equalsIgnoreCase(e.getStatus()) ||
                                    ReconciliationStatusEnum.RECONCILED.getCode().equalsIgnoreCase(e.getStatus())
                            )
                    ) {
                        excelDTO.setErrorMsg(CharSequenceUtil.format("仅{待确认}可更新,当前物流运单号【{}】", excelDTO.getTransportNo()));
                        errorList.add(excelDTO);
                        continue;
                    }
                }
            }

            CfgReconciliationFieldDTO.ErpFieldDropDownDTO erpFieldDropDownDTO = cfgErpFieldMap.getOrDefault(excelDTO.getCostName(), null);
            if (null == erpFieldDropDownDTO && StringUtils.isNotBlank(excelDTO.getCostName()) && StringUtils.isNotBlank(excelDTO.getCostValue())) {
                excelDTO.setErrorMsg(CharSequenceUtil.format("字段配置中未找到费用项【{}】", excelDTO.getCostName()));
                errorList.add(excelDTO);
                continue;
            }
            // 按sourceId分组
            Map<String, Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO>> sourceListMap = currentTrackNoList.stream()
                    .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId,
                            Collectors.toMap(TmsFirstMileReconciliationDetailDTO.ListDTO::getType, Function.identity())));

            for (Map.Entry<String, Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO>> entry : sourceListMap.entrySet()) {
                // 预计
                TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO = entry.getValue().get(DetailReconciliationTypeEnum.ESTIMATED.getCode());
                // 实际
                TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO = entry.getValue().get(DetailReconciliationTypeEnum.ACTUAL.getCode());
                // 差异
                TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO = entry.getValue().get(DetailReconciliationTypeEnum.DIFF.getCode());

                //需要更新的费用类Map
                //如果当前没有就取历史
                Map<String, TmsCostDetailDTO.UpdateDTO> updateListMap = CollectionUtils.isEmpty(actualListDTO.getUpdateList()) ?
                        costDetailMap.getOrDefault(entry.getKey(), new HashMap<>()) : actualListDTO.getUpdateList().stream().collect(Collectors.toMap(TmsCostDetailDTO.CommonDTO::getCfgCostId, Function.identity()));
                if (null != erpFieldDropDownDTO) {
                    // 设置实际为当前值
                    if (checkAndSetCostValueAndDictBasic(errorList, excelDTO, erpFieldDropDownDTO, updateListMap, actualListDTO, sourceTypeGroupMap)) {
                        continue;
                    }
                } else {
                    // 根据字段名设置
                    checkAndSetDictBasic(excelDTO, sourceTypeGroupMap, actualListDTO);
                }

                // 重新计算差异
                generateDiffAndCheckConfirm(estimatedListDTO, actualListDTO, diffListDTO);
                // 添加明细信息
                actualListDTO.setUpdateList(new LinkedList<>(updateListMap.values()));
                // 添加到当前结果
                resultMap.put(excelDTO.getTransportNo(), Arrays.asList(estimatedListDTO, actualListDTO, diffListDTO));
            }
        }
    }

    private static boolean checkAndSetCostValueAndDictBasic(List<FirstMileReconciliationStandardExcelDTO> errorList, FirstMileReconciliationStandardExcelDTO excelDTO, CfgReconciliationFieldDTO.ErpFieldDropDownDTO erpFieldDropDownDTO, Map<String, TmsCostDetailDTO.UpdateDTO> updateListMap, TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO, Map<String, List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO>> sourceTypeGroupMap) {
        if (SourceTypeEnum.TMS_CFG_COST.getCode().equalsIgnoreCase(erpFieldDropDownDTO.getSourceType())) {
            DictCostCategoryEnum categoryEnum = DictCostCategoryEnum.getByCode(erpFieldDropDownDTO.getSourceCodeValue());
            // 检查费用类型是否已存在更新
            TmsCostDetailDTO.UpdateDTO oldUpdateDTO = updateListMap.get(erpFieldDropDownDTO.getSourceId());
            if (null != oldUpdateDTO) {
                if (oldUpdateDTO.isHasUpdate()) {
                    excelDTO.setErrorMsg(CharSequenceUtil.format("当前页面费用已存在【{}】", erpFieldDropDownDTO.getSourceCodeValue()));
                    errorList.add(excelDTO);
                    return true;
                } else {
                    oldUpdateDTO.setHasUpdate(true);
                    oldUpdateDTO.setCostValue(MathUtil.valueOf(excelDTO.getCostValue()));
                }
                oldUpdateDTO.setCurrency(excelDTO.getCurrency());
                updateListMap.put(erpFieldDropDownDTO.getSourceId(), oldUpdateDTO);
            } else {
                // 历史不存在新增
                TmsCostDetailDTO.UpdateDTO updateDTO = newCostUpdateDTO(categoryEnum, excelDTO.getCostValue(), erpFieldDropDownDTO.getSourceId());
                updateDTO.setCurrency(excelDTO.getCurrency());
                updateListMap.put(erpFieldDropDownDTO.getSourceId(), updateDTO);
            }
            // 加成和设置实际值
            checkAndUpdateCfgCostValue(updateListMap, categoryEnum, actualListDTO);

            // 根据字段名设置
            checkAndSetDictBasic(excelDTO, sourceTypeGroupMap, actualListDTO);
        } else if (SourceTypeEnum.DICT_BASIC.getCode().equalsIgnoreCase(erpFieldDropDownDTO.getSourceType())) {
            // 根据字段名设置
            Object value = ReflectUtil.getFieldValue(excelDTO, erpFieldDropDownDTO.getSourceCodeValue());
            ReflectUtil.setFieldValue(actualListDTO, erpFieldDropDownDTO.getSourceCodeValue(), value);
        } else {
            excelDTO.setErrorMsg(CharSequenceUtil.format("配置类型不存在【{}】", erpFieldDropDownDTO.getSourceType()));
            errorList.add(excelDTO);
            return true;
        }
        return false;
    }

    private static void checkAndSetDictBasic(FirstMileReconciliationStandardExcelDTO excelDTO, Map<String, List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO>> sourceTypeGroupMap, TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO) {
        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> dictBasticList = sourceTypeGroupMap.get(SourceTypeEnum.DICT_BASIC.getCode());
        if (!CollectionUtils.isEmpty(dictBasticList)) {
            for (CfgReconciliationFieldDTO.ErpFieldDropDownDTO fieldDropDownDTO : dictBasticList) {
                Object value = ReflectUtil.getFieldValue(excelDTO, fieldDropDownDTO.getSourceCodeValue());
                if (null != value) {
                    ReflectUtil.setFieldValue(actualListDTO, fieldDropDownDTO.getSourceCodeValue(), value);
                }
            }
        }
    }

    @Override
    public CurrencyDTO.ViewDTO getCurrencyView(String currency) {
        if (StringUtils.isBlank(currency)) {
            return null;
        }

        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Collections.singletonList(currency));
        //币别符号
        return currencyList
                .stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getId(), currency))
                .findFirst()
                .orElse(null);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkRemoveByMainId(String id) {
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = this.listByMainIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return;
        }
        //检查明细中是否存在首次对账 存在首次对账检查是否存在二次对账 存在则不能删除 不存在则删除首次时 清空费用记录中的对账单id
        List<String> logisticsBillIds = detailEntityList.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList1 = listBySourceIds(logisticsBillIds, null);
        for (TmsFirstMileReconciliationDetailEntity detailEntity : detailEntityList){
            //检查是否存在其他对账单
            if (!CollectionUtils.isEmpty(detailEntityList1)){
                List<TmsFirstMileReconciliationDetailEntity> collect = detailEntityList1.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getMainId()) && Objects.equals(e.getSourceId(), detailEntity.getSourceId()) && !detailEntity.getReconciliationCount().equals(e.getReconciliationCount())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect) && detailEntity.getReconciliationCount() == 1){
                    List<String> mainIds = collect.stream().map(TmsFirstMileReconciliationDetailEntity::getMainId).distinct().collect(Collectors.toList());
                    List<TmsFirstMileReconciliationEntity> tmsFirstMileReconciliationEntities = tmsFirstMileReconciliationService.listByIds(mainIds);
                    List<String> codeList = tmsFirstMileReconciliationEntities.stream().map(TmsFirstMileReconciliationEntity::getCode).distinct().collect(Collectors.toList());
                    //存在二次对账时，先删除二次对账再删除首次对账
                   throw new ServiceException("物流单【{}】存在多次对账，先删除多次对账单【{}】，才能删除首次对账单", detailEntity.getTransportNo(), String.join(",",codeList));
                }
            }
        }

        detailEntityList.forEach(e -> {
            //更新其他对账单对账次数
            updateReconciliationDetailCount(e.getId(),e.getSourceId(),e.getReconciliationCount());

        });
        this.lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getMainId, id).remove();
        List<String> detailId1s = detailEntityList.stream().filter(e -> e.getReconciliationCount() > 1).map(TmsFirstMileReconciliationDetailEntity::getSourceId ).collect(Collectors.toList());
        List<String> detailId2s = detailEntityList.stream().filter(e -> e.getReconciliationCount() <= 1).map(TmsFirstMileReconciliationDetailEntity::getSourceId ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(detailId1s)){
            //删除下推对账单费用数据
            logisticsBillCostService.removeByReconciliationIds(id,detailId1s);
        }
        if (!CollectionUtils.isEmpty(detailId2s)){
            //清除对账单费用表 中对账单id
            logisticsBillCostService.removeRefByReconciliationIds(id,detailId2s);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeLogisticsBillCost(List<String> sourceIds, String reconciliationStatus) {
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillIdList(sourceIds);
        if (CollectionUtils.isEmpty(logisticsBillCostList)) {
            return;
        }
        logisticsBillCostList.forEach(e -> e.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()));
        if (!logisticsBillCostService.updateBatchById(logisticsBillCostList)) {
            throw new ServiceException("批量更新物流单相关失败, 请重试");
        }
        // 移除物流单实际明细
        List<String> costIds = logisticsBillCostList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        tmsCostDetailService.updateActual0ByMainId(costIds);
    }

    @Override
    public List<DictCountryDTO.ListDTO> checkAndFindCountry(FirstMileDeliveryEntity delivery, List<CfgAmzFulfillmentCenterEntity> centerList, List<DictCountryDTO.ListDTO> countryList) {
        if (SourceTypeEnum.FBA_SHIPMENT.getCode().equals(delivery.getSourceCode())) {
            String countryCode = centerList.stream()
                    .filter(e -> e.getCode().equalsIgnoreCase(delivery.getFulfillmentCenter())).findFirst()
                    .map(CfgAmzFulfillmentCenterEntity::getCountry)
                    .orElse("");
            DictCountryDTO.ListDTO toCountry = countryList.stream().filter(e -> e.getId().equalsIgnoreCase(countryCode)).findFirst().orElse(null);
            DictCountryDTO.ListDTO fromCountry = countryList.stream().filter(e -> e.getNameCn().equalsIgnoreCase(delivery.getCountryName())).findFirst().orElse(null);
            // FBA
            return Arrays.asList(toCountry, fromCountry);
        } else {
            DictCountryDTO.ListDTO toCountry = countryList.stream().filter(e -> e.getNameCn().equalsIgnoreCase(delivery.getCountryName())).findFirst().orElse(null);
            // 默认中国
            DictCountryDTO.ListDTO fromCountry = countryList.stream().filter(e -> "CN".equalsIgnoreCase(e.getId())).findFirst().orElse(null);
            // 海外仓
            return Arrays.asList(toCountry, fromCountry);
        }
    }

    @Override
    public List<DictCountryDTO.ListDTO> defaultCountry(String sourceToCountry, List<DictCountryDTO.ListDTO> countryList) {
        DictCountryDTO.ListDTO toCountry = countryList.stream()
                .filter(e -> e.getNameCn().equalsIgnoreCase(sourceToCountry) || e.getId().equalsIgnoreCase(sourceToCountry))
                .findFirst().orElse(null);
        // 默认中国
        DictCountryDTO.ListDTO fromCountry = countryList.stream().filter(e -> "CN".equalsIgnoreCase(e.getId())).findFirst().orElse(null);
        // 海外仓
        return Arrays.asList(toCountry, fromCountry);

    }

    @Override
    public Map<String, Map<String, TmsCostDetailDTO.UpdateDTO>> convertUpdateDTOAndMap(List<TmsFirstMileReconciliationDetailEntity> oldDetailList) {
        if (CollectionUtils.isEmpty(oldDetailList)) {
            return new HashMap<>();
        }
        List<String> detailIds = oldDetailList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        // 查询对应实际费用
        List<TmsCostDetailDTO.CostViewDTO> tmsCostDetailList = tmsCostDetailService.listCostByMainIdList(detailIds);
        if (CollectionUtils.isEmpty(tmsCostDetailList)) {
            return new HashMap<>();
        }

        // 使用流进行分组，并将每个分组的 id 收集到集合中
        Map<String, List<String>> sourceIdDetailMap = oldDetailList.stream()
                .collect(Collectors.groupingBy(
                        TmsFirstMileReconciliationDetailEntity::getSourceId,
                        Collectors.mapping(TmsFirstMileReconciliationDetailEntity::getId, Collectors.toList())));

        // mainId关联到sourceId分组
        return tmsCostDetailList.stream()
                .map(e -> new TmsCostDetailDTO.UpdateDTO(e.getId(), e.getDictCostCategory(), e.getCostValue(), e.getCfgCostId(), e.getType(), SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode()))
                .collect(Collectors.groupingBy(e -> sourceIdDetailMap.entrySet().stream()
                                .filter(entry -> entry.getValue().contains(e.getId()))
                                .map(Map.Entry::getKey)
                                .findFirst().orElse(""),
                        Collectors.toMap(TmsCostDetailDTO.UpdateDTO::getCfgCostId, Function.identity())));
    }


    /**
     * 字段配置导入
     */
    private TmsFirstMileReconciliationDetailDTO.ImportDTO importConfigFile(MultipartFile excelFile, HttpServletResponse response, TmsFirstMileReconciliationEntity mainEntity) {
        TmsFirstMileReconciliationDetailDTO.ImportDTO importDTO = new TmsFirstMileReconciliationDetailDTO.ImportDTO();

        FirstMileReconciliationConfigExcelListener excelListenerUtil = new FirstMileReconciliationConfigExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<JSONObject> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<JSONObject> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<JSONObject> errorList = excelListenerUtil.getErrorList();
        //表头
        List<String> headList = excelListenerUtil.getHeadList();
        //导入数据保存
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> successImportList = handleImportConfigData(successList, errorList, headList, mainEntity);

        String url = "";
        if (!CollectionUtils.isEmpty(errorList)) {
            String fileName = "头程对账单【配置版】错误数据.xlsx";
            List<List<Object>> exportList = errorList.stream().map(obj -> checkToList(obj.values())).collect(Collectors.toList());
            File file = ExcelUtil.exportFile(fileName, "error", exportList, headList);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successImportList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    private List<Object> checkToList(Collection<Object> values) {
        return values.stream().map(e -> e instanceof JSONNull ? null : e).collect(Collectors.toList());
    }

    /**
     * 配置导入
     */
    private List<TmsFirstMileReconciliationDetailDTO.ListDTO> handleImportConfigData(List<JSONObject> successList,
                                                                                     List<JSONObject> errorList,
                                                                                     List<String> headList,
                                                                                     TmsFirstMileReconciliationEntity mainEntity
    ) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.emptyList();
        }
        //字段配置信息
        List<CfgReconciliationFieldDTO.ErpFieldViewDTO> erpFieldList = cfgReconciliationFieldService.getByReconciliationType(CfgReconciliationTypeEnum.FIRST_MILE.getCode());
        //字段配置信息
        List<CfgReconciliationFieldDTO.ErpFieldViewDTO> erpFieldResultList = erpFieldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getThirdCode(), mainEntity.getLogisticsSupplierId())).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(erpFieldResultList)) {
            throw new ServiceException("物流商未设置字段配置，请配置后导入");
        }
        Map<String, CfgReconciliationFieldDTO.ErpFieldViewDTO> cfgErpFieldMap = erpFieldResultList.stream().collect(Collectors.toMap(CfgReconciliationFieldDTO.ErpFieldViewDTO::getThirdFieldName, Function.identity()));

        // 币种
        String currency = mainEntity.getCurrency();
        CurrencyDTO.ViewDTO currencyViewDTO = this.getCurrencyView(currency);

        //物流运单号下标
        List<String> transportNoList = new ArrayList<>();
        int transportNoIndex = MathUtil.ZERO;
        for (int i = 0; i < headList.size(); i++) {
            CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldViewDTO = cfgErpFieldMap.get(headList.get(i));
            if (ObjectUtil.isEmpty(erpFieldViewDTO) || !CharSequenceUtil.equals(erpFieldViewDTO.getErpFieldCode(), "transportNo")) {
                continue;
            }
            String finalI = String.valueOf(i);
            String soCode = successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(finalI))).map(obj -> obj.get(finalI).toString()).findFirst().orElse("");
            transportNoList.add(soCode);
            //销售订单单号下标
            transportNoIndex = Integer.parseInt(finalI);
        }
        // 查询原物流单信息
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceLogisticList = tmsFirstMileLogisticService.listByTransportNoListAndSupplierIds(
                transportNoList,
                Collections.singletonList(mainEntity.getLogisticsSupplierId()));

        // 补充来源信息
        this.fillWaitReconciliationList(sourceLogisticList,mainEntity.getId());
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> sourceLogisticMap = sourceLogisticList
                .stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTransportNo));

        // 物流跟踪单
        // 原对数据库账明细信息
        List<TmsFirstMileReconciliationDetailEntity> oldDetailList = this.listByMainIds(Collections.singletonList(mainEntity.getId()));

        List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailDTO.ListDTO.class, oldDetailList);
        // 补充基础信息
        this.fillDetailList(viewDTOList, currency, currencyViewDTO);

        // 按分组Map<物流运单号, Map<来源物流ID, 当前明细数组>>
//        Map<String, Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>>> oldDbGroupMap = viewDTOList
//                .stream()
//                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTrackNo,
//                        Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)));
        // 按分组Map<物流运单号, 当前明细数组>
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> oldDbGroupMap = viewDTOList
                .stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTransportNo));


        // 历史的明细费用ID
        Map<String, Map<String, TmsCostDetailDTO.UpdateDTO>> costDetailMap = this.convertUpdateDTOAndMap(oldDetailList);

        // 结果
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> resultMap = new HashMap<>();

        for (JSONObject jsonObject : successList) {
            List<String> errorMsgList = new ArrayList<>();
            if (cfgErpFieldMap.isEmpty()) {
                errorMsgList.add("未发现字段配置, 请联系管理员");
            }
            // 需要处理的每一列
            Map<CfgReconciliationFieldDTO.ErpFieldViewDTO, String> handleColumnMap = new HashMap<>();

            String detailCurrency = "CNY";
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(Integer.toString(transportNoIndex))) {
                    continue;
                }
                if (!CollectionUtils.isEmpty(errorMsgList)) {
                    // 已有错误信息跳过
                    continue;
                }
                Object columnValueObj = entry.getValue();
                if (null == columnValueObj || columnValueObj instanceof JSONNull) {
                    continue;
                }
                //字段名称
                String curFieldName = headList.get(Integer.parseInt(entry.getKey()));

                CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldDropDownDTO = cfgErpFieldMap.get(curFieldName);
                if (null == erpFieldDropDownDTO) {
                    errorMsgList.add(CharSequenceUtil.format("未找到该字段配置项【{}】", curFieldName));
                    break;
                }
                String erpFieldCode = erpFieldDropDownDTO.getErpFieldCode();
                String columnValueStr = columnValueObj.toString();
                if("currency".equals(erpFieldCode)) {
                	if(StringUtils.isBlank(columnValueStr)) {
                		errorMsgList.add("币种不能为空");
                        break;
                	}
                	detailCurrency = columnValueStr;
                	continue;
                }else if (!StringUtils.isNumeric(columnValueStr)) {
                    errorMsgList.add(CharSequenceUtil.format("字段【{}】内容【{}】非数字", erpFieldDropDownDTO.getErpFieldName(), columnValueObj));
                    break;
                }
                handleColumnMap.put(erpFieldDropDownDTO, columnValueStr);
            }

            if (!CollectionUtils.isEmpty(errorMsgList)) {
                jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }

            String transportNo = jsonObject.getStr(Integer.toString(transportNoIndex));
            // 对应物流单
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDetailDTO = sourceLogisticMap.get(transportNo);
            if (CollectionUtils.isEmpty(sourceDetailDTO)) {
                errorMsgList.add(CharSequenceUtil.format("未找到物流运单号【{}】的物流单", transportNo));
                jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }
            //获取来源id列表
            List<String> sourceIds = sourceLogisticList.stream().filter(Objects::nonNull).map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId).distinct().collect(Collectors.toList());
            List<TmsFirstMileReconciliationDetailEntity> detailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIds(sourceIds,DetailReconciliationTypeEnum.ACTUAL.getCode());
            // 先从结果集获取
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> currentTrackNoList = resultMap.get(transportNo);
            boolean currFlag = true;
            if (null == currentTrackNoList) {
            	currFlag = false;
                currentTrackNoList = oldDbGroupMap.get(transportNo);
                if (CollectionUtils.isEmpty(currentTrackNoList)) {
                    // 生成当前物流单的所有明细
                    currentTrackNoList = sourceDetailDTO.stream()
                            .map(e -> {
                                //当前明细对账单次数
                                int reconciliationCount = 1;
                                TmsFirstMileReconciliationDetailEntity maxDetailEntity = detailEntityList.stream().filter(f -> Objects.nonNull(f) && Objects.equals(e.getSourceId(),f.getSourceId()))
                                        .max(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount)).orElse(null);
                                if (Objects.nonNull(maxDetailEntity)){
                                    reconciliationCount = maxDetailEntity.getReconciliationCount() + 1;
                                }
                                return generateAllTypeDTO(e, reconciliationCount, Boolean.TRUE);
                            })
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                } else {
                    // 历史记录校验状态
                    if (currentTrackNoList.stream()
                            .anyMatch(e -> ReconciliationStatusEnum.CONFIRMED.getCode().equalsIgnoreCase(e.getStatus()) ||
                                    ReconciliationStatusEnum.DIFF_CONFIRM.getCode().equalsIgnoreCase(e.getStatus()) ||
                                    ReconciliationStatusEnum.RECONCILED.getCode().equalsIgnoreCase(e.getStatus()))
                    ) {
                        errorMsgList.add(CharSequenceUtil.format("仅{待确认}可更新,当前物流运单号【{}】", transportNo));
                        jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                        errorList.add(jsonObject);
                        continue;
                    }
                }
            }
            // 检查来源单号是否一致
            if (1 != currentTrackNoList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId).distinct().count()) {
                errorMsgList.add(CharSequenceUtil.format("数据异常:物流运单号【{}】的来源ID不一致", transportNo));
                jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }

            String sourceId = currentTrackNoList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId).distinct().findFirst().orElse("");
            // 对应sourceId的Map<Type, 明细>
            Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO> entryMap = currentTrackNoList.stream()
                    .collect(Collectors.toMap(TmsFirstMileReconciliationDetailDTO.ListDTO::getType, Function.identity()));

            // 预计
            TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO = entryMap.get(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            // 实际
            TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO = entryMap.get(DetailReconciliationTypeEnum.ACTUAL.getCode());
            // 差异
            TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO = entryMap.get(DetailReconciliationTypeEnum.DIFF.getCode());

            //需要更新的费用类Map
            Map<String, TmsCostDetailDTO.UpdateDTO> updateListMap = CollectionUtils.isEmpty(actualListDTO.getUpdateList()) ?
                    costDetailMap.getOrDefault(sourceId, new HashMap<>()) : actualListDTO.getUpdateList().stream().collect(Collectors.toMap(TmsCostDetailDTO.CommonDTO::getCfgCostId, Function.identity()));


            for (Map.Entry<CfgReconciliationFieldDTO.ErpFieldViewDTO, String> entry : handleColumnMap.entrySet()) {
                CfgReconciliationFieldDTO.ErpFieldViewDTO erpFieldDropDownDTO = entry.getKey();
                String columnValueStr = entry.getValue();
                // 设置实际为当前值
                if (SourceTypeEnum.TMS_CFG_COST.getCode().equalsIgnoreCase(erpFieldDropDownDTO.getSourceType())) {
                    DictCostCategoryEnum categoryEnum = DictCostCategoryEnum.getByCode(erpFieldDropDownDTO.getDictCostCategory());
                    // 检查费用类型是否已存在更新
                    TmsCostDetailDTO.UpdateDTO oldUpdateDTO = updateListMap.get(erpFieldDropDownDTO.getSourceId());
                    if (null != oldUpdateDTO) {
                        if (oldUpdateDTO.isHasUpdate()) {
                            errorMsgList.add(CharSequenceUtil.format("当前页面费用已存在【{}】", erpFieldDropDownDTO.getSourceType()));
                            jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                            errorList.add(jsonObject);
                            break;
                        } else {
                            oldUpdateDTO.setHasUpdate(true);
                            oldUpdateDTO.setCostValue(MathUtil.valueOf(columnValueStr));
                            oldUpdateDTO.setCurrency(detailCurrency);
                            updateListMap.put(erpFieldDropDownDTO.getSourceId(), oldUpdateDTO);
                        }
                    } else {
                        // 历史不存在新增
                        TmsCostDetailDTO.UpdateDTO updateDTO = newCostUpdateDTO(categoryEnum, columnValueStr, erpFieldDropDownDTO.getSourceId());
                        updateDTO.setCurrency(detailCurrency);
                        updateListMap.put(erpFieldDropDownDTO.getSourceId(), updateDTO);
                    }
                    if(currFlag) {
                    	String feeCurrency = "";
                        if(categoryEnum == DictCostCategoryEnum.SHIPPING_COST) {
                        	feeCurrency = actualListDTO.getShippingCostCurrency();
                        }else if(categoryEnum == DictCostCategoryEnum.DECLARE_COST) {
                        	feeCurrency = actualListDTO.getDeclareCostCurrency();
                        }else if(categoryEnum == DictCostCategoryEnum.OTHER_TAX_FEE) {
                        	feeCurrency = actualListDTO.getOtherTaxCurrency();
                        }else if(categoryEnum == DictCostCategoryEnum.OTHER_COST) {
                        	feeCurrency = actualListDTO.getOtherCostCurrency();
                        }
                        if(!detailCurrency.equals(feeCurrency)) {
                        	errorMsgList.add(categoryEnum.getName() + "分类下【"+ erpFieldDropDownDTO.getSourceType() +"】费用币别不一致");
                            jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                            errorList.add(jsonObject);
                            updateListMap.remove(erpFieldDropDownDTO.getSourceId());
                            break;
                        }
                    }
                    // 加成和设置实际值
                    checkAndUpdateCfgCostValue(updateListMap, categoryEnum, actualListDTO);
                } else if (SourceTypeEnum.DICT_BASIC.getCode().equalsIgnoreCase(erpFieldDropDownDTO.getSourceType())) {
                    // 根据字段名设置
                    ReflectUtil.setFieldValue(actualListDTO, erpFieldDropDownDTO.getErpFieldCode(), new BigDecimal(columnValueStr));
                } else {
                    errorMsgList.add(CharSequenceUtil.format("配置类型不存在【{}】", erpFieldDropDownDTO.getSourceType()));
                    jsonObject.set("错误信息", FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(jsonObject);
                    break;
                }
                // 重新计算差异
                generateDiffAndCheckConfirm(estimatedListDTO, actualListDTO, diffListDTO);
                // 添加明细信息
                actualListDTO.setUpdateList(new LinkedList<>(updateListMap.values()));
                // 添加到当前结果
                resultMap.put(transportNo, Arrays.asList(estimatedListDTO, actualListDTO, diffListDTO));
            }
        }
        return resultMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @NotNull
    private static TmsCostDetailDTO.UpdateDTO newCostUpdateDTO(DictCostCategoryEnum categoryEnum, String costValue, String cfgCostId) {
        TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
        updateDTO.setHasUpdate(true);
        updateDTO.setDictCostCategory(categoryEnum.getCode());
        updateDTO.setCostValue(MathUtil.valueOf(costValue));
        updateDTO.setSourceType(SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode());
        updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
        updateDTO.setCfgCostId(cfgCostId);
        return updateDTO;
    }

    /**
     * 当前的分类汇总的明细费用
     */
    private static void checkAndUpdateCfgCostValue(Map<String, TmsCostDetailDTO.UpdateDTO> updateListMap,
                                                   DictCostCategoryEnum categoryEnum,
                                                   TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO) {
    	List<UpdateDTO> dictList = updateListMap.values().stream().filter(updateDTO -> categoryEnum.getCode().equalsIgnoreCase(updateDTO.getDictCostCategory())).collect(Collectors.toList());
        // 当前的分类汇总的明细费用
        BigDecimal curCategoryValue = dictList.stream()
                .map(TmsCostDetailDTO.CommonDTO::getCostValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String currency = dictList.get(0).getCurrency();
        switch (categoryEnum) {
            case SHIPPING_COST:
                actualListDTO.setShippingCost(curCategoryValue);
                actualListDTO.setShippingCostCurrency(currency);
                break;
            case DECLARE_COST:
                actualListDTO.setDeclareCost(curCategoryValue);
                actualListDTO.setDeclareCostCurrency(currency);
                break;
            case OTHER_TAX_FEE:
                actualListDTO.setOtherTaxCost(curCategoryValue);
                actualListDTO.setOtherTaxCurrency(currency);
                break;
            case OTHER_COST:
                actualListDTO.setOtherCost(curCategoryValue);
                actualListDTO.setOtherCostCurrency(currency);
                break;
            default:
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGenFirstMileReconciliation(LocalDate startDate, LocalDate endDate) {
        if (null == startDate || null == endDate) {
            throw new ServiceException("开始时间或结束时间为空");
        }
        // 查询周期内已签收未对账的物流单
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> list = tmsFirstMileLogisticService.listAutoGenerateFirstMileReconciliation(startDate, endDate);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 根据物流商
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> map = list
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.getCurrency()))
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsSupplierId));
        List<String> billIds = list.stream().filter(Objects::nonNull).map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId).distinct().collect(Collectors.toList());
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = this.listBySourceIds(billIds, DetailReconciliationTypeEnum.ACTUAL.getCode());
        for (Map.Entry<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> entry : map.entrySet()) {
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDetailList = entry.getValue();
            this.fillWaitReconciliationData(sourceDetailList);
            TmsFirstMileReconciliationDetailDTO.ListDTO curListDTO = sourceDetailList.stream().findFirst().orElse(null);

            String supplier = entry.getKey();
//            String currency = entry.getKey().getSecond();

            // 查询对账单ID
            TmsFirstMileReconciliationEntity reconciliationEntity = tmsFirstMileReconciliationService.findByCycleAndSupplier(supplier, ApproveStatusEnum.WAIT_SUBMIT.getStatus(), startDate, endDate);
            if (null != reconciliationEntity) {
//                if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equalsIgnoreCase(reconciliationEntity.getApproveStatus())) {
//                    throw new ServiceException("对账单不处于待提交");
//                }
                reconciliationEntity.setUpdateTime(LocalDateTime.now());
            } else {
                TmsFirstMileReconciliationDetailDTO.ListDTO listDTO = sourceDetailList.get(0);
                String logisticsSupplierId = listDTO.getLogisticsSupplierId();
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCZD);
//                TmsFirstMileReconciliationEntity existEntity = tmsFirstMileReconciliationService.getByGenerate(logisticsSupplierId, startDate, endDate, curListDTO.getCurrency());
//                if (null != existEntity) {
//                    throw new ServiceException("当前周期和币种的对账单已存在");
//                }
                reconciliationEntity = new TmsFirstMileReconciliationEntity(code, startDate, endDate, logisticsSupplierId, curListDTO.getCurrency());
            }
            // 保存头程对账单
            tmsFirstMileReconciliationService.saveOrUpdate(reconciliationEntity);

            // 保存明细
            TmsFirstMileReconciliationDTO.UpdateDTO updateDTO = new TmsFirstMileReconciliationDTO.UpdateDTO();
            List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> allDetailDTOList = new LinkedList<>();
            updateDTO.setId(reconciliationEntity.getId());
            for (TmsFirstMileReconciliationDetailDTO.ListDTO listDTO : sourceDetailList) {
                //当前明细对账单次数
                int reconciliationCount = 1;
                TmsFirstMileReconciliationDetailEntity maxDetailEntity = detailEntityList.stream().filter(f -> Objects.nonNull(f) && Objects.equals(listDTO.getSourceId(),f.getSourceId()))
                        .max(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount)).orElse(null);
                if (Objects.nonNull(maxDetailEntity)){
                    reconciliationCount = maxDetailEntity.getReconciliationCount() + 1;
                }
                // 生成实际和差异记录
                List<TmsFirstMileReconciliationDetailDTO.ListDTO> saveListDTO = this.generateAllTypeDTO(listDTO,reconciliationCount, Boolean.FALSE);
                List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailDTOList = TmsFirstMileReconciliationConverter.INSTANCE.convertDetailDTOList(saveListDTO);
                allDetailDTOList.addAll(detailDTOList);
            }
            updateDTO.setDetailList(allDetailDTOList);
            tmsFirstMileReconciliationService.update(updateDTO);

            // 更新已成功对账单
            List<String> sourceIds = sourceDetailList.stream()
                    .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)
                    .distinct()
                    .collect(Collectors.toList());
            tmsFirstMileLogisticService.updateReconciliation(sourceIds, ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(),reconciliationEntity.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateCost(List<TmsFirstMileReconciliationDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (TmsFirstMileReconciliationDetailEntity detailEntity : list) {
            List<TmsCostDetailDTO.UpdateDTO> updateList = detailEntity.getUpdateList();
            if (CollectionUtils.isEmpty(updateList)) {
                continue;
            }
            tmsCostDetailService.batchUpdate(updateList, detailEntity.getId(), DictCostAttributionEnum.FIRST_MILE,Boolean.FALSE);
        }
    }

    @Override
    public String getCurrencyById(String mainId) {
        return baseMapper.getCurrencyById(mainId);
    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listByMainIdsBySort(List<String> mainIds) {
        return baseMapper.listByMainIdsBySort(mainIds);
    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listBySourceIds(List<String> sourceIds, String type) {
        if (CollectionUtils.isEmpty(sourceIds) && CharSequenceUtil.isBlank(type)){
            return Collections.emptyList();
        }
        return baseMapper.listBySourceIds(sourceIds, type);
    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listBySourceIdsAndStatus(List<String> sourceIds, String status, String type) {
        if (CollectionUtils.isEmpty(sourceIds) && CharSequenceUtil.isBlank(status) && CharSequenceUtil.isBlank(type)){
            return Collections.emptyList();
        }
        return baseMapper.listBySourceIdsAndStatus(sourceIds,status,type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReconciliationDetail(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList, TmsFirstMileReconciliationEntity mainEntity) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("明细不能为空");
        }
        String mainId = mainEntity.getId();
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = TmsFirstMileReconciliationConverter.INSTANCE.updateDetailDtoToDetailEntity(detailList);
        //原明细数据被删除的需要清除mainId
        List<TmsFirstMileReconciliationDetailEntity> oldList = this.listByMainIds(Collections.singletonList(mainId));
        Map<String, TmsFirstMileReconciliationDetailEntity> actualMap = handleUpdateData(detailEntityList, mainId, oldList);
        List<String> deleteIds = getDeleteIds(detailEntityList, oldList);
        if (!CollectionUtils.isEmpty(deleteIds)) {
            List<TmsFirstMileReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getTransportNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个头程对账明细【%s】", ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), pairList, "编辑操作");
            //更新主表id
            if (!CollectionUtils.isEmpty(deleteList)) {
                deleteList.forEach(obj -> {
                    //更新其他对账单对账次数
                    updateReconciliationDetailCount(obj.getId(),obj.getSourceId(),obj.getReconciliationCount());
                    this.lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId, obj.getId()).remove();
                    if (1== obj.getReconciliationCount()){
                        //首次对账单 修改费用状态
                        logisticsBillCostService.updateStatusByLogisticsBillIdsAndReconciliationId(Collections.singletonList(obj.getSourceId()), mainEntity.getId(),ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
                    }else {
                        //删除费用明细记录
                        logisticsBillCostService.removeByReconciliationIds(mainEntity.getId(), Collections.singletonList(obj.getSourceId()));
                    }
                });
//                this.removeByIds(deleteList);
//                // 需要移除的物流单
//                List<String> deleteSourceIds = deleteList.stream()
//                        .map(TmsFirstMileReconciliationDetailEntity::getSourceId)
//                        .collect(Collectors.toList());
//                //更新物流单对应的费用状态
//                logisticsBillCostService.updateStatusByLogisticsBillIdsAndReconciliationId(deleteSourceIds, mainEntity.getId(),ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
            }
        }
        // 统计费用合计
        BigDecimal totalCost = detailEntityList.stream().filter(e -> DetailReconciliationTypeEnum.ACTUAL.getCode().equalsIgnoreCase(e.getType()))
                .map(TmsFirstMileReconciliationDetailEntity::getTotalLogisticsCost)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        mainEntity.setTotalCost(totalCost);
        //更新主表
        tmsFirstMileReconciliationService.updateById(mainEntity);
        log.info("编辑 开始修改头程对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(detailEntityList);
        if (!save) {
            throw new ServiceException("头程对账单明细保存或更新失败");
        }
        //根据明细进行更新物流费用记录
        logisticsBillCostService.updateLogisticsBillCost(mainEntity, detailEntityList, actualMap);
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> exportFirstMileReconciliationDetail(PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        // 数据填充处理
        fillExportInfo(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initTotalLogisticsCost(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)){
            List<TmsFirstMileReconciliationEntity> list = tmsFirstMileReconciliationService.list();
            codeList = list.stream().map(TmsFirstMileReconciliationEntity::getCode).distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(codeList)){
            return;
        }
        List<TmsFirstMileReconciliationEntity> list = tmsFirstMileReconciliationService.listbyCodes(codeList);
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> ids = list.stream().map(TmsFirstMileReconciliationEntity::getId).distinct().collect(Collectors.toList());
        List<TmsFirstMileReconciliationDetailEntity> detailEntityList = listByMainIds(ids);
        List<TmsFirstMileReconciliationDetailEntity> updateList = new ArrayList<>();
        for (TmsFirstMileReconciliationEntity entity : list){
            //获取明细记录
            List<TmsFirstMileReconciliationDetailEntity> detailEntityList1 = detailEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getMainId(), entity.getId())).collect(Collectors.toList());
            Map<String, List<TmsFirstMileReconciliationDetailEntity>> map = detailEntityList1.stream().collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailEntity::getTransportNo));
            for (List<TmsFirstMileReconciliationDetailEntity> detailEntityList2 : map.values()){
                //存在三种类型明细
                TmsFirstMileReconciliationDetailEntity diff = detailEntityList2.stream().filter(e -> Objects.equals(e.getType(), DetailReconciliationTypeEnum.DIFF.getCode())).findFirst().orElse(null);
                TmsFirstMileReconciliationDetailEntity estimated = detailEntityList2.stream().filter(e -> Objects.equals(e.getType(), DetailReconciliationTypeEnum.ESTIMATED.getCode())).findFirst().orElse(null);
                TmsFirstMileReconciliationDetailEntity actual = detailEntityList2.stream().filter(e -> Objects.equals(e.getType(), DetailReconciliationTypeEnum.ACTUAL.getCode())).findFirst().orElse(null);
                if (Objects.isNull(diff)){
                    continue;
                }
                BigDecimal estimatedCost = Objects.nonNull(estimated) && Objects.nonNull(estimated.getTotalLogisticsCost()) ? estimated.getTotalLogisticsCost() : BigDecimal.ZERO;
                BigDecimal actualCost = Objects.nonNull(actual) && Objects.nonNull(actual.getTotalLogisticsCost()) ? actual.getTotalLogisticsCost() : BigDecimal.ZERO;
                diff.setTotalLogisticsCost(MathUtil.subtract(actualCost,estimatedCost));
                updateList.add(diff);
            }
        }
        if (CollectionUtils.isEmpty(updateList)){
            return;
        }
        updateList.forEach(e ->{
            this.lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId, e.getId()).set(TmsFirstMileReconciliationDetailEntity::getTotalLogisticsCost, e.getTotalLogisticsCost()).update();
        });
    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listByRelationCode(List<String> relationCodeList) {
        if (CollUtil.isEmpty(relationCodeList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TmsFirstMileReconciliationDetailEntity::getRelationCode, relationCodeList).list();
    }
}
