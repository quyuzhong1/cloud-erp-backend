package com.erp.server.wms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.wms.mapper.CfgQcUserMapper;
import com.erp.server.wms.service.CfgQcUserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CfgQcUserExcelListener extends AnalysisEventListener<CfgQcUserDTO.ImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private List<CfgQcUserDTO.ImportExcelDTO> allList = new ArrayList<>();

    private List<CfgQcUserDTO.ImportExcelDTO> errorList = new ArrayList<>();

    private List<CfgQcUserDTO.ImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    private final SupplierFeign supplierFeign = SpringUtil.getBean(SupplierFeign.class);

    private final CfgQcUserMapper cfgQcUserMapper = SpringUtil.getBean(CfgQcUserMapper.class);

    private final CfgQcUserService cfgQcUserService = SpringUtil.getBean(CfgQcUserService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public CfgQcUserExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    public void invoke(CfgQcUserDTO.ImportExcelDTO importExcelDTO, AnalysisContext analysisContext) {
        count += 1;
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }

        allList.add(importExcelDTO);

        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(importExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        if (errorMsgList.size() > 0) {
            importExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(importExcelDTO);
            return;
        }

        if (StrUtil.isBlank(importExcelDTO.getSupplierCode())) {
            errorMsgList.add("供应商编码不能为空");
        }

        if (errorMsgList.size() > 0) {
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
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            processBatch();
            updateTask(count);
        }
    }

    private void processBatch() {
        try {
            for (CfgQcUserDTO.ImportExcelDTO dto : successList) {
                List<SupplierEntity> suppliers = supplierFeign.listByCodes(Collections.singletonList(dto.getSupplierCode()));
                if (CollectionUtils.isEmpty(suppliers)) {
                    dto.setErrorMsg("供应商编码不存在");
                    errorList.add(dto);
                    continue;
                }

                SupplierEntity supplier = suppliers.get(0);
                CfgQcUserEntity exists = cfgQcUserMapper.selectBySupplierId(supplier.getId());

                if (Objects.nonNull(exists)) {
                    CfgQcUserDTO.UpdateDTO updateDTO = new CfgQcUserDTO.UpdateDTO();
                    updateDTO.setId(exists.getId());
                    updateDTO.setSupplierId(supplier.getId());
                    convertImportToCommonDTO(dto, updateDTO);
                    cfgQcUserService.update(updateDTO);
                } else {
                    CfgQcUserDTO.AddDTO addDTO = new CfgQcUserDTO.AddDTO();
                    addDTO.setSupplierId(supplier.getId());
                    convertImportToCommonDTO(dto, addDTO);
                    cfgQcUserService.add(addDTO);
                }
            }
        } catch (Exception e) {
            log.error("批量处理失败", e);
            successList.forEach(dto -> dto.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
            errorList.addAll(successList);
        }
        successList.clear();
    }

    private void convertImportToCommonDTO(CfgQcUserDTO.ImportExcelDTO importDTO, CfgQcUserDTO.CommonDTO commonDTO) {
        commonDTO.setStockInQcUserName(importDTO.getStockInQcUserName());
        commonDTO.setStockOutQcUserName(importDTO.getStockOutQcUserName());
        commonDTO.setOutsideQcUserName(importDTO.getOutsideQcUserName());
        commonDTO.setInsideQcUserName(importDTO.getInsideQcUserName());
        commonDTO.setNewProductStockInQcUserName(importDTO.getNewProductStockInQcUserName());
        commonDTO.setB2bOutsideQcUserName(importDTO.getB2bOutsideQcUserName());
        commonDTO.setReturnQcUserName(importDTO.getReturnQcUserName());
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