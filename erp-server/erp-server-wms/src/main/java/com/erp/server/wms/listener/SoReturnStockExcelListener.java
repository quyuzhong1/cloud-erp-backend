package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.OrderTypeEnum;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.wms.dto.excel.SoReturnStockImportExcelDTO;
import com.erp.model.wms.enums.ReturnTypeEnum;
import io.seata.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售退货入库导入
 * @author will
 * @date 2025/4/24 19:52
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SoReturnStockExcelListener extends AnalysisEventListener<SoReturnStockImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<SoReturnStockImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<SoReturnStockImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<SoReturnStockImportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(SoReturnStockImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CharSequenceUtil.isBlank(importExcelDTO.getTypeName())) {
            importExcelDTO.setTypeName(OrderTypeEnum.B2B.getName());
        }
        String returnTypeCode = ReturnTypeEnum.getCode(importExcelDTO.getReturnType());
        if(StringUtils.isBlank(returnTypeCode) && !CharSequenceUtil.isBlank(importExcelDTO.getReturnType())) {
            errorMsgList.add("退货类型错误，请选择正确的退货类型");
        }
        //币别
        if (CharSequenceUtil.isBlank(importExcelDTO.getCurrencyStr())) {
            importExcelDTO.setCurrencyStr(CurrencyEnum.CNY.getCurrencyName());
        }
        //时间
        if (CharSequenceUtil.isBlank(importExcelDTO.getBillDateStr())) {
            importExcelDTO.setBillDateStr(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            importExcelDTO.setBillDate(LocalDate.now());
        }else {
            try {
                importExcelDTO.setBillDate(LocalDateUtil.stringToLocalDate(importExcelDTO.getBillDateStr()));
            } catch (Exception e) {
                errorMsgList.add("入库日期格式错误");
            }
        }

        //存在错误数据则直接返回
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
}
