package com.erp.server.fms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.fms.dto.excel.AssetStocktakingPlanImportExcelDTO;
import com.erp.model.fms.enums.*;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.fms.service.AssetStocktakingPlanService;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 资产盘点方案导入Excel监听器
 * 
 * @author wuht
 * @date 2025-10-27
 */
@Slf4j
public class AssetStocktakingPlanExcelListener extends AnalysisEventListener<AssetStocktakingPlanImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    
    private final List<AssetStocktakingPlanImportExcelDTO> successList = new ArrayList<>();
    private final List<AssetStocktakingPlanImportExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    // 缓存相关常量
    private static final String CACHE_ORG_NAME_TO_ID = "stocktaking_plan:org_name_to_id:";
    private static final String CACHE_DEPT_NAME_TO_ID = "stocktaking_plan:dept_name_to_id:";
    private static final String CACHE_ASSET_LOCATION_NAME_TO_ID = "stocktaking_plan:asset_location_name_to_id:";
    private static final int CACHE_EXPIRE_TIME = 300; // 五分钟

    private final AssetStocktakingPlanService assetStocktakingPlanService = SpringUtil.getBean(AssetStocktakingPlanService.class);
    private final AssetLocationService assetLocationService = SpringUtil.getBean(AssetLocationService.class);
    private final SysUserFeign sysUserFeign = SpringUtil.getBean(SysUserFeign.class);
    private final RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public AssetStocktakingPlanExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(AssetStocktakingPlanImportExcelDTO data, AnalysisContext context) {
        count++;
        
        // 已经导入的数据跳过进度
        if (importCount != null && count < importCount) {
            return;
        }
        
        List<String> errorMsgList = new ArrayList<>();
        
        // 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (CollUtil.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        
        // 数据校验和转换
        validateAndConvertData(data, errorMsgList);
        
        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }
        
        successList.add(data);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(e -> String.valueOf(e.getNo())).distinct().collect(Collectors.toList());
                List<AssetStocktakingPlanImportExcelDTO> errorList2 = new ArrayList<>();
                assetStocktakingPlanService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("资产盘点方案Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream().map(e -> String.valueOf(e.getNo())).distinct().collect(Collectors.toList());
                List<AssetStocktakingPlanImportExcelDTO> errorList2 = new ArrayList<>();
                assetStocktakingPlanService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 数据校验和转换
     */
    private void validateAndConvertData(AssetStocktakingPlanImportExcelDTO data, List<String> errorMsgList) {
        // 验证并解析关联数据ID
        validateAndResolveIds(data, errorMsgList);
        
        // 验证并转换枚举值为code（如果有枚举字段）
        validateAndConvertEnumValues(data, errorMsgList);
    }

    /**
     * 验证并转换枚举值为code
     */
    private void validateAndConvertEnumValues(AssetStocktakingPlanImportExcelDTO data, List<String> errorMsgList) {
        // 验证并转换资产类别（如果填写了）
        if (StringUtils.isNotBlank(data.getAssetCategories())) {
            String[] categories = data.getAssetCategories().split(",");
            List<String> categoryCodes = new ArrayList<>();
            
            for (String category : categories) {
                String categoryCode = AssetCategoryEnum.getCodeByName(category.trim());
                if (StrUtil.isBlank(categoryCode)) {
                    errorMsgList.add("资产类别【" + category.trim() + "】不存在");
                } else {
                    categoryCodes.add(categoryCode);
                }
            }
            
            if (categoryCodes.size() == categories.length) {
                data.setAssetCategories(String.join(",", categoryCodes));
            }
        }
    }

    /**
     * 验证并解析关联数据ID
     */
    private void validateAndResolveIds(AssetStocktakingPlanImportExcelDTO data, List<String> errorMsgList) {
        // 验证资产组织名称是否存在并解析组织ID
        String assetOrgId = getOrgIdByName(data.getAssetOrgName());
        if (StrUtil.isBlank(assetOrgId)) {
            errorMsgList.add("资产组织【" + data.getAssetOrgName() + "】不存在");
        } else {
            data.setAssetOrgId(assetOrgId);
        }
        
        // 验证使用部门（如果填写了）
        if (StringUtils.isNotBlank(data.getUseDeptNames())) {
            String[] deptNames = data.getUseDeptNames().split(",");
            List<String> deptIdList = new ArrayList<>();
            
            for (String deptName : deptNames) {
                String deptId = getDeptIdByName(deptName.trim());
                if (StrUtil.isBlank(deptId)) {
                    errorMsgList.add("使用部门【" + deptName.trim() + "】不存在");
                } else {
                    deptIdList.add(deptId);
                }
            }
            
            if (deptIdList.size() == deptNames.length) {
                data.setUseDeptIds(String.join(",", deptIdList));
            }
        }
        
        // 验证资产位置（如果填写了）
        if (StringUtils.isNotBlank(data.getAssetLocationNames())) {
            String[] locationNames = data.getAssetLocationNames().split(",");
            List<String> locationIdList = new ArrayList<>();
            
            for (String locationName : locationNames) {
                String locationId = getAssetLocationIdByName(locationName.trim());
                if (StrUtil.isBlank(locationId)) {
                    errorMsgList.add("资产位置【" + locationName.trim() + "】不存在");
                } else {
                    locationIdList.add(locationId);
                }
            }
            
            if (locationIdList.size() == locationNames.length) {
                data.setAssetLocationIds(String.join(",", locationIdList));
            }
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTask(Integer count) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

    public List<AssetStocktakingPlanImportExcelDTO> getSuccessList() {
        return successList;
    }

    public List<AssetStocktakingPlanImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
    }

    /**
     * 根据组织名称查询组织ID（带缓存）
     */
    private String getOrgIdByName(String orgName) {
        if (StrUtil.isBlank(orgName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_ORG_NAME_TO_ID + orgName;
        String orgId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(orgId)) {
            return orgId;
        }

        try {
            // 调用组织服务根据名称查询组织信息
            SysAccountingCompanyEntity companyId = sysUserFeign.getCompanyByName(orgName);

            if (companyId != null && StrUtil.isNotBlank(companyId.getId())) {
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(companyId.getId(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return companyId.getId();
            }

            log.warn("未找到组织名称：{}", orgName);
            return null;
        } catch (Exception e) {
            log.error("查询组织ID失败，组织名称：{}，错误：{}", orgName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据部门名称查询部门ID（带缓存）
     */
    private String getDeptIdByName(String deptName) {
        if (StrUtil.isBlank(deptName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_DEPT_NAME_TO_ID + deptName;
        String deptId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(deptId)) {
            return deptId;
        }

        try {
            // 调用部门服务根据名称查询部门信息
            List<SysDepartmentEntity> deptList = sysUserFeign.getDeptByNames(
                    Collections.singletonList(deptName)
            );

            if (CollUtil.isNotEmpty(deptList)) {
                // 返回第一个匹配的部门ID
                String result = deptList.get(0).getId();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到部门名称：{}", deptName);
            return null;
        } catch (Exception e) {
            log.error("查询部门ID失败，部门名称：{}，错误：{}", deptName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据资产位置名称查询资产位置ID（带缓存）
     */
    private String getAssetLocationIdByName(String assetLocationName) {
        if (StrUtil.isBlank(assetLocationName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_ASSET_LOCATION_NAME_TO_ID + assetLocationName;
        String assetLocationId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(assetLocationId)) {
            return assetLocationId;
        }

        try {
            // 调用资产位置服务根据地址或详细地址查询资产位置信息
            List<com.erp.model.fms.entity.AssetLocationEntity> assetLocationList = assetLocationService.lambdaQuery()
                    .and(wrapper -> wrapper
                        .eq(com.erp.model.fms.entity.AssetLocationEntity::getAddress, assetLocationName)
                        .or()
                        .eq(com.erp.model.fms.entity.AssetLocationEntity::getDetailedAddress, assetLocationName)
                    )
                    .eq(com.erp.model.fms.entity.AssetLocationEntity::getIsDeleted, false)
                    .list();

            if (CollUtil.isNotEmpty(assetLocationList)) {
                // 返回第一个匹配的资产位置ID
                String result = assetLocationList.get(0).getId();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到资产位置（地址或详细地址）：{}", assetLocationName);
            return null;
        } catch (Exception e) {
            log.error("查询资产位置ID失败，资产位置名称：{}，错误：{}", assetLocationName, e.getMessage(), e);
            return null;
        }
    }
}

