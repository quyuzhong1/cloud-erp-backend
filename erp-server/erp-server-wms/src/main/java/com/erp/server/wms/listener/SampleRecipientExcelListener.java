package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.excel.SampleRecipientExcelDTO;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.erp.model.wms.entity.SampleRecipientDetailEntity;
import com.erp.server.wms.service.SampleRecipientService;
import com.erp.server.wms.service.SampleRecipientDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 样品领用单Excel导入监听器
 * @author wuhaotian
 * @since 2025-08-22
 */
@Slf4j
@Component
public class SampleRecipientExcelListener extends AnalysisEventListener<SampleRecipientExcelDTO> {

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<SampleRecipientExcelDTO> successList = new ArrayList<>();
    private final List<SampleRecipientExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    @Autowired
    private SampleRecipientService sampleRecipientService;

    @Autowired
    private SampleRecipientDetailService sampleRecipientDetailService;

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
        
        // 每1000条处理一次
        if (successList.size() >= 1000) {
            handleBatchData();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("样品领用单Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        // 处理剩余的数据
        if (!successList.isEmpty()) {
            handleBatchData();
        }
    }

    /**
     * 批量处理数据
     */
    private void handleBatchData() {
        if (successList.isEmpty()) {
            return;
        }
        
        try {
            List<SampleRecipientExcelDTO> batchData = new ArrayList<>(successList);
            successList.clear();
            
            // 调用服务处理数据
            sampleRecipientService.handleImportSuccessList(batchData, errorNoList, errorList, importType);
            
        } catch (Exception e) {
            log.error("批量处理数据失败", e);
            // 将失败的数据移到错误列表
            for (SampleRecipientExcelDTO data : successList) {
                data.setErrorMsg("批量处理失败：" + e.getMessage());
                errorList.add(data);
            }
            successList.clear();
        }
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

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
    }
}
