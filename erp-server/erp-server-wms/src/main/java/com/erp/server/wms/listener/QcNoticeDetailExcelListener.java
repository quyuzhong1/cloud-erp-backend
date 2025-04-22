package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.excel.DeliveryPlanDetailExportExcelDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QcNoticeDetailExcelListener extends AnalysisEventListener<QcNoticeDTO.QcNoticeDetailExportExcelDTO> {

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<QcNoticeDTO.QcNoticeDetailExportExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<QcNoticeDTO.QcNoticeDetailExportExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<QcNoticeDTO.QcNoticeDetailExportExcelDTO> successList = new ArrayList<>();

    @Override
    public void invoke(QcNoticeDTO.QcNoticeDetailExportExcelDTO deliveryPlanDetailExportExcelDTO, AnalysisContext analysisContext) {
        QcNoticeDTO.QcNoticeDetailExportExcelDTO viewDTO = new QcNoticeDTO.QcNoticeDetailExportExcelDTO();
        //添加数据用于判断是否为空
        allList.add(deliveryPlanDetailExportExcelDTO);

        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(deliveryPlanDetailExportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            deliveryPlanDetailExportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(deliveryPlanDetailExportExcelDTO);
            return;
        }
        successList.add(viewDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {


    }

    public List<QcNoticeDTO.QcNoticeDetailExportExcelDTO> getAllList(){
        return allList;
    }

    public List<QcNoticeDTO.QcNoticeDetailExportExcelDTO> getErrorList(){
        return errorList;
    }

    public List<QcNoticeDTO.QcNoticeDetailExportExcelDTO> getSuccessList(){
        return successList;
    }
}
