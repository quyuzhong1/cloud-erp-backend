package com.erp.server.fms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.UserTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.fms.dto.excel.AssetAcceptExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.fms.service.AssetAcceptService;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 资产验收表Excel导入监听器
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
public class AssetAcceptExcelListener extends AnalysisEventListener<AssetAcceptExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<AssetAcceptExcelDTO> successList = new ArrayList<>();
    private final List<AssetAcceptExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    public AssetAcceptExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    // 缓存相关常量
    private static final String CACHE_ORG_NAME_TO_ID = "asset_accept:org_name_to_id:";
    private static final String CACHE_USER_NAME_TO_ID = "asset_accept:user_name_to_id:";
    private static final String CACHE_DEPT_NAME_TO_ID = "asset_accept:dept_name_to_id:";
    private static final String CACHE_SKU_NO_TO_ID = "asset_accept:sku_no_to_id:";
    private static final String CACHE_SKU_ID_TO_PRODUCT_NAME = "asset_accept:sku_id_to_product_name:";
    private static final String CACHE_ASSET_LOCATION_NAME_TO_ID = "asset_accept:asset_location_name_to_id:";
    private static final int CACHE_EXPIRE_TIME = 300; // 五分钟

    private final AssetAcceptService assetAcceptService = SpringUtil.getBean(AssetAcceptService.class);
    private final SysUserFeign sysUserFeign = SpringUtil.getBean(SysUserFeign.class);
    private final RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
    private final PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);
    private final AssetLocationService assetLocationService = SpringUtil.getBean(AssetLocationService.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(AssetAcceptExcelDTO data, AnalysisContext context) {
        count++;
        data.setRowNum(count);
        
        // 已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount) {
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
        
        // 数据校验和ID解析
        validateAndResolveIds(data, errorMsgList);
        
        // 设置创建人信息
        setCreateUserInfo(data);
        
        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }
        
        successList.add(data);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(AssetAcceptExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<AssetAcceptExcelDTO> errorList2 = new ArrayList<>();
                assetAcceptService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
        log.info("资产验收表Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream().map(AssetAcceptExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<AssetAcceptExcelDTO> errorList2 = new ArrayList<>();
                assetAcceptService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
     * 日期转换处理
     */
    private void convertDateFields(AssetAcceptExcelDTO data, List<String> errorMsgList) {
        // 验收日期转换
        String acceptDateStr = data.getAcceptDateStr();
        if (StringUtils.isNotBlank(acceptDateStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDate acceptDate = null;
                try {
                    acceptDate = LocalDate.parse(acceptDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e1) {
                    try {
                        acceptDate = LocalDate.parse(acceptDateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                data.setAcceptDate(acceptDate);
            } catch (Exception e) {
                errorMsgList.add("验收日期格式错误，请使用yyyy-MM-dd或yyyy/M/d格式");
            }
        }
    }

    /**
     * 数据校验和ID解析
     */
    private void validateAndResolveIds(AssetAcceptExcelDTO data, List<String> errorMsgList) {
        
        // 设置来源类型为模具采购订单（默认值，导入时不需要填写）
        data.setSourceType(com.common.business.enums.SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
        
        // 验证验收组织名称是否存在并解析组织ID
        String acceptOrgId = getOrgIdByName(data.getAcceptOrgName());
        if (StrUtil.isBlank(acceptOrgId)) {
            errorMsgList.add("验收组织【" + data.getAcceptOrgName() + "】不存在");
        } else {
            data.setAcceptOrgId(acceptOrgId);
        }

        // 验证验收人名称是否存在并解析用户ID
        if (StringUtils.isNotBlank(data.getAcceptUserName())) {
            String acceptUserId = getUserIdByName(data.getAcceptUserName());
            if (StrUtil.isBlank(acceptUserId)) {
                errorMsgList.add("验收人【" + data.getAcceptUserName() + "】不存在");
            } else {
                data.setAcceptUserId(acceptUserId);
            }
        }

        // 验证验收部门名称是否存在并解析部门ID
        if (StringUtils.isNotBlank(data.getAcceptDeptName())) {
            String acceptDeptId = getDeptIdByName(data.getAcceptDeptName());
            if (StrUtil.isBlank(acceptDeptId)) {
                errorMsgList.add("验收部门【" + data.getAcceptDeptName() + "】不存在");
            } else {
                data.setAcceptDeptId(acceptDeptId);
            }
        }

        // 验证各种人员名称并解析ID
        validatePersonNames(data, errorMsgList);

        // 验证SKU是否存在并解析SKU ID和产品名称
        String skuId = getSkuIdBySkuNo(data.getSkuNo());
        if (StrUtil.isBlank(skuId)) {
            errorMsgList.add("SKU【" + data.getSkuNo() + "】不存在");
        } else {
            data.setSkuId(skuId);
            // 获取产品名称
            String productName = getProductNameBySkuId(skuId);
            if (StrUtil.isNotBlank(productName)) {
                data.setProductName(productName);
            }
        }

        // 解析验收数量
        if (StringUtils.isNotBlank(data.getAcceptQtyStr())) {
            try {
                Integer acceptQty = Integer.parseInt(data.getAcceptQtyStr());
                data.setAcceptQty(acceptQty);
            } catch (NumberFormatException e) {
                errorMsgList.add("验收数量格式错误：" + data.getAcceptQtyStr() + "，必须为正整数");
            }
        } else {
            data.setAcceptQty(0);
        }

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

        // 处理是否盖章字段
        if (StringUtils.isNotBlank(data.getIsNeedSealStr())) {
            String isNeedSealStr = data.getIsNeedSealStr().trim();
            if ("是".equals(isNeedSealStr) || "true".equalsIgnoreCase(isNeedSealStr) || "1".equals(isNeedSealStr)) {
                data.setIsNeedSeal(true);
            } else if ("否".equals(isNeedSealStr) || "false".equalsIgnoreCase(isNeedSealStr) || "0".equals(isNeedSealStr)) {
                data.setIsNeedSeal(false);
            } else {
                errorMsgList.add("是否盖章字段格式错误，请填写：是/否");
            }
        } else {
            data.setIsNeedSeal(false); // 默认值
        }
    }

    /**
     * 验证各种人员名称并解析ID
     */
    private void validatePersonNames(AssetAcceptExcelDTO data, List<String> errorMsgList) {
        // 采购开发
        if (StringUtils.isNotBlank(data.getPurchaseDevName())) {
            String purchaseDevId = getUserIdByName(data.getPurchaseDevName());
            if (StrUtil.isBlank(purchaseDevId)) {
                errorMsgList.add("采购开发【" + data.getPurchaseDevName() + "】不存在");
            } else {
                data.setPurchaseDevId(purchaseDevId);
            }
        }

        // 质量工程师
        if (StringUtils.isNotBlank(data.getQualityEngineerName())) {
            String qualityEngineerId = getUserIdByName(data.getQualityEngineerName());
            if (StrUtil.isBlank(qualityEngineerId)) {
                errorMsgList.add("质量工程师【" + data.getQualityEngineerName() + "】不存在");
            } else {
                data.setQualityEngineerId(qualityEngineerId);
            }
        }

        // 结构工程师
        if (StringUtils.isNotBlank(data.getStructureEngineerName())) {
            String structureEngineerId = getUserIdByName(data.getStructureEngineerName());
            if (StrUtil.isBlank(structureEngineerId)) {
                errorMsgList.add("结构工程师【" + data.getStructureEngineerName() + "】不存在");
            } else {
                data.setStructureEngineerId(structureEngineerId);
            }
        }

        // 产品经理
        if (StringUtils.isNotBlank(data.getProductManagerName())) {
            String productManagerId = getUserIdByName(data.getProductManagerName());
            if (StrUtil.isBlank(productManagerId)) {
                errorMsgList.add("产品经理【" + data.getProductManagerName() + "】不存在");
            } else {
                data.setProductManagerId(productManagerId);
            }
        }

        // 项目经理
        if (StringUtils.isNotBlank(data.getProjectManagerName())) {
            String projectManagerId = getUserIdByName(data.getProjectManagerName());
            if (StrUtil.isBlank(projectManagerId)) {
                errorMsgList.add("项目经理【" + data.getProjectManagerName() + "】不存在");
            } else {
                data.setProjectManagerId(projectManagerId);
            }
        }
    }

    /**
     * 设置创建人信息
     */
    private void setCreateUserInfo(AssetAcceptExcelDTO data) {
        try {
            LoginUser loginUser = UserContext.getLoginUser();
            if (loginUser != null) {
                data.setCreateUserId(loginUser.getUid());
                data.setCreateUserName(loginUser.getUserName());
            } else {
                // 如果获取不到当前用户，使用系统用户
                data.setCreateUserId("0");
                data.setCreateUserName("system");
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户信息失败，使用系统用户：{}", e.getMessage());
            data.setCreateUserId("0");
            data.setCreateUserName("system");
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

    public List<AssetAcceptExcelDTO> getSuccessList() {
        return successList;
    }

    public List<AssetAcceptExcelDTO> getErrorList() {
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

            if (!Objects.isNull(companyId) && StrUtil.isNotBlank(companyId.getId())) {
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
     * 根据用户名称查询用户ID（带缓存）
     */
    private String getUserIdByName(String userName) {
        if (StrUtil.isBlank(userName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_USER_NAME_TO_ID + userName;
        String userId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(userId)) {
            return userId;
        }

        try {
            // 调用用户服务根据名称查询内部用户信息
            List<com.common.business.dto.FindUserDTO> userList = sysUserFeign.listUserByUserNames(
                    Collections.singletonList(userName),
                    UserTypeEnum.ERP.code// 用户类型：1表示内部用户
            );

            if (CollUtil.isNotEmpty(userList)) {
                // 返回第一个匹配的用户ID
                String result = userList.get(0).getUserId();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到用户名称：{}", userName);
            return null;
        } catch (Exception e) {
            log.error("查询用户ID失败，用户名称：{}，错误：{}", userName, e.getMessage(), e);
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
     * 根据SKU编号查询SKU ID（带缓存）
     */
    private String getSkuIdBySkuNo(String skuNo) {
        if (StrUtil.isBlank(skuNo)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_SKU_NO_TO_ID + skuNo;
        String skuId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(skuId)) {
            return skuId;
        }

        try {
            // 调用PLM服务根据SKU编号查询SKU信息
            List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(Collections.singletonList(skuNo));
            if (CollectionUtils.isNotEmpty(skuVOS)){
                SkuVO skuVO = skuVOS.get(0);

                if (skuVO != null && StrUtil.isNotBlank(skuVO.getSkuId())) {
                    // 缓存结果
                    redissonClient.getBucket(cacheKey).set(skuVO.getSkuId(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                    return skuVO.getSkuId();
                }
            }

            log.warn("未找到SKU编号：{}", skuNo);
            return null;
        } catch (Exception e) {
            log.error("查询SKU ID失败，SKU编号：{}，错误：{}", skuNo, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据SKU ID查询产品名称（带缓存）
     */
    private String getProductNameBySkuId(String skuId) {
        if (StrUtil.isBlank(skuId)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_SKU_ID_TO_PRODUCT_NAME + skuId;
        String productName = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(productName)) {
            return productName;
        }

        try {
            // 调用PLM服务根据SKU ID查询产品信息
            List<com.erp.model.plm.vo.ProductVO.ProductPackVO> productPackBySkuIds = plmTaskFeign.getProductPackBySkuIds(Collections.singletonList(skuId));
            if (CollectionUtils.isNotEmpty(productPackBySkuIds)){
                com.erp.model.plm.vo.ProductVO.ProductPackVO productPackVO = productPackBySkuIds.get(0);
                if (productPackVO != null && StrUtil.isNotBlank(productPackVO.getProductName())) {
                    // 缓存结果
                    redissonClient.getBucket(cacheKey).set(productPackVO.getProductName(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                    return productPackVO.getProductName();
                }
            }
            log.warn("未找到产品信息，SKU ID：{}", skuId);
            return null;
        } catch (Exception e) {
            log.error("查询产品名称失败，SKU ID：{}，错误：{}", skuId, e.getMessage(), e);
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
