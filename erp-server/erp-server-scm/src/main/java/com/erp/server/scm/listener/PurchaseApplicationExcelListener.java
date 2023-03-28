package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.wms.dto.WarehouseDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 采购申请单导入监听
 * @date 2023/3/22 16:09
 */
public class PurchaseApplicationExcelListener extends AnalysisEventListener<PurchaseApplicationImportExcelDTO> {

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<PurchaseApplicationImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<PurchaseApplicationImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<PurchaseApplicationDetailDTO.AddDTO> successList = new ArrayList<>();

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

    /**
     * 仓库数据
     */
    private List<WarehouseDTO.UpdateDTO> warehouseList;

    /**
     * 核算公司
     */
    private List<BaseIdDTO> companyList;


    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public PurchaseApplicationExcelListener(List<SkuVO> skuList,List<WarehouseDTO.UpdateDTO> warehouseList,List<String> skuIds,List<BaseIdDTO> companyList) {
        this.skuList = skuList;
        this.warehouseList = warehouseList;
        this.skuIds = CollectionUtils.isNotEmpty(skuIds) ? skuIds : new ArrayList<>();
        this.companyList = companyList;
    }

    @Override
    public void invoke(PurchaseApplicationImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {

        PurchaseApplicationDetailDTO.AddDTO excelDTO = new PurchaseApplicationDetailDTO.AddDTO();
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
                        excelDTO.setUnitQty(skuEntity.getUnitQty());
                    }
                }
            }
        }
        //仓库验证
        if (CollectionUtils.isEmpty(warehouseList)) {
            errorMsgList.add("系统中未发现已启用仓库");
        } else {
            if (StringUtils.isNotBlank(importExcelDTO.getDestWarehouseName())) {
                WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getName().equals(importExcelDTO.getDestWarehouseName())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(warehouseDTO)) {
                    errorMsgList.add("请录入已审核并且启用的仓库");
                } else {
                    excelDTO.setDestWarehouseId(warehouseDTO.getId());
                }
            }
        }

        if (CollectionUtils.isEmpty(companyList)) {
            errorMsgList.add("系统中未发现已启用的采购组织和收料组织");
        } else {
            //采购组织验证
            if (StringUtils.isNotBlank(importExcelDTO.getPurchaseOrgName())) {
                BaseIdDTO baseIdDTO = companyList.stream().filter(obj -> obj.getName().equals(importExcelDTO.getPurchaseOrgName())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(baseIdDTO)) {
                    errorMsgList.add("请录入启用采购组织");
                } else {
                    excelDTO.setPurchaseOrgId(baseIdDTO.getId());
                }
            }
            //收料组织验证
            if (StringUtils.isNotBlank(importExcelDTO.getReceiveOrgName())) {
                BaseIdDTO baseIdDTO = companyList.stream().filter(obj -> obj.getName().equals(importExcelDTO.getReceiveOrgName())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(baseIdDTO)) {
                    errorMsgList.add("请录入启用收料组织");
                } else {
                    excelDTO.setReceiveOrgId(baseIdDTO.getId());
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
        excelDTO.setApplyQty(Integer.valueOf(importExcelDTO.getApplyQtyStr()));
        excelDTO.setPlanDeliveryDate(StringUtils.isBlank(importExcelDTO.getPlanDeliveryDateStr()) ? null : LocalDate.parse(importExcelDTO.getPlanDeliveryDateStr(), dateTimeFormatter));
        excelDTO.setRemark(importExcelDTO.getRemark());
        importSkuIds.add(excelDTO.getSkuId());
        successList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<PurchaseApplicationImportExcelDTO> getAllList(){
        return allList;
    }

    public List<PurchaseApplicationImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<PurchaseApplicationDetailDTO.AddDTO> getSuccessList(){
        return successList;
    }
}
