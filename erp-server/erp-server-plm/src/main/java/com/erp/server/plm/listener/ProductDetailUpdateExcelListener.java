package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.ProductDetailUpdateExcelDTO;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailUpdateExcelListener extends AnalysisEventListener<ProductDetailUpdateExcelDTO> {
    /**
     * 错误信息
     */
    private List<ProductDetailUpdateExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductDetailUpdateExcelDTO> dataList = new ArrayList<>();
    /**
     * 成功信息
     */
    private List<ProductDetailUpdateExcelDTO> successList = new ArrayList<>();
    /**
     * @Description 每解析一行数据回调一遍
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     * @param1 productDetailExcelDTO: 导入信息
     * @param2 analysisContext: 解析器上下文
     **/
    @Override
    public void invoke(ProductDetailUpdateExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(dto);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(dto);
        //存在错误数据则直接返回
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            dto.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(dto);
            return;
        }
        successList.add(dto);
    }

    /**
     * @param analysisContext: 解析器上下文
     * @Description 全部解析完回调此方法
     * @Author Luo_WG
     * @Date 2022/9/27 14:49
     **/
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }

    public List<ProductDetailUpdateExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ProductDetailUpdateExcelDTO> getSuccessList(){
        return successList;
    }

    public List<ProductDetailUpdateExcelDTO> getExcelDateList() {
        return dataList;
    }

}
