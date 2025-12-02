package com.erp.server.oms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolFeedbackExcelDTO;
import com.erp.model.oms.enums.FeedbackStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.KolFeedbackService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * KOL回片列表Excel导入监听器
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
public class KolFeedbackExcelListener extends AnalysisEventListener<KolFeedbackExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<KolFeedbackExcelDTO> successList = new ArrayList<>();
    private final List<KolFeedbackExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    // 缓存相关常量
    private static final String CACHE_SKU_NO_TO_ID = "kol_feedback:sku_no_to_id:";
    private static final String CACHE_SKU_ID_TO_PRODUCT_NAME = "kol_feedback:sku_id_to_product_name:";
    private static final int CACHE_EXPIRE_TIME = 300; // 五分钟

    private final KolFeedbackService kolFeedbackService = SpringUtil.getBean(KolFeedbackService.class);
    private final PlmTaskFeign plmTaskFeign = SpringUtil.getBean(PlmTaskFeign.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);
    private final RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);

    public KolFeedbackExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(KolFeedbackExcelDTO data, AnalysisContext context) {
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
                List<String> errorNoList = errorList.stream()
                        .map(KolFeedbackExcelDTO::getSourceCode)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                List<KolFeedbackExcelDTO> errorList2 = new ArrayList<>();
                kolFeedbackService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(
                        e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("KOL回片列表Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream()
                        .map(KolFeedbackExcelDTO::getSourceCode)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                List<KolFeedbackExcelDTO> errorList2 = new ArrayList<>();
                kolFeedbackService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(
                        e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 日期转换处理
     */
    private void convertDateFields(KolFeedbackExcelDTO data, List<String> errorMsgList) {
        // 发布日期转换
        String publishDateStr = data.getPublishDateStr();
        if (StringUtils.isNotBlank(publishDateStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDate publishDate = null;
                try {
                    publishDate = LocalDate.parse(publishDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e1) {
                    try {
                        publishDate = LocalDate.parse(publishDateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                data.setPublishDate(publishDate);
            } catch (Exception e) {
                errorMsgList.add("发布日期格式错误，请使用yyyy-MM-dd或yyyy/M/d格式");
            }
        }
    }

    /**
     * 数据校验和ID解析
     */
    private void validateAndResolveIds(KolFeedbackExcelDTO data, List<String> errorMsgList) {
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

        // 解析数量
        if (StringUtils.isNotBlank(data.getQtyStr())) {
            try {
                java.math.BigDecimal qty = new java.math.BigDecimal(data.getQtyStr());
                if (qty.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    errorMsgList.add("数量必须大于0");
                } else {
                    data.setQty(qty);
                }
            } catch (NumberFormatException e) {
                errorMsgList.add("数量格式错误：" + data.getQtyStr() + "，必须为数字");
            }
        } else {
            errorMsgList.add("数量不能为空");
        }

        // 验证回片状态
        if (StringUtils.isNotBlank(data.getFeedbackStatusStr())) {
            FeedbackStatusEnum statusEnum = FeedbackStatusEnum.getByCode(data.getFeedbackStatusStr());
            if (statusEnum == null) {
                // 尝试通过名称查找
                statusEnum = Arrays.stream(FeedbackStatusEnum.values())
                        .filter(e -> e.getName().equals(data.getFeedbackStatusStr()))
                        .findFirst()
                        .orElse(null);
            }
            if (statusEnum != null) {
                data.setFeedbackStatus(statusEnum.getCode());
            } else {
                errorMsgList.add("回片状态【" + data.getFeedbackStatusStr() + "】不存在");
            }
        } else {
            // 默认状态为待回片
            data.setFeedbackStatus(FeedbackStatusEnum.PENDING.getCode());
        }

        // URL哈希值计算
        if (StrUtil.isNotBlank(data.getUrl())) {
            String urlHash = cn.hutool.crypto.digest.DigestUtil.md5Hex(data.getUrl());
            data.setUrlHash(urlHash);
        }
    }

    /**
     * 设置创建人信息
     */
    private void setCreateUserInfo(KolFeedbackExcelDTO data) {
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

    public List<KolFeedbackExcelDTO> getSuccessList() {
        return successList;
    }

    public List<KolFeedbackExcelDTO> getErrorList() {
        return errorList;
    }

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
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
            if (CollectionUtils.isNotEmpty(skuVOS)) {
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
            List<com.erp.model.plm.vo.ProductVO.ProductPackVO> productPackBySkuIds = 
                    plmTaskFeign.getProductPackBySkuIds(Collections.singletonList(skuId));
            if (CollectionUtils.isNotEmpty(productPackBySkuIds)) {
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
}

