package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.excel.QcApplicationImportExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 质检申请监听器
 * @author will
 * @date 2026/3/24 14:17
 */
@Slf4j
public class QcApplicationExcelListener extends AnalysisEventListener<QcApplicationImportExcelDTO> {

    private final List<QcApplicationImportExcelDTO> allList = new ArrayList<>();
    private final List<QcApplicationImportExcelDTO> successList = new ArrayList<>();
    private final List<QcApplicationImportExcelDTO> errorList = new ArrayList<>();
    private int count = 0;

    public QcApplicationExcelListener() {
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(QcApplicationImportExcelDTO data, AnalysisContext context) {
        List<String> errorMsgList = new ArrayList<>();

        // 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (CollUtil.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }

        successList.add(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    public List<QcApplicationImportExcelDTO> getAllList() {
        return allList;
    }

    public List<QcApplicationImportExcelDTO> getSuccessList() {
        return successList;
    }

    public List<QcApplicationImportExcelDTO> getErrorList() {
        return errorList;
    }

    public int getCount() {
        return count;
    }
}

