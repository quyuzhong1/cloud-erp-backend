package com.erp.server.oms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.B2CManualDeliveryImportExcelDTO;
import com.erp.model.plm.dto.excel.ProductChangeImportExcelDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.plm.enums.ProductSalesPlatformEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.SoB2cImportService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class B2CManualDeliveryExcelListener extends AnalysisEventListener<B2CManualDeliveryImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    @Getter
    private Integer count = 0;

    private final String taskId;

    private final Integer importCount;

    /**
     * 错误信息
     */
    @Getter
    private List<B2CManualDeliveryImportExcelDTO> errorList = new ArrayList<>();

    private final SoB2cImportService soB2cImportService = SpringUtil.getBean(SoB2cImportService.class);

    @Getter
    private List<B2CManualDeliveryImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public B2CManualDeliveryExcelListener(String taskId,
                                      Integer importCount) {
        this.taskId = taskId;
        this.importCount = importCount;
    }


    @Override
    public void invoke(B2CManualDeliveryImportExcelDTO excelDTO, AnalysisContext context) {
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
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        String deliveryTimeStr = excelDTO.getDeliveryTimeStr();
        if (StringUtils.isNotBlank(deliveryTimeStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDateTime deliveryTime = null;
                try {
                    deliveryTime = LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e1) {
                    try {
                        deliveryTime = LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                excelDTO.setDeliveryTime(deliveryTime);
            } catch (Exception e) {
                errorMsgList.add("实际发货时间格式错误，请使用yyyy-MM-dd HH:mm:ss或yyyy/M/d HH:mm:ss格式");
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                return;
            }
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(B2CManualDeliveryImportExcelDTO::getCode).distinct().collect(Collectors.toList());
                List<B2CManualDeliveryImportExcelDTO> errorList2 = new ArrayList<>();
                soB2cImportService.handleManualDeliveryImportSuccessList(successList,errorNoList, errorList2);
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
