package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.ProductDetailImprotUpdateExcelDTO;
import io.seata.common.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailUpdateNotApproveExcelListener extends AnalysisEventListener<ProductDetailImprotUpdateExcelDTO> {
    /**
     * 错误信息
     */
    private List<ProductDetailImprotUpdateExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductDetailImprotUpdateExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<ProductDetailImprotUpdateExcelDTO> successList = new ArrayList<>();
    /**
     * @Description 每解析一行数据回调一遍
     * @Author jack
     * @Date 2025-01-19
     * @param1 ProductDetailUpdateNotApproveExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailImprotUpdateExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //添加数据用于判断是否为空
        dataList.add(dto);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(dto);
            return;
        }

        String batteryWeightStr = dto.getBatteryWeightStr();
        if(StringUtils.isNotBlank(batteryWeightStr)){
            //batteryWeightStr转BigDecimal
            BigDecimal batteryWeight = new BigDecimal(batteryWeightStr);
            dto.setBatteryWeight(batteryWeight);
        }

        successList.add(dto);
    }

    /**
     * @param analysisContext: 解析器上下文
     * @Description 全部解析完回调此方法
     * @Author jack
     * @Date 2025-01-19
     **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }

    public List<ProductDetailImprotUpdateExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ProductDetailImprotUpdateExcelDTO> getSuccessList(){
        return successList;
    }

    public List<ProductDetailImprotUpdateExcelDTO> getExcelDateList() {
        return dataList;
    }




}
