package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.excel.DeliveryPlanDetailPdaExportExcelDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeliveryPlanDetailPdaExcelListener extends AnalysisEventListener<DeliveryPlanDetailPdaExportExcelDTO> {

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<DeliveryPlanDetailPdaExportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<DeliveryPlanDetailPdaExportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<ListingInfoDTO.PageDTO> successList = new ArrayList<>();

    /**
     * 明细中已存在的第三方仓集合
     */
    private List<String> thirdSkuNoList;

    /**
     * 全部第三方仓sku
     */
    private List<ListingInfoWithSkuMappingDTO> allThirdWarehouseSkuList;

    /**
     * 导入成功的skuNo集合
     */
    private List<String> importSkuNos = new ArrayList<>();

    public DeliveryPlanDetailPdaExcelListener(List<String> thirdSkuNoList, List<ListingInfoWithSkuMappingDTO> allThirdWarehouseSkuList) {
        this.thirdSkuNoList = CollectionUtils.isNotEmpty(thirdSkuNoList) ? thirdSkuNoList : new ArrayList<>();
        this.allThirdWarehouseSkuList = allThirdWarehouseSkuList;
    }

    @Override
    public void invoke(DeliveryPlanDetailPdaExportExcelDTO deliveryPlanDetailExportExcelDTO, AnalysisContext analysisContext) {
        ListingInfoDTO.PageDTO viewDTO = new ListingInfoDTO.PageDTO();
        //添加数据用于判断是否为空
        allList.add(deliveryPlanDetailExportExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(deliveryPlanDetailExportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CollectionUtils.isEmpty(allThirdWarehouseSkuList)) {
            errorMsgList.add("系统中店铺的sku为空");
        }else if(StringUtils.isBlank(deliveryPlanDetailExportExcelDTO.getMsku()) && StringUtils.isBlank(deliveryPlanDetailExportExcelDTO.getFnsku())){
            errorMsgList.add("MSKU和FNSKU不能同时为空");
        } else {
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = allThirdWarehouseSkuList.stream()
                    .filter(v -> {
                        boolean matchSku = (deliveryPlanDetailExportExcelDTO.getMsku() == null || v.getPlatformSkuNo().equals(deliveryPlanDetailExportExcelDTO.getFnsku()));
                        boolean matchFnSku = (deliveryPlanDetailExportExcelDTO.getMsku() == null || v.getPlatformFnSku().equals(deliveryPlanDetailExportExcelDTO.getFnsku()));
                        return matchSku && matchFnSku;
                    })
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(listingInfoWithSkuMappingDTO)) {
                errorMsgList.add("系统中没有该店铺的sku");
            }else{
                if (!StrUtils.isInteger(deliveryPlanDetailExportExcelDTO.getPlanQty())) {
                    errorMsgList.add("计划数量只能为正整数");
                } else {
                    if (thirdSkuNoList.contains(listingInfoWithSkuMappingDTO.getPlatformSkuNo())) {
                        errorMsgList.add("明细列表已存在该SKU");
                    } else if (importSkuNos.contains(listingInfoWithSkuMappingDTO.getPlatformSkuNo())) {
                        errorMsgList.add("导入数据中已存在该SKU");
                    } else {
                        viewDTO = ListingInfoDTO.PageDTO.builder()
                                .id(listingInfoWithSkuMappingDTO.getTableId())
                                .platformSku(listingInfoWithSkuMappingDTO.getPlatformSkuNo())
                                .mSKU(listingInfoWithSkuMappingDTO.getPlatformSkuNo())
                                .platformSkuName(listingInfoWithSkuMappingDTO.getPlatformSkuName())
                                .skuId(listingInfoWithSkuMappingDTO.getProductSkuId())
                                .skuNo(listingInfoWithSkuMappingDTO.getProductSkuNo())
                                .productName(listingInfoWithSkuMappingDTO.getProductName())
                                .fnSku(listingInfoWithSkuMappingDTO.getPlatformFnSku())
                                .asin(listingInfoWithSkuMappingDTO.getPlatformSpuNo())
                                .qty(Integer.valueOf(deliveryPlanDetailExportExcelDTO.getPlanQty()))
                                .build();
                    }
                }
                importSkuNos.add(listingInfoWithSkuMappingDTO.getPlatformSkuNo());
            }
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            deliveryPlanDetailExportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(deliveryPlanDetailExportExcelDTO);
            return;
        }
        successList.add(viewDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<DeliveryPlanDetailPdaExportExcelDTO> getAllList(){
        return allList;
    }

    public List<DeliveryPlanDetailPdaExportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ListingInfoDTO.PageDTO> getSuccessList(){
        return successList;
    }
}
