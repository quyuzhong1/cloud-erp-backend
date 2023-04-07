package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.PurchasePriceDetailImportExcelDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailExcelListener
 * @Description TODO
 * @Date 2023-03-27 17:16
 * @Created by yl
 */
public class PurchasePriceDetailExcelListener extends AnalysisEventListener<PurchasePriceDetailImportExcelDTO> {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * sku数据
     */
    private List<SkuVO> skuList;


    /**
     * 成功的数据
     */
    private List<PurchasePriceDetailDTO.AddDTO> successList = new ArrayList<>();


    /**
     * 导入错误数据
     */
    private List<PurchasePriceDetailImportExcelDTO> errorList = new ArrayList<>();


    public PurchasePriceDetailExcelListener(List<SkuVO> skuList) {
        this.skuList = skuList;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param purchasePriceDetailImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-27 17:17
     */
    @Override
    public void invoke(PurchasePriceDetailImportExcelDTO purchasePriceDetailImportExcelDTO, AnalysisContext analysisContext) {

        PurchasePriceDetailDTO.AddDTO addDTO = new PurchasePriceDetailDTO.AddDTO();
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(purchasePriceDetailImportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已审核SKU");
        } else {
            SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(purchasePriceDetailImportExcelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(skuEntity)) {
                errorMsgList.add("sku有误");
            }
            if (skuEntity != null) {
                addDTO.setCurrency("CNY");
                addDTO.setDeliveryDay(purchasePriceDetailImportExcelDTO.getDeliveryDay());
                String effectiveDateStr = purchasePriceDetailImportExcelDTO.getEffectiveDateStr();
                addDTO.setEffectiveDate(StringUtils.isBlank(effectiveDateStr) ? null : LocalDate.parse(effectiveDateStr, dateTimeFormatter));
                addDTO.setMinQty(purchasePriceDetailImportExcelDTO.getMinQty());
                addDTO.setMaxQty(purchasePriceDetailImportExcelDTO.getMaxQty());
                addDTO.setTaxPrice(purchasePriceDetailImportExcelDTO.getTaxPrice());
                addDTO.setTaxRate(purchasePriceDetailImportExcelDTO.getTaxRate());
                addDTO.setSkuId(skuEntity.getSkuId());
                addDTO.setSkuNo(skuEntity.getSkuNo());
                addDTO.setProductName(skuEntity.getSpuName());
                successList.add(addDTO);
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            purchasePriceDetailImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(purchasePriceDetailImportExcelDTO);
            return;
        }

    }


    /**
     * 数据全部解析完成
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-27 17:17
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<PurchasePriceDetailImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<PurchasePriceDetailDTO.AddDTO> getSuccessList() {
        return successList;
    }
}
