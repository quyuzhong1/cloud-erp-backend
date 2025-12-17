package com.erp.server.plm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.constant.CommonConstants;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.ProductCustomsExcelDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.enums.CustomsTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 目的国清关信息导入
 * @Author jack
 * @Date 2025-07-10
 */
public class ProductCustomsExcelListener extends AnalysisEventListener<ProductCustomsExcelDTO>{

    /**
     * 错误信息
     */
    private List<ProductCustomsExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private List<ProductCustomsExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    private List<ProductCustomsEntity> successList = new ArrayList<>();

    //供应商
    private Map<String, String> dictCountryMap;
    //sku信息
    private Map<String,String> skuMap ;

    public ProductCustomsExcelListener(Map<String, String>  dictCountryMap,
                                      Map<String, String> skuMap) {
        this.skuMap = skuMap;
        this.dictCountryMap = dictCountryMap;
    }


    /**
     * 解析一行执行一行
     * @param productCustomsExcelDTO
     * @param analysisContext
     */
    @Override
    public void invoke(ProductCustomsExcelDTO productCustomsExcelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(productCustomsExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        dataList.add(productCustomsExcelDTO);

        ProductCustomsEntity entity = new ProductCustomsEntity();
        String skuNo = productCustomsExcelDTO.getSkuNo();
        if(StringUtils.isNotBlank(skuNo)){
            String skuId = skuMap.getOrDefault(skuNo, "");
            if(StringUtils.isNotBlank(skuId)){
                entity.setSkuId(skuId);
            }else {
                errorMsgList.add("SKU不存在");
            }
        }

        String countryName = productCustomsExcelDTO.getCountryName();
        if(StringUtils.isNotBlank(countryName)){
            String country = dictCountryMap.getOrDefault(countryName, "");
            if(StringUtils.isNotBlank(country)){
                entity.setCountry(country);
                if(!Objects.equals(country, CommonConstants.DEFAULT)){
                    entity.setCountryName(countryName);
                }
            }else {
                errorMsgList.add("国家不存在");
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            productCustomsExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(productCustomsExcelDTO);
            return;
        }

        entity.setDestinationCustomsEnName(productCustomsExcelDTO.getDestinationCustomsEnName());

        entity.setCustomsCode(productCustomsExcelDTO.getDestinationCustomsCode());

        String toDeclarePrice = productCustomsExcelDTO.getToDeclarePrice();
        if(StringUtils.isNotBlank(toDeclarePrice)){
            entity.setToDeclarePrice(new BigDecimal(toDeclarePrice));
        }

        String taxRate = productCustomsExcelDTO.getTaxRate();
        if(StringUtils.isNotBlank(taxRate)){
            entity.setTaxRate(new BigDecimal(taxRate));
        }

        String destinationVatRate = productCustomsExcelDTO.getDestinationVatRate();
        if(StringUtils.isNotBlank(destinationVatRate)){
            entity.setDestinationVatRate(new BigDecimal(destinationVatRate));
        }

        String destinationAdditionalDutyRate = productCustomsExcelDTO.getDestinationAdditionalDutyRate();
        if(StringUtils.isNotBlank(destinationAdditionalDutyRate)){
            entity.setDestinationAdditionalDutyRate(new BigDecimal(destinationAdditionalDutyRate));
        }

        String destinationAntiDumpingDutyRate = productCustomsExcelDTO.getDestinationAntiDumpingDutyRate();
        if(StringUtils.isNotBlank(destinationAntiDumpingDutyRate)){
            entity.setDestinationAntiDumpingDutyRate(new BigDecimal(destinationAntiDumpingDutyRate));
        }

        String destinationOtherTaxRate = productCustomsExcelDTO.getDestinationOtherTaxRate();
        if(StringUtils.isNotBlank(destinationOtherTaxRate)){
            entity.setDestinationOtherTaxRate(new BigDecimal(destinationOtherTaxRate));
        }

        entity.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
        entity.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());

        entity.setType(CustomsTypeEnum.CLEARANCECUSTOMS.getCode());
        successList.add(entity);
    }

    public List<ProductCustomsExcelDTO> getErrorList(){
        return errorList;
    }

    public List<ProductCustomsEntity> getSuccessList(){
        return successList;
    }

    public List<ProductCustomsExcelDTO> getExcelDateList(){
        return dataList;
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        return;
    }
}
