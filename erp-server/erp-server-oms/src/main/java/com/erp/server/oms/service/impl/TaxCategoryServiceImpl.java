package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.erp.model.oms.entity.TaxCategoryEntity;
import com.erp.server.oms.mapper.TaxCategoryMapper;
import com.erp.server.oms.service.CfgInvoiceSettingService;
import com.erp.server.oms.service.TaxCategoryService;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import com.sdk.third.tf.util.TaxCategoryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 税种基础数据服务实现类
 *
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
@Service
public class TaxCategoryServiceImpl extends SuperServiceImpl<TaxCategoryMapper, TaxCategoryEntity> implements TaxCategoryService {

    @Resource
    private TfFiscalService tfFiscalService;

    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;

    @Override
    public TaxCategoryEntity getByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<TaxCategoryEntity>()
                .eq(TaxCategoryEntity::getCategoryId, categoryId)
                .eq(TaxCategoryEntity::getIsDeleted, false));
    }

    /**
     * 初始化税种列表
     * 从第三方系统获取税种列表并初始化到cfg_tax_category表
     * 复用定时任务的逻辑
     * 
     * @return 初始化结果信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> initTaxCategoryList() {
        Map<String, Object> result = new HashMap<>();
        int totalCount = 0;
        int successCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        int errorCount = 0;
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("=====开始初始化税种列表=====");
            
            // 获取所有有效的公司配置列表（包含company_id和token）
            List<CfgInvoiceSettingEntity> companyList = getCompanyList();
            if (companyList.isEmpty()) {
                log.warn("未找到有效的公司配置，无法初始化税种数据");
                result.put("totalCount", 0);
                result.put("successCount", 0);
                result.put("updateCount", 0);
                result.put("skipCount", 0);
                result.put("errorCount", 0);
                result.put("message", "未找到有效的公司配置");
                return result;
            }
            log.info("找到{}个有效的公司配置，开始初始化税种数据", companyList.size());

            // 使用第一个有效的公司配置进行同步（税种数据是全局的，用哪个token都一样）
            CfgInvoiceSettingEntity firstCompany = companyList.get(0);
            String companyId = firstCompany.getCompanyId();
            String companyToken = firstCompany.getToken();
            if (StrUtil.isBlank(companyToken)) {
                log.warn("公司token为空，无法初始化税种数据");
                result.put("totalCount", 0);
                result.put("successCount", 0);
                result.put("updateCount", 0);
                result.put("skipCount", 0);
                result.put("errorCount", 0);
                result.put("message", "公司token为空");
                return result;
            }
            log.info("使用公司ID: {}, token进行税种数据初始化", companyId);

            // 第一步：查询税种列表，50个一页
            int pageSize = 50;
            int currentPage = 1;
            int totalPages = 1;

            // 先获取第一页，确定总页数
            log.info("开始查询税种列表，第{}页，每页{}条", currentPage, pageSize);
            TaxCategoryDTO.CategoryListResponseDTO firstPageResponse = 
                tfFiscalService.getTaxCategoryList(currentPage, pageSize, companyToken);
            
            if (firstPageResponse == null || CollUtil.isEmpty(firstPageResponse.getList())) {
                log.warn("税种列表为空，初始化任务结束");
                result.put("totalCount", 0);
                result.put("successCount", 0);
                result.put("updateCount", 0);
                result.put("skipCount", 0);
                result.put("errorCount", 0);
                result.put("message", "税种列表为空");
                return result;
            }

            totalPages = firstPageResponse.getTotalPages() != null ? firstPageResponse.getTotalPages() : 1;
            totalCount = firstPageResponse.getTotal() != null ? firstPageResponse.getTotal() : 0;
            log.info("税种总数：{}，总页数：{}", totalCount, totalPages);

            // 处理第一页数据
            int[] firstPageResult = processCategoryList(firstPageResponse.getList(), successCount, updateCount, skipCount, errorCount, companyId, companyToken);
            successCount = firstPageResult[0];
            updateCount = firstPageResult[1];
            skipCount = firstPageResult[2];
            errorCount = firstPageResult[3];

            // 循环获取剩余页数据
            for (currentPage = 2; currentPage <= totalPages; currentPage++) {
                log.info("查询税种列表，第{}页/共{}页", currentPage, totalPages);
                TaxCategoryDTO.CategoryListResponseDTO pageResponse = 
                    tfFiscalService.getTaxCategoryList(currentPage, pageSize, companyToken);
                
                if (pageResponse == null || CollUtil.isEmpty(pageResponse.getList())) {
                    log.warn("第{}页数据为空，跳过", currentPage);
                    continue;
                }

                int[] pageResult = processCategoryList(pageResponse.getList(), successCount, updateCount, skipCount, errorCount, companyId, companyToken);
                successCount = pageResult[0];
                updateCount = pageResult[1];
                skipCount = pageResult[2];
                errorCount = pageResult[3];
            }

            long endTime = System.currentTimeMillis();
            log.info("=====税种列表初始化完成=====");
            log.info("总耗时：{}ms，总数={}，成功={}，更新={}，跳过={}，失败={}", 
                (endTime - startTime), totalCount, successCount, updateCount, skipCount, errorCount);

            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("updateCount", updateCount);
            result.put("skipCount", skipCount);
            result.put("errorCount", errorCount);
            result.put("message", "初始化完成");
            result.put("duration", endTime - startTime);

        } catch (Exception e) {
            log.error("初始化税种列表失败", e);
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("updateCount", updateCount);
            result.put("skipCount", skipCount);
            result.put("errorCount", errorCount);
            result.put("message", "初始化失败：" + e.getMessage());
            throw new com.common.core.exception.ServiceException("初始化税种列表失败：" + e.getMessage(), e);
        }
        
        return result;
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
     * 复用定时任务的逻辑
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
                log.info("处理税种：categoryId={}, descricao={}", categoryId, item.getDescricao());

                // 查询税种详情
                TaxCategoryDTO.CategoryDetailDTO detail = tfFiscalService.getTaxCategoryDetail(categoryId, companyToken);
                if (detail == null) {
                    log.warn("税种详情为空，跳过：categoryId={}", categoryId);
                    skipCount++;
                    continue;
                }

                // 查询本地数据
                TaxCategoryEntity localEntity = this.getByCategoryId(categoryId);

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
                    
                    this.save(localEntity);
                    log.info("新增税种成功：categoryId={}, companyId={}", categoryId, companyId);
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
                        
                        this.updateById(localEntity);
                        log.info("更新税种成功：categoryId={}, companyId={}", categoryId, companyId);
                        successCount++;
                        updateCount++;
                    } else {
                        // MD5一致，但可能需要更新company_id
                        if (StrUtil.isBlank(localEntity.getCompanyId()) || !localEntity.getCompanyId().equals(companyId)) {
                            localEntity.setCompanyId(companyId);
                            this.updateById(localEntity);
                            log.info("更新税种company_id：categoryId={}, companyId={}", categoryId, companyId);
                        } else {
                            // MD5一致，不需要更新
                            log.info("税种数据未变化，跳过：categoryId={}", categoryId);
                        }
                        skipCount++;
                    }
                }

            } catch (Exception e) {
                log.error("处理税种失败：categoryId={}", item.getCategoryId(), e);
                errorCount++;
            }
        }

        return new int[]{successCount, updateCount, skipCount, errorCount};
    }

    /**
     * 根据公司ID查询税种列表
     * 查询指定公司关联的税种数据
     * 
     * @param companyId 公司ID（cfg_invoice_setting.company_id）
     * @return 税种列表
     */
    @Override
    public List<TaxCategoryEntity> getByCompanyId(String companyId) {
        if (StrUtil.isBlank(companyId)) {
            log.warn("公司ID为空，返回空列表");
            return new ArrayList<>();
        }
        
        List<TaxCategoryEntity> list = this.list(
            new LambdaQueryWrapper<TaxCategoryEntity>()
                .eq(TaxCategoryEntity::getCompanyId, companyId)
                .eq(TaxCategoryEntity::getIsDeleted, false)
                .orderByAsc(TaxCategoryEntity::getCategoryId)
        );
        
        log.info("根据公司ID查询税种列表：companyId={}, 数量={}", companyId, list.size());
        return list;
    }
}
