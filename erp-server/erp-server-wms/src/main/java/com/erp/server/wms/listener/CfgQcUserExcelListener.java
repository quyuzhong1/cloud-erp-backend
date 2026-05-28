package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.server.wms.mapper.CfgQcUserMapper;
import com.erp.server.wms.service.CfgQcUserService;
import com.erp.server.wms.service.WarehouseService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final KingdeeFeign kingdeeFeign = SpringUtil.getBean(KingdeeFeign.class);

    private final WarehouseService warehouseService = SpringUtil.getBean(WarehouseService.class);

    /**
     * 按组织缓存业务员管理中的质检员（type=ZJY）映射：name -> userId
     */
    private final Map<String, Map<String, String>> qcUserNameToIdMapByOrgId = new HashMap<>();

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
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            processBatch();
            updateTask(count);
        }
    }

    private void processBatch() {
        List<String> warehouseNames = successList.stream()
                .map(CfgQcUserDTO.ImportExcelDTO::getWarehouseName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<WarehouseDTO.ListDTO>> warehouseMap = new HashMap<>();
        if (CollUtil.isNotEmpty(warehouseNames)) {
            List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(warehouseNames);
            if (CollUtil.isNotEmpty(warehouseList)) {
                warehouseMap = warehouseList.stream().collect(Collectors.groupingBy(WarehouseDTO.ListDTO::getName));
            }
        }

        for (CfgQcUserDTO.ImportExcelDTO dto : new ArrayList<>(successList)) {
            try {
                List<SupplierEntity> suppliers = supplierFeign.listByCodes(Collections.singletonList(dto.getSupplierCode()));
                if (CollectionUtils.isEmpty(suppliers)) {
                    dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_SUPPLIER_NOT_FOUND.getMsg(), dto.getSupplierCode()));
                    errorList.add(dto);
                    continue;
                }

                WarehouseDTO.ListDTO warehouse = resolveWarehouse(dto, warehouseMap);
                if (warehouse == null) {
                    errorList.add(dto);
                    continue;
                }

                Map<String, String> qcUserMap = getQcUserNameToIdMap(warehouse.getOrgId());
                List<String> notFoundUsers = new ArrayList<>();
                String stockInId = resolveQcUserId(qcUserMap, dto.getStockInQcUserName(), notFoundUsers);
                String stockOutId = resolveQcUserId(qcUserMap, dto.getStockOutQcUserName(), notFoundUsers);
                String outsideId = resolveQcUserId(qcUserMap, dto.getOutsideQcUserName(), notFoundUsers);
                String insideId = resolveQcUserId(qcUserMap, dto.getInsideQcUserName(), notFoundUsers);
                String newProductStockInId = resolveQcUserId(qcUserMap, dto.getNewProductStockInQcUserName(), notFoundUsers);
                String b2bOutsideId = resolveQcUserId(qcUserMap, dto.getB2bOutsideQcUserName(), notFoundUsers);
                String returnId = resolveQcUserId(qcUserMap, dto.getReturnQcUserName(), notFoundUsers);
                if (!notFoundUsers.isEmpty()) {
                    Set<String> uniq = new LinkedHashSet<>(notFoundUsers);
                    dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_IMPORT_USER_NOT_IN_ORG.getMsg(),
                            String.join(",", uniq)));
                    errorList.add(dto);
                    continue;
                }

                SupplierEntity supplier = suppliers.get(0);
                CfgQcUserEntity exists = cfgQcUserMapper.selectBySupplierIdAndWarehouseId(supplier.getId(), warehouse.getId());

                if (Objects.nonNull(exists)) {
                    CfgQcUserDTO.UpdateDTO updateDTO = new CfgQcUserDTO.UpdateDTO();
                    updateDTO.setId(exists.getId());
                    updateDTO.setSupplierId(supplier.getId());
                    updateDTO.setWarehouseId(warehouse.getId());
                    convertImportToCommonDTO(dto, updateDTO,
                            stockInId, stockOutId, outsideId, insideId,
                            newProductStockInId, b2bOutsideId, returnId);
                    cfgQcUserService.update(updateDTO);
                } else {
                    CfgQcUserDTO.AddDTO addDTO = new CfgQcUserDTO.AddDTO();
                    addDTO.setSupplierId(supplier.getId());
                    addDTO.setWarehouseId(warehouse.getId());
                    convertImportToCommonDTO(dto, addDTO,
                            stockInId, stockOutId, outsideId, insideId,
                            newProductStockInId, b2bOutsideId, returnId);
                    cfgQcUserService.add(addDTO);
                }
            } catch (Exception e) {
                log.error("质检员配置导入处理失败，supplierCode={}", dto.getSupplierCode(), e);
                String message = e.getMessage() == null ? "" : e.getMessage();
                dto.setErrorMsg(message.length() > 200 ? message.substring(0, 200) : message);
                errorList.add(dto);
            }
        }
        successList.clear();
    }

    private WarehouseDTO.ListDTO resolveWarehouse(CfgQcUserDTO.ImportExcelDTO dto,
                                                  Map<String, List<WarehouseDTO.ListDTO>> warehouseMap) {
        String warehouseName = dto.getWarehouseName();
        if (StrUtil.isBlank(warehouseName)) {
            dto.setErrorMsg(ApiError.CFG_QC_USER_WAREHOUSE_REQUIRED.getMsg());
            return null;
        }
        List<WarehouseDTO.ListDTO> warehouses = warehouseMap.get(warehouseName.trim());
        if (CollUtil.isEmpty(warehouses)) {
            dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_WAREHOUSE_NOT_FOUND.getMsg(), warehouseName));
            return null;
        }
        if (warehouses.size() > 1) {
            dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_WAREHOUSE_NAME_DUPLICATE.getMsg(), warehouseName));
            return null;
        }
        WarehouseDTO.ListDTO warehouse = warehouses.get(0);
        if (Boolean.TRUE.equals(warehouse.getDisabled())
                || warehouse.getApproveStatus() == null
                || !ApproveStatusEnum.APPROVE.getStatus().equals(warehouse.getApproveStatus().getCode())) {
            dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_WAREHOUSE_NOT_FOUND.getMsg(), warehouseName));
            return null;
        }
        return warehouse;
    }

    private Map<String, String> getQcUserNameToIdMap(String orgId) {
        String cacheKey = StrUtil.blankToDefault(orgId, "");
        if (qcUserNameToIdMapByOrgId.containsKey(cacheKey)) {
            return qcUserNameToIdMapByOrgId.get(cacheKey);
        }
        Map<String, String> map = new HashMap<>();
        try {
            KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO param = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
            param.setType("ZJY");
            if (StrUtil.isNotBlank(orgId)) {
                param.setOrgId(orgId);
            }
            ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> apiResult = kingdeeFeign.listKingdeeUser(param);
            if (apiResult != null && apiResult.isSuccess() && CollUtil.isNotEmpty(apiResult.getData())) {
                for (UserInfoDTO.BusinessOperationUserDTO user : apiResult.getData()) {
                    if (StrUtil.isNotBlank(user.getRealName())) {
                        map.putIfAbsent(user.getRealName(), user.getUserId());
                    }
                    if (StrUtil.isNotBlank(user.getUserName())) {
                        map.putIfAbsent(user.getUserName(), user.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("加载业务员管理质检员列表失败，orgId={}", orgId, e);
        }
        qcUserNameToIdMapByOrgId.put(cacheKey, map);
        return map;
    }

    private String resolveQcUserId(Map<String, String> qcUserMap, String name, List<String> notFoundUsers) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        String userId = qcUserMap.get(name.trim());
        if (StrUtil.isBlank(userId)) {
            notFoundUsers.add(name.trim());
            return null;
        }
        return userId;
    }

    private void convertImportToCommonDTO(CfgQcUserDTO.ImportExcelDTO importDTO, CfgQcUserDTO.CommonDTO commonDTO,
                                          String stockInId, String stockOutId, String outsideId, String insideId,
                                          String newProductStockInId, String b2bOutsideId, String returnId) {
        commonDTO.setStockInQcUserId(stockInId);
        commonDTO.setStockInQcUserName(importDTO.getStockInQcUserName());
        commonDTO.setStockOutQcUserId(stockOutId);
        commonDTO.setStockOutQcUserName(importDTO.getStockOutQcUserName());
        commonDTO.setOutsideQcUserId(outsideId);
        commonDTO.setOutsideQcUserName(importDTO.getOutsideQcUserName());
        commonDTO.setInsideQcUserId(insideId);
        commonDTO.setInsideQcUserName(importDTO.getInsideQcUserName());
        commonDTO.setNewProductStockInQcUserId(newProductStockInId);
        commonDTO.setNewProductStockInQcUserName(importDTO.getNewProductStockInQcUserName());
        commonDTO.setB2bOutsideQcUserId(b2bOutsideId);
        commonDTO.setB2bOutsideQcUserName(importDTO.getB2bOutsideQcUserName());
        commonDTO.setReturnQcUserId(returnId);
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
