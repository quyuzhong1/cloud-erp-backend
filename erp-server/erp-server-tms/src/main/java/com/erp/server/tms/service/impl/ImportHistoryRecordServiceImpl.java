package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
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
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.LogisticsLastMileCostExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.listener.ImportHistoryRecordExcelListener;
import com.erp.server.tms.mapper.ImportHistoryRecordMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    public BatchResultDTO preprocessingImportExcel(BaseDTO.ImportDTO importDTO,ImportHistoryRecordDTO.ImportDTO dto) {
        if (CharSequenceUtil.isNotBlank(importDTO.getFileName())) {
            throw new ServiceException(ApiError.LOGISTICS_IMPORT_FILE_NAME_NOT_FOUND);
        }
        //查询配置主表信息
        List<CfgLogisticsCostImportEntity> cfgLogisticsCostImportList = cfgLogisticsCostImportService.listByImport(importDTO.getFileName(), dto.getBusinessType(), dto.getCostType());
        if (CollUtil.isEmpty(cfgLogisticsCostImportList)) {
            return new BatchResultDTO(importDTO.getTaskId(),importDTO.getFileName(),"未找到配置信息",Boolean.TRUE);
        }
        //查询配置明细信息
        List<String> mainIdList = cfgLogisticsCostImportList.stream().map(CfgLogisticsCostImportEntity::getId).distinct().collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> importDetailList =  cfgLogisticsCostImportDetailService.listByMainIdList(mainIdList);
        if (CollUtil.isEmpty(importDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
        }
        Map<String,List<CfgLogisticsCostImportDetailEntity>> impotyDetailMap = importDetailList.stream().collect(Collectors.groupingBy(CfgLogisticsCostImportDetailEntity::getMainId));

        //下载文件
        byte[] bytes = fileFeign.downloadFile(importDTO.getFileUrl());

        //获取批次号，同一个文件同一次导入用同一个批次号
        String batchNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        dto.setCode(batchNo);

        for (CfgLogisticsCostImportEntity costImportEntity : cfgLogisticsCostImportList) {
            List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList = impotyDetailMap.get(costImportEntity.getId());
            if (CollUtil.isEmpty(cfgImportDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
            }
            //查询配置的唯一识别号
            List<CfgLogisticsCostImportDetailEntity> cfgDetailList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cfgDetailList)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_IS_UNIQUE_KEY_NOT_FOUND,importDTO.getFileName());
            }

            ImportHistoryRecordExcelListener excelListenerUtil = new ImportHistoryRecordExcelListener(costImportEntity,cfgImportDetailList,dto,importDTO);
            try {
                EasyExcel.read(new ByteArrayInputStream(bytes), excelListenerUtil)
                        .headRowNumber(costImportEntity.getHeaderRow())
                        .sheet(costImportEntity.getSheetName()).doRead();
            } catch (ExcelCommonException e) {
                log.error("导入格式错误！", e);
                throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
            }
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(importDTO.getTaskId());
            importResultDTO.setCount(excelListenerUtil.getCount());
            //导出错误数据
            List<JSONObject> errorList = excelListenerUtil.getErrorList();
            String url = "";
            if (CollectionUtils.isNotEmpty(errorList)) {
                String fileName = "物流商费用错误数据.xlsx";
                File file = ExcelUtil.customExportUtil(fileName, errorList, excelListenerUtil.getHeadList());
                if (!file.isDirectory()) {
                    url = FastDFSClientUtil.uploadFile(file, fileName);
                }
            }
            importResultDTO.setErrorUrl(url);
            importResultDTO.setFinishTime(LocalDateTime.now());
            importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
            importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            downloadTaskFeign.updateTask(importResultDTO);
        }
        return new BatchResultDTO(importDTO.getTaskId(),importDTO.getFileName(),"成功",Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(ImportHistoryRecordDTO.ImportDTO dto,BaseDTO.ImportDTO importDTO,CfgLogisticsCostImportEntity costImportEntity, List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                        List<JSONObject> successList, List<JSONObject> errorList, List<String> headList, Map<Integer, String> headMap) {
        if (headList.size() != headList.stream().distinct().count()) {
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_HEAD_EXIST);
        }
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //查询配置的唯一键字段
        List<CfgLogisticsCostImportDetailEntity> cfgDetailList = cfgImportDetailList.stream().filter(CfgLogisticsCostImportDetailEntity::getIsUniqueKey).collect(Collectors.toList());
        if (CollUtil.isEmpty(cfgDetailList)) {
            //上面有前置校验，这里只是为了防止空指针、遗漏
            log.warn("未配置唯一键字段，无法进行数据处理");
            return;
        }

        //获取费用项配置信息
        List<TmsCfgCostEntity> cfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.LAST_MILE.getCode());
        //错误信息序号
        Integer errorIndex = getMapKey(headMap, "错误信息");

        Map<String,List<Object>> paramMap = new HashMap<>();
        for (CfgLogisticsCostImportDetailEntity cfgDetail : cfgDetailList) {
            //必填字段所在列的下标
            Integer mappingIndex = getMapKey(headMap, cfgDetail.getSourceField());
            cfgDetail.setMappingIndex(mappingIndex);

            //必填字段下面的值
            List<Object> dataList = successList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get(mappingIndex.toString())))
                    .map(obj -> obj.get(mappingIndex.toString())).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(dataList)) {
                paramMap.put(cfgDetail.getTargetField(),dataList);
            }
        }

        //根据唯一字段进行数据查询
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillService.listLogisticsBillByUniqueKey(paramMap);
        //物流费用信息
        List<String> logisticsBillDetailIdList = logisticsBillVos.stream().map(LogisticsBillDTO.LogisticsBillVo::getDetailId).distinct().collect(Collectors.toList());
        List<LogisticsBillCostEntity> logisticsBillCostList = logisticsBillCostService.listByLogisticsBillDetailIdList(logisticsBillDetailIdList);
        for (JSONObject jsonObject :  successList) {
            //主数据
            JSONObject successJson = new JSONObject();
            //费用数据
            List<TmsCostDetailDTO.UpdateDTO> updateList = new ArrayList<>();
            //用于判断是否存在重复的费用数据
            JSONObject hasData = new JSONObject();

            List<String> errorMsgList = new ArrayList<>();

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                //字段名称
                String field = headList.get(Integer.parseInt(entry.getKey()));
                if (CharSequenceUtil.equals(field,"错误信息")) {
                    continue;
                }
                CfgLogisticsCostImportDetailEntity cfgDetailEntity = cfgDetailList.stream().filter(obj -> obj.getMappingIndex().equals(Integer.valueOf(entry.getKey()))).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(cfgDetailEntity)) {
                    log.warn("导入配置未找到字段【{}】的配置项",field);
                    continue;
                }
                //判断导入字段是否是费用项
                if ("costItem".equals(cfgDetailEntity.getTargetField())){
                    TmsCfgCostEntity tmsCfgCostEntity = cfgCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCostName(), field) && CharSequenceUtil.equals(obj.getDictCostAttribution(), DictCostAttributionEnum.LAST_MILE.getCode())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(tmsCfgCostEntity)) {
                        errorMsgList.add(CharSequenceUtil.format("费用管理尾程未找到该费用名称【{}】",field));
                        continue;
                    }
                    //校验后面数据是否存在重复的
                    if (ObjectUtil.isNotEmpty(tmsCfgCostEntity)) {
                        if (ObjectUtil.isNotEmpty(hasData.get(tmsCfgCostEntity.getId()))) {
                            errorMsgList.add("费用已存在，请勿重复导入");
                        }
                        hasData.set(tmsCfgCostEntity.getId(), tmsCfgCostEntity.getCostName());
                    }
                    if (ObjectUtil.isNotEmpty(tmsCfgCostEntity) && ObjectUtil.isNotEmpty(entry.getValue())) {
                        TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
                        updateDTO.setCostValue(new BigDecimal(entry.getValue().toString()));
                        updateDTO.setType(LogisticsBillCostTypeEnum.ACTUAL.getCode());
                        updateDTO.setCfgCostId(tmsCfgCostEntity.getId());
                        updateDTO.setDictCostCategory(tmsCfgCostEntity.getDictCostCategory());
                        updateDTO.setSourceType(SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode());
                        updateList.add(updateDTO);
                    }
                }
               successJson.set(cfgDetailEntity.getTargetField(),String.valueOf(entry.getValue()));
            }
            LogisticsLastMileCostExcelDTO excelDTO = BeanUtil.toBean(successJson, LogisticsLastMileCostExcelDTO.class);
            //基础验证
            List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollectionUtils.isNotEmpty(msgList)) {
                errorMsgList.addAll(msgList);
            }
            //物流商信息
            excelDTO.setLogisticsSupplierName(costImportEntity.getDictPlatform());
            //付款类型
            excelDTO.setPayType(logisticsPayTypeEnum.getName(excelDTO.getPayType()));

            //物流单明细
            List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVoList = logisticsBillVos.stream()
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getPlatformCode()) || obj.getPlatformCode().equals(excelDTO.getPlatformCode()))
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getSoDeliveryCode()) || obj.getSoDeliveryCode().equals(excelDTO.getSoDeliveryCode()))
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getSoCode()) || obj.getSourceCode().equals(excelDTO.getSoCode()))
                    .filter(obj -> CharSequenceUtil.isBlank(excelDTO.getTrackNo()) || obj.getTrackNo().equals(excelDTO.getTrackNo()))
                    .collect(Collectors.toList());
            if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_NEW.getCode().equals(costImportEntity.getImportType())) {
                if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                    errorMsgList.add("单号已存在无法新增，请核查单号");
                }
                if (CharSequenceUtil.isBlank(excelDTO.getTrackNo())) {
                    errorMsgList.add("物流单号不能为空");
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
                jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(jsonObject);
                continue;
            }

            LogisticsBillCostEntity logisticsBillCostEntity;

            //对账确认日期
            List<Pair<String,LocalDateTime>> pairList = new ArrayList<>();
            LocalDateTime confirmTime = CharSequenceUtil.isBlank(excelDTO.getConfirmTimeStr()) ? LocalDateTime.now() : LocalDateUtil.stringToLocalDateTime(excelDTO.getConfirmTimeStr());

            if (CollUtil.isNotEmpty(logisticsBillVoList)) {
                LogisticsBillDTO.LogisticsBillVo logisticsBillVo = logisticsBillVoList.get(0);
                logisticsBillVo.setReconciliationMonth(dto.getReconciliationMonth());
                //物流费用数据验证
                List<String> importMsgList = checkCostImportData(excelDTO,logisticsBillCostList,logisticsBillVo,DictCostAttributionEnum.LAST_MILE.getCode(), costImportEntity.getImportType());
                if (CollectionUtils.isNotEmpty(importMsgList)) {
                    errorMsgList.addAll(importMsgList);
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(jsonObject);
                    continue;
                }
                //物流费用单
                 logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj ->
                                CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())
                                        && CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                                        && CharSequenceUtil.equals(obj.getPayType(),excelDTO.getPayType()))
                        .findFirst().orElse(null);
                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(costImportEntity.getImportType()) && Objects.isNull(logisticsBillCostEntity)){
                    logisticsBillCostEntity = logisticsBillCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsBillDetailId(),logisticsBillVo.getDetailId())).findFirst().orElse(null);
                }
                if (ObjectUtil.isEmpty(logisticsBillCostEntity)) {
                    continue;
                }
                //预处理直接跳过处理
                if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),dto.getProcessingType())) {
                    continue;
                }
                //数据格式化
                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO, dto, updateList, errorList, jsonObject, errorIndex, cfgCostList);

                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(costImportEntity.getImportType())){
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
                if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.PRE_PROCESSING.getCode(),dto.getProcessingType())) {
                    continue;
                }
                LogisticsBillEntity logisticsBillEntity = addImportLogisticBill(excelDTO);
               //查询物流费用加下更新
                List<LogisticsBillCostEntity> logisticsBillCost = logisticsBillCostService.getByLogisticsBillIds(Collections.singletonList(logisticsBillEntity.getId()));
                if (CollUtil.isEmpty(logisticsBillCost)) {
                    throw new ServiceException("新增物流单后未找到对应的物流费用数据，无法进行后续处理");
                }
                logisticsBillCostEntity = logisticsBillCost.get(0);

                LogisticsBillCostDTO.UpdateDTO updateDataDTO = handleLogisticsBillCostImportData(logisticsBillCostEntity, excelDTO, dto, updateList, errorList, jsonObject, errorIndex, cfgCostList);
                updateDataDTO.setCostDetailList(updateList);
                BaseResultDTO.UpdateDTO update = logisticsBillCostService.update(updateDataDTO, Boolean.TRUE);
                pairList.add(new Pair<>(update.getId(),confirmTime));
            }

            //确认
            if (CharSequenceUtil.equals(ImportHistoryRecordProcessingTypeEnum.CONFIRM_IMPORT.getCode(),dto.getProcessingType())) {
                pairList.forEach(obj -> logisticsBillCostService.updateReconciliationStatus(obj.getKey(), ReconciliationStatusEnum.CONFIRMED.getCode(), obj.getValue()));
            }
        }
    }

    /**
     * 新增物流单
     * @author will
     * @date 2026/1/22 20:04
     * @param excelDTO
     * @return com.erp.model.tms.entity.LogisticsBillEntity
     */
    private LogisticsBillEntity addImportLogisticBill (LogisticsLastMileCostExcelDTO excelDTO) {
        //新增物流单，格式化物流费用
        LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
        //发货单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSoDeliveryCode())) {
            if (excelDTO.getSoDeliveryCode().startsWith("FHTZ")) {
                List<SoDeliveryNoticeEntity> list = FeignQuery.create(SoDeliveryNoticeEntity.class).eq(SoDeliveryNoticeEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                }
            } else if (excelDTO.getSoDeliveryCode().startsWith("FHDC")) {
                List<SoB2cDeliveryEntity> list = FeignQuery.create(SoB2cDeliveryEntity.class).eq(SoB2cDeliveryEntity::getCode, excelDTO.getSoDeliveryCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSoDeliveryCode(list.get(0).getId());
                    addDTO.setSourceCode(list.get(0).getSourceCode());
                }
            }
        }
        //销售订单信息
        if (CharSequenceUtil.isNotBlank(excelDTO.getSoCode())) {
            if (CharSequenceUtil.isNotBlank(addDTO.getSourceCode()) && !CharSequenceUtil.equals(addDTO.getSourceCode(),excelDTO.getSoCode())) {
                throw new ServiceException("发货单对应的销售订单与导入的销售订单不匹配，请核查");
            }
            if (excelDTO.getSoCode().startsWith("XSD")) {
                List<SoInfoEntity> list = FeignQuery.create(SoInfoEntity.class).eq(SoInfoEntity::getCode, excelDTO.getSoCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                }
            } else if (excelDTO.getSoCode().startsWith("XSDS")) {
                List<SoB2cEntity> list = FeignQuery.create(SoB2cEntity.class).eq(SoB2cEntity::getCode, excelDTO.getSoCode()).list();
                if (CollUtil.isNotEmpty(list)) {
                    addDTO.setSourceId(list.get(0).getId());
                }
            }
        }
        addDTO.setSourceCode(excelDTO.getSoCode());
        addDTO.setPlatformCode(excelDTO.getPlatformCode());
        addDTO.setSoDeliveryCode(excelDTO.getSoDeliveryCode());

        LogisticsBillDetailDTO.AddDTO addDetailDTO = new LogisticsBillDetailDTO.AddDTO();
        addDetailDTO.setTrackNo(excelDTO.getTrackNo());
        addDTO.setDetailList(Collections.singletonList(addDetailDTO));
        return logisticsBillService.add(addDTO);
    }

    /**
     * 数据处理
     * @author will
     * @date 2026/1/22 20:04
     * @param logisticsBillCostEntity
     * @param excelDTO
     * @param dto
     * @param updateList
     * @param errorList
     * @param jsonObject
     * @param errorIndex
     * @param cfgCostList
     * @return void
     */
    private LogisticsBillCostDTO.UpdateDTO handleLogisticsBillCostImportData(LogisticsBillCostEntity logisticsBillCostEntity, LogisticsLastMileCostExcelDTO excelDTO,
                                                   ImportHistoryRecordDTO.ImportDTO dto,
                                                   List<TmsCostDetailDTO.UpdateDTO> updateList,
                                                   List<JSONObject> errorList,
                                                   JSONObject jsonObject,
                                                   Integer errorIndex,
                                                   List<TmsCfgCostEntity> cfgCostList) {
        //数据赋值
        LogisticsBillCostDTO.UpdateDTO updateDataDTO = new LogisticsBillCostDTO.UpdateDTO();
        updateDataDTO.setId(logisticsBillCostEntity.getId());
        updateDataDTO.setBillingWeightLogistics(new BigDecimal(excelDTO.getBillingWeightStr()));
        updateDataDTO.setCurrency(CharSequenceUtil.isBlank(excelDTO.getCurrency()) ? CurrencyEnum.CNY.getCurrencyCode() : excelDTO.getCurrency());
        updateDataDTO.setPayType(excelDTO.getPayType());

        //对账月份
        updateDataDTO.setReconciliationMonth(dto.getReconciliationMonth());
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

        updateList.forEach(u -> u.setCurrency(updateDataDTO.getCurrency()));
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
                jsonObject.set(errorIndex.toString(),FieldValidUtil.getMsgSort(new ArrayList<>(costIdTypeListMap.values())));
                errorList.add(jsonObject);
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
            addDataDTO.setCurrency(updateDataDTO.getCurrency());

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
     * @param importType
     * @return List<String>
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/11 14:24
     */
    private List<String> checkCostImportData (LogisticsLastMileCostExcelDTO excelDTO
            , List<LogisticsBillCostEntity> logisticsBillCostList, LogisticsBillDTO.LogisticsBillVo logisticsBillVo , String dictCostAttribution, String importType) {
        List<String> errorMsgList = new ArrayList<>();
        if (CharSequenceUtil.isBlank(excelDTO.getPlatformCode()) && CharSequenceUtil.isBlank(excelDTO.getSoCode())
                && CharSequenceUtil.isBlank(excelDTO.getSoDeliveryCode()) && CharSequenceUtil.isBlank(excelDTO.getTrackNo())) {
            throw new ServiceException(ApiError.LOGISTICS_BILL_COST_IMPORT_NOT_EXIST_BILL);
        }
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
        LogisticsBillCostEntity logisticsBillCostEntity = null;
        if (CollUtil.isEmpty(logisticsBillCostEntityList)) {
            if(!CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode().equals(importType)){
                errorMsgList.add("未找到对应对账类型的物流费用单");
            }
        } else {
            if(logisticsBillCostEntityList.size() > 1) {
                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType)) {
                    long count = logisticsBillCostEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getReconciliationStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())).count();
                    if (count > 1) {
                        errorMsgList.add("出库单和运输单号对应对账类型的物流费用单有多条，请在页面编辑指定物流费用单");
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
                logisticsBillCostEntity = logisticsBillCostEntityList.get(0);
                if (!CharSequenceUtil.equals(logisticsBillCostEntity.getType(),dictCostAttribution)) {
                    errorMsgList.add(CharSequenceUtil.format("需要导入【{}】物流单费用信息",DictCostAttributionEnum.getName(dictCostAttribution)));
                }
                if (CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode().equals(importType) ) {
                    if (CharSequenceUtil.equals(ReconciliationStatusEnum.CONFIRMED.getCode(),logisticsBillCostEntity.getReconciliationStatus())) {
                        errorMsgList.add("物流费用单已确认不支持更新");
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
        }
        return errorMsgList;
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
        // 属性赋值
        for(ImportHistoryRecordDTO.ListDTO data : list) {
            //对账月份
            if (CharSequenceUtil.isNotBlank(data.getReconciliationMonth())) {
                data.setReconciliationMonthStr(LocalDateUtil.parseStrToLocalDate(data.getReconciliationMonth()).format(DateTimeFormatter.ofPattern("yyyy年MM月")));
            }
            //处理状态名称
            data.setStatusName(ImportHistoryRecordStatusEnum.getName(data.getStatus()));
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
            if (value.equals(targetValue)) {
                resultKey = key;
                // 如果只需要找到一个匹配的key，可以break
                break;
            }
        }
        return  resultKey;
    }

}
