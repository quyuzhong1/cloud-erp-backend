package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.dto.excel.SalesDemandImportExcelDTO;
import com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO;
import com.erp.model.wms.dto.excel.DeliveryPlanDetailExportExcelDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeliveryPlanDetailExcelListener extends AnalysisEventListener<DeliveryPlanDetailExportExcelDTO> {
    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<DeliveryPlanDetailExportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<DeliveryPlanDetailExportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<OverseasDeliveryPlanDetailDTO.ViewDTO> successList = new ArrayList<>();

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
     * 子件数据
     */
    private List<BomChildrenSkuDTO> bomChildrenSkuList;

    public DeliveryPlanDetailExcelListener(List<SkuVO> skuList, List<String> skuIds, List<BomChildrenSkuDTO> bomChildrenSkuList) {
        this.skuList = skuList;
        this.skuIds = CollectionUtils.isNotEmpty(skuIds) ? skuIds : new ArrayList<>();
        this.bomChildrenSkuList = CollectionUtils.isNotEmpty(bomChildrenSkuList) ? bomChildrenSkuList : new ArrayList<>();
    }


    @Override
    public void invoke(DeliveryPlanDetailExportExcelDTO deliveryPlanDetailExportExcelDTO, AnalysisContext analysisContext) {
        OverseasDeliveryPlanDetailDTO.ViewDTO viewDTO = new OverseasDeliveryPlanDetailDTO.ViewDTO();
        //添加数据用于判断是否为空
        allList.add(deliveryPlanDetailExportExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(deliveryPlanDetailExportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已审核SKU");
        } else {
            SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(deliveryPlanDetailExportExcelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(skuEntity)) {
                errorMsgList.add("sku在系统中未匹配到");
            }
            if (skuEntity != null) {
                if (!StrUtils.isInteger(deliveryPlanDetailExportExcelDTO.getPlanQty())) {
                    errorMsgList.add("计划数量只能为正整数");
                } else {
                    if (skuIds.contains(skuEntity.getSkuId())) {
                        errorMsgList.add("明细列表已存在该SKU");
                    } else if (importSkuIds.contains(skuEntity.getSkuId())) {
                        errorMsgList.add("导入数据中已存在该SKU");
                    } else {
                        viewDTO.setQty(Integer.valueOf(deliveryPlanDetailExportExcelDTO.getPlanQty()));
                        viewDTO.setSkuId(skuEntity.getSkuId());
                        viewDTO.setSkuNo(skuEntity.getSkuNo());
                        viewDTO.setProductName(skuEntity.getSkuName());
                        viewDTO.setImageUrl(skuEntity.getSkuImagesUrl());

                        //查询sku是否存在子SKU
                        List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream().filter(req -> req.getParentSkuId().equals(skuEntity.getSkuId())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(sonSkuList)) {
                            viewDTO.setIsCombination(Boolean.TRUE);
                        } else {
                            viewDTO.setIsCombination(Boolean.FALSE);
                        }
                        successList.add(viewDTO);
                    }
                }
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            deliveryPlanDetailExportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(deliveryPlanDetailExportExcelDTO);
            return;
        }
        importSkuIds.add(viewDTO.getSkuId());
        successList.add(viewDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<DeliveryPlanDetailExportExcelDTO> getAllList(){
        return allList;
    }

    public List<DeliveryPlanDetailExportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<OverseasDeliveryPlanDetailDTO.ViewDTO> getSuccessList(){
        return successList;
    }
}
