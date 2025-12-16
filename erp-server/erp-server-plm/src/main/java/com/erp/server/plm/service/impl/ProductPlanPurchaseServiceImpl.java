package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.ProductPlanPurchaseEntity;
import com.erp.server.plm.mapper.ProductPlanPurchaseMapper;
import com.erp.server.plm.service.ProductPlanPurchaseService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0

 * @date 2023/2/20 19:52
 */
@Service
public class ProductPlanPurchaseServiceImpl extends ServiceImpl<ProductPlanPurchaseMapper, ProductPlanPurchaseEntity>
        implements ProductPlanPurchaseService {
    @Override
    public Boolean removeByProductPlanId(String productPlanId) {
        //获取当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ServiceException(ApiError.AUTH_CREDENTIALS_INVALID);
        }
        LambdaUpdateWrapper<ProductPlanPurchaseEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductPlanPurchaseEntity::getProductPlanId,productPlanId);
        updateWrapper.set(ProductPlanPurchaseEntity::getIsDeleted, Boolean.TRUE);
       return this.remove(updateWrapper);
    }

    @Override
    public ProductPlanPurchaseEntity getByProductPlanId(String productPlanId) {
        LambdaQueryWrapper<ProductPlanPurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanPurchaseEntity::getProductPlanId,productPlanId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

}
