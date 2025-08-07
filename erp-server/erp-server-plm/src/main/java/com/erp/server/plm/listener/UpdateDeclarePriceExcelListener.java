package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.UpdateDeclarePriceExcelDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 物流产品导入
 * @Author yl
 * @Date 2023-11-08 11:59
 */
public class UpdateDeclarePriceExcelListener extends AnalysisEventListener<UpdateDeclarePriceExcelDTO>{

    /**
     * 错误信息
     */
    private List<UpdateDeclarePriceExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<UpdateDeclarePriceExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<UpdateDeclarePriceExcelDTO> successList = new ArrayList<>();


    /**
     * 解析一行执行一行
     * @param UpdateDeclarePriceExcelDTO
     * @param analysisContext
     */
    @Override
    public void invoke(UpdateDeclarePriceExcelDTO UpdateDeclarePriceExcelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(UpdateDeclarePriceExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        dataList.add(UpdateDeclarePriceExcelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            UpdateDeclarePriceExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(UpdateDeclarePriceExcelDTO);
            return;
        }
        successList.add(UpdateDeclarePriceExcelDTO);
    }

    public List<UpdateDeclarePriceExcelDTO> getErrorList(){
        return errorList;
    }

    public List<UpdateDeclarePriceExcelDTO> getSuccessList(){
        return successList;
    }

    public List<UpdateDeclarePriceExcelDTO> getExcelDateList(){
        return dataList;
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }
}
