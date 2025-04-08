package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.ProductDetailUpdateApproveExcelDTO;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailUpdateApproveExcelListener extends AnalysisEventListener<ProductDetailUpdateApproveExcelDTO> {
    /**
     * 错误信息
     */
    private List<ProductDetailUpdateApproveExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductDetailUpdateApproveExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<ProductDetailUpdateApproveExcelDTO> successList = new ArrayList<>();
    /**
     * @Description 每解析一行数据回调一遍
     * @Author jack
     * @Date 2025-01-19
     * @param1 ProductDetailUpdateApproveExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailUpdateApproveExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(dto);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(dto);
            return;
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

    public List<ProductDetailUpdateApproveExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ProductDetailUpdateApproveExcelDTO> getSuccessList(){
        return successList;
    }

    public List<ProductDetailUpdateApproveExcelDTO> getExcelDateList() {
        return dataList;
    }

}
