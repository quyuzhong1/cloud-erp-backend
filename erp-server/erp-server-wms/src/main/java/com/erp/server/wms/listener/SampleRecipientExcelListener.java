package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.wms.dto.excel.SampleRecipientExcelDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 样品领用单Excel导入监听器
 * @author wuhaotian
 * @since 2025-08-22
 */
@Slf4j
public class SampleRecipientExcelListener extends AnalysisEventListener<SampleRecipientExcelDTO> {

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<SampleRecipientExcelDTO> successList = new ArrayList<>();
    private final List<SampleRecipientExcelDTO> errorList = new ArrayList<>();
    private int count = 0;

    public SampleRecipientExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    public void invoke(SampleRecipientExcelDTO data, AnalysisContext context) {
        count++;
        data.setRowNum(count);
        
        try {
            // 数据校验
            if (validateData(data)) {
                successList.add(data);
            } else {
                errorList.add(data);
            }
        } catch (Exception e) {
            log.error("解析第{}行数据失败", count, e);
            data.setErrorMsg("数据解析失败：" + e.getMessage());
            errorList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("样品领用单Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
    }

    /**
     * 数据校验
     */
    private boolean validateData(SampleRecipientExcelDTO data) {
        StringBuilder errorMsg = new StringBuilder();
        
        // 必填字段校验
        if (data.getRecipientDate() == null) {
            errorMsg.append("领用日期不能为空；");
        }
        
        if (data.getUsage() == null || data.getUsage().trim().isEmpty()) {
            errorMsg.append("用途不能为空；");
        }
        
        if (data.getWarehouseName() == null || data.getWarehouseName().trim().isEmpty()) {
            errorMsg.append("发货仓库不能为空；");
        }
        
        if (data.getUserName() == null || data.getUserName().trim().isEmpty()) {
            errorMsg.append("领用人不能为空；");
        }
        
        if (data.getDeptName() == null || data.getDeptName().trim().isEmpty()) {
            errorMsg.append("领用部门不能为空；");
        }
        
        if (data.getPickOrgName() == null || data.getPickOrgName().trim().isEmpty()) {
            errorMsg.append("领料组织不能为空；");
        }
        
        if (data.getUsageScope() == null || data.getUsageScope().trim().isEmpty()) {
            errorMsg.append("使用范围不能为空；");
        }
        
        if (data.getSkuNo() == null || data.getSkuNo().trim().isEmpty()) {
            errorMsg.append("SKU不能为空；");
        }
        
        if (data.getRecipientQty() == null || data.getRecipientQty() <= 0) {
            errorMsg.append("领用数量必须大于0；");
        }
        
        // 如果有错误信息，设置到DTO中并返回false
        if (errorMsg.length() > 0) {
            data.setErrorMsg(errorMsg.toString());
            return false;
        }
        
        return true;
    }

    public List<SampleRecipientExcelDTO> getSuccessList() {
        return successList;
    }

    public List<SampleRecipientExcelDTO> getErrorList() {
        return errorList;
    }

    public int getCount() {
        return count;
    }
}
