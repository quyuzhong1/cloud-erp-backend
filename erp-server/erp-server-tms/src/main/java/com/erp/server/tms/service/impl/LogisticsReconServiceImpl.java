package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.dto.excel.LogisticsReconImportExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.entity.LogisticsReconDetailSubEntity;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity;
import com.erp.model.tms.enums.LogisticsReconCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.model.tms.enums.LogisticsReconRefMatchTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.mapper.LogisticsReconMapper;
import com.erp.server.tms.listener.LogisticsReconExcelListener;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import com.erp.server.tms.service.LogisticsReconService;
import com.erp.server.tms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_RECON;

/**
 * <p>
 * 物流商对账单（主表） 服务实现类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@Service
public class LogisticsReconServiceImpl
        extends SuperServiceImpl<LogisticsReconMapper, LogisticsReconEntity>
        implements LogisticsReconService {

    private static final String DOC_NAME = "物流商对账单";

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private LogisticsReconDetailService logisticsReconDetailService;

    @Resource
    private LogisticsReconDetailSubService logisticsReconDetailSubService;

    @Resource
    private LogisticsReconRefLogisticsBillService logisticsReconRefLogisticsBillService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;

    @Resource
    private CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService;


    @Override
    public List<LogisticsReconDTO.TabListDTO> tabList(PermissionsDTO param) {
        LogisticsReconDTO.PagingParamDTO searchParam = new LogisticsReconDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<LogisticsReconDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        if (list == null) {
            list = new ArrayList<>();
        }
        // 不存在的状态补 0
        List<String> existStatus = list.stream()
                .map(LogisticsReconDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());
        for (String status : LogisticsReconCheckStatusEnum.getStatusList()) {
            if (!existStatus.contains(status)) {
                list.add(new LogisticsReconDTO.TabListDTO(status, 0));
            }
        }
        list.add(new LogisticsReconDTO.TabListDTO("all",
                list.stream().mapToInt(LogisticsReconDTO.TabListDTO::getCount).sum()));
        return list;
    }

    @Override
    public PagingVO<LogisticsReconDTO.ListDTO> paging(PagingDTO<LogisticsReconDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<LogisticsReconDTO.ListDTO> query =
                new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<LogisticsReconDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public LogisticsReconDTO.ViewDTO view(String id) {
        LogisticsReconEntity entity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
        LogisticsReconDTO.ViewDTO data = new LogisticsReconDTO.ViewDTO();
        data.setId(entity.getId());
        data.setReconciliationMonth(formatReconciliationMonth(entity.getReconciliationMonth()));
        data.setSupplierName(entity.getSupplierName());
        data.setTotalAmountStr(formatAmount(entity.getTotalAmount(), currencySymbol(entity.getCurrency())));
        return data;
    }


    @Override
    public List<BatchResultDTO> preprocessingImportExcel(LogisticsReconDTO.PreprocessingDTO dto) {
        // TODO 预处理：调用重构后的 buildSupplierBillDetailList（仅 ETL 清洗 + 校验，不落库）
        //  现阶段不复用 ImportHistoryRecordServiceImpl#importFile，原方法需重构后再接入
        throw new ServiceException(ApiError.LOGISTICS_RECON_PREPROCESS_IMPORT_NOT_READY);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO importExcel(LogisticsReconDTO.ImportDTO dto) {
        List<CfgLogisticsCostImportEntity> cfgList =
                cfgLogisticsCostImportService.listByImport(dto.getFileName(), dto.getBusinessType(), dto.getCostType());
        if (CollUtil.isEmpty(cfgList)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
        }
        List<String> cfgIds = cfgList.stream().map(CfgLogisticsCostImportEntity::getId).collect(Collectors.toList());
        List<CfgLogisticsCostImportDetailEntity> cfgDetails = cfgLogisticsCostImportDetailService.listByMainIdList(cfgIds);
        if (CollUtil.isEmpty(cfgDetails)) {
            throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
        }
        dto.setCfgLogisticsCostImportList(cfgList);
        dto.setImportDetailList(cfgDetails);
        String taskId = downloadTaskFeign.saveImportTask(DOC_NAME + "导入", IMPORT_TMS_LOGISTICS_RECON.getCode(), dto);
        log.info("[importExcel] submit taskId={} fileName={} cfgCount={}", taskId, dto.getFileName(), cfgList.size());
        return new BaseResultDTO.AddDTO(taskId, dto.getFileName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void executeImportTask(LogisticsReconDTO.ImportDTO dto) {
        try {
            List<CfgLogisticsCostImportEntity> cfgList = dto.getCfgLogisticsCostImportList();
            if (CollUtil.isEmpty(cfgList)) {
                cfgList = cfgLogisticsCostImportService.listByImport(dto.getFileName(), dto.getBusinessType(), dto.getCostType());
            }
            if (CollUtil.isEmpty(cfgList)) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORT_TEMPLATE_NOT_RECOGNIZED);
            }
            List<CfgLogisticsCostImportDetailEntity> allCfgDetails = dto.getImportDetailList();
            if (CollUtil.isEmpty(allCfgDetails)) {
                List<String> cfgIds = cfgList.stream().map(CfgLogisticsCostImportEntity::getId).collect(Collectors.toList());
                allCfgDetails = cfgLogisticsCostImportDetailService.listByMainIdList(cfgIds);
            }
            if (CollUtil.isEmpty(allCfgDetails)) {
                throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
            }
            Map<String, List<CfgLogisticsCostImportDetailEntity>> detailMap =
                    allCfgDetails.stream().collect(Collectors.groupingBy(CfgLogisticsCostImportDetailEntity::getMainId));
            int totalCount = 0;
            List<LogisticsReconImportExcelDTO> errorList = new ArrayList<>();
            for (CfgLogisticsCostImportEntity importCfg : cfgList) {
                List<CfgLogisticsCostImportDetailEntity> cfgDetails = detailMap.get(importCfg.getId());
                if (CollUtil.isEmpty(cfgDetails)) {
                    throw new ServiceException(ApiError.LOGISTICS_CFG_IMPORT_DETAIL_NOT_FOUND);
                }
                LogisticsReconEntity entity = createImportingMain(dto, importCfg);
                dto.setMainId(entity.getId());
                dto.setCode(entity.getCode());
                LogisticsReconExcelListener excelListener = readReconExcel(dto, importCfg);
                totalCount += excelListener.getRows().size();
                errorList.addAll(saveReconImportData(dto, importCfg, cfgDetails, excelListener));
            }
            BaseDTO.ImportResultDTO resultDTO = new BaseDTO.ImportResultDTO();
            resultDTO.setTaskId(dto.getTaskId());
            resultDTO.setCount(totalCount);
            resultDTO.setErrorUrl(uploadErrorFile(errorList));
            resultDTO.setFinishTime(LocalDateTime.now());
            resultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
            resultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
            downloadTaskFeign.updateTask(resultDTO);
        } catch (Exception e) {
            if (StrUtil.isNotBlank(dto.getMainId())) {
                lambdaUpdate()
                        .eq(LogisticsReconEntity::getId, dto.getMainId())
                        .set(LogisticsReconEntity::getImportFailReason, e.getMessage())
                        .update(new LogisticsReconEntity());
            }
            throw e;
        }
    }

    /**
     * 读取物流商对账单 Excel 数据
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param importCfg
     * @return LogisticsReconExcelListener
     */
    private LogisticsReconExcelListener readReconExcel(LogisticsReconDTO.ImportDTO dto, CfgLogisticsCostImportEntity importCfg) {
        byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
        LogisticsReconExcelListener excelListener = new LogisticsReconExcelListener();
        EasyExcel.read(new ByteArrayInputStream(bytes), excelListener)
                .headRowNumber(importCfg.getHeaderRow())
                .sheet(importCfg.getSheetName())
                .doRead();
        if (excelListener.getHeadMap().isEmpty()) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_EXCEL_HEAD_NOT_FOUND);
        }
        return excelListener;
    }

    /**
     * 保存物流商对账单导入数据
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param importCfg
     * @param cfgDetails
     * @param excelListener
     * @return List<LogisticsReconImportExcelDTO>
     */
    private List<LogisticsReconImportExcelDTO> saveReconImportData(LogisticsReconDTO.ImportDTO dto, CfgLogisticsCostImportEntity importCfg,
                                                                   List<CfgLogisticsCostImportDetailEntity> cfgDetails,
                                                                   LogisticsReconExcelListener excelListener) {
        List<LogisticsReconDetailEntity> detailList = new ArrayList<>();
        List<LogisticsReconDetailSubEntity> subList = new ArrayList<>();
        List<LogisticsReconImportExcelDTO> errorList = new ArrayList<>();
        Map<String, Integer> headerIndexMap = buildHeaderIndexMap(excelListener.getHeadMap());
        int rowNo = 1;
        for (Map<Integer, String> row : excelListener.getRows()) {
            LogisticsReconImportExcelDTO excelDTO = buildReconImportExcel(rowNo, row, headerIndexMap, cfgDetails);
            List<String> errorMsgList = FieldValidUtil.fieldValid(excelDTO);
            if (CollUtil.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                rowNo++;
                continue;
            }
            LogisticsReconDetailEntity detail = buildReconDetail(dto, rowNo++, excelDTO);
            detailList.add(detail);
            subList.add(buildReconDetailSub(dto, detail, excelDTO));
        }
        if (CollUtil.isNotEmpty(detailList)) {
            logisticsReconDetailService.saveBatch(detailList);
        }
        if (CollUtil.isNotEmpty(subList)) {
            logisticsReconDetailSubService.saveBatch(subList);
        }
        BigDecimal totalAmount = subList.stream()
                .map(LogisticsReconDetailSubEntity::getActualAmount)
                .filter(ObjectUtil::isNotEmpty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lambdaUpdate()
                .eq(LogisticsReconEntity::getId, dto.getMainId())
                .set(LogisticsReconEntity::getBusinessType, importCfg.getBusinessType())
                .set(LogisticsReconEntity::getCfgImportId, importCfg.getId())
                .set(LogisticsReconEntity::getCfgType, importCfg.getCfgType())
                .set(LogisticsReconEntity::getSupplierId, importCfg.getDictPlatform())
                .set(LogisticsReconEntity::getSupplierName, importCfg.getName())
                .set(LogisticsReconEntity::getSheetName, importCfg.getSheetName())
                .set(LogisticsReconEntity::getCheckStatus, LogisticsReconCheckStatusEnum.PENDING.getCode())
                .set(LogisticsReconEntity::getImportCount, detailList.size())
                .set(LogisticsReconEntity::getCostCount, subList.size())
                .set(LogisticsReconEntity::getTotalAmount, totalAmount)
                .set(LogisticsReconEntity::getCurrency, resolveCurrency(detailList))
                .update(new LogisticsReconEntity());
        return errorList;
    }

    /**
     * 构建表头索引
     * @author Will
     * @date: 2026/06/02
     * @param headMap
     * @return Map<String, Integer>
     */
    private Map<String, Integer> buildHeaderIndexMap(Map<Integer, String> headMap) {
        return headMap.entrySet().stream()
                .filter(entry -> StrUtil.isNotBlank(entry.getValue()))
                .collect(Collectors.toMap(entry -> entry.getValue().trim(), Map.Entry::getKey, (first, second) -> first));
    }

    /**
     * 构建导入中间对象并执行字段映射
     * @author Will
     * @date: 2026/06/02
     * @param rowNo
     * @param row
     * @param headerIndexMap
     * @param cfgDetails
     * @return LogisticsReconImportExcelDTO
     */
    private LogisticsReconImportExcelDTO buildReconImportExcel(int rowNo, Map<Integer, String> row,
                                                              Map<String, Integer> headerIndexMap,
                                                              List<CfgLogisticsCostImportDetailEntity> cfgDetails) {
        LogisticsReconImportExcelDTO excelDTO = new LogisticsReconImportExcelDTO();
        excelDTO.setNo(String.valueOf(rowNo));
        for (CfgLogisticsCostImportDetailEntity cfg : cfgDetails) {
            String target = StrUtil.blankToDefault(cfg.getTargetField(), cfg.getTargetDetailField());
            String value = getCellValue(row, headerIndexMap, cfg);
            if (StrUtil.isBlank(target) || StrUtil.isBlank(value)) {
                continue;
            }
            applyImportExcelField(excelDTO, target, value, cfg);
        }
        if (StrUtil.isNotBlank(excelDTO.getActualAmount())) {
            excelDTO.setActualAmountValue(parseAmount(excelDTO.getActualAmount(), false));
        }
        return excelDTO;
    }

    /**
     * 构建对账行明细
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param rowNo
     * @param excelDTO
     * @return LogisticsReconDetailEntity
     */
    private LogisticsReconDetailEntity buildReconDetail(LogisticsReconDTO.ImportDTO dto, int rowNo,
                                                        LogisticsReconImportExcelDTO excelDTO) {
        LogisticsReconDetailEntity detail = new LogisticsReconDetailEntity()
                .setMainId(dto.getMainId())
                .setRowNo(rowNo)
                .setSoCode(excelDTO.getSoCode())
                .setPlatformOrderNo(excelDTO.getPlatformOrderNo())
                .setTrackNo(excelDTO.getTrackNo())
                .setTransportNo(excelDTO.getTransportNo())
                .setSoDeliveryCode(excelDTO.getSoDeliveryCode())
                .setCurrency(excelDTO.getCurrency())
                .setPayType(excelDTO.getPayType())
                .setWeightLogistics(parseAmount(excelDTO.getWeightLogistics(), false))
                .setVolumeWeightLogistics(parseAmount(excelDTO.getVolumeWeightLogistics(), false))
                .setBillingWeightLogistics(parseAmount(excelDTO.getBillingWeightLogistics(), false))
                .setWeightUnit(excelDTO.getWeightUnit())
                .setThirdLength(parseAmount(excelDTO.getThirdLength(), false))
                .setThirdWidth(parseAmount(excelDTO.getThirdWidth(), false))
                .setThirdHeight(parseAmount(excelDTO.getThirdHeight(), false))
                .setCostCount(1);
        detail.setId(IdWorker.getIdStr());
        return detail;
    }

    /**
     * 构建费用项明细
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param detail
     * @param excelDTO
     * @return LogisticsReconDetailSubEntity
     */
    private LogisticsReconDetailSubEntity buildReconDetailSub(LogisticsReconDTO.ImportDTO dto,
                                                              LogisticsReconDetailEntity detail,
                                                              LogisticsReconImportExcelDTO excelDTO) {
        return new LogisticsReconDetailSubEntity()
                .setMainId(dto.getMainId())
                .setDetailId(detail.getId())
                .setSeqNo(1)
                .setCostName(excelDTO.getCostName())
                .setCfgCostName(excelDTO.getCostName())
                .setActualAmount(excelDTO.getActualAmountValue())
                .setEstimatedAmount(BigDecimal.ZERO)
                .setCurrency(StrUtil.blankToDefault(detail.getCurrency(), ""))
                .setMatchStatus(LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode())
                .setReconciliationStatus(LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
    }

    /**
     * 获取单元格值
     * @author Will
     * @date: 2026/06/02
     * @param row
     * @param headerIndexMap
     * @param cfg
     * @return String
     */
    private String getCellValue(Map<Integer, String> row, Map<String, Integer> headerIndexMap,
                                CfgLogisticsCostImportDetailEntity cfg) {
        String source = StrUtil.blankToDefault(cfg.getSourceDetailField(), cfg.getSourceField());
        Integer index = headerIndexMap.get(source);
        if (index == null) {
            return "";
        }
        return StrUtil.trimToEmpty(row.get(index));
    }

    /**
     * 映射导入字段到 Excel 中间对象
     * @author Will
     * @date: 2026/06/02
     * @param excelDTO
     * @param target
     * @param value
     * @param cfg
     * @return void
     */
    private void applyImportExcelField(LogisticsReconImportExcelDTO excelDTO, String target, String value,
                                       CfgLogisticsCostImportDetailEntity cfg) {
        if ("sourceCode".equals(target) || "soCode".equals(target)) {
            excelDTO.setSoCode(value);
        } else if ("platformCode".equals(target) || "platformOrderNo".equals(target)) {
            excelDTO.setPlatformOrderNo(value);
        } else if ("trackNo".equals(target)) {
            excelDTO.setTrackNo(value);
        } else if ("transportNo".equals(target)) {
            excelDTO.setTransportNo(value);
        } else if ("soDeliveryCode".equals(target)) {
            excelDTO.setSoDeliveryCode(value);
        } else if ("currency".equals(target)) {
            excelDTO.setCurrency(value);
        } else if ("payType".equals(target)) {
            excelDTO.setPayType(value);
        } else if ("thirdActualWeight".equals(target) || "weightLogistics".equals(target)) {
            excelDTO.setWeightLogistics(value);
        } else if ("volumeWeightLogistics".equals(target)) {
            excelDTO.setVolumeWeightLogistics(value);
        } else if ("billingWeightLogistics".equals(target)) {
            excelDTO.setBillingWeightLogistics(value);
        } else if ("logisticsWeightUnit".equals(target) || "weightUnit".equals(target)) {
            excelDTO.setWeightUnit(value);
        } else if ("thirdLength".equals(target)) {
            excelDTO.setThirdLength(value);
        } else if ("thirdWidth".equals(target)) {
            excelDTO.setThirdWidth(value);
        } else if ("thirdHeight".equals(target)) {
            excelDTO.setThirdHeight(value);
        } else if ("actualAmount".equals(target) || StrUtil.isNotBlank(cfg.getTargetDetailField())) {
            excelDTO.setCostName(StrUtil.blankToDefault(cfg.getTargetDetailFieldName(), cfg.getSourceDetailField()));
            excelDTO.setActualAmount(value);
        }
    }

    /**
     * 解析金额/数字
     * @author Will
     * @date: 2026/06/02
     * @param value
     * @param absolute
     * @return BigDecimal
     */
    private BigDecimal parseAmount(String value, Boolean absolute) {
        if (StrUtil.isBlank(value)) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = new BigDecimal(value.replace(",", "").trim());
        return Boolean.TRUE.equals(absolute) ? amount.abs() : amount;
    }

    /**
     * 导出并上传错误文件
     * @author Will
     * @date: 2026/06/02
     * @param errorList
     * @return String
     */
    private String uploadErrorFile(List<LogisticsReconImportExcelDTO> errorList) {
        if (CollUtil.isEmpty(errorList)) {
            return "";
        }
        List<LogisticsReconImportExcelDTO> sortedErrorList = errorList.stream()
                .sorted(Comparator.comparingInt(item -> {
                    try {
                        return Integer.parseInt(item.getNo());
                    } catch (Exception e) {
                        return 0;
                    }
                }))
                .collect(Collectors.toList());
        String fileName = "物流商对账单导入错误信息.xlsx";
        File file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, LogisticsReconImportExcelDTO.class);
        return file.isDirectory() ? "" : FastDFSClientUtil.uploadFile(file, fileName);
    }

    /**
     * 推导主表币别（多币别返回空）
     * @author Will
     * @date: 2026/06/02
     * @param detailList
     * @return String
     */
    private String resolveCurrency(List<LogisticsReconDetailEntity> detailList) {
        List<String> currencies = detailList.stream()
                .map(LogisticsReconDetailEntity::getCurrency)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return currencies.size() == 1 ? currencies.get(0) : "";
    }

    /**
     * 新建导入中主表
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @param importCfg
     * @return LogisticsReconEntity
     */
    private LogisticsReconEntity createImportingMain(LogisticsReconDTO.ImportDTO dto,
                                                     CfgLogisticsCostImportEntity importCfg) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DZ);
        LogisticsReconEntity entity = new LogisticsReconEntity()
                .setCode(code)
                .setReconciliationMonth(dto.getReconciliationMonth())
                .setBusinessType(importCfg.getBusinessType())
                .setCfgImportId(importCfg.getId())
                .setCfgType(importCfg.getCfgType())
                .setSupplierId(importCfg.getDictPlatform())
                .setSupplierName(importCfg.getName())
                .setSheetName(importCfg.getSheetName())
                .setFileUrl(dto.getFileUrl())
                .setFileName(dto.getFileName())
                .setCheckStatus(LogisticsReconCheckStatusEnum.IMPORTING.getCode())
                .setImportCount(0)
                .setCostCount(0);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_SAVE_FAILED);
        }
        return entity;
    }

    // ============================== 校验状态流转 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchUpdateCheckStatus(LogisticsReconDTO.UpdateCheckStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        List<LogisticsReconEntity> entities = lambdaQuery()
                .in(LogisticsReconEntity::getId, ids)
                .list();
        for (LogisticsReconEntity entity : entities) {
            try {
                validateCheckStatusTransition(entity, dto.getCheckStatus());
                LoginUser user = UserContext.getDefaultLoginUser();
                lambdaUpdate()
                        .eq(LogisticsReconEntity::getId, entity.getId())
                        .set(LogisticsReconEntity::getCheckStatus, dto.getCheckStatus())
                        .set(LogisticsReconEntity::getCheckUserId,
                                LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(dto.getCheckStatus())
                                        ? user.getUid() : "")
                        .set(LogisticsReconEntity::getCheckUserName,
                                LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(dto.getCheckStatus())
                                        ? user.getUserName() : "")
                        .set(LogisticsReconEntity::getCheckTime,
                                LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(dto.getCheckStatus())
                                        ? LocalDateTime.now() : null)
                        .update(new LogisticsReconEntity());
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE));
                String msg = StrUtil.format("用户【{}】将{}【{}】校验状态切换为【{}】，备注：{}",
                        user.getUserName(), DOC_NAME, entity.getCode(),
                        LogisticsReconCheckStatusEnum.getName(dto.getCheckStatus()), dto.getRemark());
                operateLogService.addModuleOperateLog(msg, null, entity.getId(), "校验状态切换");
            } catch (Exception e) {
                log.error("{}校验状态切换失败 id={}", DOC_NAME, entity.getId(), e);
                results.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return results;
    }

    private void validateCheckStatusTransition(LogisticsReconEntity entity, String targetStatus) {
        // 导入中不允许直接切换校验状态（需等导入完成进入待确认）
        if (LogisticsReconCheckStatusEnum.IMPORTING.getCode().equals(entity.getCheckStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_IMPORTING_CHECK_STATUS_FORBIDDEN);
        }
        // 仅允许在 待确认 / 已确认 之间流转
        if (!LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)
                && !LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_CHECK_STATUS_INVALID);
        }
        if (LogisticsReconCheckStatusEnum.PENDING.getCode().equals(targetStatus)) {
            int refCount = (int) logisticsReconRefLogisticsBillService.lambdaQuery()
                    .eq(com.erp.model.tms.entity.LogisticsReconRefLogisticsBillEntity::getMainId, entity.getId())
                    .count();
            if (refCount > 0) {
                throw new ServiceException(ApiError.LOGISTICS_RECON_MATCH_REF_EXISTS_ROLLBACK_FORBIDDEN);
            }
        }
    }

    // ============================== 合并 & 匹配 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchMatch(LogisticsReconDTO.BatchMatchDTO dto) {
        List<BatchResultDTO> results = new ArrayList<>(dto.getIds().size());
        for (String mainId : dto.getIds()) {
            try {
                LogisticsReconEntity entity = super.getByIdOpt(mainId)
                        .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
                if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                    throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_MATCH);
                }
                // TODO 合并 & 匹配：
                //  1. 把 detail + detail_sub 转 ImportDataDTO（依赖 ImportHistoryRecordServiceImpl#buildImportDataList 重构后的入口）
                //  2. 调 ImportHistoryRecordServiceImpl#importBatchAddOrUpdate(List<ImportDataDTO>, processingType, mainId) 重载
                //  3. 落 logistics_recon_ref_logistics_bill（按 detail 整批写）
                //  4. detail_sub.match_status 整批更新（主表匹配数/匹配状态由查询实时聚合，无需回写）
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE));
            } catch (Exception e) {
                log.error("[batchMatch] 失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchConfirmBill(LogisticsReconDTO.BatchConfirmBillDTO dto) {
        String targetStatus = dto.getReconciliationStatus();
        if (!ReconciliationStatusEnum.TO_BE_CONFIRM.getCode().equals(targetStatus)
                && !ReconciliationStatusEnum.CONFIRMED.getCode().equals(targetStatus)) {
            throw new ServiceException(ApiError.LOGISTICS_RECON_RECONCILIATION_STATUS_INVALID);
        }
        List<BatchResultDTO> results = new ArrayList<>(dto.getIds().size());
        LocalDateTime confirmTime = ReconciliationStatusEnum.CONFIRMED.getCode().equals(targetStatus)
                ? LocalDateTime.now() : null;
        for (String mainId : dto.getIds()) {
            try {
                LogisticsReconEntity entity = super.getByIdOpt(mainId)
                        .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, DOC_NAME));
                if (!LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                    throw new ServiceException(ApiError.LOGISTICS_RECON_ONLY_CONFIRMED_ALLOW_BILL_CONFIRM);
                }
                List<String> logisticsBillCostIds = logisticsReconRefLogisticsBillService.lambdaQuery()
                        .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                        .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
                        .list()
                        .stream()
                        .map(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId)
                        .filter(StrUtil::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList());
                if (CollUtil.isEmpty(logisticsBillCostIds)) {
                    throw new ServiceException(ApiError.LOGISTICS_RECON_MATCHED_BILL_COST_NOT_FOUND);
                }
                for (String logisticsBillCostId : logisticsBillCostIds) {
                    logisticsBillCostService.updateReconciliationStatus(logisticsBillCostId, targetStatus, confirmTime);
                }
                logisticsReconRefLogisticsBillService.lambdaUpdate()
                        .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                        .in(LogisticsReconRefLogisticsBillEntity::getLogisticsBillCostId, logisticsBillCostIds)
                        .set(LogisticsReconRefLogisticsBillEntity::getReconciliationStatus, targetStatus)
                        .update(new LogisticsReconRefLogisticsBillEntity());
                refreshDetailSubReconciliationStatus(mainId);
                String msg = StrUtil.format("用户【{}】将{}【{}】关联物流费用单对账状态更新为【{}】",
                        UserContext.getDefaultLoginUser().getUserName(), DOC_NAME, entity.getCode(),
                        ReconciliationStatusEnum.getName(targetStatus));
                operateLogService.addModuleOperateLog(msg, null, entity.getId(), "账单确认");
                results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE));
            } catch (Exception e) {
                log.error("[batchConfirmBill] 失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchUnbindMatch(LogisticsReconDTO.BatchUnbindMatchDTO dto) {
        // 按 detail_sub 维度逻辑删 ref + 还原 detail_sub.match_status = unmatched
        logisticsReconRefLogisticsBillService.removeByDetailSubIds(dto.getDetailSubIds());
        logisticsReconDetailSubService.batchUpdateMatchStatus(dto.getDetailSubIds(),
                LogisticsReconDetailMatchStatusEnum.UNMATCHED.getCode(), null);
        logisticsReconDetailSubService.lambdaUpdate()
                .in(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, dto.getDetailSubIds())
                .set(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getReconciliationStatus,
                        LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                .update(new com.erp.model.tms.entity.LogisticsReconDetailSubEntity());
        logisticsReconDetailSubService.lambdaQuery()
                .in(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, dto.getDetailSubIds())
                .list()
                .stream()
                .map(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getMainId)
                .distinct()
                .forEach(this::refreshDetailSubReconciliationStatus);
        // 解绑后匹配数由列表/详情查询实时聚合，无需回写主表

        List<BatchResultDTO> results = new ArrayList<>(dto.getDetailSubIds().size());
        for (String detailSubId : dto.getDetailSubIds()) {
            results.add(BatchResultDTO.success(detailSubId, detailSubId, OperationTypeEnum.UPDATE));
        }
        return results;
    }

    // ============================== 删除 / 导出 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchDelete(List<String> ids) {
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        List<LogisticsReconEntity> entities = lambdaQuery()
                .in(LogisticsReconEntity::getId, ids)
                .list();
        List<String> deletableIds = new ArrayList<>();
        for (LogisticsReconEntity entity : entities) {
            // 已确认的对账单不允许删除，导入中 / 待确认均可删
            if (LogisticsReconCheckStatusEnum.CONFIRMED.getCode().equals(entity.getCheckStatus())) {
                results.add(BatchResultDTO.fail(entity.getId(), entity.getCode(),
                        "已确认的对账单不允许删除"));
                continue;
            }
            deletableIds.add(entity.getId());
            results.add(BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE));
        }
        if (CollUtil.isNotEmpty(deletableIds)) {
            // 级联逻辑删 detail / detail_sub / ref
            logisticsReconRefLogisticsBillService.removeByMainIds(deletableIds);
            logisticsReconDetailSubService.removeByMainIds(deletableIds);
            logisticsReconDetailService.removeByMainIds(deletableIds);
            super.removeByIds(deletableIds);
        }
        return results;
    }

    @Override
    public void exportList(LogisticsReconDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask(DOC_NAME, EXPORT_TMS_LOGISTICS_RECON.getCode(), param);
    }

    // ============================== 主表汇总 ==============================

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshAggregate(String mainId) {
        if (ObjectUtil.isEmpty(mainId)) {
            return;
        }
        if (super.getById(mainId) == null) {
            return;
        }
        // 仅汇总导入行数（导入完成时调用一次）；匹配状态/匹配数不在主表存储，由查询实时聚合派生
        int importCount = (int) logisticsReconDetailService.lambdaQuery()
                .eq(LogisticsReconDetailEntity::getMainId, mainId)
                .count();
        lambdaUpdate()
                .eq(LogisticsReconEntity::getId, mainId)
                .set(LogisticsReconEntity::getImportCount, importCount)
                .update(new LogisticsReconEntity());
    }

    /**
     * 刷新费用项确认状态汇总（ref → detail_sub）
     * @author Will
     * @date 2026/6/2 16:30
     * @param mainId 对账单 id
     * @return void
     */
    private void refreshDetailSubReconciliationStatus(String mainId) {
        List<com.erp.model.tms.entity.LogisticsReconDetailSubEntity> subList =
                logisticsReconDetailSubService.lambdaQuery()
                        .eq(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getMainId, mainId)
                        .list();
        if (CollUtil.isEmpty(subList)) {
            return;
        }

        Map<String, List<LogisticsReconRefLogisticsBillEntity>> refMap =
                logisticsReconRefLogisticsBillService.lambdaQuery()
                        .eq(LogisticsReconRefLogisticsBillEntity::getMainId, mainId)
                        .eq(LogisticsReconRefLogisticsBillEntity::getIsDeleted, false)
                        .list()
                        .stream()
                        .collect(Collectors.groupingBy(LogisticsReconRefLogisticsBillEntity::getDetailSubId));

        List<String> confirmedSubIds = new ArrayList<>();
        List<String> partialSubIds = new ArrayList<>();
        List<String> unconfirmedSubIds = new ArrayList<>();
        for (com.erp.model.tms.entity.LogisticsReconDetailSubEntity sub : subList) {
            List<LogisticsReconRefLogisticsBillEntity> refs = refMap.get(sub.getId());
            int total = CollUtil.isEmpty(refs) ? 0 : refs.size();
            int confirmed = CollUtil.isEmpty(refs) ? 0 : (int) refs.stream()
                    .filter(ref -> ReconciliationStatusEnum.CONFIRMED.getCode().equals(ref.getReconciliationStatus()))
                    .count();
            String status = LogisticsReconReconciliationStatusEnum.resolve(confirmed, total);
            if (LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode().equals(status)) {
                confirmedSubIds.add(sub.getId());
            } else if (LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode().equals(status)) {
                partialSubIds.add(sub.getId());
            } else {
                unconfirmedSubIds.add(sub.getId());
            }
        }

        updateDetailSubReconciliationStatus(confirmedSubIds, LogisticsReconReconciliationStatusEnum.CONFIRMED.getCode());
        updateDetailSubReconciliationStatus(partialSubIds, LogisticsReconReconciliationStatusEnum.PARTIAL_CONFIRM.getCode());
        updateDetailSubReconciliationStatus(unconfirmedSubIds, LogisticsReconReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
    }

    /**
     * 批量更新费用项确认状态
     * @author Will
     * @date: 2026/06/02
     * @param ids
     * @param status
     * @return void
     */
    private void updateDetailSubReconciliationStatus(List<String> ids, String status) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        logisticsReconDetailSubService.lambdaUpdate()
                .in(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getId, ids)
                .set(com.erp.model.tms.entity.LogisticsReconDetailSubEntity::getReconciliationStatus, status)
                .update(new com.erp.model.tms.entity.LogisticsReconDetailSubEntity());
    }

    // ============================== private ==============================

    /**
     * 列表名称回填
     */
    private void fillList(List<LogisticsReconDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol, (first, second) -> first));
        for (LogisticsReconDTO.ListDTO data : list) {
            data.setCheckStatusName(LogisticsReconCheckStatusEnum.getName(data.getCheckStatus()));
            data.setReconciliationStatusName(
                    LogisticsReconReconciliationStatusEnum.getName(data.getReconciliationStatus()));
            // match_count 由 paging 子查询实时聚合得到，match_status 在此按 cost_count（费用项数）派生
            int matchCount = data.getMatchCount() == null ? 0 : data.getMatchCount();
            int costCount = data.getCostCount() == null ? 0 : data.getCostCount();
            String matchStatus = LogisticsReconMatchStatusEnum.resolve(matchCount, costCount);
            data.setMatchStatus(matchStatus);
            data.setMatchStatusName(LogisticsReconMatchStatusEnum.getName(matchStatus));
            String symbol = currencySymbolMap.getOrDefault(data.getCurrency(), "¥");
            data.setCurrencySymbol(symbol);
            data.setTotalAmountStr(formatAmount(data.getTotalAmount(), symbol));
            data.setMatchSuccessAmountStr(formatAmount(data.getMatchSuccessAmount(), symbol));
            data.setMatchFailAmountStr(formatAmount(data.getMatchFailAmount(), symbol));
        }
    }

    /**
     * 根据币别获取符号
     * @author Will
     * @date 2026/6/1 15:40
     * @param currency 币别
     * @return String
     */
    private String currencySymbol(String currency) {
        return FeignQuery.list(DictCurrencyEntity.class).stream()
                .filter(item -> StrUtil.equals(item.getId(), currency))
                .findFirst()
                .map(DictCurrencyEntity::getSymbol)
                .orElse("¥");
    }

    /**
     * 格式化对账月份展示文本
     * @author Will
     * @date 2026/6/1 15:55
     * @param reconciliationMonth 对账月份 yyyy-MM
     * @return String
     */
    private String formatReconciliationMonth(String reconciliationMonth) {
        if (StrUtil.isBlank(reconciliationMonth)) {
            return "";
        }
        String[] parts = reconciliationMonth.split("-");
        if (parts.length != 2) {
            return reconciliationMonth;
        }
        try {
            return parts[0] + "年" + Integer.parseInt(parts[1]) + "月份";
        } catch (NumberFormatException e) {
            return reconciliationMonth;
        }
    }

    /**
     * 格式化金额展示文本（带币别符号）
     * @author Will
     * @date 2026/6/1 15:40
     * @param amount 金额
     * @param symbol 币别符号
     * @return String
     */
    private String formatAmount(BigDecimal amount, String symbol) {
        if (amount == null) {
            return symbol + "0";
        }
        return symbol + amount.stripTrailingZeros().toPlainString();
    }

    /**
     * 匹配类型默认值（手动 / 新建物流单匹配 在对应方法内显式指定）
     */
    @SuppressWarnings("unused")
    private static final String DEFAULT_MATCH_TYPE = LogisticsReconRefMatchTypeEnum.AUTO.getCode();
}
