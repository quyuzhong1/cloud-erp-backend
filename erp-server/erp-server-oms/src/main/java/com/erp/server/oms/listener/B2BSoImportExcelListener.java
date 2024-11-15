package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.excel.B2BSoImportExcelDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * b2b 销售订单导入
 */
public class B2BSoImportExcelListener extends AnalysisEventListener<B2BSoImportExcelDTO> {


    Map<String, SoInfoDTO.AddDTO> map = new HashMap<>();

    /**
     * 错误的map
     */
    Map<String, String> errorMap = new HashMap<>();

    /**
     * 错误信息
     */
    private List<B2BSoImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<B2BSoImportExcelDTO> dataList = new ArrayList<>();

    /**
     * 数据校验成功的
     */
    private List<B2BSoImportExcelDTO> successList = new ArrayList<>();

    private B2BSoImportExcelListener() {
    }

    /**
     * 每解析一行执行一次
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(B2BSoImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        Integer rowNumber = analysisContext.readSheetHolder().getApproximateTotalRowNumber();
        if (rowNumber > 5000) {
            errorMsgList.add("导入最高要支持5000条");
        }
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        //数据校验是成功的
        successList.add(excelDTO);

    }

    public List<B2BSoImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<B2BSoImportExcelDTO> getSuccessList(){
        return successList;
    }

    public List<B2BSoImportExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * 所有执行完成后执行
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

}
