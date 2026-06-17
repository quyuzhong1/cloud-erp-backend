package com.erp.server.wms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.CfgQcUserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CfgQcUserExcelListener extends AnalysisEventListener<CfgQcUserDTO.ImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private static final int MAX_IMPORT_ROWS = 5000;

    private final String taskId;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private List<CfgQcUserDTO.ImportExcelDTO> allList = new ArrayList<>();

    private List<CfgQcUserDTO.ImportExcelDTO> errorList = new ArrayList<>();

    private List<CfgQcUserDTO.ImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    private final CfgQcUserService cfgQcUserService = SpringUtil.getBean(CfgQcUserService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public CfgQcUserExcelListener(String taskId, Integer importCount) {
        this.taskId = taskId;
        this.importCount = importCount;
    }

    @Override
    public void invoke(CfgQcUserDTO.ImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        count += 1;
        if (count > MAX_IMPORT_ROWS) {
            throw new ServiceException(ApiError.COMMON_IMPORT_SIZE_EXCEED_LIMIT, MAX_IMPORT_ROWS);
        }
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }

        allList.add(importExcelDTO);

        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        if (StrUtil.isBlank(importExcelDTO.getSupplierCode())) {
            errorMsgList.add(ApiError.CFG_QC_USER_SUPPLIER_REQUIRED.getMsg());
        }

        if (!errorMsgList.isEmpty()) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        successList.add(importExcelDTO);

        if (successList.size() >= BATCH_COUNT) {
            processBatch();
            updateTask(count);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            processBatch();
            updateTask(count);
        }
    }

    private void processBatch() {
        cfgQcUserService.handleImportSuccessList(successList, errorList);
    }

    private void updateTask(Integer count) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

    public List<CfgQcUserDTO.ImportExcelDTO> getAllList() {
        return allList;
    }

    public List<CfgQcUserDTO.ImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<CfgQcUserDTO.ImportExcelDTO> getSuccessList() {
        return successList;
    }
}
