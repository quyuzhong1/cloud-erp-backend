package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.serveice.SuperServiceImpl;
import com.erp.model.scm.entity.SupplierVisitSkuEntity;
import com.erp.server.scm.mapper.SupplierVisitSkuMapper;
import com.erp.server.scm.service.SupplierVisitSkuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 供应商拜访物料信息表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Service
public class SupplierVisitSkuServiceImpl extends SuperServiceImpl<SupplierVisitSkuMapper, SupplierVisitSkuEntity> implements SupplierVisitSkuService {


    /**
     * 获取到skuId集合
     *
     * @param visitIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-21 11:51
     */
    @Override
    public List<SupplierVisitSkuEntity> getByVisitIds(List<String> visitIds) {
        if (CollectionUtils.isEmpty(visitIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SupplierVisitSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SupplierVisitSkuEntity::getSupplierVisitId, visitIds);
        return this.list(queryWrapper);
    }
}
