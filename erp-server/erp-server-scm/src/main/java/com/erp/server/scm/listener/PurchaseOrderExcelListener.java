package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.excel.PurchaseOrderImportExcelDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 16:00
 */
public class PurchaseOrderExcelListener extends AnalysisEventListener<PurchaseOrderImportExcelDTO> {


    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<PurchaseOrderImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<PurchaseOrderImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<PurchaseOrderDetailDTO.AddDTO> successList = new ArrayList<>();

    /**
     * 导入成功的skuId集合
     */
    private List<String> importSkuIds = new ArrayList<>();

    /**
     * 明细中已存在的skuId集合
     */
    private List<String> skuIds ;

    /**
     * sku数据
     */
    private List<SkuVO> skuList;


    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public PurchaseOrderExcelListener(List<SkuVO> skuList,List<String> skuIds) {
        this.skuList = skuList;
        this.skuIds = CollectionUtils.isNotEmpty(skuIds) ? skuIds : new ArrayList<>();
    }

    @Override
    public void invoke(PurchaseOrderImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        PurchaseOrderDetailDTO.AddDTO excelDTO = new PurchaseOrderDetailDTO.AddDTO();
        //添加数据用于判断是否为空
        allList.add(importExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //sku验证
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已审核SKU");
        } else {
            if (StringUtils.isNotBlank(importExcelDTO.getSkuNo())) {
                SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(importExcelDTO.getSkuNo())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuEntity)) {
                    errorMsgList.add("请录入已审核SKU");
                } else {
                    if (skuIds.contains(skuEntity.getSkuId())) {
                        errorMsgList.add("明细列表已存在该SKU");
                    } else if (importSkuIds.contains(skuEntity.getSkuId())) {
                        errorMsgList.add("导入数据中已存在该SKU");
                    } else {
                        excelDTO.setSkuId(skuEntity.getSkuId());
                        excelDTO.setSkuNo(skuEntity.getSkuNo());
                        excelDTO.setProductName(skuEntity.getSkuName());
                        excelDTO.setVariantProperty(skuEntity.getVariantProperty());
                        excelDTO.setDeclareModel(skuEntity.getDeclareModel());
                        excelDTO.setDeclareName(skuEntity.getDeclareName());
                    }
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }
        excelDTO.setIsUrgent("是".equals(importExcelDTO.getIsUrgentStr()) ? Boolean.TRUE : Boolean.FALSE);
        excelDTO.setPurchaseQty(Integer.valueOf(importExcelDTO.getPurchaseQtyStr()));
        excelDTO.setPlanDeliveryDate(StringUtils.isBlank(importExcelDTO.getPlanDeliveryDateStr()) ? null : LocalDate.parse(importExcelDTO.getPlanDeliveryDateStr(), dateTimeFormatter));
        excelDTO.setRemark(importExcelDTO.getRemark());
        importSkuIds.add(excelDTO.getSkuId());
        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<PurchaseOrderImportExcelDTO> getAllList(){
        return allList;
    }

    public List<PurchaseOrderImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<PurchaseOrderDetailDTO.AddDTO> getSuccessList(){
        return successList;
    }
}
