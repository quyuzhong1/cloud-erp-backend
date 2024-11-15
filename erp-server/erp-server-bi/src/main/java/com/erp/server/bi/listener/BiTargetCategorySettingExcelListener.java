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
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import com.erp.model.bi.dto.excel.TargetCategorySettingImportExcelDTO;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.plm.entity.BasicCategoryEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-15 16:36
 */
public class BiTargetCategorySettingExcelListener extends AnalysisEventListener<TargetCategorySettingImportExcelDTO> {

    private List<String> metricsNameList;

    private List<BasicCategoryEntity> basicCategoryList;

    /**
     * 导入错误数据
     */
    private List<TargetCategorySettingImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 成功的数据
     */
    private List<BiTargetCategorySettingDTO.CommonDTO> successList = new ArrayList<>();

    public BiTargetCategorySettingExcelListener(List<String> metricsNameList, List<BasicCategoryEntity> basicCategoryList) {
        this.metricsNameList = metricsNameList;
        this.basicCategoryList = basicCategoryList;
    }

    @Override
    public void invoke(TargetCategorySettingImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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
        String categoryName = excelDTO.getCategoryName();
        BasicCategoryEntity category = basicCategoryList.stream().filter(c -> c.getName().equals(categoryName))
                .findFirst().orElse(null);
        if (ObjectUtil.isEmpty(category)) {
            errorMsgList.add("分类不存在");
        }
        //添加错误数据
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        BiTargetCategorySettingDTO.CommonDTO addDTO = new BiTargetCategorySettingDTO.CommonDTO();
        MetricsEnum metricsEnum = MetricsEnum.getByName(metricsName);
        addDTO.setMetrics(metricsEnum);
        addDTO.setMetricsName(metricsName);
        //人
        addDTO.setCategoryId(category.getId());
        addDTO.setCategoryName(category.getName());
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

    }


    public List<TargetCategorySettingImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<BiTargetCategorySettingDTO.CommonDTO> getSuccessList() {
        return successList;
    }
}
