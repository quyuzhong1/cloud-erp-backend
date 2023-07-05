package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.excel.KingdeePoImportExcelDTO;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2023/3/27 16:00
 */
public class KingdeePoExcelListener extends AnalysisEventListener<KingdeePoImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<KingdeePoImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<KingdeePoImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<PurchaseOrderDetailDTO.AddDTO> successList = new ArrayList<>();

    /**
     * 导入成功的skuId集合
     */
    private List<String> importSkuIds = new ArrayList<>();



    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public KingdeePoExcelListener() {

    }

    @Override
    public void invoke(KingdeePoImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        PurchaseOrderDetailDTO.AddDTO excelDTO = new PurchaseOrderDetailDTO.AddDTO();
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
            errorList.add(importExcelDTO);
            return;
        }
        importSkuIds.add(excelDTO.getSkuId());
        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<KingdeePoImportExcelDTO> getAllList(){
        return allList;
    }

    public List<KingdeePoImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<PurchaseOrderDetailDTO.AddDTO> getSuccessList(){
        return successList;
    }
}
