package com.erp.server.wms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.UserTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.excel.SampleRecipientExcelDTO;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.erp.model.wms.entity.SampleRecipientDetailEntity;
import com.erp.model.wms.enums.SampleRecipientExecStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.SampleRecipientService;
import com.erp.server.wms.service.SampleRecipientDetailService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.model.wms.dto.WarehouseDTO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 样品领用单Excel导入监听器
 * @author wuhaotian
 * @since 2025-08-22
 */
@Slf4j
public class SampleRecipientExcelListener extends AnalysisEventListener<SampleRecipientExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<SampleRecipientExcelDTO> successList = new ArrayList<>();
    private final List<SampleRecipientExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    public SampleRecipientExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }
    // 缓存相关常量
    private static final String CACHE_ORG_NAME_TO_ID = "sample_recipient:org_name_to_id:";
    private static final String CACHE_WAREHOUSE_NAME_TO_ID = "sample_recipient:warehouse_name_to_id:";
    private static final String CACHE_USER_NAME_TO_ID = "sample_recipient:user_name_to_id:";
    private static final String CACHE_DEPT_NAME_TO_ID = "sample_recipient:dept_name_to_id:";
    private static final String CACHE_SKU_NO_TO_ID = "sample_recipient:sku_no_to_id:";
    private static final String CACHE_SKU_ID_TO_PRODUCT_NAME = "sample_recipient:sku_id_to_product_name:";
    private static final int CACHE_EXPIRE_TIME = 300; // 五分钟

    private final  SampleRecipientService sampleRecipientService= SpringUtil.getBean(SampleRecipientService.class);

    private final  SampleRecipientDetailService sampleRecipientDetailService=SpringUtil.getBean(SampleRecipientDetailService.class);

    private final  SysUserFeign sysUserFeign=SpringUtil.getBean(SysUserFeign.class);

    private final  RedissonClient redissonClient=SpringUtil.getBean(RedissonClient.class);

    private final  DocNoGenHelper docNoGenHelper=SpringUtil.getBean(DocNoGenHelper.class);

    private final  WarehouseService warehouseService=SpringUtil.getBean(WarehouseService.class);

    private final  PlmTaskFeign plmTaskFeign=SpringUtil.getBean(PlmTaskFeign.class);

    private final  DownloadTaskFeign downloadTaskFeign=SpringUtil.getBean(DownloadTaskFeign.class);



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleRecipientExcelDTO data, AnalysisContext context) {
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
        
        // 数据校验和ID解析
        validateAndResolveIds(data, errorMsgList);
        
        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }
        
        successList.add(data);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(SampleRecipientExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<SampleRecipientExcelDTO> errorList2 = new ArrayList<>();
                sampleRecipientService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
        log.info("样品领用单Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream().map(SampleRecipientExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<SampleRecipientExcelDTO> errorList2 = new ArrayList<>();
                sampleRecipientService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
     * 数据校验和ID解析
     */
    private void validateAndResolveIds(SampleRecipientExcelDTO data, List<String> errorMsgList) {
        // 必填字段校验
        if (data.getRecipientDate() == null) {
            errorMsgList.add("领用日期不能为空");
        }
        
        if (data.getUsage() == null || data.getUsage().trim().isEmpty()) {
            errorMsgList.add("用途不能为空");
        }
        
        if (data.getWarehouseName() == null || data.getWarehouseName().trim().isEmpty()) {
            errorMsgList.add("发货仓库不能为空");
        } else {
            // 验证仓库名称是否存在并解析仓库ID
            String warehouseId = getWarehouseIdByName(data.getWarehouseName());
            if (StrUtil.isBlank(warehouseId)) {
                errorMsgList.add("发货仓库【" + data.getWarehouseName() + "】不存在");
            } else {
                data.setWarehouseId(warehouseId);
            }
        }
        
        if (data.getUserName() == null || data.getUserName().trim().isEmpty()) {
            errorMsgList.add("领用人不能为空");
        } else {
            // 验证用户名称是否存在并解析用户ID
            String userId = getUserIdByName(data.getUserName());
            if (StrUtil.isBlank(userId)) {
                errorMsgList.add("领用人【" + data.getUserName() + "】不存在");
            } else {
                data.setUserId(userId);
            }
        }
        
        if (data.getDeptName() == null || data.getDeptName().trim().isEmpty()) {
            errorMsgList.add("领用部门不能为空");
        } else {
            // 验证部门名称是否存在并解析部门ID
            String deptId = getDeptIdByName(data.getDeptName());
            if (StrUtil.isBlank(deptId)) {
                errorMsgList.add("领用部门【" + data.getDeptName() + "】不存在");
            } else {
                data.setDeptId(deptId);
            }
        }
        
        if (data.getPickOrgName() == null || data.getPickOrgName().trim().isEmpty()) {
            errorMsgList.add("领料组织不能为空");
        } else {
            // 验证组织名称是否存在并解析组织ID
            String orgId = getOrgIdByName(data.getPickOrgName());
            if (StrUtil.isBlank(orgId)) {
                errorMsgList.add("领料组织【" + data.getPickOrgName() + "】不存在");
            } else {
                data.setPickOrgId(orgId);
            }
        }
        
        if (data.getUsageScope() == null || data.getUsageScope().trim().isEmpty()) {
            errorMsgList.add("使用范围不能为空");
        }
        
        if (data.getSkuNo() == null || data.getSkuNo().trim().isEmpty()) {
            errorMsgList.add("SKU不能为空");
        } else {
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
        }
        
        if (data.getRecipientQty() == null || data.getRecipientQty() <= 0) {
            errorMsgList.add("领用数量必须大于0");
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
     * 根据仓库名称查询仓库ID（带缓存）
     */
    private String getWarehouseIdByName(String warehouseName) {
        if (StrUtil.isBlank(warehouseName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_WAREHOUSE_NAME_TO_ID + warehouseName;
        String warehouseId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(warehouseId)) {
            return warehouseId;
        }

        try {
            // 调用仓库服务根据名称查询仓库信息
            List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listWarehouseByParams(
                    WarehouseDTO.ListParamDTO.builder()
                            .warehouseName(warehouseName)
                            .showByAuth(false)
                            .build()
            );

            if (CollUtil.isNotEmpty(warehouseList)) {
                // 返回第一个匹配的仓库ID
                String result = warehouseList.get(0).getId();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到仓库名称：{}", warehouseName);
            return null;
        } catch (Exception e) {
            log.error("查询仓库ID失败，仓库名称：{}，错误：{}", warehouseName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据用户名称查询用户ID（带缓存）
     * 优先查询内部用户，如果未找到则查询外部使用人字典
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
            // 优先调用用户服务根据名称查询内部用户信息
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

            // 如果内部用户未找到，尝试查询外部使用人字典
            log.info("内部用户未找到，尝试查询外部使用人字典：{}", userName);
            
            // 这里可以根据需要调用SysDictFeign.getByIds进行外部使用人查询
            // 由于getByIds需要ID列表，而这里只有名称，需要先通过名称查询
            // 暂时保持原有逻辑，后续可以根据具体需求调整
            
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
            List<ProductVO.ProductPackVO> productPackBySkuIds = plmTaskFeign.getProductPackBySkuIds(Collections.singletonList(skuId));
            if (CollectionUtils.isNotEmpty(productPackBySkuIds)){
                ProductVO.ProductPackVO productPackVO = productPackBySkuIds.get(0);
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
}
