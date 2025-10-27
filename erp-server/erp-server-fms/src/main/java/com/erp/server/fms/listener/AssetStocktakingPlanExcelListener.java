package com.erp.server.fms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.erp.model.fms.dto.excel.AssetStocktakingPlanImportExcelDTO;
import com.erp.model.fms.enums.*;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.fms.service.AssetStocktakingPlanService;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产盘点方案导入Excel监听器
 * 
 * @author wuht
 * @date 2025-10-27
 */
@Slf4j
public class AssetStocktakingPlanExcelListener extends AnalysisEventListener<AssetStocktakingPlanImportExcelDTO> {

    private final AssetStocktakingPlanService assetStocktakingPlanService;
    private final AssetLocationService assetLocationService;
    private final SysUserFeign sysUserFeign;
    private final String taskId;
    private final String importType;
    private final Integer importCount;
    
    private final List<AssetStocktakingPlanImportExcelDTO> successList = new ArrayList<>();
    private final List<AssetStocktakingPlanImportExcelDTO> errorList = new ArrayList<>();
    private int count = 0;
    
    // 缓存数据
    private Map<String, String> assetLocationMap;
    private Map<String, String> deptNameIdMap;

    public AssetStocktakingPlanExcelListener(AssetStocktakingPlanService assetStocktakingPlanService, 
                                             AssetLocationService assetLocationService,
                                             SysUserFeign sysUserFeign,
                                             String taskId, 
                                             String importType, 
                                             Integer importCount) {
        this.assetStocktakingPlanService = assetStocktakingPlanService;
        this.assetLocationService = assetLocationService;
        this.sysUserFeign = sysUserFeign;
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        
        // 初始化缓存数据
        initCacheData();
    }

    private void initCacheData() {
        try {
            // 获取部门列表
            List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
            if (CollUtil.isNotEmpty(deptList)) {
                deptNameIdMap = deptList.stream()
                    .collect(Collectors.toMap(
                        SysDepartmentDTO::getName,
                        SysDepartmentDTO::getId,
                        (existing, replacement) -> existing
                    ));
            } else {
                deptNameIdMap = new HashMap<>();
            }
            
            // 获取资产位置列表
            List<com.erp.model.fms.entity.AssetLocationEntity> assetLocationList = assetLocationService.list();
            if (CollUtil.isNotEmpty(assetLocationList)) {
                assetLocationMap = assetLocationList.stream()
                    .collect(Collectors.toMap(
                        com.erp.model.fms.entity.AssetLocationEntity::getAddress,
                        com.erp.model.fms.entity.AssetLocationEntity::getId,
                        (existing, replacement) -> existing
                    ));
            } else {
                assetLocationMap = new HashMap<>();
            }
        } catch (Exception e) {
            log.error("初始化缓存数据失败", e);
        }
    }

