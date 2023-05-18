package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.ProductPlanRemarkEntity;
import com.erp.server.plm.mapper.ProductPlanningRemarkMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductPlanRemarkService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:54
 */
@Service
public class ProductPlanRemarkServiceImpl extends ServiceImpl<ProductPlanningRemarkMapper, ProductPlanRemarkEntity>
        implements ProductPlanRemarkService {

    @Resource
    private CommonService commonService;

    @Override
    public Boolean removeByProductPlanId(String productPlanId) {
        //获取当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        LambdaUpdateWrapper<ProductPlanRemarkEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductPlanRemarkEntity::getProductPlanId,productPlanId);
        updateWrapper.set(ProductPlanRemarkEntity::getIsDeleted, Boolean.TRUE);
        return this.remove(updateWrapper);
    }

    @Override
    public List<ProductPlanRemarkEntity> listByProductPlanId(String productPlanId) {
        LambdaQueryWrapper<ProductPlanRemarkEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPlanRemarkEntity::getProductPlanId,productPlanId);
        return this.list(queryWrapper);
    }
}
