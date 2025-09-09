package com.erp.server.oms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ExhibitionOrderImportDetailExcelDTO;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname ExhibitionOrderDetailExcelListener
 * @Date 2025-09-03
 * @Created by jack
 */
public class ExhibitionOrderDetailExcelListener extends AnalysisEventListener<ExhibitionOrderImportDetailExcelDTO> {

    /**
     * sku 信息
     */
    Map<String, SkuVO> skuMap;

    List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS;

    //是否含税
    Boolean isTax;

    /**
     * 成功的数据
     */
    private List<ExhibitionOrderDetailDTO.SkuDTO> successList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<ExhibitionOrderImportDetailExcelDTO> errorList = new ArrayList<>();


    /**
     * @param skuMap
     * @param isTax
     * @param skuAvailableQtyDTOS
     */
    public ExhibitionOrderDetailExcelListener(Map<String, SkuVO> skuMap, List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS, Boolean isTax) {
        this.isTax = isTax;
        this.skuMap = skuMap;
        this.skuAvailableQtyDTOS = skuAvailableQtyDTOS;
    }


    /**
     * 没解析一行执行一次
     *
     * @param exhibitionOrderImportDetailExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(ExhibitionOrderImportDetailExcelDTO exhibitionOrderImportDetailExcelDTO, AnalysisContext analysisContext) {
        ExhibitionOrderDetailDTO.SkuDTO addDTO = new ExhibitionOrderDetailDTO.SkuDTO();
        List<String> msgList = FieldValidUtil.fieldValid(exhibitionOrderImportDetailExcelDTO);
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if(CharSequenceUtil.isAllBlank(exhibitionOrderImportDetailExcelDTO.getPrice(), exhibitionOrderImportDetailExcelDTO.getTaxPrice())){
            errorMsgList.add("销售单价和含税单价必须填写一个");
        }
        String skuNo = exhibitionOrderImportDetailExcelDTO.getSkuNo();
        SkuVO sku = null;
        if(StringUtils.isNotBlank(skuNo)){
            sku = skuMap.getOrDefault(skuNo, null);
            if (Objects.isNull(sku)) {
                errorMsgList.add("sku不存在");
            }
        }else {
            addDTO.setSkuNo(skuNo);
            addDTO.setSkuId(sku.getSkuId());
            addDTO.setProductName(sku.getSkuName());
            addDTO.setUnit(sku.getUnitName());
        }

        //是否赠品
        String isGift = StringUtils.isBlank(exhibitionOrderImportDetailExcelDTO.getIsGift()) ? "":exhibitionOrderImportDetailExcelDTO.getIsGift();
        addDTO.setIsGift(isGift.equals("是"));

        //销售数量
        Integer qty = StringUtils.isBlank(exhibitionOrderImportDetailExcelDTO.getQty()) ? 0 :Integer.valueOf(exhibitionOrderImportDetailExcelDTO.getQty());
        addDTO.setQty(qty);

        //税率
        String taxRateStr = exhibitionOrderImportDetailExcelDTO.getTaxRate();
        BigDecimal taxRate = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(taxRateStr)) {
            taxRate = new BigDecimal(taxRateStr);
        }
        //是否含税标识
        if (Objects.nonNull(isTax) && isTax && StringUtils.isBlank(exhibitionOrderImportDetailExcelDTO.getTaxRate())){
            errorMsgList.add("税率不能为空");
        }else {
            if(isTax){
                if(taxRate.compareTo(BigDecimal.ZERO) <= 0){
                    errorMsgList.add("是否含税选择为是，税率必须大于0");
                }
            }else{
                if(taxRate.compareTo(BigDecimal.ZERO) > 0){
                    errorMsgList.add("是否含税选择为否，税率不能大于0");
                }
            }
        }

        //价格
        String priceStr = exhibitionOrderImportDetailExcelDTO.getPrice();
        String taxPriceStr = exhibitionOrderImportDetailExcelDTO.getTaxPrice();
        if (StringUtils.isBlank(priceStr) && StringUtils.isNotBlank(taxPriceStr)){
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
        addDTO.setRemark(exhibitionOrderImportDetailExcelDTO.getRemark());

        SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = skuAvailableQtyDTOS.stream()
                .filter(e -> e.getSkuId().equals(addDTO.getSkuId()) && e.getUseUserName().equals(exhibitionOrderImportDetailExcelDTO.getUseUserName()))
                .findFirst()
                .orElse(null);
        if(Objects.isNull(skuAvailableQtyDTO)){
            errorMsgList.add(ApiError.ERROR_SAMPLE_LEDGER_NOT_EXIST.msg);
        }else {
            Integer availableQty = Objects.isNull(skuAvailableQtyDTO.getAvailableQty()) ? 0 : skuAvailableQtyDTO.getAvailableQty() ;
            if(addDTO.getQty().compareTo(availableQty) > 0){
                msgList.add(CharSequenceUtil.format(ApiError.ERROR_SAMPLE_AVAILABLE_QTY.msg,addDTO.getSkuNo(),"展会"));
            }else {
                //防止明细里还有重复
                skuAvailableQtyDTO.setAvailableQty(availableQty - addDTO.getQty());

                addDTO.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                addDTO.setUseUserId(skuAvailableQtyDTO.getUseUserId());
                addDTO.setUseUserName(skuAvailableQtyDTO.getUseUserName());
                addDTO.setAvailableQty(availableQty - addDTO.getQty());
            }
        }

        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            exhibitionOrderImportDetailExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(exhibitionOrderImportDetailExcelDTO);
            return;
        }

        successList.add(addDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<ExhibitionOrderImportDetailExcelDTO> getErrorList() {
        return errorList;
    }


    public List<ExhibitionOrderDetailDTO.SkuDTO> getSuccessList() {
        return successList;
    }
}
