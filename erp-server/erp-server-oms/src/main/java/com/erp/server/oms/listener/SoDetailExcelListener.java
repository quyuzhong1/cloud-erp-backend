package com.erp.server.oms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.excel.SoDetailImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname SoDetailExcelListener

 * @Date 2023-05-17 19:47
 * @Created by yl
 */
public class SoDetailExcelListener extends AnalysisEventListener<SoDetailImportExcelDTO> {


    /**
     * sku 信息
     */
    List<SkuVO> skuList;
    //是否含税
    Boolean isTax;

    List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS;

    /**
     * 成功的数据
     */
    private List<SoDetailDTO.SkuDTO> successList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<SoDetailImportExcelDTO> errorList = new ArrayList<>();


    /**
     * 带过来
     *
     * @param skuList
     * @param isTax
     */
    public SoDetailExcelListener(List<SkuVO> skuList, List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS, Boolean isTax) {
        this.isTax = isTax;
        this.skuList = skuList;
        this.skuMappingViewDTOS = skuMappingViewDTOS;
    }


    /**
     * 没解析一行执行一次
     *
     * @param soDetailImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(SoDetailImportExcelDTO soDetailImportExcelDTO, AnalysisContext analysisContext) {
        List<String> msgList = FieldValidUtil.fieldValid(soDetailImportExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if(CharSequenceUtil.isAllBlank(soDetailImportExcelDTO.getPrice(), soDetailImportExcelDTO.getTaxPrice())){
            errorMsgList.add("销售单价和含税单价必须填写一个");
        }
        String skuNo = soDetailImportExcelDTO.getSkuNo();

        if(StringUtils.isBlank(skuNo) && StringUtils.isBlank(soDetailImportExcelDTO.getPlatformSkuNo()) ){
            errorMsgList.add("skuNo和平台skuNo不能同时为空");
        }
        SkuVO sku = null;
        if(StringUtils.isNotBlank(skuNo)){
            String finalSkuNo = skuNo;
            sku = skuList.stream().filter(s -> s.getSkuNo().equals(finalSkuNo)).findFirst().orElse(null);
            if (Objects.isNull(sku)) {
                errorMsgList.add("sku 不存在");
            }
        }
        if(StringUtils.isNotBlank(soDetailImportExcelDTO.getPlatformSkuNo())){
            SkuMappingDTO.SkuMappingViewDTO skuMappingViewDTO = skuMappingViewDTOS.stream().filter(s -> s.getPlatformSkuNo().equals(soDetailImportExcelDTO.getPlatformSkuNo())).findFirst().orElse(null);
            if(Objects.isNull(skuMappingViewDTO)){
                errorMsgList.add("客户sku映射不存在");
            }else{
                if(StringUtils.isNotBlank(skuNo) && !skuNo.equals(skuMappingViewDTO.getProductSkuNo())){
                    errorMsgList.add("客户sku映射与skuNo不匹配");
                }
                if(StringUtils.isBlank(skuNo)){
                    skuNo = skuMappingViewDTO.getProductSkuNo();
                    String finalSkuNo1 = skuNo;
                    sku = skuList.stream().filter(s -> s.getSkuNo().equals(finalSkuNo1)).findFirst().orElse(null);
                    if (Objects.isNull(sku)) {
                        errorMsgList.add("sku 不存在");
                    }
                }
            }
        }
        //是否含税标识
        if (Objects.nonNull(isTax) && isTax && CharSequenceUtil.isBlank(soDetailImportExcelDTO.getTaxRate())){
            errorMsgList.add("税率不能为空");
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            soDetailImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(soDetailImportExcelDTO);
            return;
        }
        SoDetailDTO.SkuDTO addDTO = new SoDetailDTO.SkuDTO();
        //是否补发
        String isReissue = soDetailImportExcelDTO.getIsReissue();
        addDTO.setIsReissue(isReissue.equals("是"));
        //是否赠品
        String isGift = soDetailImportExcelDTO.getIsGift();
        addDTO.setIsGift(isGift.equals("是"));

        //是否关闭
        addDTO.setIsClose(false);
        addDTO.setSkuNo(skuNo);
        addDTO.setSkuId(sku.getSkuId());
        addDTO.setProductName(sku.getSkuName());
        addDTO.setUnit(sku.getUnitName());
        //销售数量
        String qty = soDetailImportExcelDTO.getQty();
        addDTO.setQty(Integer.valueOf(qty));

        //税率
        String taxRateStr = soDetailImportExcelDTO.getTaxRate();
        BigDecimal taxRate = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(taxRateStr)) {
            taxRate = new BigDecimal(taxRateStr);
        }
        //价格
        String priceStr = soDetailImportExcelDTO.getPrice();
        String taxPriceStr = soDetailImportExcelDTO.getTaxPrice();
        if (CharSequenceUtil.isBlank(priceStr) && CharSequenceUtil.isNotBlank(taxPriceStr)){
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(MathUtil.divide(taxRate, MathUtil.BigDecimal_100), MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.getBigDecimalByStr(taxPriceStr);
            //含税单价
            BigDecimal price = MathUtil.divide(taxPrice, multiplyTax);
            addDTO.setPrice(price);
        }else {
            BigDecimal price = MathUtil.getBigDecimalByStr(priceStr);
            addDTO.setPrice(price);
        }
        addDTO.setTaxRate(taxRate);
        addDTO.setRemark(soDetailImportExcelDTO.getRemark());
        addDTO.setCustomerPO(soDetailImportExcelDTO.getCustomerPO());
        addDTO.setToCountry(soDetailImportExcelDTO.getToCountry());
        addDTO.setCustomerSkuNo(soDetailImportExcelDTO.getPlatformSkuNo());
        successList.add(addDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<SoDetailImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<SoDetailDTO.SkuDTO> getSuccessList() {
        return successList;
    }
}
