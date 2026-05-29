package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
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
import groovy.lang.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;
    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;
    @Resource
    private CfgLogisticsCostImportFieldService cfgLogisticsCostImportFieldService;
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
    private DmpTaskFeign dmpTaskFeign;


    @Resource
    @Qualifier("importHistoryRecordPool")
    private ExecutorService importHistoryRecordPool;

    private static final String DETAIL_VALUE_KEY_PREFIX = "__cfgImportValue_";
    private static final String PAY_TYPE_FIELD = "payType";
    private static final String CURRENCY_FIELD = "currency";
    private static final String LOGISTICS_WEIGHT_UNIT_FIELD = "logisticsWeightUnit";
    private static final String WEIGHT_UNIT_KG = "KG";
    private static final String WEIGHT_UNIT_G = "g";
    private static final String STANDARD_WEIGHT_UNIT = "kg";
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[A-Za-z]");

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
        // 导入模板按文件名、业务类型和费用来源识别；识别失败时不能进入异步任务，否则任务回调阶段无法确定解析规则。
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(importSyncDTO.getFileName(), importSyncDTO.getBusinessType(), importSyncDTO.getCostType());
        if (CollUtil.isEmpty(cfgLogisticsCostImportList)) {
            return BatchResultDTO.fail(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),"无法识别导入模板，请检查配置是否正确");
        }
        // 配置明细决定 Excel 表头映射、唯一识别号、费用项和清洗规则，缺失时无法解析任何业务字段。
        List<String> mainIdList = cfgLogisticsCostImportList.stream().map(CfgLogisticsCostImportEntity::getId).distinct().collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> importDetailList =  cfgLogisticsCostImportDetailService.listByMainIdList(mainIdList);
        if (CollUtil.isEmpty(importDetailList)) {
            return BatchResultDTO.fail(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND.getMsg());
        }
        importSyncDTO.setCfgLogisticsCostImportList(cfgLogisticsCostImportList);
        importSyncDTO.setImportDetailList(importDetailList);
        importSyncDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        // 大文件导入通过任务中心异步执行，当前接口只返回任务号，实际解析入口为 preprocessingImportExcel。
        String taskId = downloadTaskFeign.saveImportTask("物流商费用导入", IMPORT_TMS_IMPORT_HISTORY_RECORD.getCode(), importSyncDTO);
        return  BatchResultDTO.success(taskId,importSyncDTO.getFileName(),"导入成功");
    }


    @Override
    public BatchResultDTO preprocessingImportExcel(ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO) {
        // 同一个文件可能配置多个 sheet 或多套模板规则，按配置主表分组后逐个读取对应 sheet。
        Map<String,List<CfgLogisticsCostImportDetailEntity>> impotyDetailMap = importSyncDTO.getImportDetailList().stream().collect(Collectors.groupingBy(CfgLogisticsCostImportDetailEntity::getMainId));

        // 文件在异步任务中重新下载，避免 Controller 请求线程持有大文件内容。
        byte[] bytes = fileFeign.downloadFile(importSyncDTO.getFileUrl());

        // 同一个文件同一次导入使用同一批次号，便于导入历史、清洗结果和后续确认追踪同一批数据。
        String batchNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        importSyncDTO.setCode(batchNo);

        // 只有至少一个配置 sheet 读取到表头才算模板匹配成功；全部未命中时按 sheet 不存在处理。
        Boolean isExistSheet = Boolean.FALSE;

        for (CfgLogisticsCostImportEntity costImportEntity : importSyncDTO.getCfgLogisticsCostImportList()) {
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList = impotyDetailMap.get(costImportEntity.getId());
            if (CollUtil.isEmpty(cfgImportDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
            }
            // 唯一识别号是物流商模板匹配 ERP 物流单的核心条件，缺失时无法判断导入费用应新增或更新到哪张单据。
            List<CfgLogisticsCostImportDetailEntity> cfgDetailList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cfgDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND,importSyncDTO.getFileName());
            }

            ImportHistoryRecordExcelListener excelListenerUtil = new ImportHistoryRecordExcelListener(costImportEntity,cfgImportDetailList,importSyncDTO);
            try {
                // EasyExcel 按配置的表头行和 sheet 名读取；Listener 内部分批完成字段清洗、匹配结果生成和导入处理。
                EasyExcel.read(new ByteArrayInputStream(bytes), excelListenerUtil)
                        .headRowNumber(costImportEntity.getHeaderRow())
                        .sheet(costImportEntity.getSheetName()).doRead();
            } catch (ExcelCommonException e) {
                log.error("导入格式错误！", e);
                throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
            }


            // Excel 解析结束后更新任务状态；导入历史记录和清洗文件由 Listener 统一生成。
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(importSyncDTO.getTaskId());
            importResultDTO.setCount(excelListenerUtil.getCount());
            Map<Integer, String> headMap = excelListenerUtil.getHeadMap();
            // 当前配置 sheet 未读取到表头时继续尝试其他配置，避免多模板文件中一个 sheet 缺失直接中断全部处理。
            if (ObjectUtil.isEmpty(headMap)) {
                continue;
            }
            isExistSheet = Boolean.TRUE;

            // 预处理阶段的清洗文件挂在导入历史记录上，任务 errorUrl 保持为空；正式导入和确认导入才回填任务错误文件地址。
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

        // 构建待导入数据：包含表头校验、字段清洗、物流单预匹配、费用项解析和匹配结果回写；该阶段不直接落库。
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = buildImportDataList(importDTO, costImportEntity, cfgImportDetailList,
                successList, matchImportList, headList, headMap);

        //处理数据结束时间
        log.warn("结束处理时间 ={}",stopwatch.elapsed(TimeUnit.MILLISECONDS));


        if (CollUtil.isEmpty(importDataList)) {
            return Collections.emptyList();
        }
        // 预处理只产出清洗结果和导入历史，不执行费用新增/更新，用户确认清洗结果后再走正式导入或导入确认。
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
            return Collections.emptyList();
        }
        // 正式导入和导入确认共用落库链路；导入确认会额外返回待确认费用单，用于文件解析完成后统一确认。
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
        // 1. 校验表头唯一性和数据非空，避免后续按表头映射字段时出现歧义。
        validateHeadersAndData(headList, successList);
        // 2. 提取唯一键配置，唯一键决定导入行匹配哪张 ERP 物流单。
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = extractUniqueKeyList(cfgImportDetailList);
        // 3. 统一按配置取值并执行字段清洗，后续预查询和费用项处理均使用清洗后的值。
        prepareImportRowValues(cfgImportDetailList, headMap, successList);
        // 4. 按字段基础数据的单位属性标准化重量字段，确保预查询和业务校验使用 kg。
        List<JSONObject> validSuccessList = standardizeImportRowWeightValues(costImportEntity, cfgImportDetailList, successList, matchImportList, headMap);
        if (CollUtil.isEmpty(validSuccessList)) {
            return Collections.emptyList();
        }
        // 5. 构建 paramMap，按唯一键收集本批次所有识别号，用于一次性预查询物流单。
        Map<String, List<Object>> paramMap = buildParamMap(cfgImportDetailList, headMap, validSuccessList);
        // 6. 预查询物流单、物流费用单和费用配置，避免逐行导入时反复访问数据库。
        ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult = preQueryDbData(paramMap, costImportEntity);
        // 7. 币别字典跨服务获取，导入文件允许填币别编码或名称，因此需要先构建统一映射。
        List<DictCurrencyEntity> dictCurrencyList = sysUserFeign.currencyList();
        Map<String, String> currencyLookupMap = buildCurrencyLookupMap(dictCurrencyList);
        // 8. 汇率用于费用字段校验，避免导入不存在汇率的外币费用。
        Map<String, BigDecimal> currencyRateMap = buildCurrencyRateMap(dictCurrencyList);
        // 9. 费用项配置分为纵向和横向两种模板，后续解析方式和聚合粒度不同。
        boolean isVertical = isVerticalCostItem(cfgImportDetailList);
        if (isVertical) {
            return processVerticalCostItems(uniqueKeyList, cfgImportDetailList, preQueryResult, importDTO, costImportEntity, validSuccessList, matchImportList, headList, headMap, currencyLookupMap,currencyRateMap);
        } else {
            return processHorizontalCostItems(uniqueKeyList, cfgImportDetailList, preQueryResult, importDTO, costImportEntity, validSuccessList, matchImportList, headList, headMap, currencyLookupMap,currencyRateMap);
        }
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
        return uniqueKeyList;
    }

    /**
     * 统一生成导入字段值。
     *
     * @param cfgImportDetailList 导入配置明细
     * @param headMap 表头映射
     * @param successList Excel 数据
     * @return 无
     * @throws ServiceException 字段清洗失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void prepareImportRowValues(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, Map<Integer, String> headMap, List<JSONObject> successList) {
        // 每个配置明细生成独立缓存 key，避免同一 targetField 下多个费用项互相覆盖原始清洗值。
        Map<CfgLogisticsCostImportDetailEntity, String> detailValueKeyMap = cfgImportDetailList.stream()
                .collect(Collectors.toMap(detail -> detail, this::getDetailValueKey, (oldValue, newValue) -> oldValue));
        for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgImportDetailList) {
            // 先确定配置字段在 Excel 中的列下标，后续逐行取值时不再重复扫描表头。
            cfgDetail.setMappingIndex(getMapKey(headMap, cfgDetail.getSourceField()));
            // 清洗规则在配置中以存储格式保存，导入前解析成可执行规则列表。
            cfgDetail.setEtlRuleList(parseEtlRuleList(cfgDetail));
        }
        for (JSONObject rowData : successList) {
            for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgImportDetailList) {
                // 按配置取 Excel 值、默认值并执行清洗规则，后续匹配和费用解析都只使用该标准值。
                String value = resolveConfiguredValue(cfgDetail, rowData, headMap);
                rowData.set(detailValueKeyMap.get(cfgDetail), value);
                if (!CharSequenceUtil.equals("costItem", cfgDetail.getTargetField())) {
                    rowData.set(cfgDetail.getTargetField(), value);
                }
            }
        }
    }

    /**
     * 标准化导入重量字段。
     *
     * @param costImportEntity 费用导入配置
     * @param cfgImportDetailList 导入配置明细
     * @param successList Excel 数据
     * @param matchImportList 匹配结果数据
     * @param headMap 表头映射
     * @return 标准化成功的 Excel 数据
     * @throws ServiceException 物流商重量单位或重量值非法时抛出
     * @author jack
     * @date 2026/05/22
     */
    private List<JSONObject> standardizeImportRowWeightValues(CfgLogisticsCostImportEntity costImportEntity,
                                                              List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                              List<JSONObject> successList,
                                                              List<JSONObject> matchImportList,
                                                              Map<Integer, String> headMap) {
        if (CollUtil.isEmpty(cfgImportDetailList) || CollUtil.isEmpty(successList)) {
            return Collections.emptyList();
        }
        // 字段元数据用于判断哪些目标字段属于重量字段，以及这些字段应统一到什么标准单位。
        Map<String, CfgLogisticsCostImportFieldDTO.ListDTO> fieldMetaMap = cfgLogisticsCostImportFieldService.listByBusinessType(costImportEntity.getBusinessType())
                .stream()
                .filter(field -> CharSequenceUtil.isNotBlank(field.getField()))
                .collect(Collectors.toMap(CfgLogisticsCostImportFieldDTO.ListDTO::getField, field -> field, (oldValue, newValue) -> oldValue));
        // 构建目标字段索引，后续按 targetField 快速读取物流商重量单位等配置字段。
        Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField = buildDetailByTargetField(cfgImportDetailList);
        // 只处理字段元数据定义为重量且标准单位为 kg 的字段，避免误转换金额、尺寸等普通字段。
        List<CfgLogisticsCostImportDetailEntity> weightDetailList = buildWeightDetailList(cfgImportDetailList, fieldMetaMap);
        List<JSONObject> validSuccessList = new ArrayList<>();
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        for (JSONObject rowData : successList) {
            try {
                // 物流商可能按 g 或 kg 提供重量，先标准化单位，再统一换算重量字段值。
                String logisticsWeightUnit = normalizeLogisticsWeightUnit(getPreparedValueByTarget(rowData, detailByTargetField, LOGISTICS_WEIGHT_UNIT_FIELD));
                if (fieldMetaMap.isEmpty()) {
                    throw new ServiceException("未找到物流费用导入字段元数据，无法进行重量单位标准化");
                }
                // 后续业务统一按 kg 使用重量，因此清洗结果中的单位也一并写成标准单位。
                setPreparedValueByTarget(rowData, detailByTargetField, LOGISTICS_WEIGHT_UNIT_FIELD, STANDARD_WEIGHT_UNIT);
                for (CfgLogisticsCostImportDetailEntity cfgDetail : weightDetailList) {
                    // 单个重量字段转换失败只标记当前导入行失败，不影响同批其他行继续处理。
                    standardizeWeightValue(rowData, cfgDetail, logisticsWeightUnit);
                }
                validSuccessList.add(rowData);
            } catch (ServiceException e) {
                // 单行重量单位异常只影响当前行，避免一个物流商单位错误拖垮整批导入。
                markImportRowFailed(rowData, matchIndex, errorIndex, e.getMessage(), matchImportList);
            }
        }
        return validSuccessList;
    }

    /**
     * 标记导入行失败。
     *
     * @param rowData Excel 行数据
     * @param matchIndex 匹配结果列下标
     * @param errorIndex 错误信息列下标
     * @param errorMsg 错误信息
     * @param matchImportList 匹配结果数据
     * @return 无
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    /**
     * 构建目标字段到导入明细的索引。
     *
     * @param cfgImportDetailList 导入配置明细
     * @return 目标字段明细索引
     * @author jack
     * @date 2026/05/22
     */
    private Map<String, CfgLogisticsCostImportDetailEntity> buildDetailByTargetField(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList) {
        if (CollUtil.isEmpty(cfgImportDetailList)) {
            return Collections.emptyMap();
        }
        return cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.isNotBlank(detail.getTargetField()))
                .collect(Collectors.toMap(CfgLogisticsCostImportDetailEntity::getTargetField, detail -> detail, (oldValue, newValue) -> oldValue));
    }

    /**
     * 筛选需要标准化为 kg 的重量字段明细。
     *
     * @param cfgImportDetailList 导入配置明细
     * @param fieldMetaMap 字段元数据索引
     * @return 重量字段明细列表
     * @author jack
     * @date 2026/05/22
     */
    private List<CfgLogisticsCostImportDetailEntity> buildWeightDetailList(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                                          Map<String, CfgLogisticsCostImportFieldDTO.ListDTO> fieldMetaMap) {
        if (CollUtil.isEmpty(cfgImportDetailList) || CollUtil.isEmpty(fieldMetaMap)) {
            return Collections.emptyList();
        }
        return cfgImportDetailList.stream()
                .filter(cfgDetail -> {
                    CfgLogisticsCostImportFieldDTO.ListDTO fieldMeta = fieldMetaMap.get(cfgDetail.getTargetField());
                    if (Objects.isNull(fieldMeta) || !CharSequenceUtil.equals(CfgLogisticsCostImportFieldUnitTypeEnum.WEIGHT.getCode(), fieldMeta.getUnitType())) {
                        return false;
                    }
                    String standardUnit = CharSequenceUtil.blankToDefault(fieldMeta.getStandardUnit(), STANDARD_WEIGHT_UNIT);
                    return StrUtil.equalsIgnoreCase(STANDARD_WEIGHT_UNIT, standardUnit);
                })
                .collect(Collectors.toList());
    }

    private void markImportRowFailed(JSONObject rowData, Integer matchIndex, Integer errorIndex, String errorMsg, List<JSONObject> matchImportList) {
        if (Objects.nonNull(matchIndex)) {
            rowData.set(matchIndex.toString(), MATCH_FAIL);
        }
        if (Objects.nonNull(errorIndex)) {
            rowData.set(errorIndex.toString(), errorMsg);
        }
        matchImportList.add(rowData);
    }

    /**
     * 标准化物流商重量单位。
     *
     * @param logisticsWeightUnit 物流商重量单位
     * @return 标准化前的受支持单位
     * @throws ServiceException 物流商重量单位不支持时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String normalizeLogisticsWeightUnit(String logisticsWeightUnit) {
        String unit = CharSequenceUtil.blankToDefault(logisticsWeightUnit, WEIGHT_UNIT_KG).trim();
        if (StrUtil.equalsIgnoreCase(WEIGHT_UNIT_KG, unit)) {
            return STANDARD_WEIGHT_UNIT;
        }
        if (StrUtil.equalsIgnoreCase(WEIGHT_UNIT_G, unit)) {
            return WEIGHT_UNIT_G;
        }
        throw new ServiceException(CharSequenceUtil.format("物流商重量单位【{}】不支持，仅支持 KG 或 g", logisticsWeightUnit));
    }

    /**
     * 标准化单个重量字段值。
     *
     * @param rowData Excel 行数据
     * @param cfgDetail 导入配置明细
     * @param logisticsWeightUnit 物流商重量单位
     * @return 无
     * @throws ServiceException 重量值无法转换为数值时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void standardizeWeightValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity cfgDetail, String logisticsWeightUnit) {
        String value = getPreparedValue(rowData, cfgDetail);
        if (CharSequenceUtil.isBlank(value)) {
            return;
        }
        try {
            BigDecimal weight = new BigDecimal(normalizeAmountText(value));
            if (CharSequenceUtil.equals(WEIGHT_UNIT_G, logisticsWeightUnit)) {
                weight = weight.divide(BigDecimal.valueOf(1000));
            }
            setPreparedValue(rowData, cfgDetail, weight.stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            throw new ServiceException(CharSequenceUtil.format("字段【{}】重量值【{}】无法转换为数值", cfgDetail.getTargetFieldName(), value));
        }
    }

    /**
     * 解析字段清洗规则。
     *
     * @param cfgDetail 导入配置明细
     * @return 字段清洗规则列表
     * @throws ServiceException 字段清洗规则格式错误时抛出
     * @author jack
     * @date 2026/05/22
     */
    private List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> parseEtlRuleList(CfgLogisticsCostImportDetailEntity cfgDetail) {
        if (CollUtil.isNotEmpty(cfgDetail.getEtlRuleList())) {
            return cfgDetail.getEtlRuleList().stream()
                    .sorted(Comparator.comparing(rule -> Optional.ofNullable(rule.getIndex()).orElse(0)))
                    .collect(Collectors.toList());
        }
        return CfgLogisticsCostImportEtlRuleHelper.parseStorage(cfgDetail.getEtlRuleListStorage());
    }

    /**
     * 获取配置字段值。
     *
     * @param cfgDetail 导入配置明细
     * @param rowData Excel 行数据
     * @param headMap 表头映射
     * @return 清洗后的字段值
     * @throws ServiceException 字段清洗失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String resolveConfiguredValue(CfgLogisticsCostImportDetailEntity cfgDetail, JSONObject rowData, Map<Integer, String> headMap) {
        String value;
        if (ObjectUtil.isNotNull(cfgDetail.getMappingIndex())) {
            Object rawValue = rowData.get(cfgDetail.getMappingIndex().toString());
            value = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
        } else if (isDefaultValueField(cfgDetail.getTargetField())) {
            value = CharSequenceUtil.blankToDefault(cfgDetail.getDefaultValue(), "");
        } else {
            value = "";
        }
        return cleanFieldValue(value, cfgDetail, rowData, headMap);
    }

    /**
     * 执行字段清洗。
     *
     * @param value 字段值
     * @param cfgDetail 导入配置明细
     * @param rowData Excel 行数据
     * @param headMap 表头映射
     * @return 清洗后的字段值
     * @throws ServiceException 字段清洗失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String cleanFieldValue(String value, CfgLogisticsCostImportDetailEntity cfgDetail, JSONObject rowData, Map<Integer, String> headMap) {
        String result = value;
        List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList = cfgDetail.getEtlRuleList();
        if (CollUtil.isEmpty(ruleList)) {
            return result;
        }
        for (CfgLogisticsCostImportDetailDTO.EtlRuleDTO ruleDTO : ruleList) {
            result = applyEtlRule(result, ruleDTO, rowData, headMap, cfgDetail.getTargetFieldName());
        }
        return result;
    }

    /**
     * 执行单条清洗规则。
     *
     * @param value 字段值
     * @param ruleDTO 清洗规则
     * @param rowData Excel 行数据
     * @param headMap 表头映射
     * @param targetFieldName 目标字段名称
     * @return 清洗后的字段值
     * @throws ServiceException 字段清洗失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String applyEtlRule(String value, CfgLogisticsCostImportDetailDTO.EtlRuleDTO ruleDTO, JSONObject rowData, Map<Integer, String> headMap, String targetFieldName) {
        Map<String, Object> params = CfgLogisticsCostImportEtlRuleHelper.resolveParams(ruleDTO);
        String type = ruleDTO.getType();
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), type)) {
            return applyReplaceRule(value, params);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(), type)) {
            return applySubstringRule(value, params);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode(), type)) {
            return convertNumberSign(value, false, targetFieldName);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_NEGATIVE.getCode(), type)) {
            return convertNumberSign(value, true, targetFieldName);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(), type)) {
            return applyFillEmptyRule(value, params, rowData, headMap);
        }
        throw new ServiceException(CharSequenceUtil.format("字段【{}】清洗规则类型【{}】不支持", targetFieldName, type));
    }

    /**
     * 执行字符替换规则。
     *
     * @param value 字段值
     * @param params 规则参数
     * @return 清洗后的字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String applyReplaceRule(String value, Map<String, Object> params) {
        String sourceText = CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "sourceText");
        String mode = CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "mode");
        String targetText = CharSequenceUtil.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_EMPTY.getCode(), mode)
                ? "" : CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "targetText");
        return CharSequenceUtil.blankToDefault(value, "").replace(sourceText, targetText);
    }

    /**
     * 执行字段截取规则。
     *
     * @param value 字段值
     * @param params 规则参数
     * @return 清洗后的字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String applySubstringRule(String value, Map<String, Object> params) {
        String text = CharSequenceUtil.blankToDefault(value, "");
        String substringMode = CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "mode");
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_SYMBOL.getCode(), substringMode)) {
            return substringBySymbol(text, params);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_ORDER.getCode(), substringMode)) {
            return substringByOrder(text, params);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.CHINESE.getCode(), substringMode)) {
            return retainByPattern(text, CHINESE_PATTERN);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.ENGLISH.getCode(), substringMode)) {
            return retainByPattern(text, ENGLISH_PATTERN);
        }
        return text;
    }

    /**
     * 按符号截取字段。
     *
     * @param text 字段值
     * @param params 规则参数
     * @return 截取后的字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String substringBySymbol(String text, Map<String, Object> params) {
        String symbol = CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "symbol");
        int index = text.indexOf(symbol);
        if (index < 0) {
            return text;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.BEFORE.getCode(), CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "symbolPosition"))) {
            return text.substring(0, index);
        }
        return text.substring(index + symbol.length());
    }

    /**
     * 按顺序截取字段。
     *
     * @param text 字段值
     * @param params 规则参数
     * @return 截取后的字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String substringByOrder(String text, Map<String, Object> params) {
        Integer length = CfgLogisticsCostImportEtlRuleHelper.getIntegerParam(params, "length");
        if (Objects.isNull(length) || length >= text.length()) {
            return text;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlOrderDirectionEnum.RIGHT.getCode(), CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "orderDirection"))) {
            return text.substring(text.length() - length);
        }
        return text.substring(0, length);
    }

    /**
     * 按正则保留字符。
     *
     * @param text 字段值
     * @param pattern 正则表达式
     * @return 保留后的字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String retainByPattern(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(matcher.group());
        }
        return result.toString();
    }

    /**
     * 转换数值正负。
     *
     * @param value 字段值
     * @param negative 是否转为负数
     * @param targetFieldName 目标字段名称
     * @return 转换后的字段值
     * @throws ServiceException 字段值不是数值时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String convertNumberSign(String value, boolean negative, String targetFieldName) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }
        try {
            BigDecimal decimal = new BigDecimal(normalizeAmountText(value)).abs();
            return negative ? decimal.negate().toPlainString() : decimal.toPlainString();
        } catch (Exception e) {
            throw new ServiceException(CharSequenceUtil.format("字段【{}】无法转换为数值", targetFieldName));
        }
    }

    /**
     * 执行为空填充规则。
     *
     * @param value 字段值
     * @param params 规则参数
     * @param rowData Excel 行数据
     * @param headMap 表头映射
     * @return 清洗后的字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String applyFillEmptyRule(String value, Map<String, Object> params, JSONObject rowData, Map<Integer, String> headMap) {
        if (CharSequenceUtil.isNotBlank(value)) {
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlFillModeEnum.CUSTOM.getCode(), CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "mode"))) {
            return CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "fillValue");
        }
        Integer mappingIndex = getMapKey(headMap, CfgLogisticsCostImportEtlRuleHelper.getStringParam(params, "sourceField"));
        if (ObjectUtil.isNull(mappingIndex) || ObjectUtil.isEmpty(rowData.get(mappingIndex.toString()))) {
            return "";
        }
        return String.valueOf(rowData.get(mappingIndex.toString()));
    }

    /**
     * 判断字段是否允许使用默认值。
     *
     * @param targetField 目标字段
     * @return 是否允许使用默认值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private boolean isDefaultValueField(String targetField) {
        return CharSequenceUtil.equals(PAY_TYPE_FIELD, targetField)
                || CharSequenceUtil.equals(CURRENCY_FIELD, targetField)
                || CharSequenceUtil.equals(LOGISTICS_WEIGHT_UNIT_FIELD, targetField);
    }

    /**
     * 获取明细清洗值缓存键。
     *
     * @param cfgDetail 导入配置明细
     * @return 缓存键
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String getDetailValueKey(CfgLogisticsCostImportDetailEntity cfgDetail) {
        return DETAIL_VALUE_KEY_PREFIX + CharSequenceUtil.blankToDefault(cfgDetail.getId(), cfgDetail.getTargetField());
    }

    /**
     * 设置清洗后的明细值。
     *
     * @param rowData Excel 行数据
     * @param cfgDetail 导入配置明细
     * @param value 清洗后的明细值
     * @return 无
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private void setPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity cfgDetail, String value) {
        rowData.set(getDetailValueKey(cfgDetail), value);
        if (!CharSequenceUtil.equals("costItem", cfgDetail.getTargetField())) {
            rowData.set(cfgDetail.getTargetField(), value);
        }
    }

    /**
     * 获取清洗后的明细值。
     *
     * @param rowData Excel 行数据
     * @param cfgDetail 导入配置明细
     * @return 清洗后的明细值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String getPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity cfgDetail) {
        Object value = rowData.get(getDetailValueKey(cfgDetail));
        return ObjectUtil.isEmpty(value) ? "" : String.valueOf(value);
    }

    /**
     * 获取清洗后的目标字段值。
     *
     * @param rowData Excel 行数据
     * @param cfgImportDetailList 导入配置明细
     * @param targetField 目标字段
     * @return 清洗后的目标字段值
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private String getPreparedValueByTarget(JSONObject rowData, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, String targetField) {
        return getPreparedValueByTarget(rowData, buildDetailByTargetField(cfgImportDetailList), targetField);
    }

    /**
     * 按目标字段索引获取清洗后的目标字段值。
     *
     * @param rowData Excel 行数据
     * @param detailByTargetField 目标字段明细索引
     * @param targetField 目标字段
     * @return 清洗后的目标字段值
     * @author jack
     * @date 2026/05/22
     */
    private String getPreparedValueByTarget(JSONObject rowData, Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField, String targetField) {
        CfgLogisticsCostImportDetailEntity cfgDetail = detailByTargetField.get(targetField);
        return ObjectUtil.isNull(cfgDetail) ? "" : getPreparedValue(rowData, cfgDetail);
    }

    /**
     * 设置清洗后的目标字段值。
     *
     * @param rowData Excel 行数据
     * @param cfgImportDetailList 导入配置明细
     * @param targetField 目标字段
     * @param value 清洗后的目标字段值
     * @return 无
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private void setPreparedValueByTarget(JSONObject rowData, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, String targetField, String value) {
        setPreparedValueByTarget(rowData, buildDetailByTargetField(cfgImportDetailList), targetField, value);
    }

    /**
     * 按目标字段索引设置清洗后的目标字段值。
     *
     * @param rowData Excel 行数据
     * @param detailByTargetField 目标字段明细索引
     * @param targetField 目标字段
     * @param value 清洗后的目标字段值
     * @return 无
     * @author jack
     * @date 2026/05/22
     */
    private void setPreparedValueByTarget(JSONObject rowData, Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField, String targetField, String value) {
        CfgLogisticsCostImportDetailEntity cfgDetail = detailByTargetField.get(targetField);
        if (Objects.nonNull(cfgDetail)) {
            setPreparedValue(rowData, cfgDetail, value);
        }
    }

    // 构建 paramMap，收集唯一键所有唯一值
    private Map<String, List<Object>> buildParamMap(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList, Map<Integer, String> headMap, List<JSONObject> successList) {
        Map<String, List<Object>> paramMap = new HashMap<>();
        for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgImportDetailList) {
            if (!cfgDetail.getIsUniqueKey()) continue;
            List<Object> dataList = successList.stream()
                    .map(obj -> getPreparedValue(obj, cfgDetail))
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .map(obj -> (Object) obj)
                    .collect(Collectors.toList());
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
                SourceTypeEnum.LOGISTICS_BILL_COST.getCode() : SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode();

        // 按业务类型限定费用归属，避免自发货费用和尾程费用模板互相匹配费用项。
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(costAttribution);
        // 先按唯一识别号批量查物流单，再按模板来源过滤物流商或平台，防止相同单号跨来源误匹配。
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = filterLogisticsBillByImportSource(
                costImportEntity, logisticsBillService.listLogisticsBillByUniqueKey(paramMap));
        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if (CollUtil.isNotEmpty(logisticsBillVos)) {
            // 已有费用明细用于后续分类币种校验，导入新费用时必须和存量费用一起判断。
            List<String> logisticsBillCostIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getLogisticsBillCostId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
            List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostIdList);
            mainIdListMap = CollUtil.isEmpty(listByMainIdList) ? new HashMap<>() : listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        // 物流费用单按物流单明细预查询，后续新增/更新费用时直接在内存中匹配目标单据。
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        return new ImportHistoryRecordDTO.PreQueryResultDTO(sourceType,costAttribution,logisticsBillVos, mainIdListMap, logisticsBillCostList, cfgCostList);
    }

    /**
     * 按导入配置来源过滤物流单，避免相同识别号跨物流商或跨平台误匹配。
     */
    private List<LogisticsBillDTO.LogisticsBillVo> filterLogisticsBillByImportSource(CfgLogisticsCostImportEntity costImportEntity,
                                                                                    List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos) {
        if (CollUtil.isEmpty(logisticsBillVos) || ObjectUtil.isNull(costImportEntity)
                || CharSequenceUtil.isBlank(costImportEntity.getCfgType())
                || CharSequenceUtil.isBlank(costImportEntity.getDictPlatform())) {
            return logisticsBillVos;
        }
        String cfgType = costImportEntity.getCfgType();
        String dictPlatform = costImportEntity.getDictPlatform();
        if (CharSequenceUtil.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(), cfgType)) {
            return logisticsBillVos.stream()
                    .filter(vo -> CharSequenceUtil.equals(dictPlatform, vo.getLogisticsSupplierId()))
                    .collect(Collectors.toList());
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportCfgTypeEnum.PLATFORM.getCode(), cfgType)) {
            return logisticsBillVos.stream()
                    .filter(vo -> CharSequenceUtil.equals(dictPlatform, vo.getSalesPlatform()))
                    .collect(Collectors.toList());
        }
        return logisticsBillVos;
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
            Map<Integer, String> headMap,
            Map<String, String> currencyLookupMap,Map<String, BigDecimal> currencyRateMap) {
        List<LogisticsBillCostDTO.ImportDataDTO> importDataList = Collections.synchronizedList(new ArrayList<>());
        Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField = buildDetailByTargetField(cfgImportDetailList);
        // 纵向模板同一识别号可能有多行费用，先按唯一键分组后再汇总费用金额。
        Map<String, List<JSONObject>> map = successList.stream()
                .collect(Collectors.groupingBy(obj ->
                        uniqueKeyList.stream()
                                .map(uniqueKey -> getPreparedValue(obj, uniqueKey))
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
                    // 逐条处理每个jsonObject，分别校验和赋值
                    for (JSONObject jsonObject : value) {
                        jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                        List<String> costErrorMsgList = new ArrayList<>();
                        // 纵向模板一行代表一个费用项，rowFormatCost 负责解析费用项、实际/预估金额和币种。
                        List<TmsCostDetailDTO.UpdateDTO> updateList = rowFormatCost(successJson, jsonObject, preQueryResult.getCfgCostList(), cfgImportDetailList, detailByTargetField, headList, preQueryResult.getSourceType(), preQueryResult.getDictCostAttribution(), costErrorMsgList, currencyMap, currencyLookupMap,currencyRateMap);
                        if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                            jsonObject.set(matchIndex.toString(), MATCH_FAIL);
                            jsonObject.set(errorIndex.toString(), FieldValidUtil.getMsgSort(costErrorMsgList));
                            // 行级费用解析失败只回写当前行，分组内其他成功行仍可继续参与后续匹配和汇总。
                            synchronized (matchImportList) { updateMatchResult(Collections.singletonList(jsonObject), matchIndex.toString(), errorIndex.toString(), costErrorMsgList, matchImportList); } ;
                            continue;
                        }
                        updateAllList.addAll(updateList);
                    }
                    List<JSONObject> costSuccessList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.get(matchIndex.toString()))).collect(Collectors.toList());
                    if (CollUtil.isEmpty(costSuccessList)) {
                        continue;
                    }
                    List<String> mainErrorMsgList = new ArrayList<>();
                    // 同费用项同费用类型不允许混合币种汇总，空币种也作为独立币种参与比较。
                    validateSameCostCurrency(updateAllList, preQueryResult.getCfgCostList(), mainErrorMsgList);
                    if (CollectionUtils.isNotEmpty(mainErrorMsgList)) {
                        synchronized (matchImportList) { updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList); } ;
                        continue;
                    }
                    // 汇总同识别号下相同费用项金额，负数费用在这里直接参与求和并保留负数。
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList);
                    try {
                        // 根据唯一识别号匹配物流单，并生成后续批量新增或更新物流费用所需的数据结构。
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getLogisticsBillVoList(), preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList, preQueryResult.getDictCostAttribution(), preQueryResult.getMainIdListMap());
                        if (importDataDTO != null) importDataList.add(importDataDTO);
                    } catch (Exception e) {
                        log.error("数据处理失败 ,e = {}", e.getMessage());
                        mainErrorMsgList.add(e.getMessage());
                    }
                    // 主数据匹配或业务校验失败时，分组内所有成功解析的费用行都需要回写同一业务错误。
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
    Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField = buildDetailByTargetField(cfgImportDetailList);
    Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);
        // 横向模板同一行包含多个费用列，但同一识别号仍可能出现多行，需要先按唯一键聚合。
        Map<String, List<JSONObject>> map = successList.stream()
                .collect(Collectors.groupingBy(obj ->
                        uniqueKeyList.stream()
                                .map(uniqueKey -> getPreparedValue(obj, uniqueKey))
                                .collect(Collectors.joining("_"))
                ));
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
                        // 横向模板按费用列解析，lineFormatCost 会把一行中的多个费用列转换成费用明细列表。
                        List<TmsCostDetailDTO.UpdateDTO> updateList = lineFormatCost(successJson, jsonObject, costErrorMsgList, preQueryResult.getCfgCostList(), cfgImportDetailList, detailByTargetField, headList, preQueryResult.getDictCostAttribution(), currencyLookupMap,currencyRateMap);
                        if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                            jsonObject.set(matchIndex.toString(), MATCH_FAIL);
                            jsonObject.set(errorIndex.toString(), FieldValidUtil.getMsgSort(costErrorMsgList));
                            // 单行横向费用列异常只影响当前行，其他同识别号行仍可继续聚合。
                            synchronized (matchImportList) { updateMatchResult(Collections.singletonList(jsonObject), matchIndex.toString(), errorIndex.toString(), costErrorMsgList, matchImportList); } ;
                            continue;
                        }
                        updateAllList.addAll(updateList);
                    }
                    List<JSONObject> costSuccessList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.get(matchIndex.toString()))).collect(Collectors.toList());
                    if (CollUtil.isEmpty(costSuccessList)) {
                        continue;
                    }
                    List<String> mainErrorMsgList = new ArrayList<>();
                    // 横向多行汇总前先校验币种，避免同费用项跨行出现 CNY 和 USD 后被错误相加。
                    validateSameCostCurrency(updateAllList, preQueryResult.getCfgCostList(), mainErrorMsgList);
                    if (CollectionUtils.isNotEmpty(mainErrorMsgList)) {
                        synchronized (matchImportList) {  updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList);} ;
                        continue;
                    }
                    // 同识别号多行费用按费用项和费用类型汇总，作为后续匹配单据的总费用。
                    List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList);
                    try {
                        // 根据物流商或平台 + 唯一识别号匹配物流单，必要时按订单重量分摊到多张物流单。
                        LogisticsBillCostDTO.ImportDataDTO importDataDTO = handleImportData(uniqueKeyList, successJson, mergeCostDetail, preQueryResult.getLogisticsBillCostList(),
                                preQueryResult.getLogisticsBillVoList(), preQueryResult.getCfgCostList(), importDTO, costImportEntity, mainErrorMsgList, preQueryResult.getDictCostAttribution(), preQueryResult.getMainIdListMap());
                        if (importDataDTO != null) importDataList.add(importDataDTO);
                    } catch (Exception e) {
                        log.error("数据处理失败 ,e = {}", e.getMessage());
                        mainErrorMsgList.add(e.getMessage());
                    }
                    // 主流程错误需要回写到同识别号下所有成功解析的行，确保清洗文件能定位整组失败原因。
                    synchronized (matchImportList) {  updateMatchResult(costSuccessList, matchIndex.toString(), errorIndex.toString(), mainErrorMsgList, matchImportList);} ;
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
     * 校验同一费用项同一费用类型的币种一致性。
     */
    private void validateSameCostCurrency(List<TmsCostDetailDTO.UpdateDTO> updateDetailList,
                                          List<TmsCfgCostEntity> tmsCfgCostList,
                                          List<String> errorMsgList) {
        if (CollUtil.isEmpty(updateDetailList)) {
            return;
        }
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> costTypeMap = updateDetailList.stream()
                .collect(Collectors.groupingBy(updateDTO -> updateDTO.getCfgCostId() + "_" + updateDTO.getType()));
        for (Map.Entry<String, List<TmsCostDetailDTO.UpdateDTO>> entry : costTypeMap.entrySet()) {
            List<String> currencyList = entry.getValue().stream()
                    .map(updateDTO -> CharSequenceUtil.blankToDefault(updateDTO.getCurrency(), ""))
                    .distinct()
                    .collect(Collectors.toList());
            if (currencyList.size() <= 1) {
                continue;
            }
            TmsCostDetailDTO.UpdateDTO updateDTO = entry.getValue().get(0);
            String costName = tmsCfgCostList.stream()
                    .filter(cfgCost -> CharSequenceUtil.equals(cfgCost.getId(), updateDTO.getCfgCostId()))
                    .map(TmsCfgCostEntity::getCostName)
                    .findFirst()
                    .orElse(updateDTO.getCfgCostId());
            errorMsgList.add("【" + costName + "】相同费用类型存在不同币别");
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
    private List<TmsCostDetailDTO.UpdateDTO> lineFormatCost (JSONObject successJson,JSONObject jsonObject,List<String> errorMsgList,List<TmsCfgCostEntity> cfgCostList,
                                                             List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField,List<String> headList,String cfgAttribution,
                                                             Map<String, String> currencyLookupMap,Map<String, BigDecimal> currencyRateMap) {
        String currency = getPreparedValueByTarget(jsonObject, detailByTargetField, CURRENCY_FIELD);
        // 币别校验必须基于清洗后的值，避免预处理后仍使用原始 Excel 值。
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
            String value = getPreparedValue(jsonObject, cfgDetailEntity);
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

                if (ObjectUtil.isNotEmpty(tmsCfgCostEntity) && CharSequenceUtil.isNotBlank(value)) {
                    String entryAmount = normalizeAmountText(value);
                    //校验费用值类型
                    List<String> errorMsg = FieldValidUtil.fieldValid(new TmsCostDetailDTO.CheckValueDTO(entryAmount));
                    if (CollUtil.isNotEmpty(errorMsg)) {
                        errorMsgList.add(tmsCfgCostEntity.getCostName() + errorMsg.get(0));
                        continue;
                    }
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //实际金额
                    updateDTO.setCostValue(toCostValue(entryAmount));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateDTO.setSourceType(sourceType);
                    updateDTO.setCurrency(currency);
                    updateList.add(updateDTO);
                }
            } else {
                successJson.set(cfgDetailEntity.getTargetField(), value);
            }
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
                                                            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,Map<String, CfgLogisticsCostImportDetailEntity> detailByTargetField,List<String> headList,String sourceType,
                                                            String costAttribution,List<String> errorMsgList,HashMap<String,String> currencyMap,
                                                            Map<String, String> currencyLookupMap,Map<String, BigDecimal> currencyRateMap) {
        List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
        String currency = getPreparedValueByTarget(jsonObject, detailByTargetField, CURRENCY_FIELD);
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

        String actualAmount = normalizeAmountText(getPreparedValueByTarget(jsonObject, detailByTargetField, "actualAmount"));
        String estimatedAmount = normalizeAmountText(getPreparedValueByTarget(jsonObject, detailByTargetField, "estimatedAmount"));

        //校验费用值类型
        List<String> errorMsg = FieldValidUtil.fieldValid(new TmsCostDetailDTO.CheckAmountDTO(actualAmount,estimatedAmount));
        if (CollUtil.isNotEmpty(errorMsg)) {
            errorMsgList.addAll(errorMsg);
            return updateList;
        }

        for (CfgLogisticsCostImportDetailEntity cfgDetailEntity : cfgImportDetailList) {
            String value = getPreparedValue(jsonObject, cfgDetailEntity);
            //判断导入字段是否是费用项
            if ("costItem".equals(cfgDetailEntity.getTargetField()) && CharSequenceUtil.equals(cfgDetailEntity.getSourceDetailField(), value)){
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

                if (StrUtil.isNotBlank(actualAmount)) {
                    TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                    //实际金额
                    updateDTO.setCostValue(toCostValue(actualAmount));
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
                    updateDTO.setCostValue(toCostValue(estimatedAmount));
                    updateDTO.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
                    updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                    updateDTO.setSourceType(sourceType);
                    updateDTO.setCurrency(currency);
                    updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                    updateList.add(updateDTO);
                }
            } else if (!CharSequenceUtil.equals("costItem", cfgDetailEntity.getTargetField())) {
                successJson.set(cfgDetailEntity.getTargetField(), value);
            }
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
     * 转换费用金额。
     *
     * @param amount 金额文本
     * @return 费用金额
     * @author jack
     * @date 2026/05/22
     */
    private BigDecimal toCostValue(String amount) {
        return new BigDecimal(amount);
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
            boolean preProcessing = CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType());
            Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocatedCostMap = new HashMap<>();
            if (!preProcessing && logisticsBillVoList.size() > 1) {
                // 一个识别号匹配多张物流单时，先计算每张单的订单重量，后续按重量比例分摊导入费用。
                Map<String, BigDecimal> weightMap = buildOrderWeightMap(logisticsBillVoList, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    return importDataDTO;
                }
                // 费用分摊只在正式导入/确认导入执行；预处理阶段不做深层业务分摊，避免提前阻断清洗结果生成。
                allocatedCostMap = allocateCostDetailMap(updateList, logisticsBillVoList, weightMap);
            }
            for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
                logisticsBillVo.setReconciliationMonth(importDTO.getReconciliationMonth());
                List<TmsCostDetailDTO.UpdateDTO> currentUpdateList = !preProcessing && logisticsBillVoList.size() > 1
                        ? allocatedCostMap.getOrDefault(logisticsBillVo.getDetailId(), Collections.emptyList())
                        : updateList;
                // 校验当前导入类型是否允许作用到目标物流单，例如已存在单据不能按新单重复导入。
                String thisImportType = checkCostImportData(excelDTO, logisticsBillCostList, logisticsBillVo, costAttribution, costImportEntity, errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    // 同识别号可能已经组装了部分单据数据，任一目标单校验失败都要清空，避免半成品进入批量落库。
                    clearImportData(importDataDTO);
                    return importDataDTO;
                }
                // 更新费用时优先匹配待确认或预估确认且核对中的费用单，避免覆盖已完成对账的数据。
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
                    clearImportData(importDataDTO);
                    return importDataDTO;
                }

                //校验分类币别
                checkCategoryCurrency(currentUpdateList,logisticsBillCostEntity,cfgCostList,mainIdListMap,errorMsgList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    // 分类币别需要结合存量费用明细校验，失败时本组费用不能继续新增或更新。
                    clearImportData(importDataDTO);
                    return importDataDTO;
                }

                // 预处理到这里已经完成匹配和校验，不再组装落库 DTO。
                if (preProcessing) {
                    continue;
                }

                // 格式化物流费用主表字段和费用明细，后续统一交给批量新增/更新方法落库。
                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(confirmTime,logisticsBillCostEntity.getLogisticsBillId(),logisticsBillCostEntity.getId(),logisticsBillCostEntity.getLogisticsBillDetailId(), excelDTO,
                        importDTO, currentUpdateList, errorMsgList, cfgCostList);
                // 金额、费用项或单据字段格式化失败时，清空已组装数据，确保本识别号不会部分落库。
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    clearImportData(importDataDTO);
                    return importDataDTO;
                }
                importDataDTO.setConfirmTime(confirmTime);
                importDataDTO.setImportType(thisImportType);

                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(thisImportType)){
                    // 按已有物流费用单新增费用项，适用于配置为“导入新增（按老单）”的模板。
                    List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO,currentUpdateList);
                    // 将新增费用项和对应物流费用单封装到 ImportDataDTO，后续批量落库时统一处理。
                    getLogisticsBillCostAddData(importDataDTO,logisticsBillCostEntity,dtoList);
                }else {
                    updateDataDTO.setId(logisticsBillCostEntity.getId());
                    List<LogisticsBillCostDTO.UpdateDTO> updateBillCostList = ObjectUtil.defaultIfNull(importDataDTO.getUpdateBillCostList(), new ArrayList<>());
                    updateBillCostList.add(updateDataDTO);
                    importDataDTO.setUpdateBillCostList(updateBillCostList);
                    // 费用明细通过 mainId 关联物流费用单，批量更新时必须先写入目标主表 ID。
                    currentUpdateList.forEach(obj -> obj.setMainId(updateDataDTO.getId()));
                    List<TmsCostDetailDTO.UpdateDTO> updateCfgCostList = ObjectUtil.defaultIfNull(importDataDTO.getUpdateCfgCostList(), new ArrayList<>());
                    updateCfgCostList.addAll(currentUpdateList);
                    importDataDTO.setUpdateCfgCostList(updateCfgCostList);
                }
            }
        } else {
            // 预处理阶段只需要告诉用户未匹配到已有物流单，不创建新物流单和费用单。
            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
                return importDataDTO;
            }
            // 模板允许按新单导入时，先组装物流单和物流单明细。
            getAddImportLogisticBill(importDataDTO,excelDTO,costAttribution);
            // 新单场景下同步组装物流费用单和费用明细，后续批量落库时一次性保存。
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(importDataDTO.getConfirmTime(),importDataDTO.getLogisticsBillEntity().getId(),IdWorker.getIdStr(),"",excelDTO,
                    importDTO, updateList, errorMsgList, cfgCostList);
            // 新单费用格式化失败时不继续设置新增列表，避免创建没有费用依据的物流单。
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return importDataDTO;
            }
            LogisticsBillCostDTO.AddDTO addDTO =BeanUtil.toBean(updateDataDTO, LogisticsBillCostDTO.AddDTO.class);
            importDataDTO.setAddBillCostList(Collections.singletonList(addDTO));
            // 新增物流费用单时，费用明细先绑定本次生成的费用单 ID，保证主从数据同批保存。
            updateList.forEach(obj -> obj.setMainId(addDTO.getId()));
            importDataDTO.setUpdateCfgCostList(updateList);

          /*  updateDataDTO.setCostDetailList(updateList);
            BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
            pairList.add(new Pair<>(update.getId(),confirmTime));*/
        }
        return importDataDTO;
    }

    private void clearImportData(LogisticsBillCostDTO.ImportDataDTO importDataDTO) {
        importDataDTO.setAddBillCostList(Collections.emptyList());
        importDataDTO.setUpdateBillCostList(Collections.emptyList());
        importDataDTO.setAddCfgCostList(Collections.emptyList());
        importDataDTO.setUpdateCfgCostList(Collections.emptyList());
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
        List<LogisticsBillCostDTO.AddDTO> oldAddBillCostList = ObjectUtil.defaultIfNull(importDataDTO.getAddBillCostList(), new ArrayList<>());
        oldAddBillCostList.addAll(addBillCostList);
        importDataDTO.setAddBillCostList(oldAddBillCostList);
        List<TmsCostDetailDTO.AddDTO> oldAddCfgCostList = ObjectUtil.defaultIfNull(importDataDTO.getAddCfgCostList(), new ArrayList<>());
        oldAddCfgCostList.addAll(addCfgCostList);
        importDataDTO.setAddCfgCostList(oldAddCfgCostList);
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
     * 订单重量 = SKU毛重(g) * 上游出库单实发数量，多物流单匹配时作为费用分摊依据。
     */
    private Map<String, BigDecimal> buildOrderWeightMap(List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList, List<String> errorMsgList) {
        Map<String, BigDecimal> weightMap = new HashMap<>();
        List<String> outstockIdList = logisticsBillVoList.stream().map(LogisticsBillDTO.LogisticsBillVo::getOutstockId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (logisticsBillVoList.stream().anyMatch(vo -> CharSequenceUtil.isBlank(vo.getOutstockId()))) {
            errorMsgList.add("无法获取上游出库单用于重量分摊");
            return weightMap;
        }
        List<SoOutstockDetailEntity> outstockDetailList = FeignQuery.create(SoOutstockDetailEntity.class).in(SoOutstockDetailEntity::getMainId, outstockIdList).list();
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
        Map<String, ProductPackEntity> productPackMap = CollUtil.isEmpty(productPackList) ? new HashMap<>() : productPackList.stream().collect(Collectors.toMap(ProductPackEntity::getSkuId, obj -> obj, (first, second) -> first));
        for (LogisticsBillDTO.LogisticsBillVo logisticsBillVo : logisticsBillVoList) {
            List<SoOutstockDetailEntity> detailList = outstockDetailMap.get(logisticsBillVo.getOutstockId());
            if (CollUtil.isEmpty(detailList)) {
                errorMsgList.add("无法获取上游出库明细用于重量分摊：" + logisticsBillVo.getOutstockCode());
                continue;
            }
            BigDecimal orderWeight = BigDecimal.ZERO;
            for (SoOutstockDetailEntity detailEntity : detailList) {
                ProductPackEntity productPackEntity = productPackMap.get(detailEntity.getSkuId());
                if (ObjectUtil.isNull(productPackEntity) || ObjectUtil.isNull(productPackEntity.getGrossWeight()) || productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) <= 0) {
                    errorMsgList.add("缺少SKU毛重，无法按重量分摊：" + detailEntity.getSkuNo());
                    continue;
                }
                if (ObjectUtil.isNull(detailEntity.getActualQty())) {
                    errorMsgList.add("出库实发数量为空，无法按重量分摊：" + detailEntity.getSkuNo());
                    continue;
                }
                orderWeight = orderWeight.add(productPackEntity.getGrossWeight().multiply(BigDecimal.valueOf(detailEntity.getActualQty())));
            }
            weightMap.put(logisticsBillVo.getDetailId(), orderWeight);
        }
        BigDecimal totalWeight = weightMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            errorMsgList.add("总订单重量为0，无法执行费用分摊");
        }
        return weightMap;
    }

    private Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocateCostDetailMap(List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                                List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList,
                                                                                Map<String, BigDecimal> weightMap) {
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> resultMap = new LinkedHashMap<>();
        BigDecimal totalWeight = logisticsBillVoList.stream().map(vo -> weightMap.getOrDefault(vo.getDetailId(), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
        for (TmsCostDetailDTO.UpdateDTO updateDTO : updateList) {
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
        copyDTO.setId(source.getId());
        copyDTO.setDictCostCategory(source.getDictCostCategory());
        copyDTO.setSourceType(source.getSourceType());
        copyDTO.setMainId(source.getMainId());
        copyDTO.setCostValue(source.getCostValue());
        copyDTO.setCfgCostId(source.getCfgCostId());
        copyDTO.setType(source.getType());
        copyDTO.setCurrency(source.getCurrency());
        return copyDTO;
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
