package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 其他出库单导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OtherOutStockExcelListener extends AnalysisEventListener<OtherOutStockImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<OtherOutStockImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<OtherOutStockImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<OtherOutStockImportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(OtherOutStockImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
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
