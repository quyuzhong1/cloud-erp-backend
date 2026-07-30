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
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MessageUtils;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.excel.ImportHistoryRecordExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.server.tms.util.CfgLogisticsCostImportEtlRuleHelper;
import com.erp.server.tms.util.LogisticsBillPlatformCodeUtil;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.ImportHistoryRecordExcelListener;
import com.erp.server.tms.mapper.ImportHistoryRecordMapper;
import com.erp.server.tms.service.*;
import com.erp.server.tms.service.support.LogisticsOrderWeightSupport;
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
import java.util.regex.Pattern;
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
    private static final String LOGISTICS_WEIGHT_UNIT_FIELD = "logisticsWeightUnit";
    private static final String BILLING_WEIGHT_LOGISTICS_FIELD = "billingWeightLogistics";
    private static final String THIRD_ACTUAL_WEIGHT_FIELD = "thirdActualWeight";

    private static final Pattern NON_CHINESE_PATTERN = Pattern.compile("[^\\u4e00-\\u9fa5]");
    private static final Pattern NON_ENGLISH_PATTERN = Pattern.compile("[^A-Za-z]");

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
    private LogisticsOrderWeightSupport logisticsOrderWeightSupport;
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
        validateDuplicateImportConfig(cfgLogisticsCostImportList);
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
        // 费用项统一查尾程发货；主单 type 在 handleImportData 按匹配到的费用单 entity 解析。
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillByUniqueKey(paramMap);
        List<String> logisticsBillDetailIdList = CollUtil.isEmpty(logisticsBillVos) ? Collections.emptyList()
                : logisticsBillVos.stream()
                .map(LogisticsBillDTO.LogisticsBillVo::getDetailId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        // listLogisticsBillByUniqueKey 未返回 logisticsBillCostId，须用已查到的费用单 id 预加载存量明细。
        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if (CollUtil.isNotEmpty(logisticsBillCostList)) {
            List<String> logisticsBillCostIdList = logisticsBillCostList.stream()
                    .map(LogisticsBillCostEntity::getId)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            logisticsBillCostIdList.forEach(id -> mainIdListMap.put(id, Collections.emptyList()));
            if (CollUtil.isNotEmpty(logisticsBillCostIdList)) {
                List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostIdList);
                if (CollUtil.isNotEmpty(listByMainIdList)) {
                    mainIdListMap.putAll(listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId)));
                }
            }
        }
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

    private ImportHistoryRecordDTO.ImportGroupContextDTO buildImportGroupContext(List<JSONObject> successList,
                                                                                 List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                                                                 ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult,
                                                                                 CfgLogisticsCostImportEntity costImportEntity) {
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> billVoIndex = buildLogisticsBillVoIndex(
                preQueryResult.getLogisticsBillVoList(), uniqueKeyList, costImportEntity);
        Map<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>> groupRowMap = new LinkedHashMap<>();
        for (JSONObject row : successList) {
            List<LogisticsBillDTO.LogisticsBillVo> matchedBillList = resolveMatchedLogisticsBillVoList(uniqueKeyList,
                    row, billVoIndex, costImportEntity);
            String groupKey = buildImportRowGroupKey(row, uniqueKeyList, matchedBillList);
            groupRowMap.computeIfAbsent(groupKey, key -> new ArrayList<>())
                    .add(new ImportHistoryRecordDTO.ImportRowContextDTO(row, matchedBillList));
        }
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> groupMatchedBillMap = new LinkedHashMap<>();
        groupRowMap.forEach((groupKey, rowContextList) -> {
            List<String> ignoreErrorList = new ArrayList<>();
            groupMatchedBillMap.put(groupKey, resolveGroupMatchedLogisticsBillVoList(rowContextList, ignoreErrorList));
        });
        return new ImportHistoryRecordDTO.ImportGroupContextDTO(groupRowMap, groupMatchedBillMap);
    }

    /**
     * 构建模板导入分组键。
     * <p>普通识别字段按 Excel 识别单号分组；platformCode 允许一个物流单保存多个平台单号，
     * 因此按匹配到的物流单集合分组，避免多个平台单号分多组后覆盖同一物流费用单。</p>
     */
    private String buildImportRowGroupKey(JSONObject row,
                                          List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                          List<LogisticsBillDTO.LogisticsBillVo> matchedBillList) {
        String uniqueGroupKey = buildImportUniqueGroupKey(row, uniqueKeyList);
        if (!containsPlatformCodeUniqueKey(uniqueKeyList)) {
            return uniqueGroupKey;
        }
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

    private Map<String, List<LogisticsBillDTO.LogisticsBillVo>> buildLogisticsBillVoIndex(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos,
                                                                                          List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                                                                          CfgLogisticsCostImportEntity costImportEntity) {
        Map<String, List<LogisticsBillDTO.LogisticsBillVo>> billVoIndex = new HashMap<>();
        if (CollUtil.isEmpty(logisticsBillVos) || CollUtil.isEmpty(uniqueKeyList)) {
            return billVoIndex;
        }
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVos) {
            if (!matchesCostImportConfig(costImportEntity, logisticsBillVo)) {
                continue;
            }
            for (CfgLogisticsCostImportDetailEntity uniqueKey : uniqueKeyList) {
                String field = uniqueKey.getTargetField();
                if (CharSequenceUtil.isBlank(field)) {
                    continue;
                }
                Object billValue = BeanUtil.getFieldValue(logisticsBillVo, field);
                if (ObjectUtil.isNull(billValue)) {
                    continue;
                }
                if (CharSequenceUtil.equals(PLATFORM_CODE_FIELD, field)) {
                    LogisticsBillPlatformCodeUtil.splitPlatformCodes(String.valueOf(billValue))
                            .forEach(platformCode -> putLogisticsBillVoIndex(billVoIndex, field, platformCode, logisticsBillVo));
                    continue;
                }
                putLogisticsBillVoIndex(billVoIndex, field, String.valueOf(billValue), logisticsBillVo);
            }
        }
        return billVoIndex;
    }

    private void putLogisticsBillVoIndex(Map<String, List<LogisticsBillDTO.LogisticsBillVo>> billVoIndex,
                                         String field,
                                         String value,
                                         LogisticsBillDTO.LogisticsBillVo logisticsBillVo) {
        if (CharSequenceUtil.isBlank(field) || CharSequenceUtil.isBlank(value)) {
            return;
        }
        billVoIndex.computeIfAbsent(buildLogisticsBillVoIndexKey(field, value), key -> new ArrayList<>()).add(logisticsBillVo);
    }

    private String buildLogisticsBillVoIndexKey(String field, String value) {
        return field + ":" + value;
    }

    /**
     * 按识别单号和模板适用范围解析当前行可写入的物流单。
     * <p>该方法集中复用唯一键匹配和模板范围过滤，保证分组阶段与落库阶段使用同一套匹配规则。</p>
     */
    private List<LogisticsBillDTO.LogisticsBillVo> resolveMatchedLogisticsBillVoList(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList,
                                                                                     JSONObject successJson,
                                                                                     Map<String, List<LogisticsBillDTO.LogisticsBillVo>> billVoIndex,
                                                                                     CfgLogisticsCostImportEntity costImportEntity) {
        if (CollUtil.isEmpty(billVoIndex) || CollUtil.isEmpty(uniqueKeyList)) {
            return Collections.emptyList();
        }
        List<LogisticsBillDTO.LogisticsBillVo> candidates = Collections.emptyList();
        for (CfgLogisticsCostImportDetailEntity uniqueKey : uniqueKeyList) {
            String importValue = getPreparedValue(successJson, uniqueKey);
            if (CharSequenceUtil.isBlank(importValue)) {
                return Collections.emptyList();
            }
            List<LogisticsBillDTO.LogisticsBillVo> indexedList = billVoIndex.get(buildLogisticsBillVoIndexKey(uniqueKey.getTargetField(), importValue));
            if (CollUtil.isEmpty(indexedList)) {
                return Collections.emptyList();
            }
            if (CollUtil.isEmpty(candidates) || indexedList.size() < candidates.size()) {
                candidates = indexedList;
            }
        }
        if (CollUtil.isEmpty(candidates)) {
            return Collections.emptyList();
        }
        return distinctLogisticsBillVoList(candidates.stream()
                .filter(obj -> matchesUniqueKey(uniqueKeyList, successJson, obj))
                .filter(obj -> matchesCostImportConfig(costImportEntity, obj))
                .collect(Collectors.toList()));
    }

    /**
     * 解析同一导入分组命中的物流单集合。
     * <p>同一组内费用会被合计后统一处理，因此每行命中的物流单集合必须一致；不一致时直接报错，避免错误合并费用。</p>
     */
    private List<LogisticsBillDTO.LogisticsBillVo> resolveGroupMatchedLogisticsBillVoList(List<ImportHistoryRecordDTO.ImportRowContextDTO> rowContextList,
                                                                                          List<String> errorMsgList) {
        if (CollUtil.isEmpty(rowContextList)) {
            return Collections.emptyList();
        }
        List<LogisticsBillDTO.LogisticsBillVo> firstMatchedList = rowContextList.get(0).getMatchedBillList();
        if (firstMatchedList == null) {
            firstMatchedList = Collections.emptyList();
        }
        String firstBillSetKey = buildMatchedBillSetKey(firstMatchedList);
        for (int i = 1; i < rowContextList.size(); i++) {
            List<LogisticsBillDTO.LogisticsBillVo> currentMatchedList = rowContextList.get(i).getMatchedBillList();
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
        ImportHistoryRecordDTO.ImportGroupContextDTO importGroupContext = buildImportGroupContext(successList, uniqueKeyList, preQueryResult, costImportEntity);
        Map<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>> map = importGroupContext.getGroupRowMap();
        // 多物流单分摊依赖出库明细/SKU毛重，须在提交线程池前批量预加载；子线程内 Feign 既会按分组放大远程调用，也无法透传 RequestContext。
        preloadOrderWeightData(importGroupContext.getGroupMatchedBillMap(), preQueryResult);
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        List<java.util.concurrent.Future<Void>> futures = new ArrayList<>();
        int batchSize = 100;
        List<Map.Entry<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>>> entryList = new ArrayList<>(map.entrySet());
        for (int i = 0; i < entryList.size(); i += batchSize) {
            int end = Math.min(entryList.size(), i + batchSize);
            List<Map.Entry<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>>> batch = entryList.subList(i, end);
            futures.add(importHistoryRecordPool.submit(() -> {
                for (Map.Entry<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>> entry : batch) {
                    List<ImportHistoryRecordDTO.ImportRowContextDTO> value = entry.getValue();
                    JSONObject successJson = new JSONObject();
                    List<TmsCostDetailDTO.UpdateDTO> updateAllList = new ArrayList<>();
                    // 逐条处理每个jsonObject，分别校验和赋值
                    for (ImportHistoryRecordDTO.ImportRowContextDTO rowContext : value) {
                        JSONObject jsonObject = rowContext.getRow();
                        jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                        List<String> costErrorMsgList = new ArrayList<>();
                        List<TmsCostDetailDTO.UpdateDTO> updateList = rowFormatCost(successJson, jsonObject, preQueryResult.getCfgCostList(), cfgImportDetailList, headList, DictCostAttributionEnum.LAST_MILE_DELIVERY.getCode(), costErrorMsgList, currencyLookupMap, currencyRateMap);
                        if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                            jsonObject.set(matchIndex.toString(), MATCH_FAIL);
                            jsonObject.set(errorIndex.toString(), FieldValidUtil.getMsgSort(costErrorMsgList));
                            synchronized (matchImportList) { updateMatchResult(Collections.singletonList(jsonObject), matchIndex.toString(), errorIndex.toString(), costErrorMsgList, matchImportList); } ;
                            continue;
                        }
                        updateAllList.addAll(updateList);
                    }
                    List<ImportHistoryRecordDTO.ImportRowContextDTO> costSuccessContextList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.getRow().get(matchIndex.toString()))).collect(Collectors.toList());
                    List<JSONObject> costSuccessList = costSuccessContextList.stream().map(ImportHistoryRecordDTO.ImportRowContextDTO::getRow).collect(Collectors.toList());
                    if (CollUtil.isEmpty(costSuccessList)) {
                        continue;
                    }
                    List<String> mainErrorMsgList = new ArrayList<>();
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList, mainErrorMsgList, preQueryResult.getCfgCostList());
                    if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                        synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); }
                        continue;
                    }
                    try {
                        // 同一识别分组会先汇总费用，再把固定的物流单集合传入 handleImportData，由既有重量分摊逻辑处理一对多/多对多。
                        List<LogisticsBillDTO.LogisticsBillVo> matchedLogisticsBillVoList = resolveGroupMatchedLogisticsBillVoList(
                                costSuccessContextList, mainErrorMsgList);
                        // 分组物流单不一致时已写入错误，直接回填结果并跳过 handleImportData，避免再叠加“未找到对应物流单”的重复错误。
                        if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                            synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); }
                            continue;
                        }
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList,
                                preQueryResult.getMainIdListMap(), preQueryResult.getOrderWeightMap(), preQueryResult.getOrderWeightErrorMap(), matchedLogisticsBillVoList);
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
        ImportHistoryRecordDTO.ImportGroupContextDTO importGroupContext = buildImportGroupContext(successList, uniqueKeyList, preQueryResult, costImportEntity);
        Map<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>> map = importGroupContext.getGroupRowMap();
        // 多物流单分摊依赖出库明细/SKU毛重，须在提交线程池前批量预加载；子线程内 Feign 既会按分组放大远程调用，也无法透传 RequestContext。
        preloadOrderWeightData(importGroupContext.getGroupMatchedBillMap(), preQueryResult);
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        List<java.util.concurrent.Future<Void>> futures = new ArrayList<>();
        int batchSize = 100;
        List<Map.Entry<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>>> entryList = new ArrayList<>(map.entrySet());
        for (int i = 0; i < entryList.size(); i += batchSize) {
            int end = Math.min(entryList.size(), i + batchSize);
            List<Map.Entry<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>>> batch = entryList.subList(i, end);
            futures.add(importHistoryRecordPool.submit(() -> {
                for (Map.Entry<String, List<ImportHistoryRecordDTO.ImportRowContextDTO>> entry : batch) {
                    List<ImportHistoryRecordDTO.ImportRowContextDTO> value = entry.getValue();
                    JSONObject successJson = new JSONObject();
                    List<TmsCostDetailDTO.UpdateDTO> updateAllList = new ArrayList<>();
                    for (ImportHistoryRecordDTO.ImportRowContextDTO rowContext : value) {
                        JSONObject jsonObject = rowContext.getRow();
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
                    List<ImportHistoryRecordDTO.ImportRowContextDTO> costSuccessContextList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.getRow().get(matchIndex.toString()))).collect(Collectors.toList());
                    List<JSONObject> costSuccessList = costSuccessContextList.stream().map(ImportHistoryRecordDTO.ImportRowContextDTO::getRow).collect(Collectors.toList());
                    if (CollUtil.isEmpty(costSuccessList)) {
                        continue;
                    }
                    List<String> mainErrorMsgList = new ArrayList<>();
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList, mainErrorMsgList, preQueryResult.getCfgCostList());
                    if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                        synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); }
                        continue;
                    }
                    try {
                        // 同一识别分组会先汇总费用，再把固定的物流单集合传入 handleImportData，由既有重量分摊逻辑处理一对多/多对多。
                        List<LogisticsBillDTO.LogisticsBillVo> matchedLogisticsBillVoList = resolveGroupMatchedLogisticsBillVoList(
                                costSuccessContextList, mainErrorMsgList);
                        // 分组物流单不一致时已写入错误，直接回填结果并跳过 handleImportData，避免再叠加“未找到对应物流单”的重复错误。
                        if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                            synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); }
                            continue;
                        }
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList,
                                preQueryResult.getMainIdListMap(), preQueryResult.getOrderWeightMap(), preQueryResult.getOrderWeightErrorMap(), matchedLogisticsBillVoList);
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
     * 多物流单分摊才需要订单重量；在线程池处理前统一预加载，避免分组任务内重复 Feign 查询。
     */
    private void preloadOrderWeightData(Map<String, List<LogisticsBillDTO.LogisticsBillVo>> groupMatchedBillMap,
                                        ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult) {
        if (CollUtil.isEmpty(groupMatchedBillMap)) {
            return;
        }
        Map<String, LogisticsBillDTO.LogisticsBillVo> logisticsBillVoMap = new LinkedHashMap<>();
        for (List<LogisticsBillDTO.LogisticsBillVo> matchedBillList : groupMatchedBillMap.values()) {
            if (CollUtil.isEmpty(matchedBillList) || matchedBillList.size() <= 1) {
                continue;
            }
            matchedBillList.stream()
                    .filter(ObjectUtil::isNotNull)
                    .filter(vo -> CharSequenceUtil.isNotBlank(vo.getDetailId()))
                    .forEach(vo -> logisticsBillVoMap.putIfAbsent(vo.getDetailId(), vo));
        }
        if (CollUtil.isEmpty(logisticsBillVoMap)) {
            return;
        }
        fillOrderWeightPreQuery(new ArrayList<>(logisticsBillVoMap.values()), preQueryResult);
    }

    private void fillOrderWeightPreQuery(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                         ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult) {
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            return;
        }
        Map<String, BigDecimal> orderWeightMap = ObjectUtil.defaultIfNull(preQueryResult.getOrderWeightMap(), new HashMap<>());
        Map<String, List<String>> orderWeightErrorMap = ObjectUtil.defaultIfNull(preQueryResult.getOrderWeightErrorMap(), new HashMap<>());
        preQueryResult.setOrderWeightMap(orderWeightMap);
        preQueryResult.setOrderWeightErrorMap(orderWeightErrorMap);

        List<String> outstockIdList = logisticsBillVoList.stream()
                .map(LogisticsBillDTO.LogisticsBillVo::getOutstockId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        logisticsBillVoList.stream()
                .filter(vo -> CharSequenceUtil.isBlank(vo.getOutstockId()))
                .forEach(vo -> LogisticsOrderWeightSupport.addOrderWeightError(orderWeightErrorMap, vo.getDetailId(),
                        "无法获取上游出库单用于重量分摊"));
        if (CollUtil.isEmpty(outstockIdList)) {
            return;
        }

        List<SoOutstockDetailEntity> outstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class)
                .in(SoOutstockDetailEntity::getMainId, outstockIdList)
                .list();
        Map<String, List<SoOutstockDetailEntity>> outstockDetailMap = CollUtil.isEmpty(outstockDetailList)
                ? new HashMap<>()
                : outstockDetailList.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId));
        List<LogisticsBillDTO.LogisticsBillVo> validLogisticsBillVoList = logisticsBillVoList.stream()
                .filter(vo -> CharSequenceUtil.isNotBlank(vo.getOutstockId()) && CharSequenceUtil.isNotBlank(vo.getDetailId()))
                .collect(Collectors.toList());
        logisticsOrderWeightSupport.fillOrderWeightByOutstockDetails(
                validLogisticsBillVoList, outstockDetailMap, orderWeightMap, orderWeightErrorMap);
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
        List<LogisticsBillCostEntity> addedBillCostList = logisticsBillCostService.batchImportAdd(
                logisticsBillList, logisticsBillDetailList, logisticsBillCostAddList, processingType);

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

        List<LogisticsBillCostEntity> updatedBillCostList = logisticsBillCostService.batchImportUpdate(
                logisticsBillUpdateList, logisticsBillDetailUpdateList, logisticsBillCostUpdateList, processingType);

        //新增费用项
        List<TmsCostDetailDTO.AddDTO> costDetailAddList = importDataList.stream().flatMap(obj -> {
            List<TmsCostDetailDTO.AddDTO> list = obj.getAddCfgCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());

        //更新费用项
        List<TmsCostDetailDTO.UpdateDTO> costDetailUpdateList = importDataList.stream().flatMap(obj -> {
            List<TmsCostDetailDTO.UpdateDTO> list = obj.getUpdateCfgCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        // 复用本批已落库主单的 type/currency，按归属分组导入明细，避免重复查主单
        batchImportCostDetailsByAttribution(costDetailAddList, costDetailUpdateList, addedBillCostList, updatedBillCostList);

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

    /**
     * 按费用主单归属分组后导入费用明细，避免自发货/平台发货混批时写死归属。
     * 优先复用本批主单导入结果中的 type/currency，仅对缺失主单补查一次。
     */
    private void batchImportCostDetailsByAttribution(List<TmsCostDetailDTO.AddDTO> costDetailAddList,
                                                     List<TmsCostDetailDTO.UpdateDTO> costDetailUpdateList,
                                                     List<LogisticsBillCostEntity> addedBillCostList,
                                                     List<LogisticsBillCostEntity> updatedBillCostList) {
        if (CollUtil.isEmpty(costDetailAddList) && CollUtil.isEmpty(costDetailUpdateList)) {
            return;
        }
        if (hasBlankMainId(costDetailAddList, costDetailUpdateList)) {
            throw new ServiceException("主表id不能为空");
        }

        Set<String> mainIds = new HashSet<>();
        if (CollUtil.isNotEmpty(costDetailAddList)) {
            costDetailAddList.stream().map(TmsCostDetailDTO.AddDTO::getMainId).forEach(mainIds::add);
        }
        if (CollUtil.isNotEmpty(costDetailUpdateList)) {
            costDetailUpdateList.stream().map(TmsCostDetailDTO.UpdateDTO::getMainId).forEach(mainIds::add);
        }

        Map<String, LogisticsBillCostEntity> billCostMap = new HashMap<>();
        if (CollUtil.isNotEmpty(addedBillCostList)) {
            addedBillCostList.forEach(entity -> billCostMap.put(entity.getId(), entity));
        }
        if (CollUtil.isNotEmpty(updatedBillCostList)) {
            updatedBillCostList.forEach(entity -> billCostMap.put(entity.getId(), entity));
        }
        Set<String> missingMainIds = mainIds.stream().filter(id -> !billCostMap.containsKey(id)).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(missingMainIds)) {
            logisticsBillCostService.listByIds(missingMainIds).forEach(entity -> billCostMap.put(entity.getId(), entity));
        }
        // 内存主单 type/currency 不完整时回落查库，避免更新 DTO 未带币种导致复用失败
        Set<String> incompleteMainIds = mainIds.stream()
                .filter(id -> {
                    LogisticsBillCostEntity entity = billCostMap.get(id);
                    return entity != null
                            && (CharSequenceUtil.isBlank(entity.getType()) || CharSequenceUtil.isBlank(entity.getCurrency()));
                })
                .collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(incompleteMainIds)) {
            logisticsBillCostService.listByIds(incompleteMainIds).forEach(entity -> billCostMap.put(entity.getId(), entity));
        }

        Map<String, DictCostAttributionEnum> mainIdAttributionMap = new HashMap<>(mainIds.size());
        Map<String, String> currencyMap = new HashMap<>(mainIds.size());
        for (String mainId : mainIds) {
            LogisticsBillCostEntity billCost = billCostMap.get(mainId);
            if (billCost == null) {
                throw new ServiceException("未找到费用单据归属类型");
            }
            DictCostAttributionEnum attribution = DictCostAttributionEnum.getByCode(billCost.getType());
            if (attribution == null) {
                throw new ServiceException("费用归属类型不支持");
            }
            if (CharSequenceUtil.isBlank(billCost.getCurrency())) {
                throw new ServiceException(CharSequenceUtil.format("未找到【{}】数据币别", attribution.getName()));
            }
            mainIdAttributionMap.put(mainId, attribution);
            currencyMap.put(mainId, billCost.getCurrency());
        }

        if (CollUtil.isNotEmpty(costDetailAddList)) {
            Map<DictCostAttributionEnum, List<TmsCostDetailDTO.AddDTO>> groupMap = costDetailAddList.stream()
                    .collect(Collectors.groupingBy(dto -> mainIdAttributionMap.get(dto.getMainId())));
            groupMap.forEach((attribution, list) ->
                    tmsCostDetailService.batchImportAdd(list, attribution, currencyMap));
        }
        if (CollUtil.isNotEmpty(costDetailUpdateList)) {
            Map<DictCostAttributionEnum, List<TmsCostDetailDTO.UpdateDTO>> groupMap = costDetailUpdateList.stream()
                    .collect(Collectors.groupingBy(dto -> mainIdAttributionMap.get(dto.getMainId())));
            groupMap.forEach((attribution, list) ->
                    tmsCostDetailService.batchImportUpdate(list, attribution, Boolean.TRUE, currencyMap));
        }
    }

    private boolean hasBlankMainId(List<TmsCostDetailDTO.AddDTO> costDetailAddList,
                                   List<TmsCostDetailDTO.UpdateDTO> costDetailUpdateList) {
        if (CollUtil.isNotEmpty(costDetailAddList)
                && costDetailAddList.stream().anyMatch(dto -> CharSequenceUtil.isBlank(dto.getMainId()))) {
            return true;
        }
        return CollUtil.isNotEmpty(costDetailUpdateList)
                && costDetailUpdateList.stream().anyMatch(dto -> CharSequenceUtil.isBlank(dto.getMainId()));
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
     * 合并相同费用项、费用类型的费用。
     * <p>同一识别分组内：同费用项同类型仅允许一个币种（同币种金额合计）；出现多个币种直接报错。</p>
     */
    private List<TmsCostDetailDTO.UpdateDTO> mergeTmsCostDetail(List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                               List<String> errorMsgList,
                                                               List<TmsCfgCostEntity> cfgCostList) {
        if (CollUtil.isEmpty(updateList)) {
            return Collections.emptyList();
        }
        Map<String, String> costNameMap = CollUtil.isEmpty(cfgCostList) ? Collections.emptyMap()
                : cfgCostList.stream().collect(Collectors.toMap(TmsCfgCostEntity::getId, TmsCfgCostEntity::getCostName, (a, b) -> a));
        List<TmsCostDetailDTO.UpdateDTO> mergeList = new ArrayList<>();
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> map = updateList.stream()
                .collect(Collectors.groupingBy(obj -> obj.getCfgCostId() + "_" + CharSequenceUtil.blankToDefault(obj.getType(), "")));
        for (Map.Entry<String, List<TmsCostDetailDTO.UpdateDTO>> entry : map.entrySet()) {
            List<TmsCostDetailDTO.UpdateDTO> value = entry.getValue();
            Set<String> currencySet = value.stream()
                    .map(obj -> CharSequenceUtil.blankToDefault(obj.getCurrency(), ""))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (currencySet.size() > 1) {
                String costName = costNameMap.getOrDefault(value.get(0).getCfgCostId(), value.get(0).getCfgCostId());
                errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_SAME_ITEM_MULTI_CURRENCY, costName));
                continue;
            }
            TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
            BeanUtil.copyProperties(value.get(0), updateDTO);
            BigDecimal amount = value.stream().map(TmsCostDetailDTO.UpdateDTO::getCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            updateDTO.setCostValue(amount);
            // 本位币在落库时由 handleData 按原币×汇率重算；合并阶段先清空，避免沿用首行旧值
            updateDTO.setCostValueLocalCurrency(null);
            updateDTO.setLocalCurrency(null);
            mergeList.add(updateDTO);
        }
        if (CollUtil.isNotEmpty(errorMsgList)) {
            return Collections.emptyList();
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
     * @param mainIdListMap        预查费用明细，避免循环内逐单查库
     * @param errorMsgList       校验失败时追加错误文案，供匹配结果导出
     */
    private void appendImportConfirmAmountError(ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                String logisticsCostId,
                                                List<TmsCostDetailDTO.UpdateDTO> currentUpdateList,
                                                Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                                List<String> errorMsgList) {
        if (!CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),
                importDTO.getProcessingType())) {
            return;
        }
        String confirmMsg = logisticsBillCostService.validateImportConfirmAmountMsg(
                logisticsCostId, currentUpdateList, ReconciliationStatusEnum.CONFIRMED.getCode(), mainIdListMap);
        if (CharSequenceUtil.isNotBlank(confirmMsg)) {
            errorMsgList.add(confirmMsg);
        }
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
        //币别赋值（Excel未填且配置无默认值时直接报错，不再默认CNY）
        if (CharSequenceUtil.isBlank(currency)) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_REQUIRED));
        } else {
            String normalizeCurrency = normalizeCurrencyByDict(currency, currencyLookupMap);
            if (CharSequenceUtil.isBlank(normalizeCurrency)) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_NOT_EXIST));
            } else {
                currency = normalizeCurrency;
            }
            //币别汇率
            BigDecimal rate = currencyRateMap.get(currency);
            if (ObjectUtil.isNull(rate)) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_RATE_NOT_EXIST));
            }
        }

        //费用数据
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
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
                                                            String costAttribution, List<String> errorMsgList,
                                                            Map<String, String> currencyLookupMap, Map<String, BigDecimal> currencyRateMap) {
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        //查询币别
        String currency = cfgImportDetailList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getTargetField(), "currency"))
                .map(detail -> getPreparedValue(jsonObject, detail))
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst().orElse("");
        //币别赋值（Excel未填且配置无默认值时直接报错，不再默认CNY）
        if (CharSequenceUtil.isBlank(currency)) {
            errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_REQUIRED));
        } else {
            String normalizeCurrency = normalizeCurrencyByDict(currency, currencyLookupMap);
            if (CharSequenceUtil.isBlank(normalizeCurrency)) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_NOT_EXIST));
            } else {
                currency = normalizeCurrency;
            }
            //币别汇率
            BigDecimal rate = currencyRateMap.get(currency);
            if (ObjectUtil.isNull(rate)) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_RATE_NOT_EXIST));
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
     * 解析可选数值字段；空白返回 null，格式错误写入 errorMsgList 供行级匹配结果导出。
     */
    private BigDecimal parseOptionalDecimalField(String rawValue, String fieldLabel, List<String> errorMsgList) {
        if (CharSequenceUtil.isBlank(rawValue)) {
            return null;
        }
        try {
            String withoutCommas = rawValue.trim().replaceAll(",", "");
            return new BigDecimal(withoutCommas);
        } catch (NumberFormatException e) {
            errorMsgList.add(fieldLabel + "格式不正确");
            return null;
        }
    }

    /**
     * 费用金额转换，支持绝对值配置。
     */
    private BigDecimal toCostValue(String amount, Boolean isAbsoluteValue) {
        BigDecimal value = new BigDecimal(amount);
        if (Boolean.TRUE.equals(isAbsoluteValue)) {
            value = value.abs();
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 新增或更新数据
     * @author will
     * @date 2026/1/28 20:34
     * @param successJson
     * @param updateList
     * @param logisticsBillCostList
     * @param cfgCostList
     * @param importDTO
     * @param costImportEntity
     * @param errorMsgList
     * @return void
     */
    /**
     * 按已解析的物流单集合生成导入数据。
     * <p>matchedLogisticsBillVos 由分组阶段传入，用于固定一对多/多对多分摊范围。</p>
     */
    private LogisticsBillCostDTO.ImportDataDTO handleImportData(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList , JSONObject successJson, List<TmsCostDetailDTO.UpdateDTO> updateList, List<LogisticsBillCostEntity> logisticsBillCostList,
                                                                List<TmsCfgCostEntity> cfgCostList, ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                                CfgLogisticsCostImportEntity costImportEntity, List<String> errorMsgList, Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                                                Map<String, BigDecimal> orderWeightMap, Map<String, List<String>> orderWeightErrorMap,
                                                                List<LogisticsBillDTO.LogisticsBillVo> matchedLogisticsBillVos) {

        ImportHistoryRecordExcelDTO excelDTO = BeanUtil.toBean(successJson, ImportHistoryRecordExcelDTO.class);

        //需要导入或更新的物流费用数据
        LogisticsBillCostDTO.ImportDataDTO  importDataDTO= new LogisticsBillCostDTO.ImportDataDTO();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
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
        // 分组阶段已解析物流单集合，避免 platformCode 多行合并后被最后一行平台单号重新缩窄匹配范围。
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList = ObjectUtil.defaultIfNull(matchedLogisticsBillVos, Collections.emptyList());

        // IMPORT_ADD_NEW 已下线：按新单无法区分自发货/尾程归属，配置入口已禁用；未匹配到物流单直接报错。
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
            List<LogisticsBillCostDTO.AddDTO> addBillCostList = new ArrayList<>();
            List<TmsCostDetailDTO.AddDTO> addCfgCostList = new ArrayList<>();
            List<LogisticsBillCostDTO.UpdateDTO> updateBillCostList = new ArrayList<>();
            List<TmsCostDetailDTO.UpdateDTO> updateCfgCostList = new ArrayList<>();
            String lastImportType = "";
            // 按识别单号：同一识别号可跨物流商命中多张单，分摊与落库须按费用状态二次筛选，不能与 identify_no_supplier 共用整组分摊逻辑。
            if (!shouldFilterByCostImportPlatform(costImportEntity)) {
                List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
                List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();
                splitIdentifyNoProcessBills(logisticsBillVoList, logisticsBillCostList, excelDTO.getPayType(),
                        importDTO.getReconciliationMonth(), costImportEntity, updateBillList, addOldBillList);
                if (CollUtil.isEmpty(updateBillList) && CollUtil.isEmpty(addOldBillList)) {
                    errorMsgList.add("未找到可更新的待确认费用单或可新增的已确认费用单");
                    return importDataDTO;
                }
                // 待确认批次与 ADD_OLD 多单批次均按出库重量分摊；仅单张物流单时整行金额原样落库。
                if (CollUtil.isNotEmpty(updateBillList)) {
                    lastImportType = processImportBillVoGroup(updateBillList, true, updateList, confirmTime, excelDTO, importDTO,
                            costImportEntity, logisticsBillCostList, cfgCostList, errorMsgList, mainIdListMap, orderWeightMap,
                            orderWeightErrorMap, addBillCostList, addCfgCostList, updateBillCostList, updateCfgCostList);
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        return importDataDTO;
                    }
                }
                if (CollUtil.isNotEmpty(addOldBillList)) {
                    String addOldImportType = processImportBillVoGroup(addOldBillList, addOldBillList.size() > 1, updateList, confirmTime, excelDTO, importDTO,
                            costImportEntity, logisticsBillCostList, cfgCostList, errorMsgList, mainIdListMap, orderWeightMap,
                            orderWeightErrorMap, addBillCostList, addCfgCostList, updateBillCostList, updateCfgCostList);
                    if (CollectionUtils.isNotEmpty(errorMsgList)) {
                        return importDataDTO;
                    }
                    // 仅存在按原单新增时，importType 取 ADD_OLD；与更新并存时保留 UPDATE（落库走 add/update 列表，不依赖单一 importType）。
                    if (CharSequenceUtil.isBlank(lastImportType)) {
                        lastImportType = addOldImportType;
                    }
                }
            } else {
                // 按识别单号+物流商：命中范围已收窄，沿用整组分摊与单循环处理。
                lastImportType = processImportBillVoGroup(logisticsBillVoList, logisticsBillVoList.size() > 1, updateList, confirmTime, excelDTO, importDTO,
                        costImportEntity, logisticsBillCostList, cfgCostList, errorMsgList, mainIdListMap, orderWeightMap,
                        orderWeightErrorMap, addBillCostList, addCfgCostList, updateBillCostList, updateCfgCostList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    return importDataDTO;
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
     * 按物流单子集处理导入：匹配费用单、校验并构造落库数据。
     * <p>{@code allowAllocate=true} 且子集多于 1 条时按出库重量分摊行内汇总金额及计费重/实重；
     * 为 false 或仅 1 条时整行金额原样落到该单（单张 ADD_OLD 跨月新增场景）。</p>
     * <p>IMPORT_ADD_OLD 多单与 IMPORT_UPDATE 多单共用同一分摊规则，避免组合品子单重复计入全额费用。</p>
     * <p>自发货与尾程可在同一子集内并存，各单按自身费用单 type 设置 sourceType，不再要求同组费用归属一致。</p>
     */
    private String processImportBillVoGroup(List<LogisticsBillDTO.LogisticsBillVo> billGroup,
                                            boolean allowAllocate,
                                            List<TmsCostDetailDTO.UpdateDTO> updateList,
                                            LocalDateTime confirmTime,
                                            ImportHistoryRecordExcelDTO excelDTO,
                                            ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                            CfgLogisticsCostImportEntity costImportEntity,
                                            List<LogisticsBillCostEntity> logisticsBillCostList,
                                            List<TmsCfgCostEntity> cfgCostList,
                                            List<String> errorMsgList,
                                            Map<String, List<TmsCostDetailEntity>> mainIdListMap,
                                            Map<String, BigDecimal> orderWeightMap,
                                            Map<String, List<String>> orderWeightErrorMap,
                                            List<LogisticsBillCostDTO.AddDTO> addBillCostList,
                                            List<TmsCostDetailDTO.AddDTO> addCfgCostList,
                                            List<LogisticsBillCostDTO.UpdateDTO> updateBillCostList,
                                            List<TmsCostDetailDTO.UpdateDTO> updateCfgCostList) {
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocatedCostMap = Collections.emptyMap();
        Map<String, BigDecimal> weightMap = Collections.emptyMap();
        if (allowAllocate && billGroup.size() > 1) {
            weightMap = buildOrderWeightMap(billGroup, errorMsgList, orderWeightMap, orderWeightErrorMap);
            if (CollectionUtils.isEmpty(errorMsgList)) {
                allocatedCostMap = allocateCostDetailMap(updateList, billGroup, weightMap, errorMsgList);
            }
        }
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            return "";
        }
        BigDecimal totalImportBillingWeight = parseOptionalDecimalField(excelDTO.getBillingWeightLogistics(), "计费重", errorMsgList);
        BigDecimal totalImportThirdActualWeight = parseOptionalDecimalField(excelDTO.getThirdActualWeight(), "实重", errorMsgList);
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            return "";
        }
        BigDecimal billingWeightAllocatedSum = BigDecimal.ZERO;
        BigDecimal thirdActualWeightAllocatedSum = BigDecimal.ZERO;
        String lastImportType = "";
        for (int billIndex = 0; billIndex < billGroup.size(); billIndex++) {
            LogisticsBillDTO.LogisticsBillVo logisticsBillVo = billGroup.get(billIndex);
            logisticsBillVo.setReconciliationMonth(importDTO.getReconciliationMonth());
            applyImportLogisticsSupplierId(excelDTO, costImportEntity, Collections.singletonList(logisticsBillVo));
            // 单条待确认不分摊；多条且 allowAllocate 时使用分摊结果，避免把整行金额重复计入每张单。
            List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = allowAllocate && billGroup.size() > 1
                    ? allocatedCostMap.getOrDefault(logisticsBillVo.getDetailId(), Collections.emptyList())
                    : updateList;
            String thisImportType = checkCostImportData(excelDTO, logisticsBillCostList, logisticsBillVo, costImportEntity, errorMsgList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return lastImportType;
            }
            if (CharSequenceUtil.isNotBlank(lastImportType) && !CharSequenceUtil.equals(lastImportType, thisImportType)) {
                errorMsgList.add("同一识别单号分组匹配到的多张物流单导入类型不一致，无法合并处理");
                return lastImportType;
            }
            lastImportType = thisImportType;
            LogisticsBillCostEntity logisticsBillCostEntity = findMatchedLogisticsBillCost(logisticsBillCostList, logisticsBillVo,
                    excelDTO.getPayType(), thisImportType, costImportEntity);
            if (ObjectUtil.isEmpty(logisticsBillCostEntity)) {
                errorMsgList.add("未找到对应物流费用单");
                return lastImportType;
            }
            String billCostType = logisticsBillCostEntity.getType();
            if (!CharSequenceUtil.equals(billCostType, DictCostAttributionEnum.SELF_DELIVER.getCode())
                    && !CharSequenceUtil.equals(billCostType, DictCostAttributionEnum.LAST_MILE.getCode())) {
                errorMsgList.add(CharSequenceUtil.format("不支持的物流费用类型【{}】，仅支持自发货或尾程",
                        CharSequenceUtil.blankToDefault(DictCostAttributionEnum.getName(billCostType), billCostType)));
                return lastImportType;
            }
            // 每张单独立取归属（自发货/尾程），同识别分组内允许混合，不再强制整组同一 type。
            String rowSourceType = CharSequenceUtil.equals(billCostType, DictCostAttributionEnum.SELF_DELIVER.getCode())
                    ? SourceTypeEnum.LOGISTICS_BILL_COST.getCode()
                    : SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode();
            currentUpdateList.forEach(dto -> dto.setSourceType(rowSourceType));

            appendImportConfirmAmountError(importDTO, logisticsBillCostEntity.getId(), currentUpdateList, mainIdListMap, errorMsgList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return lastImportType;
            }

            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(), importDTO.getProcessingType())) {
                continue;
            }

            LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(confirmTime, logisticsBillCostEntity.getLogisticsBillId(),
                    logisticsBillCostEntity.getId(), logisticsBillCostEntity.getLogisticsBillDetailId(), excelDTO,
                    importDTO, currentUpdateList, errorMsgList, cfgCostList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return lastImportType;
            }
            // 多单分摊时 override updateDataDTO，避免改共享 excelDTO 导致后续子单重量被污染
            if (allowAllocate && billGroup.size() > 1 && CollUtil.isNotEmpty(weightMap)) {
                if (ObjectUtil.isNotNull(totalImportBillingWeight)) {
                    BigDecimal allocatedBillingWeight = allocateValueByWeight(totalImportBillingWeight, billGroup, weightMap,
                            billIndex, billingWeightAllocatedSum);
                    billingWeightAllocatedSum = billingWeightAllocatedSum.add(allocatedBillingWeight);
                    updateDataDTO.setBillingWeightLogistics(allocatedBillingWeight);
                }
                if (ObjectUtil.isNotNull(totalImportThirdActualWeight)) {
                    BigDecimal allocatedThirdActualWeight = allocateValueByWeight(totalImportThirdActualWeight, billGroup, weightMap,
                            billIndex, thirdActualWeightAllocatedSum);
                    thirdActualWeightAllocatedSum = thirdActualWeightAllocatedSum.add(allocatedThirdActualWeight);
                    updateDataDTO.setThirdActualWeight(allocatedThirdActualWeight);
                }
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
                currentUpdateList.forEach(obj -> obj.setMainId(updateDataDTO.getId()));
                updateCfgCostList.addAll(currentUpdateList);
            }
        }
        return lastImportType;
    }

    /**
     * 按导入模板配置的唯一识别字段匹配物流单，字段为空已在上游统一拦截。
     */
    private boolean matchesUniqueKey(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList, JSONObject successJson, LogisticsBillDTO.LogisticsBillVo logisticsBillVo) {
        return uniqueKeyList.stream().allMatch(uniqueKey -> {
            String importValue = getPreparedValue(successJson, uniqueKey);
            Object billValue = BeanUtil.getFieldValue(logisticsBillVo, uniqueKey.getTargetField());
            if (CharSequenceUtil.equals(PLATFORM_CODE_FIELD, uniqueKey.getTargetField())) {
                return LogisticsBillPlatformCodeUtil.matches(importValue, ObjectUtil.isNull(billValue) ? null : String.valueOf(billValue));
            }
            return CharSequenceUtil.equals(importValue, ObjectUtil.isNull(billValue) ? null : String.valueOf(billValue));
        });
    }

    /**
     * 物流商/平台模板只允许处理自身配置范围内的物流单，避免跨模板误写费用。
     * <p>按识别单号（identify_no）时不按配置平台过滤；按识别单号+物流商（identify_no_supplier）或空值时保持原有过滤。</p>
     */
    private boolean matchesCostImportConfig(CfgLogisticsCostImportEntity costImportEntity, LogisticsBillDTO.LogisticsBillVo logisticsBillVo) {
        if (!shouldFilterByCostImportPlatform(costImportEntity)) {
            return true;
        }
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

    /**
     * 是否按配置平台（物流商/销售平台）收窄物流单匹配范围。
     * identify_no 为 false：分组阶段可跨物流商命中，费用分摊与落库在 handleImportData 内再按状态筛选。
     */
    private boolean shouldFilterByCostImportPlatform(CfgLogisticsCostImportEntity costImportEntity) {
        String identifyType = costImportEntity.getIdentifyType();
        if (CharSequenceUtil.isBlank(identifyType)) {
            return true;
        }
        return CharSequenceUtil.equals(CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO_SUPPLIER.getCode(), identifyType);
    }

    /**
     * 导入行物流商：按识别单号+物流商时用配置平台；仅按识别单号时用命中物流单上的物流商。
     */
    private void applyImportLogisticsSupplierId(ImportHistoryRecordExcelDTO excelDTO,
                                                CfgLogisticsCostImportEntity costImportEntity,
                                                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList) {
        if (shouldFilterByCostImportPlatform(costImportEntity)) {
            excelDTO.setLogisticsSupplierId(costImportEntity.getDictPlatform());
            return;
        }
        if (CollUtil.isNotEmpty(logisticsBillVoList)) {
            excelDTO.setLogisticsSupplierId(logisticsBillVoList.get(0).getLogisticsSupplierId());
        }
    }

    /**
     * 同一文件名命中多条除识别维度外完全相同的配置时，会导致重复解析同一 sheet，提前拦截。
     */
    private void validateDuplicateImportConfig(List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList) {
        if (CollUtil.isEmpty(cfgLogisticsCostImportList)) {
            return;
        }
        Map<String, List<CfgLogisticsCostImportEntity>> grouped = cfgLogisticsCostImportList.stream()
                .collect(Collectors.groupingBy(entity -> CharSequenceUtil.join("|",
                        entity.getDictPlatform(),
                        entity.getName(),
                        entity.getSheetName(),
                        entity.getCostType())));
        for (List<CfgLogisticsCostImportEntity> group : grouped.values()) {
            if (group.size() > 1) {
                throw new ServiceException("文件名命中多条识别名称相同的费用项配置，请检查识别维度或识别名称是否重复");
            }
        }
    }

    /**
     * 按物流单明细、付款类型与导入处理类型，从预加载费用单列表中定位本次落库目标费用单。
     * <p>与 {@link #checkCostImportData} 判定的 importType 配合使用，返回值用于后续校验与 add/update 分支。</p>
     * <p>匹配规则：</p>
     * <ul>
     *   <li>{@code import_update} + 按识别单号（identify_no）：仅待确认，不含暂估确认，避免跨物流商合并时误更新不可分摊状态的单</li>
     *   <li>{@code import_update} + 按识别单号+物流商：待确认，或暂估确认且对账状态为待生成（未下推分摊）</li>
     *   <li>{@code import_add_old}：优先匹配上述可更新单；若无则放宽为同明细任意费用单，作为按原单新增的模板单</li>
     * </ul>
     *
     * @param logisticsBillCostList 预加载的物流费用单列表
     * @param logisticsBillVo       当前处理的物流单
     * @param payType               导入行付款类型
     * @param importType            导入处理类型（import_update / import_add_old）
     * @param costImportEntity      费用项导入配置，用于区分 identify_no 与 identify_no_supplier 匹配分支
     * @return 匹配到的费用单；无匹配时返回 {@code null}
     */
    private LogisticsBillCostEntity findMatchedLogisticsBillCost(List<LogisticsBillCostEntity> logisticsBillCostList,
                                                                LogisticsBillDTO.LogisticsBillVo logisticsBillVo,
                                                                String payType,
                                                                String importType,
                                                                CfgLogisticsCostImportEntity costImportEntity) {
        if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType)
                && !shouldFilterByCostImportPlatform(costImportEntity)) {
            // 按识别单号更新只动待确认单；暂估确认等状态在跨物流商合并场景下不参与分摊更新。
            return logisticsBillCostList.stream()
                    .filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(), logisticsBillVo.getDetailId())
                            && CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                            && CharSequenceUtil.equals(obj.getPayType(), payType))
                    .findFirst().orElse(null);
        }
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
     * 按识别单号维度：从分组命中的物流单中拆出两批落库对象。
     * <ul>
     *   <li>分组内存在待确认 + 同付款类型：仅待确认进更新批次（可参与重量分摊），已确认（当月/跨月）均不处理</li>
     *   <li>分组内无待确认、且配置含 import_add_old：已确认 + 同付款类型进按原单新增批次，同月/跨月由下游 checkCostImportData 校验</li>
     * </ul>
     */
    private void splitIdentifyNoProcessBills(List<LogisticsBillDTO.LogisticsBillVo> matchedBillList,
                                             List<LogisticsBillCostEntity> logisticsBillCostList,
                                             String payType,
                                             String importReconciliationMonth,
                                             CfgLogisticsCostImportEntity costImportEntity,
                                             List<LogisticsBillDTO.LogisticsBillVo> updateBillList,
                                             List<LogisticsBillDTO.LogisticsBillVo> addOldBillList) {
        boolean allowAddOld = costImportEntity.getImportType()
                .contains(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        boolean groupHasToBeConfirm = matchedBillList.stream().anyMatch(billVo -> logisticsBillCostList.stream()
                .anyMatch(cost -> CharSequenceUtil.equals(cost.getLogisticsBillDetailId(), billVo.getDetailId())
                        && CharSequenceUtil.equals(cost.getPayType(), payType)
                        && CharSequenceUtil.equals(cost.getReconciliationStatus(),
                                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())));
        if (groupHasToBeConfirm) {
            for (LogisticsBillDTO.LogisticsBillVo billVo : matchedBillList) {
                boolean hasToBeConfirm = logisticsBillCostList.stream().anyMatch(cost ->
                        CharSequenceUtil.equals(cost.getLogisticsBillDetailId(), billVo.getDetailId())
                                && CharSequenceUtil.equals(cost.getPayType(), payType)
                                && CharSequenceUtil.equals(cost.getReconciliationStatus(),
                                        ReconciliationStatusEnum.TO_BE_CONFIRM.getCode()));
                if (hasToBeConfirm) {
                    updateBillList.add(billVo);
                }
            }
            return;
        }
        if (!allowAddOld) {
            return;
        }
        for (LogisticsBillDTO.LogisticsBillVo billVo : matchedBillList) {
            boolean hasConfirmed = logisticsBillCostList.stream().anyMatch(cost ->
                    CharSequenceUtil.equals(cost.getLogisticsBillDetailId(), billVo.getDetailId())
                            && CharSequenceUtil.equals(cost.getPayType(), payType)
                            && CharSequenceUtil.equals(cost.getReconciliationStatus(),
                                    ReconciliationStatusEnum.CONFIRMED.getCode()));
            if (hasConfirmed) {
                addOldBillList.add(billVo);
            }
        }
    }

    /**
     * 订单重量 = SKU毛重 * 上游出库单实发数量，多物流单匹配时作为费用分摊依据。
     * 重量数据不完整时直接返回错误，避免把整行费用错误分摊到少数订单。
     */
    private Map<String, BigDecimal> buildOrderWeightMap(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList, List<String> errorMsgList,
                                                        Map<String, BigDecimal> preQueryOrderWeightMap,
                                                        Map<String, List<String>> preQueryOrderWeightErrorMap) {
        Map<String, BigDecimal> weightMap = new HashMap<>();
        if (logisticsBillVoList.stream().anyMatch(vo -> CharSequenceUtil.isBlank(vo.getOutstockId()))) {
            errorMsgList.add("无法获取上游出库单用于重量分摊");
            return weightMap;
        }
        int weightErrorCountBefore = errorMsgList.size();
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            BigDecimal orderWeight = preQueryOrderWeightMap == null ? null : preQueryOrderWeightMap.get(logisticsBillVo.getDetailId());
            if (ObjectUtil.isNotNull(orderWeight)) {
                weightMap.put(logisticsBillVo.getDetailId(), orderWeight);
                continue;
            }
            List<String> preQueryErrorList = preQueryOrderWeightErrorMap == null ? Collections.emptyList() : preQueryOrderWeightErrorMap.get(logisticsBillVo.getDetailId());
            if (CollUtil.isNotEmpty(preQueryErrorList)) {
                errorMsgList.addAll(preQueryErrorList);
            } else {
                errorMsgList.add("无法获取订单重量用于费用分摊：" + logisticsBillVo.getOutstockCode());
            }
        }
        BigDecimal totalWeight = weightMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (logisticsBillVoList.size() > 1 && weightMap.size() != logisticsBillVoList.size()
                && errorMsgList.size() == weightErrorCountBefore) {
            errorMsgList.add("同一识别分组存在物流单无法获取订单重量，无法合并分摊费用");
        }
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
                // 分摊后原币已变，本位币交由落库 handleData 重算
                copyDTO.setCostValueLocalCurrency(null);
                copyDTO.setLocalCurrency(null);
                resultMap.computeIfAbsent(logisticsBillVo.getDetailId(), key -> new ArrayList<>()).add(copyDTO);
            }
        }
        return resultMap;
    }

    /**
     * 按订单重量比例拆分单个数值（计费重/实重等），最后一单吸收四位小数尾差。
     * <p>与 {@link #allocateCostDetailMap} 费用分摊规则一致，保证拆分后总和等于导入值。</p>
     */
    private BigDecimal allocateValueByWeight(BigDecimal totalValue,
                                           List<LogisticsBillDTO.LogisticsBillVo> billGroup,
                                           Map<String, BigDecimal> weightMap,
                                           int billIndex,
                                           BigDecimal allocatedSum) {
        BigDecimal totalWeight = billGroup.stream()
                .map(vo -> weightMap.getOrDefault(vo.getDetailId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return totalValue;
        }
        LogisticsBillDTO.LogisticsBillVo logisticsBillVo = billGroup.get(billIndex);
        if (billIndex == billGroup.size() - 1) {
            return totalValue.subtract(allocatedSum);
        }
        return totalValue.multiply(weightMap.getOrDefault(logisticsBillVo.getDetailId(), BigDecimal.ZERO))
                .divide(totalWeight, 4, RoundingMode.HALF_UP);
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

            // 导入链路：币别未填且配置无默认值时直接报错，不再默认 CNY
            for (LogisticsBillCostDTO.AddDataDTO v : value) {
                if (CharSequenceUtil.isBlank(v.getCurrency())) {
                    throw new ServiceException(ApiError.LOGISTICS_COST_CURRENCY_REQUIRED);
                }
                boolean hasEstimated = v.getEstimatedValue() != null && v.getEstimatedValue().compareTo(BigDecimal.ZERO) != 0;
                if (hasEstimated && CharSequenceUtil.isBlank(v.getEstimatedCurrency())) {
                    throw new ServiceException(ApiError.LOGISTICS_COST_CURRENCY_REQUIRED);
                }
            }

            Map<String, List<LogisticsBillCostDTO.AddDataDTO>> cfgCostIdMaps = value.stream()
                    .collect(Collectors.groupingBy(LogisticsBillCostDTO.AddDataDTO::getCfgCostId));
            value = new ArrayList<>();
            for (Map.Entry<String, List<LogisticsBillCostDTO.AddDataDTO>> cfgCostIdMap : cfgCostIdMaps.entrySet()) {
                List<LogisticsBillCostDTO.AddDataDTO> groupValue = cfgCostIdMap.getValue();
                LogisticsBillCostDTO.AddDataDTO v = groupValue.get(0);
                String estimatedCurrency = v.getEstimatedCurrency();
                String currency = v.getCurrency();
                if (groupValue.stream().anyMatch(g -> !Objects.equals(estimatedCurrency, g.getEstimatedCurrency()))) {
                    throw new ServiceException(ApiError.LOGISTICS_COST_SAME_ITEM_MULTI_CURRENCY,
                            tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName());
                }
                if (groupValue.stream().anyMatch(g -> !Objects.equals(currency, g.getCurrency()))) {
                    throw new ServiceException(ApiError.LOGISTICS_COST_SAME_ITEM_MULTI_CURRENCY,
                            tmsCfgCostService.getById(cfgCostIdMap.getKey()).getCostName());
                }
                v.setEstimatedValue(groupValue.stream().filter(g -> g.getEstimatedValue() != null)
                        .map(LogisticsBillCostDTO.AddDataDTO::getEstimatedValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
                v.setCostValue(groupValue.stream().filter(g -> g.getCostValue() != null)
                        .map(LogisticsBillCostDTO.AddDataDTO::getCostValue).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
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
            // ADD_OLD 新增优先取 Excel 导入计费重，原单仅兜底，避免已确认单 0 kg 覆盖导入值
            addDTO.setBillingWeightLogistics(ObjectUtil.defaultIfNull(dto.getBillingWeightLogistics(),
                    logisticsBillCostEntity.getBillingWeightLogistics()));
            addDTO.setConfirmTime(importDataDTO.getConfirmTime());
            if (CharSequenceUtil.isBlank(dto.getCurrency())) {
                throw new ServiceException(ApiError.LOGISTICS_COST_CURRENCY_REQUIRED);
            }
            addDTO.setCurrency(dto.getCurrency());

            addDTO.setTrackNo(logisticsBillCostEntity.getTrackNo());
            addDTO.setChannelId(logisticsBillCostEntity.getChannelId());
            addDTO.setWeightLogistics(logisticsBillCostEntity.getWeightLogistics());
            addDTO.setVolumeWeightLogistics(logisticsBillCostEntity.getVolumeWeightLogistics());

            addDTO.setReconciliationMonth(dto.getReconciliationMonth());
            addDTO.setThirdHeight(dto.getThirdHeight());
            addDTO.setThirdWidth(dto.getThirdWidth());
            addDTO.setThirdLength(dto.getThirdLength());
            addDTO.setThirdActualWeight(dto.getThirdActualWeight());

            for (LogisticsBillCostDTO.AddDataDTO detailDTO : value) {
                BigDecimal actualValue = detailDTO.getCostValue();
                if (actualValue != null && actualValue.compareTo(BigDecimal.ZERO) != 0) {
                    TmsCostDetailDTO.AddDTO add = new TmsCostDetailDTO.AddDTO();
                    add.setMainId(addDTO.getId());
                    add.setCfgCostId(detailDTO.getCfgCostId());
                    add.setCostValue(actualValue);
                    add.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    add.setSourceType(sourceType);
                    add.setCurrency(detailDTO.getCurrency());
                    addCfgCostList.add(add);
                }

                BigDecimal estimatedValue = detailDTO.getEstimatedValue();
                if (estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) != 0) {
                    TmsCostDetailDTO.AddDTO add = new TmsCostDetailDTO.AddDTO();
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
        validateDuplicateImportConfig(cfgLogisticsCostImportList);
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
        updateDataDTO.setBillingWeightLogistics(parseOptionalDecimalField(excelDTO.getBillingWeightLogistics(), "计费重", errorMsgList));
        String currencyRequiredMsg = MessageUtils.getMessage(ApiError.LOGISTICS_COST_CURRENCY_REQUIRED);
        if (CharSequenceUtil.isBlank(excelDTO.getCurrency())) {
            errorMsgList.add(currencyRequiredMsg);
        } else {
            updateDataDTO.setCurrency(excelDTO.getCurrency());
        }
        updateDataDTO.setPayType(excelDTO.getPayType());
        updateDataDTO.setConfirmTime(confirmTime);
        updateDataDTO.setTrackNo(excelDTO.getTrackNo());
        //对账月份
        updateDataDTO.setReconciliationMonth(importDTO.getReconciliationMonth());
        //尺寸
        String thirdHeight = excelDTO.getThirdHeight();
        if (StringUtils.isNotBlank(thirdHeight)) {
            updateDataDTO.setThirdHeight(parseOptionalDecimalField(thirdHeight, "高度", errorMsgList));
        }
        String thirdWidth = excelDTO.getThirdWidth();
        if (StringUtils.isNotBlank(thirdWidth)) {
            updateDataDTO.setThirdWidth(parseOptionalDecimalField(thirdWidth, "宽度", errorMsgList));
        }
        String thirdLength = excelDTO.getThirdLength();
        if (StringUtils.isNotBlank(thirdLength)) {
            updateDataDTO.setThirdLength(parseOptionalDecimalField(thirdLength, "长度", errorMsgList));
        }
        //实重
        String thirdActualWeight = excelDTO.getThirdActualWeight();
        if (StringUtils.isNotBlank(thirdActualWeight)) {
            updateDataDTO.setThirdActualWeight(parseOptionalDecimalField(thirdActualWeight, "实重", errorMsgList));
        }

        boolean detailCurrencyMissing = false;
        for (TmsCostDetailDTO.UpdateDTO u : updateList) {
            if (CharSequenceUtil.isNotBlank(u.getCurrency())) {
                continue;
            }
            if (CharSequenceUtil.isNotBlank(updateDataDTO.getCurrency())) {
                u.setCurrency(updateDataDTO.getCurrency());
            } else {
                detailCurrencyMissing = true;
            }
        }
        if (detailCurrencyMissing && errorMsgList.stream().noneMatch(msg -> CharSequenceUtil.equals(msg, currencyRequiredMsg))) {
            errorMsgList.add(currencyRequiredMsg);
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
            //付款类型不一致的情况下走IMPORT_ADD_OLD的逻辑
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
                //只需要对Excel相同payType进行校验
                logisticsBillCostEntityList = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(excelDTO.getPayType(),obj.getPayType())).collect(Collectors.toList());

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
                //只需要对Excel相同payType进行校验
                if(CharSequenceUtil.equals(excelDTO.getPayType(),logisticsBillCostEntity.getPayType())){
                    if (CharSequenceUtil.equals(logisticsBillVo.getReconciliationMonth(),logisticsBillCostEntity.getReconciliationMonth())) {
                        errorMsgList.add("已存在相同对账月份的物流费用单，不支持新增");
                    }
                    if (!CharSequenceUtil.equals(logisticsBillVo.getReconciliationMonth(), logisticsBillCostEntity.getReconciliationMonth()) && CharSequenceUtil.equals(logisticsBillCostEntity.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
                        errorMsgList.add("已存在未确认的物流费用单，不支持新增");
                    }
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

    /**
     * 按配置顺序执行字段清洗。
     * <p>正数转为负数、负数转为正数之间为或级：按当前字段值符号最多生效其一，避免二次转换；
     * 二者与其它规则类型为并级，仍按顺序依次执行。</p>
     */
    private String cleanFieldValue(String value,
                                   CfgLogisticsCostImportDetailEntity detail,
                                   JSONObject rowData,
                                   Map<Integer, String> headMap) {
        String result = ObjectUtil.isEmpty(value) ? "" : String.valueOf(value);
        // 正数转负数 / 负数转正数互斥，命中其一后跳过另一条
        boolean signOrRuleApplied = false;
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
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.POSITIVE_TO_NEGATIVE.getCode(), type)) {
                if (!signOrRuleApplied && matchesNumberSign(result, true)) {
                    result = convertNumberBySign(result, true);
                    signOrRuleApplied = true;
                }
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.NEGATIVE_TO_POSITIVE.getCode(), type)) {
                if (!signOrRuleApplied && matchesNumberSign(result, false)) {
                    result = convertNumberBySign(result, false);
                    signOrRuleApplied = true;
                }
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
            return NON_CHINESE_PATTERN.matcher(value).replaceAll("");
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.ENGLISH.getCode(), mode)) {
            return NON_ENGLISH_PATTERN.matcher(value).replaceAll("");
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

    /**
     * 判断文本是否为符合符号条件的数值。
     *
     * @param value              原始文本
     * @param positiveToNegative true=要求正数，false=要求负数
     * @return 符合条件返回 true
     */
    private boolean matchesNumberSign(String value, boolean positiveToNegative) {
        if (CharSequenceUtil.isBlank(value)) {
            return false;
        }
        try {
            int signum = new BigDecimal(value.trim()).signum();
            return positiveToNegative ? signum > 0 : signum < 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 按符号条件转换数值。
     * <p>正数转为负数：仅当数值大于 0 时取反；负数转为正数：仅当数值小于 0 时取绝对值。非数值原样返回。
     * 与另一符号转换规则同时配置时为或级处理，由 {@link #cleanFieldValue} 保证最多生效其一。</p>
     *
     * @param value              原始文本
     * @param positiveToNegative true=正数转为负数，false=负数转为正数
     * @return 转换后的文本
     */
    private String convertNumberBySign(String value, boolean positiveToNegative) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }
        try {
            BigDecimal number = new BigDecimal(value.trim());
            int signum = number.signum();
            if (positiveToNegative) {
                if (signum <= 0) {
                    return value;
                }
                return number.negate().stripTrailingZeros().toPlainString();
            }
            if (signum >= 0) {
                return value;
            }
            return number.abs().stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private void setPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity detail, String value) {
        String text = ObjectUtil.isEmpty(value) ? "" : value;
        if (ObjectUtil.isNotNull(detail.getMappingIndex())) {
            rowData.set(detail.getMappingIndex().toString(), text);
        }
        if (CharSequenceUtil.isNotBlank(detail.getTargetField())
                && !CharSequenceUtil.equals(COST_ITEM_FIELD, detail.getTargetField())) {
            rowData.set(detail.getTargetField(), text);
        }
    }

    private String getPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity detail) {
        Object value = ObjectUtil.isNotNull(detail.getMappingIndex())
                ? rowData.get(detail.getMappingIndex().toString())
                : null;
        if (ObjectUtil.isEmpty(value)
                && CharSequenceUtil.isNotBlank(detail.getTargetField())
                && !CharSequenceUtil.equals(COST_ITEM_FIELD, detail.getTargetField())) {
            value = rowData.get(detail.getTargetField());
        }
        return ObjectUtil.isEmpty(value) ? "" : String.valueOf(value);
    }

    private void standardizeImportRowWeightValues(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                  List<JSONObject> successList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        Optional<CfgLogisticsCostImportDetailEntity> unitDetailOpt = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), LOGISTICS_WEIGHT_UNIT_FIELD))
                .findFirst();
        if (!unitDetailOpt.isPresent()) {
            return;
        }
        CfgLogisticsCostImportDetailEntity unitDetail = unitDetailOpt.get();
        List<CfgLogisticsCostImportDetailEntity> weightDetails = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), BILLING_WEIGHT_LOGISTICS_FIELD)
                        || CharSequenceUtil.equals(detail.getTargetField(), THIRD_ACTUAL_WEIGHT_FIELD))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(weightDetails)) {
            return;
        }
        for (JSONObject rowData : successList) {
            String unit = getPreparedValue(rowData, unitDetail);
            if (!UnitEnum.WeightUnitEnum.G.getCode().equalsIgnoreCase(unit)) {
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
            setPreparedValue(rowData, unitDetail, UnitEnum.WeightUnitEnum.KG.getCode());
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
