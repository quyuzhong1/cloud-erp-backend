package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.scm.dto.excel.SalesDemandImportExcelDTO;
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
    private List<PurchaseApplicationDetailDTO.AddDTO> dataList = new ArrayList<>();

    /**
     * 明细中已存在的skuId集合
     */
    private List<String> skuIds;

    /**
     * 导入成功的skuId集合
     */
    private List<String> importSkuIds = new ArrayList<>();

    /**
     * sku数据
     */
    private List<SkuVO> skuList;

    /**
     * 仓库数据
     */
    private List<WarehouseDTO.UpdateDTO> warehouseList;


    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public PurchaseApplicationExcelListener(List<SkuVO> skuList,List<WarehouseDTO.UpdateDTO> warehouseList,List<String> skuIds) {
        this.skuList = skuList;
        this.warehouseList = warehouseList;
        this.skuIds = skuIds;
    }

    @Override
    public void invoke(PurchaseApplicationImportExcelDTO purchaseApplicationImportExcelDTO, AnalysisContext analysisContext) {

        PurchaseApplicationDetailDTO.AddDTO excelDTO = new PurchaseApplicationDetailDTO.AddDTO();
        //添加数据用于判断是否为空
        allList.add(purchaseApplicationImportExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(purchaseApplicationImportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //sku验证
        if (StringUtils.isNotBlank(purchaseApplicationImportExcelDTO.getSkuNo())) {
            SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(purchaseApplicationImportExcelDTO.getSkuNo())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuEntity)) {
                errorMsgList.add("请录入已审核SKU");
            } else {
                if (CollectionUtils.isNotEmpty(skuIds)) {
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
        if (StringUtils.isNotBlank(purchaseApplicationImportExcelDTO.getDestWarehouseName())) {
            WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getName().equals(purchaseApplicationImportExcelDTO.getDestWarehouseName())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseDTO)) {
                errorMsgList.add("请录入已审核并且启用的仓库");
            } else {
                excelDTO.setDestWarehouseId(warehouseDTO.getId());
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            purchaseApplicationImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(purchaseApplicationImportExcelDTO);
            return;
        }
        excelDTO.setIsUrgent("是".equals(purchaseApplicationImportExcelDTO.getIsUrgentStr()) ? Boolean.TRUE : Boolean.FALSE);
        excelDTO.setApplyQty(Integer.valueOf(purchaseApplicationImportExcelDTO.getApplyQtyStr()));
        excelDTO.setPlanDeliveryDate(LocalDate.parse(purchaseApplicationImportExcelDTO.getPlanDeliveryDateStr(), dateTimeFormatter));
        excelDTO.setRemark(purchaseApplicationImportExcelDTO.getRemark());
        importSkuIds.add(excelDTO.getSkuId());
        dataList.add(excelDTO);
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

    public List<PurchaseApplicationDetailDTO.AddDTO> getDataList(){
        return dataList;
    }
}
