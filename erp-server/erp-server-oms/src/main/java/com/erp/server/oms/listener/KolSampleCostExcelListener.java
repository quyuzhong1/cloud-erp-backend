package com.erp.server.oms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolSampleCostImportExcelDTO;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author will
 * @Classname KolB2bApplicationExcelListener
 * @Date 2025-12-03
 */
public class KolSampleCostExcelListener extends AnalysisEventListener<KolSampleCostImportExcelDTO> {

    /**
     * 导入数据，用于判断导入是否为空
     */
    @Getter
    private List<KolSampleCostImportExcelDTO> allList = new ArrayList<>();
    /**
     * 错误信息
     */
    @Getter
    private List<KolSampleCostImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<KolSampleCostImportExcelDTO> successList = new ArrayList<>();

    public KolSampleCostExcelListener() {

    }

    /**
     * 每解析一行数据回调一遍
     * @author will
     * @date 2025/12/3 10:10
     * @param importExcelDTO
     * @param analysisContext
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(KolSampleCostImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {

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
        successList.add(importExcelDTO);

    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
