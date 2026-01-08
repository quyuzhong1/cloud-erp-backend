package com.erp.server.scm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.excel.AssetNoticeImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.service.AssetNoticeService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/10/20 11:50
 * @Param:
 * @Return:
 * @Description:
 **/
public class AssetNoticeExcelListener extends AnalysisEventListener<AssetNoticeImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<AssetNoticeImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<AssetNoticeImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据（原始Excel数据）
     */
    private List<AssetNoticeImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AssetNoticeService assetNoticeService = SpringUtil.getBean(AssetNoticeService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public AssetNoticeExcelListener(String taskId,
                                    String importType,
                                    Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    public void invoke(AssetNoticeImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }

        // 添加数据用于判断是否为空
        allList.add(importExcelDTO);

        // 基础字段验证
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        // 如果存在基础验证错误，记录错误并返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        // 日期转换处理
        convertDateFields(importExcelDTO, errorMsgList);

        // 如果存在日期转换错误，记录错误并返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        // 通过基础验证的数据添加到成功列表
        successList.add(importExcelDTO);
        
        // 批量处理
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(AssetNoticeImportExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<AssetNoticeImportExcelDTO> errorList2 = new ArrayList<>();
                assetNoticeService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO -> excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 日期转换处理
     */
    private void convertDateFields(AssetNoticeImportExcelDTO data, List<String> errorMsgList) {
        // 申请日期转换
        if (StringUtils.isNotBlank(data.getApplyDate())) {
            try {
                LocalDate applyDate = parseDate(data.getApplyDate());
                // 日期已通过parseDate处理，这里不需要额外操作
            } catch (Exception e) {
                errorMsgList.add("申请日期格式错误，请使用yyyy-MM-dd、yyyy/M/d或yyyy/MM/dd格式");
            }
        }
        
        // 计划交期转换
        if (StringUtils.isNotBlank(data.getPlanDeliveryDateStr())) {
            try {
                LocalDate planDeliveryDate = parseDate(data.getPlanDeliveryDateStr());
                // 日期已通过parseDate处理，这里不需要额外操作
            } catch (Exception e) {
                errorMsgList.add("计划交期格式错误，请使用yyyy-MM-dd、yyyy/M/d或yyyy/MM/dd格式");
            }
        }
    }


    private LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, dateTimeFormatter);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(dateStr, dateTimeFormatter2);
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(dateStr, dateTimeFormatter3);
                } catch (Exception e3) {
                    throw new RuntimeException("日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(AssetNoticeImportExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<AssetNoticeImportExcelDTO> errorList2 = new ArrayList<>();
                assetNoticeService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO -> excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    public List<AssetNoticeImportExcelDTO> getAllList(){
        return allList;
    }

    public List<AssetNoticeImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<AssetNoticeImportExcelDTO> getSuccessList(){
        return successList;
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
