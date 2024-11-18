package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.BomInfoExcelDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class BomInfoExcelListener extends AnalysisEventListener<BomInfoExcelDTO> {

    /**
     * 错误信息
     */
    private List<BomInfoExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<BomInfoExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<BomInfoExcelDTO> successList = new ArrayList<>();

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param bomInfoExcelDTO BOM导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(BomInfoExcelDTO bomInfoExcelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(bomInfoExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //添加数据用于判断是否为空
        dataList.add(bomInfoExcelDTO);
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            bomInfoExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(bomInfoExcelDTO);
            return;
        }
        successList.add(bomInfoExcelDTO);

    }

    public List<BomInfoExcelDTO> getErrorList(){
        return errorList;
    }

    public List<BomInfoExcelDTO> getSuccessList(){
        return successList;
    }

    public List<BomInfoExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }
}