    @Override
    public void invoke(AssetStocktakingPlanImportExcelDTO data, AnalysisContext context) {
        count++;
        try {
            // 数据验证
            validateData(data);
            
            // 数据转换
            AssetStocktakingPlanDTO.AddDTO addDTO = convertToAddDTO(data);
            
            // 保存数据
            assetStocktakingPlanService.add(addDTO);
            
            successList.add(data);
            log.info("第{}行数据导入成功：{}", count, data.getPlanName());
            
        } catch (Exception e) {
            log.error("第{}行数据导入失败：{}", count, e.getMessage());
            data.setErrorMsg(e.getMessage());
            errorList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("资产盘点方案导入完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        // 处理导入结果
        handleImportResult();
    }

    private void validateData(AssetStocktakingPlanImportExcelDTO data) {
        List<String> errorMsgList = new ArrayList<>();
        
        // 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (CollUtil.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        
        // 验证关联数据
        try {
            validateRelatedData(data);
        } catch (ServiceException e) {
            errorMsgList.add(e.getMessage());
        }
        
        // 如果有错误，抛出异常
        if (CollUtil.isNotEmpty(errorMsgList)) {
            throw new ServiceException(String.join("；", errorMsgList));
        }
    }

    private void validateRelatedData(AssetStocktakingPlanImportExcelDTO data) {
        // 验证资产组织
        try {
            SysAccountingCompanyEntity company = sysUserFeign.getCompanyByName(data.getAssetOrgName());
            if (company != null && StrUtil.isNotBlank(company.getId())) {
                data.setAssetOrgId(company.getId());
            } else {
                throw new ServiceException("资产组织【" + data.getAssetOrgName() + "】不存在");
            }
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException("资产组织【" + data.getAssetOrgName() + "】不存在");
        }
        
        // 验证资产类别（如果填写了）
        if (StringUtils.isNotBlank(data.getAssetCategories())) {
            String[] categories = data.getAssetCategories().split(",");
            for (String category : categories) {
                if (AssetCategoryEnum.getName(category.trim()).isEmpty()) {
                    throw new ServiceException("资产类别【" + category.trim() + "】不存在");
                }
            }
        }
        
        // 验证使用部门（如果填写了）
        if (StringUtils.isNotBlank(data.getUseDeptNames())) {
            String[] deptNames = data.getUseDeptNames().split(",");
            List<String> deptIdList = new ArrayList<>();
            
            // 批量查询部门
            try {
                List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByNames(Arrays.asList(deptNames));
                if (CollUtil.isNotEmpty(deptList)) {
                    Map<String, String> deptMap = deptList.stream()
                        .collect(Collectors.toMap(
                            SysDepartmentEntity::getName,
                            SysDepartmentEntity::getId,
                            (existing, replacement) -> existing
                        ));
                    
                    for (String deptName : deptNames) {
                        String deptId = deptMap.get(deptName.trim());
                        if (deptId == null) {
                            throw new ServiceException("使用部门【" + deptName.trim() + "】不存在");
                        }
                        deptIdList.add(deptId);
                    }
                } else {
                    throw new ServiceException("使用部门【" + deptNames[0].trim() + "】不存在");
                }
            } catch (Exception e) {
                if (e instanceof ServiceException) {
                    throw e;
                }
                throw new ServiceException("查询使用部门失败");
            }
            
            data.setUseDeptIds(String.join(",", deptIdList));
        }
        
        // 验证资产位置（如果填写了）
        if (StringUtils.isNotBlank(data.getAssetLocationNames())) {
            String[] locationNames = data.getAssetLocationNames().split(",");
            List<String> locationIdList = new ArrayList<>();
            for (String locationName : locationNames) {
                String locationId = assetLocationMap.get(locationName.trim());
                if (locationId == null) {
                    throw new ServiceException("资产位置【" + locationName.trim() + "】不存在");
                }
                locationIdList.add(locationId);
            }
            data.setAssetLocationIds(String.join(",", locationIdList));
        }
    }

    private AssetStocktakingPlanDTO.AddDTO convertToAddDTO(AssetStocktakingPlanImportExcelDTO data) {
        AssetStocktakingPlanDTO.AddDTO addDTO = new AssetStocktakingPlanDTO.AddDTO();
        
        addDTO.setAssetOrgId(data.getAssetOrgId());
        addDTO.setAssetOrgName(data.getAssetOrgName());
        addDTO.setPlanName(data.getPlanName());
        addDTO.setRemark(data.getRemark());
        addDTO.setAssetCategories(data.getAssetCategories());
        addDTO.setUseDeptIds(data.getUseDeptIds());
        addDTO.setAssetLocationIds(data.getAssetLocationIds());
        addDTO.setCardCodeStart(data.getCardCodeStart());
        addDTO.setCardCodeEnd(data.getCardCodeEnd());
        
        return addDTO;
    }

    private void handleImportResult() {
        // 这里可以添加导入结果处理逻辑，比如发送通知等
        log.info("导入任务完成，任务ID：{}，成功：{}，失败：{}", taskId, successList.size(), errorList.size());
    }

    public List<AssetStocktakingPlanImportExcelDTO> getSuccessList() {
        return successList;
    }

    public List<AssetStocktakingPlanImportExcelDTO> getErrorList() {
        return errorList;
    }

    public int getCount() {
        return count;
    }
}

