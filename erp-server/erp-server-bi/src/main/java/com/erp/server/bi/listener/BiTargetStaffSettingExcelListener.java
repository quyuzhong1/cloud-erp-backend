package com.erp.server.bi.listener;/**
 * @author Lambda
 * @Classname BiTargetStaffSettingExcelListener
 * @Description
 * @Date 2023-09-15 15:27
 * @Created by yl
 */

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.dto.excel.TargetStaffSettingImportExcelDTO;
import com.erp.model.bi.enums.MetricsEnum;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-15 15:27
 */
public class BiTargetStaffSettingExcelListener extends AnalysisEventListener<TargetStaffSettingImportExcelDTO> {

    private List<String> metricsNameList;

    private List<FindUserDTO> userList;

    /**
     * 成功的数据
     */
    private List<BiTargetStaffSettingDTO.CommonDTO> successList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<TargetStaffSettingImportExcelDTO> errorList = new ArrayList<>();

    public BiTargetStaffSettingExcelListener(List<String> metricsNameList, List<FindUserDTO> userList) {
        this.metricsNameList = metricsNameList;
        this.userList = userList;
    }

    @Override
    public void invoke(TargetStaffSettingImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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
        String staffName = excelDTO.getStaffName();
        FindUserDTO user = userList.stream().
                filter(u -> u.getUserName().equals(staffName)).
                findFirst().orElse(null);
        if (ObjectUtil.isEmpty(user)) {
            errorMsgList.add("人员不存在");
        }
        //添加错误数据
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        BiTargetStaffSettingDTO.CommonDTO addDTO = new BiTargetStaffSettingDTO.CommonDTO();
        MetricsEnum metricsEnum = MetricsEnum.getByName(metricsName);
        addDTO.setMetrics(metricsEnum);
        addDTO.setMetricsName(metricsName);
        //人
        addDTO.setStaffId(user.getUserId());
        addDTO.setStaffName(user.getUserName());
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

    public List<TargetStaffSettingImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<BiTargetStaffSettingDTO.CommonDTO> getSuccessList() {
        return successList;
    }
}
