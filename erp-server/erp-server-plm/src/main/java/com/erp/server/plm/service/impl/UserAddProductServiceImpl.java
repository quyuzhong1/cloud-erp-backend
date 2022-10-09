package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.entity.UserAddProductEntity;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.UserAddProductEntityMapper;
import com.erp.server.plm.service.UserAddProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class UserAddProductServiceImpl extends ServiceImpl<UserAddProductEntityMapper, UserAddProductEntity>
        implements UserAddProductService {


    /**
     * 收藏产品
     *
     * @param productId
     * @return boolean
     * @author yl
     * @date 2022-10-09 19:05
     */
    @Override
    public boolean userAddProduct(String productId) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (loginUser != null) {
            UserAddProductEntity entity = new UserAddProductEntity();
            entity.setProductId(productId);
            entity.setUserId(loginUser.getUid());
            return this.save(entity);
        }
        return false;
    }

    /**
     * 取消收藏产品
     *
     * @param productId
     * @return boolean
     * @author yl
     * @date 2022-10-09 19:09
     */
    @Override
    public boolean userCancelProduct(String productId) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (loginUser != null) {
            LambdaQueryWrapper<UserAddProductEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddProductEntity::getProductId, productId);
            queryWrapper.eq(UserAddProductEntity::getUserId, loginUser.getUid());
            return this.remove(queryWrapper);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> listByUserId() {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();

        return baseMapper.listByUserId(loginUser.getUid());
    }
}




