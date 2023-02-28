package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.ProductPlanSaleInfoEntity;
import com.erp.server.plm.mapper.ProductPlanSaleInfoMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductPlanSaleInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:54
 */
@Service
public class ProductPlanSaleInfoServiceImpl extends ServiceImpl<ProductPlanSaleInfoMapper, ProductPlanSaleInfoEntity>
        implements ProductPlanSaleInfoService {

    @Resource
    private CommonService commonService;

    @Override
    public Boolean removeByProductPlanId(String productPlanId) {
        //获取当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ServiceException(ApiError.ERROR_9011);
        }
        LambdaUpdateWrapper<ProductPlanSaleInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductPlanSaleInfoEntity::getProductPlanId,productPlanId);
        updateWrapper.set(ProductPlanSaleInfoEntity::getIsDeleted, Boolean.TRUE);
        updateWrapper.set(ProductPlanSaleInfoEntity::getDeletedTime, LocalDateTime.now());
        updateWrapper.set(ProductPlanSaleInfoEntity::getDeletedUserId,userInfo.getUid());
        return this.remove(updateWrapper);
    }

    @Override
    public List<ProductPlanSaleInfoEntity> listByProductPlanId(String productPlanId) {
        LambdaQueryWrapper<ProductPlanSaleInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanSaleInfoEntity::getProductPlanId,productPlanId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ProductPlanSaleInfoEntity> listByProductPlanIds(List<String> productPlanIds) {
        LambdaQueryWrapper<ProductPlanSaleInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductPlanSaleInfoEntity::getProductPlanId,productPlanIds);
        return this.list(queryWrapper);
    }
}
