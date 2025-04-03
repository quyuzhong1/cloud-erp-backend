package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.SoPriceDetailImportExcelDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailExcelListener

 * @Date 2023-03-27 17:16
 * @Created by yl
 */
@Slf4j
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SoPriceDetailExcelListener extends AnalysisEventListener<SoPriceDetailImportExcelDTO> {

    /**
     * 导入数据，用于判断导入是否为空
     */
    private final List<SoPriceDetailImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    @Getter
    private final List<SoPriceDetailImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    @Getter
    private final List<SoPriceDetailImportExcelDTO> successList = new ArrayList<>();

    /**
     * 每解析一行数据回调一遍
     *
     * @param importExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-27 17:17
     */
    @Override
    public void invoke(SoPriceDetailImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(msgList)) {
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


    /**
     * 数据全部解析完成
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-27 17:17
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

}
