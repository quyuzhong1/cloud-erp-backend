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
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.dto.excel.ImportHistoryRecordExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.ImportHistoryRecordExcelListener;
import com.erp.server.tms.mapper.ImportHistoryRecordMapper;
import com.erp.server.tms.service.*;
import com.google.common.base.Stopwatch;
import groovy.lang.Lazy;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
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
    @Resource
    private OperateLogService operateLogService;
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
    @Resource
    @Lazy
    private ImportHistoryRecordService importHistoryRecordService;

    @Resource
    @Qualifier("importHistoryRecordPool")
    private ExecutorService importHistoryRecordPool;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
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
        ImportHistoryRecordEntity old = this.getByFileUrl(entity.getFileUrl());
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
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(importSyncDTO.getFileName(), importSyncDTO.getBusinessType(), importSyncDTO.getCostType());
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
        String taskId = downloadTaskFeign.saveImportTask("物流商费用导入", IMPORT_TMS_IMPORT_HISTORY_RECORD.getCode(), importSyncDTO);
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
            List<JSONObject> errorList = excelListenerUtil.getMatchList();
            Map<Integer, String> headMap = excelListenerUtil.getHeadMap();
            //匹配结果序号
            Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
            List<JSONObject> matchErrorList = errorList.stream().filter(obj -> CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.get(matchIndex.toString()))).collect(Collectors.toList());

            String url = "";
            if (CollectionUtils.isNotEmpty(matchErrorList) && !CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importSyncDTO.getProcessingType())) {
                String fileName = "物流商费用错误数据.xlsx";
                File file = ExcelUtil.customExportUtil(fileName, matchErrorList, excelListenerUtil.getHeadList());
                if (!file.isDirectory()) {
                    url = FastDFSClientUtil.uploadFile(file, fileName);
                }
            }
            importResultDTO.setErrorUrl(url);
            importResultDTO.setFinishTime(LocalDateTime.now());
            importResultDTO.setRemark("处理完成，失败" + matchErrorList.size() + "条");
            importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            downloadTaskFeign.updateTask(importResultDTO);
        }
        return  BatchResultDTO.success(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),"导入成功");
    }

    @Override
    public void handleImportSuccessList(ImportHistoryRecordDTO.ImportSyncDTO importDTO,CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
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
            return;
        }
        //预处理直接跳过处理
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
            return;
        }
        importHistoryRecordService.importBatchAddOrUpdate(importDataList,importDTO.getProcessingType());

        //数据落库结束时间
        log.warn("保存处理时间 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
        // 2. 提取唯一键配置
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = extractUniqueKeyList(cfgImportDetailList);
        // 3. 构建 paramMap，收集唯一键所有唯一值
        Map<String, List<Object>> paramMap = buildParamMap(cfgImportDetailList, headMap, successList);
        // 4. 执行所有数据库预查询
        ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult = preQueryDbData(paramMap, costImportEntity);
        // 5. 判断纵向/横向模式
        boolean isVertical = isVerticalCostItem(cfgImportDetailList);
        if (isVertical) {
            return processVerticalCostItems(uniqueKeyList, cfgImportDetailList, preQueryResult, importDTO, costImportEntity, successList, matchImportList, headList, headMap);
        } else {
            return processHorizontalCostItems(uniqueKeyList, cfgImportDetailList, preQueryResult, importDTO, costImportEntity, successList, matchImportList, headList, headMap);
        }
    }

    // 校验表头唯一性和数据非空
    private void validateHeadersAndData(List<String> headList, List<JSONObject> successList) {
        if (headList.size() != headList.stream().distinct().count()) {
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
        return uniqueKeyList;
    }

    // 构建 paramMap，收集唯一键所有唯一值
    private Map<String, List<Object>> buildParamMap(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, Map<Integer, String> headMap, List<JSONObject> successList) {
        Map<String, List<Object>> paramMap = new HashMap<>();
        for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgImportDetailList) {
            Integer mappingIndex = getMapKey(headMap, cfgDetail.getSourceField());
            if (ObjectUtil.isEmpty(mappingIndex)) continue;
            cfgDetail.setMappingIndex(mappingIndex);
            if (!cfgDetail.getIsUniqueKey()) continue;
            List<Object> dataList = successList.stream()
                    .filter(obj -> ObjectUtil.isNotEmpty(obj.get(mappingIndex.toString())))
                    .map(obj -> obj.get(mappingIndex.toString()))
                    .distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(dataList)) {
                paramMap.put(cfgDetail.getTargetField(), dataList);
            }
        }
        return paramMap;
    }

    // 执行所有数据库预查询
    private ImportHistoryRecordDTO.PreQueryResultDTO preQueryDbData(Map<String, List<Object>> paramMap, CfgLogisticsCostImportEntity costImportEntity) {
        // 查询配置类型
        String costAttribution = CharSequenceUtil.equals(costImportEntity.getBusinessType(), CfgLogisticsCostImportBusinessTypeEnum.LOGISTICS_BILL_COST.getCode()) ?
                DictCostAttributionEnum.SELF_DELIVER.getCode() : DictCostAttributionEnum.LAST_MILE.getCode();
        //来源类型
        String sourceType = CharSequenceUtil.equals(costImportEntity.getBusinessType(),CfgLogisticsCostImportBusinessTypeEnum.LOGISTICS_BILL_COST.getCode()) ?
                SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode() : SourceTypeEnum.LOGISTICS_BILL_COST.getCode();

        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(costAttribution);
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillByUniqueKey(paramMap);
        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if (CollUtil.isNotEmpty(logisticsBillVos)) {
            List<String> logisticsBillCostIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getLogisticsBillCostId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
            List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostIdList);
            mainIdListMap = CollUtil.isEmpty(listByMainIdList) ? new HashMap<>() : listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        return new ImportHistoryRecordDTO.PreQueryResultDTO(sourceType,costAttribution,logisticsBillVos, mainIdListMap, logisticsBillCostList, cfgCostList);
    }

    // 判断是否为纵向费用项
    private boolean isVerticalCostItem(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList) {
        return cfgImportDetailList.stream().anyMatch(obj -> CharSequenceUtil.equals(obj.getTargetField(), "costItem") && CharSequenceUtil.isNotBlank(obj.getSourceDetailField()));
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
            Map<Integer, String> headMap) {
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = Collections.synchronizedList(new ArrayList<>());
        List<Integer> uniqueIndexes = uniqueKeyList.stream()
                .map(CfgLogisticsCostImportDetailEntity::getMappingIndex)
                .filter(ObjectUtil::isNotNull)
                .collect(Collectors.toList());
        Map<String, List<JSONObject>> map = successList.stream()
                .collect(Collectors.groupingBy(obj ->
                        uniqueIndexes.stream()
                                .map(idx -> String.valueOf(obj.get(idx.toString())))
                                .collect(Collectors.joining("_"))
                ));
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
                    for (JSONObject jsonObject : value) {
                        jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                        List<String> costErrorMsgList = new ArrayList<>();
                        List<TmsCostDetailDTO.UpdateDTO> updateList = rowFormatCost(successJson, jsonObject, preQueryResult.getCfgCostList(), cfgImportDetailList, headList, preQueryResult.getSourceType(), preQueryResult.getDictCostAttribution(), costErrorMsgList, currencyMap);
                        if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                            jsonObject.set(matchIndex.toString(), MATCH_FAIL);
                            jsonObject.set(errorIndex.toString(), FieldValidUtil.getMsgSort(costErrorMsgList));
                            synchronized (matchImportList) { matchImportList.add(jsonObject); }
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
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getLogisticsBillVoList(), preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList, preQueryResult.getDictCostAttribution(), preQueryResult.getMainIdListMap());
                        if (importDataDTO != null) importDataList.add(importDataDTO);
                    } catch (Exception e) {
                        log.error("数据处理失败 ,e = {}", e.getMessage());
                        mainErrorMsgList.add(e.getMessage());
                    }
                    updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList);
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
            Map<Integer, String> headMap) {
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = Collections.synchronizedList(new ArrayList<>());
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        List<java.util.concurrent.Future<Void>> futures = new ArrayList<>();
        int batchSize = 500;
        for (int i = 0; i < successList.size(); i += batchSize) {
            int end = Math.min(successList.size(), i + batchSize);
            List<JSONObject> batch = successList.subList(i, end);
            futures.add(importHistoryRecordPool.submit(() -> {
                for (JSONObject jsonObject : batch) {
                    JSONObject successJson = new JSONObject();
                    List<String> errorMsgList = new ArrayList<>();
                    List<TmsCostDetailDTO.UpdateDTO> updateList = lineFormatCost(successJson, jsonObject, errorMsgList, preQueryResult.getCfgCostList(), cfgImportDetailList, headList, preQueryResult.getDictCostAttribution());
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateList);
                    try {
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getLogisticsBillVoList(), preQueryResult.getCfgCostList(), importDTO, costImportEntity, errorMsgList, preQueryResult.getDictCostAttribution(), preQueryResult.getMainIdListMap());
                        if (importDataDTO != null) importDataList.add(importDataDTO);
                    } catch (Exception e) {
                        log.error("数据处理失败 ,e = {}", e.getMessage());
                        errorMsgList.add(e.getMessage());
                    }
                    updateMatchResult(Collections.singletonList(jsonObject), matchIndex.toString(), errorIndex.toString(), errorMsgList, matchImportList);
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
    public  void importBatchAddOrUpdate(List<LogisticsBillCostDTO.ImportDataDTO> importDataList,String processingType) {
         if (CollUtil.isEmpty(importDataList)) {
              return;
         }
         //新增物流单
        List<LogisticsBillEntity> logisticsBillList = importDataList.stream().map(LogisticsBillCostDTO.ImportDataDTO::getLogisticsBillEntity).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
         if (CollUtil.isNotEmpty(logisticsBillList)) {
             logisticsBillService.batchImportAdd(logisticsBillList);
         }

        //新增物流明细
        List<LogisticsBillDetailEntity> logisticsBillDetailList = importDataList.stream().flatMap(obj -> {
            List<LogisticsBillDetailEntity> list = obj.getLogisticsBillDetailList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(logisticsBillDetailList)) {
            logisticsBillDetailService.saveBatch(logisticsBillDetailList);
        }

        //新增物流费用
        List<LogisticsBillCostDTO.AddDTO> logisticsBillCostAddList = importDataList.stream().flatMap(obj -> {
            List<LogisticsBillCostDTO.AddDTO> list = obj.getAddBillCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        logisticsBillCostService.batchImportAdd(logisticsBillList,logisticsBillDetailList,logisticsBillCostAddList,processingType);

        //更新物流费用
        List<LogisticsBillCostDTO.UpdateDTO> logisticsBillCostUpdateList = importDataList.stream().flatMap(obj -> {
            List<LogisticsBillCostDTO.UpdateDTO> list = obj.getUpdateBillCostList();
            return list == null ? Stream.empty() : list.stream();
        }).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        logisticsBillCostService.batchImportUpdate( logisticsBillList,logisticsBillDetailList,logisticsBillCostUpdateList,processingType);

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
    }

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
     * 合并相同费用项的费用
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
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> map = updateList.stream().collect(Collectors.groupingBy(obj -> obj.getCfgCostId() + "_" + obj.getType()));
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
    private List<TmsCostDetailDTO.UpdateDTO> lineFormatCost (JSONObject successJson,JSONObject jsonObject,List<String> errorMsgList,List<TmsCfgCostEntity> cfgCostList,
                                                             List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,List<String> headList,String cfgAttribution) {
        //查询币别
        String currencyIndex = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex()) && CharSequenceUtil.equals(obj.getTargetField(), "currency"))
                .map(obj -> obj.getMappingIndex().toString()).findFirst().orElse("");
        String currency = ObjectUtil.isEmpty(jsonObject.get(currencyIndex)) ? "" : String.valueOf(jsonObject.get(currencyIndex));
        //币别赋值
        if (ObjectUtil.isNotNull(currency)) {
            CurrencyEnum currencyEnum = CurrencyEnum.getByNameOrCode(currency);
            if (ObjectUtil.isEmpty(currencyEnum)){
                errorMsgList.add("币别不存在");
            } else {
                currency = currencyEnum.getCurrencyCode();
            }
        }

        //费用数据
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        HashMap<String,String> currencyMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            //字段名称
            String field = headList.get(Integer.parseInt(entry.getKey()));
            if (CharSequenceUtil.equals(field,ERROR_MSG)) {
                continue;
            }
            if (CharSequenceUtil.equals(field,MATCH_FIELD)) {
                continue;
            }
            CfgLogisticsCostImportDetailEntity cfgDetailEntity = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex()) && obj.getMappingIndex().equals(Integer.valueOf(entry.getKey()))).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(cfgDetailEntity)) {
                log.warn("导入配置未找到字段【{}】的配置项",field);
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

                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity) && ObjectUtil.isNotEmpty(entry.getValue())) {
                    //校验费用值类型
                    List<String> errorMsg = FieldValidUtil.fieldValid(new TmsCostDetailDTO.CheckValueDTO(entry.getValue().toString()));
                    if (CollUtil.isNotEmpty(errorMsg)) {
                        errorMsgList.add(tmsCfgCostEntity.getCostName() + errorMsg.get(0));
                        continue;
                    }
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //实际金额
                    BigDecimal costValue =   isAbsoluteValue ? new BigDecimal(entry.getValue().toString()).abs() : new BigDecimal(entry.getValue().toString());
                    updateDTO.setCostValue(costValue);
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDTO.setSourceType(SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode());
                    updateDTO.setCurrency(currency);
                    updateList.add(updateDTO);
                }
            }
            successJson.set(cfgDetailEntity.getTargetField(),String.valueOf(entry.getValue()));
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
     * @param sourceType
     * @return List<UpdateDTO>
     */
    private List<TmsCostDetailDTO.UpdateDTO> rowFormatCost (JSONObject successJson,JSONObject jsonObject,List<TmsCfgCostEntity> cfgCostList,
                                                            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,List<String> headList,String sourceType,
                                                            String costAttribution,List<String> errorMsgList,HashMap<String,String> currencyMap) {
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        //查询币别
        String currencyIndex = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex()) && CharSequenceUtil.equals(obj.getTargetField(), "currency"))
                .map(obj -> obj.getMappingIndex().toString()).findFirst().orElse("");
        String currency = ObjectUtil.isEmpty(jsonObject.get(currencyIndex)) ? "" : String.valueOf(jsonObject.get(currencyIndex));
        //币别赋值
        if (CharSequenceUtil.isNotBlank(currency)) {
            CurrencyEnum currencyEnum = CurrencyEnum.getByNameOrCode(currency);
            if (ObjectUtil.isEmpty(currencyEnum)){
                errorMsgList.add("币别不存在");
            } else {
                currency = currencyEnum.getCurrencyCode();
            }
        }

        //查询实际金额
        String actualAmountIndex = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex()) &&  CharSequenceUtil.equals(obj.getTargetField(), "actualAmount"))
                .map(obj -> obj.getMappingIndex().toString()).findFirst().orElse("");
        String actualAmount = ObjectUtil.isEmpty(jsonObject.get(actualAmountIndex)) ? null : String.valueOf(jsonObject.get(actualAmountIndex));

        //查询预估金额
        String estimatedAmountIndex = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex()) &&  CharSequenceUtil.equals(obj.getTargetField(), "estimatedAmount"))
                .map(obj -> obj.getMappingIndex().toString()).findFirst().orElse("");
        String estimatedAmount = ObjectUtil.isEmpty(jsonObject.get(estimatedAmountIndex)) ? null : String.valueOf(jsonObject.get(estimatedAmountIndex));

        //校验费用值类型
        List<String> errorMsg = FieldValidUtil.fieldValid(new TmsCostDetailDTO.CheckAmountDTO(actualAmount,estimatedAmount));
        if (CollUtil.isNotEmpty(errorMsg)) {
            errorMsgList.addAll(errorMsg);
            return updateList;
        }

        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String field = headList.get(Integer.parseInt(entry.getKey()));
            if (CharSequenceUtil.equals(field,ERROR_MSG)) {
                continue;
            }
            if (CharSequenceUtil.equals(field,MATCH_FIELD)) {
                continue;
            }
            CfgLogisticsCostImportDetailEntity cfgDetailEntity = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex()) && obj.getMappingIndex().equals(Integer.valueOf(entry.getKey()))).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(cfgDetailEntity)) {
                log.warn("导入配置未找到字段【{}】的配置项",field);
                continue;
            }
            //判断导入字段是否是费用项
            if ("costItem".equals(cfgDetailEntity.getTargetField())){
                TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), cfgDetailEntity.getTargetDetailFieldName()) && CharSequenceUtil.equals(obj.getDictCostAttribution(), costAttribution)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(tmsCfgCostEntity)) {
                    errorMsgList.add(CharSequenceUtil.format("费用管理未找到该费用名称【{}】",cfgDetailEntity.getTargetDetailFieldName()));
                    continue;
                }
                String oldCurrency = currencyMap.get(tmsCfgCostEntity.getDictCostCategory());
                if (CharSequenceUtil.isNotBlank(oldCurrency) &&  !CharSequenceUtil.equals(oldCurrency, currency)) {
                    errorMsgList.add("同一费用分类下币种必须一致");
                } else {
                    //添加币别费用
                    currencyMap.put(tmsCfgCostEntity.getDictCostCategory(),currency);
                }

                if (StrUtil.isBlank(actualAmount) && StrUtil.isBlank(estimatedAmount)) {
                    errorMsgList.add(CharSequenceUtil.format("费用项【{}】实际金额和预估金额不能同时为空",cfgDetailEntity.getTargetDetailFieldName()));
                }
                //是否绝对值
                Boolean isAbsoluteValue = cfgDetailEntity.getIsAbsoluteValue();

                if (StrUtil.isNotBlank(actualAmount)) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //实际金额
                    BigDecimal costValue =   isAbsoluteValue ? new BigDecimal(actualAmount).abs() : new BigDecimal(actualAmount);
                    updateDTO.setCostValue(costValue);
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDTO.setCurrency(currency);
                    updateDTO.setSourceType(sourceType);
                    updateList.add(updateDTO);
                }
                if(StrUtil.isNotBlank(estimatedAmount)) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //预计金额
                    BigDecimal costValue =   isAbsoluteValue ? new BigDecimal(estimatedAmount).abs() : new BigDecimal(estimatedAmount);
                    updateDTO.setCostValue(costValue);
                    updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setSourceType(sourceType);
                    updateDTO.setCurrency(currency);
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateList.add(updateDTO);
                }
            }
            successJson.set(cfgDetailEntity.getTargetField(),String.valueOf(entry.getValue()));
        }
        return updateList;
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
    private LogisticsBillCostDTO.ImportDataDTO handleImportData(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList , JSONObject successJson, List<TmsCostDetailDTO.UpdateDTO> updateList, List<LogisticsBillCostEntity> logisticsBillCostList,
                                                                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos, List<TmsCfgCostEntity> cfgCostList, ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                                CfgLogisticsCostImportEntity costImportEntity, List<String> errorMsgList, String costAttribution, Map<String, List<TmsCostDetailEntity>> mainIdListMap) {

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
        //查询根据唯一键匹配物流单
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList = logisticsBillVos.stream().filter(obj -> uniqueKeyList.stream().allMatch(uniqueKey -> CharSequenceUtil.equals(String.valueOf(successJson.get(uniqueKey.getTargetField())), BeanUtil.getFieldValue(obj, uniqueKey.getTargetField()).toString()))).collect(Collectors.toList());

        //未查询到物流单则需要按新增分货（按新单）逻辑处理
        String importType = "";
        if (CollUtil.isEmpty(logisticsBillVoList)) {
            boolean contains = costImportEntity.getImportType().contains(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_NEW.getCode());
            if (!contains) {
                errorMsgList.add("未查到物流单，配置的导入处理类型不包含导入新增（按新单），请核查单号");
            }
            importType = CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_NEW.getCode();
        }

        if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_NEW.getCode().equals(importType)) {
            if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                errorMsgList.add("单号已存在无法新增，请核查单号");
            }
        } else {
            if (CollUtil.isEmpty(logisticsBillVoList)) {
                errorMsgList.add("未找到对应物流单");
            }
            if (logisticsBillVoList.size() > 1){
                errorMsgList.add("对应物流单有多条，请在补全导入订单信息后重新导入");
            }
        }
        if (CollUtil.isEmpty(updateList)) {
            errorMsgList.add("物流费用项不能为空");
        }
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            return importDataDTO;
        }

        LogisticsBillCostEntity logisticsBillCostEntity;

        LocalDateTime confirmTime = CharSequenceUtil.isBlank(excelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(excelDTO.getConfirmTimeStr());

        if (CollUtil.isNotEmpty(logisticsBillVoList)) {
            LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(0);
            logisticsBillVo.setReconciliationMonth(importDTO.getReconciliationMonth());
            //物流费用数据验证
            String thisImportType = checkCostImportData(excelDTO, logisticsBillCostList, logisticsBillVo, costAttribution, costImportEntity, errorMsgList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return importDataDTO;
            }
            //物流费用单
            logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())
                                    && (CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                                    || (CharSequenceUtil.equals(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(),obj.getReconciliationStatus())
                                    && CharSequenceUtil.equals(obj.getCheckStatus(),LogisticsBillCostCheckStatusEnum.CHECKING.getCode())))
                                    && CharSequenceUtil.equals(obj.getPayType(),excelDTO.getPayType()))
                    .findFirst().orElse(null);
            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(thisImportType) && Objects.isNull(logisticsBillCostEntity)){
                logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())).findFirst().orElse(null);
            }
            if (ObjectUtil.isEmpty(logisticsBillCostEntity)) {
                errorMsgList.add("未找到对应物流费用单");
                return importDataDTO;
            }

            //校验分类币别
            checkCategoryCurrency(updateList,logisticsBillCostEntity,cfgCostList,mainIdListMap,errorMsgList);

            //预处理直接跳过处理
            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
                return importDataDTO;
            }

            //数据格式化
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(confirmTime,logisticsBillCostEntity.getLogisticsBillId(), excelDTO,
                    importDTO, updateList, errorMsgList, cfgCostList);
            //有错误信息直接跳过不暂处理
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return importDataDTO;
            }
            importDataDTO.setConfirmTime(confirmTime);
            importDataDTO.setImportType(thisImportType);

            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(thisImportType)){
                //新增费用项数据
                List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO,updateList);
                //格式化物流费用和费用项信息
                getLogisticsBillCostAddData(importDataDTO,logisticsBillCostEntity,dtoList);

        /*        List<BaseResultDTO.AddDTO> addDTOS = logisticsBillCostService.addPayAndRefund(dtoList);
                pairList = addDTOS.stream()
                        .map(obj -> new Pair<String, LocalDateTime>(obj.getId(), confirmTime))
                        .collect(Collectors.toList());*/
            }else {
                updateDataDTO.setId(logisticsBillCostEntity.getId());
                importDataDTO.setUpdateBillCostList(Collections.singletonList(updateDataDTO));
                //费用项id赋值
                updateList.forEach(obj -> obj.setMainId(updateDataDTO.getId()));
                importDataDTO.setUpdateCfgCostList(updateList);
           /*
                updateDataDTO.setCostDetailList(updateList);
                BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                pairList.add(new Pair<>(update.getId(),confirmTime));*/
            }
        } else {
            //预处理直接跳过处理
            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
                return importDataDTO;
            }
            //格式化物流单和明细
            getAddImportLogisticBill(importDataDTO,excelDTO,costAttribution);
            //格式化物流费用单
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(importDataDTO.getConfirmTime(),importDataDTO.getLogisticsBillEntity().getId(),excelDTO,
                    importDTO, updateList, errorMsgList, cfgCostList);
            //有错误信息直接跳过不暂处理
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return importDataDTO;
            }
            LogisticsBillCostDTO.AddDTO addDTO =BeanUtil.toBean(updateDataDTO, LogisticsBillCostDTO.AddDTO.class);
            addDTO.setId(IdWorker.getIdStr());
            importDataDTO.setAddBillCostList(Collections.singletonList(addDTO));
            //费用项id赋值
            updateList.forEach(obj -> obj.setMainId(addDTO.getId()));
            importDataDTO.setUpdateCfgCostList(updateList);

          /*  updateDataDTO.setCostDetailList(updateList);
            BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
            pairList.add(new Pair<>(update.getId(),confirmTime));*/
        }
        return importDataDTO;
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
    private void getLogisticsBillCostAddData(LogisticsBillCostDTO.ImportDataDTO importDataDTO,LogisticsBillCostEntity logisticsBillCostEntity,List<LogisticsBillCostDTO.AddDataDTO> dtoList) {
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
                add.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
                add.setCurrency(detailDTO.getCurrency());
                addCfgCostList.add(add);

                BigDecimal estimatedValue = detailDTO.getEstimatedValue();
                if(estimatedValue != null && estimatedValue.compareTo(BigDecimal.ZERO) != 0) {
                    add = new TmsCostDetailDTO.AddDTO();
                    add.setMainId(addDTO.getId());
                    add.setCfgCostId(detailDTO.getCfgCostId());
                    add.setCostValue(estimatedValue);
                    add.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    add.setSourceType(SourceTypeEnum.LOGISTICS_BILL_COST.getCode());
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
        BaseDTO.ImportDTO importDTO = new BaseDTO.ImportDTO();
        importDTO.setFileName(entity.getFileName());
        importDTO.setFileUrl(entity.getFileUrl());
        ImportHistoryRecordDTO.ImportDTO dto = new ImportHistoryRecordDTO.ImportDTO();
        dto.setBusinessType(entity.getBusinessType());
        dto.setProcessingType(processingType);
        dto.setReconciliationMonth(entity.getReconciliationMonth());

        //查询配置主表信息
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(entity.getFileName(), entity.getBusinessType(), CfgLogisticsCostImportCostTypeEnum.EXCEL.getCode());
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
        ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO = new ImportHistoryRecordDTO.ImportSyncDTO(dto, importDTO);
        importSyncDTO.setTaskId(fileTaskDTOS.get(0).getTaskId());
        importSyncDTO.setCfgLogisticsCostImportList(cfgLogisticsCostImportList);
        importSyncDTO.setImportDetailList(importDetailList);
        importSyncDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        return preprocessingImportExcel(importSyncDTO);
    }

    /**
     * 新增物流单
     * @author will
     * @date 2026/1/22 20:04
     * @param excelDTO
     * @return com.erp.model.tms.entity.LogisticsBillEntity
     */
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
    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData(LocalDateTime confirmTime, String logisticsBillId, ImportHistoryRecordExcelDTO excelDTO,
                                                                             ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                                             List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                             List<String> errorMsgList,
                                                                             List<TmsCfgCostEntity> cfgCostList) {
        //数据赋值
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setLogisticsBillId(logisticsBillId);
        updateDataDTO.setBillingWeightLogistics(CharSequenceUtil.isBlank(excelDTO.getBillingWeightLogistics()) ? null : new BigDecimal(excelDTO.getBillingWeightLogistics()));
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
        updateDataDTO.setPayType(excelDTO.getPayType());
        updateDataDTO.setConfirmTime(confirmTime);
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
     * @param excelDTO
     * @param logisticsBillCostList
     * @param logisticsBillVo
     * @param dictCostAttribution
     * @param costImportEntity
     * @return List<String>
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/11 14:24
     */
    private String checkCostImportData (ImportHistoryRecordExcelDTO excelDTO
            , List<LogisticsBillCostEntity> logisticsBillCostList, LogisticsBillDTO.LogisticsBillVo logisticsBillVo ,
                                        String dictCostAttribution,CfgLogisticsCostImportEntity costImportEntity,
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

        if (CollUtil.isEmpty(logisticsBillCostEntityList)) {
            errorMsgList.add("未找到对应的物流费用单");
            return importType;
        }
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
            if (!CharSequenceUtil.equals(logisticsBillCostEntity.getType(),dictCostAttribution)) {
                errorMsgList.add(CharSequenceUtil.format("需要导入【{}】物流单费用信息",DictCostAttributionEnum.getName(dictCostAttribution)));
            }
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
    private Integer getMapKey (Map<Integer,String> headMap,String targetValue) {
        Integer resultKey = null;
        for (Integer key : headMap.keySet()) {
            // 获取对应的value
            String value = headMap.get(key);

            // 如果value等于目标值，输出对应的key
            if (value.contains(targetValue)) {
                resultKey = key;
                // 如果只需要找到一个匹配的key，可以break
                break;
            }
        }
        return  resultKey;
    }

    /**
     * @description: 根据文件URL查询导入记录
     * @author Will
     * @date: 2024/5/11 14:24
     * @param fileUrl
     * @return com.erp.model.tms.entity.ImportHistoryRecordEntity
     */
    private ImportHistoryRecordEntity getByFileUrl(String fileUrl) {
        if (CharSequenceUtil.isBlank(fileUrl)) {
            throw new ServiceException("文件URL不能为空");
        }
        return this.lambdaQuery().eq(ImportHistoryRecordEntity::getFileUrl, fileUrl).last("limit 1").one();
    }
}
