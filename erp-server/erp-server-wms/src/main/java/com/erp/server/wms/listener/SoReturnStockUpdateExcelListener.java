package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.OrderTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MessageUtils;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.wms.dto.excel.SoReturnStockUpdateImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 销售退货入库单批量更新
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SoReturnStockUpdateExcelListener extends AnalysisEventListener<SoReturnStockUpdateImportExcelDTO> {

    /** 与 {@link com.erp.server.wms.service.impl.SoReturnInstockServiceImpl} 导入行数上限一致，解析阶段截断避免大文件 OOM */
    private static final int MAX_IMPORT_ROWS = 5000;

    private final List<SoReturnStockUpdateImportExcelDTO> allList = new ArrayList<>();

    private final List<SoReturnStockUpdateImportExcelDTO> errorList = new ArrayList<>();

    private final List<SoReturnStockUpdateImportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(SoReturnStockUpdateImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        if (allList.size() >= MAX_IMPORT_ROWS) {
            throw new ServiceException(ApiError.FILE_EXCEL_IMPORT_SIZE);
        }
        allList.add(importExcelDTO);

        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isNotBlank(importExcelDTO.getTypeName())) {
            String typeCode = resolveTypeCode(importExcelDTO.getTypeName());
            if (CharSequenceUtil.isBlank(typeCode)) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_BILL_TYPE_NOT_FOUND));
            } else {
                importExcelDTO.setTypeCode(typeCode);
            }
        }
        if (CharSequenceUtil.isNotBlank(importExcelDTO.getBillDateStr())) {
            try {
                importExcelDTO.setBillDate(LocalDateUtil.stringToLocalDate(importExcelDTO.getBillDateStr()));
            } catch (Exception e) {
                errorMsgList.add(MessageUtils.getMessage(ApiError.SO_RETURN_INSTOCK_IMPORT_BILL_DATE_INVALID));
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
