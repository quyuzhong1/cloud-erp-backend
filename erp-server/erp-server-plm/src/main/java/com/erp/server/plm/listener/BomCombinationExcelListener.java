package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.BomCombinationImportExcelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品导入监听
 * @date 2023/8/17 14:17
 */
public class BomCombinationExcelListener extends AnalysisEventListener<BomCombinationImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<BomCombinationImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<BomCombinationImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<BomCombinationImportExcelDTO> successList = new ArrayList<>();


    @Override
    public void invoke(BomCombinationImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        successList.add(importExcelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        return;
    }


    public List<BomCombinationImportExcelDTO> getAllList(){
        return allList;
    }

    public List<BomCombinationImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<BomCombinationImportExcelDTO> getSuccessList(){
        return successList;
    }
}
