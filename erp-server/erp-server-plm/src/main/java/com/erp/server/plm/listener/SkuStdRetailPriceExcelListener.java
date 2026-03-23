package com.erp.server.plm.listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.erp.model.plm.dto.excel.SkuStdRetailPriceExcelDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.plm.service.impl.ProductDetailServiceImpl;
import com.erp.server.plm.service.impl.SkuStdRetailPriceServiceImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;

public class SkuStdRetailPriceExcelListener extends AnalysisEventListener<SkuStdRetailPriceExcelDTO> {
    @Getter
    private List<SkuStdRetailPriceExcelDTO> dataList = new ArrayList<>();

    @Getter
    private List<SkuStdRetailPriceExcelDTO> errorList = new ArrayList<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SkuStdRetailPriceExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        dataList.add(excelDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        
        Set<String> dbCurrencySet = FeignQuery.list(DictCurrencyEntity.class).stream().map(DictCurrencyEntity::getId).collect(Collectors.toSet());
        ProductDetailServiceImpl productDetailServiceImpl = ApplicationContextUtils.getBean(ProductDetailServiceImpl.class);
        Map<String, List<ProductDetailEntity>> skuNoGroupMap = productDetailServiceImpl.list().stream().collect(Collectors.groupingBy(ProductDetailEntity::getSkuNo));
        SkuStdRetailPriceServiceImpl serviceBean = ApplicationContextUtils.getBean(SkuStdRetailPriceServiceImpl.class);
        
        for (SkuStdRetailPriceExcelDTO excelDTO : dataList) {
        	String currency = excelDTO.getCurrency();
        	if(!dbCurrencySet.contains(currency)) {
        		excelDTO.setErrorMsg(CharSequenceUtil.format("【{}】币别在系统不存在", currency));
                errorList.add(excelDTO);
                continue;
        	}
        	String skuNo = excelDTO.getSkuNo();
        	List<ProductDetailEntity> skuList = skuNoGroupMap.get(skuNo);
        	if(CollUtil.isEmpty(skuList)) {
        		excelDTO.setErrorMsg(CharSequenceUtil.format("【{}】SKU编号在系统不存在", skuNo));
                errorList.add(excelDTO);
                continue;
        	}else if(skuList.size() > 1) {
        		excelDTO.setErrorMsg(CharSequenceUtil.format("【{}】SKU编号在系统存在多个", skuNo));
                errorList.add(excelDTO);
                continue;
        	}
        	
        	SkuStdRetailPriceDTO.AddDTO addDto = new SkuStdRetailPriceDTO.AddDTO();
        	addDto.setSkuId(skuList.get(0).getId());
        	addDto.setCurrency(currency);
        	BigDecimal stdRetailPriceVat = new BigDecimal(excelDTO.getStdRetailPriceVat());
			addDto.setStdRetailPriceVat(stdRetailPriceVat);
			BigDecimal vatRate = new BigDecimal(excelDTO.getVatRate());
			addDto.setVatRate(vatRate);
			addDto.setStdRetailPrice(stdRetailPriceVat.divide(BigDecimal.ONE.add(vatRate) , 4 , RoundingMode.HALF_UP));
			serviceBean.batchAdd(Arrays.asList(addDto), false);
        }
    }

}
