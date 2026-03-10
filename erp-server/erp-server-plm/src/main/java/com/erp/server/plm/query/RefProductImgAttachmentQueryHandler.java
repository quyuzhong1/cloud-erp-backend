package com.erp.server.plm.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.plm.entity.ProductImgCategoryEntity;
import com.erp.server.plm.service.ProductImgCategoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 图片关联表高级查询Handler
 * @date 2025-12-30
 * @author wuhaotian
 */
@Component
public class RefProductImgAttachmentQueryHandler  extends AbstractQueryHandler {
    
    @Resource
    private ProductImgCategoryService productImgCategoryService;
    
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        // 处理分类查询：点击父级层级需要将子层级的所有图片查询出来
        if ("rpia.category_id".equals(field) && value != null && StrUtil.isNotBlank(value.toString())) {
            String categoryId = value.toString();
            // 获取该分类及其所有子分类的ID列表
            List<String> allCategoryIds = getAllChildCategoryIds(categoryId);
            if (CollUtil.isEmpty(allCategoryIds)) {
                // 如果没有找到分类，返回查询空结果的SQL
                return super.getQueryEmptySql();
            }
            // 使用父类方法构建 IN 查询条件，框架会自动从上下文中获取并拼接SQL
            super.buildSplicingSQLDTO("rpia.category_id", QueryConditionEnum.IN_LIST, allCategoryIds, QueryDataTypeEnum.STRING);
        }
        return null;
    }
    
    /**
     * 递归获取分类及其所有子分类的ID列表
     * @param categoryId 分类ID
     * @return 分类ID列表（包含自身及所有子分类）
     */
    private List<String> getAllChildCategoryIds(String categoryId) {
        List<String> allIds = new ArrayList<>();
        // 添加当前分类ID
        allIds.add(categoryId);
        // 递归获取所有子分类
        getAllChildCategoryIdsRecursive(categoryId, allIds);
        return allIds;
    }
    
    /**
     * 递归获取所有子分类ID
     * @param parentId 父分类ID
     * @param allIds 用于收集所有分类ID的列表
     */
    private void getAllChildCategoryIdsRecursive(String parentId, List<String> allIds) {
        // 查询当前分类的所有直接子分类
        LambdaQueryWrapper<ProductImgCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImgCategoryEntity::getParentId, parentId);
        List<ProductImgCategoryEntity> children = productImgCategoryService.list(queryWrapper);
        
        if (CollUtil.isNotEmpty(children)) {
            for (ProductImgCategoryEntity child : children) {
                // 添加子分类ID
                allIds.add(child.getId());
                // 递归查询子分类的子分类
                getAllChildCategoryIdsRecursive(child.getId(), allIds);
            }
        }
    }
}
