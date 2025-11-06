package com.erp.server.fms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.fms.dto.excel.AssetCardImportExcelDTO;
import com.erp.model.fms.enums.*;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.fms.service.AssetCardService;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 资产卡片导入Excel监听器
 * 
 * @author wuht
 * @date 2025-01-10
 */
@Slf4j
public class AssetCardExcelListener extends AnalysisEventListener<AssetCardImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    
    private final List<AssetCardImportExcelDTO> successList = new ArrayList<>();
    private final List<AssetCardImportExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    // 缓存相关常量
    private static final String CACHE_DEPT_NAME_TO_ID = "asset_card:dept_name_to_id:";
    private static final String CACHE_ASSET_LOCATION_NAME_TO_ID = "asset_card:asset_location_name_to_id:";
    private static final int CACHE_EXPIRE_TIME = 300; // 五分钟

    private final AssetCardService assetCardService = SpringUtil.getBean(AssetCardService.class);
    private final AssetLocationService assetLocationService = SpringUtil.getBean(AssetLocationService.class);
    private final SysUserFeign sysUserFeign = SpringUtil.getBean(SysUserFeign.class);
    private final RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public AssetCardExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(AssetCardImportExcelDTO data, AnalysisContext context) {
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
        
        // 日期转换处理
        convertDateFields(data, errorMsgList);
        
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
                List<AssetCardImportExcelDTO> errorList2 = new ArrayList<>();
                assetCardService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
        log.info("资产卡片Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream().map(e -> String.valueOf(e.getNo())).distinct().collect(Collectors.toList());
                List<AssetCardImportExcelDTO> errorList2 = new ArrayList<>();
                assetCardService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
    private void validateAndConvertData(AssetCardImportExcelDTO data, List<String> errorMsgList) {
        // 验证并转换枚举值为code
        validateAndConvertEnumValues(data, errorMsgList);
        
        // 验证并解析关联数据ID
        validateAndResolveIds(data, errorMsgList);
    }

    /**
     * 日期转换处理
     */
    private void convertDateFields(AssetCardImportExcelDTO data, List<String> errorMsgList) {
        // 开始使用日期转换
        String startUseDateStr = data.getStartUseDateStr();
        if (StringUtils.isNotBlank(startUseDateStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDate startUseDate = null;
                try {
                    startUseDate = LocalDate.parse(startUseDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e1) {
                    try {
                        startUseDate = LocalDate.parse(startUseDateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                data.setStartUseDate(startUseDate);
            } catch (Exception e) {
                errorMsgList.add("开始使用日期格式错误，请使用yyyy-MM-dd或yyyy/M/d格式");
            }
        }
    }

    /**
     * 验证并转换枚举值为code
     */
    private void validateAndConvertEnumValues(AssetCardImportExcelDTO data, List<String> errorMsgList) {
        // 验证并转换计量单位
        String unitCode = UnitEnum.getCodeByName(data.getUnit());
        if (StrUtil.isBlank(unitCode)) {
            errorMsgList.add("计量单位【" + data.getUnit() + "】不存在");
        } else {
            data.setUnit(unitCode);
        }
        
        // 验证并转换资产类别
        String typeCode = AssetCategoryEnum.getCodeByName(data.getType());
        if (StrUtil.isBlank(typeCode)) {
            errorMsgList.add("资产类别【" + data.getType() + "】不存在");
        } else {
            data.setType(typeCode);
        }
        
        // 验证并转换资产状态
        String statusCode = AssetStatusEnum.getCodeByName(data.getStatus());
        if (StrUtil.isBlank(statusCode)) {
            errorMsgList.add("资产状态【" + data.getStatus() + "】不存在");
        } else {
            data.setStatus(statusCode);
        }
        
        // 验证并转换变动方式
        String changeMethodCode = ChangeMethodEnum.getCodeByName(data.getChangeMethod());
        if (StrUtil.isBlank(changeMethodCode)) {
            errorMsgList.add("变动方式【" + data.getChangeMethod() + "】不存在");
        } else {
            data.setChangeMethod(changeMethodCode);
        }
        
        // 验证并转换费用项目
        String costTypeCode = DepreciationChargeEnum.getCodeByName(data.getCostType());
        if (StrUtil.isBlank(costTypeCode)) {
            errorMsgList.add("费用项目【" + data.getCostType() + "】不存在");
        } else {
            data.setCostType(costTypeCode);
        }
    }

    /**
     * 验证并解析关联数据ID
     */
    private void validateAndResolveIds(AssetCardImportExcelDTO data, List<String> errorMsgList) {
        // 验证资产位置名称是否存在并解析资产位置ID
        String assetLocationId = getAssetLocationIdByName(data.getAssetLocationName());
        if (StrUtil.isBlank(assetLocationId)) {
            errorMsgList.add("资产位置【" + data.getAssetLocationName() + "】不存在");
        } else {
            data.setAssetLocationId(assetLocationId);
        }
        
        // 验证使用部门名称是否存在并解析部门ID
        String useDeptId = getDeptIdByName(data.getUseDeptName());
        if (StrUtil.isBlank(useDeptId)) {
            errorMsgList.add("使用部门【" + data.getUseDeptName() + "】不存在");
        } else {
            data.setUseDeptId(useDeptId);
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

    public List<AssetCardImportExcelDTO> getSuccessList() {
        return successList;
    }

    public List<AssetCardImportExcelDTO> getErrorList() {
        return errorList;
    }

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
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
