package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.dto.excel.SalesDemandImportExcelDTO;
import com.erp.model.wms.dto.WarehouseDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/21 11:28
 */
public class SalesDemandExcelListener extends AnalysisEventListener<SalesDemandImportExcelDTO> {
    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<SalesDemandImportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<SalesDemandImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<SalesDemandDetailDTO.ExcelDTO> dataList = new ArrayList<>();

    /**
     * 明细中已存在的skuId集合
     */
    private List<String> skuIds;

    /**
     * 导入成功的skuId集合
     */
    private List<String> importSkuIds;

    /**
     * sku数据
     */
    private List<SkuVO> skuList;

    /**
     * 仓库数据
     */
    private List<WarehouseDTO.UpdateDTO> warehouseList;


    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public SalesDemandExcelListener(List<SkuVO> skuList,List<WarehouseDTO.UpdateDTO> warehouseList,List<String> skuIds) {
        this.skuList = skuList;
        this.warehouseList = warehouseList;
        this.skuIds = skuIds;
    }

    @Override
    public void invoke(SalesDemandImportExcelDTO salesDemandImportExcelDTO, AnalysisContext analysisContext) {

        SalesDemandDetailDTO.ExcelDTO excelDTO = new SalesDemandDetailDTO.ExcelDTO();
        //添加数据用于判断是否为空
        allList.add(salesDemandImportExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(salesDemandImportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //sku验证
        if (StringUtils.isNotBlank(salesDemandImportExcelDTO.getSkuNo())) {
            SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(salesDemandImportExcelDTO.getSkuNo())).findFirst().orElse(null);
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
        if (StringUtils.isNotBlank(salesDemandImportExcelDTO.getDestWarehouseName())) {
            WarehouseDTO.UpdateDTO warehouseDTO = warehouseList.stream().filter(obj -> obj.getName().equals(salesDemandImportExcelDTO.getDestWarehouseName())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(warehouseDTO)) {
                errorMsgList.add("请录入已审核并且启用的仓库");
            } else {
                excelDTO.setDestWarehouseId(warehouseDTO.getId());
                excelDTO.setDestWarehouseName(salesDemandImportExcelDTO.getDestWarehouseName());
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            salesDemandImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(salesDemandImportExcelDTO);
            return;
        }
        excelDTO.setIsUrgent("是".equals(salesDemandImportExcelDTO.getIsUrgentStr()) ? Boolean.TRUE : Boolean.FALSE);
        excelDTO.setPlanStockQty(Integer.valueOf(salesDemandImportExcelDTO.getPlanStockQtyStr()));
        excelDTO.setPlanDeliveryDate(LocalDate.parse(salesDemandImportExcelDTO.getPlanDeliveryDateStr(), dateTimeFormatter));
        excelDTO.setRemark(salesDemandImportExcelDTO.getRemark());
        importSkuIds.add(excelDTO.getSkuId());
        dataList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<SalesDemandImportExcelDTO> getAllList(){
        return allList;
    }

    public List<SalesDemandImportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<SalesDemandDetailDTO.ExcelDTO> getDataList(){
        return dataList;
    }
}
