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
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.excel.TargetShopSettingImportExcelDTO;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-15 16:36
 */
public class BiTargetShopSettingExcelListener extends AnalysisEventListener<TargetShopSettingImportExcelDTO> {

    private List<String> metricsNameList;

    private List<BiShopInfoEntity> shopList;

    /**
     * 导入错误数据
     */
    private List<TargetShopSettingImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 成功的数据
     */
    private List<BiTargetShopSettingDTO.CommonDTO> successList = new ArrayList<>();

    public BiTargetShopSettingExcelListener(List<String> metricsNameList, List<BiShopInfoEntity> shopList) {
        this.metricsNameList = metricsNameList;
        this.shopList = shopList;
    }

    @Override
    public void invoke(TargetShopSettingImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        String metricsName = excelDTO.getMetricsName();
        Boolean isExistMetrics = metricsNameList.contains(metricsName);
        if (Boolean.FALSE.equals(isExistMetrics)) {
            errorMsgList.add("考核指标不存在");
        }
        String shopName = excelDTO.getShopName();
        BiShopInfoEntity shop = shopList.stream().
                filter(u -> u.getName().equals(shopName)).
                findFirst().orElse(null);
        if (ObjectUtil.isEmpty(shop) || shop == null) {
            errorMsgList.add("店铺不存在");
        }
        //添加错误数据
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        BiTargetShopSettingDTO.CommonDTO addDTO = new BiTargetShopSettingDTO.CommonDTO();
        MetricsEnum metricsEnum = MetricsEnum.getByName(metricsName);
        addDTO.setMetrics(metricsEnum);
        addDTO.setMetricsName(metricsName);
        //人
        addDTO.setShopId(shop.getId());
        addDTO.setShopName(shop.getName());
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


    public List<TargetShopSettingImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<BiTargetShopSettingDTO.CommonDTO> getSuccessList() {
        return successList;
    }
}
