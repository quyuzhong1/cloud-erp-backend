package com.erp.server.plm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.CfgMoldAlertImportExcelDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.enums.CfgMoldReturnAlertRuleCountDimEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.CfgMoldAlertRuleService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jack
 * @Classname CfgMoldAlertExcelListener
 * @Date 2025-10-16
 */
public class CfgMoldAlertExcelListener extends AnalysisEventListener<CfgMoldAlertImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    //模具
    private Map<String, MoldInfoEntity> moldInfoMap;

    private final CfgMoldAlertRuleService cfgMoldAlertRuleService = SpringUtil.getBean(CfgMoldAlertRuleService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<CfgMoldAlertImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<CfgMoldAlertImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public CfgMoldAlertExcelListener(String taskId,
                                     String importType,
                                     Integer importCount,
                                     Map<String, MoldInfoEntity> moldInfoMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.moldInfoMap = moldInfoMap;;
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-08-26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(CfgMoldAlertImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }

        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        String moldCode = excelDTO.getMoldCode();
        if(StringUtils.isNotBlank(moldCode)){
            MoldInfoEntity moldInfoEntity = moldInfoMap.getOrDefault(moldCode, null);
            if(Objects.isNull(moldInfoEntity)){
                errorMsgList.add("模具档案不存在");
            }else{
                excelDTO.setMoldId(moldInfoEntity.getId());
                excelDTO.setMoldName(moldInfoEntity.getName());
                excelDTO.setSupplierId(moldInfoEntity.getSupplierId());
                excelDTO.setSupplierName(moldInfoEntity.getSupplierName());
                excelDTO.setSupplierCode(moldInfoEntity.getSupplierCode());
            }
        }

        String disabledName = excelDTO.getDisabledName();
        if(StringUtils.isNotBlank(disabledName)){
            excelDTO.setDisabled(DisabledEnum.ENABLE.getName().equals(disabledName) ? Boolean.FALSE : Boolean.TRUE);
        }

        String countDimName = excelDTO.getCountDimName();
        if(StringUtils.isNotBlank(countDimName)){
            excelDTO.setCountDim(CfgMoldReturnAlertRuleCountDimEnum.getCode(countDimName));
        }

        String startDateStr = excelDTO.getStartDateStr();
        if(StringUtils.isNotBlank(startDateStr)){
            LocalDate startDate = null;
            try {
                startDate = LocalDate.parse(startDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    startDate = LocalDate.parse(startDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        startDate = LocalDate.parse(startDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("开始日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setStartDate(startDate);
        }
        String endtDateStr = excelDTO.getEndDateStr();
        if(StringUtils.isNotBlank(endtDateStr)){
            LocalDate endtDate = null;
            try {
                endtDate = LocalDate.parse(endtDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    endtDate = LocalDate.parse(endtDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        endtDate = LocalDate.parse(endtDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("结束日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setEndDate(endtDate);
        }

        //结束日期不能小于开始日期
        if (Objects.nonNull(excelDTO.getEndDate()) && Objects.nonNull(excelDTO.getStartDate()) && excelDTO.getEndDate().isBefore(excelDTO.getStartDate())) {
            errorMsgList.add(ApiError.ERROR_92008.msg);
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            try {
                List<CfgMoldAlertImportExcelDTO> errorList2 = new ArrayList<>();
                cfgMoldAlertRuleService.handleImportSuccessList(successList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<CfgMoldAlertImportExcelDTO> errorList2 = new ArrayList<>();
                cfgMoldAlertRuleService.handleImportSuccessList(successList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    private void updateTask(Integer count){
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

}
