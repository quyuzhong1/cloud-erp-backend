package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.KolSampleCostDTO;
import com.erp.model.oms.dto.excel.KolSampleCostImportExcelDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.KolSampleCostEntity;
import com.erp.model.oms.enums.KolSampleCostCostSourceEnum;
import com.erp.model.oms.enums.KolSampleCostFeeSourceEnum;
import com.erp.model.oms.enums.KolSampleCostImportFeeTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.enums.AllocationFeeTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.listener.KolSampleCostExcelListener;
import com.erp.server.oms.mapper.KolSampleCostMapper;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.KolSampleCostService;
import com.erp.server.oms.service.OperateLogService;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 寄样费用表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolSampleCostServiceImpl extends SuperServiceImpl<KolSampleCostMapper, KolSampleCostEntity> implements KolSampleCostService {
    private static final int IMPORT_ALLOCATION_SCALE = 6;
    private static final int PLM_SKU_COST_BATCH_SIZE = 200;

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private FileFeign fileFeign;

    @Autowired
    private CustomerInfoService customerInfoService;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSampleCostDTO.AddDTO addDTO) {
        KolSampleCostEntity kolSampleCostEntity = new KolSampleCostEntity();
        BeanMapperUtils.copy(addDTO, kolSampleCostEntity);

        // 数据处理
        handleData(kolSampleCostEntity);

        log.info("开始新增寄样费用单");
        boolean save = super.save(kolSampleCostEntity);
        if(!save) {
            throw new ServiceException("寄样费用单保存失败");
        }

        return new BaseResultDTO.AddDTO(kolSampleCostEntity.getId(), kolSampleCostEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSampleCostDTO.UpdateDTO addOrUpdateDTO) {
        KolSampleCostEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "寄样费用单"));
        KolSampleCostEntity kolSampleCostEntity =  BeanMapperUtils.map(KolSampleCostEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSampleCostEntity);
        log.info("编辑 开始修改寄样费用单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolSampleCostEntity);
        if(!save) {
            throw new ServiceException("寄样费用单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolSampleCostDTO.ListDTO> paging(PagingDTO<KolSampleCostDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<Object> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<KolSampleCostDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public void updateCost(KolSampleCostDTO.UpdateCostDTO dto) {
        String date = dto.getDate();
        LocalDate localDate = LocalDateTimeUtil.parseDate(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime endTIme = LocalDateUtil.getThisMonthEnd(localDate);
        LocalDateTime startTime = LocalDateUtil.getThisMonthStart(localDate);
        updateKolSampleCostByDate(startTime,endTIme);
    }

    @Override
    public void exportList(KolSampleCostDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("寄样费用导出", FileTaskEventEnum.EXPORT_OMS_KOL_SAMPLE_COST_REPORT.getCode(), param);
    }



    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        KolSampleCostExcelListener excelListenerUtil = new KolSampleCostExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), KolSampleCostImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        }
        //验证导入数据是否为空
        List<KolSampleCostImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        //导入数据处理
        List<KolSampleCostImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<KolSampleCostImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportSuccessList(successList, errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/kolSampleCostError.xlsx";
        String name = "kolSampleCostError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.FILE_EXPORT_ERROR_DATA_FAILED);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKolSampleCostJob() {
        LocalDateTime endTIme = LocalDateUtil.getThisMonthStart(LocalDate.now());
        LocalDateTime startTime = LocalDateUtil.getThisMonthStart(LocalDate.now()).minusMonths(1L);
        updateKolSampleCostByDate(startTime,endTIme);
    }


    /**
     * 根据时间区间更新寄样费用
     * @author will
     * @date 2025/12/9 15:00
     * @param startTime
     * @param endTIme
     * @return void
     */
    private void updateKolSampleCostByDate(LocalDateTime startTime,LocalDateTime endTIme) {
        //查询月份内存在的寄样费用
        List<KolSampleCostEntity> oldList = this.baseMapper.listByTime(startTime,endTIme);
        Map<String, KolSampleCostEntity> oldOutstockDetailCostMap = CollUtil.isEmpty(oldList)
                ? new HashMap<>()
                : oldList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getSoOutstockDetailId()))
                .collect(Collectors.toMap(KolSampleCostEntity::getSoOutstockDetailId, e -> e, (v1, v2) -> v1));

        //军区信息
        List<DictPartitionEntity> partitionList = FeignQuery.list(DictPartitionEntity.class);
        Map<String, String> partitionMap = CollUtil.isEmpty(partitionList) ? new HashMap<>() : partitionList.stream().collect(Collectors.toMap(DictPartitionEntity::getId, DictPartitionEntity::getName));

        //查询是时间区间内已出库的销售出库单
        List<SoOutstockDTO.KolSoOutstockDTO> soOutstockDTOList = soOutstockFeign.listSoOutstockByTime(new SoOutstockDTO.KolSoOutstockDateDTO(startTime,endTIme));
        if (CollUtil.isEmpty(soOutstockDTOList)) {
            //区间内没有出库单，如果存在oldList则直接删除
            deleteOldKolSampleCost(oldList);
            return;
        }
        List<KolSampleCostEntity> thisMonthList = new ArrayList<>();
        List<SoOutstockDTO.KolSoOutstockDTO> regularSoOutstockDTOList = soOutstockDTOList.stream()
                .filter(obj -> !isWdtKolSoOutstock(obj))
                .collect(Collectors.toList());
        List<SoOutstockDTO.KolSoOutstockDTO> wdtKolSoOutstockDTOList = soOutstockDTOList.stream()
                .filter(this::isWdtKolSoOutstock)
                .collect(Collectors.toList());

        // 客户档案军区兜底：销售订单/收件人取不到军区时，按出库单客户取
        Map<String, String> customerPartitionIdMap = buildCustomerPartitionIdMap(soOutstockDTOList);

        appendRegularKolSampleCost(thisMonthList, regularSoOutstockDTOList, oldOutstockDetailCostMap, partitionMap, customerPartitionIdMap);
        appendWdtKolSampleCost(thisMonthList, wdtKolSoOutstockDTOList, oldOutstockDetailCostMap, partitionMap, customerPartitionIdMap);

        if (CollUtil.isEmpty(thisMonthList)) {
            return;
        }
        List<String> skuIdList = thisMonthList.stream().map(KolSampleCostEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = thisMonthList.stream().map(KolSampleCostEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> soOrgIdList = thisMonthList.stream().map(KolSampleCostEntity::getSoOrgId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        String updateCostSourceMonth = startTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        // SKU成本
        List<InventorySkuCostDTO.InvSkuCostDTO> exactInvSkuCostDTOS = listInventorySkuCost(skuIdList, warehouseIdList, soOrgIdList);
        Map<String, InventorySkuCostDTO.InvSkuCostDTO> exactInvSkuCostMap = buildInventorySkuCostMap(exactInvSkuCostDTOS, true);
        boolean hasBlankSoOrgId = thisMonthList.stream().anyMatch(item -> CharSequenceUtil.isBlank(item.getSoOrgId()));
        List<InventorySkuCostDTO.InvSkuCostDTO> fallbackInvSkuCostDTOS = hasBlankSoOrgId && CollUtil.isNotEmpty(soOrgIdList)
                ? listInventorySkuCost(skuIdList, warehouseIdList, Collections.emptyList())
                : exactInvSkuCostDTOS;
        Map<String, InventorySkuCostDTO.InvSkuCostDTO> fallbackInvSkuCostMap = buildInventorySkuCostMap(fallbackInvSkuCostDTOS, false);
        List<KolSampleCostEntity> inventoryCostMissingList = thisMonthList.stream()
                .filter(item -> ObjUtil.isEmpty(resolveInventorySkuCost(item, exactInvSkuCostMap, fallbackInvSkuCostMap)))
                .collect(Collectors.toList());
        Map<String, BigDecimal> plmPurchaseAverageCostMap = buildPlmPurchaseAverageCostMap(inventoryCostMissingList);
        //小包费用分摊
        SmallBagCostAllocationDTO.SmallBagCostParamDTO bagCostParamDTO = new SmallBagCostAllocationDTO.SmallBagCostParamDTO();
        bagCostParamDTO.setSkuIdList(skuIdList);
        List<String> soOutstockDetailIdList = thisMonthList.stream().map(KolSampleCostEntity::getSoOutstockDetailId).distinct().collect(Collectors.toList());
        bagCostParamDTO.setSoOutstockDetailIdList(soOutstockDetailIdList);
        List<SmallBagCostAllocationDTO.SmallBagCostDTO> smallBagCostDTOS = ObjUtil.defaultIfNull(tmsFirstMileLogisticFeign.listSmallBagCost(bagCostParamDTO), CollUtil.newArrayList());
        Map<String, BigDecimal> smallBagCostMap = buildSmallBagCostMap(smallBagCostDTOS);
        Map<String, LogisticsChannelDTO.BaseDTO> logisticsChannelMap = buildLogisticsChannelMap(thisMonthList);

        for ( KolSampleCostEntity kolSampleCostEntity : thisMonthList) {
            kolSampleCostEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            kolSampleCostEntity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
            fillLogisticsInfo(kolSampleCostEntity, logisticsChannelMap);
            InventorySkuCostDTO.InvSkuCostDTO invSkuCostDTO = resolveInventorySkuCost(kolSampleCostEntity, exactInvSkuCostMap, fallbackInvSkuCostMap);
            if (ObjUtil.isEmpty(invSkuCostDTO)) {
                applyPurchaseAverageCost(kolSampleCostEntity, plmPurchaseAverageCostMap.get(kolSampleCostEntity.getSkuId()), updateCostSourceMonth);
            } else {
                applyInventorySkuCost(kolSampleCostEntity, invSkuCostDTO);
            }
            //设置小包费用
            applySmallBagCost(kolSampleCostEntity, smallBagCostMap);
            kolSampleCostEntity.setTotalCost(calculateTotalCost(kolSampleCostEntity));
        }
        super.saveOrUpdateBatch(thisMonthList);

    }

    private Map<String, BigDecimal> buildSmallBagCostMap(List<SmallBagCostAllocationDTO.SmallBagCostDTO> smallBagCostDTOS) {
        if (CollUtil.isEmpty(smallBagCostDTOS)) {
            return new HashMap<>();
        }
        Map<String, BigDecimal> smallBagCostMap = new HashMap<>();
        for (SmallBagCostAllocationDTO.SmallBagCostDTO smallBagCostDTO : smallBagCostDTOS) {
            if (CharSequenceUtil.isBlank(smallBagCostDTO.getSoOutstockDetailId())
                    || CharSequenceUtil.isBlank(smallBagCostDTO.getFeeType())) {
                continue;
            }
            BigDecimal cost = ObjUtil.defaultIfNull(smallBagCostDTO.getAllocatedAmountExchange(), BigDecimal.ZERO);
            String key = buildSmallBagCostKey(smallBagCostDTO.getSoOutstockDetailId(), smallBagCostDTO.getFeeType());
            smallBagCostMap.merge(key, cost, BigDecimal::add);
        }
        return smallBagCostMap;
    }

    private void applySmallBagCost(KolSampleCostEntity kolSampleCostEntity, Map<String, BigDecimal> smallBagCostMap) {
        String soOutstockDetailId = kolSampleCostEntity.getSoOutstockDetailId();
        // 未命中尾程分摊时保留历史值，避免覆盖导入维护的费用。
        BigDecimal shippingCost = smallBagCostMap.get(buildSmallBagCostKey(soOutstockDetailId, AllocationFeeTypeEnum.SHIPPING_COST.getCode()));
        boolean hasAllocationCost = false;
        if (Objects.nonNull(shippingCost)) {
            kolSampleCostEntity.setShippingCost(shippingCost);
            hasAllocationCost = true;
        }
        BigDecimal customsTax = smallBagCostMap.get(buildSmallBagCostKey(soOutstockDetailId, AllocationFeeTypeEnum.DECLARE_COST.getCode()));
        if (Objects.nonNull(customsTax)) {
            kolSampleCostEntity.setCustomsTax(customsTax);
            hasAllocationCost = true;
        }
        BigDecimal otherCost = smallBagCostMap.get(buildSmallBagCostKey(soOutstockDetailId, AllocationFeeTypeEnum.OTHER_COST.getCode()));
        if (Objects.nonNull(otherCost)) {
            kolSampleCostEntity.setOtherCost(otherCost);
            hasAllocationCost = true;
        }
        if (hasAllocationCost) {
            kolSampleCostEntity.setFeeSource(KolSampleCostFeeSourceEnum.SMALL_BAG_ALLOCATION.getCode());
        }
    }

    private String buildSmallBagCostKey(String soOutstockDetailId, String feeType) {
        return StrUtil.format("{}#{}", soOutstockDetailId, feeType);
    }

    private Map<String, LogisticsChannelDTO.BaseDTO> buildLogisticsChannelMap(List<KolSampleCostEntity> thisMonthList) {
        List<String> logisticsChannelIdList = thisMonthList.stream()
                .map(KolSampleCostEntity::getLogisticsChannelId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(logisticsChannelIdList)) {
            return new HashMap<>();
        }
        List<LogisticsChannelDTO.BaseDTO> logisticsChannelList = ObjUtil.defaultIfNull(logisticsFeign.listChannelInfoById(logisticsChannelIdList), CollUtil.newArrayList());
        return logisticsChannelList.stream()
                .filter(obj -> ObjUtil.isNotEmpty(obj) && CharSequenceUtil.isNotBlank(obj.getId()))
                .collect(Collectors.toMap(LogisticsChannelDTO.BaseDTO::getId, obj -> obj, (v1, v2) -> v1));
    }

    private void fillLogisticsInfo(KolSampleCostEntity kolSampleCostEntity,
                                   Map<String, LogisticsChannelDTO.BaseDTO> logisticsChannelMap) {
        if (CharSequenceUtil.isBlank(kolSampleCostEntity.getLogisticsChannelId()) || logisticsChannelMap.isEmpty()) {
            return;
        }
        LogisticsChannelDTO.BaseDTO channelDTO = logisticsChannelMap.get(kolSampleCostEntity.getLogisticsChannelId());
        if (ObjUtil.isEmpty(channelDTO)) {
            return;
        }
        // LogisticsChannelDTO.BaseDTO.mainId 语义是渠道父主体 ID，不是物流商 ID，
        // 不能作为 logisticsSupplierId 的回退，否则会污染 logistics_supplier_id 字段。
        // 仅在 logisticsSupplierId 为空时回退到明确的 supplierId 字段。
        String supplierId = CharSequenceUtil.blankToDefault(channelDTO.getLogisticsSupplierId(), channelDTO.getSupplierId());
        if (CharSequenceUtil.isNotBlank(supplierId)) {
            kolSampleCostEntity.setLogisticsSupplierId(supplierId);
        }
        if (CharSequenceUtil.isNotBlank(channelDTO.getLogisticsSupplierName())) {
            kolSampleCostEntity.setLogisticsSupplierName(channelDTO.getLogisticsSupplierName());
        }
        if (CharSequenceUtil.isBlank(kolSampleCostEntity.getLogisticsChannelName()) && CharSequenceUtil.isNotBlank(channelDTO.getName())) {
            kolSampleCostEntity.setLogisticsChannelName(channelDTO.getName());
        }
    }

    private List<InventorySkuCostDTO.InvSkuCostDTO> listInventorySkuCost(List<String> skuIdList,
                                                                         List<String> warehouseIdList,
                                                                         List<String> orgIdList) {
        if (CollUtil.isEmpty(skuIdList) || CollUtil.isEmpty(warehouseIdList)) {
            return CollUtil.newArrayList();
        }
        InventorySkuCostDTO.SkuCostParamDTO paramDTO = new InventorySkuCostDTO.SkuCostParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setOrgIdList(orgIdList);
        return ObjUtil.defaultIfNull(tmsFirstMileLogisticFeign.listInventorySkuCost(paramDTO), CollUtil.newArrayList());
    }

    private Map<String, InventorySkuCostDTO.InvSkuCostDTO> buildInventorySkuCostMap(List<InventorySkuCostDTO.InvSkuCostDTO> invSkuCostDTOS,
                                                                                     boolean withOrg) {
        Map<String, InventorySkuCostDTO.InvSkuCostDTO> costMap = new HashMap<>();
        if (CollUtil.isEmpty(invSkuCostDTOS)) {
            return costMap;
        }
        for (InventorySkuCostDTO.InvSkuCostDTO invSkuCostDTO : invSkuCostDTOS) {
            String key = withOrg
                    ? buildInventorySkuCostKey(invSkuCostDTO.getOrgId(), invSkuCostDTO.getSkuId(), invSkuCostDTO.getWarehouseId())
                    : buildInventorySkuCostKey(invSkuCostDTO.getSkuId(), invSkuCostDTO.getWarehouseId());
            costMap.merge(key, invSkuCostDTO, this::pickLatestInventorySkuCost);
        }
        return costMap;
    }

    private InventorySkuCostDTO.InvSkuCostDTO pickLatestInventorySkuCost(InventorySkuCostDTO.InvSkuCostDTO oldValue,
                                                                         InventorySkuCostDTO.InvSkuCostDTO newValue) {
        if (ObjUtil.isEmpty(oldValue)) {
            return newValue;
        }
        if (ObjUtil.isEmpty(newValue)) {
            return oldValue;
        }
        LocalDate oldAccountingMonth = oldValue.getAccountingMonth();
        LocalDate newAccountingMonth = newValue.getAccountingMonth();
        if (Objects.nonNull(oldAccountingMonth) && Objects.nonNull(newAccountingMonth)) {
            int accountingMonthCompare = newAccountingMonth.compareTo(oldAccountingMonth);
            if (accountingMonthCompare != 0) {
                return accountingMonthCompare > 0 ? newValue : oldValue;
            }
        } else if (Objects.isNull(oldAccountingMonth) && Objects.nonNull(newAccountingMonth)) {
            return newValue;
        } else if (Objects.nonNull(oldAccountingMonth)) {
            return oldValue;
        }

        LocalDateTime oldCreateTime = oldValue.getCreateTime();
        LocalDateTime newCreateTime = newValue.getCreateTime();
        if (Objects.isNull(oldCreateTime)) {
            return Objects.isNull(newCreateTime) ? oldValue : newValue;
        }
        if (Objects.isNull(newCreateTime)) {
            return oldValue;
        }
        return newCreateTime.isAfter(oldCreateTime) ? newValue : oldValue;
    }

    private String buildInventorySkuCostKey(String orgId, String skuId, String warehouseId) {
        return StrUtil.format("{}#{}#{}", orgId, skuId, warehouseId);
    }

    private String buildInventorySkuCostKey(String skuId, String warehouseId) {
        return StrUtil.format("{}#{}", skuId, warehouseId);
    }

    private InventorySkuCostDTO.InvSkuCostDTO resolveInventorySkuCost(KolSampleCostEntity kolSampleCostEntity,
                                                                      Map<String, InventorySkuCostDTO.InvSkuCostDTO> exactInvSkuCostMap,
                                                                      Map<String, InventorySkuCostDTO.InvSkuCostDTO> fallbackInvSkuCostMap) {
        // 优先按销售组织+SKU+仓库匹配；销售组织为空时按 SKU+仓库兜底。
        InventorySkuCostDTO.InvSkuCostDTO invSkuCostDTO = exactInvSkuCostMap.get(buildInventorySkuCostKey(
                kolSampleCostEntity.getSoOrgId(), kolSampleCostEntity.getSkuId(), kolSampleCostEntity.getWarehouseId()));
        if (ObjUtil.isEmpty(invSkuCostDTO) && CharSequenceUtil.isBlank(kolSampleCostEntity.getSoOrgId())) {
            invSkuCostDTO = fallbackInvSkuCostMap.get(buildInventorySkuCostKey(kolSampleCostEntity.getSkuId(), kolSampleCostEntity.getWarehouseId()));
        }
        return invSkuCostDTO;
    }

    private void applyInventorySkuCost(KolSampleCostEntity kolSampleCostEntity, InventorySkuCostDTO.InvSkuCostDTO invSkuCostDTO) {
        BigDecimal qty = MathUtil.valueOf(kolSampleCostEntity.getQty());
        BigDecimal exchangeRate = ObjUtil.defaultIfNull(invSkuCostDTO.getExchangeRate(), BigDecimal.ONE);
        kolSampleCostEntity.setExchangeRate(exchangeRate);
        BigDecimal productCost = MathUtil.multiplyWithFour(ObjUtil.defaultIfNull(invSkuCostDTO.getProductCost(), BigDecimal.ZERO), exchangeRate);
        BigDecimal firstMileShippingCost = MathUtil.multiplyWithFour(ObjUtil.defaultIfNull(invSkuCostDTO.getFirstMileShippingCost(), BigDecimal.ZERO), exchangeRate);
        BigDecimal clearanceCustomsTax = MathUtil.multiplyWithFour(ObjUtil.defaultIfNull(invSkuCostDTO.getClearanceCustomsTax(), BigDecimal.ZERO), exchangeRate);
        kolSampleCostEntity.setProductCost(MathUtil.multiplyWithFour(productCost, qty));
        kolSampleCostEntity.setFirstMileShippingCost(MathUtil.multiplyWithFour(firstMileShippingCost, qty));
        kolSampleCostEntity.setClearanceCustomsTax(MathUtil.multiplyWithFour(clearanceCustomsTax, qty));
        kolSampleCostEntity.setCostSource(KolSampleCostCostSourceEnum.SKU_COST.getCode());
        if (Objects.nonNull(invSkuCostDTO.getAccountingMonth())) {
            kolSampleCostEntity.setCostSourceMonth(invSkuCostDTO.getAccountingMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
    }

    private Map<String, BigDecimal> buildPlmPurchaseAverageCostMap(List<KolSampleCostEntity> thisMonthList) {
        List<String> skuIdList = thisMonthList.stream()
                // listByTime 从 DB 加载的数据不会带 @TableField(exist = false) 的 purchaseAverageCost，TMS 未命中时再按 SKU 兜底查 PLM。
                .map(KolSampleCostEntity::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)) {
            return new HashMap<>();
        }
        List<SkuVO> skuVOList = new ArrayList<>();
        for (List<String> batchSkuIdList : Lists.partition(skuIdList, PLM_SKU_COST_BATCH_SIZE)) {
            skuVOList.addAll(ObjUtil.defaultIfNull(plmTaskFeign.listSkuCostByIds(batchSkuIdList), CollUtil.newArrayList()));
        }
        return skuVOList.stream()
                .filter(item -> ObjUtil.isNotEmpty(item) && CharSequenceUtil.isNotBlank(item.getSkuId()))
                .filter(item -> isPositive(item.getActualTaxCost()))
                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getActualTaxCost, (v1, v2) -> v1));
    }

    private void applyPurchaseAverageCost(KolSampleCostEntity kolSampleCostEntity, BigDecimal plmPurchaseAverageCost, String updateCostSourceMonth) {
        BigDecimal qty = MathUtil.valueOf(kolSampleCostEntity.getQty());
        BigDecimal productCost = kolSampleCostEntity.getPurchaseAverageCost();
        if (!isPositive(productCost)) {
            productCost = plmPurchaseAverageCost;
        }
        if (!isPositive(productCost)) {
            log.warn("SKU含税采购平均成本为空或0，将使用0作为兜底成本。skuId={}, skuNo={}, soCode={}, sourceType={}, sourceDetailId={}",
                    kolSampleCostEntity.getSkuId(),
                    kolSampleCostEntity.getSkuNo(),
                    kolSampleCostEntity.getSoCode(),
                    kolSampleCostEntity.getSourceType(),
                    kolSampleCostEntity.getSourceDetailId());
            productCost = BigDecimal.ZERO;
        }
        kolSampleCostEntity.setExchangeRate(BigDecimal.ONE);
        kolSampleCostEntity.setProductCost(MathUtil.multiplyWithFour(productCost, qty));
        kolSampleCostEntity.setFirstMileShippingCost(BigDecimal.ZERO);
        kolSampleCostEntity.setClearanceCustomsTax(BigDecimal.ZERO);
        kolSampleCostEntity.setCostSource(KolSampleCostCostSourceEnum.PURCHASE_AVG_COST.getCode());
        kolSampleCostEntity.setCostSourceMonth(updateCostSourceMonth);
    }

    private boolean isPositive(BigDecimal value) {
        return Objects.nonNull(value) && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private Map<String, String> buildCustomerPartitionIdMap(List<SoOutstockDTO.KolSoOutstockDTO> soOutstockDTOList) {
        if (CollUtil.isEmpty(soOutstockDTOList)) {
            return new HashMap<>();
        }
        List<String> customerIds = soOutstockDTOList.stream()
                .map(SoOutstockDTO.KolSoOutstockDTO::getCustomerId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(customerIds)) {
            return new HashMap<>();
        }
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.listByIds(customerIds);
        if (CollUtil.isEmpty(customerInfoEntities)) {
            return new HashMap<>();
        }
        return customerInfoEntities.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .filter(item -> CharSequenceUtil.isNotBlank(item.getPartitionId()))
                .collect(Collectors.toMap(CustomerInfoEntity::getId, CustomerInfoEntity::getPartitionId, (a, b) -> a));
    }

    private void appendRegularKolSampleCost(List<KolSampleCostEntity> thisMonthList,
                                            List<SoOutstockDTO.KolSoOutstockDTO> soOutstockDTOList,
                                            Map<String, KolSampleCostEntity> oldOutstockDetailCostMap,
                                            Map<String, String> partitionMap,
                                            Map<String, String> customerPartitionIdMap) {
        if (CollUtil.isEmpty(soOutstockDTOList)) {
            return;
        }
        Map<String, List<SoOutstockDTO.KolSoOutstockDTO>> soOutstockMap = soOutstockDTOList.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getSoDetailId()))
                .collect(Collectors.groupingBy(SoOutstockDTO.KolSoOutstockDTO::getSoDetailId));
        List<String> soDetailIdList = new ArrayList<>(soOutstockMap.keySet());
        if (CollUtil.isEmpty(soDetailIdList)) {
            return;
        }
        List<List<String>> partitionList = Lists.partition(soDetailIdList, 5000);
        for (List<String> partitionIdList : partitionList) {
            List<KolSampleCostEntity> kolSampleCostEntityList = baseMapper.listKolSampleCostBySoDetailIdList(partitionIdList);
            if (CollUtil.isEmpty(kolSampleCostEntityList)) {
                continue;
            }
            for (KolSampleCostEntity sampleCostEntity : kolSampleCostEntityList) {
                appendKolSampleCost(thisMonthList, soOutstockMap.get(sampleCostEntity.getSoDetailId()), sampleCostEntity, oldOutstockDetailCostMap, partitionMap, customerPartitionIdMap, false);
            }
        }
    }

    private void appendWdtKolSampleCost(List<KolSampleCostEntity> thisMonthList,
                                        List<SoOutstockDTO.KolSoOutstockDTO> soOutstockDTOList,
                                        Map<String, KolSampleCostEntity> oldOutstockDetailCostMap,
                                        Map<String, String> partitionMap,
                                        Map<String, String> customerPartitionIdMap) {
        if (CollUtil.isEmpty(soOutstockDTOList)) {
            return;
        }
        List<SoOutstockDTO.KolSoOutstockDTO> validSoOutstockDTOList = soOutstockDTOList.stream()
                .filter(obj -> CharSequenceUtil.isNotBlank(obj.getPlatformCode()) && CharSequenceUtil.isNotBlank(obj.getSkuNo()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validSoOutstockDTOList)) {
            return;
        }
        Map<String, List<SoOutstockDTO.KolSoOutstockDTO>> soOutstockMap = validSoOutstockDTOList.stream()
                .collect(Collectors.groupingBy(this::buildWdtKolMatchKey));
        List<String> platformCodeList = validSoOutstockDTOList.stream()
                .map(SoOutstockDTO.KolSoOutstockDTO::getPlatformCode)
                .distinct()
                .collect(Collectors.toList());
        List<List<String>> partitionList = Lists.partition(platformCodeList, 5000);
        for (List<String> partitionIdList : partitionList) {
            Set<String> platformCodeSet = new HashSet<>(partitionIdList);
            List<SoOutstockDTO.KolSoOutstockDTO> partitionSoOutstockDTOList = validSoOutstockDTOList.stream()
                    .filter(obj -> platformCodeSet.contains(obj.getPlatformCode()))
                    .collect(Collectors.toList());
            List<KolSampleCostEntity> kolSampleCostEntityList = baseMapper.listKolSampleCostByWdtPlatformCodeList(partitionIdList);
            Map<String, KolSampleCostEntity> hitMap = CollUtil.isEmpty(kolSampleCostEntityList)
                    ? new HashMap<>()
                    : kolSampleCostEntityList.stream()
                    .filter(obj -> CharSequenceUtil.isNotBlank(obj.getSourceCode()) && CharSequenceUtil.isNotBlank(obj.getSkuNo()))
                    .collect(Collectors.toMap(this::buildWdtKolMatchKey, obj -> obj, (v1, v2) -> v1));
            for (SoOutstockDTO.KolSoOutstockDTO kolSoOutstockDTO : partitionSoOutstockDTOList) {
                String matchKey = buildWdtKolMatchKey(kolSoOutstockDTO);
                if (hitMap.containsKey(matchKey)) {
                    continue;
                }
                log.warn("KOL寄样费用统计跳过WDT出库，未命中寄样申请明细，platformCode:{}, skuNo:{}, soOutstockCode:{}, soOutstockDetailId:{}",
                        kolSoOutstockDTO.getPlatformCode(), kolSoOutstockDTO.getSkuNo(), kolSoOutstockDTO.getSoOutstockCode(), kolSoOutstockDTO.getSoOutstockDetailId());
            }
            for (Map.Entry<String, KolSampleCostEntity> entry : hitMap.entrySet()) {
                appendKolSampleCost(thisMonthList, soOutstockMap.get(entry.getKey()), entry.getValue(), oldOutstockDetailCostMap, partitionMap, customerPartitionIdMap, true);
            }
        }
    }

    private void appendKolSampleCost(List<KolSampleCostEntity> thisMonthList,
                                     List<SoOutstockDTO.KolSoOutstockDTO> kolSoOutstockDTOList,
                                     KolSampleCostEntity sampleCostEntity,
                                     Map<String, KolSampleCostEntity> oldOutstockDetailCostMap,
                                     Map<String, String> partitionMap,
                                     Map<String, String> customerPartitionIdMap,
                                     boolean useWdtSourceCodeAsSoCode) {
        if (CollUtil.isEmpty(kolSoOutstockDTOList) || ObjUtil.isEmpty(sampleCostEntity)) {
            return;
        }
        for (SoOutstockDTO.KolSoOutstockDTO kolSoOutstockDTO : kolSoOutstockDTOList) {
            KolSampleCostEntity costEntity = new KolSampleCostEntity();
            BeanMapperUtils.copy(kolSoOutstockDTO, costEntity);
            fillHistoryCost(costEntity, oldOutstockDetailCostMap.get(kolSoOutstockDTO.getSoOutstockDetailId()));
            costEntity.setType(sampleCostEntity.getType());
            costEntity.setSourceCode(sampleCostEntity.getSourceCode());
            costEntity.setSourceId(sampleCostEntity.getSourceId());
            costEntity.setSourceType(sampleCostEntity.getSourceType());
            costEntity.setSourceDetailId(sampleCostEntity.getSourceDetailId());
            // 优先按 销售订单/收件人 取军区，未取到则按出库单客户档案兜底
            String partitionId = sampleCostEntity.getPartitionId();
            if (CharSequenceUtil.isBlank(partitionId)) {
                partitionId = customerPartitionIdMap.get(kolSoOutstockDTO.getCustomerId());
            }
            costEntity.setPartitionId(partitionId);
            costEntity.setPartitionName(partitionMap.get(partitionId));
            costEntity.setPartnerId(sampleCostEntity.getPartnerId());
            costEntity.setPartnerNickname(sampleCostEntity.getPartnerNickname());
            costEntity.setFeedbackUrl(sampleCostEntity.getFeedbackUrl());
            costEntity.setPurchaseAverageCost(sampleCostEntity.getPurchaseAverageCost());
            if (useWdtSourceCodeAsSoCode) {
                costEntity.setSoCode(kolSoOutstockDTO.getSourceCode());
            }
            costEntity.setSoOrgId(kolSoOutstockDTO.getSalesOrgId());
            costEntity.setSoOrgName(kolSoOutstockDTO.getSalesOrgName());
            costEntity.setQty(kolSoOutstockDTO.getActualQty());
            costEntity.setSoOutstockDate(kolSoOutstockDTO.getSoOutstockDate());
            thisMonthList.add(costEntity);
        }
    }

    private void fillHistoryCost(KolSampleCostEntity costEntity, KolSampleCostEntity oldEntity) {
        if (ObjUtil.isEmpty(oldEntity)) {
            return;
        }
        costEntity.setId(oldEntity.getId());
        // 更新费用时如果拿不到新的SKU成本/尾程分摊，保持历史费用不变
        costEntity.setProductCost(ObjUtil.defaultIfNull(oldEntity.getProductCost(), BigDecimal.ZERO));
        costEntity.setFirstMileShippingCost(ObjUtil.defaultIfNull(oldEntity.getFirstMileShippingCost(), BigDecimal.ZERO));
        costEntity.setClearanceCustomsTax(ObjUtil.defaultIfNull(oldEntity.getClearanceCustomsTax(), BigDecimal.ZERO));
        costEntity.setShippingCost(ObjUtil.defaultIfNull(oldEntity.getShippingCost(), BigDecimal.ZERO));
        costEntity.setCustomsTax(ObjUtil.defaultIfNull(oldEntity.getCustomsTax(), BigDecimal.ZERO));
        costEntity.setOtherCost(ObjUtil.defaultIfNull(oldEntity.getOtherCost(), BigDecimal.ZERO));
        costEntity.setTotalCost(ObjUtil.defaultIfNull(oldEntity.getTotalCost(), BigDecimal.ZERO));
        costEntity.setExchangeRate(oldEntity.getExchangeRate());
        costEntity.setCurrency(CharSequenceUtil.blankToDefault(oldEntity.getCurrency(), CurrencyEnum.CNY.getCurrencyCode()));
        costEntity.setCurrencySymbol(CharSequenceUtil.blankToDefault(oldEntity.getCurrencySymbol(), CurrencyEnum.CNY.getCurrencySymbol()));
        costEntity.setCostSource(oldEntity.getCostSource());
        costEntity.setCostSourceMonth(oldEntity.getCostSourceMonth());
        costEntity.setFeeSource(oldEntity.getFeeSource());
        costEntity.setLogisticsSupplierId(oldEntity.getLogisticsSupplierId());
        costEntity.setLogisticsSupplierName(oldEntity.getLogisticsSupplierName());
        costEntity.setLogisticsChannelId(CharSequenceUtil.blankToDefault(costEntity.getLogisticsChannelId(), oldEntity.getLogisticsChannelId()));
        costEntity.setLogisticsChannelName(CharSequenceUtil.blankToDefault(costEntity.getLogisticsChannelName(), oldEntity.getLogisticsChannelName()));
    }

    private boolean isWdtKolSoOutstock(SoOutstockDTO.KolSoOutstockDTO dto) {
        return CharSequenceUtil.isNotBlank(dto.getPlatformCode()) && StrUtil.startWithIgnoreCase(dto.getPlatformCode(), "KOL");
    }

    private String buildWdtKolMatchKey(SoOutstockDTO.KolSoOutstockDTO dto) {
        return StrUtil.format("{}#{}", dto.getPlatformCode(), dto.getSkuNo());
    }

    private String buildWdtKolMatchKey(KolSampleCostEntity entity) {
        String matchSourceCode = CharSequenceUtil.blankToDefault(entity.getMatchSourceCode(), entity.getSourceCode());
        return StrUtil.format("{}#{}", matchSourceCode, entity.getSkuNo());
    }

    /**
     * 删除旧的寄样费用数据
     * @author will
     * @date 2025/12/9 16:00
     * @param oldList
     * @return void
     */
    private void deleteOldKolSampleCost(List<KolSampleCostEntity> oldList) {
        if (CollUtil.isEmpty(oldList)) {
            return;
        }
        List<String> idList = oldList.stream().map(KolSampleCostEntity::getId).collect(Collectors.toList());
        super.removeByIds(idList);
    }

    /**
     * 处理导入数据
     * @author will
     * @date 2025/12/8 19:03
     * @param successList
     * @param errorList
     * @return void
     */
    private void handleImportSuccessList(List<KolSampleCostImportExcelDTO> successList, List<KolSampleCostImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<String> soCodeList = successList.stream().map(KolSampleCostImportExcelDTO::getSoCode).distinct().collect(Collectors.toList());
        List<KolSampleCostEntity> kolSampleCostList = listBySoCodeList(soCodeList);
        for (KolSampleCostImportExcelDTO importExcelDTO : successList) {
                KolSampleCostImportFeeTypeEnum feeTypeEnum = KolSampleCostImportFeeTypeEnum.getByName(importExcelDTO.getFeeType());
                if (feeTypeEnum == null) {
                    importExcelDTO.setErrorMsg("费用项仅支持" + KolSampleCostImportFeeTypeEnum.getSupportedNameTips());
                    errorList.add(importExcelDTO);
                    continue;
                }
           /*     long count = successList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSoCode(), importExcelDTO.getSoCode())).count();
                if (count > 1) {
                    importExcelDTO.setErrorMsg("销售订单号【" + importExcelDTO.getSoCode() + "】在导入数据中存在重复");
                    errorList.add(importExcelDTO);
                    continue;
                }*/
                List<KolSampleCostEntity> costList = kolSampleCostList.stream()
                        .filter(obj -> CharSequenceUtil.equals(obj.getSoCode(), importExcelDTO.getSoCode()))
                        .sorted(Comparator.comparing(KolSampleCostEntity::getSoOutstockDetailId, Comparator.nullsLast(String::compareTo))
                                .thenComparing(KolSampleCostEntity::getId, Comparator.nullsLast(String::compareTo)))
                        .collect(Collectors.toList());
                if (CollUtil.isEmpty(costList)) {
                    importExcelDTO.setErrorMsg("未找到对应的销售订单号：" + importExcelDTO.getSoCode());
                    errorList.add(importExcelDTO);
                    continue;
                }
            //计算总数量
            Integer totalQty = costList.stream()
                    .map(KolSampleCostEntity::getQty)
                    .map(qty -> ObjUtil.defaultIfNull(qty, MathUtil.ZERO))
                    .filter(qty -> qty > 0)
                    .reduce(MathUtil.ZERO, Integer::sum);
            if (totalQty <= 0) {
                importExcelDTO.setErrorMsg("销售订单号【" + importExcelDTO.getSoCode() + "】对应实发数量总和必须大于0");
                errorList.add(importExcelDTO);
                continue;
            }
            BigDecimal importAmount = MathUtil.valueOf(importExcelDTO.getAmountStr());
            BigDecimal exchangeRate = MathUtil.valueOf(importExcelDTO.getExchangeRateStr());
            BigDecimal importLocalAmount = importAmount.multiply(exchangeRate).setScale(IMPORT_ALLOCATION_SCALE, RoundingMode.HALF_UP);
            BigDecimal totalQtyDecimal = MathUtil.valueOf(totalQty);
            BigDecimal allocatedAmount = BigDecimal.ZERO;
            int lastPositiveQtyIndex = findLastPositiveQtyIndex(costList);
            for (int index = 0; index < costList.size(); index++) {
                KolSampleCostEntity entity = costList.get(index);
                BigDecimal cost;
                Integer qty = ObjUtil.defaultIfNull(entity.getQty(), MathUtil.ZERO);
                if (qty <= 0) {
                    cost = BigDecimal.ZERO;
                } else if (index == lastPositiveQtyIndex) {
                    // 最后一条有实发数量的SKU吸收尾差，避免尾差落到0数量行。
                    cost = importLocalAmount.subtract(allocatedAmount);
                } else {
                    cost = MathUtil.valueOf(qty)
                            .multiply(importLocalAmount)
                            .divide(totalQtyDecimal, IMPORT_ALLOCATION_SCALE, RoundingMode.HALF_UP);
                    allocatedAmount = allocatedAmount.add(cost);
                }
                BigDecimal historyShippingCost = ObjUtil.defaultIfNull(entity.getShippingCost(), BigDecimal.ZERO);
                BigDecimal historyCustomsTax = ObjUtil.defaultIfNull(entity.getCustomsTax(), BigDecimal.ZERO);
                BigDecimal historyOtherCost = ObjUtil.defaultIfNull(entity.getOtherCost(), BigDecimal.ZERO);
                switch (feeTypeEnum) {
                    case LOGISTICS_FEE:
                        //“尾程-运费”=当前行SKU实发数量/同一销售单号所有SKU实发数量*原币金额*汇率+历史SKU“尾程-运费”
                        entity.setShippingCost(historyShippingCost.add(cost));
                        break;
                    case CUSTOMS_TAX:
                        //“尾程-关税”=当前行SKU实发数量/同一销售单号所有SKU实发数量*原币金额*汇率+历史“尾程-关税”
                        entity.setCustomsTax(historyCustomsTax.add(cost));
                        break;
                    case ORDER_FEE:
                        //“尾程-其他费用”=当前行SKU实发数量/同一销售单号所有SKU实发数量*原币金额*汇率+历史“尾程-其他费用”
                        entity.setOtherCost(historyOtherCost.add(cost));
                        break;
                    default:
                        throw new ServiceException("不支持的费用项：" + importExcelDTO.getFeeType());
                }
                entity.setFeeSource(KolSampleCostFeeSourceEnum.IMPORT.getCode());
                entity.setTotalCost(calculateTotalCost(entity));
            }
                super.updateBatchById(costList);

                //操作日志
            List<Pair<String, String>> pairList = costList.stream().map(obj -> new Pair<>(obj.getId(), obj.getSoCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(CharSequenceUtil.format("费用项【{}】，寄样成本字段【{}】，原币金额【{}】，汇率【{}】",importExcelDTO.getFeeType(),feeTypeEnum.getCostFieldName(),importExcelDTO.getAmountStr(),importExcelDTO.getExchangeRateStr()), ModuleTypeEnum.KOL_KOL_SAMPLE_COST.getCode(),pairList ,"尾程费用导入");
        }
    }

    /**
     * 根据销售订单号列表查询寄样费用数据
     * @author will
     * @date 2025/12/9 09:19
     * @param soCodeList
     * @return List<KolSampleCostEntity>
     */
    private List<KolSampleCostEntity> listBySoCodeList(List<String> soCodeList) {
        if (CollectionUtils.isEmpty(soCodeList)) {
            return CollUtil.newArrayList();
        }
        return lambdaQuery().in(KolSampleCostEntity::getSoCode, soCodeList).list();
    }

    private int findLastPositiveQtyIndex(List<KolSampleCostEntity> costList) {
        for (int index = costList.size() - 1; index >= 0; index--) {
            Integer qty = ObjUtil.defaultIfNull(costList.get(index).getQty(), MathUtil.ZERO);
            if (qty > 0) {
                return index;
            }
        }
        return -1;
    }

    private BigDecimal calculateTotalCost(KolSampleCostEntity entity) {
        return ObjUtil.defaultIfNull(entity.getProductCost(), BigDecimal.ZERO)
                .add(ObjUtil.defaultIfNull(entity.getFirstMileShippingCost(), BigDecimal.ZERO))
                .add(ObjUtil.defaultIfNull(entity.getClearanceCustomsTax(), BigDecimal.ZERO))
                .add(ObjUtil.defaultIfNull(entity.getShippingCost(), BigDecimal.ZERO))
                .add(ObjUtil.defaultIfNull(entity.getCustomsTax(), BigDecimal.ZERO))
                .add(ObjUtil.defaultIfNull(entity.getOtherCost(), BigDecimal.ZERO));
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<KolSampleCostDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(KolSampleCostDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String, String> skuMap = CollUtil.isEmpty(skuList) ? new HashMap<>() : skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));
        for (KolSampleCostDTO.ListDTO listDTO : list) {
            listDTO.setProductName(skuMap.get(listDTO.getSkuId()));
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolSampleCostEntity kolSampleCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
