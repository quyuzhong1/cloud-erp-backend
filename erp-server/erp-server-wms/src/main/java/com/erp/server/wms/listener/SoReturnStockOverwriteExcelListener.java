package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.business.enums.OrderTypeEnum;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.wms.dto.excel.SoReturnStockOverwriteImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 销售退货入库批量导入覆盖
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SoReturnStockOverwriteExcelListener extends AnalysisEventListener<SoReturnStockOverwriteImportExcelDTO> {

    private final List<SoReturnStockOverwriteImportExcelDTO> allList = new ArrayList<>();

    private final List<SoReturnStockOverwriteImportExcelDTO> errorList = new ArrayList<>();

    private final List<SoReturnStockOverwriteImportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(SoReturnStockOverwriteImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        allList.add(importExcelDTO);

        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isNotBlank(importExcelDTO.getTypeName())
                && CharSequenceUtil.isBlank(resolveTypeCode(importExcelDTO.getTypeName()))) {
            errorMsgList.add("单据类型错误，请选择正确的单据类型");
        }
        if (CharSequenceUtil.isNotBlank(importExcelDTO.getBillDateStr())) {
            try {
                importExcelDTO.setBillDate(LocalDateUtil.stringToLocalDate(importExcelDTO.getBillDateStr()));
            } catch (Exception e) {
                errorMsgList.add("入库日期格式错误");
            }
        }
        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        successList.add(importExcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    }

    private String resolveTypeCode(String typeName) {
        if (CharSequenceUtil.isBlank(typeName)) {
            return null;
        }
        String code = BillTypeEnum.getCodeByName(typeName);
        if (CharSequenceUtil.isNotBlank(code)) {
            return code;
        }
        code = OrderTypeEnum.getCodeByName(typeName);
        if (CharSequenceUtil.isNotBlank(code)) {
            return code;
        }
        for (BillTypeEnum billTypeEnum : BillTypeEnum.values()) {
            if (typeName.trim().equalsIgnoreCase(billTypeEnum.getCode())) {
                return billTypeEnum.getCode();
            }
        }
        for (OrderTypeEnum orderTypeEnum : OrderTypeEnum.values()) {
            if (typeName.trim().equalsIgnoreCase(orderTypeEnum.getCode())) {
                return orderTypeEnum.getCode();
            }
        }
        return null;
    }
}
