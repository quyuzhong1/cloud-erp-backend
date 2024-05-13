package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.excel.DeliveryPlanDetailExportExcelDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * 导入成功的skuId集合
     */
    private List<String> importSkuIds = new ArrayList<>();

    private String warehouseId;


    public DeliveryPlanDetailExcelListener(List<String> thirdSkuNoList,List<ListingInfoWithSkuMappingDTO> allThirdWarehouseSkuList,String warehouseId) {
        this.thirdSkuNoList = CollectionUtils.isNotEmpty(thirdSkuNoList) ? thirdSkuNoList : new ArrayList<>();
        this.allThirdWarehouseSkuList = allThirdWarehouseSkuList;
        this.warehouseId = warehouseId;
    }


    @Override
    public void invoke(DeliveryPlanDetailExportExcelDTO deliveryPlanDetailExportExcelDTO, AnalysisContext analysisContext) {
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
            errorMsgList.add("系统中第三方仓sku为空");
        } else {
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = allThirdWarehouseSkuList.stream().filter(
                    v->v.getPlatformSkuNo().equals(deliveryPlanDetailExportExcelDTO.getSkuNo()) && (v.getHasMappingAll() || v.getWarehouseId().equals(this.warehouseId))
                    )
                    .findFirst().orElse(null);
            if (Objects.nonNull(listingInfoWithSkuMappingDTO)) {
                if (!StrUtils.isInteger(deliveryPlanDetailExportExcelDTO.getPlanQty())) {
                    errorMsgList.add("计划数量只能为正整数");
                } else {
                    if (thirdSkuNoList.contains(deliveryPlanDetailExportExcelDTO.getSkuNo())) {
                        errorMsgList.add("明细列表已存在该SKU");
                    } else if (importSkuIds.contains(deliveryPlanDetailExportExcelDTO.getSkuNo())) {
                        errorMsgList.add("导入数据中已存在该SKU");
                    } else {
                        viewDTO = ListingInfoDTO.PageDTO.builder()
                                .id(listingInfoWithSkuMappingDTO.getTableId())
                                .platformSku(listingInfoWithSkuMappingDTO.getPlatformSkuNo())
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
            }else{
                errorMsgList.add("系统中没有该第三方仓sku");
            }
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            deliveryPlanDetailExportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(deliveryPlanDetailExportExcelDTO);
            return;
        }
        importSkuIds.add(deliveryPlanDetailExportExcelDTO.getSkuNo());
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

    public List<ListingInfoDTO.PageDTO> getSuccessList(){
        return successList;
    }
}
