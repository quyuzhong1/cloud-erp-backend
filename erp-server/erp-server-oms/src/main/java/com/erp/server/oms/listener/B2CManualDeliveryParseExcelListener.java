package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.B2CManualDeliveryImportExcelDTO;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * B2C 手动发货 Excel 解析监听器（仅解析，不执行业务发货）
 */
public class B2CManualDeliveryParseExcelListener extends AnalysisEventListener<B2CManualDeliveryImportExcelDTO> {

    @Getter
    private final List<B2CManualDeliveryImportExcelDTO> successList = new ArrayList<>();

    @Getter
    private final List<B2CManualDeliveryImportExcelDTO> errorList = new ArrayList<>();

    @Override
    public void invoke(B2CManualDeliveryImportExcelDTO excelDTO, AnalysisContext context) {
        List<String> errorMsgList = new ArrayList<>();
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
                LocalDateTime deliveryTime;
                try {
                    deliveryTime = LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e1) {
                    deliveryTime = LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"));
                }
                excelDTO.setDeliveryTime(deliveryTime);
            } catch (Exception e) {
                errorMsgList.add("实际发货时间格式错误，请使用yyyy-MM-dd HH:mm:ss或yyyy/M/d HH:mm:ss格式");
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                return;
            }
        }

        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // parse only
    }
}
