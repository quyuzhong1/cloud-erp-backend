package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.dto.excel.B2bCustomerPackingImportExcelDTO;
import com.erp.model.wms.enums.B2bPackingLabelSizeEnum;
import com.erp.model.wms.enums.B2bPackingTypeEnum;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * B2B客户装箱明细导入
 */
public class B2bCustomerPackingExcelListener extends AnalysisEventListener<B2bCustomerPackingImportExcelDTO> {

    private final String packingType;
    private final List<B2bThirdDeliveryDetailDTO.AddDTO> productDetailList;
    private final Map<String, B2bThirdDeliveryDetailDTO.AddDTO> skuDetailMap;
    private final Map<String, Integer> skuSaleQtyMap;

    private final List<B2bCustomerPackingDTO.ViewDTO> successList = new ArrayList<>();
    private final List<B2bCustomerPackingImportExcelDTO> errorList = new ArrayList<>();

    public B2bCustomerPackingExcelListener(String packingType, List<B2bThirdDeliveryDetailDTO.AddDTO> productDetailList) {
        this.packingType = packingType;
        this.productDetailList = CollectionUtils.isEmpty(productDetailList) ? new ArrayList<>() : productDetailList;
        this.skuDetailMap = this.productDetailList.stream()
                .filter(e -> CharSequenceUtil.isNotBlank(e.getSkuNo()))
                .collect(Collectors.toMap(B2bThirdDeliveryDetailDTO.AddDTO::getSkuNo, e -> e, (a, b) -> a));
        this.skuSaleQtyMap = new HashMap<>();
        for (B2bThirdDeliveryDetailDTO.AddDTO detail : this.productDetailList) {
            if (CharSequenceUtil.isBlank(detail.getSkuNo())) {
                continue;
            }
            skuSaleQtyMap.merge(detail.getSkuNo(), detail.getSaleQty() != null ? detail.getSaleQty() : 0, Integer::sum);
        }
    }

    @Override
    public void invoke(B2bCustomerPackingImportExcelDTO row, AnalysisContext context) {
        List<String> errorMsgList = new ArrayList<>();
        List<String> fieldErrors = FieldValidUtil.fieldValid(row);
        if (CollectionUtils.isNotEmpty(fieldErrors)) {
            errorMsgList.addAll(fieldErrors);
        }
        Integer boxSeq;
        try {
            boxSeq = Integer.parseInt(row.getBoxSeq().trim());
            if (boxSeq <= 0) {
                errorMsgList.add("序号必须为正整数");
            }
        } catch (Exception e) {
            errorMsgList.add("序号格式不正确");
            boxSeq = null;
        }
        Integer packingQty = null;
        try {
            packingQty = Integer.parseInt(row.getPackingQty().trim());
            if (packingQty <= 0) {
                errorMsgList.add("装箱数量必须为正整数");
            }
        } catch (Exception e) {
            errorMsgList.add("装箱数量格式不正确");
        }
        if (CharSequenceUtil.isNotBlank(row.getLabelSize()) && !B2bPackingLabelSizeEnum.isValid(row.getLabelSize().trim())) {
            errorMsgList.add("标签尺寸不合法");
        }
        if (B2bPackingTypeEnum.PRE_STAGED_BOX.getCode().equals(packingType) && CharSequenceUtil.isBlank(row.getBoxMarkNo())) {
            errorMsgList.add("装箱类型为已暂存箱发货时箱唛号必填");
        }
        B2bThirdDeliveryDetailDTO.AddDTO productDetail = null;
        if (CharSequenceUtil.isNotBlank(row.getSkuNo())) {
            productDetail = skuDetailMap.get(row.getSkuNo().trim());
            if (productDetail == null) {
                errorMsgList.add("SKU不在产品明细中");
            }
        }
        if (CollectionUtils.isNotEmpty(errorMsgList)) {
            row.setErrorMsg(String.join(";", errorMsgList));
            errorList.add(row);
            return;
        }
        B2bCustomerPackingDTO.ViewDTO viewDTO = new B2bCustomerPackingDTO.ViewDTO();
        viewDTO.setBoxSeq(boxSeq);
        viewDTO.setBoxMarkNo(CharSequenceUtil.blankToDefault(row.getBoxMarkNo(), "").trim());
        viewDTO.setBoxMarkRefNo(CharSequenceUtil.blankToDefault(row.getBoxMarkRefNo(), "").trim());
        viewDTO.setLabelSize(CharSequenceUtil.blankToDefault(row.getLabelSize(), "").trim());
        viewDTO.setLabelingRequirement(CharSequenceUtil.blankToDefault(row.getLabelingRequirement(), "").trim());
        viewDTO.setSkuNo(row.getSkuNo().trim());
        viewDTO.setPackingQty(packingQty);
        if (productDetail != null) {
            viewDTO.setSkuId(productDetail.getSkuId());
            viewDTO.setProductName(productDetail.getProductName());
            viewDTO.setWarehousePlatformSku(productDetail.getWarehousePlatformSku());
        }
        viewDTO.setSaleQty(skuSaleQtyMap.getOrDefault(viewDTO.getSkuNo(), 0));
        successList.add(viewDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        normalizeBoxLevelFields();
    }

    private void normalizeBoxLevelFields() {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        Map<Integer, B2bCustomerPackingDTO.ViewDTO> boxHeadMap = new HashMap<>();
        for (B2bCustomerPackingDTO.ViewDTO row : successList) {
            B2bCustomerPackingDTO.ViewDTO head = boxHeadMap.get(row.getBoxSeq());
            if (head == null) {
                boxHeadMap.put(row.getBoxSeq(), row);
            } else {
                if (!Objects.equals(CharSequenceUtil.blankToDefault(head.getBoxMarkNo(), ""), CharSequenceUtil.blankToDefault(row.getBoxMarkNo(), ""))
                        || !Objects.equals(CharSequenceUtil.blankToDefault(head.getBoxMarkRefNo(), ""), CharSequenceUtil.blankToDefault(row.getBoxMarkRefNo(), ""))
                        || !Objects.equals(CharSequenceUtil.blankToDefault(head.getLabelSize(), ""), CharSequenceUtil.blankToDefault(row.getLabelSize(), ""))
                        || !Objects.equals(CharSequenceUtil.blankToDefault(head.getLabelingRequirement(), ""), CharSequenceUtil.blankToDefault(row.getLabelingRequirement(), ""))) {
                    B2bCustomerPackingImportExcelDTO error = new B2bCustomerPackingImportExcelDTO();
                    error.setBoxSeq(String.valueOf(row.getBoxSeq()));
                    error.setSkuNo(row.getSkuNo());
                    error.setErrorMsg("相同序号行的箱唛号/箱唛参考号/标签尺寸/贴标要求须一致");
                    errorList.add(error);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(errorList)) {
            successList.clear();
        }
    }

    public List<B2bCustomerPackingDTO.ViewDTO> getSuccessList() {
        return successList;
    }

    public List<B2bCustomerPackingImportExcelDTO> getErrorList() {
        return errorList;
    }
}
