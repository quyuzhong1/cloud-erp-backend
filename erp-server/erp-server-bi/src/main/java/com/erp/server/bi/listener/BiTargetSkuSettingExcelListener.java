package com.erp.server.bi.listener;/**
 * @author Lambda
 * @Classname BiTargetShopSettingExcelListener
 * @Description
 * @Date 2023-09-15 16:36
 * @Created by yl
 */

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import com.erp.model.bi.dto.excel.TargetSkuSettingImportExcelDTO;
import com.erp.model.bi.entity.BiProductDetailEntity;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.server.bi.service.BiProductDetailService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-15 16:36
 */
public class BiTargetSkuSettingExcelListener extends AnalysisEventListener<TargetSkuSettingImportExcelDTO> {

    private List<String> metricsNameList;

    private BiProductDetailService biProductDetailService;

    /**
     * 导入错误数据
     */
    private List<TargetSkuSettingImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 成功的数据
     */
    private List<BiTargetSkuSettingDTO.CommonDTO> successList = new ArrayList<>();

    public BiTargetSkuSettingExcelListener(List<String> metricsNameList, BiProductDetailService biProductDetailService) {
        this.metricsNameList = metricsNameList;
        this.biProductDetailService = biProductDetailService;
    }

    @Override
    public void invoke(TargetSkuSettingImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String metricsName = excelDTO.getMetricsName();
        Boolean isExistMetrics = metricsNameList.contains(metricsName);
        if (!isExistMetrics) {
            errorMsgList.add("考核指标不存在");
        }
        String skuNo = excelDTO.getSku();
        BiProductDetailEntity sku= biProductDetailService.getBySkuNo(skuNo);
        if (ObjectUtil.isEmpty(sku)) {
            errorMsgList.add("sku不存在");
        }
        //添加错误数据
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        BiTargetSkuSettingDTO.CommonDTO addDTO = new BiTargetSkuSettingDTO.CommonDTO();
        MetricsEnum metricsEnum = MetricsEnum.getByName(metricsName);
        addDTO.setMetrics(metricsEnum);
        addDTO.setMetricsName(metricsName);
        //人
        addDTO.setSkuNo(sku.getSkuNo());
        addDTO.setSkuId(sku.getId());
        //一月
        addDTO.setJanuary(excelDTO.getJanuary());
        //二月
        addDTO.setFebruary(excelDTO.getFebruary());
        addDTO.setMarch(excelDTO.getMarch());
        addDTO.setApril(excelDTO.getApril());
        addDTO.setMay(excelDTO.getMay());
        addDTO.setJune(excelDTO.getJune());
        addDTO.setJuly(excelDTO.getJuly());
        addDTO.setAugust(excelDTO.getAugust());
        addDTO.setSeptember(excelDTO.getSeptember());
        addDTO.setOctober(excelDTO.getOctober());
        addDTO.setNovember(excelDTO.getNovember());
        addDTO.setDecember(excelDTO.getDecember());
        successList.add(addDTO);

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // document why this method is empty
    }


    public List<TargetSkuSettingImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<BiTargetSkuSettingDTO.CommonDTO> getSuccessList() {
        return successList;
    }
}
