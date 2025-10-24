package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.FullyManagedImportExcelDTO;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.VirtualInventoryFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.listener.FullyManagedImportExcelListener;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.query.FullyManagedQueryHandler;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_FULLY_MANAGED;

/**
 * @author zdy
 * @ClassName FullyManagedOrderServiceImpl
 * @description: 全托管订单服务
 * @date 2025年03月25日
 * @version: 1.0
 */
@Slf4j
@Service
public class FullyManagedOrderServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements FullyManagedOrderService {
    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;
    @Resource
    @Qualifier("soB2cTabExecutorPool")
    private ExecutorService soB2cTabExecutorPool;
    @Resource
    private FullyManagedQueryHandler fullyManagedQueryHandler;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;
    @Resource
    private InventoryFeign inventoryFeign;
    @Lazy
    @Resource
    private SoB2cRefService soB2cRefService;
    @Lazy
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;
    @Resource
    private SoB2cRefCategoryService soB2cRefCategoryService;
    @Resource
    private AuthDataFeign authDataFeign;

    @Resource
    private SoB2cRuleService soB2cRuleService;

    @Resource
    private WorkflowFeign workflowFeign;


    @Override
    public List<SoB2cDTO.TabListDTO> fullyManagedTabList(PermissionsDTO param) {
        FullyManagedTabEnum[] values = FullyManagedTabEnum.values();
        List<Future<SoB2cDTO.TabListDTO>> futureList = new ArrayList<>();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        String permissionSql = getPermissionSql();
        for (FullyManagedTabEnum item : values) {
            Future<SoB2cDTO.TabListDTO> submit = soB2cTabExecutorPool.submit(() -> {
                SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
                searchParamDTO.setPermissionSql(permissionSql);
                SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
                String tabSql = fullyManagedQueryHandler.getTabSql(item.getCode());
                HashMap<String,String> map = new HashMap<>();
                map.put("default",tabSql);
                searchParamDTO.setSqlMap(map);
                //查询店铺设置权限
                Integer count = this.baseMapper.listFullManagedCount(searchParamDTO);
                resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
                resultDTO.setTabFlag(item.getCode());
                resultDTO.setTabFlagName(item.getName());
                return resultDTO;
            });
            futureList.add(submit);
        }
        for(Future<SoB2cDTO.TabListDTO> f : futureList) {
            try {
                list.add(f.get());
            } catch (InterruptedException e) {
                // 恢复线程的中断状态，确保中断标志不会被忽略
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
                throw new ServiceException("线程被中断", e);
            } catch (ExecutionException e) {
                log.error("线程任务执行异常", e);
                throw new ServiceException("线程任务执行异常", e.getCause());
            } catch (ThreadDeath td) {
                log.error("捕获到 ThreadDeath，线程终止", td);
                throw td; // 重新抛出以允许线程正常终止
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeOutConfig(CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO) {
        //检查预警设置是否存在 更新配置
        CfgSettingEntity setting = cfgSettingService.getSettingByKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        if (Objects.isNull(setting)) {
            setting = new CfgSettingEntity();
        }
        setting.setKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        setting.setValue(JSONUtil.parseObj(timeOutSettingDTO).toString());
        cfgSettingService.saveOrUpdate(setting);
        //修改全托管订单的预警时间
        List<DictBasicDTO.ViewDTO> dtoList = dictBasicService.getByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
        List<String> platformList = dtoList.stream().map(DictBasicDTO.ViewDTO::getValue).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //更新全托管订单的预警时间
        this.baseMapper.updateTimeOutConfig(platformList,timeOutSettingDTO.getWarningTime().multiply(new BigDecimal(60)).intValue());
    }
    private String getPermissionSql() {
        String shopPermissionSql = authDataFeign.getShopPermissionSql("sb2c.shop_id");
        shopPermissionSql = CharSequenceUtil.isNotBlank(shopPermissionSql) ? shopPermissionSql : " and 1=1 ";
        String warehousePermissionSql = authDataFeign.getWarehousePermissionSql("sb2cd.warehouse_id");
        warehousePermissionSql = CharSequenceUtil.isNotBlank(warehousePermissionSql) ? " and exists (select 1 from so_b2c_detail sb2cd where sb2c.id = sb2cd.main_id and sb2cd.is_deleted=FALSE " + warehousePermissionSql + ")" : " and 1=1 ";
        return CharSequenceUtil.format(" {}  {}", shopPermissionSql, warehousePermissionSql);
    }
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/fullyManagedOrderTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        FullyManagedImportExcelListener excelListenerUtil = new FullyManagedImportExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FullyManagedImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            //错误的
            List<FullyManagedImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            //数据验证
            List<FullyManagedImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            //处理验证成功数据
            handleImportSuccessList(successList, errorList);
            if (errorList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/fullyManagedOrderError.xlsx";
                String name = "fullyManaged";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.ERROR_95125);
                }
                return Boolean.FALSE;
            }
        } catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }catch (ExcelAnalysisException e){
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1033);
        }
        return Boolean.TRUE;

    }

    @Override
    public Boolean exportExcel(SoB2cDTO.ExportParamDTO params) {
        downloadTaskFeign.saveDownloadTask("全托管销售订单", EXPORT_OMS_FULLY_MANAGED.getCode(), params);
        return Boolean.TRUE;
    }

    private void handleImportSuccessList(List<FullyManagedImportExcelDTO> successList, List<FullyManagedImportExcelDTO> errorList) {
        //上游已经将数据处理完成 现在开始执行导入
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //根据平台单号进行分组
        Map<String, List<FullyManagedImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(FullyManagedImportExcelDTO::getIndexStr));
        //遍历分组数据
        collect.forEach((key, value) -> {
            SoB2cDTO.AddDTO addDTO = buildAddDTO(key,value,errorList);
            if (Objects.nonNull(addDTO)){
                try {
                    SoB2cEntity soB2cEntity = soB2cService.add(addDTO, null);
                    //匹配订单规则
                    SoB2cDTO.RuleResultDTO orderRuleResult = soB2cService.orderRule(soB2cEntity.getId());
                    //匹配成功
                    if (orderRuleResult.getIsRuleMatch() && orderRuleResult.getIsPass()) {
                        //仓库规则
                        SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRule(orderRuleResult.getId(), orderRuleResult.getSoB2cDetailList(), orderRuleResult.getMap());
                        Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
                        if (warehouseRuleMatch) {
                            SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRule(soB2cEntity.getId(), new HashMap<>(), false);
                            SoB2cEntity entity = soB2cService.getById(soB2cEntity.getId());
                            if ((Objects.nonNull(logisticsRuleResult.getAutoGetTrackNo()) && Boolean.TRUE.equals(logisticsRuleResult.getAutoGetTrackNo()))
                                    || (Boolean.FALSE.equals(entity.getIsOutOfRangeDelivery()) && Objects.nonNull(logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery()) && Boolean.TRUE.equals(logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery()))) {
                                soB2cRuleService.handleAutoSubmitDelivery(soB2cEntity.getId(), logisticsRuleResult.getName());
                            }
                        }
                    }
                    //自动计算预估运费到订单的预估运费字段
                    soB2cService.autoCalcEstimatedShippingCost(Collections.singletonList(soB2cEntity.getId()));
                }catch (Exception e){
                    log.error("导入失败！>>>{}", e);
                    value.forEach(f -> f.setErrorMsg(CharSequenceUtil.format("平台【{}】平台订单号【{}】新增失败：【{}】",f.getDictPlatformName(), f.getPlatformCode(), e.getMessage())));
                    errorList.addAll(value);
                }
            }
        });
    }

    /**
     * 构建导入数据
     * @param key = platformCode + dictPlatform
     * @param value
     * @param errorList
     * @return
     */
    private SoB2cDTO.AddDTO buildAddDTO(String key, List<FullyManagedImportExcelDTO> value, List<FullyManagedImportExcelDTO> errorList) {
        //平台列表
//        List<SoB2cEntity> entityList = this.lambdaQuery().eq(SoB2cEntity::getPlatformCode, value.get(0).getPlatformCode()).eq(SoB2cEntity::getDictPlatform, value.get(0).getDictPlatform()).list();
//        if (CollUtil.isNotEmpty(entityList)) {
//            List<String> codeList = entityList.stream().map(SoB2cEntity::getCode).distinct().collect(Collectors.toList());
//            value.forEach(e -> e.setErrorMsg(CharSequenceUtil.format("平台【{}】平台订单号【{}】销售订单已存在【{}】",e.getDictPlatformName(), e.getPlatformCode(), CharSequenceUtil.join(",",codeList))));
//            errorList.addAll(value);
//            return null;
//        }
        if (value.size() > 1){
            //判断每个导入列中字段值是否一致
            boolean flag = value.stream()
                    .allMatch(e -> e.getPrice().compareTo(value.get(0).getPrice()) == 0
                            && e.getCurrencyCode().equals(value.get(0).getCurrencyCode())
                            && e.getPayTime().equals(value.get(0).getPayTime())
                            && e.getOrderSourceType().equals(value.get(0).getOrderSourceType())
                    );
            if (!flag){
                value.forEach(e -> e.setErrorMsg(CharSequenceUtil.format("平台【{}】平台订单号【{}】中订单金额/币别/下单时间/平台来源需要一致",e.getDictPlatformName(), e.getPlatformCode())));
                errorList.addAll(value);
                return null;
            }
        }
        SoB2cDTO.AddDTO addDTO = B2cOrderConverter.INSTANCE.convertFullyManagedExcelDTO(value.get(0));
        addDTO.setExtendDTO(B2cOrderConverter.INSTANCE.convertFullyManagedExtendDTO(value.get(0)));
        addDTO.setDetailList(B2cOrderConverter.INSTANCE.convertFullyManagedDetailDTO(value));
        addDTO.setLogisticsDTO(B2cOrderConverter.INSTANCE.convertFullyManagedLogisticsDTO(value.get(0)));
        return addDTO;
    }


    @Override
    public PagingVO<SoB2cDTO.ExcelExportDTO> exportFullyManagedOrder(PagingDTO<SoB2cDTO.ExportParamDTO> dto) {
        Page<SoB2cDTO.ExcelExportDTO> page;
        List<SoB2cDTO.ExcelExportDTO> records;
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        List<AdvanceQueryDTO> advanceQueryDTOList = dto.getParams().getAdvanceQueryDTOList();
        //是否缺货 过滤
        Boolean isOutStock = (Boolean) advanceQueryDTOList.stream().filter(v -> v.getField().equals("isOutStock")).findAny().orElse(new AdvanceQueryDTO()).getValue();
        try {
            if (Objects.nonNull(isOutStock)) {
                //必须选仓库而且只能选一个
                List<String> warehouseIdList = com.common.business.utils.CollectionUtils.convertStrClzToList(advanceQueryDTOList.stream().filter(v -> v.getField().equals("sb2cd.warehouse_id") && (v.getCompare().equals(QueryConditionEnum.EQ.getCompareCode()) || v.getCompare().equals(QueryConditionEnum.IN_LIST.getCompareCode()))).findFirst().orElse(new AdvanceQueryDTO()).getValue());
                if (warehouseIdList.size() != 1) {
                    throw new ServiceException("选择缺货条件必须选择仓库且只能选择一个仓库");
                }
                page = this.baseMapper.exportFullyManagedExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), Boolean.TRUE);
                // 数据处理
                records = handleExport(page.getRecords(), dto.getParams().getExportType(), Boolean.TRUE);
                if (isOutStock) {
                    //缺货
                    records = records.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.getIsOutStock()) && obj.getIsOutStock()).collect(Collectors.toList());
                    ;
                } else {
                    //无缺货标识或不缺货
                    records = records.stream().filter(obj -> ObjectUtil.isEmpty(obj.getIsOutStock()) || !obj.getIsOutStock()).collect(Collectors.toList());
                }
            } else {
                page = this.baseMapper.exportFullyManagedExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), null);
                // 数据处理
                records = handleExport(page.getRecords(), dto.getParams().getExportType(), Boolean.FALSE);
            }
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        return new PagingVO<>(records, (int) page.getTotal(), dto.getPageSize(), dto.getCurrPage());
    }

    /**
     * @param records
     * @description: 导出数据处理
     * @author Will
     * @date: 2024/4/16 18:42
     */
    private List<SoB2cDTO.ExcelExportDTO> handleExport(List<SoB2cDTO.ExcelExportDTO> records, String exportType, Boolean isOutStock) {
        //返回集合
        List<SoB2cDTO.ExcelExportDTO> resultList = new ArrayList<>();

        if (CollectionUtils.isEmpty(records)) {
            return resultList;
        }
        //skuId集合
        List<String> skuIdList = records.stream().map(SoB2cDTO.ExcelExportDTO::getSkuId).distinct().collect(Collectors.toList());
        Map<String, SkuVO> skuVOMap = new HashMap<>();
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIdList);
        if (!CollectionUtils.isEmpty(skuList)) {
            skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }
        //bom信息
        List<BomChildrenSkuDTO> bomChildrenList;
        //仓位信息
        List<String> warehouseIdList = records.stream().map(SoB2cDTO.ExcelExportDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = FeignQuery.create(WarehouseLocationEntity.class).in(WarehouseLocationEntity::getWarehouseId, warehouseIdList).select(WarehouseLocationEntity::getWarehouseId, WarehouseLocationEntity::getCode, WarehouseLocationEntity::getName).list();
        //库存信息
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = new ArrayList<>();
        //无需计算库存sku
        List<String> ignoreInventorySkuIds = new ArrayList<>();
        // 国家信息
        List<String> countryList = records.stream()
                .filter(e -> StringUtils.isNotBlank(e.getCountry()))
                .map(SoB2cDTO.ExcelExportDTO::getCountry)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(countryList)) {
            countryNameMap = sysDictFeign.listCountryByIds(countryList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }
        //根据SKU查询BOM判断是否是组合SKU
        bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        List<String> childSkuIdList = bomChildrenList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getSkuId()) && CharSequenceUtil.equals(BomTypeEnum.COMBINATION.getType(), obj.getType()))
                .map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(childSkuIdList)) {
            skuIdList.addAll(childSkuIdList);
        }

        //产品信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //虚拟仓库存
        List<String> virtualWarehouseIdList = records.stream().map(SoB2cDTO.ExcelExportDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        paramDTO.setDictInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
        paramDTO.setSkuIdList(skuIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryFeign.listInventoryQty(paramDTO);
        //实体仓库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO1 = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO1.setInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
        skuInventoryDTO1.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO1.setSkuIdList(skuIdList);
        inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO1);
        //虚拟仓库信息
        List<VirtualWarehouseEntity> virtualWarehouseList = CollectionUtils.isEmpty(virtualWarehouseIdList) ? new ArrayList<>() : FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //是否缺货
        if (isOutStock) {
            // 忽略库存计算SKU
            List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
            ignoreInventorySkuIds = CollUtil.isNotEmpty(ignoreInventorySkuList) ?
                    ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : com.google.common.collect.Lists.newArrayList();
        }
        //销售出库
        List<String> soIds = records.stream().map(SoB2cDTO.ExcelExportDTO::getId).distinct().collect(Collectors.toList());
        List<List<String>> partionSoIds = com.google.common.collect.Lists.partition(soIds, 5000);
        List<SoOutstockEntity> soOutstockEntityList = new ArrayList<>();
        partionSoIds.forEach(v -> {
            soOutstockEntityList.addAll(FeignQuery.create(SoOutstockEntity.class).in(SoOutstockEntity::getSoId, v).select(SoOutstockEntity::getSoId, SoOutstockEntity::getBillDate).list());
        });
        List<String> ids = records.stream().map(SoB2cDTO.ExcelExportDTO::getId).collect(Collectors.toList());
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);
        List<String> detailIds = records.stream().map(SoB2cDTO.ExcelExportDTO::getDetailId).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByIds(detailIds);
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryFeign.listBySourceId(ids);

        //B2C销售订单分类
        List<SoB2cRefCategoryDTO.CategoryNamesDTO> categoryNamesList = soB2cRefCategoryService.listCategoryNamesBySoIds(soIds);
        Map<String, String> categoryNamesMap = categoryNamesList.stream().collect(Collectors.toMap(SoB2cRefCategoryDTO.CategoryNamesDTO::getSoB2cId, SoB2cRefCategoryDTO.CategoryNamesDTO::getCategoryNames));

        //查询流程审核信息
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.TIK_TOK_FULLY.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
        }

        List<String> codeList = new ArrayList<>();
        List<String> codeAndParentSkuIdList = new ArrayList<>();
        for (SoB2cDTO.ExcelExportDTO exportDTO : records) {
            exportDTO.setPlatformOrderStatusName(FullyManagedPlatformStatusEnum.getErpNameByCode(exportDTO.getDictPlatform(),exportDTO.getPlatformOrderStatus()));
            //订单来源类型
            exportDTO.setOrderSourceTypeName(SoB2cExtendOrderSourceTypeEnum.getName(exportDTO.getOrderSourceType()));
            exportDTO.setInvalidTypeName(SoB2cInvalidTypeEnum.getName(exportDTO.getInvalidType()));
            //B2C销售订单分类
            exportDTO.setCategoryNames(categoryNamesMap.getOrDefault(exportDTO.getId(),""));
            SoOutstockEntity soOutstock = soOutstockEntityList.stream().filter(v -> v.getSoId().equals(exportDTO.getId())).findFirst().orElse(new SoOutstockEntity());
            exportDTO.setSoOutStockTime(soOutstock.getBillDate());
            //审核状态
            exportDTO.setApproveStatusName(ApproveStatusEnum.getName(exportDTO.getApproveStatus()));
            //订单状态
            exportDTO.setBillStatusName(SoB2cBillStatusEnum.getName(exportDTO.getBillStatus()));
            //含税成本
            exportDTO.setTaxCost(MathUtil.multiplyWithTwo(exportDTO.getTaxCost(), exportDTO.getQty()));
            //产品名称
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), exportDTO.getSkuId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                exportDTO.setProductName(productDetailEntity.getName());
                exportDTO.setVariantProperty(productDetailEntity.getVariantProperty());
            }
            String countryName = countryNameMap.getOrDefault(exportDTO.getCountry(), "");
            exportDTO.setCountryName(countryName);

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(exportDTO.getId()) && CharSequenceUtil.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                exportDTO.setApproveUserName(curApprove);
            }

            //仓位名称
            String warehouseLocationName = warehouseLocationEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCode(), exportDTO.getWarehouseLocation())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), exportDTO.getWarehouseId()))
                    .map(WarehouseLocationEntity::getName).findFirst().orElse("");
            exportDTO.setWarehouseLocationName(warehouseLocationName);


            //是否缺货
            if (isOutStock) {
                Integer useableQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(inventoryList)) {
                    //可用库存
                    useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(exportDTO.getSkuId())
                                    && obj.getWarehouseId().equals(exportDTO.getWarehouseId())
                                    && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                            .mapToInt(obj -> obj.getInventoryTotal()).sum();
                }
                exportDTO.setUseableQty(useableQty);
                //存在仓库则需要判断是否缺货
                if (CharSequenceUtil.isNotBlank(exportDTO.getWarehouseId())) {
                    //缺货订单
                    if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus())
                            || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus()))) {
                        //bean转换
                        SoB2cDetailDTO.ListDTO detailDTO = BeanMapperUtils.map(SoB2cDetailDTO.ListDTO.class, exportDTO);
                        Boolean outStock = isOutStock(bomChildrenList, inventoryList, detailDTO, ignoreInventorySkuIds);
                        exportDTO.setIsOutStock(outStock);
                    }
                }
            }

            //存在虚拟仓库则判断是否缺货
            if (CharSequenceUtil.isNotBlank(exportDTO.getVirtualWarehouseId())) {
                String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), exportDTO.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
                exportDTO.setVirtualWarehouseName(virtualWarehouseName);
                Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), exportDTO.getSkuId())
                                && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), exportDTO.getVirtualWarehouseId())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), exportDTO.getWarehouseId()))
                        .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                        .findFirst().orElse(MathUtil.ZERO);
                exportDTO.setVirtualUsableQty(virtualUsableQty);
            }

            //销售套装bom子级信息
            List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), exportDTO.getSkuId())
                            && CharSequenceUtil.equals(BomTypeEnum.COMBINATION.getType(), obj.getType()))
                    .collect(Collectors.toList());
            //发货单--提交发货时间、面单打印时间
            List<SoB2cDeliveryEntity> collect = soB2cDeliveryEntities.stream()
                    .filter(e -> Objects.nonNull(e) && Objects.equals(e.getSourceId(), exportDTO.getId()))
                    .filter(v -> !v.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode()))
                    .sorted(Comparator.comparing(SoB2cDeliveryEntity::getCreateTime).reversed())//降序
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                exportDTO.setFinishPrintTime(collect.get(0).getFinishPrintTime());
                exportDTO.setCreateDeliveryTime(collect.get(0).getCreateTime());
            }
            SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryEntities.stream().filter(e -> Objects.nonNull(e) && Objects.equals(e.getSourceId(), exportDTO.getId())).findFirst().orElse(null);
            //标签汇总
            exportDTO.setLabelOrderList(getLabelOrderList(exportDTO, soB2cRefList, childList, soB2cDeliveryEntity));
            SoB2cDetailEntity soB2cDetail = soB2cDetailEntityList.stream().filter(e -> Objects.equals(exportDTO.getDetailId(), e.getId())).findFirst().orElse(null);
            SoB2cDetailDTO.ListDTO detailDTO = BeanUtil.copyProperties(soB2cDetail, SoB2cDetailDTO.ListDTO.class);
            exportDTO.setLabelDetailList(getLabelDetailList(exportDTO, skuVOMap, bomChildrenList, inventoryList, ignoreInventorySkuIds, detailDTO, virtualInventoryList, virtualWarehouseList));
            exportDTO.setSkuQty(exportDTO.getQty());
            Integer useableQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(inventoryList)) {
                //可用库存
                useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                        .mapToInt(obj -> obj.getInventoryTotal()).sum();
            }
            exportDTO.setUseableQty(useableQty);
            //根据订单维度还是bom维度清除已存在的记录
            processRepeatData(exportDTO, codeList, codeAndParentSkuIdList);
            resultList.add(exportDTO);

        }
        return resultList;
    }

    /**
     * @param bomChildrenList
     * @param inventoryList
     * @param detailDTO
     * @return Boolean
     * @description: 判断是否缺货
     * @author Will
     * @date: 2024/4/22 14:46
     */
    private Boolean isOutStock(List<BomChildrenSkuDTO> bomChildrenList, List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList
            , SoB2cDetailDTO.ListDTO detailDTO, List<String> ignoreInventorySkuIds) {
        //判断是否是组合品
        Boolean isCombination = Boolean.FALSE;
        long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(e.getType())).count();
        if (count > 0) {
            isCombination = Boolean.TRUE;
        }

        Boolean isOutStock = Boolean.FALSE;
        //费销售套装bom判断父级SKU是否够使用
        if (!isCombination) {
            return detailDTO.getQty() > detailDTO.getUseableQty() && !ignoreInventorySkuIds.contains(detailDTO.getSkuId());
        }
        //销售套装bom需要判断子件库存是否够使用
        List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId())
                        && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            return Boolean.TRUE;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : childList) {
            //可用库存
            Integer childUseableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(childrenSkuDTO.getSkuId())
                            && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                            && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                    .mapToInt(obj -> obj.getInventoryTotal()).sum();
            if ((detailDTO.getQty() * childrenSkuDTO.getQuantity() > childUseableQty) && !ignoreInventorySkuIds.contains(childrenSkuDTO.getSkuId())) {
                isOutStock = Boolean.TRUE;
                break;
            }
        }
        return isOutStock;
    }

    /**
     * 整合销售订单标签
     *
     * @param data
     * @param soB2cRefList
     * @param childList
     * @param soB2cDeliveryEntity
     * @return
     */
    private String getLabelOrderList(SoB2cDTO.ExcelExportDTO data, List<SoB2cRefEntity> soB2cRefList, List<BomChildrenSkuDTO> childList, SoB2cDeliveryEntity soB2cDeliveryEntity) {
        StringBuilder labelOrderStr = new StringBuilder();
        //标签处理
        String label = data.getLabel();
        //主表标签
        Boolean isAddFrozenTag = Boolean.FALSE;
        if (StringUtils.isNotBlank(label)) {
            SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(label, SoB2cDTO.LabelJsonDTO.class);
            List<String> statusList = Arrays.asList("RISK_CONTROL", "IN_FROZEN", "Unfulfillable", "IN_CANCEL");
            if (Objects.equals(data.getBillStatus(), "frozen") && (statusList.contains(labelJsonDTO.getAliexpressStatus()) || statusList.contains(labelJsonDTO.getAmazonStatus()))) {
                labelOrderStr.append("冻结中,");
                isAddFrozenTag = Boolean.TRUE;
            }
            if (Objects.equals("AFN", labelJsonDTO.getFulfillmentChannel())) {
                labelOrderStr.append("FBA,");
            }
            List<String> shipNodeTypeList = Arrays.asList("WFSFulfilled", "3PLFulfilled");
            if (shipNodeTypeList.contains(labelJsonDTO.getShipNodeType())) {
                labelOrderStr.append("WFS,");
            }
            labelOrderStr.append(Objects.nonNull(labelJsonDTO.getIsRefunded()) && labelJsonDTO.getIsRefunded() ? "退款订单," : "");
        }
        String logisticsTypeName = OrderLogisticTypeEnum.getName(data.getLogisticType());
        labelOrderStr.append(CharSequenceUtil.isNotBlank(logisticsTypeName) ? logisticsTypeName + "," : "");
        labelOrderStr.append(CollectionUtils.isNotEmpty(childList) ? "组合产品," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsIntercept()) && data.getIsIntercept() ? "拦截订单," : "");
        labelOrderStr.append(Objects.equals(SourceTypeEnum.SELF_ADD.getCode(), data.getSourceType()) ? "手工订单," : "");
        if (CollectionUtils.isNotEmpty(soB2cRefList)) {
            //合并
            long mergeCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                    && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())
                    && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())).count();
            if (mergeCount > 0) {
                labelOrderStr.append("合并订单,");
            }
            //拆分
            long splitCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                    && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())
                    && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())).count();
            if (splitCount > 0) {
                labelOrderStr.append("拆分订单,");
            }
        }
        labelOrderStr.append(Objects.nonNull(soB2cDeliveryEntity) && Objects.equals("manual", soB2cDeliveryEntity.getShipmentMark()) ? "手动标发," : "");
        labelOrderStr.append(Objects.nonNull(data.getInvalidStatus()) && data.getInvalidStatus() ? "订单作废," : "");
