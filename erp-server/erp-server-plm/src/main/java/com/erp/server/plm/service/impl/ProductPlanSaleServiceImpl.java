package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.vo.LoginUser;
import com.erp.model.plm.entity.ProductPlanSaleEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProductPlanSaleMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductPlanSaleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:53
 */
@Service
public class ProductPlanSaleServiceImpl extends ServiceImpl<ProductPlanSaleMapper, ProductPlanSaleEntity>
        implements ProductPlanSaleService {

    @Resource
    private CommonService commonService;

    @Override
    public Boolean removeByProductPlanId(String productPlanId) {
        //获取当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        LambdaUpdateWrapper<ProductPlanSaleEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductPlanSaleEntity::getProductPlanId,productPlanId);
        updateWrapper.set(ProductPlanSaleEntity::getIsDeleted, Boolean.TRUE);
        updateWrapper.set(ProductPlanSaleEntity::getDeletedTime, LocalDateTime.now());
        updateWrapper.set(ProductPlanSaleEntity::getDeletedUserId,userInfo.getUid());
        return this.update(updateWrapper);
    }

    @Override
    public ProductPlanSaleEntity getByProductPlanId(String productPlanId) {
        LambdaQueryWrapper<ProductPlanSaleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanSaleEntity::getProductPlanId,productPlanId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}
