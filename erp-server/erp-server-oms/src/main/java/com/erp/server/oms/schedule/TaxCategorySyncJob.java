package com.erp.server.oms.schedule;

import cn.hutool.json.JSONUtil;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import com.sdk.third.tf.util.TaxCategoryUtil;
import com.erp.model.oms.entity.TaxCategoryEntity;
import com.erp.server.oms.service.TaxCategoryService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 同步税种数据
     * 定时任务：每天凌晨2点执行
     */
    @XxlJob("syncTaxCategory")
    public void syncTaxCategory() {
        XxlJobHelper.log("=====税种数据同步任务开始=====");
        long startTime = System.currentTimeMillis();
        
        try {
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
                tfFiscalService.getTaxCategoryList(currentPage, pageSize);
            
            if (firstPageResponse == null || firstPageResponse.getList() == null) {
                XxlJobHelper.log("税种列表为空，同步任务结束");
                return;
            }

            totalPages = firstPageResponse.getTotalPages() != null ? firstPageResponse.getTotalPages() : 1;
            totalCount = firstPageResponse.getTotal() != null ? firstPageResponse.getTotal() : 0;
            XxlJobHelper.log("税种总数：{}，总页数：{}", totalCount, totalPages);

            // 处理第一页数据
            int[] firstPageResult = processCategoryList(firstPageResponse.getList(), successCount, updateCount, skipCount, errorCount);
            successCount = firstPageResult[0];
            updateCount = firstPageResult[1];
            skipCount = firstPageResult[2];
            errorCount = firstPageResult[3];

            // 循环获取剩余页数据
            for (currentPage = 2; currentPage <= totalPages; currentPage++) {
                XxlJobHelper.log("查询税种列表，第{}页/共{}页", currentPage, totalPages);
                TaxCategoryDTO.CategoryListResponseDTO pageResponse = 
                    tfFiscalService.getTaxCategoryList(currentPage, pageSize);
                
                if (pageResponse == null || pageResponse.getList() == null || pageResponse.getList().isEmpty()) {
                    XxlJobHelper.log("第{}页数据为空，跳过", currentPage);
                    continue;
                }

                int[] result = processCategoryList(pageResponse.getList(), successCount, updateCount, skipCount, errorCount);
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
     * 处理税种列表
     * 
     * @param categoryList 税种列表
     * @param successCount 成功计数
     * @param updateCount 更新计数
     * @param skipCount 跳过计数
     * @param errorCount 错误计数
     * @return [成功数, 更新数, 跳过数, 错误数]
     */
    private int[] processCategoryList(List<TaxCategoryDTO.CategoryListItemDTO> categoryList, 
                                      int successCount, int updateCount, int skipCount, int errorCount) {
        for (TaxCategoryDTO.CategoryListItemDTO item : categoryList) {
            try {
                String categoryId = item.getCategoryId();
                XxlJobHelper.log("处理税种：categoryId={}, descricao={}", categoryId, item.getDescricao());

                // 查询税种详情
                TaxCategoryDTO.CategoryDetailDTO detail = tfFiscalService.getTaxCategoryDetail(categoryId);
                if (detail == null) {
                    XxlJobHelper.log("税种详情为空，跳过：categoryId={}", categoryId);
                    skipCount++;
                    continue;
                }

                // 查询本地数据
                TaxCategoryEntity localEntity = taxCategoryService.getByCategoryId(categoryId);

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
                    
                    taxCategoryService.save(localEntity);
                    XxlJobHelper.log("新增税种成功：categoryId={}", categoryId);
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
                        
                        taxCategoryService.updateById(localEntity);
                        XxlJobHelper.log("更新税种成功：categoryId={}", categoryId);
                        successCount++;
                        updateCount++;
                    } else {
                        // MD5一致，不需要更新
                        XxlJobHelper.log("税种数据未变化，跳过：categoryId={}", categoryId);
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
}