//        labelOrderStr.append(Objects.nonNull(data.getIsCancel()) && data.getIsCancel() ? "订单取消," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsChangeReceiverAddress()) && data.getIsChangeReceiverAddress() ? "修改收货地址," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsChangeSku()) && data.getIsChangeSku() ? "更换发货SKU," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsNotOutbound()) && data.getIsNotOutbound() ? "不出库发货," : "");
        labelOrderStr.append(Objects.nonNull(data.getIsFrozen()) && data.getIsFrozen() && !isAddFrozenTag ? "冻结中," : "");
        String labelOrder = labelOrderStr.toString();
        //移除字符串最后一个字符
        return StrUtil.isNotBlank(labelOrder) ? labelOrder.substring(0, labelOrder.length() - 1) : "";
    }
    /**
     * 汇总明细标签
     *
     * @param exportDTO
     * @param skuVOMap
     * @param bomChildrenList
     * @param inventoryList
     * @param ignoreInventorySkuIds
     * @param detailDTO
     * @param virtualInventoryList
     * @param virtualWarehouseList
     * @return
     */
    private String getLabelDetailList(SoB2cDTO.ExcelExportDTO exportDTO, Map<String, SkuVO> skuVOMap, List<BomChildrenSkuDTO> bomChildrenList, List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList, List<String> ignoreInventorySkuIds, SoB2cDetailDTO.ListDTO detailDTO, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList, List<VirtualWarehouseEntity> virtualWarehouseList) {
        StringBuilder labelDetailStr = new StringBuilder();
        SkuVO skuVO = skuVOMap.get(exportDTO.getSkuId());
        SkuVO.PropertyDTO skuPropertyDTO = Objects.isNull(skuVO) ? new SkuVO.PropertyDTO() : Objects.isNull(skuVO.getPropertyDTO()) ? new SkuVO.PropertyDTO() : skuVO.getPropertyDTO();
        List<SoB2cDetailDTO.PropertyDTO> propertyDTOList = soB2cDetailService.handlePropertyDTOList(skuPropertyDTO);
        if (CollectionUtils.isNotEmpty(propertyDTOList)) {
            String propertyStr = propertyDTOList.stream().map(SoB2cDetailDTO.PropertyDTO::getName).distinct().collect(Collectors.joining(","));
            labelDetailStr.append(propertyStr).append(",");
        }
        //标签处理
        SoB2cDetailDTO.DetailLabelDTO detailLabelDTO = new SoB2cDetailDTO.DetailLabelDTO();
        String detailLabel = exportDTO.getLabelJson();
        if (StringUtils.isNotBlank(detailLabel)) {
            SoB2cDetailDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(detailLabel, SoB2cDetailDTO.LabelJsonDTO.class);
            labelDetailStr.append(Objects.equals("U_TAXED", labelJsonDTO.getAlreadyTaxed()) || Objects.equals("I_TAXED", labelJsonDTO.getAlreadyTaxed()) ? "速卖通已税," : "");
            labelDetailStr.append(Objects.equals("cainiaoInternationalWarehouse", labelJsonDTO.getLogisticsWarehouseType()) ? "菜鸟官方仓," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("AE_PLUS_RU") ? "AE_PLUS," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("HBA_UP_EXPRESS") ? "AE_合单," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("leadTimeTag#10") ? "十日达," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("leadTimeTag#12") ? "12日达," : "");
            labelDetailStr.append(CollectionUtils.isNotEmpty(labelJsonDTO.getTagList()) && labelJsonDTO.getTagList().contains("leadTimeTag#15") ? "15日达," : "");
            labelDetailStr.append(Objects.nonNull(labelJsonDTO.getIsRefunded()) && labelJsonDTO.getIsRefunded() ? "退款订单," : "");
        }
        //存在仓库则需要判断是否缺货
        if (StrUtil.isNotBlank(exportDTO.getWarehouseId())) {
            Integer useableQty = MathUtil.ZERO;
            Integer freezeQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(inventoryList)) {
                //可用库存
                useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                        .mapToInt(obj -> obj.getInventoryTotal()).sum();
                //冻结库存
                freezeQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                && InventoryStatusEnum.FROZEN.getCode().equals(obj.getInventoryStatus()))
                        .mapToInt(obj -> obj.getInventoryTotal()).sum();
            }
            detailDTO.setUseableQty(useableQty);
            detailDTO.setFreezeQty(freezeQty);
            //缺货订单
            if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus())
                    || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus()))) {
                //实体仓缺货
                Boolean isOutStock = isOutStock(bomChildrenList, inventoryList, detailDTO, ignoreInventorySkuIds);
                detailLabelDTO.setIsOutStock(isOutStock);
            }
        }
        //存在虚拟仓库则判断是否缺货
        if (StrUtil.isNotBlank(detailDTO.getVirtualWarehouseId())) {
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailDTO.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            detailDTO.setVirtualWarehouseName(virtualWarehouseName);
            //虚拟仓缺货处理
            isVirtualOutStock(bomChildrenList, virtualInventoryList, detailLabelDTO, detailDTO);
            //缺货订单
            if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus())
                    || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(exportDTO.getBillStatus()))) {
                detailLabelDTO.setIsOutStock(Boolean.FALSE);
            }
        }
        labelDetailStr.append(Objects.nonNull(detailLabelDTO.getIsOutStock()) && detailLabelDTO.getIsOutStock() ? "缺货订单," : "");
        labelDetailStr.append(Objects.nonNull(detailLabelDTO.getIsVirtualOutStock()) && detailLabelDTO.getIsVirtualOutStock() ? "缺货订单(X缺)," : "");
        String labelDetail = labelDetailStr.toString();
        //移除字符串最后一个字符
        return StrUtil.isNotBlank(labelDetail) ? labelDetail.substring(0, labelDetail.length() - 1) : "";
    }

    private void processRepeatData(SoB2cDTO.ExcelExportDTO resultDTO, List<String> codeList, List<String> codeAndParentSkuIdList) {
        String code = resultDTO.getCode();
        if (CharSequenceUtil.isNotBlank(code) && codeList.contains(code)) {
            //清除订单维度数据
            resultDTO.setShippingCost(BigDecimal.ZERO);
            resultDTO.setAmount(BigDecimal.ZERO);
            resultDTO.setEstimatedShippingCost(BigDecimal.ZERO);
            resultDTO.setActualShippingCost(BigDecimal.ZERO);
            resultDTO.setLength(BigDecimal.ZERO);
            resultDTO.setWidth(BigDecimal.ZERO);
            resultDTO.setHeight(BigDecimal.ZERO);
            resultDTO.setWeight(BigDecimal.ZERO);
        }else if (CharSequenceUtil.isNotBlank(code)){
            codeList.add(code);
        }
        String parentSkuId = resultDTO.getParentSkuId();
        if (CharSequenceUtil.isNotBlank(code) && CharSequenceUtil.isNotBlank(parentSkuId) && codeAndParentSkuIdList.contains(code + "-" + parentSkuId)) {
            //清除bom拆分数据
            resultDTO.setTaxCost(BigDecimal.ZERO);
            resultDTO.setSourceAmount(BigDecimal.ZERO);
            resultDTO.setBaseAmount(BigDecimal.ZERO);
            resultDTO.setQty(MathUtil.ZERO);
        }else if (CharSequenceUtil.isNotBlank(code) && CharSequenceUtil.isNotBlank(parentSkuId)){
            codeAndParentSkuIdList.add(code + "-" + parentSkuId);
        }
    }
    /**
     * 判断虚拟仓是否缺货
     *
     * @param bomChildrenList
     * @param virtualInventoryList
     * @param detailDTO
     * @return Boolean
     * @author will
     * @date 2024/7/22 10:42
     */
    private void isVirtualOutStock(List<BomChildrenSkuDTO> bomChildrenList, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList
            , SoB2cDetailDTO.DetailLabelDTO detailLabelDTO, SoB2cDetailDTO.ListDTO detailDTO) {
        //返回信息
        List<SoB2cDTO.VirtualChildScarceDTO> childScarceList = new ArrayList<>();
        //判断是否是组合品
        Boolean isCombination = Boolean.FALSE;
        long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(e.getType())).count();
        if (count > 0) {
            isCombination = Boolean.TRUE;
        }

        Boolean isVirtualScarce = Boolean.FALSE;
        //费销售套装bom判断父级SKU是否够使用
        if (!isCombination) {
            //虚拟仓是否缺货
            Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), detailDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), detailDTO.getVirtualWarehouseId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), detailDTO.getWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .findFirst().orElse(MathUtil.ZERO);
            detailDTO.setVirtualUsableQty(virtualUsableQty);
            detailLabelDTO.setIsVirtualOutStock(detailDTO.getQty() > virtualUsableQty);
            detailDTO.setChildScarceList(childScarceList);
            return;
        }
        //销售套装bom需要判断子件库存是否够使用
        List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId())
                        && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            detailLabelDTO.setIsVirtualOutStock(Boolean.TRUE);
            detailDTO.setChildScarceList(childScarceList);
            return;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : childList) {
            SoB2cDTO.VirtualChildScarceDTO scarceDTO = new SoB2cDTO.VirtualChildScarceDTO();
            //虚拟仓是否缺货
            Integer childVirtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), childrenSkuDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), detailDTO.getVirtualWarehouseId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), detailDTO.getWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .findFirst().orElse(MathUtil.ZERO);
            if (!isVirtualScarce && (detailDTO.getQty() * childrenSkuDTO.getQuantity() > childVirtualUsableQty)) {
                isVirtualScarce = Boolean.TRUE;
            }
            scarceDTO.setChildUsableQty(childVirtualUsableQty);

            //针对父级可用数量
            double floor = Math.floor((double) childVirtualUsableQty / childrenSkuDTO.getQuantity());
            Integer parentUsableQty = (int) floor;
            scarceDTO.setParentUsableQty(parentUsableQty);

            Integer virtualScarceQty = detailDTO.getQty() * childrenSkuDTO.getQuantity() - childVirtualUsableQty;
            scarceDTO.setVirtualScarceQty(MathUtil.compareTo(virtualScarceQty, MathUtil.ZERO) >= MathUtil.ZERO ? virtualScarceQty : MathUtil.ZERO);
            scarceDTO.setSkuId(childrenSkuDTO.getSkuId());
            scarceDTO.setSkuNo(childrenSkuDTO.getSkuNo());
            scarceDTO.setQuantity(childrenSkuDTO.getQuantity());
            scarceDTO.setBomVersion(childrenSkuDTO.getBomVersion());
            childScarceList.add(scarceDTO);
        }
        detailLabelDTO.setIsVirtualOutStock(isVirtualScarce);
        detailDTO.setChildScarceList(childScarceList);
        if (CollectionUtils.isNotEmpty(childScarceList)) {
            //bom最小可用数
            Integer bomUsableQty = childScarceList.stream().min(Comparator.comparing(SoB2cDTO.VirtualChildScarceDTO::getParentUsableQty)).map(SoB2cDTO.VirtualChildScarceDTO::getParentUsableQty).get();
            detailDTO.setVirtualUsableQty(bomUsableQty);
        }
    }
}
