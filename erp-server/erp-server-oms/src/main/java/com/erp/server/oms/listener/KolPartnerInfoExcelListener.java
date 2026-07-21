package com.erp.server.oms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.ConvertUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolPartnerInfoImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.KolPartnerInfoService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname KolPartnerInfoExcelListener
 * @Date 2025-12-03
 */
public class KolPartnerInfoExcelListener extends AnalysisEventListener<KolPartnerInfoImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private final KolPartnerInfoService kolPartnerInfoService = SpringUtil.getBean(KolPartnerInfoService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<KolPartnerInfoImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<KolPartnerInfoImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public KolPartnerInfoExcelListener(String taskId,
                                       String importType,
                                       Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

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
    public void invoke(KolPartnerInfoImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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

//        // 达人昵称、详细地址、联系人不允许包含全角符号
//        if (ConvertUtil.containsFullWidthChar(excelDTO.getNickname())) {
//            errorMsgList.add("达人昵称不能包含全角符号");
//        }
//        if (ConvertUtil.containsFullWidthChar(excelDTO.getDetailAddress())) {
//            errorMsgList.add("详细地址不能包含全角符号");
//        }
//        if (ConvertUtil.containsFullWidthChar(excelDTO.getContactPerson())) {
//            errorMsgList.add("联系人不能包含全角符号");
//        }

        // 达人昵称、详细地址、联系人不允许包含特殊字符（表情符号等）
        if (ConvertUtil.containsSpecialChar(excelDTO.getNickname())) {
            errorMsgList.add("达人昵称存在特殊字符");
        }
        if (ConvertUtil.containsSpecialChar(excelDTO.getDetailAddress())) {
            errorMsgList.add("详细地址存在特殊字符");
        }
        if (ConvertUtil.containsSpecialChar(excelDTO.getContactPerson())) {
            errorMsgList.add("联系人存在特殊字符");
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(KolPartnerInfoImportExcelDTO::getNickname).distinct().collect(Collectors.toList());
                List<KolPartnerInfoImportExcelDTO> errorList2 = new ArrayList<>();
                kolPartnerInfoService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
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

    public static class UpdateListener extends AnalysisEventListener<KolPartnerInfoImportExcelDTO.UpdateExcelDTO> {

        private final String taskId;

        private final Integer importCount;

        @Getter
        private Integer count = 0;

        private final KolPartnerInfoService kolPartnerInfoService = SpringUtil.getBean(KolPartnerInfoService.class);

        private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

        @Getter
        private List<KolPartnerInfoImportExcelDTO.UpdateExcelDTO> errorList = new ArrayList<>();

        @Getter
        private List<KolPartnerInfoImportExcelDTO.UpdateExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

        public UpdateListener(String taskId, Integer importCount) {
            this.taskId = taskId;
            this.importCount = importCount;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void invoke(KolPartnerInfoImportExcelDTO.UpdateExcelDTO excelDTO, AnalysisContext analysisContext) {
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

            // 达人昵称、详细地址、联系人不允许包含特殊字符（表情符号等）
            if (ConvertUtil.containsSpecialChar(excelDTO.getNickname())) {
                errorMsgList.add("达人昵称存在特殊字符");
            }
            if (ConvertUtil.containsSpecialChar(excelDTO.getDetailAddress())) {
                errorMsgList.add("详细地址存在特殊字符");
            }
            if (ConvertUtil.containsSpecialChar(excelDTO.getContactPerson())) {
                errorMsgList.add("联系人存在特殊字符");
            }

            //存在错误数据则直接返回
            if (errorMsgList.size() > 0) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                return;
            }
            successList.add(excelDTO);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            if (!successList.isEmpty()){
                try {
                    List<String> errorNoList = errorList.stream().map(KolPartnerInfoImportExcelDTO.UpdateExcelDTO::getCode).distinct().collect(Collectors.toList());
                    List<KolPartnerInfoImportExcelDTO.UpdateExcelDTO> errorList2 = new ArrayList<>();
                    kolPartnerInfoService.handleImportUpdateSuccessList(successList, errorNoList, errorList2);
                    errorList.addAll(errorList2);
                }catch (Exception e){
                    successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                    errorList.addAll(successList);
                }
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

}
