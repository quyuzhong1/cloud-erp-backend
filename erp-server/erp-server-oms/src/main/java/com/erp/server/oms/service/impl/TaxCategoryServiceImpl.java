package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.oms.entity.TaxCategoryEntity;
import com.erp.server.oms.mapper.TaxCategoryMapper;
import com.erp.server.oms.service.TaxCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 税种基础数据服务实现类
 *
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
@Service
public class TaxCategoryServiceImpl extends SuperServiceImpl<TaxCategoryMapper, TaxCategoryEntity> implements TaxCategoryService {

    @Override
    public TaxCategoryEntity getByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<TaxCategoryEntity>()
                .eq(TaxCategoryEntity::getCategoryId, categoryId)
                .eq(TaxCategoryEntity::getIsDeleted, false));
    }
}
