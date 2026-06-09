package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.excel.ImportHistoryRecordExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.tms.util.CfgLogisticsCostImportEtlRuleHelper;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.ImportHistoryRecordExcelListener;
import com.erp.server.tms.mapper.ImportHistoryRecordMapper;
import com.erp.server.tms.service.*;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_IMPORT_HISTORY_RECORD;
import static com.erp.server.tms.listener.ImportHistoryRecordExcelListener.*;

/**
 * <p>
 * 导入历史记录表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-01-19
 */
@Slf4j
@Service
public class ImportHistoryRecordServiceImpl extends SuperServiceImpl<ImportHistoryRecordMapper, ImportHistoryRecordEntity> implements ImportHistoryRecordService {

    private static final String COST_ITEM_FIELD = "costItem";
    private static final String ACTUAL_AMOUNT_FIELD = "actualAmount";
    private static final String ESTIMATED_AMOUNT_FIELD = "estimatedAmount";
    private static final String PLATFORM_CODE_FIELD = "platformCode";

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;
    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Lazy
    @Resource
    private ImportHistoryRecordService importHistoryRecordService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    @Qualifier("importHistoryRecordPool")
    private ExecutorService importHistoryRecordPool;

    // 这里只是写一条 import_history_record 的本地状态，没有跨服务/跨库写入，
    // 不需要分布式事务。原先挂 @GlobalTransactional 会被异步任务框架透传的上游 Seata XID 绑定，
    // 一旦上游某个批次（如 batchImportUpdate）触发 PG 40P01 死锁被标记为 rollback-only，
    // 这条状态更新也会跟着回滚，导致"导入中心已完成 / 导入记录仍处理中"的撕裂状态。
    // 改成本地事务 + REQUIRES_NEW，保证最终状态独立提交。
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(ImportHistoryRecordDTO.AddOrUpdateDTO addOrUpdateDTO) {
        ImportHistoryRecordEntity importHistoryRecordEntity = new ImportHistoryRecordEntity();
        BeanMapperUtils.copy(addOrUpdateDTO, importHistoryRecordEntity);

        // 数据处理
        handleData(importHistoryRecordEntity);

        log.info("开始新增或更新物流授权单");
        //新增则需要生成单号
        if ( CharSequenceUtil.isBlank(importHistoryRecordEntity.getId())) {
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
            importHistoryRecordEntity.setCode(code);
        }
        boolean save = super.saveOrUpdate(importHistoryRecordEntity);
        if(!save) {
            throw new ServiceException("物流授权单保存失败");
        }

        return new BaseResultDTO.AddDTO(importHistoryRecordEntity.getId(), importHistoryRecordEntity.getCode());
    }



    @Override
    public PagingVO<ImportHistoryRecordDTO.ListDTO> paging(PagingDTO<ImportHistoryRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ImportHistoryRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ImportHistoryRecordEntity entity) {

        //根据文件URL查询是否已存在记录，存在则更新，不存在则新增
        ImportHistoryRecordEntity old = this.getByFileUrl(entity.getFileUrl(),entity.getSheetName());
        if (ObjectUtil.isNotEmpty(old)) {
            entity.setId(old.getId());
        }
    }


    @Override
    public ImportHistoryRecordDTO.ViewDTO view(String id) {
        ImportHistoryRecordEntity importHistoryRecordEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到物流授权单数据"));
        ImportHistoryRecordDTO.ViewDTO data = BeanMapperUtils.map(ImportHistoryRecordDTO.ViewDTO.class, importHistoryRecordEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    @Override
    public BatchResultDTO importFile(ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO) {
        if (CharSequenceUtil.isBlank(importSyncDTO.getFileName())) {
            return BatchResultDTO.fail(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),ApiError.LOGISTICS_IMPORT_FILE_NAME_NOT_FOUND.getMsg());
        }
        //查询配置主表信息
        //统一使用"尾程发货"的配置，不在依赖importSyncDTO.getBusinessType()来获取配置
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(importSyncDTO.getFileName(), DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode(), importSyncDTO.getCostType());
        if (CollUtil.isEmpty(cfgLogisticsCostImportList)) {
            return BatchResultDTO.fail(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),"无法识别导入模板，请检查配置是否正确");
        }
        //查询配置明细信息
        List<String> mainIdList = cfgLogisticsCostImportList.stream().map(CfgLogisticsCostImportEntity::getId).distinct().collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> importDetailList =  cfgLogisticsCostImportDetailService.listByMainIdList(mainIdList);
        if (CollUtil.isEmpty(importDetailList)) {
            return BatchResultDTO.fail(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND.getMsg());
        }
        importSyncDTO.setCfgLogisticsCostImportList(cfgLogisticsCostImportList);
        importSyncDTO.setImportDetailList(importDetailList);
        importSyncDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        String taskId = downloadTaskFeign.saveImportTask(IMPORT_TMS_IMPORT_HISTORY_RECORD.getName(), IMPORT_TMS_IMPORT_HISTORY_RECORD.getCode(), importSyncDTO);
        return  BatchResultDTO.success(taskId,importSyncDTO.getFileName(),"导入成功");
    }


