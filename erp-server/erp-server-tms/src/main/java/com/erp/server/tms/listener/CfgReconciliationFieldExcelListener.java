package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 对账字段配置导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CfgReconciliationFieldExcelListener extends AnalysisEventListener<CfgReconciliationFieldImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<CfgReconciliationFieldImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private final List<CfgReconciliationFieldImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<CfgReconciliationFieldImportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(CfgReconciliationFieldImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
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

        // 当前是否已存在
        long count = successList.stream()
                .filter(e ->
                        e.getReconciliationTypeName().equals(importExcelDTO.getReconciliationTypeName())
                                && e.getThirdName().equals(importExcelDTO.getThirdName())
                                && e.getErpFieldName().equals(importExcelDTO.getErpFieldName()))
                .count();
        if (count > 0){
            importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】【{}】【{}】当页配置已存在",
                    importExcelDTO.getReconciliationTypeName(),
                    importExcelDTO.getThirdName(),
                    importExcelDTO.getErpFieldName()
            ));
            errorList.add(importExcelDTO);
            return;
        }

        successList.add(importExcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(successList)){
            return;
        }
    }
}
