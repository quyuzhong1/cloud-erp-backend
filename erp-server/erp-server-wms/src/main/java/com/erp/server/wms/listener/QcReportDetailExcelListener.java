package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.dto.excel.QcReportDetailImportExcelDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname QcReportDetailExcelListener
 * @Description TODO
 * @Date 2023-04-21 19:31
 * @Created by yl
 */
public class QcReportDetailExcelListener extends AnalysisEventListener<QcReportDetailImportExcelDTO> {


    /**
     * 质检规则对应的质检报告
     */
    List<QcReportDTO.ListDTO> qcReportList;

    /**
     * 字典列表
     */
    List<DictBasicEntity> dictList;
    /**
     * 成功的数据
     */
    private List<QcReportDetailDTO.ListDTO> successList = new ArrayList<>();


    /**
     * 导入错误数据
     */
    private List<QcReportDetailImportExcelDTO> errorList = new ArrayList<>();


    /**
     * 带过来
     *
     * @param qcReportList
     */
    public QcReportDetailExcelListener(List<QcReportDTO.ListDTO> qcReportList, List<DictBasicEntity> dictList) {
        this.qcReportList = qcReportList;
        this.dictList = dictList;
    }


    /**
     * 没解析一行执行一次
     *
     * @param qcReportDetailImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-04-21 19:40
     */
    @Override
    public void invoke(QcReportDetailImportExcelDTO qcReportDetailImportExcelDTO, AnalysisContext analysisContext) {
        QcReportDetailDTO.ListDTO addDTO = new QcReportDetailDTO.ListDTO();
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(qcReportDetailImportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //质检报告
        QcReportDTO.ListDTO qcReport = qcReportList.stream().filter(
                obj -> obj.getQcReportName().equals(qcReportDetailImportExcelDTO.getQcReportName())
                        && obj.getQcReportContent().equals(qcReportDetailImportExcelDTO.getQcReportContent())
        ).findFirst().orElse(null);

        //质检结果
        DictBasicEntity qcResult = dictList.stream().filter(
                dict -> dict.getName().
                        equals(qcReportDetailImportExcelDTO.getResultDict())).findFirst().orElse(null);
        if(Objects.isNull(qcResult)){
            errorMsgList.add("质检结果有误");
        }
        if (Objects.isNull(qcReport)) {
            errorMsgList.add("质检报告不存在");
        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            qcReportDetailImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(qcReportDetailImportExcelDTO);
            return;
        }

        addDTO.setDescription(qcReportDetailImportExcelDTO.getDescription());
        addDTO.setQcReportId(qcReport.getQcReportId());
        addDTO.setQcReportContent(qcReport.getQcReportContent());
        addDTO.setQcReportName(qcReport.getQcReportName());
        addDTO.setResultDict(qcResult.getValue());
        successList.add(addDTO);

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


    public List<QcReportDetailImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<QcReportDetailDTO.ListDTO> getSuccessList() {
        return successList;
    }


}