    @Override
    public BatchResultDTO preprocessingImportExcel(ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO) {
        Map<String,List<CfgLogisticsCostImportDetailEntity>> impotyDetailMap = importSyncDTO.getImportDetailList().stream().collect(Collectors.groupingBy(CfgLogisticsCostImportDetailEntity::getMainId));

        //下载文件
        byte[] bytes = fileFeign.downloadFile(importSyncDTO.getFileUrl());

        //获取批次号，同一个文件同一次导入用同一个批次号
        String batchNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        importSyncDTO.setCode(batchNo);

        //标记是否存在匹配的sheet页
        Boolean isExistSheet = Boolean.FALSE;

        for (CfgLogisticsCostImportEntity costImportEntity : importSyncDTO.getCfgLogisticsCostImportList()) {
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList = impotyDetailMap.get(costImportEntity.getId());
            if (CollUtil.isEmpty(cfgImportDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
            }
            //查询配置的唯一识别号
            List<CfgLogisticsCostImportDetailEntity> cfgDetailList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cfgDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND,importSyncDTO.getFileName());
            }

            ImportHistoryRecordExcelListener excelListenerUtil = new ImportHistoryRecordExcelListener(costImportEntity,cfgImportDetailList,importSyncDTO);
            try {
                EasyExcel.read(new ByteArrayInputStream(bytes), excelListenerUtil)
                        .headRowNumber(costImportEntity.getHeaderRow())
                        .sheet(costImportEntity.getSheetName()).doRead();
            } catch (ExcelCommonException e) {
                log.error("导入格式错误！", e);
                throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
            }


            //更新导入结果
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(importSyncDTO.getTaskId());
            importResultDTO.setCount(excelListenerUtil.getCount());
            //导出错误数据
            Map<Integer, String> headMap = excelListenerUtil.getHeadMap();
            //未找到表头直接跳过
            if (ObjectUtil.isEmpty(headMap)) {
                continue;
            }
            isExistSheet = Boolean.TRUE;

            //匹配结果序号
            Integer matchErrorCount = excelListenerUtil.getMatchFailCount();
            String url = CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importSyncDTO.getProcessingType())
                    ? "" : excelListenerUtil.getMatchResultUrl();
            importResultDTO.setErrorUrl(url);
            importResultDTO.setFinishTime(LocalDateTime.now());
            importResultDTO.setRemark("处理完成，失败" + matchErrorCount + "条");
            importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            downloadTaskFeign.updateTask(importResultDTO);
        }
        if (!isExistSheet) {
            throw new ServiceException(ApiError.FILE_SHEET_NOT_EXIST);
        }
        return  BatchResultDTO.success(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),"导入成功");
    }

    @Override
    public List<ImportHistoryRecordDTO.ImportConfirmDTO> handleImportSuccessList(ImportHistoryRecordDTO.ImportSyncDTO importDTO, CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                                                 List<JSONObject> successList, List<JSONObject> matchImportList, List<String> headList, Map<Integer, String> headMap) {
        // 计时器-开始
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("处理条数={}，开始处理时间 ={}",successList.size(),stopwatch.elapsed(TimeUnit.MILLISECONDS));

        //数据处理
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = buildImportDataList(importDTO, costImportEntity, cfgImportDetailList,
                successList, matchImportList, headList, headMap);

        //处理数据结束时间
        log.warn("结束处理时间 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));


        if (CollUtil.isEmpty(importDataList)) {
            return Collections.emptyList();
        }
        //预处理直接跳过处理
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
            return Collections.emptyList();
        }
        List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmDTOList = importHistoryRecordService.importBatchAddOrUpdate(importDataList,importDTO.getProcessingType());

        //数据落库结束时间
        log.warn("保存处理时间 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return confirmDTOList;
    }

    /**
     * 导入数据处理（不落库）
     */
    private List<LogisticsBillCostDTO.ImportDataDTO> buildImportDataList(
            ImportHistoryRecordDTO.ImportSyncDTO importDTO,
            CfgLogisticsCostImportEntity costImportEntity,
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
            List<JSONObject> successList,
            List<JSONObject> matchImportList,
            List<String> headList,
            Map<Integer, String> headMap) {
        // 1. 校验表头唯一性和数据非空
        validateHeadersAndData(headList, successList);
        prepareCleanFileHeaders(cfgImportDetailList, headList, headMap);
        // 2. 提取唯一键配置
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = extractUniqueKeyList(cfgImportDetailList);
        prepareImportRowValues(cfgImportDetailList, headMap, successList);
        standardizeImportRowWeightValues(cfgImportDetailList, successList);
        // 3. 构建 paramMap，收集唯一键所有唯一值
        Map<String, List<Object>> paramMap = buildParamMap(cfgImportDetailList, headMap, successList);
        // 4. 执行所有数据库预查询
        ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult = preQueryDbData(paramMap);
        // 5. 币别字典映射（仅未禁用，支持按 id 或 name 匹配）
        List<DictCurrencyEntity> dictCurrencyList = sysUserFeign.currencyList();
        Map<String, String> currencyLookupMap = buildCurrencyLookupMap(dictCurrencyList);
        //6. 币别汇率
        Map<String, BigDecimal> currencyRateMap = buildCurrencyRateMap(dictCurrencyList);
        // 5. 判断纵向/横向模式
        boolean isVertical = isVerticalCostItem(cfgImportDetailList);
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList;
        if (isVertical) {
            importDataList = processVerticalCostItems(uniqueKeyList, cfgImportDetailList, preQueryResult, importDTO, costImportEntity, successList, matchImportList, headList, headMap, currencyLookupMap,currencyRateMap);
        } else {
            importDataList = processHorizontalCostItems(uniqueKeyList, cfgImportDetailList, preQueryResult, importDTO, costImportEntity, successList, matchImportList, headList, headMap, currencyLookupMap,currencyRateMap);
        }
        projectCleanFileRows(cfgImportDetailList, matchImportList);
        return importDataList;
    }

    /**
     * 币别汇率
     * @author will
     * @date 2026/4/29 16:01
     * @param dictCurrencyList
     * @return java.util.Map<java.lang.String,java.math.BigDecimal>
     */
    private Map<String, BigDecimal> buildCurrencyRateMap(List<DictCurrencyEntity> dictCurrencyList) {
        if (CollUtil.isEmpty(dictCurrencyList)) {
            return Collections.emptyMap();
        }
        //批量查询汇率
        Map<String, BigDecimal> exchangeRateMap = new HashMap<>();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        for (DictCurrencyEntity currencyEntity : dictCurrencyList) {
            if (CurrencyEnum.CNY.getCurrencyCode().equals(currencyEntity.getId())) {
                exchangeRateMap.put(currencyEntity.getId(), BigDecimal.ONE);
            } else {
                BigDecimal rate = dmpTaskFeign.getRate(currentDate, currencyEntity.getId());
                exchangeRateMap.put(currencyEntity.getId(), rate);
            }
        }
        return exchangeRateMap;
    }


    /**
     * 获取币别字典Map，id或name都可以
     * @author will
     * @date 2026/4/16 12:02
     * @param dictCurrencyEntities
     * @return java.util.Map<java.lang.String,java.lang.String>
     */
    private Map<String, String> buildCurrencyLookupMap(List<DictCurrencyEntity> dictCurrencyEntities) {
        if (CollUtil.isEmpty(dictCurrencyEntities)) {
            return Collections.emptyMap();
        }
        Map<String, String> lookupMap = new HashMap<>();
        for (DictCurrencyEntity entity : dictCurrencyEntities) {
            if (ObjectUtil.isNull(entity) || Boolean.TRUE.equals(entity.getDisabled())) {
                continue;
            }
            String id = CharSequenceUtil.trim(entity.getId());
            String name = CharSequenceUtil.trim(entity.getName());
            String standardCurrency = CharSequenceUtil.isNotBlank(id) ? id : name;
            if (CharSequenceUtil.isBlank(standardCurrency)) {
                continue;
            }
            if (CharSequenceUtil.isNotBlank(id)) {
                lookupMap.putIfAbsent(normalizeCurrencyKey(id), standardCurrency);
            }
            if (CharSequenceUtil.isNotBlank(name)) {
                lookupMap.putIfAbsent(normalizeCurrencyKey(name), standardCurrency);
            }
        }
        return lookupMap;
    }

    /**
     * 币别取值
     * @author will
     * @date 2026/4/16 12:02
     * @param currency
     * @param currencyLookupMap
     * @return java.lang.String
     */
    private String normalizeCurrencyByDict(String currency, Map<String, String> currencyLookupMap) {
        if (CharSequenceUtil.isBlank(currency) || CollUtil.isEmpty(currencyLookupMap)) {
            return null;
        }
        return currencyLookupMap.get(normalizeCurrencyKey(currency));
    }

    /**
     * excel币别格式化
     * @author will
     * @date 2026/4/16 12:01
     * @param value
     * @return java.lang.String
     */
    private String normalizeCurrencyKey(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    // 校验表头唯一性和数据非空
    private void validateHeadersAndData(List<String> headList, List<JSONObject> successList) {
        // 先去掉空表头，再校验是否存在重复表头
        List<String> nonEmptyHeadList = headList.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        if (nonEmptyHeadList.size() != nonEmptyHeadList.stream().distinct().count()) {
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_HEAD_EXIST);
        }
        if (CollectionUtils.isEmpty(successList)) {
            throw new ServiceException("导入数据为空");
        }
    }

    // 提取唯一键配置
    private List<CfgLogisticsCostImportDetailEntity> extractUniqueKeyList(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList) {
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgImportDetailList.stream()
                .filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(uniqueKeyList)) {
            log.warn("未配置唯一键字段，无法进行数据处理");
            throw new ServiceException("未配置唯一键字段，无法进行数据处理");
        }
        List<String> invalidUniqueFields = uniqueKeyList.stream()
                .filter(detail -> CharSequenceUtil.isBlank(detail.getSourceField()))
                .map(detail -> CharSequenceUtil.blankToDefault(detail.getTargetFieldName(), detail.getTargetField()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(invalidUniqueFields)) {
            throw new ServiceException("唯一识别字段必须配置物流商抬头字段：" + String.join("、", invalidUniqueFields));
        }
        return uniqueKeyList;
    }

    // 构建 paramMap，收集唯一键所有唯一值
    private Map<String, List<Object>> buildParamMap(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, Map<Integer, String> headMap, List<JSONObject> successList) {
        Map<String, List<Object>> paramMap = new HashMap<>();
        for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgImportDetailList) {
            if (!cfgDetail.getIsUniqueKey()) continue;
            List<Object> dataList = successList.stream()
                    .map(obj -> getPreparedValue(obj, cfgDetail))
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(dataList)) {
                paramMap.put(cfgDetail.getTargetField(), dataList);
            }
        }
        return paramMap;
    }

    // 执行所有数据库预查询
    private ImportHistoryRecordDTO.PreQueryResultDTO preQueryDbData(Map<String, List<Object>> paramMap) {
        // 审查说明：费用项统一查尾程发货；主单 type 在 handleImportData 按匹配到的费用单 entity 解析。
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillByUniqueKey(paramMap);
        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if (CollUtil.isNotEmpty(logisticsBillVos)) {
            List<String> logisticsBillCostIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getLogisticsBillCostId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
            List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostIdList);
            mainIdListMap = CollUtil.isEmpty(listByMainIdList) ? new HashMap<>() : listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        return new ImportHistoryRecordDTO.PreQueryResultDTO(logisticsBillVos, mainIdListMap, logisticsBillCostList, cfgCostList);
    }

    // 判断是否为纵向费用项
    private boolean isVerticalCostItem(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList) {
        return cfgImportDetailList.stream().anyMatch(obj -> CharSequenceUtil.equals(obj.getTargetField(), "costItem") && CharSequenceUtil.isNotBlank(obj.getSourceDetailField()));
    }

    private String buildImportUniqueGroupKey(JSONObject row, List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        return uniqueKeyList.stream()
                .map(detail -> String.valueOf(getPreparedValue(row, detail)))
                .collect(Collectors.joining("_"));
    }

    /**
     * 构建模板导入分组键。
     * <p>普通识别字段按 Excel 识别单号分组；platformCode 允许一个物流单保存多个平台单号，
     * 因此按匹配到的物流单集合分组，避免多个平台单号分多组后覆盖同一物流费用单。</p>
     */
    private String buildImportRowGroupKey(JSONObject row,
                                          List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                          ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult,
                                          CfgLogisticsCostImportEntity costImportEntity) {
        String uniqueGroupKey = buildImportUniqueGroupKey(row, uniqueKeyList);
        if (!containsPlatformCodeUniqueKey(uniqueKeyList)) {
            return uniqueGroupKey;
        }
        List<LogisticsBillDTO.LogisticsBillVo> matchedBillList = resolveMatchedLogisticsBillVoList(uniqueKeyList, row,
                preQueryResult.getLogisticsBillVoList(), costImportEntity);
        if (CollUtil.isEmpty(matchedBillList)) {
            return uniqueGroupKey;
        }
        return "billSet:" + buildMatchedBillSetKey(matchedBillList);
    }

    /**
     * 判断识别单号是否包含 platformCode。
     * <p>platformCode 与其他字段不同，数据库侧可能以逗号存储多个平台订单号，需要单独处理多对一分组。</p>
     */
    private boolean containsPlatformCodeUniqueKey(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList) {
        return uniqueKeyList.stream().anyMatch(uniqueKey -> CharSequenceUtil.equals(PLATFORM_CODE_FIELD, uniqueKey.getTargetField()));
    }

    /**
     * 按识别单号和模板适用范围解析当前行可写入的物流单。
     * <p>该方法集中复用唯一键匹配和模板范围过滤，保证分组阶段与落库阶段使用同一套匹配规则。</p>
     */
    private List<LogisticsBillDTO.LogisticsBillVo> resolveMatchedLogisticsBillVoList(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                                                                     JSONObject successJson,
                                                                                     List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos,
                                                                                     CfgLogisticsCostImportEntity costImportEntity) {
        if (CollUtil.isEmpty(logisticsBillVos)) {
            return Collections.emptyList();
        }
        return distinctLogisticsBillVoList(logisticsBillVos.stream()
                .filter(obj -> matchesUniqueKey(uniqueKeyList, successJson, obj))
                .filter(obj -> matchesCostImportConfig(costImportEntity, obj))
                .collect(Collectors.toList()));
    }

    /**
     * 解析同一导入分组命中的物流单集合。
     * <p>同一组内费用会被合计后统一处理，因此每行命中的物流单集合必须一致；不一致时直接报错，避免错误合并费用。</p>
     */
    private List<LogisticsBillDTO.LogisticsBillVo> resolveGroupMatchedLogisticsBillVoList(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                                                                          List<JSONObject> rowList,
                                                                                          ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult,
                                                                                          CfgLogisticsCostImportEntity costImportEntity,
                                                                                          List<String> errorMsgList) {
        if (CollUtil.isEmpty(rowList)) {
            return Collections.emptyList();
        }
        List<LogisticsBillDTO.LogisticsBillVo> firstMatchedList = resolveMatchedLogisticsBillVoList(uniqueKeyList, rowList.get(0),
                preQueryResult.getLogisticsBillVoList(), costImportEntity);
        String firstBillSetKey = buildMatchedBillSetKey(firstMatchedList);
        for (int i = 1; i < rowList.size(); i++) {
            List<LogisticsBillDTO.LogisticsBillVo> currentMatchedList = resolveMatchedLogisticsBillVoList(uniqueKeyList, rowList.get(i),
                    preQueryResult.getLogisticsBillVoList(), costImportEntity);
            if (!CharSequenceUtil.equals(firstBillSetKey, buildMatchedBillSetKey(currentMatchedList))) {
                errorMsgList.add("同一识别单号分组命中的物流单不一致，无法合并分摊费用");
                return Collections.emptyList();
            }
        }
        return firstMatchedList;
    }

    /**
     * 构建物流单集合比较键。
     * <p>同一个识别分组命中的物流单顺序不影响业务含义，排序后比较可避免预查询返回顺序导致误判。</p>
     */
    private String buildMatchedBillSetKey(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList) {
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            return "";
        }
        return logisticsBillVoList.stream()
                .map(logisticsBillVo -> CharSequenceUtil.blankToDefault(logisticsBillVo.getDetailId(), logisticsBillVo.getId()))
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * 按物流单明细去重，避免预查询 join 出重复明细时重复分摊费用。
     */
    private List<LogisticsBillDTO.LogisticsBillVo> distinctLogisticsBillVoList(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList) {
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            return Collections.emptyList();
        }
        LinkedHashMap<String, LogisticsBillDTO.LogisticsBillVo> billMap = new LinkedHashMap<>();
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            String key = CharSequenceUtil.blankToDefault(logisticsBillVo.getDetailId(), logisticsBillVo.getId());
            if (CharSequenceUtil.isNotBlank(key)) {
                billMap.putIfAbsent(key, logisticsBillVo);
            }
        }
        return new ArrayList<>(billMap.values());
    }

    // 纵向费用项处理（多线程）
    private List<LogisticsBillCostDTO.ImportDataDTO> processVerticalCostItems(
            List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
            ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult,
            ImportHistoryRecordDTO.ImportSyncDTO importDTO,
            CfgLogisticsCostImportEntity costImportEntity,
            List<JSONObject> successList,
            List<JSONObject> matchImportList,
            List<String> headList,
            Map<Integer, String> headMap,
            Map<String, String> currencyLookupMap,Map<String, BigDecimal> currencyRateMap) {
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = Collections.synchronizedList(new ArrayList<>());
        // 分组阶段先确定哪些 Excel 行应合并：普通识别号按识别值，platformCode 按命中的物流单集合，避免多平台单号覆盖同一费用单。
        Map<String, List<JSONObject>> map = successList.stream()
                .collect(Collectors.groupingBy(obj -> buildImportRowGroupKey(obj, uniqueKeyList, preQueryResult, costImportEntity), LinkedHashMap::new, Collectors.toList()));
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        List<java.util.concurrent.Future<Void>> futures = new ArrayList<>();
        int batchSize = 100;
        List<Map.Entry<String, List<JSONObject>>> entryList = new ArrayList<>(map.entrySet());
        for (int i = 0; i < entryList.size(); i += batchSize) {
            int end = Math.min(entryList.size(), i + batchSize);
            List<Map.Entry<String, List<JSONObject>>> batch = entryList.subList(i, end);
            futures.add(importHistoryRecordPool.submit(() -> {
                for (Map.Entry<String, List<JSONObject>> entry : batch) {
                    List<JSONObject> value = entry.getValue();
                    JSONObject successJson = new JSONObject();
                    List<TmsCostDetailDTO.UpdateDTO> updateAllList = new ArrayList<>();
                    HashMap<String, String> currencyMap = new HashMap<>();
                    // 逐条处理每个jsonObject，分别校验和赋值
                    for (JSONObject jsonObject : value) {
                        jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                        List<String> costErrorMsgList = new ArrayList<>();
                        List<TmsCostDetailDTO.UpdateDTO> updateList = rowFormatCost(successJson, jsonObject, preQueryResult.getCfgCostList(), cfgImportDetailList, headList, DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode(), costErrorMsgList, currencyMap, currencyLookupMap, currencyRateMap);
                        if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                            jsonObject.set(matchIndex.toString(), MATCH_FAIL);
                            jsonObject.set(errorIndex.toString(), FieldValidUtil.getMsgSort(costErrorMsgList));
                            synchronized (matchImportList) { updateMatchResult(Collections.singletonList(jsonObject), matchIndex.toString(), errorIndex.toString(), costErrorMsgList, matchImportList); } ;
                            continue;
                        }
                        updateAllList.addAll(updateList);
                    }
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList);
                    List<JSONObject> costSuccessList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.get(matchIndex.toString()))).collect(Collectors.toList());
                    if (CollUtil.isEmpty(costSuccessList)) {
                        continue;
                    }
                    List<String> mainErrorMsgList = new ArrayList<>();
                    try {
                        // 同一识别分组会先汇总费用，再把固定的物流单集合传入 handleImportData，由既有重量分摊逻辑处理一对多/多对多。
                        List<LogisticsBillDTO.LogisticsBillVo> matchedLogisticsBillVoList = resolveGroupMatchedLogisticsBillVoList(uniqueKeyList,
                                costSuccessList, preQueryResult, costImportEntity, mainErrorMsgList);
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getLogisticsBillVoList(), preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList,
                                preQueryResult.getMainIdListMap(), matchedLogisticsBillVoList);
                        if (importDataDTO != null) {
                            importDataList.add(importDataDTO);
                        }
                    } catch (Exception e) {
                        log.error("数据处理失败", e);
                        mainErrorMsgList.add(ObjectUtil.defaultIfNull(e.getMessage(), e.toString()));
                    }
                    synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); } ;
                }
                return null;
            }));
        }
        List<Throwable> errors = new ArrayList<>();
        for (java.util.concurrent.Future<Void> f : futures) {
            try { f.get(); } catch (Exception ex) { log.error("horizontal future get error", ex); errors.add(ex); }
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException("多线程处理数据失败", errors.get(0));
        }
        return importDataList;
    }

    // 横向费用项处理（多线程）
    private List<LogisticsBillCostDTO.ImportDataDTO> processHorizontalCostItems(
            List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
            ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult,
            ImportHistoryRecordDTO.ImportSyncDTO importDTO,
            CfgLogisticsCostImportEntity costImportEntity,
            List<JSONObject> successList,
            List<JSONObject> matchImportList,
            List<String> headList,
            Map<Integer, String> headMap,
            Map<String, String> currencyLookupMap,Map<String, BigDecimal> currencyRateMap) {
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = Collections.synchronizedList(new ArrayList<>());
        // 分组阶段先确定哪些 Excel 行应合并：普通识别号按识别值，platformCode 按命中的物流单集合，避免多平台单号覆盖同一费用单。
        Map<String, List<JSONObject>> map = successList.stream()
                .collect(Collectors.groupingBy(obj -> buildImportRowGroupKey(obj, uniqueKeyList, preQueryResult, costImportEntity), LinkedHashMap::new, Collectors.toList()));
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        List<java.util.concurrent.Future<Void>> futures = new ArrayList<>();
        int batchSize = 100;
        List<Map.Entry<String, List<JSONObject>>> entryList = new ArrayList<>(map.entrySet());
        for (int i = 0; i < entryList.size(); i += batchSize) {
            int end = Math.min(entryList.size(), i + batchSize);
            List<Map.Entry<String, List<JSONObject>>> batch = entryList.subList(i, end);
            futures.add(importHistoryRecordPool.submit(() -> {
                for (Map.Entry<String, List<JSONObject>> entry : batch) {
                    List<JSONObject> value = entry.getValue();
                    JSONObject successJson = new JSONObject();
                    List<TmsCostDetailDTO.UpdateDTO> updateAllList = new ArrayList<>();
                    for (JSONObject jsonObject : value) {
                        jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                        List<String> costErrorMsgList = new ArrayList<>();
                        List<TmsCostDetailDTO.UpdateDTO> updateList = lineFormatCost(successJson, jsonObject, costErrorMsgList, preQueryResult.getCfgCostList(), cfgImportDetailList, headList, DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode(), currencyLookupMap, currencyRateMap);
                        if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                            jsonObject.set(matchIndex.toString(), MATCH_FAIL);
                            jsonObject.set(errorIndex.toString(), FieldValidUtil.getMsgSort(costErrorMsgList));
                            synchronized (matchImportList) { updateMatchResult(Collections.singletonList(jsonObject), matchIndex.toString(), errorIndex.toString(), costErrorMsgList, matchImportList); } ;
                            continue;
                        }
                        updateAllList.addAll(updateList);
                    }
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList);
                    List<JSONObject> costSuccessList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.get(matchIndex.toString()))).collect(Collectors.toList());
                    if (CollUtil.isEmpty(costSuccessList)) {
                        continue;
                    }
                    List<String> mainErrorMsgList = new ArrayList<>();
                    try {
                        // 同一识别分组会先汇总费用，再把固定的物流单集合传入 handleImportData，由既有重量分摊逻辑处理一对多/多对多。
                        List<LogisticsBillDTO.LogisticsBillVo> matchedLogisticsBillVoList = resolveGroupMatchedLogisticsBillVoList(uniqueKeyList,
                                costSuccessList, preQueryResult, costImportEntity, mainErrorMsgList);
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getLogisticsBillVoList(), preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList,
                                preQueryResult.getMainIdListMap(), matchedLogisticsBillVoList);
                        if (importDataDTO != null) {
                            importDataList.add(importDataDTO);
                        }
                    } catch (Exception e) {
                        log.error("数据处理失败", e);
                        mainErrorMsgList.add(ObjectUtil.defaultIfNull(e.getMessage(), e.toString()));
                    }
                    synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); } ;
                }
                return null;
            }));
        }
        List<Throwable> errors = new ArrayList<>();
        for (java.util.concurrent.Future<Void> f : futures) {
            try { f.get(); } catch (Exception ex) { log.error("horizontal future get error", ex); errors.add(ex); }
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException("多线程处理数据失败", errors.get(0));
        }
        return importDataList;
    }


    /**
     * 导入批量新增或更新数据
     * @author will
     * @date 2026/4/2 18:30
     * @param importDataList
     * @return  void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImportHistoryRecordDTO.ImportConfirmDTO> importBatchAddOrUpdate(List<LogisticsBillCostDTO.ImportDataDTO> importDataList,String processingType) {
        if (CollUtil.isEmpty(importDataList)) {
            return Collections.emptyList();
        }
        //新增物流单
        List<LogisticsBillEntity> logisticsBillList = importDataList.stream().map(LogisticsBillCostDTO.ImportDataDTO::getLogisticsBillEntity).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(logisticsBillList)) {
            logisticsBillService.batchImportAdd(logisticsBillList);
        }

        //新增物流明细
        List<LogisticsBillDetailEntity> importLogisticsBillDetailList = importDataList.stream().flatMap(obj -> {
            List<LogisticsBillDetailEntity> list = obj.getLogisticsBillDetailList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        List<LogisticsBillDetailEntity> addLogisticsBillDetailList = importLogisticsBillDetailList.stream()
                .filter(obj -> CharSequenceUtil.isBlank(obj.getId()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addLogisticsBillDetailList)) {
            logisticsBillDetailService.saveBatch(addLogisticsBillDetailList);
        }

        //新增物流费用
        List<LogisticsBillCostDTO.AddDTO> logisticsBillCostAddList = importDataList.stream().flatMap(obj -> {
            List<LogisticsBillCostDTO.AddDTO> list = obj.getAddBillCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        //导入确认
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),processingType)) {
            //批量修改对账状态为已确认
            logisticsBillCostAddList.forEach(obj -> obj.setImportConfirmDTO(new ImportHistoryRecordDTO.ImportConfirmDTO(obj.getId(),obj.getConfirmTime())));
        } else {
            logisticsBillCostAddList.forEach(obj -> obj.setConfirmTime(null));
        }
        //主表数据
        List<String> logisticsBillAddIdList = logisticsBillCostAddList.stream().map(LogisticsBillCostDTO.AddDTO::getLogisticsBillId).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillAddList = CollUtil.isEmpty(logisticsBillAddIdList) ? Collections.emptyList() : logisticsBillService.listByIds(logisticsBillAddIdList);
        logisticsBillList.addAll(logisticsBillAddList);
        //明细数据
        List<String> addDetailIdList = logisticsBillCostAddList.stream().map(LogisticsBillCostDTO.AddDTO::getLogisticsBillDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailAddList = CollUtil.isEmpty(addDetailIdList) ? Collections.emptyList() : logisticsBillDetailService.listByIds(addDetailIdList);
        List<LogisticsBillDetailEntity> logisticsBillDetailList = new ArrayList<>(addLogisticsBillDetailList);
        logisticsBillDetailList.addAll(logisticsBillDetailAddList);
        logisticsBillCostService.batchImportAdd(logisticsBillList,logisticsBillDetailList,logisticsBillCostAddList,processingType);

        //更新物流费用
        List<LogisticsBillCostDTO.UpdateDTO> logisticsBillCostUpdateList = importDataList.stream().flatMap(obj -> {
            List<LogisticsBillCostDTO.UpdateDTO> list = obj.getUpdateBillCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        //导入确认
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),processingType)) {
            //批量修改对账状态为已确认
            logisticsBillCostUpdateList.forEach(obj -> obj.setImportConfirmDTO(new ImportHistoryRecordDTO.ImportConfirmDTO(obj.getId(),obj.getConfirmTime())));
        } else {
            logisticsBillCostUpdateList.forEach(obj -> obj.setConfirmTime(null));
        }
        List<String> logisticsBillUpdateIdList = logisticsBillCostUpdateList.stream().map(LogisticsBillCostDTO.UpdateDTO::getLogisticsBillId).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillUpdateList = CollUtil.isEmpty(logisticsBillUpdateIdList) ? Collections.emptyList() : logisticsBillService.listByIds(logisticsBillUpdateIdList);

        List<String> updateDetailIdList = logisticsBillCostUpdateList.stream().map(LogisticsBillCostDTO.UpdateDTO::getLogisticsBillDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillDetailEntity> logisticsBillDetailUpdateList = CollUtil.isEmpty(updateDetailIdList) ? Collections.emptyList() : logisticsBillDetailService.listByIds(updateDetailIdList);

        logisticsBillCostService.batchImportUpdate(logisticsBillUpdateList,logisticsBillDetailUpdateList,logisticsBillCostUpdateList,processingType);

        //新增费用项
        List<TmsCostDetailDTO.AddDTO> costDetailAddList = importDataList.stream().flatMap(obj -> {
            List<TmsCostDetailDTO.AddDTO> list = obj.getAddCfgCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        tmsCostDetailService.batchImportAdd(costDetailAddList,DictCostAttributionEnum.SELF_DELIVER);

        //更新费用项
        List<TmsCostDetailDTO.UpdateDTO> costDetailUpdateList = importDataList.stream().flatMap(obj -> {
            List<TmsCostDetailDTO.UpdateDTO> list = obj.getUpdateCfgCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        tmsCostDetailService.batchImportUpdate(costDetailUpdateList,DictCostAttributionEnum.SELF_DELIVER,Boolean.TRUE);

        // 汇总新增/更新费用中的导入确认信息并去重返回
        LinkedHashMap<String, ImportHistoryRecordDTO.ImportConfirmDTO> confirmMap = new LinkedHashMap<>();
        Stream.concat(
                        logisticsBillCostAddList.stream().map(LogisticsBillCostDTO.AddDTO::getImportConfirmDTO),
                        logisticsBillCostUpdateList.stream().map(LogisticsBillCostDTO.UpdateDTO::getImportConfirmDTO)
                )
                .filter(ObjectUtil::isNotNull)
                .filter(dto -> CharSequenceUtil.isNotBlank(dto.getLogisticsCostId()))
                .forEach(dto -> {
                    String key = dto.getLogisticsCostId() + "_" + String.valueOf(dto.getConfirmDateTime());
                    confirmMap.putIfAbsent(key, dto);
                });
        return new ArrayList<>(confirmMap.values());
    }

    @Override
    public void confirmImportData(ImportHistoryRecordDTO.ImportSyncDTO importDTO, List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmPairList) {
        if (CollUtil.isEmpty(confirmPairList)) {
            return;
        }
        if (!CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),importDTO.getProcessingType())) {
            return;
        }
        logisticsBillCostService.batchConfirmImport(confirmPairList, ReconciliationStatusEnum.CONFIRMED.getCode());    }

    /**
     * 更新匹配结果
     * @author will
     * @date 2026/2/11 17:00
     * @param costSuccessList 匹配成功的数据列表
     * @param matchIndex 匹配结果所在列的下标
     * @param errorIndex 错误信息所在列的下标
     * @param mainErrorMsgList 主数据错误信息列表
     * @param matchImportList 最终用于导出匹配结果的列表
     */
    private void updateMatchResult( List<JSONObject> costSuccessList,String matchIndex, String errorIndex, List<String> mainErrorMsgList,List<JSONObject> matchImportList) {
        for (JSONObject jsonObject : costSuccessList) {
            //初始化匹配成功
            jsonObject.set(matchIndex,MATCH_SUCCESS);
            //判断错误信息是否为空
            if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                jsonObject.set(matchIndex,MATCH_FAIL);
                jsonObject.set(errorIndex,FieldValidUtil.getMsgSort(mainErrorMsgList));
                matchImportList.add(jsonObject);
            } else {
                //成功信息也要放到下载结果中
                matchImportList.add(jsonObject);
            }
        }
    }

    /**
     * 合并相同费用项、费用类型和币种的费用。
     * <p>同一识别分组可能来自多行 Excel，先在这里汇总费用，再由后续逻辑按物流单重量分摊。</p>
     * @author will
     * @date 2026/2/11 17:00
     * @param updateList
     * @return List<UpdateDTO>
     */
    private List<TmsCostDetailDTO.UpdateDTO> mergeTmsCostDetail (List<TmsCostDetailDTO.UpdateDTO> updateList) {
        if (CollUtil.isEmpty(updateList)) {
            return Collections.emptyList();
        }
        List<TmsCostDetailDTO.UpdateDTO>  mergeList = new ArrayList<>();
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> map = updateList.stream()
                .collect(Collectors.groupingBy(obj -> obj.getCfgCostId() + "_" + obj.getType() + "_" + CharSequenceUtil.blankToDefault(obj.getCurrency(), "")));
        for (Map.Entry<String, List<TmsCostDetailDTO.UpdateDTO>> entry :  map.entrySet()) {
            TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
            List<TmsCostDetailDTO.UpdateDTO> value = entry.getValue();
            BeanUtil.copyProperties(value.get(0), updateDTO);
            BigDecimal amount = value.stream().map(TmsCostDetailDTO.UpdateDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            updateDTO.setCostValue(amount);
            mergeList.add(updateDTO);
        }
        return mergeList;
    }


    /**
     * 物流商模板导入（confirmImport）场景下行级校验账单确认金额。
     * <p>仅 processingType 为 confirmImport 时执行；正式导入（import）只落库费用，不校验确认金额。</p>
     *
     * @param importDTO          导入上下文，用于判断 processingType
     * @param logisticsCostId    目标物流费用单 ID
     * @param currentUpdateList  本行待落库的费用明细
     * @param errorMsgList       校验失败时追加错误文案，供匹配结果导出
     */
    private void appendImportConfirmAmountError(ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                String logisticsCostId,
                                                List<TmsCostDetailDTO.UpdateDTO> currentUpdateList,
                                                List<String> errorMsgList) {
        if (!CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),
                importDTO.getProcessingType())) {
            return;
        }
        String confirmMsg = logisticsBillCostService.validateImportConfirmAmountMsg(
                logisticsCostId, currentUpdateList, ReconciliationStatusEnum.CONFIRMED.getCode());
        if (CharSequenceUtil.isNotBlank(confirmMsg)) {
            errorMsgList.add(confirmMsg);
        }
    }

    /**
     * 校验费用分类下的币种是否一致
     * @author will
     * @date 2026/2/11 10:27
     * @param
     * @return
     */
    private void checkCategoryCurrency (List<TmsCostDetailDTO.UpdateDTO> updateDetailList,LogisticsBillCostEntity logisticsBillCostEntity,
                                        List<TmsCfgCostEntity> tmsCfgCostList,
                                        Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                        List<String> errorMsgList) {
        List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateDetailList);
        List<TmsCostDetailEntity> tmsCostDetailEntityList = mainIdListMap.get(logisticsBillCostEntity.getId());
        if(CollUtil.isNotEmpty(tmsCostDetailEntityList)) {
            List<String> cfgCostIds = validateList.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
            validateList.addAll(tmsCostDetailEntityList.stream().filter(t -> !cfgCostIds.contains(t.getCfgCostId())).collect(Collectors.toList()));
        }
        Set<String> validateCategoryCurrency = tmsCostDetailService.validateCategoryCurrency(validateList);
        if (validateCategoryCurrency.isEmpty()) {
            return;
        }
        Map<String, Set<String>> costIdTypeListMap = new HashMap<>();
        for(String validateCategory : validateCategoryCurrency) {
            String[] split = validateCategory.split("_");
            List<TmsCostDetailDTO.UpdateDTO> removeList = updateDetailList.stream().filter(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1])).collect(Collectors.toList());
            for(TmsCostDetailDTO.UpdateDTO remove : removeList) {
                String costName = tmsCfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), remove.getCfgCostId())).findFirst().orElse(null).getCostName();
                Set<String> set = costIdTypeListMap.get(costName);
                if(CollUtil.isEmpty(set)) {
                    set = new HashSet<>();
                }
                set.add(AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有一级费用币种必须一致");
                costIdTypeListMap.put(costName, set);
            }
            updateDetailList.removeIf(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1]));
        }
        if (costIdTypeListMap.isEmpty()) {
            return;
        }
        errorMsgList.addAll(costIdTypeListMap.values().stream().map(Set::toString).collect(Collectors.toList()));
    }

    /**
     * 列费用数据处理
     * @author will
     * @date 2026/1/28 20:28
     * @param successJson
     * @param jsonObject
     * @param errorMsgList
     * @param cfgCostList
     * @param cfgImportDetailList
     * @param headList
     * @return List<UpdateDTO>
     */
    private List<TmsCostDetailDTO.UpdateDTO> lineFormatCost(JSONObject successJson, JSONObject jsonObject, List<String> errorMsgList, List<TmsCfgCostEntity> cfgCostList,
                                                            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, List<String> headList, String cfgAttribution,
                                                            Map<String, String> currencyLookupMap, Map<String, BigDecimal> currencyRateMap) {
        //查询币别
        String currency = cfgImportDetailList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getTargetField(), "currency"))
                .map(detail -> getPreparedValue(jsonObject, detail))
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst().orElse("");
        //币别赋值
        if (ObjectUtil.isNotNull(currency)) {
            String normalizeCurrency = normalizeCurrencyByDict(currency, currencyLookupMap);
            if (CharSequenceUtil.isBlank(normalizeCurrency)) {
                errorMsgList.add("币别不存在");
            } else {
                currency = normalizeCurrency;
            }
            //币别汇率
            BigDecimal rate = currencyRateMap.get(currency);
            if (ObjectUtil.isNull(rate)) {
                errorMsgList.add("币别对应汇率不存在");
            }
        }

        //费用数据
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        HashMap<String,String> currencyMap = new HashMap<>();
        for (CfgLogisticsCostImportDetailEntity cfgDetailEntity : cfgImportDetailList) {
            String preparedValue = getPreparedValue(jsonObject, cfgDetailEntity);
            if (CharSequenceUtil.isBlank(cfgDetailEntity.getTargetField())) {
                continue;
            }
            //判断导入字段是否是费用项
            if ("costItem".equals(cfgDetailEntity.getTargetField())) {
                //判断导入字段是否是费用项
                TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), cfgDetailEntity.getTargetDetailFieldName()) && CharSequenceUtil.equals(obj.getDictCostAttribution(), cfgAttribution)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity)) {
                    errorMsgList.add(CharSequenceUtil.format("费用管理未找到该费用名称【{}】", cfgDetailEntity.getTargetDetailFieldName()));
                    continue;
                }

                String oldCurrency = currencyMap.get(tmsCfgCostEntity.getDictCostCategory());
                if (CharSequenceUtil.isNotBlank(oldCurrency) &&  !CharSequenceUtil.equals(oldCurrency, currency)) {
                    errorMsgList.add("同一费用分类下币种必须一致");
                } else {
                    //添加币别费用
                    currencyMap.put(tmsCfgCostEntity.getDictCostCategory(),currency);
                }

                //是否绝对值
                Boolean isAbsoluteValue = cfgDetailEntity.getIsAbsoluteValue();

                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity) && CharSequenceUtil.isNotBlank(preparedValue)) {
                    String entryAmount = normalizeAmountText(preparedValue);
                    //校验费用值类型
                    List<String> errorMsg = FieldValidUtil.fieldValid(new TmsCostDetailDTO.CheckValueDTO(entryAmount));
                    if (CollUtil.isNotEmpty(errorMsg)) {
                        errorMsgList.add(tmsCfgCostEntity.getCostName() + errorMsg.get(0));
                        continue;
                    }
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //实际金额
                    updateDTO.setCostValue(toCostValue(entryAmount, isAbsoluteValue));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDTO.setCurrency(currency);
                    updateList.add(updateDTO);
                }
            }
            successJson.set(cfgDetailEntity.getTargetField(), preparedValue);
        }
        return updateList;
    }

    /**
     * 行费用数据处理
     * @author will
     * @date 2026/1/28 20:29
     * @param successJson
     * @param cfgCostList
     * @param cfgImportDetailList
     * @param headList
     * @return List<UpdateDTO>
     */
    private List<TmsCostDetailDTO.UpdateDTO> rowFormatCost(JSONObject successJson, JSONObject jsonObject, List<TmsCfgCostEntity> cfgCostList,
                                                            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, List<String> headList,
                                                            String costAttribution, List<String> errorMsgList, HashMap<String, String> currencyMap,
                                                            Map<String, String> currencyLookupMap, Map<String, BigDecimal> currencyRateMap) {
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        //查询币别
        String currency = cfgImportDetailList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getTargetField(), "currency"))
                .map(detail -> getPreparedValue(jsonObject, detail))
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst().orElse("");
        //币别赋值
        if (CharSequenceUtil.isNotBlank(currency)) {
            String normalizeCurrency = normalizeCurrencyByDict(currency, currencyLookupMap);
            if (CharSequenceUtil.isBlank(normalizeCurrency)) {
                errorMsgList.add("币别不存在");
            } else {
                currency = normalizeCurrency;
            }
            //币别汇率
            BigDecimal rate = currencyRateMap.get(currency);
            if (ObjectUtil.isNull(rate)) {
                errorMsgList.add("币别对应汇率不存在");
            }
        }

        //查询实际金额
        String actualAmount = normalizeAmountText(cfgImportDetailList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getTargetField(), "actualAmount"))
                .map(detail -> getPreparedValue(jsonObject, detail))
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst().orElse(null));

        //查询预估金额
        String estimatedAmount = normalizeAmountText(cfgImportDetailList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getTargetField(), "estimatedAmount"))
                .map(detail -> getPreparedValue(jsonObject, detail))
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst().orElse(null));

        //校验费用值类型
        List<String> errorMsg = FieldValidUtil.fieldValid(new TmsCostDetailDTO.CheckAmountDTO(actualAmount, estimatedAmount));
        if (CollUtil.isNotEmpty(errorMsg)) {
            errorMsgList.addAll(errorMsg);
            return updateList;
        }

        for (CfgLogisticsCostImportDetailEntity cfgDetailEntity : cfgImportDetailList) {
            String preparedValue = getPreparedValue(jsonObject, cfgDetailEntity);
            if (CharSequenceUtil.isBlank(cfgDetailEntity.getTargetField())) {
                continue;
            }
            //判断导入字段是否是费用项
            if ("costItem".equals(cfgDetailEntity.getTargetField())) {
                if (!CharSequenceUtil.equals(cfgDetailEntity.getSourceDetailField(), preparedValue)) {
                    continue;
                }
                TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), cfgDetailEntity.getTargetDetailFieldName()) && CharSequenceUtil.equals(obj.getDictCostAttribution(), costAttribution)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity)) {
                    errorMsgList.add(CharSequenceUtil.format("费用管理未找到该费用名称【{}】", cfgDetailEntity.getTargetDetailFieldName()));
                    continue;
                }

                String oldCurrency = currencyMap.get(tmsCfgCostEntity.getDictCostCategory());
                if (CharSequenceUtil.isNotBlank(oldCurrency) && !CharSequenceUtil.equals(oldCurrency, currency)) {
                    errorMsgList.add("同一费用分类下币种必须一致");
                } else {
                    //添加币别费用
                    currencyMap.put(tmsCfgCostEntity.getDictCostCategory(), currency);
                }

                if (StrUtil.isBlank(actualAmount) && StrUtil.isBlank(estimatedAmount)) {
                    errorMsgList.add(CharSequenceUtil.format("费用项【{}】实际金额和预估金额不能同时为空", cfgDetailEntity.getTargetDetailFieldName()));
                }
                //是否绝对值
                Boolean isAbsoluteValue = cfgDetailEntity.getIsAbsoluteValue();

                if (StrUtil.isNotBlank(actualAmount)) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //实际金额
                    updateDTO.setCostValue(toCostValue(actualAmount, isAbsoluteValue));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDTO.setCurrency(currency);
                    updateList.add(updateDTO);
                }
                if (StrUtil.isNotBlank(estimatedAmount)) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //预计金额
                    updateDTO.setCostValue(toCostValue(estimatedAmount, isAbsoluteValue));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setCurrency(currency);
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateList.add(updateDTO);
                }
            }
            successJson.set(cfgDetailEntity.getTargetField(), preparedValue);
        }
        return updateList;
    }

    /**
     * 金额字符串标准化，兼容科学计数法（如 1.2E+3 -> 1200）和逗号分隔格式（如 1,123,456.152 -> 1123456.152）
     */
    private String normalizeAmountText(String rawAmount) {
        if (StrUtil.isBlank(rawAmount)) {
            return rawAmount;
        }
        String trimmed = rawAmount.trim();

        try {
            // 1. 先移除所有逗号（千位分隔符）
            // 注意：某些地区可能使用逗号作为小数点，这里假设逗号是千位分隔符
            String withoutCommas = trimmed.replaceAll(",", "");

            // 2. 转换并标准化
            return new BigDecimal(withoutCommas).toPlainString();
        } catch (Exception e) {
            // 无法转换时保留原值，沿用现有校验逻辑输出错误信息
            return trimmed;
        }
    }

    /**
     * 费用金额转换，支持绝对值配置。
     */
    private BigDecimal toCostValue(String amount, Boolean isAbsoluteValue) {
        BigDecimal value = new BigDecimal(amount);
        return Boolean.TRUE.equals(isAbsoluteValue) ? value.abs() : value;
    }

    /**
     * 新增或更新数据
     * @author will
     * @date 2026/1/28 20:34
     * @param successJson
     * @param updateList
     * @param logisticsBillCostList
     * @param logisticsBillVos
     * @param cfgCostList
     * @param importDTO
     * @param costImportEntity
     * @param errorMsgList
     * @return void
     */
    /**
     * 按已解析的物流单集合生成导入数据。
     * <p>matchedLogisticsBillVos 由分组阶段传入，用于固定一对多/多对多分摊范围；为空时沿用原来的行内匹配逻辑。</p>
     */
    private LogisticsBillCostDTO.ImportDataDTO handleImportData(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList , JSONObject successJson, List<TmsCostDetailDTO.UpdateDTO> updateList, List<LogisticsBillCostEntity> logisticsBillCostList,
                                                                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos, List<TmsCfgCostEntity> cfgCostList, ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                                CfgLogisticsCostImportEntity costImportEntity, List<String> errorMsgList, Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                                                List<LogisticsBillDTO.LogisticsBillVo> matchedLogisticsBillVos) {

        ImportHistoryRecordExcelDTO excelDTO = BeanUtil.toBean(successJson, ImportHistoryRecordExcelDTO.class);

        //需要导入或更新的物流费用数据
        LogisticsBillCostDTO.ImportDataDTO  importDataDTO= new LogisticsBillCostDTO.ImportDataDTO();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //物流商信息
        excelDTO.setLogisticsSupplierId(costImportEntity.getDictPlatform());
        //付款类型
        String payTypeCode = logisticsPayTypeEnum.getByName(excelDTO.getPayType());
        if (CharSequenceUtil.isBlank(payTypeCode)) {
            payTypeCode = logisticsPayTypeEnum.PAY.getCode();
        }
        excelDTO.setPayType(payTypeCode);
        List<String> emptyUniqueKeyFieldList = uniqueKeyList.stream()
                .filter(uniqueKey -> ObjectUtil.isEmpty(successJson.get(uniqueKey.getTargetField()))
                        || CharSequenceUtil.isBlank(String.valueOf(successJson.get(uniqueKey.getTargetField()))))
                .map(uniqueKey -> CharSequenceUtil.blankToDefault(uniqueKey.getSourceField(), uniqueKey.getTargetField()))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(emptyUniqueKeyFieldList)) {
            errorMsgList.add("识别号对应字段不能为空：" + String.join("、", emptyUniqueKeyFieldList));
            return importDataDTO;
        }
        // 分组阶段已解析过物流单集合时优先使用该结果，避免 platformCode 多行合并后被最后一行平台单号重新缩窄匹配范围。
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList = ObjectUtil.isNotNull(matchedLogisticsBillVos)
                ? matchedLogisticsBillVos
                : resolveMatchedLogisticsBillVoList(uniqueKeyList, successJson, logisticsBillVos, costImportEntity);

        // IMPORT_ADD_NEW 已下线：按新单无法区分自发货/尾程归属，且配置入口已禁用；未匹配到物流单直接报错（原 getAddImportLogisticBill 分支已注释）。
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            errorMsgList.add("未找到对应物流单");
        }
        if (CollUtil.isEmpty(updateList)) {
            errorMsgList.add("物流费用项不能为空");
        }
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            return importDataDTO;
        }

        LocalDateTime confirmTime = CharSequenceUtil.isBlank(excelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(excelDTO.getConfirmTimeStr());

        if (CollUtil.isNotEmpty(logisticsBillVoList)) {
            Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocatedCostMap = Collections.emptyMap();
            if (logisticsBillVoList.size() > 1) {
                // 同一识别分组对应多张物流单时，按上游出库重量拆分已汇总费用，避免要求用户人工拆成多行。
                Map<String, BigDecimal> weightMap = buildOrderWeightMap(logisticsBillVoList, errorMsgList);
                if (CollectionUtils.isEmpty(errorMsgList)) {
                    allocatedCostMap = allocateCostDetailMap(updateList, logisticsBillVoList, weightMap, errorMsgList);
                }
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return importDataDTO;
            }
            List<LogisticsBillCostDTO.AddDTO> addBillCostList = new ArrayList<>();
            List<TmsCostDetailDTO.AddDTO> addCfgCostList = new ArrayList<>();
            List<LogisticsBillCostDTO.UpdateDTO> updateBillCostList = new ArrayList<>();
            List<TmsCostDetailDTO.UpdateDTO> updateCfgCostList = new ArrayList<>();
            String lastImportType = "";
            String lastBillCostType = "";
            for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
                logisticsBillVo.setReconciliationMonth(importDTO.getReconciliationMonth());
                // 单物流单沿用已汇总费用，多物流单使用按重量分摊后的费用项。
                List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = logisticsBillVoList.size() > 1
                        ? allocatedCostMap.getOrDefault(logisticsBillVo.getDetailId(), Collections.emptyList())
                        : updateList;
                // 返回 import_update/import_add_old，用于匹配费用单与落库分支；与 selfDeliver/lastMile 费用归属无关。
                String thisImportType = checkCostImportData(excelDTO, logisticsBillCostList, logisticsBillVo, costImportEntity, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    return importDataDTO;
                }
                if (CharSequenceUtil.isNotBlank(lastImportType) && !CharSequenceUtil.equals(lastImportType, thisImportType)) {
                    errorMsgList.add("同一识别单号分组匹配到的多张物流单导入类型不一致，无法合并处理");
                    return importDataDTO;
                }
                lastImportType = thisImportType;
                LogisticsBillCostEntity logisticsBillCostEntity = findMatchedLogisticsBillCost(logisticsBillCostList, logisticsBillVo, excelDTO.getPayType(), thisImportType);
                if (ObjectUtil.isEmpty(logisticsBillCostEntity)) {
                    errorMsgList.add("未找到对应物流费用单");
                    return importDataDTO;
                }
                if (CharSequenceUtil.isNotBlank(lastBillCostType) && !CharSequenceUtil.equals(lastBillCostType, logisticsBillCostEntity.getType())) {
                    errorMsgList.add("同一识别单号分组匹配到的多张物流单费用类型不一致，无法合并处理");
                    return importDataDTO;
                }
                lastBillCostType = logisticsBillCostEntity.getType();
                String billCostType = logisticsBillCostEntity.getType();
                if (!CharSequenceUtil.equals(billCostType, DictCostAttributionEnum.SELF_DELIVER.getCode())
                        && !CharSequenceUtil.equals(billCostType, DictCostAttributionEnum.LAST_MILE.getCode())) {
                    errorMsgList.add(CharSequenceUtil.format("不支持的物流费用类型【{}】，仅支持自发货或尾程",
                            CharSequenceUtil.blankToDefault(DictCostAttributionEnum.getName(billCostType), billCostType)));
                    return importDataDTO;
                }
                String rowSourceType = CharSequenceUtil.equals(billCostType, DictCostAttributionEnum.SELF_DELIVER.getCode())
                        ? SourceTypeEnum.LOGISTICS_BILL_COST.getCode()
                        : SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode();
                currentUpdateList.forEach(dto -> dto.setSourceType(rowSourceType));

                //校验分类币别
                checkCategoryCurrency(currentUpdateList, logisticsBillCostEntity, cfgCostList, mainIdListMap, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    return importDataDTO;
                }

                // confirmImport 时校验合并导入明细后的实际金额合计，正式导入不触发
                appendImportConfirmAmountError(importDTO, logisticsBillCostEntity.getId(), currentUpdateList, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    return importDataDTO;
                }

                //预处理直接跳过落库数据构造，但保留上面的匹配和校验。
                if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(), importDTO.getProcessingType())) {
                    continue;
                }

                //数据格式化
                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(confirmTime, logisticsBillCostEntity.getLogisticsBillId(), logisticsBillCostEntity.getId(), logisticsBillCostEntity.getLogisticsBillDetailId(), excelDTO,
                        importDTO, currentUpdateList, errorMsgList, cfgCostList);
                //有错误信息直接跳过不暂处理
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    return importDataDTO;
                }

                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(thisImportType)) {
                    LogisticsBillCostDTO.ImportDataDTO addDataDTO = new LogisticsBillCostDTO.ImportDataDTO();
                    addDataDTO.setConfirmTime(confirmTime);
                    List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO, currentUpdateList);
                    getLogisticsBillCostAddData(addDataDTO, logisticsBillCostEntity, dtoList, rowSourceType);
                    List<LogisticsBillCostDTO.AddDTO> currentAddBillCostList = addDataDTO.getAddBillCostList();
                    if (CollUtil.isNotEmpty(currentAddBillCostList)) {
                        addBillCostList.addAll(currentAddBillCostList);
                    }
                    addCfgCostList.addAll(ObjectUtil.defaultIfNull(addDataDTO.getAddCfgCostList(), Collections.emptyList()));
                } else {
                    updateDataDTO.setId(logisticsBillCostEntity.getId());
                    updateBillCostList.add(updateDataDTO);
                    //费用项id赋值
                    currentUpdateList.forEach(obj -> obj.setMainId(updateDataDTO.getId()));
                    updateCfgCostList.addAll(currentUpdateList);
                }
            }
            importDataDTO.setConfirmTime(confirmTime);
            importDataDTO.setImportType(lastImportType);
            importDataDTO.setAddBillCostList(addBillCostList);
            importDataDTO.setAddCfgCostList(addCfgCostList);
            importDataDTO.setUpdateBillCostList(updateBillCostList);
            importDataDTO.setUpdateCfgCostList(updateCfgCostList);
        }
        return importDataDTO;
    }

    /**
     * 按导入模板配置的唯一识别字段匹配物流单，字段为空已在上游统一拦截。
     */
    private boolean matchesUniqueKey(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList, JSONObject successJson, LogisticsBillDTO.LogisticsBillVo logisticsBillVo) {
        return uniqueKeyList.stream().allMatch(uniqueKey -> {
            String importValue = String.valueOf(successJson.get(uniqueKey.getTargetField()));
            Object billValue = BeanUtil.getFieldValue(logisticsBillVo, uniqueKey.getTargetField());
            if (CharSequenceUtil.equals(PLATFORM_CODE_FIELD, uniqueKey.getTargetField())) {
                return matchesPlatformCodeUniqueKey(importValue, billValue);
            }
            return CharSequenceUtil.equals(importValue, ObjectUtil.isNull(billValue) ? null : String.valueOf(billValue));
        });
    }

    private boolean matchesPlatformCodeUniqueKey(String importValue, Object billValue) {
        String platformCode = CharSequenceUtil.trim(importValue);
        if (CharSequenceUtil.isBlank(platformCode) || ObjectUtil.isNull(billValue)) {
            return false;
        }
        return Arrays.stream(String.valueOf(billValue).split(","))
                .map(CharSequenceUtil::trim)
                .anyMatch(item -> CharSequenceUtil.equals(platformCode, item));
    }

    /**
     * 物流商/平台模板只允许处理自身配置范围内的物流单，避免跨模板误写费用。
     */
    private boolean matchesCostImportConfig(CfgLogisticsCostImportEntity costImportEntity, LogisticsBillDTO.LogisticsBillVo logisticsBillVo) {
        String cfgType = costImportEntity.getCfgType();
        String dictPlatform = costImportEntity.getDictPlatform();
        if (CharSequenceUtil.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(), cfgType)) {
            return CharSequenceUtil.equals(dictPlatform, logisticsBillVo.getLogisticsSupplierId());
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportCfgTypeEnum.PLATFORM.getCode(), cfgType)) {
            return CharSequenceUtil.equals(dictPlatform, logisticsBillVo.getSalesPlatform());
        }
        return true;
    }

    private LogisticsBillCostEntity findMatchedLogisticsBillCost(List<LogisticsBillCostEntity> logisticsBillCostList,
                                                                LogisticsBillDTO.LogisticsBillVo logisticsBillVo,
                                                                String payType,
                                                                String importType) {
        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getLogisticsBillDetailId(), logisticsBillVo.getDetailId())
                                && (CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                                || (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(), obj.getReconciliationStatus())
                                && CharSequenceUtil.equals(obj.getCheckStatus(), LogisticsBillCostCheckStatusEnum.CHECKING.getCode())))
                                && CharSequenceUtil.equals(obj.getPayType(), payType))
                .findFirst().orElse(null);
        if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType) && Objects.isNull(logisticsBillCostEntity)) {
            // 按旧单新增费用允许复用已存在费用单作为模板，不强制要求状态/付款类型仍可更新。
            logisticsBillCostEntity = logisticsBillCostList.stream()
                    .filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(), logisticsBillVo.getDetailId()))
                    .findFirst().orElse(null);
        }
        return logisticsBillCostEntity;
    }

    /**
     * 订单重量 = SKU毛重 * 上游出库单实发数量，多物流单匹配时作为费用分摊依据。
     * 重量数据不完整时直接返回错误，避免把整行费用错误分摊到少数订单。
     */
    private Map<String, BigDecimal> buildOrderWeightMap(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList, List<String> errorMsgList) {
        Map<String, BigDecimal> weightMap = new HashMap<>();
        List<String> outstockIdList = logisticsBillVoList.stream()
                .map(LogisticsBillDTO.LogisticsBillVo::getOutstockId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (logisticsBillVoList.stream().anyMatch(vo -> CharSequenceUtil.isBlank(vo.getOutstockId()))) {
            errorMsgList.add("无法获取上游出库单用于重量分摊");
            return weightMap;
        }
        // 出库明细和SKU包材信息一次性批量查询，避免多物流单场景循环远程查询。
        List<SoOutstockDetailEntity> outstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class)
                .in(SoOutstockDetailEntity::getMainId, outstockIdList)
                .list();
        if (CollUtil.isEmpty(outstockDetailList)) {
            errorMsgList.add("无法获取上游出库明细用于重量分摊");
            return weightMap;
        }
        Map<String, List<SoOutstockDetailEntity>> outstockDetailMap = outstockDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId));
        List<String> skuIdList = outstockDetailList.stream().map(SoOutstockDetailEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)) {
            errorMsgList.add("出库明细缺少SKU信息，无法按重量分摊");
            return weightMap;
        }
        List<ProductPackEntity> productPackList = FeignQuery.create(ProductPackEntity.class).in(ProductPackEntity::getSkuId, skuIdList).list();
        Map<String, ProductPackEntity> productPackMap = CollUtil.isEmpty(productPackList)
                ? new HashMap<>()
                : productPackList.stream().collect(Collectors.toMap(ProductPackEntity::getSkuId, obj -> obj, (first, second) -> first));
        int weightErrorCountBefore = errorMsgList.size();
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            List<SoOutstockDetailEntity> detailList = outstockDetailMap.get(logisticsBillVo.getOutstockId());
            if (CollUtil.isEmpty(detailList)) {
                errorMsgList.add("无法获取上游出库明细用于重量分摊：" + logisticsBillVo.getOutstockCode());
                continue;
            }
            BigDecimal orderWeight = BigDecimal.ZERO;
            boolean hasWeightDetailError = false;
            for (SoOutstockDetailEntity detailEntity : detailList) {
                ProductPackEntity productPackEntity = productPackMap.get(detailEntity.getSkuId());
                if (ObjectUtil.isNull(productPackEntity) || ObjectUtil.isNull(productPackEntity.getGrossWeight()) || productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) <= 0) {
                    errorMsgList.add("缺少SKU毛重，无法按重量分摊：" + detailEntity.getSkuNo());
                    hasWeightDetailError = true;
                    continue;
                }
                if (ObjectUtil.isNull(detailEntity.getActualQty())) {
                    errorMsgList.add("出库实发数量为空，无法按重量分摊：" + detailEntity.getSkuNo());
                    hasWeightDetailError = true;
                    continue;
                }
                orderWeight = orderWeight.add(productPackEntity.getGrossWeight().multiply(BigDecimal.valueOf(detailEntity.getActualQty())));
            }
            if (hasWeightDetailError) {
                continue;
            }
            if (orderWeight.compareTo(BigDecimal.ZERO) <= 0) {
                errorMsgList.add("订单重量为0，无法执行费用分摊：" + logisticsBillVo.getOutstockCode());
                continue;
            }
            weightMap.put(logisticsBillVo.getDetailId(), orderWeight);
        }
        BigDecimal totalWeight = weightMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0 && errorMsgList.size() == weightErrorCountBefore) {
            errorMsgList.add("总订单重量为0，无法执行费用分摊");
        }
        return weightMap;
    }

    /**
     * 按订单重量比例拆分每个费用项，最后一单吸收四位小数舍入尾差，保证拆分后总金额等于导入金额。
     */
    private Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocateCostDetailMap(List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                                                Map<String, BigDecimal> weightMap,
                                                                                List<String> errorMsgList) {
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> resultMap = new LinkedHashMap<>();
        BigDecimal totalWeight = logisticsBillVoList.stream().map(vo -> weightMap.getOrDefault(vo.getDetailId(), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            errorMsgList.add("总订单重量为0，无法执行费用分摊");
            return resultMap;
        }
        for (TmsCostDetailDTO.UpdateDTO updateDTO : updateList) {
            if (ObjectUtil.isNull(updateDTO) || ObjectUtil.isNull(updateDTO.getCostValue())) {
                errorMsgList.add("物流费用金额不能为空，无法执行费用分摊");
                return resultMap;
            }
            BigDecimal allocatedSum = BigDecimal.ZERO;
            for (int i = 0; i < logisticsBillVoList.size(); i++) {
                LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(i);
                BigDecimal allocatedCost = i == logisticsBillVoList.size() - 1
                        ? updateDTO.getCostValue().subtract(allocatedSum)
                        : updateDTO.getCostValue().multiply(weightMap.getOrDefault(logisticsBillVo.getDetailId(), BigDecimal.ZERO)).divide(totalWeight, 4, RoundingMode.HALF_UP);
                allocatedSum = allocatedSum.add(allocatedCost);
                TmsCostDetailDTO.UpdateDTO copyDTO = copyUpdateCostDetail(updateDTO);
                copyDTO.setCostValue(allocatedCost);
                resultMap.computeIfAbsent(logisticsBillVo.getDetailId(), key -> new ArrayList<>()).add(copyDTO);
            }
        }
        return resultMap;
    }

    private TmsCostDetailDTO.UpdateDTO copyUpdateCostDetail(TmsCostDetailDTO.UpdateDTO source) {
        TmsCostDetailDTO.UpdateDTO copyDTO = new TmsCostDetailDTO.UpdateDTO();
        BeanUtil.copyProperties(source, copyDTO);
        return copyDTO;
    }

    /**
     *
     * @author will
     * @date 2026/4/1 16:33
     * @param importDataDTO
     * @param logisticsBillCostEntity
     * @param dtoList
     * @return void
     */
    private void getLogisticsBillCostAddData(LogisticsBillCostDTO.ImportDataDTO importDataDTO,LogisticsBillCostEntity logisticsBillCostEntity,List<LogisticsBillCostDTO.AddDataDTO> dtoList,String sourceType) {
        //物流费用
        List<LogisticsBillCostDTO.AddDTO> addBillCostList  = new ArrayList<>();
        //费用项新增列表
        List<TmsCostDetailDTO.AddDTO> addCfgCostList = new ArrayList<>();

        Map<String, List<LogisticsBillCostDTO.AddDataDTO>> sourceIdDtoMaps = dtoList.stream().collect(Collectors.groupingBy(LogisticsBillCostDTO.AddDataDTO::getSourceId));
        for(Map.Entry<String, List<LogisticsBillCostDTO.AddDataDTO>> sourceIdDtoMap : sourceIdDtoMaps.entrySet()) {
            List<LogisticsBillCostDTO.AddDataDTO> value = sourceIdDtoMap.getValue();

            value.forEach(v -> {
                if(CharSequenceUtil.isBlank(v.getCurrency())) {
                    v.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                }
                if(CharSequenceUtil.isBlank(v.getEstimatedCurrency())) {
                    v.setEstimatedCurrency(CurrencyEnum.CNY.getCurrencyCode());
                }
            });

            Map<String, List<LogisticsBillCostDTO.AddDataDTO>> cfgCostIdMaps = value.stream().collect(Collectors.groupingBy(LogisticsBillCostDTO.AddDataDTO::getCfgCostId));
            value = new ArrayList<>();
            for(Map.Entry<String, List<LogisticsBillCostDTO.AddDataDTO>> cfgCostIdMap : cfgCostIdMaps.entrySet()) {
                List<LogisticsBillCostDTO.AddDataDTO> groupValue = cfgCostIdMap.getValue();
                LogisticsBillCostDTO.AddDataDTO v = groupValue.get(0);
                String estimatedCurrency = v.getEstimatedCurrency();
                String currency = v.getCurrency();
                if(groupValue.stream().anyMatch(g -> !estimatedCurrency.equals(g.getEstimatedCurrency()))) {
                    throw new ServiceException("【" + tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName() + "】相同费用类型预估金额存在不同币别");
                }
                if(groupValue.stream().anyMatch(g -> !currency.equals(g.getCurrency()))) {
                    throw new ServiceException("【" + tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName() + "】相同费用类型实际金额存在不同币别");
                }
                v.setEstimatedValue(groupValue.stream().filter(g -> g.getEstimatedValue() != null).map(LogisticsBillCostDTO.AddDataDTO::getEstimatedValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
                v.setCostValue(groupValue.stream().filter(g -> g.getCostValue() != null).map(LogisticsBillCostDTO.AddDataDTO::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
                value.add(v);
            }

            LogisticsBillCostDTO.AddDataDTO dto = value.get(0);
            LogisticsBillCostDTO.AddDTO addDTO = new LogisticsBillCostDTO.AddDTO();
            addDTO.setId(IdWorker.getIdStr());
            addDTO.setPayType(dto.getPayType());
            addDTO.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
            addDTO.setLogisticsBillId(logisticsBillCostEntity.getLogisticsBillId());
            addDTO.setLogisticsBillDetailId(logisticsBillCostEntity.getLogisticsBillDetailId());
            addDTO.setActualWeight(logisticsBillCostEntity.getActualWeight());
            addDTO.setVolumeWeight(logisticsBillCostEntity.getVolumeWeight());
            addDTO.setBillingWeightLogistics(logisticsBillCostEntity.getBillingWeightLogistics());
            addDTO.setConfirmTime(importDataDTO.getConfirmTime());
            String currency = dto.getCurrency();
            if(org.apache.commons.lang3.StringUtils.isBlank(currency)) {
                currency = CurrencyEnum.CNY.getCurrencyCode();
            }

            addDTO.setCurrency(currency);

            addDTO.setTrackNo(logisticsBillCostEntity.getTrackNo());
            addDTO.setChannelId(logisticsBillCostEntity.getChannelId());
            addDTO.setWeightLogistics(logisticsBillCostEntity.getWeightLogistics());
            addDTO.setVolumeWeightLogistics(logisticsBillCostEntity.getVolumeWeightLogistics());

            addDTO.setReconciliationMonth(dto.getReconciliationMonth());
            addDTO.setThirdHeight(dto.getThirdHeight());
            addDTO.setThirdWidth(dto.getThirdWidth());
            addDTO.setThirdLength(dto.getThirdLength());
            addDTO.setThirdActualWeight(dto.getThirdActualWeight());

            for(LogisticsBillCostDTO.AddDataDTO detailDTO : value) {
                TmsCostDetailDTO.AddDTO add = new TmsCostDetailDTO.AddDTO();
                add.setMainId(addDTO.getId());
                add.setCfgCostId(detailDTO.getCfgCostId());
                add.setCostValue(detailDTO.getCostValue());
                add.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                add.setSourceType(sourceType);
                add.setCurrency(detailDTO.getCurrency());
                addCfgCostList.add(add);

                BigDecimal estimatedValue = detailDTO.getEstimatedValue();
                if(estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) != 0) {
                    add = new TmsCostDetailDTO.AddDTO();
                    add.setMainId(addDTO.getId());
                    add.setCfgCostId(detailDTO.getCfgCostId());
                    add.setCostValue(estimatedValue);
                    add.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    add.setSourceType(sourceType);
                    add.setCurrency(detailDTO.getEstimatedCurrency());
                    addCfgCostList.add(add);
                }
            }
            addBillCostList.add(addDTO);
        }
        importDataDTO.setAddBillCostList(addBillCostList);
        importDataDTO.setAddCfgCostList(addCfgCostList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO regenerateImportExcel(String id,String processingType) {
        ImportHistoryRecordEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "导入记录");
        }
        if (!CharSequenceUtil.equals(entity.getStatus(),ImportHistoryRecordStatusEnum.WAIT_HANDLE.getCode())) {
            return BatchResultDTO.fail(id,entity.getFileName(),"当前导入记录非待处理，无法重新导入");
        }

        BaseDTO.ImportDTO importDTO = new BaseDTO.ImportDTO();
        importDTO.setFileName(entity.getFileName());
        importDTO.setFileUrl(entity.getFileUrl());
        ImportHistoryRecordDTO.ImportDTO dto = new ImportHistoryRecordDTO.ImportDTO();
        dto.setBusinessType(entity.getBusinessType());
        dto.setProcessingType(processingType);
        dto.setReconciliationMonth(entity.getReconciliationMonth());

        //查询配置主表信息
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(entity.getFileName(), DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode(), CfgLogisticsCostImportCostTypeEnum.EXCEL.getCode());
        if (CollUtil.isEmpty(cfgLogisticsCostImportList)) {
            return BatchResultDTO.fail(importDTO.getTaskId(),entity.getFileName(),"无法识别导入模板，请检查配置是否正确");
        }
        //查询配置明细信息
        List<String> mainIdList = cfgLogisticsCostImportList.stream().map(CfgLogisticsCostImportEntity::getId).distinct().collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> importDetailList =  cfgLogisticsCostImportDetailService.listByMainIdList(mainIdList);
        if (CollUtil.isEmpty(importDetailList)) {
            return BatchResultDTO.fail(importDTO.getTaskId(),entity.getFileName(),ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND.getMsg());
        }

        List<FileDTO.FileTaskDTO> fileTaskDTOS = fileFeign.listLatestFileTask(Collections.singletonList(entity.getFileUrl()));
        if (CollUtil.isEmpty(fileTaskDTOS)) {
            throw new ServiceException("未找到对应的文件信息，请检查文件是否正确上传");
        }

        //更新导入记录状态
        entity.setStatus(ImportHistoryRecordStatusEnum.HANDLE_ING.getCode());
        super.updateById(entity);

        ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO = new ImportHistoryRecordDTO.ImportSyncDTO(dto, importDTO);
        importSyncDTO.setTaskId(fileTaskDTOS.get(0).getTaskId());
        importSyncDTO.setCfgLogisticsCostImportList(cfgLogisticsCostImportList);
        importSyncDTO.setImportDetailList(importDetailList);
        importSyncDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        String taskId = downloadTaskFeign.reImportTask(fileTaskDTOS.get(0).getTaskId(),"物流商费用导入", IMPORT_TMS_IMPORT_HISTORY_RECORD.getCode(), importSyncDTO);
        return  BatchResultDTO.success(taskId,importSyncDTO.getFileName(),"重新导入成功");
    }

    /**
     * IMPORT_ADD_NEW 已下线，原按新单（import_add_new）格式化待新增物流单逻辑保留注释供恢复参考。
     * 原因：模板导入同一文件可能混合自发货/尾程，按新单无法可靠判定费用归属；handleImportData 已移除对应分支。
     */
    /*
    private void getAddImportLogisticBill (LogisticsBillCostDTO.ImportDataDTO importDataDTO,ImportHistoryRecordExcelDTO excelDTO,String costAttribution) {
        //新增物流单，格式化物流费用
        LogisticsBillEntity addBillEntity = new LogisticsBillEntity();
        addBillEntity.setLogisticsSupplierId(excelDTO.getLogisticsSupplierId());

        //发货单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSoDeliveryCode())) {
            if (excelDTO.getSoDeliveryCode().startsWith("FHTZ")) {
                List<SoDeliveryNoticeEntity> list = FeignQuery.create(SoDeliveryNoticeEntity.class).eq(SoDeliveryNoticeEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addBillEntity.setSoDeliveryCode(list.get(0).getId());
                    addBillEntity.setSourceCode(list.get(0).getSourceCode());
                    addBillEntity.setOrderType(OrderTypeEnum.B2B.getCode());
                    addBillEntity.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                }
            } else if (excelDTO.getSoDeliveryCode().startsWith("FHDC")) {
                List<SoB2cDeliveryEntity> list = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addBillEntity.setSoDeliveryCode(list.get(0).getId());
                    addBillEntity.setSourceCode(list.get(0).getSourceCode());
                    addBillEntity.setOrderType(OrderTypeEnum.B2C.getCode());
                    addBillEntity.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                }
            } else {
                addBillEntity.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }
        //销售订单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {
            if (CharSequenceUtil.isNotBlank(addBillEntity.getSourceCode()) && !CharSequenceUtil.equals(addBillEntity.getSourceCode(),excelDTO.getSourceCode())) {
                throw new ServiceException("发货单对应的销售订单与导入的销售订单不匹配，请核查");
            }
            if (excelDTO.getSourceCode().startsWith("XSD")) {
                List<SoInfoEntity> list = FeignQuery.create(SoInfoEntity.class).eq(SoInfoEntity::getCode, excelDTO.getSourceCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addBillEntity.setSourceId(list.get(0).getId());
                    addBillEntity.setOrderType(OrderTypeEnum.B2B.getCode());
                    addBillEntity.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                }
            } else if (excelDTO.getSourceCode().startsWith("XSDS")) {
                List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getCode, excelDTO.getSourceCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addBillEntity.setSourceId(list.get(0).getId());
                    addBillEntity.setOrderType(OrderTypeEnum.B2C.getCode());
                    addBillEntity.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                }
            } else {
                addBillEntity.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }

        //订单类型默认其他
        if (CharSequenceUtil.isBlank(addBillEntity.getOrderType())) {
            addBillEntity.setOrderType(OrderTypeEnum.OTHER.getCode());
        }

        addBillEntity.setSourceCode(excelDTO.getSourceCode());
        addBillEntity.setPlatformCode(excelDTO.getPlatformCode());
        addBillEntity.setSoDeliveryCode(excelDTO.getSoDeliveryCode());
        String shipmentType = CharSequenceUtil.equals(costAttribution, DictCostAttributionEnum.SELF_DELIVER.getCode()) ?ShipmentTypeEnum.SELF_DELIVER.getCode() : ShipmentTypeEnum.PLATFORM_DELIVER.getCode();
        addBillEntity.setShipmentType(shipmentType);
        addBillEntity.setId(IdWorker.getIdStr());

        LogisticsBillDetailEntity addBillDetailEntity = new LogisticsBillDetailEntity();
        addBillDetailEntity.setTrackNo(excelDTO.getTrackNo());
        addBillDetailEntity.setTrackEnable(Boolean.FALSE);
        addBillDetailEntity.setMainId(addBillEntity.getId());

        importDataDTO.setLogisticsBillEntity(addBillEntity);
        importDataDTO.setLogisticsBillDetailList(Collections.singletonList(addBillDetailEntity));
    }
    */

    /**
     * 数据处理
     * @author will
     * @date 2026/1/22 20:04
     * @param excelDTO
     * @param importDTO
     * @param updateList
     * @param cfgCostList
     * @return void
     */
    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData(LocalDateTime confirmTime, String logisticsBillId,String logisticsBIllCostId, String logisticsBIllDetailId,ImportHistoryRecordExcelDTO excelDTO,
                                                                             ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                                             List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                             List<String> errorMsgList,
                                                                             List<TmsCfgCostEntity> cfgCostList) {
        //数据赋值
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setId(logisticsBIllCostId);
        updateDataDTO.setLogisticsBillId(logisticsBillId);
        updateDataDTO.setLogisticsBillDetailId(logisticsBIllDetailId);
        updateDataDTO.setBillingWeightLogistics(CharSequenceUtil.isBlank(excelDTO.getBillingWeightLogistics()) ? null : new BigDecimal(excelDTO.getBillingWeightLogistics()));
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
        updateDataDTO.setPayType(excelDTO.getPayType());
        updateDataDTO.setConfirmTime(confirmTime);
        updateDataDTO.setTrackNo(excelDTO.getTrackNo());
        //对账月份
        updateDataDTO.setReconciliationMonth(importDTO.getReconciliationMonth());
        //尺寸
        String thirdHeight = excelDTO.getThirdHeight();
        if(StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdHeight(new BigDecimal(thirdHeight));
        }
        String thirdWidth = excelDTO.getThirdWidth();
        if(StringUtils.isNotBlank(thirdWidth)) {
            updateDataDTO.setThirdWidth(new BigDecimal(thirdWidth));
        }
        String thirdLength = excelDTO.getThirdLength();
        if(StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdLength(new BigDecimal(thirdLength));
        }
        //实重
        String thirdActualWeight = excelDTO.getThirdActualWeight();
        if(StringUtils.isNotBlank(thirdActualWeight)) {
            updateDataDTO.setThirdActualWeight(new BigDecimal(thirdActualWeight));
        }

        updateList.forEach(u ->  u.setCurrency( CharSequenceUtil.isBlank(u.getCurrency()) ?  updateDataDTO.getCurrency() : u.getCurrency()));
        List<TmsCostDetailEntity> validateList = BeanMapperUtils.copyList(TmsCostDetailEntity.class, updateList);
        List<TmsCostDetailEntity> tmsCostDetailEntityList = tmsCostDetailService.listByMainIdList(Collections.singletonList(logisticsBillId));
        if(CollUtil.isNotEmpty(tmsCostDetailEntityList)) {
            List<String> cfgCostIds = validateList.stream().map(TmsCostDetailEntity::getCfgCostId).collect(Collectors.toList());
            validateList.addAll(tmsCostDetailEntityList.stream().filter(t -> !cfgCostIds.contains(t.getCfgCostId())).collect(Collectors.toList()));
        }
        Set<String> validateCategoryCurrency = tmsCostDetailService.validateCategoryCurrency(validateList);
        if(!validateCategoryCurrency.isEmpty()) {
            Map<String, String> costIdTypeListMap = new HashMap<>();
            for(String validateCategory : validateCategoryCurrency) {
                String[] split = validateCategory.split("_");
                List<TmsCostDetailDTO.UpdateDTO> removeList = updateList.stream().filter(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1])).collect(Collectors.toList());
                for(TmsCostDetailDTO.UpdateDTO remove : removeList) {
                    String costName = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), remove.getCfgCostId())).findFirst().orElse(null).getCostName();
                    costIdTypeListMap.put(costName, AllocationFeeTypeEnum.getName(split[0]) + "-" + LogisticsBillCostTypeEnum.getName(split[1]) + "分类下所有费用币种必须一致");
                }
                updateList.removeIf(u -> u.getDictCostCategory().equals(split[0]) && u.getType().equals(split[1]));
            }
            if(!costIdTypeListMap.isEmpty()) {
                errorMsgList.addAll(costIdTypeListMap.values());
            }
        }
        return updateDataDTO;
    }

    /**
     * 数据处理
     * @author will
     * @date 2026/1/22 18:22
     * @param updateDataDTO
     * @param updateList
     * @return List<AddDataDTO>
     */
    private List<LogisticsBillCostDTO.AddDataDTO> buildAddDTO(LogisticsBillCostDTO.UpdateDTO updateDataDTO, List<TmsCostDetailDTO.UpdateDTO> updateList) {
        List<LogisticsBillCostDTO.AddDataDTO> dtoList = new ArrayList<>();
        updateList.forEach(u -> {
            LogisticsBillCostDTO.AddDataDTO addDataDTO = new LogisticsBillCostDTO.AddDataDTO();
            addDataDTO.setPayType(updateDataDTO.getPayType());
            addDataDTO.setSourceId(updateDataDTO.getId());
            addDataDTO.setBillingWeight(updateDataDTO.getBillingWeight());
            addDataDTO.setBillingWeightLogistics(updateDataDTO.getBillingWeightLogistics());
            addDataDTO.setCurrency(u.getCurrency());

            addDataDTO.setReconciliationMonth(updateDataDTO.getReconciliationMonth());
            addDataDTO.setThirdHeight(updateDataDTO.getThirdHeight());
            addDataDTO.setThirdWidth(updateDataDTO.getThirdWidth());
            addDataDTO.setThirdLength(updateDataDTO.getThirdLength());
            addDataDTO.setThirdActualWeight(updateDataDTO.getThirdActualWeight());
            addDataDTO.setCfgCostId(u.getCfgCostId());
            addDataDTO.setCostValue(u.getCostValue());
            dtoList.add(addDataDTO);
        });
        return dtoList;
    }

    /**
     * 校验物流费用单是否可导入，并判定导入处理类型（更新 / 按原单新增）。
     * 返回值供 findMatchedLogisticsBillCost 与落库分支使用，不是物流费用主单 type（selfDeliver/lastMile）。
     */
    private String checkCostImportData(ImportHistoryRecordExcelDTO excelDTO,
                                     List<LogisticsBillCostEntity> logisticsBillCostList,
                                     LogisticsBillDTO.LogisticsBillVo logisticsBillVo,
                                     CfgLogisticsCostImportEntity costImportEntity,
                                     List<String> errorMsgList) {
        if (CharSequenceUtil.isBlank(excelDTO.getPlatformCode()) && CharSequenceUtil.isBlank(excelDTO.getSourceCode())
                && CharSequenceUtil.isBlank(excelDTO.getSoDeliveryCode()) && CharSequenceUtil.isBlank(excelDTO.getTrackNo())) {
            errorMsgList.add(ApiError.LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_BILL.getMsg());
        }

        //导入类型
        String importType = "";

        //物流单明细
        if (CharSequenceUtil.isBlank(logisticsBillVo.getDetailId())) {
            errorMsgList.add("未找到对应的物流单明细");
        }
        //物流费用单
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostList.stream()
                .filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                        && CharSequenceUtil.equals(logisticsBillVo.getTrackNo(),obj.getTrackNo())
                        && CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType()))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(logisticsBillCostEntityList)) {
            /**
             * 同一物流单明细可能存在多条物流费用单，需进一步筛选出符合对账类型的物流费用单
             * 1、存在对账月份为空且对账状态为暂估确认且未下推分摊的单，或者对账月份为空且对账状态为待确认的物流费用单，或者对账月份与导入数据一致的物流费用单，走更新逻辑
             * 2、其他情况走新增（按原单）逻辑
             */

            List<LogisticsBillCostEntity> thisMonthEntityList = logisticsBillCostList.stream()
                    .filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                            && CharSequenceUtil.equals(logisticsBillVo.getTrackNo(),obj.getTrackNo())
                            && (
                            (CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode()) && CharSequenceUtil.equals(obj.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode()))
                                    || (CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()))
                    )
                            && CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType()))
                    .collect(Collectors.toList());
            //未查询到物流费用单则需要按新增分货（按原单）逻辑处理
            if (CollUtil.isNotEmpty(thisMonthEntityList)) {
                boolean contains = costImportEntity.getImportType().contains(CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode());
                if (!contains) {
                    errorMsgList.add("配置的导入处理类型不包含导入更新，请核查配置");
                }
                importType = CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode();
            } else {
                boolean contains = costImportEntity.getImportType().contains(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
                if (!contains) {
                    errorMsgList.add("未查到物流费用单，配置的导入处理类型不包含导入新增（按原单），请核查单号");
                }
                importType = CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode();
            }
        }else {
            //
            logisticsBillCostEntityList = logisticsBillCostList.stream()
                    .filter(obj -> obj.getLogisticsBillId().equals(logisticsBillVo.getId())
                            && CharSequenceUtil.equals(logisticsBillVo.getTrackNo(),obj.getTrackNo())
                            && !CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType()))
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(logisticsBillCostEntityList)) {
                errorMsgList.add("未找到对应的物流费用单");
                return importType;
            }
            boolean contains = costImportEntity.getImportType().contains(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
            if (!contains) {
                errorMsgList.add("未查到物流费用单，配置的导入处理类型不包含导入新增（按原单），请核查单号");
            }
            importType = CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode();
        }

        //校验物流费用
        if(logisticsBillCostEntityList.size() > 1) {
            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType)) {
                //更新时判断是否有多条可更新的数据,需要对账月份未空或者对账月份一致，并且为待确认或者暂估确认但是未下推费用分摊的数据
                long count = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()) || (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),obj.getReconciliationStatus())
                        && CharSequenceUtil.equals(obj.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode()))
                ).count();
                if (count > 1) {
                    errorMsgList.add("出库单和运输单号对应对账类型的物流费用单有多条，请在页面编辑指定物流费用单");
                }
                if (count == 0) {
                    errorMsgList.add("出库单和运输单号对应对账类型的物流费用单无可更新的数据，请核查");
                }
            } else {
                //新增时判断是否已存在相同对账月份
                long hasCount = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getReconciliationMonth(), logisticsBillVo.getReconciliationMonth())).count();
                if (hasCount > 0) {
                    errorMsgList.add("已存在相同对账月份的物流费用单，不支持新增");
                }
                long confirmCount = logisticsBillCostEntityList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getReconciliationMonth(), logisticsBillVo.getReconciliationMonth()) && CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())).count();
                if (confirmCount > 0) {
                    errorMsgList.add("已存在未确认的物流费用单，不支持新增");
                }
            }
        }else {
            LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntityList.get(0);
            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType) ) {
                if (!CharSequenceUtil.equals(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(),logisticsBillCostEntity.getReconciliationStatus())
                        && !CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),logisticsBillCostEntity.getReconciliationStatus())) {
                    errorMsgList.add("物流费用单非待确认不支持更新");
                }
                if (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),logisticsBillCostEntity.getReconciliationStatus())
                        && !CharSequenceUtil.equals(logisticsBillCostEntity.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode())) {
                    errorMsgList.add("暂估确认物流费用单已下推费用分摊，不支持更新");
                }
            } else{
                if (CharSequenceUtil.equals(logisticsBillVo.getReconciliationMonth(),logisticsBillCostEntity.getReconciliationMonth())) {
                    errorMsgList.add("已存在相同对账月份的物流费用单，不支持新增");
                }
                if (!CharSequenceUtil.equals(logisticsBillVo.getReconciliationMonth(), logisticsBillCostEntity.getReconciliationMonth()) && CharSequenceUtil.equals(logisticsBillCostEntity.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
                    errorMsgList.add("已存在未确认的物流费用单，不支持新增");
                }
            }
        }
        return  importType;
    }


    private void prepareImportRowValues(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                        Map<Integer, String> headMap,
                                        List<JSONObject> successList) {
        int nextVirtualIndex = headMap.keySet().stream().filter(Objects::nonNull).max(Integer::compareTo).orElse(-1) + 1;
        boolean isVertical = isVerticalCostItem(cfgImportDetailList);
        for (CfgLogisticsCostImportDetailEntity detail : cfgImportDetailList) {
            if (isVertical && isVerticalAmountDetail(detail)) {
                if (CharSequenceUtil.isNotBlank(detail.getSourceField())) {
                    Integer mappingIndex = getMapKey(headMap, detail.getSourceField());
                    if (ObjectUtil.isNotNull(mappingIndex)) {
                        detail.setMappingIndex(mappingIndex);
                    }
                }
                continue;
            }
            if (CharSequenceUtil.isBlank(detail.getSourceField())) {
                if (CharSequenceUtil.isBlank(detail.getDefaultValue())) {
                    continue;
                }
                if (ObjectUtil.isNull(detail.getMappingIndex())) {
                    detail.setMappingIndex(nextVirtualIndex++);
                }
                for (JSONObject rowData : successList) {
                    String cleanedValue = cleanFieldValue(detail.getDefaultValue(), detail, rowData, headMap);
                    setPreparedValue(rowData, detail, cleanedValue);
                }
                continue;
            }

            Integer mappingIndex = getMapKey(headMap, detail.getSourceField());
            if (ObjectUtil.isEmpty(mappingIndex)) {
                if (Boolean.TRUE.equals(detail.getIsUniqueKey())) {
                    throw new ServiceException("唯一识别字段未匹配到 Excel 抬头：" + detail.getSourceField());
                }
                continue;
            }
            detail.setMappingIndex(mappingIndex);
            String mappingKey = mappingIndex.toString();
            if (isVertical && isVerticalCostItemDetail(detail)) {
                for (JSONObject rowData : successList) {
                    Object rawValue = rowData.get(mappingKey);
                    String resolvedValue = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
                    setPreparedValue(rowData, detail, resolvedValue);
                    if (!CharSequenceUtil.equals(detail.getSourceDetailField(), resolvedValue)) {
                        continue;
                    }
                    prepareVerticalAmountValue(cfgImportDetailList, ACTUAL_AMOUNT_FIELD, detail, rowData, headMap);
                    prepareVerticalAmountValue(cfgImportDetailList, ESTIMATED_AMOUNT_FIELD, detail, rowData, headMap);
                }
                continue;
            }
            for (JSONObject rowData : successList) {
                Object rawValue = rowData.get(mappingKey);
                String resolvedValue = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
                String cleanedValue = cleanFieldValue(resolvedValue, detail, rowData, headMap);
                setPreparedValue(rowData, detail, cleanedValue);
            }
        }
    }

    private boolean isVerticalCostItemDetail(CfgLogisticsCostImportDetailEntity detail) {
        return CharSequenceUtil.equals(detail.getTargetField(), COST_ITEM_FIELD)
                && CharSequenceUtil.isNotBlank(detail.getSourceDetailField());
    }

    private boolean isVerticalAmountDetail(CfgLogisticsCostImportDetailEntity detail) {
        return CharSequenceUtil.equals(detail.getTargetField(), ACTUAL_AMOUNT_FIELD)
                || CharSequenceUtil.equals(detail.getTargetField(), ESTIMATED_AMOUNT_FIELD);
    }

    private void prepareVerticalAmountValue(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                            String amountTargetField,
                                            CfgLogisticsCostImportDetailEntity costItemDetail,
                                            JSONObject rowData,
                                            Map<Integer, String> headMap) {
        Optional<CfgLogisticsCostImportDetailEntity> amountDetailOpt = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), amountTargetField))
                .findFirst();
        if (!amountDetailOpt.isPresent()) {
            return;
        }
        CfgLogisticsCostImportDetailEntity amountDetail = amountDetailOpt.get();
        Integer mappingIndex = amountDetail.getMappingIndex();
        if (ObjectUtil.isNull(mappingIndex)) {
            if (CharSequenceUtil.isBlank(amountDetail.getSourceField())) {
                return;
            }
            mappingIndex = getMapKey(headMap, amountDetail.getSourceField());
            if (ObjectUtil.isNull(mappingIndex)) {
                return;
            }
            amountDetail.setMappingIndex(mappingIndex);
        }
        Object rawValue = rowData.get(mappingIndex.toString());
        String resolvedValue = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
        String cleanedValue = cleanFieldValue(resolvedValue, costItemDetail, rowData, headMap);
        setPreparedValue(rowData, amountDetail, cleanedValue);
    }

    private String cleanFieldValue(String value,
                                   CfgLogisticsCostImportDetailEntity detail,
                                   JSONObject rowData,
                                   Map<Integer, String> headMap) {
        String result = ObjectUtil.isEmpty(value) ? "" : String.valueOf(value);
        for (com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule : getSortedEtlRuleList(detail)) {
            String type = rule.getType();
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), type)) {
                result = applyReplaceRule(result, rule);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(), type)) {
                result = applySubstringRule(result, rule);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode(), type)) {
                result = toSignedNumberText(result, false);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_NEGATIVE.getCode(), type)) {
                result = toSignedNumberText(result, true);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(), type)) {
                result = applyFillEmptyRule(result, rule, rowData, headMap);
                continue;
            }
            log.error("不支持的字段清洗规则类型：{}", type);
        }
        return result;
    }

    private List<com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO.EtlRuleDTO> getSortedEtlRuleList(CfgLogisticsCostImportDetailEntity detail) {
        List<com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList = detail.getEtlRuleList();
        if (CollUtil.isEmpty(ruleList)) {
            ruleList = CfgLogisticsCostImportEtlRuleHelper.parseStorage(detail.getEtlRuleListStorage());
            detail.setEtlRuleList(ruleList);
        }
        if (CollUtil.isEmpty(ruleList)) {
            return Collections.emptyList();
        }
        return ruleList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(rule -> Optional.ofNullable(rule.getIndex()).orElse(0)))
                .collect(Collectors.toList());
    }

    private String applyReplaceRule(String value, com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule) {
        if (CharSequenceUtil.isBlank(rule.getSourceText())) {
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_EMPTY.getCode(), rule.getMode())) {
            return value.replace(rule.getSourceText(), "");
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), rule.getMode())) {
            return value.replace(rule.getSourceText(), rule.getTargetText() == null ? "" : rule.getTargetText());
        }
        return value;
    }

    private String applySubstringRule(String value, com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }
        String mode = rule.getMode();
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_SYMBOL.getCode(), mode)) {
            String symbol = rule.getSymbol();
            if (CharSequenceUtil.isBlank(symbol) || !value.contains(symbol)) {
                return value;
            }
            int index = value.indexOf(symbol);
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.BEFORE.getCode(), rule.getSymbolPosition())) {
                return value.substring(0, index);
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.AFTER.getCode(), rule.getSymbolPosition())) {
                return value.substring(index + symbol.length());
            }
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_ORDER.getCode(), mode)) {
            Integer length = rule.getLength();
            if (ObjectUtil.isNull(length) || length <= 0) {
                return value;
            }
            int safeLength = Math.min(length, value.length());
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlOrderDirectionEnum.RIGHT.getCode(), rule.getOrderDirection())) {
                return value.substring(value.length() - safeLength);
            }
            return value.substring(0, safeLength);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.CHINESE.getCode(), mode)) {
            return value.replaceAll("[^\\u4e00-\\u9fa5]", "");
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.ENGLISH.getCode(), mode)) {
            return value.replaceAll("[^A-Za-z]", "");
        }
        return value;
    }

    private String applyFillEmptyRule(String value,
                                      com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule,
                                      JSONObject rowData,
                                      Map<Integer, String> headMap) {
        if (CharSequenceUtil.isNotBlank(value)) {
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlFillModeEnum.CUSTOM.getCode(), rule.getMode())) {
            return rule.getFillValue() == null ? "" : rule.getFillValue();
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlFillModeEnum.FIELD.getCode(), rule.getMode())) {
            Integer fieldIndex = getMapKey(headMap, rule.getSourceField());
            if (ObjectUtil.isNull(fieldIndex)) {
                return value;
            }
            Object fieldValue = rowData.get(fieldIndex.toString());
            return ObjectUtil.isEmpty(fieldValue) ? "" : String.valueOf(fieldValue);
        }
        return value;
    }

    private String toSignedNumberText(String value, boolean negative) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }
        try {
            BigDecimal number = new BigDecimal(value.trim());
            BigDecimal signedNumber = negative ? number.abs().negate() : number.abs();
            return signedNumber.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private void setPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity detail, String value) {
        String text = ObjectUtil.isEmpty(value) ? "" : value;
        if (ObjectUtil.isNotNull(detail.getMappingIndex())) {
            rowData.set(detail.getMappingIndex().toString(), text);
        }
        if (CharSequenceUtil.isNotBlank(detail.getTargetField())) {
            rowData.set(detail.getTargetField(), text);
        }
    }

    private String getPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity detail) {
        Object value = ObjectUtil.isNotNull(detail.getMappingIndex()) ? rowData.get(detail.getMappingIndex().toString()) : null;
        if (ObjectUtil.isEmpty(value) && CharSequenceUtil.isNotBlank(detail.getTargetField())) {
            value = rowData.get(detail.getTargetField());
        }
        if (ObjectUtil.isEmpty(value)) {
            return "";
        }
        return String.valueOf(value);
    }

    private void standardizeImportRowWeightValues(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                  List<JSONObject> successList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        Optional<CfgLogisticsCostImportDetailEntity> unitDetailOpt = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), "logisticsWeightUnit"))
                .findFirst();
        if (!unitDetailOpt.isPresent()) {
            return;
        }
        CfgLogisticsCostImportDetailEntity unitDetail = unitDetailOpt.get();
        List<CfgLogisticsCostImportDetailEntity> weightDetails = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), "billingWeightLogistics")
                        || CharSequenceUtil.equals(detail.getTargetField(), "thirdActualWeight"))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(weightDetails)) {
            return;
        }
        for (JSONObject rowData : successList) {
            String unit = getPreparedValue(rowData, unitDetail);
            if (!"g".equalsIgnoreCase(unit)) {
                continue;
            }
            Map<CfgLogisticsCostImportDetailEntity, String> convertedWeightMap = new HashMap<>();
            boolean convertFailed = false;
            for (CfgLogisticsCostImportDetailEntity weightDetail : weightDetails) {
                String weightValue = getPreparedValue(rowData, weightDetail);
                if (CharSequenceUtil.isBlank(weightValue)) {
                    continue;
                }
                try {
                    BigDecimal kgValue = new BigDecimal(weightValue).divide(new BigDecimal("1000"), 4, RoundingMode.DOWN);
                    convertedWeightMap.put(weightDetail, kgValue.stripTrailingZeros().toPlainString());
                } catch (NumberFormatException e) {
                    log.warn("物流商重量值无法转换为 KG：{}", weightValue);
                    convertFailed = true;
                }
            }
            if (convertFailed) {
                continue;
            }
            convertedWeightMap.forEach((weightDetail, value) -> setPreparedValue(rowData, weightDetail, value));
            setPreparedValue(rowData, unitDetail, "kg");
        }
    }

    private void prepareCleanFileHeaders(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                         List<String> headList,
                                         Map<Integer, String> headMap) {
        List<CfgLogisticsCostImportDetailEntity> virtualDetails = cfgImportDetailList.stream()
                .filter(this::isDefaultOnlyVirtualDetail)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(virtualDetails)) {
            return;
        }
        if (isCleanFileVirtualHeadersPrepared(virtualDetails, headMap)) {
            return;
        }
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        if (ObjectUtil.isNull(matchIndex) || ObjectUtil.isNull(errorIndex)) {
            return;
        }
        while (headList.size() > matchIndex) {
            headList.remove(headList.size() - 1);
        }
        headMap.remove(matchIndex);
        headMap.remove(errorIndex);
        int nextIndex = matchIndex;
        for (CfgLogisticsCostImportDetailEntity detail : virtualDetails) {
            String headerName = CharSequenceUtil.blankToDefault(detail.getTargetFieldName(), detail.getTargetField());
            if (CharSequenceUtil.isBlank(headerName)) {
                continue;
            }
            detail.setMappingIndex(nextIndex);
            headList.add(headerName);
            headMap.put(nextIndex, headerName);
            nextIndex++;
        }
        headList.add(MATCH_FIELD);
        headMap.put(nextIndex, MATCH_FIELD);
        nextIndex++;
        headList.add(ERROR_MSG);
        headMap.put(nextIndex, ERROR_MSG);
    }

    private boolean isCleanFileVirtualHeadersPrepared(List<CfgLogisticsCostImportDetailEntity> virtualDetails,
                                                      Map<Integer, String> headMap) {
        return virtualDetails.stream().allMatch(detail -> {
            if (ObjectUtil.isNull(detail.getMappingIndex())) {
                return false;
            }
            String headerName = CharSequenceUtil.blankToDefault(detail.getTargetFieldName(), detail.getTargetField());
            return CharSequenceUtil.equals(headMap.get(detail.getMappingIndex()), headerName);
        });
    }

    private void projectCleanFileRows(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                      List<JSONObject> matchImportList) {
        if (CollUtil.isEmpty(matchImportList)) {
            return;
        }
        for (JSONObject rowData : matchImportList) {
            for (CfgLogisticsCostImportDetailEntity detail : cfgImportDetailList) {
                String preparedValue = getPreparedValue(rowData, detail);
                if (CharSequenceUtil.isBlank(detail.getSourceField())) {
                    if (isDefaultOnlyVirtualDetail(detail) && ObjectUtil.isNotNull(detail.getMappingIndex())) {
                        rowData.set(detail.getMappingIndex().toString(), preparedValue);
                    }
                    continue;
                }
                if (ObjectUtil.isNotNull(detail.getMappingIndex())) {
                    rowData.set(detail.getMappingIndex().toString(), preparedValue);
                }
            }
        }
    }

    private boolean isDefaultOnlyVirtualDetail(CfgLogisticsCostImportDetailEntity detail) {
        return CharSequenceUtil.isBlank(detail.getSourceField()) && CharSequenceUtil.isNotBlank(detail.getDefaultValue());
    }

    private void fillOne(ImportHistoryRecordDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<ImportHistoryRecordDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        List<String> operationUserIdList = list.stream().map(ImportHistoryRecordDTO.ListDTO::getOperationUserId).distinct().collect(Collectors.toList());
        List<FindUserDTO> findUserList = sysUserFeign.getUserListByUserIds(operationUserIdList);
        Map<String, String> userMap = findUserList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
        // 属性赋值
        for(ImportHistoryRecordDTO.ListDTO data : list) {
            //对账月份
            if (CharSequenceUtil.isNotBlank(data.getReconciliationMonth())) {
                String reconciliationMonthStr = LocalDate.parse(data.getReconciliationMonth() + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .format(DateTimeFormatter.ofPattern("yyyy年MM月"));
                data.setReconciliationMonthStr(reconciliationMonthStr);
            }
            //处理状态名称
            data.setStatusName(ImportHistoryRecordStatusEnum.getName(data.getStatus()));

            //操作人名称
            data.setOperationUserName(userMap.get(data.getOperationUserId()));

            //来源
            data.setTypeName(ImportHistoryRecordTypeEnum.getName(data.getType()));
        }
    }

    /**
     * @description: 根据value获取对应的key
     * @author Will
     * @date: 2024/5/11 11:41
     * @param headMap
     * @param targetValue
     * @return String
     */
    private Integer getMapKey(Map<Integer, String> headMap, String targetValue) {
        Integer resultKey = null;

        // 预处理目标值：去除首尾空格、换行等空白字符
        String cleanedTarget = targetValue.trim();

        for (Integer key : headMap.keySet()) {
            String value = headMap.get(key);

            // 如果value为null，跳过避免空指针异常
            if (value == null) {
                continue;
            }

            // 去除当前value的首尾空格、换行等空白字符
            String cleanedValue = value.trim().replaceAll("\n"," ");



            // 精准匹配（完全相等）
            if (cleanedValue.equals(cleanedTarget)) {
                resultKey = key;
                break;  // 找到第一个匹配的就返回
            }
        }
        return resultKey;
    }

    /**
     * @description: 根据文件URL查询导入记录
     * @author Will
     * @date: 2024/5/11 14:24
     * @param fileUrl
     * @return com.erp.model.tms.entity.ImportHistoryRecordEntity
     */
    private ImportHistoryRecordEntity getByFileUrl(String fileUrl,String sheetName) {
        if (CharSequenceUtil.isBlank(fileUrl) || CharSequenceUtil.isBlank(sheetName) ) {
            throw new ServiceException("文件URL、sheet页名称不能为空");
        }
        return this.lambdaQuery().eq(ImportHistoryRecordEntity::getFileUrl, fileUrl).eq(ImportHistoryRecordEntity::getSheetName,sheetName).last("limit 1").one();
    }
}
