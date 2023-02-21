package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.common.business.vo.LoginUser;
import com.erp.model.plm.entity.ProductPlanPurchaseEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProductPlanPurchaseMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductPlanPurchaseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:52
 */
@Service
public class ProductPlanPurchaseServiceImpl extends ServiceImpl<ProductPlanPurchaseMapper, ProductPlanPurchaseEntity>
        implements ProductPlanPurchaseService {
    @Resource
    private CommonService commonService;

    @Override
    public Boolean removeByProductPlanId(String productPlanId) {
        //获取当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        LambdaUpdateWrapper<ProductPlanPurchaseEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductPlanPurchaseEntity::getProductPlanId,productPlanId);
        updateWrapper.set(ProductPlanPurchaseEntity::getIsDeleted, IsConstant.YES);
        updateWrapper.set(ProductPlanPurchaseEntity::getDeletedTime, LocalDateTime.now());
        updateWrapper.set(ProductPlanPurchaseEntity::getDeletedUserId,userInfo.getUid());
        updateWrapper.set(ProductPlanPurchaseEntity::getDeletedUserName,userInfo.getUserName());
       return this.update(updateWrapper);
    }

    @Override
    public ProductPlanPurchaseEntity getByProductPlanId(String productPlanId) {
        LambdaQueryWrapper<ProductPlanPurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanPurchaseEntity::getProductPlanId,productPlanId);
        queryWrapper.eq(ProductPlanPurchaseEntity::getIsDeleted,IsConstant.NO);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

}
