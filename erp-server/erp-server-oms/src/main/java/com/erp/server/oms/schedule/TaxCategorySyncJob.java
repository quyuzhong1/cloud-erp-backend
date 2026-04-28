package com.erp.server.oms.schedule;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import com.sdk.third.tf.util.TaxCategoryUtil;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.erp.model.oms.entity.TaxCategoryEntity;
import com.erp.server.oms.service.CfgInvoiceSettingService;
import com.erp.server.oms.service.TaxCategoryService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 税种数据同步定时任务
 * 定期从第三方系统同步税种数据到本地数据库
 * 
 * @author system
 * @date 2025/01/XX
 */
@Component
@Slf4j
public class TaxCategorySyncJob {

    @Resource
    private TfFiscalService tfFiscalService;

    @Resource
    private TaxCategoryService taxCategoryService;

    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;

    /**
     * 同步税种数据
     * 定时任务：每天凌晨2点执行
     */
    @XxlJob("syncTaxCategory")
    public void syncTaxCategory() {
        XxlJobHelper.log("=====税种数据同步任务开始=====");
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取所有有效的公司配置列表（包含company_id和token）
            List<CfgInvoiceSettingEntity> companyList = getCompanyList();
            if (companyList.isEmpty()) {
                XxlJobHelper.log("未找到有效的公司配置，无法同步税种数据");
                log.error("未找到有效的公司配置，无法同步税种数据");
                return;
            }
            XxlJobHelper.log("找到{}个有效的公司配置，开始同步税种数据", companyList.size());

            // 使用第一个有效的公司配置进行同步（税种数据是全局的，用哪个token都一样）
            CfgInvoiceSettingEntity firstCompany = companyList.get(0);
            String companyId = firstCompany.getCompanyId();
            String companyToken = firstCompany.getToken();
            if (StrUtil.isBlank(companyToken)) {
                XxlJobHelper.log("公司token为空，无法同步税种数据");
                log.error("公司token为空，无法同步税种数据");
                return;
            }
            XxlJobHelper.log("使用公司ID: {}, token进行税种数据同步", companyId);

            // 第一步：查询税种列表，50个一页
            int pageSize = 50;
            int currentPage = 1;
            int totalPages = 1;
            int totalCount = 0;
            int successCount = 0;
            int updateCount = 0;
            int skipCount = 0;
            int errorCount = 0;

            // 先获取第一页，确定总页数
            XxlJobHelper.log("开始查询税种列表，第{}页，每页{}条", currentPage, pageSize);
            TaxCategoryDTO.CategoryListResponseDTO firstPageResponse = 
                tfFiscalService.getTaxCategoryList(currentPage, pageSize, companyToken);
            
            if (firstPageResponse == null || firstPageResponse.getList() == null) {
                XxlJobHelper.log("税种列表为空，同步任务结束");
                return;
            }

            totalPages = firstPageResponse.getTotalPages() != null ? firstPageResponse.getTotalPages() : 1;
            totalCount = firstPageResponse.getTotal() != null ? firstPageResponse.getTotal() : 0;
            XxlJobHelper.log("税种总数：{}，总页数：{}", totalCount, totalPages);

            // 处理第一页数据
            int[] firstPageResult = processCategoryList(firstPageResponse.getList(), successCount, updateCount, skipCount, errorCount, companyId, companyToken);
            successCount = firstPageResult[0];
            updateCount = firstPageResult[1];
            skipCount = firstPageResult[2];
            errorCount = firstPageResult[3];

            // 循环获取剩余页数据
            for (currentPage = 2; currentPage <= totalPages; currentPage++) {
                XxlJobHelper.log("查询税种列表，第{}页/共{}页", currentPage, totalPages);
                TaxCategoryDTO.CategoryListResponseDTO pageResponse = 
                    tfFiscalService.getTaxCategoryList(currentPage, pageSize, companyToken);
                
                if (pageResponse == null || pageResponse.getList() == null || pageResponse.getList().isEmpty()) {
                    XxlJobHelper.log("第{}页数据为空，跳过", currentPage);
                    continue;
                }

                int[] result = processCategoryList(pageResponse.getList(), successCount, updateCount, skipCount, errorCount, companyId, companyToken);
                successCount = result[0];
                updateCount = result[1];
                skipCount = result[2];
                errorCount = result[3];
            }

            long endTime = System.currentTimeMillis();
            XxlJobHelper.log("=====税种数据同步任务结束=====");
            XxlJobHelper.log("总耗时：{}ms", (endTime - startTime));
            XxlJobHelper.log("统计信息：总数={}，成功={}，更新={}，跳过={}，失败={}", 
                totalCount, successCount, updateCount, skipCount, errorCount);

        } catch (Exception e) {
            log.error("税种数据同步任务执行失败", e);
            XxlJobHelper.log("税种数据同步任务执行失败：{}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取所有有效的公司配置列表（包含company_id和token）
     * 用于调用税种接口（税种接口需要使用公司token）
     * 
     * @return 公司配置列表，如果未找到则返回空列表
     */
    private List<CfgInvoiceSettingEntity> getCompanyList() {
        try {
            // 查询所有有效的发票设置（有company_id、有token且未删除）
            List<CfgInvoiceSettingEntity> list = cfgInvoiceSettingService.list(
                new LambdaQueryWrapper<CfgInvoiceSettingEntity>()
                    .eq(CfgInvoiceSettingEntity::getIsDeleted, false)
                    .isNotNull(CfgInvoiceSettingEntity::getCompanyId)
                    .ne(CfgInvoiceSettingEntity::getCompanyId, "")
                    .isNotNull(CfgInvoiceSettingEntity::getToken)
                    .ne(CfgInvoiceSettingEntity::getToken, "")
            );
            
            if (list == null || list.isEmpty()) {
                log.warn("未找到有效的公司配置");
                return new ArrayList<>();
            }
            
            // 按company_id去重（保留第一个）
            Map<String, CfgInvoiceSettingEntity> companyMap = new HashMap<>();
            for (CfgInvoiceSettingEntity entity : list) {
                String companyId = entity.getCompanyId();
                if (StrUtil.isNotBlank(companyId) && !companyMap.containsKey(companyId)) {
                    companyMap.put(companyId, entity);
                }
            }
            
            List<CfgInvoiceSettingEntity> result = new ArrayList<>(companyMap.values());
            log.info("获取到{}个有效的公司配置（去重前：{}个公司）", result.size(), list.size());
            return result;
        } catch (Exception e) {
            log.error("获取公司配置列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 处理税种列表
     * 
     * @param categoryList 税种列表
     * @param successCount 成功计数
     * @param updateCount 更新计数
     * @param skipCount 跳过计数
     * @param errorCount 错误计数
     * @param companyId 公司ID（用于保存到税种记录）
     * @param companyToken 公司token（用于请求税种接口）
     * @return [成功数, 更新数, 跳过数, 错误数]
     */
    private int[] processCategoryList(List<TaxCategoryDTO.CategoryListItemDTO> categoryList, 
                                      int successCount, int updateCount, int skipCount, int errorCount,
                                      String companyId, String companyToken) {
        for (TaxCategoryDTO.CategoryListItemDTO item : categoryList) {
            try {
                String categoryId = item.getCategoryId();
                XxlJobHelper.log("处理税种：categoryId={}, descricao={}", categoryId, item.getDescricao());

                // 查询税种详情
                TaxCategoryDTO.CategoryDetailDTO detail = tfFiscalService.getTaxCategoryDetail(categoryId, companyToken);
                if (detail == null) {
                    XxlJobHelper.log("税种详情为空，跳过：categoryId={}", categoryId);
                    skipCount++;
                    continue;
                }

                // 查询本地数据（使用companyId避免多条记录问题）
                TaxCategoryEntity localEntity = taxCategoryService.getByCategoryIdAndCompanyId(categoryId, companyId);

                // 计算MD5
                String detailJsonStr = JSONUtil.toJsonStr(detail);
                String newMd5 = TaxCategoryUtil.calculateDataMd5(detailJsonStr);

                if (localEntity == null) {
                    // 新增
                    localEntity = new TaxCategoryEntity();
                    localEntity.setCategoryId(categoryId);
                    localEntity.setDescricao(detail.getDescricao());
                    localEntity.setCategoryDetail(detailJsonStr);
                    localEntity.setDataMd5(newMd5);
                    localEntity.setExternalUpdateTime(LocalDateTime.now());
                    localEntity.setDisabled(false); // 默认启用
                    localEntity.setCompanyId(companyId); // 保存公司ID，用于后续获取token
                    
                    taxCategoryService.save(localEntity);
                    XxlJobHelper.log("新增税种成功：categoryId={}, companyId={}", categoryId, companyId);
                    successCount++;
                    updateCount++;
                } else {
                    // 判断是否需要更新
                    String localMd5 = localEntity.getDataMd5();
                    if (TaxCategoryUtil.needUpdate(localMd5, detail)) {
                        // 需要更新
                        localEntity.setDescricao(detail.getDescricao());
                        localEntity.setCategoryDetail(detailJsonStr);
                        localEntity.setDataMd5(newMd5);
                        localEntity.setExternalUpdateTime(LocalDateTime.now());
                        // 更新公司ID（如果为空或不同）
                        if (StrUtil.isBlank(localEntity.getCompanyId()) || !localEntity.getCompanyId().equals(companyId)) {
                            localEntity.setCompanyId(companyId);
                        }
                        
                        taxCategoryService.updateById(localEntity);
                        XxlJobHelper.log("更新税种成功：categoryId={}, companyId={}", categoryId, companyId);
                        successCount++;
                        updateCount++;
                    } else {
                        // MD5一致，但可能需要更新company_id
                        if (StrUtil.isBlank(localEntity.getCompanyId()) || !localEntity.getCompanyId().equals(companyId)) {
                            localEntity.setCompanyId(companyId);
                            taxCategoryService.updateById(localEntity);
                            XxlJobHelper.log("更新税种company_id：categoryId={}, companyId={}", categoryId, companyId);
                        } else {
                            // MD5一致，不需要更新
                            XxlJobHelper.log("税种数据未变化，跳过：categoryId={}", categoryId);
                        }
                        skipCount++;
                    }
                }

            } catch (Exception e) {
                log.error("处理税种失败：categoryId={}", item.getCategoryId(), e);
                XxlJobHelper.log("处理税种失败：categoryId={}, 错误：{}", item.getCategoryId(), e.getMessage());
                errorCount++;
            }
        }

        return new int[]{successCount, updateCount, skipCount, errorCount};
    }


    /**
     * 初始化税种列表
     */
    @XxlJob("initTaxCategoryListJob")
    public ReturnT<String> initTaxCategoryListJob() {
        XxlJobHelper.log("=====初始化税种列表任务开始=====");
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> result = taxCategoryService.initTaxCategoryList();
            long duration = System.currentTimeMillis() - startTime;
            String resultJson = JSONUtil.toJsonStr(result);
            log.info("初始化税种列表任务执行完成，耗时：{}ms，结果：{}", duration, resultJson);
            XxlJobHelper.log("初始化税种列表任务执行完成，耗时：{}ms，结果：{}", duration, resultJson);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("初始化税种列表任务执行失败，耗时：{}ms", duration, e);
            XxlJobHelper.log("初始化税种列表任务执行失败，耗时：{}ms，错误：{}", duration, e.getMessage());
            return ReturnT.FAIL;
        }
    }
}
