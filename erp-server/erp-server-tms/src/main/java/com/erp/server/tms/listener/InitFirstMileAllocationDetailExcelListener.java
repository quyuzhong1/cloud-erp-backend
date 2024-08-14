package com.erp.server.tms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.InitFirstMileAllocationDetailExcelDTO;
import com.erp.model.tms.dto.excel.TmsWarehouseMappingExcelDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 期初头程分摊
 */
public class InitFirstMileAllocationDetailExcelListener extends AnalysisEventListener<InitFirstMileAllocationDetailExcelDTO> {

    /**
     * 错误信息
     */
    private List<InitFirstMileAllocationDetailExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<InitFirstMileAllocationDetailExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<InitFirstMileAllocationDetailExcelDTO> successList = new ArrayList<>();

    public InitFirstMileAllocationDetailExcelListener() {

    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(InitFirstMileAllocationDetailExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);

    }

    public List<InitFirstMileAllocationDetailExcelDTO> getErrorList(){
        return errorList;
    }

    public List<InitFirstMileAllocationDetailExcelDTO> getSuccessList(){
        return successList;
    }

    public List<InitFirstMileAllocationDetailExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author zdy
     * @date: 2024/8/14 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
