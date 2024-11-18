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
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import com.erp.model.bi.dto.excel.TargetNewProductSettingImportExcelDTO;
import com.erp.model.bi.enums.MetricsEnum;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-15 15:27
 */
public class BiTargetNewProductSettingExcelListener extends AnalysisEventListener<TargetNewProductSettingImportExcelDTO> {

    private List<String> metricsNameList;

    private List<FindUserDTO> userList;

    /**
     * 成功的数据
     */
    private List<BiTargetNewProductSettingDTO.CommonDTO> successList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<TargetNewProductSettingImportExcelDTO> errorList = new ArrayList<>();

    public BiTargetNewProductSettingExcelListener(List<String> metricsNameList, List<FindUserDTO> userList) {
        this.metricsNameList = metricsNameList;
        this.userList = userList;
    }

    @Override
    public void invoke(TargetNewProductSettingImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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
        String staffName = excelDTO.getStaffName();
        FindUserDTO user = userList.stream().
                filter(u -> u.getUserName().equals(staffName)).
                findFirst().orElse(null);
        if (ObjectUtil.isEmpty(user)) {
            errorMsgList.add("人员不存在");
        }
        //添加错误数据
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        BiTargetNewProductSettingDTO.CommonDTO addDTO = new BiTargetNewProductSettingDTO.CommonDTO();
        MetricsEnum metricsEnum = MetricsEnum.getByName(metricsName);
        addDTO.setMetrics(metricsEnum);
        addDTO.setMetricsName(metricsName);
        //人
        if (user == null) {
            return;
        }
        addDTO.setStaffId(user.getUserId());
        addDTO.setStaffName(user.getUserName());
        //一月
        addDTO.setJanuary(excelDTO.getJanuary());
        addDTO.setJanuaryRate(excelDTO.getJanuaryRate());
        //二月
        addDTO.setFebruary(excelDTO.getFebruary());
        addDTO.setFebruaryRate(excelDTO.getFebruaryRate());

        addDTO.setMarch(excelDTO.getMarch());
        addDTO.setMarchRate(excelDTO.getMarchRate());

        addDTO.setApril(excelDTO.getApril());
        addDTO.setAprilRate(excelDTO.getAprilRate());

        addDTO.setMay(excelDTO.getMay());
        addDTO.setMayRate(excelDTO.getMayRate());

        addDTO.setJune(excelDTO.getJune());
        addDTO.setJuneRate(excelDTO.getJuneRate());

        addDTO.setJuly(excelDTO.getJuly());
        addDTO.setJulyRate(excelDTO.getJulyRate());

        addDTO.setAugust(excelDTO.getAugust());
        addDTO.setAugustRate(excelDTO.getAugustRate());

        addDTO.setSeptember(excelDTO.getSeptember());
        addDTO.setSeptemberRate(excelDTO.getSeptemberRate());

        addDTO.setOctober(excelDTO.getOctober());
        addDTO.setDecemberRate(excelDTO.getDecemberRate());

        addDTO.setNovember(excelDTO.getNovember());
        addDTO.setNovemberRate(excelDTO.getNovemberRate());

        addDTO.setDecember(excelDTO.getDecember());
        addDTO.setDecemberRate(excelDTO.getDecemberRate());
        successList.add(addDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // document why this method is empty
    }

    public List<TargetNewProductSettingImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<BiTargetNewProductSettingDTO.CommonDTO> getSuccessList() {
        return successList;
    }
}
