package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
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
import com.erp.model.tms.dto.*;
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
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

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
    private LogisticsSupplierService logisticsSupplierService;
    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ImportHistoryRecordDTO.AddDTO addDTO) {
        ImportHistoryRecordEntity importHistoryRecordEntity = new ImportHistoryRecordEntity();
        BeanMapperUtils.copy(addDTO, importHistoryRecordEntity);

        // 数据处理
        handleData(importHistoryRecordEntity);

        log.info("开始新增物流授权单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        importHistoryRecordEntity.setCode(code);
        boolean save = super.save(importHistoryRecordEntity);
        if(!save) {
            throw new ServiceException("物流授权单保存失败");
        }

        return new BaseResultDTO.AddDTO(importHistoryRecordEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ImportHistoryRecordDTO.UpdateDTO addOrUpdateDTO) {
        ImportHistoryRecordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "物流授权单"));
        ImportHistoryRecordEntity importHistoryRecordEntity =  BeanMapperUtils.map(ImportHistoryRecordEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(importHistoryRecordEntity);
        log.info("编辑 开始修改物流授权单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(importHistoryRecordEntity);
        if(!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        return Boolean.TRUE;
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
    private void handleData(ImportHistoryRecordEntity importHistoryRecordEntity) {
        // TODO 验证数据 & 数据赋值
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
    public void confirmImportData(ImportHistoryRecordDTO.ImportSyncDTO importDTO,List<Pair<String, LocalDateTime>> confirmPairList) {
        if (CollUtil.isEmpty(confirmPairList)) {
            return;
        }
        if (!CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),importDTO.getProcessingType())) {
            return;
        }
        confirmPairList.forEach(obj -> logisticsBillCostService.confirmImport(obj.getKey(), ReconciliationStatusEnum.CONFIRMED.getCode(), obj.getValue()));
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
            List<JSONObject> errorList = excelListenerUtil.getMatchList();
            Map<Integer, String> headMap = excelListenerUtil.getHeadMap();
            //未找到表头直接跳过
            if (ObjectUtil.isEmpty(headMap)) {
                continue;
            }
            isExistSheet = Boolean.TRUE;

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
        if (!isExistSheet) {
            throw new ServiceException(ApiError.FILE_SHEET_NOT_EXIST);
        }
        return  BatchResultDTO.success(importSyncDTO.getTaskId(),importSyncDTO.getFileName(),"导入成功");
    }

    @Override
    public List<Pair<String, LocalDateTime>> handleImportSuccessList(ImportHistoryRecordDTO.ImportSyncDTO importDTO,CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                        List<JSONObject> successList, List<JSONObject> errorList, List<String> headList, Map<Integer, String> headMap) {
        if (headList.size() != headList.stream().distinct().count()) {
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_HEAD_EXIST);
        }
        List<Pair<String, LocalDateTime>> importConfirmList = new ArrayList<>();
        if (CollectionUtils.isEmpty(successList)) {
            return importConfirmList;
        }
        //查询配置的唯一键字段
        List<CfgLogisticsCostImportDetailEntity> uniqueKeyList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());
        if (CollUtil.isEmpty(uniqueKeyList)) {
            //上面有前置校验，这里只是为了防止空指针、遗漏
            log.warn("未配置唯一键字段，无法进行数据处理");
            return importConfirmList;
        }

        //查询配置类型
        String costAttribution = CharSequenceUtil.equals(costImportEntity.getBusinessType(),CfgLogisticsCostImportBusinessTypeEnum.LOGISTICS_BILL_COST.getCode()) ?
                DictCostAttributionEnum.SELF_DELIVER.getCode() : DictCostAttributionEnum.LAST_MILE.getCode();

        String sourceType = CharSequenceUtil.equals(costImportEntity.getBusinessType(),CfgLogisticsCostImportBusinessTypeEnum.LOGISTICS_BILL_COST.getCode()) ?
                SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode() : SourceTypeEnum.LOGISTICS_BILL_COST.getCode();

        //获取费用项配置信息
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(costAttribution);
        //匹配结果序号
        Integer matchIndex = getMapKey(headMap, MATCH_FIELD);
        //错误信息序号
        Integer errorIndex = getMapKey(headMap, ERROR_MSG);

        Map<String,List<Object>> paramMap = new HashMap<>();
        for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgImportDetailList) {
            //字段所在列的下标
            Integer mappingIndex = getMapKey(headMap, cfgDetail.getSourceField());
            if (ObjectUtil.isEmpty(mappingIndex)) {
                continue;
            }
            cfgDetail.setMappingIndex(mappingIndex);

            //非唯一直接跳过
            if (!cfgDetail.getIsUniqueKey()) {
                continue;
            }

            //唯一字段下面的值
            List<Object> dataList = successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(mappingIndex.toString())))
                    .map(obj -> obj.get(mappingIndex.toString())).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(dataList)) {
                paramMap.put(cfgDetail.getTargetField(),dataList);
            }
        }

        //查询字段所在下标
        long count = cfgImportDetailList.stream().map(CfgLogisticsCostImportDetailEntity::getMappingIndex).filter(ObjectUtil::isNotNull).count();
        if (count == 0) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_RECORD_HEAD_NOTFOUND);
        }

        //根据唯一字段进行数据查询
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = new ArrayList<>();
        try {
            logisticsBillVos = logisticsBillService.listLogisticsBillByUniqueKey(paramMap);
        } catch (Exception e) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_RECORD_UNIQUE_KEY_ERROR);
        }

        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        if(CollUtil.isNotEmpty(logisticsBillVos)) {
            List<String> logisticsBillCostIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getLogisticsBillCostId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
            List<TmsCostDetailEntity> listByMainIdList = tmsCostDetailService.listByMainIdList(logisticsBillCostIdList);
            mainIdListMap = CollUtil.isEmpty(listByMainIdList) ? new HashMap<>() : listByMainIdList.stream().collect(Collectors.groupingBy(TmsCostDetailEntity::getMainId));
        }
        //物流费用信息
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);

        //判断是否存在费用明细配置，则走明细项
        long costItemCount = cfgImportDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getTargetField(), "costItem") && CharSequenceUtil.isNotBlank(obj.getSourceDetailField())).count();
        if (costItemCount > 0) {
            /**
             * 存在则是走纵向费用项处理
             */
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
            for ( Map.Entry<String, List<JSONObject>> entry : map.entrySet()) {
                List<JSONObject> value = entry.getValue();
                //主数据
                JSONObject successJson = new JSONObject();

                List<TmsCostDetailDTO.UpdateDTO> updateAllList = new ArrayList<>();
                HashMap<String,String> currencyMap = new HashMap<>();
                for (JSONObject jsonObject : value) {
                    //初始化匹配成功
                    jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                    //错误数据
                    List<String> costErrorMsgList = new ArrayList<>();
                    //费用项数据合并
                    List<TmsCostDetailDTO.UpdateDTO> updateList = rowFormatCost(successJson, jsonObject, cfgCostList, cfgImportDetailList, headList,sourceType, costAttribution,costErrorMsgList,currencyMap);
                    //如果有错直接跳过不处理
                    if (CollectionUtils.isNotEmpty(costErrorMsgList)) {
                        jsonObject.set(matchIndex.toString(),MATCH_FAIL);
                        jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(costErrorMsgList));
                        errorList.add(jsonObject);
                        continue;
                    }
                    updateAllList.addAll(updateList);
                }
                //合并相同费用项的费用
                List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail = mergeTmsCostDetail(updateAllList);

                List<JSONObject> costSuccessList = value.stream().filter(obj -> !CharSequenceUtil.equals(MATCH_FAIL, (CharSequence) obj.get(matchIndex.toString()))).collect(Collectors.toList());
                if (CollUtil.isEmpty(costSuccessList)) {
                    continue;
                }

                //按主费用单新增或更新数据
                List<String> mainErrorMsgList = new ArrayList<>();
                try {
                    //新增或更新数据
                    List<Pair<String, LocalDateTime>> pairs = addOrUpdateData(uniqueKeyList, successJson, mergeCostDetail, logisticsBillCostList,
                            logisticsBillVos, cfgCostList, importDTO, costImportEntity, mainErrorMsgList, costAttribution, mainIdListMap);
                    //需要确认的数据
                    if (CollUtil.isNotEmpty(pairs)) {
                        importConfirmList.addAll(pairs);
                    }
                } catch (Exception e) {
                    log.error("费用分类币种校验异常", e);
                    mainErrorMsgList.add(e.getMessage());
                }
                //物流费用主信息错误处理
                for (JSONObject jsonObject : costSuccessList) {
                    //初始化匹配成功
                    jsonObject.set(matchIndex.toString(),MATCH_SUCCESS);
                    //判断错误信息是否为空
                    if (CollUtil.isNotEmpty(mainErrorMsgList)) {
                        jsonObject.set(matchIndex.toString(),MATCH_FAIL);
                        jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(mainErrorMsgList));
                        errorList.add(jsonObject);
                        continue;
                    }
                    //成功信息也要放到下载结果中
                    errorList.add(jsonObject);
                }
            }
        } else {
            /**
             * 不存在则是走横向费用项处理
             */
            for (JSONObject jsonObject :  successList) {
                //主数据
                JSONObject successJson = new JSONObject();
                jsonObject.set(matchIndex.toString(), MATCH_SUCCESS);
                //错误数据
                List<String> errorMsgList = new ArrayList<>();
                List<TmsCostDetailDTO.UpdateDTO> updateList = lineFormatCost( successJson,jsonObject,errorMsgList,cfgCostList,cfgImportDetailList,headList,costAttribution);
                //合并相同费用项的费用
                List<TmsCostDetailDTO.UpdateDTO> mergeCostDetail =  mergeTmsCostDetail(updateList);

                try {
                    //新增或更新数据
                    List<Pair<String, LocalDateTime>> pairs = addOrUpdateData(uniqueKeyList, successJson, mergeCostDetail, logisticsBillCostList,
                            logisticsBillVos, cfgCostList, importDTO, costImportEntity, errorMsgList, costAttribution, mainIdListMap);
                    //需要确认的数据
                    if (CollUtil.isNotEmpty(pairs)) {
                        importConfirmList.addAll(pairs);
                    }
                } catch (Exception e) {
                    log.error("费用分类币种校验异常", e);
                    errorMsgList.add(e.getMessage());
                }
                //判断错误信息是否为空
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    jsonObject.set(matchIndex.toString(),MATCH_FAIL);
                    jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(jsonObject);
                    continue;
                }
                //成功信息也要放到下载结果中
                errorList.add(jsonObject);
            }
        }
        return importConfirmList;
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
                                                             List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,List<String> headList,String costAttribution) {
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
                TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), cfgDetailEntity.getTargetDetailFieldName()) && CharSequenceUtil.equals(obj.getDictCostAttribution(), costAttribution)).findFirst().orElse(null);
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
            CfgLogisticsCostImportDetailEntity cfgDetailEntity = cfgImportDetailList.stream().filter(obj -> ObjectUtil.isNotNull(obj.getMappingIndex())
                    && obj.getMappingIndex().equals(Integer.valueOf(entry.getKey()))
                    && ((CharSequenceUtil.equals("costItem",obj.getTargetField()) && CharSequenceUtil.equals(obj.getSourceDetailField(),ObjectUtil.defaultIfNull(entry.getValue(),"").toString()))
                    || !CharSequenceUtil.equals("costItem",obj.getTargetField()))
            ).findFirst().orElse(null);
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
    private List<Pair<String,LocalDateTime>> addOrUpdateData(List<CfgLogisticsCostImportDetailEntity> uniqueKeyList ,JSONObject successJson,List<TmsCostDetailDTO.UpdateDTO> updateList, List<LogisticsBillCostEntity> logisticsBillCostList,
                                 List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos,List<TmsCfgCostEntity> cfgCostList, ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                 CfgLogisticsCostImportEntity costImportEntity,   List<String> errorMsgList,String costAttribution,Map<String, List<TmsCostDetailEntity>> mainIdListMap) {

        ImportHistoryRecordExcelDTO excelDTO = BeanUtil.toBean(successJson, ImportHistoryRecordExcelDTO.class);

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
            return Collections.emptyList();
        }

        LogisticsBillCostEntity logisticsBillCostEntity;

        //对账确认日期
        List<Pair<String,LocalDateTime>> pairList = new ArrayList<>();
        LocalDateTime confirmTime = CharSequenceUtil.isBlank(excelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(excelDTO.getConfirmTimeStr());

        if (CollUtil.isNotEmpty(logisticsBillVoList)) {
            LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(0);
            logisticsBillVo.setReconciliationMonth(importDTO.getReconciliationMonth());
            //物流费用数据验证
            String thisImportType = checkCostImportData(excelDTO, logisticsBillCostList, logisticsBillVo, costAttribution, costImportEntity, errorMsgList);
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return Collections.emptyList();
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
                return Collections.emptyList();
            }

            //校验分类币别
            checkCategoryCurrency(updateList,logisticsBillCostEntity,cfgCostList,mainIdListMap,errorMsgList);

            //预处理直接跳过处理
            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
                return Collections.emptyList();
            }
            //数据格式化
            LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO, importDTO, updateList, errorMsgList, cfgCostList);
            //有错误信息直接跳过不暂处理
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return Collections.emptyList();
            }

            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(thisImportType)){
                List<LogisticsBillCostDTO.AddDataDTO> dtoList = buildAddDTO(updateDataDTO,updateList);
                List<BaseResultDTO.AddDTO> addDTOS = logisticsBillCostService.addPayAndRefund(dtoList);
                pairList = addDTOS.stream()
                        .map(obj -> new Pair<String, LocalDateTime>(obj.getId(), confirmTime))
                        .collect(Collectors.toList());
            }else {
                updateDataDTO.setCostDetailList(updateList);
                BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                pairList.add(new Pair<>(update.getId(),confirmTime));
            }
        } else {
            //预处理直接跳过处理
            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),importDTO.getProcessingType())) {
                return Collections.emptyList();
            }
            LogisticsBillEntity logisticsBillEntity = addImportLogisticBill(excelDTO,costAttribution);
            //查询物流费用加下更新
            List<LogisticsBillCostEntity> logisticsBillCost = logisticsBillCostService.getByLogisticsBillIds(Collections.singletonList(logisticsBillEntity.getId()));
            if (CollUtil.isEmpty(logisticsBillCost)) {
                throw new ServiceException("新增物流单后未找到对应的物流费用数据，无法进行后续处理");
            }
            logisticsBillCostEntity = logisticsBillCost.get(0);

            LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO, importDTO, updateList, errorMsgList, cfgCostList);
            //有错误信息直接跳过不暂处理
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                return Collections.emptyList();
            }

            updateDataDTO.setCostDetailList(updateList);
            BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
            pairList.add(new Pair<>(update.getId(),confirmTime));
        }

        return pairList;
        /*//确认
        if (CollUtil.isEmpty(confirmPairList)) {
            return;
        }
        if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),importDTO.getProcessingType())) {
            confirmPairList.forEach(obj -> logisticsBillCostService.updateReconciliationStatus(obj.getKey(), ReconciliationStatusEnum.CONFIRMED.getCode(), obj.getValue()));
        }*/
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO regenerateImportExcel(String id) {
        ImportHistoryRecordEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "导入记录");
        }
        BaseDTO.ImportDTO importDTO = new BaseDTO.ImportDTO();
        importDTO.setFileName(entity.getFileName());
        importDTO.setFileUrl(entity.getFileUrl());
        ImportHistoryRecordDTO.ImportDTO dto = new ImportHistoryRecordDTO.ImportDTO();
        dto.setBusinessType(entity.getBusinessType());
        dto.setProcessingType(ImportHistoryRecordProcessingTypeEnum.IMPORT.getCode());
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
    private LogisticsBillEntity addImportLogisticBill (ImportHistoryRecordExcelDTO excelDTO,String costAttribution) {
        //新增物流单，格式化物流费用
        LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
        addDTO.setLogisticsSupplierId(excelDTO.getLogisticsSupplierId());

        //发货单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSoDeliveryCode())) {
            if (excelDTO.getSoDeliveryCode().startsWith("FHTZ")) {
                List<SoDeliveryNoticeEntity> list = FeignQuery.create(SoDeliveryNoticeEntity.class).eq(SoDeliveryNoticeEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                }
            } else if (excelDTO.getSoDeliveryCode().startsWith("FHDC")) {
                List<SoB2cDeliveryEntity> list = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                }
            } else {
                addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }
        //销售订单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSourceCode())) {
            if (CharSequenceUtil.isNotBlank(addDTO.getSourceCode()) && !CharSequenceUtil.equals(addDTO.getSourceCode(),excelDTO.getSourceCode())) {
                throw new ServiceException("发货单对应的销售订单与导入的销售订单不匹配，请核查");
            }
            if (excelDTO.getSourceCode().startsWith("XSD")) {
                List<SoInfoEntity> list = FeignQuery.create(SoInfoEntity.class).eq(SoInfoEntity::getCode, excelDTO.getSourceCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                    addDTO.setOrderType(OrderTypeEnum.B2B.getCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
                }
            } else if (excelDTO.getSourceCode().startsWith("XSDS")) {
                List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getCode, excelDTO.getSourceCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                    addDTO.setOrderType(OrderTypeEnum.B2C.getCode());
                    addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
                }
            } else {
                addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
            }
        }

        //订单类型默认其他
        if (CharSequenceUtil.isBlank(addDTO.getOrderType())) {
            addDTO.setOrderType(OrderTypeEnum.OTHER.getCode());
        }

        addDTO.setSourceCode(excelDTO.getSourceCode());
        addDTO.setPlatformCode(excelDTO.getPlatformCode());
        addDTO.setSoDeliveryCode(excelDTO.getSoDeliveryCode());
        String shipmentType = CharSequenceUtil.equals(costAttribution, DictCostAttributionEnum.SELF_DELIVER.getCode()) ?ShipmentTypeEnum.SELF_DELIVER.getCode() : ShipmentTypeEnum.PLATFORM_DELIVER.getCode();
        addDTO.setShipmentType(shipmentType);

        LogisticsBillDetailDTO.AddDTO addDetailDTO = new LogisticsBillDetailDTO.AddDTO();
        addDetailDTO.setTrackNo(excelDTO.getTrackNo());
        addDetailDTO.setTrackEnable(Boolean.FALSE);
        addDTO.setDetailList(Collections.singletonList(addDetailDTO));
        return logisticsBillService.add(addDTO);
    }

    /**
     * 数据处理
     * @author will
     * @date 2026/1/22 20:04
     * @param logisticsBillCostEntity
     * @param excelDTO
     * @param importDTO
     * @param updateList
     * @param cfgCostList
     * @return void
     */
    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData(LogisticsBillCostEntity logisticsBillCostEntity, ImportHistoryRecordExcelDTO excelDTO,
                                                                             ImportHistoryRecordDTO.ImportSyncDTO importDTO,
                                                                             List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                                             List<String> errorMsgList,
                                                                             List<TmsCfgCostEntity> cfgCostList) {
        //数据赋值
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setId(logisticsBillCostEntity.getId());
        updateDataDTO.setBillingWeightLogistics(CharSequenceUtil.isBlank(excelDTO.getBillingWeightLogistics()) ? null : new BigDecimal(excelDTO.getBillingWeightLogistics()));
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
        updateDataDTO.setPayType(excelDTO.getPayType());

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
        List<TmsCostDetailEntity> tmsCostDetailEntityList = tmsCostDetailService.listByMainIdList(Collections.singletonList(logisticsBillCostEntity.getId()));
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

}
